window.popupsNum = 0;
window.popupsOpen = 0;
window.popupsArray = new Array();

window.singlePostFormSubmitted = false;

var notifyHandler = null;
// Case insensitive Contains selector.
jQuery.expr[':'].ContainsI = function(a, i, m) { return jQuery(a).text().toUpperCase().indexOf(m[3].toUpperCase()) >= 0; };

$(window).ready(function(e){
	$(".single-post-form").submit(function(e){
		if (window.singlePostFormSubmitted)
			e.preventDefault();
		else
			window.singlePostFormSubmitted = true;
	});
	
	$(window).resize(function() {
		expandpopupMessage();
	});

	
	$(".boldBoxCollapsed > h4").click(function(){
		$(this).parent().toggleClass("boldBoxCollapsed");
	});
	
	// For all inputs under the body: If a text box is selected, don't allow shortcut keys to process (prevent default)
	$("body").on("keyup", "input", function(event){
		event.stopPropagation();
	});
	
	// Any inputs with the main-item-filter-input class should be named (ID) according to 
	// which div class they will be filtering on.
	$("#page-popup-root").on("input propertychange paste", "input.main-item-filter-input", function(event)
	{
		// If it's the propertychange event, make sure it's the value that changed.
	    if (window.event && event.type == "propertychange" && event.propertyName != "value")
	        return;

	    // Clear any previously set timer before setting a fresh one
	    window.clearTimeout($(this).data("timeout"));
	    $(this).data("timeout", setTimeout(function () {
	    	var selector = "div."+event.currentTarget.id.substring("filter_".length);
			var searchString = $(event.currentTarget).val();

			var conditionSelector = "a.clue:ContainsI('" + searchString + "')";
			var filterItems = $(selector);
			var showItems = filterItems.has(conditionSelector);
			var hideItems = filterItems.not(showItems);
			
			showItems.show().next("br").show();
			hideItems.hide().next("br").hide();
	    }, 500));
	});
	
	// Unfortunately, we have to use event delegation for checkboxes, since it's in popup content.
	$("#page-popup-root").on("click", ".selection-root input:checkbox.check-all", function(event)
	{
		var cb = $(event.currentTarget);
		var selectRoot = cb.parents(".selection-root");
		selectRoot.find("input:checkbox.check-group").prop( {checked:cb.prop("checked"), indeterminate:false});
		selectRoot.find(".selection-list input:checkbox").prop("checked", cb.prop("checked"));
		selectRoot.find(".selection-list .main-item").toggleClass("main-item-selected", cb.prop("checked"));
	});
	
	$("#page-popup-root").on("click", ".selection-root input:checkbox.check-group", function(event)
	{
		var cb = $(event.currentTarget);
		var groupId = cb.attr("ref");
		var groupItems = cb.parents(".selection-root").find(".selection-list #" + groupId + " .main-item");
		groupItems.find("input:checkbox").prop("checked", cb.prop("checked"));
		groupItems.toggleClass("main-item-selected", cb.prop("checked"));
		
		var allItems = cb.parents(".selection-root").find(".main-item");
		var checkedItems = allItems.has("input:checkbox:checked");
		cb.parents(".selection-root").find("input:checkbox.check-all")
			.prop({
				checked:checkedItems.length == allItems.length,
				indeterminate: checkedItems.length > 0 && checkedItems.length != allItems.length
			});
	});
	
	$("#page-popup-root").on("click", ".selection-list input:checkbox", function(event)
	{
		var cb = $(event.currentTarget);
		cb.parent(".main-item").toggleClass("main-item-selected", cb.prop("checked"));
		
		var itemsDiv = cb.parents(".selection-root");
		var invItems = itemsDiv.find(".main-item")
		var checkedItems = invItems.find("input:checkbox:checked");
		itemsDiv.find("input:checkbox.check-all")
			.prop({
				checked:checkedItems.length == invItems.length,
				indeterminate: checkedItems.length > 0 && checkedItems.length != invItems.length
				});
		
		var group = cb.parents(".selection-group");
		if(group.length > 0)
		{
			var groupName = group.prop("id");
			invItems = group.find(".main-item");
			checkedItems = invItems.find("input:checkbox:checked");
			itemsDiv.find("input:checkbox.check-group[ref="+groupName+"]")
				.prop({
					checked:checkedItems.length == invItems.length,
					indeterminate: checkedItems.length > 0 && checkedItems.length != invItems.length
					});
		}
	});
	
	$("#page-popup-root").on("click", ".selection-list .main-item-container", function(event)
	{
		$(event.currentTarget).parent().find("input:checkbox").click();
	});
	
	$(".main-expandable .main-expandable-title").click(function(){
		$(this).parent().find(".main-expandable-content").show();
		$(this).hide();
	});
	
	// Set the correct image for the header mute button
	if (isSoundEffectsEnabled())
		$("#header-mute").attr("src", "images/ui/sound-button1.png");
	else
		$("#header-mute").attr("src", "images/ui/sound-button1-mute.png");
		

	// When the window gains focus, call the "flagReadMessages" to indicate that the user has now read any unread messages that may have been waiting for him
	$(window).focus(function(){
		flagReadMessages();
	});
	
});

/**
 * This removes the * from the title, (and by extension the 'unread messages' symbol on chrome browsers).
 *  
 * You can safely call this as often as you want.
 */
function flagReadMessages()
{
	if (document.hasFocus())
	{
		if (document.title.indexOf("* ")==0)
		{
			document.title = document.title.substring(2);
		}
	}
}

/**
 * This adds a * to the title of the page (and by extension adds an 'unread messages' symbol on chrome browsers). 
 * 
 * You can safely call this as often as you want.
 */
function flagUnreadMessages()
{
	if (document.hasFocus()==false)
	{
		if (document.title.indexOf("*")<0)
		{
			document.title = "* "+document.title;
		}
	}
}

//Pop up Message
function popupPermanentOverlay(title, content, popupClassOverride) 
{
	if (popupClassOverride==null)
		popupClassOverride = "popup";
	closeAllPopups();
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
    $("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper"><div id="popup_' + popupsNum + '" class="'+popupClassOverride+'"><div id="popup_header_' + popupsNum + '" class="popup_header">' + title + '</div><div id="popup_body_' + popupsNum + '" class="popup_body"><div id="popup_text_' + popupsNum + '" class="popup_text">' + content + '</div></div><div id="popup_footer_' + popupsNum + '" class="popup_footer"></div></div></div></div>');
    expandpopupMessage();
}

function popupMessage(title, content, noBackground) {
	noBackgroundHtml = "";
	if (noBackground==true)
		noBackgroundHtml = 'style="background:none"';
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
    $("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper"><div id="popup_' + popupsNum + '" class="popup" '+noBackgroundHtml+'><div id="popup_header_' + popupsNum + '" class="popup_header">' + title + '</div><div id="popup_body_' + popupsNum + '" class="popup_body"><div id="popup_text_' + popupsNum + '" class="popup_text"><p>' + content + '</p></div></div><div id="popup_footer_' + popupsNum + '" class="popup_footer"><div id="popup_footer_okay_' + popupsNum + '" class="popup_message_okay" unselectable="on" onClick="closepopupMessage(' + popupsNum + ')" title="okay">Okay</div></div></div></div></div>');
    expandpopupMessage();
    enterPopupClose();
    }

$(document).bind("keydown",function(e) 
{
    if (popupsOpen >= 1) 
    {
        if ((e.keyCode == 13) || (e.keyCode == 27)) 
        {
            closepopupMessage(currentPopup());
        }
    }
});

function enterPopupClose() 
{
}

function currentPopup() {
    popupLast = $(".popupWrapperBackground").last();
    popupLastID = popupLast.attr("id");
    popupLastIDNum = popupLastID.split("_");
    return popupLastIDNum[1];
}
function closeAllPopups()
{
    $(".popupWrapperBackground").remove();
    $("#popups").hide();
    window.popupsOpen = 0;
    window.documentBindEnter = false;
}

function closeAllTooltips()
{
	$(".cluetip").hide();
}

function closepopupMessage(popupID) {
    $("#popupWrapperBackground_" + popupID).remove();
	window.popupsOpen = window.popupsOpen-1;
    window.popupsArray[popupID-1] = "no";
    window.documentBindEnter = false;
    if (window.popupsOpen<=0)
		$("#popups").hide();
    else
    	enterPopupClose();
}
function expandpopupMessage() 
{
	var win = $(window);
	var viewportHeight = win.height();
	var viewportWidth = win.width();
	
	$(".popup").each(function(index)
	{
		var popup = $(this);
		var width = popup.width();
		var height = popup.height();
		
		popup.css("left", viewportWidth/2-(width/2)+"px");
		popup.css("top", viewportHeight/2-(height/2)+"px");
	});
	
//    var winHeight = window.innerHeight;
//    var popupWrapperH = winHeight-125;
//    var popupWrapperM = -popupWrapperH/2;
//    var popupTextH = popupWrapperH-100;
//    $(".popupWrapper").css("height", popupWrapperH + "px");
//    $(".popupWrapper").css("margin-top", popupWrapperM + "px");
//    $(".popup_text").css("max-height", popupTextH + "px");
//    var popupM = (-popupWrapperM - ($("#popup_text_" + currentPopup()).height())); console.log(popupM + '\n' + $("#popup_text_" + currentPopup()).height());
//    if ($("#popup_" + currentPopup()).height() < popupTextH) $("#popup_" + currentPopup()).css("margin-top", popupM + "px");
//    
//    $("#popups").find("img").each(function(index,element)
//	{
//		element.src = element.src+"";
//	});
}



// SPECIAL GAME FUNCTIONS

function random(start, end)
{
	return Math.floor(Math.random()*(end-start+1))+start;
}

function popupPermanentOverlay_Searching(locationName)
{
	popupPermanentOverlay_WalkingBase("Exploring "+locationName, "You are wandering around, looking for anything of interest...");
}

function popupPermanentOverlay_Walking(locationName)
{
	popupPermanentOverlay_WalkingBase("Walking to "+locationName);
}

function popupPermanentOverlay_WalkingBase(title, text) {
	var biome = window.biome;
	if (biome==null) biome = "Temperate";
	var windowWidth = $(".main-banner").width();
	var width = windowWidth+20;
	var yOffset = 180;

	var content = "";
	
	if (isAnimationsEnabled())
	{
	
		content = "<div class='travel-scene-container'><div class='travel-scene'><div class='walkingman-container'><img class='walkingman' src='images/anim/walking.gif' style='bottom:"+(yOffset-13)+"px;left:"+(-windowWidth/2-15)+"px'/>";
	
		if (biome=="Dungeon")
		{
			// This version uses the new torch walking man
			//content = "<div class='travel-scene-container' style='background-image:none; background-color:#000000;'><div class='travel-scene'><div class='walkingman-container'><img class='walkingman' src='images/environment/dungeon/walking_torch.gif' style='bottom:"+(yOffset-13)+"px;left:"+(-windowWidth/2-15)+"px'/>";
			
			content = "<div class='travel-scene-container' style='background-image:none; background-color:#000000;'><div class='travel-scene'><div class='walkingman-container'><img class='walkingman' src='images/anim/walking.gif' style='bottom:"+(yOffset-13)+"px;left:"+(-windowWidth/2-15)+"px'/>";
			var grassTiles = 40;
			// The ground first
			for(var i = 0; i<grassTiles; i++)
			{
				var filename = "ground";
				
				filename+=random(1,4);
					
				
				var y = random(-40, 10);
				var x = random(width/2*-1,width/2)-100;
				content+="<img class='walkingman-prop' src='images/environment/dungeon/"+filename+".png' style='bottom:"+(yOffset+y)+"px; left:"+x+"px;z-index:"+(100000-y)+";' />";
			}
			
			// Add the dungeon wall
			content+="<img class='walkingman-prop' src='images/environment/dungeon/wall.jpg' style='bottom:"+(yOffset+20)+"px; left:-"+(width/2-10)+"px;z-index:140001;' />";
		
			var torches = random(1,5);
			var torchXOffset = random(0,100);
			for(var i = 0; i<torches; i++)
			{
				
				var x = torchXOffset;
				content+="<img class='walkingman-prop' src='images/environment/dungeon/torch.gif' style='bottom:"+(yOffset+40)+"px; left:"+(x+(width/torches*i)-(width/2))+"px;z-index:140001;' />";
			}
			
	//		var plants = random(0,10);
	//		// Trees and shrubs next
	//		for(var i = 0; i<plants; i++)
	//		{
	//			var filename = "baretree";
	////			var type=random(0,5);
	////			if (type==0)
	////				filename = "tree";
	////			else if (type==1)
	////				filename = "tree";
	////			else if (type==2)
	////				filename = "shrub";
	////			else if (type==3)
	////				filename = "shrub";
	////			else if (type==4)
	////				filename = "shrub";
	////			else if (type==5)
	////				filename = "baretree";
	//			
	////			if (filename == "tree")
	////				filename+=random(1,6);
	////			else if (filename == "shrub")
	////				filename+=random(1,3);
	////			else if (filename == "baretree")
	//			filename+=random(1,7);
	//	
	//			var y = random(-60, 60);
	//			var x = random(width/2*-1,width/2)-100;
	//			content+="<img class='walkingman-prop' src='images/environment/snow/"+filename+".gif' style='bottom:"+(yOffset+y-7)+"px; left:"+x+"px;z-index:"+(150000-y)+";' />";
	//		}		
		}
		else if (biome=="Snow")
		{
			var grassTiles = 80;
			// The ground first
			for(var i = 0; i<grassTiles; i++)
			{
				var filename = "snow";
				
				if (random(1,2)==1)
					filename+=random(1,6);
				else
					filename+=random(1,4);
					
		
				var y = random(-100, 100);
				var x = random(width/2*-1,width/2)-100;
				content+="<img class='walkingman-prop' src='images/environment/snow/"+filename+".gif' style='bottom:"+(yOffset+y)+"px; left:"+x+"px;z-index:"+(100000-y)+";' />";
			}
			
			
			var plants = random(0,10);
			// Trees and shrubs next
			for(var i = 0; i<plants; i++)
			{
				var filename = "baretree";
	//			var type=random(0,5);
	//			if (type==0)
	//				filename = "tree";
	//			else if (type==1)
	//				filename = "tree";
	//			else if (type==2)
	//				filename = "shrub";
	//			else if (type==3)
	//				filename = "shrub";
	//			else if (type==4)
	//				filename = "shrub";
	//			else if (type==5)
	//				filename = "baretree";
				
	//			if (filename == "tree")
	//				filename+=random(1,6);
	//			else if (filename == "shrub")
	//				filename+=random(1,3);
	//			else if (filename == "baretree")
				filename+=random(1,7);
		
				var y = random(-60, 60);
				var x = random(width/2*-1,width/2)-100;
				content+="<img class='walkingman-prop' src='images/environment/snow/"+filename+".gif' style='bottom:"+(yOffset+y-7)+"px; left:"+x+"px;z-index:"+(150000-y)+";' />";
			}
		}
		else if (biome=="Desert")
		{
			var grassTiles = 80;
			// The ground first
			for(var i = 0; i<grassTiles; i++)
			{
				var filename = "sand";
				
				if (random(1,2)==1)
					filename+=random(1,6);
				else
					filename+=random(1,4);
					
		
				var y = random(-100, 100);
				var x = random(width/2*-1,width/2)-100;
				content+="<img class='walkingman-prop' src='images/environment/desert/"+filename+".gif' style='bottom:"+(yOffset+y)+"px; left:"+x+"px;z-index:"+(100000-y)+";' />";
			}
			
			
			var plants = random(-5,20);
			// Trees and shrubs next
			for(var i = 0; i<plants; i++)
			{
				var filename = "baretree";
	//			var type=random(0,5);
	//			if (type==0)
	//				filename = "tree";
	//			else if (type==1)
	//				filename = "tree";
	//			else if (type==2)
	//				filename = "shrub";
	//			else if (type==3)
	//				filename = "shrub";
	//			else if (type==4)
	//				filename = "shrub";
	//			else if (type==5)
	//				filename = "baretree";
				
	//			if (filename == "tree")
	//				filename+=random(1,6);
	//			else if (filename == "shrub")
	//				filename+=random(1,3);
	//			else if (filename == "baretree")
				filename+=random(1,7);
		
				var y = random(-60, 60);
				var x = random(width/2*-1,width/2)-100;
				content+="<img class='walkingman-prop' src='images/environment/desert/"+filename+".gif' style='bottom:"+(yOffset+y-7)+"px; left:"+x+"px;z-index:"+(150000-y)+";' />";
			}
		}
		else // Temperate by default
		{
			var grassTiles = 80;
			// The ground first
			for(var i = 0; i<grassTiles; i++)
			{
				var filename = "grass";
				
				if (random(1,2)==1)
					filename+=random(1,6);
				else
					filename+=random(3,6);
					
		
				var y = random(-100, 100);
				var x = random(width/2*-1,width/2)-100;
				content+="<img class='walkingman-prop' src='images/environment/temperate/"+filename+".gif' style='bottom:"+(yOffset+y)+"px; left:"+x+"px;z-index:"+(100000-y)+";' />";
			}
			
			
			var plants = 40;
			// Trees and shrubs next
			for(var i = 0; i<plants; i++)
			{
				var filename = "";
				var type = random(0, 5);

				switch(type)
				{
				    case 0:
				        filename = "tree" + random(1,6);
				        break;
				    case 1:
				        filename = "tree" + random(1,6);
				        break;
				    case 2:
				        filename = "shrub" + random(1,3);
				        break;
				    case 3:
				        filename = "shrub" + random(1,3);
				        break;
				    case 4:
				        filename = "shrub" + random(1,3);
				        break;
				    case 5:
				        filename = "baretree" + random(1,7);
				        break;
				}
		
				var y = random(-60, 60);
				var x = random(width/2*-1,width/2)-100;
				content+="<img class='walkingman-prop' src='images/environment/temperate/"+filename+".gif' style='bottom:"+(yOffset+y-7)+"px; left:"+x+"px;z-index:"+(150000-y)+";' />";
			}
		}
		content+="</div>";
		content+="</div>";
	}
	if (text!=null)
		text = "<p class='text-shadow'>"+text+"</p>";
	else
		text = "";
	
	
	content+="<div class='travel-scene-text'><h1>"+title+"</h1>"+text+"<p><a class='text-shadow' href='ServletCharacterControl?type=cancelLongOperations&v="+window.verifyCode+"'>Cancel</a></p></div>";
	content+="</div>";

	$("#banner-base").html(content);
	$(".walkingman").animate({left: "+="+(windowWidth+40)+"px"}, (windowWidth/0.023), "linear");
}

//function popupPermanentOverlay_Searching(locationName) {
//	var title = "Exploring "+locationName;
//	var content = "You`re wandering about, looking for anything of interest..<br><br><br><img class='walkingman' src='images/anim/Pixelman_Walking_by_pfunked.gif'/>";	
//	popupPermanentOverlay(title, content);
//	$(".walkingman").animate({left: "+=60px"}, 800, "linear", function()
//			{
//				var img = $(this);
//				img.attr("src", "images/anim/Pixelman_Ducking_by_pfunked.gif");
//				img.animate({left: "+=0px"}, 1250, "linear", function(){
//					var img = $(this);
//					img.attr("src", "images/anim/Pixelman_Walking_by_pfunked.gif");
//					img.animate({left: "+=600px"}, 10000, "linear");
//				});
//			});
//}



function buyHouse()
{
	promptPopup("Buy House", "Are you sure you want to buy a house from the city? It will cost 2000 gold.\n\nIf you would like to proceed, please give your new home a name:", "My House", function(name){
		window.location.href="ServletCharacterControl?type=buyHouse&houseName="+encodeURIComponent(name)+"&v="+window.verifyCode;
	});
}

function storeBuyItemNew(eventObject, itemName, itemPrice, itemId, saleItemId, characterId)
{
	confirmPopup("Buy Item", "Are you SURE you want to buy this <a class='clue' rel='viewitemmini.jsp?itemId="+itemId+"'>"+itemName+"</a> for "+itemPrice+" gold?", function(){
		doCommand(eventObject, "StoreBuyItem",{"saleItemId":saleItemId,"characterId":characterId},function(data,error){
			if (error) return;
			$(".saleItem[ref='"+saleItemId+"']").html(data.createStoreItem);
		});
	});
}

function storeSellItemNew(eventObject,itemId)
{
	promptPopup("Sell Item", "How much do you want to sell this item for?", "0", function(amount){
		if (amount!=null && amount!="")
		{
		doCommand(eventObject,"StoreSellItem",{"itemId":itemId,"amount":amount},function(data,error){
			if (error) return;
			$(".invItem[ref='"+itemId+"']").remove();
			var container = $("#saleItems");
			container.html(data.createSellItem+container.html());
			});
		}
	});
}

function storeDeleteAllItemsNew(eventObject,characterKey)
{
	confirmPopup("Remove All Items", "Are you sure you want to remove ALL the items from your store?", function(){
		{
		doCommand(eventObject,"StoreDeleteAllItems");
		}
	});
}

function storeDeleteSoldItemsNew(eventObject)
{
	confirmPopup("Remove All Sold Items","Are you sure you want to remove ALL sold items from your store?", function(){
		{
		doCommand(eventObject,"StoreDeleteSoldItems");
		}
	});
}

function storeDeleteItemNew(eventObject,saleItemId,itemId)
{
	doCommand(eventObject,"StoreDeleteItem",{"saleItemId":saleItemId,"itemId":itemId},function(data,error){
		if (error) return;
		
		$(".saleItem[ref='"+saleItemId+"']").remove();
		var container = $("#invItems");
		container.html(data.createInvItem+container.html());
		});
		
}

function storeRenameNew(eventObject)
{
	promptPopup("Rename Storefront", "Provide a new name for your store:", "", function(name){
		if (name!=null && name!="")
		{
			doCommand(eventObject,"StoreRename",{"name":name});
		}
	});	
}

function storeDisabledNew(eventObject)
{
	var clickedElement = $(eventObject.currentTarget);
	doCommand(eventObject, "StoreDisable", null, function(data, error){
		if (error) return;
		clickedElement.replaceWith(data.html);
	});
}

function storeEnabledNew(eventObject)
{
	var clickedElement = $(eventObject.currentTarget);
	doCommand(eventObject, "StoreEnable", null, function(data, error){
		if (error) return;
		clickedElement.replaceWith(data.html);
	});
}

function storeSetSaleNew(eventObject)
{
	promptPopup("Store-wide Price Adjustment", "Enter the percentage you would like to adjust the value of all your wares. For example, 25 will case all the items in your store to sell at 25% of the original value. Another example, 100 will cause your items to sell at full price.", 100, function(sale){
		if (sale!=null)
		{
			doCommand(eventObject,"StoreSetSale",{"sale":sale});
		}
	});
	
}

//function storeSellItem(itemId)
//{
//	promptPopup("Sell Item", "How much do you want to sell this item for?", "0", function(confirm){
//		window.location.href="ServletCharacterControl?type=storeSellItem&itemId="+itemId+"&amount="+confirm+"&v="+window.verifyCode;
//	});
//}

//function removeAllStoreItems()
//{
//	confirmPopup("Remove All Items", "Are you sure you want to remove ALL the items from your store?", function(){
//		window.location.href='ServletCharacterControl?type=storeDeleteAllItems'+"&v="+window.verifyCode;
//	});
//}

//function storeDeleteSoldItems()
//{
//	location.href = "ServletCharacterControl?type=storeDeleteSoldItems"+"&v="+window.verifyCode;
//}
//
//function storeDeleteItem(saleItemId)
//{
//	location.href = "ServletCharacterControl?type=storeDeleteItem&saleItemId="+saleItemId+""+"&v="+window.verifyCode;	
//}

//function renameStore()
//{
//	promptPopup("Rename Storefront", "Provide a new name for your store:", "", function(name){
//		if (name!=null && name!="")
//			window.location.href='ServletCharacterControl?type=storeRename&name='+encodeURIComponent(name)+"&v="+window.verifyCode;
//	});
//}

function createCampsite()
{
	var lastNameUsed = localStorage.getItem("campsiteName");
	if (lastNameUsed==null)
		lastNameUsed = "";
	
	promptPopup("New Campsite", "Provide a new name for your campsite:", lastNameUsed, function(name){
		if (name!=null && name!="")
		{
			window.location.href='ServletCharacterControl?type=createCampsite&name='+encodeURIComponent(name)+"&v="+window.verifyCode;
			popupPermanentOverlay("Creating a new campsite..", "You are hard at work setting up a new camp. Make sure you defend it or it won't last long!");
			localStorage.setItem("campsiteName", name);
		}
	});
}

function depositDogecoinsToItem(itemId, event)
{
	promptPopup("Deposit Gold", "How much gold do you want to put in this item:", "0", function(amount){
		if (amount!=null && amount!="")
		{
			ajaxAction('ServletCharacterControl?type=depositDogecoinsToItem&itemId='+itemId+'&amount='+encodeURIComponent(amount)+"&v="+window.verifyCode, event, reloadPagePopup);
		}
	});
	
	event.stopPropagation();
}

function collectDogecoinsFromItem(itemId, event)
{
	ajaxAction("ServletCharacterControl?type=collectDogecoinsFromItem&itemId="+itemId+"&v="+window.verifyCode, event, reloadPagePopup);	
}



//function tradeSetDogecoin(currentDogecoin)
//{
//	promptPopup("Trade Gold", "How much gold do you want to add to the trade:", currentDogecoin+"", function(amount){
//		if (amount!=null && amount!="")
//		{
//			window.location.href='ServletCharacterControl?type=setTradeDogecoin&amount='+encodeURIComponent(amount)+"&v="+window.verifyCode;
//		}
//	});
//}


function toggleFullscreenChat()
{
	$(".chat_box").toggleClass("fullscreenChat");
}

function exitFullscreenChat()
{
	$(".chat_box").removeClass("fullscreenChat");
}


function loadLocationItems()
{
	closeAllPagePopups();
	closeAllPopups();
	closeAllTooltips();
	pagePopup("ajax_moveitems.jsp?preset=location");
//	$("#main-itemlist").load("locationitemlist.jsp");
//	$("#main-itemlist").click(function(){
//		$("#main-itemlist").html("<div class='boldbox' onclick='loadLocationItems()'><h4 id='main-itemlist-close'>Nearby items</h4></div>");
//	});
}

function loadLocationCharacters()
{
	closeAllPagePopups();
	closeAllPopups();
	closeAllTooltips();
	pagePopup("locationcharacterlist.jsp");
//	$("#main-characterlist").click(function(){
//		$("#main-characterlist").html("<div class='boldbox' onclick='loadLocationCharacters()'><h4 id='main-characterlist-close'>Nearby characters</h4></div>");
//	});
}

function loadLocationMerchants()
{
	closeAllPagePopups();
	closeAllPopups();
	closeAllTooltips();
	pagePopup("locationmerchantlist.jsp");
//	$("#main-merchantlist").load("locationmerchantlist.jsp");
//	$("#main-merchantlist").click(function(){
//		$("#main-merchantlist").html("<div class='boldbox' onclick='loadLocationMerchants()'><h4 id='main-merchantlist-close'>Nearby merchants</h4></div>");
//	});
}

function loadInventoryAndEquipment()
{
	loadInventory();
	loadEquipment();
}

function loadInventory()
{
	$("#inventory").load("/odp/inventorylist.jsp?ajax=true");
//	$("#inventory").click(function(){
//		$("#main-itemlist").html("<div class='boldbox' onclick='loadLocationItems()'><h4>Nearby items</h4></div>");
//	});
}

function loadEquipment()
{
	$("#equipment").load("/odp/equipmentlist.jsp?ajax=true");
//	$("#inventory").click(function(){
//		$("#main-itemlist").html("<div class='boldbox' onclick='loadLocationItems()'><h4>Nearby items</h4></div>");
//	});
}

function ajaxAction(url, eventObject, loadFunction)
{
	if (url.indexOf("?")>0)
		url+="&ajax=true";
	else
		url+="?ajax=true";
	
	url += "&v="+window.verifyCode;

	var clickedElement = $(eventObject.currentTarget);
	var originalText = clickedElement.html();
	clickedElement.html("<img src='javascript/images/wait.gif' border=0/>");
	$.get(url)
	.done(function(data){
		clickedElement.html(data);
		loadFunction();
	})
	.fail(function(data){
		loadFunction();
		popupMessage("ERROR", "There was a server error when trying to perform the action. Feel free to report this on /r/initium. A log has been generated.");
		clickedElement.html(originalText);
	});
	
	eventObject.stopPropagation();
}


function helpPopup()
{
	popupMessage("Help", "The following chat commands exist:" +
			"<ul>" +
			"<li>/changelog - This displays the latest changes to the game. <a onclick='viewChangelog()'>View change log.</a></li>" +
			"<li>/me - This allows you to say something in 3rd person</li>" +
			"<li>/map - This shows a link to the community-created map which <a href='https://docs.google.com/drawings/d/1ZGBwTTrY5ATlJOWrPnwH2qWkee7kgdRTnTDPVHYZ3Ak/edit?usp=sharing'>you can also find here.</a>" +
			"<li>/customize - This allows you to share a link to the iten customization page. <a onclick='customizeItemOrderPage()'>You can also find it here</a>" +
			"<li>/merchant - This allows you to share the link to your store with everyone in the location. Make sure to turn your store on first though! <a onclick='viewManageStore()'>You can do that here</a>" +
			"<li>/quickstart - A quick start guide for new players who want to play efficiently as quick as possible! <a href='quickstart.jsp'>Open quick start page.</a></li>" +
			"<li>/about - Easily share the link to the official 'about' page on this site. <a href='about.jsp'>Open about page.</a></li>" +
			"<li>/mechanics - Easily share the link to the official 'mechanics' page on this site. It goes into more detail about how the game works. <a href='mechanics.jsp'>Open mechanics page.</a></li>" +
			"<li>/premium - Easily share a link to where people can learn about premium accounts.</li>" + 
			"<li>/roll - Do a dice roll in chat. Use the format xdx or xtox. For example: /roll 1d6 or /roll 10to100. Full math functions work too!</li>" + 
			"<li>/app - This shows all the links to the mobile apps we have available.</li>" +
			"</ul>", false);
	
}


function shareItem(itemId)
{
	var message = "Item("+itemId+")";
	if (messager.channel == "PrivateChat" && currentPrivateChatCharacterId!=null)
	{
		message = "#"+currentPrivateChatCharacterId + ": "+message;
	}
	else if (messager.channel == "PrivateChat" && currentPrivateChatCharacterName!=null)
	{
		message = currentPrivateChatCharacterName + ": "+message;
	}

	if (messager.channel == "PrivateChat" && currentPrivateChatCharacterName==null)
	{
		alert("You cannot chat privately until you select a person to chat privately with. Click on their name and then click on Private Chat.");
		return;
	}

	
	messager.sendMessage(message);
	//popupMessage("Item shared", "Everyone who is in your location can now see the item you just shared.");
	
	closeAllTooltips();
}


function viewGroup(groupId)
{
	pagePopup("odp/ajax_group.jsp?groupId=" + groupId);
}


function createNewGroup(eventObject)
{
	promptPopup("New Group","What name will you be using for your group.\n\nPlease use ONLY letters, commas, and apostrophes and a maximum of 30 characters.\n\nThis name cannot be changed later, so choose wisely!","", function(groupName) {
				if (groupName != null && groupName != "") {
					doCommand(eventObject, "GroupCreate", {"groupName" : groupName}, function(data, error) {
						if (error) return;
						viewGroup(data.groupId);
					})
				}
			});
}


function deleteGroup(eventObject)
{
	confirmPopup("Confirmation", "Are you sure you want to delete your group?\n\nThis cannot be undone.", function() {
		doCommand(eventObject, "GroupDelete", {}, function(data, error) {
			if (error) return;
			closePagePopup();
		});
	});
}


function leaveGroup(eventObject)
{
	confirmPopup("Leave group", "Are you sure you want to leave your group?", function(){
		doCommand(eventObject, "GroupLeave");
	});
}

//function cancelLeaveGroup()
//{
//	window.location.href = "ServletCharacterControl?type=cancelLeaveGroup"+"&v="+window.verifyCode;
//}

function setGroupDescription(eventObject, existingDescription)
{
	if (existingDescription==null || existingDescription=="")
	{
		existingDescription="No description";
	}
	promptPopup("Group Description", "Set your group's description here, but please be careful to only use letters, numbers, commas, and apostrophies:", existingDescription, function(description){
		if (description!=null && description!="")
		{
			doCommand(eventObject, "GroupChangeDescription", {"description" : description});
		}
		
	});
}

function setGroupMemberRank(eventObject, oldPosition, characterId)
{
	if (oldPosition==null || oldPosition=="")
	{
		oldPosition="No position";
	}
	promptPopup("Member Rank", "Give a new rank for this member:", oldPosition, function(newPosition){
		if (newPosition!=null && newPosition!="")
		{
			doCommand(eventObject, "GroupMemberChangeRank", {"rank" : newPosition, "characterId" : characterId});
		}
	});
}

function promoteToAdmin(eventObject, characterId)
{
	confirmPopup("Promote to Admin", "Are you sure you want to promote this member to admin?", function(){
		doCommand(eventObject, "GroupMemberPromoteToAdmin", {"characterId" : characterId});
	});
}

function demoteFromAdmin(eventObject, characterId)
{
	confirmPopup("Demote from Admin", "Are you sure you want to demote this member from admin?", function(){
		doCommand(eventObject, "GroupMemberDemoteFromAdmin", {"characterId" : characterId});
	});
}

function makeGroupCreator(eventObject, characterId)
{
	confirmPopup("New Group Creator", "Are you sure you want to make this member the group creator?\n\nThis action cannot be reversed unless this member (as the new group creator) chooses to reverse it manually!", function(){
		doCommand(eventObject, "GroupMemberMakeGroupCreator", {"characterId" : characterId});
	});
}


//function duelConfirmation_Yes()
//{
//	window.location.href="ServletCharacterControl?type=duelResponse&accepted=true"+"&v="+window.verifyCode;
//}
//
//function duelConfirmation_No()
//{
//	window.location.href="ServletCharacterControl?type=duelResponse&accepted=false"+"&v="+window.verifyCode;
//}

function reloadPopup(element, backUrl, event)
{
	var reloadDiv = $("#reload-div");
	var reloadDivReturn = $("#reload-div-return");
	
	var url = $(element).attr("rel");
	
	if (url==null)
		return;
	reloadDiv.load(url);
	
	if (backUrl==null)
		reloadDivReturn.html("");
	else
		reloadDivReturn.html("<a onclick='reloadPopup(this, null, event)' rel='"+backUrl+"'>&lt;&lt; Back</a>");
	
	if (event!=null)
		event.stopPropagation();
}

/**
 * This will refresh the current popup contents, assuming the popup
 * was created via an ajax call.
 * THIS IS A WORK IN PROGRESS, UNFINISHED
 */
function refreshPopup(url, event)
{
	var reloadDiv = $("#reload-div");
	var reloadDivReturn = $("#reload-div-return");
	
	var url = $(element).attr("rel");
	
	if (url==null)
		return;
	reloadDiv.load(url);
	
	if (backUrl==null)
		reloadDivReturn.html("");
	else
		reloadDivReturn.html("<a onclick='reloadPopup(this, null, event)' rel='"+backUrl+"'>&lt;&lt; Back</a>");
	
	if (event!=null)
		event.stopPropagation();
}

//function changeStoreSale()
//{
//	promptPopup("Store-wide Price Adjustment", "Enter the percentage you would like to adjust the value of all your wares. For example, 25 will case all the items in your store to sell at 25% of the original value. Another example, 100 will cause your items to sell at full price.", 100, function(sale){
//		if (sale!=null)
//		{
//			window.location.href="ServletCharacterControl?type=storeSale&sale="+sale+"&v="+window.verifyCode;
//		}
//	});
//	
//}


function destroyThrowaway()
{
	confirmPopup("Destroy Throwaway", "Are you SURE you want to destroy your throwaway? This action is permanent!", function(){
		window.location.href = 'ServletUserControl?type=destroyThrowaway'+"&v="+window.verifyCode;
	});
}

function popupPremiumReminder()
{
	if (window.isPremium==false)
	{
		$("body").append("<p id='premiumReminder' class='highlightbox-green' style='position:fixed; bottom:0px;z-index:19999999; left:0px;right:0px; background-color:#000000;'>" +
				"When you donate at least 5 dollars, you get a premium account for life!<br>" +
				"Premium member's characters always remember their house when they die, and " +
				"their names show up as red in chat.<br>" +
				"There are a lot more benefits coming for premium members <a onclick='viewProfile()'>" +
				"so check out more details here!</a>" +
				"</p>");
	}
}

function popupCharacterTransferService(currentCharacterId, currentCharacterName, characterNameToAccept)
{
	if (characterNameToAccept==null || characterNameToAccept=="")
		characterNameToAccept = "[No character specified]";
		
	var content = "This service allows you to transfer characters between user accounts. In order to transfer a character " +
			"you have to first click on the `Accept Character by Name` link and type the name of the character that you will " +
			"accept. Then log into the account with the character you want to transfer and click the `Transfer Character to Another Account` " +
			"link. You will then specify the email address of the account you wish to transfer your character to. The transfer " +
			"will be made instantly at that point." +
			"<br>" +
			"<strong>Please note that character names are case sensitive and email addresses must be all lower case!</strong>" +
			"<br>" +
			"<br>" +
			"<h5>You are currently waiting to accept: <span class='main-item-subnote'>"+characterNameToAccept+"</span></h5>" +
			""+
			"<p><a onclick='acceptCharacterTransfer()'>Accept Character by Name</a></p>" +
			"<p><a onclick='transferCharacter("+currentCharacterId+",\""+currentCharacterName+"\")'>Transfer Character to Another Account</a></p>" +
			"";
//	<p><a onclick='acceptCharacterTransfer()'>Accept Character by Name</a></p>
//	<p><a onclick='transferCharacter("${characterName}")'>Transfer Character to Another Account</a></p>
	
	$(".cluetip").hide();
	
	popupMessage("Character Transfer Service", content, false);
}

function acceptCharacterTransfer()
{
	promptPopup("Accept Character Transfer", "What is the name of the character you are going to transfer to this account? \n\nPlease note that the name is case sensitive!", "", function(charName){
		if (charName!=null)
		{
			window.location.href = "ServletUserControl?type=acceptCharacterTransfer&name="+charName+"&v="+window.verifyCode;
		}
	});
}

function transferCharacter(currentCharName)
{
	promptPopup("Transfer Character To..", "Please type the email address of the account you wish to transfer this character to.\n\nPlease note that you are currently using: "+currentCharName, "", function(email){
		if (email!=null)
		{
			window.location.href = "ServletUserControl?type=transferCharacter&email="+email+"&v="+window.verifyCode;
		}
	});
}

function dropAllInventory()
{
	confirmPopup("Drop ALL Inventory", "Are you sure you want to drop EVERYTHING in your inventory on the ground?\n\nPlease note that items for sale in your store and equipped items will be excluded.", function(){
		ajaxAction("ServletCharacterControl?type=dropAllInventory&v="+window.verifyCode, event, function(){
			reloadPagePopup(true);
		});
	});
}

////////////////////////////////////////////////////////
//Batch item functions
/**
 * fromSelector - should select the item divs themselves, not the parent (selection-list)
 * toSelector - this is the encompassing div we'll be adding to
 */
function moveSelectedElements(fromSelector, toSelector, delimitedIds, newHtml)
{
	var itemsList = "[ref="+(delimitedIds || "").split(",").join("],[ref=")+"]";
	
	var selectedItems = $(fromSelector).filter(itemsList);
	// Get rid of following line breaks first.
	selectedItems.next("br").remove();
	selectedItems.remove();
	
	var container = $(toSelector);
	container.html(newHtml+container.html());
}

function selectedItemsDrop(event, selector)
{
	var batchItems = $(selector).has("input:checkbox:checked");
	if(batchItems.length == 0) return;
	
	confirmPopup("Drop Selected Inventory", "Are you sure you want to drop " + batchItems.length + " selected items on the ground?\n\nPlease note that items for sale in your store will be excluded.", function(){
		var itemIds = $.makeArray(
				batchItems.map(function(i, selItem){ return $(selItem).attr("ref"); }))
				.join(",");

		// This command causes the popup to reload, so no need for a callback.
		doCommand(event,"ItemsDrop",{"itemIds":itemIds});
	});
}

function selectedItemsRemoveFromStore(event, selector)
{
	var batchItems = $(selector).has("input:checkbox:checked");
	if(batchItems.length == 0) return;
	
	confirmPopup("Remove Items from Store", "Are you sure you want to remove " + batchItems.length + " selected items from your store?", function(){
		var itemIds = $.makeArray(
				batchItems.map(function(i, selItem){ return $(selItem).attr("ref"); }))
				.join(",");

		doCommand(event,"ItemsStoreDelete",{"itemIds":itemIds}, function(data, error){
			if (error) return;
			moveSelectedElements(selector, "#invItems", data.processedItems || "", data.createInvItem);
		});
	});
}

function selectedItemsSell(event, selector)
{
	var batchItems = $(selector).has("input:checkbox:checked");
	if(batchItems.length == 0) return;
	promptPopup("Sell Multiple Items", "How much do you want to sell these " + batchItems.length + " selected items for?", "0", function(amount){
		if (amount!=null && amount!="")
		{
			var itemIds = $.makeArray(
					batchItems.map(function(i, selItem){ return $(selItem).attr("ref"); }))
					.join(",");
	
			doCommand(event,"ItemsSell",{"itemIds":itemIds,"amount":amount}, function(data, error){
				if (error) return;
				moveSelectedElements(selector, "#saleItems", data.processedItems || "", data.createSellItem);
			});
		}
	});
}

function selectedItemsTrade(event, selector)
{
	var batchItems = $(selector).has("input:checkbox:checked");
	if(batchItems.length == 0) return;
	
	confirmPopup("Trade Items", "Are you sure you want to trade " + batchItems.length + " selected items?", function(){
		var itemIds = $.makeArray(
				batchItems.map(function(i, selItem){ return $(selItem).attr("ref"); }))
				.join(",");

		doCommand(event,"ItemsTrade",{"itemIds":itemIds}, function(data, error){
			if (error) return;
			moveSelectedElements(selector, "#yourTrade", data.processedItems || "", data.createTradeItem);
			tradeVersion = data.tradeVersion;
		});
	});
}

function giveHouseToGroup()
{
	confirmPopup("Give House to Group", "Are you sure you want to PERMANENTLY give this house to your group? You cannot take it back!", function(){
		window.location.href='ServletCharacterControl?type=giveHouseToGroup'+"&v="+window.verifyCode;
	});
}

function refreshInstanceRespawnWarning()
{
	if (window.instanceRespawnMs!=null)
	{
		var now = new Date().getTime();
		var seconds = (window.instanceRespawnMs - now)/1000;
		var warning = $("#instanceRespawnWarning");
		if (seconds<=0)
			warning.text("Reinforcements will arrive at any moment! If you do not vacate the premises before they arrive, you will be forced out!");
		else
			warning.text("Reinforcements will arrive in "+secondsElapsed(seconds)+". If you do not vacate the premises before they arrive, you will be forced out!");
		warning.show();
	}
}

//function buyItem(itemName, itemPrice, merchantCharacterId, saleItemId, itemId)
//{
//	confirmPopup("Buy Item", "Are you SURE you want to buy this <a class='clue' rel='viewitemmini.jsp?itemId="+itemId+"'>"+itemName+"</a> for "+itemPrice+" gold?", function(){
//		window.location.href = "ServletCharacterControl?type=storeBuyItem&characterId="+merchantCharacterId+"&saleItemId="+saleItemId+""+"&v="+window.verifyCode;
//	});
//}

function giftPremium()
{
	promptPopup("Gift Premium to Another Player", "Please specify a character name to gift premium membership to. The user who owns this character will then be given a premium membership:", "", function(characterName){
		confirmPopup("Anonymous gift?", "Do you wish to remain anonymous? The player receiving the gift will not know who gave it to them if you choose no.", function(){
			location.href = "ServletUserControl?type=giftPremium&characterName="+characterName+"&anonymous=true&v="+window.verifyCode;
		}, function(){
			location.href = "ServletUserControl?type=giftPremium&characterName="+characterName+"&anonymous=false&v="+window.verifyCode;
		});
	});
}

function newPremiumToken()
{
	confirmPopup("Create new premium token?", "Are you sure you want to create a premium token and put it in your inventory?\n\nBe aware that this token can be traded AND looted if you die.", function(){
		window.location.href = "ServletUserControl?type=newPremiumToken"+"&v="+window.verifyCode;
	});
}

function newCharacterFromUnconscious()
{
	confirmPopup("Create a new character?", "If you do this, your unconscious character will be die immediately and you will be given a new character of the same name instead.\n\nAre you SURE you want to start a new character?", function(){
		window.location.href = "ServletUserControl?type=newCharacterFromUnconscious"+"&v="+window.verifyCode;
	});
}

function enterDefenceStructureSlot(slot)
{
	if (slot=="Defending1" || slot=="Defending2" || slot=="Defending3")
	{
		confirmPopup("Defend this structure?", "Are you sure you want to defend this structure? If you do this, other players will be able to attack and kill you.", function(){
			window.location.href = "ServletCharacterControl?type=setCharacterStatus&status="+slot+"&v="+window.verifyCode;
		});
	}
	else
	{
		window.location.href = "ServletCharacterControl?type=setCharacterStatus&status="+slot+"&v="+window.verifyCode;
	}
}

var popupStackCloseCallbackHandlers = [];
var currentPopupStackIndex = 0;
var popupKeydownHandler = function(e){if (e.keyCode == 27) closePagePopup();}
function incrementStackIndex()
{
	currentPopupStackIndex++;
    if (currentPopupStackIndex==1)
    {
		$("#page-popup-root").html("<div class='page-popup-glass'></div><a class='page-popup-Reload' onclick='reloadPagePopup()'>&#8635;</a><a class='page-popup-X' onclick='closePagePopup()'>X</a>");
	    $(document).bind("keydown", popupKeydownHandler);
    }
    else
   	{
    	$("#page-popup"+(currentPopupStackIndex-1)).hide();
   	}
    return currentPopupStackIndex;
}

function decrementStackIndex()
{
	if (currentPopupStackIndex==0)
		return 0;
	
	currentPopupStackIndex--;
	if (currentPopupStackIndex==0)
	{
		$("#page-popup-root").empty();
		$(document).unbind("keydown", popupKeydownHandler);
	}
	else
	{
		$("#page-popup"+currentPopupStackIndex).show();
		$("#page-popup"+(currentPopupStackIndex+1)).remove();
	}
	return currentPopupStackIndex;
}

function pagePopup(url, closeCallback)
{
	if (url.indexOf("?")>0)
		url+="&ajax=true";
	else
		url+="?ajax=true";
	
	exitFullscreenChat();
	
	var stackIndex = incrementStackIndex();
	var pagePopupId = "page-popup"+stackIndex;
	
	$("#page-popup-root").append("<div id='"+pagePopupId+"' class='page-popup'><div id='"+pagePopupId+"-content' src='"+url+"'><img id='banner-loading-icon' src='javascript/images/wait.gif' border=0/></div></div>");
	$("#"+pagePopupId+"-content").load(url);
	
	if (closeCallback!=null)
		popupStackCloseCallbackHandlers.push(closeCallback);
	else
		popupStackCloseCallbackHandlers.push(null);
}

function pagePopupIframe(url)
{
	
	if (url.indexOf("?")>0)
		url+="&ajax=true";
	else
		url+="?ajax=true";
	
	exitFullscreenChat();
	
	var stackIndex = incrementStackIndex();
	var pagePopupId = "page-popup"+stackIndex;
	$("#page-popup-root").append("<div id='"+pagePopupId+"' class='page-popup'><iframe id='"+pagePopupId+"-content' class='page-popup-iframe' src='"+url+"'><img id='banner-loading-icon' src='javascript/images/wait.gif' border=0/></iframe></div>");
}

function closePagePopup(doNotCallback)
{
	var pagePopupId = "page-popup"+currentPopupStackIndex;
	if ($("#"+pagePopupId+"-map").length>0)
	{
		closeMap();
	}
	
	decrementStackIndex();
	
	if (doNotCallback!=true)
	{
		var func = popupStackCloseCallbackHandlers.pop();
		if (func!=null)
			func();
	}
}

function closeAllPagePopups(doNotCallback)
{
	while (currentPopupStackIndex>0)
	{		
		closePagePopup(doNotCallback);
	}
}

function closeAllPopupsTooltips(doNotCallback)
{
    closeAllPagePopups(doNotCallback);
    closeAllPopups();
    closeAllTooltips();
}


function reloadPagePopup(quietly)
{
	if (currentPopupStackIndex==0)
		return;

	var pagePopupId = "page-popup"+currentPopupStackIndex;

	// Map can't be refreshed, but map popup will return empty content as well
	var content = $("#"+pagePopupId+"-content");
	if (content.length==0)
		return;
	
	var url = content.attr("src");

	if (quietly==false)
		content.html("<img id='banner-loading-icon' src='javascript/images/wait.gif' border=0/>");
	else
		$(".page-popup-Reload").html("<img src='javascript/images/wait.gif' border=0 style='margin-top:20px;'/>");

	if (content.is("iframe"))
	{
		content.attr('src', url);
	}
	else 
	{
		content.load(url, null, function(){
			$(".page-popup-Reload").html("&#8635;");
		});
	}
}

function moveItem(event, itemId, newContainerKind, newContainerId)
{
	ajaxAction("ServletCharacterControl?type=moveItem&itemId="+itemId+"&destinationKey="+newContainerKind+"_"+newContainerId+"&v="+window.verifyCode, event, function(){
		reloadPagePopup(true);
	});
}

function loadInlineItemsAndCharacters()
{
	$("#inline-items").load("locationitemlist.jsp?ajax=true");	
	$("#inline-characters").load("locationcharacterlist.jsp?ajax=true");	
}

function loadInlineCollectables()
{
	$("#collectables-area").load("ajax_collectables.jsp?ajax=true");	
}

function inventory()
{
    closeAllPopupsTooltips();
	pagePopup("/odp/ajax_inventory.jsp");
}

function viewChangelog()
{
    closeAllPopupsTooltips();
	pagePopup("ajax_changelog.jsp");
}

function viewSettings()
{
    closeAllPopupsTooltips();
	pagePopup("ajax_settings.jsp");
}

function viewProfile()
{
    closeAllPopupsTooltips();
	pagePopup("ajax_profile.jsp");
}

function viewMap()
{
    closeAllPopupsTooltips();
	openMap();
}

function deleteAndRecreateCharacter(currentCharName)
{
	confirmPopup("New Character", "Are you suuuure you want to delete your character and start over? It's permanent!", function(){
		if (currentCharName==null)
			currentCharName = "";
		promptPopup("New Character", "Ok, what will you call your new character?", currentCharName, function(name){
			window.location.href = "ServletCharacterControl?type=startOver&name="+encodeURIComponent(name)+"&v="+window.verifyCode;
		});
	});
}

function doDrinkBeer()
{
	confirmPopup("Drink Beer", "Are you sure you want to drink? It might affect your combat abilities...", function(){
		window.location.href = "ServletCharacterControl?type=drinkBeer"+"&v="+window.verifyCode;
		
	});
		
}

function resendVerificationEmail()
{
	confirmPopup("Resend verification email", "Are you sure you need to resend the verification email? Be sure to check your spam box if you don't seem to be receiving it!", function(){
		location.href = "ServletUserControl?type=resendVerificationEmail"+"&v="+window.verifyCode;
	});
	
}

function changeEmailAddress(oldEmail)
{
	promptPopup("Change email", "What email address would you like to use for your account?", oldEmail, function(value){
		location.href = "ServletUserControl?type=changeEmailAddress&email="+encodeURIComponent(value)+"&v="+window.verifyCode;
	});
}

function viewReferrals()
{
	pagePopup("ajax_referrals.jsp");
}

function customizeItemOrderPage(itemId)
{
    closeAllPopupsTooltips();
	pagePopup("ajax_customizeitem.jsp?itemId="+itemId);
}

function orderItemCustomization(itemId, orderTypeId, requiredDetails)
{
	confirmPopup("Are you sure?", "This will send an email to a content developer notifying them that you'd like to customize an item.<br>You will be asked to provide some details in the next popup.", function(){
		promptPopup("Customization Details", requiredDetails, "", function(value){
			location.href="ServletUserControl?type=customItemOrder&itemId="+itemId+"&orderTypeId="+orderTypeId+"&v="+window.verifyCode+"&requiredDetails="+encodeURIComponent(value);
		});
	});
}

function doAttack(eventObject, charId)
{
    closeAllPopups();
    closeAllTooltips();
    doCommand(eventObject,"Attack",{"charId":charId});
}

function leaveParty()
{
	confirmPopup("Leave party", "Are you sure you want to leave your party?", function(){
		location.href = "ServletCharacterControl?type=partyLeave"+"&v="+window.verifyCode;
	});
}

function collectDogecoinFromCharacter(characterKey)
{
	location.href = "ServletCharacterControl?type=collectDogecoin&characterId="+characterKey+"&v="+window.verifyCode;
}

function combatAttackWithLeftHand()
{
	location.href = "ServletCharacterControl?type=attack&hand=LeftHand"+"&v="+window.verifyCode;
}

function combatAttackWithRightHand()
{
	location.href = "ServletCharacterControl?type=attack&hand=RightHand"+"&v="+window.verifyCode;
}

function combatEscape()
{
	location.href = "ServletCharacterControl?type=escape"+"&v="+window.verifyCode;
}

function combatAllowCharacterIn()
{
	location.href = "ServletCharacterControl?type=allowCharacterIn"+"&v="+window.verifyCode;
}

function storeDisabled()
{
	location.href = "ServletCharacterControl?type=storeDisabled"+"&v="+window.verifyCode;
}

function storeEnabled()
{
	location.href = "ServletCharacterControl?type=storeEnabled"+"&v="+window.verifyCode;
}

////////////////////////////////////////////////////////
// BUTTON BAR TOGGLES
function toggleStorefront(eventObject)
{
	doCommand(eventObject, "ToggleStorefront", {"buttonId" : eventObject.currentTarget.id});
}

function togglePartyJoins(eventObject)
{
	doCommand(eventObject, "TogglePartyJoins", {"buttonId" : eventObject.currentTarget.id});
}

function toggleDuelRequests(eventObject)
{
	popupMessage("SYSTEM", "Dueling has been disabled (and has been for months) because the current combat system doesn't work well with it. We will re-enable it once we have a solution.");
	//doCommand(eventObject, "ToggleDuelRequests", {"buttonId" : eventObject.currentTarget.id});
}

function toggleCloaked(eventObject)
{	
	doCommand(eventObject, "ToggleCloak", {"buttonId" : eventObject.currentTarget.id});
}

function campsiteDefend()
{
	location.href = "ServletCharacterControl?type=defend"+"&v="+window.verifyCode;
}

function leaveAndForgetCombatSite(pathId)
{
	location.href = "ServletCharacterControl?type=gotoAndForget&pathId="+pathId+"&v="+window.verifyCode;
}

function forgetCombatSite(locationId)
{
	location.href = "ServletCharacterControl?type=forgetCombatSite&locationId="+locationId+"&v="+window.verifyCode;
}

function groupAcceptJoinGroupApplication(eventObject, characterId)
{
	doCommand(eventObject, "GroupAcceptJoinApplication", {"characterId" : characterId});
}

function groupDenyJoinGroupApplication(eventObject, characterId)
{
	doCommand(eventObject, "GroupDenyJoinApplication", {"characterId" : characterId});
}

function groupMemberKick(eventObject, characterId)
{
	doCommand(eventObject, "GroupMemberKick", {"characterId" : characterId});
}

function groupMemberKickCancel(characterId)
{
	location.href = "ServletCharacterControl?type=groupMemberCancelKick&characterId="+characterId+""+"&v="+window.verifyCode;
}

function groupRequestJoin(eventObject, groupId)
{
	doCommand(eventObject, "GroupRequestJoin", {"groupId" : groupId});
}

function tradeRemoveItem(itemId)
{
	location.href = "ServletCharacterControl?type=removeTradeItem&itemId="+itemId+""+"&v="+window.verifyCode;
}

function tradeCancel()
{
	location.href = "ServletCharacterControl?type=tradeCancel"+"&v="+window.verifyCode;
}

function tradeReady(version)
{
	location.href = "ServletCharacterControl?type=tradeReady&ver="+version+"&v="+window.verifyCode;
}

function tradeAddItem(itemId)
{
	location.href = "ServletCharacterControl?type=addTradeItem&itemId="+itemId+""+"&v="+window.verifyCode;
}

function partyJoin(characterId)
{
	location.href = "ServletCharacterControl?type=partyJoin&characterId="+characterId+"&v="+window.verifyCode;
}

function tradeStartTradeNew(eventObject,characterId)
{
	closeAllTooltips();
	doCommand(eventObject,"TradeStartTrade",{"characterId":characterId},function(data,error){
		if (error) return;
		_viewTrade();
		popupMessage("Trade Started", data.tradePrompt);	
	})
}

function tradeRemoveItemNew(eventObject,itemId)
{
	doCommand(eventObject,"TradeRemoveItem",{"itemId":itemId},function(data,error){
		if (error) return;
		$(".tradeItem[ref='"+itemId+"']").remove();
		var container = $("#invItems");
		container.html(data.createTradeInvItem+container.html());
		tradeVersion = data.tradeVersion;
	})
}

function tradeCancelNew(eventObject)
{
	doCommand(eventObject,"TradeCancel")
}

function tradeReadyNew(eventObject)
{
	doCommand(eventObject,"TradeReady",{"tradeVersion":tradeVersion},function(data,error){
		if (error) return;
		if (data.tradeComplete == "complete")
			{
				popupMessage("Trade Complete","Trade is complete.")
				closePagePopup(true);
			}
	});
}

function tradeAddItemNew(eventObject,itemId)
{
	doCommand(eventObject,"TradeAddItem",{"itemId":itemId},function(data,error){
		if (error) return;
		$(".invItem[ref='"+itemId+"']").remove();
		var container = $("#yourTrade");
		container.html(data.createTradeItem+container.html());
		tradeVersion = data.tradeVersion;
	})
}

function tradeSetGoldNew(eventObject,currentDogecoin)
{
	promptPopup("Trade Gold", "How much gold do you want to add to the trade:", currentDogecoin+"", function(amount){
		if (amount!=null && amount!="")
		{
			doCommand(eventObject,"TradeSetGold",{"amount":amount},function(data,error){
				if (error) return;
				$("#myTradeGoldAmount").text(data.newTradeGoldAmount);
				tradeVersion = data.tradeVersion;
			})
			
		}
	});
}

function tradeAddAllItemsNew(eventObject)
{
	doCommand(eventObject,"TradeAddAllItems");
	reloadPagePopup();
}
	

function duelRequest(characterId)
{
	location.href = "ServletCharacterControl?type=duelRequest&characterId="+characterId+"&v="+window.verifyCode;
}

function viewManageStore()
{
    closeAllPagePopups();
    closeAllPopups();
    closeAllTooltips();
    pagePopup("odp/ajax_managestore.jsp");
}

function newCharacterFromDead()
{
	location.href = "ServletUserControl?type=newCharacterFromDead"+"&v="+verifyCode;
}

function switchCharacter(characterId)
{
	location.href = "ServletUserControl?type=switchCharacter&characterId="+characterId+""+"&v="+verifyCode;
}

function logout()
{
	location.href = "ServletUserControl?type=logout"+"&v="+verifyCode;
}

function attackStructure()
{
	location.href = "ServletCharacterControl?type=attackStructure"+"&v="+verifyCode;
}

function allowDuelRequests()
{
	popupMessage("SYSTEM", "Dueling has been disabled (and has been for months) because the current combat system doesn't work well with it. We will re-enable it once we hvae a solution.");
	return;
	location.href = "ServletCharacterControl?type=allowDuelRequests"+"&v="+verifyCode;
}

function disallowDuelRequests()
{
	popupMessage("SYSTEM", "Dueling has been disabled (and has been for months) because the current combat system doesn't work well with it. We will re-enable it once we hvae a solution.");
	return;
	location.href = "ServletCharacterControl?type=disallowDuelRequests"+"&v="+verifyCode;
}

function viewStore(characterId)
{
	pagePopup("/odp/ajax_viewstore.jsp?characterId="+characterId+"");
}

function setBlockadeRule(rule)
{
	location.href = "ServletCharacterControl?type=setBlockadeRule&rule="+rule+"&v="+verifyCode;
}

function doEatBerry(eventObject)
{	
	var itemId = $("#popupItemId").val();
	if (itemId == null) return;
	doCommand(eventObject,"EatBerry",{"itemId":itemId},function(data,error){
		if (error) return;
		reloadPagePopup();
		popupMessage("System Message", "That was a tasty berry! Makes you feel kinda weird though, like your insides are trying to become outsides. WOW OK, now you don't feel too good. But you understand why. You feel like you understand a lot of things.");
	});
}

function doDrinkElixir(eventObject)
{	
	var itemId = $("#popupItemId").val();
	if (itemId == null) return;
	doCommand(eventObject,"EatBerry",{"itemId":itemId},function(data,error){
		if (error) return;
		reloadPagePopup();
		popupMessage("System Message", "As you sip down the liquid you feel a rush of energy that pulsates through your body. Your mind becomes hazy and you can only focus on defeating your enemies.");
	});
}
function doDeleteCharacter(eventObject,characterId)
{
	doCommand(eventObject,"UserDeleteCharacter",{"characterId":characterId},function(data,error){
		if(error) return;
		$("a[onclick='switchCharacter(" + characterId +")']").remove();
		//fullpageRefresh();
	})
}

function viewExchange()
{
	pagePopup("/odp/ajax_exchange.jsp");
}

function renamePlayerHouse(eventObject)
{
	promptPopup("Rename Player House", "Enter a new name for your house:", "", function(newName) {
		if (newName != null && newName != "")
		{
			doCommand(eventObject, "RenamePlayerHouse", {"newName" : newName});
		}
	});
}



















////////////////////////////////////////////////////////
// COMMANDS

function ajaxUpdatePage(ajaxResponseData)
{
	// Here we update the screen with fresh html that came along with the response
	if (ajaxResponseData.responseHtml!=null && ajaxResponseData.responseHtml.length>0)
	{
		for(var i = 0; i<ajaxResponseData.responseHtml.length; i++)
		{
			var htmlData = ajaxResponseData.responseHtml[i];
			if (htmlData.type==0)
			{
				$(htmlData.selector).html(htmlData.html);
			}
			else if (htmlData.type==1)
			{
				$(htmlData.selector).replaceWith(htmlData.html);
			}
		}
	}
}


function doCommand(eventObject, commandName, parameters, callback)
{
	if (parameters==null)
		parameters = {"v":verifyCode};
	else
		parameters.v = verifyCode;
	
	// Collapse the parameters into a single string
	var parametersStr = "";
	var firstTime = true;
	for(var propertyName in parameters)
		if (parameters.hasOwnProperty(propertyName))
		{
			if (firstTime)
			{
				firstTime=false;
				parametersStr+=encodeURIComponent(propertyName)+"="+encodeURIComponent(parameters[propertyName]);
			}
			else
			{
				parametersStr+="&"+encodeURIComponent(propertyName)+"="+encodeURIComponent(parameters[propertyName]);
			}
		}
	
	// Now generate the url. We might use this later on to recall the command for some reason... probably not though. To be honest, this part was copypasta from the LongOperation command type
	var url = "cmd?cmd="+commandName;
	if (parametersStr.length>0)
		url+="&"+parametersStr;
	
	var clickedElement = null;
	var originalText = null;
	if (eventObject!=null)
	{
		clickedElement = $(eventObject.currentTarget);
		originalText = clickedElement.html();
		clickedElement.html("<img src='javascript/images/wait.gif' border=0/>");
	}
	
	
	$.get(url)
	.done(function(data)
	{
		
		// Refresh the full page or the pagePopup if applicable
		if (data.javascriptResponse == "FullPageRefresh")
		{
			fullpageRefresh();
			return;		// No need to go any further, we're refreshing the page anyway
		}
		else if (data.javascriptResponse == "ReloadPagePopup")
			reloadPagePopup();

		// Do the page update first, regarless if there was an error. We do this because even errored responses may contain page updates.
		ajaxUpdatePage(data);

		// Here we display the system message if there was a system message
		if (data.message!=null && data.message.length>0)
			popupMessage("System Message", data.message);

		// Here we display an error message popup if there was an error
		var error = false;
		if (data.errorMessage!=null && data.errorMessage.length>0)
		{
			error = true;
			popupMessage("System Message", data.errorMessage);
		}


		
		
		if (eventObject!=null)
			clickedElement.html(originalText);
		
		if (callback!=null && data!=null)
			callback(data.callbackData, error);
		else if (callback!=null && data==null)
			callback(null, error);
	
	})
	.fail(function(data)
	{
		popupMessage("ERROR", "There was a server error when trying to perform the "+commandName+" command. Feel free to report this on <a href='http://initium.reddit.com'>/r/initium</a>. A log has been generated.");
		if (eventObject!=null)
			clickedElement.html(originalText);
	});
	
	if (eventObject!=null)
		eventObject.stopPropagation();
	
}

function doSetLeader(eventObject, charId)
{
	closeAllPopups();
	closeAllTooltips();
	confirmPopup("Set new leader", "Are you sure you want set someone else to be the leader of your group?", function(){
		doCommand(eventObject,"SetLeader",{"charId":charId});
	});
}

function doSetLabel(eventObject, itemId)
{
	closeAllPopups();
	closeAllTooltips();
	promptPopup("Relabel storage item", "Enter the new label for your item.<br>(Or leave blank to reset to the original name.)", null, function(label){
		doCommand(eventObject,"SetLabel",{"itemId":itemId,"label":label});
	});
}

function doTerritoryClaim(eventObject)
{
	closeAllPopups();
	closeAllTooltips();
	confirmPopup("Territory", "You are trying to claim a territory. If you continue, the defenders of this territory might attack you.<br><br>Are you sure you want to continue?", function(){
		doCommand(eventObject,"TerritoryClaim");
	});
}

function doTerritoryVacate(eventObject)
{
	closeAllPopups();
	closeAllTooltips();
	confirmPopup("Territory", "By vacating, you are giving up the control of this territory.<br><br>Are you sure you want to continue?", function(){
		doCommand(eventObject,"TerritoryVacate");
	});
}

function doTerritoryRetreat(eventObject)
{
	closeAllPopups();
	closeAllTooltips();
	confirmPopup("Territory", "By retreating, you are leaving the territory.<br><br>Are you sure you want to continue?", function(){
		doCommand(eventObject,"TerritoryRetreat");
	});
}

function doTerritorySetRule(eventObject, rule)
{
	closeAllPopups();
	closeAllTooltips();
	doCommand(eventObject,"TerritorySetRule",{"rule":rule});
}

function doTerritorySetDefense(eventObject, line)
{
	closeAllPopups();
	closeAllTooltips();
	doCommand(eventObject,"TerritorySetDefense",{"line":line});
}





////////////////////////////////////////////////////////
// LONG OPERATIONS







function longOperation_fullPageRefresh(eventObject, operationName, operationDescription, operationBannerUrl, actionUrl, fullPageRefreshSeconds)
{
	var originalText = $(eventObject.currentTarget).html();
	$(eventObject.currentTarget).html("<img src='javascript/images/wait.gif' border=0/>");
	$.get(url)
	.done(function(data){
		fullpageRefresh();
		$(eventObject.currentTarget).html(data);
	})
	.fail(function(data){
		fullpageRefresh();
		popupMessage("ERROR", "There was a server error when trying to perform the "+operationName+" action. Feel free to report this on <a href='http://initium.reddit.com'>/r/initium</a>. A log has been generated.");
		$(eventObject.currentTarget).html(originalText);
	});
	
	eventObject.stopPropagation();
	
}


var lastLongOperationEventObject = null;
/**
 * 
 * @param eventObject
 * @param actionUrl
 * @param responseFunction This is the handler that is called when the operation returns. The data that is passed into the handler includes: data.isComplete (boolean), data.error (boolean), data.timeLeft (seconds remaining to wait)
 * @param recallFunction This function should call the original javascript call that includes the call to longOperation. This handler is invoked when the time on the long operation runs out.
 */
function longOperation(eventObject, actionUrl, responseFunction, recallFunction)
{
	lastLongOperationEventObject = eventObject;		// We're persisting the event object because when the ajax call returns, we may need to know what element was clicked when starting the long operation
	$.get(actionUrl)
	.done(function(data)
	{
		// Do the page update first, regarless if there was an error. We do this because even errored responses may contain page updates.
		ajaxUpdatePage(data);
		
		if (data.error!=undefined)
		{
			hideBannerLoadingIcon();
			popupMessage("System Message", data.error, false);
			if (data.refresh==true)
				fullpageRefresh();
			return;
		}
		if (data.refresh==true)
		{
			fullpageRefresh();
			return;
		}
		if (responseFunction!=null)
			responseFunction(data);
		
		if (data.isComplete==false)
		{
			if (data.timeLeft>=0)
			{
				setTimeout(recallFunction, (data.timeLeft+1)*1000);
				if (data.timeLeft>=5)
					popupPremiumReminder();
			}
		}
		else
		{
			if (data.description!=null)
				$("#long-operation-complete-text").html(data.description);
		}
		lastLongOperationEventObject = null;
	})
	.fail(function(xhr, textStatus, errorThrown){
		if (errorThrown=="Internal Server Error")
			popupMessage(errorThrown, "There was an error when trying to perform the action. Feel free to report this on <a href='http://initium.reddit.com'>/r/initium</a>. A log has been generated.");
		else
			popupMessage(errorThrown, "There was an error when trying to perform the action.");

		lastLongOperationEventObject = null;
	});
	
	if (eventObject!=null)
		eventObject.stopPropagation();
}

function showBannerLoadingIcon()
{
	$("#banner-base").append("<img id='banner-loading-icon' src='javascript/images/wait.gif' border=0/>");
}

function setBannerImage(url)
{
	bannerUrl = url;
	updateDayNightCycle(true);
}

function setBannerOverlayText(title, text)
{
	if (text==null)
		text = "";
	var contents = "<div class='travel-scene-text'><h1>"+title+"</h1>"+text+"<p><a href='ServletCharacterControl?type=cancelLongOperations&v="+window.verifyCode+"'>Cancel</a></p></div>";
	
	$(".travel-scene-text").remove();
	$("#banner-base").append(contents);
}

function hideBannerLoadingIcon()
{
	$('#banner-loading-icon').remove();
}

function doGoto(event, pathId, attack)
{
	if (attack == null)
		attack = false;
	showBannerLoadingIcon();
	longOperation(event, "ServletCharacterControl?type=goto_ajax&pathId="+pathId+"&attack="+attack+"&v="+window.verifyCode, 
			function(action) // responseFunction
			{
				if (action.isComplete)
				{
					fullpageRefresh();
				}
				else
				{
					var locationName = action.locationName;
					popupPermanentOverlay_Walking(locationName, window.biome);

				}
			},
			function()	// recallFunction
			{
				doGoto(null, pathId, true, window.biome);
			});
}



function doExplore(ignoreCombatSites)
{
	if (ignoreCombatSites == null)
		ignoreCombatSites = false;
	showBannerLoadingIcon();
	longOperation(null, "ServletCharacterControl?type=explore_ajax&ignoreCombatSites="+ignoreCombatSites+"&v="+window.verifyCode, 
			function(action) // responseFunction
			{
				if (action.isComplete)
				{
					fullpageRefresh();
				}
				else
				{
					var locationName = action.locationName;
					popupPermanentOverlay_Searching(locationName, window.biome);

				}
			},
			function()	// recallFunction
			{
				doExplore(ignoreCombatSites, window.biome);
			});
}


function doRest()
{
	showBannerLoadingIcon();
	longOperation(null, "ServletCharacterControl?type=rest_ajax"+"&v="+window.verifyCode, 
			function(action) // responseFunction
			{
				if (action.isComplete)
				{
					fullpageRefresh();
				}
				else
				{
					hideBannerLoadingIcon();
					setBannerImage("images/action-campsite1.gif");
					setBannerOverlayText("Resting..", action.description);
				}
			},
			function()	// recallFunction
			{
				doRest();
			});
}


function doCollectCollectable(event, collectableId)
{
	showBannerLoadingIcon();
	longOperation(event, "ServletCharacterControl?type=collectCollectable_ajax&collectableId="+collectableId+"&v="+window.verifyCode, 
			function(action) // responseFunction
			{
				if (action.isComplete)
				{
					fullpageRefresh();
				}
				else
				{
					setBannerImage(action.bannerUrl);
					setBannerOverlayText("Collecting", "This process will take "+action.secondsToWait+" seconds..");
					hideBannerLoadingIcon();
				}
			},
			function()	// recallFunction
			{
				doCollectCollectable(null, collectableId);
			});
}




/*
 * 1. Regular method doGoto() is called which calls longOperation, passes in the doGoto_ajaxResponse() function
 * 2. longOperation() ajax calls the server to setup OR continue the operation
 * 3. Ajax call returns an object with the current state (waitTime, variousArgs) and calls the doGoto_ajaxResponse()
 * 4. For certain return states (like messages/errors) the longOperation will handle it? (maybe)
 * 5. doGoto_ajaxResponse() knows how to handle a completed state and an unfinished state
 * 
 * If the page is refreshed, main.jsp will look for an ongoing longOperation before rendering, if it finds one
 * it will include a call to doGoto() with all the same parameters in some script tags.
 */

function toggleMinimizeChat()
{
	$("#chat_tab").toggle();
}

function toggleMinimizeSoldItems()
{
	$(".soldItems").toggle();
}

function updateMinimizeBox(buttonElement, selector)
{
	$(window).load(function(){
		var minimized = localStorage.getItem("minimizeBox"+selector);
		if (minimized == "true")
			minimizeBox({target:$(buttonElement)}, selector);
		else
			maximizeBox({target:$(buttonElement)}, selector);
	});
}

function toggleMinimizeBox(event, selector)
{
	if (isBoxMinimized(selector) == "true")
		maximizeBox(event, selector);
	else
		minimizeBox(event, selector);
}

function minimizeBox(event, selector)
{
	var height = $(event.target).height();
	$(selector).height(height+6);
	$(selector).css("overflow", "hidden");
	localStorage.setItem("minimizeBox"+selector, "true");
}

function maximizeBox(event, selector)
{
	$(selector).height("auto");
	$(selector).css("overflow", "");
	localStorage.setItem("minimizeBox"+selector, "false");
}

function isBoxMinimized(selector)
{
	var minimized = localStorage.getItem("minimizeBox"+selector);
	if (minimized==null) minimized = "false";
	return minimized;
}

///////////////////////////////////////////
// These are notification type handlers

function fullpageRefresh()
{
	location.reload();
}

function _viewTrade()
{
    closeAllPopupsTooltips(true);
	pagePopup("odp/ajax_trade.jsp",function(){
		doCommand(null,"TradeCancel");
//		popupMessage("Trade Cancelled","This trade has been cancelled.")
	});	
}

function updateTradeWindow()
{
	reloadPagePopup();
}

function cancelledTradeWindow()
{
	closeAllPagePopups(true);
	popupMessage("Trade Cancelled","This trade has been cancelled.");
}

function completedTradeWindow()
{
	closeAllPagePopups(true);
	popupMessage("Trade Completed","The trade completed successfully.");
}

function updateTerritory()
{
	var territoryView = $("#territoryView");
	territoryView.html("<img src='javascript/images/wait.gif' border=0/>"+territoryView.html());
	territoryView.load("ajax_territoryview.jsp");
}


/////////////////////////////////////////
// Shortcut key bindings


$(document).keyup(function(event){
	var buttonToPress = $('[shortcut="'+event.which+'"]');
	if (buttonToPress.length>0)
	{
		buttonToPress[0].click();
	}
	else if (event.which==73) // I
	{
		inventory();
	}
	else if (event.which==77) // M
	{
		viewMap();
	}
	else if (event.which==79) // O
	{
		viewSettings();
	}
	else if (event.which==80) // P
	{
		viewProfile();
	}
	else if (event.which==76) // Changed to L since B is now for Nearby Characters list
	{
		window.location.href='main.jsp';
	}
});











function secondsElapsed(secondsToConvert)
{
    if (secondsToConvert<0)
        secondsToConvert=secondsToConvert*-1;
    if (secondsToConvert==0)
        return "now";

    var dSeconds = secondsToConvert;
    var dDays = Math.floor(dSeconds/86400);
    dSeconds = dSeconds-(dDays*86400);
    var dHours = Math.floor(dSeconds/3600);
    dSeconds = dSeconds-(dHours*3600);
    var dMinutes = Math.floor(dSeconds/60);
    dSeconds = Math.floor(dSeconds-(dMinutes*60));

    if (dDays==1)
        return dDays+" day";
    else if (dDays>1)
        return dDays+" days";

    if (dHours==1)
        return dHours+" hour";
    else if (dHours>1)
        return dHours+" hours";

    if (dMinutes==1)
        return dMinutes+" minute";
    else if (dMinutes>1)
        return dMinutes+" minutes";

    if (dSeconds==1)
        return dSeconds+" second";
    else if (dSeconds>1)
        return dSeconds+" seconds";

    return "less than 1 second";
}


function xorShift32(seed) 
{
    seed ^= (seed << 11);
    seed ^= (seed >>> 25);
    seed ^= (seed << 8);
    var out = seed % 127521;     
    return (out < 0) ? -out : out;
}

function rnd(seed, min, max)
{
	var rand = xorShift32(seed);
	var dbl = (rand/127521);
 
    return (dbl*(max-min))+min;
}

function stopEventPropagation(evt) {
    if (typeof evt.stopPropagation != "undefined") {
        evt.stopPropagation();
    } else {
        evt.cancelBubble = true;
    }
}

//function slideLoaded(img){
//    var $img = $(img),
//        $slideWrapper = $img.parent(),
//         total = $slideWrapper.find('img').length,
//       percentLoaded = null;
//
//    $img.addClass('loaded');
//
//    var loaded = $slideWrapper.find('.loaded').length;
//
//    if(loaded == total){
//        percentLoaded = 100;
//        // INSTANTIATE PLUGIN
//        $slideWrapper.easyFader();
//    } else {
//        // TRACK PROGRESS
//        percentLoaded = loaded/total * 100;
//   };
//};




////////////////////////////////////////////////////
// Page popups


function confirmPopup(title, content, yesFunction, noFunction)
{
	var unique = "ID"+Math.floor((Math.random() * 990000000) + 1);
	var popupClassOverride = null;
	if (popupClassOverride==null)
		popupClassOverride = "popup";
	closeAllPopups();
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
    $("#popups").html(currentPopups + '<div tabindex="0" id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper"><div id="popup_' + popupsNum + '" class="'+popupClassOverride+'"><div id="popup_header_' + popupsNum + '" class="popup_header">' + title + '</div><div id="popup_body_' + popupsNum + '" class="popup_body"><div id="popup_text_' + popupsNum + '" class="popup_text"><p>' + content + '</p><br></div></div><div id="popup_footer_' + popupsNum + '" class="popup_footer"><a id="'+unique+'-yes" class="popup_confirm_yes">Yes</a><a id="'+unique+'-no"  class="popup_confirm_no">No</a></div></div></div></div>');
    expandpopupMessage();
    
    var popupRoot = $('#popupWrapperBackground_' + popupsNum).focus();

    popupRoot.css("outline", "0px solid tranparent");
    popupRoot.focus();
    
    popupRoot.keyup(function(e){
    	stopEventPropagation(e);
    });
    popupRoot.keydown(function(e){
    	stopEventPropagation(e);
        if (e.keyCode == 13) 
        {
        	closepopupMessage(currentPopup());
        	if (yesFunction)
        	{
        		yesFunction();
        	}
        }
        if (e.keyCode == 27)
        {
        	closepopupMessage(currentPopup());
        	if (noFunction)
        	{
        		noFunction();
        	}
        }
    });
    
    
    
    $("#"+unique+"-yes").click(function(){
    	closepopupMessage(currentPopup());
    	if (yesFunction)
    	{
    		yesFunction();
    	}
    });
    $("#"+unique+"-no").click(function(){
    	closepopupMessage(currentPopup());
    	if (noFunction)
    	{
    		noFunction();
    	}
    });
    
    
}

function promptPopup(title, content, defaultText, yesFunction, noFunction)
{
	if (content!=null)
		content = content.replace("\n", "<br>");
	
	if (defaultText==null)
		defaultText = "";
	
	defaultText = defaultText+"";
	
	defaultText = defaultText.replace("\"", "`").replace("'", "`")
	
	
	var unique = "ID"+Math.floor((Math.random() * 990000000) + 1);
	var popupClassOverride = null;
	if (popupClassOverride==null)
		popupClassOverride = "popup";
	closeAllPopups();
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
    $("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper"><div id="popup_' + popupsNum + '" class="'+popupClassOverride+'"><div id="popup_header_' + popupsNum + '" class="popup_header">' + title + '</div><div id="popup_body_' + popupsNum + '" class="popup_body"><div id="popup_text_' + popupsNum + '" class="popup_text"><p style="margin:0px">' + content + '</p><br><div style="text-align:center"><input id="popup_prompt_input_'+unique+'" class="popup_prompt_input" type="text" value="'+defaultText+'"/></div></div></div><div id="popup_footer_' + popupsNum + '" class="popup_footer"><a id="'+unique+'-yes" class="popup_confirm_yes">Okay</a><a id="'+unique+'-no" class="popup_confirm_no">Cancel</a></div></div></div></div>');
    //$("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="page-popup"><div id="popup_header_' + popupsNum + '" class="popup_header">' + title + '</div><p>' + content + '</p><br><input id="popup_prompt_input_'+unique+'" class="popup_prompt_input" type="text" value="'+defaultText+'"/><a id="'+unique+'-yes" class="popup_confirm_yes">Okay</a><a id="'+unique+'-no" class="popup_confirm_no">Cancel</a></div>');
    expandpopupMessage();
    
    var inputText = $('#popup_prompt_input_'+unique);
    
    inputText.focus();
    inputText.select();

    inputText.keyup(function(e){
    	stopEventPropagation(e);
    });
    inputText.keydown(function(e){
    	stopEventPropagation(e);
        if (e.keyCode == 13) 
        {
        	var value = null;
        	if (yesFunction)
            	value = $('#popup_prompt_input_'+unique).val();
        	
        	closepopupMessage(currentPopup());
        	
        	if (yesFunction)
        		yesFunction(value);
        }
        if (e.keyCode == 27)
        {
        	closepopupMessage(currentPopup());
        	if (noFunction)
        	{
        		noFunction();
        	}
        }
        
        e.stopPropagation();
    });
    
    
    
    $("#"+unique+"-yes").click(function(){
    	var value = null;
    	if (yesFunction)
        	value = $('#popup_prompt_input_'+unique).val();
    	
    	closepopupMessage(currentPopup());
    	
    	if (yesFunction)
    		yesFunction(value);
    });
    $("#"+unique+"-no").click(function(){
    	closepopupMessage(currentPopup());
    	if (noFunction)
    	{
    		noFunction();
    	}
    });
    
    
}




////////////////////////////////////////////////////////
// Game Settings


function isMusicEnabled()
{
	var setting = localStorage.getItem("checkboxDisableMusic");
	if (setting==false)
		return true;
	else
		return false;
}

function isSoundEffectsEnabled()
{
	var setting = localStorage.getItem("checkboxDisableEnvironmentSoundEffects");
	if (setting!="true")
		return true;
	else
		return false;
}

function isBannersEnabled()
{
	var setting = localStorage.getItem("checkboxDisableBanners");
	if (setting!="true")
		return true;
	else
		return false;
}

function isAnimatedBannersEnabled()
{
	var setting = localStorage.getItem("checkboxDisableOnlyAnimatedBanners");
	if (setting!="true")
		return true;
	else
		return false;
}

function isWeatherEnabled()
{
	var setting = localStorage.getItem("checkboxDisableWeather");
	if (setting!="true")
		return true;
	else
		return false;
}

function isAnimationsEnabled()
{
	var setting = localStorage.getItem("checkboxDisableTravelAnimations");
	if (setting!="true")
		return true;
	else
		return false;
}

function getSoundEffectsVolume()
{
	var setting = localStorage.getItem("sliderSoundEffectsVolume");
	if (setting==null) return 100;
	return parseInt(setting);
}

function getMusicVolume()
{
	var setting = localStorage.getItem("sliderMusicVolume");
	if (setting==null) return 100;
	return parseInt(setting);
}

function toggleEnvironmentSoundEffects(newState)
{
	var enabled = isSoundEffectsEnabled();
	if (enabled==null)
		enabled = true;
	if (newState !== undefined) {
		enabled = newState;
	}
	
	createjs.Sound.muted = enabled;
	localStorage.setItem("checkboxDisableEnvironmentSoundEffects", enabled+"");

	if (requestedAudioDescriptor !== null)
	{
		setAudioDescriptor(requestedAudioDescriptor[0], requestedAudioDescriptor[1], requestedAudioDescriptor[2]);
		requestedAudioDescriptor = null;
	}
	
	// Set the correct image for the header mute button
	if (enabled)
		$("#header-mute").attr("src", "images/ui/sound-button1-mute.png");
	else
		$("#header-mute").attr("src", "images/ui/sound-button1.png");
	
}

function updateEnvironmentSoundEffectsVolume()
{
	var vol = getSoundEffectsVolume();
	vol = parseFloat(vol)/100;
	createjs.Sound.volume = vol;
}

////////////////////////////////////////////////////////
//Notifications
function doPopupNotification(iconUrl, title, text, category, options, onclick, onerror)
{
	if(notifyHandler == null || notifyHandler.popupNotify === "undefined") return;
	return notifyHandler.popupNotify(iconUrl, title, text, category, options, onclick, onerror);
}
