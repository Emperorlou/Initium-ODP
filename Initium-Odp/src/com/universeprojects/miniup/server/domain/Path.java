package com.universeprojects.miniup.server.domain;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

//Paths are connections between 2 locations.
public class Path extends OdpDomain {

	public Path() {
		super(new CachedEntity("Path"));
	}

	public Path(CachedEntity cachedEntity) {
		super(cachedEntity, "Path");
	}

	// (100.0)
	public void setDiscoveryChance(Double discoveryChance) {
		getCachedEntity().setProperty("discoveryChance", discoveryChance);
	}

	public Double getDiscoveryChance() {
		return (Double) getCachedEntity().getProperty("discoveryChance");
	}

	// This is an optional name you can give the path that is for developer use only
	public void setInternalName(String internalName) {
		getCachedEntity().setProperty("internalName", internalName);
	}

	public String getInternalName() {
		return (String) getCachedEntity().getProperty("internalName");
	}

	// Leave this blank unless you want to specifically change the wording on the button the player has to click on to travel this path. Specifically, when the player is going to travel TO location 1, what do you want the button to say.
	public void setLocation1ButtonNameOverride(String location1ButtonNameOverride) {
		getCachedEntity().setProperty("location1ButtonNameOverride", location1ButtonNameOverride);
	}

	public String getLocation1ButtonNameOverride() {
		return (String) getCachedEntity().getProperty("location1ButtonNameOverride");
	}

	// (Location|type==Permanent)
	public void setLocation1Key(Key location1Key) {
		getCachedEntity().setProperty("location1Key", location1Key);
	}

	public Key getLocation1Key() {
		return (Key) getCachedEntity().getProperty("location1Key");
	}

	// This number prevents players from taking this path from location 1 unless they have a compatible key with a matching code. If blank, no key is needed.
	public void setLocation1LockCode(Long location1LockCode) {
		getCachedEntity().setProperty("location1LockCode", location1LockCode);
	}

	public Long getLocation1LockCode() {
		return (Long) getCachedEntity().getProperty("location1LockCode");
	}

	// The format for this field is ##x## (example, 120x77). These coordinates define the location of the optional link that hovers over the banner for the player to click on.
	public void setLocation1OverlayCoordinates(String location1OverlayCoordinates) {
		getCachedEntity().setProperty("location1OverlayCoordinates", location1OverlayCoordinates);
	}

	public String getLocation1OverlayCoordinates() {
		return (String) getCachedEntity().getProperty("location1OverlayCoordinates");
	}

	// This is the caption for the optional link that hovers over the banner for the player to click on.
	public void setLocation1OverlayText(String location1OverlayText) {
		getCachedEntity().setProperty("location1OverlayText", location1OverlayText);
	}

	public String getLocation1OverlayText() {
		return (String) getCachedEntity().getProperty("location1OverlayText");
	}

	// Leave this blank unless you want to specifically change the wording on the button the player has to click on to travel this path. Specifically, when the player is going to travel TO location 2, what do you want the button to say.
	public void setLocation2ButtonNameOverride(String location2ButtonNameOverride) {
		getCachedEntity().setProperty("location2ButtonNameOverride", location2ButtonNameOverride);
	}

	public String getLocation2ButtonNameOverride() {
		return (String) getCachedEntity().getProperty("location2ButtonNameOverride");
	}

	// (Location|type==Permanent)
	public void setLocation2Key(Key location2Key) {
		getCachedEntity().setProperty("location2Key", location2Key);
	}

	public Key getLocation2Key() {
		return (Key) getCachedEntity().getProperty("location2Key");
	}

	// This number prevents players from taking this path from location 2 unless they have a compatible key with a matching code. If blank, no key is needed.
	public void setLocation2LockCode(Long location2LockCode) {
		getCachedEntity().setProperty("location2LockCode", location2LockCode);
	}

	public Long getLocation2LockCode() {
		return (Long) getCachedEntity().getProperty("location2LockCode");
	}

	// The format for this field is ##x## (example, 120x77). These coordinates define the location of the optional link that hovers over the banner for the player to click on.
	public void setLocation2OverlayCoordinates(String location2OverlayCoordinates) {
		getCachedEntity().setProperty("location2OverlayCoordinates", location2OverlayCoordinates);
	}

	public String getLocation2OverlayCoordinates() {
		return (String) getCachedEntity().getProperty("location2OverlayCoordinates");
	}

	// This is the caption for the optional link that hovers over the banner for the player to click on.
	public void setLocation2OverlayText(String location2OverlayText) {
		getCachedEntity().setProperty("location2OverlayText", location2OverlayText);
	}

	public String getLocation2OverlayText() {
		return (String) getCachedEntity().getProperty("location2OverlayText");
	}

	// The name should always contain the names of the Locations that this path connects to. It is really just used in the editors.
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	// The player (or group) that owns this path. This is usually blank unless it is a path to a player house.
	public void setOwnerKey(Key ownerKey) {
		getCachedEntity().setProperty("ownerKey", ownerKey);
	}

	public Key getOwnerKey() {
		return (Key) getCachedEntity().getProperty("ownerKey");
	}

	// The amount of seconds that the player has to wait before he travels to this path. If this is left blank, the old default of 6 seconds will apply.
	public void setTravelTime(Long travelTime) {
		getCachedEntity().setProperty("travelTime", travelTime);
	}

	public Long getTravelTime() {
		return (Long) getCachedEntity().getProperty("travelTime");
	}

	public enum ForceOneWay {
		None, FromLocation1Only, FromLocation2Only,
	}

	public void setForceOneWay(ForceOneWay forceOneWay) {
		getCachedEntity().setProperty("forceOneWay", forceOneWay);
	}

	public ForceOneWay getForceOneWay() {
		return (ForceOneWay) getCachedEntity().getProperty("forceOneWay");
	}

	public enum Type {
		Permanent, CombatSite, PlayerHouse, CampSite, CollectionSite, BlockadeSite,
	}

	public void setType(Type type) {
		getCachedEntity().setProperty("type", type);
	}

	public Type getType() {
		return (Type) getCachedEntity().getProperty("type");
	}

}
