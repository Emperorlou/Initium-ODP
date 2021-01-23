package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPAuthenticator;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.GuardService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;
import com.universeprojects.miniup.server.services.OperationJSService;

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
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		CachedEntity user = db.getCurrentUser();
		CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));

		CombatService cs = new CombatService(db);
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, user, character, location, this);
		GuardService gs = new GuardService(db);

		String status = null;
		StringBuilder summaryStatus = null;
		CachedEntity targetCharacter = null;
		String characterName = (String)character.getProperty("name");
		String targetCharacterName = null;
		boolean targetKilled = false;
		
		ds.beginBulkWriteMode();
		try
		{
			if (GameUtils.isPlayerIncapacitated(character))
			{
				mpus.updateFullPage_shortcut();
				setPopupMessage("You are incapacitated and cannot attack.");
				return;
			}
			
			if(cs.isInCombat(character) == false)
			{
				mpus.updateFullPage_shortcut();
				setPopupMessage("You are not currently in combat and cannot attack!");
				return;
			}
			
			
			targetCharacter = db.getCharacterCombatant(character);
			if (targetCharacter==null || GameUtils.isPlayerIncapacitated(targetCharacter))
			{
				cs.leaveCombat(character, null);
				ds.put(character);
				mpus.updateFullPage_shortcut();
				return;
			}
			
			targetCharacterName = (String)targetCharacter.getProperty("name");
			CachedEntity targetLocation = db.getEntity((Key)targetCharacter.getProperty("locationKey"));
			// Raid boss could possibly be in instance, so check for it
			// explicitly even though isInCombatWith handles non-instance
			// already.
			if (cs.isInCombatWith(character, targetCharacter, targetLocation)==false && 
					CommonChecks.checkLocationIsInstance(targetLocation) &&
					CommonChecks.checkCharacterIsRaidBoss(targetCharacter)==false)
			{
				cs.leaveCombat(character, null);
				ds.put(character);
				mpus.updateFullPage_shortcut();
				setPopupMessage("You're not in combat with this opponent, someone else is. This can happen if someone else entered combat around the same time as you.");
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
			
			// Collect the status of each member of the fight before the attack takes place..
			summaryStatus = new StringBuilder();
			
			boolean characterMissed = false;
			boolean targetEquipmentDestroyed = false;
			boolean characterCrit = false;
			Double targetHp = (Double)targetCharacter.getProperty("hitpoints");
			
			status = db.doCharacterAttemptAttack(auth, user, character, weapon, targetCharacter);
	
			if (status!=null) characterCrit = status.contains("It's a critical hit!");
			if (status!=null) targetEquipmentDestroyed = status.contains("equipment-destroyed-notice");
			Double targetNewHp = (Double)targetCharacter.getProperty("hitpoints");
			
			db.flagNotALooter(request);
			
			if (status==null)
			{
				status = characterName+"'s attack missed!";
				characterMissed = true;
			}
			
			if (characterMissed)
			{
				summaryStatus.append(characterName).append("'s attack missed. ");
				
				doVisualEffect(hand, true, false, false);
			}
			else
			{
				String hitType = "hit";
				if (characterCrit)
					hitType = "CRITICAL HIT";
				
				if (targetHp.equals(targetNewHp))
				{
					summaryStatus.append(characterName).append(" ").append(hitType).append(", but no damage was done. ");
					
					doVisualEffect(hand, false, true, false);
				}
				else
				{
					summaryStatus.append(characterName).append(" ").append(hitType).append("! ").append(GameUtils.formatNumber(targetHp-targetNewHp)).append(" damage was done. ");
	
					if (characterCrit)
						doVisualEffect(hand, false, false, true);
					else
						doVisualEffect(hand, false, false, false);
						
				}
				
				if (targetEquipmentDestroyed)
					summaryStatus.append("<span class='equipment-destroyed-notice'>Equipment was destroyed. </span>");
				
				if (CommonChecks.checkCharacterIsUnconscious(targetCharacter))
					summaryStatus.append("<span style='color:#00bd00'>"+characterName+" wins, "+targetCharacterName+" is unconscious. </span>");
				else if (CommonChecks.checkCharacterIsDead(targetCharacter))
					summaryStatus.append("<span style='color:#00bd00'>"+characterName+" wins, "+targetCharacterName+" is dead. </span>");
			}
			
			if (GameUtils.isPlayerIncapacitated(targetCharacter)==false)
			{
				if (GuardService.checkIfGuardWantsToRun(targetCharacter))
				{
					// Guard is going to try to run
					
					boolean success = db.doCharacterAttemptEscape(location, targetCharacter, character);
					db.flagNotALooter(request);

					status+="<br>";
					status+="<strong>"+targetCharacterName+" attempts to run away...</strong>";
					status+="<br>";

					if(success)
					{
						summaryStatus.append(targetCharacterName+" managed to escape! ");
						status += targetCharacterName+" managed to escape!";
					}
					else
					{
						summaryStatus.append(targetCharacterName+" tried to escape and failed! ");
						status += targetCharacterName+" failed to escape!";
					}

					gs.doRunAndStopGuarding(targetCharacter, location.getKey());
					
					ds.putIfChanged(character, targetCharacter);
					
					
					
				}
				else
				{
				
					boolean targetMissed = false;
					boolean characterEquipmentDestroyed = false;
					boolean targetCrit = false;
					Double characterHp = (Double)character.getProperty("hitpoints");
					// Now do the counter attack
					String counterAttackStatus = db.doMonsterCounterAttack(auth, user, targetCharacter, character);
			
					status+="<br>";
					status+="<strong>The "+targetCharacterName+" counter attacks...</strong>";
					
					if (counterAttackStatus==null)
					{
						status+="The "+targetCharacterName+" missed!";
						targetMissed = true;
					}
					else 
					{
						status+=counterAttackStatus;
					}
					
					ds.putIfChanged(character, targetCharacter);
					
					if (((Double)targetCharacter.getProperty("hitpoints"))>0)
					{
						if (counterAttackStatus!=null) characterCrit = counterAttackStatus.contains("It's a critical hit!");
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
								summaryStatus.append(targetCharacterName).append(" ").append(hitType).append(", but no damage was done. ");
							else
								summaryStatus.append(targetCharacterName).append(" ").append(hitType).append("! ").append(GameUtils.formatNumber(characterHp-characterNewHp)).append(" damage was done. ");
							
							if (characterEquipmentDestroyed)
								summaryStatus.append("<span class='equipment-destroyed-notice'>Equipment was destroyed. </span>");
							
							if (CommonChecks.checkCharacterIsUnconscious(character))
								summaryStatus.append(characterName).append(" is unconscious. ");
							else if (CommonChecks.checkCharacterIsDead(character))
								summaryStatus.append(characterName).append(" is dead. ");
						}
					}
				}
			
				// Now increase experience with the weapon used
				if (weapon!=null)
				{
					if (db.increaseKnowledgeForEquipment100(weapon)==true)
					{
						String weaponClass = (String)weapon.getProperty("itemClass");
						summaryStatus.append(characterName).append("'s experience with the "+weaponClass+" has increased.");
					}
				}
			}
			else if (CommonChecks.checkCharacterIsDead(targetCharacter))
			{
				// If we're here then the target died, we'll create a token in that case (if applicable)

				if (CommonChecks.checkLocationIsInstance(targetLocation)==false)
				{
					CachedEntity defeatedTokenDef = db.getEntity("ItemDef", 6239650208546816L);
					if (defeatedTokenDef!=null)
					{
						CachedEntity defeatedToken = db.generateNewObject(defeatedTokenDef, "Item");
						defeatedToken.setProperty("containerKey", location.getKey());
						defeatedToken.setProperty("name", "Defeated: "+targetCharacter.getProperty("name").toString().substring(5));
						String description = (String)defeatedToken.getProperty("description");
						defeatedToken.setProperty("description", description+"<br><br>Slain by: "+characterName);
						ds.put(defeatedToken);
						
						targetKilled = true;
					}
				}
			}
		
		

		
		
		
			
			if (CommonChecks.checkCharacterIsInCombat(character)==false)
			{
				mpus = MainPageUpdateService.getInstance(db, db.getCurrentUser(), db.getCurrentCharacter(), location, this);
				mpus.updateFullPage_shortcut();

				if (CommonChecks.checkCharacterIsPlayer(targetCharacter))
					db.queueMainPageUpdateForCharacter(targetCharacter.getKey(), "updateFullPage_shortcut");
				
				List<CachedEntity> party = db.getParty(ds, character);
				
				if(party != null)
					for(CachedEntity ce : db.getParty(ds, character))
						db.queueMainPageUpdateForCharacter(ce.getKey(), "updateFullPage_shortcut");
				
			}
			else
			{
				// We're not done with combat
				mpus.updateInBannerCharacterWidget();
				mpus.updateInBannerCombatantWidget(targetCharacter);
				mpus.updateButtonList();
				mpus.updatePartyView();
				
				if (CommonChecks.checkCharacterIsPlayer(targetCharacter))
					db.queueMainPageUpdateForCharacter(targetCharacter.getKey(), "updateInBannerCombatantWidget", "updateInBannerCharacterWidget");
			}

			db.commitInventionEntities();
		}
		finally
		{
			
			ds.commitBulkWrite();
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
			
			// Also send a message to the opponent if they are a player...
			if (CommonChecks.checkCharacterIsPlayer(targetCharacter))
			{
				String opponentHtml = html;
				
				db.sendGameMessage(db.getDB(), targetCharacter, opponentHtml);
			}
		}
		
		if (targetKilled)
		{
			db.getGridMapService().regenerateTile(this, 500, 500);
		}
		
		
		getQuestService().checkCharacterPropertiesForObjectiveCompletions();
	}

	
	private void doVisualEffect(String hand, boolean miss, boolean fullyBlocked, boolean crit)
	{
		boolean flipX = false;
		if (hand.contains("Left")) flipX = true;
		
		String effect = null;
		if (miss) 
			effect = "weaponeffects1-b-miss.gif";
		else if (fullyBlocked)
			effect = "weaponeffects1-b-blocked.gif";
		else if (crit)
		{
			effect = "weaponeffects1-c.gif";
			flipX = !flipX;
		}
		else
			effect = "weaponeffects1-b.gif";
		
		OperationJSService js = new OperationJSService(this);
		js.playBannerFx("images/effects/"+effect, flipX, false);
	}
	
}
