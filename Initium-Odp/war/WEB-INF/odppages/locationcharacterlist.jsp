<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<div class='normal-container'>
	<h4>${charCount} Nearby Characters</h4>
	
	<c:forEach var="char" items="${charToShow}">
		${char}
	</c:forEach> 
</div>