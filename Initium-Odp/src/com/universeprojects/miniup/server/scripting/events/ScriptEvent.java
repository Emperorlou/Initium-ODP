package com.universeprojects.miniup.server.scripting.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;
import com.universeprojects.miniup.server.scripting.wrappers.BaseWrapper;
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
public abstract class ScriptEvent extends OperationBase
{
	public ScriptEvent(CachedEntity character, ODPDBAccess db)
	{
		super(db.getRequest(), db);
		this.currentCharacter = ScriptService.wrapEntity(character, db);
	}
	
	public ScriptEvent(EntityWrapper character)
	{
		super(character.getDB().getRequest(), character.getDB());
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
	 * the error text to the user with a UserErrorMessage. Will prevent certain actions
	 * from completing, as will cause the Command to return in an error state.
	 */
	public String errorText;
	/**
	 * Shows a pop up message on the front end. Uses UserErrorMessage with the isError
	 * parameter set to false, indicating it's just an informational pop up.
	 */
	public String popupMessage;
	/**
	 * Setting this to true will immediately halt execution of the command. Keep in mind
	 * that certain commands cannot be halted immediately.
	 */
	public boolean haltExecution = false;
	/**
	 * This will allow the character widgets to be refreshed, in case of new buffs/HP.
	 */
	public boolean reloadWidgets = false;
	/**
	 * Allows scripts to save attributes across script calls utilizing the same event (combat,
	 * movement, etc). Also allows passing additional values (non-entities) to global scripts.
	 */
	protected Map<String, Object> attributes = new HashMap<String, Object>();
	/**
	 * Used to indicate to the ODP which objects need to be pushed back to the DB.
	 */
	public Map<Key, EntityWrapper> saveEntities = new HashMap<Key, EntityWrapper>();
	
	/**
	 * Used to indicate to the ODP which objects need to be deleted from the DB.
	 */
	public Map<Key, EntityWrapper> deleteEntities = new HashMap<Key, EntityWrapper>();
	
	/**
	 * Used to track log entries through executions of the script. Will be compiled,
	 * and executed upon script completion.
	 */
	public Map<Level, String> logEntries = new HashMap<Level, String>();
	
	//-----------LOGGING METHODS----------//
	public void logFine(String log)
	{
		log(Level.FINE, log);
	}
	
	public void logInfo(String log)
	{
		log(Level.INFO, log);
	}
	
	public void logWarning(String log)
	{
		log(Level.WARNING, log);
	}
	
	public void logSevere(String log)
	{
		log(Level.SEVERE, log);
	}
	
	private void log(Level type, String log)
	{
		String currentLog = logEntries.get(type);
		currentLog = currentLog == null ? log : (currentLog + "\r\n" + log);
		logEntries.put(type, currentLog);
	}
	/**
	 * Indicates the event type we're dealing with. Simply used for sanity purposes,
	 * as a script has a single discrete type (onAttacking, itemLink, locationLink, etc). 
	 * @return Event type we're dealing with.
	 */
	public abstract String eventKind();
	
	/**
	 * Resets all return data to initial state. For event reuse. Does NOT clear out attributes.
	 */
	public void reset()
	{
		this.descriptionText = "";
		this.errorText = "";
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
	
	private JavascriptResponse jsResponse = JavascriptResponse.None;
	public JavascriptResponse getJavascriptResponse()
	{
		return jsResponse;
	}
	
	public void setResponseRefresh()
	{
		jsResponse = JavascriptResponse.FullPageRefresh;
	}
	
	public void setResponseReloadPopup()
	{
		jsResponse = JavascriptResponse.ReloadPagePopup;
	}
	
	public void setResponseClear()
	{
		jsResponse = JavascriptResponse.None;
	}
	
	public boolean updatesGameState()
	{
		return jsResponse == JavascriptResponse.FullPageRefresh || reloadWidgets;
	}
	
	private Map<Key, Set<String>> dbGameUpdates = new HashMap<Key, Set<String>>();
	public void sendGameUpdate(Key entityKey, String updateMethod)
	{
		Set<String> curUpdates = dbGameUpdates.get(entityKey);
		if(curUpdates == null) 
		{
			dbGameUpdates.put(entityKey, new HashSet<String>());
			curUpdates = dbGameUpdates.get(entityKey);
		}
		curUpdates.add(updateMethod);
	}
	
	public boolean removeGameUpdate(Key entityKey, String updateMethod)
	{
		Set<String> curUpdates = dbGameUpdates.get(entityKey);
		if(curUpdates == null) 
		{
			dbGameUpdates.put(entityKey, new HashSet<String>());
			curUpdates = dbGameUpdates.get(entityKey);
		}		
		return curUpdates.remove(updateMethod);
	}
	
	public String[] getGameUpdatesFor(Key entityKey)
	{
		if(dbGameUpdates.containsKey(entityKey)==false) return new String[0];
		Set<String> curUpdates = dbGameUpdates.get(entityKey);
		return curUpdates.toArray(new String[curUpdates.size()]);
	}
	
	public Map<Key, Set<String>> getGameUpdates()
	{
		return dbGameUpdates;
	}
	
	/**
	 * Helper method used in script context to mark wrapped entities for save in the ODP.
	 * @param entities
	 */
	public void saveEntity(EntityWrapper... entities)
	{
		ScriptService.log.log(Level.INFO, "SaveEntity called with " + entities.length + " entities.");
		for(EntityWrapper entity:entities)
		{
			if(entity.isEmbeddedEntity)
			{
				throw new RuntimeException("Cannot save embedded entity! Please save the parent entity instead.");
			}
			
			if(!saveEntities.containsKey(entity.getKey()))
			{
				ScriptService.log.log(Level.INFO, "Saving " + entity.getKind() + ":" + entity.getId() + " entity.");
				saveEntities.put(entity.getKey(), entity);
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
							private Iterator<EntityWrapper> wrappers = saveEntities.values().iterator();

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
	
	public Iterable<EntityWrapper> getSaveWrappers()
	{
		return saveEntities.values();
	}
	
	/**
	 * Helper method used in script context to mark wrapped entities for delete in the ODP.
	 * @param entities
	 */
	public void deleteEntity(EntityWrapper... entities)
	{
		ScriptService.log.log(Level.INFO, "DeleteEntity called with " + entities.length + " entities.");
		for(EntityWrapper entity:entities)
		{
			if(entity.isEmbeddedEntity)
			{
				EmbeddedEntity embed = (EmbeddedEntity)entity.wrappedEntity.getAttribute("entity");
				embed.setProperty("_todelete", true);
				continue;
			}
			
			if(!deleteEntities.containsKey(entity.getKey())) 
			{
				ScriptService.log.log(Level.INFO, "Deleting " + entity.getKind() + ":"+ entity.getId() + " entity.");
				deleteEntities.put(entity.getKey(), entity);
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
					private Iterator<EntityWrapper> wrappers = deleteEntities.values().iterator();

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
	
	public Iterable<EntityWrapper> getDeleteWrappers()
	{
		return deleteEntities.values();
	}
	
	@Override
	public Long getSelectedTileX()
	{
		throw new RuntimeException("Unimplemented method");
	}
	
	@Override
	public Long getSelectedTileY()
	{
		throw new RuntimeException("Unimplemented method");
	}
}
