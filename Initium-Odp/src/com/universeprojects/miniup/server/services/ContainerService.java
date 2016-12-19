package com.universeprojects.miniup.server.services;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.AbortTransactionException;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.Transaction;
import com.universeprojects.miniup.server.GameUtils;
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
		}
		else { // items inside are moved to the container's container, and container is destroyed {
			CachedEntity parentContainer = db.getEntity((Key) container.getProperty("containerKey"));
			List<CachedEntity> items = db.getFilteredList("Item", "containerKey", FilterOperator.EQUAL, parentContainer.getKey());
			
			for (CachedEntity item:items) {
				item.setProperty("containerKey", parentContainer.getKey());
				item.setProperty("movedDate", new Date());
				
				ds.put(item);
			}
			
			Long coins = (Long)container.getProperty("dogecoins");
			if (coins<=0)
				ds.delete(container);
						
			
			throw new UserErrorMessage("The container has been destroyed due to durability loss, and any items inside have fallen out of it.");
		}
	}
	
	
	/**
	 * This is for stuff that allows access to a given container (location, item, or character). Returns false if access should not be allowed.
	 * @param character
	 * @param container
	 * @return
	 */
	public boolean checkContainerAccessAllowed(CachedEntity character, CachedEntity container)
	{
		// If the container is ourselves, it's ok
		if (container.getKind().equals("Character") && GameUtils.equals(character.getKey(), container.getKey()))
			return true;
		
		// If the container is our location, it's ok
		if (container.getKind().equals("Location") && GameUtils.equals(character.getProperty("locationKey"), container.getKey()))
			return true;
		
		// If the container is an item in our inventory, it's ok
		if (container.getKind().equals("Item") && GameUtils.equals(character.getKey(), container.getProperty("containerKey")))
			return true;
		
		// If the container is an item in our location, it's ok
		if (container.getKind().equals("Item") && GameUtils.equals(character.getProperty("locationKey"), container.getProperty("containerKey")))
			return true;
		
		return false;
	}
	
	/**
	 * 
	 * Retrieves the display name to use for a specific container. Will return the container's label unless the label is null
	 * or empty. If label is null or empty, will return the item name.
	 * 
	 * @param container
	 * @return
	 */
	public String getContainerDisplayName(final CachedEntity container){
		
		String displayName = (String) container.getProperty("label");
		
		if (displayName==null || displayName.trim().equals(""))
            displayName = (String)container.getProperty("name");
		
		return displayName;
	}
	
	public boolean contains(CachedEntity container, CachedEntity item) {

		final List<CachedEntity> content = db.getFilteredList("Item",
				"containerKey", FilterOperator.EQUAL, container.getKey());

		return content.contains(item);
	}

	public boolean containsAll(CachedEntity container, List<CachedEntity> items) {

		final List<CachedEntity> content = db.getFilteredList("Item",
				"containerKey", FilterOperator.EQUAL, container.getKey());

		return content.containsAll(items);
	}
	
	// the List can be passed in as null, if you dont have a list. 
	public boolean containsEquippable(CachedEntity container, List<CachedEntity> containerContent){
	
		final List<CachedEntity> content;
		
		if(containerContent!=null){
			content = containerContent;
		}else{
			content = db.getFilteredList("Item",
					"containerKey", FilterOperator.EQUAL, container.getKey());
		}
		
		if(content.size() == 0)
			return false;
		
		for(CachedEntity item:content){
			if(item.getProperty("equipSlot")!=null && item.getProperty("equipSlot")!=""){
				return true;
			}
		}
		return false;
	}
}
