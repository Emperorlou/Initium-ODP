<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<div>
	<div class='normal-container'>
		<h4>Global Premium Membership Token Exchange</h4> 
		<p>This is a special system that is ONLY for buying and selling Initium Premium Membership tokens and Chipped Tokens. Use gold to always buy at the lowest price quickly and easily.</p>
	</div>
	<div class='normal-container'>	
		<div class='tab-row'>
			<div id='premium-token-tab' class='tab-row-tab tab-selected' onclick='focusPremiumToken()' style='height:inherit; width:inherit;'>Initium Premium Membership</div>
			<div id='chipped-token-tab' class='tab-row-tab' onclick='focusChippedToken()' style='height:inherit; width:inherit;'>Chipped Tokens</div>
		</div>
		<div id='premium-membership-token-pane'>
		<div class='main-splitScreen'>
		<h2>Buyers</h2>
		<c:forEach var="item" items="${premiumTokenBuyOrders}">
			${item}
		</c:forEach>
		</div>
		<div class='main-splitScreen'>
		<h2>Sellers</h2>
		<c:forEach var="item" items="${premiumTokens}">
			${item}
		</c:forEach>
		</div>
		</div>
		<div id='chipped-token-pane' style='display:none;'>

		<div class='main-splitScreen'>
		<h2>Buyers</h2>
		<c:forEach var="item" items="${chippedTokenBuyOrders}">
			${item}
		</c:forEach>
		</div>

		<div class='main-splitScreen'>
		<h2>Sellers</h2>
		<c:forEach var="item" items="${chippedTokens}">
			${item}
		</c:forEach>
		</div>

		</div>
	</div>		
</div>

<script type="text/javascript">
	function focusPremiumToken()
	{
		$("#premium-token-tab").addClass("tab-selected");
		$("#chipped-token-tab").removeClass("tab-selected")
		$("#premium-membership-token-pane").addClass("tab-selected").show();		
		$("#chipped-token-pane").removeClass("tab-selected").hide();
		localStorage.setItem("exchange_page_tab", "Premium");
	}
	
	function focusChippedToken()
	{
		$("#premium-token-tab").removeClass("tab-selected");
		$("#chipped-token-tab").addClass("tab-selected")
		$("#premium-membership-token-pane").hide();		
		$("#chipped-token-pane").show();
		localStorage.setItem("exchange_page_tab", "Chipped");
	}
	
	if (localStorage.getItem("exchange_page_tab") == "Chipped")
		focusChippedToken();
</script>
