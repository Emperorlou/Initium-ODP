package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;


/**
 * Enchant the stats of an item as described in the BuffDef
 * 
 * Usage notes:
 * Checks if itemId is owned by the calling player
 * If so, checks if any Buffs are available for the requested enchantment
 * If so, add the buff, update item stats and delete used up items.
 * 
 * Parameters:
 * 		itemId - itemID of the item to be enchanted 
 * 		type - the type of enchantment to add
 * 
 * @author NJ
 * 
 */
public class CommandEnchantItem extends Command {

	public CommandEnchantItem(HttpServletRequest request, HttpServletResponse response) 
	{
		super(request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		
		// Verify itemId parameter sanity
		Long itemId;
		try {
			itemId = Long.parseLong(parameters.get("itemId"));
		} catch (Exception e) {
			throw new RuntimeException("EnchantItem invalid call format, 'itemId' is not a valid id.");
		}
		CachedEntity item = db.getEntity("Item", itemId);
		if (item==null)
			throw new RuntimeException("EnchantItem invalid call format, 'itemId' is not a valid id.");
		
		// Verify that the item is in the caller's inventory.
		Key containerKey = (Key)item.getProperty("containerKey");
		if (containerKey==null)
			throw new UserErrorMessage("You can only enchant items you carry.");
		CachedEntity character = db.getCurrentCharacter(request);
		if (GameUtils.equals(containerKey,character.getKey())==false)
			throw new UserErrorMessage("You can only enchant items you carry.");
		
		// Check that caller status is set to Normal (or blank)
		String mode = (String)item.getProperty("mode");
		if (mode!=null && mode.equals("NORMAL")==false)
			throw new UserErrorMessage("You're too busy to enchant anything at the moment.");
		
		// Check if any enchantment buff exists for this item and type
		String name = (String)item.getProperty("internalName");
		if (name==null || (name=name.trim()).equals(""))
			throw new UserErrorMessage("This item can't be enchanted.");
		String type = parameters.get("type");
		if (type!=null && (type=type.trim()).equals("")==false)
			name += " - " + type;
		CachedEntity buffDef = db.getBuffDefByInternalName(null, name);
		if (buffDef==null)
			throw new UserErrorMessage("This item can't be enchanted.");
		String buffType = (String)buffDef.getProperty("buffType");
		if (buffType==null || GameUtils.isContainedInList(buffType,"Item")==false)
			throw new UserErrorMessage("This item can't be enchanted.");
		
		// Get all buffs and check for buff limit
		List<CachedEntity> buffs = db.getBuffsFor(item.getKey());
		if (buffs!=null && buffs.isEmpty()==false)
		{
			name = (String)buffDef.getProperty("name");
			int existingCount = 0;
			for(CachedEntity b:buffs)
				if (name.equals(b.getProperty("name")))
					existingCount++;
			Long maxCount = (Long)buffDef.getProperty("maxCount"); 
			if (maxCount!=null & existingCount >= maxCount)
				throw new UserErrorMessage("This item has reached its maximum enchantments and can't be enchanted further.");
		}
		
		// Check if there are any triggers and if so, check if met
		// Should probably be moved to GameUtils so it can be reused, but works for now
		List<Key> keysDel = new ArrayList<Key>();
		String trigger = (String)buffDef.getProperty("trigger");
		if (trigger!=null && (trigger=trigger.trim()).equals("")==false)
		{
			String[] triggers = trigger.split(",");
			List<CachedEntity> inventory = null;
			for (String t : triggers)
			{
				t = t.trim();
				boolean compareTo = true;
				if (t.startsWith("!")) // Reverse logic
				{
					compareTo = false;
					t = t.substring(1).trim();
				}
				String[] trig = (t.substring(1).trim()+":1").split(":");
				if (trig[0]==null || (trig[0]=trig[0].trim()).equals(""))
					continue;
				// get 1st value
				double val = 0d;
				if (t.startsWith("$")) // Character Stat
				{
					Object stat = character.getProperty(trig[0]);
					if (stat==null) continue;
					if (stat.getClass().equals(Double.class))
						val = (Double)stat;
					else if (stat.getClass().equals(Long.class))
						val = ((Long)stat).doubleValue();
				}
				else if (t.startsWith("£")) // Item Stat
				{
					// Set up shorthands for the 2 long crit fieldNames
					if (trig[0].equals("critChance"))
						trig[0] = "weaponDamageCriticalChance";
					else if (trig[0].equals("critMultiplier"))
						trig[0] = "weaponDamageCriticalMultiplier";
					Object stat = item.getProperty(trig[0]);
					if (stat==null) continue;
					if (stat.getClass().equals(Double.class))
						val = (Double)stat;
					else if (stat.getClass().equals(Long.class))
						val = ((Long)stat).doubleValue();
				}
				else if (t.startsWith("@")) // Equipped
				{
					if (inventory==null) inventory = db.getItemContentsFor(character.getKey());
					if (inventory==null || inventory.isEmpty()) continue;
					for (CachedEntity i : inventory)
					{
						if (trig[0].equals(i.getProperty("name")))
							if (db.checkCharacterHasItemEquipped(character, i.getKey()))
								val++;
					}
				}
				else if (t.startsWith("#")) // In inventory
				{
					if (inventory==null) inventory = db.getItemContentsFor(character.getKey());
					if (inventory==null || inventory.isEmpty()) continue;
					for (CachedEntity i : inventory)
					{
						if (trig[0].equals(i.getProperty("name")))
							if (db.checkItemIsVending(character.getKey(), i.getKey())==false)
							{
								val++;
								if (compareTo) keysDel.add(i.getKey());
							}
					}
				}
				else
				{
					// Unknown trigger format, just move on to the next one.
					continue;
				}
				// Get 2nd value
				trig[1] = trig[1].trim();
				if (trig[1].equals(""))
					continue;
				double compareVal = 1d;
				try {
					compareVal = new Double(trig[1]);
				} catch (Exception e) {
					if (trig[1].startsWith("$")) // Character Stat
					{
						Object stat = character.getProperty(trig[1].substring(1).trim());
						if (stat==null) continue;
						if (stat.getClass().equals(Double.class))
							compareVal = (Double)stat;
						else if (stat.getClass().equals(Long.class))
							compareVal = ((Long)stat).doubleValue();
					}
					else if (trig[1].startsWith("£")) // Item Stat
					{
						// Set up shorthands for the 2 long crit fieldNames
						trig[1] = trig[1].substring(1).trim();
						if (trig[1].equals("critChance"))
							trig[1] = "weaponDamageCriticalChance";
						else if (trig[1].equals("critMultiplier"))
							trig[1] = "weaponDamageCriticalMultiplier";
						Object stat = item.getProperty(trig[1]);
						if (stat==null) continue;
						if (stat.getClass().equals(Double.class))
							compareVal = (Double)stat;
						else if (stat.getClass().equals(Long.class))
							compareVal = ((Long)stat).doubleValue();
					}
				}
				// Finally compare
				if ((val>=compareVal)!=compareTo)
					throw new UserErrorMessage("You failed to enchant the item because the requirements weren't met.");
					
			}
		}
		
		// Set the buff, update the item stats accordingly and delete any used up items
		CachedDatastoreService ds = getDS();
		ds.beginTransaction();
		try
		{
			CachedEntity buff = db.awardBuff(ds, item.getKey(), buffDef);
			for (int i = 1; i <= 3; i++)
			{
				String field = (String)buff.getProperty("field" + i + "Name");
				if (field==null || (field=field.trim()).equals(""))
					continue;
				String effect = (String)buff.getProperty("field" + i + "Effect");
				if (effect==null || (effect=effect.trim()).equals(""))
					continue;
				if (field.equals("weaponDamage"))
				{
					// check effect format
					if (effect.matches("[-+][0-9]+[dD][0-9]+"))
					{
						Object value = item.getProperty(field);
						if (value==null) continue; 
						item.setProperty(field, db.getDDBuffEffect(effect, value.toString()));
					}
					continue;
				}
				// check effect format
				if (effect.matches("[-+][0-9.]+%?")==false)
					continue;
				// Set up shorthands for the 2 long crit fieldNames
				if (field.equals("critChance"))
					field = "weaponDamageCriticalChance";
				else if (field.equals("critMultiplier"))
					field = "weaponDamageCriticalMultiplier";
				
				Object value = item.getProperty(field);
				if (value==null) continue; 
				if (value.getClass().equals(Double.class))
					item.setProperty(field, db.getDoubleBuffEffect(effect, (Double)value));
				else if (value.getClass().equals(Long.class))
					item.setProperty(field, db.getLongBuffEffect(effect, (Long)value));
			}
			ds.delete(keysDel);
			ds.put(buff);
			ds.put(item);
			ds.commit();
		}
		finally
		{
			ds.rollbackIfActive();
		}
		
	}

}
