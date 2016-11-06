package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
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
		Map<String, ArrayList<CachedEntity>> sameNameMap = new HashMap<String, ArrayList<CachedEntity>>();
		ArrayList<CachedEntity> needsUpdate = new ArrayList<CachedEntity>();
		ArrayList<CachedEntity> needsDelete = new ArrayList<CachedEntity>();
		ArrayList<CachedEntity> sameNameList;
		boolean appendToEnd;
		String itemName;
		long quantity;
		ds.beginTransaction();
		try {
			for (CachedEntity mergeItem : batchItems) {
				if (GameUtils.equals(mergeItem.getProperty("containerKey"), character.getKey()) == false) {
					throw new UserErrorMessage("Item does not belong to character.");
				}
				if (mergeItem.hasProperty("quantity")) {
					if (mergeItem.getProperty("quantity")==null){
						quantity = 0;
					} else {
						quantity = (long) mergeItem.getProperty("quantity");
					}
					if (quantity >= 1) { // item is "stackable"
						// to slightly improve efficiency, only compare items
						// against items of the same name
						itemName = (String) mergeItem.getProperty("name");
						if (sameNameMap.containsKey(itemName)) {
							// contains a value already, compare down the line
							sameNameList = sameNameMap.get(itemName);
							appendToEnd = true;
							for (CachedEntity checkEntity : sameNameList) {
								if (canStack(checkEntity, mergeItem)) {
									checkEntity.setProperty("quantity",
											(long) checkEntity.getProperty("quantity") + quantity);
									needsDelete.add(mergeItem);
									appendToEnd = false;
									// oh god this is so inefficient, but set
									// takes up way more space and is bad for
									// iterating through
									if (!needsUpdate.contains(checkEntity)) {
										needsUpdate.add(checkEntity);
									}
									break;
								}
							}
							if (appendToEnd) {
								sameNameList.add(mergeItem);
								sameNameMap.put(itemName, sameNameList);
							}

						} else {
							sameNameList = new ArrayList<CachedEntity>();
							sameNameList.add(mergeItem);
							sameNameMap.put(itemName, sameNameList);
						}
					}

				}
			}
			for (CachedEntity updateEntity : needsUpdate) {
				ds.put(updateEntity);
			}
			for (CachedEntity deleteEntity : needsDelete) {
				ds.delete(deleteEntity);
			}
		} finally {
			ds.rollbackIfActive();
		}
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

	/**
	 * Compares two entities for stackability - that is that all properties are
	 * equal excluding quantity
	 * 
	 * @param entity1
	 *            - First entity
	 * @param entity2
	 *            - Second entity
	 * @return true if they can stack", else false
	 */
	protected boolean canStack(CachedEntity entity1, CachedEntity entity2) {
		Map<String, Object> entity1Props = entity1.getProperties();
		Map<String, Object> entity2Props = entity2.getProperties();
		if (entity1Props.size() != entity2Props.size()) {
			return false;
		} // simplest check
		// could instead remove quantity and do equal check between the two
		// maps?
		for (String checking : entity1Props.keySet()) {
			if (!checking.equals("quantity")) {
				if (!GameUtils.equals(entity1Props.get(checking), entity2Props.get(checking))) {
					return false;
				}
			}
		}
		return true;
	}

}
