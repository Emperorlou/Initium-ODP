package com.universeprojects.miniup.server.controllers;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.services.RevenueService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class GlobalBuffController extends PageController{

    public GlobalBuffController(){
        super("globalbuffs");
    }
    @Override
    protected String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ODPDBAccess db = ODPDBAccess.getInstance(request);

        RevenueService rs = new RevenueService(db);

        List<CachedEntity> buffs = db.getFilteredList("GlobalBuff", "enabled", true);

        List<String> activeBuffOutput = new ArrayList<>();
        List<String> inactiveBuffOutput = new ArrayList<>();

        //Iterate over all the buffs
        for(CachedEntity buff : buffs){
            String html = "";

            Boolean active = rs.isBuffActive(buff);

            Long price = (Long) buff.getProperty("cost");

            html += "<div class = 'boldbox'>";
            html += "<h4>" + buff.getProperty("displayName") + "</h4>";
            html += "<h5>" + WebUtils.centsToDollars(price, false) + " for " + displayTime((Long) buff.getProperty("duration")) + "</h5>";

            //TODO display the timer
            if(active){
                html += "<center><h5 class = 'highlightbox-green'>";
                html += "</h5></center>";
            }

            //if the buff is active, we add a highlight
            html += "<div class = 'paragraph ";
            if(active)
                html += "highlightbox-green";
            html += "' style = 'margin:10px text-align:right;>";

            html += buff.getProperty("description");
            html += "</div>";

            html += "<div class = 'paragraph' style = 'margin:10px; text-align:right;'>";
            html += "<a onclick = 'orderGlobalBuff(event, " + buff.getId() + ")'>Order now!</a>";
            html += "</div></div>";

            if(active){
                activeBuffOutput.add(html);
            }
            else{
                inactiveBuffOutput.add(html);
            }
        }



        request.setAttribute("activeBuffs", activeBuffOutput);
        request.setAttribute("inactiveBuffs", inactiveBuffOutput);

        return "/WEB-INF/odppages/globalbuffs.jsp";
    }

    /**
     * Parse a number of minutes into an hour/minute split
     * @param minutes
     * @return
     */
    private String displayTime(Long minutes){

        //simply dividing should drop the decimal?
        Long hours = minutes/60;

        //if less than an hour, display just minutes.
        if(hours == 0L)
            return minutes + " minutes";
        else{
            String result = hours + " hour";
            if(hours > 1)
                result += "s";

            //find out the resulting minutes
            Long resultingMinutes = minutes%60;

            if(resultingMinutes != 0)
                result += " " + resultingMinutes + " minutes";

            return result;
        }
    }
}
