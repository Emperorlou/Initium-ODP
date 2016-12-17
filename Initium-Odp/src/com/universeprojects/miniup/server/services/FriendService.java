package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private Map<CachedEntity, CachedEntity> friendsOfUser;

	public FriendService(ODPDBAccess db, CachedEntity user) {
		super(db);

		this.user = user;
		this.friendsOfUser = _getFriendsOfUser();
	}
	
	/**
	 * Returns a list of User entities that are friends of the user.
	 * @return List of CachedEntity representing Users that are friends of User.
	 */
	public  Map<CachedEntity, CachedEntity> getFriendsOfUser(){
		
		return friendsOfUser;	
		
	}
	
	/**
	 * Returns a map of friends <User,Character. that have been active in the past 10 minutes and aren't hiding
	 * online activity.
	 * @return Map<User,Character> representing Users that are online.
	 */
	public Map<CachedEntity, CachedEntity> getOnlineFriends(){
		
		Map<CachedEntity, CachedEntity> onlineFriends = new HashMap<CachedEntity, CachedEntity>();
		
		//iterate through the user/char map
		for(CachedEntity user : friendsOfUser.keySet()){
			
			CachedEntity character = friendsOfUser.get(user);
			
			Date lastMovement = (Date) character.getProperty("locationEntryDatetime");
			
			boolean online = ((new Date().getTime() - lastMovement.getTime()) < (10*60*1000));
			Boolean hideActivity = (Boolean) user.getProperty("hideUserActivity");
			
			//if the entry datetime has occured within the past 10 minutes and user activity isn't hidden
			if(lastMovement != null && online && !hideActivity.booleanValue())
				//add friend to list of online friends.
				onlineFriends.put(user,character);
			
		}
		
		return onlineFriends;

	}
	
	/**
	 * Returns a map of friends <User,Character> that have not been active in the past 10 minutes
	 *  or have hidden online activity.
	 * @return Map<User,Character> representing Users that are offline.
	 */
	public Map<CachedEntity, CachedEntity> getOfflineFriends(){
		
		Map<CachedEntity, CachedEntity> offlineFriends = new HashMap<CachedEntity, CachedEntity>();
		
		//iterate through the user/char map
		for(CachedEntity user : friendsOfUser.keySet()){
			
			CachedEntity character = friendsOfUser.get(user);
			
			Date lastMovement = (Date) character.getProperty("locationEntryDatetime");
			
			boolean online = ((new Date().getTime() - lastMovement.getTime()) < (10*60*1000));
			Boolean hideActivity = (Boolean) user.getProperty("hideUserActivity");
			
			//if the entry datetime has not occured within the past 10 minutes or user activity is hidden
			if(lastMovement != null && (!online || hideActivity.booleanValue()))
				//add friend to map of offline friends.
				offlineFriends.put(user,character);
			
		}
			
		return offlineFriends;

	}
	
	private Map<CachedEntity,CachedEntity> _getFriendsOfUser(){
		//get a list of Friend entities who's userKey is equal to the user.
		List<CachedEntity> friends = db.getFilteredList("Friend","userKey", this.user.getKey());
		
		//From the list of friends, get all of their user keys.
		List<Key> friendUserKeys = new ArrayList<Key>();
		for(CachedEntity friend : friends){
				
			Key friendKey = (Key) friend.getProperty("friendUserKey");
			
			friendUserKeys.add(friendKey);
		}
		
		List<CachedEntity> users = db.getEntities(friendUserKeys);
		List<Key> characterKeys = new ArrayList<Key>();
		for(CachedEntity user : users){			
			characterKeys.add( (Key) user.getProperty("characterKey"));			
		}
		
		List<CachedEntity> characters = db.getEntities(characterKeys);		
		
		Map<CachedEntity,CachedEntity> userCharMap = new HashMap<CachedEntity,CachedEntity>();
		
		for(int i=0; i<users.size(); i++){
			userCharMap.put(users.get(i), characters.get(i));
		}
		
		return userCharMap;
	}

}
