package com.universeprojects.miniup.server.scripting.wrappers;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class Knowledge extends EntityWrapper{

	public Knowledge(CachedEntity entity, ODPDBAccess db) {
		super(entity, db);
		// TODO Auto-generated constructor stub
	}
	
	public Key getCharacterKey() {
		return (Key) wrappedEntity.getProperty("characterKey");
	}
	
	/**
	 * Returns true if the experience is now at its cap.
	 * @return
	 */
	public boolean adjustExperience(Double adjustment) {
		Double experience = getExperience() + adjustment;
		
		if(experience > getMaxExperience()) {
			wrappedEntity.setProperty("experience", getMaxExperience());
			return true;
		}
		else {
			wrappedEntity.setProperty("experience", experience);
			return false;
		}
				
	}
	
	public Double getExperience() {
		return (Double) wrappedEntity.getProperty("experience");
	}
		
	public Double getMaxExperience() {
		return (Double) wrappedEntity.getProperty("maxExperience");
	}

}
