package com.universeprojects.miniup.server.aspects;

import java.util.List;

import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;

public class AspectFlammable extends ItemAspect
{

	public AspectFlammable(InitiumObject object)
	{
		super(object);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initialize()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public List<ItemPopupEntry> getItemPopupEntries()
	{
		return null;
	}

	@Override
	public String getPopupTag()
	{
		return "Flammable";
	}

}
