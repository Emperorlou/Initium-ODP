package com.universeprojects.miniup.server;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

public class ODPDBAccess
{
	private CachedDatastoreService ds = null;

	public ODPDBAccess()
	{
		getDB();	// Initialize the datastore service
	}

	
	public CachedDatastoreService getDB()
	{
		if (ds!=null)
			return ds;
		
		ds = new CachedDatastoreService();
		return ds; 
	}

	
	public MemcacheService getMC()
	{
		return getDB().getMC(); 
	}
	
	/**
	 * Sets a memcache value, but only if it hasn't been modified and automatically retries until successful.
	 * 
	 * Be careful not to allow this call to be used too often on the same key.
	 * Something like this is ok to be used every other second or so, but less than that
	 * might start showing some contention at some point.
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
			if (oldValue==null)
			{
				success = getMC().put(key, value, null, SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
			}
			else
				success = getMC().putIfUntouched(key, oldValue, value);
		}
		while(success==false);
	}
	
	/**
	 * Adds to a counter in memcache and returns the resulting amount.
	 *
	 * Be careful not to allow this call to be used too often on the same key.
	 * Something like this is ok to be used every other second or so, but less than that
	 * might start showing some contention at some point.
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
			if (oldValue==null)
			{
				boolean success = mc.put(key, delta, null, SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
				if (success==true)
					return delta;
			}
			else
			{
				long newValue = ((Long)oldValue.getValue())+delta;
				mc.putIfUntouched(key, oldValue, newValue);
				return newValue;
			}
		}
		while(true);
	}
	
	
	/**
	 * Gets an entity from the database by it's kind and ID. If no entity was found,
	 * this method will simply return null. 
	 * 
	 * @param kind Must not be null.
	 * @param id If this is null, the method will return null.
	 * @return
	 */
	public CachedEntity getEntity(String kind, Long id)
	{
		Key key = createKey(kind, id);
		try 
		{
			return ds.get(key);
		} catch (EntityNotFoundException e) 
		{
			return null;
		}
	}
	
	/**
	 * Creates a datastore key out of a kind and ID.
	 * 
	 * @param kind Must not be null.
	 * @param id If this is null, the method will return null.
	 * @return
	 */
	public Key createKey(String kind, Long id)
	{
		if (id==null)
			return null;
		if (kind==null)
			throw new IllegalArgumentException("Kind cannot be null.");
		
		return KeyFactory.createKey(kind, id);
	}

	/**
	 * EFFICIENTLY fetches a bunch of entities from a list of keys.
	 * 
	 * If it is possible to use this, please use it instead of calling getEntity() back to back.
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
	 * If it is possible to use this, please use it instead of calling getEntity() back to back.
	 * 
	 * @param keyList
	 * @return
	 */
	public List<CachedEntity> getEntities(Key...keyList)
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
		if (key==null) return null;
		try {
			return getDB().get(key);
		} catch (EntityNotFoundException e) {
			// Ignore
		}
		return null;
	}
	
	/**
	 * Use this if you only want to get a count of entities in the database. 
	 * 
	 * This method will only count up to a maximum of 1000 entities.
	 * 
	 * This is more efficient than using the other filtered methods because it doesn't
	 * actually fetch all the data from the database, only a list of keys.
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
		Filter f = CompositeFilterOperator.and(f1,f2);
		return getDB().fetchAsList(kind, f, 1000).size();
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
			//ignore
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
		return 16d+Math.ceil(strength*3);
	}
	
	public boolean checkCharacterExistsByName(String name)
	{
		FilterPredicate f1 = new FilterPredicate("name", FilterOperator.EQUAL, name);
		Query q = new Query("Character").setFilter(f1).setKeysOnly();
		return (getDB().fetchAsList(q, 1).isEmpty()==false);
	}
	
	public CachedEntity getCharacterByName(String name)
	{
		List<CachedEntity> names = getFilteredList("Character", "name", name);
		if (names.isEmpty())
			return null;
		
		return names.get(0);	
	}
	
	public List<CachedEntity> getCharactersByName(String name)
	{
		List<CachedEntity> names = getFilteredList("Character", "name", name);
		if (names.isEmpty())
			return null;
		
		return names;	
	}
	
	public CachedEntity getCharacterById(Long id) 
	{
		if (id==null) return null;
		try
		{
			return getDB().get(KeyFactory.createKey("Character", id));
		}
		catch (EntityNotFoundException e)
		{
			//ignore
		}
		return null;
	}
	
	/**
	 * Returns the combatant that the given character is currently fighting, or null.
	 * 
	 * @param character
	 * @return
	 */
	public CachedEntity getCharacterCombatant(CachedEntity character)
	{
		String charMode = (String)character.getProperty("mode");
		if (charMode==null || charMode.equals("COMBAT")==false)
			return null;
		
		return getEntity((Key)character.getProperty("combatant"));
	}
	
	
	public List<CachedEntity> getCharacterMerchants(CachedDatastoreService db, Key location)
	{
		if (db==null)
			db = getDB();
		Query q = new Query("Character")
			.setFilter(
				CompositeFilterOperator.and(
						new FilterPredicate("locationKey", FilterOperator.EQUAL, location), 
						new FilterPredicate("mode", FilterOperator.EQUAL, "MERCHANT")))
			.addSort("locationEntryDatetime", SortDirection.DESCENDING);
		return db.fetchAsList(q, 50);
	}
	
	/**
	 * This is really only for fixing the DB. We should never have to call this.
	 * @param db
	 * @return
	 */
	public Iterable<CachedEntity> getAllCharacterMerchants(CachedDatastoreService db)
	{
		if (db==null)
			db = getDB();
		Query q = new Query("Character").setFilter(new FilterPredicate("mode", FilterOperator.EQUAL, "MERCHANT"));
		return db.fetchAsIterable(q);
	}
	
	
	
	
	
	
	
	
	
	/**
	 * 
	 * @param banner
	 * @param name
	 * @param description
	 * @param discoverAnythingChance The PERCENTAGE chance of you discovering anything at all (this doesn't guarantee a discovery, its just the chance that you can even roll to discover something)
	 * @return
	 */
	public CachedEntity newLocation(CachedDatastoreService db, String banner, String name, String description, Double discoverAnythingChance, String type, Key parentLocationKey, Key ownerKey)
	{
		if (db==null)
			db = getDB();
		
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
		if (id==null) return null;
		try
		{
			return getDB().get(KeyFactory.createKey("Location", id));
		}
		catch (EntityNotFoundException e)
		{
			//ignore
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
		Query q = new Query("Location").setFilter(CompositeFilterOperator.or(new FilterPredicate("type", FilterOperator.EQUAL, "Permanent"),new FilterPredicate("type", FilterOperator.EQUAL, "CampSite")));
		return getDB().fetchAsIterable(q);
	}
	
	public Iterable<CachedEntity> getLocations_CampSites()
	{
		Query q = new Query("Location").setFilter(
				new FilterPredicate("type", FilterOperator.EQUAL, "CampSite"));
		return getDB().fetchAsIterable(q);
	}
	
	public List<CachedEntity> getCampsitesByParentLocation(CachedDatastoreService db, Key parentLocationKey) 
	{
		if (db==null)
			db = getDB();
		Query q = new Query("Location").setFilter(CompositeFilterOperator.and(
				new FilterPredicate("type", FilterOperator.EQUAL, "CampSite"), 
				new FilterPredicate("parentLocationKey", FilterOperator.EQUAL, parentLocationKey)
				));
		return db.fetchAsList(q, 1000);
	}
	
	
	
	
	
	
	
	
	
	public CachedEntity newPath(CachedDatastoreService db, String internalName, Key location1Key, Key location2Key, double discoveryChance, Long travelTime, String type)
	{
		return newPath(db, internalName, location1Key, null, location2Key, null, discoveryChance, travelTime, type);
	}

	public CachedEntity newPath(CachedDatastoreService db, String internalName, Key location1Key, String location2ButtonNameOverride, Key location2Key, String location1ButtonNameOverride, double discoveryChance, Long travelTime, String type)
	{
		if (db==null)
			db = getDB();
		
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
		if (id==null) return null;
		try
		{
			return getDB().get(KeyFactory.createKey("Path", id));
		}
		catch (EntityNotFoundException e)
		{
			//ignore
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
//		Filter f = CompositeFilterOperator.or(
//				CompositeFilterOperator.and(permanent, location1), 
//				CompositeFilterOperator.and(permanent, location2), 
//				CompositeFilterOperator.and(camp, location1), 
//				CompositeFilterOperator.and(camp, location2)
//				);
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
	
	public List<CachedEntity> getMonsterSpawnersForLocation(Key locationKey) {
		FilterPredicate f1 = new FilterPredicate("locationKey", FilterOperator.EQUAL, locationKey);
		Query q = new Query("MonsterSpawner").setFilter(f1);
		return getDB().fetchAsList(q, 1000);
	}
	
	public List<CachedEntity> getCollectableSpawnersForLocation(Key locationKey) {
		FilterPredicate f1 = new FilterPredicate("locationKey", FilterOperator.EQUAL, locationKey);
		Query q = new Query("CollectableSpawner").setFilter(f1);
		return getDB().fetchAsList(q, 1000);
	}
	
	public List<CachedEntity> getCollectablesForLocation(Key locationKey) {
		FilterPredicate f1 = new FilterPredicate("locationKey", FilterOperator.EQUAL, locationKey);
		Query q = new Query("Collectable").setFilter(f1);
		return getDB().fetchAsList(q, 1000);
	}
	
	
	
	
	
	
	
	public CachedEntity newSaleItem(CachedDatastoreService db, CachedEntity character, CachedEntity item, long dogecoins)
	{
		if (db==null)
			db = getDB();
		
		
		CachedEntity result = new CachedEntity("SaleItem");
		result.setProperty("characterKey", character.getKey());
		result.setProperty("itemKey", item.getKey());
		result.setProperty("dogecoins", dogecoins);
		
		result.setProperty("status", "Selling");
		result.setProperty("name", character.getProperty("name")+" - "+item.getProperty("name")+" - "+dogecoins);
		
		
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
		return (getDB().fetchAsList(q, 1).isEmpty()==false);
	}
	
	
	
	public boolean checkItemBeingSoldAlready(Key characterKey, Key itemKey)
	{
		FilterPredicate f1 = new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey);
		FilterPredicate f2 = new FilterPredicate("itemKey", FilterOperator.EQUAL, itemKey);
		Query q = new Query("SaleItem").setFilter(CompositeFilterOperator.and(f1, f2)).setKeysOnly();
		return (getDB().fetchAsList(q, 1).isEmpty()==false);
	}
	
	
	
	
	
	
	public CachedEntity newDiscovery(CachedDatastoreService db, CachedEntity character, CachedEntity entity)
	{
		if (db==null)
			db = getDB();
		
		CachedEntity oldDiscovery = getDiscoveryByEntity(character.getKey(), entity.getKey());
		if (oldDiscovery!=null)
			return oldDiscovery;
		
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
		if (id==null) return null;
		try
		{
			return getDB().get(KeyFactory.createKey("Discovery", id));
		}
		catch (EntityNotFoundException e)
		{
			//ignore
		}
		return null;
	}

	public CachedEntity getDiscoveryByEntity(Key characterKey, Key entityKey) {
		Query q = new Query("Discovery").setFilter(CompositeFilterOperator.and(new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey), new FilterPredicate("entityKey", FilterOperator.EQUAL, entityKey)));
		
		List<CachedEntity> discoveries = getDB().fetchAsList(q, 1000);
		if (discoveries.size()>1)
		{
			Logger.getLogger("DBAccess").log(Level.SEVERE, "Somehow while running DBAccess.getDiscoveryByEntity(), multiple discoveries for the same person and entity were found in the DB. They have been deleted, but this should be fixed. CharacterKey="+characterKey+", EntityKey="+entityKey);
			CachedDatastoreService db = getDB();
			for(int i = 1; i<discoveries.size(); i++)
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
	public Iterable<CachedEntity> getDiscoveriesByEntity(Key entityKey) {
		Query q = new Query("Discovery").setFilter(new FilterPredicate("entityKey", FilterOperator.EQUAL, entityKey));
		
		Iterable<CachedEntity> discoveries = getDB().fetchAsIterable(q);
		return discoveries;
	}
	
	public List<CachedEntity> getDiscoveriesForCharacterAndLocation(Key characterKey, Key location)
	{
		Query q = new Query("Discovery").setFilter(CompositeFilterOperator.and(new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey), CompositeFilterOperator.or(new FilterPredicate("location1Key", FilterOperator.EQUAL, location), new FilterPredicate("location2Key", FilterOperator.EQUAL, location))));
		List<CachedEntity> fetchAsList = getDB().fetchAsList(q, 1000);
		System.out.println(fetchAsList.size()+" discoveries found.");
		return fetchAsList;
	}

	public List<CachedEntity> getDiscoveriesForCharacter_KeysOnly(Key characterKey)
	{
		Query q = new Query("Discovery").setFilter(new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey)).setKeysOnly();
		return getDB().fetchAsList(q, 1000);
	}

	public List<CachedEntity> getDiscoveriesForCharacterByKind(Key characterKey, String discoveryKind)
	{
		Query q = new Query("Discovery").setFilter(CompositeFilterOperator.and(new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey), new FilterPredicate("kind", FilterOperator.EQUAL, discoveryKind)));
		return getDB().fetchAsList(q, 1000);
	}

	
	
	
	
	
//	public CachedEntity newItemDefinition(String itemClassName, String itemType, String iconUrl, String quantityUnit, String description, 
//			String weatherDamgeCoefficientCurve, String durabilityCurve, String warmthCurve, String weightCurve, String spaceCoefficientCurve, 
//			String equipSlot, String armor_blockChanceCurve, String armor_damageReductionCurve, String weapon_damageCurve, String weapon_rangeCurve, 
//			String container_maxWeightCurve, String container_maxSpaceCoefficientCurve)
//	{
//		Entity obj = new CachedEntity("ItemDef");
//		// Set the starting attributes
//		obj.setProperty("itemClassName", itemClassName);
//		obj.setProperty("itemType", itemType);
//		obj.setProperty("iconUrl", iconUrl);
//		obj.setUnindexedProperty("quantityUnit", quantityUnit);
//		obj.setUnindexedProperty("description", description);
//		obj.setUnindexedProperty("weatherDamgeCoefficientCurve", weatherDamgeCoefficientCurve);
//		obj.setUnindexedProperty("durabilityCurve", durabilityCurve);
//		obj.setUnindexedProperty("warmthCurve", warmthCurve);
//		obj.setUnindexedProperty("weightCurve", weightCurve);
//		obj.setUnindexedProperty("spaceCoefficientCurve", spaceCoefficientCurve);
//		obj.setProperty("equipSlot", equipSlot);
//		obj.setUnindexedProperty("armor_blockChanceCurve", armor_blockChanceCurve);
//		obj.setUnindexedProperty("armor_damageReductionCurve", armor_damageReductionCurve);
//		obj.setUnindexedProperty("weapon_damageCurve", weapon_damageCurve);
//		obj.setUnindexedProperty("weapon_rangeCurve", weapon_rangeCurve);
//		obj.setUnindexedProperty("container_maxWeightCurve", container_maxWeightCurve);
//		obj.setUnindexedProperty("container_maxSpaceCoefficientCurve", container_maxSpaceCoefficientCurve);
//
//		// Set some default attributes
//		
//		getDB().put(obj);
//		return obj;
//	}
	
	
	public CachedEntity getItemDefinitionById(Long id) 
	{
		if (id==null) return null;
		try
		{
			return getDB().get(KeyFactory.createKey("ItemDef", id));
		}
		catch (EntityNotFoundException e)
		{
			//ignore
		}
		return null;
	}
	
	public CachedEntity getItemDefinitionByClassName(String className) 
	{
		Query q = new Query("ItemDef").setFilter(new FilterPredicate("itemClassName", FilterOperator.EQUAL, className));
		return getDB().fetchSingleEntity(q);
	}
	
	
	
	
	
	
	
//	public CachedEntity newNPCDefinition(String npcClassName, String npcType, String bannerUrl, String description, 
//			String hpCurve, Integer generatedEquipmentQualityPenalty, String randomItems)
//	{
//		Entity obj = new CachedEntity("NPCDef");
//		// Set the starting attributes
//		obj.setProperty("npcClassName", npcClassName);
//		obj.setProperty("npcType", npcType);
//		obj.setProperty("bannerUrl", bannerUrl);
//		obj.setUnindexedProperty("description", description);
//		obj.setUnindexedProperty("hpCurve", hpCurve);
//		obj.setUnindexedProperty("generatedEquipmentQualityPenalty", generatedEquipmentQualityPenalty);
//		obj.setUnindexedProperty("randomItems", randomItems);
//
//		// Set some default attributes
//		
//		getDB().put(obj);
//		return obj;
//	}
	
	public CachedEntity getNPCDefinitionById(Long id) 
	{
		if (id==null) return null;
		try
		{
			return getDB().get(KeyFactory.createKey("NPCDef", id));
		}
		catch (EntityNotFoundException e)
		{
			//ignore
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
		if (getDB().fetchAsList(q, 1).size()==0)
			return true;
		else
			return false;
	}
	

	public List<CachedEntity> getStoresListFor(Key locationKey)
	{
		Query q = new Query("Character").setFilter(CompositeFilterOperator.and(new FilterPredicate("locationKey", FilterOperator.EQUAL, locationKey), new FilterPredicate("mode", FilterOperator.EQUAL, "MERCHANT")));
		return getDB().fetchAsList(q, 51);
	}


	
	public boolean isChatbanned(CachedDatastoreService db, String characterName)
	{
		if (db==null)
			db = getDB();
		
		Long isChatbanned = db.getStat("chatban-"+characterName);
		if (isChatbanned!=null && 1l == isChatbanned)
			return true;
		
		return false;
	}
	
	
	public void chatbanByIP(String characterName, String ip) {
		CachedDatastoreService db = getDB();
		db.setStat("chatbanIP-"+ip, 1l, 3600/2);
		
		db.setStat("chatban-"+characterName, 1l, 3600/2);
	}
	
	public void unchatban(String characterName, String ip)
	{
		CachedDatastoreService db = getDB();
		db.setStat("chatbanIP-"+ip, null);
		
		db.setStat("chatban-"+characterName, null);
	}
	
	public boolean isChatbannedByIP(CachedDatastoreService db, String characterName, String ip)
	{
		if (db==null)
			db = getDB();
		Long isChatbannedByIP = db.getStat("chatbanIP-"+ip);
		if (isChatbannedByIP!=null && 1l == isChatbannedByIP)
			return true;
		
		return isChatbanned(db, characterName);
	}
	
	

	public CachedEntity getGroupByName(String name)
	{
		List<CachedEntity> names = getFilteredList("Group", "name", name);
		if (names.isEmpty())
			return null;
		
		return names.get(0);	
	}
	
	
}
