package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;

/**
 * Scripting engine wrapper for the Location CachedEntity.
 * getName already exists as part of the base EntityWrapper class.
 * Only information that we really want to expose at the moment.
 * May want to find associated paths.
 * 
 * @author spfiredrake
 */
public class Location extends EntityWrapper {

	public Location(CachedEntity entity, ODPDBAccess db) {
		super(entity, db);
		isInstanceLocation = GameUtils.booleanEquals(entity.getProperty("instanceModeEnabled"), true);
	}
	
	/**
	 * Boolean indicating whether this location is instanced or not. Does not indicate
	 * that an instance timer is set.
	 */
	 public final boolean isInstanceLocation;
	
	/**
	 * Returns back whether an instance timer is set on the location.
	 * @return
	 */
	public boolean isInstanceTimerSet()
	{
		return isInstanceLocation && this.getProperty("instanceRespawnDate")!=null;
	}
	
	/**
	 * Resets the instance timer location. Only returns back whether the instance timer
	 * is running, not necessarily whether the instance timer was set.
	 * @return
	 */
	public boolean resetInstanceTimer()
	{
		if(isInstanceLocation == false) return false;
		db.resetInstanceRespawnTimer(this.wrappedEntity);
		return isInstanceTimerSet();
	}
	
	/**
	 * Sets the instance timer at the location.
	 * @param respawnTime
	 * @return
	 */
	public boolean setInstanceTimer(Long respawnTime)
	{
		return setInstanceTimer(respawnTime, false);
	}
	
	public boolean setInstanceTimer(Long respawnTime, boolean ignoreIfSet)
	{
		if(isInstanceLocation == false) return false;
		if(ignoreIfSet && isInstanceTimerSet()) return false;
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.MINUTE, respawnTime.intValue());
		
		this.setProperty("instanceRespawnDate", cal.getTime());
		
		return isInstanceTimerSet();
	}
	
	// Cache the items here. We should only ever do this once in script context,
	// don't allow it to be called multiple times.
	private Item[] _itemsHere = null;
	public Item[] itemsHere(boolean inPlayerHouse)
	{
		if(_itemsHere == null)
		{
			List<Item> items = new ArrayList<Item>();
			List<CachedEntity> dbItems = db.getItemContentsFor(this.getKey(), inPlayerHouse);
			for(CachedEntity item:dbItems)
			{
				if(item != null)
					items.add(new Item(item, this.db, this));
			}
			
			_itemsHere = new Item[items.size()];
			_itemsHere = items.toArray(_itemsHere);
		}
		return _itemsHere;
	}
	
	private Character[] _charactersHere = null;
	private Character[] getCharactersHere()
	{
		if(_charactersHere == null)
		{
			List<Character> chars = new ArrayList<Character>();
			QueryHelper qh = new QueryHelper(db.getDB());
			List<CachedEntity> dbChars = qh.getFilteredList("Character", 500, null, "locationKey", FilterOperator.EQUAL, this.getKey());
			for(CachedEntity charEnt:dbChars)
			{
				if(charEnt != null)
				{
					chars.add(new Character(charEnt, this.db));
				}
			}
			
			_charactersHere = new Character[chars.size()];
			_charactersHere = chars.toArray(_charactersHere);
		}
		
		return _charactersHere;
	}
	
	public Character[] charactersHere()
	{
		return getCharactersHere();
	}
	
	public Character[] monstersHere()
	{
		List<Character> chars = new ArrayList<Character>();
		for(Character charHere:getCharactersHere())
		{
			if(charHere.isPlayerCharacter()==false)
				chars.add(charHere);
		}
		
		return chars.toArray(new Character[chars.size()]);
	}
	
	public Character[] playersHere()
	{
		List<Character> chars = new ArrayList<Character>();
		for(Character charHere:getCharactersHere())
		{
			if(charHere.isPlayerCharacter())
				chars.add(charHere);
		}
		
		return chars.toArray(new Character[chars.size()]);
	}
}
