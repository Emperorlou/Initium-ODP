package com.universeprojects.miniup.server.commands;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ConfirmGenericEntityRequirementsBuilder;
import com.universeprojects.miniup.server.services.ODPInventionService;

public class CommandFireplaceAddFuel extends Command
{

	public CommandFireplaceAddFuel(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		CachedEntity fireplaceFuel = db.getEntity("GenericActivity", "FireplaceFuel");
		
		Map<String, Key> gerSlotsToItem = new ConfirmGenericEntityRequirementsBuilder("1", db, this, fireplaceFuel)
		.addGenericEntityRequirements("Material", "genericEntityRequirements1")
		.addGenericEntityRequirements("Material", "genericEntityRequirements1")
		.addGenericEntityRequirements("Material", "genericEntityRequirements1")
		.addGenericEntityRequirements("Material", "genericEntityRequirements1")
		.go();
		
		Collection<Key> fuel = gerSlotsToItem.values();
		
		EntityPool pool = new EntityPool(db.getDB());
		ODPInventionService inventionService = db.getInventionService(db.getCurrentCharacter(), null);
		
		inventionService.resolveGerSlotsToGers(pool, fireplaceFuel, gerSlotsToItem);
		
//		inventionService.checkIdeaWithSelectedItems(pool, idea, itemRequirementsToItems)
	}

}
