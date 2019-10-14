package com.universeprojects.miniup.server.dbentities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.EmbeddedEntity;
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

	public List<EmbeddedEntity> getObjectives()
	{
		return (List<EmbeddedEntity>)entity.getProperty("objectives");
	}
	
	public List<QuestObjective> getObjectiveData(QuestDefEntity questDef)
	{
		List<QuestObjective> list = null;

		List<EmbeddedEntity> objectiveEEs = getObjectives();
		if (objectiveEEs==null || objectiveEEs.isEmpty())
			objectiveEEs = questDef.getObjectives();
		
		if (objectiveEEs!=null)
		{
			list = new ArrayList<>();
			for(int i = 0; i<objectiveEEs.size(); i++)
			{
				EmbeddedEntity objEE = objectiveEEs.get(i);
				list.add(new QuestObjective(db, objEE, questDef, i));
			}
		}
		
		return list;
	}
	
	public void updateObjectives(List<QuestObjective> objectiveData)
	{
		List<EmbeddedEntity> list = new ArrayList<>();
		boolean complete = true;
		for(QuestObjective o:objectiveData)
		{
			list.add(o.data);
			if (o.isForceCompleteEntireQuest() && o.isComplete())
			{
				for(QuestObjective objective:objectiveData) objective.flagCompleted();
				complete = true;
				break;
			}
			if (o.isComplete()==false) complete = false;
		}
		
		if (complete)
			setStatus(QuestStatus.Complete);
		
		entity.setProperty("objectives", list);
	}

	public QuestObjective getCurrentObjective(QuestDefEntity questDef)
	{
		QuestObjective currentObjective = null;
		List<QuestObjective> objectives = this.getObjectiveData(questDef);
		if (objectives==null) return null;

		for(QuestObjective obj:objectives)
			if (obj.isComplete()==false)
			{
				currentObjective = obj;
				break;
			}
		
		return currentObjective;
	}
}
