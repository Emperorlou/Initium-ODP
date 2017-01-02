package com.universeprojects.miniup.server.domain;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

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

	public void setAutomaticWeaponChoiceMethod(AutomaticWeaponChoiceMethod automaticWeaponChoiceMethod) {
		getCachedEntity().setProperty("automaticWeaponChoiceMethod", automaticWeaponChoiceMethod);
	}

	public AutomaticWeaponChoiceMethod getAutomaticWeaponChoiceMethod() {
		return (AutomaticWeaponChoiceMethod) getCachedEntity().getProperty("automaticWeaponChoiceMethod");
	}

	public void setBannerUrl(String bannerUrl) {
		getCachedEntity().setProperty("bannerUrl", bannerUrl);
	}

	public String getBannerUrl() {
		return (String) getCachedEntity().getProperty("bannerUrl");
	}

	public void setBotCheck(Boolean botCheck) {
		getCachedEntity().setProperty("botCheck", botCheck);
	}

	public Boolean isBotCheck() {
		return (Boolean) getCachedEntity().getProperty("botCheck");
	}

	public void setBuffKeys(List<Key> buffKeys) {
		getCachedEntity().setProperty("buffs", buffKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getBuffKeys() {
		return (List<Key>) getCachedEntity().getProperty("buffs");
	}

	public void setCloaked(Boolean cloaked) {
		getCachedEntity().setProperty("cloaked", cloaked);
	}

	public Boolean isCloaked() {
		return (Boolean) getCachedEntity().getProperty("cloaked");
	}

	public void setCombatant(Character combatant) {
		getCachedEntity().setProperty("combatant", combatant);
	}

	public Character getCombatant() {
		return (Character) getCachedEntity().getProperty("combatant");
	}

	public void setCombatType(CombatType combatType) {
		getCachedEntity().setProperty("combatType", combatType);
	}

	public CombatType getCombatType() {
		return (CombatType) getCachedEntity().getProperty("combatType");
	}

	public void setCreatedDate(Date createdDate) {
		getCachedEntity().setProperty("createdDate", createdDate);
	}

	public Date getCreatedDate() {
		return (Date) getCachedEntity().getProperty("createdDate");
	}

	public void setDexterity(Double dexterity) {
		getCachedEntity().setProperty("dexterity", dexterity);
	}

	public Double getDexterity() {
		return (Double) getCachedEntity().getProperty("dexterity");
	}

	public void setDogeCoins(Long dogeCoins) {
		getCachedEntity().setProperty("dogecoins", dogeCoins);
	}

	public Long getDogeCoins() {
		return (Long) getCachedEntity().getProperty("dogecoins");
	}

	public void setDuelRequestsAllowed(Boolean duelRequestsAllowed) {
		getCachedEntity().setProperty("duelRequestsAllowed", duelRequestsAllowed);
	}

	public Boolean isDuelRequestsAllowed() {
		return (Boolean) getCachedEntity().getProperty("duelRequestsAllowed");
	}

	public void setDuelRequestOpponentKey(Key opponentKey) {
		getCachedEntity().setProperty("duelRequestOpponentKey", opponentKey);
	}

	public Key getDuelRequestOpponentKey() {
		return (Key) getCachedEntity().getProperty("duelRequestOpponentKey");
	}

	public void setEquipmentBootsKey(Key equipmentBoots) {
		getCachedEntity().setProperty("equipmentBoots", equipmentBoots);
	}

	public Key getEquipmentBootsKey() {
		return (Key) getCachedEntity().getProperty("equipmentBoots");
	}

	public void setEquipmentChestKey(Key equipmentChest) {
		getCachedEntity().setProperty("equipmentChest", equipmentChest);
	}

	public Key getEquipmentChestKey() {
		return (Key) getCachedEntity().getProperty("equipmentChest");
	}

	public void setEquipmentGlovesKey(Key equipmentGloves) {
		getCachedEntity().setProperty("equipmentGloves", equipmentGloves);
	}

	public Key getEquipmentGlovesKey() {
		return (Key) getCachedEntity().getProperty("equipmentGloves");
	}

	public void setEquipmentHelmetKey(Key equipmentHelmet) {
		getCachedEntity().setProperty("equipmentHelmet", equipmentHelmet);
	}

	public Key getEquipmentHelmetKey() {
		return (Key) getCachedEntity().getProperty("equipmentHelmet");
	}

	public void setEquipmentLeftHandKey(Key equipmentLeftHand) {
		getCachedEntity().setProperty("equipmentLeftHand", equipmentLeftHand);
	}

	public Key getEquipmentLeftHandKey() {
		return (Key) getCachedEntity().getProperty("equipmentLeftHand");
	}

	public void setEquipmentLeftRingKey(Key equipmentLeftRing) {
		getCachedEntity().setProperty("equipmentLeftRing", equipmentLeftRing);
	}

	public Key getEquipmentLeftRingKey() {
		return (Key) getCachedEntity().getProperty("equipmentLeftRing");
	}

	public void setEquipmentLegsKey(Key equipmentLegs) {
		getCachedEntity().setProperty("equipmentLegs", equipmentLegs);
	}

	public Key getEquipmentLegsKey() {
		return (Key) getCachedEntity().getProperty("equipmentLegs");
	}

	public void setEquipmentNeckKey(Key equipmentNeck) {
		getCachedEntity().setProperty("equipmentNeck", equipmentNeck);
	}

	public Key getEquipmentNeckKey() {
		return (Key) getCachedEntity().getProperty("equipmentNeck");
	}

	public void setEquipmentRightHandKey(Key equipmentRightHand) {
		getCachedEntity().setProperty("equipmentRightHand", equipmentRightHand);
	}

	public Key getEquipmentRightHandKey() {
		return (Key) getCachedEntity().getProperty("equipmentRightHand");
	}

	public void setEquipmentRightRingKey(Key equipmentRightRing) {
		getCachedEntity().setProperty("equipmentRightRing", equipmentRightRing);
	}

	public Key getEquipmentRightRingKey() {
		return (Key) getCachedEntity().getProperty("equipmentRightRing");
	}

	public void setEquipmentShirtKey(Key equipmentShirt) {
		getCachedEntity().setProperty("equipmentShirt", equipmentShirt);
	}

	public Key getEquipmentShirtKey() {
		return (Key) getCachedEntity().getProperty("equipmentShirt");
	}

	public void setEquipmentTransportKey(Key equipmentTransport) {
		getCachedEntity().setProperty("equipmentTransport", equipmentTransport);
	}

	public Key getEquipmentTransportKey() {
		return (Key) getCachedEntity().getProperty("equipmentTransport");
	}

	public void setExperienceMultiplier(Double experienceMultiplier) {
		getCachedEntity().setProperty("experienceMultiplier ", experienceMultiplier);
	}

	public Double getExperienceMultiplier() {
		return (Double) getCachedEntity().getProperty("experienceMultiplier");
	}

	public void setGroupRank(String groupRank) {
		getCachedEntity().setProperty("groupRank", groupRank);
	}

	public String getGroupRank() {
		return (String) getCachedEntity().getProperty("groupRank");
	}

	public void setGroupStatus(GroupStatus groupStatus) {
		getCachedEntity().setProperty("groupStatus", groupStatus);
	}

	public GroupStatus getGroupStatus() {
		return (GroupStatus) getCachedEntity().getProperty("groupStatus");
	}

	public void setHitPoints(Double hitpoints) {
		getCachedEntity().setProperty("hitpoints", hitpoints);
	}

	public Double getHitPoints() {
		return (Double) getCachedEntity().getProperty("hitpoints");
	}

	public void setHomeTownKey(Key homeTownKey) {
		getCachedEntity().setProperty("homeTownKey", homeTownKey);
	}

	public Key getHomeTownKey() {
		return (Key) getCachedEntity().getProperty("homeTownKey");
	}

	public void setIntelligence(Double intelligence) {
		getCachedEntity().setProperty("intelligence", intelligence);
	}

	public Double getIntelligence() {
		return (Double) getCachedEntity().getProperty("intelligence");
	}

	public void setInternalName(String internalName) {
		getCachedEntity().setProperty("internalName", internalName);
	}

	public String getInternalName() {
		return (String) getCachedEntity().getProperty("internalName");
	}

	public void setInventoryKeys(List<Key> inventory) {
		getCachedEntity().setProperty("inventory", inventory);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getInventoryKeys() {
		return (List<Key>) getCachedEntity().getProperty("inventory");
	}

	public void setLocationEntryDatetime(Date locationEntryDatetime) {
		getCachedEntity().setProperty("locationEntryDatetime", locationEntryDatetime);
	}

	public Date getLocationEntryDatetime() {
		return (Date) getCachedEntity().getProperty("locationEntryDatetime");
	}

	public void setLocationKey(Key locationKey) {
		getCachedEntity().setProperty("locationKey", locationKey);
	}

	public Key getLocationKey() {
		return (Key) getCachedEntity().getProperty("locationKey");
	}

	public void setLootPreference(LootPreference lootPreference) {
		getCachedEntity().setProperty("lootPreference", lootPreference);
	}

	public LootPreference getLootPreference() {
		return (LootPreference) getCachedEntity().getProperty("lootPreference");
	}

	public void setMaxHitpoints(Double maxHitpoints) {
		getCachedEntity().setProperty("maxHitpoints", maxHitpoints);
	}

	public Double getMaxHitpoints() {
		return (Double) getCachedEntity().getProperty("maxHitpoints");
	}

	public void setMode(Mode mode) {
		getCachedEntity().setProperty("mode", mode);
	}

	public Mode getMode() {
		return (Mode) getCachedEntity().getProperty("mode");
	}

	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	public void setNameClass(String nameClass) {
		getCachedEntity().setProperty("nameClass", nameClass);
	}

	public String getNameClass() {
		return (String) getCachedEntity().getProperty("nameClass");
	}

	public void setPartyCode(String partyCode) {
		getCachedEntity().setProperty("partyCode", partyCode);
	}

	public String getPartyCode() {
		return (String) getCachedEntity().getProperty("partyCode");
	}

	public void setPartyJoinsAllowed(Boolean partyJoinsAllowed) {
		getCachedEntity().setProperty("partyJoinsAllowed", partyJoinsAllowed);
	}

	public Boolean isPartyJoinsAllowed() {
		return (Boolean) getCachedEntity().getProperty("partyJoinsAllowed");
	}

	public void setPartyLeader(Boolean isPartyLeader) {
		getCachedEntity().setProperty("partyLeader", isPartyLeader);
	}

	public Boolean isPartyLeader() {
		return (Boolean) getCachedEntity().getProperty("partyLeader");
	}

	public void setStatus(Status status) {
		getCachedEntity().setProperty("status", status);
	}

	public Status getStatus() {
		return (Status) getCachedEntity().getProperty("status");
	}

	public void setStoreName(String storeName) {
		getCachedEntity().setProperty("storeName", storeName);
	}

	public String getStoreName() {
		return (String) getCachedEntity().getProperty("storeName");
	}

	public void setStoreSale(Double storeSale) {
		getCachedEntity().setProperty("storeSale", storeSale);
	}

	public Double getStoreSale() {
		return (Double) getCachedEntity().getProperty("storeSale");
	}

	public void setStoreStyleCustomization(String storeStyleCustomization) {
		getCachedEntity().setProperty("storeStyleCustomization", storeStyleCustomization);
	}

	public String getStoreStyleCustomization() {
		return (String) getCachedEntity().getProperty("storeStyleCustomization");
	}

	public void setStrength(Double strength) {
		getCachedEntity().setProperty("strength", strength);
	}

	public Double getStrength() {
		return (Double) getCachedEntity().getProperty("strength");
	}

	public void setType(Type type) {
		getCachedEntity().setProperty("type", type);
	}

	public Type getType() {
		return (Type) getCachedEntity().getProperty("type");
	}

	public void setUserKey(Key userKey) {
		getCachedEntity().setProperty("userKey", userKey);
	}

	public Key getUserKey() {
		return (Key) getCachedEntity().getProperty("userKey");
	}
}
