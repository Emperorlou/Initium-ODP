<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<script type='text/javascript'>
function clearQuestCompleteEffect()
{
	clearTimeout(window.questCompleteEffectTimer);
	if ($("#questCompleteEffect img").length>0)
		$("#questCompleteEffect img")[0].src='';
	$("#questCompleteEffect").remove();
}
function doQuestCompleteEffect()
{
	$("#quest-complete-label").remove();
	clearQuestCompleteEffect();
	
	var html = "<div id='questCompleteEffect' style='position: fixed;overflow: visible;mix-blend-mode: color-dodge;width: 0px;height: 0px;left: 50%;top: 30%;z-index: 10000000;'>"+
	"<img src='https://imgur.com/gCubaa9.gif' style='position:absolute;margin-left: -250px;margin-top: 0px;float: left;pointer-events: none;transform: scale(2);'>"+
	"</div>";
	$("body").prepend(html);
	
	var completeHtml = "<h3 id='quest-complete-label'>Complete!</h3>";
	setTimeout(function(){$(".quest-window").append(completeHtml);}, 500);
	window.questCompleteEffectTimer = setTimeout(clearQuestCompleteEffect, 5000);

	$("#questlist-questkey-${questDefKey}").addClass("quest-complete");
}
</script>

<h2><c:out value="${name}"/></h2>
${description}

<c:if test="${questComplete==true}">
	<script type='text/javascript'>
	doQuestCompleteEffect();
	</script>
</c:if>