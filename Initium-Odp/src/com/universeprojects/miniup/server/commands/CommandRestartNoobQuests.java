package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.QuestService;

public class CommandRestartNoobQuests extends Command
{
	public CommandRestartNoobQuests(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		
		QuestService questService = getQuestService();
		db.getDB().put(questService.createQuestInstance(QuestService.equipYourselfQuestKey).getRawEntity());

		QueryHelper q = new QueryHelper(db.getDB());
		List<Key> keys = q.getFilteredList_Keys("QuestDef", "noobQuest", true);
		
		List<Key> questsToDelete = new ArrayList<>();
		for(Key key:keys)
		{
			if (GameUtils.equals(key, QuestService.equipYourselfQuestKey)) continue;
			
			Key questKey = KeyFactory.createKey("Quest", db.getCurrentCharacterKey().toString()+key.toString());
			questsToDelete.add(questKey);
		}
		
		db.getDB().delete(questsToDelete);
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
		
		getQuestService().clearCache();
		getMPUS().updateQuestPanel();
	}

}
