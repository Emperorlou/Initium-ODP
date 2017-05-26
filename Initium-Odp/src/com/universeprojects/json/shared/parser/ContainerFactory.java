package com.universeprojects.json.shared.parser;

import java.util.List;
import java.util.Map;

/**
 * Container factory focom.universeprojects.json.shared.parserfor JSON object and JSON array.
 * 
 * @see com.universeprojects.json.shared.parser.JSONServerParser#parse(java.io.Reader, ContainerFactory)
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
@SuppressWarnings("rawtypes")
public interface ContainerFactory {
	/**
	 * @return A Map instance to store JSON object, or null if you want to use org.json.simple.JSONObject.
	 */
	Map createObjectContainer();
	
	/**
	 * @return A List instance to store JSON array, or null if you want to use org.json.simple.JSONArray. 
	 */
	List creatArrayContainer();
}
