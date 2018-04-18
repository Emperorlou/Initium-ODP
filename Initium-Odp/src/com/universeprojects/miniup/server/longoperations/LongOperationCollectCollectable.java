package com.universeprojects.miniup.server.longoperations;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ConfirmGenericEntityRequirementsBuilder;
import com.universeprojects.miniup.server.services.GenericEntityRequirementResult;
import com.universeprojects.miniup.server.services.MainPageUpdateService;
import com.universeprojects.miniup.server.services.ODPInventionService;
import com.universeprojects.miniup.server.services.ODPKnowledgeService;

public class LongOperationCollectCollectable extends LongOperation {

	public LongOperationCollectCollectable(ODPDBAccess db,
			Map<String, String[]> requestParameters)
			throws UserErrorMessage {
		super(db, requestParameters);
		// TODO Auto-generated constructor stub
	}

	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {
		
		if (ODPDBAccess.CHARACTER_MODE_COMBAT.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot collect stuff right now because you are currently in combat.");
		if (ODPDBAccess.CHARACTER_MODE_MERCHANT.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot collect stuff right now because you are currently vending.");
		if (ODPDBAccess.CHARACTER_MODE_TRADING.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot collect stuff right now because you are currently trading.");
		if (db.getCurrentCharacter().getProperty("mode")==null || "".equals(db.getCurrentCharacter().getProperty("mode")) || ODPDBAccess.CHARACTER_MODE_NORMAL.equals(db.getCurrentCharacter().getProperty("mode")))
		{/*We're in normal mode and so we can actually move*/}
		else
			throw new UserErrorMessage("You cannot collect stuff right now because you are busy.");
		
		if (GameUtils.isPlayerIncapacitated(db.getCurrentCharacter()))
			throw new UserErrorMessage("You're incapacitated, you can't do this right now.");
		
		
		String collectableIdStr = parameters.get("collectableId");
		CachedEntity collectable = db.getEntity("Collectable", Long.parseLong(collectableIdStr));
		if (collectable==null) throw new UserErrorMessage("This collectable no longer exists.");
		CachedEntity collectableDef = db.getEntity((Key)collectable.getProperty("_definitionKey"));

		setDataProperty("collectableId", collectable.getKey().getId());
		
		if (GameUtils.equals(collectable.getProperty("locationKey"), db.getCurrentCharacter().getProperty("locationKey"))==false)
			throw new UserErrorMessage("You cannot collect this, you're not even near it!");
		
		if ((Long)collectable.getProperty("collectionCount")<=0)
		{
			ds.delete(collectable);
			throw new UserErrorMessage("Looks like there's nothing left to collect here.");
		}
		
		Long seconds = (Long)collectable.getProperty("extractionEase");

		CachedEntity location = db.getEntity((Key)db.getCurrentCharacter().getProperty("locationKey"));
		
		
		if (collectableDef.getProperty("toolsRequired")!=null || collectableDef.getProperty("toolsOptional")!=null)
		{
			GenericEntityRequirementResult itemRequirementSlotsToItems = new ConfirmGenericEntityRequirementsBuilder("1", db, this, getPageRefreshJavascriptCall(), collectableDef)
			.addGenericEntityRequirements("Required Tools/Equipment", "toolsRequired")
			.addGenericEntityRequirements("Optional Tools/Equipment", "toolsOptional")
			.go();
			
			ODPKnowledgeService knowledgeService = db.getKnowledgeService(db.getCurrentCharacterKey());
			ODPInventionService inventionService = db.getInventionService(db.getCurrentCharacter(), knowledgeService);
			EntityPool pool = new EntityPool(db.getDB());
			
			pool.addToQueue(collectableDef.getProperty("toolsRequired"));
			pool.addToQueue(collectableDef.getProperty("toolsOptional"));
			pool.loadEntities();
			inventionService.poolGerSlotsAndSelectedItems(pool, collectableDef, itemRequirementSlotsToItems.slots);
			pool.loadEntities();
			
			Map<Key, List<Key>> tools = inventionService.resolveGerSlotsToGers(pool, collectableDef, itemRequirementSlotsToItems.slots, 1);
			
			Map<String,Object> processVariables = new HashMap<>();
			processVariables.put("speed",  seconds);
			
			inventionService.beginCollectableProcess(collectableDef, tools, processVariables, pool);
			
			seconds = (Long)processVariables.get("speed");
			
			setDataProperty("tools", tools);
		}
		
		int monsterTries = seconds.intValue()/30;
		if (db.randomMonsterEncounter(ds, db.getCurrentCharacter(), location, monsterTries, 0.25d))
			throw new GameStateChangeException("While you were working, someone found you..");
		
		setDataProperty("secondsToWait", seconds);
		setDataProperty("bannerUrl", collectable.getProperty("bannerUrl"));
		
		return seconds.intValue();
	}

	@Override
	String doComplete() throws UserErrorMessage {
		Long collectableId = (Long)getDataProperty("collectableId");
		CachedEntity collectable = db.getEntity("Collectable", collectableId);
		CachedEntity collectableDef = db.getEntity((Key)collectable.getProperty("_definitionKey"));

		Map<Key, List<Key>> tools = (Map<Key, List<Key>>)getDataProperty("tools");

		CachedEntity location = db.getEntity((Key)db.getCurrentCharacter().getProperty("locationKey"));
		
		
		// Now update the collectable..
		Long collectionCount = (Long)collectable.getProperty("collectionCount");
		collectionCount--;
		if (collectionCount<=0)
		{
			// Delete the collectable 
			ds.delete(collectable);
		}
		else
		{
			collectable.setProperty("collectionCount", collectionCount);
			
			ds.put(collectable);
		}
		
		
		CachedEntity itemDef = db.getEntity((Key)collectableDef.getProperty("itemDefKey"));
		
		if (itemDef==null)
			throw new RuntimeException("ItemDef is null on the collectable definition: "+collectableDef.getKey());
		
		CachedEntity item = db.generateNewObject(itemDef, "Item");

		ODPKnowledgeService knowledgeService = db.getKnowledgeService(db.getCurrentCharacterKey());
		ODPInventionService inventionService = db.getInventionService(db.getCurrentCharacter(), knowledgeService);
		if (collectableDef.getProperty("toolsRequired")!=null || collectableDef.getProperty("toolsOptional")!=null)
		{
			EntityPool pool = new EntityPool(db.getDB());
			
			inventionService.poolItemRequirementsToItems(pool, tools);

			inventionService.processCollectableResult(pool, collectableDef, tools, item, 1);

			
		}		
		knowledgeService.increaseKnowledgeFor(item, 1, 5);

		// Finish off some of those properties..

		// Check if the character is able to carry the thing or if it's too heavy...
		Long currentCarryWeight = db.getCharacterCarryingWeight(db.getCurrentCharacter());
		Long maxCarryWeight = db.getCharacterMaxCarryingWeight(db.getCurrentCharacter());
		Long itemWeight = db.getItemWeight(item);
		boolean onGround = false;
		if (currentCarryWeight+itemWeight>maxCarryWeight)
		{
			item.setProperty("containerKey", db.getCurrentCharacter().getProperty("locationKey"));
			onGround = true;
		}
		else
			item.setProperty("containerKey", db.getCurrentCharacter().getKey());
		
		item.setProperty("movedTimestamp", new Date());
		

		if (CommonChecks.isItemImmovable(item))
		{
			item.setProperty("movedTimestamp", new Date(3000, 1, 1));
			item.setProperty("containerKey", location.getKey());
		}
		
		ds.beginBulkWriteMode();
		
		db.combineStackedItemWithFirstStack(item, db.getCurrentCharacter().getKey());
		ds.put(item);
		
		ds.commitBulkWrite();
		
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, db.getCurrentUser(), db.getCurrentCharacter(), location, this);
		mpus.updateCollectablesView();
		mpus.updateLocationQuicklist();
		
		if (CommonChecks.isItemImmovable(item))
		{
			mpus.updateImmovablesPanel(item);
			return "You collected "+GameUtils.renderItem(item)+". It is now in the location where you are standing.";
		}
		else if (onGround)
			return "You got "+GameUtils.renderItem(item)+" however it was too heavy to hold and it is laying on the ground where you stand.";
		else
			return "You collected "+GameUtils.renderItem(item)+".";
			
	}

	@Override
	public String getPageRefreshJavascriptCall() {
		Long collectableId = (Long)getDataProperty("collectableId");
		return "doCollectCollectable(null, "+collectableId+", '1');";
	}

	@Override
	public Map<String, Object> getStateData() {
		Map<String, Object> stateData = super.getStateData();
		
		stateData.put("secondsToWait", getDataProperty("secondsToWait"));
		
		return stateData;
	}

	
	
}
