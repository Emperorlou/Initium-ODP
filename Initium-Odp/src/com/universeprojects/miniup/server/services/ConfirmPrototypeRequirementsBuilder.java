package com.universeprojects.miniup.server.services;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.longoperations.LongOperation;

public class ConfirmPrototypeRequirementsBuilder extends ConfirmGenericEntityRequirementsBuilder
{
	final private CachedEntity idea;
	
	public ConfirmPrototypeRequirementsBuilder(String uniqueId, ODPDBAccess db, OperationBase command, CachedEntity ideaDef, CachedEntity idea)
	{
		super(uniqueId, db, command, "doCreatePrototype(null, "+idea.getId()+", "+WebUtils.jsSafe((String)idea.getProperty("name"))+", '"+uniqueId+"', "+((LongOperation)command).getDataProperty("repsUniqueId")+")", ideaDef);
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
