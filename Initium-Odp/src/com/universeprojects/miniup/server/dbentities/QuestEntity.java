package com.universeprojects.miniup.server.dbentities;

import java.util.Date;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class QuestEntity extends InitiumEntityBase
{
	public enum QuestStatus
	{
		Active,
		Complete,
		Skipped
	}
	
	public QuestEntity(ODPDBAccess db, Key characterKey, Key questDefKey)
	{
		super(db, new CachedEntity("Quest", characterKey.toString()+questDefKey.toString()));
	}
	
	public QuestEntity(ODPDBAccess db, CachedEntity entity)
	{
		super(db, entity);
	}

	@Override
	protected String getKind()
	{
		return "Quest";
	}

	public QuestStatus getStatus()
	{
		if (entity.getProperty("status")==null) return QuestStatus.Active;
		return QuestStatus.valueOf((String)entity.getProperty("status"));
	}
	
	public void setStatus(QuestStatus status)
	{
		if (status==null) status = QuestStatus.Active;
		entity.setProperty("status", status.toString());
	}
	
	public boolean isComplete()
	{
		return getStatus()==QuestStatus.Complete;
	}

	public Key getQuestDefKey()
	{
		return (Key)entity.getProperty("questDefKey");
	}
	
	public Date getCreatedDate()
	{
		return (Date)entity.getProperty("createdDate");
	}
}
