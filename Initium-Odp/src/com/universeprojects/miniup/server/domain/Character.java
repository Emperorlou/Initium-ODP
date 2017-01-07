package com.universeprojects.miniup.server.domain;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * This is a player's character or an NPC.
 * 
 * @author kyle-miller
 *
 */
public class Character extends OdpDomain {
	public static final String KIND = "Character";

	public Character() {
		super(new CachedEntity(KIND));
	}

	public Character(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	public enum AutomaticWeaponChoiceMethod {
		HighestDamage("Highest Damage"), Random("Random"), Rarest("Rarest");

		private String value;

		private AutomaticWeaponChoiceMethod(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public enum CombatType {
		DefenceStructureAttack("Defence Structure Attack"), Monster("Monster");

		private String value;

		private CombatType(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public enum GroupStatus {
		Admin, Applied, Kicked, Member,
	}

	public enum LootPreference {
		CollectBetter("Collect Better"), CollectEverything("Collect Everything"), CollectNone("Collect None");

		private String value;

		private LootPreference(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	public enum Mode {
		COMBAT, DEAD, MERCHANT, NORMAL, TRADING, UNCONSCIOUS,
	}

	public enum Status {
		Defending1, Defending2, Defending3, Normal, Zombie,
	}

	public enum Type {
		NPC, PC,
	}

	/**
	 * 
	 * @param automaticWeaponChoiceMethod
	 */
	public void setAutomaticWeaponChoiceMethod(AutomaticWeaponChoiceMethod automaticWeaponChoiceMethod) {
		getCachedEntity().setProperty("automaticWeaponChoiceMethod", automaticWeaponChoiceMethod);
	}

	public AutomaticWeaponChoiceMethod getAutomaticWeaponChoiceMethod() {
		return (AutomaticWeaponChoiceMethod) getCachedEntity().getProperty("automaticWeaponChoiceMethod");
	}

	/**
	 * For NPCs, this is used to show the monster while in combat with them.
	 * 
	 * @param bannerUrl
	 */
	public void setBannerUrl(String bannerUrl) {
		getCachedEntity().setProperty("bannerUrl", bannerUrl);
	}

	public String getBannerUrl() {
		return (String) getCachedEntity().getProperty("bannerUrl");
	}

	/**
	 * We may periodically require a player to answer a question as a break from regular gameplay. This is done to try to make it harder to bot in the game.
	 * 
	 * @param botCheck
	 */
	public void setBotCheck(Boolean botCheck) {
		getCachedEntity().setProperty("botCheck", botCheck);
	}

	public Boolean isBotCheck() {
		return (Boolean) getCachedEntity().getProperty("botCheck");
	}

	/**
	 * All buffs currently on this character.
	 * 
	 * @param buffKeys
	 */
	public void setBuffKeys(List<Key> buffKeys) {
		getCachedEntity().setProperty("buffs", buffKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getBuffKeys() {
		return (List<Key>) getCachedEntity().getProperty("buffs");
	}

	/**
	 * If this is set to true, the character's equipment is not visible when inspected (unless the character is dead).
	 * 
	 * @param cloaked
	 */
	public void setCloaked(Boolean cloaked) {
		getCachedEntity().setProperty("cloaked", cloaked);
	}

	public Boolean isCloaked() {
		return (Boolean) getCachedEntity().getProperty("cloaked");
	}

	/**
	 * If the character is currently in the combat state, this field will hold the character this character is currently in combat with.
	 * 
	 * @param combatant
	 */
	public void setCombatant(Character combatant) {
		getCachedEntity().setProperty("combatant", combatant);
	}

	public Character getCombatant() {
		return (Character) getCachedEntity().getProperty("combatant");
	}

	/**
	 * 
	 * @param combatType
	 */
	public void setCombatType(CombatType combatType) {
		getCachedEntity().setProperty("combatType", combatType);
	}

	public CombatType getCombatType() {
		return (CombatType) getCachedEntity().getProperty("combatType");
	}

	/**
	 * Used internally. Do not change.
	 * 
	 * @param createdDate
	 */
	public void setCreatedDate(Date createdDate) {
		getCachedEntity().setProperty("createdDate", createdDate);
	}

	public Date getCreatedDate() {
		return (Date) getCachedEntity().getProperty("createdDate");
	}

	/**
	 * 
	 * @param dexterity
	 */
	public void setDexterity(Double dexterity) {
		getCachedEntity().setProperty("dexterity", dexterity);
	}

	public Double getDexterity() {
		return (Double) getCachedEntity().getProperty("dexterity");
	}

	/**
	 * The number of Dogecoins that this character has on their person.
	 * 
	 * @param dogeCoins
	 */
	public void setDogeCoins(Long dogeCoins) {
		getCachedEntity().setProperty("dogecoins", dogeCoins);
	}

	public Long getDogeCoins() {
		return (Long) getCachedEntity().getProperty("dogecoins");
	}

	/**
	 * 
	 * @param duelRequestsAllowed
	 */
	public void setDuelRequestsAllowed(Boolean duelRequestsAllowed) {
		getCachedEntity().setProperty("duelRequestsAllowed", duelRequestsAllowed);
	}

	public Boolean isDuelRequestsAllowed() {
		return (Boolean) getCachedEntity().getProperty("duelRequestsAllowed");
	}

	/**
	 * 
	 * @param opponentKey
	 */
	public void setDuelRequestOpponentKey(Key opponentKey) {
		getCachedEntity().setProperty("duelRequestOpponentKey", opponentKey);
	}

	public Key getDuelRequestOpponentKey() {
		return (Key) getCachedEntity().getProperty("duelRequestOpponentKey");
	}

	/**
	 * 
	 * @param equipmentBoots
	 */
	public void setEquipmentBootsKey(Key equipmentBoots) {
		getCachedEntity().setProperty("equipmentBoots", equipmentBoots);
	}

	public Key getEquipmentBootsKey() {
		return (Key) getCachedEntity().getProperty("equipmentBoots");
	}

	/**
	 * 
	 * @param equipmentChest
	 */
	public void setEquipmentChestKey(Key equipmentChest) {
		getCachedEntity().setProperty("equipmentChest", equipmentChest);
	}

	public Key getEquipmentChestKey() {
		return (Key) getCachedEntity().getProperty("equipmentChest");
	}

	/**
	 * 
	 * @param equipmentGloves
	 */
	public void setEquipmentGlovesKey(Key equipmentGloves) {
		getCachedEntity().setProperty("equipmentGloves", equipmentGloves);
	}

	public Key getEquipmentGlovesKey() {
		return (Key) getCachedEntity().getProperty("equipmentGloves");
	}

	/**
	 * 
	 * @param equipmentHelmet
	 */
	public void setEquipmentHelmetKey(Key equipmentHelmet) {
		getCachedEntity().setProperty("equipmentHelmet", equipmentHelmet);
	}

	public Key getEquipmentHelmetKey() {
		return (Key) getCachedEntity().getProperty("equipmentHelmet");
	}

	/**
	 * 
	 * @param equipmentLeftHand
	 */
	public void setEquipmentLeftHandKey(Key equipmentLeftHand) {
		getCachedEntity().setProperty("equipmentLeftHand", equipmentLeftHand);
	}

	public Key getEquipmentLeftHandKey() {
		return (Key) getCachedEntity().getProperty("equipmentLeftHand");
	}

	/**
	 * 
	 * @param equipmentLeftRing
	 */
	public void setEquipmentLeftRingKey(Key equipmentLeftRing) {
		getCachedEntity().setProperty("equipmentLeftRing", equipmentLeftRing);
	}

	public Key getEquipmentLeftRingKey() {
		return (Key) getCachedEntity().getProperty("equipmentLeftRing");
	}

	/**
	 * 
	 * @param equipmentLegs
	 */
	public void setEquipmentLegsKey(Key equipmentLegs) {
		getCachedEntity().setProperty("equipmentLegs", equipmentLegs);
	}

	public Key getEquipmentLegsKey() {
		return (Key) getCachedEntity().getProperty("equipmentLegs");
	}

	/**
	 * 
	 * @param equipmentNeck
	 */
	public void setEquipmentNeckKey(Key equipmentNeck) {
		getCachedEntity().setProperty("equipmentNeck", equipmentNeck);
	}

	public Key getEquipmentNeckKey() {
		return (Key) getCachedEntity().getProperty("equipmentNeck");
	}

	/**
	 * 
	 * @param equipmentRightHand
	 */
	public void setEquipmentRightHandKey(Key equipmentRightHand) {
		getCachedEntity().setProperty("equipmentRightHand", equipmentRightHand);
	}

	public Key getEquipmentRightHandKey() {
		return (Key) getCachedEntity().getProperty("equipmentRightHand");
	}

	/**
	 * 
	 * @param equipmentRightRing
	 */
	public void setEquipmentRightRingKey(Key equipmentRightRing) {
		getCachedEntity().setProperty("equipmentRightRing", equipmentRightRing);
	}

	public Key getEquipmentRightRingKey() {
		return (Key) getCachedEntity().getProperty("equipmentRightRing");
	}

	/**
	 * 
	 * @param equipmentShirt
	 */
	public void setEquipmentShirtKey(Key equipmentShirt) {
		getCachedEntity().setProperty("equipmentShirt", equipmentShirt);
	}

	public Key getEquipmentShirtKey() {
		return (Key) getCachedEntity().getProperty("equipmentShirt");
	}

	/**
	 * The transport item that this character is currently using.
	 * 
	 * @param equipmentTransport
	 */
	public void setEquipmentTransportKey(Key equipmentTransport) {
		getCachedEntity().setProperty("equipmentTransport", equipmentTransport);
	}

	public Key getEquipmentTransportKey() {
		return (Key) getCachedEntity().getProperty("equipmentTransport");
	}

	/**
	 * 
	 * @param experienceMultiplier
	 */
	public void setExperienceMultiplier(Double experienceMultiplier) {
		getCachedEntity().setProperty("experienceMultiplier ", experienceMultiplier);
	}

	public Double getExperienceMultiplier() {
		return (Double) getCachedEntity().getProperty("experienceMultiplier");
	}

	/**
	 * The group that this character belongs to.
	 * 
	 * @param groupKey
	 */
	public void setGroupKey(Key groupKey) {
		getCachedEntity().setProperty("groupKey", groupKey);
	}

	public Key getGroupKey() {
		return (Key) getCachedEntity().getProperty("groupKey");
	}

	/**
	 * The rank this character is in his current group.
	 * 
	 * @param groupRank
	 */
	public void setGroupRank(String groupRank) {
		getCachedEntity().setProperty("groupRank", groupRank);
	}

	public String getGroupRank() {
		return (String) getCachedEntity().getProperty("groupRank");
	}

	/**
	 * The current status of this character's membership with his group.
	 * 
	 * @param groupStatus
	 */
	public void setGroupStatus(GroupStatus groupStatus) {
		getCachedEntity().setProperty("groupStatus", groupStatus);
	}

	public GroupStatus getGroupStatus() {
		return (GroupStatus) getCachedEntity().getProperty("groupStatus");
	}

	/**
	 * The character`s current life points.
	 * 
	 * @param hitpoints
	 */
	public void setHitPoints(Double hitpoints) {
		getCachedEntity().setProperty("hitpoints", hitpoints);
	}

	public Double getHitPoints() {
		return (Double) getCachedEntity().getProperty("hitpoints");
	}

	/**
	 * This is the last town that the character visited. When a character dies, he respawns in the last town he visited.
	 * 
	 * @param homeTownKey
	 */
	public void setHomeTownKey(Key homeTownKey) {
		getCachedEntity().setProperty("homeTownKey", homeTownKey);
	}

	public Key getHomeTownKey() {
		return (Key) getCachedEntity().getProperty("homeTownKey");
	}

	/**
	 * 
	 * @param intelligence
	 */
	public void setIntelligence(Double intelligence) {
		getCachedEntity().setProperty("intelligence", intelligence);
	}

	public Double getIntelligence() {
		return (Double) getCachedEntity().getProperty("intelligence");
	}

	/**
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
	 * The items that this character currently has in his possession.
	 * 
	 * @param inventory
	 */
	public void setInventoryKeys(List<Key> inventory) {
		getCachedEntity().setProperty("inventory", inventory);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getInventoryKeys() {
		return (List<Key>) getCachedEntity().getProperty("inventory");
	}

	/**
	 * The last time this character moved.
	 * 
	 * @param locationEntryDatetime
	 */
	public void setLocationEntryDatetime(Date locationEntryDatetime) {
		getCachedEntity().setProperty("locationEntryDatetime", locationEntryDatetime);
	}

	public Date getLocationEntryDatetime() {
		return (Date) getCachedEntity().getProperty("locationEntryDatetime");
	}

	/**
	 * 
	 * @param locationKey
	 */
	public void setLocationKey(Key locationKey) {
		getCachedEntity().setProperty("locationKey", locationKey);
	}

	public Key getLocationKey() {
		return (Key) getCachedEntity().getProperty("locationKey");
	}

	/**
	 * This field determines what this monster will loot from a player that it kills.
	 * 
	 * @param lootPreference
	 */
	public void setLootPreference(LootPreference lootPreference) {
		getCachedEntity().setProperty("lootPreference", lootPreference);
	}

	public LootPreference getLootPreference() {
		return (LootPreference) getCachedEntity().getProperty("lootPreference");
	}

	/**
	 * The character's maximum life points.
	 * 
	 * @param maxHitpoints
	 */
	public void setMaxHitpoints(Double maxHitpoints) {
		getCachedEntity().setProperty("maxHitpoints", maxHitpoints);
	}

	public Double getMaxHitpoints() {
		return (Double) getCachedEntity().getProperty("maxHitpoints");
	}

	/**
	 * This field indicates the game state as relating to the character. For example, if the character is in combat, this field would say "combat".
	 * 
	 * @param mode
	 */
	public void setMode(Mode mode) {
		getCachedEntity().setProperty("mode", mode);
	}

	public Mode getMode() {
		return (Mode) getCachedEntity().getProperty("mode");
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
	 * This css class will be added to the character name.
	 * 
	 * @param nameClass
	 */
	public void setNameClass(String nameClass) {
		getCachedEntity().setProperty("nameClass", nameClass);
	}

	public String getNameClass() {
		return (String) getCachedEntity().getProperty("nameClass");
	}

	/**
	 * The unique code that identifies the party you belong to.
	 * 
	 * @param partyCode
	 */
	public void setPartyCode(String partyCode) {
		getCachedEntity().setProperty("partyCode", partyCode);
	}

	public String getPartyCode() {
		return (String) getCachedEntity().getProperty("partyCode");
	}

	/**
	 * If true, anyone can join your party.
	 * 
	 * @param partyJoinsAllowed
	 */
	public void setPartyJoinsAllowed(Boolean partyJoinsAllowed) {
		getCachedEntity().setProperty("partyJoinsAllowed", partyJoinsAllowed);
	}

	public Boolean isPartyJoinsAllowed() {
		return (Boolean) getCachedEntity().getProperty("partyJoinsAllowed");
	}

	/**
	 * If true, this character is the leader of his party.
	 * 
	 * @param isPartyLeader
	 */
	public void setPartyLeader(Boolean isPartyLeader) {
		getCachedEntity().setProperty("partyLeader", isPartyLeader);
	}

	public Boolean isPartyLeader() {
		return (Boolean) getCachedEntity().getProperty("partyLeader");
	}

	/**
	 * This is generally used as an additional means of knowing the state of the character for internal processing purposes.
	 * 
	 * @param status
	 */
	public void setStatus(Status status) {
		getCachedEntity().setProperty("status", status);
	}

	public Status getStatus() {
		return (Status) getCachedEntity().getProperty("status");
	}

	/**
	 * This is the name of the character`s store, if he chooses enable one.
	 * 
	 * @param storeName
	 */
	public void setStoreName(String storeName) {
		getCachedEntity().setProperty("storeName", storeName);
	}

	public String getStoreName() {
		return (String) getCachedEntity().getProperty("storeName");
	}

	/**
	 * 
	 * @param storeSale
	 */
	public void setStoreSale(Double storeSale) {
		getCachedEntity().setProperty("storeSale", storeSale);
	}

	public Double getStoreSale() {
		return (Double) getCachedEntity().getProperty("storeSale");
	}

	/**
	 * This is css that is dumped into the style tag for the store name.
	 * 
	 * @param storeStyleCustomization
	 */
	public void setStoreStyleCustomization(String storeStyleCustomization) {
		getCachedEntity().setProperty("storeStyleCustomization", storeStyleCustomization);
	}

	public String getStoreStyleCustomization() {
		return (String) getCachedEntity().getProperty("storeStyleCustomization");
	}

	/**
	 * 
	 * @param strength
	 */
	public void setStrength(Double strength) {
		getCachedEntity().setProperty("strength", strength);
	}

	public Double getStrength() {
		return (Double) getCachedEntity().getProperty("strength");
	}

	/**
	 * This field indicates whether this character is an computer character (npc) or a player character.
	 * 
	 * @param type
	 */
	public void setType(Type type) {
		getCachedEntity().setProperty("type", type);
	}

	public Type getType() {
		return (Type) getCachedEntity().getProperty("type");
	}

	/**
	 * The user that this character belongs to.
	 * 
	 * @param userKey
	 */
	public void setUserKey(Key userKey) {
		getCachedEntity().setProperty("userKey", userKey);
	}

	public Key getUserKey() {
		return (Key) getCachedEntity().getProperty("userKey");
	}
}
