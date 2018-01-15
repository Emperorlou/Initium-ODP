package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandConstructItemSkillRename extends Command
{
	final public String nameRegex = "[A-Za-z0-9.'\\-#\\(\\) ]+";
	public CommandConstructItemSkillRename(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		String newName = parameters.get("name");
		if (newName==null || newName.matches(nameRegex)==false)
			throw new UserErrorMessage("The new name must have only letters, numbers, spaces, or any of the following characters: . ' #");
		newName = newName.replace("  ", " ").trim();
		
		Long skillId = tryParseId(parameters, "skillId");
		Key skillKey = KeyFactory.createKey("ConstructItemSkill", skillId);
		CachedEntity skill = db.getEntity(skillKey);
		
		if (skill==null) 
			throw new UserErrorMessage("The skill you're trying to rename doesn't exist.");
		
		if (CommonChecks.checkIdeaIsCharacters(skill, db.getCurrentCharacterKey())==false)
			throw new UserErrorMessage("This skill is not your skill to rename.");

		// Check to see if this skill name is used anywhere else already
		QueryHelper query = new QueryHelper(db.getDB());
		if (query.getFilteredList_Count("ConstructItemSkill", "name", FilterOperator.EQUAL, newName)>0)
			throw new UserErrorMessage("Someone has already named a skill by this name.");
		
		skill.setProperty("name", newName);
		db.getDB().put(skill);
		
		updateHtmlContents("#skill-name-"+skillId, newName);
		updateHtmlContents("#skill-popup-title-name-"+skillId, newName);
	}

}
