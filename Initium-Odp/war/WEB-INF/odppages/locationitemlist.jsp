<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

<h4>${itemsCount} Nearby Items</h4>
<c:if test="${itemsCount=='50+'}">
<p>There are more items here that are not shown because they are buried. Only the first 50 items in a location can be seen at any given time.</p>
</c:if>
${collectablesToShow}
<c:forEach var="item" items="${itemsToShow}">
	${item}
</c:forEach> 