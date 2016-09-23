package com.universeprojects.miniup.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class OperationBase
{

	private List<Map<String, String>> htmlUpdates = new ArrayList<Map<String, String>>();
	
	
	/**
	 * When the command returns to the browser, the page will then find the elements that match
	 * the given jquerySelector and the element's contents will be filled with the given htmlContents.
	 * 
	 * If you just want to replace the selected elements with html, then use the 
	 * updateHtml() method instead.
	 * 
	 * @param jquerySelector
	 * @param htmlContents
	 */
	public void updateHtmlContents(String jquerySelector, String htmlContents)
	{
		Map<String,String> htmlData = new HashMap<String,String>();
		
		htmlData.put("type", "0");
		htmlData.put("selector", jquerySelector);
		htmlData.put("html", htmlContents);
		
		htmlUpdates.add(htmlData);
	}
	
	/**
	 * When the command returns to the browser, the page will then find the elements that match
	 * the given jquerySelector and the element itself will be replaced with the given newHtml.
	 * 
	 * If you just want to update the contents of the element (ie. adding html inside of a div) then 
	 * use the updateHtmlContents() method instead.
	 * 
	 * @param jquerySelector
	 * @param newHtml
	 */
	public void updateHtml(String jquerySelector, String newHtml)
	{
		Map<String,String> htmlData = new HashMap<String,String>();
		
		htmlData.put("type", "1");
		htmlData.put("selector", jquerySelector);
		htmlData.put("html", newHtml);
		
		htmlUpdates.add(htmlData);
	}
	
	public List<Map<String,String>> getHtmlUpdates()
	{
		return htmlUpdates;
	}
	
}
