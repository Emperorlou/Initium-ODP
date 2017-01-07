package com.universeprojects.miniup.server.domain;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * A player group.
 * 
 * @author kyle-miller
 *
 */
public class Group extends OdpDomain {
	public static final String KIND = "Group";

	public Group() {
		super(new CachedEntity(KIND));
	}

	private Group(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	public static final Group wrap(CachedEntity cachedEntity) {
		return new Group(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 *  If true, this group is allowing other groups to request to merge with them.
	 *  
	 * @param allowMergeRequests
	 */
	public void setAllowMergeRequests(Boolean allowMergeRequests) {
		getCachedEntity().setProperty("allowMergeRequests", allowMergeRequests);
	}

	public Boolean getAllowMergeRequests() {
		return (Boolean) getCachedEntity().getProperty("allowMergeRequests");
	}

	/**
	 *  (User)
	 *  
	 * @param creatorKey
	 */
	public void setCreatorKey(Key creatorKey) {
		getCachedEntity().setProperty("creatorKey", creatorKey);
	}

	public Key getCreatorKey() {
		return (Key) getCachedEntity().getProperty("creatorKey");
	}

	/**
	 *  (Character|type=PC)
	 *  
	 * @param declaredAlliedCharacterKeys
	 */
	public void setDeclaredAlliedCharacterKeys(List<Key> declaredAlliedCharacterKeys) {
		getCachedEntity().setProperty("declaredAlliedCharacters", declaredAlliedCharacterKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getDeclaredAlliedCharacterKeys() {
		return (List<Key>) getCachedEntity().getProperty("declaredAlliedCharacters");
	}

	/**
	 *  (Group)
	 *  
	 * @param declaredAlliedGroupKeys
	 */
	public void setDeclaredAlliedGroupKeys(List<Key> declaredAlliedGroupKeys) {
		getCachedEntity().setProperty("declaredAlliedGroups", declaredAlliedGroupKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getDeclaredAlliedGroupKeys() {
		return (List<Key>) getCachedEntity().getProperty("declaredAlliedGroups");
	}

	/**
	 *  (Character|type=PC)
	 *  
	 * @param declaredWarCharacterKeys
	 */
	public void setDeclaredWarCharacterKeys(List<Key> declaredWarCharacterKeys) {
		getCachedEntity().setProperty("declaredWarCharacters", declaredWarCharacterKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getDeclaredWarCharacterKeys() {
		return (List<Key>) getCachedEntity().getProperty("declaredWarCharacters");
	}

	/**
	 *  (Group)
	 *  
	 * @param declaredWarGroupKeys
	 */
	public void setDeclaredWarGroupKeys(List<Key> declaredWarGroupKeys) {
		getCachedEntity().setProperty("declaredWarGroups", declaredWarGroupKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getDeclaredWarGroupKeys() {
		return (List<Key>) getCachedEntity().getProperty("declaredWarGroups");
	}

	/**
	 *  This is a general purpose description for the player group. It is what players see when they inspect your group.
	 *  
	 * @param description
	 */
	public void setDescription(String description) {
		getCachedEntity().setProperty("description", description);
	}

	public String getDescription() {
		return (String) getCachedEntity().getProperty("description");
	}

	/**
	 *  (Character|groupKey)
	 *  
	 * @param memberKeys
	 */
	public void setMemberKeys(List<Key> memberKeys) {
		getCachedEntity().setProperty("members", memberKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getMemberKeys() {
		return (List<Key>) getCachedEntity().getProperty("members");
	}

	/**
	 *  The name of this group.
	 *  
	 * @param name
	 */
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	/**
	 *  (Group)
	 *  
	 * @param pendingAllianceGroupKey
	 */
	public void setPendingAllianceGroupKey(Key pendingAllianceGroupKey) {
		getCachedEntity().setProperty("pendingAllianceGroupKey", pendingAllianceGroupKey);
	}

	public Key getPendingAllianceGroupKey() {
		return (Key) getCachedEntity().getProperty("pendingAllianceGroupKey");
	}

	/**
	 *  (Group)
	 *  
	 * @param pendingMergeGroupKey
	 */
	public void setPendingMergeGroupKey(Key pendingMergeGroupKey) {
		getCachedEntity().setProperty("pendingMergeGroupKey", pendingMergeGroupKey);
	}

	public Key getPendingMergeGroupKey() {
		return (Key) getCachedEntity().getProperty("pendingMergeGroupKey");
	}

	public enum ApplicationMode {
		Locked("Locked"), AcceptingApplications("Accepting Applications");

		private String value;

		private ApplicationMode(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	/**
	 * 
	 * @param applicationMode
	 */
	public void setApplicationMode(ApplicationMode applicationMode) {
		getCachedEntity().setProperty("applicationMode", applicationMode);
	}

	public ApplicationMode getApplicationMode() {
		return (ApplicationMode) getCachedEntity().getProperty("applicationMode");
	}

}
