package com.universeprojects.miniup.server.longoperations;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class LongOperationExplore extends LongOperation {

	public LongOperationExplore(ODPDBAccess db, 
			Map<String, String[]> requestParameters) throws UserErrorMessage {
		super(db, requestParameters);
	}

	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage {
		Boolean ignoreCombatSites = false;
		Boolean findNaturalResources = false;
		if ("true".equals(parameters.get("ignoreCombatSites")))
			ignoreCombatSites = true;
		if ("true".equals(parameters.get("findNaturalResources")))
			findNaturalResources = true;
		
		if (GameUtils.isCharacterInParty(db.getCurrentCharacter()) && GameUtils.isCharacterPartyLeader(db.getCurrentCharacter())==false)
			throw new UserErrorMessage("You cannot move your party because you are not the leader.");
		
		if (ODPDBAccess.CHARACTER_MODE_COMBAT.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot explore right now because you are currently in combat.");
		if (ODPDBAccess.CHARACTER_MODE_MERCHANT.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot explore right now because you are currently vending. <br><a onclick='closeAllPopups(); toggleStorefront();'>Shutdown Store</a>");
		if (ODPDBAccess.CHARACTER_MODE_TRADING.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot explore right now because you are currently trading.");
		if (db.getCurrentCharacter().getProperty("mode")==null || "".equals(db.getCurrentCharacter().getProperty("mode")) || ODPDBAccess.CHARACTER_MODE_NORMAL.equals(db.getCurrentCharacter().getProperty("mode")))
		{/*We're in normal mode and so we can actually explore*/}
		else
			throw new UserErrorMessage("You cannot explore right now because you are busy.");

		CachedEntity location = db.getCharacterLocation(db.getCurrentCharacter());
		
		CachedEntity permanentLocation = db.getParentPermanentLocation(location);
		if (permanentLocation!=null) location = permanentLocation;
		

		if (findNaturalResources && CommonChecks.checkLocationIsGoodForNaturalResource(location)==false)
			throw new UserErrorMessage("This is not somewhere that you can find natural resources. You generally need to use this option from a major location (like Aera Countryside and the likes).");
		
		setDataProperty("ignoreCombatSites", ignoreCombatSites);
		setDataProperty("findNaturalResources", findNaturalResources);
		setDataProperty("locationName", location.getProperty("name"));
		setDataProperty("locationKey", location.getKey());
		
		return 6;
	}

	@Override
	String doComplete() throws UserErrorMessage, ContinuationException 
	{
		Boolean ignoreCombatSites = (Boolean)getDataProperty("ignoreCombatSites");
		if (ignoreCombatSites==null) ignoreCombatSites = false;
		
		Boolean findNaturalResources = (Boolean)getDataProperty("findNaturalResources");
		if (findNaturalResources==null) findNaturalResources = false;
		
		Object oldLocaiton = ds.getIfExists((Key)getDataProperty("locationKey"));
		Object oldCombatant = db.getCurrentCharacter().getProperty("combatant");
		
		String result = explore(db, ignoreCombatSites, findNaturalResources);
		if (result.equals("After some exhausting searching you failed to find anything. That doesn't necessarily mean there is nothing to be found though..") || 
				result.equals("After some searching you failed to find a new place that you haven't seen before, but you can try again.."))
		{
			Integer continuationCount = (Integer)getDataProperty("continuationCount");
			if (continuationCount==null) continuationCount = 0;
			continuationCount++;
			
			if (continuationCount==1)
				db.sendGameMessage(ds, db.getCurrentCharacter(), "You're still searching. You will continue to explore until you find something interesting (up to 2 minutes).");
			
			if (continuationCount<20)	// Maximum number of continuations
			{
				setDataProperty("continuationCount", continuationCount);
				throw new ContinuationException(6);
			}
		}
		
		Object newLocation = db.getCurrentCharacter().getProperty("locationKey");
		Object newCombatant = db.getCurrentCharacter().getProperty("combatant");
		
		CachedEntity location = db.getEntity((Key)newLocation);
		if (GameUtils.equals(oldLocaiton, newLocation)==false || GameUtils.equals(oldCombatant, newCombatant)==false)
		{
			MainPageUpdateService update = MainPageUpdateService.getInstance(db, db.getCurrentUser(), db.getCurrentCharacter(), location, this);
			update.updateFullPage_shortcut(true);
			
		}
		else
		{
			MainPageUpdateService update = MainPageUpdateService.getInstance(db, db.getCurrentUser(), db.getCurrentCharacter(), location, this);
			update.updateMonsterCountPanel();
			update.updateMidMessagePanel();
		}
		
		
		return result;
	}

	@Override
	public String getPageRefreshJavascriptCall() {
		return "doExplore()";
	}

	@Override
	public Map<String, Object> getStateData() {
		Map<String,Object> result = super.getStateData();
		
		result.put("locationName", getDataProperty("locationName"));
		
		return result;
	}

	public String explore(ODPDBAccess db, boolean ignoreCombatSites, boolean findNaturalResources) throws UserErrorMessage
	{
		ds.beginBulkWriteMode();
		try
		{
			Key locationKey = (Key)getDataProperty("locationKey");
			CachedEntity location = db.getEntity(locationKey);
			
			// First get all the things that can be discovered at the character's current location
			List<CachedEntity> discoverablePaths_PermanentOnly = null;
			List<CachedEntity> discoverablePaths_CampsAndBlockades = null;
			List<CachedEntity> discoverablePaths_BlockadesOnly = null;
			List<CachedEntity> discoverablePaths = null;
			List<CachedEntity> discoveries = null;
			Set<String> discoveredEntities = null;

			if (findNaturalResources==false)
			{
				discoverablePaths_PermanentOnly = db.getPathsByLocation_PermanentOnly(locationKey);
				Collections.shuffle(discoverablePaths_PermanentOnly);
				
				discoverablePaths_CampsAndBlockades = db.getPathsByLocationAndType(locationKey, "CampSite");
				discoverablePaths_BlockadesOnly = db.getPathsByLocationAndType(locationKey, "BlockadeSite");
				discoverablePaths_CampsAndBlockades.addAll(discoverablePaths_BlockadesOnly);
				Collections.shuffle(discoverablePaths_CampsAndBlockades);
				
				// Only when we're not ignoring old sites, otherwise don't get the paths.
				if(ignoreCombatSites==false)
				{
					discoverablePaths = db.getPathsByLocation(locationKey);
					Collections.shuffle(discoverablePaths);
				}
				
				
				// And get all the things the character has discovered...
				discoveries = db.getDiscoveriesForCharacterAndLocation(db.getCurrentCharacter().getKey(), locationKey, true);
				discoveredEntities = new HashSet<String>();
				for(CachedEntity disco:discoveries)
					if(disco != null)
						discoveredEntities.add(((Key)disco.getProperty("entityKey")).toString());
			}
			
			
			
	//		Logger.getLogger("ServletCharacterControl").log(Level.WARNING, 
	//				"Discoverable path count (no combat sites): "+discoverablePaths_PermanentOnly.size()+
	//				" Discoverable path count (combat sites included): "+discoverablePaths.size()+
	//				" Discoveries: "+discoveries.size()+" " +
	//						"Discover anything diff:  "+common.getLocationDiscoverAnythingChance());
			
			
			// Try to discover something...
			Double discoverAnythingChance = (Double)location.getProperty("discoverAnythingChance");
			if (discoverAnythingChance!=null && GameUtils.roll(discoverAnythingChance))
	 		{
				// First try to find a monster
				if (db.randomMonsterEncounter(ds, db.getCurrentCharacter(), location))
				{
					return "You're being attacked!"; 
				}
				
				// Now try to discover campsites...
				if (findNaturalResources==false)
				for(CachedEntity path:discoverablePaths_CampsAndBlockades)
				{
					Double discoveryChance = (Double)path.getProperty("discoveryChance");
					if (discoveryChance!=null && discoveryChance==100d)
						continue;
					if (discoveredEntities.contains(path.getKey().toString()))
						continue;
					if (discoveryChance!=null && discoveryChance>0 && GameUtils.roll(discoveryChance))
					{
						Key destinationKey = GameUtils.equals(path.getProperty("location1Key"), locationKey) ? 
								(Key)path.getProperty("location2Key") : (Key)path.getProperty("location1Key");											
						// Now check if the path is valid. If the "other" location doesn't exist, just delete the path right away
						if (ds.getIfExists(destinationKey)==null)
						{
							ds.delete(path);
							continue;
						}
							
						discoverPath(ds, db, path);
						
						if ("BlockadeSite".equals(path.getProperty("type")))		
							return "You found a place that could be used as a defence structure!";
						else if ("CampSite".equals(path.getProperty("type")))
							return "You found a new camp!";
						else
							return "You found a new area!";
					}
					
					
				}			
				
				// Now discover collection sites...
//				if (db.randomCollectionDiscovery(ds, db.getCurrentCharacter(), location, 1, 1d))
//				{
//					return "Ohhh, you found something interesting here. You might be able to use this..";
//				}
				
				// Now try to discover other, non-combat sites...
				if (findNaturalResources==false)
				for(CachedEntity path:discoverablePaths_PermanentOnly)
				{
					if (path.getProperty("discoveryChance") instanceof Long)
					{
						path.setProperty("discoveryChance", ((Long)path.getProperty("discoveryChance")).doubleValue());
						ds.put(path);
					}
					
					if (discoveredEntities.contains(path.getKey().toString()))
						continue;
					
					Double discoveryChance = (Double)path.getProperty("discoveryChance");
					if (discoveryChance!=null && discoveryChance==100d)
						continue;
					
					// Now include the day/night changes to the discovery chance...
					if (discoveryChance!=null && GameUtils.booleanEquals(location.getProperty("isOutside"), true))
					{
						double daynight = GameUtils.getDayNight();
						daynight*=45;
						daynight-=15;
						discoveryChance-=daynight;
					}
					
					
					if (discoveryChance!=null && discoveryChance>0 && GameUtils.roll(discoveryChance))
					{
						// Now check if the path is valid. If the "other" location doesn't exist, just delete the path right away
						Key destinationKey = GameUtils.equals(path.getProperty("location1Key"), locationKey) ? 
								(Key)path.getProperty("location2Key") : (Key)path.getProperty("location1Key");											
						// Now check if the path is valid. If the "other" location doesn't exist, just delete the path right away
						if (ds.getIfExists(destinationKey)==null)
						{
							ds.delete(path);
							continue;
						}
							
						discoverPath(ds, db, path);
						return "You found a new place! You've never been here before..";
					}
				}
				
				// Now discover combat sites
				if (ignoreCombatSites==false && findNaturalResources==false)
				{
					for(CachedEntity path:discoverablePaths)
					{
						if ("CombatSite".equals(path.getProperty("type"))==false)
							continue;
						
						if (path.getProperty("discoveryChance") instanceof Long)
						{
							path.setProperty("discoveryChance", ((Long)path.getProperty("discoveryChance")).doubleValue());
							ds.put(path);
						}
						
						if (discoveredEntities.contains(path.getKey().toString()))
							continue;
						
						Double discoveryChance = (Double)path.getProperty("discoveryChance");
						if (discoveryChance!=null && discoveryChance==100d)
							continue;
						
						if (discoveryChance!=null && discoveryChance>0 && GameUtils.roll(discoveryChance))
						{
							// Now check if the path is valid. If the "other" location doesn't exist, just delete the path right away
							Key destinationKey = GameUtils.equals(path.getProperty("location1Key"), locationKey) ? 
									(Key)path.getProperty("location2Key") : (Key)path.getProperty("location1Key");											
							// Now check if the path is valid. If the "other" location doesn't exist, just delete the path right away
							if (ds.getIfExists(destinationKey)==null)
							{
								ds.delete(path);
								continue;
							}
								
							discoverPath(ds, db, path);
							return "You found an old combat site. A battle had clearly taken place here..";
						}
					}
				}
				
				
				// Now if we're looking for natural resources, lets spawn a new site
				if (findNaturalResources)
				{
					db.doCreateNaturalResourceDiscovery(db.getCurrentCharacter());
					return "You found an area you haven't seen before. There might be something here you're looking for..";
				}
			}
		}
		finally
		{
			ds.commitBulkWrite();
		}
		
		if (findNaturalResources==false)
			return "After some exhausting searching you failed to find anything. That doesn't necessarily mean there is nothing to be found though..";
		else
			return "After some searching you failed to find a new place that you haven't seen before, but you can try again..";
	}

	
	public void discoverPath(CachedDatastoreService ds, ODPDBAccess db, CachedEntity path) throws UserErrorMessage
	{
		db.newDiscovery(ds, db.getCurrentCharacter(), path);
		
		String mode = (String)db.getCurrentCharacter().getProperty("mode");
		db.doCharacterTakePath(ds, db.getCurrentCharacter(), path, false, true);
	}
	
}
