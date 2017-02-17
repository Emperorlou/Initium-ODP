<%@page import="com.universeprojects.miniup.server.ODPDBAccess"%>
<%@page import="com.universeprojects.cacheddatastore.CachedEntity"%>
<%@page import="java.util.Date"%>
<%@page import="com.google.appengine.api.datastore.Key"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Comparator"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.List"%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@page import="com.google.appengine.api.datastore.KeyFactory"%>
<%@page import="com.universeprojects.miniup.server.WebUtils"%>
<%@page import="com.universeprojects.miniup.server.GameUtils"%>
<%@page import="com.universeprojects.miniup.server.services.GroupService"%>
<%@page import="com.universeprojects.miniup.server.GameFunctions"%>
<%@page import="com.universeprojects.miniup.server.SecurityException"%>
<%@page import="com.universeprojects.miniup.server.CommonEntities"%>
<%@page import="com.universeprojects.miniup.server.ErrorMessage"%>
<%@page import="com.universeprojects.miniup.server.JspSnippets"%>
<%@page import="com.google.appengine.api.datastore.Entity"%>
<%@page import="com.universeprojects.miniup.server.Authenticator"%>
<%
	response.setHeader("Access-Control-Allow-Origin", "*"); // This is absolutely necessary for phonegap to work

	Authenticator auth = Authenticator.getInstance(request);
	ODPDBAccess db = auth.getDB(request);
	try {
		auth.doSecurityChecks(request);
	} catch (SecurityException e) {
		JspSnippets.handleSecurityException(e, request, response);
		return;
	}

	CommonEntities common = CommonEntities.getInstance(request);

	boolean isAdmin = false;
	CachedEntity group = null;

	Long groupId = WebUtils.getLongParam(request, "groupId");
	if (groupId == null)
		groupId = (Long) request.getAttribute("groupId");
	String groupName = request.getParameter("name");

	if (groupName != null) {
		groupName = groupName.replace("_", " ");
		group = db.getGroupByName(groupName);
	} else if (groupId != null) {
		group = db.getEntity(KeyFactory.createKey("Group", groupId));
	}

	if (group == null) {
		WebUtils.forceRedirectClientTo("main.jsp", request, response,
				"The group you tried to inspect does not exist.");
		return;
	}

	request.setAttribute("groupName", group.getProperty("name"));
	request.setAttribute("groupDescription",
			group.getProperty("description"));
	String descriptionEscaped = (String) group
			.getProperty("description");
	if (descriptionEscaped != null) {
		descriptionEscaped = descriptionEscaped.replace("\"", "\\\"");
		request.setAttribute("groupDescriptionEscaped",
				descriptionEscaped);
	}

	if (common.getGroup() != null) {
		request.setAttribute("inGroup", true);
		if (common.getGroup().getKey().getId() == group.getKey()
				.getId()) {
			request.setAttribute("inThisGroup", true);
			if ("Admin".equals(common.getCharacter().getProperty(
					"groupStatus")))
				isAdmin = true;
		}
		request.setAttribute("groupStatus", common.getCharacter()
				.getProperty("groupStatus"));
	} else {
		request.setAttribute("inGroup", false);
	}

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
				return 3;
			if (e.getProperty("leaveGroupDate") != null)
				return 4;
			if (status.equals("Kicked"))
				return 5;

			return 1000;
		}
	});
	
	
	// Get the number of members of this group that were active in the past 3 hours
	int activeUsersPast3Hours = db.getActiveGroupPlayers(group, members, 60*3).size();
	request.setAttribute("activeUsersPast3Hours", activeUsersPast3Hours);
	
	// Get the number that were active in the past week
	int activeUsersPastWeek = db.getActiveGroupPlayers(group, members, 60*24*7).size();
	request.setAttribute("activeUsersPastWeek", activeUsersPastWeek);

%>

<div class='main-banner-textonly' style='height:240px; background-color:rgba(0,0,0,0.5)'>
	<div class='main-banner-text'>
		<h1>
			<c:out value="${groupName}" />
		</h1>
		<p>
			<c:out value="${groupDescription}" />
		</p>
	</div>
</div>

<div class='paragraph'>
	<span style='font-size:24px'>${activeUsersPast3Hours}</span> group members have been active in the past 3 hours and <span style='font-size:24px'>${activeUsersPastWeek}</span> group members have been active in the past week. 
</div>

<c:if test="${groupStatus=='Admin' && inThisGroup}">
	<div class='main-description'>
		<h2>Admin Controls</h2>

		<div class='main-item-controls'>
			<a
				onclick='setGroupDescription(event, "<c:out value="${groupDescriptionEscaped}"/>")'>Set&nbsp;group&nbsp;description</a>

		</div>

		<h4>New Member Applications</h4>
		<%
			if (applications.isEmpty()) {
					out.println("No new members applications at this time.");
				} else {
					for (CachedEntity character : applications) {
						boolean dead = false;
						if (((Double) character.getProperty("hitpoints")) <= 0)
							dead = true;
						out.println("<div>");
						out.println("<div class='main-item-container'>");
						out.println("<a class='main-item clue' rel='viewcharactermini.jsp?characterId="
								+ character.getKey().getId()
								+ "'>"
								+ character.getProperty("name"));
						out.println("<div class='main-item-controls' style='top:0px'>");
						out.println("<a onclick='groupAcceptJoinGroupApplication(event, "
								+ character.getKey().getId() + ")'>Accept</a>");
						out.println("<a onclick='groupDenyJoinGroupApplication(event, "
								+ character.getKey().getId() + ")'>Deny</a>");
						out.println("</div>");
						out.println("</a>");
						out.println("</div>");
						out.println("</div>");
					}
				}
		%>
	</div>
	<hr>
</c:if>


<div class='main-description'>
	<h4>Members</h4>
	<%
		for (CachedEntity character : members) {
			String groupStatus = (String) character
					.getProperty("groupStatus");

			String groupPermissionTag = "";
			if ("Admin".equals(groupStatus))
				groupPermissionTag = "(Admin)";
			if (((Key) group.getProperty("creatorKey")).getId() == character
					.getKey().getId())
				groupPermissionTag = "(Creator)";

			String groupRank = "";
			if (character.getProperty("groupRank") != null)
				groupRank = (String) character.getProperty("groupRank");
			out.println("<div>");
			out.println("<div class='main-item-container'>");
			out.println("<div class='main-item clue' rel='viewcharactermini.jsp?characterId="
					+ character.getKey().getId()
					+ "'>"
					+ character.getProperty("name"));
			out.println("</div>");
			out.println("<div class='main-item-controls' style='top:0px; display:block; margin-bottom:25px;'>");
			out.println("<span>" + groupPermissionTag + "</span> ");
			if (groupStatus.equals("Kicked") == false) {
				out.println("Position: " + groupRank + "<br>");
				if (character.getProperty("groupLeaveDate") != null)
					out.println("(This member is leaving the group. They will be out of the group in: "
							+ GameUtils
									.getTimePassedShortString((Date) character
											.getProperty("groupLeaveDate"))
							+ ")<br>");
			} else
				out.println("(Member is being kicked from the group. They will be out of the group in: "
						+ GameUtils
								.getTimePassedShortString((Date) character
										.getProperty("leaveGroupDate"))
						+ ")<br>");
			if (isAdmin
					|| ((Key) group.getProperty("creatorKey")).getId() == common
							.getCharacter().getKey().getId()) {
				out.println("<a href='#' onclick='setGroupMemberRank(event, \""
						+ character.getProperty("groupRank") + "\", "
						+ character.getKey().getId()
						+ ")'>Set position</a>");
				if (((Key) group.getProperty("creatorKey")).getId() == common
						.getCharacter().getKey().getId()) {
					if (groupStatus.equals("Admin") == false)
						out.println("<a href='#' onclick='promoteToAdmin(event, "
								+ character.getKey().getId()
								+ ")'>Promote to admin</a>");
					if (groupStatus.equals("Admin") == true) {
						if (((Key) group.getProperty("creatorKey")).getId() != character
								.getKey().getId())
							out.println("<a href='#' onclick='makeGroupCreator(event, "
									+ character.getKey().getId()
									+ ")' title='Setting this member to group creator will permanently make him in charge of adding and removing admins'>Promote to group creator</a>");
						out.println("<a href='#' onclick='demoteFromAdmin(event, "
								+ character.getKey().getId()
								+ ")'>Demote from admin</a>");
					}
				}
				if (groupStatus.equals("Kicked") == false 
						&& groupStatus.equals("Admin") == false 
						|| (Key) group.getProperty("creatorKey")).getId() == 
						common.getCharacter().getKey().getId())
					out.println("<a onclick='groupMemberKick(event, "
							+ character.getKey().getId() + ", "+character.getProperty("name")+")'>Kick</a>");
				else
					out.println("<a onclick='groupMemberKickCancel("
							+ character.getKey().getId()
							+ ")'>Cancel Kick</a>");
				if ((GameUtils.equals(group.getProperty("creatorKey"),
						character.getKey())) && (members.size() <= 1))
					out.println("<a onclick='deleteGroup(event)'>Delete group</a>");
			}
			out.println("</div>");
			out.println("</div>");
			out.println("</div>");
		}
	%>
</div>

<c:if test="${inGroup == true && inThisGroup!=true}">
	<div class='main-description'>You are currently part of a group
		and cannot join this one. If you wish to join this group, you will
		have to leave your current one first.</div>
</c:if>
<c:if test="${inGroup == false}">
	<div class='main-buttonbox'>
		<a onclick='groupRequestJoin(event, <%=group.getKey().getId()%>)'
			class='main-button'>Request to join this group</a>
	</div>
</c:if>