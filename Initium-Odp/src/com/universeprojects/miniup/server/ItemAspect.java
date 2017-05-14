package com.universeprojects.miniup.server;

import java.util.List;

public abstract class ItemAspect extends InitiumAspect
{
	public ItemAspect(InitiumObject object)
	{
		super(object);
	}

	
	public abstract List<ItemPopupEntry> getItemPopupEntries();

	public abstract String getPopupTag();
	
	public class ItemPopupEntry
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
