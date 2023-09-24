package com.universeprojects.miniup.server.dbentities;

public abstract class DBEUtils
{
	public String enumToString(Enum value)
	{
		if (value==null) return null;
		
		StringBuilder sb = new StringBuilder();
		Object[] possibleValues = value.getDeclaringClass().getEnumConstants();
		
		for(Object v:possibleValues)
		{
			if (sb.length()>0)
				sb.append(",");
			sb.append(v.toString());
		}
		
		return sb.toString();
	}
}
