package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.dbentities.QuestDefEntity;
import com.universeprojects.miniup.server.dbentities.QuestEntity;
import com.universeprojects.miniup.server.services.QuestService;

public class CommandPinQuest extends Command{

	public CommandPinQuest(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {		
		QuestService qService = db.getQuestService(this);
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		Long questDefId = Long.parseLong(parameters.get("questId"));
		CachedEntity rawQuestDef = db.getEntity("QuestDef", questDefId);
		
		if(rawQuestDef == null) 
			throw new UserErrorMessage("This quest doesn't exist.");
		
		QuestDefEntity questDef = new QuestDefEntity(db, db.getEntity("QuestDef", questDefId));
		
		QuestEntity quest = questDef.getQuestEntity(character.getKey());
		
		if(quest == null)
			throw new UserErrorMessage("You haven't started this quest yet.");
		
		if(quest.isComplete())
			throw new UserErrorMessage("This quest is already complete.");
		
		if(GameUtils.equals(character.getProperty("pinnedQuest"), quest.getKey()))
			character.setProperty("pinnedQuest", null);
		
		else character.setProperty("pinnedQuest", quest.getKey());
		
		db.sendGameMessage("You've pinned " + questDef.getName());

		ds.put(character);
		
		getMPUS().updateFullPage_shortcut();
	}

}
