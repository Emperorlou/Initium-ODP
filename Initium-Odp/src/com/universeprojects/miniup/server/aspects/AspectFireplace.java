package com.universeprojects.miniup.server.aspects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ConfirmGenericEntityRequirementsBuilder;
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

		itemPopupEntries.add(new ItemPopupEntry("Light Fire", 
				"Using kindling and something to start the fire, this command will light a fire here.", 
				"doLightFireplace(event, '"+entity.getUrlSafeKey()+"');"));
		
		itemPopupEntries.add(new ItemPopupEntry("Add Fuel", 
				"Add something flammable to the fire to keep it going.", 
				"doFireplaceAddFuel(event, '"+entity.getUrlSafeKey()+"');"));
		
		return itemPopupEntries;
	}
	
	public void addFuel(CachedEntity firewoodEntity) throws UserErrorMessage
	{
		InitiumObject firewood = new InitiumObject(db, firewoodEntity);
		if (CommonChecks.checkItemIsClass(firewoodEntity, "Firewood")==false ||
				firewood.isAspectPresent(AspectFlammable.class)==false)
			throw new UserErrorMessage("The item you chose to add to the fire is not valid fuel.");
	
//		Long fuelSpace = (Long)firewood.getProperty("space");
	}
	
	
	public Date getFuelDepletionDate()
	{
		return (Date)entity.getProperty("fuelDepletionDate");
	}
	
	public Long getMaxSpace()
	{
		return (Long)entity.getProperty("maxSpace");
	}
	
	public Long getSpace()
	{
		return (Long)entity.getProperty("space");
	}
	
	public Long getMaxTemperature()
	{
		return (Long)entity.getProperty("maxTemperature");
	}
	
	
	
	
	
	static 
	{
		addCommand("LightFireplace", AspectFireplace.CommandLightFireplace.class);
		addCommand("FireplaceAddFuel", AspectFireplace.CommandFireplaceAddFuel.class);
	}
	
	public static class CommandLightFireplace extends Command
	{

		public CommandLightFireplace(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
		{
			super(db, request, response);
		}

		@Override
		public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
		{
			Key fireplaceKey = KeyFactory.stringToKey(parameters.get("itemKey"));
			InitiumObject fireplace = db.getInitiumObject(fireplaceKey);
			
			if (fireplace.isAspectPresent(AspectFireplace.class))
			{
				ItemAspect fireplaceAspect = (ItemAspect)fireplace.getInitiumAspect("Fireplace");
//				fireplaceAspect.get
			}
			else
				throw new UserErrorMessage("You can only light a fire in a fireplace. The item you selected is not a fireplace.");
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
			String itemKey = parameters.get("itemKey");
			CachedEntity fireplaceFuel = db.getEntity("GenericActivity", "FireplaceFuel");
			
			Map<String, Key> gerSlotsToItem = new ConfirmGenericEntityRequirementsBuilder("1", db, this, "doAddFuelToFireplace(null, '"+itemKey+"')", fireplaceFuel)
			.addGenericEntityRequirements("Material", "genericEntityRequirements1")
			.addGenericEntityRequirements("Material", "genericEntityRequirements1")
			.addGenericEntityRequirements("Material", "genericEntityRequirements1")
			.addGenericEntityRequirements("Material", "genericEntityRequirements1")
			.go();
			
			Collection<Key> fuel = gerSlotsToItem.values();
			
			EntityPool pool = new EntityPool(db.getDB());
			ODPInventionService inventionService = db.getInventionService(db.getCurrentCharacter(), null);
			
			inventionService.resolveGerSlotsToGers(pool, fireplaceFuel, gerSlotsToItem);
			
//			inventionService.checkIdeaWithSelectedItems(pool, idea, itemRequirementsToItems)
		}

	}
	
	
}
