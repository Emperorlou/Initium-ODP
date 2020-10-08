package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.dbentities.QuestDefEntity;
import com.universeprojects.miniup.server.dbentities.QuestEntity;
import com.universeprojects.miniup.server.services.QuestService;

public class CommandBeginQuest extends Command
{

	public CommandBeginQuest(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		Long itemId = Long.parseLong(parameters.get("itemId"));
		CachedEntity item = db.getEntity("Item", itemId);
		
		Key questDefKey = (Key)item.getProperty("newQuest");
		if (questDefKey==null) throw new UserErrorMessage("There is no quest associated with this item.");
		
		CachedEntity questDefEntity = db.getEntity(questDefKey);
		if (questDefEntity==null) throw new UserErrorMessage("Sorry! This quest is no longer available. It's possible it was removed from the game.");
		QuestDefEntity questDef = new QuestDefEntity(db, questDefEntity);

		if (CommonChecks.checkItemIsAccessible(item, db.getCurrentCharacter())==false)
			throw new UserErrorMessage("You do not have access to this item and so you cannot start a quest from it. Make sure it is in your inventory or on the ground in your location.");
		
		QuestService questService = getQuestService();
		QuestEntity quest = questService.createQuestInstance(questDefKey);

		addCallbackData("questDefKey", questDef.getUrlSafeKey());
		
		db.sendGameMessage(ds, character, "You now have the '<a onclick='viewQuest(\""+questDef.getUrlSafeKey()+"\")'>"+questDef.getName()+"</a>' quest. You can see it in your <a onclick='viewQuests()'>quest log</a>.");
		
		ds.put(quest.getRawEntity());
	}

}
