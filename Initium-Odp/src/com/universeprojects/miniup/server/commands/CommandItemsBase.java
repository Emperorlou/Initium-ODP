package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Base class that processes a list of items.
 * Items are passed in as "itemIds" in the parameter map.
 * Implementing classes must override getEntityType if they process a different Entity.
 * 
 * @author SPFiredrake
 * 
 */
public abstract class CommandItemsBase extends Command {

	protected List<Long> processedItems = new ArrayList<Long>();
	public CommandItemsBase(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		String[] itemStrings = parameters.get("itemIds").split(",");
		List<Key> itemKeys = new ArrayList<Key>();
		for(String id:itemStrings)
		{
			try
			{
				// If we can't parse the ID, then skip processing of the item.
				Long itemId = Long.parseLong(id);
				Key itemKey = KeyFactory.createKey(getEntityType(), itemId);
				itemKeys.add(itemKey);
			}
			catch(NumberFormatException nfe)
			{}
		}
		
		List<CachedEntity> batchItems = ds.get(itemKeys);
		processBatchItems(parameters, db, ds, character, batchItems);
		
		if(processedItems.isEmpty()==false)
		{
			StringBuilder modifiedItems = new StringBuilder();
			for(Long itemId:processedItems)
			{
				modifiedItems.append(",");
				modifiedItems.append(itemId);
			}
			modifiedItems.replace(0, 1, "");
			addCallbackData("processedItems", modifiedItems.toString());
		}
	}
	
	/**
	 * Performs the command functions on the retrieved list of items.
	 */
	protected abstract void processBatchItems(Map<String, String> parameters, ODPDBAccess db, CachedDatastoreService ds, CachedEntity character, List<CachedEntity> batchItems) throws UserErrorMessage;

	/**
	 * Specifies the entity type the specified itemIds reference, used to generate the Key
	 * which will fetch the entities from the cache.
	 * @return Key entity type to generate
	 */
	protected String getEntityType()
	{
		return "Item";
	}
}
