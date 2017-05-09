package com.universeprojects.miniup.server.services;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;

public class ConfirmPrototypeRequirementsBuilder extends ConfirmGenericEntityRequirementsBuilder
{
	final private CachedEntity idea;
	
	public ConfirmPrototypeRequirementsBuilder(String uniqueId, ODPDBAccess db, OperationBase command, CachedEntity ideaDef, CachedEntity idea)
	{
		super(uniqueId, db, command, "doCreatePrototype(null, "+idea.getId()+", "+idea.getProperty("name")+", '"+uniqueId+"')", ideaDef);
		this.idea = idea;
	}
	
	@Override
	protected String getPagePopupUrl()
	{
		return "/odp/confirmrequirements?ideaId="+idea.getId()+getRepetitionsUrlParam();
	}


	@Override
	protected String getPagePopupTitle()
	{
		return "Select the tools/materials to use";
	}

}
