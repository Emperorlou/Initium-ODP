<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<c:if test='${locationsViewable==true }'>
	<c:if test='${showHidden==false}'>
		<p><a onclick='viewLocalNavigation(event, true)'>Show Hidden Paths</a></p>
	</c:if>
	<c:if test='${showHidden==true}'>
		<p><a onclick='viewLocalNavigation(event, false)'>Hide Hidden Paths</a></p>
	</c:if>
	<c:forEach var="location" items="${locations}">
		<div onclick=${location.onClick} class='standard-button-highlight sublocation-display-container location-link-${location.locationId}'>
			<div class='sublocation-display' style='background-image:url(${location.bannerUrl})'></div>
			<h5>${location.name}</h5>
			<c:if test="${location.isForgettable=='true'}">
				<div class='standard-button-highlight forget-button' onclick='doForgetCombatSite(event,${location.locationId})'>X</div>
			</c:if>
		</div>
	</c:forEach>
	<c:if test='${forgetAllCombatSitesHtml!=null }'>
		${forgetAllCombatSitesHtml}
	</c:if>
</c:if>