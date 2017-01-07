package com.universeprojects.miniup.server.domain;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * A collectable spawner belongs to a location. It defines the odds of a player finding a collectable, and links to a CollectableDef for creating the collectable in the game world.
 * 
 * @author kyle-miller
 *
 */
public class CollectableSpawner extends OdpDomain {
	public static final String KIND = "CollectableSpawner";

	public CollectableSpawner() {
		super(new CachedEntity(KIND));
	}

	public CollectableSpawner(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 *  (CollectableDef)
	 *  
	 * @param collectableDefKey
	 */
	public void setCollectableDefKey(Key collectableDefKey) {
		getCachedEntity().setProperty("collectableDefKey", collectableDefKey);
	}

	public Key getCollectableDefKey() {
		return (Key) getCachedEntity().getProperty("collectableDefKey");
	}

	/**
	 *  (Location)
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
	 *  Just some way for the editor to distinguish between spawners. Could just be the collectable item name.
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

}
