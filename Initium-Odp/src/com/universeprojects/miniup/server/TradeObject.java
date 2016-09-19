package com.universeprojects.miniup.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class TradeObject implements Serializable 
{
	private static final long serialVersionUID = 1345427970566887795L;
	private static final int numberOfMemcacheBackups = 0;
	
	String constructedKey = null;
	Key character1Key = null;
	Key character2Key = null;
	
	List<CachedEntity> character1Items = new ArrayList<CachedEntity>();
	List<CachedEntity> character2Items = new ArrayList<CachedEntity>();
	boolean character1Checked = false;
	boolean character2Checked = false;
	long character1Dogecoins = 0l;
	long character2Dogecoins = 0l;
	boolean tradeComplete = false;
	boolean tradeCancelled = false;
	int version = 0;
	
	private TradeObject() 
	{
		
	}
	
	public String getKey()
	{
		return constructedKey;
	}
	
	public static TradeObject startNewTrade(CachedDatastoreService ds, CachedEntity character, CachedEntity otherCharacter) throws UserErrorMessage
	{
		if (ds==null)
			throw new IllegalArgumentException("CachedDatastoreService cannot be null.");
		if (character==null)
			throw new IllegalArgumentException("Character cannot be null.");
		if (otherCharacter==null)
			throw new IllegalArgumentException("otherCharacter cannot be null.");

		// Determine if the characters can enter this state...
		if (character.getProperty("mode")!=null && character.getProperty("mode").equals("NORMAL")==false)
			throw new UserErrorMessage("You cannot start a trade at the moment.");
		if (otherCharacter.getProperty("mode")!=null && otherCharacter.getProperty("mode").equals("NORMAL")==false)
			throw new UserErrorMessage(""+otherCharacter.getProperty("name")+" is busy and cannot enter a trade at the moment.");
		if(character.getKey().getId()==otherCharacter.getKey().getId())
			throw new UserErrorMessage("You cannot trade with yourself.");
		
		// Ok, set the character's to be trading
		character.setProperty("mode", "TRADING");
		otherCharacter.setProperty("mode", "TRADING");
		character.setProperty("combatant", otherCharacter.getKey());
		otherCharacter.setProperty("combatant", character.getKey());
		
		ds.put(character);
		ds.put(otherCharacter);
		
		
		TradeObject to = new TradeObject();
		to.character1Key = character.getKey();
		to.character2Key = otherCharacter.getKey();
		to.constructedKey = constructKey(character.getKey(), otherCharacter.getKey());
		
		to.saveChanges(ds);
		
		return to;
	}
	
	
	public static TradeObject getTradeObjectFor(CachedDatastoreService ds, CachedEntity character) 
	{
		if (ds==null)
			throw new IllegalArgumentException("CachedDatastoreService cannot be null.");
		
		TradeObject tradeObject = null;
		if (character.getProperty("combatant")!=null)
			tradeObject = (TradeObject)ds.getSaferMemcacheValue(constructKey(character.getKey(), (Key)character.getProperty("combatant")), numberOfMemcacheBackups);
		
		// If the tradeObject is null at this point, we gotta fix the character's mode...
		if (tradeObject==null || tradeObject.isCancelled() || tradeObject.isComplete())
		{
			character.setProperty("mode", ODPDBAccess.CHARACTER_MODE_NORMAL);
			character.setProperty("combatant", null);
			ds.put(character);
		}
		else
		{
			// TEMPORARY FIX
			if (tradeObject.character1Items==null)
				tradeObject.character1Items = new ArrayList<CachedEntity>();
			if (tradeObject.character2Items==null)
				tradeObject.character2Items = new ArrayList<CachedEntity>();
		}
		
		
		return tradeObject;
	
	}
	
	public static boolean checkEntitiesChanged(CachedDatastoreService ds, List<CachedEntity> entities)
	{
		List<CachedEntity> refetchedEntities = ds.refetch(entities);
		
		// First check if any of the refetched entities no longer exist...
		for(CachedEntity e:refetchedEntities)
			if (e==null)
				return true;

		if (entities.size()!=refetchedEntities.size())
			return true;
		
		// Now check if any of the fields have changed...
		for(int i = 0; i<entities.size(); i++)
		{
			CachedEntity e = entities.get(i);
			CachedEntity refetchedE = refetchedEntities.get(i);

			// First check all fields on the entities are equal to the refetched entities
			for(String fieldName:e.getProperties().keySet())
			{
				if (GameUtils.equals(e.getProperty(fieldName), refetchedE.getProperty(fieldName))==false)
					return true;
			}
			
			// Then check all fields on the refetched entities are equal to the entities. We do this because there might be a new field added to the refetched entity that does not
			// appear in the field name list of the entity. Checking twice is marginally wasteful, but we don't need performance here and it ensures simple code and is certainly thorough.
			for(String fieldName:refetchedE.getProperties().keySet())
			{
				if (GameUtils.equals(e.getProperty(fieldName), refetchedE.getProperty(fieldName))==false)
					return true;
			}
		}
		return false;
	}
	
	
	public static String constructKey(Key character1, Key character2)
	{
		if (character1.getId()<character2.getId())
			return "TradeObject-"+character1.getId()+""+character2.getId();
		else
			return "TradeObject-"+character2.getId()+""+character1.getId();
	}
	
	
	public void saveChanges(CachedDatastoreService ds)
	{
		character1Checked = false;
		character2Checked = false;
	
		version++;
		
		ds.setSaferMemcacheValue(constructedKey, this, numberOfMemcacheBackups);
	}
	
	private void cancel(CachedDatastoreService ds)
	{
		tradeCancelled = true;
		ds.setSaferMemcacheValue(constructedKey, this, numberOfMemcacheBackups);
	}
	
	
	public int getVersion()
	{
		return version;
	}
	
	
	public void addObject(CachedDatastoreService ds, CachedEntity character, CachedEntity item)
	{
		if (ds==null)
			throw new IllegalArgumentException("CachedDatastoreService cannot be null.");
		
		
		
		
		if (character1Key.getId() == character.getKey().getId())
		{
			// CHeck if the item has already been added, if so, don't add it again
			for(int i = 0; i<character1Items.size(); i++)
				if (character1Items.get(i).getKey().getId()==item.getKey().getId())
					return;
			// Add the item
			character1Items.add(item);
		}
		else if (character2Key.getId() == character.getKey().getId())
		{
			// CHeck if the item has already been added, if so, don't add it again
			for(int i = 0; i<character2Items.size(); i++)
				if (character2Items.get(i).getKey().getId()==item.getKey().getId())
					return;
			// Add the item
			character2Items.add(item);
		}
		else
			throw new IllegalArgumentException("Character part of this trade object.");
		
		saveChanges(ds);
	}
	
	
	public void addObjects(CachedDatastoreService ds, CachedEntity character, List<CachedEntity> items)
	{
		if (ds==null)
			throw new IllegalArgumentException("CachedDatastoreService cannot be null.");
		
		for(CachedEntity item:items)
		{
		
		
		if (character1Key.getId() == character.getKey().getId())
		{
			// CHeck if the item has already been added, if so, don't add it again
			for(int i = 0; i<character1Items.size(); i++)
				if (character1Items.get(i).getKey().getId()==item.getKey().getId())
					return;
			// Add the item
			character1Items.add(item);
		}
		else if (character2Key.getId() == character.getKey().getId())
		{
			// CHeck if the item has already been added, if so, don't add it again
			for(int i = 0; i<character2Items.size(); i++)
				if (character2Items.get(i).getKey().getId()==item.getKey().getId())
					return;
			// Add the item
			character2Items.add(item);
		}
		else
			throw new IllegalArgumentException("Character part of this trade object.");
		}
		saveChanges(ds);
	}
	
	
	
	public void removeObject(CachedDatastoreService ds, CachedEntity character, CachedEntity item)
	{
		if (ds==null)
			throw new IllegalArgumentException("CachedDatastoreService cannot be null.");
		
		
		
		
		if (character1Key.getId() == character.getKey().getId())
		{
			for(int i = 0; i<character1Items.size(); i++)
				if (character1Items.get(i).getKey().getId()==item.getKey().getId())
					character1Items.remove(i);
		}
		else if (character2Key.getId() == character.getKey().getId())
		{
			for(int i = 0; i<character2Items.size(); i++)
				if (character2Items.get(i).getKey().getId()==item.getKey().getId())
					character2Items.remove(i);
		}
		else
			throw new IllegalArgumentException("Character part of this trade object.");
		
		saveChanges(ds);
	}
	
	
	
	
	
	public void setDogecoins(CachedDatastoreService ds, CachedEntity character, long amount) throws UserErrorMessage
	{
		if (ds==null)
			throw new IllegalArgumentException("CachedDatastoreService cannot be null.");
		if (amount<0)
			throw new UserErrorMessage("Gold amount must be a number between 0 and "+character.getProperty("dogecoins"));
		if (amount>(Long)character.getProperty("dogecoins"))
			throw new UserErrorMessage("You do not have enough gold to add that much.");
		
		
		if (character1Key.getId() == character.getKey().getId())
		{
			character1Dogecoins = amount;
		}
		else if (character2Key.getId() == character.getKey().getId())
		{
			character2Dogecoins = amount;
		}
		else
			throw new IllegalArgumentException("Character part of this trade object.");
		
		saveChanges(ds);
	}


	public void flagReady(CachedDatastoreService ds, CachedEntity character, boolean ready)
	{
		if (ds==null)
			throw new IllegalArgumentException("CachedDatastoreService cannot be null.");
		
		
		if (character1Key.getId() == character.getKey().getId())
		{
			character1Checked = ready;
		}
		else if (character2Key.getId() == character.getKey().getId())
		{
			character2Checked = ready;
		}
		else
			throw new IllegalArgumentException("Character part of this trade object.");

		// I have to manually save changes here so that the ready doesn't get unchecked.
		ds.setSaferMemcacheValue(constructedKey, this, numberOfMemcacheBackups);
	}
	
	public boolean isReady(CachedDatastoreService ds, CachedEntity character)
	{
		if (ds==null)
			throw new IllegalArgumentException("CachedDatastoreService cannot be null.");
		
		
		if (character1Key.getId() == character.getKey().getId())
		{
			return character1Checked;
		}
		else if (character2Key.getId() == character.getKey().getId())
		{
			return character2Checked;
		}
		else
			throw new IllegalArgumentException("Character part of this trade object.");
		
	}
	
	public void flagCancelled(CachedDatastoreService ds, CachedEntity character)
	{
		if (ds==null)
			throw new IllegalArgumentException("CachedDatastoreService cannot be null.");
		

		cancel(ds);
	}
	
	public void flagComplete(CachedDatastoreService ds)
	{
		tradeComplete = true;
		
		saveChanges(ds);
	}
	
	public boolean isComplete()
	{
		return tradeComplete;
	}
	
	public boolean isCancelled()
	{
		return tradeCancelled;
	}
	
	public List<CachedEntity> getItemsFor(Key characterKey)
	{
		if (character1Key.getId() == characterKey.getId())
		{
			return character1Items;
		}
		else if (character2Key.getId() == characterKey.getId())
		{
			return character2Items;
		}
		else
			throw new IllegalArgumentException("Character part of this trade object.");
	}
	
	public boolean isItemInTrade(Key characterKey, Key item)
	{
		List<CachedEntity> items = getItemsFor(characterKey);
		for(CachedEntity tradeItem:items)
			if (tradeItem.getKey().getId()==item.getId())
				return true;
		
		return false;
	}
	
	
	public long getDogecoinsFor(Key characterKey)
	{
		if (character1Key.getId() == characterKey.getId())
		{
			return character1Dogecoins;
		}
		else if (character2Key.getId() == characterKey.getId())
		{
			return character2Dogecoins;
		}
		else
			throw new IllegalArgumentException("Character part of this trade object.");
	}
	
	
	public Key getOtherCharacter(Key characterKey)
	{
		if (character1Key.getId() == characterKey.getId())
		{
			return character2Key;
		}
		else if (character2Key.getId() == characterKey.getId())
		{
			return character1Key;
		}
		else
			throw new IllegalArgumentException("Character part of this trade object.");
	}

	
}
