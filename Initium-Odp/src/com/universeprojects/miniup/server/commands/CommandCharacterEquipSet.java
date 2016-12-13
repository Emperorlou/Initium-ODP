package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
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
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ContainerService;

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

		ContainerService cs = new ContainerService(db);
		Long containerId = tryParseId(parameters, "containerId");
		final Key containerKey = KeyFactory.createKey("Item", containerId);
		CachedEntity container = db.getEntity(containerKey);

		if (cs.checkContainerAccessAllowed(db.getCurrentCharacter(), container) == false)
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
							"There are duplicate equipment items in the container. Id: "+setEquip.get(i).getKey().getId());
				}
				/*
				if(setEquip.get(i).getKey().getName().equals(setEquip.get(j).getKey().getName())){
					throw new UserErrorMessage(
							"There are duplicate equipment items in the container. Name: "+setEquip.get(i).getKey().getName());
				}
				// Testing how to check for duplicate items. */
			}
		}

		
		// Check if we can equip everything from the given container

		Double characterStrength = (Double) character.getProperty("strength");
		// Round character strength just like it is rounded for the popup
		characterStrength = Double.parseDouble(GameUtils
				.formatNumber(characterStrength));

		for (CachedEntity equipment : setEquip) {

			if (character == null)
				throw new IllegalArgumentException("Character cannot be null.");
			if (equipment == null)
				throw new IllegalArgumentException("Equipment cannot be null.");

			/*
			 * 
			 * if (character.getKey()
			 * .equals(equipment.getProperty("containerKey")) == false) throw
			 * new IllegalArgumentException(
			 * "The piece of equipment is not in the character's posession.");
			 * 
			 * Of course this would throw Expression always -.- 
			 */

			String equipmentSlot = (String) equipment.getProperty("equipSlot");
			if (equipmentSlot == null)
				throw new UserErrorMessage("You cannot equip this item.");

			// NOT SURE if the above checks are all needed in this case

			if (equipment.getProperty("strengthRequirement") instanceof String)
				equipment.setProperty("strengthRequirement", null);
			Double strengthRequirement = (Double) equipment
					.getProperty("strengthRequirement");
			if (strengthRequirement != null && characterStrength != null
					&& strengthRequirement > characterStrength
					&& "NPC".equals(character.getProperty("type")) == false)
				throw new UserErrorMessage(
						"You cannot equip an item from the given container, you do not have the strength to use it.");
		}

		// Unequip all equipment we already have equipped and put them in the
		// container.
		List<CachedEntity> currentEquipment = new ArrayList<CachedEntity>();
		for (String slot : ODPDBAccess.EQUIPMENT_SLOTS) {
			if (character.getProperty("equipment" + slot) != null) {
				currentEquipment.add(db.getEntity((Key)character.getProperty("equipment" + slot)));
			}
		}
		
		ds.beginBulkWriteMode();
		
		for (CachedEntity equipment : currentEquipment) {
			db.doCharacterUnequipEntity(ds, character, equipment);
			equipment.setProperty("containerKey", containerKey);
			equipment.setProperty("movedTimestamp", new Date());

			ds.put(equipment); // NOT SURE if needed
		}

		// Equip the set from the container
		for (CachedEntity equipment : setEquip) {
			db.doCharacterEquipEntity(ds, character, equipment);
		}

		ds.put(character);
		ds.commitBulkWrite();
	}
}
