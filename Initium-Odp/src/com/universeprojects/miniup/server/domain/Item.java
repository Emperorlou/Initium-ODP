package com.universeprojects.miniup.server.domain;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * This is a live instance of an item in the game world.
 * 
 * @author kyle-miller
 *
 */
public class Item extends OdpDomain {
	public static final String KIND = "Item";

	public Item() {
		super(new CachedEntity(KIND));
	}

	public Item(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	public void setBlockChance(Long blockChance) {
		getCachedEntity().setProperty("blockChance", blockChance);
	}

	public Long getBlockChance() {
		return (Long) getCachedEntity().getProperty("blockChance");
	}

	/**
	 *  This number represents the number of "activations" the item will have. For example, if a script is tied to the item, this would be the number of times that script can be triggered.
	 *  
	 * @param charges
	 */
	public void setCharges(Long charges) {
		getCachedEntity().setProperty("charges", charges);
	}

	public Long getCharges() {
		return (Long) getCachedEntity().getProperty("charges");
	}

	/**
	 * 
	 * @param containerKey
	 */
	public void setContainerKey(Key containerKey) {
		getCachedEntity().setProperty("containerKey", containerKey);
	}

	public Key getContainerKey() {
		return (Key) getCachedEntity().getProperty("containerKey");
	}

	/**
	 * 
	 * @param damageReduction
	 */
	public void setDamageReduction(Long damageReduction) {
		getCachedEntity().setProperty("damageReduction", damageReduction);
	}

	public Long getDamageReduction() {
		return (Long) getCachedEntity().getProperty("damageReduction");
	}

	/**
	 *  A short flavor description that is added to the item when it is viewed by a player.
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
	 * 
	 * @param dexterityPenalty
	 */
	public void setDexterityPenalty(Long dexterityPenalty) {
		getCachedEntity().setProperty("dexterityPenalty", dexterityPenalty);
	}

	public Long getDexterityPenalty() {
		return (Long) getCachedEntity().getProperty("dexterityPenalty");
	}

	/**
	 *  If blank, no dogecoins can be added to this item. Otherwise, dogecoins can be taken and added to it.
	 *  
	 * @param dogecoins
	 */
	public void setDogecoins(Long dogecoins) {
		getCachedEntity().setProperty("dogecoins", dogecoins);
	}

	public Long getDogecoins() {
		return (Long) getCachedEntity().getProperty("dogecoins");
	}

	/**
	 * 
	 * @param durability
	 */
	public void setDurability(Long durability) {
		getCachedEntity().setProperty("durability", durability);
	}

	public Long getDurability() {
		return (Long) getCachedEntity().getProperty("durability");
	}

	/**
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
	 *  This is an optional name to give the item for developer use only (will not be shown to the players).
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
	 *  (Item|containerKey)
	 *  
	 * @param inventoryKeys
	 */
	public void setInventoryKeys(List<Key> inventoryKeys) {
		getCachedEntity().setProperty("inventory", inventoryKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getInventoryKeys() {
		return (List<Key>) getCachedEntity().getProperty("inventory");
	}

	/**
	 * 
	 * @param item2
	 */
	public void setItem2(String item2) {
		getCachedEntity().setProperty("item2", item2);
	}

	public String getItem2() {
		return (String) getCachedEntity().getProperty("item2");
	}

	/**
	 *  Consider this to be the category or classification that this item falls under that happens to be as specific as possible. For example, an item's type may be a Weapon, the item's itemClass may be a Longsword, and finally the item's name may be Black Knight Longsword.
	 *  
	 * @param itemClass
	 */
	public void setItemClass(String itemClass) {
		getCachedEntity().setProperty("itemClass", itemClass);
	}

	public String getItemClass() {
		return (String) getCachedEntity().getProperty("itemClass");
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
	 *  For certain items that are used as materials, this generalizes the overall quality of the material.
	 *  
	 * @param materialQuality
	 */
	public void setMaterialQuality(Double materialQuality) {
		getCachedEntity().setProperty("materialQuality", materialQuality);
	}

	public Double getMaterialQuality() {
		return (Double) getCachedEntity().getProperty("materialQuality");
	}

	/**
	 *  The maximum durability this item can have. When an item is brand new, its durability will equal this field`s value.
	 *  
	 * @param maxDurability
	 */
	public void setMaxDurability(Long maxDurability) {
		getCachedEntity().setProperty("maxDurability", maxDurability);
	}

	public Long getMaxDurability() {
		return (Long) getCachedEntity().getProperty("maxDurability");
	}

	/**
	 * 
	 * @param maxSpace
	 */
	public void setMaxSpace(Long maxSpace) {
		getCachedEntity().setProperty("maxSpace", maxSpace);
	}

	public Long getMaxSpace() {
		return (Long) getCachedEntity().getProperty("maxSpace");
	}

	/**
	 * 
	 * @param maxWeight
	 */
	public void setMaxWeight(Long maxWeight) {
		getCachedEntity().setProperty("maxWeight", maxWeight);
	}

	public Long getMaxWeight() {
		return (Long) getCachedEntity().getProperty("maxWeight");
	}

	/**
	 *  Whenever the item is moved, this is updated. It allows sorting of items on the ground.
	 *  
	 * @param movedTimestamp
	 */
	public void setMovedTimestamp(Date movedTimestamp) {
		getCachedEntity().setProperty("movedTimestamp", movedTimestamp);
	}

	public Date getMovedTimestamp() {
		return (Date) getCachedEntity().getProperty("movedTimestamp");
	}

	/**
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
	 * 
	 * @param purity
	 */
	public void setPurity(Double purity) {
		getCachedEntity().setProperty("purity", purity);
	}

	public Double getPurity() {
		return (Double) getCachedEntity().getProperty("purity");
	}

	/**
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
	public void setQuantity(Long quantity) {
		getCachedEntity().setProperty("quantity", quantity);
	}

	public Long getQuantity() {
		return (Long) getCachedEntity().getProperty("quantity");
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
	 * 
	 * @param space
	 */
	public void setSpace(Long space) {
		getCachedEntity().setProperty("space", space);
	}

	public Long getSpace() {
		return (Long) getCachedEntity().getProperty("space");
	}

	/**
	 *  The minimum strength required to use this piece of equipment.
	 *  
	 * @param strengthRequirement
	 */
	public void setStrengthRequirement(Double strengthRequirement) {
		getCachedEntity().setProperty("strengthRequirement", strengthRequirement);
	}

	public Double getStrengthRequirement() {
		return (Double) getCachedEntity().getProperty("strengthRequirement");
	}

	/**
	 * How strong this material is.
	 * 
	 * @param toughness
	 */
	public void setToughness(Long toughness) {
		getCachedEntity().setProperty("toughness", toughness);
	}

	public Long getToughness() {
		return (Long) getCachedEntity().getProperty("toughness");
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
	 *  True/False if this item can be equipped as a transport object.
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
	 *  A percentage. The base movement speed of a character using this transport.
	 *  
	 * @param transportMovementSpeed
	 */
	public void setTransportMovementSpeed(Double transportMovementSpeed) {
		getCachedEntity().setProperty("transportMovementSpeed", transportMovementSpeed);
	}

	public Double getTransportMovementSpeed() {
		return (Double) getCachedEntity().getProperty("transportMovementSpeed");
	}

	/**
	 * 
	 * @param warmth
	 */
	public void setWarmth(Long warmth) {
		getCachedEntity().setProperty("warmth", warmth);
	}

	public Long getWarmth() {
		return (Long) getCachedEntity().getProperty("warmth");
	}

	/**
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
	 * 
	 * @param weaponDamageCriticalChance
	 */
	public void setWeaponDamageCriticalChance(Long weaponDamageCriticalChance) {
		getCachedEntity().setProperty("weaponDamageCriticalChance", weaponDamageCriticalChance);
	}

	public Long getWeaponDamageCriticalChance() {
		return (Long) getCachedEntity().getProperty("weaponDamageCriticalChance");
	}

	public void setWeaponDamageCriticalMultiplier(Double weaponDamageCriticalMultiplier) {
		getCachedEntity().setProperty("weaponDamageCriticalMultiplier", weaponDamageCriticalMultiplier);
	}

	public Double getWeaponDamageCriticalMultiplier() {
		return (Double) getCachedEntity().getProperty("weaponDamageCriticalMultiplier");
	}

	public void setWeaponDamageType(String weaponDamageType) {
		getCachedEntity().setProperty("weaponDamageType", weaponDamageType);
	}

	public String getWeaponDamageType() {
		return (String) getCachedEntity().getProperty("weaponDamageType");
	}

	public void setWeaponRange(Long weaponRange) {
		getCachedEntity().setProperty("weaponRange", weaponRange);
	}

	public Long getWeaponRange() {
		return (Long) getCachedEntity().getProperty("weaponRange");
	}

	public void setWeaponRangeIncrement(Long weaponRangeIncrement) {
		getCachedEntity().setProperty("weaponRangeIncrement", weaponRangeIncrement);
	}

	public Long getWeaponRangeIncrement() {
		return (Long) getCachedEntity().getProperty("weaponRangeIncrement");
	}

	public void setWeatherDamage(Long weatherDamage) {
		getCachedEntity().setProperty("weatherDamage", weatherDamage);
	}

	public Long getWeatherDamage() {
		return (Long) getCachedEntity().getProperty("weatherDamage");
	}

	// 1=gram, 1000 weight=1 kilogram=2.2 lbs. Note that with stackable items, this is the weight per unit, not the weight of the entire stack.
	public void setWeight(Long weight) {
		getCachedEntity().setProperty("weight", weight);
	}

	public Long getWeight() {
		return (Long) getCachedEntity().getProperty("weight");
	}

	public enum BlockBludgeoningCapability {
		Excellent, Good, Average, Poor, Minimal, None,
	}

	public void setBlockBludgeoningCapability(BlockBludgeoningCapability blockBludgeoningCapability) {
		getCachedEntity().setProperty("blockBludgeoningCapability", blockBludgeoningCapability);
	}

	public BlockBludgeoningCapability getBlockBludgeoningCapability() {
		return (BlockBludgeoningCapability) getCachedEntity().getProperty("blockBludgeoningCapability");
	}

	public enum BlockPiercingCapability {
		Excellent, Good, Average, Poor, Minimal, None,
	}

	public void setBlockPiercingCapability(BlockPiercingCapability blockPiercingCapability) {
		getCachedEntity().setProperty("blockPiercingCapability", blockPiercingCapability);
	}

	public BlockPiercingCapability getBlockPiercingCapability() {
		return (BlockPiercingCapability) getCachedEntity().getProperty("blockPiercingCapability");
	}

	public enum BlockSlashingCapability {
		Excellent, Good, Average, Poor, Minimal, None,
	}

	public void setBlockSlashingCapability(BlockSlashingCapability blockSlashingCapability) {
		getCachedEntity().setProperty("blockSlashingCapability", blockSlashingCapability);
	}

	public BlockSlashingCapability getBlockSlashingCapability() {
		return (BlockSlashingCapability) getCachedEntity().getProperty("blockSlashingCapability");
	}

	public enum EquipSlot {
		Gloves("Gloves"),
		Helmet("Helmet"),
		Chest("Chest"),
		Shirt("Shirt"),
		Legs("Legs"),
		LeftHand("LeftHand"),
		RightHand("RightHand"),
		TwoHands("2Hands"),
		Boots("Boots"),
		ChestAndLegs("Chest And Legs"),
		ChestAndLegsAndBoots("Chest And Legs And Boots"),
		ChestAndLegsAndBootsAndGloves("Chest And Legs And Boots And Gloves"),
		HelmetAndChest("Helmet And Chest"),
		LeftHandAndRightHandAndGloves("LeftHand And RightHand And Gloves"),
		Transport("Transport"),
		Neck("Neck"),
		Ring("Ring");

		private String value;

		private EquipSlot(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public void setEquipSlot(EquipSlot equipSlot) {
		getCachedEntity().setProperty("equipSlot", equipSlot);
	}

	public EquipSlot getEquipSlot() {
		return (EquipSlot) getCachedEntity().getProperty("equipSlot");
	}

	public enum ForcedItemQuality {
		Junk, Average, Rare, Unique, Epic, Custom, Magic,
	}

	public void setForcedItemQuality(ForcedItemQuality forcedItemQuality) {
		getCachedEntity().setProperty("forcedItemQuality", forcedItemQuality);
	}

	public ForcedItemQuality getForcedItemQuality() {
		return (ForcedItemQuality) getCachedEntity().getProperty("forcedItemQuality");
	}

	public enum ItemType {
		Weapon, Armor, Shield, Ammo, Material, Food, Jewelry, Other,
	}

	public void setItemType(ItemType itemType) {
		getCachedEntity().setProperty("itemType", itemType);
	}

	public ItemType getItemType() {
		return (ItemType) getCachedEntity().getProperty("itemType");
	}

	public void setNaturalEquipment(Boolean naturalEquipment) {
		getCachedEntity().setProperty("naturalEquipment", naturalEquipment);
	}

	public Boolean getNaturalEquipment() {
		return (Boolean) getCachedEntity().getProperty("naturalEquipment");
	}

	public void setZombifying(Boolean zombifying) {
		getCachedEntity().setProperty("zombifying", zombifying);
	}

	public Boolean getZombifying() {
		return (Boolean) getCachedEntity().getProperty("zombifying");
	}

}
