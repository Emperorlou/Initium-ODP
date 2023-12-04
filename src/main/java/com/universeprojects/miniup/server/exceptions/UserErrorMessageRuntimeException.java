package com.universeprojects.miniup.server.exceptions; 

public class UserErrorMessageRuntimeException extends RuntimeException 
{     
	private static final long serialVersionUID = 4988729542204657816L;

	public UserErrorMessageRuntimeException(String message) 
	{         
		super(message);
	} 
	
}