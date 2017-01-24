package com.universeprojects.miniup.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.miniup.server.model.GridMap;
import org.json.simple.JSONObject;


public class SandboxServlet extends HttpServlet 
{
	private static final long serialVersionUID = 9209182266946047848L;
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		int width = Integer.parseInt(request.getParameter("width"));
		int height = Integer.parseInt(request.getParameter("height"));
		int seed = Integer.parseInt(request.getParameter("seed"));
		int forestry = Integer.parseInt(request.getParameter("forestry"));
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		JSONObject result = new JSONObject();
		GridMap gridMap = RandomTileGenerator.buildNewGrid(seed, width, height, forestry);
		result.put("backgroundTiles", gridMap.getMap());
		result.put("objectMap", gridMap.getGridObjects());

		response.getWriter().write(result.toJSONString());
	}
}
