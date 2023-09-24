package com.universeprojects.miniup;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumAspect;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.GroupStatus;
import com.universeprojects.miniup.server.ODPDBAccess.LocationSubType;
import com.universeprojects.miniup.server.aspects.AspectGridMapObject;

public abstract class CommonChecks
{

	/**
	 * Returns true if the given idea definition is active and available for use or not.
	 * 
	 * @param db
	 * @param ideaDef
	 * @return
	 */
	public static boolean checkIdeaIsLive(ODPDBAccess db, CachedEntity ideaDef)
	{
		String mode = (String)ideaDef.getProperty("mode");
		if (mode==null) return false;

		if (GameUtils.isTestServer(db.getRequest()))
		{
			if (mode.equals("Live") || mode.equals("Test"))
				 return true;
		}
		
		return mode.equals("Live");
	}
	
	/**
	 * Checks if the character is not currently trading, in combat, manning a store, unconscious/dead, or anything else that would cause the character to be otherwise occupied.
	 * 
	 * @param character
	 * @return True if not busy
	 */
	public static boolean checkCharacterIsBusy(CachedEntity character)
	{
		if (character.getProperty("mode")==null || "NORMAL".equals(character.getProperty("mode")))
			return false;
		
		return true;
	}
	
	/**
	 * Checks if a character is dead.
	 * 
	 * @param character
	 * @return
	 */
	public static boolean checkCharacterIsDead(CachedEntity character)
	{
		Double hitpoints = (Double)character.getProperty("hitpoints");
		if (hitpoints <= 0 && ("DEAD".equals(character.getProperty("mode")))) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if a character is zombified.
	 * @param character
	 * @return
	 */
	public static boolean checkCharacterIsZombie(CachedEntity character) {
		return "Zombie".equals(character.getProperty("status"));
	}
	
	/**
	 * Checks if a character is unconscious. 
	 * 
	 * This will return false if the character is actually dead.
	 * 
	 * @param character
	 * @return
	 */
	public static boolean checkCharacterIsUnconscious(CachedEntity character)
	{
		Double hitpoints = (Double)character.getProperty("hitpoints");
		if (hitpoints<=0 && "DEAD".equals(character.getProperty("mode"))==false)
			return true;
		
		return false;
	}
	
	/**
	 * Checks if the character is currently in an active trade.
	 * 
	 * @param character
	 * @return
	 */
	public static boolean checkCharacterIsTrading(CachedEntity character)
	{
		if ("TRADING".equals(character.getProperty("mode")))
			return true;
		
		return false;
	}
	
	/**
	 * Checks if the character is currently vending.
	 * 
	 * @param character
	 * @return
	 */
	public static boolean checkCharacterIsVending(CachedEntity character)
	{
		if ("MERCHANT".equals(character.getProperty("mode")))
			return true;
		
		return false;
	}
	
	/**
	 * Checks if the character entity has RaidBoss status, to allow multiple combatants
	 * @param character Character entity of the mob.
	 * @return
	 */
	public static boolean checkCharacterIsRaidBoss(CachedEntity character)
	{
		if("RaidBoss".equals(character.getProperty("status")))
			return true;
		
		return false;
	}
	
	/**
	 * Checks if the given item is a valid premium membership token.
	 * 
	 * @param item
	 * @return
	 */
	public static boolean checkItemIsPremiumToken(CachedEntity item)
	{
		if ("Initium Premium Membership".equals(item.getProperty("name")))
			return true;
		
		return false;
	}
	
	/**
	 * Checks if the given item is a valid premium membership token.
	 * 
	 * @param item
	 * @return
	 */
	public static boolean checkItemIsChippedToken(CachedEntity item)
	{
		if ("Chipped Token".equals(item.getProperty("premiumTokenType")))
			return true;
		
		
		return false;
	}
	
	/**
	 * Checks if the given item is actually in the inventory of the given character.
	 * 
	 * @param item
	 * @param characterKey
	 * @return
	 */
	public static boolean checkItemIsInCharacterInventory(CachedEntity item, Key characterKey)
	{
		if (characterKey==null) throw new IllegalArgumentException("characterKey cannot be null.");
		if (item==null) throw new IllegalArgumentException("item cannot be null.");
		if (characterKey.getKind().equals("Character")==false)
			throw new IllegalArgumentException("characterKey is not an actual character.");
		
		if (characterKey.equals(item.getProperty("containerKey")))
			return true;
		
		return false;
	}
	
	/**
	 * Checks if the given item is equippable to a slot. Ignores strength requirements.
	 * @param item
	 * @return
	 */
	public static boolean checkItemIsEquippable(CachedEntity item)
	{
		String slot = (String)item.getProperty("equipSlot");
		return slot != null && "".equals(slot) == false;
	}
	
	/**
	 * Checks if the item can be picked up and/or moved or if it's a permanent feature of wherever it was placed.
	 * 
	 * @param item
	 * @return
	 */
	public static boolean checkItemIsMovable(CachedEntity item)
	{
		if (Boolean.TRUE.equals(item.getProperty("immovable")))
			return false;
		
		if ("Attached".equals(item.getProperty("GridMapObject:mode")))
			return false;
		
		return true;
	}
	
	/**
	 * Checks if the given character is a member of the group he is marked as being in. If he is not part
	 * of a group, or is not yet an official member of it (so, excluding new applications, kicked members..etc)
	 * then this method will return false. Otherwise if a character is a member of their group, it will return true.
	 * 
	 * @param character
	 * @return
	 */
	public static boolean checkCharacterIsMemberOfHisGroup(CachedEntity character)
	{
		String charGroupStatus = (String)character.getProperty("groupStatus");
		if (charGroupStatus==null) return false;
		
		if(character.getProperty("groupKey") != null &&
				(GameUtils.enumEquals(charGroupStatus, GroupStatus.Admin) ||
				 GameUtils.enumEquals(charGroupStatus, GroupStatus.Member)))
		{
			return true;
		}
		
		return false;
	}

	/**
	 * Checks that the idea actually belongs to the given character. 
	 * 
	 * @param idea
	 * @param characterKey
	 * @return
	 */
	public static boolean checkIdeaIsCharacters(CachedEntity idea, Key characterKey)
	{
		if (GameUtils.equals(characterKey, idea.getProperty("characterKey")))
			return true;
		
		return false;
	}

	/**
	 * Checks if the given character is currently in combat.
	 * 
	 * @param character
	 * @return
	 */
	public static boolean checkCharacterIsInCombat(CachedEntity character)
	{
		
		if ("COMBAT".equals(character.getProperty("mode")))
		{
			if (CommonChecks.checkCharacterIsRaidBoss(character) == false &&
					character.getProperty("combatant")==null)
			{
				character.setProperty("mode", "NORMAL");
				return false;
			}
			return true;
		}
		
		return false;
	}

	/**
	 * Checks if the given location is considered a combat site.
	 * 
	 * @param location
	 * @return
	 */
	public static boolean checkLocationIsCombatSite(CachedEntity location)
	{
		if (location != null && "CombatSite".equals(location.getProperty("type")) && 
				(GameUtils.equals(location.getProperty("subType"), null) || GameUtils.enumEquals(location.getProperty("subType"), LocationSubType.CombatSite)))
			return true;
		
		return false;
	}

	/**
	 * Checks if the given location is considered a camp site.
	 * 
	 * @param location
	 * @return
	 */
	public static boolean checkLocationIsCampSite(CachedEntity location)
	{
		if (location != null && "CampSite".equals(location.getProperty("type")))
			return true;
		
		return false;
	}
	
	/**
	 * Checks if the given slot exists in the list of available equipment slots.
	 * 
	 * @param slot
	 * @return
	 */
	public static boolean checkIsValidEquipSlot(String slot)
	{
		if(slot == null || slot.isEmpty()) return false;
		
		for(String str:ODPDBAccess.EQUIPMENT_SLOTS)
			if(slot.equals(str))
				return true;
		
		return false;
	}
	
	public static boolean checkItemHasEquipSlot(CachedEntity item, String slot)
	{
		String equipSlot = (String)item.getProperty("equipSlot");
		if(equipSlot == null || equipSlot == "") return false;
		
		for(String checkSlot:equipSlot.replaceAll(" and ", ",").replaceAll(" ", "").split(","))
			if(slot.equals(checkSlot))
				return true;
		
		return false;
	}

	public static boolean checkUserIsPremium(CachedEntity user)
	{
		if (user==null) return false;
		Boolean isPremium = Boolean.TRUE.equals(user.getProperty("premium"));
		return isPremium;
	}

	public static boolean checkItemIsClass(CachedEntity item, String className)
	{
		Object itemClassObj = item.getProperty("itemClass");
		if (itemClassObj==null) return false;
		else if (itemClassObj instanceof Key)
		{
			if (className.equals(((Key)itemClassObj).getName()))
				return true;
		}
		else if (itemClassObj instanceof String)
		{
			if (className.equals(itemClassObj))
				return true;
		}
		return false;
	}

	public static boolean checkIsRaining(CachedEntity location)
	{
		boolean isOutside = false;
		if (location.getProperty("isOutside")!=null)
			isOutside = location.getProperty("isOutside").equals("TRUE");
		
		String biomeType = "Temperate";
		if (location.getProperty("biomeType")!=null)
			biomeType = (String)location.getProperty("biomeType");
		
		if (GameUtils.getWeather()>0.65d && isOutside && biomeType.equals("Temperate"))
			return true;
		
		return false;
	}

	/**
	 * Returns true only if the item is in your inventory or on the ground in your location.
	 * 
	 * @param item
	 * @param character
	 * @return
	 */
	public static boolean checkItemIsAccessible(CachedEntity item, CachedEntity character)
	{
		Key locationKey = (Key)character.getProperty("locationKey");
		
		Key itemContainer = (Key)item.getProperty("containerKey");
		
		if (GameUtils.equals(itemContainer, character.getKey()) || GameUtils.equals(itemContainer, locationKey))
			return true;
		
		
		return false;
	}

	public static boolean checkLocationIsInstance(CachedEntity location)
	{
		return "Instance".equals(location.getProperty("combatType"));
	}
	
	public static boolean checkLocationIsGoodRestSite(CachedEntity location)
	{
		return "RestSite".equals(location.getProperty("type")) &&
				(location.getProperty("ownerKey")!=null ||
				"Aera Inn".equals(location.getProperty("name")) ||
				"Volantis Inn".equals(location.getProperty("name")) ||
				"Wastelander's Rest Stop".equals(location.getProperty("name")));
	}

	public static boolean checkLocationIsOutside(CachedEntity location)
	{
		if (location.getProperty("isOutside")==null) return true;
		return "TRUE".equals(location.getProperty("isOutside"));
	}

	public static boolean checkLocationOrPathIsTemplateMode(CachedEntity locationOrPath)
	{
		if (locationOrPath.getProperty("templateMode")==null) return false;
		return Boolean.TRUE.equals(locationOrPath.getProperty("templateMode"));
	}

	public static boolean isItemImmovable(CachedEntity item)
	{
		return Boolean.TRUE.equals(item.getProperty("immovable"));
	}

	public static boolean isItemCustom(CachedEntity material)
	{
		if (material==null) return false;
		return GameUtils.equals("Custom", material.getProperty("forcedItemQuality"));
	}

	public static boolean checkUserIsSubscribed(CachedEntity user)
	{
		if (user==null) return false;
		return GameUtils.equals(Boolean.TRUE, user.getProperty("subscribe"));
	}

	public static boolean checkItemIsCustomized(CachedEntity item)
	{
		return "Custom".equals(item.getProperty("forcedItemQuality"));
	}

	public static boolean checkItemIsEquipped(Key itemKey, CachedEntity character)
	{
		for (int i = 0; i < ODPDBAccess.EQUIPMENT_SLOTS.length; i++)
		{
			Key equipmentInSlot = (Key) character.getProperty("equipment" + ODPDBAccess.EQUIPMENT_SLOTS[i]);
			if (GameUtils.equals(itemKey, equipmentInSlot)) 
				return true;
		}

		return false;
	}

	public static boolean checkCharacterIsAdminOfHisGroup(CachedEntity character)
	{
		String charGroupStatus = (String)character.getProperty("groupStatus");
		if (charGroupStatus==null) return false;
		
		if(character.getProperty("groupKey") != null &&
				(GameUtils.enumEquals(charGroupStatus, GroupStatus.Admin)))
		{
			return true;
		}
		
		return false;
	}

	public static boolean checkCharacterIsIncapacitated(CachedEntity character)
	{
		if (checkCharacterIsDead(character) || checkCharacterIsUnconscious(character))
			return true;
		return false;
	}

	public static boolean checkItemIsStackable(CachedEntity entity)
	{
		if (entity.getProperty("quantity")!=null)
			return true;
		return false;
	}

	public static boolean checkLocationIsAutoHealForGuard(CachedEntity guardLocation, CachedEntity guard)
	{
		if (guardLocation==null) return false;
		if (guard==null) return false;
		
		if ("RestSite".equals(guardLocation.getProperty("type")) ||
				"Town".equals(guardLocation.getProperty("type")) ||
				"CityHall".equals(guardLocation.getProperty("type")))
			return true;
		
		return false;
	}

	public static boolean checkCharacterDefenderToAutoHeal(CachedEntity defenderLocation, CachedEntity defender, CachedEntity attackerLocation, CachedEntity attacker)
	{
		
		boolean defenceStructureAttack = "DefenceStructureAttack".equals(attacker.getProperty("combatType"));
		
		boolean isTerritoryCombat = false;
		if ((defenderLocation!=null && defenderLocation.getProperty("territoryKey")!=null) || 
				(attackerLocation!=null && attackerLocation.getProperty("territoryKey")!=null))
			isTerritoryCombat = true;
		
		boolean isGuardToHeal = false;
		if ("AutoDefender".equals(defender.getProperty("combatType")) && CommonChecks.checkLocationIsAutoHealForGuard(defenderLocation, defender))
			isGuardToHeal = true;
		
		if (defenderLocation!=null && CommonChecks.checkCharacterIsRaidBoss(defender) == false && 
				(defenceStructureAttack || isTerritoryCombat || isGuardToHeal))
			return true;
		
		return false;
	}
	
	public static boolean checkIsHardcore(CachedEntity entity)
	{
		if(entity != null && "Item".equals(entity.getKind())==false && "Character".equals(entity.getKind())==false)
			return false;
		
		return GameUtils.booleanEquals(entity.getProperty("hardcoreMode"), true);
	}

	public static boolean checkItemIsNatural(CachedEntity item)
	{
		if (item==null) return false;
		return "TRUE".equals(item.getProperty("naturalEquipment"));
	}

	public static boolean checkLocationIsGoodForNaturalResource(CachedEntity location)
	{
		if (checkLocationIsOutside(location) && 
				checkLocationIsInstance(location)==false && 
				checkLocationIsCampSite(location)==false && 
				checkLocationIsCombatSite(location)==false && 
				checkLocationIsGoodRestSite(location)==false && 
				"Permanent".equals(location.getProperty("type")))
			return true;
		
		return false;
	}

	public static boolean checkCharacterIsPlayer(CachedEntity character)
	{
		if (character==null) return false;
		return "".equals(character.getProperty("type")) || 
				"PC".equals(character.getProperty("type")) || 
				character.getProperty("type") == null;
	}
	
	public static boolean checkCharacterIs2DCapable(CachedEntity character)
	{
		if (character==null) return false;
		return character.getProperty("gridMapImage")!=null;
	}

	public static boolean checkLocationIsRootLocation(CachedEntity location)
	{
		if (GameUtils.equals(location.getProperty("type"), "Permanent") || GameUtils.equals(location.getProperty("type"), "Town") || GameUtils.equals(location.getProperty("type"), "RestSite"))
			if ("Global".equals(location.getProperty("mapComponentType")))
				return true;
		
		return false;
	}

	public static boolean checkLocationIsPermanentLocation(CachedEntity location)
	{
		if (GameUtils.equals(location.getProperty("type"), "Permanent") || 
				GameUtils.equals(location.getProperty("type"), "Town") ||
				GameUtils.equals(location.getProperty("type"), "RestSite") ||
				GameUtils.equals(location.getProperty("type"), "CityHall") ||
				GameUtils.equals(location.getProperty("subType"), "CampSite"))
			return true;
		
		return false;
	}

	public static boolean checkItemIsLegacyGridMapItem(CachedEntity legacyItem)
	{
		Key containerKey = (Key)legacyItem.getProperty("containerKey");
		Long tileX = (Long)legacyItem.getProperty("gridMapPositionX");
		Long tileY = (Long)legacyItem.getProperty("gridMapPositionY");
		if (containerKey!=null && containerKey.getKind().equals("Location"))
		{
			if (tileX==null || tileY==null)
				return true;
		}
		return false;
	}

	public static boolean checkNPCIs2DCombatMode(CachedEntity combatant)
	{
		if (combatant!=null && combatant.getProperty("gridMapImage")!=null && combatant.getProperty("gridMapImage").equals("")==false)
			return true;
		
		return false;
	}

	public static boolean checkLocationIsNaturalResourceSite(CachedEntity location)
	{
		return GameUtils.equals(location.getProperty("subType"), "ResourceSite");
	}

	public static boolean checkLocationIsTopdownMode(CachedEntity location)
	{
		if (GameUtils.equals(location.getProperty("topdownMode"), true))
			return true;
		
		return false;
	}

	public static boolean checkItemHasAspect(CachedEntity item, Class<? extends InitiumAspect> clazz)
	{
		List<String> list = (List<String>)item.getProperty("_aspects");
		
		if (list!=null && list.contains(clazz.getSimpleName().substring(6)))
			return true;
		
		return false;
	}

	public static boolean checkLocationIsTown(CachedEntity location)
	{
		return GameUtils.equals(location.getProperty("type"), "Town");
	}

	public static boolean checkLocationIsTownHall(CachedEntity location)
	{
		return GameUtils.equals(location.getProperty("type"), "CityHall");
	}


}
