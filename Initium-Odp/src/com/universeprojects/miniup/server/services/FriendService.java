package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

/*
 * Friend service. Used to assist in common friend entity operations.
 * 
 * @author RevMuun
 */

public class FriendService extends Service {
	
	private CachedEntity user;

	public FriendService(ODPDBAccess db, CachedEntity user) {
		super(db);

		this.user = user;		
	}
	
	/**
	 * Returns a list of User entities that are friends of the user.
	 * @return List of CachedEntity representing Users that are friends of User.
	 */
	public List<CachedEntity> getFriendsOfUser(){
		
		//get a list of Friend entities who's userKey is equal to the user.
		List<CachedEntity> friends = db.getFilteredList("Friend","userKey", this.user.getKey());
		
		//From the list of friends, get all of their user keys.
		List<Key> friendUserKeys = new ArrayList<Key>();
		for(CachedEntity friend : friends){
			
			Key friendKey = (Key) friend.getProperty("friendUserKey");
			
			friendUserKeys.add(friendKey);
		}
		
		//return a list of User entities from the friend's user keys.
		return db.getEntities(friendUserKeys);	
	}
	
	/**
	 * Returns a list of User entities that are friends of the user.
	 * @param user CachedEntity representing a User.
	 * @return List of CachedEntity representing Users that are friends of User.
	 */
	public List<CachedEntity> getFriendsOfUser(CachedEntity user){
		
		//get a list of Friend entities who's userKey is equal to the user.
		List<CachedEntity> friends = db.getFilteredList("Friend","userKey", user.getKey());
		
		//From the list of friends, get all of their user keys.
		List<Key> friendUserKeys = new ArrayList<Key>();
		for(CachedEntity friend : friends){
			
			Key friendKey = (Key) friend.getProperty("friendUserKey");
			
			friendUserKeys.add(friendKey);
		}
		
		//return a list of User entities from the friend's user keys.
		return db.getEntities(friendUserKeys);	
	}

}
