<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

<h3>Training Quest Lines</h3>

<c:forEach var="item" items="${list}">
	<c:if test="${item.questDefId==null}">
		<div class='quest-line-container'>
			<a class='quest-banner ${item.completeCssClass} standard-button-highlight ' onclick='systemMessage("Quest line not yet implemented. Soon though!")'><img src='${item.bannerUrl}'/></a>
		</div>
	</c:if>
	<c:if test="${item.questDefId!=null}">
		<div class='quest-line-container'>
			<a class='quest-banner ${item.completeCssClass} standard-button-highlight ' onclick='beginNoobQuests(${item.questDefId})'><img src='${item.bannerUrl}'/></a>
		</div>
	</c:if>
</c:forEach> 
