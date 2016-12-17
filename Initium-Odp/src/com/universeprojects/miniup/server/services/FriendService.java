package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Date;
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
	private List<CachedEntity> friendsOfUser;

	public FriendService(ODPDBAccess db, CachedEntity user) {
		super(db);

		this.user = user;
		this.friendsOfUser = _getFriendsOfUser();
	}
	
	/**
	 * Returns a list of User entities that are friends of the user.
	 * @return List of CachedEntity representing Users that are friends of User.
	 */
	public List<CachedEntity> getFriendsOfUser(){
		
		return friendsOfUser;	
		
	}
	
	/**
	 * Returns a list of friends (User entities) that have been active in the past 10 minutes and aren't hiding
	 * online activity.
	 * @return List of CachedEntity representing Users that are online.
	 */
	public List<CachedEntity> getOnlineFriends(){
		
		List<CachedEntity> onlineFriends = new ArrayList<CachedEntity>();
		
		for(CachedEntity friend : friendsOfUser){
			Date lastMovement = (Date) friend.getProperty("locationEntryDatetime");
			
			boolean online = ((new Date().getTime() - lastMovement.getTime()) >= (10*60*1000));
			Boolean hideActivity = (Boolean) friend.getProperty("hideUserActivity");
			
			//if the entry datetime has occured within the past 10 minutes and user activity isn't hidden
			if(lastMovement != null && online && !hideActivity.booleanValue())
				//add friend to list of online friends.
				onlineFriends.add(friend);
			
		}
		
		return onlineFriends;

	}
	
	/**
	 * Returns a list of friends (User entities) that have not been active in the past 10 minutes or have hidden online activity.
	 * @return List of CachedEntity representing Users that are offline.
	 */
	public List<CachedEntity> getOfflineFriends(){
		
		List<CachedEntity> offlineFriends = new ArrayList<CachedEntity>();
		
		for(CachedEntity friend : friendsOfUser){
			Date lastMovement = (Date) friend.getProperty("locationEntryDatetime");
			
			boolean online = ((new Date().getTime() - lastMovement.getTime()) >= (10*60*1000));
			Boolean hideActivity = (Boolean) friend.getProperty("hideUserActivity");
			
			//if the entry datetime hasn't occured in the past ten minutes or online activity is hidden..
			if(lastMovement != null && (online || hideActivity.booleanValue()))
				//add friend to list of offline friends.
				offlineFriends.add(friend);			
		}
			
		return offlineFriends;

	}
	
	private List<CachedEntity> _getFriendsOfUser(){
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

}
