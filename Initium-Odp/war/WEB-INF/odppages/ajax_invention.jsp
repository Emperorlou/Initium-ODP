<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<div class='highlightbox-red'>Please be aware that the invention system is still very new at this point and not particularly useful YET.</div>
<div class='tab-row normal-container backdrop1b'>
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
					<input type='checkbox' class='experiment-item-checkbox' id='${item.id}'/>
					${item.html}
				</div>
			</c:forEach>
		</c:if>
	</div>
	
</div>
<div class='tab-content' id='idea-content'>
	<div class='normal-container'>
		<div class='title'>Ideas</div>
		<div class='prototype-queue-label'></div>
	</div>
	<div class='normal-container'>
		<c:if test="${hasIdeas!=true}">
			You don't have any ideas at the moment.
		</c:if>
		<c:if test="${hasIdeas==true}">
			<c:forEach var="idea" items="${ideas}">
				<div class='idea-container' id='idea-id-${idea.id}'>
					<div class='hiddenTooltip' id='idea-popup-1-${idea.id}'>
						<h4>${idea.name}</h4>
						<p>${idea.description}</p>
						<div class='paragraph generic-field'>Time to develop: <span class='generic-field-value'>${idea.speed} seconds</span></div>
						<p>
							<a onclick='doCreatePrototype(event, ${idea.id}, "<c:out value="${idea.name}"/>");'>Create prototype now</a>
						</p>
					</div>
					<a class='instant-start-skill-button' onclick="doCreatePrototype(event, ${idea.id}, '<c:out value="${idea.name}"/>', null, null, true);">
						<img src="https://initium-resources.appspot.com/images/ui/begin-icon1.png">
					</a>					
					<a class='hint' rel='#idea-popup-1-${idea.id}'><span class='skill-icon' style='background-image: url(${idea.icon});'></span> ${idea.name}</a>
				</div>
			</c:forEach>
		</c:if>
		
	</div>
</div>
<div class='tab-content' id='itemConstruction-content'>
	<div class='normal-container'>
		<div class='title'>Object Construction</div>
		<div class='skill-queue-label'></div>
	</div>
	<div class='normal-container'>
		<c:if test="${hasConstructItemSkills!=true}">
			You don't have any skills that allow you to construct things at the moment.
		</c:if>
		<c:if test="${hasConstructItemSkills==true}">
			<c:forEach var="skill" items="${constructItemSkills}">
				<div class='skill-container' id='skill-id-${skill.id}'>
					<div class='hiddenTooltip' id='skill-popup-1-${skill.id}'>
						<h4 id='skill-popup-title-name-${skill.id}'>${skill.name}</h4>
						<p>${skill.description}</p>
						<div class='paragraph generic-field'>Construction time: <span class='generic-field-value'>${skill.speed} seconds</span></div>
						<p>
							<a onclick='doConstructItemSkill(event, ${skill.id}, "<c:out value="${skill.name}"/>");'>Create this now</a>
						</p>
						<p>
							<a onclick='doRenameConstructItemSkill(event, "<c:out value="${skill.name}"/>", ${skill.id})'>Rename this skill</a>
						</p>
						<p>
							<a onclick='doForgetConstructItemSkill(event, "<c:out value="${skill.name}"/>", ${skill.id})'>Forget this skill</a>
						</p>
						
					</div>
					<div class='generic-itemlike-container' style='vertical-align:middle;'>
						<a class='instant-start-skill-button' onclick="doConstructItemSkill(event, ${skill.id}, '<c:out value="${skill.name}"/>', null, null, true);">
							<img src="https://initium-resources.appspot.com/images/ui/begin-icon1.png">
						</a>					
						<a class='hint' rel='#skill-popup-1-${skill.id}'><span class='skill-icon' style='background-image: url(${skill.icon});'></span> 
							<div class='generic-itemlike-name-container'>
								<div class='generic-itemlike-name' id='skill-name-${skill.id}'>${skill.name}</div>
								<div class='generic-itemlike-lowerline'>${skill.itemClass}</div>
							</div>
						</a>
					</div>
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
	
	localStorage.setItem("invention_page_tab", code);
}

// Kinda hacky but it's short and it'll prolly work
var lastTab = localStorage.getItem("invention_page_tab");
if(lastTab!=null)
{
	changeInventionTab({target:$("#"+lastTab+"-tab")[0]});
}

function refreshQueueCount() {
	const queueCount = (skillQueue == null ? 0 : skillQueue.length) + (prototypeQueue == null ? 0 : prototypeQueue.length);
	$(".skill-queue-label,.prototype-queue-label").text((queueCount) + " queued actions");
}
refreshQueueCount();
</script>
