package com.universeprojects.miniup.server;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.json.shared.JSONArray;
import com.universeprojects.json.shared.JSONObject;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.commands.CommandSwitchCharacter;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.dbentities.QuestDefEntity;
import com.universeprojects.miniup.server.dbentities.QuestEntity;
import com.universeprojects.miniup.server.dbentities.QuestObjective;
import com.universeprojects.miniup.server.dbentities.QuestObjective.TutorialStep;
import com.universeprojects.miniup.server.model.GridCell;
import com.universeprojects.miniup.server.model.GridObject;
import com.universeprojects.miniup.server.services.ExperimentalPageUpdateService;
import com.universeprojects.miniup.server.services.FullPageUpdateService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;
import com.universeprojects.miniup.server.services.QuestService;

public abstract class OperationBase
{
	final protected ODPDBAccess db;
	protected HttpServletRequest request;
	
	public OperationBase(HttpServletRequest request, ODPDBAccess db)
	{
		this.db = db;
		this.request = request;
		
		if(CommonChecks.checkCharacterIsZombie(db.getCurrentCharacter()))
			if(!(this instanceof CommandSwitchCharacter))
				throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
	}
	
	/**
	 * Method stub. Returns back an ODPAuthenticator object to use in various ODPDBAccess methods.
	 * @return
	 */
	protected ODPAuthenticator getAuthenticator(){
		return null;
	}

	private Map<String, String> jsCode_Replaceable = null;
	private List<Map<String, String>> htmlUpdates = new ArrayList<Map<String, String>>();
	private List<JSONObject> gridCellUpdates = new ArrayList<>();
	private List<JSONObject> gridObjectUpdates = new ArrayList<>();
	
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
		addHtmlUpdate("0", jquerySelector, htmlContents);
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
		addHtmlUpdate("1", jquerySelector, newHtml);
	}
	
	/**
	 * When the command returns to the browser, the page will then find the first element 
	 * that matches the given jquerySelector and will insert the given htmlContents before.
	 * 
	 * @param jquerySelector
	 * @param htmlContents
	 */
	public void insertHtmlBefore(String jquerySelector, String htmlContents)
	{
		addHtmlUpdate("2", jquerySelector, htmlContents);
	}
	
	/**
	 * When the command returns to the browser, the page will then find the last element 
	 * that matches the given jquerySelector and will insert the given htmlContents after.
	 * 
	 * @param jquerySelector
	 * @param htmlContents
	 */
	public void insertHtmlAfter(String jquerySelector, String htmlContents)
	{
		addHtmlUpdate("3", jquerySelector, htmlContents);
	}
	
	/**
	 * When the command returns to the browser, the script that has the given elementId 
	 * will be deleted and the then a new script tag will be added to the page with the given javascript.
	 * The new javascript <script> tag will also have the same id as the one deleted.
	 * 
	 *
	 * @param jquerySelector
	 * @param newHtml
	 */
	public void updateJavascript(String elementId, String newJavascript)
	{
		Map<String,String> htmlData = new HashMap<String,String>();
		
		htmlData.put("type", "4");
		htmlData.put("id", elementId);
		htmlData.put("js", newJavascript);
		
		htmlUpdates.add(htmlData);
	}
	
	
	/**
	 * When the command returns to the browser, the element that has the given elementId 
	 * will be deleted.
	 *
	 * @param jquerySelector
	 * @param newHtml
	 */
	public void deleteHtml(String jquerySelector)
	{
		addHtmlUpdate("5", jquerySelector, null);
	}
	
	/**
	 * When the command returns to the browser, the elements matching the selector will
	 * have the htmlContents inserted as the first child element.
	 * @param jquerySelector jQuery matching a set of elements
	 * @param htmlContents New HTML contents to prepend.
	 */
	public void prependChildHtml(String jquerySelector, String htmlContents)
	{
		addHtmlUpdate("6", jquerySelector, htmlContents);
	}
	
	/**
	 * When the command returns to the browser, the elements matching the selector will
	 * have the htmlContents inserted as the last child element.
	 * @param jquerySelector jQuery matching a set of elements
	 * @param htmlContents New HTML contents to append.
	 */
	public void appendChildHtml(String jquerySelector, String htmlContents)
	{
		addHtmlUpdate("7", jquerySelector, htmlContents);
	}
	
	public void addJavascriptToResponse_Replaceable(String javascriptId, String javascript)
	{
		if (jsCode_Replaceable==null)
			jsCode_Replaceable = new LinkedHashMap<>();
		
		jsCode_Replaceable.put(javascriptId, javascript);
	}
	
	public void addJavascriptToResponse(String javascript)
	{
		addHtmlUpdate("8", null, javascript);
	}
	
	private void addHtmlUpdate(String updateType, String jquerySelector, String htmlContents)
	{
		Map<String,String> htmlData = new HashMap<String,String>();
		
		htmlData.put("type", updateType);
		htmlData.put("selector", jquerySelector);
		htmlData.put("html", htmlContents);
		
//		// Check if this thing has been updated already and replace it if it has
//		if (GameUtils.equals("0", updateType) && htmlUpdates!=null)
//			for(Map<String,String> existingData:htmlUpdates)
//			{
//				if (GameUtils.equals(jquerySelector, existingData.get("selector")))
//				{
//					existingData.put("html", htmlContents);
//					return;
//				}
//			}
		
		htmlUpdates.add(htmlData);
	}
	
	public List<Map<String,String>> getHtmlUpdates()
	{
		if (jsCode_Replaceable!=null)
		{
			for(String key:jsCode_Replaceable.keySet())
			{
				addJavascriptToResponse(jsCode_Replaceable.get(key));
			}
		}
		return htmlUpdates;
	}

	public List<JSONObject> getGridCellUpdates() {
		return gridCellUpdates;
	}

	public void setGridCellUpdates(List<JSONObject> gridCellUpdates) {
		this.gridCellUpdates = gridCellUpdates;
	}

	public List<JSONObject> getGridObjectUpdates() {
		return gridObjectUpdates;
	}

	public void setGridObjectUpdates(List<JSONObject> gridObjectUpdates) {
		this.gridObjectUpdates = gridObjectUpdates;
	}
	
	public void addGridCellUpdate(GridCell gridCell) {
		this.gridCellUpdates.add(gridCell.getJsonObject());
	}
	
	public void addGridObjectUpdate(GridObject gridObject) {
		this.gridObjectUpdates.add(gridObject.getJsonObject());
	}
	
	public JSONObject getMapUpdateJSON() {
		JSONObject finalJson = new JSONObject();
		JSONArray gridCellJson = new JSONArray();
		JSONArray gridObjectJson = new JSONArray();
		gridCellJson.addAll(this.gridCellUpdates);
		gridObjectJson.addAll(this.gridObjectUpdates);
		finalJson.put("GridCells", gridCellJson);
		finalJson.put("GridObject", gridObjectJson);
		return finalJson;
	}
	
	/**
	 * Allows merging updates from one OperationBase to another.
	 * Primarily used in Scripting, where updates performed in Script
	 * context don't necessarily get updated on the command level.
	 * Does a simple merge, not dealing with duplicates.
	 * @param toMerge OperationBase to merge with current instance. 
	 */
	public void mergeOperationUpdates(OperationBase toMerge)
	{
		htmlUpdates.addAll(toMerge.htmlUpdates);
		gridCellUpdates.addAll(toMerge.gridCellUpdates);
		gridObjectUpdates.addAll(toMerge.gridObjectUpdates);
	}
	
	public abstract Long getSelectedTileX();
	public abstract Long getSelectedTileY();

	
	
	
	
	
	
	
	
	
	/*///////////////////////////////////
	 * Quest related stuff	
	 */

	
	
	private QuestService questService = null;
	
	protected QuestService getQuestService()
	{
		if (questService==null)
			questService = db.getQuestService(this);
		
		return questService;
	}
	
	boolean questCompleteFlagged = false;
	public void flagQuestComplete()
	{
		questCompleteFlagged = true;
		addJavascriptToResponse("doQuestCompleteBannerEffect();");
	}
	
	public void flagObjectiveComplete()
	{
		if (questCompleteFlagged) return;
		
		addJavascriptToResponse("doObjectiveCompleteBannerEffect();");
	}
	
	public void addUITutorialsForObjective(QuestObjective o)
	{
		addJavascriptToResponse_Replaceable("uitutorial", o.generateTutorialStepsJs());
	}
	
	private MainPageUpdateService mpus = null;
	public MainPageUpdateService getMPUS()
	{
		if (mpus!=null)
		{
			if (GameUtils.equals(db.getCharacterLocationKey(db.getCurrentCharacter()), mpus.getLocation().getKey()))
				return mpus;
		}

		mpus = OperationBase.createMPUS(db, this);
		
		return mpus;
	}
	
	public static MainPageUpdateService createMPUS(ODPDBAccess db, OperationBase command)
	{
		MainPageUpdateService mpus;
		String uiStyle = db.getRequest().getParameter("uiStyle");
		
		if (uiStyle==null)
		{
			String mainPageUrl = db.getRequest().getParameter("mainPageUrl");
			if (mainPageUrl==null) mainPageUrl = db.getRequest().getServletPath();
			if (mainPageUrl.contains("/odp/full"))
			{
				uiStyle = "wow";
			}
			else if (mainPageUrl.contains("/main.jsp"))
			{
				uiStyle = "classic";
			}
			else
			{
				uiStyle = "experimental";
			}
			
		}
		
		if (uiStyle.equals("classic"))
		{
			mpus = new MainPageUpdateService(db, db.getCurrentUser(), db.getCurrentCharacter(), db.getCharacterLocation(db.getCurrentCharacter()), command);
		}
		else if (uiStyle.equals("experimental"))
		{
			mpus = new ExperimentalPageUpdateService(db, db.getCurrentUser(), db.getCurrentCharacter(), db.getCharacterLocation(db.getCurrentCharacter()), command);
		}
		else if (uiStyle.equals("wow"))
		{
			mpus = new FullPageUpdateService(db, db.getCurrentUser(), db.getCurrentCharacter(), db.getCharacterLocation(db.getCurrentCharacter()), command);
		}
		else
			throw new RuntimeException("Unhandled ui style: "+uiStyle);

		return mpus;
	}

	public void flagNoobQuestLineComplete(String questLine)
	{
		addJavascriptToResponse("questLineCompletePopup('"+questLine+"');");
	}
	
	
	
}
