package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.ConfirmAttackException;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.dbentities.GuardSetting;
import com.universeprojects.miniup.server.dbentities.GuardSetting.GuardType;
import com.universeprojects.miniup.server.longoperations.GameStateChangeException;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.GuardService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class CommandGuardNewSetting extends Command
{

	public CommandGuardNewSetting(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		boolean attack = WebUtils.getBoolParam(request, "attack", true);
		GuardType type = GuardType.valueOf(parameters.get("type"));
		Key entityKey = db.stringToKey(parameters.get("entityKey"));
		
		GuardService gService = new GuardService(db);
		
		try
		{
			CachedEntity guard = gService.tryToGuardLocation(db.getCurrentCharacter(), db.getCharacterLocation(db.getCurrentCharacter()).getKey(), type, attack);
			if (guard!=null)
			{
				CombatService combatService = new CombatService(db);
				combatService.enterCombat(db.getCurrentCharacter(), guard, true, true);
				
				MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, db.getCurrentUser(), db.getCurrentCharacter(), db.getCharacterLocation(db.getCurrentCharacter()), this);
				mpus.updateFullPage_shortcut();
				return;
			}

			GuardSetting gs = gService.newGuardService(db.getCurrentCharacterKey(), (Key)db.getCurrentCharacter().getProperty("locationKey"), entityKey, type);
			ds.put(gs.getRawEntity());
			
			db.sendGameMessage("You are now "+gs.getFullLine(db.getEntity(entityKey)).toLowerCase()+".");
			
			setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
		}
		catch(ConfirmAttackException e)
		{
			throw new UserErrorMessage(e.getMessage()+"<br><br><a onclick='closeAllPopups();newGuardSetting(event, \""+parameters.get("entityKey")+"\", \""+parameters.get("type")+"\", true);'>Click here</a> to proceed to attack those who are preventing you from guarding this area.");
		}
		
	}

}
