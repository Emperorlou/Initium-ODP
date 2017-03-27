package com.universeprojects.miniup.server.longoperations;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
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
	Map<String, Object> data = null;
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

		data = fetchLongOperationFromMC();
		
		if (data!=null && getPageRefreshJavascriptCall().equals(data.get("pageRefreshJavascriptCall"))==false)
			throw new UserErrorMessage("You are already performing an action and cannot perform another until the first action is either cancelled or finished.");
	}
	
	public static boolean continueLongOperationIfActive(ODPDBAccess db, Key characterKey, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Map<String,Object> data = (Map<String,Object>)db.getDB().getMC().get("LongOperation-"+characterKey);
		
		if (data==null) return false;
		
		request.setAttribute("longOperationRecall", data.get("pageRefreshJavascriptCall"));	
		
		WebUtils.forceRedirectClientTo("main.jsp", request, response, "You can't do that right now because you're still performing an action.");
		return true;
	}
	
	public static String getLongOperationRecall(ODPDBAccess db, Key characterKey) throws ServletException, IOException
	{
		Map<String,Object> data = (Map<String,Object>)db.getDB().getMC().get("LongOperation-"+characterKey);
		
		if (data==null) return null;
		
		String js = (String)data.get("pageRefreshJavascriptCall");
		
		return js;
	}
	
	private Map<String, Object> fetchLongOperationFromMC()
	{
		return (Map<String, Object>)ds.getMC().get("LongOperation-"+db.getCurrentCharacter().getKey());
	}
	
	private void putLongOperationToMC(Map<String, Object> data)
	{
		ds.getMC().put("LongOperation-"+db.getCurrentCharacter().getKey(), data);
	}
	
	public static void cancelLongOperations(ODPDBAccess db, Key characterKey)
	{
		db.getDB().getMC().put("LongOperation-"+characterKey, null);		
	}
	
	public boolean isComplete()
	{
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
		if (data==null)
			data = new HashMap<String, Object>();
		
		int operationSeconds = 0;
		operationSeconds = doBegin(parameters);
		data.put("pageRefreshJavascriptCall", getPageRefreshJavascriptCall());

		
		Calendar endTime = new GregorianCalendar();
		endTime.add(Calendar.SECOND, operationSeconds);
		
		data.put("endTime", endTime.getTime());
		
		putLongOperationToMC(data);
	}
	
	public String complete() throws UserErrorMessage, UserRequestIncompleteException
	{
		try
		{
			return doComplete();
		}
		finally
		{
			putLongOperationToMC(null);
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
		
		Date endTime = (Date)data.get("endTime");
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
			cancelLongOperations(db, db.getCurrentCharacter().getKey());
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
			cancelLongOperations(db, db.getCurrentCharacter().getKey());
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
		result.put("userRequestId", e.userRequestId);
		out.print(result.toJSONString());
		out.flush();
		out.close();
	}

	private boolean isNotStarted() {
		if (data==null)
			return true;
		else
			return false;
	}
	
}

