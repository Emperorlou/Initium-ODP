package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.NotificationType;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;
import com.universeprojects.miniup.server.services.TerritoryService.TerritoryCharacterFilter;

/**
 * 
 * 
 * Note: If this class starts using entities that are in the Territory entity's fields a lot, we may want to automatically do
 * a batch-get of all those commonly used entities when this service is created.
 * 
 * @author Owner
 *
 */
public class TerritoryService extends Service
{
	
	public enum TerritoryCharacterFilter
	{
		Defending, Trespassing, All
	}
	
	public enum TerritoryTravelRule
	{
		None, Whitelisted, OwningGroupOnly
	}
	
	final private CachedEntity territory;
	
	private List<CachedEntity> locations = null;
	
	public TerritoryService(ODPDBAccess db, CachedEntity territory)
	{
		super(db);
		this.territory = territory;
		
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
	public boolean isAllowedIn(CachedEntity character)
	{
		// First validate the arguments...
		if (character==null)
			throw new IllegalArgumentException("Character cannot be null.");
		if (territory==null)
			return true;

		// Get the rule for travel
		TerritoryTravelRule travelRule = null;
		try {
			travelRule = TerritoryTravelRule.valueOf((String)territory.getProperty("travelRule"));
		} catch (Exception e) {
			// Unknown travel rule, silently reset to default
			travelRule = TerritoryTravelRule.OwningGroupOnly;
		}
		
		switch (travelRule) {
		case OwningGroupOnly:
		// This really shouldn't happen, but just in case...
		default:
			// Check if the character is in the group that owns the territory.
			if (GameUtils.equals(character.getProperty("groupKey"), getOwningGroupKey()) && 
					("Member".equals(character.getProperty("groupStatus"))==true || "Admin".equals(character.getProperty("groupStatus"))==true))
				return true;
			break;
		case Whitelisted:
			// Check if the character is in the group that owns the territory.
			if (GameUtils.equals(character.getProperty("groupKey"), getOwningGroupKey()) && 
					("Member".equals(character.getProperty("groupStatus"))==true || "Admin".equals(character.getProperty("groupStatus"))==true))
				return true;
			
			// Check if he's in the whitelist
			Collection<Key> groupWhitelist = getGroupWhitelist();
			Collection<Key> characterWhitelist = getCharacterWhitelist();
			
			if ("Member".equals(character.getProperty("groupStatus"))==true || "Admin".equals(character.getProperty("groupStatus"))==true)
				for(Key whitelistGroup:groupWhitelist)
					if (GameUtils.equals(whitelistGroup, character.getProperty("groupKey")))
						return true;
			
			for(Key whitelistCharacter:characterWhitelist)
				if (GameUtils.equals(whitelistCharacter, character.getKey()))
					return true;
			break;
		case None:
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns whether the character is an admin of the owning group
	 * 
	 * @param character
	 * @return
	 */
	public boolean isTerritoryAdmin(CachedEntity character)
	{
		Key groupKey = (Key)character.getProperty("groupKey");
		if (groupKey==null)
			return false;
		if (GameUtils.equals(groupKey, getOwningGroupKey())==false)
			return false;
		if ("Admin".equals(character.getProperty("groupStatus"))==false)
			return false;

		return true;
	}
	
	/**
	 * Returns whether the character is allowed to Move/Explore/Rest
	 * if enterCombat = true, will automatically fight the nearest defender if action isn't allowed
	 * 
	 * @param character
	 * @param location
	 * @param enterCombat
	 * @return
	 */
	public boolean canPerformRegularAction(CachedEntity character, CachedEntity location, boolean enterCombat)
	{
		if (character==null)
			throw new IllegalArgumentException("canPerformRegularAction invalid call format, 'character' cannot be null.");
		if (location==null)
			throw new IllegalArgumentException("canPerformRegularAction invalid call format, 'location' cannot be null.");
		
		// If the location isn't in this territory, no need for restrictions
		if (isLocationInTerritory(location)==false)
			return true;
		
		if (isAllowedIn(character))
			return true;
		
		// If there are no active defenders, action is allowed
		CachedEntity defender = getTerritorySingleCharacter(TerritoryCharacterFilter.Defending, location);
		if (defender==null)
			return true;
		
		// At this point, the action isn't allowed
		if (enterCombat)
		{
			// Attack defender
			new CombatService(db).enterCombat(character, defender, true);
			// Since we don't know how this method is called, use sendNotifaction rather than setJavascriptResponse
			// Defender is refreshed from CombatService
			db.sendNotification(db.getDB(), character.getKey(), NotificationType.fullpageRefresh);
		}
		
		return false;
	}
	
	/**
	 * Returns the error message if the character cannot use Retreat or null otherwise.
	 * (Placed in the service for link generation purposes)
	 * 
	 * @param character
	 * @param location
	 * @return
	 */
	public String getRetreatError(CachedEntity character, CachedEntity location)
	{
		if (character==null)
			return "canRetreat invalid call format, 'character' cannot be null.";
		if (location==null)
			return "canRetreat invalid call format, 'location' cannot be null.";
		
		// Can't retreat if you're not there to begin with.
		if (isLocationInTerritory(location)==false)
			return "Retreat? Retreat from where? But that's not where you are!";
			
		// Verify character is alive and not doing something else
		if (GameUtils.isPlayerIncapacitated(character))
			return "You are incapacitated and thus cannot do this.";
		String mode = (String)character.getProperty("mode");
		if (mode!=null && mode.equals("NORMAL")==false)
			return "You're too busy to try and retreat at the moment.";
		
		// Only trespassers can retreat and only when there are active defenders (to prevent exploits)
		if (isAllowedIn(character))
			return "You're free to move around in this territory, no need to retreat stealthily.";
		if (getTerritorySingleCharacter(TerritoryCharacterFilter.Defending, location)==null)
			return "You're free to move around in this territory, no need to retreat stealthily.";
		
		return null;
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
	 * Shortcut method to get the first character in order that matches the given filter in our territory.
	 * 
	 * @param filter
	 * @param startLocation
	 * @return
	 */
	public CachedEntity getTerritorySingleCharacter(TerritoryCharacterFilter filter, CachedEntity startLocation)
	{
		List<CachedEntity> characters = getTerritoryCharacters(filter, startLocation, true, true);
		if (characters.isEmpty()) 
			return null;
		else
			return characters.get(0);
	}
	
	/**
	 * Shortcut method to get all characters that match the given filter from the territory sorted by encounter order.
	 * 
	 * @param filter
	 * @param startLocation
	 * @return
	 */
	public List<CachedEntity> getTerritoryAllCharactersSorted(TerritoryCharacterFilter filter, CachedEntity startLocation)
	{
		return getTerritoryCharacters(filter, startLocation, false, true);
	}
	
	/**
	 * Shortcut method to get all characters that match the given filter from the territory.
	 * 
	 * @param filter
	 * @param startLocation
	 * @return
	 */
	public List<CachedEntity> getTerritoryAllCharactersUnsorted(TerritoryCharacterFilter filter)
	{
		return getTerritoryCharacters(filter, null, false, false);
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
	private List<CachedEntity> getTerritoryCharacters(TerritoryCharacterFilter filter, CachedEntity startLocation, boolean single, boolean sorted)
	{
		List<CachedEntity> locs = getLocations();
		if (locs.isEmpty()) return null;
		if (startLocation==null)
			startLocation = locs.get(0);
		if (isLocationInTerritory(startLocation))
			throw new IllegalArgumentException("startLocation is not in the territory this service is servicing.");
		
		CachedDatastoreService ds = db.getDB();
		// Create a duplicate so we can modify it, implemented as set for performance
		Set<CachedEntity> remainingLocations = new HashSet<CachedEntity>(locs);
		// Remove the startLocation from the remaining territory locations
		for (Iterator<CachedEntity> iter = remainingLocations.iterator();iter.hasNext();)
		{
			if (GameUtils.equals(iter.next().getKey(), startLocation.getKey()))
			{
				iter.remove();
				break;
			}
		}
		List<List<CachedEntity>> tlcList = new ArrayList<List<CachedEntity>>();
		getTerritoryLocationCharacters(tlcList, ds, filter, startLocation, 0, remainingLocations, single, sorted);
		List<CachedEntity> characters = new ArrayList<CachedEntity>();
		for (List<CachedEntity> tlc : tlcList)
		{
			if (tlc!=null && tlc.isEmpty()==false)
			{
				// if order matters randomly shuffle those with same order
				if (sorted) Collections.shuffle(tlc);
				characters.addAll(tlc);
			}
		}
		// if order matters resort in Defending order if Defending 
		if (sorted && filter == TerritoryCharacterFilter.Defending)
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
	 * @param sorted
	 * @return
	 */
	private boolean getTerritoryLocationCharacters(List<List<CachedEntity>> tlcList, CachedDatastoreService ds, TerritoryCharacterFilter filter, CachedEntity location, int distance, Set<CachedEntity> remainingLocations, boolean single, boolean sorted)
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
		case All:
			Filter f1 = new FilterPredicate("locationKey", FilterOperator.EQUAL, location.getKey());
			Filter f2 = new FilterPredicate("type", FilterOperator.EQUAL, "PC");
			f0 = CompositeFilterOperator.and(f1, f2);
		}
		List<CachedEntity> characters = ds.fetchAsList("Character", f0, 10000);
		
		// Further filter what wasn't possible to do directly due to AppEngine limitations
		// Use manual for to obtain the index for removal purposes, traverse backwards for optimisation
		// If this becomes a bottleneck, we can convert to LinkedList before removal
		for (int i = characters.size()-1; i>=0 ; i--)
		{
			CachedEntity character = characters.get(i);
			if (GameUtils.isPlayerIncapacitated(character) ||
					(filter==TerritoryCharacterFilter.Trespassing && isAllowedIn(character)))
			{
				characters.remove(i);
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
		
		if (sorted)
		{
			// Get the neighbouring locations that are part of the territory
			List<CachedEntity> paths = db.getPathsByLocation(location.getKey());
			List<CachedEntity> locations = new ArrayList<CachedEntity>();
			for (CachedEntity path : paths)
			{
				Key locationKey = (Key)path.getProperty("location1Key");
				if (GameUtils.equals(location.getKey(),locationKey))
					locationKey = (Key)path.getProperty("location2Key");
				if (locationKey!=null)
				{
					for (Iterator<CachedEntity> iter = remainingLocations.iterator();iter.hasNext();)
					{
						CachedEntity l = iter.next();
						if (GameUtils.equals(l.getKey(),locationKey))
						{
							locations.add(l);
							iter.remove();
							break;
						}
					}
				}
			}
			// Randomly shuffle the order
			Collections.shuffle(locations);
			
			// Now recursively get the characters in these locations
			for (CachedEntity l : locations)
				// If only 1 character is required and we have one, exit early
				if (getTerritoryLocationCharacters(tlcList, ds, filter, l, distance+1, remainingLocations, single, sorted) && single)
					return true;
		}
		else
		{
			Iterator<CachedEntity> iter = remainingLocations.iterator();
			if (iter.hasNext())
			{
				CachedEntity l = iter.next();
				iter.remove();
				// If only 1 character is required and we have one, exit early
				if (getTerritoryLocationCharacters(tlcList, ds, filter, l, distance, remainingLocations, single, sorted) && single)
					return true;
			}
			
		}
		
		return false;
	}
	
	/**
	 * Moves all characters that match the filter to the first parentLocation that isn't in the territory.
	 * If for some reason parentLocation isn't set correctly, this will blank the character's location,
	 *   which in turn will send it to its hometown (therefore ensuring purge from territory) 
	 * 
	 * @param characters - when null, gets chars from DB
	 * @param filter
	 */
	public void territoryPurgeCharacters(List<CachedEntity> characters, TerritoryCharacterFilter filter) {
		if (characters==null)
		{
			// filter is already applied here, so no need to filter later 
			characters = getTerritoryAllCharactersUnsorted(filter);
			filter = TerritoryCharacterFilter.All;
		}
		territoryPurgeCharacters(characters, filter);
		List<CachedEntity> locations = getLocations();
		Key locationKey = null;
		Key bufferedLocationKey = null;
		Key parentLocationKey = null;
		CachedDatastoreService ds = db.getDB();
		for (CachedEntity character : characters)
		{
			// Filter the list if still needed
			if (filter==TerritoryCharacterFilter.Defending)
			{
				String status = (String)character.getProperty("status");
				if (status==null || status.startsWith("Defending")==false || "COMBAT".equals(character.getProperty("mode")))
					continue;
			}
			else if (filter==TerritoryCharacterFilter.Trespassing)
			{
				if (isAllowedIn(character))
					continue;
			}
			locationKey = (Key)character.getProperty("locationKey");
			// characters are returned by location, so if it's the same as the previous character, no need to recalculate
			if (locationKey!=bufferedLocationKey)
			{
				bufferedLocationKey = locationKey;
				parentLocationKey = locationKey;
				// Walk parentLocation until one is found that isn't in the territory
				boolean match;
				do
				{
					match = false;
					for (CachedEntity location : locations)
					{
						if (GameUtils.equals(parentLocationKey, location.getKey()))
						{
							parentLocationKey = (Key)location.getProperty("parentLocationKey");
							match = true;
							break;
						}
					}
				} while (match);
			}
			character.setProperty("locationKey", parentLocationKey);
			
			// Reset status because we're leaving a territory.
			character.setProperty("status", "Normal");
			ds.put(character);
			db.sendNotification(ds, character.getKey(), NotificationType.fullpageRefresh);
		}
	}
	
}
