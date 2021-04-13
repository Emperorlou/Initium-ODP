package com.universeprojects.miniup.server.scripting.events;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class DefendHitEvent extends CombatEvent{

	public DefendHitEvent(ODPDBAccess db, CachedEntity character, CachedEntity weapon, CachedEntity defender) {
		super(db, character, weapon, defender);
		// TODO Auto-generated constructor stub
	}

}
