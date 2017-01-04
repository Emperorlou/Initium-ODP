package com.universeprojects.miniup.server;

import javax.servlet.http.HttpServletRequest;

public class ODPDBAccessFactory
{
	public ODPDBAccess getInstance(HttpServletRequest request)
	{
		return new ODPDBAccess(request);
	}
}
