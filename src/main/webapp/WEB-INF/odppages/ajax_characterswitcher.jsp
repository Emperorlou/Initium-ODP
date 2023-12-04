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

.popout-character
{
	display:table-cell;
	font-size:20px;
	vertical-align:middle;
	color:#FFFFFF;
	padding-right:5px;
}


</style>
<c:forEach var="character" items="${characters}">
	<div class='selectable'>
		<a href='/main.jsp?char=${character.urlSafeKey}' target='_blank' class='popout-character'>↸</a>
		<div class='list-item'>
			${character.html}
		</div>
		<div class='list-item-X' onclick='doDeleteCharacter(event,${character.id},"${character.name}")'>X</div>
		<c:if test="${character.id==currentCharacterId }">
			<a class='character-display-box-overlay selected-character' onclick='switchCharacter(event, ${character.id})'></a>
		</c:if>
		<c:if test="${character.id!=currentCharacterId }">
			<a class='character-display-box-overlay' onclick='switchCharacter(event, ${character.id})'></a>
		</c:if>
	</div>
</c:forEach>
<div class='selectable'>
	<div id="newui" class="character-display-box" style='position:relative; filter:opacity(0.3);width:100%;'>
		<a class="" rel="" minitip="Create new character">
			<div class="avatar-equip-backing v3-window3" style="background-color:none;border-width:6px;margin-left:24px;">
				<div class="avatar-equip-cloak" style="background-image:url(&quot;https://initium-resources.appspot.com/images/ui/newui/avatar-silhouette-male1.png&quot;);font-size: 24px;text-align: center;">+</div>
			</div>
		</a>
		<div class="character-display-box-info">	
			<a class="hint hasTooltip" rel="#profile" style="">&nbsp;</a>		
			<div id="hitpointsBar" style="position:relative; display:block; background-color:#333333; width:100px; height:12px;text-align:left">								</div>
		</div>
		<c:if test="${hasPremium==true}">
		<a class='character-display-box-overlay' onclick='createNewCharacter()'></a>
		</c:if>
		<c:if test="${hasPremium!=true}">
		<a class='character-display-box-overlay' onclick='clearMakeIntoPopup(); createUpgradeToPremiumWindow()'></a>
		</c:if>
	</div>
</div>	
