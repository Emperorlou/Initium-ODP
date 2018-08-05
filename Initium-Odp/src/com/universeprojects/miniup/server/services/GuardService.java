package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.ConfirmAttackException;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.dbentities.GuardSetting;
import com.universeprojects.miniup.server.dbentities.GuardSetting.GuardExclusion;
import com.universeprojects.miniup.server.dbentities.GuardSetting.GuardType;

public class GuardService extends Service
{
	
	public GuardService(ODPDBAccess db)
	{
		super(db);
	}

	private List<GuardSetting> guardSettingsCache = null;
	private List<GuardSetting> getGuardsForLocation(CachedEntity location)
	{
		if (guardSettingsCache==null)
		{
			guardSettingsCache = entitiesToGuardSettings(query.getFilteredList("GuardSetting", "active", true, "locationKey", location.getKey()));
		}
		return guardSettingsCache;
	}
	
	public void doRunAndStopGuarding(CachedEntity guard, Key locationKey)
	{
		guard.setProperty("guardStartHP", null);
		
		deleteAllGuardSettings(guard.getKey(), locationKey);
	}
	
	public void deleteAllGuardSettings(Key characterKey)
	{
		List<Key> list = query.getFilteredList_Keys("GuardSetting", 3000, "characterKey", characterKey);
		ds.delete(list);
	}
	
	public void deleteAllGuardSettings(Key characterKey, Key locationKey)
	{
		List<Key> list = query.getFilteredList_Keys("GuardSetting", 3000, "characterKey", FilterOperator.EQUAL, characterKey, "locationKey", FilterOperator.EQUAL, locationKey);
		ds.delete(list);
	}
	
	public GuardSetting newGuardService(Key characterKey, Key locationKey, Key entityKey, GuardType type) throws UserErrorMessage
	{
		if (checkIfAlreadyGuarding(characterKey, locationKey, entityKey, type))
			throw new UserErrorMessage("You already have a guard setting for this. Remove the guard setting before you make another of the same kind.");
		
		GuardSetting gs = new GuardSetting(db, new CachedEntity("GuardSetting"));
		gs.setActive(true);
		gs.setCharacterKey(characterKey);
		gs.setLocationKey(locationKey);
		gs.setEntityKey(entityKey);
		gs.setSettings(type);
		gs.setExclude(GuardExclusion.Group, GuardExclusion.Party, GuardExclusion.Alliances);
		return gs;
	}
	
	public List<GuardSetting> getAllActiveGuardSettingsForLocation(Key locationKey)
	{
		return entitiesToGuardSettings(query.getFilteredList("GuardSetting", "active", true, "locationKey", locationKey));
	}
	
	public List<GuardSetting> getGuardSettings(Key characterKey)
	{
		return entitiesToGuardSettings(query.getFilteredList("GuardSetting", "characterKey", characterKey));
	}
	
	public List<GuardSetting> getGuardSettings(Key characterKey, Key locationKey)
	{
		return entitiesToGuardSettings(query.getFilteredList("GuardSetting", "characterKey", characterKey, "locationKey", locationKey));
	}
	
	public boolean checkIfAlreadyGuarding(Key characterKey, Key locationKey, Key entityKey, GuardType type)
	{
		return query.getFilteredList_Count("GuardSetting", "characterKey", FilterOperator.EQUAL, characterKey, "locationKey", FilterOperator.EQUAL, locationKey, "settings", FilterOperator.EQUAL, type.toString())>0;
	}
	
	/**
	 * Checks if the character is allowed to enter a given location and returns the key of the guard
	 * that would attack the character if they tried.
	 * 
	 * @param character The character that is trying to enter the given location.
	 * @param location The location the given character is trying to enter.
	 * @return The character that would attack the given character if they try to enter the given location, or null.
	 * @throws ConfirmAttackException 
	 */
	public CachedEntity tryToEnterLocation(CachedEntity character, Key locationKey, boolean attack) throws ConfirmAttackException
	{
		return guardCheck(character, locationKey, locationKey, GuardType.NoTrespassers, attack);
	}
	
	public CachedEntity tryToGuardLocation(CachedEntity character, Key locationKey, GuardType type, boolean attack) throws ConfirmAttackException
	{
		// If we want to guard against guarding, check that there are no other guard types. If there are, we have to engage them before we
		// continue...
		if (type == GuardType.NoGuarding)
		{
			return guardCheck_not_NoGuarding(character, locationKey, locationKey, attack);
		}
		else 
		{
			
			return guardCheck(character, locationKey, locationKey, GuardType.NoGuarding, attack);
		}
	}
	
	public CachedEntity tryToTakeItem(CachedEntity character, CachedEntity item, boolean attack) throws ConfirmAttackException
	{
		// Recursively look for the location that this item is in to see if it is going to be protected
		// Actually, lets not do this
//		Key containerKey = (Key)item.getProperty("containerKey");
//		while(containerKey.getKind().equals("Location")==false)
//		{
//			CachedEntity container = db.getEntity(containerKey);
//			containerKey = (Key)container.getProperty("containerKey");
//			if (containerKey==null) containerKey = (Key)container.getProperty("locationKey");
//		}
		
		// Only check items that are on the ground
		Key containerKey = (Key)item.getProperty("containerKey");
		if ((containerKey).getKind().equals("Location")==false)
			return null;
		return guardCheck(character, containerKey, item.getKey(), GuardType.NoMoving, attack);
	}
	
	public CachedEntity tryToUseItem(CachedEntity character, CachedEntity item, boolean attack) throws ConfirmAttackException
	{
		// Only check items that are on the ground
		Key containerKey = (Key)item.getProperty("containerKey");
		if ((containerKey).getKind().equals("Location")==false)
			return null;
		
		return guardCheck(character, containerKey, item.getKey(), GuardType.NoUsing, attack);
	}
	
	
	
	public CachedEntity guardCheck(CachedEntity character, Key locationKey, Key entityKey, GuardType guardType, boolean userWantsToAttack) throws ConfirmAttackException
	{
		List<CachedEntity> rawList = query.getFilteredList("GuardSetting", "active", true, "locationKey", locationKey, "entityKey", entityKey, "settings", guardType.toString());
		List<GuardSetting> guardSettings = entitiesToGuardSettings(rawList);
		prepareGuardList(guardSettings);
		
		List<Key> characterKeys = new ArrayList<>(); 
		// Go through the guards looking for those that are present and are not excluding this character
		for(int guardIndex=0; guardIndex<guardSettings.size(); guardIndex=guardIndex+5) // Checking 5 guards at a time for a good match
		{
			characterKeys.clear();
			for(int i = guardIndex; i<5 && i<guardSettings.size(); i++)
			{
				characterKeys.add(guardSettings.get(i).getCharacterKey());
			}
			
			List<CachedEntity> guards = db.getEntities(characterKeys);
			for(int i = 0; i<guards.size(); i++)
			{
				CachedEntity guard = guards.get(i);
				GuardSetting guardSetting = guardSettings.get(guardIndex+i);
				
				try 
				{
					if (verifyGuardWithGuardSetting(guard, guardSetting))
					{
						if (checkCharacterIsExcludedFromGuardSetting(guard, guardSetting, character))
							continue;
						
						if (userWantsToAttack)
							return guard;
						else
						{
							throwUserError(guardSettings.size(), entityKey, guardType);
						}
					}
				} catch (DeleteGuardSetting e) 
				{
					db.getDB().delete(e.guardSetting.getKey());
				}
			}
			
		}
		
		return null;
	}
	
	public CachedEntity guardCheck_not_NoGuarding(CachedEntity character, Key locationKey, Key entityKey, boolean userWantsToAttack) throws ConfirmAttackException
	{
		List<CachedEntity> rawList = query.getFilteredList("GuardSetting", 1000, null, "active", FilterOperator.EQUAL, true, "locationKey", FilterOperator.EQUAL, locationKey, "entityKey", FilterOperator.EQUAL, entityKey);
		List<GuardSetting> guardSettings = entitiesToGuardSettings(rawList);
		prepareGuardList(guardSettings);
		
		for(int i = guardSettings.size()-1; i>=0; i--)
		{
			GuardSetting gs = guardSettings.get(i);
			GuardType type = gs.getSettings();
			if (type.toString().equals("NoGuarding"))
			{
				guardSettings.remove(i);
			}
		}
		
		List<Key> characterKeys = new ArrayList<>(); 
		// Go through the guards looking for those that are present and are not excluding this character
		for(int guardIndex=0; guardIndex<guardSettings.size(); guardIndex=guardIndex+5) // Checking 5 guards at a time for a good match
		{
			characterKeys.clear();
			for(int i = guardIndex; i<5 && i<guardSettings.size(); i++)
			{
				characterKeys.add(guardSettings.get(i).getCharacterKey());
			}
			
			List<CachedEntity> guards = db.getEntities(characterKeys);
			for(int i = 0; i<guards.size(); i++)
			{
				CachedEntity guard = guards.get(i);
				GuardSetting guardSetting = guardSettings.get(guardIndex+i);
				
				try 
				{
					if (verifyGuardWithGuardSetting(guard, guardSetting))
					{
						if (checkCharacterIsExcludedFromGuardSetting(guard, guardSetting, character))
							continue;
						
						if (userWantsToAttack)
							return guard;
						else
						{
							throw new ConfirmAttackException("<img style='float:left;' src='https://initium-resources.appspot.com/images/ui/guardsettings1.png'/>There are up to "+guardSettings.size()+" guards still guarding "+db.getEntity(entityKey).getProperty("name")+". You will need to fight them off before you can guard against guarding here. Are you sure you want to attack?");
						}
					}
				} catch (DeleteGuardSetting e) 
				{
					db.getDB().delete(e.guardSetting.getKey());
				}
			}
			
		}
		
		return null;
	}
	
	private void throwUserError(int size, Key entityKey, GuardType guardType) throws ConfirmAttackException
	{
		CachedEntity entity = db.getEntity(entityKey);
		
		if (guardType == GuardType.NoTrespassers)
		{
			throw new ConfirmAttackException("<img style='float:left;' src='https://initium-resources.appspot.com/images/ui/guardsettings1.png'/>There are up to "+size+" guards keeping you from tresspassing in "+entity.getProperty("name")+". Are you sure you want to attack?");
		}
		else if (guardType == GuardType.NoGuarding)
		{
			throw new ConfirmAttackException("<img style='float:left;' src='https://initium-resources.appspot.com/images/ui/guardsettings1.png'/>There are up to "+size+" guards keeping you from guarding anything in "+entity.getProperty("name")+". You will need to fight them off before you can guard anything here. Are you sure you want to attack?");
		}
		else if (guardType == GuardType.NoMoving)
		{
			throw new ConfirmAttackException("<img style='float:left;' src='https://initium-resources.appspot.com/images/ui/guardsettings1.png'/>There are up to "+size+" guards keeping you from taking "+entity.getProperty("name")+". You will need to fight them off before you can take it. Are you sure you want to attack?");
		}
		else if (guardType == GuardType.NoUsing)
		{
			throw new ConfirmAttackException("<img style='float:left;' src='https://initium-resources.appspot.com/images/ui/guardsettings1.png'/>There are up to "+size+" guards keeping you from using "+entity.getProperty("name")+". You will need to fight them off before you can use it. Are you sure you want to attack?");
		}
	}

	/**
	 * Is the current character allowed to pick up this item or is it being guarded?
	 * 
	 * @param character
	 * @param territory
	 * @param location
	 * @param item
	 * @return The key of the guard who will attack this character if they try to pick up
	 */
	public Key tryPickUp(CachedEntity character, CachedEntity territory, CachedEntity location, CachedEntity item)
	{
		return null;
	}

	
	
	private List<GuardSetting> entitiesToGuardSettings(List<CachedEntity> entities)
	{
		List<GuardSetting> result = new ArrayList<>();
		for(CachedEntity e:entities)
		{
			result.add(new GuardSetting(db, e));
		}
		return result;
	}
	
	private void prepareGuardList(List<GuardSetting> guardSettings)
	{
		Collections.shuffle(guardSettings);
		Collections.sort(guardSettings, new Comparator<GuardSetting>() {

			@Override
			public int compare(GuardSetting o1, GuardSetting o2) {
				Long o1Line = o1.getLine();
				Long o2Line = o2.getLine();
				if (o1Line==null) o1Line = 0L;
				if (o2Line==null) o2Line = 0L;
				
				return o1Line.compareTo(o2Line);
			}
		});
	}
	
	private boolean verifyGuardWithGuardSetting(CachedEntity guard, GuardSetting guardSetting) throws DeleteGuardSetting
	{
		// make sure the guard still exists
		if (guard==null) throw new DeleteGuardSetting(guardSetting);
		if (CommonChecks.checkCharacterIsDead(guard)) throw new DeleteGuardSetting(guardSetting);
		// Make sure the guard actually belongs to this guard setting
		if (GameUtils.equals(guard.getKey(), guardSetting.getCharacterKey())==false) throw new RuntimeException("Verifying a guard with a guard setting that is meant for someone else.");
		// Make sure the guard is in the location where the guarding is to take place
		if (GameUtils.equals(guard.getProperty("locationKey"), guardSetting.getLocationKey())==false)  throw new DeleteGuardSetting(guardSetting);
		// Make sure the guard is not busy doing something else
		if (CommonChecks.checkCharacterIsInCombat(guard)) return false;
		// Make sure the guard is not incapacitated
		if (CommonChecks.checkCharacterIsIncapacitated(guard)) return false;
		// Make sure the guard isn't set to run
		if (GuardService.checkIfGuardWantsToRun(guard)) return false;
		
		return true;
	}

	private boolean checkCharacterIsExcludedFromGuardSetting(CachedEntity guard, GuardSetting guardSetting, CachedEntity perp)
	{
		// Check if the perp is supposed to be excluded because he is an alt of the guard
		if (GameUtils.equals(guard.getProperty("userKey"), perp.getProperty("userKey")))
			 return true;
		
		// Check if the perp is supposed to be excluded because of his group
		if (guardSetting.getExclude().contains(GuardExclusion.Group))
		{
			if (CommonChecks.checkCharacterIsMemberOfHisGroup(perp) && CommonChecks.checkCharacterIsMemberOfHisGroup(guard))
			{
				if (GameUtils.equals(perp.getProperty("groupKey"), guard.getProperty("groupKey")))
					return true;
			}
		}

		// Check if hte perp is supposed to be excluded because he is in a party with the guard
		if (guardSetting.getExclude().contains(GuardExclusion.Party))
		{
			if (perp.getProperty("partyCode")!=null && guard.getProperty("partyCode")!=null)
				if (GameUtils.equals(perp.getProperty("partyCode"), guard.getProperty("partyCode")))
					 return true;
		}
		
		return false;
	}



	public class DeleteGuardSetting extends Exception
	{
		private static final long serialVersionUID = -6858368362449950659L;
		
		final GuardSetting guardSetting;
		
		public DeleteGuardSetting(GuardSetting guardSetting)
		{
			this.guardSetting = guardSetting;
		}
	}



	public void deleteGuardSettings(Key characterKey)
	{
		List<Key> list = query.getFilteredList_Keys("GuardSetting", "characterKey", characterKey);
		ds.delete(list);
	}

	public static boolean checkCharacterIsInCombatGuarding(CachedEntity character)
	{
		if (character==null) return false;
		return character.getProperty("guardStartHP")!=null;
	}

	public static boolean checkIfGuardWantsToRun(CachedEntity character)
	{
		Double guardRunHitpoints = (Double)character.getProperty("guardRunHitpoints");
		double targetCharacterHitpoints = (Double)character.getProperty("hitpoints");
		if (checkCharacterIsInCombatGuarding(character) && guardRunHitpoints!=null && targetCharacterHitpoints<=guardRunHitpoints)
		{
			return true;
		}
		
		return false;
	}
}
