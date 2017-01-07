package com.universeprojects.miniup.server.domain;

import java.util.Objects;

import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * Generic parent class for all Initium ODP domain classes
 * 
 * @author kyle-miller
 *
 */
public abstract class OdpDomain {
	private final CachedEntity cachedEntity;

	public abstract String getKind();

	protected OdpDomain(CachedEntity cachedEntity) {
		assert cachedEntity != null : "Cannot create a domain object without a cached entity";
		this.cachedEntity = cachedEntity;
		assert Objects.equals(getKind(), this.cachedEntity.getKind()) : String.format("Cannot create domain object of kind %s with a cached entity of kind %s", getKind(), this.cachedEntity.getKind());
	}

	public CachedEntity getCachedEntity() {
		return this.cachedEntity;
	}

	public boolean equals(Object o) {
		return (o != null) && (o instanceof OdpDomain) && getCachedEntity().getKey().equals(((OdpDomain) o).getCachedEntity().getKey());
	}
}
