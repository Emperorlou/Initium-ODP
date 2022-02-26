package com.universeprojects.miniup.server.controllers;

import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class GlobalBuffController extends PageController{

    public GlobalBuffController(){
        super()
    }
    @Override
    protected String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        return null;
    }
}
