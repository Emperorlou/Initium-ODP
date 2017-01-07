package com.universeprojects.miniup.server.domain;

import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * This type of skill defines how to construct an item. A character would have an instance of this.
 * 
 * @author kyle-miller
 *
 */
public class ConstructItemSkill extends OdpDomain {
	public static final String KIND = "ConstructItemSkill";

	public ConstructItemSkill() {
		super(new CachedEntity(KIND));
	}

	public ConstructItemSkill(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

}
