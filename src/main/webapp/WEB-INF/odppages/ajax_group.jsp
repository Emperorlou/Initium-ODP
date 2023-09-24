<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<div class='main-banner-textonly' style='height:240px; background-color:rgba(0,0,0,0.5)'>
	<div class='main-banner-text'>
		<h1>
			<c:out value="${groupName}" />
		</h1>
		<p>
			${groupDescription}
			<br />
		</p>
	</div>
</div>

<c:if test="${isGroupMerged}">
	<div class='main-description'>	
		<h4>This group has merged with <a class='link' onclick='viewGroup(${mergedGroupId})'>${mergedGroupName}</a>. 
	</div>
</c:if>

<c:if test="${isGroupMerged == false}">
<div class='paragraph'>
	<span style='font-size:24px'>${activeUsersPast3Hours}</span> group members have been active in the past 3 hours and <span style='font-size:24px'>${activeUsersPastWeek}</span> group members have been active in the past week. 
</div>
 
<c:if test="${isAdmin}">
	<c:if test="${inThisGroup}">
	<div class='main-description'>	
		<h2>Admin Controls</h2>
		<div class='main-item-controls'>
			<a onclick='setGroupDescription(event, "<c:out value="${groupDescriptionEscaped}"/>")'>Set group description</a>
			<br/><br/>
			${currentMergeRequest}
			<br/><br/>
			<c:if test="${isCreator}">
				${mergeRequestToggleButton}
				<br/><br/>
				<a onclick='declareWar(event)'>Declare War</a>
				<br/><br/>
				<a onclick='submitGroupAllianceRequest(event)'>Request Alliance</a>
			<br/><br/>
			</c:if>
		</div>
		<c:if test="${isCreator}">
		<h4>Group Merge Applications</h4>
		<c:forEach var="groupApp" items="${groupMergeApplications}">
			${groupApp}
		</c:forEach>
		</c:if>
		<h4>New Member Applications</h4>
		<c:forEach var="memberApp" items="${newMemberApplicants}">
			${memberApp}
		</c:forEach>
		<h4>Group Alliance Applications</h4>
		<c:forEach var="allyApp" items="${pendingGroupAllies}">
			${allyApp}
		</c:forEach>				
	</div>
	<hr>
	</c:if>
	<c:if test="${inThisGroup == false && allowMergeRequests}">
	<div class='main-description'>	
		<h2>Admin Controls</h2>
		<div class='main-item-controls'>
			${currentMergeRequest}
		</div>
	</div>
	<hr>
	</c:if>
</c:if>

<c:if test="${inGroup == false}">
	<div class='main-buttonbox'>
		<a onclick='groupRequestJoin(event, ${groupId})'
			class='v3-main-button'>Request to join this group</a>
	</div>
</c:if>

<div class='main-description'>
	<h4>Declared Alliances</h4>
	<c:forEach var="ally" items="${alliedGroups}">
		${ally}
	</c:forEach>
	<h4>Declared Wars</h4>
	<c:forEach var="warGroup" items="${declaredWars}">
		${warGroup}
	</c:forEach>
	<h4>Received Wars</h4>
	<c:forEach var="warGroup" items="${receivedWars}">
		${warGroup}
	</c:forEach>
	<h4>Members</h4>
	<c:forEach var="curMember" items="${groupMembers}">
		${curMember}
	</c:forEach>	
</div>

<c:if test="${inGroup == true && inThisGroup!=true}">
	<div class='main-description'>You are currently part of a group
		and cannot join this one. If you wish to join this group, you will
		have to leave your current one first.</div>
</c:if>
<c:if test="${inGroup == false}">
	<div class='main-buttonbox'>
		<a onclick='groupRequestJoin(event, ${groupId})'
			class='v3-main-button'>Request to join this group</a>
	</div>
</c:if>

</c:if>
