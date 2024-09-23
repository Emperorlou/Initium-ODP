<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<!-- <div class='paragraph' style='text-align:right'><a onclick='restartNoobQuests()'>Restart Noob Quests</a></div> -->

<center><a class='standard-button-highlight' onclick='viewTrainingQuestLines();'><img alt='Training quests' src='/images/ui3/quest-banners/button-training-quests1.png'/></a></center>

<h2>Quests</h2>
<c:if test="${hasActiveQuests!=true && hasFinishedQuests!=true}">
	You don't have any quests at the moment.
</c:if>
<c:if test="${hasActiveQuests==true}">
	<h4>Active Quests</h4>
	<c:forEach var="quest" items="${activeQuests}">
		<div class='quest-container' id='questlist-questkey-${quest.key}'>
			<a onclick='viewQuest("${quest.key}")'>${quest.name}</a>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<p style='display:inline-block'><a onclick='deleteQuest("${quest.key}")' title='End this quest'>Stop Permanently</a></p>
		</div>
	</c:forEach>
</c:if>

<c:if test="${hasFinishedQuests == true }">
	<h4>Finished Quests</h4>
	<c:forEach var="finishedQuest" items="${finishedQuests}">
		<div class='quest-container quest-complete' id='questlist-questkey-${finishedQuest.key}'>
			<a onclick='viewQuest("${finishedQuest.key}")'>${finishedQuest.name}</a>
		</div>
	</c:forEach>
</c:if>

