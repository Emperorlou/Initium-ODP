package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class Group extends EntityWrapper 
{
	private List<Character> groupCharacters = null;
	public Group(CachedEntity group, ODPDBAccess db) 
	{
		super(group, db);
	}
	
	public Key creatorKey()
	{
		return (Key)this.getProperty("creatorKey");
	}
	
	public String getDescription()
	{
		return (String)this.getProperty("description");
	}
	
	private void populateGroupMembers()
	{
		List<CachedEntity> chars = db.getGroupMembers(null, wrappedEntity);
		groupCharacters = groupCharacters == null ? new ArrayList<Character>() : groupCharacters;
		for(CachedEntity member:chars)
		{
			if(member != null)
			{
				Character newChar = new Character(member, db);
				groupCharacters.add(newChar);
			}
		}
	}
	
	public Character[] getGroupMembers()
	{
		if(groupCharacters == null)
			populateGroupMembers();
		
		Character[] chars = new Character[groupCharacters.size()];
		return groupCharacters.toArray(chars);
	}
}
