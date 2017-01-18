package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collections;
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
					(GameUtils.enumEquals(charGroupStatus, GroupStatus.Admin) ||
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
	
	public boolean isCharacterGroupCreator()
	{
		return GameUtils.equals(this.character.getKey(), this.characterGroup.getProperty("creatorKey"));
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
	 * @return True if character is admin of group and has a pending merge. False otherwise.
	 */
	public boolean cancelMergeRequest()
	{
		if(this.isAdmin && getMergeRequestGroupKeyFor(this.characterGroup) != null)
		{
			this.characterGroup.setProperty("pendingMergeGroupKey", null);
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
					member.setProperty("groupStatus", GroupStatus.Member.toString());
				}
				else
				{
					member.setProperty("groupKey", null);
					member.setProperty("groupStatus", null);
				}
				member.setProperty("groupRank", null);
				saveEntities.add(member);
			}
			
			// Transfer all group houses.
			mergeGroupEntities = db.getFilteredList("Location", "ownerKey", mergeGroup.getKey());
			if(mergeGroupEntities == null) mergeGroupEntities = new ArrayList<CachedEntity>();
			for(CachedEntity house:mergeGroupEntities)
			{
				house.setProperty("ownerKey", this.characterGroup.getKey());
				saveEntities.add(house);
			}
			
			// Re-assign all group paths.
			mergeGroupEntities = db.getFilteredList("Path", "ownerKey", mergeGroup.getKey());
			for(CachedEntity path:mergeGroupEntities)
			{
				path.setProperty("ownerKey", this.characterGroup.getKey());
				saveEntities.add(path);
			}
						
			// Find all groups that were wanting to merge with this group and clear it out.
			mergeGroupEntities = db.getFilteredList("Group", "pendingMergeGroupKey", mergeGroup.getKey());
			for(CachedEntity reqGroup:mergeGroupEntities)
			{
				reqGroup.setProperty("pendingMergeGroupKey", null);
				saveEntities.add(reqGroup);
			}
			
			//Removing wars/alliances from merge group
			mergeGroup.setProperty("declaredAlliedGroups", null);
			mergeGroup.setProperty("declaredWarGroups", null);
			
			//Clean any pending alliances with merge group
			mergeGroupEntities = db.getFilteredList("Group", "pendingAllianceGroupKey", mergeGroup.getKey());
			for (CachedEntity reqGroup : mergeGroupEntities)
			{
				reqGroup.setProperty("pendingAllianceGroupKey", null);
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
	public List<Key> cleanNullKeysFromList(List<Key> keys)
	{
		if (keys != null)
		{
			if (keys.contains(null))
			{
				keys.removeAll(Collections.singleton(null));
				return keys;
			}
			return keys;
		}
		return null;
	}
	
	public boolean isGroupAtWarWithCharGroup(CachedEntity group) 
	{
		List<Key> charGroupWars = getCharGroupWarKeys();
		List<Key> groupWars = getGroupWarKeys(group);
		if (charGroupWars != null && charGroupWars.contains(group.getKey()))
			return true;
		else if (groupWars != null && groupWars.contains(this.characterGroup.getKey()))
			return true;
		else
			return false;					
	}
	
	public boolean isGroupAlliedWithCharGroup(CachedEntity group)
	{
		List<Key> charGroupAllies = getCharGroupAllianceKeys();
		if (charGroupAllies != null)
			return charGroupAllies.contains(group.getKey());
		else
			return false;
	}
	@SuppressWarnings("unchecked")
	public List<Key> getGroupAllianceKeys(CachedEntity group)
	{
		return (List<Key>)group.getProperty("declaredAlliedGroups");
	}
	
	@SuppressWarnings("unchecked")
	public List<Key> getCharGroupAllianceKeys()
	{
		return (List<Key>)this.characterGroup.getProperty("declaredAlliedGroups");
	}
	
	@SuppressWarnings("unchecked")
	public List<Key> getGroupWarKeys(CachedEntity group)
	{
		return (List<Key>)group.getProperty("declaredWarGroups");
	}
	
	@SuppressWarnings("unchecked")
	public List<Key> getCharGroupWarKeys()
	{
		return (List<Key>)this.characterGroup.getProperty("declaredWarGroups");
	}
	
	@SuppressWarnings("unchecked")
	public boolean beginWar(CachedDatastoreService ds, CachedEntity warGroup) throws UserErrorMessage
	{
		//null check
	if (warGroup == null)
		return false;
	
	if(this.isAdmin && this.isCharacterInSpecifiedGroup(warGroup) == false)
	{
		List<Key> charGroupWars = getCharGroupWarKeys();
		List<Key> warGroupAllies = getGroupWarKeys(warGroup);
		if (charGroupWars == null)
		{
			if (warGroupAllies != null && isGroupAlliedWithCharGroup(warGroup))
				throw new UserErrorMessage(
						"Cannot start a war with an allied group.");
			if (GameUtils.equals(this.characterGroup.getKey(), warGroup.getKey()))
				throw new UserErrorMessage(
						"Cannot declare war against yourself.");
			if (GameUtils.equals(this.characterGroup.getProperty("pendingAllianceGroupKey"), warGroup.getKey()))
					this.characterGroup.setProperty("pendingAllianceGroupKey", null);
			List<Key> createCharGroupWars = new ArrayList<Key>();
			createCharGroupWars.add(warGroup.getKey());
			this.characterGroup.setProperty("declaredWarGroups", createCharGroupWars);
			ds.put(this.characterGroup);
			return true;
		}
		else if (charGroupWars != null)
		{
			if (warGroupAllies != null && isGroupAlliedWithCharGroup(warGroup))
				throw new UserErrorMessage(
						"Cannot start a war with an allied group.");
			if (GameUtils.equals(this.characterGroup.getKey(), warGroup.getKey()))
				throw new UserErrorMessage(
						"Cannot declare war against yourself.");
			if (GameUtils.equals(this.characterGroup.getProperty("pendingAllianceGroupKey"), warGroup.getKey()))
				this.characterGroup.setProperty("pendingAllianceGroupKey", null);
			charGroupWars.add(warGroup.getKey());
			this.characterGroup.setProperty("declaredWarGroups", charGroupWars);
			ds.put(this.characterGroup);
			return true;
		}
	}
	return false;
}
	@SuppressWarnings("unchecked")
	public boolean endWar(CachedDatastoreService ds, CachedEntity warGroup) throws UserErrorMessage
	{
		if(this.isAdmin && this.isCharacterInSpecifiedGroup(warGroup) == false)
		{
			List<Key> charGroupWars = (List<Key>)this.characterGroup.getProperty("declaredWarGroups");
			
			if (charGroupWars != null)
			{
				charGroupWars.remove(warGroup.getKey());
				this.characterGroup.setProperty("declaredWarGroups", cleanNullKeysFromList(charGroupWars));
				ds.put(this.characterGroup);
				return true;
			}
			return false;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public CachedEntity setAllianceRequest(CachedEntity allyGroup) throws UserErrorMessage
	{
		if (allyGroup != null)
		{
			List<Key> allyGroupWars = getGroupWarKeys(allyGroup);
			if (allyGroupWars != null && isGroupAtWarWithCharGroup(allyGroup))
				throw new UserErrorMessage(
						"Cannot request an alliance with a group you are at war with.");
			if (GameUtils.equals(allyGroup.getKey(), this.characterGroup.getKey()))
				throw new UserErrorMessage(
						"Group to ally is character's own group.");
				
				if(this.isAdmin && this.isCharacterInSpecifiedGroup(allyGroup) == false)
				{
						this.characterGroup.setProperty("pendingAllianceGroupKey", allyGroup.getKey());
						return this.characterGroup;
				}
		}
		else
		{
			throw new UserErrorMessage(
					"Group does not exist!");
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public boolean acceptAllianceRequest(CachedDatastoreService ds, CachedEntity allyGroup) throws UserErrorMessage
	{
		List<CachedEntity> groupsToSave = new ArrayList<CachedEntity>();
		if(this.isAdmin && this.isCharacterInSpecifiedGroup(allyGroup) == false)
		{
			List<Key> charGroupAlliances = (List<Key>)this.characterGroup.getProperty("declaredAlliedGroups");
			List<Key> allyGroupAlliances = (List<Key>)allyGroup.getProperty("declaredAlliedGroups");
			
			if (charGroupAlliances == null && allyGroupAlliances == null)
			{
				List<Key> createAllianceListCharGroup = new ArrayList<Key>();
				List<Key> createAllianceListAllyGroup = new ArrayList<Key>();

				createAllianceListCharGroup.add(allyGroup.getKey());
				createAllianceListAllyGroup.add(this.characterGroup.getKey());

				this.characterGroup.setProperty("declaredAlliedGroups", createAllianceListCharGroup);
				allyGroup.setProperty("declaredAlliedGroups", createAllianceListAllyGroup);
				allyGroup.setProperty("pendingAllianceGroupKey", null);
				
				groupsToSave.add(this.characterGroup);
				groupsToSave.add(allyGroup);
				ds.put(groupsToSave);
				return true;
			}
			else if (charGroupAlliances == null && allyGroupAlliances != null)
			{
				List<Key> createAllianceListCharGroup = new ArrayList<Key>();

				createAllianceListCharGroup.add(allyGroup.getKey());
				allyGroupAlliances.add(this.characterGroup.getKey());
				this.characterGroup.setProperty("declaredAlliedGroups", createAllianceListCharGroup);
				allyGroup.setProperty("declaredAlliedGroups", allyGroupAlliances);
				allyGroup.setProperty("pendingAllianceGroupKey", null);
				
				groupsToSave.add(this.characterGroup);
				groupsToSave.add(allyGroup);
				ds.put(groupsToSave);
				return true;
			}
			else if (charGroupAlliances != null && allyGroupAlliances == null)
			{
				List<Key> createAllianceListAllyGroup = new ArrayList<Key>();

				charGroupAlliances.add(allyGroup.getKey());
				createAllianceListAllyGroup.add(this.characterGroup.getKey());

				this.characterGroup.setProperty("declaredAlliedGroups", charGroupAlliances);
				allyGroup.setProperty("declaredAlliedGroups", createAllianceListAllyGroup);
				allyGroup.setProperty("pendingAllianceGroupKey", null);
				
				groupsToSave.add(this.characterGroup);
				groupsToSave.add(allyGroup);
				ds.put(groupsToSave);
				return true;
			}
			else if (charGroupAlliances.contains(allyGroup.getKey()) || allyGroupAlliances.contains(this.characterGroup.getKey()))
			{				
				throw new UserErrorMessage(
						"Already allied with this group.");
			}
			else if (charGroupAlliances != null && allyGroupAlliances != null)
			{
				charGroupAlliances.add(allyGroup.getKey());
				allyGroupAlliances.add(this.characterGroup.getKey());
				this.characterGroup.setProperty("declaredAlliedGroups", charGroupAlliances);
				allyGroup.setProperty("declaredAlliedGroups", allyGroupAlliances);
				allyGroup.setProperty("pendingAllianceGroupKey", null);
				groupsToSave.add(this.characterGroup);
				groupsToSave.add(allyGroup);
				ds.put(groupsToSave);
				return true;
			}	
		}
		return false;
	}
	
	public boolean declineAllianceRequest(CachedDatastoreService ds, CachedEntity allyGroup)
	{
		if(this.isAdmin && this.isCharacterInSpecifiedGroup(allyGroup) == false)
		{
			allyGroup.setProperty("pendingAllianceGroupKey", null);
			ds.put(allyGroup);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean deleteAlliance(CachedDatastoreService ds, CachedEntity allyGroup) throws UserErrorMessage
	{
		if(this.isAdmin)
		{
			List<Key> charGroupAlliances = (List<Key>)this.characterGroup.getProperty("declaredAlliedGroups");
			List<Key> allyGroupAlliances = (List<Key>)allyGroup.getProperty("declaredAlliedGroups");
			List<CachedEntity> groupsToSave = new ArrayList<CachedEntity>();
			
			if (charGroupAlliances != null && allyGroupAlliances != null)
			{
				if (charGroupAlliances.remove(allyGroup.getKey()))
				this.characterGroup.setProperty("declaredAlliedGroups", cleanNullKeysFromList(charGroupAlliances));
				
				allyGroupAlliances.remove(this.characterGroup.getKey());
				allyGroup.setProperty("declaredAlliedGroups", cleanNullKeysFromList(allyGroupAlliances));
				
				groupsToSave.add(allyGroup);
				groupsToSave.add(this.characterGroup);
				ds.put(groupsToSave);
				return true;			
			}
			return false;
		}
		throw new UserErrorMessage(
				"You are not an admin of the group.");
	}
}
