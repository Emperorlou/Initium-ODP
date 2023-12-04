package com.universeprojects.miniup.server.services;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.miniup.server.InitiumKey;

public class GenericEntityRequirementResult implements Serializable
{
	private static final long serialVersionUID = 5635978106621359645L;
	public Map<String,List<InitiumKey>> slots = new HashMap<>();
	public Integer repetitionCount = null; 
}