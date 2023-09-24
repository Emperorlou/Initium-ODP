package com.universeprojects.miniup.server.longoperations;

public class AbortedActionException extends Exception {

	public enum Type
	{
		CombataWhileMoving
	}
	
	private Type type;
	public AbortedActionException(Type type) 
	{
		this.type = type;
	}

	
	public Type getType()
	{
		return type;
	}

}
