package com.universeprojects.miniup.server.scripting.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;
import com.universeprojects.miniup.server.services.ScriptService;

/**
 * Represents the base Event type for use in the scripting service. This allows
 * for various script types to return a common set of data for use in the ODP, with
 * the specific implementations containing the event specific data to use.
 * These objects will be used in the script to actually return data back to the ODP.
 *   
 * @author spfiredrake
 */
public abstract class ScriptEvent 
{
	public ScriptEvent(CachedEntity character, ODPDBAccess db)
	{
		this(ScriptService.wrapEntity(character, db));
	}
	
	public ScriptEvent(EntityWrapper character)
	{
		this.currentCharacter = character;
	}

	/**
	 * Every script event is performed in relation to a character, so we keep a reference to it
	 * to use for the life of the event.
	 */
	public EntityWrapper currentCharacter;
	/**
	 * User friendly text indicating what the source accomplished. This is the flavor
	 * text that will be shown to the user to indicate that something special happened as
	 * a result of script execution.
	 */
	public String descriptionText;
	/**
	 * Indicates that an error was encountered within the script, and should display
	 * the error text to the user with a UserErrorMessage.
	 */
	public String errorText;
	/**
	 * Setting this to true will immediately halt execution of the command. Keep in mind
	 * that certain commands cannot be halted immediately.
	 */
	public boolean haltExecution = false;
	/**
	 * Allows scripts to save attributes across script calls utilizing the same event (combat,
	 * movement, etc).
	 */
	protected Map<String, Object> attributes = new HashMap<String, Object>();
	/**
	 * Used to indicate to the ODP which objects need to be pushed back to the DB.
	 */
	public List<EntityWrapper> saveEntities = new ArrayList<EntityWrapper>();
	
	/**
	 * Used to indicate to the ODP which objects need to be deleted from the DB.
	 */
	public List<EntityWrapper> deleteEntities = new ArrayList<EntityWrapper>();
	
	/**
	 * Indicates the event type we're dealing with. Simply used for sanity purposes,
	 * as a script has a single discrete type (onAttacking, itemLink, locationLink, etc). 
	 * @return Event type we're dealing with.
	 */
	public abstract String eventKind();
	
	/**
	 * Resets all return data to initial state. For event reuse. Does NOT clear out attributes.
	 */
	protected void reset()
	{
		this.descriptionText = null;
		this.errorText = null;
		this.haltExecution = false;
		this.saveEntities.clear();
		this.deleteEntities.clear();
	}
	
	/**
	 * Allows objects to be stored in script context across calls (for commands or
	 * events that fire multiple times, such as combat or movement)
	 * @param key Key to put in the attributes map
	 * @param value Value to persist across calls. Can be null.
	 */
	public void setAttribute(String key, Object value)
	{
		if(key == null || key.isEmpty()) return;
		this.attributes.put(key, value);
	}
	
	/**
	 * Allows objects to be retrieved in script context across calls (for commands or
	 * events that fire multiple times, such as combat or movement)
	 * @param key Key to retrieve from the map.
	 * @return Object from the attributes map. If key does not exist, null is returned.
	 */
	public Object getAttribute(String key)
	{
		if(attributes.containsKey(key)) return attributes.get(key);
		return null;
	}
	
	/**
	 * Helper method used in script context to mark wrapped entities for save in the ODP.
	 * @param entities
	 */
	public void saveEntity(EntityWrapper... entities)
	{
		ScriptService.log.log(Level.ALL, "SaveEntity called with " + entities.length + " entities.");
		for(EntityWrapper entity:entities)
		{
			if(!saveEntities.contains(entity)) 
			{
				ScriptService.log.log(Level.ALL, "Saving " + entity.getKind() + ":"+ entity.getName() + " entity.");
				saveEntities.add(entity);
			}
		}
	}
	
	/**
	 * Allows calling commands to iterate over the WrappedEntity save list
	 * as CachedEntity, to make saving easier.
	 * @return
	 */
	public Iterable<CachedEntity> getSaveEntities()
	{
		return new Iterable<CachedEntity>()
		{
			public Iterator<CachedEntity> iterator()
			{
				return new Iterator<CachedEntity>() {
					private Iterator<EntityWrapper> wrappers = saveEntities.iterator();

					public boolean hasNext() {
						return wrappers.hasNext();
					}

					public CachedEntity next() {
						return wrappers.next().wrappedEntity;
					}

					public void remove() {
						wrappers.remove();
					}
				};
			}
		};
	}
	
	/**
	 * Helper method used in script context to mark wrapped entities for delete in the ODP.
	 * @param entities
	 */
	public void deleteEntity(EntityWrapper... entities)
	{
		ScriptService.log.log(Level.ALL, "DeleteEntity called with " + entities.length + " entities.");
		for(EntityWrapper entity:entities)
		{
			if(!saveEntities.contains(entity)) 
			{
				ScriptService.log.log(Level.ALL, "Deleting " + entity.getKind() + ":"+ entity.getName() + " entity.");
				saveEntities.add(entity);
			}
		}
	}
	
	/**
	 * Allows calling commands to iterate over the WrappedEntity delete list
	 * as CachedEntity, to make deleting easier.
	 * @return
	 */
	public Iterable<CachedEntity> getDeleteEntities()
	{
		return new Iterable<CachedEntity>()
		{
			public Iterator<CachedEntity> iterator()
			{
				return new Iterator<CachedEntity>() {
					private Iterator<EntityWrapper> wrappers = deleteEntities.iterator();

					public boolean hasNext() {
						return wrappers.hasNext();
					}

					public CachedEntity next() {
						return wrappers.next().wrappedEntity;
					}

					public void remove() {
						wrappers.remove();
					}
				};
			}
		};
	}
}
