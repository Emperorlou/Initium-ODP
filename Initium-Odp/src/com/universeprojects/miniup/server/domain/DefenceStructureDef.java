package com.universeprojects.miniup.server.domain;

import com.universeprojects.cacheddatastore.CachedEntity;

//This is used to describe how to create a DefenceStructure object for the game.
public class DefenceStructureDef extends OdpDomain {

	public DefenceStructureDef() {
		super(new CachedEntity("DefenceStructureDef"));
	}

	public DefenceStructureDef(CachedEntity cachedEntity) {
		super(cachedEntity, "DefenceStructureDef");
	}

	public void setBannerUrl(String bannerUrl) {
		getCachedEntity().setProperty("bannerUrl", bannerUrl);
	}

	public String getBannerUrl() {
		return (String) getCachedEntity().getProperty("bannerUrl");
	}

	public void setBlockBludgeoningCapability(String blockBludgeoningCapability) {
		getCachedEntity().setProperty("blockBludgeoningCapability", blockBludgeoningCapability);
	}

	public String getBlockBludgeoningCapability() {
		return (String) getCachedEntity().getProperty("blockBludgeoningCapability");
	}

	public void setBlockPiercingCapability(String blockPiercingCapability) {
		getCachedEntity().setProperty("blockPiercingCapability", blockPiercingCapability);
	}

	public String getBlockPiercingCapability() {
		return (String) getCachedEntity().getProperty("blockPiercingCapability");
	}

	public void setBlockSlashingCapability(String blockSlashingCapability) {
		getCachedEntity().setProperty("blockSlashingCapability", blockSlashingCapability);
	}

	public String getBlockSlashingCapability() {
		return (String) getCachedEntity().getProperty("blockSlashingCapability");
	}

	public void setHitpoints(String hitpoints) {
		getCachedEntity().setProperty("hitpoints", hitpoints);
	}

	public String getHitpoints() {
		return (String) getCachedEntity().getProperty("hitpoints");
	}

	public void setMaxHitpoints(String maxHitpoints) {
		getCachedEntity().setProperty("maxHitpoints", maxHitpoints);
	}

	public String getMaxHitpoints() {
		return (String) getCachedEntity().getProperty("maxHitpoints");
	}

	public void setTypeName(String typeName) {
		getCachedEntity().setProperty("typeName", typeName);
	}

	public String getTypeName() {
		return (String) getCachedEntity().getProperty("typeName");
	}

	public void setWeaponRangeBonus(String weaponRangeBonus) {
		getCachedEntity().setProperty("weaponRangeBonus", weaponRangeBonus);
	}

	public String getWeaponRangeBonus() {
		return (String) getCachedEntity().getProperty("weaponRangeBonus");
	}

}
