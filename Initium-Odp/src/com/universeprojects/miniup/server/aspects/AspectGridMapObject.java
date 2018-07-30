package com.universeprojects.miniup.server.aspects;

import java.util.List;

import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;

public class AspectGridMapObject extends ItemAspect
{

	public AspectGridMapObject(InitiumObject object)
	{
		super(object);
	}

	@Override
	public List<ItemPopupEntry> getItemPopupEntries()
	{
		return null;
	}

	@Override
	public String getPopupTag()
	{
		return null;
	}

}
