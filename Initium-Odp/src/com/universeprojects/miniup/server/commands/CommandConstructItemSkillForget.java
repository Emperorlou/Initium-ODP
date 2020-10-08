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

public class CommandConstructItemSkillForget extends Command
{

	public CommandConstructItemSkillForget(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		Long skillId = tryParseId(parameters, "skillId");
		Key skillKey = KeyFactory.createKey("ConstructItemSkill", skillId);
		CachedEntity skill = db.getEntity(skillKey);
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		if (skill==null) return;
		
		if (CommonChecks.checkIdeaIsCharacters(skill, db.getCurrentCharacterKey())==false)
			throw new UserErrorMessage("This skill is not your skill to forget.");
		
		if (skill.getProperty("item")!=null)
			db.getDB().delete((Key)skill.getProperty("item"), skill.getKey());
		else
			db.getDB().delete(skill.getKey());

		deleteHtml("#skill-id-"+skillKey.getId());
	}

}
