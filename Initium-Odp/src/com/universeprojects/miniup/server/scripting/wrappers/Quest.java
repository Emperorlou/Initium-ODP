package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.dbentities.QuestDefEntity;
import com.universeprojects.miniup.server.dbentities.QuestEntity;
import com.universeprojects.miniup.server.dbentities.QuestEntity.QuestStatus;
import com.universeprojects.miniup.server.dbentities.QuestObjective;

public class Quest extends EntityWrapper{
	
	private QuestEntity rawQuest;
	private QuestDefEntity rawQuestDef;
	protected List<QuestObjective> objectives;
	
	public Quest(CachedEntity entity, ODPDBAccess db) {
		super(entity, db);
		
		rawQuest = new QuestEntity(db, entity);
		
		CachedEntity qde = db.getEntity(rawQuest.getQuestDefKey());
		
		rawQuestDef = new QuestDefEntity(db, qde);
		objectives = rawQuest.getObjectiveData(rawQuestDef);
		
	}
	
	public Quest(QuestEntity qe, ODPDBAccess db) {
		super(qe.getRawEntity(), db);
		
		rawQuest = new QuestEntity(db, qe.getRawEntity());
		
		CachedEntity qde = db.getEntity(rawQuest.getQuestDefKey());
		
		rawQuestDef = new QuestDefEntity(db, qde);
		objectives = rawQuest.getObjectiveData(rawQuestDef);
	}
	
	@Override
	public String getName() {
		return rawQuestDef.getName();
	}
	
	public String getQuestLine() {
		return rawQuestDef.getQuestLine();
	}
	
	public void setStatus(String status) {
		if(status == "Complete") {
			rawQuest.setStatus(QuestStatus.Active);
		}
		if(status == "Skipped") {
			rawQuest.setStatus(QuestStatus.Skipped);
		}
		if(status == "Active") {
			rawQuest.setStatus(QuestStatus.Active);
		}
	}
	
	public boolean isComplete() {
		return rawQuest.isComplete();
	}
	
	public boolean isSkipped() {
		return QuestStatus.Skipped == rawQuest.getStatus();
	}
	
	public boolean isActive() {
		return QuestStatus.Active == rawQuest.getStatus();
	}
	
	public void completeObjectiveByName(String name) {
		QuestObjective qo = rawQuest.getObjectiveByName(name, rawQuestDef);
		
		qo.flagCompleted();
	}
	
	public void completeCurrentObjective() {
		QuestObjective current = rawQuest.getCurrentObjective(rawQuestDef);
		
		current.flagCompleted();
	}
	
	/**
	 * Updates all objectives; flags the quest as complete if all objectives are complete.
	 */
	public void updateObjectives() {
		rawQuest.updateObjectives(rawQuest.getObjectiveData(rawQuestDef));
	}

	/**
	 * Add a custom objective to the quest. This objective can only be marked as "complete" from script context.
	 * @param name - the name of the objective; this will be displayed to the player
	 * @param completeEntireQuest - if true, completing this objective will complete the entire quest.
	 */
	public void addObjective(String name, boolean completeEntireQuest){
		List<EmbeddedEntity> objectives = rawQuest.getObjectives();

		if(objectives == null) objectives = new ArrayList<>();

		EmbeddedEntity newObj = new EmbeddedEntity();
		newObj.setProperty("name", name);
		newObj.setProperty("forceCompleteEntireQuest", completeEntireQuest);

		rawQuest.getRawEntity().setProperty("objectives", objectives);
	}
}
