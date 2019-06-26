package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandCollectItem extends Command
{

	public CommandCollectItem(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		Long itemId = tryParseId(parameters, "itemId");
		
		CachedEntity item = db.getEntity(KeyFactory.createKey("Item", itemId));

		Long tileX = (Long)item.getProperty("gridMapPositionX");
		Long tileY = (Long)item.getProperty("gridMapPositionY");
		
		db.doCharacterCollectItem(this, db.getCurrentCharacter(), item);
		
		if(item.isUnsaved())
			ds.put(item);
		
		if (tileX!=null && tileY!=null)
		{
			db.getGridMapService().regenerateDBItemTileCache(tileX.intValue(), tileY.intValue());
			addJavascriptToResponse(db.getGridMapService().generateGridObjectJson(tileX.intValue(), tileY.intValue()));
			deleteHtml(".tileContentsItem[ref=Item\\("+itemId+"\\)]");
		}
		
	}

}
