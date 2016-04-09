window.popupsNum = 0;
window.popupsOpen = 0;
window.popupsArray = new Array();

window.singlePostFormSubmitted = false;

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

	$("#btnNewCharacter").click(function (){
		confirmPopup("New Character", "Are you suuuure you want to delete your character and start over? It's permanent!", function(){
			if (charName==null)
				charName = "";
			promptPopup("New Character", "Ok, what will you call your new character?", charName, function(name){
				window.location.href = "ServletCharacterControl?type=startOver&name="+encodeURIComponent(name)+"";
			});
		});
	});
	
	$(".boldBoxCollapsed > h4").click(function(){
		$(this).parent().toggleClass("boldBoxCollapsed");
	});
	
	// If a text box is selected, don't allow shortcut keys to process (prevend default)
	$("input").keyup(function(event){
		event.stopPropagation();
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
		
	
});

//Pop up Message
function popupPermanentOverlay(title, content, popupClassOverride) 
{
	if (popupClassOverride==null)
		popupClassOverride = "popup";
	closepopupMessage(null, "killAll");
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
	closepopupMessage(null, "killAll");
}

function closeAllTooltips()
{
	$(".cluetip").hide();
}

function closepopupMessage(popupID, all) {
    if (all == "killAll") {
        $(".popupWrapperBackground").remove();
        $("#popups").hide();
        window.popupsOpen = 0;
        window.documentBindEnter = false;
        }
    else if (popupID >= 1) {
        $("#popupWrapperBackground_" + popupID).remove();
        	window.popupsOpen = window.popupsOpen-1;
            window.popupsArray[popupID-1] = "no";
            window.documentBindEnter = false;
            if (window.popupsOpen<=0) {
        		$("#popups").hide();
                }
            else enterPopupClose();
        }
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
				var type=random(0,5);
				if (type==0)
					filename = "tree";
				else if (type==1)
					filename = "tree";
				else if (type==2)
					filename = "shrub";
				else if (type==3)
					filename = "shrub";
				else if (type==4)
					filename = "shrub";
				else if (type==5)
					filename = "baretree";
				
				if (filename == "tree")
					filename+=random(1,6);
				else if (filename == "shrub")
					filename+=random(1,3);
				else if (filename == "baretree")
					filename+=random(1,7);
		
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
	
	
	content+="<div class='travel-scene-text'><h1>"+title+"</h1>"+text+"<p><a class='text-shadow' href='ServletCharacterControl?type=cancelLongOperations'>Cancel</a></p></div>";
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
		window.location.href="ServletCharacterControl?type=buyHouse&houseName="+encodeURIComponent(name);
	});
}

function storeSellItem(itemId)
{
	promptPopup("Sell Item", "How much do you want to sell this item for?", "0", function(confirm){
		window.location.href="ServletCharacterControl?type=storeSellItem&itemId="+itemId+"&amount="+confirm;
	});
}

function renameStore()
{
	promptPopup("Rename Storefront", "Provide a new name for your store:", "", function(name){
		if (name!=null && name!="")
			window.location.href='ServletCharacterControl?type=storeRename&name='+encodeURIComponent(name);
	});
}

function createCampsite()
{
	var lastNameUsed = localStorage.getItem("campsiteName");
	if (lastNameUsed==null)
		lastNameUsed = "";
	
	promptPopup("New Campsite", "Provide a new name for your campsite:", lastNameUsed, function(name){
		if (name!=null && name!="")
		{
			window.location.href='ServletCharacterControl?type=createCampsite&name='+encodeURIComponent(name);
			popupPermanentOverlay("Creating a new campsite..", "You are hard at work setting up a new camp. Make sure you defend it or it wont last long!");
			localStorage.setItem("campsiteName", name);
		}
	});
}

function depositDogecoinsToItem(itemId, event)
{
	promptPopup("Deposit Gold", "How much gold do you want to put in this item:", "0", function(amount){
		if (amount!=null && amount!="")
		{
			ajaxAction('ServletCharacterControl?type=depositDogecoinsToItem&itemId='+itemId+'&amount='+encodeURIComponent(amount), event, reloadPagePopup)
		}
	});
	
	event.stopPropagation();
}

function collectDogecoinsFromItem(itemId, event)
{
	ajaxAction("ServletCharacterControl?type=collectDogecoinsFromItem&itemId="+itemId+"", event, reloadPagePopup);	
}



function tradeSetDogecoin(currentDogecoin)
{
	promptPopup("Trade Gold", "How much gold do you want to add to the trade:", currentDogecoin+"", function(amount){
		if (amount!=null && amount!="")
		{
			window.location.href='ServletCharacterControl?type=setTradeDogecoin&amount='+encodeURIComponent(amount);
		}
	});
}


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
	pagePopup("ajax_moveitems.jsp?preset=location");
//	$("#main-itemlist").load("locationitemlist.jsp");
//	$("#main-itemlist").click(function(){
//		$("#main-itemlist").html("<div class='boldbox' onclick='loadLocationItems()'><h4 id='main-itemlist-close'>Nearby items</h4></div>");
//	});
}

function loadLocationCharacters()
{
	pagePopup("locationcharacterlist.jsp");
//	$("#main-characterlist").click(function(){
//		$("#main-characterlist").html("<div class='boldbox' onclick='loadLocationCharacters()'><h4 id='main-characterlist-close'>Nearby characters</h4></div>");
//	});
}

function loadLocationMerchants()
{
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
	$("#inventory").load("inventorylist.jsp?ajax=true");
//	$("#inventory").click(function(){
//		$("#main-itemlist").html("<div class='boldbox' onclick='loadLocationItems()'><h4>Nearby items</h4></div>");
//	});
}

function loadEquipment()
{
	$("#equipment").load("equipmentlist.jsp?ajax=true");
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

	
	var originalText = $(eventObject.target).text();
	$(eventObject.target).html("<img src='javascript/images/wait.gif' border=0/>");
	$.get(url)
	.done(function(data){
		$(eventObject.target).html(data);
		loadFunction();
	})
	.fail(function(data){
		loadFunction();
		popupMessage("ERROR", "There was a server error when trying to perform the action. Feel free to report this on /r/initium. A log has been generated.");
		$(eventObject.target).text(originalText);
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
			"<li>/merchant - This allows you to share the link to your store with everyone in the location. Make sure to turn your store on first though! <a href='managestore.jsp'>You can do that here</a>" +
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


function createNewGroup()
{
	promptPopup("New Group", "What name will you be using for your group.\n\nPlease use ONLY letters, commas, and apostrophes and a maximum of 30 characters.\n\nThis name cannot be changed later, so choose wisely!", "", function(groupName){
		if (groupName!=null && groupName!="")
		{
			window.location.href='ServletCharacterControl?type=createGroup&groupName='+encodeURIComponent(groupName);
		}
	});
}

function leaveGroup()
{
	confirmPopup("Leave group", "Are you sure you want to leave your group?", function(){
		window.location.href = "ServletCharacterControl?type=requestLeaveGroup";
		
	});
		
}

function setGroupDescription(existingDescription)
{
	promptPopup("Group Description", "Set your group's description here, but please be careful to only use letters, numbers, commas, and apostrophies:", existingDescription, function(description){
		if (description!=null && description!="")
		{
			window.location.href='ServletCharacterControl?type=setGroupDescription&description='+encodeURIComponent(description);
		}
		
	});
}

function setGroupMemberRank(oldPosition, characterId)
{
	promptPopup("Member Rank", "Give a new rank for this member:", oldPosition, function(newPosition){
		if (newPosition!=null && newPosition!="")
		{
			window.location.href='ServletCharacterControl?type=groupMemberChangeRank&characterId='+characterId+'&rank='+encodeURIComponent(newPosition);
		}
		
	});
}

function promoteToAdmin(characterId)
{
	confirmPopup("Promote to Admin", "Are you sure you want to promote this member to admin?", function(){
		window.location.href="ServletCharacterControl?type=groupMemberPromoteToAdmin&characterId="+encodeURIComponent(characterId);
	});
}

function demoteFromAdmin(characterId)
{
	confirmPopup("Demote from Admin", "Are you sure you want to demote this member from admin?", function(){
		window.location.href="ServletCharacterControl?type=groupMemberDemoteFromAdmin&characterId="+encodeURIComponent(characterId);
	});
}

function makeGroupCreator(characterId)
{
	confirmPopup("New Group Creator", "Are you sure you want to make this member into the group creator?\n\nThis action cannot be reversed unless the this member (as the new group creator) chooses to reverse it manually!", function(){
		window.location.href="ServletCharacterControl?type=groupMemberMakeGroupCreator&characterId="+encodeURIComponent(characterId);
	});
}


function duelConfirmation_Yes()
{
	window.location.href="ServletCharacterControl?type=duelResponse&accepted=true";
}

function duelConfirmation_No()
{
	window.location.href="ServletCharacterControl?type=duelResponse&accepted=false";
}

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

function changeStoreSale()
{
	promptPopup("Store-wide Price Adjustment", "Enter the percentage you would like to adjust the value of all your wares. For example, 25 will case all the items in your store to sell at 25% of the original value. Another example, 100 will cause your items to sell at full price.", 100, function(sale){
		if (sale!=null)
		{
			window.location.href="ServletCharacterControl?type=storeSale&sale="+sale;
		}
	});
	
}


function destroyThrowaway()
{
	confirmPopup("Destroy Throwaway", "Are you SURE you want to destroy your throwaway? This action is permanent!", function(){
		window.location.href = 'ServletUserControl?type=destroyThrowaway';
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
			window.location.href = "ServletUserControl?type=acceptCharacterTransfer&name="+charName;
		}
	});
}

function transferCharacter(currentCharName)
{
	promptPopup("Transfer Character To..", "Please type the email address of the account you wish to transfer this character to.\n\nPlease note that you are currently using: "+currentCharName, "", function(email){
		if (email!=null)
		{
			window.location.href = "ServletUserControl?type=transferCharacter&email="+email;
		}
	});
}

function dropAllInventory(verifyCode)
{
	confirmPopup("Drop ALL Inventory", "Are you sure you want to drop EVERYTHING in your inventory on the ground?\n\nPlease note that items for sale in your store and equipped items will be excluded.", function(){
		ajaxAction("ServletCharacterControl?type=dropAllInventory&v="+verifyCode, event, function(){
			reloadPagePopup(true);
		});
	});
}

function removeAllStoreItems()
{
	confirmPopup("Remove All Items", "Are you sure you want to remove ALL the items from your store?", function(){
		window.location.href='ServletCharacterControl?type=storeDeleteAllItems';
	});
}

function giveHouseToGroup()
{
	confirmPopup("Give House to Group", "Are you sure you want to PERMANENTLY give this house to your group? You cannot take it back!", function(){
		window.location.href='ServletCharacterControl?type=giveHouseToGroup';
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
			warning.text("Reinforcements will arrive at any moment! If you do not vacate the premises before they arrive, you will be forced out!")
		else
			warning.text("Reinforcements will arrive in "+secondsElapsed(seconds)+". If you do not vacate the premises before they arrive, you will be forced out!");
		warning.show();
	}
}

function buyItem(itemName, itemPrice, merchantCharacterId, saleItemId, itemId)
{
	confirmPopup("Buy Item", "Are you SURE you want to buy this <a class='clue' rel='viewitemmini.jsp?itemId="+itemId+"'>"+itemName+"</a> for "+itemPrice+" gold?", function(){
		window.location.href = "ServletCharacterControl?type=storeBuyItem&characterId="+merchantCharacterId+"&saleItemId="+saleItemId+"";
	});
}

function giftPremium()
{
	promptPopup("Gift Premium to Another Player", "Please specify a character name to gift premium membership to. The user who owns this character will then be given a premium membership:", "", function(characterName){
		var anonymous = false;
		confirmPopup("Anonymous gift?", "Do you wish to remain anonymous? The player receiving the gift will not know who gave it to them if you choose no.", function(){
			anonymous = true;
		});
		location.href = "ServletUserControl?type=giftPremium&characterName="+characterName+"&anonymous="+anonymous;
	});
}

function newPremiumToken()
{
	confirmPopup("Create new premium token?", "Are you sure you want to create a premium token and put it in your inventory?\n\nBe aware that this token can be traded AND looted if you die.", function(){
		window.location.href = "ServletUserControl?type=newPremiumToken";
	});
}

function newCharacterFromUnconscious()
{
	confirmPopup("Create a new character?", "If you do this, your unconscious character will be die immediately and you will be given a new character of the same name instead.\n\nAre you SURE you want to start a new character?", function(){
		window.location.href = "ServletUserControl?type=newCharacterFromUnconscious";
	});
}

function enterDefenceStructureSlot(slot)
{
	if (slot=="Defending1" || slot=="Defending2" || slot=="Defending3")
	{
		confirmPopup("Defend this structure?", "Are you sure you want to defend this structure? If you do this, other players will be able to attack and kill you.", function(){
			window.location.href = "ServletCharacterControl?type=setCharacterStatus&status="+slot;
		});
	}
	else
	{
		window.location.href = "ServletCharacterControl?type=setCharacterStatus&status="+slot;
	}
}


var currentPopupStackIndex = 0;

function pagePopup(url)
{
	if (url.indexOf("?")>0)
		url+="&ajax=true";
	else
		url+="?ajax=true";
	
	exitFullscreenChat();
	
	currentPopupStackIndex++;
	var pagePopupId = "page-popup"+currentPopupStackIndex;
	
	$("#page-popup-root").append("<div id='"+pagePopupId+"'><div id='"+pagePopupId+"-content' src='"+url+"' class='page-popup'><img id='banner-loading-icon' src='javascript/images/wait.gif' border=0/></div><div class='page-popup-glass'></div><a class='page-popup-Reload' onclick='reloadPagePopup()' style='font-family:Lucida Sans'>&#8635;</a><a class='page-popup-X' onclick='closePagePopup()'>X</a></div>");
	$("#"+pagePopupId+"-content").load(url);
	
	
    if (currentPopupStackIndex==1)
    {
	    $(document).bind("keydown",function(e) 
	    {
	    	if ((e.keyCode == 27)) 
	    	{
		        closePagePopup();
	        }
	    });
    }
    
    if (currentPopupStackIndex>1)
    	$("#page-popup"+(currentPopupStackIndex-1)).hide();
}

function closePagePopup()
{
	if (currentPopupStackIndex==0)
		return;
	
	var pagePopupId = "page-popup"+currentPopupStackIndex;
	
	$("#"+pagePopupId).remove();
	
    
    if (currentPopupStackIndex>1)
    	$("#page-popup"+(currentPopupStackIndex-1)).show();
    
    currentPopupStackIndex--;
}

function closeAllPagePopups()
{
	// Clear all popups before opening inventory
	for(var i = 0; i<currentPopupStackIndex; i++)
		closePagePopup();
}


function reloadPagePopup(quietly)
{
	if (currentPopupStackIndex==0)
		return;
	
	var pagePopupId = "page-popup"+currentPopupStackIndex;
	
	var url = $("#"+pagePopupId+"-content").attr("src");
	if (quietly)
	{}
	else
		$("#"+pagePopupId).html("<div id='"+pagePopupId+"-content' src='"+url+"' class='page-popup'><img id='banner-loading-icon' src='javascript/images/wait.gif' border=0/></div><div class='page-popup-glass'></div><a class='page-popup-Reload' onclick='reloadPagePopup()' style='font-family:Lucida Sans'>&#8635;</a><a class='page-popup-X' onclick='closePagePopup()'>X</a>");
	$("#"+pagePopupId+"-content").load(url);
}

function moveItem(event, itemId, newContainerKind, newContainerId)
{
	ajaxAction("ServletCharacterControl?type=moveItem&itemId="+itemId+"&destinationKey="+newContainerKind+"_"+newContainerId, event, function(){
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
	// Clear all popups before opening inventory
	for(var i = 0; i<currentPopupStackIndex; i++)
		closePagePopup();
	pagePopup("ajax_inventory.jsp");
}

function viewChangelog()
{
	closeAllPopups();
	pagePopup("ajax_changelog.jsp");
}

function viewSettings()
{
	closeAllPopups();
	pagePopup("ajax_settings.jsp");
}

function viewProfile()
{
	closeAllPopups();
	closeAllTooltips();
	pagePopup("ajax_profile.jsp");
}



































////////////////////////////////////////////////////////
// COMMANDS

function doCommand(eventObject, commandName, parameters, callback)
{
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
	
	// Now generate the url
	var url = "cmd?cmd="+commandName;
	if (parametersStr.length>0)
		url+="&"+parametersStr;
	
	var originalText = $(eventObject.target).text();
	$(eventObject.target).html("<img src='javascript/images/wait.gif' border=0/>");
	$.get(url)
	.done(function(data)
	{
		if (data.javascriptResponse == "FullPageRefresh")
			fullpageRefresh();
		else if (data.javascriptResponse == "ReloadPagePopup")
			reloadPagePopup();

		if (data.message!=null && data.message.length>0)
			popupMessage("System Message", data.message);

		if (data.errorMessage!=null && data.errorMessage.length>0)
			popupMessage("System Message", data.errorMessage);
			
		if (callback==null && data!=null)
			callback(data.callbackData);
		else if (callback==null && data==null)
			callback();
		
		$(eventObject.target).text(originalText);
	})
	.fail(function(data)
	{
		popupMessage("ERROR", "There was a server error when trying to perform the "+commandName+" command. Feel free to report this on <a href='http://initium.reddit.com'>/r/initium</a>. A log has been generated.");
		$(eventObject.target).text(originalText);
	});
	
	eventObject.stopPropagation();
	
}






////////////////////////////////////////////////////////
// LONG OPERATIONS







function longOperation_fullPageRefresh(eventObject, operationName, operationDescription, operationBannerUrl, actionUrl, fullPageRefreshSeconds)
{
	var originalText = $(eventObject.target).text();
	$(eventObject.target).html("<img src='javascript/images/wait.gif' border=0/>");
	$.get(url)
	.done(function(data){
		fullpageRefresh();
		$(eventObject.target).html(data);
	})
	.fail(function(data){
		fullpageRefresh();
		popupMessage("ERROR", "There was a server error when trying to perform the "+operationName+" action. Feel free to report this on <a href='http://initium.reddit.com'>/r/initium</a>. A log has been generated.");
		$(eventObject.target).text(originalText);
	});
	
	eventObject.stopPropagation();
	
}


var lastLongOperationEventObject = null;
function longOperation(eventObject, actionUrl, responseFunction, recallFunction)
{
	lastLongOperationEventObject = eventObject;		// We're persisting the event object because when the ajax call returns, we may need to know what element was clicked when starting the long operation
	$.get(actionUrl)
	.done(function(data){
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
	var contents = "<div class='travel-scene-text'><h1>"+title+"</h1>"+text+"<p><a href='ServletCharacterControl?type=cancelLongOperations'>Cancel</a></p></div>";
	
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
	longOperation(event, "ServletCharacterControl?type=goto_ajax&pathId="+pathId+"&attack="+attack, 
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
	longOperation(null, "ServletCharacterControl?type=explore_ajax&ignoreCombatSites="+ignoreCombatSites, 
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
	longOperation(null, "ServletCharacterControl?type=rest_ajax", 
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
	longOperation(event, "ServletCharacterControl?type=collectCollectable_ajax&collectableId="+collectableId, 
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





/////////////////////////////////////////
// Shortcut key bindings


window.disableShortcuts = false;
$(document).keyup(function(event){
	if (window.disableShortcuts==false)
	{
		var buttonToPress = $('[shortcut="'+event.which+'"]');
		if (buttonToPress.length>0)
		{
			window.disableShortcuts = true;
			buttonToPress[0].click();
		}
		
		if (event.which==80)
		{
			viewProfile();
		}
		else if (event.which==73)
		{
			inventory();
		}
		else if (event.which==77)
		{
			popupMessage("SYSTEM", "This is where I would put my map <h1>IF I HAD ONE</h1>", false);
		}
		else if (event.which==66)
		{
			window.disableShortcuts = true;
			window.location.href='main.jsp';
		}
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
	closepopupMessage(null, "killAll");
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
	closepopupMessage(null, "killAll");
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

function toggleEnvironmentSoundEffects()
{
	var enabled = isSoundEffectsEnabled();
	if (enabled==null)
		enabled = true;
	
	
	
	createjs.Sound.muted = enabled;
	localStorage.setItem("checkboxDisableEnvironmentSoundEffects", enabled+"");
	
	
	
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