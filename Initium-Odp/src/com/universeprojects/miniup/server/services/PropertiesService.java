package com.universeprojects.miniup.server.services;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class PropertiesService extends Service {
	
	public PropertiesService(ODPDBAccess db) {
		super(db);
	}
	
	public CachedEntity buyHouse(ODPDBAccess db, CachedDatastoreService ds, CachedEntity user, CachedEntity character, CachedEntity currentLocation, String houseName, long cost) throws UserErrorMessage {

		if (!houseName.matches("[A-Za-z0-9, ]+")) {
			throw new UserErrorMessage("A property name can only have letters, numbers, commas, and spaces in its name.");
		}
		if (houseName.length() > 40) {
			throw new UserErrorMessage("A property name can be up to 40 characters long.");
		}
		
		if ("CityHall".equals(currentLocation.getProperty("type")) == false)
			throw new UserErrorMessage("You cannot buy a house here. You're not in a town hall.");
		Long dogecoins = (Long) character.getProperty("dogecoins");
		if (dogecoins < cost)
			throw new UserErrorMessage("You do not have enough gold to buy a house at this time. You have " + dogecoins + " but require at least " + cost + ". Come back when you have " + (cost - dogecoins) + " more.");
		
		ds.beginTransaction();
		CachedEntity playerHouse = null;
		try {
			character.refetch(ds);
			// Get the path from the city hall, we will create the house branching from the other end of it...
			List<CachedEntity> paths = db.getPathsByLocation(currentLocation.getKey());
			if (paths.isEmpty())
				throw new RuntimeException("Invalid city hall. No paths from this location were found.");
			CachedEntity pathFromCityHall = paths.get(0);

			// Figure out which end of the path is the location key for the city we'll put the house in
			Key cityLocation = null;
			Key location1Key = (Key) pathFromCityHall.getProperty("location1Key");
			Key location2Key = (Key) pathFromCityHall.getProperty("location2Key");
			if (currentLocation.getKey().getId() == location1Key.getId()) {
				cityLocation = location2Key;
			} else {
				cityLocation = location1Key;
			}

			// House = Location, so create that here, with the city as the parent location
			playerHouse = new CachedEntity("Location");
			// Set the starting attributes
			playerHouse.setProperty("banner", "https://initium-resources.appspot.com/images/special-house2.jpg");
			playerHouse.setProperty("name", houseName);
			playerHouse.setProperty("description", "This is " + character.getProperty("name") + "'s property called '" + houseName + "'! No one can go here unless they have the location shared with them. Feel free to store equipment and cash here!");
			playerHouse.setProperty("discoverAnythingChance", -1d);
			playerHouse.setProperty("type", "RestSite");
			playerHouse.setProperty("parentLocationKey", cityLocation);
			playerHouse.setProperty("ownerKey", user.getKey());
			playerHouse.setProperty("isOutside", "FALSE");
			playerHouse.setProperty("supportsCampfires", 1L);
			playerHouse.setProperty("createdDate", new Date());

			ds.put(playerHouse);
			
			// Connect a path from the house to the city where we bought it
			CachedEntity pathToHouse = db.newPath(ds, "Path to house - " + houseName, playerHouse.getKey(), "Leave " + houseName, cityLocation, "Go to " + houseName, 0d, 0l, "PlayerHouse");
			pathToHouse.setProperty("ownerKey", user.getKey());

			// Save path and have our character discover it
			ds.put(pathToHouse);
			db.doCharacterDiscoverEntity(ds, character, pathToHouse);
			
			// Finally subtract the money from the player's character
			character.setProperty("dogecoins", dogecoins - cost);
			character.setProperty("locationKey", playerHouse.getKey());
			ds.put(character);

			ds.commit();
			
			// Give all the player alts the discovery of the path now...			
			List<CachedEntity> userCharacters = db.getFilteredList("Character", "userKey", user.getKey());
			for(CachedEntity characterEntity:userCharacters)
			{
				// If discovery already exists, nothing happens
				db.doCharacterDiscoverEntity(ds, characterEntity, pathToHouse);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			ds.rollbackIfActive();
		}
		
		return playerHouse;
	}
	
	public void rediscoverHouses(CachedEntity user)
	{
		List<CachedEntity> userCharacters = db.getFilteredList("Character", "userKey", user.getKey());
		for(CachedEntity character:userCharacters)
			db.discoverAllPropertiesFor(null, user, character);
	}
	
	public boolean doesCharacterOwnHouse(CachedEntity location, CachedEntity character)
	{
		Key ownerKey = (Key)location.getProperty("ownerKey");
		return GameUtils.equals(character.getProperty("userKey"), ownerKey);
	}
	
	public boolean doesUserOwnHouse(CachedEntity location, CachedEntity user)
	{
		Key ownerKey = (Key)location.getProperty("ownerKey");
		return GameUtils.equals(user.getKey(), ownerKey);
	}
}
