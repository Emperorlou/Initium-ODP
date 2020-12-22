package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ContainerService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * Equips a set of items from container and puts previously equipped items into
 * the container.
 * 
 * @author Malzawar
 * 
 */
public class CommandCharacterEquipSet extends Command {

	public CommandCharacterEquipSet(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");

		ContainerService cs = new ContainerService(db);
		Long containerId = tryParseId(parameters, "containerId");
		final Key containerKey = KeyFactory.createKey("Item", containerId);
		CachedEntity container = db.getEntity(containerKey);

		//Don't add toEquip if we cannot wear it.
		if (character == null)
			throw new IllegalArgumentException("Character cannot be null.");
		
		if (cs.checkContainerAccessAllowed(character, container, true) == false)
			throw new UserErrorMessage(
					"You do not have access to this container.");

		final List<CachedEntity> setEquip = db.getFilteredList("Item",
				"containerKey", FilterOperator.EQUAL, containerKey);

		if (setEquip.size() == 0)
			throw new UserErrorMessage("The container is empty");

		// Checking that the container we were given doesn't have any equipment
		// duplicates.
		for (int i = 0; i < setEquip.size(); i++) {
			for (int j = i + 1; j < setEquip.size(); j++) { // Double for loop
															// to compare every
															// item to every
															// other item not
															// yet compared to
				if (GameUtils.equals(setEquip.get(i).getKey(), setEquip.get(j)
						.getKey())) {
					throw new UserErrorMessage(
							"There are duplicate equipment items in the container. Id: "
									+ setEquip.get(i).getKey().getId());
				}
			}
		}

		// Sort the items in container and get only one for each slot for
		// equipping
		Collections.sort(setEquip, new Comparator<CachedEntity>() {
			@Override
			public int compare(CachedEntity e1, CachedEntity e2) {

				if (e1.getProperty("movedTimestamp") == null
						|| e2.getProperty("movedTimestamp") == null) {
					return 0;
				}

				return ((Date) e2.getProperty("movedTimestamp"))
						.compareTo((Date) e1.getProperty("movedTimestamp"));
			}
		});

		List<CachedEntity> toEquip = new ArrayList<CachedEntity>();
		String[] tempArray = ODPDBAccess.EQUIPMENT_SLOTS;
		List<String> slotList = new ArrayList<String>(Arrays.asList(tempArray));

		// "Helmet", "Chest", "Shirt", "Gloves", "Legs", "Boots", "RightHand",
		// "LeftHand", "RightRing", "LeftRing", "Neck"
		
		
		
		Double characterStrength = (Double) character.getProperty("strength");
		// Round character strength just like it is rounded for the popup
		characterStrength = Double.parseDouble(GameUtils
				.formatNumber(characterStrength));

		for (CachedEntity equipment : setEquip) {

			if (slotList == null || slotList.size() == 0) {
				break;
			}
			
			if (equipment.getProperty("strengthRequirement") instanceof String)
				equipment.setProperty("strengthRequirement", null);
			Double strengthRequirement = (Double) equipment
					.getProperty("strengthRequirement");
			if (strengthRequirement != null && characterStrength != null
					&& strengthRequirement > characterStrength
					&& "NPC".equals(character.getProperty("type")) == false)
				continue;
			
			String equipSlotRaw = (String) equipment.getProperty("equipSlot");

			if (equipSlotRaw == null){
				continue;
			}
			
			if (equipSlotRaw.equals("Ring"))
				equipSlotRaw = "LeftRing, RightRing";
			
			if (equipSlotRaw.equals("2Hands"))
				equipSlotRaw = "LeftHand and RightHand";
				

			equipSlotRaw = equipSlotRaw.trim();
			if (equipSlotRaw.endsWith(","))
				equipSlotRaw = equipSlotRaw.substring(0,
						equipSlotRaw.length() - 1);
			String[] equipSlotArr = equipSlotRaw.split(",");

			if (equipSlotArr.length == 0)
				throw new RuntimeException("No equip slots exist for the '"
						+ equipment.getProperty("name") + "' item.");

			else if (equipSlotArr.length == 1) {

				String[] equipSlotArrAnd = equipSlotArr[0].split(" and ");
				if (equipSlotArrAnd.length == 1) {

					String destinationSlot = equipSlotArrAnd[0].trim();
					if (slotList.contains(destinationSlot)) {
						slotList.remove(destinationSlot);
						toEquip.add(equipment);
					}

				} else if (equipSlotArrAnd.length > 1) {

					boolean equippable = true;
					for (String andSlot : equipSlotArrAnd) {
						if (!slotList.contains(andSlot)) {
							equippable = false;
							break;
						}
					}
					if (equippable) {
						slotList.removeAll(Arrays.asList(equipSlotArrAnd));
						toEquip.add(equipment);
					}
				}
			} else if (equipSlotArr.length > 1) {
				for (int i = 0; i < equipSlotArr.length; i++) {
					String destinationSlot = equipSlotArr[i].trim();
					if (slotList.contains(destinationSlot)) {
						slotList.remove(destinationSlot);
						toEquip.add(equipment);
						break;
					}
				}
			}
		}
		
		// Get our current equipment
		List<CachedEntity> currentEquipment = new ArrayList<CachedEntity>();
		for (String slot : ODPDBAccess.EQUIPMENT_SLOTS) {
			if (character.getProperty("equipment" + slot) != null) {
				currentEquipment.add(db.getEntity((Key) character
						.getProperty("equipment" + slot)));
			}
		}

		Long containerMaxWeight = ((Long) container.getProperty("maxWeight"));
		Long currentEquipmentWeight = db.getItemCarryingWeight(character,
				currentEquipment);
		Long toEquipEquipmentWeight = db.getItemCarryingWeight(container,
				toEquip);
		Long containerRemainingWeight = containerMaxWeight
				- db.getItemCarryingWeight(container, setEquip);

		Long containerMaxSpace = ((Long) container.getProperty("maxSpace"));
		Long currentEquipmentSpace = db.getItemCarryingSpace(character,
				currentEquipment);
		Long toEquipEquipmentSpace = db
				.getItemCarryingSpace(container, toEquip);
		Long containerRemainingSpace = containerMaxSpace
				- db.getItemCarryingSpace(container, setEquip);

		if (containerRemainingWeight < (currentEquipmentWeight - toEquipEquipmentWeight)) {
			throw new UserErrorMessage(
					"Cannot swap out set, not enough free weight in the container");
		}

		if (containerRemainingSpace < (currentEquipmentSpace - toEquipEquipmentSpace)) {
			throw new UserErrorMessage(
					"Cannot swap out set, not enough free space in the container");
		}

		// Unequip all equipment we already have equipped and put them in the
		// container.
		ds.beginBulkWriteMode();

		for (CachedEntity equipment : currentEquipment) {

			db.doCharacterUnequipEntity(ds, character, equipment);
			equipment.setProperty("containerKey", containerKey);
			equipment.setProperty("movedTimestamp", new Date());

			ds.put(equipment);
		}

		// Equip the set from the container
		for (CachedEntity equipment : toEquip) {
			equipment.setProperty("containerKey", character.getKey());
			equipment.setProperty("movedTimestamp", new Date());
			ds.put(equipment);

			db.doCharacterEquipEntity(ds, character, equipment);
		}

		ds.put(character);
		ds.commitBulkWrite();
		
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, db.getCurrentUser(), character, null, this);
		mpus.updateInBannerCharacterWidget();
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}
}
