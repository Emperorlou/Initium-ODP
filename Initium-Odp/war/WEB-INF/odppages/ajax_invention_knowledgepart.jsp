<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<ul>
<c:forEach var="knowledge" items="${knowledgeTree.children}">
	<li ref='${knowledge.id}'><img src='${knowledge.iconUrl}' border=0/> ${knowledge.name}</li>
    <c:set var="knowledgeTree" value="${knowledge}" scope="request"/>
	<c:if test="${knowledgeTree.children!=null}">
	    <jsp:include page="ajax_invention_knowledgepart.jsp"/>
	</c:if>
</c:forEach>
</ul>