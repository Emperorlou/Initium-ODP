<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<script type="text/javascript">
	function focusPremiumToken()
	{
		$("#premium-token-tab").addClass("tab-selected");
		$("#chipped-token-tab").removeClass("tab-selected")
		$("#premium-membership-token-pane").addClass("tab-selected").show();		
		$("#chipped-token-pane").removeClass("tab-selected").hide();
		
	}
	
	function focusChippedToken()
	{
		$("#premium-token-tab").removeClass("tab-selected");
		$("#chipped-token-tab").addClass("tab-selected")
		$("#premium-membership-token-pane").hide();		
		$("#chipped-token-pane").show();		
	}
</script>
<div>
	<h4>Global Premium Membership Token Exchange</h4> 
	<p>This is a special system that is ONLY for buying and selling Initium Premium Membership tokens and Chipped Tokens. Use gold to always buy at the lowest price quickly and easily.</p>
	<div class='tab-row'>
		<div id='premium-token-tab' class='tab-row-tab tab-selected' onclick='focusPremiumToken()' style='height:inherit; width:inherit;'>Initium Premium Membership</div>
		<div id='chipped-token-tab' class='tab-row-tab' onclick='focusChippedToken()' style='height:inherit; width:inherit;'>Chipped Tokens</div>
	</div>
	<div id='premium-membership-token-pane'>
	<c:forEach var="item" items="${premiumTokens}">
		${item}
	</c:forEach>
	</div>
	<div id='chipped-token-pane' style='display:none;'>
	<c:forEach var="item" items="${chippedTokens}">
		${item}
	</c:forEach>
	</div>
</div>