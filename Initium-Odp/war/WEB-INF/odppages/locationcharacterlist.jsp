<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<h4>${charCount} Nearby Characters</h4>
<div class='normal-container'>
	<c:forEach var="chr" items="${charToShow}">
		${chr}
	</c:forEach> 
</div>