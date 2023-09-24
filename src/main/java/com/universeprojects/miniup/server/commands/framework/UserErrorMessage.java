package com.universeprojects.miniup.server.commands.framework;

public class UserErrorMessage extends Exception {
	private static final long serialVersionUID = -3317374960212093965L;
	private boolean isError = true;
	
	public UserErrorMessage(String message) 
	{
		super(message);
	}
	
	public UserErrorMessage(String message, boolean isError) 
	{
		super(message);
		this.isError = isError;
	}
	
	public boolean isError()
	{
		return isError;
	}

}
