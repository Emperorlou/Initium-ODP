<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<h4>This location's guards</h4>
<p>Freedom fighters: ${guardCounts.NoGuarding}</p>
<p>Trespass Guards:  ${guardCounts.NoTrespassers}</p>
<h4>Your PvP guard rules for this location</h4>
<c:if test="${hasGuardSettings==false}">
	You are not currently guarding anything.
</c:if>
<c:if test="${hasGuardSettings }">
	<ul>
	<c:forEach var="guardSetting" items="${guardSettings }">
		<li><a style='color:#FF0000' onclick='deleteGuardSetting(event, ${guardSetting.id})'>X</a> <c:out value="${guardSetting.text}"/></li>
	</c:forEach>
	</ul>
</c:if>
<hr>
<h4>Quick Options</h4>
<p><a onclick='doCommand(event, "GuardNewSetting", {entityKey:"${locationKey}", type:"NoGuarding"});'> + Disallow others to guard anything here</a></p>
<p><a onclick='doCommand(event, "GuardNewSetting", {entityKey:"${locationKey}", type:"NoTrespassers"});'> + Disallow trespassers here</a></p>
<br>
<p><a onclick='doCommand(event, "GuardDeleteAllSettings", {});'> - Clear ALL your guard settings</a></p>
<p><a onclick='doCommand(event, "GuardDeleteLocationSettings", {});'> - Clear your guard settings for this location</a></p>
<br>
<c:if test="${guardRunHitpoints!=null}">
	You are currently set to attempt to run when your hitpoints reach <strong>${guardRunHitpoints}</strong> or below.
</c:if>
<c:if test="${guardRunHitpoints==null}">
	You are currently set to fight to the death.  
</c:if>
<div>
	<div class='main-item-controls'>
		<a onclick='changeGuardRunHitpoints(event, "${guardRunHitpoints}")'>Change run settings</a>
	</div>
</div>