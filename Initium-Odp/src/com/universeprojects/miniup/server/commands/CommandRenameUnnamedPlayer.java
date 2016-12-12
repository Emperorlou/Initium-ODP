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

/**
 * Renname Unnamed character command.
 * 
 * @author RevMuun
 * 
 */

public class CommandRenameUnnamedPlayer extends Command {
	
	/**
	 * Command provides rennaming capabilities for unnamed characters.
	 * @param db
	 * 			  ODP Database
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandRenameUnnamedPlayer(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);		
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		
		//get the ODP database object.
		ODPDBAccess db = getDB();
		//grab the CachedDatastoreService (seems to be a singleton)
		CachedDatastoreService ds = getDS();
		//get the current character
		CachedEntity character = db.getCurrentCharacter();
		
		//grab the current character name as well as the one provided by the user.
		String charName = (String) character.getProperty("name");
		String newName = parameters.get("newName");
		
		//this command is intended only for characters with the name "Unnamed". Case sensitive.
		if(charName.equals("Unnamed")){
			//looking at ODPDBAccess.isCharacterNameOk(), it appears to do all the validation. Other than checking
			//if the character is named "Unnamed" no extra validation will be done here.
			if(db.isCharacterNameOk(this.request, newName)){
				
					//everything looks good. Update the CachedEntity and put it in the database.
					character.setProperty("name", newName);
					
					ds.put(character);
			}			
		}
		else{
			throw new UserErrorMessage("Only \"Unnamed\" characters can be renamed");
		}
	}

}
