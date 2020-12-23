package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PropertyContainer;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.dbentities.QuestDefEntity;
import com.universeprojects.miniup.server.dbentities.QuestEntity;
import com.universeprojects.miniup.server.dbentities.QuestEntity.QuestStatus;
import com.universeprojects.miniup.server.dbentities.QuestObjective;

public class QuestService extends Service
{
	private final OperationBase command;
	private final CachedEntity character;
	public final static Key equipYourselfQuestKey = KeyFactory.createKey("QuestDef", 5394555692384256L);

	public QuestService(OperationBase command, ODPDBAccess db, CachedEntity character)
	{
		super(db);
		this.command = command;
		this.character = character;
	}

	private List<QuestDefEntity> allQuestDefs = null;
	public List<QuestDefEntity> getAllQuestDefs()
	{
		if (allQuestDefs!=null) return allQuestDefs;
		allQuestDefs = new ArrayList<>();
	
		List<Key> questDefsToFetch = new ArrayList<>();
		
		
		List<QuestEntity> allQuests = getAllQuests();
		if (allQuests!=null)
			for(QuestEntity quest:allQuests)
				questDefsToFetch.add((Key)quest.getQuestDefKey());
		
		List<CachedEntity> qdefs = ds.get(questDefsToFetch);
		
		for(CachedEntity item:qdefs)
			allQuestDefs.add(new QuestDefEntity(db, item));

		return allQuestDefs;
	}
	
	
	private List<QuestEntity> allQuests = null;
	public List<QuestEntity> getAllQuests()
	{
		if (allQuests!=null) return allQuests;
		
		List<CachedEntity> questEntities = query.getFilteredList("Quest", "characterKey", character.getKey());
		if  (questEntities==null || questEntities.isEmpty()) return null;
		
		allQuests = new ArrayList<QuestEntity>();
		for(CachedEntity item:questEntities)
		{
			QuestEntity questEntity = new QuestEntity(db, item);
			allQuests.add(questEntity);
		}
		
		return allQuests;
	}
	
	
	
	public Map<Key, QuestDefEntity> getMapOfAllQuestDefs()
	{
		List<QuestDefEntity> list = getAllQuestDefs();
		
		if (list==null) return null;
		Map<Key, QuestDefEntity> result = new LinkedHashMap<>();
		
		for(QuestDefEntity item:list)
			result.put(item.getKey(), item);
		
		return result;
		
	}

	private List<QuestDefEntity> activeQuestDefs = null;
	public List<QuestDefEntity> getActiveQuestDefs()
	{
		if (activeQuestDefs!=null) return activeQuestDefs;
		activeQuestDefs = new ArrayList<>();
	
		List<Key> questDefsToFetch = new ArrayList<>();
		
		
		List<QuestEntity> activeQuests = getActiveQuests();
		if (activeQuests!=null)
			for(QuestEntity quest:activeQuests)
				questDefsToFetch.add((Key)quest.getQuestDefKey());
		
		List<CachedEntity> qdefs = ds.get(questDefsToFetch);
		
		for(CachedEntity item:qdefs)
			activeQuestDefs.add(new QuestDefEntity(db, item));

		return activeQuestDefs;
	}
	
	
	private List<QuestEntity> activeQuests = null;
	public List<QuestEntity> getActiveQuests()
	{
		if (activeQuests!=null) return activeQuests;
		
		List<CachedEntity> questEntities = query.getFilteredList("Quest", "characterKey", character.getKey());
		if  (questEntities==null || questEntities.isEmpty()) return null;
		
		activeQuests = new ArrayList<QuestEntity>();
		for(CachedEntity item:questEntities)
		{
			if (item==null) continue;
			QuestEntity questEntity = new QuestEntity(db, item);
			if (questEntity.isComplete()==false)
				activeQuests.add(questEntity);
		}
		
		return activeQuests;
	}
	
	public QuestEntity getPinnedQuest() {
		CachedEntity rawQuest = db.getEntity((Key) character.getProperty("pinnedQuest"));
		if(rawQuest == null) return null;
		return new QuestEntity(db, rawQuest);
	}
	
	public QuestEntity createQuestInstance(Key questDefKey)
	{
		QuestEntity quest = new QuestEntity(db, character.getKey(), questDefKey);
		quest.getRawEntity().setProperty("characterKey", character.getKey());
		quest.getRawEntity().setProperty("questDefKey", questDefKey);
		quest.getRawEntity().setProperty("createdDate", new Date());
		return quest;
	}
	

	public void createDefaultNoobQuests()
	{
		QuestEntity entity = createQuestInstance(equipYourselfQuestKey);
		ds.put(entity.getRawEntity());
	}

	private boolean doExpressionChecks(QuestObjective objective, List<List<String>> expressions, PropertyContainer entityToCheck)
	{
		if (expressions==null) return false;
		
		final String operators = "(==|!=|>=|<=|\\*=)";
		for(int i = 0; i<expressions.size(); i++)
		{
			List<String> orFilters = expressions.get(i);
			
			boolean success = false;
			for(String filter:orFilters)
			{
				String[] parts = filter.split(operators);
				String fieldName = parts[0];
				String operator = filter.replaceAll(".*"+operators+".*", "$1");
				String conditionalValue = parts[1];
				Object value = null;
				if (entityToCheck.getProperty(fieldName)!=null)
					value = entityToCheck.getProperty(fieldName);
				if (value==null) value="null";
				
				if (db.doIfExpressionCheck(value, operator, conditionalValue))
				{
					success = true;
					break;
				}
			}
			if (success==true)
				return success;
		}
		
		return false;
	}

	Map<Key, QuestEntity> questEntityCache = null;
	private QuestEntity getQuestEntityFor(QuestDefEntity questDefEntity)
	{
		if (questEntityCache==null) questEntityCache = new HashMap<>();
		
		QuestEntity quest = questEntityCache.get(questDefEntity.getKey());
		if (quest==null)
		{
			quest = questDefEntity.getQuestEntity(character.getKey());
			questEntityCache.put(questDefEntity.getKey(), quest);
		}
		
		return quest;
	}
	
	
	
	
	
	
	
	
	public List<QuestObjective> checkCharacterPropertiesForObjectiveCompletions()
	{
		List<QuestObjective> completedObjectives = null;
		for(QuestDefEntity questDef:getActiveQuestDefs())
		{
			List<QuestObjective> objectiveData = questDef.getObjectiveData(character.getKey());
			for(QuestObjective objective:objectiveData)
			{
				if (objective.isComplete()) continue;
				if (objective.getNeededCharacterFieldValues()==null || objective.getNeededCharacterFieldValues().isEmpty()) continue;
				boolean success = doExpressionChecks(objective, objective.getNeededCharacterFieldValues(), character);
				
				if (success)
				{
					objective.flagCompleted();
					getQuestEntityFor(objective.getQuestDef()).updateObjectives(objectiveData);
					
					if (completedObjectives==null) completedObjectives = new ArrayList<>();
					completedObjectives.add(objective);
				}
			}
		}
		
		
		
		doQuestUpdated(completedObjectives);
		
		return completedObjectives;
	}

	public List<QuestObjective> checkAcquiredItemForObjectiveCompletions(CachedEntity item)
	{
		List<QuestObjective> completedObjectives = null;
		for(QuestDefEntity questDef:getActiveQuestDefs())
		{
			List<QuestObjective> objectiveData = questDef.getObjectiveData(character.getKey());
			for(QuestObjective objective:objectiveData)
			{
				if (objective.isComplete()) continue;
				if (objective.getNeededInventoryItemFieldValues()==null || objective.getNeededInventoryItemFieldValues().isEmpty()) continue;
				boolean success = doExpressionChecks(objective, objective.getNeededInventoryItemFieldValues(), item);
				
				if (success)
				{
					objective.flagCompleted();
					getQuestEntityFor(objective.getQuestDef()).updateObjectives(objectiveData);

					if (completedObjectives==null) completedObjectives = new ArrayList<>();
					completedObjectives.add(objective);
				}
			}
		}
		
		doQuestUpdated(completedObjectives);
		
		return completedObjectives;
	}

	public List<QuestObjective> checkLocationForObjectiveCompletions(CachedEntity location)
	{
		List<QuestObjective> completedObjectives = null;
		for(QuestDefEntity questDef:getActiveQuestDefs())
		{
			List<QuestObjective> objectiveData = questDef.getObjectiveData(character.getKey());
			for(QuestObjective objective:objectiveData)
			{
				if (objective.isComplete()) continue;
				if (objective.getNeededLocationFieldValues()==null || objective.getNeededLocationFieldValues().isEmpty()) continue;
				boolean success = doExpressionChecks(objective, objective.getNeededLocationFieldValues(), location);
				
				if (success)
				{
					objective.flagCompleted();
					getQuestEntityFor(objective.getQuestDef()).updateObjectives(objectiveData);

					if (completedObjectives==null) completedObjectives = new ArrayList<>();
					completedObjectives.add(objective);
				}
			}
		}
		
		doQuestUpdated(completedObjectives);
		
		return completedObjectives;
	}

	
	private void doQuestUpdated(List<QuestObjective> completedObjectives)
	{
		if (completedObjectives==null || completedObjectives.isEmpty()) return;
		
		// If we're here, objectives were completed
		Map<Key, QuestDefEntity> completedQuestDefEntities = new HashMap<>();
		Map<Key, QuestEntity> completedQuestsEntities = new HashMap<>();
		
		for(QuestObjective obj:completedObjectives)
			completedQuestDefEntities.put(obj.getQuestDef().getKey(), obj.getQuestDef());
		
		List<CachedEntity> questEntities = new ArrayList<>();
		for(QuestDefEntity questDef:completedQuestDefEntities.values())
		{
			QuestEntity questEntity = questDef.getQuestEntity(character.getKey());
			questEntities.add(questEntity.getRawEntity());
			completedQuestsEntities.put(questEntity.getKey(), questEntity);
		}
		db.getDB().put(questEntities);
		
		for(QuestObjective objective:completedObjectives)
		{
			db.sendGameMessage("'"+objective.getName()+"' objective complete");
		}

		boolean questComplete = false;
		for(CachedEntity quest:questEntities)
		{
			if (GameUtils.equals(quest.getProperty("status"), "Complete")==false) continue;
				
			questComplete = true;
			QuestDefEntity questDef = completedQuestDefEntities.get(quest.getProperty("questDefKey"));
			db.sendGameMessage("'"+questDef.getName()+"' quest complete!");

			boolean isNoobQuestLine = false;
			if (questDef.getQuestLine()!=null && questDef.isNoobQuest())
				isNoobQuestLine = true;
			
			activeQuestDefs.remove(questDef);
			activeQuests.remove(completedQuestsEntities.get(quest.getKey()));
			
			////////////////////////////////////////////////
			// Quest is now complete for the first time.
			
			// Advance to the next quests if there are any
			List<Key> nextQuestDefKeys = questDef.getNextQuests();
			db.pool.addToQueue(nextQuestDefKeys);
			db.pool.loadEntities();
			
			
			if (nextQuestDefKeys!=null && nextQuestDefKeys.isEmpty()==false)
			{
				for(Key questDefKey:nextQuestDefKeys)
				{
					QuestEntity nextQuest = createQuestInstance(questDefKey);
					QuestDefEntity questDefEntity = new QuestDefEntity(db, db.pool.get(questDefKey));
					activeQuestDefs.add(questDefEntity);
					activeQuests.add(nextQuest);
					
					db.getDB().put(nextQuest.getRawEntity());
					
					db.sendGameMessage("You have a new quest '<a onclick='viewQuest(\""+questDefEntity.getUrlSafeKey()+"\")'>"+db.pool.get(questDefKey).getProperty("name")+"</a>'.");
					
					// Do some initial checks to see if we already have some completed objectives on this new quest
					checkCharacterPropertiesForObjectiveCompletions();
					checkLocationForObjectiveCompletions(db.getCharacterLocation(character));
		
				}
			}
			else
			{
				command.flagNoobQuestLineComplete(questDef.getQuestLine());
			}
		}
		
		if (questComplete)
			command.flagQuestComplete();
		else
			command.flagObjectiveComplete();

//		if (activeQuestDefs.isEmpty()==false)
//		{
//			QuestDefEntity questDef = activeQuestDefs.get(0);
//			QuestEntity quest = activeQuests.get(0);
//			QuestObjective currentObjective = quest.getCurrentObjective(questDef);
////			if (currentObjective!=null)
////				command.addUITutorialsForObjective(currentObjective);
//		}
		
		command.getMPUS().updateQuestPanel();
		
		
	}



	public void clearCache()
	{
		activeQuestDefs = null;
		activeQuests = null;
		allQuests = null;
		allQuestDefs = null;
	}
	
	
}
