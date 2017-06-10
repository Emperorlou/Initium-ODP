package com.universeprojects.miniup.server.aspects;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.Convert;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ConfirmGenericEntityRequirementsBuilder;
import com.universeprojects.miniup.server.services.ConfirmGenericEntityRequirementsBuilder.GenericEntityRequirementResult;
import com.universeprojects.miniup.server.services.ODPInventionService;

public class AspectFireplace extends ItemAspect
{
	AspectFireplace(InitiumObject object)
	{
		super(object);
	}

	@Override
	protected void initialize()
	{

	}

	@Override
	public List<ItemPopupEntry> getItemPopupEntries()
	{
		List<ItemPopupEntry> itemPopupEntries = new ArrayList<ItemPopupEntry>();
		long currentTimeMs = System.currentTimeMillis();
		if (isFireActive(currentTimeMs) && isFireExpired(currentTimeMs) || 
				isFireActive(currentTimeMs) && getMinutesSinceLastUpdate(currentTimeMs)>=5)
		{
			updateFireProgress();
			db.getDB().put(entity);
		}
		
		
		StringBuilder text = new StringBuilder();
		
		if (isFireActive(currentTimeMs))
		{
			text.append("The fire will run out of fuel in ").append(GameUtils.getTimePassedShortString(getFuelDepletionDate())).append(".<br>");
			if (getSpace()!=null && getMaxSpace()!=null)
				text.append("The fire is ").append(GameUtils.formatPercent(getSpace()/getMaxSpace())).append(" full of fuel.");
		}
		
		itemPopupEntries.add(new ItemPopupEntry("", text.toString(), ""));
		
		itemPopupEntries.add(new ItemPopupEntry("Light Fire", 
				"Using kindling and something to start the fire, this command will light a fire here.", 
				"doFireplaceLight(event, '"+entity.getUrlSafeKey()+"');"));
		
		itemPopupEntries.add(new ItemPopupEntry("Add Fuel", 
				"Add something flammable to the fire to keep it going.", 
				"doFireplaceAddFuel(event, '"+entity.getUrlSafeKey()+"');"));
		
		return itemPopupEntries;
	}
	
	public void addFuel(ODPInventionService inventionService, CachedEntity firewoodEntity) throws UserErrorMessage
	{
		addFuel(inventionService, firewoodEntity, null);
	}
	
	public void addFuel(ODPInventionService inventionService, CachedEntity firewoodEntity, Integer quantityLimit) throws UserErrorMessage
	{
		if (isFireActive(System.currentTimeMillis())==false)
			throw new UserErrorMessage("You can only add fuel to a fire that is already started.");
		
		InitiumObject firewood = new InitiumObject(db, firewoodEntity);
		if (CommonChecks.checkItemIsClass(firewoodEntity, "Firewood")==false && firewood.isAspectPresent(AspectFlammable.class)==false)
			throw new UserErrorMessage("The item you chose to add to the fire is not valid fuel.");
	
		Long firewoodQuantity = (Long)firewoodEntity.getProperty("quantity");
		Long fuelSpace = db.getItemSpace(firewoodEntity);
		Long fuelWeight = db.getItemWeight(firewoodEntity);
		Long depletionDate = getFuelDepletionDate().getTime();
		
		// Reduce fuelWeight to match the quantity we're actually going to burn (if applicable)
		if (quantityLimit!=null && firewoodQuantity!=null)
		{
			if (firewoodQuantity<quantityLimit)
				throw new UserErrorMessage("You don't have enough '"+firewoodEntity.getProperty("name")+"'. You have "+firewoodQuantity+" but require "+quantityLimit+".");
			
			fuelWeight = (Long)firewoodEntity.getProperty("weight");
			fuelWeight*=quantityLimit;
			
			fuelSpace = (Long)firewoodEntity.getProperty("space");
			fuelSpace*=quantityLimit;
		}
		
		
		if (getSpace()+fuelSpace>getMaxSpace())
			throw new UserErrorMessage("There is not enough room to put all of this into the fire.");
		
		// Now adjust the fuel weight by how much is flammable (if applicable)
		Long percentFlammable = (Long)firewoodEntity.getProperty("Flammable:percentOfFlammableMaterial");
		if (percentFlammable!=null)
		{
			fuelWeight=new Double(Math.round(fuelWeight.doubleValue()*(percentFlammable.doubleValue()/100d))).longValue();
		}
		
		int burnTime = getBurnTime(fuelWeight, fuelSpace);
		
		depletionDate += (burnTime*1000);
		
		
		setFuelDepletionDate(new Date(depletionDate));
		setSpace(getSpace()+fuelSpace);

		if (quantityLimit==null) quantityLimit = 1;
		inventionService.consumeItems(firewoodEntity, quantityLimit);
	}
	
	public void updateFireProgress()
	{
		long currentTime = System.currentTimeMillis();
		if (isFireActive(currentTime))
		{
			// Fire is active...
			boolean fireDone = false;
			if (isFireExpired(currentTime))
			{
				currentTime = getFuelDepletionDate().getTime();
				fireDone = true;
			}
			double percentComplete = getFirePercentComplete(currentTime);
	
			// Reduce the percent complete by how much ash is going to be left in the fireplace...
			percentComplete*=(1-getAshPercentage());
			
			Double space = getSpace();
			space*=(1-percentComplete);
			
			setFuelStartDate(new Date(currentTime));
			setSpace(space);
			
			// Is the fire done? If so, reset it
			if (fireDone)
			{
				setFuelStartDate(null);
				setFuelDepletionDate(null);
			}

			setIconPostfix();
			
			db.getDB().put(entity);
		}
	}
	
	private void setIconPostfix()
	{
		if (entity.getProperty("icon")==null) return;

		String oldIcon = (String)entity.getProperty("icon");
		
		long currentTimeMs = System.currentTimeMillis();
		
		if (isFireActive(currentTimeMs) && isFireExpired(currentTimeMs)==false)
		{
			long minutes = getMinutesUntilExpired(currentTimeMs);
			if (minutes<20)
				setIconToSmokeLight();
			else if (minutes<45)
				setIconToSmokeHeavy();
			else
				setIconToLit();
		}
		else
		{
			setIconToUnlit();
		}
		
		String newIcon = (String)entity.getProperty("icon");
		
		if (GameUtils.equals(oldIcon, newIcon)==false)
		{
			Key locationKey = (Key)entity.getProperty("containerKey");
			db.sendMainPageUpdateForLocation(locationKey, db.getDB(), new String[]{"updateImmovablesPanel"});
		}
		
	}
	
	
	private void setIconPostfix(String postfix)
	{
		String icon = (String)entity.getProperty("icon");
		
		if (icon!=null)
		{
			if (icon.endsWith(postfix))
				return;
			
			if (icon.contains("-lit.gif"))
				icon = icon.replace("-lit.gif", "");
			else if (icon.contains("-smoke-heavy.gif"))
				icon = icon.replace("-smoke-heavy.gif", "");
			else if (icon.contains("-smoke-light.gif"))
				icon = icon.replace("-smoke-light.gif", "");
			else if (icon.contains("-unlit.gif"))
				icon = icon.replace("-unlit.gif", "");
			
			icon+=postfix;
			entity.setProperty("icon", icon);
		}
	}
	
	public void setIconToLit()
	{
		setIconPostfix("-lit.gif");
	}
	
	public void setIconToSmokeLight()
	{
		setIconPostfix("-smoke-light.gif");
	}
	
	public void setIconToSmokeHeavy()
	{
		setIconPostfix("-smoke-heavy.gif");
	}
	
	public void setIconToUnlit()
	{
		setIconPostfix("-unlit.gif");
	}
	
	/**
	 * Burn time is influenced by 3 major factors
	 * 1. The density of the fuel
	 * 3. How much fuel is being added by weight
	 * 3. How much fuel is already in the fire (current fire intensity). Higher means it will be more wasteful.
	 * 
	 * @param weight
	 * @param space
	 * @return How many seconds of burn time would be added to the fire by adding fuel with the given parameters.
	 */
	private int getBurnTime(double weight, double space)
	{
		// Find the weight to space ratio. High space and low weight means it will burn much more quickly than usual.
		// A low density multiplier (like 0.2) means it is more like tinder or paper.
		double density = weight/space;
		
		
		double units = weight/2000;
		
		double intensityMultiplier = 1d;
		if (getFuelDepletionDate()!=null)
		{
			Long burnMinutesLeft = (getFuelDepletionDate().getTime()-System.currentTimeMillis())/1000/60;
			intensityMultiplier = GameUtils.curveMultiplier(burnMinutesLeft, 10, 120, 1, 0.3);
		}		
		// This starting burn time is for a single, max dense log
		double burnTime = 3600;
		
		burnTime*=units;
		burnTime*=density;
		burnTime*=intensityMultiplier;
		
		return (int)burnTime;
		
	}
	
	/**
	 * 
	 * @return Number between 0 and 1.
	 */
	public Double getFirePercentComplete(long currentTimeInMs)
	{
		if (getFuelDepletionDate()!=null && getFuelStartDate()!=null)
		{
			// Fire is active
			double percentComplete = (((double)currentTimeInMs-getFuelStartDate().getTime()))/(((double)getFuelDepletionDate().getTime()-getFuelStartDate().getTime()));
			return percentComplete;
		}
		return null;
	}
	
	public boolean isFireActive(long currentTimeMs)
	{
		if (getFuelDepletionDate()!=null && getFuelStartDate()!=null)
			return true;
		
		return false;
	}
	
	public boolean isFireExpired(long currentTimeMs)
	{
		if (getFuelDepletionDate()!=null && getFuelStartDate()!=null && getFuelDepletionDate().getTime()<=currentTimeMs)
			return true;
		
		return false;
	}
	
	public long getMinutesSinceLastUpdate(long currentTimeMs)
	{
		if (getFuelStartDate()==null) return 0L;
		return GameUtils.elapsed(Convert.DateToCalendar(new Date(currentTimeMs)), Convert.DateToCalendar(getFuelStartDate()), Calendar.MINUTE);		
	}
	
	public long getMinutesUntilExpired(long currentTimeMs)
	{
		if (getFuelDepletionDate()==null) return 0L;
		return GameUtils.elapsed(Convert.DateToCalendar(new Date(currentTimeMs)), Convert.DateToCalendar(getFuelDepletionDate()), Calendar.MINUTE);		
	}
	
	
	/**
	 * This returns the percentage of ash that would be given per unit of wood.
	 * 
	 * @return Number between 0 and 1.
	 */
	public double getAshPercentage()
	{
		double temperature = 3000d;
		
		// Between 0.43 and 1.82 percent of the fuel remains as ash based on how hot the fire can get. Hotter means less ash. (wikipedia)
		double minAsh = 0.43;
		double maxAsh = 1.82;
		
		double minTemp = 1100;
		double maxTemp = 3000;

		
		if (temperature<=minTemp) temperature = minTemp;
		if (temperature>=maxTemp) temperature = maxTemp; 
		
		double heatPercent = (temperature-minTemp)/(maxTemp-minTemp);
		double ashRange = maxAsh-minAsh;
		
		double ashPercentage = ((ashRange-(heatPercent*ashRange))+minAsh)/100d;
		
		return ashPercentage;
	}
	
	
	public Date getFuelStartDate()
	{
		return (Date)getProperty("fuelStartDate");
	}
	
	public void setFuelStartDate(Date value)
	{
		setProperty("fuelStartDate", value);
	}
	
	public Date getFuelDepletionDate()
	{
		return (Date)getProperty("fuelDepletionDate");
	}

	public void setFuelDepletionDate(Date value)
	{
		setProperty("fuelDepletionDate", value);
	}
	
	public Double getMaxSpace()
	{
		
		return (Double)getProperty("maxSpace");
	}
	
	public void setSpace(Double value)
	{
		setProperty("space", value);
	}
	
	public Double getSpace()
	{
		Double space = (Double)getProperty("space");
		
		if (space==null) space = 0d;
		
		return space;
	}
	
	public Long getMaxTemperature()
	{
		return (Long)getProperty("maxTemperature");
	}
	
	
	
	
	
	static 
	{
		addCommand("FireplaceLight", AspectFireplace.CommandFireplaceLight.class);
		addCommand("FireplaceAddFuel", AspectFireplace.CommandFireplaceAddFuel.class);
	}
	
	
	
	public static class CommandFireplaceLight extends Command
	{

		public CommandFireplaceLight(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
		{
			super(db, request, response);
		}

		@Override
		public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
		{
			Key fireplaceKey = KeyFactory.stringToKey(parameters.get("itemKey"));
			InitiumObject fireplace = db.getInitiumObject(fireplaceKey);
			
			if (fireplace.isAspectPresent(AspectFireplace.class)==false)
				throw new UserErrorMessage("You can only light a fire in a fireplace. The item you selected is not a fireplace.");

			if (CommonChecks.checkItemIsAccessible(fireplace.getEntity(), db.getCurrentCharacter())==false)
				throw new UserErrorMessage("You're not near this item.");

			
			CachedEntity location = db.getEntity((Key)db.getCurrentCharacter().getProperty("locationKey"));
			
			if (CommonChecks.checkIsRaining(location))
				throw new UserErrorMessage("You cannot light a fire while it's raining.");
			
			ODPInventionService inventionService = db.getInventionService(db.getCurrentCharacter(), null);
			AspectFireplace fireplaceAspect = (AspectFireplace)fireplace.getInitiumAspect("Fireplace");

			CachedDatastoreService ds = db.getDB();
			ds.beginBulkWriteMode();

			try
			{
				fireplaceAspect.updateFireProgress();
				
				if (fireplaceAspect.isFireActive(System.currentTimeMillis()))
					throw new UserErrorMessage("You cannot light a fire that is already lit.");
				
				CachedEntity fireplaceLight = db.getEntity("GenericActivity", "FireplaceLight");
				
				GenericEntityRequirementResult gerSlotsToItem = new ConfirmGenericEntityRequirementsBuilder("1", db, this, "doLightFireplace(null, '"+KeyFactory.keyToString(fireplaceKey)+"')", fireplaceLight)
				.addGenericEntityRequirements((String)fireplaceLight.getProperty("genericEntityRequirements1Name"), "genericEntityRequirements1")
				.addGenericEntityRequirements((String)fireplaceLight.getProperty("genericEntityRequirements2Name"), "genericEntityRequirements2")
				.go();
				
				
				
				List<String> slots = new ArrayList<String>(gerSlotsToItem.slots.keySet());
				Collections.sort(slots);
				
				List<Key> tinderKey = gerSlotsToItem.slots.get(slots.get(0));
				List<Key> kindlingKey = gerSlotsToItem.slots.get(slots.get(1));
				List<Key> firestarterKey = gerSlotsToItem.slots.get(slots.get(2));
				List<Key> additionalFirewoodKey = gerSlotsToItem.slots.get(slots.get(3));
				
				EntityPool pool = new EntityPool(db.getDB());
				
				List<Key> gers1 = (List<Key>)fireplaceLight.getProperty("genericEntityRequirements1");
				List<Key> gers2 = (List<Key>)fireplaceLight.getProperty("genericEntityRequirements2");
				
				pool.addToQueue(gers1, gers2);
				
				pool.loadEntities();
				
				inventionService.poolGerSlotsAndSelectedItems(pool, fireplaceLight, gerSlotsToItem.slots);
				
				pool.loadEntities();
				
				List<CachedEntity> tinder = pool.get(tinderKey);
				List<CachedEntity> kindling = pool.get(kindlingKey);
				List<CachedEntity> firestarter = pool.get(firestarterKey);
				List<CachedEntity> additionalFirewood = pool.get(additionalFirewoodKey);
				
				if (tinder==null)
					throw new UserErrorMessage("You need tinder to start a fire.");
				
				if (inventionService.getTotalQuantity(kindling)<3)
					throw new UserErrorMessage("You need at least 3 kindling to start a fire.");
				
				if (firestarter==null)
					throw new UserErrorMessage("You need a firestarter to start a fire.");
				
				
				Map<Key, List<Key>> gerToItems = inventionService.resolveGerSlotsToGers(pool, fireplaceLight, gerSlotsToItem.slots, 1);
				
				inventionService.checkGersMatchItems(pool, gerToItems, 1);
				
				
				// Now light the dang fire!
				fireplaceAspect.setFuelStartDate(new Date());
				fireplaceAspect.setFuelDepletionDate(new Date(System.currentTimeMillis()+1000));	// The spark that lasts 1 second
				fireplaceAspect.setIconPostfix();
				
				fireplaceAspect.addFuel(inventionService, tinder, 1);
				fireplaceAspect.addFuel(inventionService, kindling, 3);
				if (additionalFirewood!=null)
					fireplaceAspect.addFuel(inventionService, additionalFirewood);
				
				// Also use the firestarter
				Long durability = (Long)firestarter.getProperty("durability");
				if (durability!=null)
				{
					long newDurability = durability-1;
					
					// If the quantity we need is less than the amount available, then only consume what we need
					firestarter.setProperty("durability", newDurability);
	
					if (newDurability<1)
						ds.delete(firestarter);
					else
						ds.put(firestarter);
				}
				
				ds.put(fireplace.getEntity());

			}
			finally
			{
				ds.commitBulkWrite();
			}
				
			// Delete all HTML of an item
			if (inventionService.getDeletedEntities()!=null)
				for(Key deletedKey:inventionService.getDeletedEntities())
					if (deletedKey.getKind().equals("Item"))
						deleteHtml(".deletable-Item"+deletedKey.getId());
			
		}
		
	}
	
	public static class CommandFireplaceAddFuel extends Command
	{

		public CommandFireplaceAddFuel(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
		{
			super(db, request, response);
		}

		@Override
		public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
		{
			Key fireplaceKey = KeyFactory.stringToKey(parameters.get("itemKey"));
			InitiumObject fireplace = db.getInitiumObject(fireplaceKey);
			
			if (fireplace.isAspectPresent(AspectFireplace.class)==false)
				throw new UserErrorMessage("You can only light a fire in a fireplace. The item you selected is not a fireplace.");
			
			if (CommonChecks.checkItemIsAccessible(fireplace.getEntity(), db.getCurrentCharacter())==false)
				throw new UserErrorMessage("You're not near this item.");

			AspectFireplace fireplaceAspect = (AspectFireplace)fireplace.getInitiumAspect("Fireplace");
				
			CachedEntity location = db.getEntity((Key)db.getCurrentCharacter().getProperty("locationKey"));
			ODPInventionService inventionService = db.getInventionService(db.getCurrentCharacter(), null);
			
			CachedDatastoreService ds = db.getDB();
			ds.beginBulkWriteMode();
			try
			{
				fireplaceAspect.updateFireProgress();
				
				if (fireplaceAspect.isFireActive(System.currentTimeMillis())==false)
					throw new UserErrorMessage("You can only add fuel to a fire that is already started.");
				
				CachedEntity fireplaceFuel = db.getEntity("GenericActivity", "FireplaceFuel");
				
				GenericEntityRequirementResult gerSlotsToItem = new ConfirmGenericEntityRequirementsBuilder("1", db, this, "doAddFuelToFireplace(null, '"+KeyFactory.keyToString(fireplaceKey)+"')", fireplaceFuel)
				.addGenericEntityRequirements("Material", "genericEntityRequirements1")
				.go();
				
				Collection<Key> fuelList = gerSlotsToItem.slots.values();
				Key fuelKey = fuelList.iterator().next();
				
				
				CachedEntity fuel = db.getEntity(fuelKey);

				fireplaceAspect.addFuel(inventionService, fuel);
				
				
				ds.put(fireplace.getEntity());


			}
			finally
			{
				ds.commitBulkWrite();
			}
			
			// Delete all HTML of an item
			if (inventionService.getDeletedEntities()!=null)
				for(Key deletedKey:inventionService.getDeletedEntities())
					if (deletedKey.getKind().equals("Item"))
						deleteHtml(".deletable-Item"+deletedKey.getId());
			
				
		}

	}

	@Override
	public String getPopupTag()
	{
		return "Can contain a fire";
	}
	
	
}
