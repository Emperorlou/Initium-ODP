package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPAuthenticator;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class CommandCombatAttack extends Command 
{
	public CommandCombatAttack(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) 
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		ODPAuthenticator auth = getAuthenticator();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity user = db.getCurrentUser();
		CachedEntity location = ds.getIfExists((Key)character.getProperty("locationKey"));

		CombatService cs = new CombatService(db);
		MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), db.getCurrentCharacter(), location, this);
		
		if (GameUtils.isPlayerIncapacitated(character))
		{
			mpus.shortcut_fullPageUpdate(cs);
			setPopupMessage("You are incapacitated and cannot attack.");
			return;
		}
		
		if(cs.isInCombat(character) == false)
			throw new UserErrorMessage("You are not currently in combat and cannot attack!");
		
		CachedEntity targetCharacter = db.getCharacterCombatant(character);
		if (targetCharacter==null)
		{
			cs.leaveCombat(character, null);
			mpus.shortcut_fullPageUpdate(cs);
			return;
		}
		
		if ("NPC".equals(targetCharacter.getProperty("type"))==false && db.isCharacterDefending(location, character))
			throw new UserErrorMessage("You cannot trigger your own attack while defending.");
		
		String hand = request.getParameter("hand");
		if(CommonChecks.checkIsValidEquipSlot(hand) == false)
			throw new RuntimeException("Invalid slot specified!");
		
		Key weaponKey = (Key)character.getProperty("equipment"+hand);
		CachedEntity weapon = null;
		if (weaponKey!=null)
		{
			weapon = db.getEntity(weaponKey);
			if(weapon != null && GameUtils.isContainedInList("LeftHand,RightHand",hand) == false)
				throw new UserErrorMessage("You realize the futility in attempting to attack with " + weapon.getProperty("name") + ", and decide against it...");
		}
		
		String status = db.doCharacterAttemptAttack(auth, user, character, weapon, targetCharacter);

		db.flagNotALooter(request);
		
		if (status==null)
			status = "Your attack missed!";
		
		// Now do the counter attack
		String counterAttackStatus = db.doMonsterCounterAttack(auth, user, targetCharacter, character);
		if (((Double)targetCharacter.getProperty("hitpoints"))>0)
		{
			status+="<br>";
			status+="<strong>The "+targetCharacter.getProperty("name")+" counter attacks...</strong>";
			
			if (counterAttackStatus==null)
			{
				status+="The "+targetCharacter.getProperty("name")+" missed!";
			}
			else 
			{
				status+=counterAttackStatus;
			}
		}
		if(status != null && status.isEmpty() == false)
			db.sendGameMessage(db.getDB(), character, status);
		
		
		if (GameUtils.isPlayerIncapacitated(character) || GameUtils.isPlayerIncapacitated(targetCharacter))
		{
			// We're done with combat
			cs = new CombatService(db);
			mpus = new MainPageUpdateService(db, db.getCurrentUser(), db.getCurrentCharacter(), location, this);
			mpus.shortcut_fullPageUpdate(cs);
			return;
		}
		else
		{
			// We're not done with combat
			mpus.updateInBannerCharacterWidget();
			mpus.updateInBannerCombatantWidget(targetCharacter);
			mpus.updateButtonList(cs);
		}
		
		
	}
	
}
