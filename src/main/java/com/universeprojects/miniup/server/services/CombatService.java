package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;

public class CombatService extends Service
{
	public CombatService(ODPDBAccess db)
	{
		super(db);
	}

	public void leaveCombat(CachedEntity attacker, CachedEntity defender)
	{
		CachedDatastoreService ds = db.getDB();
		attacker.setProperty("combatant", null);
		attacker.setProperty("mode", "NORMAL");
		attacker.setProperty("combatType", null);
		ds.put(attacker);
		
		if (defender!=null && GameUtils.equals(defender.getProperty("combatant"), attacker.getKey()))
		{
			attacker.setProperty("combatant", null);
			attacker.setProperty("mode", "NORMAL");
			attacker.setProperty("combatType", null);
			ds.put(defender);
		}
		
	}

	public void enterCombat(CachedEntity attacker, CachedEntity defender, boolean autoAttack)
	{
		enterCombat(attacker, defender, autoAttack, false);
	}
	
	
	public void enterCombat(CachedEntity attacker, CachedEntity defender, boolean autoAttack, boolean isDefenderGuarding)
	{
		if (GameUtils.equals(attacker.getKey(), defender.getKey())) throw new RuntimeException("You cannot attack yourself.");
		
		attacker.setProperty("combatant", defender.getKey());
		attacker.setProperty("mode", "COMBAT");
		
		defender.setProperty("mode", "COMBAT");
		if(CommonChecks.checkCharacterIsRaidBoss(defender)==false)
			defender.setProperty("combatant", attacker.getKey());
		else
			autoAttack = true;
		
		attacker.setProperty("combatType", null);
		defender.setProperty("combatType", null);
		
		if (isDefenderGuarding)
		{
			defender.setProperty("guardStartHP", defender.getProperty("hitpoints"));
		}
		
		CachedDatastoreService ds = db.getDB();

		if (autoAttack)
		{
			defender.setProperty("combatType", "AutoDefender");
			attacker.setProperty("combatType", "DefenceStructureAttack");
			db.flagCharacterCombatAction(db.getDB(), attacker);
		}
		
		ds.put(attacker);
		ds.put(defender);
		
		db.sendMainPageUpdateForCharacter(ds, defender.getKey(), "updateFullPage_shortcut");
	}

	public boolean isInCombat(CachedEntity character)
	{
		return CommonChecks.checkCharacterIsInCombat(character);
	}

	public boolean isInCombatWith(CachedEntity character, CachedEntity opponent, CachedEntity location)
	{
		if (CommonChecks.checkCharacterIsInCombat(character)==false) return false;
		Key characterCombatant = (Key)character.getProperty("combatant");
		Key opponentCombatant = (Key)opponent.getProperty("combatant");
		
		if (location != null && CommonChecks.checkLocationIsInstance(location) && CommonChecks.checkCharacterIsRaidBoss(opponent)==false)
		{
			if (GameUtils.equals(characterCombatant, opponent.getKey()) && GameUtils.equals(opponentCombatant, character.getKey()) && 
					CommonChecks.checkCharacterIsIncapacitated(character)==false && CommonChecks.checkCharacterIsIncapacitated(opponent)==false)
				return true;
		}
		else
		{
			return GameUtils.equals(characterCombatant, opponent.getKey());
		}

		return false;
	}
	
	public boolean isInDefenseStructure(CachedEntity character, CachedEntity location)
	{
		Key defenceStructure = (Key)location.getProperty("defenceStructure");
		if (defenceStructure==null && "Instance".equals(location.getProperty("combatType"))==false)
			return false;
		return true;
	}	

	/**
	 * Removes all items from the specified character for which the strength requirement
	 * is no longer met (i.e. a buff is applied or expires).
	 * TODO: THIS IS UNTESTED.
	 */
	public void updateEquips(CachedEntity character) {
		List<Key> itemKeys = new ArrayList<Key>();
		CachedDatastoreService ds = db.getDB();
		for (String slot : ODPDBAccess.EQUIPMENT_SLOTS) {
			Key itemKey = (Key) character.getProperty("equipment" + slot);
			if (itemKey != null) {
				itemKeys.add(itemKey);
			}
		}
		List<CachedEntity> itemEntities = db.getEntities(itemKeys);
		Double characterStrength = db.getCharacterStrength(character);
		Double strengthRequirement;
		for (CachedEntity item : itemEntities){
			if (item != null) {
				strengthRequirement = (Double) item.getProperty("strengthRequirement");
				if (strengthRequirement!=null && characterStrength<strengthRequirement){
					db.doCharacterUnequipEntity(ds, character, item);
				}
			}
		}
	}
}
