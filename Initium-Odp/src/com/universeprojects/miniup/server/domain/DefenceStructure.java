package com.universeprojects.miniup.server.domain;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * This entity is used to describe a defensive structure in the game world. It has hitpoints so it can be destroyed. It is attached to a location and acts as a blockade to keep other players from entering said location.
 * 
 * @author kyle-miller
 *
 */
public class DefenceStructure extends OdpDomain {
	public static final String KIND = "DefenceStructure";

	public DefenceStructure() {
		super(new CachedEntity(KIND));
	}

	public DefenceStructure(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 *  The banner that will be used when a player is not in combat at the defence structure site.
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
	 *  How well the structure blocks these kinds of attacks. This is a direct damage reduction.
	 *  
	 * @param blockBludgeoningCapability
	 */
	public void setBlockBludgeoningCapability(Long blockBludgeoningCapability) {
		getCachedEntity().setProperty("blockBludgeoningCapability", blockBludgeoningCapability);
	}

	public Long getBlockBludgeoningCapability() {
		return (Long) getCachedEntity().getProperty("blockBludgeoningCapability");
	}

	/**
	 *  How well the structure blocks these kinds of attacks. This is a direct damage reduction.
	 *  
	 * @param blockPiercingCapability
	 */
	public void setBlockPiercingCapability(Long blockPiercingCapability) {
		getCachedEntity().setProperty("blockPiercingCapability", blockPiercingCapability);
	}

	public Long getBlockPiercingCapability() {
		return (Long) getCachedEntity().getProperty("blockPiercingCapability");
	}

	/**
	 *  How well the structure blocks these kinds of attacks. This is a direct damage reduction.
	 *  
	 * @param blockSlashingCapability
	 */
	public void setBlockSlashingCapability(Long blockSlashingCapability) {
		getCachedEntity().setProperty("blockSlashingCapability", blockSlashingCapability);
	}

	public Long getBlockSlashingCapability() {
		return (Long) getCachedEntity().getProperty("blockSlashingCapability");
	}

	/**
	 *  The hitpoints remaining on the structure itself.
	 *  
	 * @param hitpoints
	 */
	public void setHitpoints(Double hitpoints) {
		getCachedEntity().setProperty("hitpoints", hitpoints);
	}

	public Double getHitpoints() {
		return (Double) getCachedEntity().getProperty("hitpoints");
	}

	/**
	 *  (Character)
	 *  
	 * @param leaderKey
	 */
	public void setLeaderKey(Key leaderKey) {
		getCachedEntity().setProperty("leaderKey", leaderKey);
	}

	public Key getLeaderKey() {
		return (Key) getCachedEntity().getProperty("leaderKey");
	}

	/**
	 *  (Location|type==Permanent)
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
	 *  The materials that were used to construct this structure. When the structure is destroyed or dismantled, some of the materials may be recoverable.
	 *  
	 * @param materialsUsed
	 */
	public void setMaterialsUsed(List<String> materialsUsed) { // TODO - type
		getCachedEntity().setProperty("materialsUsed", materialsUsed);
	}

	@SuppressWarnings("unchecked")
	public List<String> getMaterialsUsed() {
		return (List<String>) getCachedEntity().getProperty("materialsUsed");
	}

	/**
	 *  The maximum hitpoints the structure can have.
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
	 * A name given to the structure by the builders.
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
	 *  The type of structure this is. For example, a Wooden Tower, or Stone Tower, or Keep, or Wooden Gated Campsite.
	 *  
	 * @param typeName
	 */
	public void setTypeName(String typeName) {
		getCachedEntity().setProperty("typeName", typeName);
	}

	public String getTypeName() {
		return (String) getCachedEntity().getProperty("typeName");
	}

	/**
	 *  When a player attacks the structure, this is the weapon range bonus defenders are given when they attack with ranged weapons.
	 *  
	 * @param weaponRangeBonus
	 */
	public void setWeaponRangeBonus(Long weaponRangeBonus) {
		getCachedEntity().setProperty("weaponRangeBonus", weaponRangeBonus);
	}

	public Long getWeaponRangeBonus() {
		return (Long) getCachedEntity().getProperty("weaponRangeBonus");
	}

	public enum BlockadeRule {
		BlockAllParent("BlockAllParent"), BlockAllSelf("BlockAllSelf"), None("None");

		private String value;

		private BlockadeRule(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	/**
	 * 
	 * @param blockadeRule
	 */
	public void setBlockadeRule(BlockadeRule blockadeRule) {
		getCachedEntity().setProperty("blockadeRule", blockadeRule);
	}

	public BlockadeRule getBlockadeRule() {
		return (BlockadeRule) getCachedEntity().getProperty("blockadeRule");
	}

	/**
	 * 
	 * @param blockadeRuleChangeable
	 */
	public void setBlockadeRuleChangeable(Boolean blockadeRuleChangeable) {
		getCachedEntity().setProperty("blockadeRuleChangeable", blockadeRuleChangeable);
	}

	public Boolean getBlockadeRuleChangeable() {
		return (Boolean) getCachedEntity().getProperty("blockadeRuleChangeable");
	}

}
