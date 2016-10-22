<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<div>
	<h4>Global Premium Membership Token Exchange</h4> 
	<p>This is a special system that is ONLY for buying and selling Initium Premium Membership tokens. Use gold to always buy at the lowest price quickly and easily.</p>
	
	<c:forEach var="item" items="${items}">
		${item}
	</c:forEach>
</div>