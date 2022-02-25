package com.universeprojects.miniup.server.services;

import java.util.*;
import java.util.Map.Entry;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class ItemFilterService extends Service{
	
	private final String[] qualities = new String[]{"item-junk", "item-normal", "item-rare", "item-unique", "item-magic", "item-epic", "item-custom", "item-event", "item-legendary"};
	private final List<String> qual = Arrays.asList(qualities);

	private Map<String, Integer> qualityMap = null;

	public ItemFilterService(ODPDBAccess db) {
		super(db);
	}
	
	public void addItemFilter(CachedEntity item) throws UserErrorMessage {

		Map<String, String> filters = getFilters();	
		filters.put((String) item.getProperty("name"), GameUtils.determineQuality(item.getProperties()));

		if(filters.size() > 100) throw new UserErrorMessage("You have too many filters! Try getting rid of some.");

		setFilters(filters);
		
	}

	public String removeItemFilter(String name){
		Map<String, String> filters = getFilters();

		String result = filters.remove(name);

		setFilters(filters);

		return result;
	}
	
	public void removeAllFilters() {
		db.getCurrentCharacter().setProperty("itemFilters", null);
		filterCache = null;
	}
	
	public boolean allowItem(CachedEntity item) {
		
		Map<String, String> filters = getFilters();
		
		if(filters == null || filters.size() == 0) return true; //no filters? return true.
		
		for(Entry<String, String> entry : filters.entrySet()) {

			//wrong filter? continue
			if(GameUtils.equals(entry.getKey(), item.getProperty("name")) == false) continue;

			//if the index of the items quality is HIGHER than the index of the filter's quality, return true
			return isQualityBetterThan(GameUtils.determineQuality(item.getProperties()), entry.getValue());
		}

		//if we dont have a filter for this item, return true
		return true;
	}

	//this method tests if the item's quality is higher than the filter's quality.
	public boolean isQualityBetterThan(String itemQuality, String filterQuality) {

		return qual.indexOf(itemQuality) > qual.indexOf(filterQuality);
	}
	
	/**
	 * Grab the filters stored on the character. Cache the result, so we dont have to do the map parsing algorithm every time
	 * @return
	 */
	private Map<String, String> filterCache = null;
	public Map<String, String> getFilters(){

		if(filterCache != null) return filterCache;

		filterCache = db.getValue_StringStringMap(db.getCurrentCharacter(), "itemFilters");
		if(filterCache == null)
			filterCache = new HashMap<>();
		
		return filterCache;
	}

	public void setFilters(Map<String, String> filters){
		db.setValue_StringStringMap(db.getCurrentCharacter(), "itemFilters", filters);

		filterCache = null;
	}
}
