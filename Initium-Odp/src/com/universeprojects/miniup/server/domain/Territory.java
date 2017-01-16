package com.universeprojects.miniup.server.domain;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * This entity defines a group of locations that are controllable by a player.
 * 
 * @author kyle-miller
 *
 */
public class Territory extends OdpDomain {
	public static final String KIND = "Territory";

	public Territory() {
		super(new CachedEntity(KIND));
	}

	private Territory(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	public static final Territory wrap(CachedEntity cachedEntity) {
		return new Territory(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 *  (Character)
	 *  
	 * @param characterWhitelist
	 */
	public void setCharacterWhitelist(List<Character> characterWhitelist) {
		getCachedEntity().setProperty("characterWhitelist", characterWhitelist);
	}

	@SuppressWarnings("unchecked")
	public List<Character> getCharacterWhitelist() {
		return (List<Character>) getCachedEntity().getProperty("characterWhitelist");
	}

	/**
	 *  (Group)
	 *  
	 * @param groupWhitelist
	 */
	public void setGroupWhitelist(List<Group> groupWhitelist) {
		getCachedEntity().setProperty("groupWhitelist", groupWhitelist);
	}

	@SuppressWarnings("unchecked")
	public List<Group> getGroupWhitelist() {
		return (List<Group>) getCachedEntity().getProperty("groupWhitelist");
	}

	/**
	 *  (Location|territoryKey)
	 *  
	 * @param locations
	 */
	public void setLocations(List<Location> locations) {
		getCachedEntity().setProperty("locations", locations);
	}

	@SuppressWarnings("unchecked")
	public List<Location> getLocations() {
		return (List<Location>) getCachedEntity().getProperty("locations");
	}

	/**
	 *  A name given to this territory.
	 *  
	 * @param name
	 */
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	/**
	 *  (Group)
	 *  
	 * @param owningGroupKey
	 */
	public void setOwningGroupKey(Key owningGroupKey) {
		getCachedEntity().setProperty("owningGroupKey", owningGroupKey);
	}

	public Key getOwningGroupKey() {
		return (Key) getCachedEntity().getProperty("owningGroupKey");
	}

	public enum TravelRule {
		None, OwningGroupOnly,
	}

	/**
	 * 
	 * @param travelRule
	 */
	public void setTravelRule(TravelRule travelRule) {
		getCachedEntity().setProperty("travelRule", travelRule);
	}

	public TravelRule getTravelRule() {
		return (TravelRule) getCachedEntity().getProperty("travelRule");
	}

}
