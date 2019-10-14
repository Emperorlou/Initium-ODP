package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.dbentities.QuestDefEntity;
import com.universeprojects.miniup.server.dbentities.QuestEntity;
import com.universeprojects.miniup.server.services.QuestService;

public class CommandBeginNoobQuests extends Command
{

	public CommandBeginNoobQuests(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		Long questDefId = Long.parseLong(parameters.get("questDefId"));
		
		Key questDefKey = KeyFactory.createKey("QuestDef", questDefId);
		
		CachedEntity questDefEntity = db.getEntity(questDefKey);
		if (questDefEntity==null) throw new UserErrorMessage("Sorry! This quest is no longer available. It's possible it was removed from the game.");
		QuestDefEntity questDef = new QuestDefEntity(db, questDefEntity);

		if (questDef.isNoobQuest()==false)
			throw new UserErrorMessage("Sorry! This quest is not a noob quest. You cannot force start it this way.");
		
		String questLine = questDef.getQuestLine();

		// Delete all the previous quests in this quest line now...
		QueryHelper q = new QueryHelper(db.getDB());
		List<Key> keys = q.getFilteredList_Keys("QuestDef", "noobQuest", true, "questLine", questLine);
		
		List<Key> questsToDelete = new ArrayList<>();
		for(Key key:keys)
		{
			if (GameUtils.equals(key, questDefKey)) continue;
			
			Key questKey = KeyFactory.createKey("Quest", db.getCurrentCharacterKey().toString()+key.toString());
			questsToDelete.add(questKey);
		}
		
		db.getDB().delete(questsToDelete);
		
		
		QuestService questService = getQuestService();
		QuestEntity quest = questService.createQuestInstance(questDefKey);

		addCallbackData("questDefKey", questDef.getUrlSafeKey());
		
		db.sendGameMessage(ds, db.getCurrentCharacter(), "You now have the '<a onclick='viewQuest(\""+questDef.getUrlSafeKey()+"\")'>"+questDef.getName()+"</a>' quest. You can see it in your <a onclick='viewQuests()'>quest log</a>.");
		
		ds.put(quest.getRawEntity());
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
		
		
	}

}
