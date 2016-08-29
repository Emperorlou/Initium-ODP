package com.universeprojects.miniup.server.services;

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
}
