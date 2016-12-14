package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.GroupStatus;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class GroupService extends Service {

	final private CachedEntity character;
	final private CachedEntity characterGroup;
	final private boolean isAdmin;
	
	public GroupService(ODPDBAccess db, CachedEntity character) {
		super(db);
		
		this.character = character;
		Object charGroupStatus = this.character.getProperty("groupStatus");
		if(this.character.getProperty("groupKey") != null &&
				(GameUtils.enumEquals(charGroupStatus, GroupStatus.Admin) ||
				 GameUtils.enumEquals(charGroupStatus, GroupStatus.Member)))
		{
			this.characterGroup = db.getEntity((Key)this.character.getProperty("groupKey"));
			this.isAdmin = this.characterGroup != null && 
					(GameUtils.enumEquals(this.character.getProperty("groupStatus"), GroupStatus.Admin) ||
					 GameUtils.equals(this.character.getKey(), this.characterGroup.getProperty("creatorKey")));
		}
		else
		{
			this.isAdmin = false;
			this.characterGroup = null;
		}
	}
	
	/**
	 * Indicates whether the current user is in the specified group.
	 * @param checkGroup Group CachedEntity to check against
	 * @return True if character is in the specified group. False otherwise.
	 */
	public boolean isCharacterInSpecifiedGroup(CachedEntity checkGroup)
	{
		return this.characterGroup != null && checkGroup != null && GameUtils.equals(this.characterGroup.getKey(), checkGroup.getKey());
	}
	
	/**
	 * Returns whether the current user is in any group. Determined in service constructor.
	 * @return True if character is in a group. False otherwise.
	 */
	public boolean characterHasGroup()
	{
		return this.characterGroup != null;
	}
	
	/**
	 * Returns whether the current user is an admin of his group (if he is in one). Gets set to false if not in a group.
	 * @return
	 */
	public boolean isCharacterGroupAdmin()
	{
		return this.isAdmin;
	}
	
	/**
	 * Returns the characters group CachedEntity. Initialized in service constructor,
	 * and can be null if character is not in a group.
	 * @return Group CachedEntity the character belongs to.
	 */
	public CachedEntity getCharacterGroup()
	{
		return this.characterGroup;
	}
	
	public boolean doesGroupAllowMergeRequests(CachedEntity group)
	{
		return Boolean.TRUE.equals(group.getProperty("allowMergeRequests"));
	}
	
	public Key getMergeRequestGroupKeyFor(CachedEntity group)
	{
		return (Key)group.getProperty("pendingMergeGroupKey");
	}
	
	/**
	 * Returns whether the specified Group has requested to merge with the characters group.
	 * @param mergeGroup
	 * @return
	 */
	public boolean isPendingMergeWith(CachedEntity mergeGroup)
	{
		return this.isCharacterInSpecifiedGroup(mergeGroup) == false &&
				GameUtils.equals(this.characterGroup.getKey(), mergeGroup.getProperty("pendingMergeGroupKey"));
	}
	
	/**
	 * Returns whether the specified group has already been merged.
	 * Checks for a null creatorKey and a valid pendingMergeGroupKey
	 * @param group
	 * @return
	 */
	public boolean isSpecifiedGroupMerged(CachedEntity group)
	{
		return group != null && group.getProperty("creatorKey") == null &&
				getMergeRequestGroupKeyFor(group) != null;
	}
	
	/**
	 * Returns whether the characters group has requested to merge with the specified group.
	 * @param mergeGroup
	 * @return
	 */
	public boolean hasGroupRequestedMergeWith(CachedEntity mergeGroup)
	{
		return this.isCharacterInSpecifiedGroup(mergeGroup) == false &&
				GameUtils.equals(mergeGroup.getKey(), getMergeRequestGroupKeyFor(this.characterGroup));
	}
	
	/**
	 * Can only process for cases when modify group is the character's group, and the character
	 * is an admin of the group.
	 * @return True if set allowMergeRequests property to true. False otherwise.
	 */
	public boolean setAllowMergeRequests(CachedEntity setGroup)
	{
		if(this.isAdmin && this.isCharacterInSpecifiedGroup(setGroup))
		{
			setGroup.setProperty("allowMergeRequests", Boolean.TRUE);
			return true;
		}
		return false;
	}
	
	/**
	 * Can only process for cases when modify group is the character's group, and the character
	 * is an admin of the group.
	 * @return True if set allowMergeRequests property to false. False otherwise.
	 */
	public boolean setDisallowMergeRequests(CachedEntity setGroup)
	{
		if(this.isAdmin && this.isCharacterInSpecifiedGroup(setGroup))
		{
			setGroup.setProperty("allowMergeRequests", Boolean.FALSE);
			return true;
		}
		return false;
	}
	
	/**
	 * Adds the specified groups Key to character groups pendingMergeGroupKey property, 
	 * indicating a merge request has been submitted. 
	 * @param mergeGroup Group to request merge with.
	 * @return Returns the specified characters Group entity if successful, null otherwise. 
	 */
	public CachedEntity setRequestMergeWith(CachedEntity mergeGroup)
	{
		if(this.isAdmin && this.isCharacterInSpecifiedGroup(mergeGroup) == false)
		{
			if(doesGroupAllowMergeRequests(mergeGroup))
			{
				this.characterGroup.setProperty("pendingMergeGroupKey", mergeGroup.getKey());
				return this.characterGroup;
			}
		}
		return null;
	}
	
	/**
	 * Clears out the pendingMergeGroupKey of the current users Group.
	 * @return True if specified group is current users group. False otherwise.
	 */
	public boolean cancelMergeRequestWith(CachedEntity group)
	{
		if(this.isAdmin && this.isCharacterInSpecifiedGroup(group))
		{
			group.setProperty("pendingMergeGroupKey", null);
			return true;
		}
		return false;
	}
	
	public boolean denyMergeApplicationFrom(CachedEntity requestGroup)
	{
		if(this.isAdmin && this.isPendingMergeWith(requestGroup))
		{
			// We've validated that the requesting group is wanting to merge
			// with this character's group. Deny the request by blanking out the 
			// pending merge key.
			requestGroup.setProperty("pendingMergeGroupKey", null);
			return true;
		}
		return false;
	}
	
	/**
	 * Merges all characters and houses of the specified group with the current
	 * characters group.
	 * @param mergeGroup
	 */
	public boolean acceptMergeApplicationFrom(CachedDatastoreService ds, CachedEntity mergeGroup) throws UserErrorMessage
	{
		if(this.isAdmin && this.isCharacterInSpecifiedGroup(mergeGroup) == false)
		{
			// Maintain list of entities we'll save.
			List<CachedEntity> saveEntities = new ArrayList<CachedEntity>();
			
			// Transfer group members first.
			List<CachedEntity> mergeGroupEntities = db.getGroupMembers(ds, mergeGroup);
			for(CachedEntity member:mergeGroupEntities)
			{
				Object charGroupStatus = member.getProperty("groupStatus");
				if(GameUtils.enumEquals(charGroupStatus, GroupStatus.Admin) ||
						GameUtils.enumEquals(charGroupStatus, GroupStatus.Member))
				{
					member.setProperty("groupKey", this.characterGroup.getKey());
					member.setProperty("groupStatus", GroupStatus.Member);
				}
				else
				{
					member.setProperty("groupKey", null);
					member.setProperty("groupStatus", null);
				}
				member.setProperty("groupRank", null);
				saveEntities.add(member);
			}
			
			mergeGroupEntities = db.getFilteredList("Location", "ownerKey", mergeGroup.getKey());
			if(mergeGroupEntities == null) mergeGroupEntities = new ArrayList<CachedEntity>();
			for(CachedEntity house:mergeGroupEntities)
			{
				house.setProperty("ownerKey", this.characterGroup.getKey());
				saveEntities.add(house);
			}
			
			// Find all groups that were wanting to merge with this group and clear it out.
			mergeGroupEntities = db.getFilteredList("Group", "pendingMergeGroupKey", mergeGroup.getKey());
			for(CachedEntity reqGroup:mergeGroupEntities)
			{
				reqGroup.setProperty("pendingMergeGroupKey", null);
				saveEntities.add(reqGroup);
			}
			
			// Likely going to need to remove alliances/warring groups from the
			// merged group, but will handle that later.
			
			// Set the owner key to null, to indicate that the group has been
			// merged and no one belongs to this group anymore.
			mergeGroup.setProperty("creatorKey", null);
			saveEntities.add(mergeGroup);
			
			// Save all the modified entities.
			ds.beginBulkWriteMode();
			ds.put(saveEntities);
			ds.commitBulkWrite();
			
			// Discover all group houses. Do this outside of block, since we don't
			// care as much if this fails (since it can be fired off).
			List<CachedEntity> allGroupMembers = db.getGroupMembers(ds, this.characterGroup);
			for(CachedEntity member:allGroupMembers)
				db.discoverAllGroupPropertiesFor(ds, member);
			
			return true;
		}
		return false;
	}
}
