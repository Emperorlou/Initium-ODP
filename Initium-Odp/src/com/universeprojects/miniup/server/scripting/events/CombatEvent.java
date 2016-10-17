package com.universeprojects.miniup.server.scripting.events;

import java.util.HashMap;
import java.util.Map;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;
import com.universeprojects.miniup.server.scripting.wrappers.Character;
import com.universeprojects.miniup.server.scripting.wrappers.Item;

/**
 * Represents the Combat scripting event. This will handle all the combat specific
 * scripts, and will handle onAttacking, onAttackingHit, onDefending, and onDefendingHit
 * script types, fired from the Combat command.
 * Note that weapon will be the weapon used to initiate the attack, but can (will) likely
 * be different depending on the triggering item (triggering item is the weapon/equipment).
 *   
 * @author spfiredrake
 */
public class CombatEvent extends ScriptEvent {
	public Character attacker;
	public Character defender; 
	public Item weapon;
	public Map<String, Long> damage = new HashMap<String, Long>();
	
	/**
	 * Initializes the combat event with the character as both the event source and attacking entity.
	 * @param db ODPDBAccess instance, needed to initialize entity wrappers
	 * @param character Current character, also used as the attacking entity
	 * @param weapon Weapon currently being used to attack.
	 * @param defender Defending character.
	 */
	public CombatEvent(ODPDBAccess db, CachedEntity character, CachedEntity weapon, CachedEntity defender)
	{
		this(new Character(character, db), new Item(weapon, db), new Character(defender, db));
	}
	
	/**
	 * Initializes the combat event with the wrapped entities. Uses character as the attacking entity.
	 * @param character Current character, also used as the attacking entity
	 * @param weapon Weapon currently being used to attack.
	 * @param defender Defending character.
	 */
	public CombatEvent(EntityWrapper character, EntityWrapper weapon, EntityWrapper combatant) {
		this((Character)character, (Character)character, (Item)weapon, (Character)combatant);
	}
	
	/**
	 * Initializes the combat event with the wrapped entities.
	 * @param currentCharacter Current character, also used as the attacking entity
	 * @param attacker Attacking entity.
	 * @param weapon Weapon currently being used to attack.
	 * @param defender Defending entity.
	 */
	public CombatEvent(Character currentCharacter, Character attacker, Item weapon, Character defender)
	{
		super(currentCharacter);
		this.attacker = attacker;
		this.weapon = weapon;
		this.defender = defender;
	}
	
	/**
	 * Initializes an "empty" combat event. Must call setTargets before utilizing in script context.
	 * @param character Source entity for the event (entity that triggered the action/event)
	 * @param db ODPDBAccess instance, needed to initialize entity wrappers
	 */
	public CombatEvent(CachedEntity character, ODPDBAccess db)
	{
		super(character, db);
	}
	
	/**
	 * Initializes an "empty" combat event. Must call setTargets before utilizing in script context.
	 * @param character Source entity for the event (entity that triggered the action/event)
	 */
	public CombatEvent(EntityWrapper character)
	{
		super(character);
	}
	
	@Override
	public String eventKind() {
		return "Combat";
	}
	
	public void setContext(Character attacker, Item weapon, Character defender){
		this.attacker = attacker;
		this.weapon = weapon;
		this.defender = defender;
	}
	
	public Long addDamage(String damageType, Long damageDealt)
	{
		if(!damage.containsKey(damageType))
			damage.put(damageType, 0L);
		
		Long newDamage = damage.get(damageType) + damageDealt;
		damage.put(damageType, newDamage);
		return newDamage;
	}
	
	public Long getDamage(String damageType)
	{
		if(!damage.containsKey(damageType))
			return 0L;
		
		return damage.get(damageType);
	}
	
	public Long getTotalDamage()
	{
		Long totDamage = 0L;
		for(Long dmg:damage.values())
			totDamage += dmg;
		return totDamage;
	}
}
