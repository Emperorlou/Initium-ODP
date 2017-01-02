package com.universeprojects.miniup.server.domain;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

//This is a "node" that is used to extract items from the game world. Use it for gathering wood, food (like berries/apples/wheat), stone, iron ore...etc.
public class Collectable extends OdpDomain {

	public Collectable() {
		super(new CachedEntity("Collectable"));
	}

	public Collectable(CachedEntity cachedEntity) {
		super(cachedEntity, "Collectable");
	}

	// The image used for the CollectionSite location.
	public void setBannerUrl(String bannerUrl) {
		getCachedEntity().setProperty("bannerUrl", bannerUrl);
	}

	public String getBannerUrl() {
		return (String) getCachedEntity().getProperty("bannerUrl");
	}

	// The number of times this node can be collected from before it disappears.
	public void setCollectionCount(Long collectionCount) {
		getCachedEntity().setProperty("collectionCount", collectionCount);
	}

	public Long getCollectionCount() {
		return (Long) getCachedEntity().getProperty("collectionCount");
	}

	// A short flavor description that is added to the item when a player views it.
	public void setDescription(String description) {
		getCachedEntity().setProperty("description", description);
	}

	public String getDescription() {
		return (String) getCachedEntity().getProperty("description");
	}

	// Denotes how easy it is to collect material from this node (in seconds). 0 = extremely easy (no wait time), 60 = 60 seconds of wait time to extract once.
	public void setExtractionEase(Long extractionEase) {
		getCachedEntity().setProperty("extractionEase", extractionEase);
	}

	public Long getExtractionEase() {
		return (Long) getCachedEntity().getProperty("extractionEase");
	}

	// A 16x16 image icon that represents this item in the inventory.
	public void setIcon(String icon) {
		getCachedEntity().setProperty("icon", icon);
	}

	public String getIcon() {
		return (String) getCachedEntity().getProperty("icon");
	}

	// This is an optional name you can give the item that is for developer use only (it will not be shown to the players).
	public void setInternalName(String internalName) {
		getCachedEntity().setProperty("internalName", internalName);
	}

	public String getInternalName() {
		return (String) getCachedEntity().getProperty("internalName");
	}

	// (ItemDef)
	public void setItemDefKey(Key itemDefKey) {
		getCachedEntity().setProperty("itemDefKey", itemDefKey);
	}

	public Key getItemDefKey() {
		return (Key) getCachedEntity().getProperty("itemDefKey");
	}

	// (Location)
	public void setLocationKey(Key locationKey) {
		getCachedEntity().setProperty("locationKey", locationKey);
	}

	public Key getLocationKey() {
		return (Key) getCachedEntity().getProperty("locationKey");
	}

	// The number of times this node can be collected from before it disappears.
	public void setMaxCollectionCount(Long maxCollectionCount) {
		getCachedEntity().setProperty("maxCollectionCount", maxCollectionCount);
	}

	public Long getMaxCollectionCount() {
		return (Long) getCachedEntity().getProperty("maxCollectionCount");
	}

	// This name will function as the `item class` for ingame items.
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	// (ItemDef)
	public void setToolsOptionalKeys(List<Key> itemDefKeys) { // TODO - type
		getCachedEntity().setProperty("toolsOptional", itemDefKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getToolsOptionalKeys() {
		return (List<Key>) getCachedEntity().getProperty("toolsOptional");
	}

	// (ItemDef)
	public void setToolsRequiredKeys(List<Key> itemDefKeys) { // TODO - type
		getCachedEntity().setProperty("toolsRequired", itemDefKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getToolsRequiredKeys() {
		return (List<Key>) getCachedEntity().getProperty("toolsRequired");
	}

}
