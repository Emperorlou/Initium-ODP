<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

<h4>${itemsCount} Nearby Items</h4>

<c:forEach var="item" items="${itemsToShow}">
	${item}
</c:forEach> 