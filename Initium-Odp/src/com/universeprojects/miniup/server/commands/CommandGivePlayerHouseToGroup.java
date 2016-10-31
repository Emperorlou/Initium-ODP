package com.universeprojects.miniup.server.commands;

import java.util.List;
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

public class CommandGivePlayerHouseToGroup extends Command{
	
	public CommandGivePlayerHouseToGroup(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		CachedEntity user = db.getCurrentUser();
		CachedEntity character = db.getCurrentCharacter();
		
		Key charactersGroupKey = (Key) character.getProperty("groupKey");
		CachedEntity charactersGroup = db.getEntity(charactersGroupKey);	
		
		CachedEntity playerHouse = db.getEntity((Key) character.getProperty("locationKey"));
		
		if (charactersGroup==null){
			throw new UserErrorMessage("You are not part of a group and cannot do this.");
		}
		
		if (!(GameUtils.equals(playerHouse.getProperty("ownerKey"), user.getKey()))){
			throw new UserErrorMessage("You do not own this house and therefore cannot assign it to your group.");		
		}					
		// Get the path to the player house too...
		List<CachedEntity> paths = db.getPathsByLocation(playerHouse.getKey());
		
		if (paths.size()!=1 || "PlayerHouse".equals(paths.get(0).getProperty("type"))==false) {
			throw new UserErrorMessage("You can only do this for player houses.");
		}
		
		ds.beginTransaction();
		try{
			// Now set all the path and the location's owner to the player's group
			for(CachedEntity p:paths)
			{
				p.setProperty("ownerKey", charactersGroup.getKey());
				ds.put(p);
			}
			
			playerHouse.setProperty("ownerKey", charactersGroup.getKey());
			playerHouse.setProperty("description", "This house is the property of "+charactersGroup.getProperty("name")+". Feel free to leave equipment and gold here, but bear in mind that everyone in the group has access to this house.");
			
			ds.put(playerHouse);			
			
			ds.commit();
			
			// Now go through all group members and give them the house discovery...
			List<CachedEntity> members = db.getFilteredList("Character", "groupKey", charactersGroup.getKey());
			for(CachedEntity m:members){
				db.newDiscovery(ds, m, paths.get(0));		
			}
	
		} catch (Exception e) {
			throw e;
		} finally {
			ds.rollbackIfActive();
		}
		
		setJavascriptResponse(JavascriptResponse.FullPageRefresh);

		
	}




}
