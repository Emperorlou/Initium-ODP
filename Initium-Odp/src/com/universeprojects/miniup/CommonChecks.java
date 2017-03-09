package com.universeprojects.miniup;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess.GroupStatus;

public abstract class CommonChecks
{

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
		if (hitpoints<=0 && "DEAD".equals(character.getProperty("mode")))
			return true;
		
		return false;
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
		if (characterKey.equals(idea.getProperty("characterKey")))
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
			return true;
		
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
		if ("CombatSite".equals(location.getProperty("type")))
			return true;
		
		return false;
	}
	
}








