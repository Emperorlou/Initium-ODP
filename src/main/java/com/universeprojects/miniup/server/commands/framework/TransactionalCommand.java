package com.universeprojects.miniup.server.commands.framework;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.miniup.server.ODPDBAccess;

public abstract class TransactionalCommand extends Command
{

	public TransactionalCommand(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
	{
		super(db, request, response);
		// TODO Auto-generated constructor stub
	}
	
}
