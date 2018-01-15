package com.universeprojects.miniup.server.longoperations;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.DBUtils;
import com.universeprojects.json.shared.JSONObject;
import com.universeprojects.miniup.server.Convert;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CaptchaService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;



public abstract class LongOperation extends OperationBase
{
	ODPDBAccess db;
	CachedDatastoreService ds;
	private Map<String,String> parameters;
	private String userMessage = null;
	private boolean fullRefresh = false;
	
	CachedEntity longOperationDataEntity = null;
	Map<String, Object> data = null; 
	
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
			longOperationDataEntity = getLongOperationDataEntity(db, db.getCurrentCharacterKey());
			if (longOperationDataEntity==null)
				longOperationDataEntity = new CachedEntity("LongOperation", getLongOperationDataEntityKey(db.getCurrentCharacterKey()));
			
			data = getLongOperationData(db, longOperationDataEntity);
			
			if (data!=null)
			{
				String currentPageRefreshJSCall = null;
				try
				{
					currentPageRefreshJSCall = getPageRefreshJavascriptCall();
				}
				catch(Exception e)
				{
					throw new InvalidLongOperationFieldValueException(e);
				}
				
				if (currentPageRefreshJSCall.equals(data.get("pageRefreshJavascriptCall"))==false)
					throw new UserErrorMessage("You are already performing an action and cannot perform another until the first action is either cancelled or finished.");
			}
		}
		catch(InvalidLongOperationFieldValueException e)
		{
			cancelLongOperations(db, db.getCurrentCharacterKey());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getLongOperationData(ODPDBAccess db, CachedEntity longOperationDataEntity)
	{
		if (longOperationDataEntity==null) return null;
		
		String packedLongOperation = (String)longOperationDataEntity.getProperty("data");
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
	
	@SuppressWarnings("unchecked")
	public static Map<String,Object> getLongOperationData(ODPDBAccess db, Key characterKey)
	{
		return getLongOperationData(db, getLongOperationDataEntity(db, characterKey));
	}
	
	public static boolean continueLongOperationIfActive(ODPDBAccess db, Key characterKey, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Map<String,Object> data = getLongOperationData(db, characterKey);
		
		if (data==null) return false;
		
		request.setAttribute("longOperationRecall", data.get("pageRefreshJavascriptCall"));	
		
		WebUtils.forceRedirectClientTo("main.jsp", request, response, "You can't do that right now because you're still performing an action.");
		return true;
	}
	
	public static String getLongOperationRecall(ODPDBAccess db, Key characterKey) throws ServletException, IOException
	{
		Map<String,Object> data = getLongOperationData(db, characterKey);
			
		if (data==null) return null;
		
		String js = (String)data.get("pageRefreshJavascriptCall");
		
		if (js==null)
			throw new InvalidLongOperationFieldValueException();
		
		return js;
	}
	
	public void setDataProperty(String fieldName, Object value)
	{
		if (data==null) data = new LinkedHashMap<String,Object>();
		data.put(fieldName, value);
	}
	
	public Object getDataProperty(String fieldName)
	{
		if (data==null) return null;
		return data.get(fieldName);
	}
	
	
	
	private void putLongOperationData()
	{
		if (data==null)
			longOperationDataEntity.setProperty("data", null);
		else
			longOperationDataEntity.setProperty("data", DBUtils.serializeObjectToString(data));
		
		Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Putting long operation: "+longOperationDataEntity.getKey());
		db.getDB().put(longOperationDataEntity);
	}
	
	public void cancelLongOperation(ODPDBAccess db, Key characterKey)
	{
		if (longOperationDataEntity==null) return;
		Logger.getLogger(this.getClass().getSimpleName()).log(Level.WARNING, "Cancelling long operation: "+longOperationDataEntity.getKey());
		longOperationDataEntity.setProperty("data", null);
		
		setDataProperty("cancelled", true);

		if (db.getDB().isBulkWriteModeOn())
			db.getDB().cancelBulkWrite();

		db.getDB().delete(longOperationDataEntity);
		longOperationDataEntity = null;
	}
	
	public static void cancelLongOperations(ODPDBAccess db, Key characterKey)
	{
		CachedEntity dataEntity = getLongOperationDataEntity(db, characterKey);
		if (dataEntity==null) return;
		dataEntity.setProperty("data", null);
		if (db.getDB().isBulkWriteModeOn())
			db.getDB().cancelBulkWrite();
		
		db.getDB().delete(dataEntity);
	}
	
	public boolean isComplete()
	{
		if (data==null)
			return false;
		Date endTime = (Date)data.get("endTime");
		if (endTime==null) return false;
		
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
		
		if (data==null)
			data = new LinkedHashMap<String,Object>();
		
		data.put("pageRefreshJavascriptCall", getPageRefreshJavascriptCall());
		
		Calendar endTime = new GregorianCalendar();
		endTime.add(Calendar.SECOND, operationSeconds);
		data.put("endTime", endTime.getTime());

	}
	
	
	public String complete() throws UserErrorMessage, UserRequestIncompleteException, ContinuationException
	{
		setDataProperty("finished", true);
		try
		{
			return doComplete();
		}
		catch(UserErrorMessage | UserRequestIncompleteException | ContinuationException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			cancelLongOperation(db, db.getCurrentCharacterKey());
			throw e;
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
	abstract String doComplete() throws UserErrorMessage, UserRequestIncompleteException, ContinuationException;
	
	
	public Map<String, Object> getStateData()
	{
		Map<String,Object> result = new HashMap<String,Object>();
		
		Date endTime = (Date)getDataProperty("endTime");
		
		long timeLeft = 0;
		if (endTime!=null)
			timeLeft = GameUtils.elapsed(Convert.DateToCalendar(endTime), new GregorianCalendar(), Calendar.SECOND);
		
		result.put("timeLeft", timeLeft);
		result.put("isComplete", isComplete());
		result.put("message", userMessage);
		result.put("responseHtml", getHtmlUpdates());
		result.put("_2dViewportUpdates", getMapUpdateJSON());
		result.put("isShowingTimeLeft", isShowingTimeLeft());
		result.put("hasNewGameMessages", db.hasNewGameMessages());
		
		
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
				db.sendGameMessage(db.getDB(), db.getCurrentCharacter(), complete());
				
				result.putAll(getStateData());
				data = null;
			}
			else
			{
				if (isNotStarted())
				{
					begin();
					result.putAll(getStateData());
					if (isComplete())
					{
						db.sendGameMessage(db.getDB(), db.getCurrentCharacter(), complete());
						data = null;
					}
				}
				else
					result.putAll(getStateData());
				
				request.setAttribute("longOperationRecall", getPageRefreshJavascriptCall());
				
			}
		} catch (UserErrorMessage e) {
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			result = new JSONObject();
			result.putAll(getStateData());
			result.put("hasNewGameMessages", db.hasNewGameMessages());
			result.put("error", e.getMessage());
			result.put("cancelled", true);
			out.print(result.toJSONString());
			out.flush();
			out.close();
			cancelLongOperations(db, db.getCurrentCharacterKey());
			return;
		}
		catch(ContinuationException e)
		{
			if (e.reason!=null)
				db.sendGameMessage(db.getDB(), db.getCurrentCharacter(), e.getMessage());
				
			Calendar endTime = new GregorianCalendar();
			endTime.add(Calendar.SECOND, e.seconds);
			setDataProperty("endTime", endTime.getTime());
			
			
			result.put("continuation", e.seconds);
			result.putAll(getStateData());
//			response.setContentType("application/json");
//			PrintWriter out = response.getWriter();
//			result = new JSONObject();
//			out.print(result.toJSONString());
//			out.flush();
//			out.close();
		}
		catch(UserRequestIncompleteException e)
		{
			sendUserRequest(response, e);
			return;
		}
		catch (GameStateChangeException e)
		{
			db.sendGameMessage(db.getDB(), db.getCurrentCharacter(), e.getMessage());
//			sendErrorMessageAndFullRefresh(response, null);
			cancelLongOperation(db, db.getCurrentCharacterKey());
			
			MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), db.getCurrentCharacter(), db.getEntity((Key)db.getCurrentCharacter().getProperty("locationKey")), this);
			mpus.updateFullPage_shortcut();

			result.putAll(getStateData());
			result.put("silentError", true);
			
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			out.print(result.toJSONString());
			out.flush();
			out.close();
			return;
		}
		
		putLongOperationData();
		
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


	public static void sendCancelled(HttpServletResponse response, String message) throws IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JSONObject result = new JSONObject();
		result.put("message", message);
		result.put("cancelled", true);
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
		if (data==null)
			return true;
		else
			return false;
	}

	public static String getLongOperationDataEntityKey(Key characterKey)
	{
		return characterKey.toString();
	}
	
	public static CachedEntity getLongOperationDataEntity(ODPDBAccess db, Key characterKey)
	{
		return db.getEntity("LongOperation", getLongOperationDataEntityKey(characterKey));
	}
}

