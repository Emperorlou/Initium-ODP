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

/**
 * Extension of the CommandsItemBase abstract class. Merges the stacks of the
 * specified items If multiple stackable items are selected, condense as far as
 * possible 5A, 10B, 3A, 4B, 6D -> 8A, 14B, 6D
 * 
 * @author Stephani
 */
public class CommandItemsStackMerge extends CommandItemsBase {

	public CommandItemsStackMerge(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	protected void processBatchItems(Map<String, String> parameters, ODPDBAccess db, CachedDatastoreService ds,
			CachedEntity character, List<CachedEntity> batchItems) throws UserErrorMessage {
		CachedEntity firstEntity; // for readability
		Map<String, CachedEntity> firstOfKind = new HashMap<String, CachedEntity>();
		ds.beginTransaction();
		try {
			for (CachedEntity mergeItem : batchItems) {
				if (GameUtils.equals(mergeItem.getProperty("containerKey"), character.getKey()) == false) {
					throw new UserErrorMessage("Item does not belong to character.");
				}
				if (mergeItem.hasProperty("quantity")) {
					if (firstOfKind.containsKey(mergeItem.getKind())) {
						firstEntity = firstOfKind.get(mergeItem.getKind());
						firstEntity.setProperty("quantity",
								(long) firstEntity.getProperty("quantity") + (long) mergeItem.getProperty("quantity"));
						ds.delete(mergeItem); // Do I need to use the key here?
					} else {
						firstOfKind.put(mergeItem.getKind(), mergeItem);
					}
				}
			}
			for (String stackKind : firstOfKind.keySet()) {
				if (firstOfKind.get(stackKind).isUnsaved()) {
					ds.put(firstOfKind.get(stackKind));
				}
			}
		} finally {
			ds.rollbackIfActive();
		}
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}
