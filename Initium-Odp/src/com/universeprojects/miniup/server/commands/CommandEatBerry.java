package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.PropertyContainer;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class CommandEatBerry extends Command {
	
	public CommandEatBerry(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		CachedEntity character = db.getCurrentCharacter();
		Long itemId = tryParseId(parameters,"itemId");		
		CachedEntity item = db.getEntity("Item",itemId);
		
		if (item==null)
			throw new UserErrorMessage("Item doesn't exist.");
		
		if (GameUtils.equals(item.getProperty("containerKey"),character.getKey())==false)
			throw new UserErrorMessage("You cannot consume this item. It must be in your inventory!");

		List<EmbeddedEntity> buffs = db.getBuffsFor(character);
		boolean appliedBuff = false;
		if ("Old Candy".equals(item.getProperty("name"))==true){
			int candyCount = 0;
			for (PropertyContainer buff:buffs){
				if("Treat!".equals(buff.getProperty("name")))
					candyCount ++;
				if("Trick!".equals(buff.getProperty("name")))
					candyCount ++;
				if("Sick".equals(buff.getProperty("name")))
					throw new UserErrorMessage("You've eaten way too much candy!");
			}
			
			if (candyCount>=10)
			{
				for (PropertyContainer buff:buffs){
					if("Treat!".equals(buff.getProperty("name")) ||
						"Trick!".equals(buff.getProperty("name")))
					{
						db.markBuffToDelete(buff);
						appliedBuff |= db.awardBuffByName(ds, character, "Sick");
					}
				}
			}
			else
			{
				appliedBuff = db.awardBuff_Candy(ds, character);
			}
			
			if(appliedBuff==false)
				throw new UserErrorMessage("The thought of eating any more candy makes you feel sick...");
		}
		else if("Strange Elixir".equals(item.getProperty("name"))==true){
			appliedBuff = db.awardBuffByName(ds, character, "Strange Elixir"); 
			if(appliedBuff==false)
				throw new UserErrorMessage("Only one elixir buff can be active at a time");
		}
		else if ("Mysterious Berry".equals(item.getProperty("name"))==true){
			appliedBuff = db.awardBuffByName(ds,character,"Mysterious Berry");
			if(appliedBuff==false)
				throw new UserErrorMessage("Only one berry buff can be active at a time.");
		}
		else throw new UserErrorMessage("Why would you even try to eat that?");
		
		if(appliedBuff)
		{
			Long quantity = (Long)item.getProperty("quantity");
			if(quantity==null || quantity <= 1L)
				ds.delete(item);
			else
			{
				item.setProperty("quantity", quantity-1);
				ds.put(item);
			}
			ds.put(character);
		}
		
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, null, character, null, this);
		mpus.updateInBannerCharacterWidget();
	}
}
	
	
