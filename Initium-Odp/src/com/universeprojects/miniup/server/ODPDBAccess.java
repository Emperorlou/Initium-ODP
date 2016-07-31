package com.universeprojects.miniup.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class ODPDBAccess
{
	public enum CharacterMode
	{
		NORMAL, COMBAT, MERCHANT, TRADING, UNCONSCIOUS, DEAD
	}

	public enum CharacterType
	{
		PC, NPC
	}

	public enum AutomaticWeaponChoiceMethod
	{
		Random, Rarest, HighestDamage
	}

	public enum CombatType
	{
		DefenceStructureAttack
	}

	public enum CharacterStatus
	{
		Normal, Defending1, Defending2, Defending3
	}

	public enum BlockadeRule
	{
		BlockAllParent, BlockAllSelf, None
	}

	public enum BlockCapability
	{
		None, Minimal, Poor, Average, Good, Excellent
	}

	public enum GroupStatus
	{
		Applied, Member, Admin, Kicked
	}
	
	public enum TerritoryCharacterFilter
	{
		Defending, Trespassing, All
	}

	public static final String STORE_NAME_REGEX = "[A-Za-z0-9- _/.,%:!?+*&'\"~\\(\\)]+";
	public static final String CAMP_NAME_REGEX = "[A-Za-z0-9- ,'&]+";
	public static final String GROUP_NAME_REGEX = "[A-Za-z ,'`]+";
	public static final String CHARACTER_MODE_NORMAL = "NORMAL";
	public static final String CHARACTER_MODE_COMBAT = "COMBAT";
	public static final String CHARACTER_MODE_MERCHANT = "MERCHANT";
	public static final String CHARACTER_MODE_TRADING = "TRADING";
	public static final String[] EQUIPMENT_SLOTS = new String[]
	{
			"Helmet", "Chest", "Shirt", "Gloves", "Legs", "Boots", "RightHand", "LeftHand", "RightRing", "LeftRing", "Neck"
	};
	private CachedDatastoreService ds = null;

	public Map<Key, List<CachedEntity>> buffsCache = new HashMap<Key, List<CachedEntity>>();

	public ODPDBAccess()
	{
		getDB(); // Initialize the datastore service
	}

	public CachedDatastoreService getDB()
	{
		if (ds != null) return ds;

		ds = new CachedDatastoreService();
		return ds;
	}

	// Just a method stub that is not part of the ODP (but still callable)
	public void sendNotification(CachedDatastoreService ds, Key character, NotificationType notificationType)
	{
		
	}
	
	public MemcacheService getMC()
	{
		return getDB().getMC();
	}

	/**
	 * Sets a memcache value, but only if it hasn't been modified and
	 * automatically retries until successful.
	 * 
	 * Be careful not to allow this call to be used too often on the same key.
	 * Something like this is ok to be used every other second or so, but less
	 * than that might start showing some contention at some point.
	 * 
	 * @param key
	 * @param value
	 */
	public void setMemcacheValue(String key, Object value)
	{
		boolean success = false;
		do
		{
			IdentifiableValue oldValue = getMC().getIdentifiable(key);
			if (oldValue == null)
			{
				success = getMC().put(key, value, null, SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
			}
			else
				success = getMC().putIfUntouched(key, oldValue, value);
		}
		while (success == false);
	}

	/**
	 * Adds to a counter in memcache and returns the resulting amount.
	 * 
	 * Be careful not to allow this call to be used too often on the same key.
	 * Something like this is ok to be used every other second or so, but less
	 * than that might start showing some contention at some point.
	 * 
	 * @param key
	 * @param delta
	 * @return
	 */
	public long addToMemcacheNumber(String key, long delta)
	{
		MemcacheService mc = getMC();
		do
		{
			IdentifiableValue oldValue = getMC().getIdentifiable(key);
			if (oldValue == null)
			{
				boolean success = mc.put(key, delta, null, SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
				if (success == true) return delta;
			}
			else
			{
				long newValue = ((Long) oldValue.getValue()) + delta;
				mc.putIfUntouched(key, oldValue, newValue);
				return newValue;
			}
		}
		while (true);
	}

	/**
	 * Fur the methods that look like getCurrentXXX() we store the entities
	 * returned in the request for quick subsequent access. In some cases, this
	 * might make the view we redirect to (like a jsp) stale. For example, if we
	 * switch characters the old character will be shown in the view until a the
	 * player refreshes the page.
	 * 
	 * To combat this, for the specific cases where this issue might arise, call
	 * this method to clear all the request-level cached entities.
	 * 
	 * @param request
	 */
	public void clearRequestCache(HttpServletRequest request)
	{
		request.setAttribute("userEntity", null);
		request.setAttribute("characterEntity", null);
	}

	/**
	 * Returns true if the player is currently logged in using either a regular
	 * account or a throwaway character.
	 * 
	 * @param request
	 * @return
	 */
	public boolean isLoggedIn(HttpServletRequest request)
	{
		HttpSession session = request.getSession(true);

		Long authenticatedInstantCharacterId = (Long) session.getAttribute("instantCharacterId");
		Long userId = (Long) session.getAttribute("userId");
		if (userId == null && authenticatedInstantCharacterId == null) return false;

		return true;
	}

	/**
	 * Gets the key of the user who is currently logged in (if they are logged
	 * in).
	 * 
	 * This will return null if: 1. The user is not currently logged in. 2. The
	 * user is currently using a throwaway account and so only has a throwaway
	 * character but no user entity.
	 * 
	 * @param request
	 * @return
	 */
	public Key getCurrentUserKey(HttpServletRequest request)
	{
		HttpSession session = request.getSession(true);

		Long userId = (Long) session.getAttribute("userId");

		return createKey("User", userId);
	}

	/**
	 * Gets the user entity of the user who is currently logged in (if they are
	 * logged in).
	 * 
	 * This will return null if: 1. The user is not currently logged in. 2. The
	 * user is currently using a throwaway account and so only has a throwaway
	 * character but no user entity.
	 * 
	 * @param request
	 * @return
	 */
	public CachedEntity getCurrentUser(HttpServletRequest request)
	{
		if (request.getAttribute("userEntity") != null) return (CachedEntity) request.getAttribute("userEntity");

		Key userKey = getCurrentUserKey(request);

		if (userKey == null) return null;

		CachedEntity user = getEntity(userKey);

		// For efficiency, we're going to keep the user entity in the request so
		// we don't have to fetch it from the DB more than once in a single
		// request
		request.setAttribute("userEntity", user);

		return user;
	}

	/**
	 * Gets the character entity of the user who is currently logged in (if they
	 * are logged in).
	 * 
	 * This will return null if The user is not currently logged in or if the
	 * user has no character on his account for some reason.
	 * 
	 * @param request
	 * @return
	 */
	public CachedEntity getCurrentCharacter(HttpServletRequest request)
	{
		if (request==null) throw new IllegalArgumentException("Request cannot be null.");
		
		if (request.getAttribute("characterEntity") != null) return (CachedEntity) request.getAttribute("characterEntity");

		HttpSession session = request.getSession(true);

		Long authenticatedInstantCharacterId = (Long) session.getAttribute("instantCharacterId");
		Long userId = (Long) session.getAttribute("userId");
		if (userId != null)
		{
			CachedEntity user = getCurrentUser(request);
			Key characterKey = (Key) user.getProperty("characterKey");
			CachedEntity character = getEntity(characterKey);

			if (character != null)
			{
				request.setAttribute("characterEntity", character);

				return character;
			}
		}

		if (authenticatedInstantCharacterId != null)
		{
			Key characterKey = createKey("Character", authenticatedInstantCharacterId);
			CachedEntity character = getEntity(characterKey);

			if (character != null)
			{
				request.setAttribute("characterEntity", character);

				return character;
			}
		}

		return null;

	}

	/**
	 * Gets an entity from the database by it's kind and ID. If no entity was
	 * found, this method will simply return null.
	 * 
	 * @param kind
	 *            Must not be null.
	 * @param id
	 *            If this is null, the method will return null.
	 * @return
	 */
	public CachedEntity getEntity(String kind, Long id)
	{
		Key key = createKey(kind, id);
		try
		{
			return ds.get(key);
		}
		catch (EntityNotFoundException e)
		{
			return null;
		}
	}

	/**
	 * Creates a datastore key out of a kind and ID.
	 * 
	 * @param kind
	 *            Must not be null.
	 * @param id
	 *            If this is null, the method will return null.
	 * @return
	 */
	public Key createKey(String kind, Long id)
	{
		if (id == null) return null;
		if (kind == null) throw new IllegalArgumentException("Kind cannot be null.");

		return KeyFactory.createKey(kind, id);
	}

	/**
	 * EFFICIENTLY fetches a bunch of entities from a list of keys.
	 * 
	 * If it is possible to use this, please use it instead of calling
	 * getEntity() back to back.
	 * 
	 * @param keyList
	 * @return
	 */
	public List<CachedEntity> getEntities(List<Key> keyList)
	{
		return ds.fetchEntitiesFromKeys(keyList);
	}

	/**
	 * EFFICIENTLY fetches a bunch of entities from a list of keys.
	 * 
	 * If it is possible to use this, please use it instead of calling
	 * getEntity() back to back.
	 * 
	 * @param keyList
	 * @return
	 */
	public List<CachedEntity> getEntities(Key... keyList)
	{
		return ds.fetchEntitiesFromKeys(keyList);
	}

	/**
	 * Fetches the CachedEntity from the given key.
	 * 
	 * If key is null, this method will return null.
	 * 
	 * @param key
	 * @return
	 */
	public CachedEntity getEntity(Key key)
	{
		if (key == null) return null;
		try
		{
			return getDB().get(key);
		}
		catch (EntityNotFoundException e)
		{
			// Ignore
		}
		return null;
	}

	/**
	 * Use this if you only want to get a count of entities in the database.
	 * 
	 * This method will only count up to a maximum of 1000 entities.
	 * 
	 * This is more efficient than using the other filtered methods because it
	 * doesn't actually fetch all the data from the database, only a list of
	 * keys.
	 * 
	 * @param kind
	 * @param fieldName
	 * @param operator
	 * @param equalToValue
	 * @return
	 */
	public Integer getFilteredList_Count(String kind, String fieldName, FilterOperator operator, Object equalToValue)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, operator, equalToValue);
		return getDB().fetchAsList(kind, f1, 1000).size();
	}

	public List<CachedEntity> getFilteredList(String kind, int limit, Cursor cursor, String fieldName, FilterOperator operator, Object equalToValue)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, operator, equalToValue);
		return getDB().fetchAsList(kind, f1, limit, cursor);
	}

	public List<CachedEntity> getFilteredList(String kind, int limit, String fieldName, FilterOperator operator, Object equalToValue)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, operator, equalToValue);
		return getDB().fetchAsList(kind, f1, limit);
	}

	public List<CachedEntity> getFilteredList(String kind, String fieldName, FilterOperator operator, Object equalToValue)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, operator, equalToValue);
		return getDB().fetchAsList(kind, f1, 1000);
	}

	public Integer getFilteredList_Count(String kind, String fieldName, FilterOperator operator, Object equalToValue, String fieldName2, FilterOperator operator2, Object equalToValue2)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, operator, equalToValue);
		FilterPredicate f2 = new FilterPredicate(fieldName2, operator2, equalToValue2);
		Filter f = CompositeFilterOperator.and(f1, f2);
		return getDB().fetchAsList(kind, f, 1000).size();
	}

	public List<CachedEntity> getFilteredList(String kind)
	{
		return getDB().fetchAsList(kind, null, 1000);
	}

	public List<CachedEntity> getFilteredList(String kind, String fieldName, Object equalToValue)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, FilterOperator.EQUAL, equalToValue);
		return getDB().fetchAsList(kind, f1, 1000);
	}

	public List<CachedEntity> getFilteredList(String kind, String fieldName, Object equalToValue, String fieldName2, Object equalToValue2)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, FilterOperator.EQUAL, equalToValue);
		FilterPredicate f2 = new FilterPredicate(fieldName2, FilterOperator.EQUAL, equalToValue2);
		Filter filter = CompositeFilterOperator.and(f1, f2);
		return getDB().fetchAsList(kind, filter, 1000);
	}

	public List<CachedEntity> getFilteredORList(Cursor cursor, String kind, String fieldName, Object equalToValue, String fieldName2, Object equalToValue2)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, FilterOperator.EQUAL, equalToValue);
		FilterPredicate f2 = new FilterPredicate(fieldName2, FilterOperator.EQUAL, equalToValue2);
		Filter filter = CompositeFilterOperator.or(f1, f2);
		Query q = new Query(kind);
		q.setFilter(filter);

		return getDB().fetchAsList(q, 1000, cursor);
	}

	public List<CachedEntity> getFilteredORList(Cursor cursor, String kind, String fieldName, Object equalToValue, String fieldName2, Object equalToValue2, String fieldName3, Object equalToValue3)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, FilterOperator.EQUAL, equalToValue);
		FilterPredicate f2 = new FilterPredicate(fieldName2, FilterOperator.EQUAL, equalToValue2);
		FilterPredicate f3 = new FilterPredicate(fieldName3, FilterOperator.EQUAL, equalToValue3);
		Filter filter = CompositeFilterOperator.or(f1, f2, f3);
		Query q = new Query(kind);
		q.setFilter(filter);

		return getDB().fetchAsList(q, 1000, cursor);
	}

	public CachedEntity getUserById(long id)
	{
		try
		{
			return getDB().get(createKey("User", id));
		}
		catch (EntityNotFoundException e)
		{
			// ignore
		}
		return null;
	}

	public CachedEntity getUserByEmail(String email)
	{
		email = email.trim().toLowerCase();
		Query q = new Query("User").setFilter(new FilterPredicate("email", FilterOperator.EQUAL, email));
		return getDB().fetchSingleEntity(q);
	}

	public CachedEntity getUserByUsername(String username)
	{
		username = username.trim().toLowerCase();
		Query q = new Query("User").setFilter(new FilterPredicate("username", FilterOperator.EQUAL, username));
		return getDB().fetchSingleEntity(q);
	}

	public CachedEntity getUserByCharacterName(String characterName)
	{
		CachedEntity character = getCharacterByName(characterName);
		Query q = new Query("User").setFilter(new FilterPredicate("characterKey", FilterOperator.EQUAL, character.getKey()));
		return getDB().fetchSingleEntity(q);
	}

	public CachedEntity getUserByCharacterKey(Key characterKey)
	{
		Query q = new Query("User").setFilter(new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey));
		return getDB().fetchSingleEntity(q);
	}

	public double calculateHitpoints(double strength)
	{
		return 16d + Math.ceil(strength * 3);
	}

	public boolean checkCharacterExistsByName(String name)
	{
		FilterPredicate f1 = new FilterPredicate("name", FilterOperator.EQUAL, name);
		Query q = new Query("Character").setFilter(f1).setKeysOnly();
		return (getDB().fetchAsList(q, 1).isEmpty() == false);
	}

	public CachedEntity getCharacterByName(String name)
	{
		List<CachedEntity> names = getFilteredList("Character", "name", name);
		if (names.isEmpty()) return null;

		return names.get(0);
	}

	public List<CachedEntity> getCharactersByName(String name)
	{
		List<CachedEntity> names = getFilteredList("Character", "name", name);
		if (names.isEmpty()) return null;

		return names;
	}

	public CachedEntity getCharacterById(Long id)
	{
		if (id == null) return null;
		try
		{
			return getDB().get(KeyFactory.createKey("Character", id));
		}
		catch (EntityNotFoundException e)
		{
			// ignore
		}
		return null;
	}

	/**
	 * Returns the combatant that the given character is currently fighting, or
	 * null.
	 * 
	 * @param character
	 * @return
	 */
	public CachedEntity getCharacterCombatant(CachedEntity character)
	{
		String charMode = (String) character.getProperty("mode");
		if (charMode == null || charMode.equals("COMBAT") == false) return null;

		return getEntity((Key) character.getProperty("combatant"));
	}

	public List<CachedEntity> getCharacterMerchants(CachedDatastoreService db, Key location)
	{
		if (db == null) db = getDB();
		Query q = new Query("Character").setFilter(
				CompositeFilterOperator.and(new FilterPredicate("locationKey", FilterOperator.EQUAL, location), new FilterPredicate("mode", FilterOperator.EQUAL, "MERCHANT"))).addSort(
				"locationEntryDatetime", SortDirection.DESCENDING);
		return db.fetchAsList(q, 50);
	}

	/**
	 * This is really only for fixing the DB. We should never have to call this.
	 * 
	 * @param db
	 * @return
	 */
	public Iterable<CachedEntity> getAllCharacterMerchants(CachedDatastoreService db)
	{
		if (db == null) db = getDB();
		Query q = new Query("Character").setFilter(new FilterPredicate("mode", FilterOperator.EQUAL, "MERCHANT"));
		return db.fetchAsIterable(q);
	}

	/**
	 * 
	 * @param banner
	 * @param name
	 * @param description
	 * @param discoverAnythingChance
	 *            The PERCENTAGE chance of you discovering anything at all (this
	 *            doesn't guarantee a discovery, its just the chance that you
	 *            can even roll to discover something)
	 * @return
	 */
	public CachedEntity newLocation(CachedDatastoreService db, String banner, String name, String description, Double discoverAnythingChance, String type, Key parentLocationKey, Key ownerKey)
	{
		if (db == null) db = getDB();

		CachedEntity location = new CachedEntity("Location");
		// Set the starting attributes
		location.setProperty("banner", banner);
		location.setProperty("name", name);
		location.setProperty("description", description);
		location.setProperty("discoverAnythingChance", discoverAnythingChance);
		location.setProperty("type", type);
		location.setProperty("parentLocationKey", parentLocationKey);
		location.setProperty("ownerKey", ownerKey);

		// Set some default attributes

		db.put(location);
		return location;
	}

	public Key getDefaultLocationKey()
	{
		return KeyFactory.createKey("Location", 5629499534213120l);
	}

	public CachedEntity getLocationById(Long id)
	{
		if (id == null) return null;
		try
		{
			return getDB().get(KeyFactory.createKey("Location", id));
		}
		catch (EntityNotFoundException e)
		{
			// ignore
		}
		return null;
	}

	public CachedEntity getLocationByName(String name)
	{
		Query q = new Query("Location").setFilter(new FilterPredicate("name", FilterOperator.EQUAL, name));
		return getDB().fetchSingleEntity(q);
	}

	public List<CachedEntity> getAllLocations()
	{
		Query q = new Query("Location");
		return getDB().fetchAsList(q, 1000);
	}

	public List<CachedEntity> getAllCombatSites(Cursor cursor)
	{
		Query q = new Query("Location").setFilter(new FilterPredicate("type", FilterOperator.EQUAL, "CombatSite"));
		return getDB().fetchAsList(q, 1000, cursor);
	}

	public List<CachedEntity> getAllCharacters(Cursor cursor)
	{
		Query q = new Query("Character");
		return getDB().fetchAsList(q, 1000, cursor);
	}

	public Iterable<CachedEntity> getLocationsForMonsterLevelUpdate()
	{
		Query q = new Query("Location").setFilter(CompositeFilterOperator.or(new FilterPredicate("type", FilterOperator.EQUAL, "Permanent"), new FilterPredicate("type", FilterOperator.EQUAL,
				"CampSite")));
		return getDB().fetchAsIterable(q);
	}

	public Iterable<CachedEntity> getLocations_CampSites()
	{
		Query q = new Query("Location").setFilter(new FilterPredicate("type", FilterOperator.EQUAL, "CampSite"));
		return getDB().fetchAsIterable(q);
	}

	public List<CachedEntity> getCampsitesByParentLocation(CachedDatastoreService db, Key parentLocationKey)
	{
		if (db == null) db = getDB();
		Query q = new Query("Location").setFilter(CompositeFilterOperator.and(new FilterPredicate("type", FilterOperator.EQUAL, "CampSite"), new FilterPredicate("parentLocationKey",
				FilterOperator.EQUAL, parentLocationKey)));
		return db.fetchAsList(q, 1000);
	}

	public CachedEntity newPath(CachedDatastoreService db, String internalName, Key location1Key, Key location2Key, double discoveryChance, Long travelTime, String type)
	{
		return newPath(db, internalName, location1Key, null, location2Key, null, discoveryChance, travelTime, type);
	}

	public CachedEntity newPath(CachedDatastoreService db, String internalName, Key location1Key, String location2ButtonNameOverride, Key location2Key, String location1ButtonNameOverride,
			double discoveryChance, Long travelTime, String type)
	{
		if (db == null) db = getDB();

		CachedEntity path = new CachedEntity("Path");
		// Set the starting attributes
		path.setProperty("name", internalName);
		path.setProperty("location1Key", location1Key);
		path.setProperty("location2Key", location2Key);
		path.setProperty("discoveryChance", discoveryChance);
		path.setProperty("location1ButtonNameOverride", location1ButtonNameOverride);
		path.setProperty("location2ButtonNameOverride", location2ButtonNameOverride);
		path.setProperty("discoveryChance", discoveryChance);
		path.setProperty("travelTime", travelTime);
		path.setProperty("type", type);

		// Set some default attributes
		db.put(path);

		return path;
	}

	public CachedEntity getPathById(Long id)
	{
		if (id == null) return null;
		try
		{
			return getDB().get(KeyFactory.createKey("Path", id));
		}
		catch (EntityNotFoundException e)
		{
			// ignore
		}
		return null;
	}

	public List<CachedEntity> getPathsByLocation_PermanentOnly(Key locationKey)
	{
		FilterPredicate permanent = new FilterPredicate("type", FilterOperator.EQUAL, "Permanent");
		FilterPredicate camp = new FilterPredicate("type", FilterOperator.EQUAL, "CampSite");
		FilterPredicate location1 = new FilterPredicate("location1Key", FilterOperator.EQUAL, locationKey);
		FilterPredicate location2 = new FilterPredicate("location2Key", FilterOperator.EQUAL, locationKey);
		Filter f = CompositeFilterOperator.and(permanent, location1);
		// Filter f = CompositeFilterOperator.or(
		// CompositeFilterOperator.and(permanent, location1),
		// CompositeFilterOperator.and(permanent, location2),
		// CompositeFilterOperator.and(camp, location1),
		// CompositeFilterOperator.and(camp, location2)
		// );
		Query q = new Query("Path").setFilter(f);
		List<CachedEntity> location1List = getDB().fetchAsList(q, 1000);

		f = CompositeFilterOperator.and(permanent, location2);
		q = new Query("Path").setFilter(f);
		List<CachedEntity> location2List = getDB().fetchAsList(q, 1000);

		location1List.addAll(location2List);

		return location1List;

	}

	public List<CachedEntity> getPathsByLocationAndType(Key locationKey, String type)
	{
		FilterPredicate camp = new FilterPredicate("type", FilterOperator.EQUAL, type);
		FilterPredicate location1 = new FilterPredicate("location1Key", FilterOperator.EQUAL, locationKey);
		FilterPredicate location2 = new FilterPredicate("location2Key", FilterOperator.EQUAL, locationKey);

		Filter f = CompositeFilterOperator.and(camp, location1);
		Query q = new Query("Path").setFilter(f);
		List<CachedEntity> location1List = getDB().fetchAsList(q, 1000);

		f = CompositeFilterOperator.and(camp, location2);
		q = new Query("Path").setFilter(f);
		List<CachedEntity> location2List = getDB().fetchAsList(q, 1000);

		location1List.addAll(location2List);

		return location1List;

	}

	public List<CachedEntity> getAllPathsExcludingCombatSites()
	{
		FilterPredicate f1 = new FilterPredicate("type", FilterOperator.EQUAL, "Permanent");
		FilterPredicate f2 = new FilterPredicate("type", FilterOperator.EQUAL, "PlayerHouse");
		Filter f = CompositeFilterOperator.or(f1, f2);
		Query q = new Query("Path").setFilter(f);
		return getDB().fetchAsList(q, 1000);
	}

	public List<CachedEntity> getPathsByLocation(Key locationKey)
	{
		FilterPredicate f1 = new FilterPredicate("location1Key", FilterOperator.EQUAL, locationKey);
		FilterPredicate f2 = new FilterPredicate("location2Key", FilterOperator.EQUAL, locationKey);
		Filter f = CompositeFilterOperator.or(f1, f2);
		Query q = new Query("Path").setFilter(f);
		return getDB().fetchAsList(q, 1000);
	}

	public List<CachedEntity> getPathsByLocation_KeysOnly(Key locationKey)
	{
		FilterPredicate f1 = new FilterPredicate("location1Key", FilterOperator.EQUAL, locationKey);
		FilterPredicate f2 = new FilterPredicate("location2Key", FilterOperator.EQUAL, locationKey);
		Filter f = CompositeFilterOperator.or(f1, f2);
		Query q = new Query("Path").setFilter(f).setKeysOnly();
		return getDB().fetchAsList(q, 1000);
	}

	public List<CachedEntity> getMonsterSpawnersForLocation(Key locationKey)
	{
		FilterPredicate f1 = new FilterPredicate("locationKey", FilterOperator.EQUAL, locationKey);
		Query q = new Query("MonsterSpawner").setFilter(f1);
		return getDB().fetchAsList(q, 1000);
	}

	public List<CachedEntity> getCollectableSpawnersForLocation(Key locationKey)
	{
		FilterPredicate f1 = new FilterPredicate("locationKey", FilterOperator.EQUAL, locationKey);
		Query q = new Query("CollectableSpawner").setFilter(f1);
		return getDB().fetchAsList(q, 1000);
	}

	public List<CachedEntity> getCollectablesForLocation(Key locationKey)
	{
		FilterPredicate f1 = new FilterPredicate("locationKey", FilterOperator.EQUAL, locationKey);
		Query q = new Query("Collectable").setFilter(f1);
		return getDB().fetchAsList(q, 1000);
	}

	public CachedEntity newSaleItem(CachedDatastoreService db, CachedEntity character, CachedEntity item, long dogecoins)
	{
		if (db == null) db = getDB();

		CachedEntity result = new CachedEntity("SaleItem");
		result.setProperty("characterKey", character.getKey());
		result.setProperty("itemKey", item.getKey());
		result.setProperty("dogecoins", dogecoins);

		result.setProperty("status", "Selling");
		result.setProperty("name", character.getProperty("name") + " - " + item.getProperty("name") + " - " + dogecoins);

		db.put(result);

		return result;
	}

	public List<CachedEntity> getSaleItemsFor(Key characterKey)
	{
		FilterPredicate f1 = new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey);
		Query q = new Query("SaleItem").setFilter(f1);
		return getDB().fetchAsList(q, 1000);
	}

	public boolean checkItemIsVending(Key characterKey, Key itemKey)
	{
		Filter f1 = CompositeFilterOperator.and(new FilterPredicate("itemKey", FilterOperator.EQUAL, itemKey), new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey));
		Query q = new Query("SaleItem").setFilter(f1);
		return (getDB().fetchAsList(q, 1).isEmpty() == false);
	}

	public boolean checkItemBeingSoldAlready(Key characterKey, Key itemKey)
	{
		FilterPredicate f1 = new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey);
		FilterPredicate f2 = new FilterPredicate("itemKey", FilterOperator.EQUAL, itemKey);
		Query q = new Query("SaleItem").setFilter(CompositeFilterOperator.and(f1, f2)).setKeysOnly();
		return (getDB().fetchAsList(q, 1).isEmpty() == false);
	}

	public CachedEntity newDiscovery(CachedDatastoreService db, CachedEntity character, CachedEntity entity)
	{
		if (db == null) db = getDB();

		CachedEntity oldDiscovery = getDiscoveryByEntity(character.getKey(), entity.getKey());
		if (oldDiscovery != null) return oldDiscovery;

		if (entity.getKind().equals("Path"))
		{
			CachedEntity discovery = new CachedEntity("Discovery");
			// Set the starting attributes
			discovery.setProperty("characterKey", character.getKey());
			discovery.setProperty("entityKey", entity.getKey());
			discovery.setProperty("kind", entity.getKind());
			discovery.setProperty("location1Key", entity.getProperty("location1Key"));
			discovery.setProperty("location2Key", entity.getProperty("location2Key"));

			// Set some default attributes

			db.put(discovery);

			return discovery;
		}
		else if (entity.getKind().equals("Character"))
		{
			CachedEntity discovery = new CachedEntity("Discovery");
			// Set the starting attributes
			discovery.setProperty("characterKey", character.getKey());
			discovery.setProperty("entityKey", entity.getKey());
			discovery.setProperty("kind", entity.getKind());

			// Set some default attributes

			db.put(discovery);
			return discovery;
		}
		else
			throw new RuntimeException("Unhandled discovery type");
	}

	public CachedEntity getDiscoveryById(Long id)
	{
		if (id == null) return null;
		try
		{
			return getDB().get(KeyFactory.createKey("Discovery", id));
		}
		catch (EntityNotFoundException e)
		{
			// ignore
		}
		return null;
	}

	public CachedEntity getDiscoveryByEntity(Key characterKey, Key entityKey)
	{
		Query q = new Query("Discovery").setFilter(CompositeFilterOperator.and(new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey), new FilterPredicate("entityKey",
				FilterOperator.EQUAL, entityKey)));

		List<CachedEntity> discoveries = getDB().fetchAsList(q, 1000);
		if (discoveries.size() > 1)
		{
			Logger.getLogger("DBAccess").log(
					Level.SEVERE,
					"Somehow while running DBAccess.getDiscoveryByEntity(), multiple discoveries for the same person and entity were found in the DB. They have been deleted, but this should be fixed. CharacterKey="
							+ characterKey + ", EntityKey=" + entityKey);
			CachedDatastoreService db = getDB();
			for (int i = 1; i < discoveries.size(); i++)
				db.delete(discoveries.get(i).getKey());
		}
		if (discoveries.isEmpty())
			return null;
		else
			return discoveries.get(0);
	}

	/**
	 * THIS IS ONLY FOR FIXING THE DB
	 * 
	 * @param entityKey
	 * @return
	 */
	public Iterable<CachedEntity> getDiscoveriesByEntity(Key entityKey)
	{
		Query q = new Query("Discovery").setFilter(new FilterPredicate("entityKey", FilterOperator.EQUAL, entityKey));

		Iterable<CachedEntity> discoveries = getDB().fetchAsIterable(q);
		return discoveries;
	}

	public List<CachedEntity> getDiscoveriesForCharacterAndLocation(Key characterKey, Key location)
	{
		Query q = new Query("Discovery").setFilter(CompositeFilterOperator.and(new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey),
				CompositeFilterOperator.or(new FilterPredicate("location1Key", FilterOperator.EQUAL, location), new FilterPredicate("location2Key", FilterOperator.EQUAL, location))));
		List<CachedEntity> fetchAsList = getDB().fetchAsList(q, 1000);
		System.out.println(fetchAsList.size() + " discoveries found.");
		return fetchAsList;
	}

	public List<CachedEntity> getDiscoveriesForCharacter_KeysOnly(Key characterKey)
	{
		Query q = new Query("Discovery").setFilter(new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey)).setKeysOnly();
		return getDB().fetchAsList(q, 1000);
	}

	public List<CachedEntity> getDiscoveriesForCharacterByKind(Key characterKey, String discoveryKind)
	{
		Query q = new Query("Discovery").setFilter(CompositeFilterOperator.and(new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey), new FilterPredicate("kind",
				FilterOperator.EQUAL, discoveryKind)));
		return getDB().fetchAsList(q, 1000);
	}

	public CachedEntity getItemDefinitionById(Long id)
	{
		if (id == null) return null;
		try
		{
			return getDB().get(KeyFactory.createKey("ItemDef", id));
		}
		catch (EntityNotFoundException e)
		{
			// ignore
		}
		return null;
	}

	public CachedEntity getItemDefinitionByClassName(String className)
	{
		Query q = new Query("ItemDef").setFilter(new FilterPredicate("itemClassName", FilterOperator.EQUAL, className));
		return getDB().fetchSingleEntity(q);
	}

	public CachedEntity getNPCDefinitionById(Long id)
	{
		if (id == null) return null;
		try
		{
			return getDB().get(KeyFactory.createKey("NPCDef", id));
		}
		catch (EntityNotFoundException e)
		{
			// ignore
		}
		return null;
	}

	public CachedEntity getNPCDefinitionByClassName(String className)
	{
		Query q = new Query("NPCDef").setFilter(new FilterPredicate("npcClassName", FilterOperator.EQUAL, className));
		return getDB().fetchSingleEntity(q);
	}

	public boolean checkStoreNameUnique(String storeName)
	{
		storeName = storeName.trim().toLowerCase();
		Query q = new Query("Character").setKeysOnly().setFilter(new FilterPredicate("storeName", FilterOperator.EQUAL, storeName)).setKeysOnly();
		if (getDB().fetchAsList(q, 1).size() == 0)
			return true;
		else
			return false;
	}

	public List<CachedEntity> getStoresListFor(Key locationKey)
	{
		Query q = new Query("Character").setFilter(CompositeFilterOperator.and(new FilterPredicate("locationKey", FilterOperator.EQUAL, locationKey), new FilterPredicate("mode", FilterOperator.EQUAL,
				"MERCHANT")));
		return getDB().fetchAsList(q, 51);
	}
	
	
	public boolean isChatbanned(CachedDatastoreService db, String characterName)
	{
		if (db == null) db = getDB();

		Long isChatbanned = db.getStat("chatban-" + characterName);
		if (isChatbanned != null && 1l == isChatbanned) return true;

		return false;
	}

	public void chatbanByIP(String characterName, String ip)
	{
		CachedDatastoreService db = getDB();
		
		// Ignore if char is Dev ("content-dev-nickname" needs to be set)
		// Dev can still be banned by IP if on regular char
		// See isChatbannedByIP for auto unban in that case
		CachedEntity character = getCharacterByName(characterName);
		if (character!=null)
		{
			String nameClass = (String)character.getProperty("nameClass");
			if (nameClass!=null && nameClass.contains("content-dev-nickname"))
				return;
		}
		
		db.setStat("chatbanIP-" + ip, 1l, 3600 / 2);

		db.setStat("chatban-" + characterName, 1l, 3600 / 2);
	}

	public void unchatban(String characterName, String ip)
	{
		CachedDatastoreService db = getDB();
		db.setStat("chatbanIP-" + ip, null);

		db.setStat("chatban-" + characterName, null);
	}

	public boolean isChatbannedByIP(CachedDatastoreService db, String characterName, String ip)
	{
		if (db == null) db = getDB();
		
		Long isChatbannedByIP = db.getStat("chatbanIP-" + ip);
		if (isChatbannedByIP != null && 1l == isChatbannedByIP)
		{
			// Unban if char is Dev ("content-dev-nickname" needs to be set)
			// This is possible if IP got banned by non Dev char
			CachedEntity character = getCharacterByName(characterName);
			if (character!=null)
			{
				String nameClass = (String)character.getProperty("nameClass");
				if (nameClass!=null && nameClass.contains("content-dev-nickname"))
				{
					unchatban(characterName, ip);
					return false;
				}
			}
			return true;
		}

		return isChatbanned(db, characterName);
	}

	public CachedEntity getGroupByName(String name)
	{
		List<CachedEntity> names = getFilteredList("Group", "name", name);
		if (names.isEmpty()) return null;

		return names.get(0);
	}

	public boolean isCharacterNameOk(HttpServletRequest request, String characterName) throws UserErrorMessage
	{
		if (characterName == null) throw new UserErrorMessage("Character name cannot be blank.");
		characterName = characterName.trim();
		if (characterName.length() < 1 || characterName.length() > 30 || !characterName.matches("[A-Za-z ]+"))
			throw new UserErrorMessage("Character name must contain only letters and spaces, and must be between 1 and 30 characters long.");

		if (characterName.startsWith("Dead ")) throw new UserErrorMessage("Character name cannot start with the word Dead.");

		if (characterName.startsWith("Unconscious ")) throw new UserErrorMessage("Character name cannot start with the word Unconscious.");

		if (getCharacterByName(characterName) != null) throw new UserErrorMessage("Character name already exists in our database. Please choose another.");

		String ip = WebUtils.getClientIpAddr(request);
		CachedDatastoreService ds = getDB();
		if (ds.flagActionLimiter("signupIPLimiter" + ip, 600, 1)) throw new UserErrorMessage("Please report this error to the admin.");

		return true;
	}

	public void doCharacterEquipEntity(CachedDatastoreService db, CachedEntity character, CachedEntity equipment) throws UserErrorMessage
	{
		doCharacterEquipEntity(db, character, equipment, true);
	}

	public void doCharacterEquipEntity(CachedDatastoreService db, CachedEntity character, CachedEntity equipment, boolean replaceItem) throws UserErrorMessage
	{
		if (db == null) db = getDB();

		// Get all the equip slots that this item can fit into and decide which
		// one to use
		String equipSlotRaw = (String) equipment.getProperty("equipSlot");
		equipSlotRaw = equipSlotRaw.trim();
		if (equipSlotRaw.endsWith(",")) equipSlotRaw = equipSlotRaw.substring(0, equipSlotRaw.length() - 1);
		String[] equipSlotArr = equipSlotRaw.split(",");

		String destinationSlot = null;
		if (equipSlotArr.length == 0)
			throw new RuntimeException("No equip slots exist for the '" + equipment.getProperty("name") + "' item.");
		else if (equipSlotArr.length == 1)
			destinationSlot = equipSlotArr[0];
		else if (equipSlotArr.length > 1)
		{
			for (int i = 0; i < equipSlotArr.length; i++)
			{
				if (character.getProperty("equipment" + equipSlotArr[i]) == null)
				{
					destinationSlot = equipSlotArr[i];
					break;
				}
			}

			if (destinationSlot == null) throw new UserErrorMessage("The equipment slots needed to equip this item are all used. Unequip something first.");
		}

		if (destinationSlot == null) throw new RuntimeException("There was no equipSlot specified for this item: " + equipment);

		if (character == null) throw new IllegalArgumentException("Character cannot be null.");
		if (equipment == null) throw new IllegalArgumentException("Equipment cannot be null.");
		if (destinationSlot == null) throw new IllegalArgumentException("destinationSlot cannot be null.");

		if (character.getKey().equals(equipment.getProperty("containerKey")) == false) throw new IllegalArgumentException("The piece of equipment is not in the character's posession.");

		destinationSlot = destinationSlot.trim(); // Clean it up, just in case

		String equipmentSlot = (String) equipment.getProperty("equipSlot");
		if (equipmentSlot == null) throw new UserErrorMessage("You cannot equip this item.");
		if (equipmentSlot.contains(destinationSlot) == false) throw new RuntimeException("You cannot put a " + equipmentSlot + " item in the " + destinationSlot + " slot.");

		Double characterStrength = (Double) character.getProperty("strength");
		// ROund character strength just like it is rounded for the popup
		characterStrength = Double.parseDouble(GameUtils.formatNumber(characterStrength));
		if (equipment.getProperty("strengthRequirement") instanceof String) equipment.setProperty("strengthRequirement", null);
		Double strengthRequirement = (Double) equipment.getProperty("strengthRequirement");
		if (strengthRequirement != null && characterStrength != null && strengthRequirement > characterStrength && "NPC".equals(character.getProperty("type")) == false)
			throw new UserErrorMessage("You cannot equip this item, you do not have the strength to use it.");

		if (isItemForSale(db, equipment)) throw new UserErrorMessage("You cannot equip this item, it is currently for sale.");

		if (destinationSlot.equals("2Hands")) destinationSlot = "LeftHand and RightHand";

		if (destinationSlot != null && destinationSlot.trim().equals("") == false)
		{
			String[] destinationSlots = destinationSlot.split(" and ");

			// Check if we need to unequip some items first if they're in the
			// way (and replacing is requested)
			for (String slot : destinationSlots)
			{

				// If we already have some equipment in the slot we want to
				// equip to, then unequip it first...
				if (character.getProperty("equipment" + slot) != null)
				{
					if (replaceItem == false) return;
					doCharacterUnequipEntity(db, character, slot);
				}
			}

			// Now equip
			for (String slot : destinationSlots)
			{
				// Now equip this weapon...
				character.setProperty("equipment" + slot, equipment.getKey());
			}
		}

		db.put(character);

	}

	public void doCharacterUnequipEntity(CachedDatastoreService db, CachedEntity character, String fromSlot)
	{
		if (character == null) throw new IllegalArgumentException("Character cannot be null.");

		Key equipmentKey = (Key) character.getProperty("equipment" + fromSlot);
		if (equipmentKey == null) return;

		CachedEntity equipment = getEntity(equipmentKey);

		if (equipment == null) return;

		if (db == null) db = getDB();

		doCharacterUnequipEntity(db, character, equipment);

	}

	public void doCharacterUnequipEntity(CachedDatastoreService db, CachedEntity character, CachedEntity equipment)
	{
		if (character == null) throw new IllegalArgumentException("Character cannot be null.");

		if (equipment == null) return;

		if (db == null) db = getDB();

		for (String slot : EQUIPMENT_SLOTS)
		{
			if (character.getProperty("equipment" + slot) != null && ((Key) character.getProperty("equipment" + slot)).getId() == equipment.getKey().getId())
				character.setProperty("equipment" + slot, null);
		}

		db.put(character);

	}

	public void doCharacterUnequipEntity(CachedDatastoreService db, CachedEntity character, Key entityKey)
	{
		if (db == null) db = getDB();

		if (character == null) throw new IllegalArgumentException("Character cannot be null.");

		for (int i = 0; i < EQUIPMENT_SLOTS.length; i++)
		{
			Key equipment = (Key) character.getProperty("equipment" + EQUIPMENT_SLOTS[i]);
			if (equipment != null && entityKey.getId() == equipment.getId())
			{
				doCharacterUnequipEntity(db, character, EQUIPMENT_SLOTS[i]);
			}
		}

	}

	public void doCharacterRestFully(CachedEntity character)
	{
		Double hitpoints = (Double) character.getProperty("maxHitpoints");
		if ((Double) character.getProperty("hitpoints") < hitpoints)
		;
		{
			character.setProperty("hitpoints", hitpoints);

			getDB().put(character);
		}

	}

	public void doCharacterCollectItem(CachedEntity character, CachedEntity item) throws UserErrorMessage
	{
		if (character == null) throw new IllegalArgumentException("Character cannot be null.");
		if (item == null) throw new IllegalArgumentException("Item cannot be null.");

		if (character.getProperty("locationKey").equals(item.getProperty("containerKey")) == false) throw new UserErrorMessage("The item is not here anymore. Perhaps someone else grabbed it.");

		// Check if the character can actually carry something else or if its
		// all too heavy...
		Long newItemWeight = (Long) item.getProperty("weight");
		if (newItemWeight != null && newItemWeight > 0d)
		{
			long carrying = getCharacterCarryingWeight(character);
			long maxCarrying = getCharacterMaxCarryingWeight(character);

			if (carrying + newItemWeight > maxCarrying)
				throw new UserErrorMessage("You cannot carry any more stuff! You are currently carrying " + GameUtils.formatNumber(carrying) + " grams and can carry a maximum of "
						+ GameUtils.formatNumber(maxCarrying) + " grams.");
		}

		item.setProperty("containerKey", character.getKey());
		item.setProperty("movedTimestamp", new Date());

		getDB().put(item);
	}

	public Long getCharacterWeight(CachedEntity character)
	{
		if (character == null) throw new IllegalArgumentException("Character cannot be null.");

		long carrying = getCharacterCarryingWeight(character);

		// Also determine the weight of the character based on his base
		// strength...
		Double str = (Double) character.getProperty("strength");
		long charWeight = Math.round(str * 12500d);

		return carrying + charWeight;
	}

	public void doCharacterDropItem(CachedEntity character, CachedEntity item) throws UserErrorMessage
	{
		if (character == null) throw new IllegalArgumentException("Character cannot be null.");
		if (item == null) throw new IllegalArgumentException("Item cannot be null.");

		Key characterLocationKey = (Key) character.getProperty("locationKey");

		if (checkCharacterHasItemEquipped(character, item.getKey())) throw new UserErrorMessage("Your character has this item equipped. You cannot drop it until it is unequipped.");

		if (checkItemIsVending(character.getKey(), item.getKey()))
			throw new UserErrorMessage("The item you are trying to drop is currently in your store. You cannot drop an item that you plan on selling.");

		item.setProperty("containerKey", characterLocationKey);
		item.setProperty("movedTimestamp", new Date());

		getDB().put(item);
	}

	public void doCharacterDropAllInventory(CachedEntity character) throws UserErrorMessage
	{
		CachedDatastoreService ds = getDB();
		if (character == null) throw new IllegalArgumentException("Character cannot be null.");

		Key characterLocationKey = (Key) character.getProperty("locationKey");

		List<CachedEntity> inventory = getFilteredList("Item", "containerKey", character.getKey());
		for (CachedEntity item : inventory)
		{
			if (checkCharacterHasItemEquipped(character, item.getKey())) continue;

			if (checkItemIsVending(character.getKey(), item.getKey())) continue;

			item.setProperty("containerKey", characterLocationKey);
			item.setProperty("movedTimestamp", new Date());
			ds.put(item);
		}
	}

	/**
	 * Checks if the given character has the given item (itemKey) equipped in
	 * any slot.
	 * 
	 * @param character
	 * @param itemKey
	 * @return true if item is equipped
	 */
	public boolean checkCharacterHasItemEquipped(CachedEntity character, Key itemKey)
	{
		for (int i = 0; i < EQUIPMENT_SLOTS.length; i++)
		{
			Key equipmentInSlot = (Key) character.getProperty("equipment" + EQUIPMENT_SLOTS[i]);
			if (equipmentInSlot != null && itemKey.getId() == equipmentInSlot.getId()) return true;
		}

		return false;
	}

	public Double getCharacterDexterity(CachedEntity character)
	{
		Double dex = getDoubleBuffableProperty(character, "dexterity");

		// Get all dexterity reducing armors and include that in the
		// calculation...
		for (String slot : EQUIPMENT_SLOTS)
		{
			if (character.getProperty("equipment" + slot) != null)
			{
				CachedEntity equipment = getEntity((Key) character.getProperty("equipment" + slot));
				if (equipment == null) continue;
				Long penalty = (Long) equipment.getProperty("dexterityPenalty");
				if (penalty != null)
				{
					dex -= (dex * (penalty.doubleValue() / 100d));
				}
			}
		}

		if (dex < 2) dex = 2d;

		return dex;
	}

	public Double getCharacterStrength(CachedEntity character)
	{
		Double str = getDoubleBuffableProperty(character, "strength");

		return str;
	}

	public Long getItemCarryingSpace(CachedEntity item)
	{
		List<CachedEntity> inventory = getFilteredList("Item", "containerKey", item.getKey());
		return getItemCarryingSpace(item, inventory);
	}

	public Long getItemCarryingWeight(CachedEntity item)
	{
		List<CachedEntity> inventory = getFilteredList("Item", "containerKey", item.getKey());
		return getItemCarryingWeight(item, inventory);
	}

	public Long getCharacterCarryingWeight(CachedEntity character)
	{
		List<CachedEntity> inventory = getFilteredList("Item", "containerKey", character.getKey());
		List<CachedEntity> inventoryCharacters = getFilteredList("Character", "locationKey", character.getKey());
		return getCharacterCarryingWeight(character, inventory, inventoryCharacters);
	}

	public Long getCharacterCarryingWeight(CachedEntity character, List<CachedEntity> inventory, List<CachedEntity> inventoryCharacters)
	{
		long carrying = 0l;

		for (CachedEntity item : inventory)
		{
			Long itemWeight = (Long) item.getProperty("weight");
			if (itemWeight == null) continue;
			// If the item is equipped (not in the left/right hand) then don't
			// count it's weight against us
			if ("LeftHand".equals(item.getProperty("equipSlot")) == false && "RightHand".equals(item.getProperty("equipSlot")) == false && "2Hands".equals(item.getProperty("equipSlot")) == false
					&& checkCharacterHasItemEquipped(character, item.getKey())) continue;

			carrying += itemWeight;
		}

		for (CachedEntity c : inventoryCharacters)
		{
			Long weight = getCharacterWeight(c);

			carrying += weight;
		}

		return carrying;
	}

	public long getCharacterMaxCarryingWeight(CachedEntity character)
	{
		long maxCarryWeight = 60000;
		Double str = (Double) character.getProperty("strength");

		maxCarryWeight += (long) Math.round((str - 3d) * 50000d);
		
		// Allow Buff maxCarryWeight
		return getLongBuffableValue(character, "maxCarryWeight", maxCarryWeight);
	}

	public Long getItemCarryingWeight(CachedEntity character, List<CachedEntity> inventory)
	{
		long carrying = 0l;

		for (CachedEntity item : inventory)
		{
			Long itemWeight = (Long) item.getProperty("weight");
			if (itemWeight == null) continue;

			carrying += itemWeight;
		}

		// for(CachedEntity c:inventoryCharacters)
		// {
		// Long weight = getCharacterWeight(c);
		//
		// carrying+=weight;
		// }

		return carrying;
	}

	public Long getItemCarryingSpace(CachedEntity character, List<CachedEntity> inventory)
	{
		long space = 0l;

		for (CachedEntity item : inventory)
		{
			Long itemWeight = (Long) item.getProperty("space");
			if (itemWeight == null) continue;

			space += itemWeight;
		}

		// for(CachedEntity c:inventoryCharacters)
		// {
		// Long weight = getCharacterWeight(c);
		//
		// carrying+=weight;
		// }

		return space;
	}

	public CachedEntity awardBuff(CachedDatastoreService ds, Key parentKey, String icon, String name, String description, int durationInSeconds, String field1Name, String field1Effect,
			String field2Name, String field2Effect, String field3Name, String field3Effect, int maximumCount)
	{
		List<CachedEntity> buffsAlreadyOn = getBuffsFor(parentKey);

		int existingCount = 0;
		for (CachedEntity b : buffsAlreadyOn)
			if (name.equals(b.getProperty("name"))) existingCount++;

		if (existingCount >= maximumCount) return null;
		CachedEntity buff = new CachedEntity("Buff");

		buff.setProperty("parentKey", parentKey);
		buff.setProperty("icon", icon);
		buff.setProperty("name", name);
		buff.setProperty("description", description);
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.SECOND, durationInSeconds);
		buff.setProperty("expiry", cal.getTime());
		buff.setProperty("field1Name", field1Name);
		buff.setProperty("field1Effect", field1Effect);
		buff.setProperty("field2Name", field2Name);
		buff.setProperty("field2Effect", field2Effect);
		buff.setProperty("field3Name", field3Name);
		buff.setProperty("field3Effect", field3Effect);

		addBuffToBuffsCache(parentKey, buff);
		return buff;
	}

	private void addBuffToBuffsCache(Key parentKey, CachedEntity buff)
	{
		List<CachedEntity> buffs = buffsCache.get(parentKey);

		if (buffs == null)
		{
			buffs = new ArrayList<CachedEntity>();
			buffsCache.put(parentKey, buffs);
		}

		buffs.add(buff);
	}

	public void awardBuff_Pumped(CachedDatastoreService ds, CachedEntity attackingCharacter)
	{
		if (ds == null) ds = getDB();

		CachedEntity buff = awardBuff(ds, attackingCharacter.getKey(), "images/small/Pixel_Art-Icons-Buffs-S_Buff14.png", "Pumped!",
				"You're pumped! This buff is awarded when you kill a monster while still being full health. The effect lasts for 1 minute.", 60, "strength", "+10%", "dexterity", "+10%",
				"intelligence", "+5%", 3);

		if (buff != null) ds.put(buff);
	}

	public void awardBuff_Drunk(CachedDatastoreService ds, CachedEntity character)
	{
		if (ds == null) ds = getDB();

		CachedEntity buff = awardBuff(ds, character.getKey(), "images/small2/Pixel_Art-Misc-Beer-Stein1.png", "Drunk",
				"You're drunk! This effect is awarded when you drink at the Inn in Aera. The effect lasts for 20 minutes.", 1200, "strength", "+5%", "dexterity", "-10%",
				"intelligence", "-10%", 6);

		if (buff != null) ds.put(buff);
	}

	public void awardBuff_WellRested(CachedDatastoreService ds, CachedEntity character)
	{
		if (ds == null) ds = getDB();

		CachedEntity buff = awardBuff(ds, character.getKey(), "images/small2/Pixel_Art-Armor-Icons-Moon1.png", "Well Rested",
				"You are well rested. This happens when you rest in a nice location (like at a house). The effect lasts for 15 minutes.", 900, "strength", "+10%", "movementSpeed", "+40%", null, null,
				1);

		if (buff != null) ds.put(buff);
	}

	public List<CachedEntity> sortSaleItemList(List<CachedEntity> items)
	{
		List<CachedEntity> sorted = new ArrayList<CachedEntity>(items);
		Collections.sort(sorted, new Comparator<CachedEntity>()
		{
			@Override
			public int compare(CachedEntity item1, CachedEntity item2)
			{
				String item1Type = (String) item1.getProperty("itemType");
				String item2Type = (String) item2.getProperty("itemType");

				String item1Name = (String) item1.getProperty("name");
				String item2Name = (String) item2.getProperty("name");

				Long item1Cost = (Long) item1.getProperty("store-dogecoins");
				Long item2Cost = (Long) item2.getProperty("store-dogecoins");

				if (item1Type == null) item1Type = "";
				if (item2Type == null) item2Type = "";
				if (item1Name == null) item1Name = "";
				if (item2Name == null) item2Name = "";
				if (item1Cost == null) item1Cost = 0l;
				if (item2Cost == null) item2Cost = 0l;

				if (item1Type.compareTo(item2Type) == 0)
				{
					if (item1Name.compareTo(item2Name) == 0)
					{
						return item1Cost.compareTo(item2Cost);
					}
					else
						return item1Name.compareTo(item2Name);
				}
				else
					return item1Type.compareTo(item2Type);
			}

		});

		return sorted;
	}

	public List<CachedEntity> getGroupMembers(CachedDatastoreService ds, CachedEntity group)
	{
		if (ds == null) ds = getDB();

		List<CachedEntity> entities = getFilteredList("Character", "groupKey", group.getKey());
		return entities;
	}

	public void setStoreSale(CachedDatastoreService ds, CachedEntity character, Double sale) throws UserErrorMessage
	{
		if (ds == null) ds = getDB();

		if (ds.flagActionLimiter("saleChangeLimiter-" + character.getKey().getId(), 600, 2))
			throw new UserErrorMessage("You are trying to change the sale settings for your store too often. Try again in about 10 minutes.");

		if (sale < 0) throw new UserErrorMessage("Sales can not be negative.");

		character.setProperty("storeSale", sale);

		ds.put(character);

	}

	/**
	 * This queries for all SaleItem entries for the given item and returns true
	 * if the returned list of SaleItems is empty.
	 * 
	 * @param ds
	 * @param item
	 * @return
	 */
	public boolean isItemForSale(CachedDatastoreService ds, CachedEntity item)
	{
		List<CachedEntity> saleItems = getFilteredList("SaleItem", "itemKey", item.getKey());
		for (CachedEntity saleItem : saleItems)
		{
			// Some database integrity cleanup here, if the character that is
			// selling this item doesn't actually have the item, delete the
			// SaleItem entity
			if ("Selling".equals(saleItem.getProperty("status")) && ((Key) saleItem.getProperty("characterKey")).getId() != ((Key) item.getProperty("containerKey")).getId())
			{
				ds.delete(saleItem);
				continue;
			}
			if ("Selling".equals(saleItem.getProperty("status"))) return true;
		}

		return false;
	}

	public Double getDoubleBuffableValue(CachedEntity entity, String fieldName, Double startValue)
	{
		if (startValue == null) return null;
		List<String> buffEffects = getBuffEffectsFor(entity.getKey(), fieldName);

		for (String effect : buffEffects)
		{
			effect = effect.replace("+", "");
			if (effect.endsWith("%"))
			{
				effect = effect.substring(0, effect.length() - 1);
				double val = new Double(effect);
				val /= 100;
				startValue *= (1 + val);
			}
			else
			{
				double val = new Double(effect);
				startValue += val;
			}
		}

		return startValue;
	}

	public Double getDoubleBuffableProperty(CachedEntity entity, String fieldName)
	{
		return getDoubleBuffableValue(entity, fieldName, (Double)entity.getProperty(fieldName));
	}

	public Long getLongBuffableValue(CachedEntity entity, String fieldName, Long startValue)
	{
		if (startValue == null) return null;
		List<String> buffEffects = getBuffEffectsFor(entity.getKey(), fieldName);

		for (String effect : buffEffects)
		{
			effect = effect.replace("+", "");
			if (effect.endsWith("%"))
			{

				effect = effect.substring(0, effect.length() - 1);
				double val = new Double(effect);
				val /= 100;
				startValue = Math.round(startValue.doubleValue() * val);
			}
			else
			{
				double val = new Double(effect);
				startValue = Math.round(startValue.doubleValue() + val);
			}
		}

		return startValue;
	}

	public Long getLongBuffableProperty(CachedEntity entity, String fieldName)
	{
		return getLongBuffableValue(entity, fieldName, (Long)entity.getProperty(fieldName));
	}

	public List<String> getBuffEffectsFor(Key entityKey, String fieldName)
	{
		List<String> result = new ArrayList<String>();
		List<CachedEntity> buffs = getBuffsFor(entityKey);

		for (CachedEntity buff : buffs)
		{
			for (int i = 1; i <= 3; i++)
			{
				if (fieldName.equals(buff.getProperty("field" + i + "Name")))
				{
					String effect = (String) buff.getProperty("field" + i + "Effect");
					effect = effect.trim();
					if (effect != null && effect.matches("[-+][0-9.]+%?")) result.add(effect);
				}
			}

		}

		Collections.sort(result, new Comparator<String>()
		{
			@Override
			public int compare(String o1, String o2)
			{
				if (o1.endsWith(("%")))
					return 1;
				else
					return -1;
			}

		});

		return result;
	}

	private void cleanUpBuffs(List<CachedEntity> buffs)
	{
		if (buffs == null) return;
		CachedDatastoreService ds = getDB();

		for (int i = buffs.size() - 1; i >= 0; i--)
		{
			CachedEntity buff = buffs.get(i);
			if (buff==null)
			{
				buffs.remove(i);
				continue;
			}
			Date expiry = (Date) buff.getProperty("expiry");
			if (expiry != null && expiry.before(new Date()))
			{
				ds.delete(buff);
				buffs.remove(i);
			}
		}
	}

	public List<CachedEntity> getBuffsFor(Key entityKey)
	{
		List<CachedEntity> buffs = buffsCache.get(entityKey);

		if (buffs == null)
		{
			buffs = getFilteredList("Buff", "parentKey", entityKey);
			if (buffs == null) buffs = new ArrayList<CachedEntity>();

			buffsCache.put(entityKey, buffs);
		}

		cleanUpBuffs(buffs);

		return buffs;
	}

	public void shuffleCharactersByAttackOrder(List<CachedEntity> characters)
	{
		Collections.shuffle(characters); // We first shuffle so that characters
											// with the same status will be
											// randomized
		Collections.sort(characters, new Comparator<CachedEntity>()
		{

			@Override
			public int compare(CachedEntity o1, CachedEntity o2)
			{
				String o1Status = (String) o1.getProperty("status");
				String o2Status = (String) o2.getProperty("status");
				if (o1Status == null) o1Status = "Normal";
				if (o2Status == null) o2Status = "Normal";
				return o1Status.compareTo(o2Status);
			}
		});

	}

	public List<CachedEntity> getItemsListSortedForLocation(CachedDatastoreService ds, Key locationKey)
	{
		if (ds == null) ds = getDB();
		Query q = new Query("Item").setFilter(new FilterPredicate("containerKey", FilterOperator.EQUAL, locationKey)).addSort("movedTimestamp", SortDirection.DESCENDING);
		return ds.fetchAsList(q, 50);
	}

	public List<CachedEntity> getItemContentsFor(Key container)
	{
		if (container.getKind().equals("Location"))
		{
			return getItemsListSortedForLocation(null, container);
		}
		else
		{
			return getFilteredList("Item", "containerKey", FilterOperator.EQUAL, container);
		}
	}

	public void doMoveItem(CachedDatastoreService ds, CachedEntity character, CachedEntity item, CachedEntity newContainer) throws UserErrorMessage
	{
		if (ds == null) ds = getDB();

		if (GameUtils.equals(item.getKey(), newContainer.getKey())) throw new UserErrorMessage("lol, you cannot transfer an item into itself, the universe would explode.");

		if (checkCharacterHasItemEquipped(character, item.getKey())) throw new UserErrorMessage("Your character has this item equipped. You cannot move it until it is unequipped.");

		if (checkItemIsVending(character.getKey(), item.getKey()))
			throw new UserErrorMessage("The item you are trying to drop is currently in your store. You cannot move an item that you plan on selling.");

		String startKind = ((Key) item.getProperty("containerKey")).getKind();
		String endKind = newContainer.getKey().getKind();

		boolean handled = false;

		if (startKind.equals("Character"))
		{
			if (endKind.equals("Location"))
			{
				handled = true;
				// Make sure we're holding the item that we wish to move to the
				// ground
				if (GameUtils.equals(item.getProperty("containerKey"), character.getKey()) == false) throw new UserErrorMessage("You do not have possession of this item and so you cannot move it.");

				// Items can only be moved into locations if the character is
				// currently in said location
				if (character.getProperty("locationKey").equals(newContainer.getKey()) == false)
					throw new UserErrorMessage("You are not standing in the same location as the location you wish to move the item to. You cannot do this.");

			}
			else if (endKind.equals("Item"))
			{
				handled = true;
				// Make sure the container we're moving to is either in our
				// inventory or in our location...
				if (GameUtils.equals(newContainer.getProperty("containerKey"), character.getKey()) == false
						&& GameUtils.equals(newContainer.getProperty("containerKey"), character.getProperty("locationKey")) == false)
					throw new UserErrorMessage("You do not have physical access to this item so you cannot transfer anything to/from it. It needs to be near you or in your inventory.");

				// Check if the container is already in a container, and if the
				// item we're transferring is a container. We don't want to
				// allow that depth.
				if (item.getProperty("maxWeight") != null)
				{
					if (((Key) newContainer.getProperty("containerKey")).getKind().equals("Item"))
						throw new UserErrorMessage("You cannot put a container within a container within a container. We cannot allow that depth of containering because efficiency.");
				}

				// Make sure we can actually put things into this item
				// container...
				Long maxWeight = (Long) newContainer.getProperty("maxWeight");
				Long maxSpace = (Long) newContainer.getProperty("maxSpace");
				if (maxWeight == null || maxSpace == null) throw new UserErrorMessage("This item cannot contain other items.");

				Long itemWeight = (Long) item.getProperty("weight");
				Long itemSpace = (Long) item.getProperty("space");
				if (itemWeight == null) itemWeight = 0L;
				if (itemSpace == null) itemSpace = 0L;

				List<CachedEntity> containerInventory = getItemContentsFor(newContainer.getKey());

				Long containerCarryingWeight = getItemCarryingWeight(newContainer, containerInventory);
				Long containerCarryingSpace = getItemCarryingSpace(newContainer, containerInventory);

				if (containerCarryingWeight + itemWeight > maxWeight) throw new UserErrorMessage("The container cannot accept this item. It the item is too heavy.");

				if (containerCarryingSpace + itemSpace > maxSpace) throw new UserErrorMessage("This item will not fit. There is not enough space.");

			}
			else if (endKind.equals("Character"))
			{
				throw new UserErrorMessage("Characters cannot currently put items into other characters except through trade.");
			}

		}
		else if (startKind.equals("Location"))
		{
			if ("Character".equals(newContainer.getKind()))
			{
				handled = true;
				// Items can only be picked up from locations if the character
				// is currently in said location
				if (GameUtils.equals(character.getProperty("locationKey"), item.getProperty("containerKey")) == false)
					throw new UserErrorMessage("You are not near this item, you cannot pick it up.");

				// Check if the character can actually carry something else or
				// if its all too heavy...
				Long itemWeight = (Long) item.getProperty("weight");
				if (itemWeight == null) itemWeight = 0L;
				// If the item has a maxWeight, we will treat it as a container
				// and include it's contents in the weight calculation..
				if (item.getProperty("maxWeight") != null)
				{
					Long itemCarryingWeight = getItemCarryingWeight(item);
					itemWeight += itemCarryingWeight;
				}

				if (itemWeight > 0L)
				{
					long carrying = getCharacterCarryingWeight(character);
					long maxCarrying = getCharacterMaxCarryingWeight(character);

					if (carrying + itemWeight > maxCarrying)
						throw new UserErrorMessage("You cannot carry any more stuff! You are currently carrying " + GameUtils.formatNumber(carrying) + " grams and can carry a maximum of "
								+ GameUtils.formatNumber(maxCarrying) + " grams.");
				}

			}

		}
		else if (startKind.equals("Item"))
		{
			if ("Character".equals(newContainer.getKind()))
			{
				handled = true;

				Key oldContainerKey = (Key) item.getProperty("containerKey");
				CachedEntity oldContainer = getEntity(oldContainerKey);

				CachedEntity characterPickingUp = newContainer;

				// Items can only be picked up from item-containers if the
				// character is currently in the same location as said container
				// OR if the container is in the character's inventory
				if (GameUtils.equals(characterPickingUp.getProperty("locationKey"), oldContainer.getProperty("containerKey")) == false
						&& GameUtils.equals(characterPickingUp.getKey(), oldContainer.getProperty("containerKey")) == false)
					throw new UserErrorMessage("You do not have physical access to this item so you cannot transfer anything to/from it. It needs to be near you or in your inventory.");

				// Check if the character can actually carry something else or
				// if its all too heavy...
				Long itemWeight = (Long) item.getProperty("weight");
				if (itemWeight == null) itemWeight = 0L;
				// If the item has a maxWeight, we will treat it as a container
				// and include it's contents in the weight calculation..
				if (item.getProperty("maxWeight") != null)
				{
					Long itemCarryingWeight = getItemCarryingWeight(item);
					itemWeight += itemCarryingWeight;
				}

				if (itemWeight > 0L)
				{
					long carrying = getCharacterCarryingWeight(character);
					long maxCarrying = getCharacterMaxCarryingWeight(character);

					if (carrying + itemWeight > maxCarrying)
						throw new UserErrorMessage("You cannot carry any more stuff! You are currently carrying " + GameUtils.formatNumber(carrying) + " grams and can carry a maximum of "
								+ GameUtils.formatNumber(maxCarrying) + " grams.");
				}

			}
		}

		if (handled == false) throw new IllegalArgumentException("Unhandled situation. Staring = " + startKind + ", Ending = " + endKind);

		item.setProperty("containerKey", newContainer.getKey());
		item.setProperty("movedTimestamp", new Date());

		ds.put(item);
	}
	
	/**
	 * This is for stuff that allows access to a given container (location, item, or character). Returns false if access should not be allowed.
	 * @param character
	 * @param container
	 * @return
	 */
	public boolean checkContainerAccessAllowed(CachedEntity character, CachedEntity container)
	{
		// If the container is ourselves, it's ok
		if (container.getKind().equals("Character") && GameUtils.equals(character.getKey(), container.getKey()))
			return true;
		
		// If the container is our location, it's ok
		if (container.getKind().equals("Location") && GameUtils.equals(character.getProperty("locationKey"), container.getKey()))
			return true;
		
		// If the container is an item in our inventory, it's ok
		if (container.getKind().equals("Item") && GameUtils.equals(character.getKey(), container.getProperty("containerKey")))
			return true;
		
		// If the container is an item in our location, it's ok
		if (container.getKind().equals("Item") && GameUtils.equals(character.getProperty("locationKey"), container.getProperty("containerKey")))
			return true;
		
		return false;
	}

	
	public void doDrinkBeer(CachedDatastoreService ds, CachedEntity character) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		
		List<CachedEntity> buffs = getBuffsFor(character.getKey());
		
		// Look at the existing buffs to see if we're already maxed out on drinking
		int drunkCount = 0;
		for(CachedEntity buff:buffs)
			if ("Drunk".equals(buff.getProperty("name")))
				drunkCount++;
		
		if (drunkCount>=6)
			throw new UserErrorMessage("The bar tender thinks you've had enough to drink.");
		
		awardBuff_Drunk(ds, character);
		
	}

	
	public void doStoreSellItem(CachedEntity character, Long itemId, Long amount) throws UserErrorMessage {
		
		if (amount<0)
			throw new UserErrorMessage("You cannot sell an item for less than 0 gold.");
		
		CachedDatastoreService db = getDB();
		
		
		Key itemKey = KeyFactory.createKey("Item", itemId);
		
		if (checkCharacterHasItemEquipped(character, itemKey))
			throw new UserErrorMessage("Unable to sell this item, you currently have it equipped.");
		
		CachedEntity item = getEntity(itemKey);
		
		if (item==null)
			throw new UserErrorMessage("Item doesn't exist.");
		
		if (item.getProperty("containerKey").equals(character.getKey())==false)
			throw new UserErrorMessage("You do not have this item. You cannot sell an item that is not in your inventory.");
		
		if (checkItemBeingSoldAlready(character.getKey(), itemKey))
			throw new UserErrorMessage("You are already selling that item. If you want to change the price, remove the existing entry first.");
		
		
		
		newSaleItem(db, character, item, amount);
	}


	public void doStoreDeleteItemByItemId(Key characterKey, Long itemId) throws UserErrorMessage
	{
		List<CachedEntity> list = getFilteredList("SaleItem", "itemKey", KeyFactory.createKey("Item", itemId), "characterKey", characterKey);
		for(CachedEntity e:list)
			doStoreDeleteItem(characterKey, e);
	}
	
	public void doStoreDeleteItem(Key characterKey, Long sellItemId) throws UserErrorMessage 
	{
		CachedDatastoreService db = getDB();
		
		Key sellItemKey = KeyFactory.createKey("SaleItem", sellItemId);
		CachedEntity sellItem = getEntity(sellItemKey);
		
		doStoreDeleteItem(characterKey, sellItem);
	}
	
	public void doStoreDeleteItem(Key characterKey, CachedEntity sellItem) throws UserErrorMessage 
	{
		CachedDatastoreService db = getDB();
		
		if (sellItem==null)
			return;
		
		if (characterKey.equals(sellItem.getProperty("characterKey"))==false)
			throw new IllegalArgumentException("The SellItem this user is trying to delete does not belong to his character.");
		
		db.delete(sellItem.getKey());
	}
	
	public void doStoreDeleteAllItems(Key characterKey) throws UserErrorMessage 
	{
		CachedDatastoreService db = getDB();
		
		List<CachedEntity> saleItems = getSaleItemsFor(characterKey);
		
		for(CachedEntity item:saleItems)
		{
			if (item!=null)
			{
				if (characterKey.equals(item.getProperty("characterKey"))==false)
					throw new IllegalArgumentException("The SellItem this user is trying to delete does not belong to his character.");
				
				db.delete(item);
			}
		}
	}
	
	public void doStoreDeleteAllSoldItems(Key characterKey) throws UserErrorMessage 
	{
		CachedDatastoreService db = getDB();
		
		List<CachedEntity> saleItems = getSaleItemsFor(characterKey);
		
		for(CachedEntity item:saleItems)
		{
			if (item!=null)
			{
				if (characterKey.equals(item.getProperty("characterKey"))==false)
					throw new IllegalArgumentException("The SellItem this user is trying to delete does not belong to his character.");
				
				if (item.getProperty("status")!=null && item.getProperty("status").equals("Sold"))
					db.delete(item);
			}
		}
	}
	
	public void doStoreEnable(CachedDatastoreService db, CachedEntity character, CachedEntity characterLocation) throws UserErrorMessage
	{
		if (db==null)
			db = getDB();
		
//		if ("MarketSite".equals(characterLocation.getProperty("type"))==false)
//			throw new UserErrorMessage("You cannot setup shop outside of a marketplace.");
		
		if ("COMBAT".equals(character.getProperty("mode")))
			throw new UserErrorMessage("You cannot setup shop while in combat lol");
		
		setCharacterMode(db, character, ODPDBAccess.CHARACTER_MODE_MERCHANT);
		
		
		doCharacterTimeRefresh(db, character);	// This is saving the character, so no need to save after this
		
//		db.put(character);
	}

	
	public void doStoreDisable(CachedDatastoreService db, CachedEntity character) throws UserErrorMessage
	{
		if (db==null)
			db = getDB();
		
		if ("MERCHANT".equals(character.getProperty("mode")))
		{
			character.setProperty("mode", CHARACTER_MODE_NORMAL);
			
			db.put(character);
		}
	}


	public void doStoreRename(CachedDatastoreService db, CachedEntity character, String storeName) 
	{
		if (db==null)
			db = getDB();
		
		character.setProperty("storeName", storeName);
		
		db.put(character);
		
	}


	public void doStoreBuyItem(CachedDatastoreService db, CachedEntity character, Long saleItemId) throws UserErrorMessage {
		if (db==null)
			db = getDB();
		
		
		CachedEntity saleItem = getEntity("SaleItem", saleItemId);
		if (saleItem==null)
			throw new UserErrorMessage("This item has been taken down. The owner is no longer selling it.");
		if ("Sold".equals(saleItem.getProperty("status")))
			throw new UserErrorMessage("The owner of the store has already sold this item.");
		if ("Hidden".equals(saleItem.getProperty("status")))
			throw new UserErrorMessage("The owner of the store is not selling this item at the moment.");

		Long cost = (Long)saleItem.getProperty("dogecoins");
		if (cost==null)
			throw new UserErrorMessage("The sale item is not setup properly. It has no cost.");
		
		
		CachedEntity sellingCharacter = getEntity((Key)saleItem.getProperty("characterKey"));
		if (CHARACTER_MODE_MERCHANT.equals(sellingCharacter.getProperty("mode"))==false)
			throw new UserErrorMessage("The owner of the store is not selling at the moment.");
		if (((Key)sellingCharacter.getProperty("locationKey")).getId()!=((Key)character.getProperty("locationKey")).getId())
			throw new UserErrorMessage("You are not in the same location as the seller. You can only buy from a merchant who is in the same location as you.");

		if (character.getKey().getId() == sellingCharacter.getKey().getId())
			throw new UserErrorMessage("You cannot buy items from yourself.");
		
		Double storeSale = (Double)sellingCharacter.getProperty("storeSale");
		if (storeSale==null) storeSale = 100d;

		cost=Math.round(cost.doubleValue()*(storeSale/100));
		
		if (cost>(Long)character.getProperty("dogecoins"))
			throw new UserErrorMessage("You do not have enough funds to buy this item. You have "+character.getProperty("dogecoins")+" and it costs "+saleItem.getProperty("dogecoins")+".");
		
		CachedEntity item = getEntity((Key)saleItem.getProperty("itemKey"));
		if (item==null)
			throw new UserErrorMessage("The item being sold has been removed.");
		if (((Key)item.getProperty("containerKey")).getId()!=sellingCharacter.getKey().getId())
			throw new UserErrorMessage("The item you tried to buy is not actually in the seller's posession. Purchase has been cancelled.");
		
		
		if (cost<0)
			throw new UserErrorMessage("You cannot buy a negatively priced item.");
		
		db.beginTransaction();
		try
		{
			saleItem.setProperty("status", "Sold");
			saleItem.setProperty("soldTo", character.getKey());
			sellingCharacter.setProperty("dogecoins", ((Long)sellingCharacter.getProperty("dogecoins"))+cost);
			character.setProperty("dogecoins", ((Long)character.getProperty("dogecoins"))-cost);
			item.setProperty("containerKey", character.getKey());
			item.setProperty("movedTimestamp", new Date());
			
			db.put(saleItem);
			db.put(sellingCharacter);
			db.put(character);
			db.put(item);
			
			db.commit();
		}
		finally
		{
			db.rollbackIfActive();
		}
		
		
		
		
		
	}
	
	
	public void setCharacterMode(CachedDatastoreService ds, CachedEntity character, String mode)
	{
		if ("TRADING".equals(character.getProperty("mode")))
		{
			try {
				setTradeCancelled(ds, character);
			} catch (UserErrorMessage e) {
				// Ignore errors
			}
		}
		character.setProperty("mode", mode);
	}

	
	
	
	//////////////////////////////////////////////////
	// 1 on 1 TRADE RELATED STUFF
	
	public void startTrade(CachedDatastoreService ds, CachedEntity character, CachedEntity otherCharacter) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		
		if (((Key)character.getProperty("locationKey")).getId() != ((Key)otherCharacter.getProperty("locationKey")).getId())
			throw new UserErrorMessage("You cannot start a trade with a character that is not in your location.");
		
		TradeObject.startNewTrade(ds, character, otherCharacter);
		
		sendNotification(ds, otherCharacter.getKey(), NotificationType.fullpageRefresh);
	}
	
	
	public void addTradeItem(CachedDatastoreService ds, CachedEntity character, CachedEntity item) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, character);
		if (tradeObject==null || tradeObject.isCancelled())
			throw new UserErrorMessage("Trade has been cancelled.");
		
		if (((Key)item.getProperty("containerKey")).getId() != character.getKey().getId())
			throw new UserErrorMessage("You do not currently have that item in your posession and cannot trade it.");
		
		tradeObject.addObject(ds, character, item);
		
	}
	
	public void removeTradeItem(CachedDatastoreService ds, CachedEntity character, CachedEntity item) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, character);
		if (tradeObject==null || tradeObject.isCancelled())
			throw new UserErrorMessage("Trade has been cancelled.");
		
		tradeObject.removeObject(ds, character, item);
	}
	
	public void setTradeDogecoin(CachedDatastoreService ds, CachedEntity character, long amount) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, character);
		if (tradeObject==null || tradeObject.isCancelled())
			throw new UserErrorMessage("Trade has been cancelled.");
		
		tradeObject.setDogecoins(ds, character, amount);
	}
	
	public TradeObject setTradeReady(CachedDatastoreService ds, CachedEntity character, int version) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, character);
		if (tradeObject==null || tradeObject.isCancelled())
			throw new UserErrorMessage("Trade has been cancelled.");
		
		if (tradeObject.getVersion()!=version)
			throw new UserErrorMessage("The other user changed something.");

		if (TradeObject.checkEntitiesChanged(ds, tradeObject.character1Items))
		{
			throw new UserErrorMessage("Items have changed, please restart the trade.");
		}
	
		if (TradeObject.checkEntitiesChanged(ds, tradeObject.character2Items))
		{
			throw new UserErrorMessage("Items have changed, please restart the trade.");
		}
		
		
		if (tradeObject.isReady(ds, character))
			tradeObject.flagReady(ds, character, false);
		else
			tradeObject.flagReady(ds, character, true);

		CachedEntity character1 = getEntity(tradeObject.character1Key);
		CachedEntity character2 = getEntity(tradeObject.character2Key);

		if (((Key)character1.getProperty("locationKey")).getId() != ((Key)character2.getProperty("locationKey")).getId())
		{
			setTradeCancelled(ds, character);
			throw new UserErrorMessage("You canot trade with a character who is not in your location.");
		}
		
		
		if (tradeObject.isReady(ds, character) && tradeObject.isReady(ds, getEntity(tradeObject.getOtherCharacter(character.getKey()))))
		{
			// If both users are ready at this point, perform the trade...
		
			
			
			// First give the items and dogecoins to character2...
			for(CachedEntity item:tradeObject.character1Items)
			{
				//If the item being traded was up for sale, remove it from the sale list now
				doStoreDeleteItemByItemId(tradeObject.character1Key, item.getKey().getId());
				
				item.setProperty("containerKey", character2.getKey());
				item.setProperty("movedTimestamp", new Date());
				ds.put(item);
			}
			character2.setProperty("dogecoins", (Long)character2.getProperty("dogecoins")+tradeObject.character1Dogecoins);
			character1.setProperty("dogecoins", (Long)character1.getProperty("dogecoins")-tradeObject.character1Dogecoins);
			
			// Now give the items and dogecoins to character1...
			for(CachedEntity item:tradeObject.character2Items)
			{
				//If the item being traded was up for sale, remove it from the sale list now
				doStoreDeleteItemByItemId(tradeObject.character2Key, item.getKey().getId());
				
				item.setProperty("containerKey", character1.getKey());
				item.setProperty("movedTimestamp", new Date());
				ds.put(item);
			}
			character1.setProperty("dogecoins", (Long)character1.getProperty("dogecoins")+tradeObject.character2Dogecoins);
			character2.setProperty("dogecoins", (Long)character2.getProperty("dogecoins")-tradeObject.character2Dogecoins);
			
			
			// Change the character modes
			character1.setProperty("mode", CHARACTER_MODE_NORMAL);
			character1.setProperty("combatant", null);
			character1.setProperty("combatType", null);
			character2.setProperty("mode", CHARACTER_MODE_NORMAL);
			character2.setProperty("combatant", null);
			character2.setProperty("combatType", null);
			
			// Do a quick check to see if an exploit is being done somehow making a characters gold negative. If so, throw.
			
			if ((Long)character1.getProperty("dogecoins")<0l || (Long)character2.getProperty("dogecoins")<0l)
				throw new IllegalStateException("Trade was resulting in a negative balance for one or more recipients: "+character1.getKey()+", "+character2.getKey());
			
	
			ds.put(character1);
			ds.put(character2);
			
			tradeObject.flagComplete(ds);
		}
		
		if (character1.getKey().getId() == character.getKey().getId())
			sendNotification(ds, character2.getKey(), NotificationType.fullpageRefresh);
		else
			sendNotification(ds, character1.getKey(), NotificationType.fullpageRefresh);

		return tradeObject;
	}
	
	public void setTradeCancelled(CachedDatastoreService ds, CachedEntity character) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, character);
		if (tradeObject==null || tradeObject.isCancelled())
			throw new UserErrorMessage("Trade has been cancelled.");

		
		tradeObject.flagCancelled(ds, character);
		
		sendNotification(ds, tradeObject.getOtherCharacter(character.getKey()), NotificationType.fullpageRefresh);

	}
	
	public boolean isCharacterTrading(CachedDatastoreService ds, CachedEntity character)
	{
		if (ds==null)
			ds = getDB();
		
		if (CHARACTER_MODE_TRADING.equals(character.getProperty("mode"))==false)
			return false;
		
		TradeObject t = TradeObject.getTradeObjectFor(ds, character);

		if (t==null || t.isCancelled())
			return false;
		
		return true;
	}
	
	
	
	
	/**
	 * This is a special method that is called from time to time after certain player actions.
	 * Essentially, instead of a game "tick", we do things in this method that relates to "stuff that happens over time".
	 * The key is the character's locationEntryDatetime field. Every time this method is called, that field is updated
	 * and the amount of time that passes since the last update must be taken into account when modifying things.
	 * 
	 * For example, if a character is resting and we want to gain 1 hitpoints every 10 seconds, then we would determine
	 * how much time has passed and modify the character's hitpoints at that point.
	 * 
	 * Another example could be when a character enters a cold area, the amount of time spent in that area will reduce his
	 * hit points based on how warm the character is (depending on what he is wearing). 
	 * @param db
	 * @param character
	 */
	public void doCharacterTimeRefresh(CachedDatastoreService db, CachedEntity character)
	{
		//TODO: I think we don't need this at all at the moment.
//		if (character.getProperty("locationEntryDatetime") instanceof String)
//		{
//			character.setProperty("locationEntryDatetime", new Date());
//			db.put(character);
//			return;
//		}
//		Date locationStartDate = (Date)character.getProperty("locationEntryDatetime");
//		if (locationStartDate==null)
//		{
//			// If the location start date is null, then just set it and get out of here
//			character.setProperty("locationEntryDatetime", new Date());
//			db.put(character);
//			return;
//		}
//		
//		boolean characterChanged = false;
//		try
//		{
//			Date now = new Date();
//			character.setProperty("locationEntryDatetime", now);
//			
//			// All of these values denote the amount of time that has passed since the last time refresh...
//			long ms = now.getTime()-locationStartDate.getTime();
//			long seconds = ms/1000;
//			long minutes = seconds/60;
//			long hours = minutes/60;
//			long days = hours/24;
//			
//			// HERE is where we will progress long progressions (like building, harvesting/gathering, reseraching, experimenting, practicing..etc)
//
//			// TODO: Consider if we really NEED to save the character after each refresh. Maybe we should only save it if something actually changes.. that might require a more advanced time-refresh framework, but it would save excessive writes.
//
//			
//			
//		}
//		finally
//		{
			db.put(character);
//		}
		
		
		
	}
	
	
	public boolean isCharacterDefending(CachedEntity characterLocation, CachedEntity character)
	{
		if (character.getProperty("status")!=null && character.getProperty("status").equals(CharacterStatus.Normal)==false)
			return false;
		
		if (characterLocation==null)
			characterLocation = getEntity((Key)character.getProperty("locationKey"));
		
		if (characterLocation.getProperty("defenceStructure")==null || "TRUE".equals(characterLocation.getProperty("defenceStructuresAllowed"))==false)
			return false;
		
		return true;
	}
	
	
	
	
}
