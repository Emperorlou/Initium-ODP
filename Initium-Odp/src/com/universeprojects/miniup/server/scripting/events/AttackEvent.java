package com.universeprojects.miniup.server.scripting.events;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

/**
 * unimplemented
 * @author Evan
 *
 */
public class AttackEvent extends CombatEvent{

	public AttackEvent(ODPDBAccess db, CachedEntity character, CachedEntity weapon, CachedEntity defender) {
		super(db, character, weapon, defender);
		// TODO Auto-generated constructor stub
	}
}
