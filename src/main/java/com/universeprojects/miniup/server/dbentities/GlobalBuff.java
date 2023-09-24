package com.universeprojects.miniup.server.dbentities;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class GlobalBuff extends InitiumEntityBase {

	Long cost;
	String description;
	String displayName;
	Long duration;
	
	
	public GlobalBuff(ODPDBAccess db, CachedEntity entity) {
		super(db, entity);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getKind() {
		// TODO Auto-generated method stub
		return "GlobalBuff";
	}

}
