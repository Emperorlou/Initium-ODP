package com.universeprojects.miniup.server;

import com.universeprojects.cacheddatastore.AbortTransactionException;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.Transaction;

public abstract class InitiumTransaction<T> extends Transaction<T> {

	public InitiumTransaction(CachedDatastoreService ds) {
		super(ds);
	}

	@Override
	public T doTransaction(CachedDatastoreService ds)
			throws AbortTransactionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T run() throws AbortTransactionException 
	{
		return super.run();
	}
	
	

}
