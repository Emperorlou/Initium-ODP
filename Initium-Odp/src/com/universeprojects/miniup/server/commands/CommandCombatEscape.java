package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPAuthenticator;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class CommandCombatEscape extends Command {

	public CommandCombatEscape(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity user = db.getCurrentUser();
		CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
		CombatService cs = new CombatService(db);
		MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), db.getCurrentCharacter(), location, this);
		
		CachedEntity targetCharacter = db.getCharacterCombatant(character);
		if (targetCharacter==null)
		{
			cs.leaveCombat(character, null);
			mpus.updateFullPage_shortcut();
			return;
		}
		
		if (cs.isInCombatWith(character, targetCharacter, location)==false)
		{
			cs.leaveCombat(character, null);
			mpus.updateFullPage_shortcut();			
			throw new UserErrorMessage("You're not in combat with this opponent, someone else is. This can happen if someone else entered combat around the same time as you.");
		}
		
		
		// Throws UserErrorMessage. This is a valid situation, as it means the character is attempting 
		// to do something not allowed (running as non-party leader, escaping while defending, etc).
		boolean success = db.doCharacterAttemptEscape(location, character, targetCharacter);
		db.flagNotALooter(request);
		
		String userMessage = "";
		if(success)
		{
			userMessage = "You managed to escape!";
		}
		else
		{
			ODPAuthenticator auth = new ODPAuthenticator();
			String counterAttackStatus = db.doMonsterCounterAttack(auth, user, targetCharacter, character);
			
			if (((Double)targetCharacter.getProperty("hitpoints"))>0)
            {
                userMessage+="<br>";
                userMessage+="<strong>The "+targetCharacter.getProperty("name")+" attacks you as you're fleeing...</strong><br>";

                if (counterAttackStatus==null)
                {
                    userMessage+="The "+targetCharacter.getProperty("name")+" missed!";
                }
                else 
                {
                    userMessage+=counterAttackStatus;
                }
            }
		}
		
		character = ds.refetch(character);
		location = db.getEntity((Key)character.getProperty("locationKey"));
		mpus = new MainPageUpdateService(db, user, character, location, this);
		
		if(userMessage != null && userMessage.isEmpty() == false)
			db.sendGameMessage(db.getDB(), character, userMessage);
		
		if (cs.isInCombat(character)==false || GameUtils.isPlayerIncapacitated(character))
		{
			// We're done with combat
			mpus.updateFullPage_shortcut();
			return;
		}
		else
		{
			// We're not done with combat
			mpus.updateInBannerCharacterWidget();
			mpus.updateInBannerCombatantWidget(targetCharacter);
			mpus.updateButtonList();
		}
	}

}
