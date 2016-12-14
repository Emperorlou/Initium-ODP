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
				throw new UserErrorMessage("Error in item selection. Only 2 containers can be involved in a swap.");
				
			ContainerService cs = new ContainerService(db);
				
			//make sure both items are containers and character has access to them.
			for(CachedEntity item : batchItems){					
				if(!GameUtils.isStorageItem(item))
					throw new UserErrorMessage("You must select two storage items to swap.");
				if(!cs.checkContainerAccessAllowed(character, item))
					throw new UserErrorMessage("Character does not have access to container.");
			}
			
			//check that at least one container is empty.
			if(db.getItemContentsFor(batchItems.get(0).getKey()).size() != 0 && 
					db.getItemContentsFor(batchItems.get(1).getKey()).size() != 0){
				throw new UserErrorMessage("At least one storage item must be empty in order to swap contents.");
			}				
			
			//determine which container is empty. Since we checked that at least one container is empty,
			//we can assume that if index 1 has equipment, then index 0 is empty.
			int emptyContainerIndex = 0;
			int fullContainerIndex = 1;
			if(db.getItemContentsFor(batchItems.get(1).getKey()).size() == 0){
				emptyContainerIndex = 1;
				fullContainerIndex = 0;
			}
			
			Long spaceReq = db.getItemCarryingSpace(batchItems.get(fullContainerIndex));
			Long weightReq = db.getItemCarryingWeight(batchItems.get(fullContainerIndex));
			Long spaceAvail = (Long) batchItems.get(emptyContainerIndex).getProperty("maxSpace");
			Long weightAvail = (Long) batchItems.get(emptyContainerIndex).getProperty("maxWeight");
			
			//check that we have room in the new container to fit everything.
			if(spaceAvail < spaceReq)
				throw new UserErrorMessage("There is not enough space available in the new container.");
			if(weightAvail < weightReq)
				throw new UserErrorMessage("There is not enough weight available in the new container.");
			
			//actually move the content now and save to database.
			ds.beginBulkWriteMode();
			for(CachedEntity item : db.getItemContentsFor(batchItems.get(fullContainerIndex).getKey())){
				
				item.setProperty("containerKey",batchItems.get(emptyContainerIndex));
				item.setProperty("moveTimestamp", new Date());
				
				ds.put(item);				
			}			
			ds.commitBulkWrite();
	}

}
