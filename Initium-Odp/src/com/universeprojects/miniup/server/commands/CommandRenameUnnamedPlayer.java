package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;
import com.universeprojects.miniup.server.services.PropertiesService;

//ODPDBAccess.isCharacterNameOk()

public class CommandRenameUnnamedPlayer extends Command {
	
	//private HttpServletRequest theRequest;

	public CommandRenameUnnamedPlayer(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
		//theRequest = request;
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		// TODO Auto-generated method stub
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		String charName = (String) character.getProperty("name");
		String newName = parameters.get("newName");
		
		if(charName == "Unnamed"){
			//looking at ODPDBAccess.isCharacterNameOk(), it appears to do all the validation. Other than checking
			//if the character is named "Unnamed" no extra validation will be done here.
			try{
				if(db.isCharacterNameOk(this.request, charName)){
					
				}
			}
			catch(UserErrorMessage uem){
				throw uem;
			}
		}
		else{
			throw new UserErrorMessage("Only \"Unnamed\" characters can be renamed");
		}
	}

}
