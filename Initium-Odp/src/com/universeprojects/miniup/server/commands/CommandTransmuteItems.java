package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Provides functionailty for transmuting a combination of items into a new resulting item.
 * 
 * Parameters:
 * 		containerId - ID of the player's transmute box
 * 
 * @author jDyn
 * 
 */
public class CommandTransmuteItems extends Command {
	public CommandTransmuteItems(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}
	
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		Long containerId = tryParseId(parameters, "containerId");
		Key containerKey = KeyFactory.createKey("Item", containerId);
		
		List<CachedEntity> materials = db.getFilteredList("Item", "containerKey", FilterOperator.EQUAL, containerKey);
		
		if (materials.size() < 2)
			throw new UserErrorMessage("You must select at least two items to transmute.");
		
		List<Key> materialsKeys = new ArrayList<Key>();
		
		for (CachedEntity material:materials) {
			materialsKeys.add((Key) material.getProperty("_definitionKey"));
		}
		
		// sorting for ease of comparing this list to other material lists
		Collections.sort(materialsKeys);
		
		List<CachedEntity> recipes = db.getFilteredList("TransmuteRecipe", "materials", FilterOperator.EQUAL, materialsKeys);
		
		for (CachedEntity recipe:recipes) {
			List<Key> recipeMaterialsKeys = (List<Key>) recipe.getProperty("materials");
			
			if (recipeMaterialsKeys.size() != materialsKeys.size())
				recipes.remove(recipe);
			else {
				Collections.sort(recipeMaterialsKeys);
				if (!recipeMaterialsKeys.equals(materialsKeys))
					recipes.remove(recipe);
			}
		}
		
		if (recipes.size() == 0)
			throw new UserErrorMessage("You tried transmuting the items, but nothing happened.");
		else if (recipes.size() == 1) {
			CachedEntity recipe = recipes.get(0);
			List<Key> results = (List<Key>) recipe.getProperty("results");
			CachedEntity resultItem = null;
			
			for (Key result:results) {
				resultItem = db.generateNewObject(db.getEntity(result), "Item");
				
				// put the item(s) in character's transmute box
				resultItem.setProperty("containerKey", containerKey);
				resultItem.setProperty("movedDate", new Date());
				
				ds.put(resultItem);
			}
			
			// now remove the transmuted materials from player
			for (CachedEntity item:materials) {
				item.setProperty("containerKey", null);
				item.setProperty("movedDate", new Date());
				
				ds.put(item);
			}
		}
		else {
			String recipeNames = (String) recipes.get(0).getProperty("name");
			
			for (int i=1; i<recipes.size(); i++) {
				recipeNames.concat(", " + recipes.get(i));
			}
			
			throw new RuntimeException("Duplicate recipes: " + recipeNames);
		}
	}
}
