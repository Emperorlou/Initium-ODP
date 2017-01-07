package com.universeprojects.miniup.server.domain;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * Defines an instance of a buff that is currently active on a character (or on some other entity).
 * 
 * @author kyle-miller
 *
 */
public class Buff extends OdpDomain {
	public static final String KIND = "Buff";

	public Buff() {
		super(new CachedEntity(KIND));
	}

	public Buff(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 * The description the player sees when they click on the buff.
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
	 * The date this buff will be removed.
	 * 
	 * @param expiry
	 */
	public void setExpiry(Date expiry) {
		getCachedEntity().setProperty("expiry", expiry);
	}

	public Date getExpiry() {
		return (Date) getCachedEntity().getProperty("expiry");
	}

	/**
	 * The effect to apply to the field in question. You can add/subtract using +10 or -0.05, or you can do +0.1% and -15%.
	 * 
	 * @param field1Effect
	 */
	public void setField1Effect(String field1Effect) {
		getCachedEntity().setProperty("field1Effect", field1Effect);
	}

	public String getField1Effect() {
		return (String) getCachedEntity().getProperty("field1Effect");
	}

	/**
	 * The name of the field that we will affect with this buff.
	 * 
	 * @param field1Name
	 */
	public void setField1Name(String field1Name) {
		getCachedEntity().setProperty("field1Name", field1Name);
	}

	public String getField1Name() {
		return (String) getCachedEntity().getProperty("field1Name");
	}

	/**
	 * The effect to apply to the field in question. You can add/subtract using +10 or -0.05, or you can do +0.1% and -15%.
	 * 
	 * @param field2Effect
	 */
	public void setField2Effect(String field2Effect) {
		getCachedEntity().setProperty("field2Effect", field2Effect);
	}

	public String getField2Effect() {
		return (String) getCachedEntity().getProperty("field2Effect");
	}

	/**
	 * The name of the field that we will affect with this buff.
	 * 
	 * @param field2Name
	 */
	public void setField2Name(String field2Name) {
		getCachedEntity().setProperty("field2Name", field2Name);
	}

	public String getField2Name() {
		return (String) getCachedEntity().getProperty("field2Name");
	}

	/**
	 * The effect to apply to the field in question. You can add/subtract using +10 or -0.05, or you can do +0.1% and -15%.
	 * 
	 * @param field3Effect
	 */
	public void setField3Effect(String field3Effect) {
		getCachedEntity().setProperty("field3Effect", field3Effect);
	}

	public String getField3Effect() {
		return (String) getCachedEntity().getProperty("field3Effect");
	}

	/**
	 * The name of the field that we will affect with this buff.
	 * 
	 * @param field3Name
	 */
	public void setField3Name(String field3Name) {
		getCachedEntity().setProperty("field3Name", field3Name);
	}

	public String getField3Name() {
		return (String) getCachedEntity().getProperty("field3Name");
	}

	/**
	 * The icon for the buff. It shows up in the UI.
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
	 * This is an optional name you can give the item that is for developer use only (it will not be shown to the players).
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
	 * The name of the buff (visible to the player).
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
	 * The entity this buff is attached to. It could be an item or a character or anything really, as long as the game is ready to take buffs into account for a given entity.
	 * 
	 * @param parentKey
	 */
	public void setParentKey(Key parentKey) {
		getCachedEntity().setProperty("parentKey", parentKey);
	}

	public Key getParentKey() {
		return (Key) getCachedEntity().getProperty("parentKey");
	}

	/**
	 * (Script)
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
	 * Optionally used to state the specifics required to activate this Buff.
	 * 
	 * @param trigger
	 */
	public void setTrigger(String trigger) {
		getCachedEntity().setProperty("trigger", trigger);
	}

	public String getTrigger() {
		return (String) getCachedEntity().getProperty("trigger");
	}

	public enum BuffType {
		Character, Item,
	}

	/**
	 * 
	 * @param buffType
	 */
	public void setBuffType(BuffType buffType) {
		getCachedEntity().setProperty("buffType", buffType);
	}

	public BuffType getBuffType() {
		return (BuffType) getCachedEntity().getProperty("buffType");
	}

}
