package com.universeprojects.miniup.server.domain;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * Characters and items exist in locations. Paths connect locations so characters can travel between them.
 * 
 * @author kyle-miller
 *
 */
public class Location extends OdpDomain {
	public static final String KIND = "Location";

	public Location() {
		super(new CachedEntity(KIND));
	}

	public Location(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
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

	/**
	 * This field requires a very special format. See the docs for details. For reference: filename(oddsOfPlaying,volume,fadeInOut,mode) - Multiple sounds are separated by && - The filename is always WITHOUT the extension.
	 * 
	 * @param audioDescriptorPreset
	 */
	public void setAudioDescriptorPreset(AudioDescriptorPreset audioDescriptorPreset) {
		getCachedEntity().setProperty("audioDescriptorPreset", audioDescriptorPreset);
	}

	public AudioDescriptorPreset getAudioDescriptorPreset() {
		return (AudioDescriptorPreset) getCachedEntity().getProperty("audioDescriptorPreset");
	}

	/**
	 * Choose from a list of preset audioDescriptors. The actual audioDescriptor will add sounds IN ADDITION to the preset
	 * 
	 * @param audioDescriptor
	 */
	public void setAudioDescriptor(String audioDescriptor) {
		getCachedEntity().setProperty("audioDescriptor", audioDescriptor);
	}

	public String getAudioDescriptor() {
		return (String) getCachedEntity().getProperty("audioDescriptor");
	}

	/**
	 * This is the big image that is displayed when a player visits this location.
	 * 
	 * @param banner
	 */
	public void setBanner(String banner) {
		getCachedEntity().setProperty("banner", banner);
	}

	public String getBanner() {
		return (String) getCachedEntity().getProperty("banner");
	}

	/**
	 * This is mostly used to determine what the environment looks like for the walking/exploring animation.
	 * 
	 * @param biomeType
	 */
	public void setBiomeType(BiomeType biomeType) {
		getCachedEntity().setProperty("biomeType", biomeType);
	}

	public BiomeType getBiomeType() {
		return (BiomeType) getCachedEntity().getProperty("biomeType");
	}

	/**
	 * List of characters currently in this location.
	 * 
	 * @param characterKeys
	 */
	public void setCharactersHereKeys(List<Key> characterKeys) {
		getCachedEntity().setProperty("charactersHere", characterKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getCharactersHereKeys() {
		return (List<Key>) getCachedEntity().getProperty("charactersHere");
	}

	/**
	 * Leave this blank. When a location is set to "ToDelete", a cron job will go through and delete this location and it's contents at a later date. If it is set to ToCollapse, a cron job will MOVE the contents of this location to the parent.
	 * 
	 * @param cleanupSetting
	 */
	public void setCleanupSetting(CleanupSetting cleanupSetting) {
		getCachedEntity().setProperty("cleanupSetting", cleanupSetting);
	}

	public CleanupSetting getCleanupSetting() {
		return (CleanupSetting) getCachedEntity().getProperty("cleanupSetting");
	}

	/**
	 * The number of collectables players need to find before the location's overall spawn rate drops to 0%.
	 * 
	 * @param collectableCount
	 */
	public void setCollectableCount(Double collectableCount) {
		getCachedEntity().setProperty("collectableCount", collectableCount);
	}

	public Double getCollectableCount() {
		return (Double) getCachedEntity().getProperty("collectableCount");
	}

	/**
	 * Every 10 minutes, the collectables count will increase by this percentage (of the maxCollectableCount). Note, it can be a decimal value.
	 * 
	 * @param collectableRegenerationRate
	 */
	public void setCollectableRegenerationRate(Double collectableRegenerationRate) {
		getCachedEntity().setProperty("collectableRegenerationRate", collectableRegenerationRate);
	}

	public Double getCollectableRegenerationRate() {
		return (Double) getCachedEntity().getProperty("collectableRegenerationRate");
	}

	/**
	 * All the collectable spawners that are associated with this location. Collectable spawners define the odds of a player finding a collectable node, and the CollectableDef that is to be used to create the collectable.
	 * 
	 * @param collectableSpawnerKeys
	 */
	public void setCollectableSpawnerKeys(List<Key> collectableSpawnerKeys) {
		getCachedEntity().setProperty("collectableSpawners", collectableSpawnerKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getCollectableSpawnerKeys() {
		return (List<Key>) getCachedEntity().getProperty("collectableSpawners");
	}

	/**
	 * This defines the way the location treats combat and denotes what the players are allowed to do regarding combat. Instance = Fights with monsters are always 1v1. PvP = Fights between players are permitted.
	 * 
	 * @param collectableKeys
	 */
	public void setCollectableKeys(List<Key> collectableKeys) {
		getCachedEntity().setProperty("collectables", collectableKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getCollectableKeys() {
		return (List<Key>) getCachedEntity().getProperty("collectables");
	}

	/**
	 * All collectables that are associated with this location. These are nodes where raw materials can be farmed from.
	 * 
	 * @param collectableKeys
	 */
	public void setCombatType(CombatType combatType) {
		getCachedEntity().setProperty("combatType", combatType);
	}

	public CombatType getCombatType() {
		return (CombatType) getCachedEntity().getProperty("collectables");
	}

	/**
	 * A percentage. This indicates how quickly items deteriorate when left on the ground.
	 * 
	 * @param decayRate
	 */
	public void setDecayRate(Long decayRate) {
		getCachedEntity().setProperty("decayRate", decayRate);
	}

	public Long getDecayRate() {
		return (Long) getCachedEntity().getProperty("decayRate");
	}

	/**
	 * 
	 * @param defenceStructureKey
	 */
	public void setDefenceStructureKey(Key defenceStructureKey) {
		getCachedEntity().setProperty("defenceStructure", defenceStructureKey);
	}

	public Key getDefenceStructureKey() {
		return (Key) getCachedEntity().getProperty("defenceStructure");
	}

	/**
	 * THIS IS IMPORTANT. It must be set to TRUE for any location that could be defended by defenders. It is not enough to set this to true on the location that actually contains the DefenceStructure, it must also be set on surrounding locations that a defence structure protects. ALSO, remember to set any connecting paths types to BlockadeSite.
	 * 
	 * @param defenceStructuresAllowed
	 */
	public void setDefenceStructuresAllowed(Boolean defenceStructuresAllowed) {
		getCachedEntity().setProperty("defenceStructuresAllowed", defenceStructuresAllowed);
	}

	public Boolean isDefenceStructuresAllowed() {
		return (Boolean) getCachedEntity().getProperty("defenceStructuresAllowed");
	}

	/**
	 * The player will see this description of the location every time.
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		getCachedEntity().setProperty("description", description);
	}

	public String getDescription() {
		return (String) getCachedEntity().getProperty("description");
	}

	/**
	 * Out of 100. The odds that a player exploring this location will find anything at all.
	 * 
	 * @param discoverAnythingChance
	 */
	public void setDiscoverAnythingChance(Double discoverAnythingChance) {
		getCachedEntity().setProperty("discoverAnythingChance", discoverAnythingChance);
	}

	public Double getDiscoverAnythingChance() {
		return (Double) getCachedEntity().getProperty("discoverAnythingChance");
	}

	/**
	 * If this location is a DuelSite, this field describes how to deal with deaths when the duel is over.
	 * 
	 * @param duelType
	 */
	public void setDuelType(DuelType duelType) {
		getCachedEntity().setProperty("duelType", duelType);
	}

	public DuelType getDuelType() {
		return (DuelType) getCachedEntity().getProperty("duelType");
	}

	/**
	 * This turns on instance mode for this location.
	 * 
	 * @param isInstanceModeEnabled
	 */
	public void setInstanceModeEnabled(Boolean isInstanceModeEnabled) {
		getCachedEntity().setProperty("instanceModeEnabled", isInstanceModeEnabled);
	}

	public Boolean isInstanceModeEnabled() {
		return (Boolean) getCachedEntity().getProperty("instanceModeEnabled");
	}

	/**
	 * The actual date that the instance will respawn. If this is null, then no respawn is scheduled. The date is set when the first monster is killed.
	 * 
	 * @param instanceRespawnDate
	 */
	public void setInstanceRespawnDate(Date instanceRespawnDate) {
		getCachedEntity().setProperty("instanceRespawnDate", instanceRespawnDate);
	}

	public Date getInstanceRespawnDate() {
		return (Date) getCachedEntity().getProperty("instanceRespawnDate");
	}

	/**
	 * This is the amount of time (in minutes) that the instance will wait before respawning all monsters starting from the first monster death.
	 * 
	 * @param instanceRespawnDelay
	 */
	public void setInstanceRespawnDelay(Long instanceRespawnDelay) {
		getCachedEntity().setProperty("instanceRespawnDelay", instanceRespawnDelay);
	}

	public Long getInstanceRespawnDelay() {
		return (Long) getCachedEntity().getProperty("instanceRespawnDelay");
	}

	/**
	 * This is for developers only. It's a name that is not visible to the players and is used for internal purposes only.
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
	 * If this location is considered an outside location, then set this to true. This is used for the day/night cycle and when showing rain/weather. If this is true, then at night the banner image will become dark and when it is raining, an animated rain overlay will be shown.
	 * 
	 * @param itemKeys
	 */
	public void setItemsHereKeys(List<Key> itemKeys) {
		getCachedEntity().setProperty("itemsHere", itemKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getItemsHereKeys() {
		return (List<Key>) getCachedEntity().getProperty("itemsHere");
	}

	/**
	 * 
	 * @param maxCollectableCount
	 */
	public void setMaxCollectableCount(Double maxCollectableCount) {
		getCachedEntity().setProperty("maxCollectableCount", maxCollectableCount);
	}

	public Double getMaxCollectableCount() {
		return (Double) getCachedEntity().getProperty("maxCollectableCount");
	}

	/**
	 * 
	 * @param maxCombatSites
	 */
	public void setMaxCombatSites(Long maxCombatSites) {
		getCachedEntity().setProperty("maxCombatSites", maxCombatSites);
	}

	public Long getMaxCombatSites() {
		return (Long) getCachedEntity().getProperty("maxCombatSites");
	}

	/**
	 * The MAXIMUM number of monsters players need to kill before the location's overall spawn rate drops to 0%. This value is used to determine how high the monsterCount should regenerate to.
	 * 
	 * @param maxMonsterCount
	 */
	public void setMaxMonsterCount(Double maxMonsterCount) {
		getCachedEntity().setProperty("maxMonsterCount", maxMonsterCount);
	}

	public Double getMaxMonsterCount() {
		return (Double) getCachedEntity().getProperty("maxMonsterCount");
	}

	/**
	 * Every 10 minutes, the monster count will increase by this percentage (of the maxMonsterCount). Note, it can be a decimal value. This would be especially useful for bosses that should only spawn once in a while. For example, setting a maxMonsterCount of 1 on a boss site, and setting a regeneration rate of 1 would cause the boss to be available again in 16.66 hours.
	 * 
	 * @param monsterRegenerationRate
	 */
	public void setMonsterRegenerationRate(Double monsterRegenerationRate) {
		getCachedEntity().setProperty("monsterRegenerationRate", monsterRegenerationRate);
	}

	public Double getMonsterRegenerationRate() {
		return (Double) getCachedEntity().getProperty("monsterRegenerationRate");
	}

	/**
	 * All the monster spawners that are associated with this location. Monster spawners define the odds of a player finding a monster, and the NPCDef that is to be used to create the monster.
	 * 
	 * @param monsterKeys
	 */
	public void setMonsterKeys(List<Key> monsterKeys) {
		getCachedEntity().setProperty("monsters", monsterKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getMonsterKeys() {
		return (List<Key>) getCachedEntity().getProperty("monsters");
	}

	/**
	 * The name of the location.
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
	 * All of the NPCArmy entities that are linked to this location.
	 * 
	 * @param npcArmyKeys
	 */
	public void setNpcArmyKeys(List<Key> npcArmyKeys) {
		getCachedEntity().setProperty("npcArmies", npcArmyKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getNpcArmyKeys() {
		return (List<Key>) getCachedEntity().getProperty("npcArmies");
	}

	/**
	 * 
	 * @param isOutside
	 */
	public void setIsOutside(Boolean isOutside) {
		getCachedEntity().setProperty("isOutside", isOutside);
	}

	public Boolean isOutside() {
		return (Boolean) getCachedEntity().getProperty("isOutside");
	}

	/**
	 * The player (or group) that owns this location. Usually this is blank unless it is a player house.
	 * 
	 * @param ownerKey
	 */
	public void setOwnerKey(Key ownerKey) {
		getCachedEntity().setProperty("ownerKey", ownerKey);
	}

	public Key getOwnerKey() {
		return (Key) getCachedEntity().getProperty("ownerKey");
	}

	/**
	 * For certain types of locations, they require a parent location in order to function correctly. Even permanent locations should have a parent location so that characters running from a monster in that location (not in a combat site) knows which way to run to.
	 * 
	 * @param parentLocationKey
	 */
	public void setParentLocationKey(Key parentLocationKey) {
		getCachedEntity().setProperty("parentLocationKey", parentLocationKey);
	}

	public Key getParentLocationKey() {
		return (Key) getCachedEntity().getProperty("parentLocationKey");
	}

	/**
	 * All the paths that lead to/from this location. You can consider the pathsFrom and pathsTo fields to be the same list, it is just a technicality that we had to split them up into 2.
	 * 
	 * @param pathKeys
	 */
	public void setPathsFromKeys(List<Key> pathKeys) {
		getCachedEntity().setProperty("pathsFrom", pathKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getPathsFromKeys() {
		return (List<Key>) getCachedEntity().getProperty("pathsFrom");
	}

	/**
	 * All the paths that lead to/from this location. You can consider the pathsFrom and pathsTo fields to be the same list, it is just a technicality that we had to split them up into 2.
	 * 
	 * @param pathKeys
	 */
	public void setPathsToKeys(List<Key> pathKeys) {
		getCachedEntity().setProperty("pathsTo", pathKeys);
	}

	@SuppressWarnings("unchecked")
	public List<Key> getPathsToKeys() {
		return (List<Key>) getCachedEntity().getProperty("pathsTo");
	}

	/**
	 * A list of scripts that are attached to this location. These will be directly clickable by anyone who is here.
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
	 * If this location should allow players to create a camp, set this to the number of camps that the location can support. PLEASE NOTE: Only set this to 1 for the time being. Multiple camps seems to be an annoyance and doesn't offer any real benefit.
	 * 
	 * @param supportsCamps
	 */
	public void setSupportsCamps(Long supportsCamps) {
		getCachedEntity().setProperty("supportsCamps", supportsCamps);
	}

	public Long getSupportsCamps() {
		return (Long) getCachedEntity().getProperty("supportsCamps");
	}

	/**
	 * The territory that this location belongs to.
	 * 
	 * @param territoryKey
	 */
	public void setTerritoryKey(Key territoryKey) {
		getCachedEntity().setProperty("territoryKey", territoryKey);
	}

	public Key getTerritoryKey() {
		return (Key) getCachedEntity().getProperty("territoryKey");
	}

	/**
	 * This field allows the game mechanics to treat a location differently depending on it`s type. If no type is specified, then permanent is assumed.
	 * 
	 * @param type
	 */
	public void setType(Type type) {
		getCachedEntity().setProperty("type", type);
	}

	public Type getType() {
		return (Type) getCachedEntity().getProperty("type");
	}
}
