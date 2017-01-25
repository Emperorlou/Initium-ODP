package com.universeprojects.miniup.server.domain;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * A monster spawner belongs to a location. It defines the odds of a player finding a monster, and links to a NPCDef for creating the monster in the game world.
 * 
 * @author kyle-miller
 *
 */
public class MonsterSpawner extends OdpDomain {
	public static final String KIND = "MonsterSpawner";

	public MonsterSpawner() {
		super(new CachedEntity(KIND));
	}

	private MonsterSpawner(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	public static final MonsterSpawner wrap(CachedEntity cachedEntity) {
		return new MonsterSpawner(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 *  The number of monsters that will always spawn during the instance spawning routine.
	 *  
	 * @param instanceMonsterCount
	 */
	public void setInstanceMonsterCount(Long instanceMonsterCount) {
		getCachedEntity().setProperty("instanceMonsterCount", instanceMonsterCount);
	}

	public Long getInstanceMonsterCount() {
		return (Long) getCachedEntity().getProperty("instanceMonsterCount");
	}

	/**
	 *  (Location|type==Permanent)
	 *  
	 * @param locationKey
	 */
	public void setLocationKey(Key locationKey) {
		getCachedEntity().setProperty("locationKey", locationKey);
	}

	public Key getLocationKey() {
		return (Key) getCachedEntity().getProperty("locationKey");
	}

	/**
	 *  Just some way for the editor to distinguish between spawners. Could just be the npc name.
	 *  
	 * @param name
	 */
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	/**
	 *  If this is set to true, the monster that spawns here will be spawned directly into the location and not in a combat site that branches from the location.
	 *  
	 * @param noCombatSite
	 */
	public void setNoCombatSite(Boolean noCombatSite) {
		getCachedEntity().setProperty("noCombatSite", noCombatSite);
	}

	public Boolean getNoCombatSite() {
		return (Boolean) getCachedEntity().getProperty("noCombatSite");
	}

	/**
	 *  (NPCDef)
	 *  
	 * @param npcDefKey
	 */
	public void setNpcDefKey(Key npcDefKey) {
		getCachedEntity().setProperty("npcDefKey", npcDefKey);
	}

	public Key getNpcDefKey() {
		return (Key) getCachedEntity().getProperty("npcDefKey");
	}

	/**
	 *  (0.0)
	 *  
	 * @param spawnChance
	 */
	public void setSpawnChance(Double spawnChance) {
		getCachedEntity().setProperty("spawnChance", spawnChance);
	}

	public Double getSpawnChance() {
		return (Double) getCachedEntity().getProperty("spawnChance");
	}

	/**
	 *  This String defines how many monsters spawn as a result of this MonsterSpawner.
	 *  
	 * @param spawnCount
	 */
	public void setSpawnCount(String spawnCount) {
		getCachedEntity().setProperty("spawnCount", spawnCount);
	}

	public String getSpawnCount() {
		return (String) getCachedEntity().getProperty("spawnCount");
	}

	/**
	 * 
	 * @param instanceModeEnabled
	 */
	public void setInstanceModeEnabled(Boolean instanceModeEnabled) {
		getCachedEntity().setProperty("instanceModeEnabled", instanceModeEnabled);
	}

	public Boolean getInstanceModeEnabled() {
		return (Boolean) getCachedEntity().getProperty("instanceModeEnabled");
	}

	public enum InstanceMonsterStatus {
		Normal, Defending1, Defending2, Defending3,
	}

	/**
	 * 
	 * @param instanceMonsterStatus
	 */
	public void setInstanceMonsterStatus(InstanceMonsterStatus instanceMonsterStatus) {
		getCachedEntity().setProperty("instanceMonsterStatus", instanceMonsterStatus);
	}

	public InstanceMonsterStatus getInstanceMonsterStatus() {
		return (InstanceMonsterStatus) getCachedEntity().getProperty("instanceMonsterStatus");
	}

}
