package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandBuyHouse extends Command {
	public static final long DOGECOIN_COST = 2000;

	public CommandBuyHouse(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();

		CachedEntity user = db.getCurrentUser();
		CachedEntity character = db.getCurrentCharacter();

		CachedEntity currentLocation = db.getEntity((Key) character.getProperty("locationKey"));
		String houseName = db.cleanCharacterName(parameters.get("houseName"));

		if (!houseName.matches("[A-Za-z0-9, ]+")) {
			throw new UserErrorMessage("A property name can only have letters, numbers, commas, and spaces in the name.");
		}
		if (houseName.length() > 40) {
			throw new UserErrorMessage("A property name can be up to 40 characters long.");
		}

		if ("CityHall".equals(currentLocation.getProperty("type")) == false)
			throw new UserErrorMessage("You cannot buy a house here. You're not in a town hall.");
		Long dogecoins = (Long) character.getProperty("dogecoins");
		if (dogecoins < DOGECOIN_COST)
			throw new UserErrorMessage("You do not have enough gold to buy a house at this time. You have " + dogecoins + " but require at least " + DOGECOIN_COST + ". Come back when you have " + (DOGECOIN_COST - dogecoins) + " more.");

		ds.beginTransaction();
		CachedEntity playerHouse = null;
		try {
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
			playerHouse = db.newLocation(ds, "images/special-house2.jpg", houseName, "This is " + character.getProperty("name") + "'s property called '" + houseName + "'! No one can go here unless they have the location shared with them. Feel free to store equipment and cash here!", -1d, "RestSite", cityLocation, user.getKey());

			// Connect a path from the house to the city where we bought it
			CachedEntity pathToHouse = db.newPath(ds, "Path to house - " + houseName, playerHouse.getKey(), "Leave " + houseName, cityLocation, "Go to " + houseName, 0d, 0l, "PlayerHouse");
			pathToHouse.setProperty("ownerKey", user.getKey());

			// Save path and have our character discover it
			ds.put(pathToHouse);
			db.doCharacterDiscoverEntity(ds, character, pathToHouse);
			
			// Finally subtract the money from the player's character
			character.setProperty("dogecoins", dogecoins - DOGECOIN_COST);
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

		setPopupMessage("Your house is ready for you! Just leave " + currentLocation.getProperty("name") + " and you'll find it right away. It's called '" + playerHouse.getProperty("name") + "'.");
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}
}
