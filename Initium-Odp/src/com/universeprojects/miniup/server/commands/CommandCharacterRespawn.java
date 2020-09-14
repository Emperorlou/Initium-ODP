package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class CommandCharacterRespawn extends Command
{

	public CommandCharacterRespawn(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		
		CachedEntity user = db.getCurrentUser();
		CachedEntity deadCharacter = db.getCurrentCharacter();

		CachedDatastoreService ds = db.getDB();
			
		if(!CommonChecks.checkCharacterIsIncapacitated(deadCharacter)) throw new UserErrorMessage("You can't respawn a character that's alive...");	

		deadCharacter.setProperty("userKey", null);
		//Commit deadcharacter to DB?
		
		CachedEntity newCharacter = db.doCreateNewCharacterFromDead(ds, getAuthenticator(), user, deadCharacter);
		
		setPopupMessage("You have been given a new character by the name of "+newCharacter.getProperty("name")+". When your last character died, he dropped all of his gear where he was standing and it should still be there if no one else has found it yet. If you had anything worth keeping, you could attempt to recover it with this new character, but whatever killed you is probably still near your corpse - beware!");
		
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, user, newCharacter, db.getCharacterLocation(newCharacter), this);
		mpus.updateFullPage_shortcut(true);
	}
}
