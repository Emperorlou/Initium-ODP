<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<script type='text/javascript'>
function changeInventionTab(event)
{
	var element = $(event.target);
	var id = element.attr("id");
	var code = id.substring(0, id.indexOf("-"));
	
	$(".tab-selected").removeClass("tab-selected");
	element.addClass("tab-selected");
	
	$(".tab-content-selected").removeClass("tab-content-selected");
	$(".tab-content#"+code+"-content").addClass("tab-content-selected");
	console.log(code);
}

</script>

<div class='tab-row'>
	<div onclick='changeInventionTab(event)' class='tab-row-tab' id='knowledge-tab'></div>
	<div onclick='changeInventionTab(event)' class='tab-row-tab' id='experimentation-tab'></div>
	<div onclick='changeInventionTab(event)' class='tab-row-tab' id='idea-tab'></div>
	<div onclick='changeInventionTab(event)' class='tab-row-tab' id='itemConstruction-tab'></div>
	<div onclick='changeInventionTab(event)' class='tab-row-tab' id='buildingConstruction-tab'></div>
</div>
<div class='tab-content' id='knowledge-content'>
	<div class='normal-container'>
		<div class='title'>Knowledge</div>
	</div>
	<c:if test="${hasKnowledge!=true}">
		<div class='normal-container'>
			You really don't know anything yet..heh.
		</div>
	</c:if>
	<c:if test="${hasKnowledge==true}">
		<div class='normal-container'>
			<jsp:include page="ajax_invention_knowledgepart.jsp"/>
		</div>
	</c:if>
</div>
<div class='tab-content' id='experimentation-content'>
	<div class='normal-container'>
		<div class='title'>Experimentation</div>
	</div>
	<c:if test="${hasExperimentItems==true}">
		<div class='normal-container big-link-list'>
			<a class='big-link' onclick='doExperiment(event)'>Begin Experiments</a>
		</div>	
	</c:if>
	<div class='normal-container'>
		<c:if test="${hasExperimentItems!=true}">
			There is nothing available to you that you can experiment with.
		</c:if>
		<c:if test="${hasExperimentItems==true}">
			<c:forEach var="item" items="${availableItems}">
				<div class='experiment-available-item-container'>
					<input type='checkbox' id='experimentItem(${item.id})'/>
					${item.html}
				</div>
			</c:forEach>
		</c:if>
	</div>
	
</div>
<div class='tab-content' id='idea-content'>
	<div class='normal-container'>
		<div class='title'>Ideas</div>
	</div>
	<div class='normal-container'>
		<c:if test="${hasIdeas!=true}">
			You don't have any ideas at the moment.
		</c:if>
		<c:if test="${hasIdeas==true}">
			<c:forEach var="idea" items="${ideas}">
				<div class='idea-container'>
					<a onclick='doCreatePrototype(event, ${idea.id}, "<c:out value="${idea.name}"/>");'><img src='${idea.iconUrl}' border=0/> ${idea.name}</a>
				</div>
			</c:forEach>
		</c:if>
		
	</div>
</div>
<div class='tab-content' id='itemConstruction-content'>
	<div class='normal-container'>
		<div class='title'>Object Construction</div>
	</div>
	<div class='normal-container'>
		<c:if test="${hasConstructItemSkills!=true}">
			You don't have any skills that allow you to construct things at the moment.
		</c:if>
		<c:if test="${hasConstructItemSkills==true}">
			<c:forEach var="skill" items="${constructItemSkills}">
				<div class='skill-container'>
					<a onclick='doConstructItemSkill(event, ${skill.id}, "<c:out value="${skill.name}"/>");'><img src='${skill.iconUrl}' border=0/> ${skill.name}</a>
				</div>
			</c:forEach>
		</c:if>
		
	</div>
</div>
<div class='tab-content' id='buildingConstruction-content'>
	<div class='normal-container'>
		<div class='title'>Building Construction</div>
	</div>
	<div class='normal-container'>
		<c:if test="${hasConstructBuildingSkills!=true}">
			You don't have any skills that allow you to build structures at the moment.
		</c:if>
		
	</div>
</div>