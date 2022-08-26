package com.universeprojects.miniup.server.services;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class CustomizationStoreService extends Service {

	public CustomizationStoreService(ODPDBAccess db) {
		super(db);
	}
	

	public List<CachedEntity> getBuyableList() {
		return getBuyableList(null);
	}	
	
	
	public List<CachedEntity> getBuyableList(CachedEntity item) {
		
		List<CachedEntity> list = query.getFilteredList("CustomizationStoreItemDef", "status", "Live");

		// Go through the list and remove any options that are not compatible with the given item (if one is given)
		if (item != null) {
			for(int i = list.size() - 1; i>=0; i--) {
				CachedEntity entry = list.get(i);
				if (isBuyableCompatibleWithItem(entry, item) == false)
					list.remove(i);
			}			
		}
		
		// If any of these sales have expired, we're going to update the item's status to be expired now
		Date currentDate = new Date();
		for(int i = list.size() - 1; i>=0; i--) {
			CachedEntity entry = list.get(i);
			if (entry.getProperty("saleExpiryDate") != null && ((Date) entry.getProperty("saleExpiryDate")).before(currentDate)) {
				entry.setProperty("status", "Expired");
				ds.put(entry);
				list.remove(i);
			}
		}
		ds.commitBulkWrite();
		
		
		return list;
	}
	
	public boolean isBuyableCompatibleWithItem(CachedEntity buyable, CachedEntity item) {
		String appliedEquipSlot = (String) buyable.getProperty("appliedEquipSlot");
		String appliedItemType = (String) buyable.getProperty("appliedItemType");
		String appliedDamageType = (String) buyable.getProperty("appliedDamageType");
		HashSet<String> compatibleEquipSlots = new HashSet<>();
		HashSet<String> compatibleItemTypes = new HashSet<>();
		HashSet<String> compatibleDamageTypes = new HashSet<>();
		
		if (appliedEquipSlot != null) compatibleEquipSlots.addAll(Arrays.asList(appliedEquipSlot.split("\\s*,\\s*")));
		if (appliedItemType != null) compatibleItemTypes.addAll(Arrays.asList(appliedItemType.split("\\s*,\\s*")));
		if (appliedDamageType != null) compatibleDamageTypes.addAll(Arrays.asList(appliedDamageType.split("\\s*,\\s*")));
		
		String itemItemType = (String) item.getProperty("itemType");
		String itemDamageType = (String) item.getProperty("weaponDamageType");
		String[] itemEquipSlots = item.getProperty("equipSlot") != null ? ((String)item.getProperty("equipSlot")).split(",") : null;
		
		if (compatibleEquipSlots.isEmpty() == false) {
			boolean success = false;
			if (itemEquipSlots != null) for(String slot:itemEquipSlots) if (compatibleEquipSlots.contains(slot)) {
				success = true;
				break;
			}
			if (success == false) return false;
		}
		
		if (compatibleItemTypes.isEmpty() == false && compatibleItemTypes.contains(itemItemType) == false) {
			return false;
		}
		
		if (compatibleDamageTypes.isEmpty() == false && compatibleDamageTypes.contains(itemDamageType) == false) {
			return false;
		}
		
		return true;
	}

}
