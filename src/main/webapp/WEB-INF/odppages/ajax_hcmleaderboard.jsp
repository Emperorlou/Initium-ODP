<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<strong style='float:left'>Name</strong>
<strong style='float:right'>Points</strong>
<br>
<hr>
<ol>
	<c:forEach var="character" items="${characters}">
		<li>
			${character.html} <span style='float:right'>${character.points}</span>
		</li>
	</c:forEach>
</ol>
