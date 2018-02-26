<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<h4>${charCount} Nearby Characters</h4>
<div class='normal-container'>
	<c:forEach var="char" items="${charToShow}">
		${char}
	</c:forEach> 
</div>