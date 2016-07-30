package com.universeprojects.miniup.server.services;

import java.util.Collection;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;

/**
 * 
 * 
 * Note: If this class starts using entities that are in the Territory entity's fields a lot, we may want to automatically do
 * a batch-get of all those commonly used entities when this service is created.
 * 
 * @author Owner
 *
 */
public class TerritoryService
{
	final private ODPDBAccess db;
	final private CachedEntity territory;
	
	private List<CachedEntity> locations = null;
	
	public TerritoryService(ODPDBAccess db, CachedEntity territory)
	{
		this.territory = territory;
		this.db = db;
		
	}
	
	private List<CachedEntity> getLocations()
	{
		if (locations!=null) return locations;
		locations = db.getFilteredList("Location", "territoryKey", territory.getKey());
		return locations;
	}
	
	public boolean isLocationInTerritory(CachedEntity location)
	{
		return GameUtils.equals(location.getProperty("territoryKey"), territory.getKey());
	}

	/**
	 * Check if the given character is allowed to move into the given location.
	 * 
	 * @param character
	 * @param location
	 * @return
	 */
	public boolean isAllowedIn(CachedEntity character, CachedEntity location)
	{
		// First validate the arguments...
		if (character==null)
			throw new IllegalArgumentException("Character cannot be null.");
		if (location==null)
			throw new IllegalArgumentException("Location cannot be null.");
		if (territory==null)
			return true;
		if (isLocationInTerritory(location)==false)
			throw new IllegalArgumentException("Location is not part of the territory this service is servicing.");

		
		// Get the rule for travel
		String travelRule = (String)territory.getProperty("travelRule");
		
		//None,Whitelisted,OwningGroupOnly,OwningGroupAdminsOnly
		if ("OwningGroupAdminsOnly".equals(travelRule))
		{
			// Check if the character is in the group that owns the territory and check if he's an admin. If he is not, then we'll return false.
			if (GameUtils.equals(character.getProperty("groupKey"), getOwningGroupKey()) && 
					("Admin".equals(character.getProperty("groupStatus"))))
				return true;
		}
		else if ("OwningGroupOnly".equals(travelRule))
		{
			// Check if the character is in the group that owns the territory. If he is not in it, then we'll return false.
			if (GameUtils.equals(character.getProperty("groupKey"), getOwningGroupKey()) && 
					("Member".equals(character.getProperty("groupStatus"))==false || "Admin".equals(character.getProperty("groupStatus"))==false))
				return true;
		}
		else if ("Whitelisted".equals(travelRule))
		{
			// Check if the character is in the group that owns the territory. If he is not in it, then we'll check if he's in the whitelist.
			if (GameUtils.equals(character.getProperty("groupKey"), getOwningGroupKey()) == false || 
					("Member".equals(character.getProperty("groupStatus"))==false && "Admin".equals(character.getProperty("groupStatus"))==false))
			{
				// Check if he's in the whitelist
				Collection<Key> groupWhitelist = getGroupWhitelist();
				Collection<Key> characterWhitelist = getCharacterWhitelist();
				
				for(Key whitelistGroup:groupWhitelist)
					if (GameUtils.equals(whitelistGroup, character.getProperty("groupKey")))
						return true;
				
				for(Key whitelistCharacter:characterWhitelist)
					if (GameUtils.equals(whitelistCharacter, character.getKey()))
						return true;
			}
				
		}
		else 
		{
			return true;
		}
		
		return false;
	}

	/**
	 * Returns the group entity that owns this territory.
	 * @return
	 */
	public Key getOwningGroupKey()
	{
		return (Key)territory.getProperty("owningGroupKey");
	}
	
	
	public Collection<Key> getGroupWhitelist()
	{
		return (Collection<Key>)territory.getProperty("groupWhitelist");
	}
	
	public Collection<Key> getCharacterWhitelist()
	{
		return (Collection<Key>)territory.getProperty("characterWhitelist");
	}
}
