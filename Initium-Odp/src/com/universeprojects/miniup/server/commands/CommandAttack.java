package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;


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

	public CommandAttack(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
	{
		super(db, request, response);
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
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
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
		
		// DB sanity check
		GameUtils.normalizeDatabaseState_Character(ds, monster, location);
		
		// Check if monster is alive
		if (GameUtils.isPlayerIncapacitated(monster))
			throw new UserErrorMessage("This monster is already dead!");
		
		if("Instance".equals(location.getProperty("combatType"))==false)
		{
			if(CommonChecks.checkCharacterIsRaidBoss(monster)==false && 
					"CombatSite".equals(location.getProperty("type"))==false && 
					monster.getProperty("combatant") != null)
				throw new UserErrorMessage("This monster is already in combat.");
			
			// All future checks are instance only, so exit if not in one
			// instanceModeEnabled doesn't catch the hybrid setup, so test manually 
			Key defenceStructure = (Key)location.getProperty("defenceStructure");
			if (defenceStructure==null)
				return;
		}
		else
		{
			// Instance only: Check if monster is in combat
			if ("COMBAT".equals(monster.getProperty("mode")))
				throw new UserErrorMessage("This monster is already in combat.");
			
			// Instance only: Check for party
			String partyCode = (String)character.getProperty("partyCode");
			if (partyCode!=null && partyCode.trim().equals("")==false)
				throw new UserErrorMessage("You're not able to party up here.");
		}
		
		
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		ds.beginTransaction();
		try {
			// Verify parameter sanity
			CachedEntity monster = db.getCharacterById(tryParseId(parameters, "charId"));
			if (monster==null)
				throw new RuntimeException("Attack invalid call format, 'charId' is not a valid id.");
			
			// Check if monster can be attacked
			CachedEntity character = db.getCurrentCharacter();
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
		
		MainPageUpdateService update = MainPageUpdateService.getInstance(db, db.getCurrentUser(), db.getCurrentCharacter(), db.getCharacterLocation(db.getCurrentCharacter()), this);
		update.updateFullPage_shortcut(false);
	}

}
