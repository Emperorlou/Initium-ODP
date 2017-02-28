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
</style>
<script type='text/javascript'>
var crSelectedRequirementId = null;
function selectRequirement(event, requirementId)
{
	crSelectedRequirementId = requirementId;
	$(".confirm-requirements-requirement").removeClass("confirm-requirements-selected");
	$(event.target).closest(".confirm-requirements-requirement").addClass("confirm-requirements-selected");
	doCommand(event, "ConfirmRequirementsUpdate", {id:requirementId});	
}

function selectItem(event, itemId)
{
	$("#itemForRequirement"+crSelectedRequirementId).val(itemId);
	var itemPanelForRequirement = $("#itemHtmlForRequirement"+crSelectedRequirementId);
	var itemVisual = $(event.target).closest(".confirm-requirements-item-candidate").find(".itemToSelect");
	itemVisual.detach();
	itemPanelForRequirement.append(itemVisual);
}

function unselectItem(event)
{
	
}
</script>
<div class='main-splitScreen'>
<c:forEach var="requirementCategory" items="${formattedRequirements}">
	<h4><c:out value="${requirementCategory.name}"/></h4>
	<c:forEach var="requirement" items="${requirementCategory.list}">
		<div class='hiddenTooltip' id='requirementHelp-${requirement.id }'><h4>${requirement.name}</h4></h2><c:out value="${requirement.description}"/></div>
		<input type='hidden' id='itemForRequirement${requirement.id}' name='itemForRequirement${requirement.id}'/>
		<div onclick='selectRequirement(event, ${requirement.id})' class='confirm-requirements-entry confirm-requirements-requirement'>
			<div class='hint questionmark' rel='#requirementHelp-${requirement.id}'>?</div><div><c:out value="${requirement.name}"/></div>
			<div id='itemHtmlForRequirement${requirement.id}'></div>
		</div>
	</c:forEach>
</c:forEach>
<br>
<div class='center'>
	<a class='big-link center'>Start Prototyping</a>
</div>

</div>

<div class='main-splitScreen'>
<h4>Available Items</h4>
<div id='item-candidates'>
<-- Select a slot on the left to begin
</div>
</div> 
