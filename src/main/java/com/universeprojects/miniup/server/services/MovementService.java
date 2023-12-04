package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.GroupStatus;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class MovementService extends Service {

	private QueryHelper queryHelper;

	public MovementService(ODPDBAccess db) {
		super(db);
		this.queryHelper = new QueryHelper(super.db.getDB());
	}
	
	public void checkCharacterIsAllowedToEnterOwnedProperty(CachedEntity path, CachedEntity destination, List<CachedEntity> party) throws UserErrorMessage
	{
		Key ownerKey = (Key) destination.getProperty("ownerKey");
		if (ownerKey != null) {
			// Check to see if all members of the party have access to enter owned housing.
			if("Group".equals(ownerKey.getKind())){
				Set<String> groupKeySet = new HashSet<String>(); // Sets guarantee one and only one entry per unique value
				List<String> isActiveInGroupStatusList = Arrays.asList(GroupStatus.Admin.name(), GroupStatus.Member.name()); // Wish we had streaming to build this appropriately
				for(CachedEntity partyMember: party) {
					//Removes those who have just applied or have been kicked
					boolean belongstoGroup = isActiveInGroupStatusList.contains(partyMember.getProperty("groupStatus"));
					Key groupKey = (Key) partyMember.getProperty("groupKey"); // This should always be here if they belong to a group
					String mapKey = belongstoGroup ? groupKey.toString() : UUID.randomUUID().toString(); // random UUID to ensure uniqueness
					groupKeySet.add(mapKey);
				}

				if((!groupKeySet.contains(ownerKey.toString()) || groupKeySet.size() != 1) && 
						"Town".equals(destination.getProperty("type"))==false/*Exclude leaving*/) { 
					throw new UserErrorMessage(String.format("You cannot enter a group owned house unless %s.", party.size() > 1 ? "all members of your party are members of the group" : "you are a member of the group"));
				}
			} else if("User".equals(ownerKey.getKind())) {
				Key pathOwner = (Key) path.getProperty("ownerKey");
				for (CachedEntity partyMember : party) {
					boolean isPathOwner = GameUtils.equals(pathOwner, partyMember.getProperty("userKey"));
					if (!isPathOwner && !isPathDiscovered(partyMember.getKey(), path) && 
							"Town".equals(destination.getProperty("type"))==false/*Exclude leaving*/) {
						throw new UserErrorMessage(String.format("You cannot enter a player owned house unless %s.", party.size() > 1 ? "every character already has been given access" : "you already have been given access"));
					}
				}
			} else {
				// TODO - Exception? If we can't determine the owner type; the character will be allowed to take the path. 
			}
		}
	}
	
	public void checkForLocks(CachedEntity character, CachedEntity pathToTake, Key destinationLocationKey) throws UserErrorMessage
	{
		Object lockCode = null;
		Boolean checkAllCharacters = false;
		String errorMessage = null;
		if (GameUtils.equals(destinationLocationKey, pathToTake.getProperty("location1Key"))) {
			lockCode = pathToTake.getProperty("location2LockCode");
			
			checkAllCharacters = (Boolean) pathToTake.getProperty("location1KeyCheckAll");
			
			errorMessage = (String) pathToTake.getProperty("location1KeyErrorMessage");
		}
		else if (GameUtils.equals(destinationLocationKey, pathToTake.getProperty("location2Key"))) {
			lockCode = pathToTake.getProperty("location1LockCode");
			
			checkAllCharacters = (Boolean) pathToTake.getProperty("location2KeyCheckAll");
			
			errorMessage = (String) pathToTake.getProperty("location2KeyErrorMessage");
		}
		else
			throw new RuntimeException("Player is not located at either end of the specified path.");
		
		if(checkAllCharacters == null) checkAllCharacters = false;
		if(errorMessage == null) errorMessage = "This location is locked. You must have the correct key before you can access it.";
		if (lockCode != null) 
		{
			List<CachedEntity> characters = new ArrayList<>();
			
			if(checkAllCharacters) {
				if(character.getProperty("partyCode") != null) 	characters.addAll(db.getParty(db.getDB(), character));
				else if(characters.size() == 0) characters.add(character);
			}
			else characters.add(character);
			
			for(CachedEntity checkCharacter:characters) {
				if (checkHasKey(checkCharacter, (long)lockCode) == false) 
					throw new UserErrorMessage(errorMessage);
				
				else {
					FilterPredicate f1 = new FilterPredicate("containerKey", FilterOperator.EQUAL, checkCharacter.getKey());
					FilterPredicate f2 = new FilterPredicate("keyCode", FilterOperator.EQUAL, (long)lockCode);
					CachedDatastoreService ds = db.getDB();
					
					List<CachedEntity> matchingKeys = ds.fetchAsList("Item", CompositeFilterOperator.and(f1, f2), 1000);
					
					// If no item keys, then it's a buff key, which doesn't get affected.
					if(matchingKeys.isEmpty()==false)
					{
						// first matching key loses 1 durability
						CachedEntity key = matchingKeys.get(0);
						
						if (GameUtils.equals(key.getProperty("durability"), null) == false) {
							long durability = (long) key.getProperty("durability");
							
							if (durability > 1) {
								key.setProperty("durability", durability - 1);
								ds.put(key);
							}
							else
								ds.delete(key);
						}
					}
				}
			}
		}
	}
	
	private boolean checkHasKey(CachedEntity character, long lockCode) {
		int matchingKeys = db.getFilteredList_Count("Item", "containerKey", FilterOperator.EQUAL, character.getKey(), "keyCode", FilterOperator.EQUAL, (long)lockCode);
		
		//if we dont have any physical keys, we check for buffs.
		if(matchingKeys == 0) {
			//grab all the buffs associated with this character
			List<EmbeddedEntity> buffs = db.getBuffsFor(character);
			for(EmbeddedEntity buff : buffs) {
				
				
				//if we find a buff with the proper keycode, just return true.
				Long buffCode = (Long) buff.getProperty("keyCode");
				
				if(GameUtils.equals(lockCode, buffCode)) return true;

			}
		}
		return (matchingKeys > 0);
	}

	/**
	 * Returns true if the character has a Discovery for the path.
	 * 
	 * @param characterKey
	 * @param pathKey
	 * @return
	 */
	public boolean isPathDiscovered(Key characterKey, CachedEntity path) {
		if (path.getProperty("discoveryChance").equals(100d)) return true;
		return queryHelper.getFilteredList_Count("Discovery", "characterKey", FilterOperator.EQUAL, characterKey, "entityKey", FilterOperator.EQUAL, path.getKey()) > 0;
	}
}
