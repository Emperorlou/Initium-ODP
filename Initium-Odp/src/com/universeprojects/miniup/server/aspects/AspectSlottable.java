package com.universeprojects.miniup.server.aspects;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;

/**
 * 
 * @author Evan
 *
 */
public class AspectSlottable extends ItemAspect{

	public AspectSlottable(InitiumObject object) {
		super(object);
	}
	
	//This aspect isn't usable on it's own, therefore no popup.
	@Override
	public List<ItemPopupEntry> getItemPopupEntries(CachedEntity currentCharacter) {
		return null;
	}

	@Override
	public String getPopupTag() {
		return "Slottable";
	}
	
	@SuppressWarnings("unchecked")
	public List<Key> getScripts(){
		return (List<Key>) getProperty("storedScripts");
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getAspects(){
		return (List<String>) getProperty("storedAspects");
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getModifiers(){
		return (List<String>) getProperty("storedModifiers");
	}


}