package com.universeprojects.miniup.server.services;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;

public class ExperimentalPageUpdateService extends MainPageUpdateService
{

	public ExperimentalPageUpdateService(ODPDBAccess db, CachedEntity user, CachedEntity character, CachedEntity location, OperationBase operation)
	{
		super(db, user, character, location, operation);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub

	}
	
	@Override
	protected String updateButtonList_CombatMode()
	{
		return "";
	}

	
}
