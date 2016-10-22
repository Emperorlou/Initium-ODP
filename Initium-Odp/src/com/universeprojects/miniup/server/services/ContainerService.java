package com.universeprojects.miniup.server.services;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.AbortTransactionException;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.Transaction;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class ContainerService extends Service {
	
	public ContainerService(ODPDBAccess db) {
		super(db);
	}
	
	public void doUse(CachedDatastoreService ds, final CachedEntity container, long durabilityLoss) throws UserErrorMessage {
		long durability = (long) container.getProperty("durability");
		
		if (durability > 0) {
			container.setProperty("durability", durability - durabilityLoss);
			ds.put(container);
		}
		else { // items inside are moved to the container's container, and container is destroyed {
			try {	
				CachedEntity parentContainer = (CachedEntity) new Transaction<CachedEntity>(ds) {
					
					@Override
					public CachedEntity doTransaction(CachedDatastoreService ds) throws AbortTransactionException {
						CachedEntity parentContainer = db.getEntity((Key) container.getProperty("containerKey"));
						List<CachedEntity> items = db.getFilteredList("Item", "containerKey", FilterOperator.EQUAL, parentContainer.getKey());
						
						for (CachedEntity item:items) {
							item.setProperty("containerKey", parentContainer.getKey());
							item.setProperty("movedDate", new Date());
							
							ds.put(item);
						}
						
						ds.delete(container);
						
						return parentContainer;
					}
				}.run();
			}
			catch (AbortTransactionException e) {
				throw new RuntimeException(e);
			}
			
			throw new UserErrorMessage("The container has been destroyed due to durability loss, and any items inside have fallen out of it.");
		}
	}
}
