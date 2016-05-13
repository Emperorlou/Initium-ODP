package com.universeprojects.miniup.server.commands;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;


/**
 * Allows the player to attack a mob at the current location
 * 
 * Usage notes:
 * Checks if caller can attack and initiates combat if so
 * 
 * Parameters:
 * 		charId - characterId of monster to attack 
 * 
 * Support methods:
 * 		canAttack - returns boolean whether attack is allowed
 * 
 * @author NJ
 *
 */
public class CommandAttack extends Command {

	public CommandAttack(HttpServletRequest request, HttpServletResponse response) 
	{
		super(request, response);
	}

	public static boolean canAttack(CachedDatastoreService ds, CachedEntity location, CachedEntity character, CachedEntity monster)
	{
		try {
			canAttackHelper(ds, location, character, monster);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private static void canAttackHelper(CachedDatastoreService ds, CachedEntity location, CachedEntity character, CachedEntity monster) throws UserErrorMessage
	{
		if (ds==null || location==null || character==null || monster==null)
			throw new RuntimeException("canAttackHelper invalid parameter.");
		
		// Check that monster is an actual monster
		if ("NPC".equals(monster.getProperty("type"))==false)
			throw new UserErrorMessage("You can only attack monsters this way.");
		
		// Check that they're both at the same location
		if (GameUtils.equals(location.getKey(), character.getProperty("locationKey"))==false || GameUtils.equals(location.getKey(), monster.getProperty("locationKey"))==false)
			throw new UserErrorMessage("You can only attack monsters in the immediate vicinity.");
		
		// Check that character status is set to Normal (or blank)
		String mode = (String)character.getProperty("mode");
		if (mode!=null && mode.equals("NORMAL")==false)
			throw new UserErrorMessage("You're too busy to attack anything at the moment.");
		
		// Additional incapacitated check just in case mode wasn't set properly.
		if (GameUtils.isPlayerIncapacitated(character))
			throw new UserErrorMessage("You're incapacitated at the moment.");
		
		// Check if monster is alive
		Double hitpoints = (Double)monster.getProperty("hitpoints");
		mode = (String)monster.getProperty("mode");
		if (hitpoints==null || hitpoints<=0 || "DEAD".equals(mode))
		{
			// DB sanity check. Mode should be DEAD
			if ("DEAD".equals(mode)==false)
			{
				monster.setProperty("mode", "DEAD");
				monster.setProperty("combatType", null);
				monster.setProperty("combatant", null);
				String name = (String)monster.getProperty("name");
				if (name.startsWith("Dead ")==false)
					monster.setProperty("name", "Dead "+name);
				ds.put(monster);
				if ("TRUE".equals(location.getProperty("instanceModeEnabled")) && location.getProperty("instanceRespawnDate")==null)
				{
					Date instanceRespawnDate = (Date)location.getProperty("instanceRespawnDate");
					Long instanceRespawnDelay = (Long)location.getProperty("instanceRespawnDelay");
					if (instanceRespawnDate==null && instanceRespawnDelay!=null)
					{
						GregorianCalendar cal = new GregorianCalendar();
						cal.add(Calendar.MINUTE, instanceRespawnDelay.intValue());
						location.setProperty("instanceRespawnDate", cal.getTime());
						ds.put(location);
					}
				}
			}
			throw new UserErrorMessage("This monster is already dead!");
		}
		
		// All future checks are instance only, so exit if not in one
		// instanceModeEnabled doesn't catch the hybrid setup, so test manually 
		Key defenceStructure = (Key)location.getProperty("defenceStructure");
		if ("Instance".equals(location.getProperty("combatType"))==false && (defenceStructure==null || defenceStructure.equals("")))
			return;

		// Instance only: Check if monster is in combat, note that DEAD mode was checked beforehand
		if (mode!=null && mode.equals("NORMAL")==false)
		{
			// DB sanity check. Monster mode should only be null, DEAD, NORMAL or COMBAT
			if (mode.equals("COMBAT"))
			{
				// DB sanity check. Combatant should be alive and in combat with monster (location can legit be different)
				CachedEntity combatant = null;
				Key combatantKey = (Key)monster.getProperty("combatant");
				if (combatantKey!=null && combatantKey.equals("")==false)
				{
					try {
						combatant = ds.get(combatantKey);
					} catch (EntityNotFoundException e) {
						// Ignore
					}
				}
				if (combatant!=null && GameUtils.isPlayerIncapacitated(combatant)==false && "COMBAT".equals(combatant.getProperty("mode")) && GameUtils.equals(monster.getKey(), combatant.getProperty("combatant")))
					throw new UserErrorMessage("This monster is already in combat.");
			}
			monster.setProperty("mode", "NORMAL");
			monster.setProperty("combatType", null);
			monster.setProperty("combatant", null);
			ds.put(monster);
		}
		
		// Instance only: Check for party
		String partyCode = (String)character.getProperty("partyCode");
		if (partyCode!=null && partyCode.equals("")==false)
			throw new UserErrorMessage("You're not able to party up here.");
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();

		// Verify parameter sanity
		Long charId;
		try {
			charId = Long.parseLong(parameters.get("charId"));
		} catch (Exception e) {
			throw new RuntimeException("Attack invalid call format, 'charId' is not a valid id.");
		}
		
		CachedDatastoreService ds = getDS();
		ds.beginTransaction();
		try {
			CachedEntity monster = db.getCharacterById(charId);
			if (monster==null)
				throw new RuntimeException("Attack invalid call format, 'charId' is not a valid id.");
			
			// Check if monster can be attacked
			CachedEntity character = db.getCurrentCharacter(request);
			CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
			canAttackHelper(ds, location, character, monster);

			character.setProperty("mode", "COMBAT");
			monster.setProperty("mode", "COMBAT");
			// instanceModeEnabled doesn't catch the hybrid setup, so test manually 
			Key defenceStructure = (Key)location.getProperty("defenceStructure");
			if ("Instance".equals(location.getProperty("combatType"))==false && (defenceStructure==null || defenceStructure.equals("")))
			{
				// Is set to blank on random monster encounter, so emulated that behaviour
				character.setProperty("combatType", null);
				monster.setProperty("combatType", null);
			}
			else
			{
				character.setProperty("combatType", "DefenceStructureAttack");
				monster.setProperty("combatType", "DefenceStructureAttack");
			}
			character.setProperty("combatant", monster.getKey());
			monster.setProperty("combatant", character.getKey());
			ds.put(character);
			ds.put(monster);
			ds.commit();
		} catch (UserErrorMessage uem) {
			// commit any DB sanity changes canAttackHelper may have made
			ds.commit();
			throw uem;
		} finally {
			ds.rollbackIfActive();
		}
		setJavascriptResponse(JavascriptResponse.FullPageRefresh);
		
	}

}
