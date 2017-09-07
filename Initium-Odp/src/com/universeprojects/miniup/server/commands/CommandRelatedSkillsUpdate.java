package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ODPInventionService;

public class CommandRelatedSkillsUpdate extends Command
{

	public CommandRelatedSkillsUpdate(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		String itemKeyStr = parameters.get("itemKey");
		CachedEntity item = ds.getIfExists(KeyFactory.stringToKey(itemKeyStr));
		if (item.getKind().equals("Item")==false) return;
		
		ODPInventionService inventionService = db.getInventionService(db.getCurrentCharacter(), null);
		EntityPool pool = new EntityPool(ds);
		List<CachedEntity> skills = inventionService.getRelatedSkills(pool, item);
		
		StringBuilder html = new StringBuilder();

		html.append("<div>");
		html.append("<h4>Related skills</h4>");
		if (skills==null || skills.isEmpty())
		{
			html.append("You do not have any skills relating to this item at the moment.");
		}
		else
		{
			for(CachedEntity skill:skills)
			{
				html.append("<p>");
				String skillName = WebUtils.jsSafe((String)skill.getProperty("name"));
				html.append("<a onclick='doConstructItemSkill(event, "+skill.getId()+", \""+skillName+"\");'>");
				html.append("<img src='"+GameUtils.getResourceUrl(skill.getProperty("icon"))+"'/> ");
				html.append(skillName+"</a>");
				html.append("</p>");
			}
		}
		html.append("</div>");
		
		updateHtmlContents("#related-skills", html.toString());		
		
	}

}
