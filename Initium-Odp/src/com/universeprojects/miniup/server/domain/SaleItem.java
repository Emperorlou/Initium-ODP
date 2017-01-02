package com.universeprojects.miniup.server.domain;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

//This entity is used to reference an item in a character's inventory that is for sale.
public class SaleItem extends OdpDomain {

	public SaleItem() {
		super(new CachedEntity("SaleItem"));
	}

	public SaleItem(CachedEntity cachedEntity) {
		super(cachedEntity, "SaleItem");
	}

	// (Character)
	public void setCharacterKey(Key characterKey) {
		getCachedEntity().setProperty("characterKey", characterKey);
	}

	public Key getCharacterKey() {
		return (Key) getCachedEntity().getProperty("characterKey");
	}

	// The amount this item is being sold for.
	public void setDogecoins(Long dogecoins) {
		getCachedEntity().setProperty("dogecoins", dogecoins);
	}

	public Long getDogecoins() {
		return (Long) getCachedEntity().getProperty("dogecoins");
	}

	// (Item)
	public void setItemKey(Key itemKey) {
		getCachedEntity().setProperty("itemKey", itemKey);
	}

	public Key getItemKey() {
		return (Key) getCachedEntity().getProperty("itemKey");
	}

	// Just a name given to the SaleItem to make it easier to view in the editor.
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	// If the item has been sold, this is the date the transaction took place.
	public void setSoldDate(String soldDate) {
		getCachedEntity().setProperty("soldDate", soldDate);
	}

	public String getSoldDate() {
		return (String) getCachedEntity().getProperty("soldDate");
	}

	// (Character)
	public void setSoldTo(Character soldTo) {
		getCachedEntity().setProperty("soldTo", soldTo);
	}

	public Character getSoldTo() {
		return (Character) getCachedEntity().getProperty("soldTo");
	}

	// This specialized field is used in some cases to identify this item by a particular special category or type. For example, premium tokens will be listed as a a particular special ID because we need to find all the premium tokens being sold for the global exchange.
	public void setSpecialId(String specialId) {
		getCachedEntity().setProperty("specialId", specialId);
	}

	public String getSpecialId() {
		return (String) getCachedEntity().getProperty("specialId");
	}

	public enum Status {
		Selling, Sold, Hidden,
	}

	public void setStatus(Status status) {
		getCachedEntity().setProperty("status", status);
	}

	public Status getStatus() {
		return (Status) getCachedEntity().getProperty("status");
	}

}
