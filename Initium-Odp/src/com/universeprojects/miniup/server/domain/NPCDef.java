package com.universeprojects.miniup.server.domain;

import java.util.List;

import com.universeprojects.cacheddatastore.CachedEntity;

//Defines how to generate a new NPC for the character entity type.
public class NPCDef extends OdpDomain {

	public NPCDef() {
		super(new CachedEntity("NPCDef"));
	}

	public NPCDef(CachedEntity cachedEntity) {
		super(cachedEntity, "NPCDef");
	}

	// (images/npc-generic1.jpg)
	public void setBannerUrl(String bannerUrl) {
		getCachedEntity().setProperty("bannerUrl", bannerUrl);
	}

	public String getBannerUrl() {
		return (String) getCachedEntity().getProperty("bannerUrl");
	}

	public void setCloaked(Boolean cloaked) {
		getCachedEntity().setProperty("cloaked", cloaked);
	}

	public Boolean getCloaked() {
		return (Boolean) getCachedEntity().getProperty("cloaked");
	}

	// The dexterity for this NPC. Dexterity is used to determine the hit/dodge chance in combat. For a standard human, 5 is the average dexterity. Keep in mind that the effect of ALL stats increase exponentially as they go up, so keep this in mind.
	public void setDexterity(String dexterity) {
		getCachedEntity().setProperty("dexterity", dexterity);
	}

	public String getDexterity() {
		return (String) getCachedEntity().getProperty("dexterity");
	}

	// The number of Dogecoins this NPC will drop. This number will be scaled down depending on the Dogecoin reserves available (but content developers don`t need to worry about that).
	public void setDogecoins(String dogecoins) {
		getCachedEntity().setProperty("dogecoins", dogecoins);
	}

	public String getDogecoins() {
		return (String) getCachedEntity().getProperty("dogecoins");
	}

	// Default is 1 for blank entries. Low level monsters should be 1, high level monsters should be at most 5 (5x the experience gain rate). Choose the value that best suits your monster based on it's difficulty to kill for a character with max stats.
	public void setExperienceMultiplier(Double experienceMultiplier) {
		getCachedEntity().setProperty("experienceMultiplier", experienceMultiplier);
	}

	public Double getExperienceMultiplier() {
		return (Double) getCachedEntity().getProperty("experienceMultiplier");
	}

	// The amount of hitpoints this NPC will have. Generally, a standard human should have 15-20 hp.
	public void setHitpoints(String hitpoints) {
		getCachedEntity().setProperty("hitpoints", hitpoints);
	}

	public String getHitpoints() {
		return (String) getCachedEntity().getProperty("hitpoints");
	}

	// The intelligence for this NPC. At the moment intelligence isn`t used, but we may eventually use it to affect critical hit chances in the future. Keep in mind that the effect of ALL stats increase exponentially as they go up, so keep this in mind.
	public void setIntelligence(String intelligence) {
		getCachedEntity().setProperty("intelligence", intelligence);
	}

	public String getIntelligence() {
		return (String) getCachedEntity().getProperty("intelligence");
	}

	public void setInternalName(String internalName) {
		getCachedEntity().setProperty("internalName", internalName);
	}

	public String getInternalName() {
		return (String) getCachedEntity().getProperty("internalName");
	}

	// (ItemSpawner|npcDefKey)
	public void setItemSpawners(List<ItemSpawner> itemSpawners) {
		getCachedEntity().setProperty("itemSpawners", itemSpawners);
	}

	@SuppressWarnings("unchecked")
	public List<ItemSpawner> getItemSpawners() {
		return (List<ItemSpawner>) getCachedEntity().getProperty("itemSpawners");
	}

	// (MonsterSpawner|npcDefKey)
	public void setMonsterSpawners(List<MonsterSpawner> monsterSpawners) {
		getCachedEntity().setProperty("monsterSpawners", monsterSpawners);
	}

	@SuppressWarnings("unchecked")
	public List<MonsterSpawner> getMonsterSpawners() {
		return (List<MonsterSpawner>) getCachedEntity().getProperty("monsterSpawners");
	}

	// This is the name of the NPC. It is carried forward when the NPC is generated from the NPCDef
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	// These numbers override the usual weather modifiers for spawn rates. Leave them blank if you want the default weather effects. The modifiers are percentage based, so 100 means no change, 50 means half, 200 means double, etc. For example, if you want to make sure that your monster NEVER spawns when it's raining, you would set spawnChanceRainMultiplier to 0. (NOTE: Spawn multiplier effects do not apply when the spawn location is indoors. Weather and time of day can only affect outdoor spawns.)
	public void setSpawnChanceClearMultiplier(Long spawnChanceClearMultiplier) {
		getCachedEntity().setProperty("spawnChanceClearMultiplier", spawnChanceClearMultiplier);
	}

	public Long getSpawnChanceClearMultiplier() {
		return (Long) getCachedEntity().getProperty("spawnChanceClearMultiplier");
	}

	// These numbers override the usual weather modifiers for spawn rates. Leave them blank if you want the default weather effects. The modifiers are percentage based, so 100 means no change, 50 means half, 200 means double, etc. For example, if you want to make sure that your monster NEVER spawns when it's raining, you would set spawnChanceRainMultiplier to 0. (NOTE: Spawn multiplier effects do not apply when the spawn location is indoors. Weather and time of day can only affect outdoor spawns.)
	public void setSpawnChanceDaytimeMultiplier(Long spawnChanceDaytimeMultiplier) {
		getCachedEntity().setProperty("spawnChanceDaytimeMultiplier", spawnChanceDaytimeMultiplier);
	}

	public Long getSpawnChanceDaytimeMultiplier() {
		return (Long) getCachedEntity().getProperty("spawnChanceDaytimeMultiplier");
	}

	// These numbers override the usual weather modifiers for spawn rates. Leave them blank if you want the default weather effects. The modifiers are percentage based, so 100 means no change, 50 means half, 200 means double, etc. For example, if you want to make sure that your monster NEVER spawns when it's raining, you would set spawnChanceRainMultiplier to 0. (NOTE: Spawn multiplier effects do not apply when the spawn location is indoors. Weather and time of day can only affect outdoor spawns.)
	public void setSpawnChanceNighttimeMultiplier(Long spawnChanceNighttimeMultiplier) {
		getCachedEntity().setProperty("spawnChanceNighttimeMultiplier", spawnChanceNighttimeMultiplier);
	}

	public Long getSpawnChanceNighttimeMultiplier() {
		return (Long) getCachedEntity().getProperty("spawnChanceNighttimeMultiplier");
	}

	// These numbers override the usual weather modifiers for spawn rates. Leave them blank if you want the default weather effects. The modifiers are percentage based, so 100 means no change, 50 means half, 200 means double, etc. For example, if you want to make sure that your monster NEVER spawns when it's raining, you would set spawnChanceRainMultiplier to 0. (NOTE: Spawn multiplier effects do not apply when the spawn location is indoors. Weather and time of day can only affect outdoor spawns.)
	public void setSpawnChanceRainMultiplier(Long spawnChanceRainMultiplier) {
		getCachedEntity().setProperty("spawnChanceRainMultiplier", spawnChanceRainMultiplier);
	}

	public Long getSpawnChanceRainMultiplier() {
		return (Long) getCachedEntity().getProperty("spawnChanceRainMultiplier");
	}

	// The strength for this NPC. At the moment, Strength isn't used very much (only affects hp on player characters). Eventually, Strength will be used to determine how much a player can carry, hitpoints, how much additional damage is done from a hit, how much damage a shield can block. Keep in mind that the effect of ALL stats increase exponentially as they go up, so keep this in mind.
	public void setStrength(String strength) {
		getCachedEntity().setProperty("strength", strength);
	}

	public String getStrength() {
		return (String) getCachedEntity().getProperty("strength");
	}

	// This should just always be NPC since NPCDef always creates an NPC.
	public void setType(String type) {
		getCachedEntity().setProperty("type", type);
	}

	public String getType() {
		return (String) getCachedEntity().getProperty("type");
	}

	public enum AutomaticWeaponChoiceMethod {
		Rarest, HighestDamage, Random,
	}

	public void setAutomaticWeaponChoiceMethod(AutomaticWeaponChoiceMethod automaticWeaponChoiceMethod) {
		getCachedEntity().setProperty("automaticWeaponChoiceMethod", automaticWeaponChoiceMethod);
	}

	public AutomaticWeaponChoiceMethod getAutomaticWeaponChoiceMethod() {
		return (AutomaticWeaponChoiceMethod) getCachedEntity().getProperty("automaticWeaponChoiceMethod");
	}

	public enum ItemTypeMinimums {
		Weapon, Armor, Other,
	}

	public void setItemTypeMinimums(ItemTypeMinimums itemTypeMinimums) {
		getCachedEntity().setProperty("itemTypeMinimums", itemTypeMinimums);
	}

	public ItemTypeMinimums getItemTypeMinimums() {
		return (ItemTypeMinimums) getCachedEntity().getProperty("itemTypeMinimums");
	}

	public enum LootPreference {
		CollectEverything, CollectBetter, CollectNone,
	}

	public void setLootPreference(LootPreference lootPreference) {
		getCachedEntity().setProperty("lootPreference", lootPreference);
	}

	public LootPreference getLootPreference() {
		return (LootPreference) getCachedEntity().getProperty("lootPreference");
	}

	public enum Status {
		Normal, Defending1, Defending2, Defending3, Zombie,
	}

	public void setStatus(Status status) {
		getCachedEntity().setProperty("status", status);
	}

	public Status getStatus() {
		return (Status) getCachedEntity().getProperty("status");
	}

}
