package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.web.PageController;

public class ConfirmRequirementsController extends PageController
{

	public ConfirmRequirementsController()
	{
		super("confirmrequirements");
	}

	@Override
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		CachedDatastoreService ds = db.getDB();
		CachedEntity character = db.getCurrentCharacter(); 
		
//		InventionService invention = new InventionService(db, character);

		// Get the list of EntityRequirements we were given and work from those
		// (Maybe include the entity that owns the EntityRequirement for backreference [like the ConstructionToolRequirement])
		String[] entityRequirementKeysRaw = request.getParameterValues("entityRequirements");
		String[] entityRequirementOwnerKeysRaw = request.getParameterValues("entityRequirementOwners");
		
		List<Key> entityRequirementKeys = rawKeysToKeys(entityRequirementKeysRaw);
		List<Key> entityRequirementOwnerKeys = rawKeysToKeys(entityRequirementOwnerKeysRaw);
		
		List<CachedEntity> entityRequirements = new ArrayList<CachedEntity>();
		List<CachedEntity> entityRequirementOwners = new ArrayList<CachedEntity>();
		List<List<CachedEntity>> candidatesForRequirement = new ArrayList<List<CachedEntity>>();
		
		@SuppressWarnings("unchecked")
		List<CachedEntity> batchEntities = ds.get(entityRequirementKeys, entityRequirementOwnerKeys);
		for(int i = 0; i<entityRequirementKeys.size(); i++)
			entityRequirements.add(batchEntities.get(i));
		for(int i = 0; i<entityRequirementOwnerKeys.size(); i++)
			entityRequirementOwners.add(batchEntities.get(i+entityRequirementKeys.size()));
			
		
		Set<CachedEntity> itemsAlreadyAdded = new HashSet<CachedEntity>();
		// Now we have all the entity requirements for the thing we're working on, we're going to look for 
		// candidates that are available to the character for the thing in question
		for(CachedEntity entityRequirement:entityRequirements)
		{
//			List<CachedEntity> candidatesFor = invention.getItemCandidatesFor(entityRequirement, itemsAlreadyAdded);
//			if (candidatesFor.size()>0)
//				itemsAlreadyAdded.add(candidatesFor.get(0));
		}
	    
		
		
		
	    return "/WEB-INF/odppages/ajax_confirmrequirements.jsp";
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
}

