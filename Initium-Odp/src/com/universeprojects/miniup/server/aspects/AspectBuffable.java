package com.universeprojects.miniup.server.aspects;

import java.util.List;

import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;

public class AspectBuffable extends ItemAspect
{

	public AspectBuffable(InitiumObject object)
	{
		super(object);
	}

	@Override
	protected void initialize()
	{

	}

	@Override
	public List<ItemPopupEntry> getItemPopupEntries()
	{
		return null;
	}

	@Override
	public String getPopupTag()
	{
		return "May contain magical buffs";
	}

}
