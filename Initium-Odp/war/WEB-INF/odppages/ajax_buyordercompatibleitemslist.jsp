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
<div class='main-item-subnote'>Selling up to <span class='boHighlight'>${buyOrderMaxQuantity}</span> units of <span class='boHighlight'>${buyOrderName}</span> at <span class='boHighlight'>${buyOrderValueEach}g</span> each</div>
<c:forEach var="item" items="${items}">
	<div class='selectable'>
		<!-- <div class='list-item-X' onclick='doDeleteCharacter(event,${character.id},"${character.name}")'>X</div> -->
		<div>
			${item.html}
		</div>
		<a class='character-display-box-overlay' onclick='storeBuyOrderExecute(event, ${buyOrderId}, ${item.id}, ${buyOrderMaxQuantity}, ${buyOrderValueEach})'></a>
	</div>
</c:forEach>
