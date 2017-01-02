package com.universeprojects.miniup.server.domain;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

//This entity defines a group of locations that are controllable by a player.
public class Territory extends OdpDomain {
	public static final String KIND = "Territory";

	public Territory() {
		super(new CachedEntity(KIND));
	}

	public Territory(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	// (Character)
	public void setCharacterWhitelist(List<Character> characterWhitelist) {
		getCachedEntity().setProperty("characterWhitelist", characterWhitelist);
	}

	@SuppressWarnings("unchecked")
	public List<Character> getCharacterWhitelist() {
		return (List<Character>) getCachedEntity().getProperty("characterWhitelist");
	}

	// (Group)
	public void setGroupWhitelist(List<Group> groupWhitelist) {
		getCachedEntity().setProperty("groupWhitelist", groupWhitelist);
	}

	@SuppressWarnings("unchecked")
	public List<Group> getGroupWhitelist() {
		return (List<Group>) getCachedEntity().getProperty("groupWhitelist");
	}

	// (Location|territoryKey)
	public void setLocations(List<Location> locations) {
		getCachedEntity().setProperty("locations", locations);
	}

	@SuppressWarnings("unchecked")
	public List<Location> getLocations() {
		return (List<Location>) getCachedEntity().getProperty("locations");
	}

	// A name given to this territory.
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	// (Group)
	public void setOwningGroupKey(Key owningGroupKey) {
		getCachedEntity().setProperty("owningGroupKey", owningGroupKey);
	}

	public Key getOwningGroupKey() {
		return (Key) getCachedEntity().getProperty("owningGroupKey");
	}

	public enum TravelRule {
		None, OwningGroupOnly,
	}

	public void setTravelRule(TravelRule travelRule) {
		getCachedEntity().setProperty("travelRule", travelRule);
	}

	public TravelRule getTravelRule() {
		return (TravelRule) getCachedEntity().getProperty("travelRule");
	}

}
