package com.universeprojects.miniup.server;

public class ContentDeveloperException extends RuntimeException
{
	private static final long serialVersionUID = -7497204099834656469L;

	/**
	 * This message will be shown to the player with a note to pass it along to a content developer so they can fix it.
	 * 
	 * @param message
	 */
	public ContentDeveloperException(String message)
	{
		super(message);
	}
}
