package com.universeprojects.miniup.server.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;

public class ItemFilterService extends Service{
	
	private final String[] qualities = new String[]{"item-junk", "item-normal", "item-rare", "item-unique", "item-magic", "item-epic", "item-custom"};
	private Map<String, Integer> qualityMap = null;

	public ItemFilterService(ODPDBAccess db) {
		super(db);
	}
	
	public void addItemFilter(CachedEntity item) {
		
		CachedEntity character = db.getCurrentCharacter();
		
		Map<String, String> filters = getFilters();	
		filters.put((String) item.getProperty("name"), GameUtils.determineQuality(item.getProperties()));
		
		db.setValue_StringStringMap(character, "itemFilters", filters);
	}
	
	public void removeAllFilters() {
		db.getCurrentCharacter().setProperty("itemFilters", null);
	}
	
	public boolean allowItem(CachedEntity item) {
		
		Map<String, String> filters = getFilters();
		
		if(filters == null || filters.size() == 0) return true; //no filters? return true.
		
		for(Entry<String, String> entry : filters.entrySet()) {
			
			if(GameUtils.equals(entry.getKey(), item.getProperty("name")) == false) continue;
			
			return isQualityBetterThan(GameUtils.determineQuality(item.getProperties()), entry.getValue());
		}
		
		return true;
	}
	
	public boolean isQualityBetterThan(String item, String filter) {
		return getQualityMap().get(item) > getQualityMap().get(filter);
	}
	
	/**
	 * it might be worth ditching this in favor of a list?
	 * @return
	 */
	private Map<String, Integer> getQualityMap(){
		if(qualityMap != null)
			return qualityMap;
		
		qualityMap = new HashMap<>();
		
		for(int i = 0; i != qualities.length; i++) 
			qualityMap.put(qualities[i], i);
				
		return qualityMap;
	}
	
	/**
	 * is it faster to cache this?
	 * @return
	 */
	public Map<String, String> getFilters(){
		return db.getValue_StringStringMap(db.getCurrentCharacter(), "itemFilters");
	}
}
