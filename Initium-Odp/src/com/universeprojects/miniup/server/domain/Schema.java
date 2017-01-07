package com.universeprojects.miniup.server.domain;

import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * Settings that relate to the database schema itself.
 * 
 * @author kyle-miller
 *
 */
public class Schema extends OdpDomain {
	public static final String KIND = "Schema";

	public Schema() {
		super(new CachedEntity(KIND));
	}

	public Schema(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 *  The url where we can find the image server.
	 *  
	 * @param baseImageUrl
	 */
	public void setBaseImageUrl(String baseImageUrl) {
		getCachedEntity().setProperty("baseImageUrl", baseImageUrl);
	}

	public String getBaseImageUrl() {
		return (String) getCachedEntity().getProperty("baseImageUrl");
	}

}
