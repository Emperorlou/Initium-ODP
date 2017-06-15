package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * Allows the player to switch between owned characters
 * 
 * @author jenga201
 * 
 */
public class CommandSwitchCharacter extends Command {

	public CommandSwitchCharacter(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();

		// Up front authentication validation
		CachedEntity user = db.getCurrentUser();
		if (user == null) {
			throw new UserErrorMessage(
					"You cannot switch users if you're not logged into a user account. Are you just using a throwaway? If so, try converting the throwaway to a full account first.");
		}

		// Get characterId to switch to
		Long targetCharacterId;
		try {
			targetCharacterId = Long.parseLong(parameters.get("characterId"));
		} catch (NumberFormatException e) {
			throw new UserErrorMessage("This character doesn't exist.");
		}

		// Get target character entity and validate
		CachedEntity targetCharacter = db.getEntity("Character", targetCharacterId);
		if (targetCharacter == null) {
			throw new UserErrorMessage("This character doesn't exist.");
		}

		// Compare userKey between current character and target character
		CachedEntity character = db.getCurrentCharacter();
		if (!GameUtils.equals(character.getProperty("userKey"), targetCharacter.getProperty("userKey"))) {
			throw new UserErrorMessage("The character you are trying to switch to does not belong to you.");
		}

		// Don't switch to characters with zombie status
		if ("Zombie".equals(targetCharacter.getProperty("status"))) {
			throw new UserErrorMessage("You cannot switch to this character, it is now a zombie.");
		}

		// Set and save new character
		user.setProperty("characterKey", targetCharacter.getKey());
		try {
			ds.put(user);
		} catch (Exception e) {
			throw new UserErrorMessage("Error while switching character: " + e.getMessage());
		}

		// Set the cached currentCharacter to target
		request.setAttribute("characterEntity", targetCharacter);
		
		// Update the verifyCode to the new character
		StringBuilder js = new StringBuilder();
		try {
			js.append("window.verifyCode = '" + db.getVerifyCode() + "';");
		} catch (NotLoggedInException e) {
			js.append("window.verifyCode = '';");
		}

		js.append("window.chatIdToken = '" + db.getChatIdToken(targetCharacter.getKey()) + "';");
		js.append("window.messager.idToken = window.chatIdToken;");

		addJavascriptToResponse(js.toString());
		
		// Consolidating this to quick refresh the page
		CachedEntity location = ds.getIfExists((Key) targetCharacter.getProperty("locationKey"));
		MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), targetCharacter, location, this);
		mpus.shortcut_fullPageUpdate();
	}
}
