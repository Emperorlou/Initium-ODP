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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Global Buffs and all associated methods, including hooks
 */
public class RevenueService extends Service{
    public RevenueService(ODPDBAccess db) {
        super(db);
    }

    /**
     * Purchaes another buff
     * @param user
     * @param globalBuff
     * @throws AbortTransactionException
     */
    public void incrementGlobalBuff(CachedEntity user, CachedEntity globalBuff) throws AbortTransactionException, UserErrorMessage {
        Boolean halted = new InitiumTransaction<Boolean>(db.getDB()) {
            @Override
            public Boolean doTransaction(CachedDatastoreService ds) throws AbortTransactionException {

                Long playerDonations = (Long) user.getProperty("totalDonations");
                Long cost = (Long) globalBuff.getProperty("cost");

                //If the cost is greater than they are able to afford, cancel the operation.
                if(cost > playerDonations) return false;

                user.setProperty("totalDonations", playerDonations - cost);
                //at this point the purchase is validated.

                List<EmbeddedEntity> purchases = getPurchases(globalBuff);
                Long minutes = (Long) globalBuff.getProperty("minutes");
                if(minutes == null || minutes == 0) throw new IllegalArgumentException("Minutes cannot be null or 0.");

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
     * @param definitionKey
     * @return the % increase to apply to the target key. 0 is the default.
     */
    public Long computeBuffForDefinition(Key definitionKey) {
        String keyString = definitionKey.getKind() + "(" + definitionKey.getId() + ")";
        Key buffKey = db.createKey("GlobalBuff", keyString);

        CachedEntity globalBuff = null;

        try{
             globalBuff = db.getDB().get(buffKey);
        }
        //no entity? return 0
        catch(EntityNotFoundException e){
            return 0L;
        }

        //check to see if the buff is actually active. if not, return 0
        if(globalBuff == null || !isBuffEnabled(globalBuff) || !isBuffActive(globalBuff))
            return 0L;

        //calculate
        return computeBuff(globalBuff);

    }

    /**
     * Compute the buff's value, given all of the purchases.
      * @param globalBuff
     * @return
     */
    public Long computeBuff(CachedEntity globalBuff){

        Long baseValue = (Long) globalBuff.getProperty("baseValue");

        return baseValue*getActivePurchases(globalBuff).size();
    }

    /**
     * Find the latest date out of a set of the expiry dates.
     * @param globalBuff
     * @return
     */
    public Date getExpiryDate(CachedEntity globalBuff){
        List<EmbeddedEntity> purchases = getSortedActivePurchases(globalBuff);

        EmbeddedEntity latest = purchases.get(purchases.size() -1);

        return (Date) latest.getProperty("expiryDate");
    }

    /**
     * Find the earliest date out of a set of dates
     * @param globalBuff
     * @return
     */
    public Date getDecrementDate(CachedEntity globalBuff){
        List<EmbeddedEntity> purchases = getSortedActivePurchases(globalBuff);

        EmbeddedEntity earliest = purchases.get(0);


        return (Date) earliest.getProperty("expiryDate");
    }

    /**
     * Returns true if there are any active purchases.
     * @param globalBuff
     * @return
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

    public boolean isBuffEnabled(CachedEntity globalBuff){
        return (Boolean) globalBuff.getProperty("enabled");
    }

    /**
     * Gets a list of the active purchases, orted earliest -> latest
     * @param globalBuff
     * @return
     */
    public List<EmbeddedEntity> getSortedActivePurchases(CachedEntity globalBuff){
        List<EmbeddedEntity> purchases = getActivePurchases(globalBuff);

        //I think this is correct???
        purchases.sort((d1, d2) -> {
            Date date1 = (Date) d1.getProperty("expiryDate");
            Date date2 = (Date) d2.getProperty("expiryDate");

            return date1.compareTo(date2);
        });


        return purchases;
    }

    /**
     * Return a list of all the active purchases for this buff.
     * @param globalBuff
     * @return
     */
    public List<EmbeddedEntity> getActivePurchases(CachedEntity globalBuff){
        List<EmbeddedEntity> purchases = getPurchases(globalBuff);

        return purchases
                .stream()
                .filter(p -> false == isExpired((Date) p.getProperty("expiryDate")))
                .collect(Collectors.toList());
    }

    /**
     * @param globalBuff
     * @return
     */
    private List<EmbeddedEntity> getPurchases(CachedEntity globalBuff){
        List<EmbeddedEntity> purchases = (List<EmbeddedEntity>) globalBuff.getProperty("purchases");
        if(purchases == null)
            purchases = new ArrayList<>();

        return purchases;
    }

    /**
     * Returns true is the given date is BEFORE the current date.
     * @param expiryDate
     * @return
     */
    public boolean isExpired(Date expiryDate){
        return expiryDate.before(new Date());
    }
}
