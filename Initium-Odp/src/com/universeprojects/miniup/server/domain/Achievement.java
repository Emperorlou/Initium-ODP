package com.universeprojects.miniup.server.domain;

import com.universeprojects.cacheddatastore.CachedEntity;

public class Achievement extends OdpDomain {
	public static final String KIND = "Achievement";

	public Achievement() {
		super(new CachedEntity(KIND));
	}

	private Achievement(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	public static final Achievement wrap(CachedEntity cachedEntity) {
		return new Achievement(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}
	//Achievement title
	public void setTitle(String title) {
		getCachedEntity().setProperty("title", title);
	}
		
	public String getTitle() {
		return (String) getCachedEntity().getProperty("title");
	}

	//Achievement description
	public void setDescription(String description) {
		getCachedEntity().setProperty("description", description);
	}
		
	public String getDescription() {
		return (String) getCachedEntity().getProperty("description");
	}
	
	//Achievement image URL
	public void setImageUrl(String imageUrl) {
		getCachedEntity().setProperty("imageUrl", imageUrl);
	}
		
	public String getImageUrl() {
		return (String) getCachedEntity().getProperty("imageUrl");
	}
}
