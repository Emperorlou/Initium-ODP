package com.universeprojects.miniup.server.services;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.AbortTransactionException;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.InitiumTransaction;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global Buffs and all associated methods, including hooks
 */
public class RevenueService extends Service{
    public RevenueService(ODPDBAccess db) {
        super(db);
    }

    /**
     * Purchase a buff, given a user and a buff.
     * @param user the user who is activating this buff
     * @param globalBuff the buff they are activating
     * @throws AbortTransactionException
     * @throws UserErrorMessage
     */
    public void incrementGlobalBuff(CachedEntity user, CachedEntity globalBuff) throws AbortTransactionException, UserErrorMessage {
        Boolean halted = new InitiumTransaction<Boolean>(db.getDB()) {
            @Override
            public Boolean doTransaction(CachedDatastoreService ds) throws AbortTransactionException {

                //I believe this is necessary
                user.refetch(ds);
                globalBuff.refetch(ds);

                //calculate the cost of the buff.
                Long playerDonations = (Long) user.getProperty("totalDonations");
                Long cost = (Long) globalBuff.getProperty("cost");

                //If the cost is greater than they are able to afford, cancel the operation.
                if(cost > playerDonations) return false;

                user.setProperty("totalDonations", playerDonations - cost);

                //at this point the purchase is validated.

                List<EmbeddedEntity> purchases = getPurchases(globalBuff);
                Long minutes = (Long) globalBuff.getProperty("minutes");
                if(minutes == null || minutes == 0)
                    throw new IllegalArgumentException("Minutes cannot be null or 0.");

                //Generate a date at the given expiry time.
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.MINUTE, Math.toIntExact(minutes));

                //Generate the new purchase embedded entity
                EmbeddedEntity latest = new EmbeddedEntity();
                latest.setProperty("buyerKey", user.getKey());
                latest.setProperty("buyerName", db.getCurrentCharacter().getProperty("name"));
                latest.setProperty("expiryDate", cal.getTime());

                purchases.add(latest);
                globalBuff.setProperty("purchases", purchases);

                ds.put(user, globalBuff);

                return true;
            }
        }.run();

        if(!halted) throw new UserErrorMessage("You can't afford that!");
    }

    /**
     * Given a key, returns the global buff rate for that key. This should be an NPCDef or an ItemDef, but could
     * theoretically be anything.
     * @param definitionKey the key that we are applying the buff to
     * @return the % increase to apply to the target key. 0 is the default.
     */
    public Long computeBuffForDefinition(Key definitionKey) {
        Key buffKey = db.createKey("GlobalBuff", definitionKey.toString());

        CachedEntity globalBuff = null;

        try{
             globalBuff = db.getDB().get(buffKey);
        }
        //no entity? return 0
        catch(EntityNotFoundException e){
            return 0L;
        }

        //check to see if the buff is actually active. if not, return 0
        if(globalBuff == null || !isBuffActive(globalBuff) || !isBuffEnabled(globalBuff))
            return 0L;

        //calculate
        return computeBuff(globalBuff);

    }

    /**
     * Compute the buff's value, given all of the purchases.
      * @param globalBuff the buff we are operating on
     * @return the % increase that this buff applies
     */
    public Long computeBuff(CachedEntity globalBuff){

        Long baseValue = (Long) globalBuff.getProperty("baseValue");

        return baseValue*getActivePurchases(globalBuff).size();
    }

    /**
     * Find the latest date out of a set of the expiry dates.
     * @param globalBuff the buff we are operating on
     * @return the date at which this buff will no longer be active.
     */
    public Date getExpiryDate(CachedEntity globalBuff){
        List<EmbeddedEntity> purchases = getSortedActivePurchases(globalBuff);

        EmbeddedEntity latest = purchases.get(purchases.size() -1);

        return (Date) latest.getProperty("expiryDate");
    }

    /**
     * Find the earliest date out of a set of dates
     * @param globalBuff the buff we are operating on
     * @return the date at which this buff will DECREMENT (ie: one purchase has run out but another is active)
     */
    public Date getDecrementDate(CachedEntity globalBuff){
        List<EmbeddedEntity> purchases = getSortedActivePurchases(globalBuff);

        EmbeddedEntity earliest = purchases.get(0);

        return (Date) earliest.getProperty("expiryDate");
    }

    /**
     * @param globalBuff the buff we are operating on
     * @return true if the buff is active right now
     */
    public boolean isBuffActive(CachedEntity globalBuff){
        Date currentDate = new Date();

        for(EmbeddedEntity ee : getPurchases(globalBuff)){
            Date expiryDate = (Date) ee.getProperty("expiryDate");

            //If the current date is BEFORE the expiry date, we return true.
            if(currentDate.before(expiryDate))
                return true;
        }

        //if none of them are active, return false.
        return false;
    }

    /**
     * Are we showing this buff as an option to the player?
     * @param globalBuff the buff we are operating on
     * @return true if the buff is enabled
     */
    public boolean isBuffEnabled(CachedEntity globalBuff){
        return (Boolean) globalBuff.getProperty("enabled");
    }

    /**
     * @param globalBuff the buff we are operating on
     * @return a sorted list of all active purchases for this buff. earliest -> latest
     */
    public List<EmbeddedEntity> getSortedActivePurchases(CachedEntity globalBuff){
        List<EmbeddedEntity> purchases = getActivePurchases(globalBuff);

        //I think this is correct??? TODO
        purchases.sort((d1, d2) -> {
            Date date1 = (Date) d1.getProperty("expiryDate");
            Date date2 = (Date) d2.getProperty("expiryDate");

            return date1.compareTo(date2);
        });

        return purchases;
    }

    /**
     * @param globalBuff the buff we are operating on
     * @return an unsorted list of all the active purchases for this buff.
     */
    public List<EmbeddedEntity> getActivePurchases(CachedEntity globalBuff){
        List<EmbeddedEntity> purchases = getPurchases(globalBuff);

        //TODO im not sure if initium is on java 8, lol. I hope this implementation works
        return purchases
                .stream()
                .filter(p -> false == isExpired((Date) p.getProperty("expiryDate")))
                .collect(Collectors.toList());
    }

    /**
     * @param globalBuff the buff we are operating on
     * @return all purchases for this buff. There could theoretically be a LOT of these dudes.
     */
    private List<EmbeddedEntity> getPurchases(CachedEntity globalBuff){
        List<EmbeddedEntity> purchases = (List<EmbeddedEntity>) globalBuff.getProperty("purchases");
        if(purchases == null)
            purchases = new ArrayList<>();

        return purchases;
    }

    /**
     * @param expiryDate the given date
     * @return true is the given date is BEFORE the current date.
     */
    public boolean isExpired(Date expiryDate){
        return expiryDate.before(new Date());
    }
}
