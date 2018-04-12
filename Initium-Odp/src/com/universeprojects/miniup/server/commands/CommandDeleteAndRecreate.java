package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.AbortTransactionException;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.Transaction;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPAuthenticator;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class CommandDeleteAndRecreate extends Command {

	public CommandDeleteAndRecreate(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException 
	{
		final ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
        
        String name = parameters.get("name");
        if (name==null) throw new UserErrorMessage("Character name cannot be blank.");
        
        CachedEntity currentChar = db.getCurrentCharacter();
        if(currentChar == null) throw new RuntimeException("Current character entity is null");
        
        // Only sanitize if character name differs.
        if(name.equals(currentChar.getProperty("name"))==false)
        {
	        name = db.cleanCharacterName(name);
	        if (name.length()<1 || name.length()>30 || !name.matches("[A-Za-z ]+"))
	            throw new UserErrorMessage("Character name must contain only letters and spaces, and must be between 1 and 40 characters long.");
        }

        // Check if the name is already in use
        if (name.equals(currentChar.getProperty("name"))==false && db.checkCharacterExistsByName(name))
            throw new UserErrorMessage("Character name is already in use.");
        
        // Cannot be in combat.
        if(CommonChecks.checkCharacterIsBusy(currentChar))
        	throw new UserErrorMessage("Character is currently busy.");
        
        // Check if this user has changed names too many times in the past 10 minutes (only 1 name change in 10 mins allowed)
        String ip = WebUtils.getClientIpAddr(request);
        if (name.equals(currentChar.getProperty("name"))==false)
        {
            if (ds.flagActionLimiter("startOverLimiter-"+ip, 600, 1))
                throw new UserErrorMessage("You cannot change your name more than once in 10 minutes.");
        }

        // Being in a party seems to mess things up, so leave the party first.
        db.doLeaveParty(ds, currentChar);
        
        // Drop all characters.
        List<CachedEntity> carryingChars = db.getFilteredList("Character", "locationKey", currentChar.getKey());
        if(carryingChars != null && carryingChars.isEmpty() == false)
        {
        	CachedEntity location = ds.getIfExists((Key)currentChar.getProperty("locationKey"));
        	ds.beginBulkWriteMode();
        	for(CachedEntity carried:carryingChars)
        	{
        		try
        		{
        			db.doCharacterDropCharacter(location, currentChar, carried);
        		}
        		catch(Exception ex)
        		{
        			carried.setProperty("locationKey", location.getKey());
        			ds.put(carried);
        		}
        	}
        	ds.commitBulkWrite();
        }
        
        final Key characterKey = currentChar.getKey();
        final Key userKey = (Key)currentChar.getProperty("userKey");
        final ODPAuthenticator auth = this.getAuthenticator();
        final String newName = name;
        CachedEntity newChar = null;
        
        try {
			newChar = (CachedEntity) new Transaction<CachedEntity>(ds) {

				@Override
				public CachedEntity doTransaction(CachedDatastoreService ds) throws AbortTransactionException {

					CachedEntity oldChar = ds.refetch(characterKey);
					CachedEntity curUser = null; 
					if(userKey != null) curUser = ds.refetch(userKey);
					
					ds.delete(characterKey);
			        
			        CachedEntity newChar = db.newPlayerCharacter(null, auth, curUser, newName, oldChar);

					ds.put(newChar);

					return newChar;
				}
			}.run();
		}
		catch (AbortTransactionException e) {
			throw new RuntimeException(e);
		}
        
        CachedEntity user = ds.getIfExists(userKey);
        if(user != null)
        {
	        // Delete old discoveries and discover the paths that belong to this 
        	// user automatically. Let it fail silently, but log the exception.
	        try
	        {
	        	db.doDeleteCharacterDiscoveries(ds, characterKey);
	            db.discoverAllPropertiesFor(ds, user, newChar);
	        }
	        catch(Exception ex)
	        {
	        	Logger.getLogger(CommandDeleteAndRecreate.class.getName()).log(Level.SEVERE, "Failed to discover user properties for recreated character!", ex);
	        }
        }

		// Set the cached currentCharacter to target
		request.setAttribute("characterEntity", newChar);
		
		// Update the verifyCode to the new character
		StringBuilder js = new StringBuilder();
		try {
			js.append("window.verifyCode = '" + db.getVerifyCode() + "';");
		} catch (NotLoggedInException e) {
			js.append("window.verifyCode = '';");
		}

		js.append("window.chatIdToken = '" + db.getChatIdToken(newChar.getKey()) + "';");
		js.append("window.messager.idToken = window.chatIdToken;");
		js.append("window.characterId = " + newChar.getId() + ";");

		addJavascriptToResponse(js.toString());
		
		// Consolidating this to quick refresh the page
		CachedEntity location = ds.getIfExists((Key)newChar.getProperty("locationKey"));
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, user, newChar, location, this);
		mpus.updateFullPage_shortcut();
	}

}
