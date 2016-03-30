package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;

public abstract class Command 
{
	private CachedDatastoreService ds;
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	private String popupMessage;
	
	public Command(CachedDatastoreService ds, HttpServletRequest request, HttpServletResponse response)
	{
		this.ds = ds;
		this.request = request;
		this.response = response;
	}
	
	
	void setPopupMessage(String message)
	{
		this.popupMessage = message;
	}
	
	
	String getPopupMessage()
	{
		return popupMessage;
	}
	
	
	/**
	 * The command's execution logic is done here. 
	 */
	public abstract void run(Map<String, String> parameters) throws UserErrorMessage;
	
	
	
}
