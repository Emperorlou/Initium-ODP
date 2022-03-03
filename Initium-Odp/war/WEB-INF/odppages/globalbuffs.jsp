<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<center><h1>Global Buffs</h1></center>
<p>
Describe how this feature works
</p>

<br><br>

<c:if test = "${activeBuffs.size() != 0}">
    <center><h2><Active Global Buffs</h2></center>

    <c:forEach var = "activeBuff" items = "${activeBuffs}">
        ${activeBuff}
    </c:forEach>
</c:if>

<br><br>

<c:if test = "${inactiveBuffs.size() != 0}">
    <center><h2><Inactive Global Buffs</h2></center>

    <c:forEach var = "inactiveBuff" items = "${inactiveBuffs}">
        ${inactiveBuff}
    </c:forEach>
</c:if>