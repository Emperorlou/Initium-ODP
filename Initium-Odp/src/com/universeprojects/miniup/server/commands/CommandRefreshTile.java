package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.GridMapService;

public class CommandRefreshTile extends Command
{

	public CommandRefreshTile(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		Integer tileX = parseInteger("selected2DTileX");
		Integer tileY = parseInteger("selected2DTileY");
		
		GridMapService gms = db.getGridMapService();
		
		gms.regenerateDBItemTileCache(tileX, tileY);
		
		gms.putLocationData(ds);
		
		addJavascriptToResponse(gms.generateGridObjectJson(tileX, tileY));
		
		
	}

}
