<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<div class='boldbox'>
	<div class="inventory-main-header">
		<h4>Your Equipment</h4>
		<div class="inventory-main-commands">
			<div class="command-row">
				<a class="command-cell left" title="Unequips all items currently on your character." onclick="characterUnequipAll(event)">Unequip All</a>&nbsp;
			</div>
		</div>
	</div>
	<c:forEach var="slot" items="${equipList}">
		${slot}
	</c:forEach> 
</div>