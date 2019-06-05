<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<style>
.selectable
{
	position:relative;
}

.selected-character
{
    border: 3px solid #00FF00;
    border-radius: 8px;
    background-color: rgba(0,255,0,0.2);
}
.character-widget-simple-mode
{
	padding: 13px;
}
.boHighlight
{
	color:#FFFFFF;
}
</style>
<div id='pet-food-header' style='background-color: #181715;z-index:2;'>
	<div>
		<img class='item-icon-img-${petId}' src='${petIcon}'/>
	</div>
	<div class='main-item-subnote'>Feeding up to <span id='petFoodMaxKg' class='boHighlight'>${petFoodMax}</span> kg of food</div>
	
	<h3>Viable pet food nearby</h3>
	<p><a onclick='petFeedExecuteAll(event, ${petId});'>Feed All</a></p>
</div>

<c:forEach var="item" items="${items}">
	<div class='selectable deletable-Item${item.id}' id='${item.id}'>  
		<!-- <div class='list-item-X' onclick='doDeleteCharacter(event,${character.id},"${character.name}")'>X</div> -->
		<div>
			${item.html}
		</div>
		<a class='character-display-box-overlay' onclick='petFeedExecute(event, ${petId}, ${item.id})'></a>
	</div>
</c:forEach>

<script type='text/javascript'>
$("#pet-food-header").stick_in_parent();
</script>
