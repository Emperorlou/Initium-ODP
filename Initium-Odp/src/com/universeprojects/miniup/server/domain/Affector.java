package com.universeprojects.miniup.server.domain;

import com.universeprojects.cacheddatastore.CachedEntity;

//This object defines the influences attributes on other objects have on curve solutions.
public class Affector extends OdpDomain {
	public static final String KIND = "Affector";

	public Affector() {
		super(new CachedEntity(KIND));
	}

	public Affector(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	// The field name on a result that this affector applies to.
	public void setApplyField(String applyField) {
		getCachedEntity().setProperty("applyField", applyField);
	}

	public String getApplyField() {
		return (String) getCachedEntity().getProperty("applyField");
	}

	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}


}
