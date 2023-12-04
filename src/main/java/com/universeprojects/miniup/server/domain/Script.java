package com.universeprojects.miniup.server.domain;

import com.google.appengine.api.datastore.Text;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * Javascript that can be written directly from the editor to perform various actions in-game without requiring a code deployment.
 * 
 * @author kyle-miller
 *
 */
public class Script extends OdpDomain {
	public static final String KIND = "Script";

	public Script() {
		super(new CachedEntity(KIND));
	}

	private Script(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	public static final Script wrap(CachedEntity cachedEntity) {
		return new Script(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 *  This text would get displayed to the user. It is the link name the user would click on to activate this script.
	 *  
	 * @param caption
	 */
	public void setCaption(String caption) {
		getCachedEntity().setProperty("caption", caption);
	}

	public String getCaption() {
		return (String) getCachedEntity().getProperty("caption");
	}

	/**
	 *  The description for this script that is displayed to the user. It should describe what this script does within the context of the game (using language that a player would be reading).
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
	 *  For internal use only to help categorize things.
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
	 *  All scripts need a unique name so we can call them by name (especially for global scripts).
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
	 *  The actual javascript for this script function.
	 *  
	 * @param script
	 */
	public void setScript(Text script) {
		getCachedEntity().setProperty("script", script);
	}

	public Text getScript() {
		return (Text) getCachedEntity().getProperty("script");
	}

	public enum Type {
		global, directItem, directLocation, onAttack, onAttackHit, onDefend, onDefendHit, onMoveBegin, onMoveEnd, onServerTick, onCombatTick,
	}

	/**
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
