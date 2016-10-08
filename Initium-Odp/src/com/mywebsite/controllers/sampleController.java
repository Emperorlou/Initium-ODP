package com.mywebsite.controllers;

import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class HelloController extends PageController {
  super("hello");
}

@Override
protected final String processRequest(HttpServletRequest request, HttpServletResponse response) 
  throws ServletException, IOException {

  // set request attributes, to be accessible in the JSP page
  request.setAttribute("message", "Hello World!");

  return "/WEB-INF/pages/hello.jsp";
}