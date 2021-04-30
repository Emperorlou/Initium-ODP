package com.universeprojects.miniup.server.aspects;

import java.util.List;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.universeprojects.cacheddatastore.CachedEntity;
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
	public List<ItemPopupEntry> getItemPopupEntries(CachedEntity currentCharacter)
	{
		return null;
	}

	@Override
	public String getPopupTag()
	{
		return "May contain magical buffs";
	}
	
	
	@SuppressWarnings("unchecked")
	public List<EmbeddedEntity> getBuffs()
	{
		return (List<EmbeddedEntity>)getProperty("buffs");
	}
	
	public void clearAllBuffs()
	{
		setProperty("buffs", null);
	}

	public void setBuffs(List<EmbeddedEntity> list)
	{
		setProperty("buffs", list);
	}

	@Override
	public Integer getVersion() {
		return 1;
	}

}
