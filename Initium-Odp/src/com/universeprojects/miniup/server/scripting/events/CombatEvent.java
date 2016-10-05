package com.universeprojects.miniup.server.scripting.events;

import javax.transaction.NotSupportedException;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;
import com.universeprojects.miniup.server.scripting.wrappers.Character;
import com.universeprojects.miniup.server.scripting.wrappers.Item;

/**
 * Represents the Combat scripting event. This will handle all the combat specific
 * scripts, and will handle onAttacking, onAttackingHit, onDefending, and onDefendingHit
 * script types, fired from the Combat command.
 * Note that weapon will be the weapon used to initiate the attack, but the script that
 * fires the event could possibly be the off-hand weapon from a critical hit.
 *   
 * @author spfiredrake
 */
public class CombatEvent extends ScriptEvent {
	public Character enemyCombatant; 
	public Item weaponUsed;
	public Long damage = 0L;
	
	public CombatEvent(ODPDBAccess db, CachedEntity character, CachedEntity weapon, CachedEntity combatant)
	{
		this(new Character(character, db), new Item(weapon, db), new Character(combatant, db));
	}
	
	public CombatEvent(EntityWrapper character, EntityWrapper weapon, EntityWrapper combatant) {
		this((Character)character, (Item)weapon, (Character)combatant);
	}
	
	public CombatEvent(Character character, Item weapon, Character combatant)
	{
		super(character);
		weaponUsed = weapon;
		enemyCombatant = combatant;
	}
	
	public CombatEvent(CachedEntity character, ODPDBAccess db) throws NotSupportedException
	{
		super(character, db);
		throw new NotSupportedException("Constructor type not supported.");
	}
	
	public CombatEvent(EntityWrapper character) throws NotSupportedException
	{
		super(character);
		throw new NotSupportedException("Constructor type not supported.");
	}
	
	@Override
	public String eventKind() {
		return "Combat";
	}
}
