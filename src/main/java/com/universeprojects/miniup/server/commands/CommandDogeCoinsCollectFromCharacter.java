package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.TransactionCommand;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ContainerService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;
import com.universeprojects.miniup.server.services.QuestService;

/**
 * Collects all gold from the specified character ID
 * 
 * Parameters:
 * 		characterId - ID of the character entity we'll be collecting from 
 * 
 * @author SPFiredrake
 *
 */
public class CommandDogeCoinsCollectFromCharacter extends TransactionCommand {

	public CommandDogeCoinsCollectFromCharacter(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}
	

	@Override
	public void runBeforeTransaction(Map<String, String> parameters) throws UserErrorMessage
	{
		
	}

	@Override
	public void runInsideTransaction(Map<String, String> parameters) throws UserErrorMessage
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		character = ds.refetch(character);

		long charId = tryParseId(parameters, "characterId");
		CachedEntity collectFromCharacter = db.getEntity("Character", charId);
		if(collectFromCharacter == null)
			throw new UserErrorMessage("Body no longer exists");
		
		if(GameUtils.isPlayerIncapacitated(collectFromCharacter)==false)
			throw new UserErrorMessage("Cannot collect coins from a living character");
		
		if(GameUtils.isPlayerIncapacitated(character))
			throw new UserErrorMessage("Cannot collect coins when incapacitated.");
		
		// If character we collect from has no coins, we don't need to do anything.
		if(collectFromCharacter.getProperty("dogecoins").equals(0L))
			return;
		
		if (GameUtils.equals(collectFromCharacter.getKey(), character.getKey()))
			throw new UserErrorMessage("You cannot collect coins from yourself.");
		
		ContainerService cs = new ContainerService(db);
		
		// Check whether character can access this body's container/location.
		CachedEntity otherCharacterLocation = db.getEntity((Key)collectFromCharacter.getProperty("locationKey"));
		if(cs.checkContainerAccessAllowed(character, otherCharacterLocation)==false)
			throw new UserErrorMessage("You cannot collect coins from this body because you're not in the same location as it.");
		
		Long characterCoins = (Long)character.getProperty("dogecoins");
		Long collectCoins = (Long)collectFromCharacter.getProperty("dogecoins");
		character.setProperty("dogecoins", characterCoins + collectCoins);
		collectFromCharacter.setProperty("dogecoins", 0L);
		
		
		// Order matters! Always subtract coins first!
		ds.put(collectFromCharacter);
		ds.put(character);
		
		MainPageUpdateService service = MainPageUpdateService.getInstance(db, db.getCurrentUser(), character, null, this);
		service.updateMoney();
		
	}

	@Override
	public void runAfterTransaction(Map<String, String> parameters) throws UserErrorMessage
	{
		
	}

}
