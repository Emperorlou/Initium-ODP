package com.universeprojects.miniup.server.commands;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;

public abstract class TransactionalCommand extends Command
{

	public TransactionalCommand(CachedDatastoreService ds,
			HttpServletRequest request, HttpServletResponse response) {
		super(ds, request, response);
		// TODO Auto-generated constructor stub
	}
	
}
