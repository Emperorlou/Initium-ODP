package com.universeprojects.miniup.server.aspects;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.InitiumAspect;
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
	public List<Key> getStoredScripts(){
		return (List<Key>) getProperty("storedScripts");
	}
	
	public List<String> getStoredAspects(){	
		//TODO: Unimplemented.
		return null;
		//return (List<String>) getProperty("storedAspects");
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getStoredModifiers(){
		return (List<String>) getProperty("storedModifiers");
	}
}