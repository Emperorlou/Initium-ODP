<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<div>
	<div class='normal-container'>
		<h4>Cosmetics and Customization Store</h4> 
		<p>This store allows you to change the cosmetics of an item.</p>
	</div>
	
	
	<span class='boldbox self-center' style='display:inline-block'>
		<p>You currently have <span style='font-size:20px'><c:out value="${totalDonations}"/></span> in available donation credit. <br>
		<a onclick='viewProfile()'>Click here</a> to get more donation credit.</p>
		<br>
		<c:if test="${noItem==false}">
			<h4>Item to customize: ${itemRendered}</h4>
		</c:if>
		<c:if test="${noItem}">
			<div class='highlightbox-red'>
			No item currently selected. To select an item to customize, inspect an item in your inventory and click on the Customize link in the top left corner of the item's popup.
			</div>
		</c:if>
		<c:if test="${userVerified }">
			<br>
			<div class='highlightbox-red'>
				Your email address is not verified and this could be a problem. We use the email you have associated
				to your account for the correspondence between you and the content developer who is processing
				your order. 
				<br>
 				You should have received a verification email when you signed up. If you haven't, you can <a onclick='resendVerificationEmail()'>click here to resend the verification email</a>.<br>
				If you wish to change your email address, you can <a onclick='changeEmailAddress(&quot;"+common.getUser().getProperty("email")+"&quot;)'>do that here</a>.
				<br>
				If you're using a throwaway account, please convert it to a regular account first.
 
			</div>
		</c:if>
	</span>
	<br><br>

	<c:if test="${error != null}">
		<center><h1>Customize an Item</h1></center>
		<div class='highlightbox-red'>${error}</div>
	</c:if>	

	<c:if test="${error == null}">	
		<div class='normal-container'>	
			<div class='tab-row'>
				<div id='rename-item-custom-tab-button' class='tab-row-tab' onclick='focusTab("rename-item-custom")' style='height:inherit; width:inherit;'>Rename/Flavor</div>
				<div id='featured-deals-tab-button' class='tab-row-tab tab-selected' onclick='focusTab("featured-deals")' style='height:inherit; width:inherit;'>Reskin</div>
				<div id='all-cosmetics-tab-button' class='tab-row-tab' onclick='focusTab("all-cosmetics")' style='height:inherit; width:inherit;'>All Cosmetics</div>
			</div>
	
			<div class='tab-pane' id='rename-item-custom' style='display:none'>
			
				<div class='boldbox'>
					<h4>Custom Item - Name and Flavor Text</h4>
					<div>
						<h5>500 credits</h5>
					</div>
				
					<div class='self-center' style='display:inline-block'>
						<div>
							<i>Item Preview</i>
						</div>
						<div id='custom-nameflavor-preview' class='boldbox' style='display:inline-block; width:320px; background-color:#000000;'>
					
						</div>
					
						<!--Here we want to load the preview window -->
						<div class='boldbox' style='display:inline-block; width:320px;vertical-align:top;'>
							<h5>Specify new name/flavor</h5>
							<input id='custom-nameflavor-itemname' type='text' placeholder='New item name' style='width:95%;margin:5px'/><br>
							<textarea id='custom-nameflavor-flavor' placeholder='New item flavor text' style='width:95%; height:200px; margin:5px;'></textarea>
						</div>
					</div>
					
					<div class='paragraph' style='margin:10px; text-align:right;'>
						<button onclick='orderInstantNameFlavorCustomization(event, ${itemId}, $("#custom-nameflavor-itemname").val(), $("#custom-nameflavor-flavor").val());'>Order Now!</button>
					</div>
				</div>
			
			</div>
			
			<div class='tab-pane' id='featured-deals'>
				<h2>Compatible Skins</h2>
				
				<c:if test="${noItem == true}">
					<p>You have to select an item before you can buy a skin for it. Click on an item and then go to the Customize link at the top of the window that pops up.</p>
				</c:if>

				<c:if test="${noItem==false}">				
					<c:if test="${isBuyablesAvailable == false}"><p>Sorry! There are no compatible customizations for this item in the store. Let a content dev know and they might be able to add some!</p></c:if>
					<c:forEach var="category" items="${buyables}">
						<h5>${category.key}</h5>
						<div class='custom-store-list-container'>
							<c:forEach var="item" items="${category.value}">
								<div class='custom-store-item-container custom-store-${item.rarity}-bg'>
									<div class='custom-store-large-image-preview' id='large-hover-img-${item.id}' style='display:none'>
										<c:if test="${item.largeImage == null || item.largeImage == ''}">
											<p>No large image available.</p>
										</c:if>
										<c:if test="${item.largeImage != null && item.largeImage != ''}">
											<i>Large image preview</i>
											<img src='${item.largeImage}'/>
										</c:if>
									</div>
									<div onmouseenter='$("#large-hover-img-${item.id}").show();' onmouseleave='$("#large-hover-img-${item.id}").hide();' class='custom-store-item-image' style='background-image:url(${item.icon})'>
										<div class='custom-store-item-image-overlay' style='background-image: url(${item.effectOverlay}); filter: brightness(${item.effectOverlayBrightness});'></div>										
									</div>
									<div class='custom-store-item-cost'>${item.costFormatted}</div>
									<button class='custom-store-item-buybutton' onclick='orderCustomStoreItem(event, ${itemId}, ${item.id})'>Buy</button>
									<c:if test="${item.expiryDateFormatted != null}">
										<div class='custom-store-item-expiry'>${item.expiryDateFormatted} left</div>
									</c:if> 
								</div>
							</c:forEach>
						</div>
					</c:forEach>
				</c:if>
			</div>
			
			<div class='tab-pane' id='all-cosmetics' style='display:none;'>
				<h2>All Skins</h2>
				<p>To apply one of these skins to an item, click on the item and then click the Customize link at the top.</p>
				<c:forEach var="category" items="${allBuyables}">
					<h5>${category.key}</h5>
					<div class='custom-store-list-container'>
						<c:forEach var="item" items="${category.value}">
							<div class='custom-store-item-container custom-store-${item.rarity}-bg'>
								<div class='custom-store-large-image-preview' id='large-hover-img2-${item.id}' style='display:none'>
									<c:if test="${item.largeImage == null || item.largeImage == ''}">
										<p>No large image available.</p>
									</c:if>
									<c:if test="${item.largeImage != null && item.largeImage != ''}">
										<i>Large image preview</i>
										<img src='${item.largeImage}'/>
									</c:if>
								</div>
								<div onmouseenter='$("#large-hover-img2-${item.id}").show();' onmouseleave='$("#large-hover-img2-${item.id}").hide();' class='custom-store-item-image' style='background-image:url(${item.icon})'></div>
								<div class='custom-store-item-cost'>${item.costFormatted}</div>
								<c:if test="${item.expiryDateFormatted != null}">
									<div class='custom-store-item-expiry'>${item.expiryDateFormatted} left</div>
								</c:if> 
							</div>
						</c:forEach>
					</div>
				</c:forEach>
			</div>
			
			
		</div>
	</c:if>
		
				
</div>

<script type="text/javascript">
	function focusTab(tabId)
	{
		$(".tab-row-tab").removeClass("tab-selected")
		$(".tab-pane").hide();
		$("#" + tabId + "-tab-button").addClass("tab-selected");
		$("#" + tabId).show();
		localStorage.setItem("customstore_page_tab", tabId);
	}
	
	if (localStorage.getItem("customstore_page_tab") != null)
		focusTab(localStorage.getItem("customstore_page_tab"));
	
	
	
	
	
	$("#custom-nameflavor-preview").load("/odp/viewitemmini?itemId=${itemId}", null, function(){
		// Completed
		$("#item-comparison-link").remove();
		$("#custom-nameflavor-preview [name=itemName]").removeClass().addClass("item-custom");
		
	});
	
	function updateNameFlavorName(){
		$("#custom-nameflavor-preview [name=itemName]").text($(this).val());
	}
	
	function updateNameFlavorDescription(){
		var text = $(this).val();
		
		text = text.replace(/\n/g, "<br>");
		
		$("#custom-nameflavor-preview [name=description]").html(text);
	}
	
	$("#custom-nameflavor-itemname").change(updateNameFlavorName).keyup(updateNameFlavorName);
	$("#custom-nameflavor-flavor").change(updateNameFlavorDescription).keyup(updateNameFlavorDescription);
	
</script>
