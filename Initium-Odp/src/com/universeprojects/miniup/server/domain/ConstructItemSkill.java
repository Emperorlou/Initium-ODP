package com.universeprojects.miniup.server.domain;

import com.universeprojects.cacheddatastore.CachedEntity;

//This type of skill defines how to construct an item. A character would have an instance of this.
public class ConstructItemSkill extends OdpDomain {

	public ConstructItemSkill() {
		super(new CachedEntity("ConstructItemSkill"));
	}

	public ConstructItemSkill(CachedEntity cachedEntity) {
		super(cachedEntity, "ConstructionItemSkill");
	}

}
