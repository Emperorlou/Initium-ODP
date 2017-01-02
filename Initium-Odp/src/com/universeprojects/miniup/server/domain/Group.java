package com.universeprojects.miniup.server.domain;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

//A player group.
public class Group extends OdpDomain {
	public static final String KIND = "Group";

	public Group() {
		super(new CachedEntity(KIND));
	}

	public Group(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	// If true, this group is allowing other groups to request to merge with them.
	public void setAllowMergeRequests(Boolean allowMergeRequests) {
		getCachedEntity().setProperty("allowMergeRequests", allowMergeRequests);
	}

	public Boolean getAllowMergeRequests() {
		return (Boolean) getCachedEntity().getProperty("allowMergeRequests");
	}

	// (User)
	public void setCreatorKey(Key creatorKey) {
		getCachedEntity().setProperty("creatorKey", creatorKey);
	}

	public Key getCreatorKey() {
		return (Key) getCachedEntity().getProperty("creatorKey");
	}

	// (Character|type=PC)
	public void setDeclaredAlliedCharacterKeys(List<Key> declaredAlliedCharacterKeys) {
		getCachedEntity().setProperty("declaredAlliedCharacters", declaredAlliedCharacterKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getDeclaredAlliedCharacterKeys() {
		return (List<Key>) getCachedEntity().getProperty("declaredAlliedCharacters");
	}

	// (Group)
	public void setDeclaredAlliedGroupKeys(List<Key> declaredAlliedGroupKeys) {
		getCachedEntity().setProperty("declaredAlliedGroups", declaredAlliedGroupKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getDeclaredAlliedGroupKeys() {
		return (List<Key>) getCachedEntity().getProperty("declaredAlliedGroups");
	}

	// (Character|type=PC)
	public void setDeclaredWarCharacterKeys(List<Key> declaredWarCharacterKeys) {
		getCachedEntity().setProperty("declaredWarCharacters", declaredWarCharacterKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getDeclaredWarCharacterKeys() {
		return (List<Key>) getCachedEntity().getProperty("declaredWarCharacters");
	}

	// (Group)
	public void setDeclaredWarGroupKeys(List<Key> declaredWarGroupKeys) {
		getCachedEntity().setProperty("declaredWarGroups", declaredWarGroupKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getDeclaredWarGroupKeys() {
		return (List<Key>) getCachedEntity().getProperty("declaredWarGroups");
	}

	// This is a general purpose description for the player group. It is what players see when they inspect your group.
	public void setDescription(String description) {
		getCachedEntity().setProperty("description", description);
	}

	public String getDescription() {
		return (String) getCachedEntity().getProperty("description");
	}

	// (Character|groupKey)
	public void setMemberKeys(List<Key> memberKeys) {
		getCachedEntity().setProperty("members", memberKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getMemberKeys() {
		return (List<Key>) getCachedEntity().getProperty("members");
	}

	// The name of this group.
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	// (Group)
	public void setPendingAllianceGroupKey(Key pendingAllianceGroupKey) {
		getCachedEntity().setProperty("pendingAllianceGroupKey", pendingAllianceGroupKey);
	}

	public Key getPendingAllianceGroupKey() {
		return (Key) getCachedEntity().getProperty("pendingAllianceGroupKey");
	}

	// (Group)
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

	public void setApplicationMode(ApplicationMode applicationMode) {
		getCachedEntity().setProperty("applicationMode", applicationMode);
	}

	public ApplicationMode getApplicationMode() {
		return (ApplicationMode) getCachedEntity().getProperty("applicationMode");
	}

}
