package com.universeprojects.miniup.server.aspects;

import java.util.List;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;

public class AspectFirestarter extends ItemAspect
{

	public AspectFirestarter(InitiumObject object)
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
		return null;
	}

	@Override
	public String getPopupTag()
	{
		return "Can start a fire";
	}

	@Override
	public Integer getVersion() {
		return 1;
	}

}
