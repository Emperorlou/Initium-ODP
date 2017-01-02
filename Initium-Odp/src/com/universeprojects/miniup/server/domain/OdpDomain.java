package com.universeprojects.miniup.server.domain;

import com.google.appengine.labs.repackaged.com.google.common.base.Objects;
import com.universeprojects.cacheddatastore.CachedEntity;

public abstract class OdpDomain {
	private CachedEntity cachedEntity;

	public OdpDomain(CachedEntity cachedEntity) {
		assert cachedEntity != null : "Cannot create a domain object without a cached entity";
		this.cachedEntity = cachedEntity;
	}

	public OdpDomain(CachedEntity cachedEntity, String kind) {
		this(cachedEntity);
		assert Objects.equal(kind, this.cachedEntity.getKind()) : String.format("Cannot create domain object of kind %s with a cached entity of kind %s", kind, this.cachedEntity.getKind());
	}

	public CachedEntity getCachedEntity() {
		return this.cachedEntity;
	}
}
