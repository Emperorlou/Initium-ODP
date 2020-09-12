package com.universeprojects.miniup.server.aspects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.InitiumAspect;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.ItemAspect.ItemPopupEntry;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * 
 * @author Evan
 *
 */
public class AspectSlotted extends ItemAspect {

	public AspectSlotted(InitiumObject object) {
		super(object);
	}

	//Here, we need to properly set the link. use aspectpet as a base.
	@Override
	public List<ItemPopupEntry> getItemPopupEntries(CachedEntity currentCharacter)
	{
		if (CommonChecks.checkItemIsAccessible(object.getEntity(), currentCharacter) && hasOpenSlots()) {
			ItemPopupEntry ipe = new ItemPopupEntry("Insert an item into an available slot", "Insert a slottable item into this item.",
					"viewSlottableItems(event, " + object.getKey().getId() + ")");
			return Arrays.asList(new ItemPopupEntry[]
			{
					ipe
			});
		}
		else return null;
	}

	@Override
	public String getPopupTag() {
		if (getMaxCount()>0)
			return "Slotted";
		else
			return null;
	}

	/**
	 * Returns the number of max slots on this aspect.
	 * @return
	 */
	public long getMaxCount() {
		Long count = (Long)getProperty("maxCount");
		if (count==null) count = 0L;
		return count;
	}
	
	/**
	 * Returns true if this aspect has any open slots.
	 * @return
	 */
	public boolean hasOpenSlots() {
		return getOpenCount() > 0;
	}
	
	/**
	 * Returns the number of open slots on this aspect.
	 * @return
	 */
	public long getOpenCount() {
		Long max = getMaxCount();
		
		@SuppressWarnings("unchecked")
		List<EmbeddedEntity> currentlySlotted = (List<EmbeddedEntity>)getProperty("slotItems");
		
		if(currentlySlotted == null) return max;
		
		return max-currentlySlotted.size();
	}
	
	/**
	 * Returns a map the slotted items and their slottable aspect.
	 * Returns null if there are no slotted items.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<InitiumObject, AspectSlottable> getSlottedItems(){
		
		Map<InitiumObject, AspectSlottable> toReturn = new HashMap<>();
		
		List<InitiumObject> slottedItems = getOnlySlottedItems();
		
		if(slottedItems == null) return null;
		
		for(InitiumObject slotItem:slottedItems) {
			AspectSlottable slottableAspect = slotItem.getAspect(AspectSlottable.class);
			
			if(slottableAspect == null) continue;
			
			toReturn.put(object, slottableAspect);
		}
		
		return toReturn;
	}
	
	/**
	 * getSlottedItems returns a map of the slotted items and their aspect.
	 * This returns ONLY the slotted items.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<InitiumObject> getOnlySlottedItems(){
		
		List<EmbeddedEntity> emEnts = (List<EmbeddedEntity>) getProperty("slotItems");
		List<InitiumObject> toReturn = new ArrayList<>();
		
		if(emEnts == null) return null;
		
		for(EmbeddedEntity item:emEnts) {
			toReturn.add(new InitiumObject(db, item));
		}
		
		return toReturn;
	}
	
	/**
	 * @param object
	 */
	private void addItemToSlot(InitiumObject object) {
		List<InitiumObject> currentSlots = getOnlySlottedItems();
		
		//TODO: How to embed an entity?
	}
	
	static {
		addCommand("InsertItemToSlot", AspectSlotted.CommandInsertItemToSlot.class);
	}
	
	
	/**
	 * given a slotted item and a slottable item, insert it into the slot and apply the
	 * appropriate modifiers to the parent item.
	 * @author Evan
	 *
	 */
	public static class CommandInsertItemToSlot extends Command {

		public CommandInsertItemToSlot(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
			super(db, request, response);
		}
		
		//Pass the ID of the item with slots and the item we're slotting in. We will need to sanitize it.
		@Override
		public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException{
			
			//generate the base item from the given ID, then do basic checks.
			Key baseKey = KeyFactory.createKey("Item", Long.parseLong(parameters.get("baseId")));
			InitiumObject baseItem = db.getInitiumObject(baseKey);
			
			if(baseItem == null) throw new UserErrorMessage("Invalid Item ID.");
			if(!CommonChecks.checkItemIsAccessible(baseItem.getEntity(), db.getCurrentCharacter()))
				throw new UserErrorMessage("This item isn't accessible right now.");
			
			AspectSlotted slottedAspect = (AspectSlotted) baseItem.getInitiumAspect("Slotted");
			if(slottedAspect == null) throw new UserErrorMessage("This item has no slots.");	
			if(!slottedAspect.hasOpenSlots()) throw new UserErrorMessage("All slots on this item are full.");
			
			
			//generate the slottable item from the given ID, then do basic checks.
			Key slottableKey = KeyFactory.createKey("Item", Long.parseLong(parameters.get("slottableId")));
			InitiumObject slottableItem = db.getInitiumObject(slottableKey);
			
			if(slottableItem == null) throw new UserErrorMessage("Invalid Item ID");
			if(!CommonChecks.checkItemIsAccessible(slottableItem.getEntity(), db.getCurrentCharacter()))
				throw new UserErrorMessage("This item isn't accessible right now.");
			
			AspectSlottable slottableAspect = (AspectSlottable) slottableItem.getInitiumAspect("Slottable");
			if(slottableAspect == null) throw new UserErrorMessage("This item can't be inserted into a slot.");
			
			//we've made it this far, which means the base item and the slottable item are both completely valid.
			//time to actually insert the item.
			slottedAspect.addItemToSlot(slottableItem);
			
			db.delete(slottableItem.getEntity().getKey());
			deleteHtml(".deletable-Item" + slottableItem.getEntity().getKey());
		}
	}
	

	/**
	 * Private.... for now?
	 * @author Evan
	 *
	 */
	private static class CommandRemoveAllInsertedItems extends Command {
		public CommandRemoveAllInsertedItems(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
			super(db, request, response);
		}

		@Override
		public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {
			return;
			// TODO Auto-generated method stub
			
			//validate the item that we're removing the slotted items from.
			
			//Convert the embedded entities to real entities
		}
	}
}