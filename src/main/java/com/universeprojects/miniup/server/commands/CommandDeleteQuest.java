package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
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

public class CommandDeleteQuest extends Command{

	public CommandDeleteQuest(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {		
		QuestService qService = db.getQuestService(this);
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		String questDefKeyString = request.getParameter("questId");
		Key questDefKey = KeyFactory.stringToKey(questDefKeyString);
		QuestDefEntity questDef = new QuestDefEntity(db, db.getDB().getIfExists(questDefKey));
		
		QuestEntity quest = questDef.getQuestEntity(character.getKey());
		
		if(quest.isComplete())
			throw new UserErrorMessage("This quest is already complete.");

		if (quest.getCharacterKey().equals(character.getKey()) == false)
			throw new UserErrorMessage("This isn't this character's quest.. maybe you need to refresh?");
		
		if(GameUtils.equals(character.getProperty("pinnedQuest"), quest.getKey()))
			character.setProperty("pinnedQuest", null);
		
		qService.deleteQuest(quest.getKey());
		
		db.sendGameMessage("Permanently stopped the "+questDef.getName()+" quest for you. If you want to start a questline, click on the big white ! button in the button bar.");

		ds.put(character);
		
		getMPUS().updateFullPage_shortcut();

		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}
