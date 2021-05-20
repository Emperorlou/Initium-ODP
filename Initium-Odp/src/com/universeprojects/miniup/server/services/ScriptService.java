package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.ScriptType;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;
import com.universeprojects.miniup.server.scripting.jsaccessors.DBAccessor;
import com.universeprojects.miniup.server.scripting.wrappers.Buff;
import com.universeprojects.miniup.server.scripting.wrappers.Discovery;
import com.universeprojects.miniup.server.scripting.wrappers.Item;
import com.universeprojects.miniup.server.scripting.wrappers.Knowledge;
import com.universeprojects.miniup.server.scripting.wrappers.Character;
import com.universeprojects.miniup.server.scripting.wrappers.Location;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;
import com.universeprojects.miniup.server.scripting.wrappers.Path;
import com.universeprojects.miniup.server.scripting.wrappers.Quest;

import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

public class ScriptService extends Service 
{
	public final static Logger log = Logger.getLogger(ScriptService.class.getName());
	
	private static ScriptEngine engine = null;
	private Map<String, Object> contextObjects = new HashMap<>();
	
	private boolean canExecute = false;
	
	public static ScriptService getScriptService(ODPDBAccess db)
	{
		HttpServletRequest request = db.getRequest();
		if (request.getAttribute("scriptService") != null) return (ScriptService) request.getAttribute("scriptService");

		ScriptService service = new ScriptService(db, request);
		request.setAttribute("scriptService", service);

		return service;
	}
	
	/**
	 * Define what script context is allowed to see.
	 * @author Evan
	 *
	 */
	private class Filter implements ClassFilter {

		/**
		 * We only allow java.lang and java.util objects to be exposed
		 * @return true if the object is allowed, false otherwise.
		 */
		@Override
		public boolean exposeToScripts(String s) {
			if(s.startsWith("java.lang.")) {
				return true;
			}
			if(s.startsWith("java.util.")) {
				return true;
			}
			return false;
		}
	}
	
	private ScriptService(ODPDBAccess db, HttpServletRequest request)
	{
		super(db);

		
		//initialize the threadsafe ScriptEngine with the filter.
		if(engine == null) 
			engine = new NashornScriptEngineFactory().getScriptEngine(new Filter());
		
		canExecute = true;
		
		//add the core object to the context
		contextObjects.put("core", new DBAccessor(db, request));
	}
	
	/**
	 * Static method to wrap a CachedEntity in the EntityWrapper, for use in the script context.
	 * @param entity CachedEntity object
	 * @param db DB instance, needed for entity specific functions (moving items, getting buffs, etc).
	 * @return
	 */
	public static EntityWrapper wrapEntity(Object entity, ODPDBAccess db)
	{
		// We want to allow null wrappers to be returned, in cases such as GlobalEvent,
		// where we pass a null CachedEntity for the currentCharacter parameter.
		if(entity == null) return null;
		
		CachedEntity ce = null;
		if(entity instanceof EmbeddedEntity)
		{
			EmbeddedEntity ent = (EmbeddedEntity)entity;
			ce = new CachedEntity(ent.getKey());
			for(String fieldName:ent.getProperties().keySet())
				ce.setProperty(fieldName, ent.getProperty(fieldName));
			ce.setAttribute("entity", entity);
		}
		else if(entity instanceof CachedEntity)
		{
			ce = (CachedEntity)entity;
		}
		else
		{
			throw new RuntimeException("Entity does not support scripting.");
		}
		
		switch(ce.getKind())
		{
			case "Item":
				return new Item(ce, db);
			case "Character":
				return new Character(ce, db);
			case "Location":
				return new Location(ce, db);
			case "Buff":
				return new Buff(ce, db);
			case "Path":
				return new Path(ce, db);
			case "Discovery":
				return new Discovery(ce, db);
			case "Knowledge":
				return new Knowledge(ce, db);
			case "Quest":
				return new Quest(ce, db);
			default: 
				throw new RuntimeException("Entity does not support scripting.");
		}
	}
	
	/**
	 * Executes the script. Wraps entitySource in the script wrapper type first.  
	 * @param scriptEntity The actual script entity itself.
	 * @param entitySource The CachedEntity which fired the script.
	 * @return True if script executed successfully, false otherwise.
	 */
	public boolean executeScript(ScriptEvent event, CachedEntity scriptEntity, CachedEntity entitySource)
	{
		if(scriptEntity == null && entitySource != null && "Script".equals(entitySource.getKind()))
		{
			// Entity is a global script. Go ahead and pass it here.
			scriptEntity = entitySource;
			entitySource = null;
		}
		
		return executeScript(event, scriptEntity, ScriptService.wrapEntity(entitySource, this.db));
	}
	
	/**
	 * Executes the script. Wraps entitySource in the script wrapper type first.  
	 * @param scriptEntity The actual script entity itself.
	 * @param entitySource The entity which fired the script, will be passed into the script context to utilize in source.
	 * @return True if script executed successfully, false otherwise.
	 */
	public boolean executeScript(ScriptEvent event, CachedEntity scriptEntity, EntityWrapper sourceEntity)
	{
		if(event == null) throw new IllegalArgumentException("event cannot be null!");
		
		if(!canExecute)
		{
			log.log(Level.ALL, "Script service is not properly initialized!");
			return false;
		}
		
		// Does this script actually do anything?
		String script = (String)scriptEntity.getProperty("script");
		if (script == null || script.isEmpty())
		{
			// This is not necessarily an error, so it is not thrown as such
			event.errorText = "But nothing happened.";
			return false;
		}
		
		String scriptName = (String)scriptEntity.getProperty("internalName");
	    try
	    {
	    	log.log(Level.WARNING, "Executing script: " + scriptName + " (" + scriptEntity.getKey().getId() + ")");
	    	
	    	//add the event and sourcEntity objects to the context
	    	contextObjects.put("event", event);
	    	contextObjects.put("sourceEntity", sourceEntity);
	    	
	    	//load the Bindings object for the script context
	    	Bindings bindings = engine.createBindings();
	    	bindings.putAll(bindings);
	    	
	    	//evaluate the script, given the bindings.
	    	engine.eval(script, bindings);
	    	
	    	return true;
	    }
	    catch (Exception e)
	    {
	    	log.log(Level.SEVERE, "Exception during Script exection of " + scriptName, e);
	    }
		return false;
	}
	
	/**
	 * Gets all scripts of a singular type associated with the list of entities. 
	 * @param db ODPDBAccess instance, to get the script entities themselves
	 * @param entities List of entities to get the scripts for.
	 * @param type ScriptType for scripts we want to find
	 * @return Mapping of all scripts associated to a particular entity.
	 */
	@SuppressWarnings("unchecked")
	public static Map<CachedEntity, List<CachedEntity>>
		getEntityScriptsOfType(ODPDBAccess db, List<CachedEntity> entities, ScriptType type)
	{
		// This will be the return.
		Map<CachedEntity, List<CachedEntity>> scriptMap = 
				new HashMap<CachedEntity, List<CachedEntity>>();

		// This map tracks the script -> List<entities>. Only contains 
		// valid (non-null) entities, and the mapped list could be empty on return.
		Map<Key, List<CachedEntity>> entityScripts = new HashMap<Key, List<CachedEntity>>();
		// This will be used to retrieve all the scripts from the DB
		HashSet<Key> getKeys = new HashSet<Key>();
		for(CachedEntity curEntity:entities)
		{
			// A null entity could be possible, such as fighting bare handed (weapon == null)
			if(curEntity == null) continue;
			scriptMap.put(curEntity, new ArrayList<CachedEntity>());
			List<Key> entityKeys = (List<Key>)curEntity.getProperty("scripts");
			if(entityKeys == null) continue;
			for(Key key:entityKeys)
			{
				if(key == null) continue;
				if(!entityScripts.containsKey(key)) entityScripts.put(key, new ArrayList<CachedEntity>());
				entityScripts.get(key).add(curEntity);
				getKeys.add(key);
			}
		}
		
		// Only allow this specific type of script to be processed.
		List<CachedEntity> scripts = db.getScriptsOfType(new ArrayList<Key>(getKeys), type);
		
		// For each script, get the associated entities, and add to the return
		// of entity -> List<script>. These will fire as one-offs, not in batches.
		for(CachedEntity script:scripts)
		{
			if(script == null) continue;
			// Get the list of entities associated with this script
			List<CachedEntity> scriptSources = entityScripts.get(script.getKey());
			for(CachedEntity entity:scriptSources)
			{
				if(!scriptMap.containsKey(entity)) scriptMap.put(entity, new ArrayList<CachedEntity>());
				scriptMap.get(entity).add(script);
			}
		}
			
		return scriptMap;
	}
	
	/**
	 * Gets all combat scripts associated with the common combat entities 
	 * @param db ODPDBAccess instance, to get the script entities themselves 
	 * @param attacker CachedEntity of the attacking character
	 * @param primaryWeapon CachedEntity of the primary (attack command) weapon of the attacking entity
	 * @param secondaryWeapon CachedEntity of the secondary (offhand) weapon of the attacking entity
	 * @param defender CachedEntity of the defending character
	 * @return ScriptType -> Entity -> List<Script> map.
	 */
	@SuppressWarnings("unchecked")
	public static Map<ScriptType, Map<CachedEntity, List<CachedEntity>>> 
		getCombatScripts(ODPDBAccess db, CachedEntity attacker, CachedEntity primaryWeapon, CachedEntity secondaryWeapon, CachedEntity defender)
	{
		// This will be the return. ScriptType -> Entity -> List<Script>
		Map<ScriptType, Map<CachedEntity, List<CachedEntity>>> scriptMap = 
				new HashMap<ScriptType, Map<CachedEntity, List<CachedEntity>>>();

		// This map tracks the script -> List<entities>. Only contains 
		// valid (non-null) entities, and the mapped list could be empty on return.
		Map<Key, List<CachedEntity>> entityScripts = new HashMap<Key, List<CachedEntity>>();
		// This will be used to retrieve all the scripts from the DB
		HashSet<Key> getKeys = new HashSet<Key>();
		for(CachedEntity curEntity:Arrays.asList(attacker, primaryWeapon, secondaryWeapon, defender))
		{
			// A null entity could be possible, such as fighting bare handed (weapon == null)
			if(curEntity == null) continue;
			List<Key> entityKeys = (List<Key>)curEntity.getProperty("scripts");
			if(entityKeys == null) continue; 
			for(Key key:entityKeys)
			{
				if(key == null) continue;
				if(!entityScripts.containsKey(key)) entityScripts.put(key, new ArrayList<CachedEntity>());
				entityScripts.get(key).add(curEntity);
				getKeys.add(key);
			}
		}
		
		// Only allow these specific types of scripts to be processed.
		List<CachedEntity> scripts = db.getScriptsOfType(new ArrayList<Key>(getKeys), 
				ScriptType.onAttack, ScriptType.onAttackHit, ScriptType.onDefend, ScriptType.onDefendHit);
		
		List<CachedEntity> attackerEntities = Arrays.asList(attacker, primaryWeapon, secondaryWeapon);
		for(CachedEntity script:scripts)
		{
			if(script == null) continue;
			ScriptType sType = ScriptType.valueOf(ScriptType.class, (String)script.getProperty("type"));
			
			// If allowed event type already exists in the script map, get it.
			// Otherwise, create a new one, but don't add it yet. We only want
			// a record in this map if we actually have a (correct) script to execute.
			Map<CachedEntity, List<CachedEntity>> currentScriptMap = 
				scriptMap.containsKey(sType) ? 
					scriptMap.get(sType) : 
					new HashMap<CachedEntity, List<CachedEntity>>();
			
			// Get all the entities associated with this particular script.
			List<CachedEntity> scriptSources = entityScripts.get(script.getKey());
			
			switch(sType)
			{
				case onAttack:
				case onAttackHit:
					for(CachedEntity attack:attackerEntities)
					{
						if(attack != null && scriptSources.contains(attack))
						{
							// This entity belongs to this script. Go ahead and add to the underlying list.
							if(!currentScriptMap.containsKey(attack)) currentScriptMap.put(attack, new ArrayList<CachedEntity>());
							currentScriptMap.get(attack).add(script);
						}
					}
				case onDefend:
				case onDefendHit:
					if(defender != null && scriptSources.contains(defender))
					{
						if(!currentScriptMap.containsKey(defender)) currentScriptMap.put(defender, new ArrayList<CachedEntity>());
						currentScriptMap.get(defender).add(script);
					}
				default:
					break;
			}
			// Only adding to the script map if we actually have a script from the correct entity.
			// Put will replace in the map, which is fine since it's the same instance we originally
			// got from the map and is a constant time operation
			if(!currentScriptMap.isEmpty()) scriptMap.put(sType, currentScriptMap);
		}
		return scriptMap;
	}
	
	/**
	 * After a script is complete, clean up the event.
	 * @param event
	 */
	public void cleanupEvent(ScriptEvent event) {
		for(CachedEntity saveEntity:event.getSaveEntities())
			ds.put(saveEntity);
		for(CachedEntity delEntity:event.getDeleteEntities())
			ds.delete(delEntity);
		
		for(Entry<Key,Set<String>> update:event.getGameUpdates().entrySet())
		{
			if("Location".equals(update.getKey().getKind()))
			{
				for(String method:update.getValue())
					db.sendMainPageUpdateForLocation(update.getKey(), ds, method);
			}
			else if("Character".equals(update.getKey().getKind()))
			{
				for(String method:update.getValue())
					db.sendMainPageUpdateForCharacter(ds, update.getKey(), method);
			}
		}
		
		//send all the specified game messages to the appropriate characters.
		for(Entry<Key, List<String>> messagesToSend : event.getGameMessages().entrySet()) {
			for(String message : messagesToSend.getValue()){
				db.sendGameMessage(db.getDB(), messagesToSend.getKey(), message);
			}
		}
	}
}
