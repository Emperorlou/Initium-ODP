package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.universeprojects.miniup.server.services.ModifierService;
import com.universeprojects.miniup.server.services.QuestService;
import com.universeprojects.miniup.server.services.ScriptService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class ViewItemController extends PageController {

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
		if (itemId==null) return null;
		Key itemKey = KeyFactory.createKey("Item", itemId);
		CachedEntity item = db.getEntity(itemKey); 
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
				QuestService questService = new QuestService(db);
				QuestDefEntity questDef = new QuestDefEntity(db, questDefEntity);
				QuestEntity quest = questDef.getQuestEntity(db.getCurrentCharacterKey());
				
				if (quest!=null && quest.isComplete())
					request.setAttribute("questComplete", true);
				
				request.setAttribute("newQuest", true);
				
			}
			
		}
		
		
		if (CommonChecks.checkItemIsAccessible(item, character))
			request.setAttribute("showRelatedSkills", true);
		
		request.setAttribute("isItemOwner", GameUtils.equals(item.getProperty("containerKey"), character.getKey()));
		request.setAttribute("item", getItemStats(db, character, item, false));
		request.setAttribute("itemKey", KeyFactory.keyToString(item.getKey()));
		
		ds.beginBulkWriteMode();
		
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
					comparisons.add(getItemStats(db, character, equipment, true));
			}
		}
		
		ds.commitBulkWrite();
		
		if(comparisons.isEmpty() == false)
			request.setAttribute("comparisons", comparisons);
		
		return "/WEB-INF/odppages/viewitemmini.jsp";
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
						
						List<ItemPopupEntry> curEntries = itemAspect.getItemPopupEntries();
						if(curEntries != null)
							itemPopupEntries.addAll(curEntries);
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

		
		String iconUrl = (String)item.getProperty("icon");
		if (iconUrl!=null && iconUrl.startsWith("http://"))
			iconUrl = "https://"+iconUrl.substring(7);
		else if (iconUrl!=null && iconUrl.startsWith("http")==false)
			iconUrl = "https://initium-resources.appspot.com/"+iconUrl;
		
		itemMap.put("icon", iconUrl);
		if (item.getProperty("quantity")!=null)
			itemMap.put("quantity", GameUtils.formatNumber((Long)item.getProperty("quantity")));
		
		String itemName = (String)item.getProperty("name");
		if(itemName == null) itemName = "(null)";
		String itemClass = (String)item.getProperty("itemClass");
		if(itemClass == null) itemClass = "";
		
		itemMap.put("quality", GameUtils.determineQuality(item.getProperties()));
		itemMap.put("name", itemName);
		itemMap.put("itemClass", itemClass);
		
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
			List<String> itemSlots = new ArrayList<String>();
	        List<Map<String, Object>> savedSlot = new ArrayList <Map<String, Object>>();
			
			for (int i = 0; i< (int)field; i++) 
			{
			    EmbeddedEntity currentSlot = null;
			    if(i < slotItems.size()) currentSlot = slotItems.get(i);
			    
			    if(currentSlot != null)
			    {
			        /* A caseswitch will be put here to go through
			         * all the different gem possibilities and assign
			         *  a value to slot.Name, slot.Image, etc.
			         */
			    }
			    else
			    {
			    	// Create a map to store information about the slot before saving it to the List
			        Map <String, Object> unsavedSlot = new HashMap <String, Object>();
			        String slotName = "Empty slot";
			        unsavedSlot.put("name", slotName);
			        String slotTooltip = "This is an empty slot where gems can be socketed for stat bonuses.";
			        unsavedSlot.put("tooltip", slotTooltip);
			        Boolean slotIsEmpty = true;
			        unsavedSlot.put("tooltip", slotIsEmpty);
			        savedSlot.add(unsavedSlot);
			    }
		        
			}
			itemMap.put("slots", savedSlot);zz
		}
		
		field = item.getProperty("intelligenceModifier");
		if (field!=null && field.toString().trim().equals("")==false)
		{
			requirements=true;

			itemMap.put("intmod", field.toString());
		}

		itemMap.put("requirements", requirements);
		
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
		
		
		// Modifiers
		ModifierService mService = new ModifierService(db);
		List<String> modifiers = mService.getFullModifierLines(item);
		itemMap.put("modifiers", modifiers);
		
		return itemMap;
	}
}
