package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * 
 * @author Evan
 *
 */
public class CommandItemsStackSplit extends CommandItemsBase {

	public CommandItemsStackSplit(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	protected void processBatchItems(Map<String, String> parameters, ODPDBAccess db, CachedDatastoreService ds,
			CachedEntity character, List<CachedEntity> batchItems) throws UserErrorMessage {
		Long splitItemQuantity;
		if (batchItems.size() > 12) {
			throw new UserErrorMessage("You can only split 12 items at a time.");
		}
		ds.beginTransaction();
		try {
			
			Long stackSize;
			Long stacks;
			try {
				stackSize = tryParseId(parameters, "stackSize");
				stacks = tryParseId(parameters, "stacks");
			}
			catch (NumberFormatException e) {
				throw new UserErrorMessage("Please input an integer value.");
			}
			if (stackSize <= 0 || stacks <= 0) {
				throw new UserErrorMessage("Please input a positive integer value.");
			}
			
			if(batchItems.size() > 1 && stacks > 1) {
				throw new UserErrorMessage("You can't split multiple items into multiple stacks.");
			}
			
			if(stacks > 24) {
				throw new UserErrorMessage("Too many stacks. Max is 24.");
			}
			
			//iterate through all of our items.
			for(CachedEntity splitItem:batchItems) {	
				if (CommonChecks.isItemCustom(splitItem)) {
					sendError("You cannot split a custom item.", splitItem);
					continue;
				}
				if (GameUtils.equals(splitItem.getProperty("containerKey"), character.getKey()) == false) {
					sendError("Item does not belong to character.", splitItem);
					continue;
				}
				if (!splitItem.hasProperty("quantity")) {
					sendError("You can only split stackable items.", splitItem);
					continue;
				}
				splitItemQuantity = (Long) splitItem.getProperty("quantity");
				if (splitItemQuantity==null) {
					sendError("You can only split stackable items.", splitItem);
					continue;
				}
				if (splitItemQuantity < 2) {
					sendError("You can only split stacks of two or more items.", splitItem);
					continue;
				}
				if(stackSize*stacks >= splitItemQuantity) {
					sendError("Given " + stacks + " stacks and a stack size of " + stackSize + ", you don't have enough of this item.", splitItem);
					continue;
				}
				
				long newStackSize = splitItemQuantity - (stackSize*stacks);
				ds.preallocateIdsFor(splitItem.getKind(), stacks.intValue());
				
				//Create all of the new item stacks.
				for(int i = 0; i < stacks.intValue(); i++) {
					CachedEntity newStack = new CachedEntity(splitItem.getKind(), ds.getPreallocatedIdFor(splitItem.getKind()));
					CachedDatastoreService.copyFieldValues(splitItem, newStack);
					
					newStack.setProperty("quantity", stackSize);
					ds.put(newStack);
				}
				
				splitItem.setProperty("quantity", newStackSize);
				ds.put(splitItem);
				
				ds.commit();
			}
		} 
		
		finally {
			ds.rollbackIfActive();
		}

		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}
	
	/**
	 * Send a formatted error to the user in the form of a game message.
	 * @param error
	 * @param item
	 */
	private void sendError(String error, CachedEntity item) {
		db.sendGameMessage("Unable to split item: " + GameUtils.renderItem(item) + ". " + error);
	}

}
