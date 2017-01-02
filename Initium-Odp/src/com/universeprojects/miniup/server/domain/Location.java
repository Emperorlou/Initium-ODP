package com.universeprojects.miniup.server.domain;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

public class Location extends OdpDomain {

	public Location() {
		super(new CachedEntity("Location"));
	}

	public Location(CachedEntity cachedEntity) {
		super(cachedEntity, "Location");
	}

	public enum AudioDescriptorPreset {
		DenseForest, ForestStream, GrassyPlains, LightForest, Mountains, Ocean, OceanSailing, VeryWindy, Windy,
	}

	public enum BiomeType {
		Cave, Desert, Dungeon, Snow, Temperate,
	}

	public enum CleanupSetting {
		ToCollapse, ToDelete,
	}

	public enum CombatType {
		Instance, Normal, PvP,
	}

	public enum DuelType {

	}

	public enum Type {
		CampSite, CityHall, CollectionSite, CombatSite, Permanent, RestSite, Town,
	}

	public void setAudioDescriptorPreset(AudioDescriptorPreset audioDescriptorPreset) {
		getCachedEntity().setProperty("audioDescriptorPreset", audioDescriptorPreset);
	}

	public AudioDescriptorPreset getAudioDescriptorPreset() {
		return (AudioDescriptorPreset) getCachedEntity().getProperty("audioDescriptorPreset");
	}

	public void setAudioDescriptor(String audioDescriptor) {
		getCachedEntity().setProperty("audioDescriptor", audioDescriptor);
	}

	public String getAudioDescriptor() {
		return (String) getCachedEntity().getProperty("audioDescriptor");
	}

	public void setBanner(String banner) {
		getCachedEntity().setProperty("banner", banner);
	}

	public String getBanner() {
		return (String) getCachedEntity().getProperty("banner");
	}

	public void setBiomeType(BiomeType biomeType) {
		getCachedEntity().setProperty("biomeType", biomeType);
	}

	public BiomeType getBiomeType() {
		return (BiomeType) getCachedEntity().getProperty("biomeType");
	}

	public void setCharactersHereKeys(List<Key> characterKeys) {
		getCachedEntity().setProperty("charactersHere", characterKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getCharactersHereKeys() {
		return (List<Key>) getCachedEntity().getProperty("charactersHere");
	}

	public void setCleanupSetting(CleanupSetting cleanupSetting) {
		getCachedEntity().setProperty("cleanupSetting", cleanupSetting);
	}

	public CleanupSetting getCleanupSetting() {
		return (CleanupSetting) getCachedEntity().getProperty("cleanupSetting");
	}

	public void setCollectableCount(Double collectableCount) {
		getCachedEntity().setProperty("collectableCount", collectableCount);
	}

	public Double getCollectableCount() {
		return (Double) getCachedEntity().getProperty("collectableCount");
	}

	public void setCollectableRegenerationRate(Double collectableRegenerationRate) {
		getCachedEntity().setProperty("collectableRegenerationRate", collectableRegenerationRate);
	}

	public Double getCollectableRegenerationRate() {
		return (Double) getCachedEntity().getProperty("collectableRegenerationRate");
	}

	public void setCollectableSpawnerKeys(List<Key> collectableSpawnerKeys) {
		getCachedEntity().setProperty("collectableSpawners", collectableSpawnerKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getCollectableSpawnerKeys() {
		return (List<Key>) getCachedEntity().getProperty("collectableSpawners");
	}

	public void setCollectableKeys(List<Key> collectableKeys) {
		getCachedEntity().setProperty("collectables", collectableKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getCollectableKeys() {
		return (List<Key>) getCachedEntity().getProperty("collectables");
	}

	public void setDecayRate(Long decayRate) {
		getCachedEntity().setProperty("decayRate", decayRate);
	}

	public Long getDecayRate() {
		return (Long) getCachedEntity().getProperty("decayRate");
	}

	public void setDefenceStructureKey(Key defenceStructureKey) {
		getCachedEntity().setProperty("defenceStructure", defenceStructureKey);
	}

	public Key getDefenceStructureKey() {
		return (Key) getCachedEntity().getProperty("defenceStructure");
	}

	public void setDefenceStructuresAllowed(Boolean defenceStructuresAllowed) {
		getCachedEntity().setProperty("defenceStructuresAllowed", defenceStructuresAllowed);
	}

	public Boolean isDefenceStructuresAllowed() {
		return (Boolean) getCachedEntity().getProperty("defenceStructuresAllowed");
	}

	public void setDescription(String description) {
		getCachedEntity().setProperty("description", description);
	}

	public String getDescription() {
		return (String) getCachedEntity().getProperty("description");
	}

	public void setDiscoverAnythingChance(Double discoverAnythingChance) {
		getCachedEntity().setProperty("discoverAnythingChance", discoverAnythingChance);
	}

	public Double getDiscoverAnythingChance() {
		return (Double) getCachedEntity().getProperty("discoverAnythingChance");
	}

	public void setDuelType(DuelType duelType) {
		getCachedEntity().setProperty("duelType", duelType);
	}

	public DuelType getDuelType() {
		return (DuelType) getCachedEntity().getProperty("duelType");
	}

	public void setInstanceModeEnabled(Boolean isInstanceModeEnabled) {
		getCachedEntity().setProperty("instanceModeEnabled", isInstanceModeEnabled);
	}

	public Boolean isInstanceModeEnabled() {
		return (Boolean) getCachedEntity().getProperty("instanceModeEnabled");
	}

	public void setInstanceRespawnDate(Date instanceRespawnDate) {
		getCachedEntity().setProperty("instanceRespawnDate", instanceRespawnDate);
	}

	public Date getInstanceRespawnDate() {
		return (Date) getCachedEntity().getProperty("instanceRespawnDate");
	}

	public void setInstanceRespawnDelay(Long instanceRespawnDelay) {
		getCachedEntity().setProperty("instanceRespawnDelay", instanceRespawnDelay);
	}

	public Long getInstanceRespawnDelay() {
		return (Long) getCachedEntity().getProperty("instanceRespawnDelay");
	}

	public void setInternalName(String internalName) {
		getCachedEntity().setProperty("internalName", internalName);
	}

	public String getInternalName() {
		return (String) getCachedEntity().getProperty("internalName");
	}

	public void setItemsHereKeys(List<Key> itemKeys) {
		getCachedEntity().setProperty("itemsHere", itemKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getItemsHereKeys() {
		return (List<Key>) getCachedEntity().getProperty("itemsHere");
	}

	public void setMaxCollectableCount(Double maxCollectableCount) {
		getCachedEntity().setProperty("maxCollectableCount", maxCollectableCount);
	}

	public Double getMaxCollectableCount() {
		return (Double) getCachedEntity().getProperty("maxCollectableCount");
	}

	public void setMaxCombatSites(Long maxCombatSites) {
		getCachedEntity().setProperty("maxCombatSites", maxCombatSites);
	}

	public Long getMaxCombatSites() {
		return (Long) getCachedEntity().getProperty("maxCombatSites");
	}

	public void setMaxMonsterCount(Double maxMonsterCount) {
		getCachedEntity().setProperty("maxMonsterCount", maxMonsterCount);
	}

	public Double getMaxMonsterCount() {
		return (Double) getCachedEntity().getProperty("maxMonsterCount");
	}

	public void setMonsterRegenerationRate(Double monsterRegenerationRate) {
		getCachedEntity().setProperty("monsterRegenerationRate", monsterRegenerationRate);
	}

	public Double getMonsterRegenerationRate() {
		return (Double) getCachedEntity().getProperty("monsterRegenerationRate");
	}

	public void setMonsterKeys(List<Key> monsterKeys) {
		getCachedEntity().setProperty("monsters", monsterKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getMonsterKeys() {
		return (List<Key>) getCachedEntity().getProperty("monsters");
	}

	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	public void setNpcArmyKeys(List<Key> npcArmyKeys) {
		getCachedEntity().setProperty("npcArmies", npcArmyKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getNpcArmyKeys() {
		return (List<Key>) getCachedEntity().getProperty("npcArmies");
	}

	public void setIsOutside(Boolean isOutside) {
		getCachedEntity().setProperty("isOutside", isOutside);
	}

	public Boolean isOutside() {
		return (Boolean) getCachedEntity().getProperty("isOutside");
	}

	public void setOwnerKey(Key ownerKey) {
		getCachedEntity().setProperty("ownerKey", ownerKey);
	}

	public Key getOwnerKey() {
		return (Key) getCachedEntity().getProperty("ownerKey");
	}

	public void setParentLocationKey(Key parentLocationKey) {
		getCachedEntity().setProperty("parentLocationKey", parentLocationKey);
	}

	public Key getParentLocationKey() {
		return (Key) getCachedEntity().getProperty("parentLocationKey");
	}

	public void setPathsFromKeys(List<Key> pathKeys) {
		getCachedEntity().setProperty("pathsFrom", pathKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getPathsFromKeys() {
		return (List<Key>) getCachedEntity().getProperty("pathsFrom");
	}

	public void setPathsToKeys(List<Key> pathKeys) {
		getCachedEntity().setProperty("pathsTo", pathKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getPathsToKeys() {
		return (List<Key>) getCachedEntity().getProperty("pathsTo");
	}

	public void setScriptKeys(List<Key> scriptKeys) {
		getCachedEntity().setProperty("scripts", scriptKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getScriptKeys() {
		return (List<Key>) getCachedEntity().getProperty("scripts");
	}

	public void setSupportsCamps(Long supportsCamps) {
		getCachedEntity().setProperty("supportsCamps", supportsCamps);
	}

	public Long getSupportsCamps() {
		return (Long) getCachedEntity().getProperty("supportsCamps");
	}

	public void setTerritoryKey(Key territoryKey) {
		getCachedEntity().setProperty("territoryKey", territoryKey);
	}

	public Key getTerritoryKey() {
		return (Key) getCachedEntity().getProperty("territoryKey");
	}

	public void setType(Type type) {
		getCachedEntity().setProperty("type", type);
	}

	public Type getType() {
		return (Type) getCachedEntity().getProperty("type");
	}
}
