package com.universeprojects.miniup.server.longoperations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
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
		if ("true".equals(parameters.get("ignoreCombatSites")))
				ignoreCombatSites = true;
		
		if (GameUtils.isCharacterInParty(db.getCurrentCharacter()) && GameUtils.isCharacterPartyLeader(db.getCurrentCharacter())==false)
			throw new UserErrorMessage("You cannot move your party because you are not the leader.");
		
		if (ODPDBAccess.CHARACTER_MODE_COMBAT.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot explore right now because you are currently in combat.");
		if (ODPDBAccess.CHARACTER_MODE_MERCHANT.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot explore right now because you are currently vending.");
		if (ODPDBAccess.CHARACTER_MODE_TRADING.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot explore right now because you are currently trading.");
		if (db.getCurrentCharacter().getProperty("mode")==null || "".equals(db.getCurrentCharacter().getProperty("mode")) || ODPDBAccess.CHARACTER_MODE_NORMAL.equals(db.getCurrentCharacter().getProperty("mode")))
		{/*We're in normal mode and so we can actually explore*/}
		else
			throw new UserErrorMessage("You cannot explore right now because you are busy.");

		Key locationKey = (Key)db.getCurrentCharacter().getProperty("locationKey");
		CachedEntity location = db.getEntity(locationKey);
		
		
		setDataProperty("ignoreCombatSites", ignoreCombatSites);
		setDataProperty("locationName", location.getProperty("name"));
		
		return 6;
	}

	@Override
	String doComplete() throws UserErrorMessage, ContinuationException 
	{
		Boolean ignoreCombatSites = (Boolean)getDataProperty("ignoreCombatSites");
		if (ignoreCombatSites==null) ignoreCombatSites = false;
		
		Object oldLocaiton = db.getCurrentCharacter().getProperty("locationKey");
		Object oldCombatant = db.getCurrentCharacter().getProperty("combatant");
		
		String result = explore(db, ignoreCombatSites);
		if (result.equals("After some exhausting searching you failed to find anything. That doesn't necessarily mean there is nothing to be found though.."))
		{
			Integer continuationCount = (Integer)getDataProperty("continuationCount");
			if (continuationCount==null) continuationCount = 0;
			continuationCount++;
			
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
			MainPageUpdateService update = new MainPageUpdateService(db, db.getCurrentUser(), db.getCurrentCharacter(), location, this);
			update.updateFullPage_shortcut();
			
		}
		else
		{
			MainPageUpdateService update = new MainPageUpdateService(db, db.getCurrentUser(), db.getCurrentCharacter(), location, this);
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

	public String explore(ODPDBAccess db, boolean ignoreCombatSites) throws UserErrorMessage
	{
		ds.beginBulkWriteMode();
		try
		{
			Key locationKey = (Key)db.getCurrentCharacter().getProperty("locationKey");
			CachedEntity location = db.getEntity(locationKey);
			
			// First get all the things that can be discovered at the character's current location
			List<CachedEntity> discoverablePaths_PermanentOnly = db.getPathsByLocation_PermanentOnly(locationKey);
			Collections.shuffle(discoverablePaths_PermanentOnly);
			
			List<CachedEntity> discoverablePaths_CampsAndBlockades = db.getPathsByLocationAndType(locationKey, "CampSite");
			List<CachedEntity> discoverablePaths_BlockadesOnly = db.getPathsByLocationAndType(locationKey, "BlockadeSite");
			discoverablePaths_CampsAndBlockades.addAll(discoverablePaths_BlockadesOnly);
			Collections.shuffle(discoverablePaths_CampsAndBlockades);
			
			List<CachedEntity> discoverablePaths = db.getPathsByLocation(locationKey);
			Collections.shuffle(discoverablePaths);
			
			// And get all the things the character has discovered...
			List<CachedEntity> discoveries = db.getDiscoveriesForCharacterAndLocation(db.getCurrentCharacter().getKey(), locationKey, true);
			
			
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
					return "You found yourself a monster! He doesn't look too happy.."; 
				}
				
				// Now try to discover campsites...
				for(CachedEntity path:discoverablePaths_CampsAndBlockades)
				{
					Double discoveryChance = (Double)path.getProperty("discoveryChance");
					if (discoveryChance!=null && discoveryChance==100d)
						continue;
					if (discoveryChance!=null && discoveryChance>0 && GameUtils.roll(discoveryChance))
					{
						boolean skipPath = false;
						// First check if we have already discovered this. If so, just keep rolling for the rest of the stuff to discover
						for(CachedEntity discovery:discoveries)
							if (discovery.getProperty("kind").equals("Path") && ((Key)discovery.getProperty("entityKey")).getId() == path.getKey().getId())
							{
								skipPath=true;
								break;
							}
						if (skipPath) continue;
	
						// Now check if the path is valid. If the "other" location doesn't exist, just delete the path right away
						if (((Key)path.getProperty("location1Key")).getId()==locationKey.getId())
						{
							if (ds.getIfExists((Key)path.getProperty("location2Key"))==null)
							{
								ds.delete(path);
								continue;
							}
						}
						else
						{
							if (ds.getIfExists((Key)path.getProperty("location1Key"))==null)
							{
								ds.delete(path);
								continue;
							}
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
				if (db.randomCollectionDiscovery(ds, db.getCurrentCharacter(), location, 1, 1d))
				{
					return "Ohhh, you found something interesting here. You might be able to use this..";
				}
				
				// Now try to discover other, non-combat sites...
				for(CachedEntity path:discoverablePaths_PermanentOnly)
				{
					if (path.getProperty("discoveryChance") instanceof Long)
					{
						path.setProperty("discoveryChance", ((Long)path.getProperty("discoveryChance")).doubleValue());
						ds.put(path);
					}
					
					Double discoveryChance = (Double)path.getProperty("discoveryChance");
					if (discoveryChance!=null && discoveryChance==100d)
						continue;
	
					
					// Now include the day/night changes to the discovery chance...
					if (discoveryChance!=null && location.getProperty("isOutside")!=null && location.getProperty("isOutside").equals("TRUE"))
					{
						double daynight = GameUtils.getDayNight();
						daynight*=45;
						daynight-=15;
						discoveryChance-=daynight;
					}
					
					
					if (discoveryChance!=null && discoveryChance>0 && GameUtils.roll(discoveryChance))
					{
						boolean skipPath = false;
						// First check if we have already discovered this. If so, just keep rolling for the rest of the stuff to discover
						for(CachedEntity discovery:discoveries)
							if (discovery.getProperty("kind").equals("Path") && ((Key)discovery.getProperty("entityKey")).getId() == path.getKey().getId())
							{
								skipPath=true;
								break;
							}
						if (skipPath) continue;
	
						// Now check if the path is valid. If the "other" location doesn't exist, just delete the path right away
						if (GameUtils.equals(path.getProperty("location1Key"), db.getCurrentCharacter().getProperty("locationKey")))
						{
							if (ds.getIfExists((Key)path.getProperty("location2Key"))==null)
							{
								ds.delete(path);
								continue;
							}
						}
						else
						{
							if (ds.getIfExists((Key)path.getProperty("location1Key"))==null)
							{
								ds.delete(path);
								continue;
							}
						}
						
							
						discoverPath(ds, db, path);
						return "You found a new place! You've never been here before..";
					}
				}
				
				// Now discover combat sites
				if (ignoreCombatSites==false)
				for(CachedEntity path:discoverablePaths)
				{
					if ("CombatSite".equals(path.getProperty("type"))==false)
						continue;
					
	
					if (path.getProperty("discoveryChance") instanceof Long)
					{
						path.setProperty("discoveryChance", ((Long)path.getProperty("discoveryChance")).doubleValue());
						ds.put(path);
					}
					
					
					Double discoveryChance = (Double)path.getProperty("discoveryChance");
					if (discoveryChance!=null && discoveryChance==100d)
						continue;
					if (discoveryChance!=null && discoveryChance>0 && GameUtils.roll(discoveryChance))
					{
						boolean skipPath = false;
						// First check if we have already discovered this. If so, just keep rolling for the rest of the stuff to discover
						for(CachedEntity discovery:discoveries)
							if (discovery.getProperty("kind").equals("Path") && ((Key)discovery.getProperty("entityKey")).getId() == path.getKey().getId())
							{
								skipPath=true;
								break;
							}
						if (skipPath) continue;
	
						
						// Now check if the path is valid. If the "other" location doesn't exist, just delete the path right away
						if (GameUtils.equals(path.getProperty("location1Key"), db.getCurrentCharacter().getProperty("locationKey")))
						{
							if (ds.getIfExists((Key)path.getProperty("location2Key"))==null)
							{
								ds.delete(path);
								continue;
							}
						}
						else
						{
							if (ds.getIfExists((Key)path.getProperty("location1Key"))==null)
							{
								ds.delete(path);
								continue;
							}
						}
							
						discoverPath(ds, db, path);
						return "You found an old combat site. A battle had clearly taken place here..";
					}
				}
			}
		}
		finally
		{
			ds.commitBulkWrite();
		}
		
		return "After some exhausting searching you failed to find anything. That doesn't necessarily mean there is nothing to be found though..";
	}

	
	public void discoverPath(CachedDatastoreService ds, ODPDBAccess db, CachedEntity path) throws UserErrorMessage
	{
		
		db.newDiscovery(ds, db.getCurrentCharacter(), path);
		
		String mode = (String)db.getCurrentCharacter().getProperty("mode");
		db.doCharacterTakePath(ds, db.getCurrentCharacter(), path, false, true);
	}
	
}
