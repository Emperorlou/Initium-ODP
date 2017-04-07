package com.universeprojects.miniup.server.services;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;

public class ConfirmSkillRequirementsBuilder extends ConfirmGenericEntityRequirementsBuilder
{
	
	public ConfirmSkillRequirementsBuilder(String uniqueId, ODPDBAccess db, OperationBase command, CachedEntity skill)
	{
		super(uniqueId, db, command, skill);
	}
	
	@Override
	protected String getPagePopupUrl()
	{
		return "/odp/confirmrequirements?constructItemSkillId="+entity.getId();
	}


	@Override
	protected String getPagePopupTitle()
	{
		return "Select the tools/materials to use";
	}


	@Override
	protected String getJavascriptRecallFunction()
	{
		return "doConstructItemSkill(null,"+entity.getId()+",'"+entity.getProperty("name").toString().replace("'", "\\'")+"');";
	}

}
