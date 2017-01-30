package com.universeprojects.miniup.server.commands;

import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.model.GridCell;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class CommandMapPlow extends Command {

	public CommandMapPlow(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		try {
			int xGridCoord = Integer.parseInt(parameters.get("xGridCoord").trim());
			int yGridCoord = Integer.parseInt(parameters.get("yGridCoord").trim());
			GridCell updatedGridCell = new GridCell(xGridCoord, yGridCoord);
			updatedGridCell.setBackgroundFile("boulder1.png");
			addGridCellUpdate(updatedGridCell);
		} catch (Exception e) {
			throw new UserErrorMessage("Invalid coordinate.");
		}
	}
}
