package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.services.GroupService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class GroupController extends PageController {

	public GroupController() {
		super("ajax_group");
	}

	@Override
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}

		response.setHeader("Access-Control-Allow-Origin", "*");     // This is absolutely necessary for phonegap to work

	    CachedDatastoreService ds = db.getDB();
	    CachedEntity character = db.getCurrentCharacter(); 
		GroupService service = new GroupService(db, character);
		CachedEntity group = null;

		Long groupId = WebUtils.getLongParam(request, "groupId");
		if (groupId == null)
			groupId = (Long) request.getAttribute("groupId");
		String groupName = request.getParameter("name");

		if (groupName != null) {
			groupName = groupName.replace("_", " ");
			group = db.getGroupByName(groupName);
		} else if (groupId != null) {
			group = db.getEntity("Group", groupId);
		}

		if (group == null) {
			WebUtils.forceRedirectClientTo("main.jsp", request, response,
					"The group you tried to inspect does not exist.");
			return null;
		}

		if(request.getAttribute("groupId") == null) 
			request.setAttribute("groupId", group.getId());
		
		request.setAttribute("groupName", group.getProperty("name"));
		request.setAttribute("groupDescription", group.getProperty("description"));
		String descriptionEscaped = (String) group.getProperty("description");
		if (descriptionEscaped != null) {
			descriptionEscaped = descriptionEscaped.replace("\"", "\\\"");
			request.setAttribute("groupDescriptionEscaped", descriptionEscaped);
		}

		// Group has been merged.
		if(service.isSpecifiedGroupMerged(group))
		{
			request.setAttribute("isGroupMerged", true);
			CachedEntity mergedGroup = db.getEntity(service.getMergeRequestGroupKeyFor(group));
			request.setAttribute("mergedGroupId", mergedGroup.getId());
			request.setAttribute("mergedGroupName", mergedGroup.getProperty("name"));
		}
		else
		{
			request.setAttribute("isGroupMerged", false);
			// Sort the group members by groupStatus and then alphabetically
			List<CachedEntity> members = db.getGroupMembers(null, group);
			List<CachedEntity> applications = new ArrayList<CachedEntity>();
			// GO through the members list and pull out all the applications into a new list
			for (int i = members.size() - 1; i >= 0; i--)
				if ("Applied".equals(members.get(i).getProperty("groupStatus"))) {
					if(GameUtils.isPlayerIncapacitated(members.get(i))==false)
						applications.add(members.get(i));
					members.remove(i);
				}
	
			// Now sort the remaining members 
			Collections.sort(members, new Comparator<CachedEntity>() {
				public int compare(CachedEntity e1, CachedEntity e2) {
					int e1SortNumber = sortNumber(e1);
					int e2SortNumber = sortNumber(e2);
	
					if (e1SortNumber == e2SortNumber) {
						return ((String) e1.getProperty("name"))
								.compareTo((String) e2.getProperty("name"));
					} else {
						if (e1SortNumber > e2SortNumber)
							return 1;
						else
							return -1;
					}
				}
	
				private int sortNumber(CachedEntity e) {
					String status = (String) e.getProperty("groupStatus");
					if (status == null)
						return 1000;
					if (status.equals("Applied"))
						return 1;
					if (status.equals("Admin"))
						return 2;
					if (status.equals("Member"))
					{
						if (isOnline(e))
							return 3;
						else
							return 4;
					}
					if (e.getProperty("leaveGroupDate") != null)
						return 5;
					if (status.equals("Kicked"))
						return 6;
	
					return 1000;
				}
			});
			
			// Output members.
			List<String> memberOutput = new ArrayList<String>();
			boolean inGroup = service.isCharacterInSpecifiedGroup(group);
			boolean canDeleteGroup = members.size() <= 1;
			for(CachedEntity member:members)
			{
				String output = HtmlComponents.generateGroupMember(character, member, group, inGroup, canDeleteGroup);
				if (isOnline(member))
					output = "<span style='float:left; text-shadow:0px 0px 5px;' title='This indicates whether or not you`re connected to the chat server'><span style='color:#00FF00'>&#9679;</span></span>" + output;
				memberOutput.add(output);
			}
			request.setAttribute("groupMembers", memberOutput);
			
			// Handle group
			if (service.characterHasGroup()) {
				request.setAttribute("inGroup", true);
				boolean isAdmin = service.isCharacterGroupAdmin();
				boolean isCreator = service.isCharacterGroupCreator();
				request.setAttribute("isAdmin", isAdmin);
				request.setAttribute("isCreator", isCreator);
				
				// Group merge information.
				boolean allowMergeRequests = service.doesGroupAllowMergeRequests(group);
				
				if(service.isCharacterInSpecifiedGroup(group))
				{
					request.setAttribute("inThisGroup", true);
					if(isAdmin)
					{
						// Character is an admin, check to see if we have any groups wanting to merge
						List<CachedEntity> mergeGroupApplications = db.getFilteredList("Group", "pendingMergeGroupKey", group.getKey());
						List<String> mergeApplicationsOutput = new ArrayList<String>();
						for(CachedEntity candidate:mergeGroupApplications)
						{
							if(candidate.getProperty("creatorKey") == null) continue;
							String output = HtmlComponents.generateGroupMergeApplication(candidate);
							mergeApplicationsOutput.add(output);
						}
						
						if(mergeApplicationsOutput.isEmpty()) 
							mergeApplicationsOutput.add("No group merge requests at this time.");
						request.setAttribute("groupMergeApplications", mergeApplicationsOutput);
						
						List<String> appliedMembersOutput = new ArrayList<String>();
						// Now output the applied members.
						for(CachedEntity app:applications)
						{
							String output = HtmlComponents.generateGroupMemberApplication(app);
							appliedMembersOutput.add(output);
						}
						
						if(appliedMembersOutput.isEmpty()) 
							appliedMembersOutput.add("No new members applications at this time.");
						
						request.setAttribute("newMemberApplicants", appliedMembersOutput);
						
						if(isCreator)
						{
							if(allowMergeRequests)
								request.setAttribute("mergeRequestToggleButton", "<a id='mergeRequestDisallow' onclick='groupMergeRequestsDisallow(event)' title='Clicking this will prevent other groups from requesting to merge with this group.'>Disallow Merge Requests</a>");
							else
								request.setAttribute("mergeRequestToggleButton", "<a id='mergeRequestAllow' onclick='groupMergeRequestsAllow(event)' title='Clicking this will allow other groups to request merging with this group.'>Allow Merge Requests</a>");
							
							String currentMergeRequestString = "No pending merge requests.";
							Key mergeGroupKey = service.getMergeRequestGroupKeyFor(group);
							if(mergeGroupKey != null)
							{
								CachedEntity mergeGroup = db.getEntity(mergeGroupKey);
								if(mergeGroup != null)
									currentMergeRequestString = "<a id='groupMergeRequest' onclick='groupMergeCancelRequest(event)' title='Clicking this will cancel the pending merge request with the specified group'>Cancel merge request with "+mergeGroup.getProperty("name")+"</a>";
							}
							request.setAttribute("currentMergeRequest", currentMergeRequestString);
						}
					}
				}
				else
				{
					request.setAttribute("inThisGroup", false);
					if(isCreator && allowMergeRequests)
					{
						request.setAttribute("allowMergeRequests", allowMergeRequests);
						// Group allows merge requests and viewing character is an admin.
						// Need to determine which version of the request merge to show.
						String requestMergeString = "";
						if(service.hasGroupRequestedMergeWith(group))
							requestMergeString = "<a id='groupMergeRequest' onclick='groupMergeCancelRequest(event)' title='Clicking this will cancel the pending merge request with the specified group'>Cancel Merge Request</a>";
						else
							requestMergeString = "<a id='groupMergeRequest' onclick='groupMergeSubmitRequest(event, "+group.getId()+")' title='Clicking this will submit a request to merge with the current group.'>Request Merge With Group</a>";
						request.setAttribute("currentMergeRequest", requestMergeString);
					}
					else
						request.setAttribute("allowMergeRequests", false);
				}
			} 
			else {
				request.setAttribute("inGroup", false);
				request.setAttribute("inThisGroup", false);
				request.setAttribute("isAdmin", false);
			}
			
			
			// Get the number of members of this group that were active in the past 3 hours
			int activeUsersPast3Hours = db.getActiveGroupPlayers(group, members, 60*3).size();
			request.setAttribute("activeUsersPast3Hours", activeUsersPast3Hours);
			
			// Get the number that were active in the past week
			int activeUsersPastWeek = db.getActiveGroupPlayers(group, members, 60*24*7).size();
			request.setAttribute("activeUsersPastWeek", activeUsersPastWeek);

			List<CachedEntity> receivedWars = db.getFilteredList("Group", "declaredWarGroups", group.getKey());
			@SuppressWarnings("unchecked")
			List<Key> alliedGroups = (List<Key>)group.getProperty("declaredAlliedGroups");
			@SuppressWarnings("unchecked")
			List<Key> declaredWars = (List<Key>)group.getProperty("declaredWarGroups");
			boolean isCreator = service.isCharacterGroupCreator();

			if (declaredWars != null)
			{
				List<CachedEntity> declaredWarGroups = db.getEntities(declaredWars);
				List<String> warGroupNames = new ArrayList<String>();
				
				for (CachedEntity war : declaredWarGroups) 
				{
					if (war == null)
						continue;
					String output = HtmlComponents.generateWarDeclarations(war, isCreator, inGroup);
					warGroupNames.add(output);
				}
				request.setAttribute("declaredWars", warGroupNames);
			}	
			if (receivedWars != null)
			{
				List<String> warGroupNames = new ArrayList<String>();
				
				for (CachedEntity war : receivedWars)
				{
					if (war == null)
						continue;
					String output = HtmlComponents.generateWarsReceived(war);
					warGroupNames.add(output);
				}
				request.setAttribute("receivedWars", warGroupNames);
			}
			if (alliedGroups != null)
			{
				List<CachedEntity> declaredAlliedGroups = db.getEntities(alliedGroups);
				List<String> alliedGroupNames = new ArrayList<String>();
				boolean isAdmin = service.isCharacterGroupAdmin();
				
				for (CachedEntity allies : declaredAlliedGroups)
				{
					if (allies == null)
						continue;
					String output = HtmlComponents.generateAlliedGroups(allies, isCreator, inGroup);
					alliedGroupNames.add(output);
				}
				request.setAttribute("alliedGroups", alliedGroupNames);
			}

			List<CachedEntity> allyRequests = db.getFilteredList("Group", "pendingAllianceGroupKey", group.getKey());
			List<String> outputAllyRequests = new ArrayList<String>();
			
				for (CachedEntity allyReq : allyRequests)
				{
					String output = HtmlComponents.generateGroupAllianceRequest(allyReq, isCreator, inGroup);
					outputAllyRequests.add(output);
				}
				if (outputAllyRequests.isEmpty())
					outputAllyRequests.add("No group alliance requests at this time.");
				
				request.setAttribute("pendingGroupAllies", outputAllyRequests);
			}
		
		return "/WEB-INF/odppages/ajax_group.jsp";
	}

	
	private boolean isOnline(CachedEntity character)
	{
		Date timestamp = (Date)character.getProperty("locationEntryDatetime");
		if (timestamp!=null)
		{
			long timestampMs = timestamp.getTime();
			long msPassed = System.currentTimeMillis()-timestampMs;
			if (msPassed<1000*60*60*3)	// 3 hours
				 return true;
		}
		
		return false;
	}
}
