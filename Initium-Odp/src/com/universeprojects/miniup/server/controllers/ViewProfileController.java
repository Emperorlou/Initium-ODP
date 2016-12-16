package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

/**
 * Controller for the view_profile page.
 * Available request params:
 * 	inGroup - Boolean indicating whether current character is in a group
 *  groupName - Name of group character belongs to
 *  groupId - ID of group character belongs to
 *  groupRank - Rank of character in group
 *  groupStatus - Status of character in group
 *  isLeavingGroup - Boolean indicating whether the current character is leaving a group
 *  leavingGroupWaitTime - Time character must wait before he has left the group
 *  joinDate - Date user joined Initium
 *  premium - Boolean indicating whether user is Premium status
 *  userId - ID of the current user
 *  donationHistory - How much the user has donated
 *  totalDonations - Donation credits currently available to user
 *  hideUserActivity = Boolean indicating whether to hide this user's online status from their friends. - added by RevMuun
 * @author spfiredrake
 */
@Controller
public class ViewProfileController extends PageController {
	public ViewProfileController() {
		super("view_profile");
	}

	@Override
	protected String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ODPDBAccess db = new ODPDBAccess(request);
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity user = db.getCurrentUser();
		if(character == null)
			throw new ServletException("Unable to get current character from request");
		
		// Start with Group information
		if (character.getProperty("groupKey")!=null)
		{
			CachedEntity group = db.getEntity((Key)character.getProperty("groupKey"));
			request.setAttribute("inGroup", true);
			request.setAttribute("groupName", group.getProperty("name"));
			request.setAttribute("groupId", group.getId());
			request.setAttribute("groupRank", character.getProperty("groupRank"));
			request.setAttribute("groupStatus", character.getProperty("groupStatus"));
			String groupStatus = (String)character.getProperty("groupStatus");
			if (groupStatus.equals("Kicked")==false && character.getProperty("groupLeaveDate")!=null)
			{
				request.setAttribute("isLeavingGroup", true);
				request.setAttribute("leavingGroupWaitTime", GameUtils.getTimePassedShortString((Date)character.getProperty("groupLeaveDate")));
			}
		}
		else
		{
			request.setAttribute("inGroup", false);
		}

		
		// User specific info
		Long totalDonations = null;
		Long donationHistory = null;
		if (user!=null)
		{
			totalDonations = (Long)user.getProperty("totalDonations");
			donationHistory = (Long)user.getProperty("donationHistory");
			Date joinDate = (Date)user.getProperty("createdDate");
			request.setAttribute("joinDate", GameUtils.formatDate_Long(joinDate));
			request.setAttribute("premium", user.getProperty("premium"));
			request.setAttribute("userId", user.getId());
		}
		else
			request.setAttribute("premium", false);
		
		request.setAttribute("charName", character.getProperty("name"));
		request.setAttribute("hideUserActivity", user.getProperty("hideUserActivity"));
		
		if (totalDonations==null) totalDonations = 0L;
		if (donationHistory==null) donationHistory = 0L;
		Double donationHistoryDollars = donationHistory.doubleValue()/100;
		Double donationDollars = totalDonations.doubleValue()/100;
		request.setAttribute("donationHistory", "$"+GameUtils.formatNumber(donationHistoryDollars));
		request.setAttribute("totalDonations", "$"+GameUtils.formatNumber(donationDollars));
		
		return "/WEB-INF/odppages/view_profile.jsp";
	}
}
