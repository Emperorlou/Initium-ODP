package com.universeprojects.miniup.server.services;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.NotificationType;
import com.universeprojects.miniup.server.ODPDBAccess;

public class CombatService extends Service
{
	public CombatService(ODPDBAccess db)
	{
		super(db);
	}
	
	public void enterCombat(CachedEntity attacker, CachedEntity defender, boolean autoAttack)
	{
		attacker.setProperty("combatant", defender.getKey());
		attacker.setProperty("mode", "COMBAT");
		
		defender.setProperty("combatant", attacker.getKey());
		defender.setProperty("mode", "COMBAT");
		
		attacker.setProperty("combatType", null);
		defender.setProperty("combatType", null);
		
		CachedDatastoreService ds = db.getDB();

		if (autoAttack)
		{
			attacker.setProperty("combatType", "DefenceStructureAttack");
			db.flagCharacterCombatAction(db.getDB(), attacker);
		}
		
		
		ds.put(attacker);
		ds.put(defender);
		
		db.sendNotification(ds, defender.getKey(), NotificationType.fullpageRefresh);
	}
	
	/**
	 * Removes all items on the current character for which the strength requirement
	 * is no longer met (i.e. a buff is applied or expires).
	 */
	public void updateEquips(){
		CachedEntity character = db.getCurrentCharacter();
		CachedDatastoreService ds = db.getDB();
		for(String slot:ODPDBAccess.EQUIPMENT_SLOTS)
		{
			Key itemKey = (Key)character.getProperty("equipment"+slot);
			CachedEntity item = db.getEntity(itemKey);
			Double characterStrength = db.getCharacterStrength(character);
			Double strengthRequirement;
			if (item!=null)
			{
				strengthRequirement = (Double)item.getProperty("strengthRequirement");
				if (strengthRequirement!=null && characterStrength<strengthRequirement){
					db.doCharacterUnequipEntity(ds, character, item);
				}
			}
		}
	}
}