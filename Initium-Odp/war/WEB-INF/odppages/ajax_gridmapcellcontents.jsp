<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<c:forEach var="item" items="${items}">
	<div class='tileContentsItem' ref='${item.id}'>${item.html}</div>
</c:forEach>

<script type='text/javascript'>
$(".mini-page-popup-reload").click(refreshTile);
</script>