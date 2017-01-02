package com.universeprojects.miniup.server.domain;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

//Collectable definitions. This entity describes how to create an instance of a Collectable.
public class CollectableDef extends OdpDomain {

	public CollectableDef() {
		super(new CachedEntity("CollectableDef"));
	}

	public CollectableDef(CachedEntity cachedEntity) {
		super(cachedEntity, "CollectableDef");
	}

	// The banner url that is to be used in the CollectionSite location generated for this collectable.
	public void setBannerUrl(String bannerUrl) {
		getCachedEntity().setProperty("bannerUrl", bannerUrl);
	}

	public String getBannerUrl() {
		return (String) getCachedEntity().getProperty("bannerUrl");
	}

	// The description that is to be added to the CollectionSite location when it is created.
	public void setDescription(String description) {
		getCachedEntity().setProperty("description", description);
	}

	public String getDescription() {
		return (String) getCachedEntity().getProperty("description");
	}

	// Denotes how easy it is to collect material from this node (in seconds). 0 = extremely easy (no wait time), 60 = 60 seconds of wait time to extract once.
	public void setExtractionEase(String extractionEase) {
		getCachedEntity().setProperty("extractionEase", extractionEase);
	}

	public String getExtractionEase() {
		return (String) getCachedEntity().getProperty("extractionEase");
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

	// The number of times this node can be collected from before it disappears.
	public void setMaxCollectionCount(String maxCollectionCount) {
		getCachedEntity().setProperty("maxCollectionCount", maxCollectionCount);
	}

	public String getMaxCollectionCount() {
		return (String) getCachedEntity().getProperty("maxCollectionCount");
	}

	// This name will function as the `item class` for ingame items.
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	// (ConstructionToolRequirement)
	public void setToolsOptionalKeys(List<Key> itemDefKeys) { // TODO - type
		getCachedEntity().setProperty("toolsOptional", itemDefKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getToolsOptionalKeys() {
		return (List<Key>) getCachedEntity().getProperty("toolsOptional");
	}

	// (ConstructionToolRequirement)
	public void setToolsRequiredKeys(List<Key> itemDefKeys) { // TODO - type
		getCachedEntity().setProperty("toolsRequired", itemDefKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getToolsRequiredKeys() {
		return (List<Key>) getCachedEntity().getProperty("toolsRequired");
	}

}
