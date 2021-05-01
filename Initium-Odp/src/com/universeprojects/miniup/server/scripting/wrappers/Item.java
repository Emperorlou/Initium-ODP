package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ScriptService;

/**
 * Scripting engine wrapper for the Item CachedEntity.
 * 
 * @author spfiredrake
 */
public class Item extends EntityWrapper
{
	public boolean destroyed = false;
	protected EntityWrapper containerEntity;
	
	public Item(CachedEntity item, ODPDBAccess db)
	{
		super(item, db);
	}
	
	public Item(CachedEntity item, ODPDBAccess db, EntityWrapper container)
	{
		super(item, db);
		containerEntity = container;
	}
	
	public EntityWrapper container()
	{
		if(this.containerEntity == null)
		{
			CachedEntity parEnt = db.getEntity((Key)this.getProperty("containerKey"));
			if(parEnt != null)
				containerEntity = ScriptService.wrapEntity(parEnt, db);
		}
		return this.containerEntity;
	}

	public Key getContainerKey() {
		return (Key) this.getProperty("containerKey");
	}
	
	public boolean setContainer(EntityWrapper ent)
	{
		if(GameUtils.equals(getContainerKey(), ent.getKey()))
			return false;
		this.containerEntity = ent;
		setProperty("containerKey", ent.getKey());
		setProperty("movedTimestamp", new Date());
		return true;
	}
	
	private Item[] contents = null;
	public Item[] getContents() {
		Key key = (Key) this.getProperty("containerKey");
		if(key.getKind().equals("Item")) return new Item[0];
		if(contents != null) return contents;
		
		List<CachedEntity> results = db.getFilteredList("Item", "containerKey", getKey());
				
		List<Item> toReturn = new ArrayList<>();
		
		for(CachedEntity ce : results) {
			toReturn.add((Item) ScriptService.wrapEntity(ce, db));
		}
		
		contents = toReturn.toArray(new Item[toReturn.size()]);
		
		return contents;
	}
	
	public Long getQuantity()
	{
		return (Long)this.getProperty("quantity");
	}
	
	public boolean setQuantity(Long newQuantity)
	{
		Long oldQuantity = this.getQuantity();
		if(GameUtils.equals(oldQuantity, newQuantity) || newQuantity < 1) return false;
		this.setProperty("quantity", newQuantity);
		return true;
	}
	
	public boolean adjustQuantity(Long addQty)
	{
		Long newQty = this.getQuantity();
		if(newQty == null) return false;
		newQty += addQty;
		this.setProperty("quantity", newQty);
		return newQty < 1;
	}
	
	public Long getDurability()
	{
		return (Long)this.getProperty("durability");
	}
	
	public Long getMaxDurability()
	{
		return (Long)this.getProperty("maxDurability");
	}
	
	public boolean setDurability(Long newDura)
	{
		if(GameUtils.equals(getDurability(), newDura)) return false;
		this.setProperty("durability", newDura);
		return true;
	}
	
	/**
	 * Adjusts the durability by the specified amount.
	 * @param addDura Amount to add (or subtract) from durability.
	 * @return Whether the item needs to be destroyed.
	 */
	public boolean adjustDurability(Long addDura)
	{
		Long newDura = getDurability();
		Long maxDura = getMaxDurability();
		if(newDura == null) return false;
		if(maxDura == null) maxDura = newDura;
		newDura += addDura;
		this.setProperty("durability", Math.min(newDura, maxDura));
		return newDura < 0;
	}
	
	public boolean isKeyItem()
	{
		return this.getProperty("keyCode") != null;
	}
	
	public boolean isValidKeyCode(Long keyCode)
	{
		if(keyCode == null) return false;
		if(!isKeyItem()) return false;
		return GameUtils.equals(this.getProperty("keyCode"), keyCode);
	}
	
	public Date getCreatedDate()
	{
		return (Date)this.getProperty("createdDate");
	}
	
	public String renderItem() {
		return renderItem(false);
	}
	
	public String renderItem(boolean popupEmbedded)
	{
		EntityWrapper owner = this.container();
		CachedEntity ownerEntity = owner == null ? db.getCurrentCharacter() : owner.wrappedEntity;
		return GameUtils.renderItem(this.db, db.getRequest(), ownerEntity, this.wrappedEntity, popupEmbedded, false);
	}
}
