package com.universeprojects.miniup.server.domain;
import com.universeprojects.cacheddatastore.CachedEntity;

//Define Achievement
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
	
	//Title of the Achievement
	public void setTitle(String title) {
		getCachedEntity().setProperty("title", title);
	}
	
	public String getTitle() {
		return (String) getCachedEntity().getProperty("title");
	}
	
	//Description of the Achievement
	public void setDescription(String description) {
		getCachedEntity().setProperty("description",  description);
	}
	
	public String getDescription() {
		return (String) getCachedEntity().getProperty("description");
	}
	
	public void setImageUrl(String imageUrl) {
		getCachedEntity().setProperty("imageUrl", imageUrl);
	}
	
	public String getImageUrl() {
		return (String) getCachedEntity().getProperty("imageUrl");
	}
}
