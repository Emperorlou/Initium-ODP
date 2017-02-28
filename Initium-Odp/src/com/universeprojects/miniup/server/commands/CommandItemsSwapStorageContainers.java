package com.universeprojects.miniup.server.commands;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ContainerService;

/*
 * 	Author: RevMuun
 */

public class CommandItemsSwapStorageContainers extends CommandItemsBase {

	public CommandItemsSwapStorageContainers (ODPDBAccess db, HttpServletRequest request, HttpServletResponse response){
		super(db,request,response);
	}
	
	@Override
	protected void processBatchItems(Map<String, String> parameters, ODPDBAccess db, 
			CachedDatastoreService ds, CachedEntity character, List<CachedEntity> batchItems) throws UserErrorMessage{
		
			//we're swapping 2 containers here. If there's more than 2 items in the list, something went wrong.
			if(batchItems.size() != 2)
				throw new UserErrorMessage("Error in item selection. 2 (and only 2) containers must be involved in a swap.");
				
			ContainerService cs = new ContainerService(db);
				
			//make sure both items are containers and character has access to them.
			for(CachedEntity item : batchItems){					
				if(!GameUtils.isStorageItem(item))
					throw new UserErrorMessage("You must select two storage items to swap.");
				if(!cs.checkContainerAccessAllowed(character, item))
					throw new UserErrorMessage("You do not have access to one of these containers.");
			}
			
			CachedEntity containerOne = batchItems.get(0);
			CachedEntity containerTwo = batchItems.get(1);
			List<CachedEntity> itemOneContents = db.getItemContentsFor(containerOne.getKey(), false);
			List<CachedEntity> itemTwoContents = db.getItemContentsFor(containerTwo.getKey(), false);
			
			//check that at least one container is empty.
			if(itemOneContents.size() != 0 && 
					itemTwoContents.size() != 0){
				throw new UserErrorMessage("At least one storage item must be empty in order to swap contents.");
			}				
			
					
			//determine which container is empty. Since we checked that at least one container is empty,
			//we can assume that if index 1 has equipment, then index 0 is empty.
			CachedEntity emptyContainer = containerOne;
			CachedEntity fullContainer = containerTwo;
			List<CachedEntity> itemsToMove = itemTwoContents;
			if(itemTwoContents.size() == 0){
				emptyContainer = containerTwo;
				fullContainer = containerOne;
				itemsToMove = itemOneContents;
			}
			
			Long spaceReq = db.getItemCarryingSpace(character, itemsToMove);
			Long weightReq = db.getItemCarryingWeight(character, itemsToMove);
			Long spaceAvail = (Long) emptyContainer.getProperty("maxSpace");
			Long weightAvail = (Long) emptyContainer.getProperty("maxWeight");
			
			//check that we have room in the new container to fit everything.
			if(spaceAvail < spaceReq)
				throw new UserErrorMessage("You don't have enough space available in the new container. Maybe try a different one?");
			if(weightAvail < weightReq)
				throw new UserErrorMessage("The container you're swapping in to cannot carry so much weight. Try a different container.");
			
			//actually move the content now and save to database.
			ds.beginBulkWriteMode();
			for(CachedEntity item : itemsToMove){
				
				item.setProperty("containerKey",emptyContainer.getKey());
				item.setProperty("moveTimestamp", new Date());
				
				ds.put(item);				
			}			
			ds.commitBulkWrite();
			
			String fullLabel = (String) fullContainer.getProperty("label");
			String emptyLabel = (String) fullContainer.getProperty("label");
			
			setPopupMessage("Container contents in " + cs.getContainerDisplayName(fullContainer) + "container " +
					"have been moved into the " + cs.getContainerDisplayName(emptyContainer) + "container!");
	}

}
