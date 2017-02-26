package com.universeprojects.miniup;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

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
			return true;
		
		return false;
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
}








