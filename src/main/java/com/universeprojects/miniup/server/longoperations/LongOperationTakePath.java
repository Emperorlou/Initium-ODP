package com.universeprojects.miniup.server.longoperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.AbortTransactionException;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumTransaction;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.ConfirmAttackException;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.WarnPlayerException;
import com.universeprojects.miniup.server.scripting.events.MoveEvent;
import com.universeprojects.miniup.server.services.BlockadeService;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.GuardService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;
import com.universeprojects.miniup.server.services.ScriptService;
import com.universeprojects.miniup.server.services.TerritoryService;

public class LongOperationTakePath extends LongOperation {

	public LongOperationTakePath(ODPDBAccess db, 
			Map<String, String[]> requestParameters) throws UserErrorMessage {
		super(db, requestParameters);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getPageRefreshJavascriptCall() {
		Long pathId = (Long)getDataProperty("pathId");
		boolean attack = false;
		if (getDataProperty("attack")!=null && "true".equals(getDataProperty("attack").toString()))
			attack = true;
		return "doGoto(null, "+pathId+", "+attack+");";
	}

	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage
	{
		int scriptDelay = 0;
		
		CombatService combatService = new CombatService(db);
		
		boolean allowAttack = false;
		if ("true".equals(parameters.get("attack")))
			allowAttack=true;
		setDataProperty("attack", allowAttack);
		setDataProperty("pathId", Long.parseLong(parameters.get("pathId")));
			
		CachedEntity path = db.getEntity(KeyFactory.createKey("Path", Long.parseLong(parameters.get("pathId"))));
		if (path==null)
			throw new UserErrorMessage("Unable to take path. The path does not exist.");

		try
		{
			CachedEntity character = db.getCurrentCharacter();
			
			if(CommonChecks.checkCharacterIsZombie(character))
				throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
			
			String forceOneWay = (String)path.getProperty("forceOneWay");
			if ("FromLocation1Only".equals(forceOneWay) && GameUtils.equals(character.getProperty("locationKey"), path.getProperty("location2Key")))
				throw new UserErrorMessage("You cannot take this path.");
			if ("FromLocation2Only".equals(forceOneWay) && GameUtils.equals(character.getProperty("locationKey"), path.getProperty("location1Key")))
				throw new UserErrorMessage("You cannot take this path.");		
			
			
			if (ODPDBAccess.CHARACTER_MODE_COMBAT.equals(character.getProperty("mode")))
			{
				// TODO: Double check that the combat mode is legitimate
				throw new UserErrorMessage("You cannot move right now because you are currently in combat.");
			}
			if (ODPDBAccess.CHARACTER_MODE_MERCHANT.equals(character.getProperty("mode")))
				throw new UserErrorMessage("You cannot move right now because you are currently vending. <br><a onclick='closeAllPopups(); toggleStorefront();'>Shutdown Store</a>");
			if (ODPDBAccess.CHARACTER_MODE_TRADING.equals(character.getProperty("mode")))
				throw new UserErrorMessage("You cannot move right now because you are currently trading.");
			if (GameUtils.isPlayerIncapacitated(character))
				throw new UserErrorMessage("You are currently knocked out and cannot move.");
			if (character.getProperty("mode")==null || "".equals(character.getProperty("mode")) || ODPDBAccess.CHARACTER_MODE_NORMAL.equals(character.getProperty("mode")))
			{/*We're in normal mode and so we can actually move*/}
			else
				throw new UserErrorMessage("You cannot move right now because you are busy.");
			
			CachedEntity destination = null;
			Key destinationKey = null;
			// First get the character's current location
			Key currentLocationKey = db.getCharacterLocationKey(character);
			CachedEntity currentLocation = null;
			
			currentLocation = db.getEntity(currentLocationKey);
			
			destination = getDestination(currentLocation, path);
			destinationKey = destination.getKey();
			
	
			boolean isInParty = true;
			if (character.getProperty("partyCode")==null || character.getProperty("partyCode").equals(""))
				isInParty = false;
			
			if(isInParty && GameUtils.isCharacterPartyLeader(character) == false)
				throw new UserErrorMessage("You cannot move your party because you are not the leader.");
			
			if(isInParty && db.getParty(ds, character).size() > 4)
				throw new UserErrorMessage("You have too many members in your party!");
			
			// Log taken path.
			Logger.getLogger(this.getClass().getSimpleName()).log(
					Level.WARNING, 
					String.format("%s taking %s from %s", 
							character.getKey(),
							path.getKey(),
							currentLocationKey));
			
			//onMoveBegin scripts
			@SuppressWarnings("unchecked")
			List<Key> scriptKeys = (List<Key>) path.getProperty("scripts");
			
			if (scriptKeys!=null && scriptKeys.isEmpty()==false)
			{
				List<CachedEntity> onBeginScripts = db.getScriptsOfType(scriptKeys, ODPDBAccess.ScriptType.onMoveBegin);
				
				ScriptService service = ScriptService.getScriptService(db);
				
				for(CachedEntity script : onBeginScripts) {
					MoveEvent mEvent = new MoveEvent(db, character, currentLocation, destination);
					
					if(service.executeScript(mEvent, script, path)) {
						if(!mEvent.haltExecution) {
							
							scriptDelay += mEvent.delay;
							service.cleanupEvent(mEvent);
							
							if(mEvent.stop) {
								if(mEvent.stopMessage == null) 
									mEvent.stopMessage = "You were interrupted!";
								
								throw new UserErrorMessage(mEvent.stopMessage);
									
							}

						}
					}
				}
			}
			
			// Do the territory interruption now
			doTerritoryInterruption(destination, path, allowAttack, isInParty);
			
			// Check if we're going to enter combat from Instance
			if ("Instance".equals(destination.getProperty("combatType")))
			{
				if(isInParty)
					throw new UserErrorMessage("You are approaching an instance but cannot attack as a party. Disband your party before attacking the instance (you can still do it together, just not using party mechanics).");
	
				db.resetInstanceRespawnTimer(destination);
				
				CachedEntity monster = db.getCombatantFor(db.getCurrentCharacter(), destination);
				if (monster!=null)
				{
					ds.beginBulkWriteMode();
					
					ds.put(monster);
					ds.put(db.getCurrentCharacter());
					
					if(destination.isUnsaved())
						ds.put(destination);
					
					ds.commitBulkWrite();
					throw new GameStateChangeException("A "+monster.getProperty("name")+" stands in your way.");
				}
				
				// Getting here means we need to check if any spawners have available monsters.
				final List<CachedEntity> instanceSpawners = db.getInstanceSpawnersForLocation(currentLocationKey, destinationKey);
				if(instanceSpawners.isEmpty() == false)
				{
					final CachedEntity finalChar = db.getCurrentCharacter(); 
					CachedEntity mobSpawner = null;
					try 
					{
						mobSpawner = new InitiumTransaction<CachedEntity>(ds) 
						{
							@Override
							public CachedEntity doTransaction(CachedDatastoreService ds) 
							{
								finalChar.refetch(ds);
								for(CachedEntity curSpawner:instanceSpawners)
								{
									curSpawner.refetch(ds);
									
									Double availableSpawn = (Double)curSpawner.getProperty("availableMonsterCount");
									if(availableSpawn == null || availableSpawn.intValue() < 1) continue;
									
									if(curSpawner.getProperty("npcDefKey") != null)
									{
										availableSpawn = Math.max(0.0d, availableSpawn-1.0d);
										curSpawner.setProperty("availableMonsterCount", availableSpawn);
										
										ds.put(curSpawner);
										return curSpawner;
									}
								}
								// Null indicates nothing spawned. Fall through.
								return null;
							}
						}.run();
					} 
					catch (AbortTransactionException e) 
					{
						throw new RuntimeException(e.getMessage());
					}
					
					if(mobSpawner != null)
					{
						CachedEntity npcDef = ds.getIfExists((Key)mobSpawner.getProperty("npcDefKey"));
						if(npcDef != null)
						{
							ds.beginBulkWriteMode();
							monster = db.doCreateMonster(npcDef, (Key)mobSpawner.getProperty("locationKey"));
							
							// Set combatants and modes.
							finalChar.setProperty("combatant", monster.getKey());
							finalChar.setProperty("combatType", "DefenceStructureAttack");
							finalChar.setProperty("mode", ODPDBAccess.CHARACTER_MODE_COMBAT);
							db.flagCharacterCombatAction(ds, finalChar);
							
							monster.setProperty("combatant", finalChar.getKey());
							monster.setProperty("mode", ODPDBAccess.CHARACTER_MODE_COMBAT);
							String status = (String)mobSpawner.getProperty("instanceMonsterStatus");
							if(status == null) status = "Normal";
							monster.setProperty("status", status);
							monster.setProperty("monsterSpawnerKey", mobSpawner.getKey());
							
							// Clear HCM flag. Instance mobs/items should not limit HCM encounters.
							monster.setProperty("hardcoreMode", null);
							
							ds.put(finalChar, monster);
							
							if(destination.isUnsaved())
								ds.put(destination);
							
							ds.commitBulkWrite();
							
							throw new GameStateChangeException("A "+monster.getProperty("name")+" stands in your way.");
						}
					}
				}
			}
			else if ("CombatSite".equals(destination.getProperty("type"))==false)	// However, for non-instances... (and not combat sites)
			{
				// Now determine if the path contains an NPC that the character would immediately enter battle with...
				QueryHelper qh = new QueryHelper(ds);
				List<CachedEntity> npcsInTheArea = qh.getFilteredList("Character", "locationKey", destinationKey, "type", "NPC");
				npcsInTheArea = new ArrayList<CachedEntity>(npcsInTheArea);
		
				if (npcsInTheArea.isEmpty()==false)
				{
					db.shuffleCharactersByAttackOrder(npcsInTheArea);
					for(CachedEntity possibleNPC:npcsInTheArea)
						if ((CommonChecks.checkCharacterIsBusy(possibleNPC) == false ||
							// Fixes Combat mode set with null combatant issue
							CommonChecks.checkCharacterIsInCombat(possibleNPC) == false || 
							CommonChecks.checkCharacterIsRaidBoss(possibleNPC)) && 
								(Double)possibleNPC.getProperty("hitpoints")>0d)
						{
							// We do not set partied field on NPC block.
							combatService.enterCombat(character, possibleNPC, false);
							
							throw new GameStateChangeException("A "+possibleNPC.getProperty("name")+" stands in your way."); // If we've been interrupted, we'll just get out and not actually travel to the location
						}
				}
			}
			
			BlockadeService bs = new BlockadeService(db);
			// Check if we're being blocked by the blockade
			CachedEntity blockadeStructure = bs.getBlockadeFor(db.getCurrentCharacter(), destination);
			
			if (isInParty && blockadeStructure!=null)
				throw new UserErrorMessage("You are approaching a defensive structure but you cannot attack as a party. Disband your party before attacking the defensive structure.");
			
			if (allowAttack==false && blockadeStructure!=null)
				throw new UserErrorMessage("You are approaching a defensive structure which will cause you to enter into combat with whoever is defending the structure. Are you sure you want to approach?<br><br><a onclick='closeAllPopups();doGoto(event,"+path.getKey().getId()+",true)'>Click here to attack!</a>", false);
			
			GuardService guardService = new GuardService(db);
			CachedEntity guardToAttack = guardService.tryToEnterLocation(character, destinationKey, allowAttack);
			if (guardToAttack!=null)
			{
				combatService.enterCombat(character, guardToAttack, true, true);
				
				throw new GameStateChangeException(); // If we've been interrupted, we'll just get out and not actually travel to the location
				
			}

			
			CachedEntity rootDestination = db.getRootLocation(destination);
			CachedEntity rootCurrentLocation = db.getRootLocation(currentLocation);
			boolean longDistanceTravel = GameUtils.equals(rootDestination, rootCurrentLocation)==false;
			
			// Ok, lets begin then...
			setDataProperty("locationName", destination.getProperty("name"));
			
			Long buffedTravel = 0L;
			if ((path.getProperty("travelTime")!=null && ((Long)path.getProperty("travelTime"))>0) || longDistanceTravel)
				buffedTravel = db.getPathTravelTime(path, character);
			
			buffedTravel += scriptDelay;
			
			if(buffedTravel == 0)
				buffedTravel = 1L; //time of 0 sometimes rubberbands.
			
			setDataProperty("secondsToWait", buffedTravel);
			
			setLongOperationName("Walking to "+destination.getProperty("name"));
			setLongOperationDescription("You're traveling between 2 major locations.");
			
			return buffedTravel.intValue();
		}
		catch(ConfirmAttackException e)
		{
			throw new UserErrorMessage(e.getMessage()+"<br><br><a onclick='closeAllPopups();doGoto(event,"+path.getKey().getId()+",true)'>Click here</a> to continue to advance anyway.");
		}
	}

	

	@Override
	String doComplete() throws UserErrorMessage {
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity location = db.getCharacterLocation(character);
		CachedEntity path = db.getEntity(KeyFactory.createKey("Path", (Long)getDataProperty("pathId")));
		if (path==null)
			throw new UserErrorMessage("The path you were attempting to take no longer exists.");
		
		db.getDB().beginBulkWriteMode();
		try
		{
			//onMoveEnd scripts. We want these to run before the game rolls for a monster encounter, so we can interrupt
			//the path if necessary.
			@SuppressWarnings("unchecked")
			List<Key> scriptKeys = (List<Key>) path.getProperty("scripts");
			
			if (scriptKeys!=null && scriptKeys.isEmpty()==false)
			{
				List<CachedEntity> onEndScripts = db.getScriptsOfType(scriptKeys, ODPDBAccess.ScriptType.onMoveEnd);
				
				ScriptService service = ScriptService.getScriptService(db);
				
				for(CachedEntity script : onEndScripts) {
					MoveEvent mEvent = new MoveEvent(db, character, location, getDestination(location, path));
					
					if(service.executeScript(mEvent, script, path)) {
						if(!mEvent.haltExecution) {
							
							service.cleanupEvent(mEvent);
							
							if(mEvent.stop) {
								if(mEvent.stopMessage == null) 
									mEvent.stopMessage = "You were interrupted!";
																
								throw new UserErrorMessage(mEvent.stopMessage);
							}
						}
					}
				}
			}
			
			if (db.randomMonsterEncounter(ds, character, location, 1, 0.5d))
			{
				// Create discovery for character, and optionally unhide it (unsaved = already exists);
				CachedEntity discovery = db.newDiscovery(null, character, path);
				if(discovery != null && discovery.getKey().isComplete())
				{
					discovery.setProperty("hidden", false);
					db.getDB().put(discovery);
				}
				throw new GameStateChangeException("While you were on your way, someone found you...", true);
			}
		}
		finally
		{
			db.getDB().commitBulkWrite();
		}
			
		Boolean attack = (Boolean)getDataProperty("attack");
		if (attack==null) attack = false;
	
		CachedEntity newLocation = db.doCharacterTakePath(ds, db.getCurrentCharacter(), path, attack);
		
		
		MainPageUpdateService update = MainPageUpdateService.getInstance(db, db.getCurrentUser(), db.getCurrentCharacter(), newLocation, this);
		update.updateFullPage_shortcut(true);

		getQuestService().checkLocationForObjectiveCompletions(newLocation);
		getQuestService().checkCharacterPropertiesForObjectiveCompletions();
		
		return "You have arrived at "+newLocation.getProperty("name")+".";
	}

	@Override
	public Map<String, Object> getStateData() {
		Map<String, Object> result = super.getStateData();
		
		result.put("locationName", getDataProperty("locationName"));
		
		return result;
	}


	private void doTerritoryInterruption(CachedEntity destination, CachedEntity path, boolean allowAttack, boolean isInParty) throws UserErrorMessage
	{
		
		// See if we're going to get interrupted by territory rules..
		CachedEntity territory = db.getEntity((Key)destination.getProperty("territoryKey"));
		if (territory!=null)
		{
			if (isInParty)
				throw new UserErrorMessage("You cannot enter a PvP territory while in a party. Disband your party first.");
			
			TerritoryService ts = new TerritoryService(db, territory);
			try
			{
				boolean warn = true;
				if (allowAttack==true) warn = false;
				ts.processRegularActionInterruption(db.getCurrentCharacter(), destination, allowAttack, warn);
			}
			catch (WarnPlayerException e)
			{
				setDataProperty("attack", true);
				throw new UserErrorMessage("You are entering a territory that is restricted to you. If you continue, the defenders of this territory will attack you.<br><br><a onclick='closeAllPopups();doGoto(event,"+path.getKey().getId()+",true)'>Click here</a> to continue to advance anyway.");
			}
		}
	}
	private CachedEntity getDestination(CachedEntity currentLocation, CachedEntity path) throws UserErrorMessage{
		// Then determine which location the character will end up on.
		// If we find that the character isn't on either end of the path, we'll throw.
		CachedEntity character = db.getCurrentCharacter();
		Key currentLocationKey = currentLocation.getKey();
		Key destinationKey = null;
		Key pathLocation1Key = (Key)path.getProperty("location1Key");
		Key pathLocation2Key = (Key)path.getProperty("location2Key");
		if (currentLocationKey.getId()==pathLocation1Key.getId())
			destinationKey = pathLocation2Key;
		else if (currentLocationKey.getId()==pathLocation2Key.getId())
			destinationKey = pathLocation1Key;
		else
		{
			// We want to now allow players to travel between root locations easily so lets check if that's a possibility now...
			if (CommonChecks.checkLocationIsRootLocation(currentLocation)==false)
			{
				CachedEntity rootLocation = db.getRootLocation(currentLocation);
				if (GameUtils.equals(rootLocation.getKey(), pathLocation1Key))
					destinationKey = pathLocation2Key;
				else if (GameUtils.equals(rootLocation.getKey(), pathLocation2Key))
					destinationKey = pathLocation1Key;
			}
			
			if (destinationKey==null)
				throw new UserErrorMessage("Character cannot take a path when he is not located at either end of it. Character("+character.getKey().getId()+") Path("+path.getKey().getId()+")");
		}

		// Script paths have script as destination. Let them discover (with party),
		// but don't let them take the path. Generated button has trigger script.
		if("Script".equals(destinationKey.getKind()))
		{
			try
			{
				List<CachedEntity> party = db.getParty(ds, character);
				if(party == null || party.size() == 0) party = Arrays.asList(character);
				for(CachedEntity member:party)
					db.newDiscovery(ds, member, path);
			}
			catch (Exception tex) {}
			throw new UserErrorMessage("You cannot travel this path normally...");
		}
		
		return db.getEntity(destinationKey);
	}
}
