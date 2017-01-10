package com.universeprojects.miniup.server;

import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SandboxServlet extends HttpServlet {
	private static final long serialVersionUID = 9209182266946047848L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int width = Integer.parseInt(request.getParameter("width"));
		int seed = Integer.parseInt(request.getParameter("seed"));
		int forestry = Integer.parseInt(request.getParameter("forestry"));
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		JSONObject result = new JSONObject();
		result.putAll(RandomTileGenerator.getBuildingCells(seed, width, forestry));
		response.getWriter().write(result.toJSONString());

	}
}
