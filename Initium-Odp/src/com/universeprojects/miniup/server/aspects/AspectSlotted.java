package com.universeprojects.miniup.server.aspects;

import java.util.List;

import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;

public class AspectSlotted extends ItemAspect
{

	public AspectSlotted(InitiumObject object)
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
		if (getMaxCount()>0)
			return "Slotted";
		else
			return null;
	}

	
	public long getMaxCount()
	{
		Long count = (Long)this.getProperty("maxCount");
		if (count==null) count = 0L;
		return count;
	}
	
}
