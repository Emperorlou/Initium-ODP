package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.TerritoryCharacterFilter;

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
	
	/**
	 * Shortcut method to get the first character that matches the given filter in our territory.
	 * 
	 * @param filter
	 * @param startLocation
	 * @return
	 */
	public CachedEntity getTerritoryAvailableCharacter(TerritoryCharacterFilter filter, CachedEntity startLocation)
	{
		List<CachedEntity> characters = getTerritoryCharacters(filter, startLocation, true);
		if (characters.isEmpty()) 
			return null;
		else
			return characters.get(0);
	}
	
	/**
	 * Shortcut method to get all characters that match the given filter from the territory.
	 * 
	 * @param filter
	 * @param startLocation
	 * @return
	 */
	public List<CachedEntity> getTerritoryAllCharacters(TerritoryCharacterFilter filter, CachedEntity startLocation)
	{
		return getTerritoryCharacters(filter, startLocation, false);
	}
	
	/**
	 * This method will return a list of characters in the territory based on the given filter. 
	 * It can also return a single character (returning early to not waste time).
	 * 
	 * @param filter
	 * @param startLocation
	 * @param single
	 * @return
	 */
	public List<CachedEntity> getTerritoryCharacters(TerritoryCharacterFilter filter, CachedEntity startLocation, boolean single)
	{
		if (startLocation==null) return null;
		if (isLocationInTerritory(startLocation))
			throw new IllegalArgumentException("startLocation is not in the territory this service is servicing.");
		
		CachedDatastoreService ds = db.getDB();
		List<CachedEntity> territoryLocations = db.getFilteredList("Location", "territoryKey", territory.getKey());
		List<List<CachedEntity>> tlcList = new ArrayList<List<CachedEntity>>();
		getTerritoryLocationCharacters(tlcList, ds, filter, startLocation, 0, territory, territoryLocations, single);
		List<CachedEntity> characters = new ArrayList<CachedEntity>();
		for (List<CachedEntity> tlc : tlcList)
		{
			if (tlc!=null && tlc.isEmpty()==false)
			{
				// randomly shuffle the order
				Collections.shuffle(tlc);
				characters.addAll(tlc);
			}
		}
		// resort in Defending order if Defending 
		if (filter == TerritoryCharacterFilter.Defending)
		{
			// Status can only be DefendingX due to the Query, so no need for error checking
			Collections.sort(characters, new Comparator<CachedEntity>()
			{
				@Override
				public int compare(CachedEntity o1, CachedEntity o2)
				{
					return ((String)o1.getProperty("status")).compareTo((String)o2.getProperty("status"));
				}
			});
		}
		return characters;
	}
	
	/**
	 * This recursive method iterates through all the locations in a given territory (from the given start point)
	 * 
	 * @param tlcList
	 * @param db
	 * @param filter
	 * @param location
	 * @param distance
	 * @param territory
	 * @param territoryLocations
	 * @param single
	 * @return
	 */
	private boolean getTerritoryLocationCharacters(List<List<CachedEntity>> tlcList, CachedDatastoreService ds, TerritoryCharacterFilter filter, CachedEntity location, int distance, CachedEntity territory, List<CachedEntity> territoryLocations, boolean single)
	{
		// set up the DB search filter
		Filter f0 = null;
		switch (filter) {
		case Defending:
		{
			Filter f1 = new FilterPredicate("locationKey", FilterOperator.EQUAL, location.getKey());
			Filter f2 = new FilterPredicate("type", FilterOperator.EQUAL, "PC");
			Filter f3 = new FilterPredicate("mode", FilterOperator.NOT_EQUAL, "COMBAT");
			Filter f4a = new FilterPredicate("status", FilterOperator.EQUAL, "Defending1");
			Filter f4b = new FilterPredicate("status", FilterOperator.EQUAL, "Defending2");
			Filter f4c = new FilterPredicate("status", FilterOperator.EQUAL, "Defending3");
			Filter f4 = CompositeFilterOperator.or(f4a, f4b, f4c);
			f0 = CompositeFilterOperator.and(f1, f2, f3, f4);
		} break;
		case Trespassing:
		{
			Filter f1 = new FilterPredicate("locationKey", FilterOperator.EQUAL, location.getKey());
			Filter f2 = new FilterPredicate("type", FilterOperator.EQUAL, "PC");
			Filter f3 = new FilterPredicate("mode", FilterOperator.NOT_EQUAL, "COMBAT");
			f0 = CompositeFilterOperator.and(f1, f2, f3);
		} break;
		case All:
			Filter f1 = new FilterPredicate("locationKey", FilterOperator.EQUAL, location.getKey());
			Filter f2 = new FilterPredicate("type", FilterOperator.EQUAL, "PC");
			f0 = CompositeFilterOperator.and(f1, f2);
		}
		List<CachedEntity> characters = ds.fetchAsList("Character", f0, 10000);
		
		// Further filter what wasn't possible to do directly due to AppEngine limitations
		Next: for (int i = 0; i < characters.size(); i++)
		{
			CachedEntity character = characters.get(i);
			if (GameUtils.isPlayerIncapacitated(character))
			{
				characters.remove(i);
				i--;
				continue Next;
			}
			if (filter==TerritoryCharacterFilter.Trespassing)
			{
				// Remove whitelisted characters
				List<CachedEntity> allowedChars = (List<CachedEntity>)territory.getProperty("characterWhitelist");
				Key key = character.getKey();
				for (CachedEntity c : allowedChars)
				{
					if (c.getKey().equals(key))
					{
						characters.remove(i);
						i--;
						continue Next;
					}
				}
				List<CachedEntity> allowedGroups = (List<CachedEntity>)territory.getProperty("groupWhitelist");
				key = (Key)character.getProperty("groupKey");
				if (key==null) continue Next;
				// since characters can have a groupKey while not part of the group, check groupStatus
				if (GameUtils.isContainedInList("Member,Admin",(String)character.getProperty("groupStatus"))==false)
					 continue Next;
				for (CachedEntity c : allowedGroups)
				{
					if (c.getKey().equals(key))
					{
						characters.remove(i);
						i--;
						continue Next;
					}
				}
			}
		}
		
		// store the result
		if (tlcList.size() <= distance)
			tlcList.add(characters);
		else
			tlcList.get(distance).addAll(characters);
		
		// If only 1 character is required and we have one, exit early
		if (single && characters.isEmpty()==false)
			return true;
		
		// Get the neighbouring locations that are part of the territory
		List<CachedEntity> paths = db.getPathsByLocation(location.getKey());
		List<CachedEntity> locations = new ArrayList<CachedEntity>();
		for (CachedEntity path : paths)
		{
			Key locationKey = (Key)path.getProperty("location1Key");
			if (location.getKey().equals(locationKey))
				locationKey = (Key)path.getProperty("location2Key");
			if (locationKey!=null)
				for (CachedEntity l : territoryLocations)
					if (l.getKey().equals(locationKey))
						locations.add(l);
		}
		// Randomly shuffle the order
		Collections.shuffle(locations);
		
		// Now recursively get the characters in these locations
		for (CachedEntity l : locations)
		{
			// If only 1 character is required and we have one, exit early
			if (getTerritoryLocationCharacters(tlcList, ds, filter, l, distance+1, territory, territoryLocations, single) && single)
				return true;
		}
		
		return false;
	}
	
}
