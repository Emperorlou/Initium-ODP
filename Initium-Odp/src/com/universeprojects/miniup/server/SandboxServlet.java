package com.universeprojects.miniup.server;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.repackaged.com.google.gson.Gson;

public class SandboxServlet extends HttpServlet {
	private static final long serialVersionUID = 9209182266946047848L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int width = Integer.parseInt(request.getParameter("width"));
		int seed = Integer.parseInt(request.getParameter("seed"));

		String json = new Gson().toJson(RandomTileGenerator.getBuildingCells(seed, width));
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Map<String, String> options = new LinkedHashMap<>();
		options.put("value1", "label1");
		options.put("value2", "label2");
		options.put("value3", "label3");
		String json = new Gson().toJson(options);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}
}
