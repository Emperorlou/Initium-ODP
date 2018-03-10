<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<c:if test='${showHidden!=true}'>
	<p><a onclick='viewLocalNavigation(event, true)'>Show Hidden Paths</a></p>
</c:if>
<c:forEach var="location" items="${locations}">
	<div onclick='doGoto(event, ${location.id})' class='sublocation-display-container'>
		<div class='sublocation-display' style='background-image:url(${location.bannerUrl})'></div>
		<h5>${location.name}</h5>
	</div>
</c:forEach>