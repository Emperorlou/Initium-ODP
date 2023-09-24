package com.universeprojects.miniup.server.aspects;

import java.util.List;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;

public class AspectGridMapObject extends ItemAspect
{

	public AspectGridMapObject(InitiumObject object)
	{
		super(object);
	}

	@Override
	public List<ItemPopupEntry> getItemPopupEntries(CachedEntity currentCharacter)
	{
		return null;
	}

	@Override
	public String getPopupTag()
	{
		return null;
	}

	public boolean isAttached()
	{
		return "Attached".equals(getProperty("mode"));
	}
	
	public boolean isLoose()
	{
		if (getProperty("mode")==null) return true;
		return "Loose".equals(getProperty("mode"));
	}

	@Override
	public Integer getVersion() {
		return 1;
	}

}
