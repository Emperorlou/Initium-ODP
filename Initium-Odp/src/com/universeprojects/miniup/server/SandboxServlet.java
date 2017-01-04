package com.universeprojects.miniup.server;

import org.json.simple.JSONArray;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "SandboxServlet")
public class SandboxServlet extends HttpServlet {
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
