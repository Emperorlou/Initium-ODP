package com.universeprojects.miniup.server.domain;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Item.EquipSlot;

/**
 * Item definitions. This entity describes how to create an instance of an item.
 * 
 * @author kyle-miller
 *
 */
public class ItemDef extends OdpDomain {
	public static final String KIND = "ItemDef";

	public ItemDef() {
		super(new CachedEntity(KIND));
	}

	public ItemDef(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 *  The percentage chance of a successfully blocked attack. Critical hits cannot be blocked by this block chance.
	 *  
	 * @param blockChance
	 */
	public void setBlockChance(String blockChance) {
		getCachedEntity().setProperty("blockChance", blockChance);
	}

	public String getBlockChance() {
		return (String) getCachedEntity().getProperty("blockChance");
	}

	/**
	 *  This number represents the number of "activations" the item will have. For example, if a script is tied to the item, this would be the number of times that script can be triggered.
	 *  
	 * @param charges
	 */
	public void setCharges(String charges) {
		getCachedEntity().setProperty("charges", charges);
	}

	public String getCharges() {
		return (String) getCachedEntity().getProperty("charges");
	}

	/**
	 *  Amount of damage, upon a successful block, that this item can absorb.
	 *  
	 * @param damageReduction
	 */
	public void setDamageReduction(String damageReduction) {
		getCachedEntity().setProperty("damageReduction", damageReduction);
	}

	public String getDamageReduction() {
		return (String) getCachedEntity().getProperty("damageReduction");
	}

	/**
	 *  A short flavor description that is added to the item when a player views it.
	 *  
	 * @param description
	 */
	public void setDescription(Text description) {
		getCachedEntity().setProperty("description", description);
	}

	public Text getDescription() {
		return (Text) getCachedEntity().getProperty("description");
	}

	/**
	 *  The amount of a penalty this item has on the wearer when they perform actions that have to do with dexterity. The wearer`s dexterity will be subtracted by the amount in this field.
	 *  
	 * @param dexterityPenalty
	 */
	public void setDexterityPenalty(String dexterityPenalty) {
		getCachedEntity().setProperty("dexterityPenalty", dexterityPenalty);
	}

	public String getDexterityPenalty() {
		return (String) getCachedEntity().getProperty("dexterityPenalty");
	}

	/**
	 *  If left blank, no dogecoins can be added to the resulting item. Otherwise, dogecoins can be added or removed.
	 *  
	 * @param dogecoins
	 */
	public void setDogecoins(String dogecoins) {
		getCachedEntity().setProperty("dogecoins", dogecoins);
	}

	public String getDogecoins() {
		return (String) getCachedEntity().getProperty("dogecoins");
	}

	/**
	 *  Generally 1 durability lost per standard use
	 *  
	 * @param durability
	 */
	public void setDurability(String durability) {
		getCachedEntity().setProperty("durability", durability);
	}

	public String getDurability() {
		return (String) getCachedEntity().getProperty("durability");
	}

	/**
	 *  A 16x16 image icon that represents this item in the inventory.
	 *  
	 * @param icon
	 */
	public void setIcon(String icon) {
		getCachedEntity().setProperty("icon", icon);
	}

	public String getIcon() {
		return (String) getCachedEntity().getProperty("icon");
	}

	/**
	 *  For equipment that go in multiple slots, this icon will be used for the second slot that is listed.
	 *  
	 * @param icon2
	 */
	public void setIcon2(String icon2) {
		getCachedEntity().setProperty("icon2", icon2);
	}

	public String getIcon2() {
		return (String) getCachedEntity().getProperty("icon2");
	}

	/**
	 *  This is an optional name you can give the item that is for developer use only (it will not be shown to the players).
	 *  
	 * @param internalName
	 */
	public void setInternalName(String internalName) {
		getCachedEntity().setProperty("internalName", internalName);
	}

	public String getInternalName() {
		return (String) getCachedEntity().getProperty("internalName");
	}

	/**
	 *  (ItemSpawner|itemDefKey)
	 *  
	 * @param itemSpawnerList
	 */
	public void setItemSpawnerList(List<ItemSpawner> itemSpawnerList) {
		getCachedEntity().setProperty("itemSpawnerList", itemSpawnerList);
	}

	@SuppressWarnings("unchecked")
	public List<ItemSpawner> getItemSpawnerList() {
		return (List<ItemSpawner>) getCachedEntity().getProperty("itemSpawnerList");
	}

	/**
	 *  This code is used to allow players to pass through paths that have a corresponding lockCode if the item is in the player's inventory.
	 *  
	 * @param keyCode
	 */
	public void setKeyCode(Long keyCode) {
		getCachedEntity().setProperty("keyCode", keyCode);
	}

	public Long getKeyCode() {
		return (Long) getCachedEntity().getProperty("keyCode");
	}

	/**
	 *  Custom label for items. Changeable by players for storage items.
	 *  
	 * @param label
	 */
	public void setLabel(String label) {
		getCachedEntity().setProperty("label", label);
	}

	public String getLabel() {
		return (String) getCachedEntity().getProperty("label");
	}

	/**
	 *  Maximum space (volume) this item can contain. 10 space = size of thumbnail, 500 space = size of fist, 2500 space = size of forearm, 6000 space = size of entire arm, 120000 space = size of large grown man.
	 *  
	 * @param maxSpace
	 */
	public void setMaxSpace(String maxSpace) {
		getCachedEntity().setProperty("maxSpace", maxSpace);
	}

	public String getMaxSpace() {
		return (String) getCachedEntity().getProperty("maxSpace");
	}

	/**
	 *  Maximum weight this item can carry. 1=gram, 1000 weight=1 kilogram
	 *  
	 * @param maxWeight
	 */
	public void setMaxWeight(String maxWeight) {
		getCachedEntity().setProperty("maxWeight", maxWeight);
	}

	public String getMaxWeight() {
		return (String) getCachedEntity().getProperty("maxWeight");
	}

	/**
	 *  This name will function as the `item class` for ingame items.
	 *  
	 * @param name
	 */
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	/**
	 *  This field will be output directly into the popup when an item is clicked but ONLY if the owner of this item (it has to be in their inventory) clicked on it.
	 *  
	 * @param ownerOnlyHtml
	 */
	public void setOwnerOnlyHtml(Text ownerOnlyHtml) {
		getCachedEntity().setProperty("ownerOnlyHtml", ownerOnlyHtml);
	}

	public Text getOwnerOnlyHtml() {
		return (Text) getCachedEntity().getProperty("ownerOnlyHtml");
	}

	/**
	 *  This is a list of item fields and a + or - indicating if more is better or less is better. The overall quality of an item is then calculated based on this field list and based on the String min/max of the ItemDef used to create this item.
	 *  
	 * @param qualityUnit
	 */
	public void setQualityUnit(String qualityUnit) {
		getCachedEntity().setProperty("qualityUnit", qualityUnit);
	}

	public String getQualityUnit() {
		return (String) getCachedEntity().getProperty("qualityUnit");
	}

	/**
	 *  This field indicates how many identical items are in this 'stack'.
	 *  
	 * @param quantity
	 */
	public void setQuantity(String quantity) {
		getCachedEntity().setProperty("quantity", quantity);
	}

	public String getQuantity() {
		return (String) getCachedEntity().getProperty("quantity");
	}

	/**
	 *  (Script)
	 *  
	 * @param scriptKeys
	 */
	public void setScriptKeys(List<Key> scriptKeys) {
		getCachedEntity().setProperty("scripts", scriptKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getScriptKeys() {
		return (List<Key>) getCachedEntity().getProperty("scripts");
	}

	/**
	 *  10 space = size of thumbnail, 500 space = size of fist, 2500 space = size of forearm, 6000 space = size of entire arm, 120000 space = size of large grown man
	 *  
	 * @param space
	 */
	public void setSpace(String space) {
		getCachedEntity().setProperty("space", space);
	}

	public String getSpace() {
		return (String) getCachedEntity().getProperty("space");
	}

	/**
	 * 
	 * @param specialAbilities
	 */
	public void setSpecialAbilities(String specialAbilities) {
		getCachedEntity().setProperty("specialAbilities", specialAbilities);
	}

	public String getSpecialAbilities() {
		return (String) getCachedEntity().getProperty("specialAbilities");
	}

	/**
	 *  The minimum strength required to use this piece of equipment.
	 *  
	 * @param strengthRequirement
	 */
	public void setStrengthRequirement(String strengthRequirement) {
		getCachedEntity().setProperty("strengthRequirement", strengthRequirement);
	}

	public String getStrengthRequirement() {
		return (String) getCachedEntity().getProperty("strengthRequirement");
	}

	/**
	 *  Setting this to true signals that this item is capable of transmuting it's contents using the transmute system. This item needs to be setup to be a container for this to work.
	 *  
	 * @param transmuteEnabled
	 */
	public void setTransmuteEnabled(Boolean transmuteEnabled) {
		getCachedEntity().setProperty("transmuteEnabled", transmuteEnabled);
	}

	public Boolean getTransmuteEnabled() {
		return (Boolean) getCachedEntity().getProperty("transmuteEnabled");
	}

	/**
	 *  True/False if this item is equippable as a transport.
	 *  
	 * @param transportEnabled
	 */
	public void setTransportEnabled(Boolean transportEnabled) {
		getCachedEntity().setProperty("transportEnabled", transportEnabled);
	}

	public Boolean getTransportEnabled() {
		return (Boolean) getCachedEntity().getProperty("transportEnabled");
	}

	/**
	 *  A percentage. The base movement speed of a character equipped with this transport.
	 *  
	 * @param transportMovementSpeed
	 */
	public void setTransportMovementSpeed(String transportMovementSpeed) {
		getCachedEntity().setProperty("transportMovementSpeed", transportMovementSpeed);
	}

	public String getTransportMovementSpeed() {
		return (String) getCachedEntity().getProperty("transportMovementSpeed");
	}

	/**
	 *  3 warmth = gloves, 10 warmth = sweater, 30 warmth = heavy jacket, 100 warmth = snowsuit
	 *  
	 * @param warmth
	 */
	public void setWarmth(String warmth) {
		getCachedEntity().setProperty("warmth", warmth);
	}

	public String getWarmth() {
		return (String) getCachedEntity().getProperty("warmth");
	}

	/**
	 *  Damage should be a String Generator type that resolves to a Dice Expression.
	 *  
	 * @param weaponDamage
	 */
	public void setWeaponDamage(String weaponDamage) {
		getCachedEntity().setProperty("weaponDamage", weaponDamage);
	}

	public String getWeaponDamage() {
		return (String) getCachedEntity().getProperty("weaponDamage");
	}

	/**
	 *  The odds that a hit will result in critical damage. 5 = 20% chance, 10 = 10% chance, 20 = 5% chance, 100 = 1% chance.
	 *  
	 * @param weaponDamageCriticalChance
	 */
	public void setWeaponDamageCriticalChance(String weaponDamageCriticalChance) {
		getCachedEntity().setProperty("weaponDamageCriticalChance", weaponDamageCriticalChance);
	}

	public String getWeaponDamageCriticalChance() {
		return (String) getCachedEntity().getProperty("weaponDamageCriticalChance");
	}

	/**
	 *  The multiplier that is applied to the damage done when a critical hit occurs.
	 *  
	 * @param weaponDamageCriticalMultiplier
	 */
	public void setWeaponDamageCriticalMultiplier(String weaponDamageCriticalMultiplier) {
		getCachedEntity().setProperty("weaponDamageCriticalMultiplier", weaponDamageCriticalMultiplier);
	}

	public String getWeaponDamageCriticalMultiplier() {
		return (String) getCachedEntity().getProperty("weaponDamageCriticalMultiplier");
	}

	/**
	 *  The maximum distance that this weapon can possibly land a hit. 1=1 foot
	 *  
	 * @param weaponRangeIncrement
	 */
	public void setWeaponRangeIncrement(String weaponRangeIncrement) {
		getCachedEntity().setProperty("weaponRangeIncrement", weaponRangeIncrement);
	}

	public String getWeaponRangeIncrement() {
		return (String) getCachedEntity().getProperty("weaponRangeIncrement");
	}

	/**
	 *  0=no degrdation, 1=very very slow degredation, 20=standard degredation for items with wood in them, 50=degredation for all wood items, 100=degredation for cloth items, 1000=degredation for paper
	 *  
	 * @param weatherDamage
	 */
	public void setWeatherDamage(String weatherDamage) {
		getCachedEntity().setProperty("weatherDamage", weatherDamage);
	}

	public String getWeatherDamage() {
		return (String) getCachedEntity().getProperty("weatherDamage");
	}

	/**
	 *  1=gram, 1000 weight=1 kilogram=2.2 lbs. Note that with stackable items, this is the weight per unit, not the weight of the entire stack.
	 *  
	 * @param weight
	 */
	public void setWeight(String weight) {
		getCachedEntity().setProperty("weight", weight);
	}

	public String getWeight() {
		return (String) getCachedEntity().getProperty("weight");
	}

	public enum BlockBludgeoningCapability {
		Excellent, Good, Average, Poor, Minimal, None,
	}

	/**
	 * 
	 * @param blockBludgeoningCapability
	 */
	public void setBlockBludgeoningCapability(BlockBludgeoningCapability blockBludgeoningCapability) {
		getCachedEntity().setProperty("blockBludgeoningCapability", blockBludgeoningCapability);
	}

	public BlockBludgeoningCapability getBlockBludgeoningCapability() {
		return (BlockBludgeoningCapability) getCachedEntity().getProperty("blockBludgeoningCapability");
	}

	public enum BlockPiercingCapability {
		Excellent, Good, Average, Poor, Minimal, None,
	}

	/**
	 * 
	 * @param blockPiercingCapability
	 */
	public void setBlockPiercingCapability(BlockPiercingCapability blockPiercingCapability) {
		getCachedEntity().setProperty("blockPiercingCapability", blockPiercingCapability);
	}

	public BlockPiercingCapability getBlockPiercingCapability() {
		return (BlockPiercingCapability) getCachedEntity().getProperty("blockPiercingCapability");
	}

	public enum BlockSlashingCapability {
		Excellent, Good, Average, Poor, Minimal, None,
	}

	/**
	 * 
	 * @param blockSlashingCapability
	 */
	public void setBlockSlashingCapability(BlockSlashingCapability blockSlashingCapability) {
		getCachedEntity().setProperty("blockSlashingCapability", blockSlashingCapability);
	}

	public BlockSlashingCapability getBlockSlashingCapability() {
		return (BlockSlashingCapability) getCachedEntity().getProperty("blockSlashingCapability");
	}

	/**
	 * 
	 * @param equipSlot
	 */
	public void setEquipSlot(EquipSlot equipSlot) {
		getCachedEntity().setProperty("equipSlot", equipSlot);
	}

	public EquipSlot getEquipSlot() {
		return (EquipSlot) getCachedEntity().getProperty("equipSlot");
	}

	public enum ForcedItemQuality {
		Junk, Average, Rare, Unique, Epic, Magic,
	}

	/**
	 * 
	 * @param forcedItemQuality
	 */
	public void setForcedItemQuality(ForcedItemQuality forcedItemQuality) {
		getCachedEntity().setProperty("forcedItemQuality", forcedItemQuality);
	}

	public ForcedItemQuality getForcedItemQuality() {
		return (ForcedItemQuality) getCachedEntity().getProperty("forcedItemQuality");
	}

	public enum ItemClass {
		Barb("Barb"),
		Battleaxe("Battleaxe"),
		Broadsword("Broadsword"),
		Claw("Claw"),
		Club("Club"),
		Dagger("Dagger"),
		Dart("Dart"),
		Flamberge("Flamberge"),
		Greataxe("Greataxe"),
		Greatclub("Greatclub"),
		Greatsword("Greatsword"),
		Guisarme("Guisarme"),
		Halberd("Halberd"),
		HalfPike("Half-Pike"),
		Hammer("Hammer"),
		HandAxe("Hand Axe"),
		HeavyCrossbow("Heavy Crossbow"),
		Horn("Horn"),
		Katar("Katar"),
		Knife("Knife"),
		LightCrossbow("Light Crossbow"),
		Lochaber("Lochaber"),
		Longbow("Longbow"),
		Longspear("Longspear"),
		Longsword("Longsword"),
		Mace("Mace"),
		Macuahuitl("Macuahuitl"),
		ParryingDagger("Parrying Dagger"),
		Pickaxe("Pickaxe"),
		Pike("Pike"),
		Quarterstaff("Quarterstaff"),
		Rapier("Rapier"),
		Scimitar("Scimitar"),
		ShortStaff("Short Staff"),
		Shortbow("Shortbow"),
		Shortspear("Shortspear"),
		Shortsword("Shortsword"),
		Shovel("Shovel"),
		Shuriken("Shuriken"),
		SpikedClub("Spiked Club"),
		Talwar("Talwar"),
		ThrowingAxe("Throwing Axe"),
		Tooth("Tooth"),
		Trident("Trident"),
		TwoHandFalchion("Two-Hand Falchion"),
		Wand("Wand"),
		Warhammer("Warhammer"),
		Whip("Whip"),
		;

		private String value;

		private ItemClass(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	/**
	 * 
	 * @param itemClass
	 */
	public void setItemClass(ItemClass itemClass) {
		getCachedEntity().setProperty("itemClass", itemClass);
	}

	public ItemClass getItemClass() {
		return (ItemClass) getCachedEntity().getProperty("itemClass");
	}

	public enum ItemType {
		Weapon, Armor, Shield, Ammo, Material, Food, Other,
	}

	/**
	 * 
	 * @param itemType
	 */
	public void setItemType(ItemType itemType) {
		getCachedEntity().setProperty("itemType", itemType);
	}

	public ItemType getItemType() {
		return (ItemType) getCachedEntity().getProperty("itemType");
	}

	/**
	 * 
	 * @param naturalEquipment
	 */
	public void setNaturalEquipment(Boolean naturalEquipment) {
		getCachedEntity().setProperty("naturalEquipment", naturalEquipment);
	}

	public Boolean getNaturalEquipment() {
		return (Boolean) getCachedEntity().getProperty("naturalEquipment");
	}

	public enum WeaponDamageType {
		Bludgeoning("Bludgeoning"),
		Piercing("Piercing"),
		Slashing("Slashing"),
		BludgeoningAndPiercing("Bludgeoning and Piercing"),
		BludgeoningAndSlashing("Bludgeoning and Slashing"),
		PiercingAndSlashing("Piercing and Slashing"),
		BludgeoningAndPiercingAndSlashing("Bludgeoning and Piercing and Slashing"),
		;

		private String value;

		private WeaponDamageType(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	/**
	 * 
	 * @param weaponDamageType
	 */
	public void setWeaponDamageType(WeaponDamageType weaponDamageType) {
		getCachedEntity().setProperty("weaponDamageType", weaponDamageType);
	}

	public WeaponDamageType getWeaponDamageType() {
		return (WeaponDamageType) getCachedEntity().getProperty("weaponDamageType");
	}

	/**
	 * 
	 * @param zombifying
	 */
	public void setZombifying(Boolean zombifying) {
		getCachedEntity().setProperty("zombifying", zombifying);
	}

	public Boolean getZombifying() {
		return (Boolean) getCachedEntity().getProperty("zombifying");
	}

}
