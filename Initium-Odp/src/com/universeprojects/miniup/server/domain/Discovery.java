package com.universeprojects.miniup.server.domain;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

//This entry tells us whether or not a character can see a given entity. If they cannot see it, they can usually try to search to find it. Discoveries are generally used for finding: Paths, and Items.
public class Discovery extends OdpDomain {
	public static final String KIND = "Discovery";

	public Discovery() {
		super(new CachedEntity(KIND));
	}

	public Discovery(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	// (Character)
	public void setCharacterKey(Key characterKey) {
		getCachedEntity().setProperty("characterKey", characterKey);
	}

	public Key getCharacterKey() {
		return (Key) getCachedEntity().getProperty("characterKey");
	}

	// The entity that was discovered. Most of the time it is a Path entity, but it can be other things since this entity is meant to be generic.
	public void setEntityKey(Key entityKey) {
		getCachedEntity().setProperty("entityKey", entityKey);
	}

	public Key getEntityKey() {
		return (Key) getCachedEntity().getProperty("entityKey");
	}

	// (Location|type==Permanent)
	public void setLocation1Key(Key location1Key) {
		getCachedEntity().setProperty("location1Key", location1Key);
	}

	public Key getLocation1Key() {
		return (Key) getCachedEntity().getProperty("location1Key");
	}

	// (Location|type==Permanent)
	public void setLocation2Key(Key location2Key) {
		getCachedEntity().setProperty("location2Key", location2Key);
	}

	public Key getLocation2Key() {
		return (Key) getCachedEntity().getProperty("location2Key");
	}

	public void setHidden(Boolean hidden) {
		getCachedEntity().setProperty("hidden", hidden);
	}

	public Boolean getHidden() {
		return (Boolean) getCachedEntity().getProperty("hidden");
	}

	public enum DiscoveryKind {
		Path,
	}

	public void setDiscoveryKind(DiscoveryKind discoveryKind) {
		getCachedEntity().setProperty("kind", discoveryKind);
	}

	public DiscoveryKind getDiscoveryKind() {
		return (DiscoveryKind) getCachedEntity().getProperty("kind");
	}

}
