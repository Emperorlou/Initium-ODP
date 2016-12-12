package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/*
 * 
[8:32]  might be best if we use multiple selection for this
[8:32]  there are a few commands that use a multiple-selection abstract command as a base
[8:33]  we would basically just have 2 chests selected, one needs to be empty I guess (for lack of a better way)
[8:34]  clicking the "swap" link would cause all the items to go from one to the other
[8:34]  specifically, the chest with items would have all items move to the chest without items
[8:34]  I guess any gold in the chest would move too
[8:34]  if there is any
 * 
initiumdev [8:36 AM]  
if we wanted to get fancy later, we could maybe have a little popup that asks which direction the swap should go

papa_marsh [8:36 AM]  
also need to check to make sure the new chest has enough maxSpace and maxWeight to fit everything
 */

public class CommandItemsSwapStorageContainers extends Command {

	public CommandItemsSwapStorageContainers (ODPDBAccess db, HttpServletRequest request, HttpServletResponse response){
		super(db,request,response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		// TODO Auto-generated method stub

	}

}
