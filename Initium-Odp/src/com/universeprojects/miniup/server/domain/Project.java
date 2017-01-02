package com.universeprojects.miniup.server.domain;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

//A collection of stats on the Initium project level.
public class Project extends OdpDomain {

	public Project() {
		super(new CachedEntity("Project"));
	}

	public Project(CachedEntity cachedEntity) {
		super(cachedEntity, "Project");
	}

	// (Location|type==Town)
	public void setDefaultSpawnLocationKey(Key defaultSpawnLocationKey) {
		getCachedEntity().setProperty("defaultSpawnLocationKey", defaultSpawnLocationKey);
	}

	public Key getDefaultSpawnLocationKey() {
		return (Key) getCachedEntity().getProperty("defaultSpawnLocationKey");
	}

	public void setDonationCount(Integer donationCount) {
		getCachedEntity().setProperty("donationCount", donationCount);
	}

	public Integer getDonationCount() {
		return (Integer) getCachedEntity().getProperty("donationCount");
	}

	// (10)
	public void setSignupCount(Integer signupCount) {
		getCachedEntity().setProperty("signupCount", signupCount);
	}

	public Integer getSignupCount() {
		return (Integer) getCachedEntity().getProperty("signupCount");
	}

	// (20)
	public void setUniqueVisitorCount(Integer uniqueVisitorCount) {
		getCachedEntity().setProperty("uniqueVisitorCount", uniqueVisitorCount);
	}

	public Integer getUniqueVisitorCount() {
		return (Integer) getCachedEntity().getProperty("uniqueVisitorCount");
	}

}
