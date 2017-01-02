package com.universeprojects.miniup.server.domain;

import com.universeprojects.cacheddatastore.CachedEntity;

//Settings that relate to the database schema itself.
public class Schema extends OdpDomain {

	public Schema() {
		super(new CachedEntity("Schema"));
	}

	public Schema(CachedEntity cachedEntity) {
		super(cachedEntity, "Schema");
	}

	// The url where we can find the image server.
	public void setBaseImageUrl(String baseImageUrl) {
		getCachedEntity().setProperty("baseImageUrl", baseImageUrl);
	}

	public String getBaseImageUrl() {
		return (String) getCachedEntity().getProperty("baseImageUrl");
	}

}
