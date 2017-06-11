<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<div class="item-popup">
	<input type='hidden' id='popupItemId' value='${itemId}'/>
	<c:if test="${isItemOwner}"><a href='#' onclick='customizeItemOrderPage(${itemId})' style='float:left'>Customize</a></c:if>
	${itemHtml}
	<c:if test="${showChippedTokenUI}">
	<div class='normal-container'>
		<p>
		<a onclick='combineChippedTokens(event, ${itemId})'>Make Premium Token</a><br>
		If this stack of Chipped Tokens has a quantity of at least 100, clicking the above link will turn 100 of these tokens into an Initium Premium Membership token.
		</p>
	</div>
	</c:if>
	<c:if test="${showPremiumTokenUI}">
	<div class='normal-container'>
		<p>
		<a href='ServletUserControl?type=usePremiumToken&itemId=${itemId}'>Deposit to Account</a> <br>
		Click this link to use this token on yourself. This will give your account premium membership. 
		If you already have a premium membership, 5 dollars will be added towards your donation credit 
		which you can use to gift premium to someone else, order a customization, or to create another 
		token like this at a later date.
		</p>
	
		<p>
		<a onclick='splitPremiumToken(event, ${itemId})'>Make 100 Chipped Tokens</a> <br>
		Clicking this option will give you a single stack of 100 Chipped Tokens in your inventory.
		You can later turn those tokens back into an Initium Premium Membership token, or trade the Chipped Tokens like they were any other item.
		</p>
	</div>
	</c:if>
	
	<c:if test="${comparisons != null}">
	<p id='item-comparison-link'><a onclick='$("#item-comparisons").show(); $("#item-comparison-link").hide();'>Compare with existing equipment</a></p>
	<div id='item-comparisons' style='display:none;'>
		<c:forEach items="${comparisons}" var="comparison">
		<hr/>
		${comparison}
		</c:forEach>
	</div>
	</c:if>
</div>