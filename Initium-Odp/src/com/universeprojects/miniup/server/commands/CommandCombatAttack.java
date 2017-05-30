package com.universeprojects.miniup.server.commands;

import java.util.Map;
import java.util.Random;

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
		if (cs.isInCombatWith(character, targetCharacter, location)==false)
		{
			cs.leaveCombat(character, null);
			mpus.shortcut_fullPageUpdate(cs);			
			throw new UserErrorMessage("You're not in combat with this opponent, someone else is. This can happen if someone else entered combat around the same time as you.");
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
		
		// Collect the status of each member of the fight before the attack takes place..
		StringBuilder summaryStatus = new StringBuilder();
		
		boolean characterMissed = false;
		boolean targetEquipmentDestroyed = false;
		boolean characterCrit = false;
		Double targetHp = (Double)targetCharacter.getProperty("hitpoints");
		
		String status = db.doCharacterAttemptAttack(auth, user, character, weapon, targetCharacter);

		if (status!=null) characterCrit = status.contains("CRITICAL");
		if (status!=null) targetEquipmentDestroyed = status.contains("equipment-destroyed-notice");
		Double targetNewHp = (Double)targetCharacter.getProperty("hitpoints");
		
		db.flagNotALooter(request);
		
		if (status==null)
		{
			status = "Your attack missed!";
			characterMissed = true;
		}
		
		if (characterMissed)
		{
			summaryStatus.append("Your attack missed. ");
		}
		else
		{
			String hitType = "hit";
			if (characterCrit)
				hitType = "CRITICAL HIT";
			
			if (targetHp.equals(targetNewHp))
				summaryStatus.append("You ").append(hitType).append(", but no damage was done. ");
			else
				summaryStatus.append("You ").append(hitType).append("! ").append(GameUtils.formatNumber(targetHp-targetNewHp)).append(" damage was done. ");
			
			if (targetEquipmentDestroyed)
				summaryStatus.append("<span class='equipment-destroyed-notice'>Equipment was destroyed. </span>");
			
			if (CommonChecks.checkCharacterIsUnconscious(targetCharacter))
				summaryStatus.append("<span style='color:#00bd00'>You win, your opponent is unconscious. </span>");
			else if (CommonChecks.checkCharacterIsDead(targetCharacter))
				summaryStatus.append("<span style='color:#00bd00'>You win, your opponent is dead. </span>");
		}
		
		if (GameUtils.isPlayerIncapacitated(targetCharacter)==false)
		{
			boolean targetMissed = false;
			boolean characterEquipmentDestroyed = false;
			boolean targetCrit = false;
			Double characterHp = (Double)character.getProperty("hitpoints");
			// Now do the counter attack
			String counterAttackStatus = db.doMonsterCounterAttack(auth, user, targetCharacter, character);
	
			status+="<br>";
			status+="<strong>The "+targetCharacter.getProperty("name")+" counter attacks...</strong>";
			
			if (counterAttackStatus==null)
			{
				status+="The "+targetCharacter.getProperty("name")+" missed!";
				targetMissed = true;
			}
			else 
			{
				status+=counterAttackStatus;
			}
			
			ds.putIfChanged(character, targetCharacter);
			
			if (((Double)targetCharacter.getProperty("hitpoints"))>0)
			{
				if (counterAttackStatus!=null) characterCrit = counterAttackStatus.contains("CRITICAL");
				if (counterAttackStatus!=null) targetEquipmentDestroyed = counterAttackStatus.contains("equipment-destroyed-notice");
				Double characterNewHp = (Double)character.getProperty("hitpoints");
				
				if (targetMissed)
				{
					summaryStatus.append("Their attack missed. ");
				}
				else
				{
					String hitType = "hit";
					if (targetCrit)
						hitType = "CRITICAL HIT";
					
					if (characterHp.equals(characterNewHp))
						summaryStatus.append("They ").append(hitType).append(", but no damage was done. ");
					else
						summaryStatus.append("They ").append(hitType).append("! ").append(GameUtils.formatNumber(characterHp-characterNewHp)).append(" damage was done. ");
					
					if (characterEquipmentDestroyed)
						summaryStatus.append("<span class='equipment-destroyed-notice'>Equipment was destroyed. </span>");
					
					if (CommonChecks.checkCharacterIsUnconscious(character))
						summaryStatus.append("You are unconscious. ");
					else if (CommonChecks.checkCharacterIsDead(character))
						summaryStatus.append("You are dead. ");
				}
			}
		}
		
		
		if(status != null && status.isEmpty() == false)
		{
			// We want to make a summary combat message and add the full details by a click
			Random rnd = new Random();
			Long randomId = rnd.nextLong();
			
			String html = "";
			html+="<div class='hiddenTooltip' id='hitDetails-"+randomId+"'>"+status+"</div>";
			html+=summaryStatus.toString()+" <span class='hint' rel='#hitDetails-"+randomId+"' style='color:#FFFFFF'>[More..]</span>";
			db.sendGameMessage(db.getDB(), character, html);
		}
		
		
		if (GameUtils.isPlayerIncapacitated(character) || GameUtils.isPlayerIncapacitated(targetCharacter))
		{
			if (CommonChecks.checkLocationIsCombatSite(location)) location = ds.refetch(location);
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
