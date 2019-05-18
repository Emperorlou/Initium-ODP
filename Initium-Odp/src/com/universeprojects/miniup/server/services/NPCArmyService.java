package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;

public class NPCArmyService extends Service
{
	final static Logger log = Logger.getLogger(NPCArmyService.class.getName());
	final Key npcArmyKey;
	CachedEntity npcArmy;
	
	
	List<CachedEntity> npcs = null;
	Set<Long> excludedPropagateLocations = new HashSet<Long>();  
	
	
	public NPCArmyService(ODPDBAccess db, CachedEntity npcArmy)
	{
		super(db);
		this.npcArmyKey = npcArmy.getKey();
		this.npcArmy = npcArmy;
		
		if(npcArmy.getProperty("excludedLocations") != null)
		{
			@SuppressWarnings("unchecked")
			List<Key> exclude = (List<Key>)npcArmy.getProperty("excludedLocations");
			for(Key location:exclude)
				excludedPropagateLocations.add(location.getId());
		}
	}
	
	public String getUniqueId()
	{
		return (String)npcArmy.getProperty("uniqueId");
	}

	public void setUniqueId(String uniqueId)
	{
		npcArmy.setProperty("uniqueId", uniqueId);
	}

	public long getPropagationCount()
	{
		if (npcArmy.getProperty("propagationCount")==null) return 0L;
		return (Long)npcArmy.getProperty("propagationCount");
	}

	public void setPropagationCount(long propagationCount)
	{
		npcArmy.setProperty("propagationCount", propagationCount);
	}

	public double getSpawnsPerTick()
	{
		if (npcArmy.getProperty("spawnsPerTick")==null) return 0L;
		return (Double)npcArmy.getProperty("spawnsPerTick");
	}

	public void setSpawnsPerTick(double spawnsPerTick)
	{
		npcArmy.setProperty("spawnsPerTick", spawnsPerTick);
	}

	public boolean isSeed()
	{
		return Boolean.TRUE.equals(npcArmy.getProperty("seed"));
	}

	public void setSeed(Boolean seed)
	{
		npcArmy.setProperty("seed", seed);
	}

	public String getPropagatedMaxSpawnCount()
	{
		return (String)npcArmy.getProperty("propagatedMaxSpawnCount");
	}

	public void setPropagatedMaxSpawnCount(String propagatedMaxSpawnCount)
	{
		npcArmy.setProperty("propagatedMaxSpawnCount", propagatedMaxSpawnCount);
	}

	public Key getLocationKey()
	{
		return (Key)npcArmy.getProperty("locationKey");
	}

	public void setLocationKey(Key locationKey)
	{
		npcArmy.setProperty("locationKey", locationKey);
	}

	public Key getNpcDefKey()
	{
		return (Key)npcArmy.getProperty("npcDefKey");
	}

	public void setNpcDefKey(Key npcDefKey)
	{
		npcArmy.setProperty("npcDefKey", npcDefKey);
	}

	public long getMaxSpawnCount()
	{
		if (npcArmy.getProperty("maxSpawnCount")==null) return 0L;
		return (Long)npcArmy.getProperty("maxSpawnCount");
	}
	
	public Long getMinSpawnsToPropagate()
	{
		Long value = (Long)npcArmy.getProperty("minSpawnsToPropagate");
		if (value==null) 
			value = getMaxSpawnCount();
		
		return value;
	}

	public void setMaxSpawnCount(long maxSpawnCount)
	{
		npcArmy.setProperty("maxSpawnCount", maxSpawnCount);
	}

	public NPCArmyService(ODPDBAccess db, Key npcArmyKey)
	{
		this(db, db.getEntity(npcArmyKey));
	}
	
	public List<Key> getExcludedPropagateLocations()
	{
		@SuppressWarnings("unchecked")
		List<Key> excludedLocations = (List<Key>)npcArmy.getProperty("excludedLocations");
		if(excludedLocations == null) excludedLocations = new ArrayList<Key>();
		return excludedLocations;
	}

	public List<CachedEntity> getNPCs()
	{
		if (npcs!=null)
			return npcs;
		
		npcs = db.getFilteredList("Character", 
				"locationKey", getLocationKey(), 
				"_definitionKey", getNpcDefKey());
		
		List<Key> deadNPCs = new ArrayList<Key>();
		for(int i = npcs.size()-1; i>=0; i--)
		{
			if (npcs.get(i)==null || npcs.get(i).getProperty("hitpoints")==null || (Double)npcs.get(i).getProperty("hitpoints")<1d)
			{
				deadNPCs.add(npcs.get(i).getKey());
				npcs.remove(i);
			}
			else if(npcs.get(i).getProperty("combatant") != null)
				npcs.remove(i);
		}
		
		if(deadNPCs.isEmpty()==false)
			db.getDB().delete(deadNPCs);
		
		return npcs;
	}
	
	/**
	 * This should be called on the server tick, which is called once every 10 minutes.
	 */
	public void doTick()
	{
		// Check if we need to propagate
		attemptToPropagate();

		// Check if there are NO npcs left and seed is NOT set. If so, we'll delete the army as it has been defeated
		if (isSeed()==false && getNPCs().size()==0)
		{
			deleteNPCArmy();
			return;
		}
		
		// Spawn more monsters
		attemptToSpawn();
		
		if (npcArmy.isUnsaved())
			db.getDB().put(npcArmy);
	}
	
	private void deleteNPCArmy()
	{
		db.getDB().delete(npcArmyKey);
	}
	
	/**
	 * This method will attempt to spawn a new NPC at the army's location depending on the "spawns per tick" setting and only up 
	 * to a maximum of "max spawn count" for the location. 	
	 */
	private void attemptToSpawn()
	{
		if (getMaxSpawnCount()<=0L) return;
		
		if (getSpawnsPerTick()<=0d) return;
		
		if (getNPCs().size()>=getMaxSpawnCount()) return;
		
		
		
		if (isSeed())
		{
			CachedEntity npcDef = db.getEntity(getNpcDefKey());
			long spawnCount = Math.round(getSpawnsPerTick());
			if (spawnCount<1) spawnCount = 1;
			for(int i = 0; i<spawnCount; i++)
				db.doCreateMonster(npcDef, getLocationKey());

			// Once we've seeded the spawn, we'll want to turn off the seed field
			if (isSeed())
				setSeed(false);
		}
		else if (getSpawnsPerTick()<1 && GameUtils.roll(getSpawnsPerTick())==true)
		{
			CachedEntity npcDef = db.getEntity(getNpcDefKey());
			db.doCreateMonster(npcDef, getLocationKey());
		}
		else if (getSpawnsPerTick()>=1)
		{
			CachedEntity npcDef = db.getEntity(getNpcDefKey());
			long spawnCount = Math.round(getSpawnsPerTick());
			for(int i = 0; i<spawnCount; i++)
				db.doCreateMonster(npcDef, getLocationKey());
		}
	}
	
	
	/**
	 * This method will attempt to propagate the army to an adjacent location if it is time to do so.
	 * 
	 * Propagation only occurs if we have more "propagationCount" left AND if there are "maxSpawnCount"
	 * NPCs that are ALIVE in the area still.
	 */
	private void attemptToPropagate()
	{
		log.info("Attempting to propagate from "+getLocationKey()+"..");
		
		if (getPropagationCount()<=0) return;
		
		
		// If we are at or over the maxSpawnCount, then we will attempt to propagate
		if (getNPCs().size()>=getMinSpawnsToPropagate())
		{
			// Get all the "permanent" paths that lead away from here
			List<CachedEntity> paths = db.getPathsByLocation_PermanentOnly(getLocationKey());
			
			Collections.shuffle(paths);
			
			// Now get all the location keys that are on the other side of the paths that lead away from here...
			Map<CachedEntity, Key> pathsToLocations = new HashMap<CachedEntity, Key>();
			for(CachedEntity path:paths)
			{
				Key nextLocation = null;
				if (GameUtils.equals(path.getProperty("location1Key"), getLocationKey()))
					nextLocation = (Key)path.getProperty("location2Key");
				else
					nextLocation = (Key)path.getProperty("location1Key");
				
				if(this.excludedPropagateLocations.contains(nextLocation.getId()))
				{
					log.info(nextLocation.toString() + ": spawner excluded location to propagate");
				}
				pathsToLocations.put(path, nextLocation);
			}
			List<CachedEntity> locationEntities = db.getEntities(new ArrayList(pathsToLocations.values()));

			log.info("Number of paths: "+paths.size());
			// Choose a path. Paths are already shuffled so we'll just choose at random
			for(int i = 0; i<paths.size(); i++)
			{
				if (getPropagationCount()<=0) return;
				
				if (paths.get(i).getProperty("discoveryChance")==null || (Double)paths.get(i).getProperty("discoveryChance")<=0d)
					continue;
				
				CachedEntity pathToPropagateTo = paths.get(i);
				Key locationToPropagateTo = pathsToLocations.get(pathToPropagateTo);
				
				// If there is already an NPCArmy at this location, we will add to it
				List<CachedEntity> npcArmiesAtNextLocation = db.getFilteredList("NPCArmy", "locationKey", locationToPropagateTo);
				
				CachedEntity npcArmyAtNextLocation = null;
				for(CachedEntity npcArmyAtNextLocationCandidate:npcArmiesAtNextLocation)
					if (GameUtils.equals(npcArmyAtNextLocationCandidate.getProperty("npcDefKey"), getNpcDefKey()))
					{
						npcArmyAtNextLocation = npcArmyAtNextLocationCandidate;
						break;
					}
				
				// If there is no army at the next location, we will create one. If there is, we'll add a propagation value to it
				if (npcArmyAtNextLocation==null)
				{
					CachedEntity locationEntity = locationEntities.get(i);
					if (locationEntity==null) continue; // Cancel propagation to here, the location is deleted
					if (isValidPropogateLocation(locationEntity) == false) return;
					log.info("Propagating to a new location: "+locationToPropagateTo);
					CachedEntity newNpcArmy = new CachedEntity("NPCArmy");
					newNpcArmy.setProperty("maxSpawnCount", db.solveCurve_Long(getPropagatedMaxSpawnCount()));
					newNpcArmy.setProperty("propagatedMaxSpawnCount", getPropagatedMaxSpawnCount());
					newNpcArmy.setProperty("propagationCount", 0L);
					newNpcArmy.setProperty("seed", true);
					newNpcArmy.setProperty("spawnsPerTick", getSpawnsPerTick());
					newNpcArmy.setProperty("npcDefKey", getNpcDefKey());
					newNpcArmy.setProperty("locationKey", locationToPropagateTo);
					newNpcArmy.setProperty("uniqueId", getUniqueId());
					newNpcArmy.setProperty("name", getUniqueId()+": "+locationEntity.getProperty("name")+"(max: "+newNpcArmy.getProperty("maxSpawnCount")+")");
					newNpcArmy.setProperty("excludedLocations", getExcludedPropagateLocations());
	
					setPropagationCount(getPropagationCount()-1);

					db.getDB().put(newNpcArmy);
				}
				else
				{
					Long nextLocationPropagationCount = (Long)npcArmyAtNextLocation.getProperty("propagationCount");
					// If the location we're propagating to has the same or more propagationCount, then don't bother performing the propagation
					// and try the next path instead
					if (nextLocationPropagationCount>=getPropagationCount())
					{
						log.finest("Was going to propagate to "+locationToPropagateTo+" but it has "+nextLocationPropagationCount+" propagation count when we only have "+getPropagationCount()+", so we're going to skip propagation here and try another path.");
						continue;
					}
					
					log.info("Propagating to a location we previous propagated to before: "+locationToPropagateTo);
					npcArmyAtNextLocation.setProperty("propagationCount", nextLocationPropagationCount+1);
					setPropagationCount(getPropagationCount()-1);
					
					db.getDB().put(npcArmyAtNextLocation);
					
				}
			}			
		}
	}
	
	public boolean isValidPropogateLocation(CachedEntity location)
	{
		return location != null && 
				"Script".equals(location.getKind()) == false &&
				CommonChecks.checkLocationIsInstance(location) == false;
	}
	
	
}
