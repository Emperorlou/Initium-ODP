package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * Unequips all items currently equipped to character by setting equipment slots null.
 * 
 * @author SPFiredrake
 *
 */
public class CommandCharacterUnequipAll extends Command {

	public CommandCharacterUnequipAll(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		// TODO Auto-generated method stub
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		List<Key> itemKeys = new ArrayList<Key>();
		for(String slot:ODPDBAccess.EQUIPMENT_SLOTS)
		{
			Key item = (Key)character.getProperty("equipment"+slot);
			if(item != null && itemKeys.contains(item)==false)
				itemKeys.add(item);
			
			character.setProperty("equipment" + slot, null);
		}
		ds.put(character);
		
		MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), character, null, this);
		mpus.updateInBannerCharacterWidget();
		
		if(itemKeys.isEmpty()==false)
		{
			StringBuilder sb = new StringBuilder();
			List<CachedEntity> items = db.getEntities(itemKeys);
			for(CachedEntity item:items)
			{
				sb.append(GameUtils.renderInventoryItem(db, item, character, false));
			}
			prependChildHtml("#invItems", sb.toString());
		}
	}

}
