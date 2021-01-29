package com.universeprojects.miniup.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PropertyContainer;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.universeprojects.cacheddatastore.AbortTransactionException;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.cacheddatastore.Transaction;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.aspects.AspectBuffable;
import com.universeprojects.miniup.server.commands.CommandItemsStackMerge;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.longoperations.AbortedActionException;
import com.universeprojects.miniup.server.scripting.events.CombatEvent;
import com.universeprojects.miniup.server.services.BlockadeService;
import com.universeprojects.miniup.server.services.ContainerService;
import com.universeprojects.miniup.server.services.GridMapService;
import com.universeprojects.miniup.server.services.GuardService;
import com.universeprojects.miniup.server.services.ModifierService;
import com.universeprojects.miniup.server.services.ModifierService.ModifierType;
import com.universeprojects.miniup.server.services.MovementService;
import com.universeprojects.miniup.server.services.ODPInventionService;
import com.universeprojects.miniup.server.services.ODPKnowledgeService;
import com.universeprojects.miniup.server.services.QuestService;
import com.universeprojects.miniup.server.services.ScriptService;

public class ODPDBAccess
{
	public static final int CHAT_BAN_SECONDS = 180;
	
	Logger log = Logger.getLogger(ODPDBAccess.class.getName());

	public static ODPDBAccessFactory factory = null;
	
	final public static int welcomeMessagesLimiter = 0;
	
	final private HttpServletRequest request;
	
	final QueryHelper query;
	
	private Map<Key,Set<String>> mpusSendQueue = null;

	public enum LocationSubType
	{
		ResourceSite,
		PlayerHouse,
		CombatSite
	}
	
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
	
	public enum ScriptType
	{
		directItem, directLocation, onAttack, onAttackHit, onDefend, onDefendHit, 
		onMoveBegin, onMoveEnd, onServerTick, onCombatTick, combatItem, global,
		ownerHtml;
	}

	public enum StaticBuffables
	{
		movementSpeed,	// Possibly implemented
		skillExecutionSpeed,
		avoidDetection,	// Implemented
		physicalDamage	// Implemented, for the most part
	}
	
	
	public static final Date HardcoreModeCutoffDate = new GregorianCalendar(2018, 7, 15).getTime();
	public static final String STORE_NAME_REGEX = "[A-Za-z0-9- _/.,%:!?+*&'\"~\\(\\)]+";
	public static final String CAMP_NAME_REGEX = "[A-Za-z0-9- ,'&]+";
	public static final String GROUP_NAME_REGEX = "[A-Za-z ,'`]+";
	public static final String CHARACTER_MODE_NORMAL = "NORMAL";
	public static final String CHARACTER_MODE_COMBAT = "COMBAT";
	public static final String CHARACTER_MODE_MERCHANT = "MERCHANT";
	public static final String CHARACTER_MODE_TRADING = "TRADING";
	public static final String[] EQUIPMENT_SLOTS = new String[]
	{
			"Pet", 
			"Helmet", "Chest", "Shirt", "Gloves", "Legs", "Boots", "RightHand", "LeftHand", "RightRing", "LeftRing", "Neck",
			"Cosmetic1", "Cosmetic2", "Cosmetic3"
	};

	private CachedDatastoreService ds = null;

	public Map<Key, List<PropertyContainer>> buffsCache = new HashMap<>();
	public Map<Key, Map<String, Double>> statsCache = new HashMap<Key, Map<String, Double>>(); 

	public EntityPool pool = null;
	
	protected ODPDBAccess(HttpServletRequest request)
	{
		this.request = request;
		getDB(); // Initialize the datastore service
		pool = new EntityPool(ds);
		query = new QueryHelper(ds);
	}
	
	public static ODPDBAccess getInstance(HttpServletRequest request)
	{
		if (factory==null)
			return new ODPDBAccess(request);
		
		return factory.getInstance(request);
	}

	public Collection<CachedEntity> getCharacterEquipment(CachedEntity character)
	{
		List<Key> equipmentKeys = new ArrayList<>();
		for(String slot:EQUIPMENT_SLOTS)
		{
			Key equipmentKey = (Key)character.getProperty("equipment"+slot);
			if (equipmentKey!=null)
				equipmentKeys.add(equipmentKey);
		}
		pool.addToQueue(equipmentKeys);
		pool.loadEntities();
		List<CachedEntity> list = pool.get(equipmentKeys);
		while(list.remove(null));
		return list;
	}
	
	
	public boolean isTestServer()
	{
		if (getRequest().getRequestURL().toString().contains("test")) 
			return true;
		
		if (CachedDatastoreService.isUsingRemoteAPI()==true)
			return true;
		
		return false;
	}
	
	public HttpServletRequest getRequest()
	{
		return request;
	}
	
	public CachedDatastoreService getDB()
	{
		CachedDatastoreService.singlePutMode = true;
		
		// Check if we hvae a CDS in the request attributes already
		if (ds==null)
			ds = (CachedDatastoreService)request.getAttribute("CachedDatastoreService");
		if (ds != null) return ds;

		ds = new CachedDatastoreService()
		{
			@Override
			protected boolean isPutEventHandlerEnabled()
			{
				return true;
			}
			
			protected boolean putEventHandler(CachedEntity entity) 
			{
				if (entity!=null)
				{
					if (entity.getKind().equals("Character"))
					{
						// Always update the locationEntryDatetime timestamp
						entity.setProperty("locationEntryDatetime", new Date());
					}
					else if (entity.getKind().equals("Item"))
					{
						// If this is a procedural entity then we need to turn it into a proper one first
						String proceduralKey = (String)entity.getAttribute("proceduralKey");
						if (proceduralKey!=null)
						{
							GridMapService gms = getGridMapService();
							if (gms.isStillProceduralEntity(proceduralKey))
							{
								gms.convertFromProceduralToDBItem(entity);
							}
						}
					}
				}
				
				
				return true;
			}
		};

		request.setAttribute("CachedDatastoreService", ds);
		
		
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
	public Key getCurrentUserKey()
	{
		HttpSession session = getRequest().getSession(true);

		Long userId = (Long) session.getAttribute("userId");

		if (userId==null) throw new NotLoggedInException();
		
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
	public CachedEntity getCurrentUser()
	{
		if (request.getAttribute("userEntity") != null) return (CachedEntity) request.getAttribute("userEntity");

		Key userKey = getCurrentUserKey();

		if (userKey == null) return null;

		CachedEntity user = getEntity(userKey);

		// For efficiency, we're going to keep the user entity in the request so
		// we don't have to fetch it from the DB more than once in a single
		// request
		request.setAttribute("userEntity", user);

		return user;
	}

	
	/**
	 * Gets the character key of the user who is currently logged in (if they
	 * are logged in).
	 * 
	 * This will return null if The user is not currently logged in or if the
	 * user has no character on his account for some reason.
	 * 
	 * @param request
	 * @return
	 */
	public Key getCurrentCharacterKey()
	{
		if (getRequest().getAttribute("characterEntity") != null) return ((CachedEntity) getRequest().getAttribute("characterEntity")).getKey();

		HttpSession session = request.getSession(true);

		Long authenticatedInstantCharacterId = (Long) session.getAttribute("instantCharacterId");
		Long userId = (Long) session.getAttribute("userId");
		if (userId != null)
		{
			CachedEntity user = getCurrentUser();
			
			String overrideCharKeyStr = request.getParameter("char");
			Key characterKey = null;
			
			if (overrideCharKeyStr!=null && overrideCharKeyStr.trim().equals("")==false)
			{
				characterKey = KeyFactory.stringToKey(overrideCharKeyStr);
			}
			else
			{
				characterKey = (Key) user.getProperty("characterKey");
			}
			return characterKey;
		}
		else
			throw new NotLoggedInException();

	}
	
	/**
	 * This is only used by the getCurrentCharacter() method to find a suitable replacement character
	 * when the current character is null or otherwise not set on the user.
	 * 
	 * @return
	 */
	private CachedEntity getCurrentCharacter_whenNull()
	{
		List<CachedEntity> allChars = getUserCharacters(getCurrentUser());
		boolean found = false;
		for(CachedEntity chr:allChars)
		{
			if (chr!=null)
			{
				getCurrentUser().setProperty("characterKey", chr.getKey());
				found = true;
				return chr;
			}
		}
		
		if (found==false)
		{
			try
			{
				CachedEntity newChar = newPlayerCharacter(ds, getAuthenticator(), getCurrentUser(), "Unnamed", null);
				ds.put(newChar);
				return newChar;
			}
			catch(DDOSProtectionException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		throw new RuntimeException("Couldn't find a suitable character to switch the user to and couldn't create a new character.");
	}

	public CachedEntity getCurrentCharacter()
	{
		return getCurrentCharacter(false);
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
	public CachedEntity getCurrentCharacter(boolean ignoreNotLoggedIn)
	{
		if (getRequest().getAttribute("characterEntity") != null) return (CachedEntity) getRequest().getAttribute("characterEntity");

		HttpSession session = request.getSession(true);

		Long authenticatedInstantCharacterId = (Long) session.getAttribute("instantCharacterId");
		Long userId = (Long) session.getAttribute("userId");
		if (userId != null)
		{
			CachedEntity user = getCurrentUser();
			Key characterKey = getCurrentCharacterKey();
			CachedEntity character = getEntity(characterKey);

			if (character != null)
			{
				// VERY IMPORTANT!!!!!!
				// Check that the character key we have is ACTUALLY from the logged in user. Since we now allow overriding of the current character, 
				// we need to make sure other people aren't trying to take control of someone else's character.
				if (GameUtils.equals(character.getProperty("userKey"), user.getKey())==false)
				{
					//TODO: Here it would be good to throw an exception that causes the client to do a hard refresh
					throw new RuntimeException("Attempted to use a character that does not belong to the logged in user's account.");
				}
				
				request.setAttribute("characterEntity", character);

				return character;
			}
		}
		else
			if (ignoreNotLoggedIn)
				return null;
			else
				throw new NotLoggedInException();

		CachedEntity newCharacter = getCurrentCharacter_whenNull();
		//TODO: Here it would be good to throw an exception that causes the client to create a new character (if they're logged in)
		return newCharacter;
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
		pool.addToQueue(key);
		pool.loadEntities();
		return pool.get(key);
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
	public CachedEntity getEntity(String kind, String entityName)
	{
		Key key = createKey(kind, entityName);
		pool.addToQueue(key);
		pool.loadEntities();
		return pool.get(key);
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
	 * Creates a datastore key out of a kind and name.
	 * 
	 * @param kind
	 *            Must not be null.
	 * @param entityName
	 *            If this is null, the method will return null.
	 * @return
	 */
	public Key createKey(String kind, String entityName)
	{
		if (entityName == null) return null;
		if (kind == null) throw new IllegalArgumentException("Kind cannot be null.");

		return KeyFactory.createKey(kind, entityName);
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
		pool.addToQueue(keyList);
		pool.loadEntities();
		return pool.get(keyList);
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
		List<Key> list = Arrays.asList(keyList);
		pool.addToQueue(list);
		pool.loadEntities();
		return pool.get(list);

	}

	public InitiumObject getInitiumObject(Key key)
	{
		if (key==null) return null;
		CachedEntity entity = getEntity(key);
		if (entity==null) return null;
		return new InitiumObject(this, entity);
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
		pool.addToQueue(key);
		pool.loadEntities();
		return pool.get(key);
	}

	/**
	 * Fetches a list CachedEntity from the given keys.
	 * 
	 * Important: If a given CachedEntity cannot be found in the database, the return will
	 * contain a null entry for that key's index.
	 * 
	 * @param owningEntityKey
	 * @return
	 */
	public List<CachedEntity> getEntity(Key...keys)
	{
		if (keys == null) return null;
		List<Key> list = Arrays.asList(keys);
		pool.addToQueue(list);
		pool.loadEntities();
		return pool.get(list);
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
		return getDB().fetchAsList_Keys(kind, f1, 5000).size();
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
		return getDB().fetchAsList_Keys(kind, f, 5000).size();
	}

	public Integer getFilteredList_Count(String kind, String fieldName, FilterOperator operator, Object equalToValue, String fieldName2, FilterOperator operator2, Object equalToValue2, String fieldName3, FilterOperator operator3, Object equalToValue3)
	{
		FilterPredicate f1 = new FilterPredicate(fieldName, operator, equalToValue);
		FilterPredicate f2 = new FilterPredicate(fieldName2, operator2, equalToValue2);
		FilterPredicate f3 = new FilterPredicate(fieldName3, operator3, equalToValue3);
		Filter f = CompositeFilterOperator.and(f1, f2, f3);
		return getDB().fetchAsList_Keys(kind, f, 5000).size();
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

	/**
	 * This returns an alphabetically sorted list of all characters associated with a userkey.
	 * This will also filter out all invalid characters, notable dead and zombie.
	 * @param userKey
	 * @return
	 */
	public List<CachedEntity> getAlphabetSortedValidCharactersByUser(Key userKey){
		List<CachedEntity> characters = getFilteredList("Character", "userKey", userKey);
		Collections.sort(characters, new Comparator<CachedEntity>()
		{
			@Override
			public int compare(CachedEntity o1, CachedEntity o2)
			{
				return ((String)o1.getProperty("name")).compareTo((String)o2.getProperty("name"));
			}
		});

		//this will validate all the characters. Filter out zambie.
		List<CachedEntity> toReturn = new ArrayList<>();
		for(CachedEntity ce : characters) {
			
			//if the character is a zombie, skip.
			if(CommonChecks.checkCharacterIsZombie(ce)) continue;
			
			//if the character is both dead and the name starts with dead, skip.
			if(((String)ce.getProperty("name")).startsWith("Dead ") && CommonChecks.checkCharacterIsDead(ce)) continue;
			
			toReturn.add(ce);
		}

		return toReturn;
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
		if (character==null) return null;
		Key userKey = (Key)character.getProperty("userKey");
		return getEntity(userKey);
	}

	public CachedEntity getUserByCharacterKey(Key characterKey)
	{
		Query q = new Query("User").setFilter(new FilterPredicate("characterKey", FilterOperator.EQUAL, characterKey));
		return getDB().fetchSingleEntity(q);
	}

	public double calculateHitpoints(double strength)
	{
		if (strength >= 10.995 && strength <= 11.01) {
			strength = strength + 0.006;
		}
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
	public CachedEntity newLocation(CachedDatastoreService db, String banner, String name, String description, Double discoverAnythingChance, String type, CachedEntity parentLocation, Key ownerKey, int gridMapSize, LocationSubType subType)
	{
		if (db == null) db = getDB();

		CachedEntity location = new CachedEntity("Location", db.getPreallocatedIdFor("Location"));
		// Set the starting attributes
		location.setProperty("banner", banner);
		location.setProperty("name", name);
		location.setProperty("description", description);
		location.setProperty("discoverAnythingChance", discoverAnythingChance);
		location.setProperty("type", type);
		location.setProperty("parentLocationKey", parentLocation.getKey());
		location.setProperty("ownerKey", ownerKey);
		location.setProperty("gridMapWidth", (long)gridMapSize);
		location.setProperty("gridMapHeight", (long)gridMapSize);

		pool.addEntityDirectly(location);
		
		// GridMap stuff
		GridMapService gmService = new GridMapService(this, parentLocation);
		gmService.generateGridMapElementsFromParent(this, parentLocation, location);
		
		
		db.put(location);
		return location;
	}
	
	private GridMapService gridMapService = null;
	public GridMapService getGridMapService()
	{
		if (gridMapService==null)
			gridMapService = new GridMapService(this, getCharacterLocation(getCurrentCharacter()));
		
		return gridMapService;
	}

	private QuestService questService = null;
	public QuestService getQuestService(OperationBase command)
	{
		if (questService==null)
			questService = new QuestService(command, this, getCurrentCharacter());
		
		return questService;
	}

	public Key getDefaultLocationKey()
	{
		return (Key)getProject().getProperty("defaultSpawnLocationKey");
	}

	private CachedEntity getProject()
	{
		try
		{
			return ds.get(KeyFactory.createKey("Project", 4902705788092416L));
		}
		catch (EntityNotFoundException e)
		{
			throw new RuntimeException("Unable to find the project entity.");
		}
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

	public List<CachedEntity> getLocationCharacters(Key locationKey)
	{
		List<CachedEntity> charactersHere = query.getFilteredList("Character", 51, "locationKey", FilterOperator.EQUAL, locationKey);
		return charactersHere;
	}
	
	public Iterable<CachedEntity> getLocationsForMonsterLevelUpdate()
	{
		Query q = new Query("Location").setFilter(CompositeFilterOperator.or(new FilterPredicate("type", FilterOperator.EQUAL, "Permanent"), 
				new FilterPredicate("type", FilterOperator.EQUAL, "CampSite")));
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

		db.preallocateIdsFor("Path", 1);
		CachedEntity path = new CachedEntity("Path", db.getPreallocatedIdFor("Path"));
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
	
	/**
	 * Returns a list of all paths visible by a character at a location.
	 * @param characterKey - the key of the character that is viewing these paths.
	 * @param locationKey - the key of the location.
	 * @param showHidden - do we load hidden paths as well.
	 * @return
	 */
	public List<CachedEntity> getVisiblePathsByLocation (Key characterKey, Key locationKey, boolean showHidden){
	   
		if(characterKey == null || locationKey == null) throw new RuntimeException("Error loading paths for Character: " + characterKey + " at Location: " + locationKey);
		
		List<CachedEntity> discoveriesForCharacterAndLocation = getDiscoveriesForCharacterAndLocation(characterKey, locationKey, showHidden);
	    List<Key> alwaysVisiblePaths = getLocationAlwaysVisiblePaths_KeysOnly(locationKey);
		
	    pool.addToQueue(alwaysVisiblePaths);
	    
	    Set<Key> pathKeys = new LinkedHashSet<>(alwaysVisiblePaths);
	    for(CachedEntity discovery:discoveriesForCharacterAndLocation)
	    {
	    	pool.addToQueue(discovery.getProperty("location1Key"), discovery.getProperty("location2Key"), discovery.getProperty("entityKey"));
	    	pathKeys.add((Key)discovery.getProperty("entityKey"));
	    }
	    
	    pool.loadEntities();
	    
	    // Also load in the always-visible destination locations
	    if (alwaysVisiblePaths.isEmpty()==false)
	    {
	    	for(Key key:alwaysVisiblePaths)
	    	{
	    		CachedEntity path = pool.get(key);
		    	pool.addToQueue(path.getProperty("location1Key"), path.getProperty("location2Key"));
	    	}
	    	pool.loadEntities();
	    }
	    
	    List<CachedEntity> paths = pool.get(pathKeys);
	    
		Iterator<CachedEntity> itr = paths.iterator();
		while(itr.hasNext()) {
			CachedEntity path = itr.next();
			if(path == null) itr.remove();
		}
		
		return paths;
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
//		FilterPredicate f1 = new FilterPredicate("type", FilterOperator.EQUAL, "Permanent");
//		FilterPredicate f2 = new FilterPredicate("type", FilterOperator.EQUAL, "PlayerHouse");
//		Filter f = CompositeFilterOperator.or(f1, f2);
//		Query q = new Query("Path").setFilter(f);
//		return getDB().fetchAsList(q, 1000);
//		QueryHelper query = new QueryHelper(ds);
//		List<CachedEntity> list = query.getFilteredList("Path", "location1Key", locationKey);
//		list.addAll(query.getFilteredList("Path", "location2Key", locationKey));
//		return list;
		return null;
		
	}

	public List<CachedEntity> getPathsByLocation(Key locationKey)
	{
//		FilterPredicate f1 = new FilterPredicate("location1Key", FilterOperator.EQUAL, locationKey);
//		FilterPredicate f2 = new FilterPredicate("location2Key", FilterOperator.EQUAL, locationKey);
//		Filter f = CompositeFilterOperator.or(f1, f2);
//		Query q = new Query("Path").setFilter(f);
//		return getDB().fetchAsList(q, 1000);
		List<CachedEntity> list = query.getFilteredList("Path", "location1Key", locationKey);
		list.addAll(query.getFilteredList("Path", "location2Key", locationKey));
		return list;
	}

	public List<Key> getPathsByLocation_KeysOnly(Key locationKey)
	{
//		FilterPredicate f1 = new FilterPredicate("location1Key", FilterOperator.EQUAL, locationKey);
//		FilterPredicate f2 = new FilterPredicate("location2Key", FilterOperator.EQUAL, locationKey);
//		Filter f = CompositeFilterOperator.or(f1, f2);
//		Query q = new Query("Path").setFilter(f).setKeysOnly();
//		return getDB().fetchAsList(q, 1000);
		List<Key> list = query.getFilteredList_Keys("Path", "location1Key", locationKey);
		list.addAll(query.getFilteredList_Keys("Path", "location2Key", locationKey));
		return list;
		
		
	}
	
	public List<CachedEntity> getPathsBetweenLocations(Key location1Key, Key location2Key)
	{
		List<CachedEntity> foundPaths = query.getFilteredList("Path", "location1Key", location1Key, "location2Key", location2Key);
		foundPaths.addAll(query.getFilteredList("Path", "location1Key", location2Key, "location2Key", location1Key));
		return foundPaths;
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

		// This is a special case for premium tokens to set the specialId
		if (CommonChecks.checkItemIsPremiumToken(item) || CommonChecks.checkItemIsChippedToken(item))
		{
			result.setProperty("specialId", "Initium Premium Membership");
		}
		
		
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

		if (entity.getKey().isComplete() && character.getKey().isComplete())
		{
			CachedEntity oldDiscovery = getDiscoveryByEntity(character.getKey(), entity.getKey());
			if (oldDiscovery != null) return oldDiscovery;
		}

		if (entity.getKind().equals("Path"))
		{
			CachedEntity discovery = new CachedEntity("Discovery");
			// Set the starting attributes
			discovery.setProperty("characterKey", character.getKey());
			discovery.setProperty("entityKey", entity.getKey());
			discovery.setProperty("kind", entity.getKind());
			discovery.setProperty("location1Key", entity.getProperty("location1Key"));
			discovery.setProperty("location2Key", entity.getProperty("location2Key"));
			discovery.setProperty("hidden", false);
			discovery.setProperty("createdDate", new Date());

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
			discovery.setProperty("hidden", false);
			discovery.setProperty("createdDate", new Date());

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

	public List<CachedEntity> getDiscoveriesForCharacterAndLocation(Key characterKey, Key location, boolean showHidden) {
		return getDiscoveriesForCharacterAndLocation(characterKey, location, showHidden, 1000);
	}
	public List<CachedEntity> getDiscoveriesForCharacterAndLocation(Key characterKey, Key location, boolean showHidden, Integer limit)
	{
		if (limit==null) limit = 1000;
		
		List<CachedEntity> fetchAsList = null;
		
		
		// FOR SOME REASON, THE NICE CODE BELOW TAKES ALMOST A FULL SECOND TO COMPLETE IF SHOW HIDDEN = FALSE. WTF
//		if (showHidden)
//		{
//			List<CachedEntity> location1List = query.getFilteredList("Discovery", "characterKey", characterKey, "location1Key", location);
//			List<CachedEntity> location2List = query.getFilteredList("Discovery", "characterKey", characterKey, "location2Key", location);
//			fetchAsList = location1List;
//			fetchAsList.addAll(location2List);
//		}
//		else
//		{
//			List<CachedEntity> location1List = query.getFilteredList("Discovery", "characterKey", characterKey, "location1Key", location, "hidden", false);
//			List<CachedEntity> location2List = query.getFilteredList("Discovery", "characterKey", characterKey, "location2Key", location, "hidden", false);
//			fetchAsList = location1List;
//			fetchAsList.addAll(location2List);
//		}

		
		// So instead, we will query for all discoveries and then just pair them down
		List<CachedEntity> location1List = query.getFilteredList("Discovery", limit, null, "characterKey", FilterOperator.EQUAL, characterKey, "location1Key", FilterOperator.EQUAL, location);
		List<CachedEntity> location2List = query.getFilteredList("Discovery", limit, null, "characterKey", FilterOperator.EQUAL, characterKey, "location2Key", FilterOperator.EQUAL, location);
		fetchAsList = location1List;
		fetchAsList.addAll(location2List);
		
		if (showHidden==false)
			for(int i = fetchAsList.size()-1; i>=0; i--)
				if (fetchAsList.get(i)==null || GameUtils.equals(fetchAsList.get(i).getProperty("hidden"), true))
					fetchAsList.remove(i);
		
		
		log.log(Level.WARNING, fetchAsList.size() + " discoveries found.");
		
		return fetchAsList;
	}

	/**
	 * Returns no more than a count of 20 locations. There could be more. Also there is a high likelihood that
	 * any number over 10 there could actually be hundreds because this is done with 2 queries and combat sites
	 * tend to favor one or the other.
	 *  
	 * @param characterKey
	 * @param location
	 * @return
	 */
	public int getDiscoveriesForCharacterAndLocation_Count(Key characterKey, Key location)
	{
		// So instead, we will query for all discoveries and then just pair them down
		Long count = query.getFilteredList_Count("Discovery", 10, "characterKey", FilterOperator.EQUAL, characterKey, "location1Key", FilterOperator.EQUAL, location);
		Long count2 = query.getFilteredList_Count("Discovery", 10, "characterKey", FilterOperator.EQUAL, characterKey, "location2Key", FilterOperator.EQUAL, location);
		if (count==null) count = 0L;
		if (count2==null) count2 = 0L;
		
		return count.intValue() + count2.intValue();
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
		
		db.setStat("chatbanIP-" + ip, 1l, CHAT_BAN_SECONDS);

		db.setStat("chatban-" + characterName, 1l, CHAT_BAN_SECONDS);
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
		if (characterName==null) throw new UserErrorMessage("Character name cannot be blank.");
		characterName = characterName.trim();
		if (characterName.length()<1 || characterName.length()>30 || !characterName.matches("^[A-Za-z ]+[!]?$"))
			throw new UserErrorMessage("Character name must contain only letters and spaces, and must be between 1 and 30 characters long.");
		
		if (characterName.startsWith("Dead "))
			throw new UserErrorMessage("Character name cannot start with the word Dead.");
		
		if (characterName.startsWith("Unconscious "))
			throw new UserErrorMessage("Character name cannot start with the word Unconscious.");
		
		if (getCharacterByName(characterName)!=null) throw new UserErrorMessage("Character name already exists in our database. Please choose another.");
		
		
		return true;
	}

	public void doCharacterEquipEntity(CachedDatastoreService db, CachedEntity character, CachedEntity equipment) throws UserErrorMessage
	{
		doCharacterEquipEntity(db, character, equipment, true, false);
	}

	public void doCharacterEquipEntity(CachedDatastoreService db, CachedEntity character, CachedEntity equipment, boolean replaceItem, boolean skipSaleCheck) throws UserErrorMessage
	{
		if (db==null)
			db = getDB();
		
		if (equipment==null)
			throw new IllegalArgumentException("Equipment cannot be null.");
		
		// Get all the equip slots that this item can fit into and decide which one to use
		String equipSlotRaw = (String)equipment.getProperty("equipSlot");
		
		if (equipSlotRaw==null)
			throw new UserErrorMessage("This item is not equipable.");
		
		if (equipSlotRaw.equals("Ring"))
			equipSlotRaw = "LeftRing,RightRing";
		
		if (equipSlotRaw.equals("Cosmetic"))
			equipSlotRaw = "Cosmetic1,Cosmetic2,Cosmetic3";
		
		equipSlotRaw = equipSlotRaw.trim();
		if (equipSlotRaw.endsWith(","))
			equipSlotRaw = equipSlotRaw.substring(0, equipSlotRaw.length()-1);
		String[] equipSlotArr = equipSlotRaw.split(",");
		
		String destinationSlot = null;
		if (equipSlotArr.length==0)
			throw new RuntimeException("No equip slots exist for the '"+equipment.getProperty("name")+"' item.");
		else if (equipSlotArr.length==1)
			destinationSlot = equipSlotArr[0];
		else if (equipSlotArr.length>1)
		{
			// Swap first matching slot if the slot matches and player is equipping.
			boolean swapSlot = CommonChecks.checkCharacterIsPlayer(character) && 
					("Shield".equals(equipment.getProperty("itemType")) || "Weapon".equals(equipment.getProperty("itemType")));
			
			for(int i = equipSlotArr.length-1; i>=0; i--)
			{
				String slotCheck = equipSlotArr[i].trim();
				
				if(CommonChecks.checkIsValidEquipSlot(slotCheck)==false) 
					continue;
				
				if (character.getProperty("equipment"+slotCheck)==null)
				{
					destinationSlot = slotCheck;
					break;
				}
				
				if(swapSlot && destinationSlot == null)
				{
					// If it's a shield/weapon type, find first swap slot.
					CachedEntity currentSlotItem = db.getIfExists((Key)character.getProperty("equipment"+slotCheck));
					
					// Do not break out of the loop early, in case a "free" slot exists.
					if(currentSlotItem != null && 
						GameUtils.equals(equipment.getProperty("itemType"), currentSlotItem.getProperty("itemType")) &&
							("Shield".equals(currentSlotItem.getProperty("itemType")) || 
							"2Hands".equals(currentSlotItem.getProperty("equipSlot"))==false))
						destinationSlot = slotCheck;
				}
			}
			
			if (destinationSlot==null)
				throw new UserErrorMessage("The equipment slots needed to equip this item are all used. Unequip something first.");
		}
		
		if (destinationSlot==null)
			throw new RuntimeException("There was no equipSlot specified for this item: "+equipment);
		
		
		if (character==null)
			throw new IllegalArgumentException("Character cannot be null.");

		ContainerService cs = new ContainerService(this);
		if (!character.getKey().equals(equipment.getProperty("containerKey")))
			throw new IllegalArgumentException("The piece of equipment is not in the character's posession. Character: "+character.getKey());

		// Only prevent HCM equips for players
		if(CommonChecks.checkCharacterIsPlayer(character) == true && 
				CommonChecks.checkIsHardcore(character) == true && 
				CommonChecks.checkIsHardcore(equipment) == false)
			throw new UserErrorMessage("You can only equip Hardcore Mode items.");
		
		destinationSlot = destinationSlot.trim(); // Clean it up, just in case
		
//		String equipmentSlot = (String)equipment.getProperty("equipSlot");
//		if (equipmentSlot==null)
//			throw new UserErrorMessage("You cannot equip this item.");
//		if (destinationSlot.contains(equipmentSlot)==false)
//			throw new CodeException("You cannot put a "+equipmentSlot+" item in the "+destinationSlot+" slot.");

		Double characterStrength = (Double)character.getProperty("strength");
		// ROund character strength just like it is rounded for the popup
		characterStrength = Double.parseDouble(GameUtils.formatNumber(characterStrength));
		if (equipment.getProperty("strengthRequirement") instanceof String)
			equipment.setProperty("strengthRequirement", null);
		Double strengthRequirement = (Double)equipment.getProperty("strengthRequirement");
		if (strengthRequirement!=null && characterStrength!=null && strengthRequirement>characterStrength && "NPC".equals(character.getProperty("type"))==false)
			throw new UserErrorMessage("You cannot equip this item, you do not have the strength to use it.");
		
		
		if (skipSaleCheck==false && isItemForSale(db, equipment))
			throw new UserErrorMessage("You cannot equip this item, it is currently for sale.");
		
		if (destinationSlot.equals("2Hands"))
			destinationSlot = "LeftHand and RightHand";

		
		if (destinationSlot!=null && destinationSlot.trim().equals("")==false)
		{
			String[] destinationSlots = destinationSlot.split(" and ");
			// Validate destination slots first.
			for (String slot:destinationSlots)
				if(CommonChecks.checkIsValidEquipSlot(slot.trim())==false)
					throw new RuntimeException("Invalid EquipSlot specified for item.");
			
			// Check if we need to unequip some items first if they're in the way (and replacing is requested)
			for (String slot:destinationSlots)
			{
				
				// If we already have some equipment in the slot we want to equip to, then unequip it first...
				if (character.getProperty("equipment"+slot)!=null)
				{
					if (replaceItem==false)
						return;
					doCharacterUnequipEntity(db, character, slot);
				}
			}			
			
			// Now equip
			for (String slot:destinationSlots)
			{
				// Now equip this weapon...
				character.setProperty("equipment"+slot, equipment.getKey());
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
			if (GameUtils.equals(equipment.getKey(), character.getProperty("equipment" + slot)))
				character.setProperty("equipment" + slot, null);
		}

		db.put(character);
	}

	public void doCharacterUnequipEntity(CachedDatastoreService db, CachedEntity character, Key entityKey)
	{
		if (db == null) db = getDB();

		CachedEntity equipment = getEntity(entityKey);
		if (equipment == null) return;

		doCharacterUnequipEntity(db, character, equipment);
	}

	public void doCharacterRestFully(CachedEntity character)
	{
		Double hitpoints = (Double) character.getProperty("maxHitpoints");
		if ((Double) character.getProperty("hitpoints") < hitpoints)
		{
			character.setProperty("hitpoints", hitpoints);

		}

	}

	
	public void doCharacterCollectItem(OperationBase command, CachedEntity character, CachedEntity item) throws UserErrorMessage
	{
		doCharacterCollectItem(command, character, item, false);
	}
	
	
	public void doCharacterCollectItem(OperationBase command, CachedEntity character, CachedEntity item, boolean overrideImmovable) throws UserErrorMessage
	{
		if (character == null) throw new IllegalArgumentException("Character cannot be null.");
		if (item == null) throw new UserErrorMessage("This item no longer exists.");

		if (CommonChecks.checkCharacterIsIncapacitated(character))
			throw new UserErrorMessage("You cannot do that right now. You're incapacitated.");
		
		if (overrideImmovable==false && CommonChecks.checkItemIsMovable(item)==false)
			throw new UserErrorMessage("You cannot pick up this item.");
		
		if (character.getProperty("locationKey").equals(item.getProperty("containerKey")) == false) throw new UserErrorMessage("The item is not here anymore. Perhaps someone else grabbed it.");

		// Check if the character can actually carry something else or if its
		// all too heavy...
		Long newItemWeight = getItemWeight(item);
		if (newItemWeight != null && newItemWeight > 0d)
		{
			long carrying = getCharacterCarryingWeight(character);
			long maxCarrying = getCharacterMaxCarryingWeight(character);

			if (carrying + newItemWeight > maxCarrying)
				throw new UserErrorMessage("You cannot carry any more stuff! You are currently carrying " + GameUtils.formatNumber(carrying) + " grams and can carry a maximum of "
						+ GameUtils.formatNumber(maxCarrying) + " grams.");
		}
		//TODO: stack stackable items with an existing stack
		item.setProperty("containerKey", character.getKey());
		item.setProperty("movedTimestamp", new Date());

		QuestService questService = command.getQuestService();
		questService.checkAcquiredItemForObjectiveCompletions(item);
		
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
	
    /**
     * Drops (and possibly revives) the character to the specified location
     * @param location Current location of the characters.
     * @param currentCharacter Character who will be performing the drop.
     * @param dropCharacter Character who will be dropped (and possibly revived).
     */
    public void doCharacterDropCharacter(CachedEntity location, CachedEntity character, CachedEntity characterToDrop) throws UserErrorMessage
    {
        if (character==null)
            throw new IllegalArgumentException("Character cannot be null.");
        if (characterToDrop==null)
            throw new IllegalArgumentException("Character to drop cannot be null.");
         
        if (GameUtils.equals(character.getKey(), characterToDrop.getProperty("locationKey"))==false)
            throw new UserErrorMessage("You cannot put this character down, you're not carrying them.");
         
        Key characterLocationKey = (Key)character.getProperty("locationKey");
         
        characterToDrop.setProperty("locationKey", characterLocationKey);
        characterToDrop.setProperty("movedTimestamp", new Date());
         
        // If we are dropping the character in a rest area, heal him to 1 hp
        if ("RestSite".equals(location.getProperty("type")) || "CampSite".equals(location.getProperty("type")))
        {
            if ((Double)characterToDrop.getProperty("hitpoints")<=0 && 
                    "NPC".equals(characterToDrop.getProperty("type"))==false &&
                    GameUtils.enumEquals(characterToDrop.getProperty("mode"), CharacterMode.UNCONSCIOUS))
            {
                characterToDrop.setProperty("hitpoints", 1d);
                 
                // Also unequip all of his equipment. 
                for(String slot:EQUIPMENT_SLOTS)
                    characterToDrop.setProperty("equipment"+slot, null);
                 
                // Reset his mode to normal
                characterToDrop.setProperty("mode", CHARACTER_MODE_NORMAL);
            }
        }
         
        getDB().put(characterToDrop);
    }
	
	/**
	 * Performs the drop of the item from the character and saves the entity. 
	 * Calls tryCharacterDropItem, which will throw if not successful. 
	 */
	public void doCharacterDropItem(CachedEntity character, CachedEntity item, Long tileX, Long tileY) throws UserErrorMessage
	{
		tryCharacterDropItem(character, item, tileX, tileY, true);
		getDB().put(item);
		getGridMapService().putLocationData(getDB());
		
	}
	
	/**
	 * Attempts to drop the item, setting the appropriate fields on the Item entity.
	 * @return True if item was dropped, false only if throwError is false and the item is equipped or vending.
	 * @throws UserErrorMessage Thrown if item is equipped or vending, and throwError parameter is true.
	 */
	public boolean tryCharacterDropItem(CachedEntity character, CachedEntity item, Long tileX, Long tileY, boolean throwError) throws UserErrorMessage
	{
		if (character == null) throw new IllegalArgumentException("Character cannot be null.");
		if (item == null) throw new IllegalArgumentException("Item cannot be null.");

		CachedEntity location = getCharacterLocation(character);

		if (checkCharacterHasItemEquipped(character, item.getKey())) {
			if(throwError) throw new UserErrorMessage("Your character has this item equipped. You cannot drop it until it is unequipped.");
			return false;
		}

		if (checkItemIsVending(character.getKey(), item.getKey())) {
			if(throwError) throw new UserErrorMessage("The item you are trying to drop is currently in your store. You cannot drop an item that you plan on selling.");
			return false;
		}
		InitiumObject iObject = new InitiumObject(this, item);
		iObject.moveItemToLocation(location, tileX, tileY);

		return true;
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
			if (GameUtils.equals(itemKey, equipmentInSlot)) 
				return true;
		}

		return false;
	}

	public Double getCharacterDexterity(CachedEntity character)
	{
		Double dex = getCachedCharacterStat(character, "dexterity");
		if(dex == null)
			dex = getDoubleBuffableProperty(character, "dexterity");
	
		if (dex < 2) dex = 2d;

		return dex;
	}

	public Double getCharacterStrength(CachedEntity character)
	{
		Double str = getCachedCharacterStat(character, "strength");
		if(str == null)
			str = getDoubleBuffableProperty(character, "strength");
	
		if (str < 2) str = 2d;

		return str;
	}
	
	public Double getCharacterIntelligence(CachedEntity character)
	{
		Double valInt = getCachedCharacterStat(character, "intelligence");
		if(valInt == null)
			valInt = getDoubleBuffableProperty(character, "intelligence");
	
		if (valInt < 2) valInt = 2d;

		return valInt;
	}
	
	public void fillCachedCharacterStats(CachedEntity character)
	{
		Double baseDex = (Double)character.getProperty("dexterity");
		Double baseInt = (Double)character.getProperty("intelligence");
		Double baseStr = (Double)character.getProperty("strength");
		
		Double buffDex = getDoubleBuffableProperty(character, "dexterity") - baseDex;
		Double buffInt = getDoubleBuffableProperty(character, "intelligence") - baseInt;
		Double buffStr = getDoubleBuffableProperty(character, "strength") - baseStr;
		
		// Get all dexterity reducing armors and include that in the
		// calculation...
		List<Key> slotKeys = new ArrayList<Key>();
		for (String slot : EQUIPMENT_SLOTS)
			if (character.getProperty("equipment" + slot) != null)
				slotKeys.add((Key)character.getProperty("equipment" + slot));
		
		List<CachedEntity> equips = getDB().get(slotKeys);
		for(CachedEntity item:equips)
		{
			if (item == null) continue;
			Long modifier = (Long) item.getProperty("dexterityPenalty");
			if (modifier != null)
				buffDex -= (baseDex * (modifier.doubleValue() / 100d));
			
			modifier = (Long) item.getProperty("intelligenceModifier");
			if (modifier != null)
				buffInt += (baseInt * (modifier.doubleValue() / 100d));
			
			modifier = (Long) item.getProperty("strengthModifier");
			if (modifier != null)
				buffStr += (baseStr * (modifier.doubleValue() / 100d));
		}
		
		Map<String, Double> charMap = new HashMap<String, Double>();
		charMap.put("strength", baseStr + buffStr);
		charMap.put("intelligence", baseInt + buffInt);
		charMap.put("dexterity", baseDex + buffDex);
		statsCache.put(character.getKey(), charMap);
	}
	
	public Double getCachedCharacterStat(CachedEntity charEntity, String stat)
	{
		Map<String, Double> charMap = statsCache.get(charEntity.getKey());
		if(charMap == null) 
		{
			fillCachedCharacterStats(charEntity);
			charMap = statsCache.get(charEntity.getKey());
			if(charMap == null) return getDoubleBuffableProperty(charEntity, stat);
		}
		return charMap.get(stat);
	}
	
	public void setCachedCharacterStat(CachedEntity charEntity, String stat, Double value)
	{
		Map<String, Double> charMap = statsCache.get(charEntity.getKey());
		if(charMap == null)
		{
			charMap = new HashMap<String, Double>();
			statsCache.put(charEntity.getKey(), charMap);
		}
		charMap.put(stat, value);
	}
	
	@SuppressWarnings("unchecked")
	private void addBuffTo(CachedEntity entity, EmbeddedEntity buff)
	{
		if (entity.getKind().equals("Character"))
		{
			List<EmbeddedEntity> buffs = (List<EmbeddedEntity>)entity.getProperty("buffs");
			if (buffs==null) buffs = new ArrayList<>();
			buffs.add(buff);
			entity.setProperty("buffs", buffs);
		}
		else if (entity.getKind().equals("Item"))
		{
			if (((List<String>)entity.getProperty("_aspects")).contains("Buffable")==false)
				throw new IllegalArgumentException("The item '"+entity.getProperty("name")+"' is not buffable. It's missing the Buffable aspect.");
				
			List<EmbeddedEntity> buffs = (List<EmbeddedEntity>)entity.getProperty("Buffable:buffs");
			if (buffs==null) buffs = new ArrayList<>();
			buffs.add(buff);
			entity.setProperty("Buffable:buffs", buffs);
		}
		else throw new IllegalArgumentException("Unhandled kind: "+entity.getKind());
	}
	
	public void markBuffToDelete(PropertyContainer buff)
	{
		if (buff==null) return;

		buff.setProperty("_todelete", true);
	}

	public PropertyContainer awardBuff(CachedDatastoreService ds, CachedEntity parent, String icon, String name, String description, int durationInSeconds, String field1Name, String field1Effect,
			String field2Name, String field2Effect, String field3Name, String field3Effect, int maximumCount)
	{
		List<EmbeddedEntity> buffsAlreadyOn = getBuffsFor(parent);

		int existingCount = 0;
		for (PropertyContainer b : buffsAlreadyOn)
			if (name.equals(b.getProperty("name"))) existingCount++;

		if (existingCount >= maximumCount) return null;
		EmbeddedEntity buff = new EmbeddedEntity();
		buff.setKey(KeyFactory.createKey("Buff", GameUtils.rnd.nextLong()));

		buff.setProperty("parentKey", parent.getKey());
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

		addBuffTo(parent, buff);
		
		return buff;
	}
	
	public EmbeddedEntity awardBuffByDef(String buffDefName, CachedEntity character)
	{
		List<CachedEntity> buffDefs = getFilteredList("BuffDef", "name", buffDefName);
		if(buffDefs.size() > 1)
		{
			// BuffDef.name must be unique
			return null; 
		}
		
		// There should only be 1. If not, it will fall through and return a null value. 
		for(CachedEntity def:buffDefs)
		{
			if(def.getProperty("maxCount")!=null)
			{
				Long maximumCount = (Long)def.getProperty("maxCount");
				int existingCount = 0;
				List<EmbeddedEntity> buffs = getBuffsFor(character);
				for(EmbeddedEntity appliedBuff:buffs)
					if(buffDefName.equals(appliedBuff.getProperty("name")))
						existingCount++;
				if (existingCount >= maximumCount) return null;
			}
			
			EmbeddedEntity newBuff = generateNewEmbeddedObject(def, "Buff");
			if(newBuff != null)
			{
				if (newBuff.getProperty("expiry")!=null)
				{
					int expiry = ((Long)newBuff.getProperty("expiry")).intValue();
					GregorianCalendar cal = new GregorianCalendar();
					cal.add(Calendar.SECOND, expiry);
					newBuff.setProperty("expiry", cal.getTime());
				}
				newBuff.setProperty("parentKey", character.getKey());
				
				addBuffTo(character, newBuff);
			}
			
			return newBuff;
		}
		return null;
	}
	
	public boolean awardBuffByName(CachedDatastoreService ds, CachedEntity character, String buffName)
	{
		if (ds == null) ds = getDB();
		
		EmbeddedEntity buff = awardBuffByDef(buffName, character);
		
		return buff != null;
	}


	public boolean awardBuff_Pumped(CachedDatastoreService ds, CachedEntity attackingCharacter)
	{
		if (ds == null) ds = getDB();

		PropertyContainer buff = awardBuff(ds, attackingCharacter, "images/small/Pixel_Art-Icons-Buffs-S_Buff14.png", "Pumped!",
				"You're pumped! This buff is awarded when you kill a monster while still being full health. The effect lasts for 1 minute.", 60, 
				"strength", "+10%", 
				"dexterity", "+10%",
				"intelligence", "+5%", 3);

		return buff != null;
	}
	
	public boolean awardBuff_Candy(CachedDatastoreService ds, CachedEntity character)
	{
		Double buffDouble = Math.random();
		PropertyContainer buff = null;
		if(buffDouble <= 0.16){
			buff = awardBuff(ds, character, "images/small2/Pixel_Art-Misc-Buff_Treat.png","Treat!",
				"That was some good candy! You feel stronger!",600,"strength","+0.2",null,null,null,null,10);
		}
		if((buffDouble >= 0.17) && (buffDouble < 0.32)){
			buff = awardBuff(ds, character, "images/small2/Pixel_Art-Misc-Buff_Treat.png","Treat!",
				"That was some good candy! You feel more agile!",600,"dexterity","+0.2",null,null,null,null,10);
		}
		if((buffDouble >= 0.32) && (buffDouble < 0.48)){
			buff = awardBuff(ds, character, "images/small2/Pixel_Art-Misc-Buff_Treat.png","Treat!",
				"That was some good candy! You feel smarter!",600,"Intelligence","+0.2",null,null,null,null,10);
		}
		if((buffDouble >= 0.48) && (buffDouble < 0.64)){
			buff = awardBuff(ds, character, "images/small2/Pixel_Art-Misc-Buff_Trick.png","Trick!",
				"That candy was terrible! You feel weaker!",600,"strength","-0.2",null,null,null,null,10);
		}
		if((buffDouble >= 0.64) && (buffDouble < 0.80)){
			buff = awardBuff(ds, character, "images/small2/Pixel_Art-Misc-Buff_Trick.png","Trick!",
				"That candy was terrible! You feel slower!",600,"strength","-0.2",null,null,null,null,10);
		}
		if((buffDouble >= 0.80) && (buffDouble < 0.96)){
			buff = awardBuff(ds, character, "images/small2/Pixel_Art-Misc-Buff_Trick.png","Trick!",
				"That candy was terrible! You feel dumb!",600,"intelligence","-0.2",null,null,null,null,10);
		}
		if((buffDouble >= 0.96)){
			buff = awardBuff(ds, character, "images/small2/Pixel_Art-Misc-Buff_Treat.png","Treat!",
				"That was some good candy! You feel great!",600,"strength","+0.2","dexterity","+0.2","intelligence","+0.2",10);
		}
		
		return buff != null;
	}

	//Sort sale items by Type --> Name --> Max Damage or Block Chance --> Cost
	public List<CachedEntity> sortSaleItemList(List<CachedEntity> items)
	{
		List<CachedEntity> sorted = new ArrayList<CachedEntity>(items);
		
		// Adding a computed value for weapons/armor/shields for sort.
		for(CachedEntity item:sorted)
		{
			String itemType = (String)item.getProperty("itemType");
			if("Armor".equals(itemType) || "Shield".equals(itemType))
			{
				Double sortMax = 0.0d;
				if(item.getProperty("blockChance") != null)
					sortMax = ((Long)item.getProperty("blockChance")).doubleValue();
				if(sortMax != null && item.getProperty("damageReduction") != null)
					sortMax += Math.abs(((Long)item.getProperty("damageReduction")).doubleValue() / 1000.0d);
				item.setProperty("_sortMax", sortMax);
			}
			else if("Weapon".equals(itemType))
			{
				Double sortMax = GameUtils.getWeaponMaxDamage(item);
				if(sortMax != null)
					sortMax += Math.abs(GameUtils.getWeaponAverageDamage(item) / 1000.0d);
				item.setProperty("_sortMax", sortMax);
			}
		}
		
		Collections.sort(sorted, new Comparator<CachedEntity>()
		{
			@Override
			public int compare(CachedEntity item1, CachedEntity item2)
			{
				String item1Type = (String) item1.getProperty("itemType");
				String item2Type = (String) item2.getProperty("itemType");

				String item1Name = (String) item1.getProperty("name");
				String item2Name = (String) item2.getProperty("name");

				String item1Label = (String) item1.getProperty("label");
				String item2Label = (String) item2.getProperty("label");

				Long item1Cost = (Long) item1.getProperty("store-dogecoins");
				Long item2Cost = (Long) item2.getProperty("store-dogecoins");
				
				Double item1Max = (Double)item1.getProperty("_sortMax");
				Double item2Max = (Double)item2.getProperty("_sortMax");

				if (item1Type == null) item1Type = "";
				if (item2Type == null) item2Type = "";
				if (item1Name == null) item1Name = "";
				if (item2Name == null) item2Name = "";
				if (item1Label == null) item1Label = "";
				if (item2Label == null) item2Label = "";
				if (item1Max == null) item1Max = 0d;
				if (item2Max == null) item2Max = 0d;
				if (item1Cost == null) item1Cost = 0l;
				if (item2Cost == null) item2Cost = 0l;
				
				int compValue = item1Type.compareTo(item2Type);
				if (compValue != 0) return compValue;
				compValue = item1Name.compareTo(item2Name);
				if (compValue != 0) return compValue;
				compValue = item1Label.compareTo(item2Label);
				if (compValue != 0) return compValue;
				compValue = -item1Max.compareTo(item2Max);
				if (compValue != 0) return compValue;
				// If all other comparisons equal, compare costs.
				return item1Cost.compareTo(item2Cost);
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

	
	
	
	
	
	
	///////////////////////////////////
	// BUFFS
	//
	// All this buff stuff should go in it's own service for goodness sake
	///////////////////////////////////
	
	
	
	
	
	
	
	
	public void reduceBuffCharges(CachedEntity item)
	{
		@SuppressWarnings("unchecked")
		List<EmbeddedEntity> buffs = (List<EmbeddedEntity>)item.getProperty("Buffable:buffs");
		
		if (buffs!=null)
		{
			for(int i = buffs.size()-1; i>=0; i--)
			{
				Long charges = (Long)buffs.get(i).getProperty("charges");
				if (charges!=null)
				{
					charges -= 1;
					if (charges<=0)
					{
						buffs.get(i).setProperty("_todelete", true);
						buffs.remove(i);
					}
					else
						buffs.get(i).setProperty("charges", charges);
				}
			}
		}
		
	}
	
	public Double getDoubleBuffableValue(CachedEntity entity, StaticBuffables fieldName, Double startValue)
	{
		return getDoubleBuffableValue(entity, fieldName.toString(), startValue);
	}	
	
	public Double getDoubleBuffableValue(CachedEntity entity, String fieldName, Double startValue)
	{
		if (startValue == null) return null;
		List<String> buffEffects = getBuffEffectsFor(entity, fieldName);

		Double buffAmount = 0d;
		for (String effect : buffEffects)
		{
			effect = effect.replace("+", "");
			if (effect.endsWith("%"))
			{
				effect = effect.substring(0, effect.length() - 1);
				double val = new Double(effect);
				val /= 100;
				buffAmount += startValue * val;
			}
			else
			{
				double val = new Double(effect);
				buffAmount += val;
			}
		}
		
		ModifierService mService = new ModifierService(this);
		Collection<CachedEntity> characterEquipment = getCharacterEquipment(entity);
		ModifierType modifierType = mService.getModifierType(fieldName);
		if (modifierType!=null)
		{
			for(CachedEntity equip:characterEquipment)
			{
				if(equip != null)
					buffAmount += mService.getAffectedValue(startValue, equip, modifierType) - startValue;
			}
		}
		
		return startValue + buffAmount;
	}

	public Double getDoubleBuffableProperty(CachedEntity entity, String fieldName)
	{
		return getDoubleBuffableValue(entity, fieldName, (Double)entity.getProperty(fieldName));
	}

	public Long getLongBuffableValue(CachedEntity entity, StaticBuffables fieldName, Long startValue)
	{
		return getLongBuffableValue(entity, fieldName.toString(), startValue);
	}
	
	public Long getLongBuffableValue(CachedEntity entity, String fieldName, Long startValue)
	{
		if (startValue == null) return null;
		List<String> buffEffects = getBuffEffectsFor(entity, fieldName);

		Double buffAmount = 0d;
		Double statStart = startValue.doubleValue();
		for (String effect : buffEffects)
		{
			effect = effect.replace("+", "");
			if (effect.endsWith("%"))
			{

				effect = effect.substring(0, effect.length() - 1);
				double val = new Double(effect);
				val /= 100;
				buffAmount += statStart * val;
			}
			else
			{
				double val = new Double(effect);
				buffAmount += val;
			}
		}
		
		
		ModifierService mService = new ModifierService(this);
		Collection<CachedEntity> characterEquipment = getCharacterEquipment(entity);
		ModifierType modifierType = mService.getModifierType(fieldName);
		if (modifierType!=null)
		{
			for(CachedEntity equip:characterEquipment)
			{
				buffAmount += mService.getAffectedValue(statStart, equip, modifierType) - statStart;
			}
		}
		
		return Math.round(statStart + buffAmount);
	}

	public Long getLongBuffableProperty(CachedEntity entity, String fieldName)
	{
		return getLongBuffableValue(entity, fieldName, (Long)entity.getProperty(fieldName));
	}

	public List<String> getBuffEffectsFor(CachedEntity entity, String fieldName)
	{
		List<String> result = new ArrayList<String>();
		List<EmbeddedEntity> buffs = getBuffsFor(entity);

		for (EmbeddedEntity buff : buffs)
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

	public List<String> getBuffEffectsFor(CachedEntity entity)
	{
		List<String> result = new ArrayList<String>();
		List<EmbeddedEntity> buffs = getBuffsFor(entity);

		for (EmbeddedEntity buff : buffs)
		{
			for (int i = 1; i <= 3; i++)
			{
				if (buff.getProperty("field" + i + "Name")!=null)
				{
					String buffName = (String)buff.getProperty("name");
					String effect = (String) buff.getProperty("field" + i + "Effect");
					effect = effect.trim();
					if (effect != null && effect.matches("[-+][0-9.]+%?")) 
					{
						effect+=" "+GameUtils.camelCaseToSpaced((String)buff.getProperty("field" + i + "Name"))+" from "+buffName+" effect";
						result.add(effect);
					}
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

	private boolean isBuffExpired(PropertyContainer buff)
	{
		if (buff==null)
			return true;
		Date expiry = (Date) buff.getProperty("expiry");
		if (expiry != null && expiry.before(new Date()))
			return true;
		
		if (GameUtils.equals(buff.getProperty("_todelete"), true))
			return true;
		
		if (buff.getProperty("charges")!=null)
		{
			if ((Long)buff.getProperty("charges")<=0)
				return true;
		}
		
		return false;
	}
	
	public List<EmbeddedEntity> getBuffsFor(CachedEntity entity)
	{
		List<EmbeddedEntity> buffs = new ArrayList<>();
		
		if (entity.getKind().equals("Character"))
			buffs = getBuffsForCharacter(entity);
		else if (entity.getKind().equals("Item"))
			getBuffs(entity, buffs);

		return buffs;
	}
	
	public List<EmbeddedEntity> getBuffsForCharacter(CachedEntity character)
	{
		ArrayList<EmbeddedEntity> buffs = new ArrayList<>();
		
		getBuffs(character, buffs);
		
		Collection<CachedEntity> characterEquipment = getCharacterEquipment(character);
		for(CachedEntity equipment:characterEquipment)
		{
			getBuffs(equipment, buffs);
		}
		
		return buffs;
	}
	
	@SuppressWarnings("unchecked")
	private void getBuffs(CachedEntity entity, List<EmbeddedEntity> buffs)
	{
		if (GameUtils.equals(entity.getKind(), "Item"))
		{
			if (entity.getProperty("_aspects")!=null && ((List<String>)entity.getProperty("_aspects")).contains("Buffable"))
			{
				InitiumObject item = new InitiumObject(this, entity);
				AspectBuffable buffable = item.getAspect(AspectBuffable.class);
				if (buffable!=null)
				{
					List<EmbeddedEntity> list = buffable.getBuffs();
					boolean buffsRemoved = false;
					if (list!=null)
						for(int i = list.size()-1; i>=0; i--)
							if (isBuffExpired(list.get(i)))
							{
								list.remove(i);
								buffsRemoved = true;
							}

					if (buffsRemoved)
						buffable.setBuffs(list);
					
					if (list!=null)
						buffs.addAll(list);
				}
			}
		}
		else if (GameUtils.equals(entity.getKind(), "Character"))
		{
			List<EmbeddedEntity> list = (List<EmbeddedEntity>)entity.getProperty("buffs");
			boolean buffsRemoved = false;
			if (list!=null)
				for(int i = list.size()-1; i>=0; i--)
					if (isBuffExpired(list.get(i)))
					{
						list.remove(i);
						buffsRemoved = true;
					}

			if (buffsRemoved)
				entity.setProperty("buffs", list);
			
			if (list!=null)
				buffs.addAll(list);
		}
		else
			throw new RuntimeException("Unsupported type");
	}

	
	
	
	///////////////////////////////////
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
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

	public List<Key> getItemsListSortedForLocation_Keys(CachedDatastoreService ds, Key locationKey)
	{
		if (ds == null) ds = getDB();
		Query q = new Query("Item").setFilter(new FilterPredicate("containerKey", FilterOperator.EQUAL, locationKey)).addSort("movedTimestamp", SortDirection.DESCENDING);
		return ds.fetchAsList_Keys(q, 50, null);
	}

	/**
	 * Returns a list of items for the specified container. Location will return 50 max items.
	 * @param container
	 * @return
	 */
	public List<CachedEntity> getItemContentsFor(Key container)
	{
		return getItemContentsFor(container, false);
	}
	
	/**
	 * Returns a list of items for the specified container. If not in a player/group owned house,
	 * will only show 50 items.
	 * @param container
	 * @param inOwnedHouse
	 * @return
	 */
	public List<CachedEntity> getItemContentsFor(Key container, boolean inOwnedHouse)
	{
		if ("Location".equals(container.getKind()) && inOwnedHouse == false)
		{
			return getItemsListSortedForLocation(null, container);
		}
		else
		{
			return getFilteredList("Item", "containerKey", FilterOperator.EQUAL, container);
		}
	}
	
	public boolean tryMoveItem(OperationBase command, CachedEntity character, CachedEntity item, CachedEntity newContainer, Long requestQuantity) throws UserErrorMessage
	{
		CachedDatastoreService ds = getDB();

		if (CommonChecks.checkItemIsMovable(item)==false)
			throw new UserErrorMessage("You cannot move this item.");
		
		if (GameUtils.equals(item.getKey(), newContainer.getKey()))
			throw new UserErrorMessage("lol, you cannot transfer an item into itself, the universe would explode.");

		if (checkCharacterHasItemEquipped(character, item.getKey()))
			throw new UserErrorMessage("Your character has this item equipped. You cannot move it until it is unequipped.");
		
		if (checkItemIsVending(character.getKey(), item.getKey()))
			throw new UserErrorMessage("The item you are trying to drop is currently in your store. You cannot move an item that you plan on selling.");
		
		CachedEntity startContainer = getEntity((Key)item.getProperty("containerKey"));
		String startKind = startContainer.getKind();
		String endKind = newContainer.getKey().getKind();

		ContainerService cs = new ContainerService(this);
		
		if (cs.checkContainerAccessAllowed(character, startContainer)==false)
			throw new UserErrorMessage("Hey!");
		if (cs.checkContainerAccessAllowed(character, newContainer)==false)
			throw new UserErrorMessage("Hey!");

		Long itemQuantity = (Long)item.getProperty("quantity");
		if (itemQuantity==null) itemQuantity = 1L;

		log.log(Level.WARNING, String.format("Moving %s from %s to %s", 
				item.getKey().toString(), startContainer.getKey().toString(), newContainer.getKey().toString()));
		
		// We fetch these values to use later in case of partial pickups
		Long itemSingleWeight = (Long)item.getProperty("weight");
		Long itemSingleSpace = (Long)item.getProperty("space");
		if (itemSingleWeight==null) itemSingleWeight = 0L;
		if (itemSingleSpace==null) itemSingleSpace = 0L;

		// A couple sanity checks
		if (requestQuantity != null && requestQuantity > itemQuantity) requestQuantity = itemQuantity;
		if (requestQuantity != null && requestQuantity < 1L) requestQuantity = 1L;

		// Declaring these here so we can use them at the end for a message
		boolean stackTooHeavy = false;
		boolean stackTooLarge = false;

		boolean handled = false;

		if (startKind.equals("Character"))
		{
			
			if (endKind.equals("Location"))
			{
				handled = true;
				// Make sure we're holding the item that we wish to move to the ground
				if (GameUtils.equals(item.getProperty("containerKey"), character.getKey())==false)
					throw new UserErrorMessage("You do not have possession of this item and so you cannot move it.");
				
				// Items can only be moved into locations if the character is currently in said location
				if (character.getProperty("locationKey").equals(newContainer.getKey())==false)
					throw new UserErrorMessage("You are not standing in the same location as the location you wish to move the item to. You cannot do this.");
				
			}
			else if (endKind.equals("Item"))
			{
				handled = true;
				// Make sure the container we're moving to is either in our inventory or in our location...
				if (GameUtils.equals(newContainer.getProperty("containerKey"), character.getKey())==false && GameUtils.equals(newContainer.getProperty("containerKey"), character.getProperty("locationKey"))==false)
					throw new UserErrorMessage("You do not have physical access to this item so you cannot transfer anything to/from it. It needs to be near you or in your inventory.");


				// Check if the container is already in a container, and if the item we're transferring is a container. We don't want to allow that depth.
				if (item.getProperty("maxWeight")!=null)
				{
					if (((Key)newContainer.getProperty("containerKey")).getKind().equals("Item"))
						throw new UserErrorMessage("You cannot put a container within a container within a container. We cannot allow that depth of containering because efficiency.");
				}
				
				// Make sure we can actually put things into this item container...
				Long maxWeight = (Long)newContainer.getProperty("maxWeight");
				Long maxSpace = (Long)newContainer.getProperty("maxSpace");
				if (maxWeight==null || maxSpace==null)
					throw new UserErrorMessage("This item cannot contain other items.");

				// If quantity is specified, measure for specified quantity rather than whole stack
				Long itemWeight = (requestQuantity != null) ? requestQuantity*itemSingleWeight : getItemWeight(item);
				Long itemSpace = (requestQuantity != null) ? requestQuantity*itemSingleSpace : getItemSpace(item);
				
				List<CachedEntity> containerInventory = getItemContentsFor(newContainer.getKey());
				
				Long containerAvailableWeight = maxWeight - getItemCarryingWeight(newContainer, containerInventory);
				Long containerAvailableSpace = maxSpace - getItemCarryingSpace(newContainer, containerInventory);

				// Skip checks below if encumbered and item has weight or space at all
				if (containerAvailableWeight < 0L && itemSingleWeight > 0L)
					throw new UserErrorMessage("The container cannot accept this item. It is too heavy.");
				if (containerAvailableSpace < 0L && itemSingleSpace > 0L)
					throw new UserErrorMessage("This item will not fit. There is not enough space.");

				// If the item is dimensionless, we skip the following checks altogether
				stackTooHeavy = ((containerAvailableWeight < itemWeight) && (itemSingleWeight>0L));
				stackTooLarge = ((containerAvailableSpace < itemSpace) && (itemSingleSpace>0L));

				if (stackTooHeavy && stackTooLarge)
				{
					requestQuantity = Math.min((containerAvailableWeight / itemSingleWeight), (containerAvailableSpace / itemSingleSpace));
					if (requestQuantity<=0L)
						throw new UserErrorMessage("Container is completely full. Have you ever seen the show Hoarders?");
				}
				else if (stackTooHeavy)
				{
					requestQuantity = Math.min(itemQuantity, (containerAvailableWeight / itemSingleWeight));
					if (requestQuantity<=0L)
						throw new UserErrorMessage("The container cannot accept this item. It is too heavy.");
				}
				else if (stackTooLarge)
				{
					requestQuantity = Math.min(itemQuantity, (containerAvailableSpace / itemSingleSpace));
					if (requestQuantity<=0L)
						throw new UserErrorMessage("This item will not fit. There is not enough space.");
				}

				// Now we'll reduce the durability of the container
				if (itemWeight>=1000)
					cs.doUse(ds, newContainer, 1);
				if (newContainer.isUnsaved())
					ds.put(newContainer);
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
				// Items can only be picked up from locations if the character is currently in said location
				if (GameUtils.equals(character.getProperty("locationKey"), item.getProperty("containerKey"))==false)
					throw new UserErrorMessage("You are not near this item, you cannot pick it up.");
				
				// Check if the character can actually carry something else or if its all too heavy...
				// And if quantity is specified, measure for specified quantity rather than whole stack
				Long itemWeight = (requestQuantity != null) ? requestQuantity*itemSingleWeight : getItemWeight(item);

				// If the item has a maxWeight, we will treat it as a container and include it's contents in the weight calculation..
				if (item.getProperty("maxWeight")!=null)
				{
					Long itemCarryingWeight = getItemCarryingWeight(item);
					itemWeight+=itemCarryingWeight;
				}

				Long characterAvailableWeight = (Long)getCharacterMaxCarryingWeight(character) - getCharacterCarryingWeight(character);

				// Skip checks below if encumbered and item has weight at all
				if (characterAvailableWeight < 0L && itemSingleWeight > 0L)
					throw new UserErrorMessage("You try to pick it up, but it's too heavy!");

				// If the item is weightless, we skip the following check
				stackTooHeavy = ((characterAvailableWeight < itemWeight) && (itemSingleWeight>0L));

				if (stackTooHeavy)
				{
					requestQuantity = Math.min(itemQuantity, (characterAvailableWeight / itemSingleWeight));
					if (requestQuantity<=0L)
						throw new UserErrorMessage("You try to pick it up, but it's too heavy! You can only carry "+GameUtils.formatNumber(characterAvailableWeight)+" grams more.");
				}
			}
			
		}
		else if (startKind.equals("Item"))
		{
			if ("Character".equals(newContainer.getKind()))
			{
				handled = true;
				
				Key oldContainerKey = (Key)item.getProperty("containerKey");
				CachedEntity oldContainer = getEntity(oldContainerKey);
				
				CachedEntity characterPickingUp = newContainer;
				
				// Items can only be picked up from item-containers if the character is currently in the same location as said container
				// OR if the container is in the character's inventory
				if (GameUtils.equals(characterPickingUp.getProperty("locationKey"), oldContainer.getProperty("containerKey"))==false && 
						GameUtils.equals(characterPickingUp.getKey(), oldContainer.getProperty("containerKey"))==false)
					throw new UserErrorMessage("You do not have physical access to this item so you cannot transfer anything to/from it. It needs to be near you or in your inventory.");
				
				// Check if the character can actually carry something else or if its all too heavy...
				// And if quantity is specified, measure for specified quantity rather than whole stack
				Long itemWeight = (requestQuantity != null) ? requestQuantity*itemSingleWeight : getItemWeight(item);

				// If the item has a maxWeight, we will treat it as a container and include it's contents in the weight calculation..
				if (item.getProperty("maxWeight")!=null)
				{
					Long itemCarryingWeight = getItemCarryingWeight(item);
					itemWeight+=itemCarryingWeight;
				}

				Long characterAvailableWeight = (Long)getCharacterMaxCarryingWeight(character) - getCharacterCarryingWeight(character);

				// Skip checks below if encumbered and item has weight at all
				if (characterAvailableWeight < 0L && itemSingleWeight > 0L)
					throw new UserErrorMessage("You try to pick it up, but it's too heavy!");

				// If the item is weightless, we skip the following check
				stackTooHeavy = ((characterAvailableWeight < itemWeight) && (itemSingleWeight>0L));

				if (stackTooHeavy)
				{
					requestQuantity = Math.min(itemQuantity, (characterAvailableWeight / itemSingleWeight));
					if (requestQuantity<=0L)
						throw new UserErrorMessage("You try to pick it up, but it's too heavy! You can only carry "+GameUtils.formatNumber(characterAvailableWeight)+" grams more.");
				}
			}
		}
		
		if (handled==false)
			throw new UserErrorMessage("Unable to move this item. It is probably no longer there. Try hitting the refresh button at the top of this popup?");

		// With earlier checks, requestQuantity must be either null or greater than zero
		// If not null, then either the user request passed container checks or request was lowered meet container capacity
		if (requestQuantity != null && item.getProperty("quantity") != null && requestQuantity < itemQuantity)
		{
			final Key itemKey = item.getKey();
			final String itemKind = item.getKind();
			final Long oldQuantity = itemQuantity;
			final Long newQuantity = requestQuantity;

			try {
				item = (CachedEntity) new Transaction<CachedEntity>(ds) {

					@Override
					public CachedEntity doTransaction(CachedDatastoreService ds) throws AbortTransactionException {

						CachedEntity item = ds.refetch(itemKey);

						CachedEntity newItem = new CachedEntity(itemKind);

						CachedDatastoreService.copyFieldValues(item, newItem);
						
						item.setProperty("quantity", newQuantity);

						newItem.setProperty("quantity", oldQuantity - newQuantity);

						newItem.setProperty("createdDate", new Date());

						ds.put(newItem);

						return item;
					}
				}.run();
			}
			catch (AbortTransactionException e) {
				throw new RuntimeException(e);
			}
		}

		item.setProperty("containerKey", newContainer.getKey());
		item.setProperty("movedTimestamp", new Date());

		// This call moved here from doMoveItem()
		ds.put(item);

		
		if (GameUtils.equals(getCurrentCharacterKey(), newContainer.getKey()))
		{
			QuestService questService = command.getQuestService();
			questService.checkAcquiredItemForObjectiveCompletions(item);
		}
		
		if (stackTooLarge)
		{
			throw new UserErrorMessage("Container was only able to fit "+requestQuantity+" of the items.");
		}
		else if (stackTooHeavy)
		{
			if ("Item".equals(newContainer.getKind()))
			{
				throw new UserErrorMessage("Container was only able to fit "+requestQuantity+" of the items.");
			}
			else if ("Character".equals(newContainer.getKind()))
			{
				throw new UserErrorMessage("You could only pick up "+requestQuantity+" of the items.");
			}
		}
		
		return handled;
	}

	public void doMoveItem(OperationBase command, CachedDatastoreService ds, CachedEntity character, CachedEntity item, CachedEntity newContainer, Long quantity) throws UserErrorMessage
	{
		// Throws if unsuccessful.
		tryMoveItem(command, character, item, newContainer, quantity);

		// Moved this to tryMoveItem() for messaging purposes
		/* ds.put(item); */
	}

	public void doMoveItem(OperationBase command, CachedDatastoreService ds, CachedEntity character, CachedEntity item, CachedEntity newContainer) throws UserErrorMessage
	{
		doMoveItem(command, ds, character, item, newContainer, null);
	}

	public boolean doDrinkBeer(CachedDatastoreService ds, CachedEntity character) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		
		List<EmbeddedEntity> buffs = getBuffsFor(character);
		
		// Look at the existing buffs to see if we're already maxed out on drinking
		int drunkCount = 0;
		for(PropertyContainer buff:buffs)
			if ("Drunk".equals(buff.getProperty("name")))
				drunkCount++;
		
		if (drunkCount>=6)
			throw new UserErrorMessage("The bar tender thinks you've had enough to drink.");
		
		return awardBuffByName(ds, character, "Drunk");
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
				TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, character);
				if (tradeObject!=null && tradeObject.isCancelled()==false && tradeObject.isComplete()==false)
				{
					setTradeCancelled(ds, tradeObject, character);
				}
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
		
		sendNotification(ds, otherCharacter.getKey(), NotificationType.tradeStarted);
	}
	
	
	public void addTradeItem(CachedDatastoreService ds, TradeObject tradeObject, CachedEntity character, CachedEntity item) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		if (tradeObject==null || tradeObject.isCancelled())
			throw new UserErrorMessage("Trade has been cancelled.");
		
		if (((Key)item.getProperty("containerKey")).getId() != character.getKey().getId())
			throw new UserErrorMessage("You do not currently have that item in your posession and cannot trade it.");
		
		tradeObject.addObject(ds, character, item);
		
	}
	
	public void addTradeItems(CachedDatastoreService ds, TradeObject tradeObject, CachedEntity character, List<CachedEntity> items) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		for(int i = items.size()-1; i>=0; i--)
		{
			CachedEntity item = items.get(i);
			if (tradeObject==null || tradeObject.isCancelled())
				throw new UserErrorMessage("Trade has been cancelled.");
			
			if (((Key)item.getProperty("containerKey")).getId() != character.getKey().getId())
			{
				item.refetch(ds);
				items.remove(i);
			}
		}
		
		tradeObject.addObjects(ds, character, items);
	}
	
	public void removeTradeItem(CachedDatastoreService ds, TradeObject tradeObject, CachedEntity character, CachedEntity item) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		
		tradeObject.removeObject(ds, character, item);
	}
	
	public void setTradeDogecoin(CachedDatastoreService ds, TradeObject tradeObject, CachedEntity character, long amount) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		
		tradeObject.setDogecoins(ds, character, amount);
	}
	
	public TradeObject setTradeReady(CachedDatastoreService ds, TradeObject tradeObject, CachedEntity character, Integer version) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		
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

		CachedEntity character1 = null;
		CachedEntity character2 = null;
		if (GameUtils.equals(tradeObject.character2Key, character.getKey()))
		{
			character1 = getEntity(tradeObject.getOtherCharacter(character.getKey()));
			character2 = character;
			
			// Here is a special feature where we automatically make the other character ready if the traders are the same user
			if (GameUtils.equals(character1.getProperty("userKey"), character2.getProperty("userKey")))
			{
				tradeObject.flagReady(ds, character1, tradeObject.isReady(ds, character));
				character1.setProperty("mode", ODPDBAccess.CHARACTER_MODE_NORMAL);
				character1.setProperty("combatant", null);
			}
			
		}
		else if (GameUtils.equals(tradeObject.character1Key, character.getKey()))
		{
			character1 = character;
			character2 = getEntity(tradeObject.getOtherCharacter(character.getKey()));

			// Here is a special feature where we automatically make the other character ready if the traders are the same user
			if (GameUtils.equals(character1.getProperty("userKey"), character2.getProperty("userKey")))
			{
				tradeObject.flagReady(ds, character2, tradeObject.isReady(ds, character));
				character2.setProperty("mode", ODPDBAccess.CHARACTER_MODE_NORMAL);
				character2.setProperty("combatant", null);
			}
		}

		if (((Key)character1.getProperty("locationKey")).getId() != ((Key)character2.getProperty("locationKey")).getId())
		{
			setTradeCancelled(ds, tradeObject, character);
			throw new UserErrorMessage("You cannot trade with a character who is not in your location.");
		}
		

		if (tradeObject.isReady(ds, character1) && tradeObject.isReady(ds, character2))
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
			// We don't want to reset the other character's mode because we want that other character to try to load up the trade window again
			// so that the user can discover the result of the trade. If we reset the combatant to null, he won't be able to find the TradeObject
			// again.
			if (GameUtils.equals(character1.getKey(), character.getKey()))	// Set our combatant and mode to normal, but NOT the other guy, he will do this when he refreshes his trade page
			{
				character1.setProperty("mode", CHARACTER_MODE_NORMAL);
				character1.setProperty("combatant", null);
				character1.setProperty("combatType", null);
			}
			else
			{
				character2.setProperty("mode", CHARACTER_MODE_NORMAL);
				character2.setProperty("combatant", null);
				character2.setProperty("combatType", null);
			}
			
			// Do a quick check to see if an exploit is being done somehow making a characters gold negative. If so, throw.
			
			if ((Long)character1.getProperty("dogecoins")<0l || (Long)character2.getProperty("dogecoins")<0l)
				throw new IllegalStateException("Trade was resulting in a negative balance for one or more recipients: "+character1.getKey()+", "+character2.getKey());
			
	
			ds.put(character1);
			ds.put(character2);
			
			tradeObject.flagComplete(ds);
		}
		
		if (character1.getKey().getId() == character.getKey().getId())
			sendNotification(ds, character2.getKey(), NotificationType.tradeChanged);
		else
			sendNotification(ds, character1.getKey(), NotificationType.tradeChanged);

		return tradeObject;
	}
	
	public void setTradeCancelled(CachedDatastoreService ds, TradeObject tradeObject, CachedEntity character) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		
		CachedEntity character1 = getEntity(tradeObject.character1Key);
		CachedEntity character2 = getEntity(tradeObject.character2Key);
		
		character1.setProperty("mode", CHARACTER_MODE_NORMAL);
		character1.setProperty("combatant", null);
		character2.setProperty("mode", CHARACTER_MODE_NORMAL);
		character2.setProperty("combatant", null);
		
		ds.put(character1);
		ds.put(character2);
		
		tradeObject.flagCancelled(ds, character);
		
		sendNotification(ds, tradeObject.getOtherCharacter(character.getKey()), NotificationType.tradeCancelled);

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
		if ("AutoDefender".equals(character.getProperty("combatType")))
			return true;
		
		if (character.getProperty("status")!=null && character.getProperty("status").equals(CharacterStatus.Normal)==false)
			return false;
		
		if (characterLocation==null)
			characterLocation = getEntity((Key)character.getProperty("locationKey"));
		
		if (characterLocation.getProperty("defenceStructure")!=null && "TRUE".equals(characterLocation.getProperty("defenceStructuresAllowed"))==false)
			return true;
		
		if (characterLocation.getProperty("territoryKey")!=null && "DefenceStructureAttack".equals(character.getProperty("combatType"))==false)
			return true;
		
		return false;
	}
	
	
	

	private String getCharacterLastCombatActionKey(CachedEntity character)
	{
		if (character.getProperty("combatant")==null)
			return null;
		else
			return "PVP_LastCombatAction_"+character.getKey().getId()+"vs"+((Key)character.getProperty("combatant")).getId();
	}

	public void flagCharacterCombatAction(CachedDatastoreService ds, CachedEntity character) 
	{
		String key = getCharacterLastCombatActionKey(character);
		if (key==null) return;
		ds.getMC().put(key, System.currentTimeMillis());
	}

	public Long getLastCharacterCombatAction(CachedDatastoreService ds, CachedEntity character)
	{
		String key = getCharacterLastCombatActionKey(character);
		if (key==null) return null;
		return (Long)ds.getMC().get(key);
	}
	
	
	////////// GROUP METHODS //////////
	///////////////////////////////////

	/**
	 * This method sets the character's group properties to null. The group
	 * entity is not required.
	 * 
	 * @param ds
	 *            Datastore containing character
	 * @param character
	 *            Character that is leaving the group
	 */
	public final void doLeaveGroup(CachedDatastoreService ds,
			final CachedEntity character)
	{
		if (ds == null)
		{
			ds = getDB();
		}

		character.setProperty("groupKey", null);
		character.setProperty("groupStatus", null);
		character.setProperty("groupRank", null);

		ds.put(character);
	}

	public void discoverAllPropertiesFor(CachedDatastoreService ds, CachedEntity user, CachedEntity character)
	{
		if (ds==null)
			ds = getDB();
		
		if (user!=null)
		{
			List<CachedEntity> paths = getFilteredList("Path", "ownerKey", user.getKey());
			
			for(CachedEntity path:paths)
				newDiscovery(ds, character, path);

			CachedEntity group = getEntity((Key)character.getProperty("groupKey"));
			
			if (group!=null)
				discoverAllGroupPropertiesFor(ds, character);
			
		}
	}
	
	
	/**
	 * 
	 * @param ds
	 *            Datastore containing character
	 * @param character
	 *            Character to discover properties for
	 */
	public final void discoverAllGroupPropertiesFor(CachedDatastoreService ds,
			CachedEntity character)
	{
		if (ds==null)
			ds = getDB();
		
		if (isCharacterAGroupMember(character))
		{
			List<CachedEntity> paths = getFilteredList("Path", "ownerKey", character.getProperty("groupKey"));
			
			for(CachedEntity path:paths)
				newDiscovery(ds, character, path);
		}		
	}

	public boolean isCharacterAGroupMember(CachedEntity character)
	{
		if (character.getProperty("groupKey")==null) return false;
		
		String status = (String)character.getProperty("groupStatus");
		if (status==null) return false;
		if (status.equals("Member") || status.equals("Admin")) return true;
		return false;
	}

	/**
	 * Checks required to accept or deny some applicant.
	 * 
	 * @param applicant
	 *            Applicant being checked
	 * @param character
	 *            Character being checked
	 * @param groupKey
	 *            Group being checked
	 * @return true if all checks passed
	 * @throws UserErrorMessage
	 */
	public boolean applicationAcceptOrDenyChecks(final CachedEntity applicant,
			final CachedEntity character, final Key groupKey)
			throws UserErrorMessage
	{
		boolean bool = true;

		if (((Key) applicant.getProperty("groupKey")).getId() != groupKey
				.getId())
		{
			bool = false;
			throw new UserErrorMessage(
					"Applicant is no longer applying to the group you manage.");
		}

		if (!"Admin".equals(character.getProperty("groupStatus")))
		{
			bool = false;
			throw new UserErrorMessage(
					"You are not an admin of your group and cannot perform this action.");
		}

		if (!"Applied".equals(applicant.getProperty("groupStatus")))
		{
			bool = false;
			throw new UserErrorMessage("User is already a member of the group.");
		}

		return bool;
	}

	
	public void doGivePlayerHouseToGroup(CachedDatastoreService ds, CachedEntity charactersGroup, CachedEntity user, CachedEntity playerHouse) throws UserErrorMessage
	{
		if (charactersGroup==null)
			throw new UserErrorMessage("You are not part of a group and cannot do this.");
		
		if (((Key)playerHouse.getProperty("ownerKey")).getId() != user.getKey().getId())
			throw new UserErrorMessage("You do not own this house and therefore cannot assign it to your group.");
		
		if (ds==null)
			ds = getDB();
		
		// Get the path to the player house too...
		List<CachedEntity> paths = getPathsByLocation(playerHouse.getKey());
		
		if (paths.size()!=1 || "PlayerHouse".equals(paths.get(0).getProperty("type"))==false) 
			throw new UserErrorMessage("You can only do this for player houses.");
		
		// Now set all the path and the location's owner to the player's group
		for(CachedEntity p:paths)
		{
			p.setProperty("ownerKey", charactersGroup.getKey());
			ds.put(p);
		}
		
		playerHouse.setProperty("ownerKey", charactersGroup.getKey());
		playerHouse.setProperty("description", "This house is the property of "+charactersGroup.getProperty("name")+". Feel free to leave equipment and gold here, but bear in mind that everyone in the group has access to this house.");
		
		ds.put(playerHouse);
		
		
		// Now go through all group members and give them the house discovery...
		List<CachedEntity> members = getFilteredList("Character", "groupKey", charactersGroup.getKey());
		for(CachedEntity m:members)
			newDiscovery(ds, m, paths.get(0));
	}

	
	////////// END GROUP METHODS //////////
	///////////////////////////////////////
	
	/**
	 * 
	 * @param db
	 * @param character, the entity to leave the party
	 * Takes in an entity and sets their partyCode and partyLeader properties to null (safety checks are done in CommandLeaveParty.java)
	 */
	public void doLeaveParty(CachedDatastoreService ds, CachedEntity character) {
		if (ds == null)
		{
			ds = getDB();
		}

		character.setProperty("partyCode", null);
		character.setProperty("partyLeader", null);

		ds.put(character);
	}
	
	public void doCharacterDiscoverEntity(CachedDatastoreService db, CachedEntity character, CachedEntity entityToDiscover)
	{
		//TODO: Probably implement some sort of caching for this. Check the cache first for the discovery and add to the cache new discoveries.
		newDiscovery(db, character, entityToDiscover);
	}

	public String cleanCharacterName(String name)
	{
		name = name.trim();
		name = name.replaceAll("\\s{2,}"," ");
		return name;
	}
	
	/**
	 * Returns the CachedEntity object representing the party leader of @partyCode
	 * Will return null if the partyCode is empty or null (or if for some reason no one in the party is a leader).
	 * Can optionally pass a list of members instead of a party code to search for a party member.
	 * 
	 * @param ds
	 * @param partyCode code of the party to grab the leader from
	 * @param members list of cachedentitys in a party
	 * @return the party leader of the party.
	 */
	public CachedEntity getPartyLeader(CachedDatastoreService ds, String partyCode, List<CachedEntity> members) {
		if (partyCode == null || partyCode.trim().equals("")) {
			return null;
		}
		
		if (members == null) {
			members = getParty(ds, partyCode);
		}
		
		for (CachedEntity member : members) {
			String leader = (String) member.getProperty("partyLeader");
			if (leader != null && leader.equals("TRUE")) {
				return member;
			}
		}
		return null;
	}
	
	
	List<CachedEntity> cachedParty = null;
	/**
	 * Don't do this unless you really need to.
	 * @param cachedParty
	 */
	public void setPartyCache(List<CachedEntity> cachedParty)
	{
		this.cachedParty = cachedParty;
	}
	/**
	 * Returns all party members belonging to the party that the selfCharacter belongs to.
	 * 
	 * If he doesn't belong to a party, we return null.
	 * 
	 * @param ds
	 * @param selfCharacter
	 * @return
	 */
	public List<CachedEntity> getParty(CachedDatastoreService ds, CachedEntity selfCharacter)
	{
		
		String partyCode = (String)selfCharacter.getProperty("partyCode");
		if (partyCode==null || partyCode.equals(""))
			return null;
		
		if (ds==null)
			ds = getDB();
		
		if (cachedParty!=null)
			return cachedParty;
		
		cachedParty = getFilteredList("Character", "partyCode", partyCode);
		
		// Replace the newly fetched self character with the entity we already have
		for(int i = 0; i<cachedParty.size(); i++)
			if (GameUtils.equals(selfCharacter.getKey(), cachedParty.get(i).getKey()))
				cachedParty.set(i, selfCharacter);
	
		// Clear out any dead characters from the party.
		for(int i = cachedParty.size()-1; i >= 0; i--)
			if(GameUtils.equals(selfCharacter.getKey(), cachedParty.get(i).getKey())==false &&
					CommonChecks.checkCharacterIsDead(cachedParty.get(i)))
			{
				cachedParty.get(i).setProperty("partyCode", null);
				ds.put(cachedParty.get(i));
				cachedParty.remove(i);
			}
		
		if (cachedParty.size() == 1)
		{
			selfCharacter.setProperty("partyCode", null);
			ds.put(selfCharacter);
			return null;
		}

		return cachedParty;
	}

	protected List<CachedEntity> getParty(CachedDatastoreService ds, String partyCode)
	{
		if (partyCode==null || partyCode.equals(""))
			return null;
		
		if (ds==null)
			ds = getDB();
		
		List<CachedEntity> result = getFilteredList("Character", "partyCode", partyCode);
	
		// Clear out any dead characters from the party.
		for(int i = result.size()-1; i >= 0; i--)
			if(GameUtils.equals(getCurrentCharacterKey(), result.get(i).getKey())==false &&
					CommonChecks.checkCharacterIsDead(result.get(i)))
			{
				result.get(i).setProperty("partyCode", null);
				ds.put(result.get(i));
				result.remove(i);
			}
				
		if (result.size()==1)
		{
			result.get(0).setProperty("partyCode", null);
			return null;
		}
		
		return result;
	}

	public void doPartyJoinsAllowed(CachedDatastoreService ds, CachedEntity character, boolean joinsAllowed) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
	
		List<CachedEntity> party = getParty(ds, character);
	
		CachedEntity leader = null;
		if (party==null)
		{
			leader = character;
		}
		else
		{
			if ("TRUE".equals(character.getProperty("partyLeader")))
				leader = character;
			else
			{
				for(CachedEntity e:party)
					if ("TRUE".equals(e.getProperty("partyLeader")))
						leader = e;
			}
		}
		
		if (leader==null)
			throw new UserErrorMessage("You are in a party but you are not the leader, therefore you do not have permission to decide whether or not joins are allowed.");
		
		if (joinsAllowed)
			leader.setProperty("partyJoinsAllowed", joinsAllowed ? "TRUE" : "FALSE");
		
		ds.put(leader);
		return;
	}

	
	/**
	 * Method stub placeholder for ODP.
	 * 
	 *  This method will accept a curve formula and output a number.
	 * 
	 * @param curve
	 * @return
	 */
	public Object solveCurve(String curve)
	{
		return 0L;
	}
	
	/**
	 * Method stub placeholder for ODP. 
	 * 
	 *  This method will accept a curve formula and output a number.
	 *  This variant specifically will try to parse the resulting number 
	 *  as a long so you have to make sure the curve formula will result in a whole number.
	 *  
	 * @param curve
	 * @return
	 */
	public Long solveCurve_Long(String curve)
	{
		return 0L;
	}
	
	
	
	/**
	 * Method stub placeholder for ODP. 
	 *
	 *  This method will accept a curve formula and output a number.
	 *  This variant specifically will try to parse the resulting number 
	 *  as a double.
	 * 
	 * @param curve
	 * @return
	 */
	public Double solveCurve_Double(String curve)
	{
		return 0D;
	}
	
	/**
	 * Method stub placeholder for ODP.
	 * 
	 * This method will generate a monster from an NPCDef. The only thing this method
	 * doesn't do is set the location for the monster.
	 * 
	 * @param npcDefinition
	 * @return
	 */
	public CachedEntity doCreateMonster(CachedEntity npcDefinition, Key locationKey)
	{
		return null;
	}

	public void flagActiveCharacter(CachedEntity character)
	{
		if (character!=null)
		{
			
//			Date currentDate = (Date)character.getProperty("locationEntryDatetime");
//			if (currentDate==null || currentDate.getTime()+(1000*300)<System.currentTimeMillis())
//			{
//				character.refetch(getDB());
//				character.setProperty("locationEntryDatetime", new Date());
//				ds.put(character);
//			}
		}
	}
	
	
	
	//////////////////////////////////////
	// Long Operation stuff
	
	
	
	
	public void flagNotALooter(HttpServletRequest request)
	{
		request.setAttribute("notLooter", true);
	}
	
	@Deprecated
	public void doCharacterAttemptTakePath(CachedDatastoreService ds, HttpServletRequest request, CachedEntity path, CachedEntity character, boolean attack) throws UserErrorMessage, AbortedActionException
	{
		if (ds==null)
			ds = getDB();
		
		
		if (GameUtils.isCharacterInParty(character) && GameUtils.isCharacterPartyLeader(character)==false)
		{
			throw new UserErrorMessage("You cannot move your party because you are not the leader.");
		}

		
		
		Long travelTime = (Long)path.getProperty("travelTime");
		if (travelTime==null)
			GameUtils.timePasses(6);	// Default travel time is 6 seconds
		else
			GameUtils.timePasses(travelTime.intValue());
		
		

		String forceOneWay = (String)path.getProperty("forceOneWay");
		if ("FromLocation1Only".equals(forceOneWay) && GameUtils.equals(character.getProperty("locationKey"), path.getProperty("location2Key")))
			throw new UserErrorMessage("You cannot take this path.");
		if ("FromLocation2Only".equals(forceOneWay) && GameUtils.equals(character.getProperty("locationKey"), path.getProperty("location1Key")))
			throw new UserErrorMessage("You cannot take this path.");		
		
		
		CachedEntity startLocation = getEntity((Key)character.getProperty("locationKey"));
		if (randomMonsterEncounter(ds, character, startLocation, 1, 0.5d))
		{
			flagNotALooter(request);
			throw new AbortedActionException(AbortedActionException.Type.CombataWhileMoving);
		}
		
		
		doCharacterTakePath(ds, character, path, attack);

		flagNotALooter(request);
		
	}

	public void setClientDescription(CachedDatastoreService ds, Key characterKey, String message)
	{
		if (ds==null)
			ds = getDB();
		
		ds.getMC().put("ClientDescription-"+characterKey, message);
	}
	
	
	public void addToClientDescription(CachedDatastoreService ds, Key characterKey, String message)
	{
		String currentMessage = getClientDescription(ds, characterKey);
		if (currentMessage==null)
			currentMessage=message;
		else
			currentMessage+=message;
		
		setClientDescription(ds, characterKey, currentMessage);
	}
	
	public String getClientDescription(CachedDatastoreService ds, Key characterKey)
	{
		if (ds==null)
			ds = getDB();
		
		return (String)ds.getMC().get("ClientDescription-"+characterKey);
	}
	
	public String getClientDescriptionAndClear(CachedDatastoreService ds, Key characterKey)
	{
		if (ds==null)
			ds = getDB();
		
		String clientMessage = (String)ds.getMC().get("ClientDescription-"+characterKey);
		if (clientMessage!=null)
			setClientDescription(ds, characterKey, null);
		
		return clientMessage;
	}

	
	/**
	 * This is a placeholder since the actual implementation is not in the ODP.
	 * 
	 * This method takes the given definition object (like an ItemDef or NPCDef) and creates
	 * a new CachedEntity with all of the fields from the definition and with the curve
	 * formulas all solved.
	 * 
	 * This version of this method allocates a key for the generated entity.
	 * 
	 * @param definition
	 * @param generatedEntityKind The "entity type" or "kind" of the returned CachedEntity
	 * @return
	 */
	public CachedEntity generateNewObject(CachedEntity definition, String generatedEntityKind)
	{
		return null;
	}

	/**
	 * This is a placeholder since the actual implementation is not in the ODP.
	 * 
	 * This method takes the given definition object (like an ItemDef or NPCDef) and creates
	 * a new CachedEntity with all of the fields from the definition and with the curve
	 * formulas all solved.
	 * 
	 * @param definition
	 * @param generatedEntityKind The "entity type" or "kind" of the returned CachedEntity
	 * @param allocateKey If set to false, the method wont give the generated Item entity a key (useful if you don't plan on saving it to the DB in most cases)
	 * @return
	 */
	public CachedEntity generateNewObject(Random rnd, CachedEntity definition, String generatedEntityKind, boolean allocateKey)
	{
		return null;
	}

	public EmbeddedEntity generateNewEmbeddedObject(CachedEntity definition, String destinationKind)
	{
		Random rnd = new Random();
		long id = rnd.nextLong();
		if (id<0) id*=-1;
		Key generatedEntityKey = KeyFactory.createKey(destinationKind, id);
		return generateNewEmbeddedObject(rnd, definition, generatedEntityKey);
	}
	
	public EmbeddedEntity generateNewEmbeddedObject(Random rnd, CachedEntity definition, Key generatedEntityKey)
	{
		return null;
	}
	
	/**
	 * This is a placeholder since the actual implementation is not in the ODP.
	 * 
	 * This executes the full randomMonsterEncounter method with the tries set to 1 and multiplier set to 1.
	 * 
	 * @param ds
	 * @param character
	 * @param location
	 * @param retries This is the number of times to retry when trying to get a random encounter. Use this when you want there to be a higher chance of getting an encounter.
	 * @return
	 */
	public boolean randomMonsterEncounter(CachedDatastoreService ds, CachedEntity character, CachedEntity location)
	{
		return randomMonsterEncounter(ds, character, location, 1, 1d);
	}
	
	
	/**
	 * This is a placeholder since the actual implementation is not in the ODP.
	 * 
	 * This method rolls a random chance to find a monster in the given location. It will load
	 * all the monster spawners and attempt to spawn each one with the spawner's random chance.
	 * 
	 * @param ds
	 * @param character The character who will enter combat with this monster if successful
	 * @param location The location that contains the monster spawners to spawn
	 * @param tries The number of times this method should attempt to spawn a monster. This is an easy way of upping the spawn chances. This basically just efficiently retries the spawn "tries" number of times if no spawn is successful.
	 * @param individualMonsterChanceMultilier This number is multiplied against the individual monster spawn chance. The most common use for this multiplier is weather effect multipliers.
	 * @return
	 */
	public boolean randomMonsterEncounter(CachedDatastoreService ds, CachedEntity character, CachedEntity location, int tries, Double individualMonsterChanceMultilier) 
	{
		return false;
	}

	
	public boolean randomMonsterEncounter(CachedDatastoreService ds, CachedEntity character, CachedEntity location, int tries, Double individualMonsterChanceMultilier, Boolean noCombatSiteOverride)
	{
		// TODO Auto-generated method stub
		return false;
	}
	

//	/**
//	 * This is a placeholder since the actual implementation is not in the ODP.
//	 * 
//	 * @param db
//	 * @param character
//	 * @param path
//	 * @throws UserErrorMessage
//	 */
//	public CachedEntity doCharacterTakePath(CachedDatastoreService db, CachedEntity character, CachedEntity path) throws UserErrorMessage
//	{
//		return doCharacterTakePath(db, character, path, false);
//	}
//	
//	/**
//	 * This is a placeholder since the actual implementation is not in the ODP.
//	 * 
//	 * @param db
//	 * @param character
//	 * @param path
//	 * @param allowAttack
//	 * @throws UserErrorMessage
//	 */
//	public CachedEntity doCharacterTakePath(CachedDatastoreService db, CachedEntity character, CachedEntity path, boolean allowAttack) throws UserErrorMessage
//	{
//		return null;
//	}
	

	/**
	 * This is a placeholder since the actual implementation is not in the ODP.
	 * 
	 * @param ds
	 * @param character
	 * @param location
	 * @param tries
	 * @param individualCollectionDiscoveryChanceMultiplier
	 * @return
	 */
	public boolean randomCollectionDiscovery(CachedDatastoreService ds, CachedEntity character, CachedEntity location, int tries, Double individualCollectionDiscoveryChanceMultiplier) 
	{
		return false;
	}	
	
	
	
	public void setPartiedField(List<CachedEntity> party, CachedEntity character, String fieldName, Object fieldValue)
	{
		character.setProperty(fieldName, fieldValue);
		if (party!=null)
			for(CachedEntity member:party)
				if (GameUtils.equals(member.getKey(), character.getKey())==false)
				{
					member.setProperty(fieldName, fieldValue);
				}
	}
	
	//TODO: Figure out why the _SkipSelf variant is identical to the non _SkipSelf version... probably a bug, but no one is complaining
	public void putPartyMembersToDB_SkipSelf(CachedDatastoreService ds, List<CachedEntity> party, CachedEntity character)
	{
		if (party!=null)
			for(CachedEntity member:party)
				if (GameUtils.equals(member.getKey(), character.getKey())==false)
				{
					ds.put(member);
				}
	}
	
	public void putPartyMembersToDB(CachedDatastoreService ds, List<CachedEntity> party, CachedEntity character)
	{
		ds.put(character);
		if (party!=null)
			for(CachedEntity member:party)
				if (GameUtils.equals(member.getKey(), character.getKey())==false)
				{
					ds.put(member);
				}
	}

	/**
	 * This method PROBABLY COULD BE migrated over to the ODP.
	 * 
	 * 
	 * @param currentCharacter
	 * @param destination
	 * @return
	 */
	public CachedEntity getBlockadeFor(CachedEntity currentCharacter, CachedEntity destination)
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	public void sortAndShuffleCharactersByAttackOrder(List<CachedEntity> characters)
	{
		// An odd bug where null entries are getting into the character list
		for(int i = characters.size()-1; i>=0; i--)
			if (characters.get(i)==null) characters.remove(i);
		
		Collections.shuffle(characters);	// We first shuffle so that characters with the same status will be randomized
		Collections.sort(characters, new Comparator<CachedEntity>(){

			@Override
			public int compare(CachedEntity o1, CachedEntity o2) {
				String o1Status = (String)o1.getProperty("status");
				String o2Status = (String)o2.getProperty("status");
				if (o1Status==null) o1Status = "Normal";
				if (o2Status==null) o2Status = "Normal";
				return o1Status.compareTo(o2Status);
			}
		});
		
	}
	
	

	public CachedEntity getCombatantFor(CachedEntity character, CachedEntity destination) 
	{
		List<CachedEntity> monsters = getFilteredList("Character", "type", "NPC", "locationKey", destination.getKey());
		
		if (monsters==null || monsters.isEmpty())
			return null;
		
		sortAndShuffleCharactersByAttackOrder(monsters);
		
		for(CachedEntity monster:monsters)
		{
			if (monster.getProperty("combatant")==null && GameUtils.isPlayerIncapacitated(monster)==false)
			{
				character.setProperty("combatant", monster.getKey());
				character.setProperty("combatType", "DefenceStructureAttack");
				character.setProperty("mode", CHARACTER_MODE_COMBAT);
				flagCharacterCombatAction(getDB(), character);
				
				monster.setProperty("combatant", character.getKey());
				monster.setProperty("mode", CHARACTER_MODE_COMBAT);
				
				
				
				return monster;
			}
		}
		return null;
	}
	
	public List<CachedEntity> getInstanceSpawnersForLocation(final Key originKey, Key destinationKey)
	{
		List<CachedEntity> spawners = query.getFilteredORList(null, "MonsterSpawner", "locationKey", originKey, "locationKey", destinationKey);
		List<CachedEntity> foundSpawners = new ArrayList<CachedEntity>();
		for(CachedEntity spawn:spawners)
		{
			// If it's not an instance spawner, or NPC is null, do not add spawner.
			if(GameUtils.booleanEquals(spawn.getProperty("instanceModeEnabled"), false) ||
					spawn.getProperty("npcDefKey") == null) continue;
			
			Double availableSpawns = (Double)spawn.getProperty("availableMonsterCount");
			if(availableSpawns == null || availableSpawns.intValue() < 1) continue;
			
			foundSpawners.add(spawn);
		}
		
		Collections.shuffle(foundSpawners);	// We first shuffle so that characters with the same status will be randomized
		Collections.sort(foundSpawners, new Comparator<CachedEntity>(){

			@Override
			public int compare(CachedEntity o1, CachedEntity o2) {
				if(GameUtils.equals(o1.getProperty("locationKey"), o2.getProperty("locationKey")))
				{
					String o1Status = (String)o1.getProperty("instanceMonsterStatus");
					String o2Status = (String)o2.getProperty("instanceMonsterStatus");
					if (o1Status==null) o1Status = "Normal";
					if (o2Status==null) o2Status = "Normal";
					return o1Status.compareTo(o2Status);
				}
				else if(GameUtils.equals(originKey, o1.getProperty("locationKey")))
					return -1;
				else
					return 1;
			}
		});
		
		return foundSpawners;
	}
	
	/**
	 * Method stub. This is actually implemented in the primary repo because the secret key is there.
	 * 
	 * @param response
	 * @param remoteip
	 * @return
	 */
	public boolean validateCaptcha(String response, String remoteip)
	{
		return false;
	}
	
	public List<CachedEntity> getScriptsOfType(List<Key> scripts, ScriptType... types)
	{
		if(scripts == null || scripts.isEmpty()) return new ArrayList<CachedEntity>();
		List<CachedEntity> scriptEntities = getEntities(scripts);
		HashSet<ScriptType> validTypes = new HashSet<ScriptType>(Arrays.asList(types));
		Iterator<CachedEntity> iter = scriptEntities.iterator();
		while(iter.hasNext())
		{
			CachedEntity currentScript = iter.next();
			String scriptType = (String)currentScript.getProperty("type");
			if(scriptType == null || scriptType == "")
				iter.remove();
			else
			{
				ScriptType currentType = ScriptType.valueOf(scriptType);
				if(!validTypes.contains(currentType))
					iter.remove();
			}
		}
		
		return scriptEntities;
	}


	private class AttackResult
	{
		public int damage = 0;
		public boolean hit = false;
		public int damageBlocked = 0;
		public String status = "";
	}
	public String doCharacterAttemptAttack(ODPAuthenticator auth, CachedEntity user, CachedEntity sourceCharacter, CachedEntity weapon, CachedEntity targetCharacter)
    {
        if (sourceCharacter==null)
            throw new IllegalArgumentException("Source character cannot be null.");
        if (targetCharacter==null)
            throw new IllegalArgumentException("Target character cannot be null.");


        
        CachedDatastoreService db = getDB();

        
        // Here we're flagging that a combat action took place so that the cron job in charge of ensuring combat keeps moving along
        // doesn't automatically attack for us
        if ("PC".equals(sourceCharacter.getProperty("type")) && "PC".equals(targetCharacter.getProperty("type")))
            flagCharacterCombatAction(db, sourceCharacter);

        
        
        // Regardless of whether or not the attack is successful, give a stat increase against NPCs only
        if (CommonChecks.checkCharacterIsPlayer(sourceCharacter) && CommonChecks.checkCharacterIsPlayer(targetCharacter) == false)
        {
            // Get the stat increase multiplier...
            double multiplier = 1d;
            if (targetCharacter.getProperty("experienceMultiplier")!=null)
                multiplier = (Double)targetCharacter.getProperty("experienceMultiplier");
            if (multiplier>5) multiplier = 5d;
            if (multiplier<0) multiplier = 0d;   
            if (CommonChecks.checkIsHardcore(sourceCharacter)) multiplier *= 4d;
            
            Double[] maxStats = getMaxCharacterStats(sourceCharacter.getKey());
            
            doCharacterIncreaseStat(db, sourceCharacter, "strength", maxStats[0], 4d*multiplier);
            doCharacterIncreaseStat(db, sourceCharacter, "dexterity", maxStats[1], 2d*multiplier);
            doCharacterIncreaseStat(db, sourceCharacter, "intelligence", maxStats[2], 0.5d*multiplier);
        }
        
        Double charDex = getCharacterDexterity(sourceCharacter);
        Double monsterDex = getCharacterDexterity(targetCharacter);
        Random rnd = new Random();
        if (rnd.nextDouble()*charDex>=rnd.nextDouble()*monsterDex)
        {
        	// Determine whether the weapon zombifies the target.
        	boolean zombifyWeapon = weapon != null && GameUtils.booleanEquals(weapon.getProperty("zombifying"), true);
            AttackResult attackResult = attackWithWeapon(db, sourceCharacter, weapon, targetCharacter);
            String status = attackResult.status;
            int damage = attackResult.damage;

            // If we are dual wielding weapons, use the random crit chance to determine if we get a free second hit
            // with our second weapon...
            
        	// Get both weapons on this attacker (in case of dual wielding)
        	CachedEntity otherWeapon = null;
        	Key leftHand = (Key)sourceCharacter.getProperty("equipmentLeftHand");
        	Key rightHand = (Key)sourceCharacter.getProperty("equipmentRightHand");
        	if (weapon!=null)
        	{
        		// Lets increase weapon experience now already
//        		if ("PC".equals(sourceCharacter.getProperty("type")))
//        		{
//        			ODPKnowledgeService knowledgeService = getKnowledgeService(sourceCharacter.getKey());
//        			knowledgeService.increaseKnowledgeFor(weapon, 1, 100);
//        		}
        		
        		// If the weapon we're attacking with is in the right hand, the "otherWeapon" should be whatever is in the left hand
	        	if (GameUtils.equals(weapon.getKey(), rightHand)==true && GameUtils.equals(weapon.getKey(), leftHand)==false)
	        	{
	        		otherWeapon = getEntity(leftHand);
	        		if (otherWeapon!=null && "Weapon".equals(otherWeapon.getProperty("itemType"))==false)
	        			otherWeapon = null;
	        	}
        		// And the opposite
	        	if (GameUtils.equals(weapon.getKey(), leftHand)==true && GameUtils.equals(weapon.getKey(), rightHand)==false)
	        	{
	        		otherWeapon = getEntity(rightHand);
	        		if (otherWeapon!=null && "Weapon".equals(otherWeapon.getProperty("itemType"))==false)
	        			otherWeapon = null;
	        	}
	        	
	        	// Now roll to see if we score a double attack with dual wielding...
	    		if (otherWeapon!=null)
	    		{
	    			Double doubleHitChance = 0d;
	    		    if (weapon.getProperty("weaponDamageCriticalChance")!=null)
	    		    	doubleHitChance = ((Long)weapon.getProperty("weaponDamageCriticalChance")).doubleValue();
	    		    
	        		// Increase the critChance by the character's intelligence such that every point of int 
	        		// increases the chance by 2.5% (unscaled) starting from 4
	        		Double intelligence = getCharacterIntelligence(sourceCharacter);
	        		if (intelligence!=null)
	        		{
	        		    double adj = intelligence-4d;
	        		    adj*=2.5d;
	        		    doubleHitChance+=adj;
	        		}
	        		
	        		boolean doubleHit = false;
	        		if (doubleHitChance!=null && GameUtils.roll(doubleHitChance))
	        		    doubleHit = true;
	        		
	        		if (doubleHit)
	        		{
	        			// Since the other weapon dealt the killing blow, that's the only one that matters.
	        			zombifyWeapon = GameUtils.booleanEquals(otherWeapon.getProperty("zombifying"), true);
	        			status +="<strong>"+sourceCharacter.getProperty("name")+" also attacks with "+otherWeapon.getProperty("name")+"..</strong><br>";
	        			AttackResult attackResult2 = attackWithWeapon(db, sourceCharacter, otherWeapon, targetCharacter);
	        			status += attackResult2.status;
	        			damage += attackResult2.damage;
	        		}
	        		
	    		}
        	}
            
    		
        	// Track damage done.
            if(damage > 0)
            {
            	Map<String, String> damageMap = getValue_StringStringMap(targetCharacter, "combatStatsDamageMap");
            	if(damageMap == null) damageMap = new HashMap<String, String>();
            	
            	String charKey = sourceCharacter.getKey().toString();
            	String damageString = damageMap.get(charKey);
            	Integer priorDamage = 0;
            	try
            	{
        			priorDamage = Integer.parseInt(damageString);
        			priorDamage += damage;
            	}
            	catch(Exception ex) { priorDamage = damage; }
            	damageMap.put(charKey, priorDamage.toString());
            	setValue_StringStringMap(targetCharacter, "combatStatsDamageMap", damageMap);
            	
            	queueMainPageUpdateForCharacter(targetCharacter.getKey(), "updateInBannerCharacterWidget");
            }
            
            
            Double targetHitpoints = (Double)targetCharacter.getProperty("hitpoints");
            targetHitpoints-=damage;
            
            targetCharacter.setProperty("hitpoints", targetHitpoints);
            
            
            
            
            
            
            // Check if the target is killed
            if (targetHitpoints<=0)
            {
                // Now we heal the defender if he was infact a defender
                pool.addToQueue(sourceCharacter.getProperty("locationKey"), targetCharacter.getProperty("locationKey"));
                if (CommonChecks.checkCharacterDefenderToAutoHeal(getEntity((Key)sourceCharacter.getProperty("locationKey")), sourceCharacter, 
                		getEntity((Key)targetCharacter.getProperty("locationKey")), targetCharacter))
                {
					Double hitpoints = (Double)sourceCharacter.getProperty("hitpoints");
					Double maxHitpoints = (Double)sourceCharacter.getProperty("maxHitpoints");
					Double guardStartHP = (Double)sourceCharacter.getProperty("guardStartHP");
					if (guardStartHP!=null && hitpoints<guardStartHP)
					{
						sourceCharacter.setProperty("hitpoints", guardStartHP);
						sourceCharacter.setProperty("guardStartHP", null);
					}
					else if (hitpoints<maxHitpoints)
						sourceCharacter.setProperty("hitpoints", maxHitpoints);
                }
                
                // If the NPC who did the killing is a zombie (or used a zombify weapon), the target character will be turned into a zombie instead of killing them
                if ((zombifyWeapon || "Zombie".equals(sourceCharacter.getProperty("status"))) && 
                		"NPC".equals(sourceCharacter.getProperty("type"))) 
                {
                    doCharacterZombify(auth, db, sourceCharacter, targetCharacter);
                    //status+=" The battle is over, you " + verb + "! But the target character has been turned into a zombie!";
                    status+=" The battle is over, you lost! But you have also been turned into a zombie!";
                    if (user!=null && user.isUnsaved())
                    	db.put(user);
                    db.put(sourceCharacter, targetCharacter);
                }
                else
                {
                	// Kill character. Loot gets ignored if character is not logged in.
                	String loot = doCharacterKilled(user, targetCharacter, sourceCharacter);
                	if(isLoggedIn(getRequest()))
                	{
	                	// We check if the source character is the same as the current character. If yes, battle is won, if not, it's lost.
	                	String verb = GameUtils.equals(getCurrentCharacterKey(), sourceCharacter.getKey()) ? "won" : "lost";
	                    status += " The battle is over, you " + verb + "!" ;
	                    if (loot!=null)
	                    {
	                    	status=status+"<br><br>"+loot;
	                    }
                	}
                	else
                		status += " The battle is over.";
                }
                
            }
            
            if (targetCharacter.isUnsaved())
            	db.put(targetCharacter);
            if (sourceCharacter.isUnsaved())
            	db.put(sourceCharacter);
            if (user!=null && user.isUnsaved())
            	db.put(user);
            
            return status; 
        }
        else
        {
            // The attack missed, pass back null.
            return null;
        }
    }




	/**
	 * Placeholder
	 * @param characterKey
	 * @return
	 */
	public Double[] getMaxCharacterStats(Key characterKey)
	{
		// TODO Auto-generated method stub
		return null;
	}

	private AttackResult attackWithWeapon(CachedDatastoreService db, CachedEntity sourceCharacter, CachedEntity weapon, CachedEntity targetCharacter)
	{
		AttackResult attackResult = new AttackResult();
		
		// First get the weapon we're using...
		Object result = 0d;
		if (weapon!=null && weapon.getProperty("weaponDamage")!=null && weapon.getProperty("weaponDamage").toString().trim().equals("")==false)
		{
		    result = solveProperty("Attack with Weapon", weapon, "weaponDamage");
		    if (result==null)
		        throw new RuntimeException("'Attack with Weapon' failed to solve.");
		}
		
		if (result instanceof Double)
		    attackResult.damage = ((Double)result).intValue();
		else if (result instanceof Long)
			attackResult.damage = ((Long)result).intValue();
		else if (result instanceof Integer)
			attackResult.damage = (int)result;
		
		// Get all character stats first. Destroying equipment will affect all modifiers,
		// so will need to be recalculated AFTER the hit.
		Double str = getCharacterStrength(sourceCharacter);
		Double intelligence = getCharacterIntelligence(sourceCharacter);
		
		// Lets just go ahead and determine the strength bonus of the attack if it succeeds 
		int strengthDamageBonus = 0;
		if (str==null) str = 3d;
		str-=3d;
		// Special case, if the character is using a 2handed weapon, then double the effect of strength...
		if (weapon!=null && "2Hands".equals(weapon.getProperty("equipSlot")))
		    str*=1.5;
		str*=2d;
		strengthDamageBonus = new Double(GameUtils.rnd.nextDouble()*str).intValue();
		if (strengthDamageBonus<0) strengthDamageBonus=0;

		
		
		String weaponName = "bare hands";
		if (weapon!=null)
		{
		    weaponName = (String)weapon.getProperty("name");
		    
		    // At this point, reduce the durability of the weapon by 1
		    Long durability = (Long)weapon.getProperty("durability");
		    if (durability!=null)
		    {
		        if (durability<=0)
		        {
		            doDestroyEquipment(db, sourceCharacter, weapon);
		            // Force recalc of stats.
		            statsCache.remove(sourceCharacter.getKey());
		            if (weapon.getProperty("naturalEquipment")!=null && weapon.getProperty("naturalEquipment").equals("TRUE"))
		            	attackResult.status = "<div class='equipment-destroyed-notice'>"+sourceCharacter.getProperty("name")+"'s "+weapon.getProperty("name")+" is no longer usable. </div>";
		            else
		            	attackResult.status = "<div class='equipment-destroyed-notice'>"+sourceCharacter.getProperty("name")+"'s "+weapon.getProperty("name")+" was so badly damaged it has been destroyed. </div>";
		        }
		        else
		        {
		            weapon.setProperty("durability", durability-1);

					// Also reduce the charges for any buff that the weapon might have that has charges on it
		            reduceBuffCharges(weapon);
		            
		            db.put(weapon);
		        }
		    }
		}
		
		Double critChance = 0d;
		if (weapon!=null)
		{
		    if (weapon.getProperty("weaponDamageCriticalChance")!=null)
		        critChance = ((Long)weapon.getProperty("weaponDamageCriticalChance")).doubleValue();
		    
		}
		
		// Increase the critChance by the character's intelligence such that every point of int 
		// increases the chance by 2.5% (unscaled) starting from 4
		if (intelligence!=null)
		{
		    double adj = intelligence-4d;
		    adj*=2.5d;
		    critChance+=adj;
		}
		
		boolean criticalHit = false;
		if (critChance!=null && GameUtils.roll(critChance))
		    criticalHit = true;
		
		if (criticalHit)
		{
		    // Critical hit!
		    Double critMultiplier = 2d;
		    if (weapon!=null && (weapon.getProperty("weaponDamageCriticalMultiplier") instanceof Double))
		    {
		        critMultiplier = (Double)weapon.getProperty("weaponDamageCriticalMultiplier");
		    }
		    int oldDamage = attackResult.damage+strengthDamageBonus;
		    attackResult.damage*=critMultiplier;
		    attackResult.damage += strengthDamageBonus;

			// Now include any physical damage bonuses
			int physicalDamageBonus = -attackResult.damage;
			attackResult.damage = getLongBuffableValue(sourceCharacter, StaticBuffables.physicalDamage, (long)attackResult.damage).intValue();
			physicalDamageBonus += attackResult.damage;

			String physicalDamageBonusLine = "";
			if (physicalDamageBonus>0)
				physicalDamageBonusLine = ", "+physicalDamageBonus+" was from physical damage bonus effects";

		    attackResult.status += "It's a critical hit! "+oldDamage+" damage ("+strengthDamageBonus+" was from strength"+physicalDamageBonusLine+") was done to the "+targetCharacter.getProperty("name")+" with "+sourceCharacter.getProperty("name")+"'s "+weaponName+". But because of the critical hit, an additional "+(attackResult.damage-oldDamage)+" damage was done for a total of "+attackResult.damage+" damage."; 
		}
		else
		{
		    // Regular hit
			attackResult.damage+=strengthDamageBonus;
			
			// Now include any physical damage bonuses
			int physicalDamageBonus = -attackResult.damage;
			attackResult.damage = getLongBuffableValue(sourceCharacter, StaticBuffables.physicalDamage, (long)attackResult.damage).intValue();
			physicalDamageBonus += attackResult.damage;
			String physicalDamageBonusLine = "";
			if (physicalDamageBonus>0)
				physicalDamageBonusLine = ", "+physicalDamageBonus+" was from physical damage bonus effects";
			
			attackResult.status += "The attack hit! "+attackResult.damage+" damage ("+strengthDamageBonus+" was from strength"+physicalDamageBonusLine+") was done to the "+targetCharacter.getProperty("name")+" with "+sourceCharacter.getProperty("name")+"'s "+weaponName+".";
		}
		
		//Run onAttackHit scripts
		
		ScriptService service = ScriptService.getScriptService(this);
		
		List<CachedEntity> scripts = service.getScriptsOfType(weapon, ScriptType.onAttackHit);
		
		for(CachedEntity script : scripts) {
			CombatEvent event = new CombatEvent(this, sourceCharacter, weapon, targetCharacter);
			
			service.executeScript(event, script, weapon);
			if(!event.haltExecution)
				service.cleanupEvent(event);
		}
		
		
		
		// Try blocking the damage if there is any damage...
		if (attackResult.damage>0)
		{
		    Map<String, Object> blockResult = doBlockAttack(sourceCharacter, weapon, attackResult.damage, targetCharacter);
		    
		    
		    
		    if (blockResult.get("blocked")!=null && blockResult.get("blocked").equals(true))
		    {
		        ArrayList<CachedEntity> blockingArmor = (ArrayList<CachedEntity>)blockResult.get("blockingArmor");
		        if (blockingArmor!=null && blockingArmor.size()>0)
		        {
		            String armorNames = "";
		            for(int i = 0; i<blockingArmor.size();i++)
		            {
		                if (i>0) 
						{
						    if (blockingArmor.size() == 2)
						    	armorNames += " and ";
						    else 
						    	armorNames+=", ";
						    
						    // Only need the "final" case when we have more than 2 elements
						    if (blockingArmor.size()>2 && i==blockingArmor.size()-1)
			                    armorNames+="and ";
						}
		                
		                armorNames+=(String)blockingArmor.get(i).getProperty("name");
		            }
		            
		            long damageReduction = 0l;
		            if ((Long)blockResult.get("damageReduction")!=null)
		                damageReduction = (Long)blockResult.get("damageReduction");
		            if (damageReduction == attackResult.damage)
		            	attackResult.status += "<br>However, this attack was completely blocked due to the "+armorNames+". No "+((String)blockResult.get("damageType")).toLowerCase()+" damage was done.";
		            else
		            	attackResult.status += "<br>However, this attack was <u>partially</u> blocked.<br> "+blockResult.get("damageType")+" damage was reduced by "+blockResult.get("damageReduction")+" due to the "+armorNames+", "+(attackResult.damage-damageReduction)+" total damage was dealt.";
		            
		            if (blockResult.get("status")!=null)
		            	attackResult.status += "<br>"+(String)blockResult.get("status");
		                
		            attackResult.damage-=damageReduction;
		            
                    if (attackResult.damage==0)
                        return attackResult;
		        }
		    }
		}
		return attackResult;
	}

	/**Placeholder
	 * 
	 * @param string
	 * @param weapon
	 * @return
	 */
	public Object solveProperty(String string, CachedEntity weapon, String fieldName)
	{
		return null;
	}


	/**
	 * Returns true if the attack has been completely blocked (so the calling method can stop checking equipment and damaging durability)
	 * @param result
	 * @param sourceCharacter
	 * @param targetCharacter
	 * @param sourceWeapon
	 * @param blockingEntity
	 * @return
	 */
	private boolean updateBlockAttackResult(Map<String, Object> result, CachedEntity sourceCharacter, CachedEntity targetCharacter, CachedEntity sourceWeapon, int damage, CachedEntity blockingEntity)
	{
		String damageType = null;
		if (sourceWeapon != null) damageType = (String) sourceWeapon.getProperty("weaponDamageType");
		if (damageType != null && damageType.equals("")) damageType = null;
		String blockCapability = "";
		Long damageReduction = (Long) blockingEntity.getProperty("damageReduction");

		if (damageReduction == null) damageReduction = 10l;

		// If this weapon supports multiple damage types, choose which one is
		// best depending on the armor's weakness
		if (damageType != null)
		{
			// Decide what damage type to use if there is more than once
			// choice...
			if (damageType.contains(" and ") || damageType.contains(","))
			{
				String[] damageTypes = damageType.split("( and |,\\s*)");
				String damageTypeCandidate = null;
				int damageTypeCandidateOrdinal = 10;
				for (String candidate : damageTypes)
				{
					String blockingCapabilityStr = (String) blockingEntity.getProperty("block" + candidate + "Capability");
					if (blockingCapabilityStr == null || blockingCapabilityStr.equals("")) blockingCapabilityStr = "Average";
					int ordinal = BlockCapability.valueOf(blockingCapabilityStr).ordinal();
					if (damageTypeCandidate == null)
					{
						damageTypeCandidate = candidate;
						damageTypeCandidateOrdinal = ordinal;
						continue;
					}

					if (ordinal < damageTypeCandidateOrdinal)
					{
						damageTypeCandidate = candidate;
						damageTypeCandidateOrdinal = ordinal;
					}
				}

				damageType = damageTypeCandidate;
			}

			blockCapability = (String) blockingEntity.getProperty("block" + damageType + "Capability");
			if (blockCapability == null) blockCapability = "Average";

			// Now figure out how much damage reduction will apply...
			if (blockCapability.equals("None")) damageReduction = 0l;
			else if (blockCapability.equals("Minimal")) damageReduction /= 2;
			else if (blockCapability.equals("Excellent")) damageReduction *= 2;
			else if (blockCapability.equals("Good")) damageReduction = new Double(damageReduction.doubleValue() * 1.5d).longValue();
			else if (blockCapability.equals("Poor")) damageReduction = new Double(damageReduction.doubleValue() * 0.75d).longValue();
			
		}
		else
			damageType = "";
		result.put("damageType", damageType);

		Long totalBlockedSoFar = (Long) result.get("damageReduction");
		if (totalBlockedSoFar == null) totalBlockedSoFar = 0l;
		long reduction = totalBlockedSoFar + damageReduction;
		if (reduction > damage) reduction = damage;

		result.put("blocked", true);
		if (result.get("damageReduction") != null)
		{
			@SuppressWarnings("unchecked")
			List<CachedEntity> blockingArmor = (List<CachedEntity>) result.get("blockingArmor");
			blockingArmor.add(blockingEntity);
		}
		else
		{
			ArrayList<CachedEntity> blockingEntities = new ArrayList<CachedEntity>();
			blockingEntities.add(blockingEntity);
			result.put("blockingArmor", blockingEntities);
		}
		result.put("damageReduction", reduction);

		// Now reduce durability of the blocking equipment
		// At this point, reduce the durability of the weapon by 1
		Long durability = (Long) blockingEntity.getProperty("durability");
		if (durability != null)
		{
			if (durability <= 0)
			{
				doDestroyEquipment(getDB(), targetCharacter, blockingEntity);
				statsCache.remove(targetCharacter.getKey());
				String status = (String) result.get("status");
				if (status == null) status = "";
				if (blockingEntity.getProperty("naturalEquipment") != null && blockingEntity.getProperty("naturalEquipment").equals("TRUE"))
					result.put("status", status + "<div class='equipment-destroyed-notice'>" + targetCharacter.getProperty("name") + "'s " + blockingEntity.getProperty("name")
							+ " is no longer usable. </div>");
				else
					result.put("status", status + "<div class='equipment-destroyed-notice'>" + targetCharacter.getProperty("name") + "'s " + blockingEntity.getProperty("name")
							+ " was so badly damaged it has been destroyed. </div>");

			}
			else
			{
				blockingEntity.setProperty("durability", durability - 1);
				
				// Also reduce the charges for any buff that the equipment might have that has charges on it
				reduceBuffCharges(blockingEntity);
				
				getDB().put(blockingEntity);
			}
		}

		if (reduction == damage)
			return true;
		else
			return false;

	}
	 
	public Map<String, Object> doBlockAttack(CachedEntity sourceCharacter, CachedEntity sourceWeapon, int damage, CachedEntity targetCharacter)
	{
		Map<String, Object> result = new HashMap<String, Object>();

		// Collect all items that will block, to fetch all at once and attempt to block.
		List<Key> blockItems = new ArrayList<Key>();
		// First see if what the character is holding has blocked the attack
		for(Key checkKey:Arrays.asList(
				(Key)targetCharacter.getProperty("equipmentLeftHand"),
				(Key)targetCharacter.getProperty("equipmentRightHand"),
				(Key)targetCharacter.getProperty("equipmentRightRing"),
				(Key)targetCharacter.getProperty("equipmentLeftRing"),
				(Key)targetCharacter.getProperty("equipmentNeck")))
		{
			if(checkKey!=null)
				blockItems.add(checkKey);
		}
		
		// Next, randomly determine where the attack is likely to land on the body (which piece of equipment will be hit)
		// Body/Arms = 50%, Legs = 30%, Head = 10%, Hands = 5%, Feet = 5%
		Random rnd = new Random();
		int hitPlacement = rnd.nextInt(100);
		if (GameUtils.between(hitPlacement, 0, 50))
		{
			// chest/arms hit
			for(Key checkKey:Arrays.asList(
					(Key)targetCharacter.getProperty("equipmentChest"),
					(Key)targetCharacter.getProperty("equipmentShirt")))
				if(checkKey != null)
					blockItems.add(checkKey);
		}
		else if (GameUtils.between(hitPlacement, 50, 80))
		{
			// legs hit
			Key armorKey = (Key)targetCharacter.getProperty("equipmentLegs");
			if (armorKey!=null)
				blockItems.add(armorKey);
			
		}
		else if (GameUtils.between(hitPlacement, 80, 90))
		{
			// head hit
			Key armorKey = (Key)targetCharacter.getProperty("equipmentHelmet");
			if (armorKey!=null)
				blockItems.add(armorKey);
		}
		else if (GameUtils.between(hitPlacement, 90, 95))
		{
			// Hands hit
			Key armorKey = (Key)targetCharacter.getProperty("equipmentGloves");
			if (armorKey!=null)
				blockItems.add(armorKey);
		}
		else if (GameUtils.between(hitPlacement, 95, 100))
		{
			// feet hit
			Key armorKey = (Key)targetCharacter.getProperty("equipmentBoots");
			if (armorKey!=null)
				blockItems.add(armorKey);
		}
		
		// Get all the entities from DB. 
		// Possible that item has been destroyed, so clear out null entries first.
		List<CachedEntity> blockEntities = getDB().get(blockItems);
		for(int i = blockEntities.size()-1; i >= 0; i--)
			if(blockEntities.get(i) == null) blockEntities.remove(i);
		
		// Process all the blocks now. 
		for(CachedEntity block:GameUtils.roll(blockEntities, "blockChance", null, null))
		{
			if (block!=null)
			{				
				ScriptService service = ScriptService.getScriptService(this);
				
				List<CachedEntity> scripts = service.getScriptsOfType(block, ScriptType.onDefendHit);
				
				for(CachedEntity script : scripts) {
					CombatEvent event = new CombatEvent(this, sourceCharacter, block, targetCharacter);
										
					service.executeScript(event, script, block);
					
					if(!event.haltExecution)
						service.cleanupEvent(event);
				}
				
				
				if(updateBlockAttackResult(result, sourceCharacter, targetCharacter, sourceWeapon, damage, block))
					break;
			}
		}
		
		return result;
	}
	
	
	
	/**
	 * 
	 * @param user
	 * @param characterToDieFinal
	 * @param attackingCharacterFinal
	 * @return If auto-looting took place, this text includes the loot that was collected.
	 */
	public String doCharacterKilled(CachedEntity user, final CachedEntity characterToDieFinal, final CachedEntity attackingCharacterFinal)
	{
		String loot = "";
		final CachedDatastoreService db = getDB();
		
		final CachedEntity locationFinal = getEntity((Key)characterToDieFinal.getProperty("locationKey"));
		
		Map<String, Object> result = null;
		try 
		{
			result = new InitiumTransaction<Map<String, Object>>(db) 
			{
				public boolean disableTransaction() {return true;};
				
				@Override
				public Map<String, Object> doTransaction(CachedDatastoreService ds) 
				{
					boolean giveLootToAttacker = false;
//					ds.refetch(Arrays.asList(characterToDieFinal, attackingCharacterFinal, locationFinal));
					
					// We no longer set the character name to Dead here. We now do that from the death screen.
//						String charName = (String)characterToDie.getProperty("name");	// Save the character name for later
//						characterToDie.setProperty("name", "Dead "+characterToDie.getProperty("name"));
//						db.put(characterToDie);
					
					
//					boolean attackingCharacterNeedsNotification = false;
//					if (GameUtils.enumEquals(attackingCharacterFinal.getProperty("combatType"), CombatType.DefenceStructureAttack))
//						attackingCharacterNeedsNotification = false;
					
					// Now make the attacking character no longer in combat mode
					setCharacterMode(null, attackingCharacterFinal, ODPDBAccess.CHARACTER_MODE_NORMAL);
					attackingCharacterFinal.setProperty("combatant", null);
					attackingCharacterFinal.setProperty("combatType", null);
					attackingCharacterFinal.setProperty("locationEntryDatetime", new Date());
	
					// Make the attacking character party no longer in combat mode
					List<CachedEntity> party = getParty(ds, attackingCharacterFinal);
					if (party != null) {
						setPartiedField(party, attackingCharacterFinal, "mode", ODPDBAccess.CHARACTER_MODE_NORMAL);
						setPartiedField(party, attackingCharacterFinal, "combatant", null);
						setPartiedField(party, attackingCharacterFinal, "combatType", null);
						setPartiedField(party, attackingCharacterFinal, "locationEntryDatetime", new Date());
						
					}
					
					////////////////////////
					// Now, depending on if the killed character is an NPC or not, and if the killer is an NPC or not, do some stuff...
			
					
					// First, always set the timestamp
					characterToDieFinal.setProperty("locationEntryDatetime", new Date());

					
					// If the attacker is a PC
					if (attackingCharacterFinal.getProperty("type")==null || "".equals(attackingCharacterFinal.getProperty("type")) || "PC".equals(attackingCharacterFinal.getProperty("type")))
					{
						if (locationFinal.getProperty("type")!=null && locationFinal.getProperty("type").equals("CombatSite"))
						{
							// Now change the location's banner to the generic defeated banner
							locationFinal.setProperty("banner", "images/npc-defeated1.jpg");
							locationFinal.setProperty("description", "This is the location where a battle took place, but the battle is over now.");
						}
					}
					
					// If the killed character is a NPC
					if (characterToDieFinal.getProperty("type")==null || "".equals(characterToDieFinal.getProperty("type")) || "NPC".equals(characterToDieFinal.getProperty("type")))
					{
						resetInstanceRespawnTimer(locationFinal);
						
						
						characterToDieFinal.setProperty("mode", "DEAD");
						characterToDieFinal.setProperty("name", "Dead "+characterToDieFinal.getProperty("name"));
					}
					else
					{
						characterToDieFinal.setProperty("mode", "UNCONSCIOUS");
						
						// If we didn't die, and player is in a good rest site (Inn, owned house), 
						// then revive but drop all items.
						if(doCharacterDieChance(characterToDieFinal) == false &&
								CommonChecks.checkLocationIsGoodRestSite(locationFinal))
						{
							// Died in good rest spot, heal to 1hp
							characterToDieFinal.setProperty("hitpoints", 1d);
				        	
			                // Also unequip all of his equipment. 
			                for(String slot:EQUIPMENT_SLOTS)
			                	characterToDieFinal.setProperty("equipment"+slot, null);
			                
			                // Reset his mode to normal
			                characterToDieFinal.setProperty("mode", CHARACTER_MODE_NORMAL);
						}
					}
					
					// If the attacking character was at full health when he killed his opponent, award a buff
					if (attackingCharacterFinal.getProperty("hitpoints").equals(attackingCharacterFinal.getProperty("maxHitpoints")))
					{
						awardBuff_Pumped(ds, attackingCharacterFinal);
					}
					

					// If the character was a party leader, re-assign leader to another player (a LIVE one hopefully)
					if ("TRUE".equals(characterToDieFinal.getProperty("partyLeader")))
					{
						Double maxDex = 0.0d;
						CachedEntity newLeader = null;
						List<CachedEntity> partyMembers = getParty(db, characterToDieFinal);
						if (partyMembers!=null)
							for(CachedEntity member:partyMembers)
								if (GameUtils.isPlayerIncapacitated(member)==false && (Double)member.getProperty("dexterity") > maxDex)
								{
									maxDex = (Double)member.getProperty("dexterity");
									newLeader = member;
								}
						
						if(newLeader != null)
						{
							characterToDieFinal.setProperty("partyLeader", "FALSE");
							newLeader.setProperty("partyLeader", "TRUE");
							db.put(newLeader);
						}
					}
					
					
					
					// Here we check if the attacking character was in a rest area AND if it is in a blockade structure. If so,
					// we will restore his health
					// ATTACKING CHARS NO LONGER GET FREE HEALS
//					CachedEntity attackingCharacterLocation = getEntity((Key)attackingCharacter.getProperty("locationKey"));
//					if (attackingCharacterLocation!=null &&
//							"RestSite".equals(attackingCharacterLocation.getProperty("type")) && 
//							attackingCharacterLocation.getProperty("defenceStructure")!=null)
//					{
//						Double hitpoints = (Double)attackingCharacter.getProperty("hitpoints");
//						Double maxHitpoints = (Double)attackingCharacter.getProperty("maxHitpoints");
//						if (hitpoints<maxHitpoints)
//							attackingCharacter.setProperty("hitpoints", maxHitpoints);
//						
//						
//						attackingCharacter.setProperty("mode", CHARACTER_MODE_NORMAL);
//						attackingCharacter.setProperty("combatant", null);
//						attackingCharacter.setProperty("combatType", null);
//
//					}
//					if (attackingCharacterNeedsNotification)
//						queueMainPageUpdateForCharacter(attackingCharacterFinal.getKey(), "updateFullPage_shortcut");
					

					// Here we check if the battle took place in an instance, defence structure, or territory. If so, 
					// the attacker will autoloot.
					// ALSO
					// If the fight took place in a non-combat site AND the hitpoints is less than 100, we will auto loot
					Long gold = null;
//					CachedEntity attackerCharacterLocation = getEntity((Key)attackingCharacterFinal.getProperty("locationKey"));
					if (	
//							/* If the attacker or defender were standing in an instance*/
//							(locationFinal.getProperty("territoryKey")!=null || 
//									locationFinal.getProperty("defenceStructure")!=null || 
//							"Instance".equals(locationFinal.getProperty("combatType")) ||
//							attackerCharacterLocation.getProperty("territoryKey")!=null || 
//							attackerCharacterLocation.getProperty("defenceStructure")!=null || 
//							"Instance".equals(attackerCharacterLocation.getProperty("combatType")))
//							||
//							/* If the fight took place in a non-combat site and the max hitpoints of the defender was less than 100 */
//							("CombatSite".equals(locationFinal.getProperty("type"))==false &&
//							(Double)characterToDieFinal.getProperty("maxHitpoints")<100d)
							"CombatSite".equals(locationFinal.getProperty("type"))==false
						)
					{
						gold = (Long)characterToDieFinal.getProperty("dogecoins");
						if (attackingCharacterFinal.getProperty("dogecoins")==null) attackingCharacterFinal.setProperty("dogecoins", 0L);
						
						if (gold!=null)
							attackingCharacterFinal.setProperty("dogecoins", ((Long)attackingCharacterFinal.getProperty("dogecoins"))+gold);
						characterToDieFinal.setProperty("dogecoins", 0l);
						giveLootToAttacker = true;
					}
					

					characterToDieFinal.setProperty("combatType", null);
					characterToDieFinal.setProperty("status", CharacterMode.NORMAL.toString());
					characterToDieFinal.setProperty("combatant", null);
					
					// Leave the party if we haven't already
					doRequestLeaveParty(ds, characterToDieFinal, true);
					
					db.put(characterToDieFinal);
					db.put(locationFinal);
					db.put(attackingCharacterFinal);
					if (party != null) {
						putPartyMembersToDB_SkipSelf(db, party, attackingCharacterFinal);
					}
					
					Map<String, Object> result = new HashMap<String, Object>();
					
					result.put("characterToDie", characterToDieFinal);
					result.put("attackingCharacter", attackingCharacterFinal);
					result.put("location", locationFinal);
					result.put("giveLootToAttacker", giveLootToAttacker);
					result.put("goldCollected", gold);
					
					return result;
				}
			}.run();
		} 
		catch (AbortTransactionException e) 
		{
			throw new RuntimeException(e.getMessage());
		}

		CachedEntity characterToDie = characterToDieFinal;
		CachedEntity location = locationFinal;
		CachedEntity attackingCharacter = attackingCharacterFinal;
		boolean giveLootToAttacker = (Boolean)result.get("giveLootToAttacker");
		boolean overburdened = false;
		
		// If we're in a territory, always give loot to attacker
		if (location.getProperty("territoryKey")!=null)
			giveLootToAttacker = true;
		
		// If loot preference for NPC is CollectNone, do not loot the items.
		if("NPC".equals(attackingCharacter.getProperty("type")) && giveLootToAttacker &&
				"CollectNone".equals(attackingCharacter.getProperty("lootPreference")))
			giveLootToAttacker = false;
		
		// If the character was defending a defence structure, then refresh the leader on that structure
		if (giveLootToAttacker)
		{
			CachedEntity defenceStructure = getEntity((Key)location.getProperty("defenceStructure"));
			if (defenceStructure!=null)
				refreshDefenceStructureLeader(db, defenceStructure, null);
			// Add the gold collected to the loot message...
			loot+="<strong>Loot</strong><br>";
			loot+="<span>Gold Collected: "+result.get("goldCollected")+"</span><br>";
			
			// Check if the character is overburdened or not. If so, we cannot give the loot to the attacker
			double carryingWeight = getCharacterCarryingWeight(attackingCharacter);
			double maxCarryingWeight = getCharacterMaxCarryingWeight(attackingCharacter);
			if (carryingWeight>maxCarryingWeight)
			{
				loot+="<div class='highlightbox-red'>You are overburdened so the following loot has been dropped at your feet where other players could potentially take it.</div>";
				overburdened=true;
				
				// If a player was the one that lived and he is overburdened, then send him a game message to that effect
				if (GameUtils.equals(getCurrentCharacter(), attackingCharacter))
					sendGameMessage(db, attackingCharacter, "You are overburdened! Some loot is on the floor where your enemy has fallen.");
			}
		}
		
		// Only set HCM items for HCM mobs (or legacy mobs without an HCM flag specified). 
		boolean setHardcoreModeItems = false;
		if("NPC".equals(characterToDie.getProperty("type")) &&
				(characterToDie.getProperty("hardcoreMode") == null || CommonChecks.checkIsHardcore(characterToDie)) && 
				CommonChecks.checkIsHardcore(attackingCharacter))
		{
			// For now, only do HCM item drops. Do NOT save these entities, potential race conditions.
			Map<String, String> damageMap = getValue_StringStringMap(characterToDie, "combatStatsDamageMap");
			Map<String, Double> characterMap = new HashMap<String, Double>();
        	if(damageMap != null)
        	{
        		// Lets get all the associated entities first, and damage for each key
        		List<Key> damageKeys = new ArrayList<Key>();
        		
        		for(Entry<String,String> entry:damageMap.entrySet())
        		{
        			damageKeys.add(GameUtils.parseKey(entry.getKey()));
        			Double curDamage = Double.valueOf(entry.getValue());
        			characterMap.put(entry.getKey(), curDamage);
        		}
        		
        		List<CachedEntity> damagers = getEntities(damageKeys);
        		if(damagers.size() > 0)
        		{
	        		Double maxHitpoints = (Double)characterToDie.getProperty("maxHitpoints");
	        		// We need to ensure the highest damage is from an HCM character, and that the total
	        		// HCM damage is at least 50%.
	        		Double maxSCM = 0d;
	        		Double maxHCM = 0d;
	        		Double totalDamage = 0d;
	        		for(CachedEntity dmg:damagers)
	        		{
	        			if(dmg == null) continue;
	        			Double curDamage = characterMap.get(dmg.getKey().toString());
	        			
	        			if(CommonChecks.checkIsHardcore(dmg))
	        			{
	        				totalDamage += curDamage;
	        				maxHCM = Math.max(maxHCM, curDamage);
	        			}
	        			else
	        				maxSCM = Math.max(maxSCM, curDamage);
	        		}
	        		
	        		// HCM needs to be highest damage, and total HCM damage should be > half.
	        		setHardcoreModeItems = maxHCM >= maxSCM && totalDamage >= (maxHitpoints/2.0);
        		}
        		
            	if(setHardcoreModeItems)
        		{
            		// Only if HCM alts were max damage, award points equal to a percentage
            		// of total damage done by this alt.
            		Double curCharDamage = characterMap.get(attackingCharacter.getKey().toString());
            		Long curPoints = (Long)attackingCharacter.getProperty("hardcoreRank");
            		if(curPoints == null) curPoints = 0L;
            		
            		Long newPoints = (Long)characterToDie.getProperty("hardcorePointValue");
            		if(newPoints != null && curCharDamage != null && curCharDamage > 0d)
            		{
            			Double earnedPoints = Math.min((curCharDamage / (Double)characterToDie.getProperty("maxHitpoints")),1d) * newPoints;
            			newPoints = earnedPoints.longValue();
                	}
            		else
            			newPoints = 1L;

        			attackingCharacter.setProperty("hardcoreRank", newPoints + curPoints);
        		}
        	}
		}
		
		// First, move all items in his inventory to the ground...
		List<CachedEntity> items = getFilteredList("Item", "containerKey", characterToDie.getKey());
		
		StringBuilder dropAllSB = new StringBuilder();
		for(CachedEntity item:items)
		{
			if (item==null) continue;
			
			// If the item is a "naturalEquipment", simply delete it instead of moving to the ground
			if ("TRUE".equals(item.getProperty("naturalEquipment")))
			{
				db.delete(item.getKey());
			}
			else
			{
				if(item.getProperty("hardcoreMode") == null)
				{
					if(this.isTestServer())
						Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING,
								String.format("SetHardcore: %1$b; Equippable: %2$b; Created: %3$s; Cutoff: %4$tF", 
										setHardcoreModeItems, 
										CommonChecks.checkItemIsEquippable(item), 
										item.getProperty("createdDate") == null ? "FALSE" : ((Date)item.getProperty("createdDate")).toString(), 
										HardcoreModeCutoffDate));
					
					// Only set HCM on equippables.
					if(setHardcoreModeItems 
							&& CommonChecks.checkItemIsEquippable(item)
							&& item.getProperty("createdDate") != null 
							&& (this.isTestServer() || ((Date)item.getProperty("createdDate")).after(HardcoreModeCutoffDate)))
						item.setProperty("hardcoreMode", true);
					else
						item.setProperty("hardcoreMode", false);
				}
				
				if (giveLootToAttacker==false)
				{
					item.setProperty("containerKey", location.getKey());
					item.setProperty("movedTimestamp", new Date());
				}
				else
				{
					if (overburdened)
					{
						item.setProperty("containerKey", location.getKey());
						item.setProperty("movedTimestamp", new Date());
						
						if ("NPC".equals(attackingCharacterFinal.getProperty("type"))==false)
							loot+=GameUtils.renderItem(item)+"<br>";
					}
					else
					{
						item.setProperty("containerKey", attackingCharacter.getKey());
						item.setProperty("movedTimestamp", new Date());
						
						if ("NPC".equals(attackingCharacterFinal.getProperty("type"))==false)
						{
							dropAllSB.append("," + item.getId().toString());
							loot+="<div style='display:inline-block; margin-right:3px;'>";
							loot+=GameUtils.renderItem(item)+"<br>";
							loot+="<div>";
							loot+="		<div class='main-item-controls'>";
							// Get all the slots this item can be equipped in
							loot+="			<a onclick='characterDropItem(event, "+item.getKey().getId()+", loadInventory)' >Drop on ground</a>";
							if (item.getProperty("maxWeight")!=null)
							{
								loot+="<a onclick='pagePopup(\"/odp/ajax_moveitems?selfSide=Character_"+attackingCharacterFinal.getKey().getId()+"&otherSide=Item_"+item.getKey().getId()+"\")'>Open</a>";
							}
							loot+="		</div>";
							loot+="</div>";
							loot+="</div>";
						}
					}
				}
				db.put(item);
			}
		}
		
		if(dropAllSB.length()>0)
		{
			String dropAllString = dropAllSB.substring(1);
			loot+="<div style='display:block; margin:6px;'>";
			loot+="	<div class='main-item-controls'>";
			loot+="		<a onclick='doCommand(event,\"ItemsDrop\",{\"itemIds\":\"" + dropAllString + "\"});' >Drop All</a>";
			loot+="	</div>";
			loot+="</div>";
		}
		
		// Now drop all the carried characters.
		List<CachedEntity> carriedChars = getFilteredList("Character", "locationKey", characterToDie.getKey());
		for(CachedEntity chars:carriedChars)
		{
			chars.setProperty("locationKey", location.getKey());
			db.put(chars);
		}
		
		
		if (loot.equals(""))
			loot = null;
		
		// Clear guard settings on death.
		GuardService gService = new GuardService(this);
		gService.deleteAllGuardSettings(characterToDie.getKey(), location.getKey());
		
		// Finally, lets update the character that was passed into this method so further processing will have
		// the updated field values
		// DEPRECATED: We are maintaining proper state directly on the referenced entity.
		//CachedDatastoreService.copyFieldValues(characterToDie, characterToDieFinal);
		//CachedDatastoreService.copyFieldValues(attackingCharacter, attackingCharacterFinal);
		
		return loot;
	}
	
	
	
	private void doCharacterZombify(ODPAuthenticator auth, CachedDatastoreService ds, CachedEntity attackingCharacter, CachedEntity zombifyingCharacter)
	{
		if (ds==null)
			ds = getDB();
		
		String newZombieName = "Zombie "+(String)zombifyingCharacter.getProperty("name");
		// First create a new character for the player and set the player to be using the new character on his user entity...
		try
		{
			CachedEntity zombifiedUser = getEntity((Key)zombifyingCharacter.getProperty("userKey"));
			CachedEntity newCharacter = doCreateNewCharacterFromDead(ds, auth, zombifiedUser, zombifyingCharacter);

			if (zombifiedUser!=null)
			{
				zombifiedUser.setProperty("characterKey", newCharacter.getKey());
				ds.put(zombifiedUser);
				queueMainPageUpdateForCharacter(newCharacter.getKey(), "updateFullPage_shortcut");
			}
		}
		catch (UserErrorMessage e)
		{
			Logger.getLogger(this.getClass().getSimpleName()).log(Level.SEVERE, "Couldn't create new character from dead.", e);
		}

		
		// Now reset the attacker's combat mode..
		setCharacterMode(ds, attackingCharacter, ODPDBAccess.CHARACTER_MODE_NORMAL);
		attackingCharacter.setProperty("combatant", null);
		attackingCharacter.setProperty("combatType", null);
		
		// Now turn the player's old body into a zombie...
		zombifyingCharacter.setProperty("type", "NPC");
		zombifyingCharacter.setProperty("hitpoints", zombifyingCharacter.getProperty("maxHitpoints"));
		zombifyingCharacter.setProperty("name", newZombieName);
		zombifyingCharacter.setProperty("combatant", null);
		zombifyingCharacter.setProperty("combatType", null);
		setCharacterMode(ds, zombifyingCharacter, ODPDBAccess.CHARACTER_MODE_NORMAL);
		zombifyingCharacter.setProperty("status", "Zombie");

		
	}
	

	/**
	 * Placeholder
	 * 
	 * @param ds2
	 * @param auth
	 * @param zombifiedUser
	 * @param zombifyingCharacter
	 * @return
	 */
	public CachedEntity doCreateNewCharacterFromDead(CachedDatastoreService ds2, ODPAuthenticator auth, CachedEntity zombifiedUser, CachedEntity zombifyingCharacter) throws UserErrorMessage
	{
		return null;
	}

	public boolean doCharacterDieChance(CachedEntity character)
	{
		if((Double)character.getProperty("hitpoints")<=0d && GameUtils.enumEquals(character.getProperty("mode"), CharacterMode.UNCONSCIOUS))
		{
			Double chance = (Double)character.getProperty("hitpoints");
			chance*=-1;
			chance+=1;
			
			if (CommonChecks.checkIsHardcore(character) || GameUtils.roll(chance))
			{
				character.setProperty("mode", CharacterMode.DEAD.toString());
				
				sendMainPageUpdateForCharacter(getDB(), character.getKey(), "updateFullPage_shortcut");
				
				return true;
			}
		}
		return false;
	}



	public void doDestroyEquipment(CachedDatastoreService db, CachedEntity character, CachedEntity equipment)
	{
		if (db==null)
			db = getDB();
		
		if (checkCharacterHasItemEquipped(character, equipment.getKey()))
		{
			doCharacterUnequipEntity(db, character, equipment.getKey());
		}
		
		db.delete(equipment.getKey());
	}

	public void doRequestLeaveParty(CachedDatastoreService ds, CachedEntity character)
	{
		doRequestLeaveParty(ds, character, false);
	}
	
	public void doRequestLeaveParty(CachedDatastoreService ds, CachedEntity character, boolean dontSave)
	{
		if (ds==null)
			ds = getDB();
		
		String partyCode = (String)character.getProperty("partyCode");
		if (partyCode==null || partyCode.equals(""))
			return;
		
		List<CachedEntity> party = getParty(ds, character);
		if (party==null)
			return;
		
		if ("TRUE".equals(character.getProperty("partyLeader")))
		{
			// We need to reassign to a new leader before leaving the party...
			if ("TRUE".equals(party.get(0).getProperty("partyLeader")))
			{
				party.get(1).setProperty("partyLeader", "TRUE");
				ds.put(party.get(1));
			}
			else
			{
				party.get(0).setProperty("partyLeader", "TRUE");
				ds.put(party.get(0));
			}
		}
			
		
		
		character.setProperty("partyCode", null);
		character.setProperty("partyLeader", null);
		character.setProperty("partyJoinsAllowed", null);
		
		if (dontSave==false)
			ds.put(character);
		
		getParty(ds, partyCode);
	}

	
	public void doRequestJoinParty(CachedDatastoreService ds, CachedEntity character, CachedEntity partiedCharacter) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();

		if (character.getKey().getId() == partiedCharacter.getKey().getId())
			throw new UserErrorMessage("You cannot join yourself in a party. You should seriously invest in some friends.");
		
		if (((Key)character.getProperty("locationKey")).getId() != ((Key)partiedCharacter.getProperty("locationKey")).getId())
			throw new UserErrorMessage("You cannot party with a character that is not in the same location as you.");
		
		if (getParty(ds, character)!=null)
			throw new UserErrorMessage("You cannot join another party until you leave the one you're in already. <a onclick='leaveParty()'>Click here</a> to leave your current party.");
		
		// Get party first. If single member in party, clears out party code, which
		// allows us to create party with new code.
		List<CachedEntity> currentParty = getParty(ds, partiedCharacter);
		String partyCode = (String)partiedCharacter.getProperty("partyCode");
		if (partyCode==null || partyCode.equals("") || currentParty == null)
		{
			if ("TRUE".equals(partiedCharacter.getProperty("partyJoinsAllowed"))==false)
				throw new UserErrorMessage("This user is not accepting party joins at this time.");
			
			Random rnd = new Random();
			partyCode = rnd.nextLong()+"";
			
			partiedCharacter.setProperty("partyCode", partyCode);
			partiedCharacter.setProperty("partyLeader", "TRUE");
			
			ds.put(partiedCharacter);
		}
		else
		{
			CachedEntity leader = null;
			for(CachedEntity e:currentParty)
				if ("TRUE".equals(e.getProperty("partyLeader")))
					leader = e;
			
			if (leader==null)
				throw new RuntimeException("Party had no leader. This shouldn't be possible.");
			
			if ("TRUE".equals(leader.getProperty("partyJoinsAllowed"))==false)
				throw new UserErrorMessage("This party is not accepting new members at the moment. The party leader is "+leader.getProperty("name")+".");
			
			// Determine if this party can hold more members, if not, throw
			if (currentParty.size()>=4)
				throw new UserErrorMessage("The party is full. A party can have a maximum of 4 members.");
		}
		
		character.setProperty("partyLeader", null);
		character.setProperty("partyCode", partyCode);
		
		ds.put(character);
	}

	
	public void doChangePartyLeader(CachedDatastoreService ds, CachedEntity selfCharacter, CachedEntity newLeader) throws UserErrorMessage
	{
		if (ds==null)
			ds = getDB();
		
		String selfPartyCode = (String)selfCharacter.getProperty("partyCode");
		String newLeaderPartyCode = (String)newLeader.getProperty("partyCode");
		
		if (selfPartyCode.equals(newLeaderPartyCode)==false)
			throw new UserErrorMessage("You cannot assign that character as leader because he is not currently in your party.");
		
		if ("TRUE".equals(selfCharacter.getProperty("partyLeader"))==false)
			throw new UserErrorMessage("You cannot change the party leader of your party because you are not the party leader.");
		
		newLeader.setProperty("partyLeader", "TRUE");
		selfCharacter.setProperty("partyLeader", null);

		ds.put(newLeader);
		ds.put(selfCharacter);
	}
	
	

	/**
	 * This will handle the leader state for a defence structure. It ensures the leader is always set properly (or unset).
	 * 
	 * @param ds
	 * @param defenceStructure
	 * @param charactersAtDefenceStructure
	 * @return True indicates the leader was changed. False indicates no change.
	 */
	public boolean refreshDefenceStructureLeader(CachedDatastoreService ds, CachedEntity defenceStructure, List<CachedEntity> charactersAtDefenceStructure)
	{
		// See if we need a new leader or if the old one is ok...
		CachedEntity currentLeader = getEntity((Key)defenceStructure.getProperty("leaderKey"));
		
		if (currentLeader!=null && GameUtils.isPlayerIncapacitated(currentLeader)==false && 
				currentLeader.getProperty("status")!=null && 
				((String)currentLeader.getProperty("status")).startsWith("Defending"))
		{
			return false;
		}
	
		// If we weren't given a list of characters, then fetch them now...
		charactersAtDefenceStructure = getFilteredList("Character", "locationKey", defenceStructure.getProperty("locationKey"));
		
		
		// Order the characters by their status...
		shuffleCharactersByAttackOrder(charactersAtDefenceStructure);	
		
		
		
		// Assess the situation...
		for(CachedEntity chr:charactersAtDefenceStructure)
		{
			if (GameUtils.isPlayerIncapacitated(chr))
				continue;
			
			if (chr.getProperty("status")!=null && ((String)chr.getProperty("status")).startsWith("Defending"))
			{
				defenceStructure.setProperty("leaderKey", chr.getKey());
				ds.put(defenceStructure);
				return true;
			}
		}
		
		
		
		if (currentLeader==null)
			return false;
		else
		{
			defenceStructure.setProperty("leaderKey", null);
			ds.put(defenceStructure);
			return true;
		}
		
		
	}


	/**
	 * The amount is a dynamically scaled number (also scaled down by 100). The maximum for a stat will be 8, minimum 3
	 * so if the amount = 1 and the current stat is 3, the amount it will increase by
	 * will literally be .05. If the current stat was 5.5, the amount it would increase by
	 * would literally be .025.
	 * 
	 * @param character
	 * @param statName
	 * @param amount
	 */
	public void doCharacterIncreaseStat(CachedDatastoreService ds, CachedEntity character, String statName, double maxStat, Double amount)
	{
		amount/=500;
		if(ds==null)
			ds = getDB();
		Double currentStat = (Double)character.getProperty(statName);
		if (currentStat==null) throw new IllegalArgumentException("Stat name is wrong '"+statName+"'.");
		
		// Normalize the stat since 3 is our lowest for the purpose of this formula
		maxStat-=3;
		currentStat-=3;
		
		// Determine the amount to increase...
		double scale = ((currentStat/maxStat)-1)*-1;
		amount*=scale;
		
		character.setProperty(statName, currentStat+3d+amount);
		
		////////////////////
		// Special cases
		
		
		// If this is strength, also increase hitpoints if necessary...
		if (statName.equals("strength"))
		{
			double newHitpoints = calculateHitpoints((Double)character.getProperty("strength"));
			if (newHitpoints != (Double)character.getProperty("maxHitpoints"))
			{
				double delta = newHitpoints - (Double)character.getProperty("maxHitpoints"); 
				character.setProperty("hitpoints", (Double)character.getProperty("hitpoints")+delta);
				character.setProperty("maxHitpoints", newHitpoints);
			}
		}
		
	}
	

	public boolean isCharacterAbleToCreateCampsite(CachedDatastoreService ds, CachedEntity character, CachedEntity location)
	{
		if ((Long)location.getProperty("supportsCamps")==null || (Long)location.getProperty("supportsCamps")==0)
			return false;

		// Now check if the location has a monster count of less than 25%. If so, allow them to create a camp.
//		Double monsterCount = getMonsterCountForLocation(ds, location);
//		Double maxMonsterCount = (Double)location.getProperty("maxMonsterCount");
//		if (maxMonsterCount==null)
//			return true;
//		if (monsterCount/maxMonsterCount>0.25d)
//			return false;
		
		return true;
		
	}

	
	public long getActivePlayers()
	{
		CachedDatastoreService ds = getDB();
		
		Long count = ds.getStat("ActivePlayerCount");
		if (count==null)
		{
			GregorianCalendar cal = new GregorianCalendar();
			cal.add(Calendar.MINUTE, -10);
			count = getFilteredList_Count("Character", "locationEntryDatetime", FilterOperator.GREATER_THAN, cal.getTime()).longValue();
			ds.setStat("ActivePlayerCount", count, 300);
		}
		
		return count;
	}

	public List<CachedEntity> getActivePlayers(int minutesSinceLastActivity)
	{
		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.MINUTE, minutesSinceLastActivity*-1);
		return getFilteredList("Character", "locationEntryDatetime", FilterOperator.GREATER_THAN, cal.getTime());
	}

	/**
	 * Returns the list of users in a group that have been active within the last given minutes.
	 * 
	 * @param group
	 * @param minutesSinceLastActivity
	 * @return
	 */
	public List<CachedEntity> getActiveGroupPlayers(CachedEntity group, List<CachedEntity> groupMembers, int minutesSinceLastActivity)
	{
		
		if (groupMembers==null)
			groupMembers = getGroupMembers(null, group);
		else
			groupMembers = (ArrayList<CachedEntity>)((ArrayList<CachedEntity>)groupMembers).clone();	// lol
			
		
		// GO through the members list and pull out all the applications into a new list
		for (int i = groupMembers.size() - 1; i >= 0; i--)
			if ("Applied".equals(groupMembers.get(i).getProperty("groupStatus"))) 
				groupMembers.remove(i);

		
		
		// filter out any members that are not within the last activity range we want...
		for(int i = groupMembers.size()-1; i>=0; i--)
		{
			CachedEntity member = groupMembers.get(i);
			Date lastAction = (Date)member.getProperty("locationEntryDatetime");
			Date currentDate = new Date();
			
			if (lastAction==null || (lastAction.getTime()<currentDate.getTime()-(minutesSinceLastActivity*60*1000)))
				groupMembers.remove(i);
		}
			
		
		// Now pull out all the user keys so we can fetch them all at once
		Set<Key> usersToFetch = new HashSet<Key>();
		for(CachedEntity member:groupMembers)
		{
			Key userKey = (Key)member.getProperty("userKey");
			
			if (userKey!=null)
				usersToFetch.add(userKey);
		}
		List<CachedEntity> users = getDB().get(usersToFetch);
		
		
		// Now go through each of the users in the group and remove non-premium members
		for(int i = users.size()-1; i>=0; i--)
		{
			CachedEntity user = users.get(i);
			
			if (GameUtils.equals(user.getProperty("premium"), true)==false)
				users.remove(i);
		}
		
		return users;
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
		
		for(CachedEntity item:inventory)
		{
			if (item==null) continue;
			Long weight = getItemWeight(item);
			
			if (weight==0L)
				continue;
			
			// If the item is equipped (not in the left/right hand) then don't count it's weight against us
			if (CommonChecks.checkItemHasEquipSlot(item, "LeftHand")==false && 
					CommonChecks.checkItemHasEquipSlot(item, "RightHand")==false && 
					CommonChecks.checkItemHasEquipSlot(item, "2Hands")==false && 
					checkCharacterHasItemEquipped(character, item.getKey()))
				continue;
			
			carrying+=weight;
		}

		for(CachedEntity c:inventoryCharacters)
		{
			Long weight = getCharacterWeight(c);
			
			carrying+=weight;
		}
		
		return carrying;
	}
	
	public Long getCharacterMaxCarryingWeight(CachedEntity character)
	{
		long maxCarryWeight = 60000;
		Double str = (Double)character.getProperty("strength");
		
		maxCarryWeight += (long)Math.round((str-3d)*50000d);
		
		
		return maxCarryWeight;
	}
	
	/**
	 * This variation of the method accepts a list of the contents of the container (the inventory argument) if
	 * you happen to already have it. This saves an extra query of the database and is an optimization.
	 * 
	 * @param container
	 * @param containerInventory
	 * @return
	 */
	public Long getItemCarryingWeight(CachedEntity container, List<CachedEntity> containerInventory)
	{
		
		ContainerService cs = new ContainerService(this);
		if(!cs.containsAll(container, containerInventory))
			throw new RuntimeException("Container doesn't contain all items from given list");
		
		long carrying = 0l;
		
		for(CachedEntity internalItem:containerInventory)
		{
			carrying+=getItemWeight(internalItem);
		}

		
		return carrying;
	}
	
	/**
	 * This variation of the method accepts a list of the contents of the container (the inventory argument) if
	 * you happen to already have it. This saves an extra query of the database and is an optimization.
	 * 
	 * @param container
	 * @param containerInventory
	 * @return
	 */
	public Long getItemCarryingSpace(CachedEntity container, List<CachedEntity> containerInventory)
	{
		
		ContainerService cs = new ContainerService(this);
		if(!cs.containsAll(container, containerInventory))
			throw new RuntimeException("Container doesn't contain all items from given list");
		
		long space = 0l;
		
		for(CachedEntity internalItem:containerInventory)
		{
			Long itemSpace = (Long)internalItem.getProperty("space");
			if (itemSpace==null)
				continue;
			
			space+=itemSpace;
		}

		
		return space;
	}

	public Long getItemWeight(CachedEntity item)
	{
		if (item==null) return null;
		Long itemQuantity = (Long)item.getProperty("quantity");
		if (itemQuantity==null) itemQuantity = 1L;
		Long itemWeight = (Long)item.getProperty("weight");
		if (itemWeight==null) itemWeight = 0L;
		
		return itemWeight*itemQuantity;
	}
	
	public Long getItemSpace(CachedEntity item)
	{
		Long itemQuantity = (Long)item.getProperty("quantity");
		if (itemQuantity==null) itemQuantity = 1L;
		Long itemSpace = (Long)item.getProperty("space");
		if (itemSpace==null) itemSpace = 0L;
		
		return itemSpace*itemQuantity;
	}

	/**THIS IS A PLACEHOLDER. Actual implementation is not in the ODP.
	 * 
	 * Using the given entityRequirement, this method will attempt to determine if the given entity 
	 * meets the requirements laid out in the entityRequirement.
	 * 
	 * @param entityRequirement
	 * @param entity
	 * @return 
	 */
	public boolean validateEntityRequirement(CachedEntity entityRequirement, CachedEntity entity)
	{
		return false;
	}

	
	
	public CachedEntity doCharacterTakePath(CachedDatastoreService db, CachedEntity character, CachedEntity path) throws UserErrorMessage
	{
		return doCharacterTakePath(db, character, path, false);
	}
	
	
	public CachedEntity doCharacterTakePath(CachedDatastoreService db, CachedEntity character, CachedEntity path, boolean allowAttack) throws UserErrorMessage
	{
		return doCharacterTakePath(db, character, path, allowAttack, false);
	}
	
	public CachedEntity doCharacterTakePath(CachedDatastoreService db, CachedEntity character, CachedEntity path, boolean allowAttack, boolean isExplore) throws UserErrorMessage
	{
		if (db==null)
			db = getDB();

		if (CHARACTER_MODE_COMBAT.equals(character.getProperty("mode")))
			throw new UserErrorMessage("You cannot move while you're in combat.");
		
		CachedEntity destination = null;
		Key destinationKey = null;
		// First get the character's current location
		Key currentLocationKey = (Key)character.getProperty("locationKey");
		CachedEntity currentLocation = null;
		
		// Then determine which location the character will end up on.
		// If we find that the character isn't on either end of the path, we'll throw.
		Key pathLocation1Key = (Key)path.getProperty("location1Key");
		Key pathLocation2Key = (Key)path.getProperty("location2Key");
		if (currentLocationKey.getId()==pathLocation1Key.getId())
			destinationKey = pathLocation2Key;
		else if (currentLocationKey.getId()==pathLocation2Key.getId())
			destinationKey = pathLocation1Key;
		else
		{
			// We want to now allow players to travel between root locations easily so lets check if that's a possibility now...
			currentLocation = getEntity(currentLocationKey);
			if (CommonChecks.checkLocationIsRootLocation(currentLocation)==false)
			{
				CachedEntity rootLocation = getRootLocation(currentLocation);
				if (GameUtils.equals(rootLocation.getKey(), pathLocation1Key))
					destinationKey = pathLocation2Key;
				else if (GameUtils.equals(rootLocation.getKey(), pathLocation2Key))
					destinationKey = pathLocation1Key;
			}
			
			if (destinationKey==null)
				throw new UserErrorMessage("Character cannot take a path when he is not located at either end of it.");
		}
		
		if("Script".equals(destinationKey.getKind())) return null;
		destination = getEntity(destinationKey);

		String forceOneWay = (String)path.getProperty("forceOneWay");
		if ("FromLocation1Only".equals(forceOneWay) && currentLocationKey.getId() == pathLocation2Key.getId())
			throw new UserErrorMessage("You cannot take this path.");
		if ("FromLocation2Only".equals(forceOneWay) && currentLocationKey.getId() == pathLocation1Key.getId())
			throw new UserErrorMessage("You cannot take this path.");
		
		return doCharacterTakePath(db, character, path, destination, allowAttack, isExplore);
	}
	
	public CachedEntity doCharacterTakePath(CachedDatastoreService db, CachedEntity character, CachedEntity path, CachedEntity destination, boolean allowAttack, boolean isExplore) throws UserErrorMessage
	{
		if (db==null)
			db = getDB();

		if (CHARACTER_MODE_COMBAT.equals(character.getProperty("mode")))
			throw new UserErrorMessage("You cannot move while you're in combat.");
		
		Key destinationKey = destination.getKey();
		// First get the character's current location
		Key currentLocationKey = (Key)character.getProperty("locationKey");
		
		if (CommonChecks.checkLocationIsCombatSite(destination));
			destination.setProperty("lastUsedDate", new Date());
		


		String partyCode = (String) character.getProperty("partyCode");
		boolean isInParty = partyCode != null && !"".equals(partyCode);
		List<CachedEntity> party = getParty(db, character);
		// We use the party collection for non-partied character as well.
		if (party == null) party = Collections.singletonList(character);
		List<Key> partyKeys = new ArrayList<Key>();
		for(CachedEntity member:party)
			if(member != null && GameUtils.equals(member.getKey(), character.getKey())==false)
				partyKeys.add(member.getKey());
		
		MovementService movementService = new MovementService(this);

		if (path!=null)
		{
			movementService.checkCharacterIsAllowedToEnterOwnedProperty(path, destination, party);

			// Discover the path the party is taking if we haven't already discovered it..
			if (party.size() > 1)
				for(CachedEntity member:party)
					if (GameUtils.equals(member.getKey(), character.getKey())==false)
						doCharacterDiscoverEntity(db, member, path);
		
			// Check if this property is locked and if so, if we have the key to enter it...
			movementService.checkForLocks(character, path, destinationKey);
		}

		// Blockade needs to happen on TakePath
		BlockadeService bs = new BlockadeService(this);
		// Check if we're being blocked by the blockade
		EngageBlockadeOpponentResult opponentResult = null; 
		CachedEntity blockadeStructure = bs.getBlockadeFor(character, destination);
		
		if (isInParty && blockadeStructure!=null)
			throw new UserErrorMessage("You are approaching a defensive structure but you cannot attack as a party. Disband your party before attacking the defensive structure.");
		
		if (isInParty && "Instance".equals(destination.getProperty("combatType")))
			throw new UserErrorMessage("You are approaching an instance but cannot attack as a party. Disband your party before attacking the instance (you can still do it together, just not using party mechanics).");
		
		if (isInParty && "TRUE".equals(character.getProperty("partyLeader"))==false)
			throw new UserErrorMessage("You cannot move the party because you're not the party leader.");
		
		if (allowAttack==false && blockadeStructure!=null)
			throw new UserErrorMessage("You are approaching a defensive structure which will cause you to enter into combat with whoever is defending the structure. Are you sure you want to approach?<br><br><a onclick='closeAllPopups();doGoto(event,"+path.getKey().getId()+",true)'>Click here to attack!</a>", false);
		
		boolean playerSetLocation = false;
		if(isInParty == false)
		{
			// Here is where we'll check if we're entering combat with a defending player...
			System.out.println("");
			if (blockadeStructure!=null)
				opponentResult = bs.engageBlockadeOpponent(character.getKey(), currentLocationKey, destination, (Key)blockadeStructure.getProperty("locationKey"), blockadeStructure);
			
			if (opponentResult==null || opponentResult.freeToPass)
			{
				// There are no opponents at all so allow the player to advance
				playerSetLocation = true;
			}
			else if (opponentResult.defender!=null)
			{
				// We're engaging the enemy! We need to get out of here
				return null;
			}
			else if (opponentResult.defender==null && opponentResult.freeToPass==false && opponentResult.onlyNPCDefenders==true)
			{
				// There are no opponents available AND all opponents are NPC opponents, so we will let the player pass
				playerSetLocation = true;
			}
			else if (opponentResult.defender==null && opponentResult.freeToPass==false && opponentResult.onlyNPCDefenders==false)
			{
				// This situation occurs when all the defenders are busy, the player has to wait for combat
				throw new UserErrorMessage("There is active combat going on at this site but you have no room to engage and cannot pass. Try again later.", false);
			}
			else
				throw new RuntimeException("Unhandled situation exception.");
		}
		
		if(CommonChecks.checkLocationIsInstance(destination))
			resetInstanceRespawnTimer(destination);
		
		boolean isCombatSite = CommonChecks.checkLocationIsCombatSite(destination);
		// Only engage if it's done via explore or we're travelling to combat site
		if(isExplore || isCombatSite)
		{
			// Now determine if the path contains an NPC that the character would immediately enter battle with...
			List<CachedEntity> npcsInTheArea = query.getFilteredList("Character", 500, null, "locationKey", FilterOperator.EQUAL, destinationKey, "type", FilterOperator.EQUAL, "NPC");
			npcsInTheArea = new ArrayList<CachedEntity>(npcsInTheArea);

			shuffleCharactersByAttackOrder(npcsInTheArea);
			for(CachedEntity possibleNPC:npcsInTheArea)
				if ((isCombatSite || CommonChecks.checkCharacterIsRaidBoss(possibleNPC) || CommonChecks.checkCharacterIsInCombat(possibleNPC)==false) && 
						(Double)possibleNPC.getProperty("hitpoints")>0d)
				{
					character.setProperty("mode", CHARACTER_MODE_COMBAT);
					character.setProperty("combatant", possibleNPC.getKey());
					possibleNPC.setProperty("mode", CHARACTER_MODE_COMBAT);
					
					if(isCombatSite)
					{
						// In combat sites, every member fights the same mob.
						setPartiedField(party, character, "mode", CHARACTER_MODE_COMBAT);
						setPartiedField(party, character, "combatant", possibleNPC.getKey());
					}
					else if(CommonChecks.checkCharacterIsRaidBoss(possibleNPC)==false)
					{
						// Otherwise, need to see if it's 1v1 combat.
						// Non-combat site and non-raid boss is 1v1.
						possibleNPC.setProperty("combatType", "DefenceStructureAttack");
						possibleNPC.setProperty("combatant", character.getKey());
						// Also need to make sure we don't set the location.
						playerSetLocation = false;
					}
					ds.put(possibleNPC);
					break;
				}
			
			if(isCombatSite || CommonChecks.checkCharacterIsInCombat(character) == false) playerSetLocation = true;
		}
		else
			playerSetLocation = true;
		
		if(playerSetLocation)
		{
			setPartiedField(party, character, "locationKey", destinationKey);
			setPartiedField(party, character, "locationEntryDatetime", new Date());
			if("Town".equals(destination.getProperty("type")))
				setPartiedField(party, character, "homeTownKey", destinationKey);
		}
		
		// Now check if we have a discovery for this path we just took...
		if (path!=null)
		{
			CachedEntity discovery = getDiscoveryByEntity(character.getKey(), path.getKey()); 
			if (discovery==null)
			{
				newDiscovery(db, character, path);
			}
			else
			{
				if (GameUtils.booleanEquals(discovery.getProperty("hidden"), true))
				{
					discovery.setProperty("hidden", false);
					db.put(discovery);
				}
			}
			
			// HACK: We didn't always save the PlayerHouse Paths against users. If this PlayerHouse path doesn't have a user assigned to it, assign it to us
			if ("PlayerHouse".equals(path.getProperty("type")) && 
					(path.getProperty("ownerKey")==null || 
					("Town".equals(destination.getProperty("type"))==false && destination.getProperty("ownerKey")==null)))
			{
				CachedEntity user = getUserByCharacterKey(character.getKey());
				if (user!=null)
				{
					path.setProperty("ownerKey", user.getKey());
					destination.setProperty("ownerKey", user.getKey());
					
					db.put(path);
					// Note: The destination location gets saved later
				}
			}
			// HACK: We didn't always set the player house to be indoors and include the ability to make a fire. Now we do.
			if ("PlayerHouse".equals(path.getProperty("type")) && 
					"FALSE".equals(destination.getProperty("isOutside"))==false &&
					"RestSite".equals(destination.getProperty("type")))
			{
				destination.setProperty("isOutside", "FALSE");
				destination.setProperty("supportsCampfires", 1L);
			}
		}
		
		// Here we're going to take a list we got from the opponentResult stuff and reuse it for performance reasons...
		// We're going to determine if we should refresh the leader on the defence structure we just arrived at or left from
		if (opponentResult!=null && opponentResult.charactersInBlockade!=null)
		{
			for(int i = 0; i<opponentResult.charactersInBlockade.size(); i++)
			{
				if (opponentResult.charactersInBlockade.get(i).getKey().getId() == character.getKey().getId())
				{
					opponentResult.charactersInBlockade.set(i, character);
					break;
				}
			}
			
			// Check if we are entering a defence structure location
			if (blockadeStructure != null)
			{
				refreshDefenceStructureLeader(db, blockadeStructure, opponentResult.charactersInBlockade);
			}
		}

		// Lets just go ahead and reset the status field for the character here (this relates to defence structures)
		//character.setProperty("status", null);
		
		doCharacterTimeRefresh(db, character);	// This is saving the character so no need to save after this
		
		putPartyMembersToDB_SkipSelf(db, party, character);
		
		if (destination.isUnsaved())
			ds.put(destination);
		
		// Now remove all guard settings since we moved
		GuardService gService = new GuardService(this);
		if (party!=null && party.isEmpty()==false)
		{
			for(CachedEntity c:party)
				gService.deleteGuardSettings(c.getKey());
		}
		else
			gService.deleteGuardSettings(character.getKey());
		
		// Send main-page update for all party members but the leader.
		if(partyKeys.isEmpty()==false)
			queueMainPageUpdateForCharacters(partyKeys, "updateFullPage_shortcut");
		
		
		
		
		
		return destination;
	}
	
	public class EngageBlockadeOpponentResult
	{
		public CachedEntity defender;
		public boolean freeToPass;
		public Boolean hasDefenders;
		public boolean onlyNPCDefenders;
		public List<CachedEntity> charactersInBlockade;	// This is a performance related field so we don't have to fetch this list again for other reasons
		
	}
	/**
	 * This is a placeholder because the actual implementation is not in the ODP.
	 * @param db
	 * @param key
	 * @param currentLocationKey
	 * @param destination
	 * @param property
	 * @param blockadeStructure
	 * @return
	 */
	public EngageBlockadeOpponentResult engageBlockadeOpponent(CachedDatastoreService db, Key key, Key currentLocationKey, CachedEntity destination, Key property, CachedEntity blockadeStructure)  throws UserErrorMessage
	{
		return null;
	}

	
	/**
	 * Checks if it's time to delete a combat site based on some preset rule.
	 * 
	 * Currently a combat site gets deleted if it's "lastUsedDate" was set 48 hours ago. 
	 * This date is reset any time a character enters the combat site.
	 * 
	 * @param combatSiteLocation
	 * @returnf
	 */
	public boolean isTimeToDeleteCombatSite(CachedEntity combatSiteLocation)
	{
		Date lastUsedDate = (Date)combatSiteLocation.getProperty("lastUsedDate");
		if (lastUsedDate==null)
		{
			lastUsedDate = new Date();
			combatSiteLocation.setProperty("lastUsedDate", lastUsedDate);
			ds.put(combatSiteLocation);
		}
		
		// If it has been less than the amount of time we want to wait before collapsing, lets get out of here
		if (GameUtils.elapsed(lastUsedDate, Calendar.HOUR)<48)
			return false;
		
		return true;
	}
	
	/**
	 * This is a placeholder because the actual implementation is not in the ODP.
	 * 
	 * @param ds
	 * @param currentCharacter
	 * @param owningEntityKey
	 */
	public void doDeleteCombatSite(CachedDatastoreService ds, CachedEntity playerCharacter, Key locationKey)
	{
		doDeleteCombatSite(ds, playerCharacter, locationKey, false, false);
	}
	
	/**This will need to be updated as we add more stuff.
	 * We already need to delete items contained by monsters and items on the ground.
	 * 
	 * @param monster
	 */
	public void doDeleteCombatSite(CachedDatastoreService ds, CachedEntity playerCharacter, Key locationKey, Boolean leaveAndForget, boolean forceDelete)
	{
		if (ds==null)
			ds = getDB();
		
		// Check if the player character is standing in this location, if so get out!
		if (playerCharacter!=null && playerCharacter.getProperty("locationKey").equals(locationKey))
			throw new IllegalArgumentException("Not allowed to delete a combat site that still has the player character in it.");
		
		
		CachedEntity location = getEntity(locationKey);
		if (location==null)
			return;
		if ("CombatSite".equals(location.getProperty("type"))==false)
			return;
		
		List<CachedEntity> characters = query.getFilteredList("Character", "locationKey", locationKey);
		List<Key> paths = getPathsByLocation_KeysOnly(locationKey);
		List<CachedEntity> discoveries = new ArrayList<CachedEntity>();
		if (playerCharacter!=null)
			discoveries = getDiscoveriesForCharacterAndLocation(playerCharacter.getKey(), locationKey, true);
		List<CachedEntity> items = query.getFilteredList("Item", "containerKey", locationKey);
		

		// Here is a special case:
		// 1. If the monster is dead
		// 2. If the monster has no money on him anymore
		// 3. If the player chose "leave and forget"
		// 4. Site has no characters.
		// Then we will force delete the site
		if (leaveAndForget && forceDelete==false && characters.size()<=1 && paths.size()==1)
		{
			CachedEntity monster = null;
			
			if(characters.isEmpty()==false)
				monster = characters.get(0);
			
			if (monster == null || ("NPC".equals(monster.getProperty("type")) &&
					(Double)monster.getProperty("hitpoints")<1 &&
					(long)monster.getProperty("dogecoins")==0))
			{
				forceDelete = true;
			}
		}
		
//		boolean hasPlayer = false;
//		boolean hasNPCWeWantToKeep = false;
//		for(CachedEntity character:characters)
//		{
//			if (character.getProperty("type")==null || character.getProperty("type").equals("NPC")==false)
//				hasPlayer = true;
//			
//			if ("NPC".equals(character.getProperty("type")) && (Double)character.getProperty("hitpoints")>0d && GameUtils.equals(character.getProperty("hitpoints"), character.getProperty("maxHitpoints"))==false)
//				hasNPCWeWantToKeep=true;
//			
//		}
		
		
		// Having the paths size>1 cancels the deletion of the combat site. This is very important so that people don't get
		// stranded in rare cases where there are other sites branching from this one. The outer branches must be deleted first, 
		// but we can do that lazily.
		if ((paths.size()>1 && forceDelete==false) ||
				isTimeToDeleteCombatSite(location)==false && forceDelete==false) 
				
		{
			// Delete the discoveries of the paths to this location
			for(CachedEntity discovery:discoveries)
			{
				for(Key path:paths)
				{
					Key discoveryEntityKey = (Key)discovery.getProperty("entityKey");
					if (discoveryEntityKey.getKind().equals(path.getKind()) && discoveryEntityKey.getId()==path.getId())
					{
						discovery.setProperty("hidden", true);
						ds.put(discovery);
					}
				}
				
				// Right now discoveries are only used for paths. More may need to be handled later on.
			}
			
			return;
		}
		else
		{
			
			
			// Delete all characters standing in this place (should only be NPCs)
			for(CachedEntity character:characters)
				if ("NPC".equals(character.getProperty("type")))
					ds.delete(character.getKey());
				else
				{
					Key parentLocation = getParentLocationKey(ds, location);
					character.setProperty("locationKey", parentLocation);
					character.setProperty("locationEntryDatetime", new Date());
					ds.put(character);
				}
					
			
			// Delete all items on the ground
			for(CachedEntity item:items)
				ds.delete(item.getKey());
			
			// Delete all paths to this location
			for(Key path:paths)
				ds.delete(path);
			
			// Delete the discoveries of the paths to this location
			for(CachedEntity discovery:discoveries)
			{
				if (discovery==null)
					continue;
				for(Key path:paths)
				{
					Key discoveryEntityKey = (Key)discovery.getProperty("entityKey");
					if (discoveryEntityKey.getKind().equals(path.getKind()) && discoveryEntityKey.getId()==path.getId())
						ds.delete(discovery.getKey());
				}
				
				// Right now discoveries are only used for paths. More may need to be handled later on.
			}
			
			// Finally delete the location itself
			ds.delete(locationKey);
			
			
		}
	}
	
	/**
	 * Instead of deleting the combat site and everything in it, it will move any characters or items to the combat site's parent location
	 * and only delete the combat site's paths and the combat site location itself, once that's done.
	 * @param ds
	 * @param playerCharacter
	 * @param location
	 * @param forceDelete
	 */
	public void doCollapseCombatSite(CachedDatastoreService ds, CachedEntity playerCharacter, CachedEntity location, CachedEntity backupParentLocation)
	{
		if (ds==null)
			ds = getDB();

		Key locationKey = location.getKey();
		
		if ("CombatSite".equals(location.getProperty("type"))==false)
			return;
		
		// Check that the location is older than a number of days before allowing this
		Date createdDate = (Date)location.getProperty("createdDate");
		
		if (createdDate==null)
			return;

		// If it has been less than the amount of time we want to wait before collapsing, lets get out of here
		if (GameUtils.elapsed(createdDate, Calendar.HOUR)<24)
			return;
		
		CachedEntity parentLocation = getParentLocation(ds, location);
		
		if (parentLocation==null)
			parentLocation = backupParentLocation;
		
		List<CachedEntity> characters = query.getFilteredList("Character", "locationKey", locationKey);
		List<Key> paths = getPathsByLocation_KeysOnly(locationKey);
		List<CachedEntity> discoveries = new ArrayList<CachedEntity>();
		if (playerCharacter!=null)
			discoveries = getDiscoveriesForCharacterAndLocation(playerCharacter.getKey(), locationKey, true);
		List<CachedEntity> items = query.getFilteredList("Item", "containerKey", locationKey);
		

//		boolean hasLiveNpc = false;
//		boolean hideOnly = false;
//		for(CachedEntity character:characters)
//		{
//			if (character.getProperty("type")==null || character.getProperty("type").equals("NPC")==false)
//				hideOnly = true;
//			else if ("NPC".equals(character.getProperty("type")) && (Double)character.getProperty("hitpoints")>0d)
//				hasLiveNpc=true;
//		}
		
		
		// Having the paths size>1 cancels the collapse of the combat site. This is very important so that people don't get
		// stranded in rare cases where there are other sites branching from this one. The outer branches must be deleted first, 
		// but we can do that lazily.
		if (paths.size()>1) 
		{
			// We'll just leave this one for now
			return;
		}
		else
		{
			
			
			// Move all characters standing in this place (should only be NPCs)
			for(CachedEntity character:characters)
			{
				character.setProperty("locationKey", parentLocation.getKey());
				character.setProperty("locationEntryDatetime", new Date());
				ds.put(character);
			}
			
			// Move all items on the ground
			for(CachedEntity item:items)
			{
				item.setProperty("containerKey", parentLocation.getKey());
				item.setProperty("movedTimestamp", new Date());
				ds.put(item);
			}
			
			// Delete all paths to this location
			for(Key path:paths)
				ds.delete(path);
			
			// Delete the discoveries of the paths to this location
			for(CachedEntity discovery:discoveries)
			{
				if (discovery==null)
					continue;
				for(Key path:paths)
				{
					Key discoveryEntityKey = (Key)discovery.getProperty("entityKey");
					if (discoveryEntityKey.getKind().equals(path.getKind()) && discoveryEntityKey.getId()==path.getId())
						ds.delete(discovery.getKey());
				}
				
				// Right now discoveries are only used for paths. More may need to be handled later on.
			}
			
			// Finally delete the location itself
			ds.delete(locationKey);
			
			
		}
	}
	
	

	/**
	 * Determines the parent location for the given location. This is used for certain 
	 * game mechanics that require a parent/child concept. For example, running will always cause you 
	 * to run to the parent location.
	 *  
	 * @param ds
	 * @param location
	 * @return The location that is considered to be the parent of the given location.
	 */
	public CachedEntity getParentLocation(CachedDatastoreService ds, CachedEntity location)
	{
		return getEntity(getParentLocationKey(ds, location));
	}

	
	/**
	 * Determines the parent location for the given location. This is used for certain 
	 * game mechanics that require a parent/child concept. For example, running will always cause you 
	 * to run to the parent location.
	 *  
	 * @param ds
	 * @param location
	 * @return The location that is considered to be the parent of the given location.
	 */
	public Key getParentLocationKey(CachedDatastoreService ds, CachedEntity location)
	{
		if (location.getProperty("parentLocationKey")==null)
		{
			// FOR LAZY MIGRATION OF THE DATABASE, KEEP THIS PART ACTIVE.
			List<CachedEntity> paths = getPathsByLocation(location.getKey());
			
			if (paths.isEmpty())
				return null;	// This should never happen, but basically there is no escape from this combat site. Let's not throw here though.
			
			CachedEntity path = paths.get(0);
			Key properLocationKey = null;
			if (GameUtils.equals(path.getProperty("location1Key"), location.getKey()))
				properLocationKey = (Key)path.getProperty("location2Key");
			else
				properLocationKey = (Key)path.getProperty("location1Key");
			
			location.setProperty("parentLocationKey", properLocationKey);
			if (ds==null) ds = getDB();
			ds.put(location);
 			
			Logger.getLogger("GameFunctions").log(Level.SEVERE, "Parent location on a "+location.getProperty("type")+" was not set properly. - We decided to associate it."); 
			
			return properLocationKey;
		}
		else
		{
			Key key = (Key)location.getProperty("parentLocationKey");
			return key;
		}
	}

	
	
	
	
	/**
	 * If we return true, it's because the escape was successful and the
	 * character will have already been placed in the new location.
	 * 
	 * @param character
	 * @param monster
	 * @return
	 * @throws UserErrorMessage 
	 */
	public boolean doCharacterAttemptEscape(CachedEntity characterLocation, CachedEntity character, CachedEntity monster) throws UserErrorMessage
	{
		CachedDatastoreService db = getDB();
//		db.beginTransaction();
		
		try
		{
			String mode = (String)character.getProperty("mode");
			
			if (mode==null || mode.equals(ODPDBAccess.CHARACTER_MODE_COMBAT)==false)
				throw new UserErrorMessage("Character must be in combat in order to attempt to escape.");
			
			if (character.getProperty("partyCode")!=null && character.getProperty("partyCode").equals("")==false)
			{
				if ("TRUE".equals(character.getProperty("partyLeader"))==false)
					throw new UserErrorMessage("You are not the party leader and therefore cannot choose to have the party run from the fight. If you want to run anyway, you will have to leave your party first.");
			}

			// Here we're flagging that a combat action took place so that the cron job in charge of ensuring combat keeps moving along
			// doesn't automatically attack for us
			if ("PC".equals(character.getProperty("type")) && "PC".equals(monster.getProperty("type")))
				flagCharacterCombatAction(db, character);
			
			
			Double characterDex = getCharacterDexterity(character);
			Double monsterDex = getCharacterDexterity(monster);
			
			
			Random rnd = new Random();
			if (rnd.nextDouble()*characterDex>rnd.nextDouble()*monsterDex || GuardService.checkIfGuardWantsToRun(monster))
			{
				List<CachedEntity> party = getParty(db, character);
				
				
				
				
				
				// Now we want to check if the monster should regain hitpoints or not
				// Hitpoints should be regenerated if the monster is in a rest area 
				CachedEntity monsterLocation = getEntity((Key)monster.getProperty("locationKey"));

				// Here we heal the defender but when running from RaidBoss, don't want them to heal.
				if (CommonChecks.checkCharacterDefenderToAutoHeal(monsterLocation, monster, characterLocation, character))
				{
					Double hitpoints = (Double)monster.getProperty("hitpoints");
					Double maxHitpoints = (Double)monster.getProperty("maxHitpoints");
					Double guardStartHP = (Double)monster.getProperty("guardStartHP");
					if (guardStartHP!=null && hitpoints<guardStartHP)
					{
						monster.setProperty("hitpoints", guardStartHP);
						monster.setProperty("guardStartHP", null);
					}
					else if (hitpoints<maxHitpoints)
					{
						monster.setProperty("hitpoints", maxHitpoints);
					}
					
					monster.setProperty("mode", CHARACTER_MODE_NORMAL);
					monster.setProperty("combatant", null);
					monster.setProperty("combatType", null);
					
					if (GuardService.checkIfGuardWantsToRun(monster))
					{
						GuardService gs = new GuardService(this);
						gs.doRunAndStopGuarding(monster, monsterLocation.getKey());
					}
					
					db.put(monster);

					setPartiedField(party, character, "mode", CHARACTER_MODE_NORMAL);
					setPartiedField(party, character, "combatant", null);
					setPartiedField(party, character, "combatType", null);
					putPartyMembersToDB(db, party, character);

					// now notify the party members to refresh
					if (party!=null)
						for(CachedEntity member:party)
							if (member.getKey().getId()!=character.getKey().getId())
								queueMainPageUpdateForCharacter(member.getKey(), "updateFullPage_shortcut");
					
				}
				else
				{
					setPartiedField(party, character, "mode", CHARACTER_MODE_NORMAL);
					setPartiedField(party, character, "combatant", null);
					setPartiedField(party, character, "combatType", null);
					putPartyMembersToDB(db, party, character);

					// now notify the party members to refresh
					if (party!=null)
						for(CachedEntity member:party)
							if (member.getKey().getId()!=character.getKey().getId())
								queueMainPageUpdateForCharacter(member.getKey(), "updateFullPage_shortcut");

					// First lets take the monster out of combat
					monster.setProperty("mode", CHARACTER_MODE_NORMAL);
					monster.setProperty("combatant", null);
					monster.setProperty("combatType", null);
					
					db.put(monster);					
					
					// Move locations! But only if we're in the same location as the monster..
					if (GameUtils.equals(monster.getProperty("locationKey"), character.getProperty("locationKey")))
					{
						// Don't discover the path to the combat area, we shouldn't be able to remember how to get back there
						// Take the path back to the source location. First we need to find the path..
						List<CachedEntity> paths = getPathsByLocation((Key)character.getProperty("locationKey"));
						CachedEntity parentLocation = getParentLocation(db, monsterLocation);
					
					
						// Escape down the parent location if one exists
						if (parentLocation!=null)
						{
							for(CachedEntity path:paths)
								if (((Key)path.getProperty("location1Key")).getId()==parentLocation.getKey().getId() ||
										((Key)path.getProperty("location2Key")).getId()==parentLocation.getKey().getId())
								{
									doCharacterTakePath(db, character, path, true);
									return true;
								}
						}
	
						// Now shuffle the paths, looks like the parent location wasn't set right or at all
						Collections.shuffle(paths);
						
						// Otherwise, escape down the first permanent location we find
						for(CachedEntity path:paths)
							if ("Permanent".equals(path.getProperty("type")))
							{
								doCharacterTakePath(db, character, path, true);
								return true;
							}
						
		
						// If we're here, we didn't go down ANY path, so just go down the first one then...
						if (paths.isEmpty())
						{
							if (parentLocation==null)
							{
								character.setProperty("locationKey", getDefaultLocationKey());
							}
							else
								character.setProperty("locationKey", parentLocation.getKey());
						}
						else
							doCharacterTakePath(db, character, paths.get(0), true);
					}
				}				

				
				
				queueMainPageUpdateForCharacter(monster.getKey(), "updateFullPage_shortcut");
				
				
				return true;
			}
			else
			{
				
				
//				db.commit();
				return false;
			}
		}
		finally
		{
//			db.rollbackIfActive();
		}
	}

	
	public String doMonsterCounterAttack(ODPAuthenticator auth, CachedEntity user, CachedEntity monster, CachedEntity character)
	{
		// Decide what weapon to use...
		CachedEntity weaponToUse = null;
		CachedEntity leftHand = null;
		CachedEntity rightHand = null;
		
		if ((Double)monster.getProperty("hitpoints")>0)
		{
			leftHand = getEntity((Key)monster.getProperty("equipmentLeftHand"));
			rightHand = getEntity((Key)monster.getProperty("equipmentRightHand"));
			
			// Here we're choosing the weapon to use as the left hand by default, if it is a weapon
			if (leftHand!=null)
			{
				String itemType = (String)leftHand.getProperty("itemType");
				if (itemType!=null && itemType.equals("Weapon"))
					weaponToUse = leftHand;
			}
			
			// Here we're getting the right hand item (if it's a weapon) and choosing a weapon if we're dual wielding
			if (rightHand!=null)
			{
				String itemType = (String)rightHand.getProperty("itemType");
				if (itemType!=null && itemType.equals("Weapon"))
					if (weaponToUse!=null)
					{
						// Both hands have weapons, choose randomly between them by default or if automaticWeaponChoiceMethod==Random
						if (GameUtils.enumEquals(monster.getProperty("automaticWeaponChoiceMethod"), AutomaticWeaponChoiceMethod.HighestDamage) || 
								GameUtils.enumEquals(monster.getProperty("type"), CharacterType.PC) && (monster.getProperty("automaticWeaponChoiceMethod")==null || ((String)monster.getProperty("automaticWeaponChoiceMethod")).trim().equals("")))
						{
							Double maxDamageLeftHand = GameUtils.getWeaponMaxDamage(leftHand);
							Double maxDamageRightHand = GameUtils.getWeaponMaxDamage(rightHand);
							
							if ((maxDamageLeftHand!=null && maxDamageRightHand!=null && maxDamageLeftHand>maxDamageRightHand))
								weaponToUse = leftHand;
							else
								weaponToUse = rightHand;
							
						}
						else
						{
							if (new Random().nextBoolean())
								weaponToUse = leftHand;
							else
								weaponToUse = rightHand;
						}
					}
					else
						weaponToUse = rightHand;
			}
		
			return doCharacterAttemptAttack(auth, user, monster, weaponToUse, character);
		}		
		else
		{
			return monster.getProperty("name")+" is dead.";
		}
	}
	
	public CachedEntity getCharacterStrongestWeapon(CachedEntity character)
	{
		CachedEntity result = null;
		
		CachedEntity leftHand = getEntity((Key)character.getProperty("equipmentLeftHand"));
		CachedEntity rightHand = getEntity((Key)character.getProperty("equipmentRightHand"));
		
		if (leftHand!=null && rightHand!=null)
		{
			String leftType = (String)leftHand.getProperty("type");
			String rightType = (String)rightHand.getProperty("type");
			
			if (leftType==null)leftType = "";
			if (rightType==null) rightType = "";
			
			if (leftType.contains("Weapon") && rightType.contains("Weapon"))
			{
				// Now compare the two
				long leftScore = GameUtils.determineQualityScore(leftHand.getProperties());
				long rightScore = GameUtils.determineQualityScore(rightHand.getProperties());
				if (leftScore>rightScore)
					result = leftHand;
				else
					result = rightHand;
			}
			else if (leftType.contains("Weapon"))
				result = leftHand;
			else if (rightType.contains("Weapon"))
				result = rightHand;
		}
		
		return result;
	}

	
	/**
	 * This is a placeholder. The actual implementation is not in the ODP.
	 * @return
	 * @throws NotLoggedInException 
	 */
	public String getVerifyCode() throws NotLoggedInException
	{
		return null;
	}

	/**
	 * THis is a placeholder. The actual implementation is not in the ODP.
	 * @param key
	 * @return
	 */
	public String getChatIdToken(Key key)
	{
		return null;
	}
	
	/**
	 * Returns a usable InvetionService object.
	 * 
	 * The InventionService requires a character so the logged-in user's character will
	 * be used.
	 * 
	 * @return
	 */
	public ODPInventionService getInventionService(CachedEntity character, ODPKnowledgeService knowledgeService)
	{
		return null;
	}

	/**
	 * Returns a usable KnowledgeService object.
	 * 
	 * The KnowledgeService requires a character so the logged-in user's character will
	 * be used.
	 * 
	 * @param characterKey The key of the character we want to work with. Only this character's knowledge will be used.
	 * @return
	 */
	public ODPKnowledgeService getKnowledgeService(Key characterKey)
	{
		return null;
	}

	public void resetInstanceRespawnTimer(CachedEntity location)
	{
		if ("TRUE".equals(location.getProperty("instanceModeEnabled")) && location.getProperty("instanceRespawnDate")==null)
		{
			Date instanceRespawnDate = (Date)location.getProperty("instanceRespawnDate");
			Long instanceRespawnDelay = (Long)location.getProperty("instanceRespawnDelay");
			if (instanceRespawnDate==null && instanceRespawnDelay!=null)
			{
				GregorianCalendar cal = new GregorianCalendar();
				cal.add(Calendar.MINUTE, instanceRespawnDelay.intValue());
				
				location.setProperty("instanceRespawnDate", cal.getTime());
				
			}
		}
	}

	public CachedEntity _newPremiumMembershipToken(Key owningCharacterKey)
	{
		return null;
	}

	public String getNewChatAuthToken(Key key)
	{
		return null;
	}

	public Key decodeAndCheckChatAuthToken(String authToken) throws SecurityException {
		return null;
	}

	public Long getUserCharacterSlotCount(CachedEntity user)
	{
		if (user==null) return 1L;
		Long count = (Long)user.getProperty("maximumCharacterCount");
		if (count==null)
		{
			if (CommonChecks.checkUserIsPremium(user))
				count = 8L;
			else
				count = 1L;
		}
		
		return count;
	}
	
	public List<CachedEntity> getUserCharacters(CachedEntity user)
	{
		List<CachedEntity> chars = getFilteredList("Character", "userKey", user.getKey());
		for(int i = chars.size()-1; i>=0; i--)
		{
			if (CommonChecks.checkCharacterIsDead(chars.get(i)))
				chars.remove(i);
		}
		
		return chars;
	}

	/**
	 * Gets 2D list of keys stored in an Entity2DCollection field type on an entity.
	 * This field type stores it's data in a funny way for indexing purposes, making
	 * this method necessary.
	 * 
	 * @param entity
	 * @param fieldName
	 * @return
	 */
	public List<List<Key>> getEntity2DCollectionValueFromEntity(CachedEntity entity, String fieldName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<List<Object>> get2DCollectionValueFromEntity(CachedEntity entity, String fieldName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Picks up the character, ensuring the user accounts are different,
	 * characters are at the same location, and pickupCharacter is able to
	 * be collected (premium, incapacitated, etc).
	 * 
	 * @param user
	 * @param character
	 * @param pickupChar
	 */
	public void doCharacterCollectCharacter(CachedEntity user, CachedEntity character, CachedEntity characterToBePickedUp) throws UserErrorMessage
    {
        if (character==null)
            throw new IllegalArgumentException("Character cannot be null.");
        if (characterToBePickedUp==null)
            throw new IllegalArgumentException("Character to be picked up cannot be null.");
        
        if (GameUtils.equals(character.getProperty("locationKey"),characterToBePickedUp.getProperty("locationKey"))==false)
            throw new UserErrorMessage("The "+characterToBePickedUp.getProperty("name")+" is not here anymore.");
        
        // Check if the character being picked up is not conscious. If he isn't, we can't pick him up
        if ((Double)characterToBePickedUp.getProperty("hitpoints")>0)
            throw new UserErrorMessage("You can only pick up characters that are unconscious or dead.");
        
        if (user==null || Boolean.TRUE.equals((Boolean)user.getProperty("premium"))==false)
            throw new UserErrorMessage("This is a premium member feature. You cannot pick someone up unless you're a premium member.");

        if ("NPC".equals(characterToBePickedUp.getProperty("type"))==false)
        {
        	if (GameUtils.equals(user.getKey(), characterToBePickedUp.getProperty("userKey")))
        		throw new UserErrorMessage("You cannot use one of your own characters to pick up another one of your characters. It needs to be another player!");
        			
            CachedEntity userOfCharacterToBePickedUp = getEntity((Key)characterToBePickedUp.getProperty("userKey"));
            if (userOfCharacterToBePickedUp==null || Boolean.TRUE.equals((Boolean)userOfCharacterToBePickedUp.getProperty("premium"))==false)
                throw new UserErrorMessage("You cannot pick up an unconsious person unless THEY are a premium member.");
        }
        
        // Check if the character can actually carry something else or if its all too heavy...
        Long newItemWeight = getCharacterWeight(characterToBePickedUp);
        if (newItemWeight!=null && newItemWeight>0d)
        {
            long carrying = getCharacterCarryingWeight(character);
            long maxCarrying = getCharacterMaxCarryingWeight(character);
            
            if (carrying+newItemWeight>maxCarrying)
                throw new UserErrorMessage("You cannot carry "+characterToBePickedUp.getProperty("name")+"! You are currently carrying "+GameUtils.formatNumber(carrying)+" grams and can carry a maximum of "+GameUtils.formatNumber(maxCarrying)+" grams.");
        }
        
        
        characterToBePickedUp.setProperty("locationKey", character.getKey());
        characterToBePickedUp.setProperty("movedTimestamp", new Date());
        
        getDB().put(characterToBePickedUp);
    }

	/**This tells all active characters in a given location to update their page with the given updateMethods (the names of the methods).
	 * 
	 * Note, this only works for update methods that take no arguments.
	 * 
	 * @param locationKey The location where the update should trigger (all active characters in this location)
	 * @param ds
	 * @param updateMethods The list of method names in the MainPageUpdateService you wish to trigger (arg-less methods only).
	 */
	public void sendMainPageUpdateForLocation(Key locationKey, CachedDatastoreService ds, String...updateMethods)
	{
		// TODO Auto-generated method stub
		
	}

	/**This tells all characters given via the characterKeys param to update their page with the given updateMethods (the names of the methods).
	 * 
	 * Note, this only works for update methods that take no arguments.
	 * 
	 * @param ds
	 * @param characterKeys The characters to send the update to
	 * @param updateMethods The list of method names in the MainPageUpdateService you wish to trigger (arg-less methods only).
	 */
	public void sendMainPageUpdateForCharacters(CachedDatastoreService ds, Collection<Key> characterKeys, String...updateMethods)
	{
		// TODO Auto-generated method stub
		
	}

	/**This tells all characters given via the characterKeys param to update their page with the given updateMethods (the names of the methods).
	 * 
	 * Note, this only works for update methods that take no arguments.
	 * 
	 * @param ds
	 * @param characterKeys The characters to send the update to
	 * @param updateMethods The list of method names in the MainPageUpdateService you wish to trigger (arg-less methods only).
	 */
	public void sendMainPageUpdateForCharacter(CachedDatastoreService ds, Key characterKey, String...updateMethods)
	{
		// TODO Auto-generated method stub
		
	}
	
	public void queueMainPageUpdateForCharacters(Collection<Key> characterKeys, String...updateMethods)
	{
		if (characterKeys==null) return;
		
		for(Key charKey:characterKeys)
		{
			if (GameUtils.equals(charKey, getCurrentCharacterKey()))
				continue;
			queueMainPageUpdateForCharacter(charKey, updateMethods);
		}
	}
	
	public void queueMainPageUpdateForCharacter(Key characterKey, String...updateMethods)
	{
		if (isLoggedIn(getRequest())==false) return;	// It's ok to do this when we're not logged in because cron job
		if (GameUtils.equals(characterKey, getCurrentCharacterKey()))
			return;
		
		if (mpusSendQueue==null) mpusSendQueue = new HashMap<>();
		Set<String> uMethods = mpusSendQueue.get(characterKey);
		if (uMethods==null) 
		{
			uMethods = new HashSet<String>();
			mpusSendQueue.put(characterKey, uMethods);
		}
		
		uMethods.addAll(Arrays.asList(updateMethods));
	}
	
	public void sendMainPageUpdateQueue(CachedDatastoreService ds)
	{
		if (mpusSendQueue==null) return;
		
		for(Key charKey:mpusSendQueue.keySet()){
			if (mpusSendQueue.get(charKey)==null) continue;
			Set<String> uMethods = mpusSendQueue.get(charKey);
			sendMainPageUpdateForCharacter(ds, charKey, uMethods.toArray(new String[uMethods.size()]));
		}
		
		mpusSendQueue.clear();
	}

	public void sendSoundEffectToCharacters(CachedDatastoreService ds, Collection<Key> characterKeys, String...soundEffects)
	{
		// TODO Auto-generated method stub
		
	}

	public void sendSoundEffectToCharacter(CachedDatastoreService ds, Key characterKey, String...soundEffects)
	{
		// TODO Auto-generated method stub
		
	}

	public void sendSoundEffectToLocation(CachedDatastoreService ds, Key locationKey, String...soundEffects)
	{
		// TODO Auto-generated method stub
		
	}

	public void sendSystemMessageToLocation(Key locationKey, String message)
	{
		// TODO Auto-generated method stub
		
	}

	public Double getMonsterCountForLocation(CachedDatastoreService db, CachedEntity location)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void sendGameMessage(String message)
	{
		sendGameMessage(getDB(), getCurrentCharacter(), message);
	}
	
	public void sendGameMessage(CachedDatastoreService ds, CachedEntity character, String message)
	{
		// TODO Auto-generated method stub
		
	}
	
	public void sendGameMessage(CachedDatastoreService ds, Key entityKey, String message) {
		// TODO Auto-generated method stub
		
	}

	public boolean hasNewGameMessages()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public CachedEntity newAchievement(CachedDatastoreService db, String title, String description, String icon, Long pointValue)
	{
		if (db == null) db = getDB();

		CachedEntity achievement = new CachedEntity("Achievement");
		// Set the achievement fields
		achievement.setProperty("title", title);
		achievement.setProperty("description", description);
		achievement.setProperty("icon", icon);
		achievement.setProperty("pointValue", pointValue);

		db.put(achievement);
		return achievement;
	}

	public Map<String, String> getValue_StringStringMap(CachedEntity entity, String fieldName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setValue_StringStringMap(CachedEntity entity, String fieldName, Map<String, String> value)
	{
		// TODO Auto-generated method stub
		
	}
	
	public boolean combineStackedItemWithFirstStack(CachedEntity stackedItem, Key characterKey)
	{
		if (stackedItem.getProperty("quantity")==null) return false;
		
		List<CachedEntity> inventory = query.getFilteredList("Item", "containerKey", characterKey, "name", stackedItem.getProperty("name"));
		for(CachedEntity item:inventory)
		{
			if (item!=null && CommandItemsStackMerge.canStack(stackedItem, item))
			{
				Long quantity = (Long)item.getProperty("quantity");
				quantity+=(Long)stackedItem.getProperty("quantity");
				
				stackedItem.setProperty("quantity", quantity);
				if (item.getKey().isComplete())
					ds.delete(item);
				ds.put(stackedItem);
				
				return true;
			}
		}
		
		return false;
	}
	
	public void doDeleteCharacter(Key key)
	{
		CachedDatastoreService db = getDB();
		
		doDeleteCharacterDiscoveries(db, key);
		
		db.delete(key);
	}
	
	public void doDeleteCharacterDiscoveries(CachedDatastoreService db, Key key)
	{
		if(db == null) db = getDB();

		Query q = new Query("Discovery").setFilter(new FilterPredicate("characterKey", FilterOperator.EQUAL, key)).setKeysOnly();
		Iterable<CachedEntity> entities = db.fetchAsIterable(q);
		
		List<Key> discoveryKeys = new ArrayList<Key>();
		if(entities != null)
			for(CachedEntity entity:entities)
				discoveryKeys.add(entity.getKey());
		
		db.delete(discoveryKeys);
	}
	
    public CachedEntity newPlayerCharacter(CachedDatastoreService ds, ODPAuthenticator auth, CachedEntity user, String name, CachedEntity oldCharacter)
    {
        if (ds==null)
            ds = getDB();
        
        CachedEntity group = null;
        
        name = name.replaceAll("\\s{2,}", " ").trim();
        
        Key homeTownKey = null;
        
        CachedEntity newCharacter = new CachedEntity("Character", ds.getPreallocatedIdFor("Character"));
    	newCharacter.setProperty("hardcoreMode", name.endsWith("!"));
    	newCharacter.setProperty("hardcoreRank", 0L);
        // Set the starting attributes
        newCharacter.setProperty("name", name.replaceAll("!$", "").trim());
        newCharacter.setProperty("homeTownKey", homeTownKey);

        // Set some default attributes
        if (homeTownKey==null)
            newCharacter.setProperty("locationKey", getDefaultLocationKey());
        else
            newCharacter.setProperty("locationKey", homeTownKey);
        newCharacter.setProperty("dogecoins", 0l);
        newCharacter.setProperty("mode", ODPDBAccess.CHARACTER_MODE_NORMAL);
        newCharacter.setProperty("type", "PC");

        Double dex = 5d;
        Double str = 5d;
        Double intel = 5d;
        
        Double hp = calculateHitpoints(str);
        
        newCharacter.setProperty("strength", str);
        newCharacter.setProperty("dexterity", dex);
        newCharacter.setProperty("intelligence", intel);
        newCharacter.setProperty("hitpoints", hp);
        newCharacter.setProperty("maxHitpoints", hp);
        
        newCharacter.setProperty("createdDate", new Date());
        newCharacter.setProperty("locationEntryDatetime", new Date());
        if (user!=null)
        {
            if (user.getKey().isComplete()==false)
                ds.put(user);
            newCharacter.setProperty("userKey", user.getKey());
        }

        // If there is a previous character, copy over some things from him
        if (oldCharacter!=null)
        {
            Key groupKey = (Key)oldCharacter.getProperty("groupKey");
            group = getEntity(groupKey);
            if (group!=null)
            {
                newCharacter.setProperty("groupKey", oldCharacter.getProperty("groupKey"));
                newCharacter.setProperty("groupRank", oldCharacter.getProperty("groupRank"));
                newCharacter.setProperty("groupStatus", oldCharacter.getProperty("groupStatus"));
                newCharacter.setProperty("leaveGroupDate", oldCharacter.getProperty("leaveGroupDate"));
                
                // ok he was in a group, we'll need to copy some stuff over to the new char
                // If the dead character is the creator of the group, copy over creator status...
                if (((Key)group.getProperty("creatorKey")).getId()==oldCharacter.getKey().getId())
                {
                    // He was the creator, make the new guy the creator now...
                    group.setProperty("creatorKey", newCharacter.getKey());
                    ds.put(group);
                }
                
            }
        }
        else
        {
            // If the new character is null, lets put the new guy through the tutorial
//            newCharacter.setProperty("locationKey", KeyFactory.createKey("Location", 6073730381905920L));
//            newCharacter.setProperty("dogecoins", 1L);
        }

        
        
        if (user!=null)
        {
            user.setProperty("characterKey", newCharacter.getKey());
            
            ds.put(user);
        }
        else
        {
            auth.login(newCharacter); 
        }
        
        return newCharacter;
    }

	public List<List<String>> getValueFromFieldTypeFieldFilter2DCollection(PropertyContainer entity, String fieldName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean validateFieldFilter(CachedEntity entityWithFilterValue, String fieldFilterFieldName, CachedEntity entityToValidate)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public CachedEntity newCustomOrder(CachedEntity finalUser, CachedEntity finalCharacter, CachedEntity finalEntity, Key typeKey, String description, boolean instant) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setOrderComplete(ODPAuthenticator auth, Long orderId) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		
	}

	public String getChatToken()
	{
		return null;
	}

	public void setValue_FieldTypeFieldFilter2DCollection(CachedEntity entity, String fieldName, List<List<String>> value)
	{
		// TODO Auto-generated method stub
		
	}

	public String getChatToken(Key key)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean isLocalhost()
	{
		return getRequest().getServerName().contains("localhost");
	}

	public Collection<String> getFieldNamesForEntity(String kind)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<String> getFieldNamesForAspect(String aspectName)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * This increases the knowledge you have with a piece of equipment (requires an itemClass assigned to it and
	 * for that item class to be created in the database; requires ItemClass entity).
	 * 
	 * The 100 means there is a 1 in 100 chance of getting experience with the equipment, this is to 
	 * reduce load on the DB.
	 * @return 
	 */
	public boolean increaseKnowledgeForEquipment100(CachedEntity equipment)
	{
		return false;
	}

	public String getFieldTypeFor(CachedEntity entity, String fieldName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Calculates the travel time after buffed movement speed.
	 * @param character Character entity we're getting buff movement speed for.
	 * @param travelTime Initial travel time.
	 * @return
	 */
	private Long getBuffedTravelTime(CachedEntity character, Long travelTime)
	{
		// Values are inverse here. Positive flat adjustments subtract from travel
		// time, and percentage adjustments divide the time. An X% increase represents
		// 100 + X decrease in time, so 100% should be 1/2 the time, 200% should 
		// be 1/3 the time, etc. Percentages are multiplicative, not additive, and
		// apply after the flat adjustments.
		
		// Start with the original travel time.
		Double workingAmount = travelTime.doubleValue();
		double modifyFactor = 1.0d;
		List<String> buffs = getBuffEffectsFor(character, "movementSpeed");
		for (String effect : buffs)
		{
			effect = effect.replace("+", "");
			if (effect.endsWith("%"))
			{
				effect = effect.substring(0, effect.length() - 1);
				double val = new Double(effect);
				val = 1.0+(val/100);
				modifyFactor = modifyFactor * val;
			}
			else
			{
				double val = new Double(effect);
				workingAmount = workingAmount - val;
			}
		}
		
		// If it's close to 0, treat it as 0. Otherwise we get
		// a large number, and we clamp value to [0,30].
		if(modifyFactor >= 0.01d) 
			travelTime = Math.round(workingAmount/modifyFactor);

		travelTime = Math.min(Math.max(travelTime, 0), 30);
		return travelTime;
	}
	
	
	public Long getPathTravelTime(CachedEntity path, CachedEntity character)
	{
		Long travelTime = (Long)path.getProperty("travelTime");
		if (travelTime==null)
			travelTime = 6L;
		
		Long buffedTravel = getBuffedTravelTime(character, travelTime);
		return buffedTravel;
	}

	public Key getCharacterLocationKey(CachedEntity character)
	{
		Key locationKey = (Key)character.getProperty("locationKey");
		
		
		if (locationKey==null)
		{
			locationKey = (Key)getCurrentCharacter().getProperty("homeTownKey");
			if (locationKey==null)
				locationKey = getDefaultLocationKey();
			character.setProperty("locationKey", locationKey);
			
			if (GameUtils.equals(character.getKey(), getCurrentCharacterKey()))
				getCurrentCharacter().setProperty("locationKey", locationKey);
		}
		
		return locationKey;
	}

	public CachedEntity getCharacterLocation(CachedEntity character)
	{
		Key locationKey = (Key)character.getProperty("locationKey");
		CachedEntity location = getEntity(locationKey);
		
		
		
		if (locationKey==null || location==null)
		{
			location = getEntity((Key)getCurrentCharacter().getProperty("homeTownKey"));
			if (location==null)
				location = getEntity(getDefaultLocationKey());
			character.setProperty("locationKey", location.getKey());
			
			if (CommonChecks.checkCharacterIsVending(character))
				character.setProperty("mode", null);
			
			if (GameUtils.equals(character.getKey(), getCurrentCharacterKey()))
			{
				getCurrentCharacter().setProperty("locationKey", location.getKey());
				getCurrentCharacter().setProperty("mode", null);
			}
		}
		
		return location;
	}

	public Key stringToKey(String value)
	{
		return null;
	}
	
	public String keyToString(Key value)
	{
		if (value==null) return null;
		return value.toString();
	}

	public ODPAuthenticator getAuthenticator() throws DDOSProtectionException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean applyFieldChanges(CachedEntity entityToChange, EmbeddedEntity changeMap)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public List<Key> getImmovablesKeys(Key locationKey)
	{
		if (isTestServer()==false)	// ANYONE KNOW WHY THIS TAKES SO DAMN LONG????
			return query.getFilteredList_Keys("Item", "immovable", FilterOperator.EQUAL, true, "containerKey", FilterOperator.EQUAL, locationKey);
		else
			return new ArrayList<>(0);
	}

	
	public void commitInventionEntities()
	{
		// TODO Auto-generated method stub
	}
	
	public boolean isKey(String keyString)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public Key keyStringToKey(String keyString)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, Double> getFieldTypeMapEntityDouble(CachedEntity entity, String fieldName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getFieldTypeStringStringMap(CachedEntity entity, String fieldName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setFieldTypeMapEntityDouble(CachedEntity entity, String fieldName, Map<String, Double> value)
	{
		// TODO Auto-generated method stub
		
	}

	public void setFieldTypeStringStringMap(CachedEntity entity, String fieldName, Map<String, String> value)
	{
		// TODO Auto-generated method stub
		
	}

	public CachedEntity doCreateNaturalResourceDiscovery(CachedEntity character)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public CachedEntity getRootLocation(CachedEntity location)
	{
		
		if (CommonChecks.checkLocationIsRootLocation(location))
		{
			return location;
		}
		else
		{
			// Find the permanent location which functions as the "root" or the "major" location
			for(int i = 0; i<4; i++)
			{
				CachedEntity parent = getParentLocation(ds, location);
				if (parent==null) return location;
				location = parent;
				if (CommonChecks.checkLocationIsRootLocation(location))
					return parent;
			}
		}
		return null;
	}

	public CachedEntity getParentPermanentLocation(CachedEntity location)
	{
		
		if (CommonChecks.checkLocationIsPermanentLocation(location))
		{
			return location;
		}
		else
		{
			// Find the permanent location which functions as the "root" or the "major" location
			for(int i = 0; i<4; i++)
			{
				CachedEntity parent = getParentLocation(ds, location);
				if (parent==null) return location;
				location = parent;
				if (CommonChecks.checkLocationIsPermanentLocation(location))
					return parent;
			}
		}
		return null;
	}

	public List<Key> getLocationAlwaysVisiblePaths_KeysOnly(Key locationKey)
	{
		List<Key> alwaysVisiblePaths = query.getFilteredList_Keys("Path", 100, "location1Key", locationKey, "discoveryChance", 100d);
		alwaysVisiblePaths.addAll(query.getFilteredList_Keys("Path", 100, "location2Key", locationKey, "discoveryChance", 100d));
		return alwaysVisiblePaths;
	}
	
	public List<CachedEntity> getLocationAlwaysVisiblePaths(Key locationKey)
	{
		List<CachedEntity> alwaysVisiblePaths = getFilteredList("Path", "location1Key", locationKey, "discoveryChance", 100d);
		alwaysVisiblePaths.addAll(query.getFilteredList("Path", "location2Key", locationKey, "discoveryChance", 100d));
		return alwaysVisiblePaths;
	}

	public Object solveCurve(Random rnd, String curve)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Long solveCurve_Long(Random rnd, String curve)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Double solveCurve_Double(Random rnd, String curve)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void performModifierTypeOperation(CachedEntity entityToChange, EmbeddedEntity entityModifier)
	{
		// TODO Auto-generated method stub
		
	}

	public void performModifierTypeOperation(CachedEntity entityToChange, CachedEntity entityModifier)
	{
		// TODO Auto-generated method stub
		
	}
	
	public boolean performModifierTypeOperation(CachedEntity entityToChange, CachedEntity configEntity, String modifierKeyFieldName, String modifierEmbeddedFieldName)
	{
		boolean changed = false;
		Object entityModifierEmbeddedObj = configEntity.getProperty(modifierEmbeddedFieldName);
		if (entityModifierEmbeddedObj!=null)
		{
			if (entityModifierEmbeddedObj instanceof Collection)
			{
				for(EmbeddedEntity e:(Collection<EmbeddedEntity>)entityModifierEmbeddedObj)
				{
					performModifierTypeOperation(entityToChange, e);
					changed = true;
				}
			}
			else if (entityModifierEmbeddedObj instanceof EmbeddedEntity)
			{
				performModifierTypeOperation(entityToChange, (EmbeddedEntity)entityModifierEmbeddedObj);
				changed = true;
			}
			else throw new RuntimeException("Unhandled type: "+entityModifierEmbeddedObj.getClass().getSimpleName());
			
		}
		
		Object entityModifierObj = (Object)configEntity.getProperty(modifierKeyFieldName);
		if (entityModifierObj!=null)
		{
			if (entityModifierObj instanceof Key)
			{
				CachedEntity modifier = getDB().getIfExists((Key)entityModifierObj);
				
				performModifierTypeOperation(entityToChange, modifier);
				changed = true;
			}
			else if (entityModifierObj instanceof Collection)
			{
				
				Collection<CachedEntity> list = getDB().get((Collection<Key>)entityModifierObj);

				for(CachedEntity modifier:list)
				{
					performModifierTypeOperation(entityToChange, modifier);
					changed = true;
				}
				
			}
			else throw new RuntimeException("Unhandled type: "+entityModifierObj.getClass().getSimpleName());
		}
		
		return changed;
	}
	
	
    public boolean doIfExpressionCheck(Object value1, String operator, String value2) {
    	return false;
    }

}
