package com.universeprojects.miniup.server;

import org.json.simple.JSONArray;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class SandboxServlet extends HttpServlet {
	private static final long serialVersionUID = 9209182266946047848L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int width = Integer.parseInt(request.getParameter("width"));
		int seed = Integer.parseInt(request.getParameter("seed"));
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		JSONArray result = new JSONArray();
		result.add(RandomTileGenerator.getBuildingCells(seed, width));
		response.getWriter().write(result.toJSONString());

	}
}
