package com.universeprojects.miniup.server.scripting.events;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class AttackHitEvent extends CombatEvent{
	public String status; //this will get appended to the hidden text of an attack.
	public Long damage; //this will get added to the damage calculation before damage is applied. Status should reflect this.

	public AttackHitEvent(ODPDBAccess db, CachedEntity character, CachedEntity weapon, CachedEntity defender) {
		super(db, character, weapon, defender);
		// TODO Auto-generated constructor stub
	}

}
