package com.universeprojects.miniup.server.longoperations;

public class GameStateChangeException extends RuntimeException {

	public boolean refreshChat = false;
	public GameStateChangeException() {
		// TODO Auto-generated constructor stub
	}

	public GameStateChangeException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
	
	public GameStateChangeException(String message, boolean resetChat)
	{
		super(message);
		this.refreshChat = resetChat;
	}

}
