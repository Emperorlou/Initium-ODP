package com.universeprojects.miniup.server.aspects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.GridMapService;

public class AspectPickable extends ItemAspect
{

	public AspectPickable(InitiumObject object)
	{
		super(object);
	}

	@Override
	public List<ItemPopupEntry> getItemPopupEntries()
	{
		AspectGridMapObject gmoAspect = getObject().getAspect(AspectGridMapObject.class);
		List<ItemPopupEntry> result = null;
		if (gmoAspect!=null && gmoAspect.isAttached())
		{
			String itemKey = null;
			String proceduralKey = null;
			if (getObject().getKey().isComplete())
				itemKey = getObject().getKey().toString();
			else
				proceduralKey = (String)getObject().getEntity().getAttribute("proceduralKey");
			
			result = new ArrayList<>();
			ItemPopupEntry ipe = new ItemPopupEntry("Pick", 
					"You can pick this plant from the ground, it will go into your inventory when you do.", 
					"doCommand(event, 'PickablePick', {itemKey:'"+itemKey+"', proceduralKey:'"+proceduralKey+"'});");
			result.add(ipe);
		}
		return result;
	}

	@Override
	public String getPopupTag()
	{
		AspectGridMapObject gmoAspect = getObject().getAspect(AspectGridMapObject.class);
		if (gmoAspect!=null && gmoAspect.isAttached())
			return "Pickable";
		
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
			String itemKeyStr = parameters.get("itemKey");
			String proceduralKeyStr = parameters.get("proceduralKey");
			CachedEntity itemEntity = null;
			GridMapService gmService = null;
			if (itemKeyStr!=null && itemKeyStr.equals("null")==false)
			{
				Key itemKey = db.stringToKey(itemKeyStr);
				itemEntity = db.getEntity(itemKey);
			}
			else if (proceduralKeyStr!=null && proceduralKeyStr.equals("null")==false)
			{
				CachedEntity location = db.getCharacterLocation(db.getCurrentCharacter());
				gmService = new GridMapService(db, location);
				itemEntity = gmService.generateSingleItemFromProceduralKey(db, location, proceduralKeyStr);
			}
			if (itemEntity==null) throw new UserErrorMessage("I couldn't find the thing you wanted to pick.");
			InitiumObject item = new InitiumObject(db, itemEntity);
			
			// Check to make sure the item we're trying to pick actually has the aspects we require
			if (item.hasAspect(AspectGridMapObject.class)==false || item.hasAspect(AspectPickable.class)==false)
				throw new UserErrorMessage("You cannot pick this object.");
			
			db.doCharacterCollectItem(db.getCurrentCharacter(), itemEntity, true);
			
			ds.put(itemEntity);

			// Now that the entity has been saved, we need to update the location data
			
			gmService.removeEntity(itemEntity);
			gmService.putLocationData(ds);
			
			
			if (itemKeyStr!=null && itemKeyStr.equals("null")==false)
			{
				deleteHtml(".tileContentsItem[ref='"+itemKeyStr+"']");
				deleteHtml(".gridObject[id*='"+itemKeyStr+"']");
			}
			else if (proceduralKeyStr!=null && proceduralKeyStr.equals("null")==false)
			{
				deleteHtml(".tileContentsItem[ref='"+proceduralKeyStr+"']");
				deleteHtml(".gridObject[id*='"+proceduralKeyStr+"']");
			}

			addJavascriptToResponse(db.getGridMapService().generateGridObjectJson(getSelectedTileX().intValue(), getSelectedTileY().intValue()));
		}
		
	}
}
