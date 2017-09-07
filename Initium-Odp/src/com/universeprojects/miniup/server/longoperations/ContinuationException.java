package com.universeprojects.miniup.server.longoperations;

public class ContinuationException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7030497936220407018L;
	final public String reason;
	final public int seconds;
	
	public ContinuationException(int seconds)
	{
		this.reason = null;
		this.seconds = seconds;
	}
	
	public ContinuationException(int seconds, String message)
	{
		this.reason = message;
		this.seconds = seconds;
	}
}
