package com.universeprojects.miniup.server.commands;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;

public abstract class Command 
{
	private CachedDatastoreService ds;
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	public Command(CachedDatastoreService ds, HttpServletRequest request, HttpServletResponse response)
	{
		this.ds = ds;
		this.request = request;
		this.response = response;
	}
	
	
	void setPopupMessage(String message)
	{
		//TODO: Set the appropriate attribute for this
	}
	
	
}
