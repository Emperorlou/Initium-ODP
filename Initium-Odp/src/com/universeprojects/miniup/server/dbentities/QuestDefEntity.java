package com.universeprojects.miniup.server.dbentities;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ContentDeveloperException;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.services.ODPInventionService;

public class QuestDefEntity extends InitiumEntityBase
{

	public QuestDefEntity(ODPDBAccess db, CachedEntity entity)
	{
		super(db, entity);
	}

	@Override
	protected String getKind()
	{
		return "QuestDef";
	}

	public String getName()
	{
		return (String) entity.getProperty("name");
	}

	public String getDescription()
	{
		return (String) entity.getProperty("description");
	}

	@SuppressWarnings("unchecked")
	public List<String> getRequiredCharacterFieldValuesObjectiveNames()
	{
		return (List<String>) entity.getProperty("requiredCharacterFieldValuesObjectiveNames");
	}

	@SuppressWarnings("unchecked")
	public List<String> getRequiredItemsObjectiveNames()
	{
		return (List<String>) entity.getProperty("requiredItemsObjectiveNames");
	}

	public List<List<String>> getRequiredCharacterFieldFilters()
	{
		return db.getValueFromFieldTypeFieldFilter2DCollection(entity, "requiredCharacterFieldValues");
	}

	@SuppressWarnings("unchecked")
	public List<Key> getRequiredItems()
	{
		return (List<Key>) entity.getProperty("requiredItems");
	}

	@SuppressWarnings("unchecked")
	public List<Key> getNextQuests()
	{
		return (List<Key>) entity.getProperty("nextQuest");
	}

	@SuppressWarnings("unchecked")
	public List<EmbeddedEntity> getObjectives()
	{
		return (List<EmbeddedEntity>) entity.getProperty("objectives");
	}

	public void setObjectives(List<EmbeddedEntity> objectives)
	{

	}

	public void setObjectiveData(List<QuestObjective> objectives)
	{

	}

	public class Objective
	{
		final public Integer index;
		final public QuestDefEntity questDef;
		final public EmbeddedEntity data;

		public Objective(EmbeddedEntity data, QuestDefEntity parent, int objectiveIndex)
		{
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
			return (String) data.getProperty("name");
		}

		public boolean isComplete()
		{
			return GameUtils.booleanEquals(data.getProperty("complete"), true);
		}

		public Double getProgress()
		{
			return (Double) data.getProperty("progress");
		}

		public Double getProgressTotal()
		{
			return (Double) data.getProperty("progressTotal");
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

		public void flagCompleted()
		{
			data.setProperty("complete", true);
		}
	}

	public List<QuestObjective> getObjectiveData(Key characterKey)
	{
		List<QuestObjective> list = new ArrayList<>();

		QuestEntity questEntity = getQuestEntity(characterKey);
		
		
		List<EmbeddedEntity> objectiveEEs = questEntity.getObjectives();
		if (objectiveEEs==null || objectiveEEs.isEmpty()) objectiveEEs = getObjectives();
		
		if (objectiveEEs!=null)
			for (int i = 0; i < objectiveEEs.size(); i++)
			{
				EmbeddedEntity objEE = objectiveEEs.get(i);
				list.add(new QuestObjective(db, objEE, this, i));
			}

		return list;

	}


	// public List<QuestObjective> getObjectiveDataOld()
	// {
	// List<QuestObjective> list = new ArrayList<>();
	//
	// List<String> requiredItemsObjectiveNames =
	// getRequiredItemsObjectiveNames();
	// List<Key> requiredItemEntityRequirementKeys = getRequiredItems();
	//
	// List<String> requiredCharacterFieldValuesObjectiveNames =
	// getRequiredCharacterFieldValuesObjectiveNames();
	// List<List<String>> requiredCharacterFieldFilters =
	// getRequiredCharacterFieldFilters();
	//
	//
	// // Trying to see if all lists have the same number of entries in it (each
	// entry corresponds to one objective)
	// // TODO: Make this better, currently it'll throw an NPE in some
	// situations but I'm lazy to figure this out and it's super late
	// if (requiredItemEntityRequirementKeys!=null &&
	// requiredItemsObjectiveNames!=null &&
	// requiredItemEntityRequirementKeys.size()!=requiredItemsObjectiveNames.size())
	// throw new ContentDeveloperException("Quest definition is not setup
	// properly for the quest entitled '"+getName()+"'. Objective related lists
	// must have the same number of rows/entries.");
	//
	// if (requiredCharacterFieldFilters!=null &&
	// requiredCharacterFieldValuesObjectiveNames!=null &&
	// requiredCharacterFieldFilters.size()!=requiredCharacterFieldValuesObjectiveNames.size())
	// throw new ContentDeveloperException("Quest definition is not setup
	// properly for the quest entitled '"+getName()+"'. Objective related lists
	// must have the same number of rows/entries.");
	//
	//
	// List<CachedEntity> requiredItemEntityRequirements =
	// ds.get(requiredItemEntityRequirementKeys);
	//
	// // The required item objectives...
	// ODPInventionService inventionService =
	// db.getInventionService(db.getCurrentCharacter(), null);
	// List<CachedEntity> inventory = query.getFilteredList("Item",
	// "containerKey", db.getCurrentCharacterKey());
	// if (requiredItemEntityRequirements!=null)
	// for(int i = 0; i<requiredItemEntityRequirements.size(); i++)
	// {
	// CachedEntity entityRequirement = requiredItemEntityRequirements.get(i);
	//
	// Objective objective = new Objective();
	// list.add(objective);
	//
	// objective.name = requiredItemsObjectiveNames.get(i);
	// boolean success = false;
	// for(CachedEntity item:inventory)
	// if (inventionService.validateEntityRequirement(entityRequirement, item))
	// {
	// success = true;
	// break;
	// }
	//
	// objective.complete = success;
	// }
	//
	//
	// // The character field objectives...
	// final String operators = "(==|!=|>=|<=|\\*=)";
	// List<List<String>> allFilters = getRequiredCharacterFieldFilters();
	// if (allFilters!=null)
	// for(int i = 0; i<allFilters.size(); i++)
	// {
	// List<String> orFilters = allFilters.get(i);
	//
	// Objective objective = new Objective();
	// list.add(objective);
	//
	// objective.name = requiredCharacterFieldValuesObjectiveNames.get(i);
	// boolean success = false;
	// for(String filter:orFilters)
	// {
	// String[] parts = filter.split(operators);
	// String fieldName = parts[0];
	// String operator = filter.replaceAll(".*"+operators+".*", "$1");
	// String conditionalValue = parts[1];
	// Object value = null;
	// if (db.getCurrentCharacter().getProperty(fieldName)!=null)
	// value = db.getCurrentCharacter().getProperty(fieldName);
	//
	// if (inventionService.doIfExpressionCheck(conditionalValue, operator,
	// value))
	// {
	// success = true;
	// break;
	// }
	// }
	//
	// objective.complete = success;
	// }
	//// db.validateFieldFilter(getRawEntity(), "", db.getCurrentCharacter());
	//
	//
	// return list;
	// }

	public QuestEntity getQuestEntity(Key characterKey)
	{
		Key questKey = getQuestEntityKey(characterKey);

		CachedEntity entity = db.getEntity(questKey);
		
		if (entity == null) 
			return null;
		else
			db.pool.addEntityDirectly(entity);
		
		return new QuestEntity(db, entity);
	}

	public Key getQuestEntityKey(Key characterKey)
	{
		return KeyFactory.createKey("Quest", characterKey.toString() + getKey().toString());
	}

}
