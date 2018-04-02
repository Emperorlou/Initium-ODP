package com.universeprojects.miniup.server.dbentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;

public class GuardSetting extends InitiumEntityBase
{
	public enum GuardExclusion
	{
		Group, Party, Alliances
	}
	
	public enum GuardType
	{
		NoMoving,
		NoUsing,
		NoTrespassers,
		NoGuarding
	}
	
	public GuardSetting(ODPDBAccess db, CachedEntity entity)
	{
		super(db, entity);
	}

	@Override
	protected String getKind()
	{
		return "GuardSetting";
	}

	public boolean isActive()
	{
		return (Boolean)entity.getProperty("active");
	}
	
	public void setActive(boolean value){
		entity.setProperty("active", value);
	}
	
	public Key getCharacterKey()
	{
		return (Key)entity.getProperty("characterKey");
	}

	public void setCharacterKey(Key characterKey)
	{
		entity.setProperty("characterKey", characterKey);
	}

	public Key getEntityKey()
	{
		return (Key)entity.getProperty("entityKey");
	}

	public void setEntityKey(Key entityKey)
	{
		entity.setProperty("entityKey", entityKey);
	}

	public Set<GuardExclusion> getExclude()
	{
		String raw = (String)entity.getProperty("exclude");
		if (raw==null) return new HashSet<>();
		String[] rawValues = raw.split(",");
		
		Set<GuardExclusion> result = new HashSet<>();
		for(String val:rawValues)
			result.add(GuardExclusion.valueOf(val));
		
		return result;
	}

	public void setExclude(GuardExclusion...exclude)
	{
		entity.setProperty("exclude", StringUtils.join(exclude, ','));
	}

	public Key getLocationKey()
	{
		return (Key)entity.getProperty("locationKey");
	}

	public void setLocationKey(Key locationKey)
	{
		entity.setProperty("locationKey", locationKey);
	}

	public Set<GuardType> getSettings()
	{
		String raw = (String)entity.getProperty("settings");
		if (raw==null) return new HashSet<>();
		String[] rawValues = raw.split(",");
		
		Set<GuardType> result = new HashSet<>();
		for(String val:rawValues)
			result.add(GuardType.valueOf(val));
		
		return result;
	}

	public void setSettings(GuardType...settings)
	{
		entity.setProperty("settings", StringUtils.join(settings, ','));
	}
	
	
	public Long getLine()
	{
		return (Long)entity.getProperty("line");
	}
	
	public void setLine(Long line)
	{
		entity.setProperty("line", line);
	}
	
	public String getFullLine(CachedEntity entityGuarding)
	{
		String name = (String)entityGuarding.getProperty("name");
		String personsExclusionText = getExclusionText(getExclude());
		String guardTypeText = getGuardTypeText(entityGuarding.getKind(), getSettings());
		
		String finalGuardLine = "Guarding "+name+" from anyone "+guardTypeText+" "+personsExclusionText;
		
		return finalGuardLine.trim();
	}
	
	
	protected static String generateHumanReadableList(List<String> list)
	{
		String text = "";
		int i = 0; 
		boolean isLast = false;
		boolean isFirst = true;
		boolean onlyOne = list.size()==1;
		for(String e:list)
		{
			String separator = "";
			if (onlyOne==false)
			{
				if (GameUtils.equals(i+1, list.size())) isLast = true;
				if (isFirst)
				{
					// Skip
				}
				else if (isLast)
				{
					separator = ", and ";
				}
				else
				{
					separator = ", ";
				}
				isFirst = false;
			}
			
			text+=separator+e;
			i++;
		}
		return text;
	}
	    
	protected static String getExclusionText(Set<GuardExclusion> e)
	{
		String text = "";
		if (e==null || e.isEmpty()) return text;

		text+=" (excluding ";
		List<String> list = new ArrayList<>();
		for(GuardExclusion ge:e)
		{
			if (ge == GuardExclusion.Group)
				list.add("your group");
			else if (ge == GuardExclusion.Party)
				list.add("your party");
			else if (ge == GuardExclusion.Alliances)
				list.add("your group's alliances");
			else
				throw new RuntimeException("Unhandled type");
			
		}
		text+=generateHumanReadableList(list);
		text+=")";
		return text;
	}

	protected static String getGuardTypeText(String kind, Set<GuardType> types)
	{
		String text = "...actually nevermind";
		if (types==null || types.isEmpty()) return text;

		text=" ";
		List<String> list = new ArrayList<>();
		for(GuardType ge:types)
		{
			if (kind==null)
			{
				throw new RuntimeException("Unhandled type");
			}
			else if (kind.equals("Item"))
			{
				if (ge == GuardType.NoMoving)
					list.add("taking it");
				else if (ge == GuardType.NoTrespassers)
					list.add("entering it");
				else if (ge == GuardType.NoUsing)
					list.add("using it");
				else if (ge == GuardType.NoGuarding)
					list.add("guarding it");
				else
					throw new RuntimeException("Unhandled type");
			}
			else if (kind.equals("Location"))
			{
				if (ge == GuardType.NoTrespassers)
					list.add("coming here");
				else if (ge == GuardType.NoMoving)
					list.add("taking anything");
				else if (ge == GuardType.NoUsing)
					list.add("using anything");
				else if (ge == GuardType.NoGuarding)
					list.add("guarding anything");
				else
					throw new RuntimeException("Unhandled type");
			}
			
				
			
		}
		text+=generateHumanReadableList(list);
		
		return text;
	}
	
}
