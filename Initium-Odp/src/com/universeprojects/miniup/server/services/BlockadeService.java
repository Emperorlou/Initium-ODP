package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.AbortTransactionException;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.InitiumTransaction;
import com.universeprojects.miniup.server.NotificationType;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.EngageBlockadeOpponentResult;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.exceptions.UserErrorMessageRuntimeException;

public class BlockadeService extends Service 
{

	public BlockadeService(ODPDBAccess db) 
	{
		super(db);
	}
	
	/**
	* Returns the blockade that would block the given character if he were to travel to the given location.
	*
	* If no block would happen, this method returns null.
	*
	* @param character
	* @param destinationLocation
	* @return
	*/
	public CachedEntity getBlockadeFor(CachedEntity character, CachedEntity destinationLocation)
	{         
		if (character==null)
			throw new IllegalArgumentException("Character cannot be null.");
		if (destinationLocation==null)
			throw new IllegalArgumentException("Location cannot be null.");
		
		if ("TRUE".equals(destinationLocation.getProperty("defenceStructuresAllowed"))==false)
			return null;
		
		// Get all paths that lead from this location
		List<CachedEntity> paths = db.getPathsByLocation(destinationLocation.getKey());
		// Go through the paths looking for BlockadeSite types. We will need to look through those.
		List<CachedEntity> blockadeLocations = new ArrayList<CachedEntity>();
		// Also add our current location (right at the beginning)
		if ("TRUE".equals(destinationLocation.getProperty("defenceStructuresAllowed")))
		{
			blockadeLocations.add(destinationLocation);
		}
		for(CachedEntity path:paths)
			if ("BlockadeSite".equals(path.getProperty("type")))
			{
				if (((Key)path.getProperty("location1Key")).getId() == destinationLocation.getKey().getId())
					blockadeLocations.add(db.getEntity((Key)path.getProperty("location2Key")));
				else
					blockadeLocations.add(db.getEntity((Key)path.getProperty("location1Key")));
			}
			
		if (blockadeLocations.isEmpty())
			return null;
		
		for(CachedEntity blockadeLocation:blockadeLocations)
		{
			CachedEntity blockadeStructure = db.getEntity((Key)blockadeLocation.getProperty("defenceStructure"));
			if (blockadeStructure==null)
				continue;
			if (isBlockedByBlockadeRules(character, destinationLocation.getKey(), blockadeStructure))
				return blockadeStructure;
		}
		
		return null;
	}
	
    /**
     * THIS IS A TRANSACTION METHOD
     * 
     * 
     * @param ds
     * @param character
     * @param startingLocation The location the character is currently in
     * @param parentLocation The location that the defence structure's location links to. 
     * @param blockadeLocation The location of the blockade itself.
     * @param blockadeStructure
     * @return
     * @throws UserErrorMessage 
     */
    public EngageBlockadeOpponentResult engageBlockadeOpponent(final Key characterKey, final Key startingLocation, final CachedEntity parentLocation, final Key blockadeLocationKey, final CachedEntity blockadeStructure) throws UserErrorMessage
    {
    	CachedDatastoreService ds = db.getDB();
        
        if (characterKey==null)
            throw new IllegalArgumentException("Character cannot be null.");
        if (blockadeLocationKey==null)
            throw new IllegalArgumentException("Blockade location cannot be null.");
        if (blockadeStructure==null)
            throw new IllegalArgumentException("Blockade structure cannot be null.");
        final ODPDBAccess.EngageBlockadeOpponentResult finalResult = db.new EngageBlockadeOpponentResult();
        
        final List<CachedEntity> charactersInBlockade = query.getFilteredList("Character", "locationKey", blockadeLocationKey);
        Collections.shuffle(charactersInBlockade);
        try 
        {
            new InitiumTransaction<CachedEntity>(ds) 
            {
                
                @Override
                public CachedEntity doTransaction(CachedDatastoreService ds) 
                {
                    CachedEntity chosenOpponent = null;
                    
                    //Refetch the character entity because it will be changed in this transaction
                    CachedEntity character = db.getEntity(characterKey);
                    if (ODPDBAccess.CHARACTER_MODE_COMBAT.equals(character.getProperty("mode")))
                        throw new UserErrorMessageRuntimeException("Character is already in combat. This action cannot be performed right now.");
                    
                    finalResult.charactersInBlockade = charactersInBlockade;
                    
                    if (startingLocation!=null && startingLocation.getId() == blockadeLocationKey.getId())
                    {
                        finalResult.freeToPass = true;
                        return null;
                    }
                    
                    // Here we'll include whether or not the character is free to pass the blockade
                    finalResult.freeToPass = true;
                    finalResult.hasDefenders = false;
                    for(CachedEntity defender:charactersInBlockade)
                        if (("Defending1".equals(defender.getProperty("status")) || "Defending2".equals(defender.getProperty("status")) || "Defending3".equals(defender.getProperty("status"))) && 
                                (Double)defender.getProperty("hitpoints")>0)
                        {
                            finalResult.freeToPass = false;
                            finalResult.hasDefenders = true;
                            break;
                        }
                    finalResult.onlyNPCDefenders = true;
                    for(CachedEntity defender:charactersInBlockade)
                        if (("Defending1".equals(defender.getProperty("status")) || "Defending2".equals(defender.getProperty("status")) || "Defending3".equals(defender.getProperty("status"))) && 
                                (Double)defender.getProperty("hitpoints")>0 && 
                                "NPC".equals(defender.getProperty("type"))==false)
                        {
                            finalResult.onlyNPCDefenders = false;
                            break;
                        }
                            
                    
                    if (finalResult.freeToPass)
                        return null;
                    
                    
                    // First line defense...
                    for(CachedEntity defender:charactersInBlockade)
                        if ("Defending1".equals(defender.getProperty("status")) && 
                                (ODPDBAccess.CHARACTER_MODE_COMBAT.equals(defender.getProperty("mode"))==false) &&
                                (Double)defender.getProperty("hitpoints")>0 &&
                                defender.getKey().getId() != character.getKey().getId())
                        {
                            
                            chosenOpponent=defender;
                            break;
                        }
                    
                    
                    // Second line defense...
                    if (chosenOpponent==null)
                        for(CachedEntity defender:charactersInBlockade)
                            if ("Defending2".equals(defender.getProperty("status")) && 
                                    (ODPDBAccess.CHARACTER_MODE_COMBAT.equals(defender.getProperty("mode"))==false) &&
                                    (Double)defender.getProperty("hitpoints")>0 &&
                                    defender.getKey().getId() != character.getKey().getId())
                            {
                                
                                chosenOpponent=defender;
                                break;
                            }
                    
                    
                    // Third line defense...
                    if (chosenOpponent==null)
                        for(CachedEntity defender:charactersInBlockade)
                            if ("Defending3".equals(defender.getProperty("status")) && 
                                    (ODPDBAccess.CHARACTER_MODE_COMBAT.equals(defender.getProperty("mode"))==false) &&
                                    (Double)defender.getProperty("hitpoints")>0 &&
                                    defender.getKey().getId() != character.getKey().getId())
                            {
                                
                                chosenOpponent=defender;
                                break;
                            }
                    
                    if (chosenOpponent!=null)
                    {
                        CachedEntity opponent = null;
                        opponent = ds.refetch(chosenOpponent);
                        
                        // Compare the fetched opponent and the chosen opponent for property value equality. If anything changed, do it over
                        if (opponent.getProperties().equals(chosenOpponent.getProperties())==false)
                            throw new ConcurrentModificationException();
                        
                        character.setProperty("combatant", opponent.getKey());
                        character.setProperty("mode", ODPDBAccess.CHARACTER_MODE_COMBAT);
                        character.setProperty("combatType", "DefenceStructureAttack");
                        db.flagCharacterCombatAction(ds, character);
                        
                        opponent.setProperty("combatant", character.getKey());
                        opponent.setProperty("mode", ODPDBAccess.CHARACTER_MODE_COMBAT);
                        
                        ds.put(opponent);
                        ds.put(character);
                    }
                    
                    finalResult.defender = chosenOpponent;
                    
                    return chosenOpponent;
                }
            }.run();
        } 
        catch (AbortTransactionException e) 
        {
            throw new UserErrorMessage(e.getMessage());
        }

        if (finalResult.defender!=null)
            db.sendNotification(ds, finalResult.defender.getKey(), NotificationType.fullpageRefresh);

        return finalResult;
    }

    /**
     * This simply determines if the character will be blocked by a blockade based on the blockade's rules.
     * 
     * @param character
     * @param location
     * @return
     */
    private boolean isBlockedByBlockadeRules(CachedEntity character, Key destinationLocationKey, CachedEntity blockadeStructure)
    {
        if (character==null)
            throw new IllegalArgumentException("Character cannot be null.");
        if (destinationLocationKey==null)
            throw new IllegalArgumentException("Parent location cannot be null.");
        if (blockadeStructure==null)
            throw new IllegalArgumentException("Blockade structure cannot be null.");
        
        
        if ("BlockAllParent".equals(blockadeStructure.getProperty("blockadeRule")))
            return true;
        else if ("BlockAllSelf".equals(blockadeStructure.getProperty("blockadeRule")) && 
        		destinationLocationKey.getId() == ((Key)blockadeStructure.getProperty("locationKey")).getId())
            return true;
        else
            return false;
    }
    
	/**
	* This will cause the defender's combatant to be allowed entry into the defensive structure location.
	*
	* @param ds
	* @param defender
	*/
	public void doCharacterAllowEntryIntoDefenceStructure(CachedEntity defender)
	{
		CachedDatastoreService ds = db.getDB();
		
		if (defender==null)
			throw new IllegalArgumentException("Defender cannot be null.");
		
		
		Key defenderLocationKey = (Key)defender.getProperty("locationKey");
		
		CachedEntity characterToAllowIn = db.getEntity((Key)defender.getProperty("combatant"));
		
		defender.setProperty("mode", ODPDBAccess.CHARACTER_MODE_NORMAL);
		defender.setProperty("combatant", null);
		defender.setProperty("combatType", null);
		
		Double hitpoints = (Double)defender.getProperty("hitpoints");
		Double maxHitpoints = (Double)defender.getProperty("maxHitpoints");
		if (hitpoints<maxHitpoints)
			defender.setProperty("hitpoints", maxHitpoints);
	
		characterToAllowIn.setProperty("mode", ODPDBAccess.CHARACTER_MODE_NORMAL);
		characterToAllowIn.setProperty("combatant", null);
		characterToAllowIn.setProperty("combatType", null);
		characterToAllowIn.setProperty("locationKey", defenderLocationKey);
	
		ds.put(defender);
		ds.put(characterToAllowIn);
		db.sendNotification(ds, characterToAllowIn.getKey(), NotificationType.fullpageRefresh);
		// TODO: Check to make sure the character is actually defending a structure so we don't just teleport random people.
		// Right now it's not possible to enter combat with anyone who isn't dueling you or attacking your defending structure so it's ok, but eventually
		// this might not be the only cases.
	}
	
	public void setBlockadeRule(CachedEntity character, 
			CachedEntity location, String newRule) throws UserErrorMessage 
	{
		ODPDBAccess.BlockadeRule.valueOf(newRule);	// Check if this is a valid value, it will throw an IllegalArgumentException if it's not
	
		CachedDatastoreService ds=db.getDB();
		
		CachedEntity defenceStructure = db.getEntity((Key)location.getProperty("defenceStructure"));
		
		if (defenceStructure==null)
			throw new UserErrorMessage("You're not near a defence structure and so you cannot change the defence mode.");
		if (defenceStructure.getProperty("leaderKey")==null || ((Key)defenceStructure.getProperty("leaderKey")).getId() != character.getKey().getId())
			throw new UserErrorMessage("You are not the leader of this defence structure and so cannot change the defence mode.");

		if ("FALSE".equals(defenceStructure.getProperty("blockadeRuleChangeable")))
			throw new UserErrorMessage("You cannot change the blockade rule for this structure, it is not configurable.");
		
		if ((defenceStructure.getProperty("blockadeRule")==null && newRule!=null) || defenceStructure.getProperty("blockadeRule").equals(newRule)==false)
		{
			defenceStructure.setProperty("blockadeRule", newRule);
			ds.put(defenceStructure);
		}
		
	}
	
	public void doCharacterAttackStructure(CachedEntity character, CachedEntity location) throws UserErrorMessage
	{
		if (location.getProperty("defenceStructure")==null)
			throw new UserErrorMessage("There is no defence structure here to attack.");
		
		EngageBlockadeOpponentResult result = engageBlockadeOpponent(
			character.getKey(), null, null, location.getKey(), 
			db.getEntity((Key)location.getProperty("defenceStructure")));
			
		if (result.charactersInBlockade!=null && result.charactersInBlockade.size()==1 && 
				result.charactersInBlockade.get(0).getKey().getId() == character.getKey().getId())
			throw new UserErrorMessage("Dude, relax. You can't attack yourself and you're the only one defending here.");
		if (result.hasDefenders==true && result.defender==null && result.onlyNPCDefenders==false)
			throw new UserErrorMessage("Unable to attack at the moment, all defenders are already in combat.");
		if (result.hasDefenders==false)
			throw new UserErrorMessage("You cannot attack because there is no one defending the tower!");
		
	}
}
