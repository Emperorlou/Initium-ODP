package com.universeprojects.miniup.server.domain;

import java.util.Date;

import com.google.appengine.api.datastore.Text;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * Set a description
 * 
 * @author kyle-miller
 *
 */
public class ChangeLog extends OdpDomain {
	public static final String KIND = "ChangeLog";

	public ChangeLog() {
		super(new CachedEntity(KIND));
	}

	public ChangeLog(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 * 
	 * @param createdDate
	 */
	public void setCreatedDate(Date createdDate) {
		getCachedEntity().setProperty("createdDate", createdDate);
	}

	public Date getCreatedDate() {
		return (Date) getCachedEntity().getProperty("createdDate");
	}

	/**
	 *  (0)
	 *  
	 * @param importance
	 */
	public void setImportance(Long importance) {
		getCachedEntity().setProperty("importance", importance);
	}

	public Long getImportance() {
		return (Long) getCachedEntity().getProperty("importance");
	}

	/**
	 * 
	 * @param log
	 */
	public void setLog(Text log) {
		getCachedEntity().setProperty("log", log);
	}

	public Text getLog() {
		return (Text) getCachedEntity().getProperty("log");
	}

}
