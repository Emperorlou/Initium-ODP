package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class SoundtrackController extends PageController {
	
	public SoundtrackController() {
		super("soundtrack");
	}

	@Override
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		List<Map<String,String>> tracks = new ArrayList<Map<String,String>>();
		
		tracks.add(track("New Horizons 1"));
		tracks.add(track("Army of Angels"));
		tracks.add(track("A Kings Ransom"));
		tracks.add(track("Olympus"));
		tracks.add(track("Mysterious Forest"));
		tracks.add(track("Bring Adventures"));
		tracks.add(track("Battle of Wills 1"));
		tracks.add(track("Battle of Wills 2"));
		tracks.add(track("Calm Before the Storm"));
		tracks.add(track("The Darkest Hour"));
		tracks.add(track("Downfall"));
		tracks.add(track("Easy Target"));
		tracks.add(track("In The Periphery"));
		tracks.add(track("Intergalactic Warfare"));
		tracks.add(track("New Horizons 2"));
		tracks.add(track("Quest"));
		tracks.add(track("Rainy and Mystical"));
		tracks.add(track("Rise or Fall"));
		tracks.add(track("Something Else"));
		tracks.add(track("Struggle In The Jungle"));
		tracks.add(track("The Uprising"));
		tracks.add(track("Tragic Hero"));
		tracks.add(track("Under Pressure"));
		tracks.add(track("Voices of War 1"));
		tracks.add(track("Voices of War 2"));
		tracks.add(track("Soundstages 18"));
		
		request.setAttribute("tracks", tracks);
		
		return "/WEB-INF/odppages/soundtrack.jsp";
	}
	
	private Map<String,String> track(String name)
	{
		Map<String,String> result = new HashMap<String,String>();
		
		result.put("name", name);
		result.put("url", "/music/"+name+".mp3");
		
		return result;
	}
}