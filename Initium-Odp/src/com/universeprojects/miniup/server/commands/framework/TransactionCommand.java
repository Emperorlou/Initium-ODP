package com.universeprojects.miniup.server.commands.framework;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.AbortTransactionException;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.Transaction;
import com.universeprojects.miniup.server.ODPDBAccess;

public abstract class TransactionCommand extends Command
{

	public TransactionCommand(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(final Map<String, String> parameters) throws UserErrorMessage
	{
		runBeforeTransaction(parameters);
		
		try
		{
			new Transaction<Void>(db.getDB()){

				@Override
				public Void doTransaction(CachedDatastoreService ds) throws AbortTransactionException
				{
					try
					{
						runInsideTransaction(parameters);
					}
					catch (UserErrorMessage e)
					{
						throw new AbortTransactionException(e.getMessage());
					}
					return null;
				}
				
			}.run();
		}
		catch (AbortTransactionException e)
		{
			throw new UserErrorMessage(e.getMessage());
		}
		
		runAfterTransaction(parameters);
	}
	
	public abstract void runBeforeTransaction(Map<String, String> parameters) throws UserErrorMessage;

	public abstract void runInsideTransaction(Map<String, String> parameters) throws UserErrorMessage;

	public abstract void runAfterTransaction(Map<String, String> parameters) throws UserErrorMessage;
	
	protected void refetch(CachedEntity entity)
	{
		db.getDB().refetch(entity);
	}

	protected void refetch(List<CachedEntity> entities)
	{
		db.getDB().refetch(entities);
	}
}
