package com.universeprojects.miniup.server.aspects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;
import com.universeprojects.miniup.server.ItemAspect.ItemPopupEntry;

public class AspectTree extends ItemAspect
{

	protected AspectTree(InitiumObject object)
	{
		super(object);
	}

	@Override
	protected void initialize()
	{

	}

	@Override
	public List<ItemPopupEntry> getItemPopupEntries(CachedEntity currentCharacter)
	{
		ArrayList<ItemPopupEntry> result = new ArrayList<>();

		if (isFelled()==false)
		{
			ItemPopupEntry entry = createPopupEntryFromSkill(currentCharacter.getKey(), KeyFactory.createKey("ConstructItemIdeaDef", 6516394060677120L));
			if (entry!=null)
			{
				result.add(entry);
				return result;
			}
			else
			{
				result.add(new ItemPopupEntry("", "You don't have any ideas for what you can do with this tree at the moment but try experimenting with it and a little think to see what you might come up with!<br>(Click the flask icon below)", ""));
				return result;
			}
		}
		else if (GameUtils.equals(getBark(), 0L) && GameUtils.equals(getKindling(), 0L) && GameUtils.equals(getSmallBranches(), 0L) && GameUtils.equals(getLargeBranches(), 0L) && 
				getSmallLogs()>0L)
		{
			result.add(createPopupEntryFromSkill(currentCharacter.getKey(), KeyFactory.createKey("ConstructItemIdeaDef", 6232759201955840L)));
		}
		else if (GameUtils.equals(getBark(), 0L) && GameUtils.equals(getKindling(), 0L) && GameUtils.equals(getSmallBranches(), 0L) && GameUtils.equals(getLargeBranches(), 0L) && 
				getLargeLogs()>0L)
		{
			result.add(createPopupEntryFromSkill(currentCharacter.getKey(), KeyFactory.createKey("ConstructItemIdeaDef", 6274310997278720L)));
		}
		
		if (getBark()>0)
		{
			result.add(createPopupEntryFromSkill(currentCharacter.getKey(), KeyFactory.createKey("ConstructItemIdeaDef", 5188681358114816L)));
		}
		if (getKindling()>0)
		{
			result.add(createPopupEntryFromSkill(currentCharacter.getKey(), KeyFactory.createKey("ConstructItemIdeaDef", 5919984516857856L)));
		}
		if (getSmallBranches()>0)
		{
			result.add(createPopupEntryFromSkill(currentCharacter.getKey(), KeyFactory.createKey("ConstructItemIdeaDef", 4823379239763968L)));
		}
		if (getLargeBranches()>0)
		{
			result.add(createPopupEntryFromSkill(currentCharacter.getKey(), KeyFactory.createKey("ConstructItemIdeaDef", 6047534979219456L)));
		}
		
		result.removeAll(Collections.singleton(null));
		if (result==null || result.isEmpty())
			result.add(new ItemPopupEntry("", "You don't have any ideas on what to do with trees at the moment but try experimenting and having a little sit down to see what you might come up with!<br>(Click the flask icon below)", ""));
		
		return result;
	}

	@Override
	public String getPopupTag()
	{
		return null;
	}


	public boolean isFelled()
	{
		return GameUtils.equals(getProperty("felled"), true);
	}

	public Long getKindling()
	{
		if (getProperty("kindling")==null) return 0L;
		return (Long)getProperty("kindling");
	}

	public Long getLargeBranches()
	{
		if (getProperty("largeBranches")==null) return 0L;
		return (Long)getProperty("largeBranches");
	}

	public Long getSmallBranches()
	{
		if (getProperty("smallBranches")==null) return 0L;
		return (Long)getProperty("smallBranches");
	}

	public Long getLargeLogs()
	{
		if (getProperty("largeLogs")==null) return 0L;
		return (Long)getProperty("largeLogs");
	}

	public Long getSmallLogs()
	{
		if (getProperty("smallLogs")==null) return 0L;
		return (Long)getProperty("smallLogs");
	}

	public Long getBark()
	{
		if (getProperty("bark")==null) return 0L;
		return (Long)getProperty("bark");
	}

}
