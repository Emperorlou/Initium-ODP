package com.universeprojects.miniup.server;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;

public abstract class ItemAspect extends InitiumAspect
{
	public ItemAspect(InitiumObject object)
	{
		super(object);
	}

	
	public abstract List<ItemPopupEntry> getItemPopupEntries(CachedEntity currentCharacter);

	public abstract String getPopupTag();

	public InitiumObject getObject()
	{
		return (InitiumObject)getGameObject();
	}

	public ItemPopupEntry createPopupEntryFromSkill(Key characterKey, Key ideaDefKey)
	{
		QueryHelper q = new QueryHelper(db.getDB());
		
		List<CachedEntity> skills = q.getFilteredList("ConstructItemSkill", 1, null, "characterKey", FilterOperator.EQUAL, characterKey, "_definitionKey", FilterOperator.EQUAL, ideaDefKey);
		CachedEntity ideaDef = db.getEntity(ideaDefKey);
		
		if (skills!=null && skills.isEmpty()==false)
		{
			CachedEntity skill = skills.get(0);
			String name = (String)ideaDef.getProperty("name");
			if (ideaDef.getProperty("nameShort")!=null) name = (String)ideaDef.getProperty("nameShort");
			String icon = (String)skill.getProperty("icon");
			String description = (String)ideaDef.getProperty("skillDescription");
			String js = "closeAllPopups(); closeAllPagePopups(); doConstructItemSkill(event, "+skill.getKey().getId()+", '"+WebUtils.jsSafe(name)+"');";
			ItemPopupEntry ipe = new ItemPopupEntry(name, description, js);
			return ipe;
		}

		List<CachedEntity> ideas = q.getFilteredList("ConstructItemIdea", 1, null, "characterKey", FilterOperator.EQUAL, characterKey, "_definitionKey", FilterOperator.EQUAL, ideaDefKey);
		if (ideas!=null && ideas.isEmpty()==false)
		{
			CachedEntity idea = ideas.get(0);
			String name = (String)ideaDef.getProperty("name");
			if (ideaDef.getProperty("nameShort")!=null) name = (String)ideaDef.getProperty("nameShort");
			String icon = (String)idea.getProperty("icon");
			String description = (String)ideaDef.getProperty("ideaDescription");
			String js = "closeAllPopups(); closeAllPagePopups(); doCreatePrototype(event, "+idea.getKey().getId()+", '"+WebUtils.jsSafe(name)+"');";
			ItemPopupEntry ipe = new ItemPopupEntry(name, description, js);
			return ipe;
		}

		return null;
//		String name = "Experiment on "+this.getObject().getName();
//		String icon = (String)ideaDef.getProperty("icon");
//		String description = (String)ideaDef.getProperty("ideaDescription")+"<br>You do not currently have an idea for this skill. Perhaps try experimenting.";
//		String js = "closeAllPopups(); closeAllPagePopups(); doExperiment(event, "+this.getInitiumObject().getKey().getId()+");";
//		ItemPopupEntry ipe = new ItemPopupEntry(name, description, js);
//		return ipe;
	}
	
	public static class ItemPopupEntry
	{
		public String name;
		public String description;
		public String clickJavascript;
		
		public ItemPopupEntry(String name, String description, String clickJavascript)
		{
			this.name = name;
			this.description = description;
			this.clickJavascript = clickJavascript;
		}
		
	}
}
