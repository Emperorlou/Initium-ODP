package com.universeprojects.miniup.server.domain;

import java.util.List;

import com.universeprojects.cacheddatastore.CachedEntity;

//Tags are categorical concepts that are applied to an item. Items with the same tags are considered to have the same use or conceptual properties in-game. The more tags items have in common, the more similar in form and function the items are.
public class Tag extends OdpDomain {
	public static final String KIND = "Tag";

	public Tag() {
		super(new CachedEntity(KIND));
	}

	public Tag(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	// (Affector)
	public void setAffectors(List<Object> affectors) { // TODO Objects?
		getCachedEntity().setProperty("affectors", affectors);
	}

	@SuppressWarnings("unchecked")
	public List<Object> getAffectors() {
	 return (List<Object>) getCachedEntity().getProperty("affectors");
	}

	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

}
