package com.universeprojects.miniup.server.commands.framework;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.ODPGameFunctions;

@SuppressWarnings("serial")
public class ServletCommand extends HttpServlet 
{
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException 
	{
		String cmd = request.getParameter("cmd");
		try
		{
			if (isLoggedIn(request)==false)
				throw new UserErrorMessage("You are not currently logged in. <a href='login.jsp'>Click here</a> to log in before doing anything else.");
			
			
			// Reflectively get the command class...
			String className = "Command"+cmd;
			Class c = null;
			try 
			{
				c = Class.forName("com.universeprojects.miniup.server.commands."+className);
			} 
			catch (ClassNotFoundException e) 
			{
				throw new IllegalArgumentException("Failed to execute command. Class name 'com.universeprojects.miniup.server.commands.Command"+cmd+"' does not exist.");
			}
			
			// Reflectively get the constructor for the command...
			Constructor<Command> constructor = null;
			try 
			{
				constructor = c.getConstructor(CachedDatastoreService.class, HttpServletRequest.class, HttpServletResponse.class);
			} 
			catch (NoSuchMethodException | SecurityException e) 
			{
				throw new RuntimeException("Malformed command class. "+e.getMessage());
			}
			
			
			ODPGameFunctions ds = new ODPGameFunctions();
			
			// Now create the command instance...
			Command command = null;
			try 
			{
				command = constructor.newInstance(ds, request, response);
			} 
			catch (InstantiationException e) 
			{
				throw new RuntimeException("Error in command constructor.", e);
			} 
			catch (IllegalAccessException e) 
			{
				throw new RuntimeException("Error in command constructor.", e);
			} 
			catch (IllegalArgumentException e) 
			{
				throw new RuntimeException("Error in command constructor.", e);
			} 
			catch (InvocationTargetException e) 
			{
				throw new RuntimeException("Error in command constructor.", e);
			}
			

			// Get all the parameters for this request, they will be included in the command...
			Map<String, String> params = new HashMap<String, String>();
			for(Object name:request.getParameterMap().keySet())
				params.put((String)name, request.getParameter((String)name));
	
			// Run the command!
			command.run(params);
				
			
			// Now return some result that is parsed on the client side.
			JSONObject result = new JSONObject();
			response.setContentType("application/json");
			//
			result.put("javascriptResponse", command.getJavascriptResponse().toString());
			result.put("message", command.getPopupMessage());
			//
			PrintWriter out = response.getWriter();
			out.print(result.toString());
			out.flush();
			out.close();
		}
		catch(UserErrorMessage e)
		{
			JSONObject result = new JSONObject();
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();

			try
			{
				if (e.isError()==false)
					result.put("message", e.getMessage());
				else
					result.put("errorMessage", e.getMessage());
			} 
			catch (JSONException e1) 
			{
				// This shouldn't happen
				throw new RuntimeException("JSON error..", e);
			}
				
			
			out.print(result.toString());
			out.flush();
			out.close();
		}
		catch(Exception e)
		{
			JSONObject result = new JSONObject();
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();

			try {
				if (e.getMessage()==null || e.getMessage().equals(""))
						result.put("errorMessage", "There was a server error. You can report this to a dev if you'd like.Command: "+cmd+" - Message: NPE");
				else
					result.put("errorMessage", "There was a server error. You can report this to a dev if you'd like.Command: "+cmd+" - Message: "+e.getMessage());
			} 
			catch (JSONException e1) 
			{
				// This shouldn't happen
				throw new RuntimeException("JSON error..", e);
			}
			
			out.print(result.toString());
			out.flush();
			out.close();
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
		}
	}
	

	
	
	public boolean isLoggedIn(HttpServletRequest request)
	{
		HttpSession session = request.getSession(true);
		
		Long authenticatedInstantCharacterId = (Long)session.getAttribute("instantCharacterId");
		Long userId = (Long)session.getAttribute("userId");
		if (userId==null && authenticatedInstantCharacterId==null)
			return false;
		
		return true;
	}
	
	
}
