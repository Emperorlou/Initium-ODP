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

/*
 * Command for adding a friend to a user's friend list.
 * @author RevMuun
 */

public class CommandUserAddFriend extends Command {

	public CommandUserAddFriend(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		
		CachedDatastoreService ds = getDS();
		
		//get current user.
		CachedEntity user = db.getCurrentUser();
		
		//get the character that was selected.
		Long friendId = tryParseId(parameters,"characterId");		
		CachedEntity friendChar = db.getCharacterById(friendId);
		
		if(friendChar == null)
			throw new UserErrorMessage("Somehow you picked a non-existant person to add as a friend.");
		
		//get the user to add
		CachedEntity friendUser = db.getEntity((Key) friendChar.getProperty("userKey"));
		
		if(friendUser == null)
			throw new UserErrorMessage("That character does not have a user account (possibly a throwaway?). They can't be your friend.");
		
		//create the new Friend entity
		CachedEntity newFriend = new CachedEntity("Friend");
		newFriend.setProperty("userKey", user.getKey());
		newFriend.setProperty("friendUserKey", friendUser.getKey());
		//Summary is not used by the game itself, but when you're creating a new friend, set this to the friendUserKey's 
		//current character name. It is simply meant to make it easier to browse friends lists in the editor.
		newFriend.setProperty("summary",friendChar.getProperty("name"));
		
		//save to data store
		ds.put(newFriend);
	}

}
