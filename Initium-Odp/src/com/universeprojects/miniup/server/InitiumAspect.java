package com.universeprojects.miniup.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.gefcommon.shared.elements.GameAspect;
import com.universeprojects.gefcommon.shared.elements.GameObject;
import com.universeprojects.miniup.server.commands.framework.Command;

public abstract class InitiumAspect implements GameAspect<Key>
{
	final protected ODPDBAccess db;
	final protected InitiumObject object;
	final protected CachedEntity entity;
	final protected String aspectId;
	
	protected InitiumAspect(InitiumObject object)
	{
		this.object = object;
		this.entity = object.getEntity();
		this.db = object.getDB();
		this.aspectId = this.getClass().getSimpleName().substring(6);
	}
	
	/**
	 * This method is called when the aspect class is instantiated. Use this to set
	 * default field values.
	 */
	protected void initialize()
	{
		
	}

	@Override
	public void setProperty(String fieldName, Object value)
	{
		entity.setProperty(new StringBuilder().append(aspectId).append(":").append(fieldName).toString(), value);
	}
	
	@Override
	public Object getProperty(String fieldName)
	{
		return entity.getProperty(new StringBuilder().append(aspectId).append(":").append(fieldName).toString());
	}

	@Override
	public String getName()
	{
		return aspectId;
	}
	
	@Override
	public Collection<String> getPropertyNames()
	{
		return db.getFieldNamesForAspect(aspectId);
	}	
	
	@Override
	public GameObject<Key> getGameObject()
	{
		return object;
	}
	
	
	
	public static Map<String, Class<? extends Command>> aspectCommands = new HashMap<>();
	public static void addCommand(String commandName, Class<? extends Command> commandClass)
	{
		if (aspectCommands.containsKey(commandName)) throw new IllegalArgumentException("An aspect command by the name '"+commandName+"' already exists. Use another name.");
		aspectCommands.put(commandName, commandClass);
	}
	
	public static Class<? extends Command> getAspectCommand(String commandName)
	{
		return aspectCommands.get(commandName);
	}

	
	@Override
	public void setSubObjectSingle(String name, GameObject<Key> object)
	{
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void setSubObjectList(String name, List<GameObject<Key>> object)
	{
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void setSubObjectMap(String name, Map<?, GameObject<Key>> object)
	{
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Map<String, GameObject<Key>> getSubObjectsSingle()
	{
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Map<String, List<GameObject<Key>>> getSubObjectsList()
	{
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Map<String, Map<?, GameObject<Key>>> getSubObjectsMap()
	{
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}
	
	
}
