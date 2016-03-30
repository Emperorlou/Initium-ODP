package com.universeprojects.miniup.server.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.universeprojects.cacheddatastore.CachedDatastoreService;

@SuppressWarnings("serial")
public class ServletCommand extends HttpServlet 
{
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException 
	{
//		resp.setContentType("text/plain");
//		resp.getWriter().println("Hello, world");
		
		
		String cmd = request.getParameter("cmd");
		try
		{
			
			// Reflectively get the command...
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
			
			
			Constructor<Command> constructor = null;
			try 
			{
				constructor = c.getConstructor(CachedDatastoreService.class, HttpServletRequest.class, HttpServletResponse.class);
			} 
			catch (NoSuchMethodException | SecurityException e) 
			{
				throw new RuntimeException("Malformed command class. "+e.getMessage());
			}
			
			
			CachedDatastoreService ds = new CachedDatastoreService();
			
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
			
			
			Map<String, String> params = request.getParameterMap();
	
			command.run(params);
				
			JSONObject result = new JSONObject();
			response.setContentType("application/json");
			
			result.put("message", command.getPopupMessage());
			
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
			throw new RuntimeException(e);
		}
	}
	
	
}
