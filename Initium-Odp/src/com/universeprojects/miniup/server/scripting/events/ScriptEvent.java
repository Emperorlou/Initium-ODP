package com.universeprojects.miniup.server.scripting.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
	 * Used to indicate to the ODP which objects need to be pushed back to the DB.
	 */
	protected List<EntityWrapper> saveEntities = new ArrayList<EntityWrapper>();
	
	/**
	 * Indicates the event type we're dealing with. Simply used for sanity purposes,
	 * as a script has a single discrete type (onAttacking, itemLink, locationLink, etc). 
	 * @return Event type we're dealing with.
	 */
	public abstract String eventKind();
	
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
}
