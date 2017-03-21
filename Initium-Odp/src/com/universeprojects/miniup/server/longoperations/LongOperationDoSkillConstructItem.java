package com.universeprojects.miniup.server.longoperations;

import java.util.Map;

import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class LongOperationDoSkillConstructItem extends LongOperation
{

	public LongOperationDoSkillConstructItem(ODPDBAccess db, Map<String, String[]> requestParameters) throws UserErrorMessage
	{
		super(db, requestParameters);
	}

	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	String doComplete() throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPageRefreshJavascriptCall()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
