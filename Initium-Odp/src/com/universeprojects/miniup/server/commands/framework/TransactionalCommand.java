package com.universeprojects.miniup.server.commands.framework;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;

public abstract class TransactionalCommand extends Command
{

	public TransactionalCommand(HttpServletRequest request, HttpServletResponse response) 
	{
		super(request, response);
		// TODO Auto-generated constructor stub
	}
	
}
