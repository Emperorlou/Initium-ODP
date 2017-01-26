package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class CommandPlayerReadMap extends Command {

	public CommandPlayerReadMap(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		// TODO Auto-generated method stub
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity user = db.getCurrentUser();
		
		Long itemId = tryParseId(parameters, "itemId");
		Long pathId = tryParseId(parameters, "pathId");
		
		CachedEntity item = db.getEntity("Item", itemId);
		CachedEntity path = db.getEntity("Path", pathId);
		CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
		
		// Validation
		if(item == null) throw new UserErrorMessage("This item does not exist");
		if(path == null) throw new UserErrorMessage("This path does not exist");
		if(location == null) throw new RuntimeException("Character location is null");
		
		if("Map to Location".equals(item.getProperty("name"))==false) throw new UserErrorMessage("The specified item is not a map.");
		if(GameUtils.equals(item.getProperty("containerKey"), character.getKey()) == false)
			throw new UserErrorMessage("You are not currently in possession of this map!");
		
		CombatService cs = new CombatService(db);
		if(cs.isInCombat(character))
			throw new UserErrorMessage("You cannot read the map while in combat!");
		
		// Always handle durability. 
		if(item.getProperty("durability") != null)
		{
			Long durability = (Long)item.getProperty("durability");
			if(durability > 1)
			{
				durability -= 1;
				item.setProperty("durability", durability);
				ds.put(item);
			}
			else
			{
				ds.delete(item);
			}
			setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
		}
		// Verify map is for the correct specified path. Do this after the durability drain.
		if(GameUtils.equals(item.getProperty("keyCode"), pathId) == false)
			throw new UserErrorMessage("You are unable to decipher the map.");
		
		// Doesn't matter if they already know the path. Teach it to them anyway.
		db.doCharacterDiscoverEntity(ds, character, path);
		// If they are at either end of the path, refresh the button list so they can see it.
		if(GameUtils.equals(location.getKey(), path.getProperty("location1Key")) ||
				GameUtils.equals(location.getKey(), path.getProperty("location2Key")))
		{
			MainPageUpdateService mpus = new MainPageUpdateService(db, user, character, location, this);
			mpus.updateButtonList(cs);
		}
	}

}
