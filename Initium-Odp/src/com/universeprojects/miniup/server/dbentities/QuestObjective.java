package com.universeprojects.miniup.server.dbentities;

import java.util.List;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;

public class QuestObjective
{
	final public ODPDBAccess db;
	final public Integer index;
	final public QuestDefEntity questDef;
	final public EmbeddedEntity data;
	
	public QuestObjective(ODPDBAccess db, EmbeddedEntity data, QuestDefEntity parent, int objectiveIndex)
	{
		this.db = db;
		this.index = objectiveIndex;
		this.data = data;
		this.questDef = parent;
	}
	
	public QuestDefEntity getQuestDef()
	{
		return questDef;
	}
	
	public int getObjectiveIndex()
	{
		return index;
	}
	
	public String getName()
	{
		return (String)data.getProperty("name");
	}

	public boolean isComplete()
	{
		return GameUtils.booleanEquals(data.getProperty("complete"), true);
	}

	public Double getProgress()
	{
		return (Double)data.getProperty("progress");
	}
	
	public Double getProgressTotal()
	{
		return (Double)data.getProperty("progressTotal");
	}
	
	public List<List<String>> getNeededCharacterFieldValues()
	{
		return db.getValueFromFieldTypeFieldFilter2DCollection(data, "neededCharacterFieldValues");
	}
	public List<List<String>> getNeededInventoryItemFieldValues()
	{
		return db.getValueFromFieldTypeFieldFilter2DCollection(data, "neededInventoryItemFieldValues");
	}
	public List<List<String>> getNeededLocationFieldValues()
	{
		return db.getValueFromFieldTypeFieldFilter2DCollection(data, "neededLocationFieldValues");
	}
	
	public boolean isForceCompleteEntireQuest()
	{
		return GameUtils.equals(data.getProperty("forceCompleteEntireQuest"), true);
	}

	
	
	public void flagCompleted()
	{
		data.setProperty("complete", true);
	}

}
