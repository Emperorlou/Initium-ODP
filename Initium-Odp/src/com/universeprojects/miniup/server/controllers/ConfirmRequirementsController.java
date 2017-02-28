package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.web.PageController;

public class ConfirmRequirementsController extends PageController
{
	public enum Type
	{
		IdeaToPrototype
	}
	
	public ConfirmRequirementsController()
	{
		super("confirmrequirements");
	}

	@Override
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Type type = determineType(request);
		
		ODPDBAccess db = ODPDBAccess.getInstance(request);

		if (type == Type.IdeaToPrototype)
		{
			processForIdea(request, db);
		}
		
		
	    return "/WEB-INF/odppages/ajax_confirmrequirements.jsp";
	}


	private void processForIdea(HttpServletRequest request, ODPDBAccess db)
	{
		CachedEntity character = db.getCurrentCharacter(); 
		CachedDatastoreService ds = db.getDB();
		EntityPool pool = new EntityPool(ds);
		Long ideaId = Long.parseLong(request.getParameter("ideaId"));
		CachedEntity idea = db.getEntity(KeyFactory.createKey("ConstructItemIdea", ideaId));
		
		// Make sure the idea we're processing is actually owned by the character who is executing it
		if (GameUtils.equals(idea.getProperty("characterKey"), character.getKey())==false)
			throw new IllegalArgumentException("Possible hack attempt. An ideaId from a different character was used.");
		
		
	}
	
	
	
	
	private List<Key> rawKeysToKeys(String[] rawKeys)
	{
		List<Key> result = new ArrayList<Key>();
		for(String rawKey:rawKeys)
		{
			if (rawKey.endsWith("\")"))
			{
				// Keys using names
				String[] parts = rawKey.split("\\(\"");
				if (parts.length!=2)
					throw new IllegalArgumentException("Key was malformed: "+rawKey);
				
				String kind = parts[0];
				String name = parts[1].substring(0, parts[1].length()-2);
				
				result.add(KeyFactory.createKey(kind, name));
			}
			else
			{
				// Keys using IDs
				String[] parts = rawKey.split("\\(");
				if (parts.length!=2)
					throw new IllegalArgumentException("Key was malformed: "+rawKey);
				
				String kind = parts[0];
				String idStr = parts[1].substring(0, parts[1].length()-1);
				Long id = Long.parseLong(idStr);
				
				result.add(KeyFactory.createKey(kind, id));
			}
		}
		return result;
	}
	
	private Type determineType(HttpServletRequest request)
	{
		if (request.getParameterMap().containsKey("ideaId"))
			return Type.IdeaToPrototype;
		
		throw new IllegalArgumentException("Unable to determine type for confirm requirements page.");
	}
}

