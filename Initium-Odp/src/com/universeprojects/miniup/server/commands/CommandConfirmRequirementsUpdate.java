package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ODPInventionService;

public class CommandConfirmRequirementsUpdate extends Command
{

	public CommandConfirmRequirementsUpdate(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage
	{
		List<Key> gerKeyList = new ArrayList<Key>();
		String gerKeyListRaw = parameters.get("gerKeyList");
		for(String gerKeyString:gerKeyListRaw.split(","))
			gerKeyList.add(KeyFactory.stringToKey(gerKeyString));
		
		List<CachedEntity> gers = db.getDB().get(gerKeyList);
//		Long genericEntityRequirementId = tryParseId(parameters, "id");
//		CachedEntity ger = db.getEntity(KeyFactory.createKey("GenericEntityRequirement", genericEntityRequirementId));
//		if (ger==null) throw new RuntimeException("Unable to find the entity requirement by ID: "+genericEntityRequirementId);
		
		ODPInventionService invention = db.getInventionService(db.getCurrentCharacter(), null);
		
		EntityPool pool = new EntityPool(db.getDB());
		
		Set<CachedEntity> items = invention.getItemCandidatesFor(pool, gers); 

		StringBuilder html = new StringBuilder();
		html.append("<div>For ").append(gers.get(0).getProperty("name")).append("...</div>");
		html.append("<div class='list'>");
		for(CachedEntity item:items)
		{
			html.append("<div onclick='selectItem(event, ").append(item.getId()).append(")' class='itemToSelect confirm-requirements-entry confirm-requirements-item-candidate ").append("deletable-Item").append(item.getId()).append("'><div class='selectarea'><div onclick='unselectItem(event)' class='X'>X</div></div>").append(GameUtils.renderItem(db, db.getCurrentCharacter(), item)).append("</div>");
		}
		html.append("</div>");
		if (items.isEmpty())
			html.append("<p>You do not have anything in your inventory or where you're standing for this.</p>");
		updateHtmlContents("#item-candidates", html.toString());
	}

}
