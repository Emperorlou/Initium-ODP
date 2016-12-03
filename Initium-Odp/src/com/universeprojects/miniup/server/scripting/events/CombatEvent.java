package com.universeprojects.miniup.server.scripting.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.ScriptType;
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
	
	public CombatEvent(ODPDBAccess db, CachedEntity character, CachedEntity attacker, CachedEntity weapon, CachedEntity defender)
	{
		this(new Character(character, db), new Character(attacker, db), new Item(weapon, db), new Character(defender, db));
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
	
	public void setContext(ODPDBAccess db, CachedEntity attacker, CachedEntity weapon, CachedEntity defender)
	{
		this.attacker = new Character(attacker, db);
		this.weapon = new Item(weapon, db);
		this.defender = new Character(defender, db);
		this.damage.clear();
		this.reset();
	}
	
	@Override
	public String eventKind() {
		return "Combat";
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
	
	@SuppressWarnings("unchecked")
	public static Map<ScriptType, HashMap<CachedEntity, List<CachedEntity>>> 
		getCombatScripts(ODPDBAccess db, CachedEntity attacker, CachedEntity weapon, CachedEntity defender)
	{
		// This will be the return.
		Map<ScriptType, HashMap<CachedEntity, List<CachedEntity>>> scriptMap = 
				new HashMap<ScriptType, HashMap<CachedEntity, List<CachedEntity>>>();

		// This map tracks the actual entities which generated which scripts
		Map<CachedEntity, List<Key>> entityScripts = new HashMap<CachedEntity, List<Key>>();
		// This will be used to retrieve all the scripts from the DB
		HashSet<Key> getKeys = new HashSet<Key>();
		for(CachedEntity curEntity:Arrays.asList(attacker, weapon, defender))
		{
			// A null entity could be possible, such as fighting bare handed (weapon == null)
			if(curEntity == null) continue;
			List<Key> entityKeys = (List<Key>)curEntity.getProperty("scripts");
			if(entityKeys == null) entityKeys = new ArrayList<Key>();
			entityScripts.put(curEntity, entityKeys);
			getKeys.addAll(entityKeys);
		}
		
		// Only allow these specific types of scripts to be processed.
		List<CachedEntity> scripts = db.getScriptsOfType(new ArrayList<Key>(getKeys), 
				ScriptType.onAttack, ScriptType.onAttackHit, ScriptType.onDefend, ScriptType.onDefendHit);
		for(CachedEntity script:scripts)
		{
			ScriptType sType = ScriptType.valueOf((String)script.getProperty("type"));
			
			// If allowed event type already exists in the script map, get it.
			// Otherwise, create a new one, but don't add it yet. We only want
			// a record in this map if we actually have a (correct) script to execute.
			HashMap<CachedEntity, List<CachedEntity>> currentScriptMap = 
				scriptMap.containsKey(sType) ? 
					scriptMap.get(sType) : 
					new HashMap<CachedEntity, List<CachedEntity>>();
			
			// On the other hand, here we WANT to add the list of entities to this particular script
			// Since a script can be associated to multiple entities, allow it to go through here.
			List<CachedEntity> scriptSources = 
				currentScriptMap.containsKey(script) ? 
					currentScriptMap.get(script) :
					currentScriptMap.put(script, new ArrayList<CachedEntity>());
					
			switch(sType)
			{
				case onAttack:
				case onAttackHit:
					if(attacker != null && entityScripts.get(attacker).contains(script.getKey()))
						scriptSources.add(attacker);
					if(weapon != null && entityScripts.get(weapon).contains(script.getKey()))
						scriptSources.add(weapon);
				case onDefend:
				case onDefendHit:
					if(defender != null && entityScripts.get(defender).contains(script.getKey()))
						scriptSources.add(defender);
				default:
					break;
			}
			// Only adding to the script map if we actually have a script from the correct entity.
			// Put will replace in the map, which is fine since it's the same instance we originally
			// got from the map and is a constant time operation
			if(!scriptSources.isEmpty()) scriptMap.put(sType, currentScriptMap);
		}
		return scriptMap;
	}
}
