package com.universeprojects.miniup.server.longoperations;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.DBUtils;
import com.universeprojects.miniup.server.Convert;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CaptchaService;



public abstract class LongOperation extends OperationBase
{
	ODPDBAccess db;
	CachedDatastoreService ds;
	private Map<String,String> parameters;
	private String userMessage = null;
	private boolean fullRefresh = false;
	
	public LongOperation(ODPDBAccess db, Map<String, String[]> requestParameters) throws UserErrorMessage
	{
		if (db==null)
			throw new IllegalArgumentException("GameFunctions cannot be null.");
		if (db.getCurrentCharacter()==null)
			throw new SecurityException("Not logged in.");
		
		Map<String, String> params = new HashMap<String, String>();
		if (requestParameters!=null)
			for(String key:requestParameters.keySet())
			{
				String[] values = requestParameters.get(key);
				if (values!=null && values.length>0)
					params.put(key, requestParameters.get(key)[0]);
			}

		
		this.ds = db.getDB();
		this.parameters = params;
		this.db = db;

		try
		{
			Map<String, Object> data = getLongOperationData(db.getCurrentCharacter());
			
			if (data!=null && getPageRefreshJavascriptCall().equals(data.get("pageRefreshJavascriptCall"))==false)
				throw new UserErrorMessage("You are already performing an action and cannot perform another until the first action is either cancelled or finished.");
		}
		catch(InvalidLongOperationFieldValueException e)
		{
			db.getCurrentCharacter().setProperty("longOperation", null);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getLongOperationData(CachedEntity character)
	{
		String packedLongOperation = (String)character.getProperty("longOperation");
		if (packedLongOperation==null || packedLongOperation.equals("")) return null;
		
		try
		{
			return (Map<String,Object>)DBUtils.deserializeObjectFromString(packedLongOperation);
		}
		catch (Exception e)
		{
			throw new InvalidLongOperationFieldValueException(e);
		}
	}
	
	public static boolean continueLongOperationIfActive(ODPDBAccess db, CachedEntity character, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Map<String,Object> data = getLongOperationData(character);
		
		if (data==null) return false;
		
		request.setAttribute("longOperationRecall", data.get("pageRefreshJavascriptCall"));	
		
		WebUtils.forceRedirectClientTo("main.jsp", request, response, "You can't do that right now because you're still performing an action.");
		return true;
	}
	
	public static String getLongOperationRecall(ODPDBAccess db, CachedEntity character) throws ServletException, IOException
	{
		Map<String,Object> data = getLongOperationData(character);
		
		if (data==null) return null;
		
		String js = (String)data.get("pageRefreshJavascriptCall");
		
		if (js==null)
			throw new InvalidLongOperationFieldValueException();
		
		return js;
	}
	
	public void setDataProperty(String fieldName, Object value)
	{
		setDataProperty(db.getCurrentCharacter(), fieldName, value);
	}
	
	public void setDataProperty(CachedEntity character, String fieldName, Object value)
	{
		Map<String,Object> data = getLongOperationData(character);
		if (data==null) data = new LinkedHashMap<String,Object>();
		data.put(fieldName, value);
		setLongOperationData(character, data);
	}
	
	public Object getDataProperty(String fieldName)
	{
		return getDataProperty(db.getCurrentCharacter(), fieldName);
	}
	
	public Object getDataProperty(CachedEntity character, String fieldName)
	{
		Map<String,Object> data = getLongOperationData(character);
		if (data==null) return null;
		return data.get(fieldName);
	}
	
	
	private void setLongOperationData(CachedEntity character, Map<String,Object> data)
	{
		character.setProperty("longOperation", DBUtils.serializeObjectToString(data));
	}
	
	public static void cancelLongOperations(ODPDBAccess db, CachedEntity character)
	{
		character.setProperty("longOperation", null);
		
		db.getDB().put(character);
	}
	
	public boolean isComplete()
	{
		Map<String, Object> data = getLongOperationData(db.getCurrentCharacter());
		if (data==null)
			return false;
		Date endTime = (Date)data.get("endTime");
		
		long endMillisecond = endTime.getTime();
		
		if (endMillisecond<System.currentTimeMillis())
			return true;
		
		return false;
	}
	
	/**
	 * Returns true if the user is to see how much time is left on the long operation.
	 * 
	 * @return
	 */
	public boolean isShowingTimeLeft()
	{
		return true;
	}
	
	
	/**
	 * This causes the long operation to start.
	 */
	public void begin() throws UserErrorMessage, UserRequestIncompleteException
	{
		int operationSeconds = 0;
		operationSeconds = doBegin(parameters);
		Map<String, Object> data = getLongOperationData(db.getCurrentCharacter());
		if (data==null)
			data = new LinkedHashMap<String,Object>();
		// Here we are going to save the longOperation data that was prepared in the doBegin() call
		// then grab from the database to make sure we have the latest character data, then REput the
		// longOperation data into the character now that we know it's the latest, then save it
		CachedEntity character = db.getEntity(db.getCurrentCharacterKey());
		setLongOperationData(character, data);
		
		data.put("pageRefreshJavascriptCall", getPageRefreshJavascriptCall());
		
		Calendar endTime = new GregorianCalendar();
		endTime.add(Calendar.SECOND, operationSeconds);
		data.put("endTime", endTime.getTime());
		
		setLongOperationData(character, data);
		setLongOperationData(db.getCurrentCharacter(), data);
		
		db.getDB().put(character);
	}
	
	public String complete() throws UserErrorMessage, UserRequestIncompleteException
	{
		try
		{
			return doComplete();
		}
		finally
		{
			// Here we are going to grab from the database to make sure we have the latest character data, then put the
			// null longOperation data into the character now that we know it's the latest, then save it
			CachedEntity character = db.getEntity(db.getCurrentCharacterKey());
			setLongOperationData(character, null);
			db.getDB().put(character);
		}
		
	}
	
	public void setUserMessage(String message)
	{
		this.userMessage = message;
	}
	
	/**
	 * If true, when the request is returned, the page will refresh.
	 * 
	 * @param value
	 */
	public void setFullRefresh(boolean value)
	{
		this.fullRefresh = value;
	}
	
	
	abstract int doBegin(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException;
	
	/**
	 * 
	 * @return Text output that the player will see after completing this operation.
	 * @throws UserErrorMessage
	 */
	abstract String doComplete() throws UserErrorMessage, UserRequestIncompleteException;
	
	
	public Map<String, Object> getStateData()
	{
		Map<String,Object> result = new HashMap<String,Object>();
		
		Date endTime = (Date)getDataProperty(db.getCurrentCharacter(), "endTime");
		long timeLeft = GameUtils.elapsed(Convert.DateToCalendar(endTime), new GregorianCalendar(), Calendar.SECOND);
		
		result.put("timeLeft", timeLeft);
		result.put("isComplete", isComplete());
		result.put("message", userMessage);
		result.put("responseHtml", getHtmlUpdates());
		result.put("_2dViewportUpdates", getMapUpdateJSON());
		result.put("isShowingTimeLeft", isShowingTimeLeft());
		
		
		result.put("refresh", fullRefresh);
		
		
		return result;
	}
	
	public abstract String getPageRefreshJavascriptCall();
	
	public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, GameStateChangeException
	{
		CaptchaService captcha = new CaptchaService(db);
		if (captcha.isBotCheckTime())
			throw new GameStateChangeException();
		
		JSONObject result = new JSONObject();
		try 
		{
			if (isComplete())
			{
				db.addToClientDescription(ds, db.getCurrentCharacter().getKey(), complete());
				
				result.putAll(getStateData());
			}
			else
			{
				if (isNotStarted())
				{
					begin();
					if (isComplete())
						db.addToClientDescription(ds, db.getCurrentCharacter().getKey(), complete());
				}
				request.setAttribute("longOperationRecall", getPageRefreshJavascriptCall());
				
				result.putAll(getStateData());
			}
		} catch (UserErrorMessage e) {
			sendErrorMessage(response, e.getMessage());
			cancelLongOperations(db, db.getCurrentCharacter());
			return;
		}
		catch(UserRequestIncompleteException e)
		{
			sendUserRequest(response, e);
			return;
		}
		catch (GameStateChangeException e)
		{
			db.addToClientDescription(ds, db.getCurrentCharacter().getKey(), e.getMessage());
			sendErrorMessageAndFullRefresh(response, null);
			cancelLongOperations(db, db.getCurrentCharacter());
			return;
		}
		
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print(result.toJSONString());
		out.flush();
		out.close();
	}

	public static void sendCaptchaRequired(HttpServletResponse response) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JSONObject result = new JSONObject();
		result.put("captcha", true);
		out.print(result.toJSONString());
		out.flush();
		out.close();
	}

	public static void sendErrorMessage(HttpServletResponse response, String message) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JSONObject result = new JSONObject();
		result.put("error", message);
		out.print(result.toJSONString());
		out.flush();
		out.close();
	}

	public static void sendErrorMessageAndFullRefresh(HttpServletResponse response, String message) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JSONObject result = new JSONObject();
		if (message!=null)
			result.put("error", message);
		result.put("refresh", true);
		out.print(result.toJSONString());
		out.flush();
		out.close();
	}

	public static void sendUserRequest(HttpServletResponse response, UserRequestIncompleteException e) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JSONObject result = new JSONObject();
		result.put("message", e.playerMessage);
		result.put("pagePopupUrl", e.pagePopupUrl);
		result.put("pagePopupTitle", e.pagePopupTitle);
		result.put("urlParameters", e.urlParameters);
		result.put("userRequestId", e.userRequestId);
		out.print(result.toJSONString());
		out.flush();
		out.close();
	}

	private boolean isNotStarted() {
		Map<String, Object> data = getLongOperationData(db.getCurrentCharacter());
		if (data==null)
			return true;
		else
			return false;
	}
	
}

