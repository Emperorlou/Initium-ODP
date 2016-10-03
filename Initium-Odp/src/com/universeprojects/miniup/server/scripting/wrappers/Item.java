package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.Date;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class Item extends WrapperBase
{
	public Item(CachedEntity item, ODPDBAccess db)
	{
		super(item, db);
	}

	public String getName() {
		return (String) this.getProperty("name");
	}

	public Key getContainerKey() {
		return (Key) this.getProperty("containerKey");
	}

	public void setContainerKey(Key containerKey) {
		this.setProperty("containerKey", containerKey);
	}

	public Date getMovedTimestamp() {
		return (Date) this.getProperty("movedTimestamp");
	}

	public void setMovedTimestamp(Date movedTimestamp) {
		this.setProperty("movedTimestamp", movedTimestamp);
	}
}
