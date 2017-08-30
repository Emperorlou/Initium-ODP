package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.dbentities.QuestDefEntity;
import com.universeprojects.miniup.server.dbentities.QuestEntity;
import com.universeprojects.miniup.server.dbentities.QuestEntity.QuestStatus;

public class QuestService extends Service
{
	public final static Key equipYourselfQuestKey = KeyFactory.createKey("QuestDef", 5394555692384256L);

	public QuestService(ODPDBAccess db)
	{
		super(db);
	}
	
	public Map<Key, QuestDefEntity> getMapOfActiveQuestDefs()
	{
		List<QuestDefEntity> list = getActiveQuestDefs();
		
		if (list==null) return null;
		Map<Key, QuestDefEntity> result = new LinkedHashMap<>();
		
		for(QuestDefEntity item:list)
			result.put(item.getKey(), item);
		
		return result;
		
	}
	
	public List<QuestDefEntity> getActiveQuestDefs()
	{
		List<QuestDefEntity> questDefs = new ArrayList<>();
	
		List<Key> questDefsToFetch = new ArrayList<>();
		
		
		List<QuestEntity> activeQuests = getActiveQuests();
		if (activeQuests!=null)
			for(QuestEntity quest:activeQuests)
				questDefsToFetch.add((Key)quest.getQuestDefKey());
		
		List<CachedEntity> qdefs = ds.get(questDefsToFetch);
		
		for(CachedEntity item:qdefs)
			questDefs.add(new QuestDefEntity(db, item));
		
		return questDefs;
	}
	
	
	private List<QuestEntity> questsCache = null;
	public List<QuestEntity> getActiveQuests()
	{
		if (questsCache!=null) return questsCache;
		
		List<CachedEntity> questEntities = query.getFilteredList("Quest", "characterKey", db.getCurrentCharacterKey());
		if  (questEntities==null || questEntities.isEmpty()) return null;
		
		questsCache = new ArrayList<QuestEntity>();
		for(CachedEntity item:questEntities)
			questsCache.add(new QuestEntity(db, item));
		
		return questsCache;
	}
	
	public QuestEntity createQuestInstance(Key characterKey, Key questDefKey)
	{
		QuestEntity quest = new QuestEntity(db, characterKey, questDefKey);
		quest.getRawEntity().setProperty("characterKey", characterKey);
		quest.getRawEntity().setProperty("questDefKey", questDefKey);
		quest.getRawEntity().setProperty("createdDate", new Date());
		return quest;
	}
	
	/**
	 * Returns true if a new quest was granted to the player.
	 * 
	 * @param quest
	 * @param questDef
	 * @param isComplete
	 * @return
	 */
	public boolean updateQuest(QuestEntity quest, QuestDefEntity questDef, boolean isComplete)
	{
		boolean newQuestGiven = false;
		if (isComplete && quest.getStatus()!=QuestStatus.Complete)
		{
			////////////////////////////////////////////////
			// Quest is now complete for the first time.
			
			
			// 1. Set the quest's complete status
			quest.setStatus(QuestStatus.Complete);

			
			// 2. Advance to the next quests if there are any
			Key characterKey = db.getCurrentCharacterKey();
			List<Key> nextQuestDefKeys = questDef.getNextQuests();
			if (nextQuestDefKeys!=null)
				for(Key questDefKey:nextQuestDefKeys)
				{
					QuestEntity nextQuest = createQuestInstance(characterKey, questDefKey);
					db.getDB().put(nextQuest.getRawEntity());
					newQuestGiven = true;
				}
			
			db.getDB().put(quest.getRawEntity());
		}
		return newQuestGiven;
	}

	public void createDefaultNoobQuests(Key characterKey)
	{
		QuestEntity entity = createQuestInstance(characterKey, equipYourselfQuestKey);
		ds.put(entity.getRawEntity());
	}

}
