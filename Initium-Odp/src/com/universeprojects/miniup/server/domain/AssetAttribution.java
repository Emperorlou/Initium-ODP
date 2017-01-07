package com.universeprojects.miniup.server.domain;

import com.google.appengine.api.datastore.Text;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * Represents the credit given to an artist for one piece of artwork or other asset used in the game. AssetAttribution entities will get listed on a credits page in the game so that artists receive proper attribution.
 * 
 * @author kyle-miller
 *
 */
public class AssetAttribution extends OdpDomain {
	public static final String KIND = "AssetAttribution";

	public AssetAttribution() {
		super(new CachedEntity(KIND));
	}

	private AssetAttribution(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	public static final AssetAttribution wrap(CachedEntity cachedEntity) {
		return new AssetAttribution(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 * An easily recognizable way to refer to the artist. This should be the name that the artist chooses to go by professionally.
	 * 
	 * @param artistName
	 */
	public void setArtistName(String artistName) {
		getCachedEntity().setProperty("artistName", artistName);
	}

	public String getArtistName() {
		return (String) getCachedEntity().getProperty("artistName");
	}

	/**
	 * The website of the artist where the asset was originally hosted. This should be where the artist himself hosted the image, which may be different from where the image was found.
	 * 
	 * @param artistWebsite
	 */
	public void setArtistWebsite(String artistWebsite) {
		getCachedEntity().setProperty("artistWebsite", artistWebsite);
	}

	public String getArtistWebsite() {
		return (String) getCachedEntity().getProperty("artistWebsite");
	}

	/**
	 * This is the URL of the asset within our database. This will link the asset after modifications are done and it is ready to go into the game.
	 * 
	 * @param assetDatabaseURL
	 */
	public void setAssetDatabaseURL(String assetDatabaseURL) {
		getCachedEntity().setProperty("assetDatabaseURL", assetDatabaseURL);
	}

	public String getAssetDatabaseURL() {
		return (String) getCachedEntity().getProperty("assetDatabaseURL");
	}

	/**
	 * This is the URL of the original asset and should be a direct link to the asset file itself. This will link the asset before any modifications were done (the 'raw' art).
	 * 
	 * @param assetOriginalURL
	 */
	public void setAssetOriginalURL(String assetOriginalURL) {
		getCachedEntity().setProperty("assetOriginalURL", assetOriginalURL);
	}

	public String getAssetOriginalURL() {
		return (String) getCachedEntity().getProperty("assetOriginalURL");
	}

	/**
	 * This is a link to the license itself that applies to the asset on the creativecommons.org website. An example would be: https://creativecommons.org/licenses/by/3.0/legalcode
	 * 
	 * @param licenseLink
	 */
	public void setLicenseLink(String licenseLink) {
		getCachedEntity().setProperty("licenseLink", licenseLink);
	}

	public String getLicenseLink() {
		return (String) getCachedEntity().getProperty("licenseLink");
	}

	/**
	 * This is the type of license that applies to the asset. An example would be: Attribution 3.0 Unported (CC BY 3.0)
	 * 
	 * @param licenseType
	 */
	public void setLicenseType(String licenseType) {
		getCachedEntity().setProperty("licenseType", licenseType);
	}

	public String getLicenseType() {
		return (String) getCachedEntity().getProperty("licenseType");
	}

	/**
	 * This must list all changes done to the asset that in any way modified it from its original state. This includes cropping, filtering, editing, and any other modifications.
	 * 
	 * @param modificationsMade
	 */
	public void setModificationsMade(Text modificationsMade) {
		getCachedEntity().setProperty("modificationsMade", modificationsMade);
	}

	public Text getModificationsMade() {
		return (Text) getCachedEntity().getProperty("modificationsMade");
	}

}
