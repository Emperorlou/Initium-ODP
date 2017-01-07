package com.universeprojects.miniup.server.domain;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * A collection of stats on the Initium project level.
 * 
 * @author kyle-miller
 *
 */
public class Project extends OdpDomain {
	public static final String KIND = "Project";

	public Project() {
		super(new CachedEntity(KIND));
	}

	private Project(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	public static final Project wrap(CachedEntity cachedEntity) {
		return new Project(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 *  (Location|type==Town)
	 *  
	 * @param defaultSpawnLocationKey
	 */
	public void setDefaultSpawnLocationKey(Key defaultSpawnLocationKey) {
		getCachedEntity().setProperty("defaultSpawnLocationKey", defaultSpawnLocationKey);
	}

	public Key getDefaultSpawnLocationKey() {
		return (Key) getCachedEntity().getProperty("defaultSpawnLocationKey");
	}

	/**
	 * 
	 * @param donationCount
	 */
	public void setDonationCount(Integer donationCount) {
		getCachedEntity().setProperty("donationCount", donationCount);
	}

	public Integer getDonationCount() {
		return (Integer) getCachedEntity().getProperty("donationCount");
	}

	/**
	 *  (10)
	 *  
	 * @param signupCount
	 */
	public void setSignupCount(Integer signupCount) {
		getCachedEntity().setProperty("signupCount", signupCount);
	}

	public Integer getSignupCount() {
		return (Integer) getCachedEntity().getProperty("signupCount");
	}

	/**
	 *  (20)
	 *  
	 * @param uniqueVisitorCount
	 */
	public void setUniqueVisitorCount(Integer uniqueVisitorCount) {
		getCachedEntity().setProperty("uniqueVisitorCount", uniqueVisitorCount);
	}

	public Integer getUniqueVisitorCount() {
		return (Integer) getCachedEntity().getProperty("uniqueVisitorCount");
	}

}
