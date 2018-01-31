<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<div class='boldbox selection-root'>
	<div class="inventory-main-header">
		<span class='paragraph boldbox-right-link'><a onclick='dropAllInventory(event)' title='This will drop everything in your inventory onto the ground. Equipped and vending items will NOT be dropped.'>Drop All</a></span>
		<h4>Your Inventory</h4>
		<div class="main-item-filter">
			<input class="main-item-filter-input" id="filter_invItem" type="text" placeholder="Filter inventory...">
		</div>
		<div class="inventory-main-commands">
			<div class="command-row">
				<label class="command-cell" title="Marks all inventory items for batch operations."><input type="checkbox" class="check-all">Select All</label>
				<a class="command-cell right" title="Drops any items you've selected in your inventory on the ground." onclick="selectedItemsDrop(event, '#invItems .invItem')">Drop Selected</a>
			</div>
			<div class="command-row">
				<a class="command-cell" title="Merge the selected items." onclick="mergeItemStacks(event, '#invItems .invItem')">Merge Items</a>
				<a class="command-cell right" title="Split the slected item." onclick="splitItemStack(event, '#invItems .invItem')">Split Item</a>
			</div>
			<div class="command-row">
				<a class="command-cell" title="Select 2 containers and click this link to quickly swap the contents of one container for the other. One container must be empty." onclick="swapContainers(event, '#invItems .invItem')">Swap Containers</a>
			</div>
		</div>
	</div>
	<div id="invItems" class="selection-list">
		
		<c:if test="${isCarryingCharacters}">
		<div class='main-description'>	
			<h4>Characters</h4>
			<c:forEach var="char" items="${carriedCharacters}">
				${char}
			</c:forEach> 
			<br/>
			<div class='main-item-controls'>
				<a onclick='characterDropAllCharacters(event)'>Put all on ground.</a>
			</div>
		</div>
		</c:if>
		
		<c:forEach var="item" items="${itemList}">
			${item}
		</c:forEach> 
	</div>
</div>