package com.universeprojects.miniup.server.commands.jsaccessors;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.CommandSetLabel;
import com.universeprojects.miniup.server.commands.CommandSetLeader;
import com.universeprojects.miniup.server.commands.framework.Command;

interface Dispatcher
{
	Command dispatch(HttpServletRequest request, HttpServletResponse response);
}

public class CommandAccessor {
	Map<String,Dispatcher> commands = new HashMap<>();
	final HttpServletRequest request;
	final HttpServletResponse response;
	
	public CommandAccessor(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		final ODPDBAccess dbFinal = db;
		this.request = request;
		this.response = response;

		//// Initialise list of white-listed commands
		// Set Label
		commands.put("SetLabel", new Dispatcher() {
			@Override
			public Command dispatch(HttpServletRequest request, HttpServletResponse response) {
				return new CommandSetLabel(dbFinal, request, response);
			}});
		// Set Leader
		commands.put("SetLeader", new Dispatcher() {
			@Override
			public Command dispatch(HttpServletRequest request, HttpServletResponse response) {
				return new CommandSetLeader(dbFinal, request, response);
			}});
	}

	public Command getCommand(String commandName)
	{
		Dispatcher dispatcher = commands.get("commandName");
		if (dispatcher == null)
		{
			throw new RuntimeException("CommandAccessor: no such command '" + commandName + "'");
		}
		return dispatcher.dispatch(request, response);
	}
}
