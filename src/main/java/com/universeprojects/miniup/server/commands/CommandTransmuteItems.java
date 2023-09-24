package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.universeprojects.cacheddatastore.AbortTransactionException;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.Transaction;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ContainerService;

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
		final ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		ContainerService cs = new ContainerService(db);
		Long containerId = tryParseId(parameters, "containerId");
		final Key containerKey = KeyFactory.createKey("Item", containerId);
		
		CachedEntity container = db.getEntity(containerKey);
		
		if (cs.checkContainerAccessAllowed(character, container)==false)
			throw new UserErrorMessage("You do not have access to this container.");
		
		if (GameUtils.equals(container.getProperty("transmuteEnabled"), true)==false)
			throw new UserErrorMessage("You can only transmute items that are in a valid transmuting container.");
		
		final List<CachedEntity> materials = db.getFilteredList("Item", "containerKey", FilterOperator.EQUAL, containerKey);
		
		List<Key> materialsKeys = new ArrayList<Key>();
		
		for (CachedEntity material:materials) {
			// While we're here, check if any of the mats are a custom item
			if (CommonChecks.isItemCustom(material))
				throw new UserErrorMessage("You cannot use custom items as materials.");
			
			if (GameUtils.equals(material.getProperty("quantity"), null))
				materialsKeys.add((Key) material.getProperty("_definitionKey"));
			else {
				long quantity = (long) material.getProperty("quantity");
				
				for (int i = 0; i < quantity; i++) {
					materialsKeys.add((Key) material.getProperty("_definitionKey"));
				}
			}
		}
		
		if (materialsKeys.size() < 2)
			throw new UserErrorMessage("You must select at least two items to transmute.");
		
		// sorting for ease of comparing this list to other material lists
		Collections.sort(materialsKeys);
		
		CompositeFilter f = buildFilter("materials", materialsKeys, FilterOperator.EQUAL, CompositeFilterOperator.AND);
		List<CachedEntity> recipes = ds.fetchAsList("TransmuteRecipe", f, 1000);
		
		// go through the recipes list and remove the ones that don't match
		Iterator<CachedEntity> iter = recipes.iterator();
		CachedEntity next = null;
		List<Key> recipeMaterialsKeys = null;
		
		while (iter.hasNext()) {
			next = iter.next();
			recipeMaterialsKeys = (List<Key>) next.getProperty("materials");
			Collections.sort(recipeMaterialsKeys);
			
			if (!listEquals(recipeMaterialsKeys, materialsKeys))
				iter.remove();
		}
		
		// perform the actual transmutation of the items
		if (recipes.size() == 0) {
			cs.doUse(ds, container, 1);
			ds.put(container);
			throw new UserErrorMessage("You tried transmuting the items, but nothing happened.");
		}
		else if (recipes.size() == 1) {
			CachedEntity recipe = recipes.get(0);
			final List<CachedEntity> results = db.getEntities((List<Key>)recipe.getProperty("results"));
			
			try {
				container = (CachedEntity) new Transaction<CachedEntity>(ds) {
				
					@Override
					public CachedEntity doTransaction(CachedDatastoreService ds) throws AbortTransactionException {
						CachedEntity container = db.getEntity(containerKey);
						boolean hcm = CommonChecks.checkIsHardcore(character);
						
						// Remove the transmuted materials from player first
						for (CachedEntity item:materials) 
						{
							CachedEntity rfItem = refetch(item);
							
							rfItem.setProperty("containerKey", null);
							rfItem.setProperty("movedTimestamp", new Date());
							// For any equipable items, check if they are HCM. This will determine HCM status of resulting items.
							if(hcm && CommonChecks.checkItemIsEquippable(rfItem) && CommonChecks.checkIsHardcore(rfItem) == false)
								hcm = false;
							
							ds.put(rfItem);
						}
						
						for (CachedEntity result:results) 
						{
							CachedEntity resultItem = db.generateNewObject(result, "Item");
							
							// put the item(s) in character's transmute box
							resultItem.setProperty("containerKey", containerKey);
							resultItem.setProperty("movedTimestamp", new Date());
							if(resultItem.getProperty("durability") != null)
								resultItem.setProperty("maxDurability", resultItem.getProperty("durability"));
							
							if(CommonChecks.checkItemIsEquippable(resultItem))
								resultItem.setProperty("hardcoreMode", hcm);
							
							ds.put(resultItem);
						}
						
						return container;
					}
					
				}.run();
				
				cs.doUse(ds, container, 1);
				ds.put(container);
				setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
			}
			catch (AbortTransactionException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			String recipeNames = (String) recipes.get(0).getProperty("name");
			
			for (int i=1; i<recipes.size(); i++) {
				recipeNames.concat(", " + recipes.get(i).getProperty("name"));
			}
			
			throw new RuntimeException("Duplicate recipes: " + recipeNames);
		}
	}
	
	/**
	 * Builds a composite filter from a list of any size, that can be used in a query
	 * 
	 * @param property - entity property to filter on
	 * @param keys - list of Keys to each be used as a value for a filter
	 * @param operator - operator to use for each individual filter
	 * @param compositeOperator - operator to use to tie filters together
	 * 
	 * @return CompositeFilter 
	 */
	private CompositeFilter buildFilter(String property, List<Key> keys, FilterOperator operator, CompositeFilterOperator compositeOperator) {
		
		if (keys.size() == 0)
			return null;
		
		List<Filter> filterList = new ArrayList<Filter>();
		
		for (Object key:keys) {
			filterList.add(new FilterPredicate(property, operator, key));
		}
		
		CompositeFilter filter = new CompositeFilter(compositeOperator, filterList);
		
		return filter;
	}
	
	/**
	 * Checks the equality of each entity in two key lists, based on ID
	 * 
	 * @param l1 - List to compare with l2
	 * @param l2 - List to compare with l1
	 * 
	 * @return true/false
	 */
	private boolean listEquals(List<Key> l1, List<Key> l2) {
		
		if (l1.size() != l2.size())
			return false;
		
		for (int i=0; i<l1.size(); i++) {
			if (!GameUtils.equals(l1.get(i), l2.get(i)))
				return false;
		}
		
		return true;
	}
}
