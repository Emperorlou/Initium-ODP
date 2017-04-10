package com.universeprojects.miniup.server;

import java.util.HashMap;
import java.util.Map;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.commands.framework.Command;

public abstract class InitiumAspect
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
	protected abstract void initialize();

	public void setProperty(String fieldName, Object value)
	{
		entity.setProperty(new StringBuilder().append(aspectId).append(":").append(fieldName).toString(), value);
	}
	
	public Object getProperty(String fieldName)
	{
		return entity.getProperty(new StringBuilder().append(aspectId).append(":").append(fieldName).toString());
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
	
	
}
