package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
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
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;

/**
 * Extension of the CommandsItemBase abstract class.
 * Drops the specified items on the ground of characters location.  
 * 
 * @author SPFiredrake
 * 
 */
public class CommandItemsDrop extends CommandItemsBase {
	
	public CommandItemsDrop(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	protected void processBatchItems(Map<String, String> parameters, 
			ODPDBAccess db, CachedDatastoreService ds,
			CachedEntity character, List<CachedEntity> batchItems)
			throws UserErrorMessage {
		
		List<CachedEntity> saveEntities = new ArrayList<CachedEntity>();
		for(CachedEntity dropItem:batchItems)
		{
			if(GameUtils.equals(dropItem.getProperty("containerKey"), character.getKey()) == false)
				continue;
			
			if(CommonChecks.checkCharacterIsBusy(character))
				throw new UserErrorMessage("Your character is currently busy and cannot drop items.");
			
			if(db.tryCharacterDropItem(character, dropItem, false))
				saveEntities.add(dropItem);
		}
		
		ds.beginBulkWriteMode();
		ds.put(saveEntities);
		ds.commitBulkWrite();
		
		// Reload the popup to show the modified inventory.
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}
