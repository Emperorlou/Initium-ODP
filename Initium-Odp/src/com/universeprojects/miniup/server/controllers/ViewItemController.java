package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumAspect;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.ItemAspect;
import com.universeprojects.miniup.server.ItemAspect.ItemPopupEntry;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.dbentities.QuestDefEntity;
import com.universeprojects.miniup.server.dbentities.QuestEntity;
import com.universeprojects.miniup.server.scripting.events.SimpleEvent;
import com.universeprojects.miniup.server.services.ContainerService;
import com.universeprojects.miniup.server.services.GridMapService;
import com.universeprojects.miniup.server.services.ModifierService;
import com.universeprojects.miniup.server.services.QuestService;
import com.universeprojects.miniup.server.services.ScriptService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class ViewItemController extends PageController {

	public String[] toolStatNames = new String[] {
			"Strength",
			"Complexity",
			"Ease Of Use",
			"Efficiency",
			"Precision",
			"Reliability",
			"Beauty",
	};
	
	// This is a list of stat names where being more negative is better
	public HashSet<String> negativeStatIsGood_Names = new HashSet<>(Arrays.asList(new String[] {
		"strReq",
		"dexpen",
		"weight",
		"space"
	}));
	
	public ViewItemController() {
		super("viewitemmini");
	}

	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}

		response.setHeader("Access-Control-Allow-Origin", "*");     // This is absolutely necessary for phonegap to work

	    CachedDatastoreService ds = db.getDB();
	    CachedEntity character = db.getCurrentCharacter(); 
	    
	    Long itemId = WebUtils.getLongParam(request, "itemId");
	    String proceduralKey = WebUtils.getStrParam(request, "proceduralKey");
	    CachedEntity item = null;
		if (itemId!=null)
		{
			Key itemKey = KeyFactory.createKey("Item", itemId);
			item = db.getEntity(itemKey); 
		}
		else if (proceduralKey!=null)
		{
			item = GridMapService.generateSingleItemFromProceduralKey(db, db.getCharacterLocation(character), proceduralKey);
		}
		if (item==null)
		{
			response.sendError(404);
			return null;
		}
		
		if (CommonChecks.checkItemIsNatural(item))
		{
			return null;
		}
		
		if(character == null)
		{
			return null;
		}
		
		request.setAttribute("itemId", itemId);
		
		// This is a special case for premium tokens...
		if (CommonChecks.checkItemIsPremiumToken(item))
			request.setAttribute("showPremiumTokenUI", true);
		
		if (CommonChecks.checkItemIsChippedToken(item))
			request.setAttribute("showChippedTokenUI", true);

		if (item.getProperty("newQuest")!=null)
		{
			CachedEntity questDefEntity = db.getEntity((Key)item.getProperty("newQuest"));
			if (questDefEntity!=null)
			{
//				QuestService questService = new QuestService(db, db.getCurrentCharacter());
				QuestDefEntity questDef = new QuestDefEntity(db, questDefEntity);
				QuestEntity quest = questDef.getQuestEntity(db.getCurrentCharacterKey());
				
				if (quest!=null && quest.isComplete())
					request.setAttribute("questComplete", true);
				
				request.setAttribute("newQuest", true);
				
			}
			
		}
		
		
		if (CommonChecks.checkItemIsAccessible(item, character))
			request.setAttribute("showRelatedSkills", true);

		ds.beginBulkWriteMode();
		
		request.setAttribute("isItemOwner", GameUtils.equals(item.getProperty("containerKey"), character.getKey()));
		Map<String, Object> itemStats = getItemStats(db, character, item, false);
		request.setAttribute("item", itemStats);
		if (item.getKey().isComplete())
			request.setAttribute("itemKey", KeyFactory.keyToString(item.getKey()));
		else
			request.setAttribute("proceduralKey", proceduralKey);
			
		
		
		List<CachedEntity> comparisonEquipment = new ArrayList<>();
		List<Map<String,Object>> comparisons = new ArrayList<Map<String,Object>>();
		String equipSlot = (String)item.getProperty("equipSlot");
		if (equipSlot!=null)
		{
			if (equipSlot.equals("2Hands")) equipSlot = "LeftHand and RightHand";
			String[] equipSlots = equipSlot.split("(,| and )");
			for(String slot:equipSlots)
			{
				CachedEntity equipment = db.getEntity((Key)character.getProperty("equipment"+slot));
				if (equipment!=null)
				{
					comparisonEquipment.add(equipment);
					comparisons.add(getItemStats(db, character, equipment, true));
				}
			}
		}
		
		ds.commitBulkWrite();
		
		if(comparisons.isEmpty() == false)
		{
			request.setAttribute("comparisons", comparisons);
			
			Map<String,String> statComparisons = generateInlineStatComparisons(item, comparisonEquipment, itemStats, comparisons);
			
			request.setAttribute("statComp", statComparisons);
			
		}
		
		return "/WEB-INF/odppages/viewitemmini.jsp";
	}
	private Map<String, String> generateInlineStatComparisons(CachedEntity item, List<CachedEntity> comparisonEquipment, Map<String, Object> itemStats,
			List<Map<String, Object>> comparisons)
	{
		for(int i = comparisonEquipment.size()-1; i>=0; i--)
		{
			if (GameUtils.equals(item.getProperty("itemType"), comparisonEquipment.get(i).getProperty("itemType")))
			{
				Map<String, Object> compStats = comparisons.get(i);
				Map<String, String> deltaStats = new HashMap<>();
				for(String fieldName:itemStats.keySet())
				{
					Double itemStatValue = null;
					Double compStatValue = null;
					try
					{
						itemStatValue = Double.parseDouble((String)itemStats.get(fieldName));
						compStatValue = Double.parseDouble((String)compStats.get(fieldName));
					}
					catch(Exception e)
					{
						continue;
					}
					
					double delta = itemStatValue-compStatValue;
					
					String value = GameUtils.formatNumber(delta);
					if (delta==0) continue;
					if (delta>0) 
					{
						if (negativeStatIsGood_Names.contains(fieldName))
							value = "<span class='comparestat-bad'>+"+value+"</span>";
						else
							value = "<span class='comparestat-good'>+"+value+"</span>";
					}
					else
					{
						if (negativeStatIsGood_Names.contains(fieldName))
							value = "<span class='comparestat-good'>"+value+"</span>";
						else
							value = "<span class='comparestat-bad'>"+value+"</span>";
					}
					deltaStats.put(fieldName, value);
				}
				return deltaStats;
			}
		}
		return new HashMap<>();
	}


	/**
	 * Returns a map of stat:data for the item.
	 * @param item
	 * @param isComparison
	 * @return
	 */
	private Map<String,Object> getItemStats(ODPDBAccess db, CachedEntity currentChar, CachedEntity item, boolean isComparisonItem)
	{
		if(item == null) return null;
		
		if (new InitiumObject(db, item).update()==true)   // Honestly the whole controller should be using InitiumObject instead of CachedEntity
			db.getDB().put(item);
			
		Map<String,Object> itemMap = new HashMap<String,Object>();
		itemMap.put("itemId", item.getId());
		
		boolean selfUser = GameUtils.equals(currentChar.getKey(), item.getProperty("containerKey"));
		Object field = null;
		if(!isComparisonItem)
		{
			List<ItemPopupEntry> itemPopupEntries = new ArrayList<ItemPopupEntry>();
			StringBuilder aspectList = new StringBuilder();
			
			// Self user: scripts, owner only HTML, aspects, and premium token
			if(selfUser)
			{
				// Get all the directItem scripts on this item 
				@SuppressWarnings("unchecked")
				List<Key> scriptKeys = (List<Key>)item.getProperty("scripts");
				if (scriptKeys!=null && scriptKeys.isEmpty()==false)
				{
					List<CachedEntity> directItemScripts = db.getScriptsOfType(scriptKeys, 
							ODPDBAccess.ScriptType.directItem, ODPDBAccess.ScriptType.ownerHtml);
					if (directItemScripts!=null && directItemScripts.isEmpty()==false)
					{
						boolean ownerHtmlSpecified = false;
						for(CachedEntity script:directItemScripts)
						{
							if(GameUtils.booleanEquals(script.getProperty("hidden"), true)) continue;
							Object scriptType = script.getProperty("type");
							if(GameUtils.enumEquals(scriptType, ODPDBAccess.ScriptType.directItem))
							{
								ItemPopupEntry scriptEntry = new ItemPopupEntry(
										(String)script.getProperty("caption"), 
										(String)script.getProperty("description"), 
										"doTriggerItem(event,"+script.getId()+","+item.getId()+")");
								
								itemPopupEntries.add(scriptEntry);
							}
							else if(ownerHtmlSpecified == false && GameUtils.enumEquals(scriptType, ODPDBAccess.ScriptType.ownerHtml))
							{
								SimpleEvent scriptEvent = new SimpleEvent(currentChar, db);
								if(ScriptService.getScriptService(db).executeScript(scriptEvent, script, item) && scriptEvent.getAttribute("ownerOnlyHtml") != null)
								{
									ownerHtmlSpecified = true;
									itemMap.put("ownerOnlyHtml", scriptEvent.getAttribute("ownerOnlyHtml"));
								}
							}
						}
					}
				}
				
				// Owner only HTML
				field = item.getProperty("ownerOnlyHtml");
				if(field != null && "".equals(field)==false && itemMap.containsKey("ownerOnlyHtml") == false)
					itemMap.put("ownerOnlyHtml", field.toString());
			}
			
			//If the item is accessible, generate custom HTML.
			if(CommonChecks.checkItemIsAccessible(item, currentChar)) {
				
				Object reachableHtml = item.getProperty("reachableHtml");
				
				//if its empty, check for the scripts.
				if(reachableHtml == null || "".equals(reachableHtml) == false) {
					
					@SuppressWarnings("unchecked")
					List<Key> scriptKeys = (List<Key>) item.getProperty("scripts");
					
					if (scriptKeys!=null && scriptKeys.isEmpty()==false) {
						List<CachedEntity> reachableHtmlScripts = db.getScriptsOfType(scriptKeys, ODPDBAccess.ScriptType.reachableHtml);
						
						if (reachableHtmlScripts!=null && reachableHtmlScripts.isEmpty()==false){
							for(CachedEntity script : reachableHtmlScripts) {
								
								if(GameUtils.booleanEquals(script.getProperty("hidden"), true)) continue;
								
								
								SimpleEvent event = new SimpleEvent(currentChar, db);
								if(ScriptService.getScriptService(db).executeScript(event, script, item) && event.getAttribute("reachableHtml") != null)
								{
									itemMap.put("reachableHtml", event.getAttribute("reachableHtml"));
								}
							}
						}
					}
					
					if(itemMap.containsKey("reachableHtml") == false) {
						itemMap.put("reachableHtml", null);
					}
				}
				else {
					itemMap.put("reachableHtml", reachableHtml);
				}
			}
			
			// Aspects
			InitiumObject iObject = new InitiumObject(db, item);
			if (iObject.hasAspects())
			{
				// Go through the aspects on this item and include any special links that it may have
				for(InitiumAspect initiumAspect:iObject.getAspects())
				{
					if (initiumAspect instanceof ItemAspect)
					{
						ItemAspect itemAspect = (ItemAspect)initiumAspect;
						
						String popupTag = itemAspect.getPopupTag();
						if (popupTag!=null)
						{
							if (aspectList.length()>0)
							{
								aspectList.append(", ");
								aspectList.append(popupTag.toLowerCase());
							}
							else
								aspectList.append(popupTag);
						}
						
						List<ItemPopupEntry> curEntries = itemAspect.getItemPopupEntries(currentChar);
						if(curEntries != null)
						{
							curEntries.removeAll(Collections.singleton(null));
							itemPopupEntries.addAll(curEntries);
						}
					}
				}
				
				if(aspectList.length() > 0)
					itemMap.put("aspectList", aspectList.toString());
			}
			
			// Here, since this can technically contain both directItem scripts and
			// ItemAspect popup entries.
			if(itemPopupEntries.isEmpty()==false)
			{
				List<Map<String,String>> popupEntries = new ArrayList<Map<String,String>>();
				for(ItemPopupEntry entry:itemPopupEntries)
				{
					Map<String, String> formattedEntry = new HashMap<String, String>();
					formattedEntry.put("name", entry.name);
					formattedEntry.put("description", entry.description);
					formattedEntry.put("clickJavascript", entry.clickJavascript);
					popupEntries.add(formattedEntry);
				}
				itemMap.put("popupEntries", popupEntries);
			}
		}
		
		
		if(GameUtils.isStorageItem(item))
		{
			ContainerService cs = new ContainerService(db);
			itemMap.put("isContainer", cs.checkContainerAccessAllowed(currentChar, item));
		}
		else
			itemMap.put("isContainer", false);

		
		boolean isGridMapObjectImage = false;
		String iconUrl = GameUtils.getResourceUrl(item.getProperty("icon"));
		if (iconUrl==null)
		{
			// Check if we can get the image some other way...
			iconUrl = GameUtils.getResourceUrl(item.getProperty("GridMapObject:image"));
			if (iconUrl!=null)
				isGridMapObjectImage = true;
		}
		
		
		itemMap.put("isGridMapObjectImage", isGridMapObjectImage);
		itemMap.put("icon", iconUrl);
		if (item.getProperty("quantity")!=null)
			itemMap.put("quantity", GameUtils.formatNumber((Long)item.getProperty("quantity")));
		
		String itemName = (String)item.getProperty("name");
		if(itemName == null) itemName = "(null)";
		String itemClass = (String)item.getProperty("itemClass");
		if(itemClass == null) itemClass = "";
		String itemQuality = GameUtils.determineQuality(item.getProperties());
		if(itemQuality == null) itemQuality = "";
		if(CommonChecks.checkIsHardcore(item)) itemQuality = (itemQuality + " hardcore").trim();
		
		itemMap.put("quality", itemQuality);
		itemMap.put("name", itemName);
		itemMap.put("itemClass", itemClass);

		// Buff stuff
		List<EmbeddedEntity> buffs = db.getBuffsFor(item);
		if (buffs!=null && buffs.isEmpty()==false)
			itemMap.put("buffs", true);
		
		// Printing buffs
		StringBuilder sb = new StringBuilder();
		
		for(EmbeddedEntity buff:buffs)
		{
			String iUrl = GameUtils.getResourceUrl(buff.getProperty("icon"));
			sb.append("<img src='" + iUrl + "' border='0'>");
		}
		itemMap.put("printBuff", sb.toString());
		
		// Buff list
		itemMap.put("buffList", GameUtils.renderBuffsList(buffs));	
		
		
		
		String itemSlot = (String)item.getProperty("equipSlot");
		String itemType = (String)item.getProperty("itemType");
		if(itemType == null) itemType = "";
		// If itemSlot is null, then we only want to use itemType, otherwise use both.
		if(itemSlot == null || "".equals(itemSlot)) itemSlot = itemType;
		else if("".equals(itemType) == false) itemSlot = itemSlot + " " + itemType; 
		
		itemMap.put("itemSlot", itemSlot);

		boolean requirements = false;
		field = item.getProperty("strengthRequirement");
		if (field!=null && field.toString().trim().equals("")==false)
		{
			requirements=true;
			itemMap.put("strReq", GameUtils.formatNumber(field));
		}
		
		field = item.getProperty("dexterityPenalty");
		if (field!=null && field.toString().trim().equals("")==false)
		{
			requirements=true;

			itemMap.put("dexpen", field.toString());
		}
		
		field = item.getProperty("strengthModifier");
		if (field!=null && field.toString().trim().equals("")==false)
		{
			requirements=true;
			
			itemMap.put("strmod", field.toString());
		}
		
		field = item.getProperty("Slotted:maxCount");
		if (field!=null && field.toString().trim().equals("")==false)
		{
			requirements=true;
			
			List<EmbeddedEntity> slotItems = (List<EmbeddedEntity>) item.getProperty("Slotted:slotItems");
	        List<Map<String, Object>> savedSlot = new ArrayList <Map<String, Object>>();
			ModifierService mService = new ModifierService(db);
			
	        
	        
			for (int i = 0; i< (long)field; i++) 
			{
			    EmbeddedEntity currentSlot = null;
			    if(slotItems!=null && i < slotItems.size()) currentSlot = slotItems.get(i);
			    
			    if(currentSlot != null)
			    {
			    	Map <String, Object> unsavedSlot = new HashMap <String, Object>();
			    	String slotModifierText = new String();
			    	Object slotName = currentSlot.getProperty("name");
			    	unsavedSlot.put("slotName", slotName);
			    	
			    	Object slotIcon = GameUtils.getResourceUrl(currentSlot.getProperty("icon"));
			    	unsavedSlot.put("slotIcon", slotIcon);
			    	
			    	List<String> slotModifiers = mService.getFullModifierLines(currentSlot);
			    	
			    	if(slotModifiers!=null) {
			    	
			    	for (int track = 0; track< slotModifiers.size(); track++) {
			    		
			    		String unsavedModifier = slotModifiers.get(track);
			    		slotModifierText = unsavedModifier + "<br>";
			    		
			    	}
			    	}
			    	unsavedSlot.put("slotModifiers", slotModifierText);
			    	
			        unsavedSlot.put("slotIsEmpty", false);
			        savedSlot.add(unsavedSlot);
			    }
			    else
			    {
			    	// Create a map to store information about the slot before saving it to the List
			        Map <String, Object> unsavedSlot = new HashMap <String, Object>();
			        String slotName = "Empty slot";
			        unsavedSlot.put("slotName", slotName);
			        String slotTooltip = "This is an empty slot where gems can be socketed for stat bonuses.";
			        unsavedSlot.put("slotTooltip", slotTooltip);
			        
			        unsavedSlot.put("slotIsEmpty", true);
			        savedSlot.add(unsavedSlot);
			    }
		        
			}
			itemMap.put("slots", savedSlot);
		}
		
		field = item.getProperty("intelligenceModifier");
		if (field!=null && field.toString().trim().equals("")==false)
		{
			requirements=true;

			itemMap.put("intmod", field.toString());
		}

		itemMap.put("requirements", requirements);
		
		// For tools
		boolean hasToolStats = false;
		for(String nameRaw:toolStatNames)
		{
			String name = nameRaw.replaceAll(" ", "");
			String fieldName = "attribute"+name;
			field = item.getProperty(fieldName);
			if (field!=null && field.toString().trim().equals("")==false)
			{
				hasToolStats = true;
				itemMap.put(name, GameUtils.formatNumber(field));
			}
		}	
		itemMap.put("hasToolStats", hasToolStats);
		
		
		// For Weapons
		field = item.getProperty("weaponDamage");
		if (field!=null && field.toString().trim().equals("")==false)
		{
			itemMap.put("weaponDamage", field.toString().substring(2));
		}
		
		field = item.getProperty("weaponDamageCriticalChance");
		if (field!=null && field.toString().trim().equals("")==false)
		{
			Double critChance = Double.parseDouble(field.toString());
			if (currentChar!=null && currentChar.getProperty("intelligence") instanceof Double)
				critChance += (db.getCharacterIntelligence(currentChar)-4d)*2.5d;
			
			
			itemMap.put("weaponCriticalChance", GameUtils.formatNumber(Double.parseDouble(field.toString())));
			itemMap.put("weaponCriticalChanceBuffed", GameUtils.formatNumber(critChance));
		}
		
		field = item.getProperty("weaponDamageCriticalMultiplier");
		if (field!=null && field.toString().trim().equals("")==false)
			itemMap.put("weaponCriticalMultiplier", Double.parseDouble(field.toString()));
		
		field = item.getProperty("weaponDamageType");
		if (field!=null && field.toString().trim().equals("")==false)
			itemMap.put("weaponDamageType", field.toString());
		
		if (item.getProperty("weaponDamage")!=null && item.getProperty("weaponDamage").toString().length()>3)
		{
			Double weaponAverageDamage = GameUtils.getWeaponAverageDamage(item);
			Double weaponMaxDamage = GameUtils.getWeaponMaxDamage(item);
			
			itemMap.put("weaponDamageMax", GameUtils.formatNumber(weaponMaxDamage));
			itemMap.put("weaponDamageAvg", GameUtils.formatNumber(weaponAverageDamage));
		}
		
		// For armors
		field = item.getProperty("blockChance");
		Long blockChance = (Long)field;
		if (field!=null && field.toString().trim().equals("")==false)
			itemMap.put("blockChance", field.toString());
		
		// If there is a block chance AND the damage reduction is null/blank, default DR is 10
		if (blockChance != null && (item.getProperty("damageReduction")==null || field.toString().trim().equals("")))
			field = 10l;
		else
			field = item.getProperty("damageReduction");
		if (field!=null && field.toString().trim().equals("")==false)
			itemMap.put("damageReduction", field.toString());
		
		// If there is a block chance AND the damage reduction is null/blank, default DR is 10
		if (blockChance != null && (item.getProperty("blockBludgeoningCapability")==null || field.toString().trim().equals("")))
			field = "Average";
		else
			field = item.getProperty("blockBludgeoningCapability");
		if (field!=null && field.toString().trim().equals("")==false)
			itemMap.put("blockBludgeoningCapability", field.toString());

		// If there is a block chance AND the damage reduction is null/blank, default DR is 10
		if (blockChance != null && (item.getProperty("blockPiercingCapability")==null || field.toString().trim().equals("")))
			field = "Average";
		else
			field = item.getProperty("blockPiercingCapability");
		if (field!=null && field.toString().trim().equals("")==false)
			itemMap.put("blockPiercingCapability", field.toString());
		
		// If there is a block chance AND the damage reduction is null/blank, default DR is 10
		if (blockChance != null && (item.getProperty("blockSlashingCapability")==null || field.toString().trim().equals("")))
			field = "Average";
		else
			field = item.getProperty("blockSlashingCapability");
		if (field!=null && field.toString().trim().equals("")==false)
			itemMap.put("blockSlashingCapability", field.toString());
		
		
		
		// For storage items
		field = item.getProperty("maxSpace");
		if (field!=null && field.toString().trim().equals("")==false)
		{
			String result = "";
			Long maxSpace = (Long)field;
			if (maxSpace>=28316.8)
			{
				result = GameUtils.formatNumber(maxSpace/28316.8d)+" ft&#179;";
			}
			else
			{
				result = GameUtils.formatNumber(maxSpace)+" cc";
			}
			itemMap.put("maxSpace", result);
		}
		
		field = item.getProperty("maxWeight");
		if (field!=null && field.toString().trim().equals("")==false)
		{
			String result = "";
			Long maxWeight = (Long)field;
			if (maxWeight>=1000)
			{
				result = GameUtils.formatNumber(maxWeight/1000d)+" kg";
			}
			else
			{
				result = GameUtils.formatNumber(maxWeight)+" g";
			}
			itemMap.put("maxWeight", result);
		}
		
		field=null;
		if (item.getProperty("weight")!=null)
			field = db.getItemWeight(item);
		if (field!=null && field.toString().trim().equals("")==false)
		{
			String result = "";
			Long weight = (Long)field;
			if (weight>=1000)
			{
				result = GameUtils.formatNumber(weight/1000d)+"&nbsp;kg";
			}
			else
			{
				result = GameUtils.formatNumber(weight)+"&nbsp;g";
			}
			itemMap.put("weight", result);
		}

		field=null;
		if (item.getProperty("space")!=null)
			field = db.getItemSpace(item);
		if (field!=null && field.toString().trim().equals("")==false)
		{
			String result = "";
			Long space = (Long)field;
			if (space>=28316.8)
			{
				result = GameUtils.formatNumber(space/28316.8d)+"&nbsp;ft&#179;";
			}
			else
			{
				result = GameUtils.formatNumber(space)+"&nbsp;cc";
			}
			itemMap.put("space", result);
		}
		
		field = item.getProperty("warmth");
		if (field!=null && field.toString().trim().equals("")==false)
			itemMap.put("warmth", field.toString());
		
		field = item.getProperty("durability");
		Object fieldMax = item.getProperty("maxDurability");
		if (field!=null && field.toString().trim().equals("")==false)
			itemMap.put("durability", field + "/" + fieldMax);
		
		field = item.getProperty("description");
		if (field!=null && field.toString().trim().equals("")==false)
			itemMap.put("description", field.toString());
		
		//If we have explicitely allowed charges to be visible on this item, display them.
		Boolean chargesVisible = (Boolean) item.getProperty("chargesVisible");
		if(chargesVisible == null) chargesVisible = false;
		if(chargesVisible) {
			field = item.getProperty("charges");
			if(field != null && field.toString().trim().equals("") == false) {
				itemMap.put("charges", field.toString());
			}
		}
		
		
		// Modifiers
		ModifierService mService = new ModifierService(db);
		List<String> modifiers = mService.getFullModifierLines(item);

		// Also buff effects are shown in the same format as modifiers
		List<String> buffEffects = db.getBuffEffectsFor(item);
		if (modifiers==null) modifiers = new ArrayList<>();
		if (buffEffects!=null) modifiers.addAll(buffEffects);
		
		itemMap.put("modifiers", modifiers);
		
		return itemMap;
	}
}
