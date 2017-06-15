package com.universeprojects.miniup.server.services;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.longoperations.LongOperation;

public class ConfirmSkillRequirementsBuilder extends ConfirmGenericEntityRequirementsBuilder
{
	final private CachedEntity skill;
	
	public ConfirmSkillRequirementsBuilder(String uniqueId, ODPDBAccess db, OperationBase command, CachedEntity ideaDef, CachedEntity skill)
	{
		super(uniqueId, db, command, "doConstructItemSkill(null, "+skill.getId()+", "+skill.getProperty("name")+", '"+uniqueId+"', "+((LongOperation)command).getDataProperty("repsUniqueId")+")", ideaDef);
		this.skill = skill;
	}
	
	protected String getPagePopupUrl()
	{
		return "/odp/confirmrequirements?constructItemSkillId="+skill.getId()+getRepetitionsUrlParam();
	}


	@Override
	protected String getPagePopupTitle()
	{
		return "Select the tools/materials to use";
	}

}
