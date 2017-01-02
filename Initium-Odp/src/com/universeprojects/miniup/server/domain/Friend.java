package com.universeprojects.miniup.server.domain;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

//This entity is used to create a friends list for a user with other users.
public class Friend extends OdpDomain {

	public Friend() {
		super(new CachedEntity("Friend"));
	}

	public Friend(CachedEntity cachedEntity) {
		super(cachedEntity, "Friend");
	}

	// (Character)
	public void setFriendUserKey(Key friendUserKey) {
		getCachedEntity().setProperty("friendUserKey", friendUserKey);
	}

	public Key getFriendUserKey() {
		return (Key) getCachedEntity().getProperty("friendUserKey");
	}

	// This is not used by the game itself, but when you're creating a new friend, set this to the friendUserKey's current character name. It is simply meant to make it easier to browse friends lists in the editor.
	public void setSummary(String summary) {
		getCachedEntity().setProperty("summary", summary);
	}

	public String getSummary() {
		return (String) getCachedEntity().getProperty("summary");
	}

	// (Character)
	public void setUserKey(Key userKey) {
		getCachedEntity().setProperty("userKey", userKey);
	}

	public Key getUserKey() {
		return (Key) getCachedEntity().getProperty("userKey");
	}

}
