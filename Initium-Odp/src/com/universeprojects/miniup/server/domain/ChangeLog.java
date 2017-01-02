package com.universeprojects.miniup.server.domain;

import java.util.Date;

import com.google.appengine.api.datastore.Text;
import com.universeprojects.cacheddatastore.CachedEntity;

//Set a description
public class ChangeLog extends OdpDomain {

	public ChangeLog() {
		super(new CachedEntity("ChangeLog"));
	}

	public ChangeLog(CachedEntity cachedEntity) {
		super(cachedEntity, "ChangeLog");
	}

	public void setCreatedDate(Date createdDate) {
		getCachedEntity().setProperty("createdDate", createdDate);
	}

	public Date getCreatedDate() {
		return (Date) getCachedEntity().getProperty("createdDate");
	}

	// (0)
	public void setImportance(Long importance) {
		getCachedEntity().setProperty("importance", importance);
	}

	public Long getImportance() {
		return (Long) getCachedEntity().getProperty("importance");
	}

	public void setLog(Text log) {
		getCachedEntity().setProperty("log", log);
	}

	public Text getLog() {
		return (Text) getCachedEntity().getProperty("log");
	}

}
