package com.universeprojects.miniup.server.domain;

import java.util.List;

import com.universeprojects.cacheddatastore.CachedEntity;

//This is a recipe that the transmute system uses. When a player puts items inside a transmute box, it changes the items into something else. This recipe defines how and when that works. Please note that no 2 recipes can have the exact same materials! When material requirements are implemented however, we can use the same materials as long as the requirements differ enough so that when a player puts some items in the transmute box, the items don't ever end up matching 2 recipes. 
public class TransmuteRecipe extends OdpDomain {

	public TransmuteRecipe() {
		super(new CachedEntity("TransmuteRecipe"));
	}

	public TransmuteRecipe(CachedEntity cachedEntity) {
		super(cachedEntity, "TransmuteRecipe");
	}

	// (Not yet in use) Using a special notation, this will be used to define the requirements for each of the materials.
	public void setMaterialRequirements(List<String> materialRequirements) {
		getCachedEntity().setProperty("materialRequirements", materialRequirements);
	}

	@SuppressWarnings("unchecked")
	public List<String> getMaterialRequirements() {
		return (List<String>) getCachedEntity().getProperty("materialRequirements");
	}

	// (ItemDef)
	public void setMaterials(List<Item> materials) {
		getCachedEntity().setProperty("materials", materials);
	}

	@SuppressWarnings("unchecked")
	public List<Item> getMaterials() {
		return (List<Item>) getCachedEntity().getProperty("materials");
	}

	// Mostly just used for keeping things organized in the editor
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	// (ItemDef)
	public void setResults(List<Item> results) {
		getCachedEntity().setProperty("results", results);
	}

	@SuppressWarnings("unchecked")
	public List<Item> getResults() {
		return (List<Item>) getCachedEntity().getProperty("results");
	}

}
