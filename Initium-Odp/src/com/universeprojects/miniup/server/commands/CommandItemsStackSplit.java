package com.universeprojects.miniup.server.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;

public class CommandItemsStackSplit extends CommandItemsBase {

	public CommandItemsStackSplit(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	protected void processBatchItems(Map<String, String> parameters, ODPDBAccess db, CachedDatastoreService ds,
			CachedEntity character, List<CachedEntity> batchItems) throws UserErrorMessage {
		if (batchItems.size() > 1) {
			throw new UserErrorMessage("You can only split one item at a time.");
		}
		CachedEntity splitItem = batchItems.get(0);
		if (GameUtils.equals(splitItem.getProperty("containerKey"), character.getKey()) == false) {
			throw new UserErrorMessage("Item does not belong to character.");
		}
		if (!splitItem.hasProperty("quantity")) {
			throw new UserErrorMessage("You can only split stackable items.");
		}
		if ((long) splitItem.getProperty("quantity") < 2) {
			throw new UserErrorMessage("You can only split stacks of two or more items.");
		}
		String stackSizeString = parameters.get("stackSize");
		long stackSize;
		try {
			stackSize = Integer.parseInt(stackSizeString);
		} catch (NumberFormatException e) {
			throw new UserErrorMessage("Please input an integer value.");
		}
		if (stackSize <= 0) {
			throw new UserErrorMessage("Please input a positive integer value.");
		}
		long splitItemStackSize = (long) splitItem.getProperty("quantity");
		if (stackSize >= splitItemStackSize) {
			throw new UserErrorMessage(
					"Please input a quantity smaller than the size of the stack you are trying to split.");
		}
		long newStackSize = splitItemStackSize - stackSize;

		ds.beginTransaction();
		try {
			CachedEntity newItemStack = db.generateNewObject(splitItem, splitItem.getKind());
			splitItem.setProperty("quantity", stackSize);
			ds.put(splitItem);
			newItemStack.setProperty("quantity", newStackSize);
			ds.put(newItemStack);
		} finally {
			ds.rollbackIfActive();
		}

		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}
