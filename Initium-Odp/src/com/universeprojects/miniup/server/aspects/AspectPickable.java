package com.universeprojects.miniup.server.aspects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.GridMapService;

public class AspectPickable extends ItemAspect
{
	enum Type
	{
		PickPlant,
		CollectGeneric
	}

	public AspectPickable(InitiumObject object)
	{
		super(object);
	}

	@Override
	public List<ItemPopupEntry> getItemPopupEntries(CachedEntity currentCharacter)
	{
		if (isEnabled()==false) return null;
		
		AspectGridMapObject gmoAspect = getObject().getAspect(AspectGridMapObject.class);
		List<ItemPopupEntry> result = null;
		if (gmoAspect!=null && gmoAspect.isAttached() && this.isEnabled())
		{
			String itemKey = null;
			String proceduralKey = null;
			if (getObject().getKey().isComplete())
				itemKey = getObject().getKey().toString();
			else
				proceduralKey = (String)getObject().getEntity().getAttribute("proceduralKey");
			
			result = new ArrayList<>();
			
			if (getType()==Type.PickPlant)
			{
				ItemPopupEntry ipe = new ItemPopupEntry("Pick", 
						"You can pick this plant from the ground, it will go into your inventory when you do.", 
						"doCommand(event, 'PickablePick', {itemKey:'"+itemKey+"', proceduralKey:'"+proceduralKey+"'});");
				result.add(ipe);
			}
			else if (getType()==Type.CollectGeneric)
			{
				ItemPopupEntry ipe = new ItemPopupEntry("Collect", 
						"You can collect this, it will go into your inventory when you do.", 
						"doCommand(event, 'PickablePick', {itemKey:'"+itemKey+"', proceduralKey:'"+proceduralKey+"'});");
				result.add(ipe);
			}
		}
		return result;
	}

	@Override
	public String getPopupTag()
	{
		if (isEnabled()==false) return null;
		
		if (getType()==Type.PickPlant)
		{
			AspectGridMapObject gmoAspect = getObject().getAspect(AspectGridMapObject.class);
			if (gmoAspect!=null && gmoAspect.isAttached())
				return "Pickable";
		}
		else if (getType()==Type.CollectGeneric)
		{
			return "Collectable";
		}
		
		return null;
	}

	static
	{
		addCommand("PickablePick", AspectPickable.CommandPickablePick.class);
	}	
	
	
	public static class CommandPickablePick extends Command
	{

		public CommandPickablePick(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
		{
			super(db, request, response);
		}

		@Override
		public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
		{
			String message = "";
			boolean isProceduralPick = false;
			String itemKeyStr = parameters.get("itemKey");
			String proceduralKeyStr = parameters.get("proceduralKey");
			CachedEntity itemEntity = null;
			CachedEntity location = db.getCharacterLocation(db.getCurrentCharacter());
			GridMapService gmService = new GridMapService(db, location);
			if (itemKeyStr!=null && itemKeyStr.equals("null")==false)
			{
				Key itemKey = db.stringToKey(itemKeyStr);
				itemEntity = db.getEntity(itemKey);
			}
			else if (proceduralKeyStr!=null && proceduralKeyStr.equals("null")==false)
			{
				itemEntity = gmService.generateSingleItemFromProceduralKey(db, location, proceduralKeyStr);
				isProceduralPick = true;
			}
			if (itemEntity==null) throw new UserErrorMessage("I couldn't find the thing you wanted to pick/collect.");
			InitiumObject item = new InitiumObject(db, itemEntity);
			
			// Check to make sure the item we're trying to pick actually has the aspects we require
//			if (item.hasAspect(AspectGridMapObject.class)==false || item.hasAspect(AspectPickable.class)==false)
//				throw new UserErrorMessage("You cannot pick this object.");
			
			AspectPickable pickable = item.getAspect(AspectPickable.class);
			
			if (pickable!=null && pickable.isEnabled()==false)
				throw new UserErrorMessage("You cannot pick/collect this object. At least not in it's current state.");
			
			if (item.hasAspect(AspectGridMapObject.class) && item.getAspect(AspectGridMapObject.class).isLoose())
				throw new UserErrorMessage("You cannot pick/collect this item. Perhaps try just picking it up the regular way?");
			
			// Right away lets set this to not pickable since we're going to be picking it now
			if (pickable!=null)
				pickable.setProperty("enabled", false);
			
			// Here we'll pick up the entity - if we're supposed to...
			
			if (pickable==null || pickable.isToDelete())
			{
				if (isProceduralPick)
				{
					gmService.removeProceduralEntity(proceduralKeyStr);
				}
				else
					ds.delete(itemEntity);
			}
			else
			{
				pickable.setProperty("enabled", false);
			}
			
			if (pickable!=null && pickable.isNotActuallyPickingUpEntity()==false)
			{
				db.doCharacterCollectItem(this, db.getCurrentCharacter(), itemEntity, true);
				ds.put(itemEntity);
				
				message = addToCollectedMessage(message, itemEntity);
			}

			// Now modify the entity, if we're supposed to
			if (pickable!=null && pickable.isToDelete()==false)
			{
				boolean changed = db.performModifierTypeOperation(itemEntity, itemEntity, "entityModifier", "entityModifierEmbedded");
				
				if (changed && isProceduralPick)
				{
					gmService.removeProceduralEntity(proceduralKeyStr);
					ds.put(item.getEntity());
					gmService.addEntity(item.getEntity());
				}
			}
			// Now create the additional entities, if we're supposed to
			if (pickable!=null && pickable.getAdditionalItems()!=null)
			{
				List<CachedEntity> newItems = new ArrayList<>();
				List<CachedEntity> list = ds.get(pickable.getAdditionalItems());
				for(CachedEntity itemDef:list)
				{
					Integer multiplier = (int)Math.round((Double)pickable.getAdditionalItemsQuantityMultiplier());
					CachedEntity newItem = db.generateNewObject(itemDef, "Item");
					newItem.setProperty("containerKey", location.getKey());
					newItem.setProperty("gridMapPositionX", getSelectedTileX());
					newItem.setProperty("gridMapPositionY", getSelectedTileY());
					
					if (newItem.getProperty("quantity")!=null)
					{
						Long quantity = (Long)newItem.getProperty("quantity");
						Long newQuantity = quantity*multiplier;
						
						if (newQuantity<=0) continue;	// If we're about to create a thing with <1 quantity, lets not create it
						
						newItem.setProperty("quantity", newQuantity);
					}
					
					db.doCharacterCollectItem(this, db.getCurrentCharacter(), newItem);
					
					newItems.add(newItem);

					message = addToCollectedMessage(message, newItem);
				}
				
				ds.put(newItems);
			}

			gmService.regenerateTile(this);
		}

		private String addToCollectedMessage(String message, CachedEntity itemEntity)
		{
			if (message==null || message.equals(""))
			{
				message = "You collected ";
				message += GameUtils.renderItem(itemEntity);
			}
			else
			{
				message += ", "+GameUtils.renderItem(itemEntity);
			}
			
			return message;
		}
		
	}
	
	
	
	public Type getType()
	{
		if (getProperty("type")==null) return Type.PickPlant;
		
		return Type.valueOf((String)getProperty("type"));
	}
	
	public double getAdditionalItemsQuantityMultiplier()
	{
		String rawCurve = (String)getProperty("additionalItemsQuantityMultiplier");
		if (rawCurve==null || rawCurve.equals("")) return 1d;
		Double solvedCurve = (Double)db.solveCurve(rawCurve);
		if (solvedCurve==null) return 1d;
		
		return solvedCurve;
		
	}

	public List<Key> getAdditionalItems()
	{
		return (List<Key>)getProperty("additionalItems");
	}
	
	public boolean isNotActuallyPickingUpEntity()
	{
		if (getProperty("doNotActuallyPickupEntity")==null) return false;
		
		return (Boolean)getProperty("doNotActuallyPickupEntity");
	}
	
	public boolean isEnabled()
	{
		if (getProperty("enabled")==null) return true;
		
		return (Boolean)getProperty("enabled");
	}
	
	public boolean isToDelete()
	{
		if (getProperty("deleteEntity")==null) return true;
		
		return (Boolean)getProperty("deleteEntity");
	}
	
	public Key getEntityModifier()
	{
		return (Key)getProperty("entityModifier");
	}
	
	public EmbeddedEntity getEntityModifierEmbedded()
	{
		return (EmbeddedEntity)getProperty("entityModifierEmbedded");
	}
	
	
}
