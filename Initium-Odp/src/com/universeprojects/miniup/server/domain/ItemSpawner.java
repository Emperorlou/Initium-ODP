package com.universeprojects.miniup.server.domain;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * Item spawners are used to define which items should spawn on an NPC when it is created. It includes spawn odds as well. The NPC has a special field that specifies exactly how many of any type of item needs to spawn at a minimum.
 * 
 * @author kyle-miller
 *
 */
public class ItemSpawner extends OdpDomain {
	public static final String KIND = "ItemSpawner";

	public ItemSpawner() {
		super(new CachedEntity(KIND));
	}

	public ItemSpawner(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 *  (ItemDef)
	 *  
	 * @param itemDefKey
	 */
	public void setItemDefKey(Key itemDefKey) {
		getCachedEntity().setProperty("itemDefKey", itemDefKey);
	}

	public Key getItemDefKey() {
		return (Key) getCachedEntity().getProperty("itemDefKey");
	}

	/**
	 *  Just a name for the spawner so we can identify it in the editor.
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
	 *  (100.0)
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
	 * 
	 * @param doNotEquip
	 */
	public void setDoNotEquip(Boolean doNotEquip) {
		getCachedEntity().setProperty("doNotEquip", doNotEquip);
	}

	public Boolean getDoNotEquip() {
		return (Boolean) getCachedEntity().getProperty("doNotEquip");
	}

	public enum Type {
		Weapon, Armor, Ammo, Other, newEntity,
	}

	/**
	 * 
	 * @param type
	 */
	public void setType(Type type) {
		getCachedEntity().setProperty("type", type);
	}

	public Type getType() {
		return (Type) getCachedEntity().getProperty("type");
	}

}
