<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<!-- <div class='paragraph' style='text-align:right'><a onclick='restartNoobQuests()'>Restart Noob Quests</a></div> -->

<center><a class='standard-button-highlight' onclick='viewTrainingQuestLines();'><img alt='Training quests' src='https://initium-resources.appspot.com/images/ui3/quest-banners/button-training-quests1.png'/></a></center>

<h4>Quests</h4>
<c:if test="${hasQuests!=true}">
	You don't have any quests at the moment.
</c:if>
<c:if test="${hasQuests==true}">
	<c:forEach var="quest" items="${data}">
		<c:if test="${quest.complete==true}">
		<div class='quest-container quest-complete' id='questlist-questkey-${quest.key}'>
		</c:if>
		<c:if test="${quest.complete==false}">
		<div class='quest-container' id='questlist-questkey-${quest.key}'>
		</c:if>
			<a onclick='viewQuest("${quest.key}")'>${quest.name}</a>
		</div>
	</c:forEach>
</c:if>
