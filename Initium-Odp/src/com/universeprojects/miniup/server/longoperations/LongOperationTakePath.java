package com.universeprojects.miniup.server.longoperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.WarnPlayerException;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.TerritoryService;

public class LongOperationTakePath extends LongOperation {

	public LongOperationTakePath(ODPDBAccess db, 
			Map<String, String[]> requestParameters) throws UserErrorMessage {
		super(db, requestParameters);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getPageRefreshJavascriptCall() {
		Long pathId = (Long)data.get("pathId");
		boolean attack = false;
		if (data.get("attack")!=null && "true".equals(data.get("attack").toString()))
			attack = true;
		return "doGoto(null, "+pathId+", "+attack+");";
	}

	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage
	{
		CombatService combatService = new CombatService(db);
		
		boolean allowAttack = false;
		if ("true".equals(parameters.get("attack")))
			allowAttack=true;
		data.put("attack", allowAttack);
		data.put("pathId", Long.parseLong(parameters.get("pathId")));
		
		CachedEntity path = db.getEntity(KeyFactory.createKey("Path", Long.parseLong(parameters.get("pathId"))));
		if (path==null)
			throw new UserErrorMessage("Unable to take path. The path does not exist.");
		
		String forceOneWay = (String)path.getProperty("forceOneWay");
		if ("FromLocation1Only".equals(forceOneWay) && GameUtils.equals(db.getCurrentCharacter().getProperty("locationKey"), path.getProperty("location2Key")))
			throw new UserErrorMessage("You cannot take this path.");
		if ("FromLocation2Only".equals(forceOneWay) && GameUtils.equals(db.getCurrentCharacter().getProperty("locationKey"), path.getProperty("location1Key")))
			throw new UserErrorMessage("You cannot take this path.");		
		
		
		if (ODPDBAccess.CHARACTER_MODE_COMBAT.equals(db.getCurrentCharacter().getProperty("mode")))
		{
			// TODO: Double check that the combat mode is legitimate
			throw new UserErrorMessage("You cannot move right now because you are currently in combat.");
		}
		if (ODPDBAccess.CHARACTER_MODE_MERCHANT.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot move right now because you are currently vending.");
		if (ODPDBAccess.CHARACTER_MODE_TRADING.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot move right now because you are currently trading.");
		if (db.getCurrentCharacter().getProperty("mode")==null || "".equals(db.getCurrentCharacter().getProperty("mode")) || ODPDBAccess.CHARACTER_MODE_NORMAL.equals(db.getCurrentCharacter().getProperty("mode")))
		{/*We're in normal mode and so we can actually move*/}
		else
			throw new UserErrorMessage("You cannot move right now because you are busy.");
		
		CachedEntity destination = null;
		Key destinationKey = null;
		// First get the character's current location
		Key currentLocationKey = (Key)db.getCurrentCharacter().getProperty("locationKey");
		
		// Then determine which location the character will end up on.
		// If we find that the character isn't on either end of the path, we'll throw.
		Key pathLocation1Key = (Key)path.getProperty("location1Key");
		Key pathLocation2Key = (Key)path.getProperty("location2Key");
		if (currentLocationKey.getId()==pathLocation1Key.getId())
			destinationKey = pathLocation2Key;
		else if (currentLocationKey.getId()==pathLocation2Key.getId())
			destinationKey = pathLocation1Key;
		else
			throw new UserErrorMessage("Character cannot take a path when he is not located at either end of it. Character("+db.getCurrentCharacter().getKey().getId()+") Path("+path.getKey().getId()+")");
		destination = db.getEntity(destinationKey);

		boolean isInParty = true;
		if (db.getCurrentCharacter().getProperty("partyCode")==null || db.getCurrentCharacter().getProperty("partyCode").equals(""))
			isInParty = false;
		
		if(isInParty && GameUtils.isCharacterPartyLeader(db.getCurrentCharacter()) == false)
			throw new UserErrorMessage("You cannot move your party because you are not the leader.");
		
		if(isInParty && db.getParty(ds, db.getCurrentCharacter()).size() > 4)
			throw new UserErrorMessage("You have too many members in your party!");
		
		// Do the territory interruption now
		doTerritoryInterruption(destination, path, allowAttack, isInParty);
		
		// Check if we're being blocked by the blockade
		CachedEntity blockadeStructure = db.getBlockadeFor(db.getCurrentCharacter(), destination);
		
		// Check if we're going to enter combat from Instance
		if ("Instance".equals(destination.getProperty("combatType")))
		{
			if(isInParty)
				throw new UserErrorMessage("You are approaching an instance but cannot attack as a party. Disband your party before attacking the instance (you can still do it together, just not using party mechanics).");
			
			CachedEntity monster = db.getCombatantFor(db.getCurrentCharacter(), destination);
			if (monster!=null)
			{
				ds.beginBulkWriteMode();
				
				ds.put(monster);
				ds.put(db.getCurrentCharacter());
				
				db.resetInstanceRespawnTimer(destination);
				if(destination.isUnsaved())
					ds.put(destination);
				
				ds.commitBulkWrite();
				throw new GameStateChangeException("A "+monster.getProperty("name")+" stands in your way.");
			}
		}
		else if ("CombatSite".equals(destination.getProperty("type"))==false)	// However, for non-instances... (and not combat sites)
		{
			// Now determine if the path contains an NPC that the character would immediately enter battle with...
			List<CachedEntity> npcsInTheArea = db.getFilteredList("Character", 300, "locationKey", FilterOperator.EQUAL, destinationKey);
			npcsInTheArea = new ArrayList<CachedEntity>(npcsInTheArea);
	
			if (npcsInTheArea.isEmpty()==false)
			{
				db.shuffleCharactersByAttackOrder(npcsInTheArea);
				for(CachedEntity possibleNPC:npcsInTheArea)
					if ("NPC".equals(possibleNPC.getProperty("type")) && (Double)possibleNPC.getProperty("hitpoints")>0d)
					{
						List<CachedEntity> party = db.getParty(ds, db.getCurrentCharacter());
						db.setPartiedField(party, db.getCurrentCharacter(), "mode", ODPDBAccess.CHARACTER_MODE_COMBAT);
						db.setPartiedField(party, db.getCurrentCharacter(), "combatant", possibleNPC.getKey());
						db.putPartyMembersToDB_SkipSelf(ds, party, db.getCurrentCharacter());
						
						combatService.enterCombat(db.getCurrentCharacter(), possibleNPC, false);
						
						throw new GameStateChangeException("A "+possibleNPC.getProperty("name")+" stands in your way."); // If we've been interrupted, we'll just get out and not actually travel to the location
					}
			}
		}
		
		
		
		if (isInParty && blockadeStructure!=null)
			throw new UserErrorMessage("You are approaching a defensive structure but you cannot attack as a party. Disband your party before attacking the defensive structure.");

		if (isInParty && "Instance".equals(destination.getProperty("combatType")))
			throw new UserErrorMessage("You are approaching an instance but cannot attack as a party. Disband your party before attacking the instance (you can still do it together, just not using party mechanics).");
		
		
		if (allowAttack==false && blockadeStructure!=null)
			throw new UserErrorMessage("You are approaching a defensive structure which will cause you to enter into combat with whoever is defending the structure. Are you sure you want to approach?<br><br><a onclick='closeAllPopups();doGoto(event,"+path.getKey().getId()+",true)'>Click here to attack!</a>", false);
		
		
		// Ok, lets begin then...
		data.put("locationName", destination.getProperty("name"));
		
		Long travelTime = (Long)path.getProperty("travelTime");
		if (travelTime==null)
			travelTime = 6L;
		data.put("secondsToWait", travelTime);
		
		return travelTime.intValue();
	}

	@Override
	String doComplete() throws UserErrorMessage {
		Key locationKey = (Key)db.getCurrentCharacter().getProperty("locationKey");
		CachedEntity location = db.getEntity(locationKey);
		
		if (db.randomMonsterEncounter(ds, db.getCurrentCharacter(), location, 1, 0.5d))
			throw new GameStateChangeException("While you were on your way, someone found you..");
		
		
		CachedEntity path = db.getEntity(KeyFactory.createKey("Path", (Long)data.get("pathId")));
		Boolean attack = (Boolean)data.get("attack");
		if (attack==null) attack = false;
		
		CachedEntity newLocation = db.doCharacterTakePath(ds, db.getCurrentCharacter(), path, attack);

//		CombatService cs = new CombatService(db);
//		MainPageUpdateService update = new MainPageUpdateService(db, db.getCurrentUser(), db.getCurrentCharacter(), newLocation, this);
//		update.updateInBannerOverlayLinks();
//		update.updateLocationDescription();
//		update.updateLocationDirectScripts();
//		update.updateLocationJs();
//		update.updateTerritoryView();
//		update.updateActivePlayerCount();
//		update.updateLocationName();
//		update.updateButtonList(cs);

		setFullRefresh(true);
		
		return "You have arrived.";
	}

	@Override
	public Map<String, Object> getStateData() {
		Map<String, Object> result = super.getStateData();
		
		result.put("locationName", data.get("locationName"));
		
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
				data.put("attack", true);
				throw new UserErrorMessage("You are entering a territory that is restricted to you. If you continue, the defenders of this territory will attack you.<br><br><a onclick='closeAllPopups();doGoto(event,"+path.getKey().getId()+",true)'>Click here</a> to continue to advance anyway.");
			}
		}
	}


}
