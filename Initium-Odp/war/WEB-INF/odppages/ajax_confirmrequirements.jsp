<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<style>
	.confirm-requirements-entry
	{
		padding:10px;
		border:2px solid #777777;
		border-radius:5px;
		cursor:pointer;
		margin:3px;
	}
	.confirm-requirements-entry:hover
	{
		border:2px solid #FFFFFF;
	}
	.confirm-requirements-entry .questionmark
	{
		position:relative;
		top:-14px;
		right:-14px;
		float:right;
		font-size:20px;
		color:#FFFFFF;
		padding:10px;
		
	}
	.confirm-requirements-requirement
	{
		color:#AAAAAA;
	}
	.confirm-requirements-item-candidate
	{
	}
	.confirm-requirements-item-candidate .selectarea
	{
		display:inline-block;
		width:32px;
		height:32px;
		background-color:#444444;
		cursor:pointer;
	}
	.confirm-requirements-selected
	{
		border-color:#00FF00 !important;
		background-color:rgba(0,255,0,0.1);
	}
	.selectarea
	{
		position:relative;
		vertical-align:middle;
	}
	.selectarea .X
	{
		position:absolute;
		top:0px;
		left:0px;
		display:none;
		width:32px;
		height:32px;
		text-align:center;
		margin-top: 6px;
		margin-left: 2px;
		font-size: 21px;
		font-size:20px;
	}
	.confirm-requirements-selected .selectarea .X
	{
		display:block;
		
	}
</style>
<script type='text/javascript'>

</script>
<div class='center' style='margin-bottom:15px'>
	<c:if test="${type=='IdeaToPrototype'}">
		<a id='confirmRequirementsButton-${repsUniqueId}' data='startbutton-${ideaId}' onclick='doCreatePrototype(event, ${ideaId}, "<c:out value="${ideaName}"/>", "${userRequestId}", "${repsUniqueId}", null, ${selected2DTileX}, ${selected2DTileY}, true)' class='v3-main-button-half'>Start Prototyping</a>
	</c:if>
	<c:if test="${type=='ConstructItemSkill'}">
		<a id='confirmRequirementsButton-${repsUniqueId}' data='startbutton-${skillId}' onclick='doConstructItemSkill(event, ${skillId}, "<c:out value="${skillName}"/>", "${userRequestId}", "${repsUniqueId}", null, ${selected2DTileX}, ${selected2DTileY}, true)' class='v3-main-button-half'>Begin</a>
	</c:if>
	<c:if test="${type=='CollectCollectable'}">
		<a id='confirmRequirementsButton-${repsUniqueId}' data='startbutton-${collectableId}' onclick='doCollectCollectable(event, ${collectableId}, "${userRequestId}", "${repsUniqueId}")' class='v3-main-button-half'>Begin</a>
	</c:if>
	<c:if test="${type=='GenericCommand'}">
		<a id='confirmRequirementsButton-${repsUniqueId}' onclick='doCommand(event, "${commandName}", ${commandParameters}, null, "${userRequestId}", "${repsUniqueId}")' class='v3-main-button-half'>Okay</a>
	</c:if>
	<c:if test="${type=='GenericLongOperation'}">
		<a id='confirmRequirementsButton-${repsUniqueId}' onclick='longOperation(event, "${longOperationUrl}", null, null, "${userRequestId}", "${repsUniqueId}")' class='v3-main-button-half'>Okay</a>
	</c:if>
</div>

<c:if test="${maxReps!=null}">
How many times do you want to do this: <input type='number' id='repetitionCount' min='1' max='${maxReps}' uniqueId='${repsUniqueId}'/>
<p style="display: inline-block;">
	<a onclick="$('#repetitionCount').val('10000')" style="font-size: 25px;">&#8734;</a>
</p>
<br> 
</c:if>



	<div id='requirement-categories' class='main-splitScreen'>
	<c:forEach var="requirementCategory" items="${formattedRequirements}">
		<h4><c:out value="${requirementCategory.name}"/></h4>
		<c:forEach var="requirement" items="${requirementCategory.list}">
			<div class='hiddenTooltip' id='requirementHelp-${requirement.slotName }'><h4>${requirement.name}</h4></h2><c:out value="${requirement.description}"/></div>
			<div id='requirement-container-${requirement.slotName}' slotName='${requirement.slotName}' onclick='selectRequirement(event, "${requirement.slotName}", "${requirement.gerKeyList}")' class='confirm-requirements-entry confirm-requirements-requirement'>
				<div class='hint questionmark' rel='#requirementHelp-${requirement.slotName}'>?</div><div><c:out value="${requirement.name}"/></div>
				<div id='itemHtmlForRequirement${requirement.slotName}'>${requirement.defaultItem}</div>
			</div>
		</c:forEach>
	</c:forEach>
	<br>
	
	</div>

	<c:if test="${hideAvailableItemsPanel!=true}">
		<div class='main-splitScreen'>
		<h4>Available Items</h4>
		<p style='float:right;margin:0px;'><a onclick='selectAll(event)'>Add All</a></p>
		<div id='item-candidates'>
		<-- Select a slot on the left to begin
		</div>
	</c:if>
</div> 
<c:if test="${type=='IdeaToPrototype' && autoStart==true}">
	<script type='text/javascript'>
		$("[data='startbutton-${ideaId}']").click();
		closePagePopup();
	</script>
</c:if>
<c:if test="${type=='ConstructItemSkill' && autoStart==true}">
	<script type='text/javascript'>
		$("[data='startbutton-${skillId}']").click();
		closePagePopup();
	</script>
</c:if>
