package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ContentDeveloperException;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.controllers.SublocationsController.PathType;

public class LocationService extends Service {

	final EntityPool pool;
	
	public LocationService(ODPDBAccess db) {
		super(db);
		
		pool = new EntityPool(db.getDB());
	}
	
	
	
	/**
	 * Checks 1 level deep if all connected locations to this template root path are also set to templateMode. We can't mix modes.
	 * @param location
	 */
	private boolean isTemplateValid(CachedEntity parentLocation, CachedEntity templatePath) {
		if (CommonChecks.checkLocationOrPathIsTemplateMode(templatePath) == false) return false;
		
		pool.addEntityDirectly(templatePath, parentLocation);
		
		Key otherLocationKey = GameUtils.equals(templatePath.getProperty("location1Key"), parentLocation.getKey()) ? 
				(Key) templatePath.getProperty("location2Key") : 
					(Key) templatePath.getProperty("location1Key");
		
		pool.loadEntities(otherLocationKey);
		
		CachedEntity location = pool.get(otherLocationKey);
		
		List<Key> branchingPathKeys = db.getPathsByLocation_KeysOnly(location.getKey());
		pool.addToQueue(branchingPathKeys);

		pool.loadEntities();
		
		List<CachedEntity> branchingPaths = pool.get(branchingPathKeys);
		List<Key> branchingOtherLocationKeys = new ArrayList<>();
		for(CachedEntity branchingPath:branchingPaths) {
			Key branchingOtherLocationKey = GameUtils.equals(branchingPath.getProperty("location1Key"), location.getKey()) ? 
											(Key) branchingPath.getProperty("location2Key") : 
												(Key) branchingPath.getProperty("location1Key");
											
			branchingOtherLocationKeys.add(branchingOtherLocationKey);								
			pool.addToQueue(branchingOtherLocationKey);
		}
		
		pool.loadEntities();
		
		List<CachedEntity> branchingOtherLocations = pool.get(branchingOtherLocationKeys);
		for(CachedEntity branchingOtherLocation:branchingOtherLocations) {
			if (branchingOtherLocation != null && 
					GameUtils.equals(branchingOtherLocation.getKey(), parentLocation.getKey())==false && 
					CommonChecks.checkLocationOrPathIsTemplateMode(branchingOtherLocation) == false) return false;
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param parentLocation
	 * @param templatePath
	 * @return An array of 2. The newly generated Path and Location, in that order. They have not yet been pushed to the DB.
	 */
	public List<CachedEntity> generateTemplatePath(CachedEntity parentLocation, CachedEntity templatePath, int maxDepth) {
		if (isTemplateValid(parentLocation, templatePath) == false) 
			throw new ContentDeveloperException("Invalid location/path template " + templatePath.getKey()+". It is connected via paths to a non-template location.");
		
		Key otherLocationKey = GameUtils.equals(templatePath.getProperty("location1Key"), parentLocation.getKey()) ? 
				(Key) templatePath.getProperty("location2Key") : 
					(Key) templatePath.getProperty("location1Key");
				
		if (otherLocationKey == null) throw new ContentDeveloperException("Invalid location/path template " + templatePath.getKey()+". location1Key or location2Key is invalid.");
		
		CachedEntity otherLocation = pool.get(otherLocationKey);
		
		CachedEntity generatedLocation = db.generateNewObject(otherLocation, "Location");
		generatedLocation.setProperty("templateMode", false);
		generatedLocation.setProperty("parentLocationKey", parentLocation.getKey());
		
		CachedEntity newPath = db.newPath(ds, 
				parentLocation.getProperty("name") + " - " + generatedLocation.getProperty("name"), 
				parentLocation.getKey(), generatedLocation.getKey(), 
				100d, 0L, PathType.CombatSite.toString());
		
		List<CachedEntity> generatedPathsAndLocations = recursiveGenerateTemplatePathLocation(generatedLocation, otherLocation, templatePath, maxDepth, 1);

		generatedPathsAndLocations.add(0, newPath);
		generatedPathsAndLocations.add(1, generatedLocation);
		return generatedPathsAndLocations;
	}



	private List<CachedEntity> recursiveGenerateTemplatePathLocation(CachedEntity generatedParentLocation, CachedEntity templateLocation, CachedEntity templatePath, int maxDepth, int currentDepth) {
		if (currentDepth == maxDepth) return new ArrayList<>();
		
		if (isTemplateValid(templateLocation, templatePath) == false) 
			throw new ContentDeveloperException("Invalid location/path template " + templatePath.getKey()+". It is connected via paths to a non-template location.");
		
		List<CachedEntity> result = new ArrayList<>();
		
		// Get all the path options
		List<CachedEntity> nextPathOptions = new ArrayList<>();
		List<CachedEntity> unfilteredPathOptions = db.getPathsByLocation(templateLocation.getKey());
		double totalSpawnOdds = 0d;
		for(CachedEntity possibleNextPath:unfilteredPathOptions) {
			if (GameUtils.equals(possibleNextPath, templatePath) == false) {
				nextPathOptions.add(possibleNextPath);
				double nextPathOptionSpawnOdds = (possibleNextPath.getProperty("templateSpawnOdds") == null ? 1d : (Double) possibleNextPath.getProperty("templateSpawnOdds"));
				totalSpawnOdds += nextPathOptionSpawnOdds;
			}
		}
		nextPathOptions.sort((o1, o2) -> -1 * ((Double)(o1.getProperty("templateSpawnOdds") == null ? 1d : (Double)o1.getProperty("templateSpawnOdds"))).compareTo((o2.getProperty("templateSpawnOdds") == null ? 1d : (Double)o2.getProperty("templateSpawnOdds"))));

		// Now randomly choose the next path
		CachedEntity chosenNextPath = null;
		Random rnd = new Random();
		double rndVal = rnd.nextDouble() * totalSpawnOdds;
		
		double rollingCount = 0d;
		for(CachedEntity possibleChosenPath:nextPathOptions) {
			double nextPathOptionSpawnOdds = (possibleChosenPath.getProperty("templateSpawnOdds") == null ? 1d : (Double) possibleChosenPath.getProperty("templateSpawnOdds"));
			
			if (rndVal <= nextPathOptionSpawnOdds + rollingCount) {
				chosenNextPath = possibleChosenPath;
				break;
			}
			rollingCount += nextPathOptionSpawnOdds;
		}
		
		if (chosenNextPath == null) return new ArrayList<>();
		
		// Now create the path and it's connecting location and wire it up properly
		Key otherTemplateLocationKey = GameUtils.equals(chosenNextPath.getProperty("location1Key"), templateLocation.getKey()) ? 
				(Key) chosenNextPath.getProperty("location1Key") : 
					(Key) chosenNextPath.getProperty("location2Key");
				
		if (otherTemplateLocationKey == null) throw new ContentDeveloperException("Invalid location/path template " + templatePath.getKey()+". location1Key or location2Key is invalid.");
		
		pool.loadEntities(otherTemplateLocationKey);
		
		CachedEntity otherTemplateLocation = pool.get(otherTemplateLocationKey);
		
		CachedEntity generatedLocation = db.generateNewObject(otherTemplateLocation, "Location");
		generatedLocation.setProperty("templateMode", false);
		generatedLocation.setProperty("parentLocationKey", generatedParentLocation.getKey());
		
		CachedEntity newPath = db.newPath(ds, 
				generatedParentLocation.getProperty("name") + " - " + generatedLocation.getProperty("name"), 
				generatedParentLocation.getKey(), generatedLocation.getKey(), 
				100d, 0L, PathType.CombatSite.toString());
		result.add(newPath);
		result.add(generatedLocation);
		
		result.addAll(recursiveGenerateTemplatePathLocation(generatedLocation, otherTemplateLocation, templatePath, maxDepth, currentDepth+1));
		
		
		
		return result;
	}
	
	private CachedEntity generateLocationFromDef(CachedEntity parent, CachedEntity locationDef, boolean generatePath) {

		CachedEntity newLocation = db.generateNewObject(new Random(), locationDef, "Location", true);
		newLocation.setProperty("createdDate", new Date());
		newLocation.setProperty("parentLocationKey", parent.getKey());
		
		return newLocation;
	}

}
