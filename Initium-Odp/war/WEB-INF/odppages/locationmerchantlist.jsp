<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>

<div style='height:20px;'>
	<p><a onclick='viewExchange()' style='float:right;'>View the Premium Membership Token exchange</a></p>
</div>
<div class='normal-container'>
	<h4>${shopCount} Nearby Merchants</h4>
	<c:forEach var="shop" items="${shopsToShow}">
		${shop}
	</c:forEach> 
</div>