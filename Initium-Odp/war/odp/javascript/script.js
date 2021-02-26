var selectedTileX = null;
var selectedTileY = null;

var uiStyle = localStorage.getItem("selectUIStyle");
if (uiStyle==null) uiStyle = "experimental";

var queryParams = "";
if (location.href.indexOf("?")>-1)
	queryParams = location.href.substring(location.href.indexOf("?"));

if (location.href.indexOf("main.jsp")>-1 || 
		location.href.indexOf("/odp/experimental")>-1 || 
		location.href.indexOf("/odp/full")>-1)
{
	if (uiStyle=="experimental")
	{
		if (location.href.indexOf("/odp/experimental")==-1)
			location.href = "/odp/experimental"+queryParams;
	}
	else if (uiStyle=="wowlike")
	{
		if (location.href.indexOf("/odp/full")==-1)
			location.href = "/odp/full"+queryParams;
	}
	else
	{
		if (location.href.indexOf("/main.jsp")==-1)
			location.href = "/main.jsp"+queryParams;
	}
}

window.popupsNum = 0;
window.popupsOpen = 0;
window.popupsArray = new Array();

window.singlePostFormSubmitted = false;

var getUrlParameter = function(sParam) {
    var sPageURL = decodeURIComponent(window.location.search.substring(1)),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
};

window.characterOverride = getUrlParameter("char");
if (window.characterOverride==null || window.characterOverride=="null")
	window.characterOverride = "";

var notifyHandler = null;
// Case insensitive Contains selector.f
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
	$("body").on("keyup", "input,textarea", function(event){
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
			setSelectionCheckboxes(event, null);
	    }, 500));
	});
	
	// Unfortunately, we have to use event delegation for checkboxes, since it's in popup content.
	$("#page-popup-root").on("click", ".selection-root input:checkbox.check-all", function(event)
	{
		var cb = $(event.currentTarget);
		var selectRoot = cb.parents(".selection-root");
		selectRoot.find("input:checkbox.check-group:not(:disabled)").prop( {checked:cb.prop("checked"), indeterminate:false});
		var selectItems = selectRoot.find(".selection-list .main-item:visible");
		selectItems.find("input:checkbox").prop("checked", cb.prop("checked"));
		selectItems.toggleClass("main-item-selected", cb.prop("checked"));
	});
	
	$("#page-popup-root").on("click", ".selection-root input:checkbox.check-group", function(event)
	{
		var cb = $(event.currentTarget);
		var groupId = cb.attr("ref");
		var groupItems = cb.parents(".selection-root").find(".selection-list #" + groupId + " .main-item:visible");
		groupItems.find("input:checkbox").prop("checked", cb.prop("checked"));
		groupItems.toggleClass("main-item-selected", cb.prop("checked"));
		
		setSelectionCheckboxes(event, groupId);
	});
	
	$("#page-popup-root").on("click", ".selection-list input:checkbox", function(event)
	{
		var cb = $(event.currentTarget);
		cb.parent(".main-item").toggleClass("main-item-selected", cb.prop("checked"));
		
		setSelectionCheckboxes(event);
	});
	
	$("#page-popup-root").on("click", ".selection-list .main-item-container", function(event)
	{
		$(event.currentTarget).parent().find("input:checkbox").click();
	});
	
	var isTouchEvent = false;
	
	// Handlers for the minitip overlays
	$('body').on("mouseover", "[minitip]", function(event) {
		if (!isTouchEvent) {
			var elem = $(this);
			elem.append('<div class="minitip">' + $(this).attr("minitip") + '</div>');
			var elemPos = elem.position();
			$(".minitip").css("top", (elemPos.bottom+10)+"px");
			$(".minitip").css("left", elemPos.left+"px");
		};
	});
	
	$('body').on("mouseout", "[minitip]", function(event) {
		$(this).find(".minitip").remove();
	});
	
	$('body').on("touchstart", "[minitip]", function(event) {
		isTouchEvent = true;
		setTimeout(function() { isTouchEvent = false;}, 300);
	});
	
	$('body').on("taphold", "[minitip]", function(event) {
		$(this).append('<div class="minitip">' + $(this).attr("minitip") + '</div>');
		return false;
	});
	
	$(".main-expandable .main-expandable-title").click(function(){
		$(this).parent().find(".main-expandable-content").show();
		$(this).hide();
	});
	
	// Set the correct image for the header mute button
	if (isSoundEffectsEnabled())
	{
		$("#header-mute img").attr("src", "https://initium-resources.appspot.com/images/ui/sound-button1.png");
		$("#sound-button img").attr("src", "https://initium-resources.appspot.com/images/ui3/header-button-sound-on1.png");
		
	}
	else
	{
		$("#header-mute img").attr("src", "https://initium-resources.appspot.com/images/ui/sound-button1-mute.png");
		$("#sound-button img").attr("src", "https://initium-resources.appspot.com/images/ui3/header-button-sound-off1.png");
}
		

	// When the window gains focus, call the "flagReadMessages" to indicate that the user has now read any unread messages that may have been waiting for him
	$(window).focus(function(){
		flagReadMessages();
		
		updateBannerWeatherSystem();
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
	$("#banner-text-overlay").hide();
	if (popupClassOverride==null)
		popupClassOverride = "popup backdrop1c v3-window1";
	closeAllPopups();
	$(".popupBlurrable").addClass("blur");
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
    $("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground" onclick="closepopupMessage('+popupsNum+')"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper" onclick="event.stopPropagation();"><div id="popup_' + popupsNum + '" class="'+popupClassOverride+'"><div id="popup_header_' + popupsNum + '" class="popup_header">' + title + '</div><div id="popup_body_' + popupsNum + '" class="popup_body"><div id="popup_text_' + popupsNum + '" class="popup_text">' + content + '</div></div><div id="popup_footer_' + popupsNum + '" class="popup_footer"></div></div></div></div>');
    expandpopupMessage();
}

function popupMessage(title, content, noBackground) 
{
	noBackgroundHtml = "";
	if (noBackground==true)
		noBackgroundHtml = 'style="background:none"';

	$(".popupBlurrable").addClass("blur");
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
    $("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground" onclick="closepopupMessage('+popupsNum+')"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper" onclick="event.stopPropagation();"><div id="popup_' + popupsNum + '" class="popup backdrop1c v3-window1" '+noBackgroundHtml+'><div id="popup_header_' + popupsNum + '" class="popup_header">' + title + '</div><div id="popup_body_' + popupsNum + '" class="popup_body"><div id="popup_text_' + popupsNum + '" class="popup_text"><div class="paragraph">' + content + '</div></div></div><div id="popup_footer_' + popupsNum + '" class="popup_footer"><div id="popup_footer_okay_' + popupsNum + '" class="popup_message_okay" unselectable="on" onClick="closepopupMessage(' + popupsNum + ')" title="okay">Okay</div></div></div></div></div>');
    expandpopupMessage();
    enterPopupClose();
   }

$(document).bind("keydown",function(e) 
{
	if (isMakePopupOpen() && e.keyCode == 27)
	{
		clearMakeIntoPopup();
		return;
	}
    if (popupsOpen >= 1) 
    {
        if ((e.keyCode == 13) || (e.keyCode == 27)) 
        {
            closepopupMessage(currentPopup());
            return;
        }
    }
    
    if (currentPopupStackIndex>0)
    {
    	if (e.keyCode == 27){
    		closePagePopup();
    		return;
    	}
    }
    
    if (e.keyCode == 27) 
    {
    	if (window.viewBannerDefault!=null) viewBannerDefault();
    }
});



function isPagePopupsOpen()
{
	if (window.popupsOpen<=0)
		return false;
	
	return true;
}

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
    $(".blur").removeClass("blur");
}

function closeAllTooltips()
{
	$(".cluetip").hide();
}

function closepopupMessage(popupID) {
	
    $("#popupWrapperBackground_" + popupID).remove();
    if (window.popupsOpen>0)
    	window.popupsOpen = window.popupsOpen-1;
    window.popupsArray[popupID-1] = "no";
    window.documentBindEnter = false;
    if (window.popupsOpen<=0)
    {
		$("#popups").hide();
		$(".blur").removeClass("blur");
    }
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

function clearPopupPermanentOverlay()
{
	hideLongOperationProgress();
	cancelTravelLine();
	$("#banner-text-overlay").show();
	$("#banner-base").html("");
	if (window.previousBannerUrl!=null)
	{
		window.bannerUrl = window.previousBannerUrl;
		updateBannerWeatherSystem();
	}
}

function popupPermanentOverlay_Experiment(title, text)
{
	$("#banner-text-overlay").hide();
	setBannerImage("https://initium-resources.appspot.com/images/animated/invention1.gif");
	var content="<div class='travel-scene-text'><h1>"+title+"</h1>"+text+"<p><a class='text-shadow' onclick='cancelLongOperations(event)'>Cancel</a></p></div>";

	$("#banner-base").html(content);
	
}

function popupPermanentOverlay_Searching(locationName)
{
	$("#banner-text-overlay").hide();
	if (bannerUrl!=null && window.previousBannerUrl!=bannerUrl)
		window.previousBannerUrl = bannerUrl;
	popupPermanentOverlay_WalkingBase("Exploring "+locationName, "You are wandering around, looking for anything of interest...");
}

function popupPermanentOverlay_Walking(locationName)
{
	if (bannerUrl!=null && window.previousBannerUrl!=bannerUrl)
		window.previousBannerUrl = bannerUrl;
	popupPermanentOverlay_WalkingBase("Walking to "+locationName);
}

function popupPermanentOverlay_WalkingBase(title, text) {
	$("#banner-text-overlay").hide();
	var biome = window.biome;
	if (biome==null) biome = "Temperate";
	var windowWidth = $(".main-banner").width();
	var width = windowWidth+20;
	var yOffset = 180;

	var content = "";
	
	if (isAnimationsEnabled() && uiStyle!="experimental" && uiStyle!="wowlike")
	{
	 
		content = "<div class='travel-scene-container'><div class='travel-scene'><div class='walkingman-container'><img class='walkingman' src='https://initium-resources.appspot.com/images/anim/walking.gif' style='bottom:"+(yOffset-13)+"px;left:"+(-windowWidth/2-15)+"px'/>";
	
		if (biome=="Dungeon")
		{
			// This version uses the new torch walking man
			//content = "<div class='travel-scene-container' style='background-image:none; background-color:#000000;'><div class='travel-scene'><div class='walkingman-container'><img class='walkingman' src='https://initium-resources.appspot.com/images/environment/dungeon/walking_torch.gif' style='bottom:"+(yOffset-13)+"px;left:"+(-windowWidth/2-15)+"px'/>";
			
			content = "<div class='travel-scene-container' style='background-image:none; background-color:#000000;'><div class='travel-scene'><div class='walkingman-container'><img class='walkingman' src='https://initium-resources.appspot.com/images/anim/walking.gif' style='bottom:"+(yOffset-13)+"px;left:"+(-windowWidth/2-15)+"px'/>";
			var grassTiles = 40;
			// The ground first
			for(var i = 0; i<grassTiles; i++)
			{
				var filename = "ground";
				
				filename+=random(1,4);
					
				
				var y = random(-40, 10);
				var x = random(width/2*-1,width/2)-100;
				content+="<img class='walkingman-prop' src='https://initium-resources.appspot.com/images/environment/dungeon/"+filename+".png' style='bottom:"+(yOffset+y)+"px; left:"+x+"px;z-index:"+(100000-y)+";' />";
			}
			
			// Add the dungeon wall
			content+="<img class='walkingman-prop' src='https://initium-resources.appspot.com/images/environment/dungeon/wall.jpg' style='bottom:"+(yOffset+20)+"px; left:-"+(width/2-10)+"px;z-index:140001;' />";
		
			var torches = random(1,5);
			var torchXOffset = random(0,100);
			for(var i = 0; i<torches; i++)
			{
				
				var x = torchXOffset;
				content+="<img class='walkingman-prop' src='https://initium-resources.appspot.com/images/environment/dungeon/torch.gif' style='bottom:"+(yOffset+40)+"px; left:"+(x+(width/torches*i)-(width/2))+"px;z-index:140001;' />";
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
	//			content+="<img class='walkingman-prop' src='https://initium-resources.appspot.com/images/environment/snow/"+filename+".gif' style='bottom:"+(yOffset+y-7)+"px; left:"+x+"px;z-index:"+(150000-y)+";' />";
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
				content+="<img class='walkingman-prop' src='https://initium-resources.appspot.com/images/environment/snow/"+filename+".gif' style='bottom:"+(yOffset+y)+"px; left:"+x+"px;z-index:"+(100000-y)+";' />";
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
				content+="<img class='walkingman-prop' src='https://initium-resources.appspot.com/images/environment/snow/"+filename+".gif' style='bottom:"+(yOffset+y-7)+"px; left:"+x+"px;z-index:"+(150000-y)+";' />";
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
				content+="<img class='walkingman-prop' src='https://initium-resources.appspot.com/images/environment/desert/"+filename+".gif' style='bottom:"+(yOffset+y)+"px; left:"+x+"px;z-index:"+(100000-y)+";' />";
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
				content+="<img class='walkingman-prop' src='https://initium-resources.appspot.com/images/environment/desert/"+filename+".gif' style='bottom:"+(yOffset+y-7)+"px; left:"+x+"px;z-index:"+(150000-y)+";' />";
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
				content+="<img class='walkingman-prop' src='https://initium-resources.appspot.com/images/environment/temperate/"+filename+".gif' style='bottom:"+(yOffset+y)+"px; left:"+x+"px;z-index:"+(100000-y)+";' />";
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
				content+="<img class='walkingman-prop' src='https://initium-resources.appspot.com/images/environment/temperate/"+filename+".gif' style='bottom:"+(yOffset+y-7)+"px; left:"+x+"px;z-index:"+(150000-y)+";' />";
			}
		}
		content+="</div>";
		content+="</div>";
	}
	if (text!=null)
		text = "<p class='text-shadow'>"+text+"</p>";
	else
		text = "";
	
	
	content+="<div class='travel-scene-text'><h1>"+title+"</h1>"+text+"<p><a class='text-shadow' onclick='cancelLongOperations(event)'>Cancel</a></p></div>";
	content+="</div>";

	$("#banner-base").html(content);
	$(".walkingman").animate({left: "+="+(windowWidth+40)+"px"}, (windowWidth/0.023), "linear");
}

//function popupPermanentOverlay_Searching(locationName) {
//	var title = "Exploring "+locationName;
//	var content = "You`re wandering about, looking for anything of interest..<br><br><br><img class='walkingman' src='https://initium-resources.appspot.com/images/anim/Pixelman_Walking_by_pfunked.gif'/>";	
//	popupPermanentOverlay(title, content);
//	$(".walkingman").animate({left: "+=60px"}, 800, "linear", function()
//			{
//				var img = $(this);
//				img.attr("src", "https://initium-resources.appspot.com/images/anim/Pixelman_Ducking_by_pfunked.gif");
//				img.animate({left: "+=0px"}, 1250, "linear", function(){
//					var img = $(this);
//					img.attr("src", "https://initium-resources.appspot.com/images/anim/Pixelman_Walking_by_pfunked.gif");
//					img.animate({left: "+=600px"}, 10000, "linear");
//				});
//			});
//}

function rediscoverHouses(event)
{
	doCommand(event, "UserRediscoverHouses");
}

function buyHouse(eventObject)
{
	promptPopup("Buy House", "Are you sure you want to buy a house from the city? It will cost 2000 gold.\n\nIf you would like to proceed, please give your new home a name:", "My House", function(name){
		doCommand(eventObject, "BuyHouse", {"houseName":name});
	});
}

function doCollectItem(event, itemId)
{
	doCommand(event, "CollectItem", {itemId:itemId}, 
		function(data,error){
			if(error) return;
			$(event.currentTarget || event.target).text("");
		});
}


function playerReadMap(eventObject, itemId, pathId, hasDura)
{
	closeAllTooltips();
	var readMap = function() { doCommand(eventObject, "PlayerReadMap", {"itemId":itemId,"pathId":pathId}); }; 
	if(hasDura)
	{
		confirmPopup("Confirm Read Map", "This map looks to be worn. Read anyway?", readMap);
	}
	else
		readMap();
}

function createMapToHouse(eventObject)
{
	confirmCancelPopup("Create Permanent Map", "Do you want to create a permanent map? Be warned that anyone will be able to learn the path to your house if they get access to it. It will cost 500g.", true,
			function() { doCommand(eventObject, "PlayerCreateHouseMap", {"reusable":"true"}); },
			function() { doCommand(eventObject, "PlayerCreateHouseMap", {"reusable":"false"}); });
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

function deletePlayerHouse(eventObject, pathId)
{
	confirmPopup("Delete Player House", "Deleting this house will cause THIS character to forget how to get back to this house, however the house itself will still be accessible by other characters who know about it's existence. <br>Are you absolutely sure you want to delete this house?", function() {
		doCommand(eventObject, "DeletePlayerHouse", {"pathId" : pathId});
	});
}

function storeBuyItemNew(eventObject, itemName, itemPrice, itemId, saleItemId, characterId, quantity)
{
	var yesFunction = function(qty){
		doCommand(eventObject, "StoreBuyItem",{"saleItemId":saleItemId,"characterId":characterId,"quantity":qty},function(data,error){
			if (error) return;
			$(".saleItem[ref='"+saleItemId+"']").html(data.createStoreItem);
		});
	};
	
	if(typeof quantity === "undefined" || quantity === 1)
		confirmPopup("Buy Item", "Are you SURE you want to buy this <a class='clue' rel='/odp/viewitemmini?itemId="+itemId+"'>"+itemName+"</a> for "+itemPrice+" gold?", yesFunction);
	else{
		var calcPrice = itemPrice.replace(/,/g,"");
		rangePopup("Buy Item", "Please specify the number of <a class='clue' rel='/odp/viewitemmini?itemId="+itemId+"'>"+itemName+"</a> to purchase ("+itemPrice+" gold each):",0,quantity,
			function(qty) { // textFunction
				return "Total cost: " + (qty*calcPrice);
			},
			yesFunction,
			null); // noFunction
	}
}

function storeSellItemNew(eventObject,itemId)
{
	promptPopup("Sell Item", "How much do you want to sell this item for?<br/>For stacks of items, will be per item purchased.", "0", function(amount){
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

function storeRepriceItem(eventObject,saleItemId)
{
	promptPopup("Reprice Item", "How much do you want to reprice this item for?<br/>For stacks of items, will be per item purchased.", "0", function(amount){
		if (amount!=null && amount!="")
		{
			doCommand(eventObject,"StoreRepriceItem",{"saleItemId":saleItemId,"amount":amount},function(data,error){
				if (error) return;
				$(".saleItem[ref='"+saleItemId+"']").replaceWith(data.repriceItem);
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

function storeRenameNew(eventObject, oldName)
{
	promptPopup("Rename Storefront", "Provide a new name for your store:", oldName.replace(/'/,"&apos;"), function(name){
		if (name!=null && name!="")
		{
			doCommand(eventObject,"StoreRename",{"name":name});
		}
	});	
}

function storeDisabledNew(eventObject)
{
	var clickedElement = $(eventObject.currentTarget);
	doCommand(eventObject, "StoreDisable");
}

function storeEnabledNew(eventObject)
{
	var clickedElement = $(eventObject.currentTarget);
	doCommand(eventObject, "StoreEnable");
}

function storeSetSaleNew(eventObject)
{
	promptPopup("Store-wide Price Adjustment", "Enter the percentage you would like to adjust the value of all your wares. For example, 25 will case all the items in your store to sell at 25% of the original value. Another example, 100 will cause your items to sell at full price.", 100, function(sale)
	{
		if (sale!=null)
		{
			doCommand(eventObject,"StoreSetSale",{"sale":sale});
		}
	});
	
}

function transmuteItems(eventObject, containerId) 
{
	doCommand(eventObject, "TransmuteItems", {"containerId":containerId});
}

//function storeSellItem(itemId)
//{
//	promptPopup("Sell Item", "How much do you want to sell this item for?", "0", function(confirm){
//		window.location.href="/ServletCharacterControl?type=storeSellItem&itemId="+itemId+"&amount="+confirm+"&v="+window.verifyCode+"&char="+window.characterOverride;
//	});
//}

//function removeAllStoreItems()
//{
//	confirmPopup("Remove All Items", "Are you sure you want to remove ALL the items from your store?", function(){
//		window.location.href='/ServletCharacterControl?type=storeDeleteAllItems'+"&v="+window.verifyCode+"&char="+window.characterOverride;
//	});
//}

//function storeDeleteSoldItems()
//{
//	location.href = "/ServletCharacterControl?type=storeDeleteSoldItems"+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
//
//function storeDeleteItem(saleItemId)
//{
//	location.href = "/ServletCharacterControl?type=storeDeleteItem&saleItemId="+saleItemId+""+"&v="+window.verifyCode+"&char="+window.characterOverride;	
//}

//function renameStore()
//{
//	promptPopup("Rename Storefront", "Provide a new name for your store:", "", function(name){
//		if (name!=null && name!="")
//			window.location.href='/ServletCharacterControl?type=storeRename&name='+encodeURIComponent(name)+"&v="+window.verifyCode+"&char="+window.characterOverride;
//	});
//}

function createCampsite()
{
	clearMakeIntoPopup();
	
	var lastNameUsed = localStorage.getItem("campsiteName");
	if (lastNameUsed==null)
		lastNameUsed = "";
	
	promptPopup("New Campsite", "Provide a new name for your campsite:", lastNameUsed, function(name){
		if (name!=null && name!="")
		{
			localStorage.setItem("campsiteName", name);
			doCampCreate(name);
		}
	}, 
	null, 
	true);
}

function depositDogecoinsToItem(itemId, event)
{
	promptPopup("Deposit Gold", "How much gold do you want to put in this item:", $("#mainGoldIndicator").text().replace(/,/g,""), function(amount){
		if (amount!=null)
		{
			doCommand(event, "DogeCoinsDepositToItem", {"itemId" : itemId, "amount": amount}, function(data, error){
				if(error) return;
				reloadPagePopup();
			});
		}
	});
}

function collectDogecoinsFromItem(itemId, event, reload)
{
	if(reload===undefined) reload = true;
	// Command updates the gold indicator as needed, but not the inventory gold span. 
	// Just reload popup (if one is open, that is).
	doCommand(event, "DogeCoinsCollectFromItem", {"itemId" : itemId}, function(data, error){
		if(error) return;
		
		if(reload) reloadPagePopup();
		else $(event.currentTarget || event.target).text("Collect 0 gold");
	});
}

function collectDogecoinsFromCharacter(characterId, event)
{
	// Command updates the gold indicator as needed.
	doCommand(event, "DogeCoinsCollectFromCharacter", {"characterId" : characterId}, function(data,error){
		if(error) return;
		$(event.currentTarget || event.target).text("Collect 0 gold");
	});
}

function doCollectCharacter(event, characterId, characterName)
{
	doCommand(event, "CharacterCollectCharacter", {"characterId" : characterId, "characterName" : characterName}, function(data,error){
		if(error) return;
		// Inline nearby characters isn't in a popup, so clear the link as well.
		$(event.currentTarget || event.target).html("");
		reloadPagePopup();
	});
}

//function tradeSetDogecoin(currentDogecoin)
//{
//	promptPopup("Trade Gold", "How much gold do you want to add to the trade:", currentDogecoin+"", function(amount){
//		if (amount!=null && amount!="")
//		{
//			window.location.href='/ServletCharacterControl?type=setTradeDogecoin&amount='+encodeURIComponent(amount)+"&v="+window.verifyCode+"&char="+window.characterOverride;
//		}
//	});
//}


function toggleFullscreenChat()
{
	closeAllPagePopups();
	closeAllPopups();
	$(".chat_box").toggleClass("fullscreenChat-old");
	$(".chat-container").toggleClass("fullscreenChat");
}

function exitFullscreenChat()
{
	$(".chat_box").removeClass("fullscreenChat-old");
	$(".chat-container").removeClass("fullscreenChat");
}


function loadLocationItems()
{
	clearMakeIntoPopup();
	closeAllPagePopups();
	closeAllPopups();
	closeAllTooltips();
	pagePopup("/odp/ajax_moveitems?preset=location", null, "Nearby Items");
//	$("#main-itemlist").load("locationitemlist.jsp");
//	$("#main-itemlist").click(function(){
//		$("#main-itemlist").html("<div class='boldbox' onclick='loadLocationItems()'><h4 id='main-itemlist-close'>Nearby items</h4></div>");
//	});
}

function loadLocationCharacters()
{
	clearMakeIntoPopup();
	closeAllPagePopups();
	closeAllPopups();
	closeAllTooltips();
	pagePopup("/odp/locationcharacterlist", null, "Nearby Characters");
//	$("#main-characterlist").click(function(){
//		$("#main-characterlist").html("<div class='boldbox' onclick='loadLocationCharacters()'><h4 id='main-characterlist-close'>Nearby characters</h4></div>");
//	});
}

function loadLocationMerchants()
{
	clearMakeIntoPopup();
	closeAllPagePopups();
	closeAllPopups();
	closeAllTooltips();
	pagePopup("/odp/locationmerchantlist", null, "Nearby Merchants");
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
// used to sort armor and shields
function compareArmor (a, b) {
	var parser = /([\-\d]+)\/([\-\d]+)\/([\-\d]+) ([EGAPMN])\/([EGAPMN])\/([EGAPMN])/;
    var astats = parser.exec($(a).find('[minitip]').attr('minitip'));
    var bstats = parser.exec($(b).find('[minitip]').attr('minitip'));
    var adp = parseFloat(astats[1]);
	var abc = parseFloat(astats[2]);
	var adr = parseFloat(astats[3]);
    var bdp = parseFloat(bstats[1]);
	var bbc = parseFloat(bstats[2]);
	var bdr = parseFloat(bstats[3]);
	switch ($(a).find(".main-item-name").html().localeCompare($(b).find(".main-item-name").html())) {
		case -1:
			return 1;
		case 1:
			return -1;
		default:
			if (abc < bbc) {
				return -1;
			}
			if (abc > bbc) {
				return 1;
			}
			if (adr < bdr) {
				return -1;
			}
			if (adr > bdr) {
				return 1;
			}
			if (adp < bdp) {
				return 1;
			}
			if (adp > bdp) {
				return -1;
			}
			return 0;
	}
}

function compareWeapons(a, b) {
	var parser = /(\d+)D(\d+)x([.\d]+) (-?\d+)% \(([\d.,]+)\/([\d.,]+)\).*?([\-\d]+)\/([\-\d]+)\/([\-\d]+) ([EGAPMN])\/([EGAPMN])\/([EGAPMN])/;
    var astats = parser.exec($(a).find('[minitip]').attr('minitip'));
    var bstats = parser.exec($(b).find('[minitip]').attr('minitip'));
    var amax = parseFloat(astats[5]);
	var aavg = parseFloat(astats[6]);
    var bmax = parseFloat(bstats[5]);
	var bavg = parseFloat(bstats[6]);
	switch ($(a).find(".main-item-name").html().localeCompare($(b).find(".main-item-name").html())) {
		case -1:
			return 1;
		case 1:
			return -1;
		default:
			if (amax < bmax) {
				return -1;
			}
			if (amax > bmax) {
				return 1;
			}
			if (aavg < bavg) {
				return -1;
			}
			if (aavg > bavg) {
				return 1;
			}
			return 0;
	}
}

function loadInventory()
{
	$("#inventory").load("/odp/inventorylist?ajax=true&char="+window.characterOverride);
//	$("#inventory").click(function(){
//		$("#main-itemlist").html("<div class='boldbox' onclick='loadLocationItems()'><h4>Nearby items</h4></div>");
//	});
}

function loadEquipment()
{
	$("#equipment").load("/odp/equipmentlist?ajax=true&char="+window.characterOverride);
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
	
	url += "&v="+window.verifyCode+"&char="+window.characterOverride;
	
	if (window.characterOverride!=null && window.characterOverride.length>10)
		url+="&char="+window.characterOverride;

	var clickedElement = $(eventObject.currentTarget);
	var originalText = clickedElement.html();
	clickedElement.html("<img src='/javascript/images/wait.gif' border=0/>");
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


/**
 * Displays a popup that shows various chat commands available.
 */
function helpPopup()
{
	/*popupMessage("Help", "The following chat commands exist:" +
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
			"<li>/competition - This puts up a link to the official competition page. This page allows you to donate to prize pools and is usually used to organize competitions between the content developers for creating new content.</li>" +
			"<li>/faq - This puts up a link to a player made Frequently Asked Questions document which <a href='http://initium.wikia.com/wiki/Staub%27s_FAQ_Guide' target='_blank'>you can also find here.</a></li>" +
			"<li>/guide - This puts up a link to a player made Starter Guide which <a href='http://initium.wikia.com/wiki/Starter_Guide' target='_blank'>you can also find here.</a></li>" +
			"<li>/group - This puts up a link to the group that you belong to if you belong to one.</li>" +
			"<li>/groups - This puts up a link to a player made list of groups in Initium which <a href='http://initium.wikia.com/wiki/Category:Player_Groups' target='_blank'>you can also find here.</a></li>" +
			"<li>/wiki - This puts up a link to a player made wiki for Initium which <a href='http://initium.wikia.com/wiki/Initium_Wiki' target='_blank'>you can also find here.</a></li>" +
			"</ul>", false);*/
	pagePopup("/odp/chatHelp.html", null, "Chat Help");
}

function openMechanicsPage()
{
	pagePopup("/odp/mechanics.jsp", null, "Mechanics");
}


function shareItem(itemId)
{
	var message = "Item("+itemId+")";
	
	var target = null;
    if (messager.channel == "PrivateChat" && currentPrivateChatCharacterId!=null)
	{
		target = "#"+currentPrivateChatCharacterId;
	}
	else if (messager.channel == "PrivateChat" && currentPrivateChatCharacterName!=null)
	{
		target = currentPrivateChatCharacterName;
	}

	if (messager.channel == "PrivateChat" && currentPrivateChatCharacterName==null)
	{
		alert("You cannot chat privately until you select a person to chat privately with. Click on their name and then click on Private Chat.");
		return;
	}
	
	messager.sendMessage(message, target);
	//popupMessage("Item shared", "Everyone who is in your location can now see the item you just shared.");
	
	closeAllTooltips();
}


function viewGroup(groupId)
{
	closeAllTooltips();
	pagePopup("/odp/ajax_group?groupId=" + groupId, null, "Group Page");
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

function declareWar(eventObject)
{
	promptPopup("Declare War", "Enter the name of the group you want to declare on.", "",  function(groupName) {
		if (groupName != null || groupName != "") {
			doCommand(eventObject, "GroupDoSetWar", {"groupName" : groupName, "decision" : "begin"}, function(error)  {
				if (error) return;
			})
		}
	});
}
function endWar(eventObject, groupId) 
{
	confirmPopup("End War", "Are you sure you want to end this war?", function(){
		doCommand(eventObject, "GroupDoSetWar", {"groupId" : groupId, "decision" : "end"});
	});
}
//function cancelLeaveGroup()d
//{
//	window.location.href = "/ServletCharacterControl?type=cancelLeaveGroup"+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
function groupAcceptAllianceRequest(eventObject, groupId)
{
	confirmPopup("Accept Alliance", "Are you sure you want to ally yourself with this group?", function() {
		doCommand(eventObject, "GroupProcessAllianceReq", {"groupId" : groupId, "decision" : "accept"});
	})
}

function groupDeleteAlliance(eventObject, groupId)
{
	confirmPopup("End Alliance", "Are you sure you want to end this alliance?", function() {
		doCommand(eventObject, "GroupDeleteAlliance", {"groupId" : groupId});
	})
}
function groupDeclineAllianceRequest(eventObject, groupId)
{
	var decision = "decline";
	confirmPopup("Decline Alliance", "Are you sure you want to decline this alliance?", function() {
		doCommand(eventObject, "GroupProcessAllianceReq", {"groupId" : groupId, "decision" : "decline"});
	})
}

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

function submitGroupAllianceRequest(eventObject) 
{
	promptPopup("Request Alliance", "Enter the name of the group you want to ally with.", "",  function(groupName) {
		if (groupName != null || groupName != "") {
			doCommand(eventObject, "GroupAllianceRequest", {"groupName" : groupName}, function(error)  {
				if (error) return;
			})
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
//	window.location.href="/ServletCharacterControl?type=duelResponse&accepted=true"+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
//
//function duelConfirmation_No()
//{
//	window.location.href="/ServletCharacterControl?type=duelResponse&accepted=false"+"&v="+window.verifyCode+"&char="+window.characterOverride;
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
//			window.location.href="/ServletCharacterControl?type=storeSale&sale="+sale+"&v="+window.verifyCode+"&char="+window.characterOverride;
//		}
//	});
//	
//}


function destroyThrowaway()
{
	confirmPopup("Destroy Throwaway", "Are you SURE you want to destroy your throwaway? This action is permanent!", function(){
		enforceSingleAction();
		window.location.href = 'ServletUserControl?type=destroyThrowaway'+"&v="+window.verifyCode+"&char="+window.characterOverride;
	});
}

function popupPremiumReminder()
{
//	if (window.isPremium==false)
//	{
//		$("body").append("<p id='premiumReminder' class='highlightbox-green' style='position:fixed; bottom:0px;z-index:19999999; left:0px;right:0px; background-color:#000000;'>" +
//				"When you donate at least 5 dollars, you get a premium account for life!<br>" +
//				"Premium member's characters always remember their house when they die, and " +
//				"their names show up as red in chat.<br>" +
//				"There are a lot more benefits coming for premium members <a onclick='viewProfile()'>" +
//				"so check out more details here!</a>" +
//				"</p>");
//	}
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
			enforceSingleAction();
			window.location.href = "/ServletUserControl?type=acceptCharacterTransfer&name="+charName+"&v="+window.verifyCode+"&char="+window.characterOverride+"&char="+window.characterOverride;
		}
	});
}

function transferCharacter(currentCharName)
{
	promptPopup("Transfer Character To..", "Please type the email address of the account you wish to transfer this character to.\n\nPlease note that you are currently using: "+currentCharName, "", function(email){
		if (email!=null)
		{
			enforceSingleAction();
			window.location.href = "/ServletUserControl?type=transferCharacter&email="+encodeURIComponent(email)+"&v="+window.verifyCode+"&char="+window.characterOverride;
		}
	});
}

function dropAllInventory(event)
{
	confirmPopup("Drop ALL Inventory", "Are you sure you want to drop EVERYTHING in your inventory on the ground?\n\nPlease note that items for sale in your store and equipped items will be excluded.", function(){
		doCommand(event, "CharacterDropAll");
	});
}

////////////////////////////////////////////////////////
//Batch item functions
/**
 * @param fromSelector - should select the item divs themselves, not the parent (selection-list)
 * @param toSelector - this is the encompassing div we'll be adding to
 * @param delimitedIds - The list of ID's we'll be removing from the original list (fromSelector)
 * @param newHtml - Raw HTML to add to the new list (toSelector)
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

/**
 * This call is used to set the checkboxes in the inventory headers. Required to be called
 * using an event, so we can get the root and work from there (on popups with multiple
 * roots, such as inventory/equipment, this keeps us in the correct context).
 * @param event - Event object fired from the user action. Used to get context list.
 * @param groupId - Specified when clicking on a select group checkbox, bypassing checkbox state for groups entirely
 */
function setSelectionCheckboxes(event, groupId)
{
	// On link clicks, currentTarget should be null since it's not assigned from a shared parent
	// The link itself has the onclick, so coalesce to event.target
	var selectRoot = $(event.currentTarget || event.target).parents(".selection-root");
	var allItems = selectRoot.find(".main-item:visible");
	var checkedItems = allItems.has("input:checkbox:checked");
	
	// Check-all first
	selectRoot.find("input:checkbox.check-all")
		.prop({
			checked: allItems.length > 0 && checkedItems.length == allItems.length,
			indeterminate: checkedItems.length > 0 && checkedItems.length != allItems.length
		});
	
	// If we pass in a groupId, that means we've clicked a group checkbox already.
	// There won't be any overlapping groups (yet), so don't bother doing anything else with groups.
	if(groupId == null || groupId == "")
	{
		// Check if this event belongs to a group. We can limit our selection that way.
		var belongsToGroup = $(event.currentTarget || event.target).parents(".selection-group").prop("id");
		
		var groupFilter = belongsToGroup == null ? "" : "[ref=" + belongsToGroup + "]" 
		selectRoot.find("input:checkbox.check-group" + groupFilter).each(function(idx, grp) {
			var groupCB = $(grp);
			var groupItems = allItems.filter("#" + groupCB.attr("ref") + " .main-item");
			var groupChecked = checkedItems.filter(groupItems);
			groupCB
				.prop({
					checked:groupItems.length > 0 && groupChecked.length == groupItems.length,
					indeterminate: groupChecked.length > 0 && groupChecked.length != groupItems.length
				});
			// Set the checkbox enabled state in case there are new elements
			// in the group.
			groupCB.get(0).disabled = groupItems.length == 0;
		});
	}
}

function selectedItemsDrop(event, selector)
{
	var batchItems = $(selector).has("input:checkbox:visible:checked");
	if(batchItems.length == 0) return;
	
	confirmPopup("Drop Selected Inventory", "Are you sure you want to drop " + batchItems.length + " selected items on the ground?\n\nPlease note that items for sale in your store will be excluded.", function(){
		var itemIds = batchItems.map(function(i, selItem){ return $(selItem).attr("ref"); }).get().join(",");

		// This command causes the popup to reload, so no need for a callback.
		doCommand(event,"ItemsDrop",{"itemIds":itemIds});
	});
}

function selectedItemsRemoveFromStore(event, selector)
{
	var batchItems = $(selector).has("input:checkbox:visible:checked");
	if(batchItems.length == 0) return;
	
	confirmPopup("Remove Items from Store", "Are you sure you want to remove " + batchItems.length + " selected items from your store?", function(){
		var itemIds = batchItems.map(function(i, selItem){ return $(selItem).attr("ref"); }).get().join(",");

		doCommand(event,"ItemsStoreDelete",{"itemIds":itemIds}, function(data, error){
			if (error) return;
			moveSelectedElements(selector, "#invItems", data.processedItems || "", data.createInvItem);
			setSelectionCheckboxes(event);
		});
	});
}

function selectedItemsSell(event, selector)
{
	var batchItems = $(selector).has("input:checkbox:visible:checked");
	if(batchItems.length == 0) return;
	promptPopup("Sell Multiple Items", "How much do you want to sell these " + batchItems.length + " selected items for?", "0", function(amount){
		if (amount!=null && amount!="")
		{
			var itemIds = batchItems.map(function(i, selItem){ return $(selItem).attr("ref"); }).get().join(",");
	
			doCommand(event,"ItemsSell",{"itemIds":itemIds,"amount":amount}, function(data, error){
				if (error) return;
				moveSelectedElements(selector, "#saleItems", data.processedItems || "", data.createSellItem);
				setSelectionCheckboxes(event);
			});
		}
	});
}

function selectedItemsTrade(event, selector)
{
	var batchItems = $(selector).has("input:checkbox:visible:checked");
	if(batchItems.length == 0) return;
	
	confirmPopup("Trade Items", "Are you sure you want to trade " + batchItems.length + " selected items?", function(){
		var itemIds = batchItems.map(function(i, selItem){ return $(selItem).attr("ref"); }).get().join(",");

		doCommand(event,"ItemsTrade",{"itemIds":itemIds}, function(data, error){
			if (error) return;
			moveSelectedElements(selector, "#yourTrade", data.processedItems || "", data.createTradeItem);
			tradeVersion = data.tradeVersion;
			setSelectionCheckboxes(event);
		});
	});
}

function autofixStuckInLocation(event)
{
	doCommand(event, "AutofixStuckInLocation"); //Does this need a callback? Still not sure I fully understand that part.
}

function autofixLootStuckOnMonster(event)
{
	doCommand(event, "AutofixLootStuckOnMonster"); //callback? 
}


function autofixDeathModeNotSet(event)
{
	doCommand(event, "AutofixDeathModeNotSet"); //callback? 
}

function characterDropCharacter(event, characterId)
{
	doCommand(event, "CharacterDropCharacter", {"characterId":characterId}, loadInventory);
}

function characterDropAllCharacters(event)
{
	doCommand(event, "CharacterDropAllCharacters", null, loadInventory);
}

function characterDropItem(event, itemId, callback)
{
	// No callback necessary. Command clears out the item (doesn't touch header).
	doCommand(event, "CharacterDropItem", {"itemId":itemId}, callback);
}

function characterEquipSet(event, containerId)
{
	doCommand(event, "CharacterEquipSet", {"containerId":containerId}, loadInventoryAndEquipment);
}

function characterEquipItem(event, itemId)
{
	doCommand(event, "CharacterEquipItem", {"itemId":itemId});
}

function characterUnequipItem(event, itemId)
{
	doCommand(event, "CharacterUnequipItem", {"itemId":itemId});
}

function characterUnequipAll(event)
{
	doCommand(event, "CharacterUnequipAll");
}

function giveHouseToGroup(eventObject)
{
	confirmPopup("Give House to Group", "Are you sure you want to PERMANENTLY give this house to your group? You cannot take it back!", function(){
		doCommand(eventObject,"GivePlayerHouseToGroup");
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
	else
	{
		var warning = $("#instanceRespawnWarning");
		warning.text("");
		warning.hide();
	}
}

//function buyItem(itemName, itemPrice, merchantCharacterId, saleItemId, itemId)
//{
//	confirmPopup("Buy Item", "Are you SURE you want to buy this <a class='clue' rel='viewitemmini.jsp?itemId="+itemId+"'>"+itemName+"</a> for "+itemPrice+" gold?", function(){
//		window.location.href = "/ServletCharacterControl?type=storeBuyItem&characterId="+merchantCharacterId+"&saleItemId="+saleItemId+""+"&v="+window.verifyCode+"&char="+window.characterOverride;
//	});
//}

function giftPremium(name)
{
	closeAllTooltips();
	
	if (name==null) name = "";
	promptPopup("Gift Premium to Another Player", "Please specify a character name to gift premium membership to. The user who owns this character will then be given a premium membership:", name, function(characterName){
		confirmPopup("Anonymous gift?", "Do you wish to remain anonymous? The player receiving the gift will not know who gave it to them if you choose yes.", function(){
			doCommand(null, "PremiumGift", {characterName:characterName, anonymous:true});
		}, function(){
			doCommand(null, "PremiumGift", {characterName:characterName, anonymous:false});
		});
	});
}

function newPremiumToken()
{
	confirmPopup("Create new premium token?", "Are you sure you want to create a premium token and put it in your inventory?\n\nBe aware that this token can be traded AND looted if you die.", function(){
		enforceSingleAction();
		window.location.href = "/ServletUserControl?type=newPremiumToken"+"&v="+window.verifyCode+"&char="+window.characterOverride;
	});
}

function newCharacterFromUnconscious(event)
{
	confirmPopup("Create a new character?", "Are you Sure? If you do this, your unconscious character will be die immediately and you will be given a new character of the same name instead.\n\nAre you SURE you want to start a new character?", function(){
		doCommand(event, "CharacterRespawn");
	});
}

//function enterDefenceStructureSlot(slot)
//{
//	if (slot=="Defending1" || slot=="Defending2" || slot=="Defending3")
//	{
//		confirmPopup("Defend this structure?", "Are you sure you want to defend this structure? If you do this, other players will be able to attack and kill you.", function(){
//			enforceSingleAction();
//			window.location.href = "/ServletCharacterControl?type=setCharacterStatus&status="+slot+"&v="+window.verifyCode+"&char="+window.characterOverride;
//		});
//	}
//	else
//	{
//		enforceSingleAction();
//		window.location.href = "/ServletCharacterControl?type=setCharacterStatus&status="+slot+"&v="+window.verifyCode+"&char="+window.characterOverride;
//	}
//}

var popupStackCloseCallbackHandlers = [];
var currentPopupStackIndex = 0;
function incrementStackIndex()
{
	currentPopupStackIndex++;
    if (currentPopupStackIndex==1)
    {
		$("#page-popup-root").html("");
	    
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
		window.scrollTo(0,0);
		$("#page-popup-root").empty();
		$(".page-popup-newui").remove();
	}
	else
	{
		$("#page-popup"+currentPopupStackIndex).show();
		$("#page-popup"+(currentPopupStackIndex+1)).remove();
	}
	return currentPopupStackIndex;
}

function pagePopup(url, closeCallback, title, refreshFunctionJs)
{
	if (refreshFunctionJs==null)
		refreshFunctionJs = "reloadPagePopup()";
	
	if (currentPopupStackIndex==0)
		if (typeof window.panGrid === "function" && $(window).width()>=1200)
			panGrid(-$(window).width()/4, 0);
	
	if (url.indexOf("?")>0)
		url+="&ajax=true";
	else
		url+="?ajax=true";
	
	if (window.characterOverride!=null && window.characterOverride.length>10)
		url += "&char="+window.characterOverride;
	
	exitFullscreenChat();
	
	var stackIndex = incrementStackIndex();
	var pagePopupId = "page-popup"+stackIndex;
	
	//<div id='"+pagePopupId+"' class='location-controls-page'><div class='header1'><div class='header1-buttonbar'><div class='header1-buttonbar-inner'><div class='header1-button header1-buttonbar-left' onclick='reloadPagePopup()'>↻</div><div class='header1-buttonbar-middle'><div id='pagepopup-title'>"+popupTitle+"</div></div><div class='header1-button header1-buttonbar-right' onclick='closePagePopup()'>X</div></div></div></div><div class='main1 location-controls-page-internal'><div id='"+pagePopupId+"-content' class='location-controls' src='+url+'><img id='banner-loading-icon' src='/javascript/images/wait.gif' border=0/></div></div></div>
	$("#page-popup-root").append("<div id='"+pagePopupId+"' class='page-popup v3-window1'><a class='page-popup-Reload' onclick='"+refreshFunctionJs+"'>&#8635;</a><a class='page-popup-X' onclick='closePagePopup()'>X</a><div class='page-popup-title'><h4>"+title+"</h4></div><div class='page-popup-content' id='"+pagePopupId+"-content' src='"+url+"'><img id='banner-loading-icon' src='/javascript/images/wait.gif' border=0/></div><div class='mobile-spacer'></div></div>");
	$("#"+pagePopupId+"-content").load(url);
	$("body").scrollTo("#buttonbar");
	
	if (closeCallback!=null)
		popupStackCloseCallbackHandlers.push(closeCallback);
	else
		popupStackCloseCallbackHandlers.push(null);
}

function pagePopupHtml(title, html)
{
	exitFullscreenChat();
	
	var stackIndex = incrementStackIndex();
	var pagePopupId = "page-popup"+stackIndex;
	//<div id='"+pagePopupId+"' class='location-controls-page'><div class='header1'><div class='header1-buttonbar'><div class='header1-buttonbar-inner'><div class='header1-button header1-buttonbar-left' onclick='reloadPagePopup()'>↻</div><div class='header1-buttonbar-middle'><div id='pagepopup-title'>"+popupTitle+"</div></div><div class='header1-button header1-buttonbar-right' onclick='closePagePopup()'>X</div></div></div></div><div class='main1 location-controls-page-internal'><div id='"+pagePopupId+"-content' class='location-controls' src='+url+'><img id='banner-loading-icon' src='/javascript/images/wait.gif' border=0/></div></div></div>
	$("#page-popup-root").append("<div id='"+pagePopupId+"' class='page-popup v3-window1'><a class='page-popup-X' onclick='closePagePopup()'>X</a><div class='page-popup-title'><h4>"+title+"</h4></div><div class='page-popup-content' id='"+pagePopupId+"-content'>"+html+"</div><div class='mobile-spacer'></div></div>");
	$("body").scrollTo("#buttonbar");
}

function pagePopupIframe(url)
{
	
	if (url.indexOf("?")>0)
		url+="&ajax=true&char="+window.characterOverride;
	else
		url+="?ajax=true&char="+window.characterOverride;
	
	exitFullscreenChat();
	
	var stackIndex = incrementStackIndex();
	var pagePopupId = "page-popup"+stackIndex;
	$("#page-popup-root").append("<div id='"+pagePopupId+"' class='page-popup'><iframe id='"+pagePopupId+"-content' class='page-popup-iframe' src='"+url+"'><img id='banner-loading-icon' src='/javascript/images/wait.gif' border=0/></iframe></div>");
}

function closePagePopup(doNotCallback)
{
	var pagePopupId = "page-popup"+currentPopupStackIndex;
	if ($("#"+pagePopupId+"-map").length>0)
	{
		closeMap();
	}
	
	decrementStackIndex();
	
	if (currentPopupStackIndex==0)
	{
		if (typeof window.panGrid === "function" && $(window).width()>=1200)
			panGrid($(window).width()/4, 0);
		$(".half-page-variant").hide();
	}
	
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
    clearMakeIntoPopup();
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

	ga('send', 'pageview', url);	
	
	if (quietly==false)
		content.html("<img id='banner-loading-icon' src='/javascript/images/wait.gif' border=0/>");
	else
	{
		$(".page-popup-Reload").html("<img src='/javascript/images/wait.gif' border=0 style='margin-top:20px;'/>");
		$("#page-popup-reloadbutton").html("<img src='/javascript/images/wait.gif' border=0/>");	// new ui variant
	}

	if (content.is("iframe"))
	{
		content.attr('src', url);
	}
	else 
	{
		content.load(url, null, function(){
			$(".page-popup-Reload").html("&#8635;");
			$("#page-popup-reloadbutton").html("&#8635;");	// new ui variant
		});
	}
}

window.moveItemTimer = null;
function moveItem(event, itemId, newContainerKind, newContainerId)
{
	var destinationKey = newContainerKind+"_"+newContainerId;
	doCommand(event, "MoveItem", {itemId:itemId, destinationKey:destinationKey}, 
		function(data,error){
	        // Clear any previously set timer before setting a fresh one
	    	if(window.moveItemTimer != null)
	    		window.clearTimeout(window.moveItemTimer);
	    	window.moveItemTimer = setTimeout(function () {
	            reloadPagePopup(true);
	            window.moveItemTimer = null;
	        }, 2000);
	        
	        if(error) return;
	        $(event.currentTarget || event.target).text("");
		});
}

function loadInlineItemsAndCharacters()
{
	$("#inline-items").load("/odp/locationitemlist?ajax=true&char="+window.characterOverride);	
	$("#inline-characters").load("/odp/locationcharacterlist?ajax=true&char="+window.characterOverride);	
}

function loadInlineCollectables()
{
	$("#collectables-area").load("ajax_collectables.jsp?ajax=true&char="+window.characterOverride);	
}

function inventory(event)
{
	pagePopup("/odp/ajax_inventory.jsp", null, "Your Inventory");
}

function viewChangelog()
{
	pagePopup("/ajax_changelog.jsp", null, "Change Log");
}

function viewSettings()
{
	pagePopup("/odp/ajax_settings", null, "Game Settings");
}

function viewProfile()
{
	pagePopup("/odp/view_profile", null, "Your Profile");
}

function viewAutofix()
{
	pagePopup("/odp/autofix", null, "Autofix Tools");
}

function viewMap()
{
	exitFullscreenChat();
    closeAllPopupsTooltips();
	openMap();
	$("body").scrollTo("#buttonbar");
}

function viewContainer(containerId, title, closePopups)
{
	if(closePopups)
	{
		closeAllPopups();
		closeAllTooltips();
	}
	
	if(containerId == null)
		pagePopup("/odp/ajax_moveitems?preset=location", null, title || "Nearby Items");
	else
		pagePopup("/odp/ajax_moveitems?selfSide=Character_"+window.characterId+"&otherSide=Item_"+containerId, null, title);
}

function renameCharacter(eventObject, currentCharName)
{
	promptPopup("Rename Character", "Ok, what will you call your character?", currentCharName, function(name){
		doCommand(eventObject, "RenameUnnamedPlayer", {"newName":name});
	});
}

function deleteAndRecreateCharacter(event, currentCharName)
{
	confirmPopup("New Character", "Are you suuuure you want to delete your character and start over? It's permanent!", function(){
		if (currentCharName==null)
			currentCharName = "";
		promptPopup("New Character", "Ok, what will you call your new character?<br/><span style='font-size: 10px'>Add ! at the end for hardcore mode.</span>", currentCharName, function(name){
			showBannerLoadingIcon();
			closeAllPagePopups(true);
			doCommand(event, "DeleteAndRecreate", {"name":name}, clearPopupPermanentOverlay);
		});
	});
}

function doDrinkBeer(eventObject)
{
	doCommand(eventObject,"DrinkBeer");
}

function doLeaveAndForgetCombatSite(eventObject, pathId)
{
	clearMakeIntoPopup();
	showBannerLoadingIcon();
	doCommand(eventObject, "LeaveAndForgetCombatSite", {"pathId" : pathId}, clearPopupPermanentOverlay);
}

/**
 * Calls the command to forget the combat site
 * @param eventObject
 * @param locationId   This is the locationId of the combat site you want to forget.
 */
function doForgetCombatSite(eventObject, locationId)
{
	doCommand(eventObject, "ForgetCombatSite", {"locationId" : locationId});
}

/**
 * Calls the command to forget all combat sites/destroyed camps
 * @param eventObject
 * @param forgettableCombatSiteArray
 */
function doForgetAllCombatSites(eventObject, forgettableCombatSiteArray)
{
	confirmPopup("System Message","Are you sure you want to forget all combat sites in this location?", function()
	{
		clearMakeIntoPopup();
		doCommand(eventObject, "ForgetAllCombatSites", {"forgettableCombatSiteArray" : forgettableCombatSiteArray});
	});
}

/**
 * Calls the command to show all hidden sites.
 * @param eventObject
 */
function doShowHiddenSites(eventObject)
{
	doCommand(eventObject, "ShowHiddenSites");
}

function resendVerificationEmail()
{
	confirmPopup("Resend verification email", "Are you sure you need to resend the verification email? Be sure to check your spam box if you don't seem to be receiving it!", function(){
		enforceSingleAction();
		location.href = "/ServletUserControl?type=resendVerificationEmail"+"&v="+window.verifyCode+"&char="+window.characterOverride;
	});
	
}

function changeEmailAddress(oldEmail)
{
	promptPopup("Change email", "What email address would you like to use for your account?", oldEmail, function(value){
		enforceSingleAction();
		location.href = "/ServletUserControl?type=changeEmailAddress&email="+encodeURIComponent(value)+"&v="+window.verifyCode+"&char="+window.characterOverride;
	});
}

function viewReferrals()
{
	pagePopup("/ajax_referrals.jsp", null, "Latest Referral Visits");
}

function customizeItemOrderPage(itemId)
{
    closeAllPopupsTooltips();
	pagePopup("/ajax_customizeitem.jsp?itemId="+itemId, null, "Customization Order");
}

function orderItemCustomization(itemId, orderTypeId, requiredDetails)
{
	confirmPopup("Are you sure?", "This will send an email to a content developer notifying them that you'd like to customize an item.<br>You will be asked to provide some details in the next popup.", function(){
		promptPopup("Customization Details", requiredDetails, "", function(value){
			enforceSingleAction();
			location.href="/ServletUserControl?type=customItemOrder&itemId="+itemId+"&orderTypeId="+orderTypeId+"&v="+window.verifyCode+"&char="+window.characterOverride+"&requiredDetails="+encodeURIComponent(value);
		});
	});
}

function orderInstantNameFlavorCustomization(eventObject, itemId, itemName, flavor)
{
	confirmPopup("Are you sure?", "Do you want to order this item customization? It will occur automatically and if you make a mistake it might take some time to get it fixed by a dev.", function(){
		doCommand(eventObject, "CustomizationNameFlavor", {itemId:itemId, itemName:itemName, flavor:flavor});
	});
}

function doTriggerGlobal(event, globalId, attributes, entities, closeTools)
{
	doTriggerEffect(event, "Global", null, "global", globalId, attributes, entities, closeTools);
}

function doTriggerLocation(event, effectId, locationId, attributes, closeTools)
{
	doTriggerEffect(event, "Link", effectId, "location", locationId, attributes, null, closeTools);
}

function doTriggerItem(event, effectId, itemId, attributes, closeTools)
{
	doTriggerEffect(event, "Link", effectId, "item", itemId, attributes, null, closeTools);
}

function doTriggerEffect(event, effectType, effectId, sourceType, sourceId, attributes, entities, closeTools)
{
	if(closeTools == null || closeTools)
		closeAllTooltips();
	var params = {};
	params[sourceType + "Id"] = sourceId;
	if(effectId) params["scriptId"] = effectId;
	if(attributes) params["attributes"] = attributes;
	if(entities) params["entities"] = entities;
	doCommand(event, "Script"+effectType, params);
}

function doAttack(eventObject, charId)
{
    closeAllPopups();
    closeAllTooltips();
    doCommand(eventObject,"Attack",{"charId":charId});
}

function doLeaveParty(eventObject) {
	confirmPopup("Leave party", "Are you sure you want to leave your party?", function(){
		doCommand(eventObject, "PartyLeave");
	});
}

function joinParty(eventObject, partyCode) {
	doCommand(eventObject, "PartyJoin", {"inputType":"partyCode","partyCode": partyCode});
}

function joinPartyCharacter(eventObject, characterId) {
	doCommand(eventObject, "PartyJoin", {"inputType":"characterId","characterId":characterId});
}

function joinPartyCharacterName(eventObject, characterName) {
	doCommand(eventObject, "PartyJoin", {"inputType":"characterName","characterName":characterName});
}

//Old leave party function
function leaveParty(eventObject)
{
	confirmPopup("Leave party", "Are you sure you want to leave your party?", function(){
		doCommand(eventObject, "PartyLeave");
//		enforceSingleAction();
//		location.href = "/ServletCharacterControl?type=partyLeave"+"&v="+window.verifyCode+"&char="+window.characterOverride;
	});
}

var singleActionIssued = false;
function enforceSingleAction()
{
	if (singleActionIssued)
		throw "Attempted to execute more than one action at a time. This action has been cancelled.";
	
	singleActionIssued = true;
}

//function combatAttackWithLeftHand()
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=attack&hand=LeftHand"+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
//
//function combatAttackWithRightHand()
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=attack&hand=RightHand"+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
//
//function combatEscape()
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=escape"+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
//
//function combatAllowCharacterIn()
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=allowCharacterIn"+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
//
//function storeDisabled()
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=storeDisabled"+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
//
//function storeEnabled()
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=storeEnabled"+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}

////////////////////////////////////////////////////////
// BUTTON BAR TOGGLES
function toggleStorefront(eventObject)
{
	if (eventObject!=null)
		doCommand(eventObject, "ToggleStorefront", {"buttonId" : eventObject.currentTarget.id});
	else
		doCommand(eventObject, "ToggleStorefront");
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

function toggleHideUserActivity(eventObject)
{
	doCommand(eventObject, "ToggleHideUserActivity");
}

//function campsiteDefend()
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=defend"+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
//
//function leaveAndForgetCombatSite(pathId)
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=gotoAndForget&pathId="+pathId+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
//
//function forgetCombatSite(locationId)
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=forgetCombatSite&locationId="+locationId+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}

function groupAcceptJoinGroupApplication(eventObject, characterId)
{
	doCommand(eventObject, "GroupAcceptJoinApplication", {"characterId" : characterId});
}

function groupDenyJoinGroupApplication(eventObject, characterId)
{
	doCommand(eventObject, "GroupDenyJoinApplication", {"characterId" : characterId});
}

function groupMemberKick(eventObject, characterId, characterName)
{
	confirmPopup("Kick Group Member", "Are you sure you want to kick this group member ("+characterName+")?", function(){
		doCommand(eventObject, "GroupMemberKick", {"characterId" : characterId});
	});
}

//function groupMemberKickCancel(characterId)
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=groupMemberCancelKick&characterId="+characterId+""+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}

function groupRequestJoin(eventObject, groupId)
{
	doCommand(eventObject, "GroupRequestJoin", {"groupId" : groupId});
}

function groupMergeRequestsAllow(eventObject)
{
	doCommand(eventObject, "GroupMergeAllowRequests");
}

function groupMergeRequestsDisallow(eventObject)
{
	doCommand(eventObject, "GroupMergeDisallowRequests");
}

function groupMergeDenyApplication(eventObject, groupId)
{
	confirmPopup("Deny Merge Request", "Are you sure you want to deny this merge request?", function(){
		doCommand(eventObject, "GroupMergeDenyApplication", {"groupId" : groupId});
	});
}

function groupMergeAcceptApplication(eventObject, groupId)
{
	confirmPopup("Accept Merge Request", "Are you sure you want to accept this merge request? All group members and group houses will transfer to this group.", function(){
		doCommand(eventObject, "GroupMergeAcceptApplication", {"groupId" : groupId});
	});
}

function groupMergeSubmitRequest(eventObject, groupId)
{
	confirmPopup("Submit Merge Request", "Are you sure you want to submit this merge request? All group members and group houses will transfer to the group.<br/>Note that you can only have 1 active merge request at a time.", function(){
		doCommand(eventObject, "GroupMergeSubmitRequest", {"groupId" : groupId});
	});
}

function groupMergeCancelRequest(eventObject)
{
	confirmPopup("Cancel Merge Request", "Are you sure you want to cancel this merge request?", function(){
		doCommand(eventObject, "GroupMergeCancelRequest");
	});
}

//function tradeRemoveItem(itemId)
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=removeTradeItem&itemId="+itemId+""+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
//
//function tradeCancel()
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=tradeCancel"+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
//
//function tradeReady(version)
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=tradeReady&ver="+version+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
//
//function tradeAddItem(itemId)
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=addTradeItem&itemId="+itemId+""+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}
//
//function partyJoin(characterId)
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=partyJoin&characterId="+characterId+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}

function tradeStartTradeNew(eventObject,characterId)
{
	closeAllTooltips();
	doCommand(eventObject,"TradeStartTrade",{"inputType":"characterId","characterId":characterId},function(data,error){
		if (error) return;
		_viewTrade();
		popupMessage("Trade Started", data.tradePrompt);	
	})
}

function tradeStartTradeNewCharacterName(eventObject,characterName)
{
	closeAllTooltips();
	doCommand(eventObject,"TradeStartTrade",{"inputType":"characterName","characterName":characterName},function(data,error){
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

function tradeSetGoldNew(eventObject,currentDogecoin,curAvailDogecoin)
{
	promptPopup("Trade Gold", "How much gold do you want to add to the trade (out of "+ curAvailDogecoin +"):", currentDogecoin+"", function(amount){
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
	

//function duelRequest(characterId)
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=duelRequest&characterId="+characterId+"&v="+window.verifyCode+"&char="+window.characterOverride;
//}

function viewManageStore()
{
    closeAllPopups();
    closeAllTooltips();
    pagePopup("/odp/ajax_managestore.jsp", null, "Your Store");
}

function newCharacterFromDead(event)
{
	doCommand(event, "CharacterRespawn");
}

function switchCharacter(eventObject, characterId, direction)
{
	direction = direction || 0;
	doCommand(eventObject,"SwitchCharacter",{"characterId":characterId, "direction":direction},function()
	{
		var charParam = (window.characterOverride != null && window.characterOverride.length>10) ? ("?char=" + window.characterOverride) : "";
		if (location.href.indexOf("/main.jsp")>-1)
			window.history.replaceState({}, document.title, "/" + "main.jsp" + charParam);		
		else if (location.href.indexOf("/odp/experimental")>-1)
			window.history.replaceState({}, document.title, "/" + "odp/experimental" + charParam);
		else if (location.href.indexOf("/odp/full")>-1)
			window.history.replaceState({}, document.title, "/" + "odp/full" + charParam);	

		closeAllTooltips();
		clearMakeIntoPopup();
	});
}

function logout()
{
	enforceSingleAction();
	location.href = "/ServletUserControl?type=logout"+"&v="+verifyCode;
}

//function attackStructure()
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=attackStructure"+"&v="+verifyCode;
//}

//function allowDuelRequests()
//{
//	popupMessage("SYSTEM", "Dueling has been disabled (and has been for months) because the current combat system doesn't work well with it. We will re-enable it once we have a solution.");
//	return;
//	location.href = "/ServletCharacterControl?type=allowDuelRequests"+"&v="+verifyCode;
//}
//
//function disallowDuelRequests()
//{
//	popupMessage("SYSTEM", "Dueling has been disabled (and has been for months) because the current combat system doesn't work well with it. We will re-enable it once we have a solution.");
//	return;
//	location.href = "/ServletCharacterControl?type=disallowDuelRequests"+"&v="+verifyCode;
//}

function viewStore(characterId)
{
	pagePopup("/odp/ajax_viewstore.jsp?characterId="+characterId+"", null, "Merchant Store");
}

//function setBlockadeRule(rule)
//{
//	enforceSingleAction();
//	location.href = "/ServletCharacterControl?type=setBlockadeRule&rule="+rule+"&v="+verifyCode;
//}

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

function doEatCandy(eventObject)
{	
	var itemId = $("#popupItemId").val();
	if (itemId == null) return;
	doCommand(eventObject,"EatBerry",{"itemId":itemId},function(data,error){
		if (error) return;
		reloadPagePopup();
		popupMessage("System Message", "You eat the ancient candy bar and your insides gurgle back at you. You probably shouldn't eat too much of this. Who knows what could happen?");
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
function doDeleteCharacter(eventObject,characterId,characterName)
{
	confirmPopup("Delete Character","Are you sure you want to delete " + characterName + "?",function(){
		confirmPopup("Delete Character","I mean, are you REALLY sure you want to delete " + characterName + "? You can't take it back!",function(){
			doCommand(eventObject,"UserDeleteCharacter",{"characterId":characterId},function(data,error){
				if(error) return;
				$("a[onclick='switchCharacter(eventObject, " + characterId +")']").parent("li").remove();
			});
		});
	});
}

function doCombatEscape(event)
{
	doCommand(event, "CombatEscape");
}

function doCombatAttackLeftHand(event)
{
	doCommand(event, "CombatAttack", {"hand":"LeftHand"});
}

function doCombatAttackRightHand(event)
{
	doCommand(event, "CombatAttack", {"hand":"RightHand"});
}

function viewExchange()
{
	pagePopup("/odp/ajax_exchange", null, "Exchange");
}

function viewTrainingQuestLines()
{
	closeAllPopups();
	loadQuestWindow("/odp/trainingquestlist");
}

function viewQuests()
{
	pagePopup("/odp/questlist", null, "Available Quests");
}

function viewQuest(keyString)
{
	createQuestWindow("<div id='quest-id-"+keyString+"'><div style='text-align:center'><img src='/javascript/images/wait.gif' border=0/></div></div>");
	
	$("#quest-id-"+keyString).load("/odp/quest?key="+keyString+"&char="+window.characterOverride);
}

function createQuestWindow(html) 
{
	$(".popupBlurrable").addClass("blur");
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
	
	
	
	var windowHtml = "";
	windowHtml += 
			"<div class='quest-window-container'>" +
			"<div class='quest-window'>" +
			"<div class='quest-window-internal'>";
	
	windowHtml+=html;
	
	windowHtml+=
			"</div>";
	
	windowHtml+=
			"<div class='quest-window-bottombutton-container'>"+
            "<div class='quest-window-bottombutton' onclick='closepopupMessage(" + popupsNum +")'>"+
            "Okay"+
            "</div>"+
            "</div>"+
            
			"</div>"+
			"</div>";

	
	
    $("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground" onclick="closepopupMessage('+popupsNum+')"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper" onclick="event.stopPropagation();">'+windowHtml+'</div></div>');
    expandpopupMessage();
    enterPopupClose();
}




function loadQuestWindow(url) 
{
	$(".popupBlurrable").addClass("blur");
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
	
	
	
	var windowHtml = "";
	windowHtml += 
			"<div class='quest-window-container'>" +
			"<div class='quest-window'>" +
			"<div class='quest-window-internal'>";
	
	windowHtml+="<img class='wait' src='/javascript/images/wait.gif' border='0'/>";
	
	windowHtml+=
			"</div>";
	
	windowHtml+=
			"<div class='quest-window-bottombutton-container'>"+
            "<div class='quest-window-bottombutton' onclick='closepopupMessage(" + popupsNum +")'>"+
            "Okay"+
            "</div>"+
            "</div>"+
            
			"</div>"+
			"</div>";

	
	
    $("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground" onclick="closepopupMessage('+popupsNum+')"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper" onclick="event.stopPropagation();">'+windowHtml+'</div></div>');
    expandpopupMessage();
    enterPopupClose();
    
    $(".quest-window-internal").load(url);
}


function createMapWindow(html) 
{
	$(".popupBlurrable").addClass("blur");
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
	
	
	
	var windowHtml = "";
	windowHtml += 
			"<div class='overheadmap-window'>" +
			"<div class='map-contents global-navigation-map overheadmap-window-internal'>";
	
	windowHtml+=html;
	
	windowHtml+=
			"</div>";
	
	windowHtml+="</div>";

	windowHtml+="<a class='overheadmap-X' onclick='closepopupMessage(" + popupsNum +")'>X</a>";
	
	
    $("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground" onclick="closepopupMessage('+popupsNum+')"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper" onclick="event.stopPropagation();">'+windowHtml+'</div></div>');
    expandpopupMessage();
    enterPopupClose();
}



function createWelcomeWindow() 
{
	$(".popupBlurrable").addClass("blur");
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
	
	
	
	var windowHtml = "";
	windowHtml += 
			"<div class='quest-window-container'>" +
			"<div class='quest-window'>" +
			"<div class='quest-window-internal'>";
	
	windowHtml+="<h2>Welcome to Initium!</h2>" +
			"<p>You're about to begin playing the game by following a short series of quests. This is highly recommended for new players as it is a fun and engaging way to learn " +
			"how to play the game. If you have any questions, feel free to speak up in <b>Global</b> chat, our community can be a bit weird but they're generally very helpful!</p>" +
			"";
	
	windowHtml+=
			"</div>";
	
	windowHtml+="" +
			"<a id='header-mute' onclick='toggleEnvironmentSoundEffects()' style='position: absolute;left: -69px;bottom: -43px;text-shadow: 1px 1px 1px #000000;width: 244px;'>" +
			"<img id='header-mute' src='https://initium-resources.appspot.com/images/ui/sound-button1.png' border='0' style='max-height:18px;vertical-align: bottom;-webkit-filter: drop-shadow(1px 1px 0px #000000);filter: drop-shadow(1px 1px 0px #000000);'/> " +
			"Click here to disable sounds" +
			"</a>";
	
	windowHtml+=
			"<div class='quest-window-bottombutton-container'>"+
            "<div class='quest-window-bottombutton' style='left:-163px; background-image:url(https://initium-resources.appspot.com/images/ui3/button-red.png);' onclick='closepopupMessage(" + popupsNum +")'>"+
            "Skip"+
            "</div>"+
            "<div class='quest-window-bottombutton' style='left:0px' onclick='closepopupMessage(" + popupsNum +");viewQuest(\"ag1zfnBsYXlpbml0aXVtchULEghRdWVzdERlZhiAgJD-ncrKCQw\");'>"+
            "Okay"+
            "</div>"+
            "</div>"+
            
			"</div>"+
			"</div>";

	
	
    $("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground" onclick="closepopupMessage('+popupsNum+')"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper" onclick="event.stopPropagation();">'+windowHtml+'</div></div>');
    expandpopupMessage();
    enterPopupClose();
}

/*
<form action='https://www.paypal.com/cgi-bin/webscr' method='post' target='_top'>
<input type='hidden' name='custom' value='5299602559336448'> 
<input type='hidden' name='cmd' value='_donations'> 
<input type='hidden' name='business' value='narmstrong@playinitium.com'>
<input type='hidden' name='amount' value='5.00'>
<input type='hidden' name='currency_code' value='USD'>
<input type='hidden' name='item_name' value='Initium Development'>
<input type='hidden' name='tax' value='0'>
<input type='image' src='https://initium-resources.appspot.com/images/ui/paypal-donate-button.png' border='0' name='submit' alt='PayPal - The safer, easier way to pay online!'> 
<img alt='' border='0' src='https://www.paypalobjects.com/en_US/i/scr/pixel.gif' width='1' height='1'>
</form>
*/

function createDonationWindow() 
{
	$(".popupBlurrable").addClass("blur");
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
	
	
	
	var windowHtml = "";
	windowHtml += 
			"<div class='quest-window-container'>" +
			"<div class='quest-window'>" +
			"<div class='quest-window-internal'>";
	
	windowHtml+="<h2 style='text-align:center;'>The cost of a full account will soon increase!</h2>" +
			"<p>Currently, while Initium is still in development, you can get a full premium account for only <b>$5.00</b> however the cost for a premium account will " +
			"increase once the game is officially launched. Take advantage of this pre-launch price by donating at least 5 dollars.</p>" +
			"<p>" + 
			"Premium account features include:" +
			"<ul>" +
			"<li>Other players can rescue you if you go unconscious</li>" +
			"<li>Multiple characters on the same account (8 character slots to start)</li>" +
			"</ul>" +
			"</p>" +
			"<p>Getting a premium account now will ensure your account will be grandfathered in when the price structure changes!</p>" +
			"<p>" +
			"<center>" +
			"<form id='donatequick' action='https://www.paypal.com/cgi-bin/webscr' method='post' target='_blank' onsubmit='setTimeout(function(){closepopupMessage(" + popupsNum +"); createWelcomeWindow();}, 1000)'>" +
			"<input type='hidden' name='custom' value='"+window.userId+"'> " +
			"<input type='hidden' name='cmd' value='_donations'> " +
			"<input type='hidden' name='business' value='narmstrong@playinitium.com'>" +
			"<input type='hidden' name='amount' value='5.00'>" +
			"<input type='hidden' name='currency_code' value='USD'>" +
			"<input type='hidden' name='item_name' value='Initium Development'>" +
			"<input type='hidden' name='tax' value='0'>" +
			"<input type='image' src='https://initium-resources.appspot.com/images/ui/paypal-donate-button.png' border='0' id='donatequick-submit' name='submit' alt='PayPal - The safer, easier way to pay online!'>" + 
			"<img alt='' border='0' src='https://www.paypalobjects.com/en_US/i/scr/pixel.gif' width='1' height='1'>" +
			"</form>" +
			"</center>" +
			"</p>";
	
	windowHtml+=
			"</div>";
	
	windowHtml+="" +
			"<a id='header-mute' onclick='toggleEnvironmentSoundEffects()' style='position: absolute;left: -69px;bottom: -43px;text-shadow: 1px 1px 1px #000000;width: 244px;'>" +
			"<img id='header-mute' src='https://initium-resources.appspot.com/images/ui/sound-button1.png' border='0' style='max-height:18px;vertical-align: bottom;-webkit-filter: drop-shadow(1px 1px 0px #000000);filter: drop-shadow(1px 1px 0px #000000);'/> " +
			"Click here to disable sounds" +
			"</a>";
	
	windowHtml+=
			"<div class='quest-window-bottombutton-container'>"+
            "<div class='quest-window-bottombutton' style='left:-163px; background-image:url(https://initium-resources.appspot.com/images/ui3/button-red.png);' onclick='closepopupMessage(" + popupsNum +"); createWelcomeWindow();'>"+
            "Skip"+
            "</div>"+
            "<div class='quest-window-bottombutton' style='left:0px' onclick='$(\"#donatequick\").submit(); closepopupMessage(" + popupsNum +"); createWelcomeWindow();'>"+
            "Okay"+
            "</div>"+
            "</div>"+
            
			"</div>"+
			"</div>";

	
	
    $("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground" onclick="closepopupMessage('+popupsNum+')"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper" onclick="event.stopPropagation();">'+windowHtml+'</div></div>');
    expandpopupMessage();
    enterPopupClose();
}


function createUpgradeToPremiumWindow() 
{
	$(".popupBlurrable").addClass("blur");
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
	
	
	
	var windowHtml = "";
	windowHtml += 
			"<div class='quest-window-container'>" +
			"<div class='quest-window'>" +
			"<div class='quest-window-internal' style='padding-bottom:0px;'>";
	
	if (window.isPremium==true)
	{
		windowHtml+="" +
		"<h4>Thank-you for your continued support!</h4>";
	
	}
	else
	{
		windowHtml+="" +
				"<h4>Upgrade to premium for $5.00</h4>" +
				"<p><b>The cost of a full account will soon increase!</b></p>" +
				"<p>Currently, while Initium is still in development, you can get a full premium account for only <b>$5.00</b> however the cost for a premium account will " +
				"increase once the game is officially launched. Take advantage of this pre-launch price by donating at least 5 dollars.</p>" +
				"<p>" +
				"Premium account features include:" +
				"<ul>" +
				"<li>Other players can rescue you if you go unconscious</li>" +
				"<li>Multiple characters on the same account (8 character slots to start)</li>" +
				"</ul>" +
				"</p>" +
				"<p>Getting a premium account now will ensure your account will be grandfathered in when the price structure changes!</p>" +
				"<p><b>ALSO..</b></p>";
	}
	windowHtml+="" +
	"<p>Please be aware that donations of $25 or more will result in DOUBLE the donation credit (as though you donated twice as much) which you can use to buy cosmetic item customizations, trade with other players, buy more character slots, and gift premium memberships to those less fortunate.</p>";
	
	windowHtml+=
			"</div>";
	
	windowHtml+="" +
			"<center style='position: absolute; bottom: -39px; width: 100%;'>" +
			"<form id='donatequick' action='https://www.paypal.com/cgi-bin/webscr' method='post' target='_blank' onsubmit='setTimeout(function(){closepopupMessage(" + popupsNum +"); }, 1000)'>" +
			"<input type='hidden' name='custom' value='"+window.userId+"'> " +
			"<input type='hidden' name='cmd' value='_donations'> " +
			"<input type='hidden' name='business' value='narmstrong@playinitium.com'>" +
			"<input type='number' name='amount' id='donate-amount' value='5.00' placeholder='Donation amount'><br>" +
			"<input type='hidden' name='currency_code' value='USD'>" +
			"<input type='hidden' name='item_name' value='Initium Development'>" +
			"<input type='hidden' name='tax' value='0'>" +
			"<input type='image' src='https://initium-resources.appspot.com/images/ui/paypal-donate-button.png' border='0' id='donatequick-submit' name='submit' alt='PayPal - The safer, easier way to pay online!' style='cursor:pointer;'><br>" + 
			"<img alt='' border='0' src='https://www.paypalobjects.com/en_US/i/scr/pixel.gif' width='1' height='1'>" +
			"</form>" +
			"</center>" +
			"<a id='header-mute' onclick='toggleEnvironmentSoundEffects()' style='position: absolute;left: -69px;bottom: -43px;text-shadow: 1px 1px 1px #000000;width: 244px;'>" +
			"<img id='header-mute' src='https://initium-resources.appspot.com/images/ui/sound-button1.png' border='0' style='max-height:18px;vertical-align: bottom;-webkit-filter: drop-shadow(1px 1px 0px #000000);filter: drop-shadow(1px 1px 0px #000000);'/> " +
			"Click here to disable sounds" +
			"</a>";
	
	windowHtml+=
			"<div class='quest-window-bottombutton-container'>"+
            "<div class='quest-window-bottombutton' style='left:-163px; background-image:url(https://initium-resources.appspot.com/images/ui3/button-red.png);' onclick='closepopupMessage(" + popupsNum +"); '>"+
            "Skip"+
            "</div>"+
            "<div class='quest-window-bottombutton' style='left:0px' onclick='$(\"#donatequick\").submit(); closepopupMessage(" + popupsNum +"); '>"+
            "Okay"+
            "</div>"+
            "</div>"+
            
			"</div>"+
			"</div>";

	
	
    $("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground" onclick="closepopupMessage('+popupsNum+')"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper" onclick="event.stopPropagation();">'+windowHtml+'</div></div>');
    expandpopupMessage();
    enterPopupClose();
}




function viewInvention()
{
	closeAllPagePopups();
	var params = "?selected2DTileX="+selectedTileX+"&selected2DTileY="+selectedTileY; 
	pagePopup("/odp/invention"+params, null, "Invention", "viewInvention()");
}

function combineChippedTokens(event, itemId)
{
	doCommand(event, "CombineChippedTokens", {itemId:itemId}, function(){
		closeAllTooltips();		
	});
}

function splitPremiumToken(event, itemId)
{
	doCommand(event, "SplitPremiumToken", {itemId:itemId}, function(){
		closeAllTooltips();		
	});
}

function doRenameConstructItemSkill(event, currentName, skillId)
{
	closeAllTooltips();
	
	promptPopup("Rename Skill", "Provide a new name for this skill. You may use letters, numbers, spaces, or any of these characters: . ' #", currentName, function(name){
		doCommand(event, "ConstructItemSkillRename", {skillId:skillId,name:name});
	});
}

function doForgetConstructItemSkill(event, name, skillId)
{
	closeAllTooltips();
	
	confirmPopup("Permanently Forget Skill", "Are you sure you want to forget the "+name+" skill? You cannot undo this operation.", function(){
		doCommand(event, "ConstructItemSkillForget", {skillId:skillId});
	});
}

function doBuyCharacterSlots(event)
{
	confirmPopup("Get more character slots", "Are you sure you want to get 8 more character slots for $5 in donation credit?", function(){
		doCommand(event, "BuyCharacterSlots");
	});
}

function doFireplaceLight(event, itemKey)
{
	closeAllTooltips();
	
	doCommand(event, "FireplaceLight", {itemKey:itemKey});
}


function doFireplaceAddFuel(event, itemKey)
{
	closeAllTooltips();
	
	doCommand(event, "FireplaceAddFuel", {itemKey:itemKey});
}

var lastUpdate = null;
var queuedMainPageUpdates = [];
function requestUpdateMainPage(updateList)
{
	var needsToWait = queuedMainPageUpdates.length==0;
	
	// Add all the new updates we need to do to the queue if they aren't already there
	var newUpdateList = [];
	if (updateList!=null)
		newUpdateList = updateList.split(",");
	for(var i = 0; i<newUpdateList.length; i++)
		if (queuedMainPageUpdates.indexOf(newUpdateList[i])==-1)
			queuedMainPageUpdates.push(newUpdateList[i]);

	if (needsToWait)
	{
		setTimeout(requestUpdateMainPage, 500);
		return;
	}
	
	var currentTime = new Date().getTime();
	if (lastUpdate==null || lastUpdate<currentTime-3000)
	{
		var data = "";
		var firstTime = true;
		for(var i = 0; i<queuedMainPageUpdates.length; i++)
		{
			if (firstTime)
				firstTime = false;
			else
				data+=",";
			data+=queuedMainPageUpdates.pop();
		}
		doCommand(null, "RequestMainPageUpdate", {updateList:data, newUI:window.newUI});
		lastUpdate = currentTime;
	}
	else
	{
		setTimeout(requestUpdateMainPage, 500);
	}
}

function playSoundsFromNotification(soundList)
{
	if (isSoundEffectsEnabled()==false) return;
	
	var sounds = [];
	if (soundList!=null)
		sounds = soundList.split(",");
	
	for(var i = 0; i<sounds.length; i++)
		playAudio(sounds[i], 0.3);
}

function restartNoobQuests()
{
	confirmPopup("Reset quests?", "Are you sure you want to restart this quest line?<br>You will lose all progress on it if you do this.", function(){
		doCommand(event, "RestartNoobQuests");
	});
}

function loadRelatedSkills(itemKey)
{
	doCommand(event, "RelatedSkillsUpdate", {itemKey:itemKey});
}

function makeIntoPopupFromUrl(url, title, disableGlass)
{
	clearMakeIntoPopup();
	
	window.scrollTo(0,0);
	if (disableGlass!=true)
		$(".make-popup-root").append("<div onclick='clearMakeIntoPopup()' class='make-popup-underlay'></div>");
	$(".make-popup-root").append("<div style='position:absolute; left:50%; top:10%;z-index:10000000;'><div class='main-buttonbox v3-window3 make-popup make-popup-html' style='position:relative; margin-left:-50%!important;left:0px;'>" +
			"	<a class='make-popup-X' onclick='clearMakeIntoPopup()'>X</a>" +
			"	<h4>"+title+"</h4>" +
			"<div id='make-popup-url-contents'><img class='wait' src='/javascript/images/wait.gif' border='0'/></div></div></div>");

	$("#make-popup-url-contents").load(url);
}



function makeIntoPopupHtml(html, disableGlass)
{
	clearMakeIntoPopup();
	
	window.scrollTo(0,0);
	if (disableGlass!=true)
		$(".make-popup-root").append("<div onclick='clearMakeIntoPopup()' class='make-popup-underlay'></div>");
	html = "<a class='make-popup-X' onclick='clearMakeIntoPopup()'>X</a>" + html;
	$(".make-popup-root").append("<div style='position:absolute; left:50%; top:10%;'><div class='main-buttonbox v3-window3 make-popup make-popup-html' style='position:relative; margin-left:-50%!important;left:0px;'>"+html+"</div></div>");
}

function isMakePopupOpen()
{
	return $(".make-popup").length>0;
}

function makeIntoPopup(jquerySelector)
{
	clearMakeIntoPopup();
	
	window.scrollTo(0,0);
	$(".make-popup-root").append("<div onclick='clearMakeIntoPopup()' class='make-popup-underlay'></div>");
	$(jquerySelector).addClass("make-popup").prepend("<a class='make-popup-X' onclick='clearMakeIntoPopup()'>X</a>");
}

function clearMakeIntoPopup()
{
	$(".make-popup-html").remove();
	$(".make-popup").removeClass("make-popup");
	$(".make-popup-underlay").remove();
	$(".make-popup-X").remove();
	$(".make-popup-removable").remove();
}

function clearMiniPagePopup()
{
	$(".mini-page-popup").html("").hide();
}



function miniPagePopup(url, title)
{
	window.scrollTo(0,0);
	
	var html = 
			"<a class='mini-page-popup-reload' onclick='reloadMiniPagePopup()'>&#8635;</a>	\r\n" + 
			"<a class='mini-page-popup-X' onclick='clearMiniPagePopup()'>X</a>	\r\n" + 
			"<h4>"+title+"</h4>\r\n" + 
			"<div id='mini-page-popup-contents'>\r\n" + 
			"	<img class='wait' src='/javascript/images/wait.gif' border='0'/>\r\n" + 
			"</div>\r\n";
	
	$(".mini-page-popup").html(html).show();

	$("#mini-page-popup-contents").load(url).attr("url", url);
}

function getMiniPagePopupTitle()
{
	if (isMiniPagePopupActive())
		return $(".mini-page-popup h4").text();
	
	return null;
}

function isMiniPagePopupActive()
{
	return $(".mini-page-popup").is(':visible');
}

function reloadMiniPagePopup()
{
	if (isMiniPagePopupActive())
	{
		var url = $("#mini-page-popup-contents").attr("url");
		if (url==null) return;
		$("#mini-page-popup-contents").load(url);
	}
}



function viewCharacterSwitcher()
{
	$("body").append("" +
			"<div id='characterswitcher-container' class='main-buttonbox v3-window3 make-popup make-popup-removable'>" +
			"	<a class='make-popup-X' onclick='clearMakeIntoPopup()'>X</a>" +
			"	<h4>Switch Characters</h4>" +
			"	<div id='characterswitcher'><img class='wait' src='/javascript/images/wait.gif' border='0'/></div>" +
			"</div>" +
			"<div onclick='clearMakeIntoPopup()' class='make-popup-underlay'></div>");
	$("#characterswitcher").load("/odp/characterswitcher?char="+window.characterOverride);
}

function createNewCharacter(event)
{
	clearMakeIntoPopup();
	
	promptPopup("New Character", "Give your new character a name:<br/><span style='font-size: 10px'>Add ! at the end for hardcore mode.</span>", "", function(name){
		doCommand(event, "NewCharacter", {name:name}, function(){
			clearMakeIntoPopup();
			$("#characterswitcher-container").remove();
		});
	});
}

function mustCreateNewCharacter()
{
	promptPopup("New Character", "It seems your character died a while ago and we no longer know " + 
			"what your old character\'s name was. <br>Give your new character a name:<br/><span style='font-size: 10px'>Add ! at the end for hardcore mode.</span>", "", function(name){
		doCommand(event, "NewCharacter", {name:name}, function(){
		});
	});
}

function toggleMainPullout()
{
	var mainPullout = $("#main-pullout");
	
	if ($("#main-pullout:visible").length>0)
	{
		// It's visible, make it invisible
		mainPullout.hide();
		$(".hiddenPullout-underlay").remove();
	}
	else
	{
		// It's NOT visible, make it visible
		mainPullout.before("<div class='hiddenPullout-underlay' onclick='toggleMainPullout()'></div>");
		mainPullout.show();
	}
}

function storeNewBuyOrder(event)
{
	promptPopup("New Buy Order", "Provide the name of the item you want to buy", "", function(itemName){
		promptPopup("New Buy Order", "How many do you want to buy? Leave this blank if there is no limit.", "1", function(quantity){
			promptPopup("New Buy Order", "How much will you buy each unit for?", "", function(gold){
				doCommand(event, "StoreNewBuyOrder", {itemName:itemName,quantity:quantity,value:gold}, function(data){
					 $("#buyOrders").prepend(data.createBuyOrder);
				});
			}, function(){return;});
		}, function(){return;});
	}, function(){return;});
}

function storeBuyOrderExecute(event, buyOrderId, itemId)
{
	confirmPopup("Sell your stuff", "Are you sure you want to sell all of this item? <br>(If you want to sell only a partial amount, you will have to split the stack first in your inventory)", function(){
		doCommand(event, "StoreBuyOrderExecute", {buyOrderId:buyOrderId, itemId:[itemId]}, function(data){
			// Refresh the popup so we can sell another
			$("#buyordercompatibleitemslist-container").html(
					"	<a class='make-popup-X' onclick='clearMakeIntoPopup()'>X</a>" +
					"	<h4>Item picker</h4>" +
					"	<div id='buyordercompatibleitemslist'><img class='wait' src='/javascript/images/wait.gif' border='0'/></div>");
			$("#buyordercompatibleitemslist").load("/odp/buyordercompatibleitemslist?buyOrderId="+buyOrderId+"&char="+window.characterOverride);
		});
	});
}

function storeBuyOrderExecuteAll(event, buyOrderId)
{
	var itemIds = [];
	var items = $("#buyordercompatibleitemslist").children(".selectable");
	for(var i = 0; i<items.length; i++)
	{
		var id = items.get(i).id;
		itemIds.push(parseInt(id));
	}
	
	confirmPopup("Sell your stuff", "Are you sure you want to sell EVERYTHING you are holding that matches this buy order?<br>(Only 20 can be sold at a time)", function(){
		doCommand(event, "StoreBuyOrderExecute", {buyOrderId:buyOrderId, itemId:itemIds}, function(data){
			$("#buyordercompatibleitemslist-container").html(
					"	<a class='make-popup-X' onclick='clearMakeIntoPopup()'>X</a>" +
					"	<h4>Item picker</h4>" +
					"	<div id='buyordercompatibleitemslist'><img class='wait' src='/javascript/images/wait.gif' border='0'/></div>");
			$("#buyordercompatibleitemslist").load("/odp/buyordercompatibleitemslist?buyOrderId="+buyOrderId+"&char="+window.characterOverride);
		});
	});
}

function viewBuyOrderOptions(event, buyOrderId)
{
	$("body").append("" +
			"<div id='buyordercompatibleitemslist-container' class='main-buttonbox v3-window3 make-popup make-popup-removable'>" +
			"	<a class='make-popup-X' onclick='clearMakeIntoPopup()'>X</a>" +
			"	<h4>Item picker</h4>" +
			"	<div id='buyordercompatibleitemslist'><img class='wait' src='/javascript/images/wait.gif' border='0'/></div>" +
			"</div>" +
			"<div onclick='clearMakeIntoPopup()' class='make-popup-underlay'></div>");
	$("#buyordercompatibleitemslist").load("/odp/buyordercompatibleitemslist?buyOrderId="+buyOrderId+"&char="+window.characterOverride);
}

function storeDeleteBuyOrder(event, buyOrderId)
{
	doCommand(event, "StoreDeleteBuyOrder", {buyOrderId:buyOrderId});
	
}

function closeClosableMessage(id)
{
	localStorage.setItem("closableMessage-"+id, true);
	
	$("#"+id).hide();
}

function initializeClosableMessage(id)
{
	if (localStorage.getItem("closableMessage-"+id)!=null)
		$("#"+id).hide();
}

function viewGlobeNavigation()
{
	var mapData = $(".global-navigation-map");
	if (mapData.length==0) return;
	html = $(mapData[mapData.length-1]).html();
	createMapWindow(html);
}

function view2DView(forcedMode)
{
	if (forcedMode==false || (forcedMode==null && $("body").attr("bannerstate")==="location-2d"))
		viewBannerDefault();
	else
	{
		$("body").attr("bannerstate", "location-2d");
		
		// Call the center function to view the banner centered
		centerGridOnScreen();
	}

}

function ensureNotInCombatView()
{
	var val = $("body").attr("bannerstate");
	if (val=="combat-2d")
		$("body").attr("bannerstate", "location-2d");
}

function setBannerSizeMode(mode)
{
	bannerSizeMode = mode;
	updateBannerWeatherSystem();
}

function drawTravelLine(event, startX, startY, x, y, seconds)
{
	$(".minimap-button-cancel").show();
	var maps = $(".map-contents");
	for(var i = 0; i<maps.length; i++)
	{
		var map = $(maps[i]);
		var scale = map.css("transform");
		if (scale!=null && scale!="none")
		{
			scale = scale.replace("scale(", "").replace(")", "");
			scale = parseFloat(scale);
		}
		else
			scale = 1;
		
		if (map.hasClass("minimap-contents"))
			scale*=0.6;
		
		map.append("" +
				"<svg class='travel-line-container' style='position:absolute;left:0px; top:0px;bottom:0px;right:0px;width:100%;height:100%;z-index:1;'>" +
				"	<line class='travel-line' x1='"+startX+"' y1='"+startY+"' x2='"+startX+"' y2='"+startY+"'/>" +
				"</svg>"
				).append("<a class='travel-line-cancel-button' onclick='cancelLongOperations(event)'><img src='https://initium-resources.appspot.com/images/ui4/button-cancel1.png' alt='Cancel Travel'/></a>");
		
		
		map[0].travelLineData = 
		{
			startTime:new Date().getTime(),
			endTime:new Date().getTime()+((seconds+1)*1000),
			x1:startX,
			y1:startY,
			x2:x*scale,
			y2:y*scale
		};
		
		setTimeout(updateTravelLine, 100);
	}
}

function cancelTravelLine()
{
	$(".minimap-button-cancel").hide();
	
	var maps = $(".map-contents");
	for(var i = 0; i<maps.length; i++)
	{
		var map = $(maps[i]);
		map[0].travelLineData = null;
	}
	$(".travel-line-container").remove();
	$(".travel-line-cancel-button").remove();
}

function updateTravelLine()
{
	var maps = $(".map-contents");
	for(var i = 0; i<maps.length; i++)
	{
		var map = $(maps[i]);
		var data = map[0].travelLineData;
		if (data==null) continue;
		
		var elapsed = new Date().getTime()-data.startTime;
		var percentComplete = elapsed/(data.endTime-data.startTime);
		if (percentComplete>1) percentComplete = 1;
		
		var x = (data.x2-data.x1)*percentComplete;
		var y = (data.y2-data.y1)*percentComplete;
		
		x+=data.x1;
		y+=data.y1;
		
		var travelLine = map.find(".travel-line");
		var parent = travelLine.parent();
		
		var canvasWidth = parent.width();
		var canvasHeight = parent.height();
		
		travelLine.attr("x1", data.x1+(canvasWidth/2)).attr("y1", data.y1+(canvasHeight/2)).attr("x2",x+(canvasWidth/2)).attr("y2",y+(canvasHeight/2));
		
		if (percentComplete<1)
		{
			setTimeout(updateTravelLine, 100);
		}
	}
	
}

function isGlobeNavigationVisible()
{
	if ($(".global-navigation-map").length>1)
		return true;
	
	return false;
}

//function addOverheadMapCell(locationName, locationId, imageUrl, positionX, positionY)
//{
//	positionX = positionX*100+"px";
//	positionY = positionY*100+"px";
//	
//	var html = 
//		  "<div class='overheadmap-cell-container-base'>"; 
//	html+="		<a onclick='doGoto(event, "+locationId+", false);' class='path-overlay-link overheadmap-cell-container' style='left:"+positionX+"; top:"+positionY+";'>";
//    html+="			<div class='overheadmap-cell-label-container'>";
//    html+="				<div class='label'>"+locationName+"</div>";
//    html+="			</div>";
//    html+="			<img src='"+imageUrl+"'/>";
//    html+="		</a>";
//    html+="</div>";
//    
//    $("#overheadmap-cells").append(html);
//}

function questLineCompletePopup(questLineName)
{
	createQuestWindow("<h4>'"+questLineName+"' Quest Line Complete!</h4>" +
			"<p>" +
			"Congratulations you completed a quest line. If you'd like to learn about another part of the game, try " +
			"another training quest line." +
			"</p>" +
			"" +
			"<h3 id='quest-complete-label' style='position:initial; '>Complete!</h3>" +
			"<center><a class='standard-button-highlight' onclick='viewTrainingQuestLines();'><img alt='Training quests' src='https://initium-resources.appspot.com/images/ui3/quest-banners/button-training-quests1.png'/></a></center>" +
			"");
	genericOverlayEffect("https://initium-resources.appspot.com/images/ui3/complete-effect.gif?1", 500);
}

function beginNoobQuests(questDefId)
{
	confirmPopup("Begin quest line?", "Are you sure you want to begin this quest line? If you already have progress on this quest line your progress will be reset. Are you sure?", function()
	{
		closeAllPopups();
		doCommand(null, "BeginNoobQuests", {questDefId:questDefId}, function(data)
		{
			if (data.error) return;
			
			viewQuest(data.questDefKey);
		});
	});
}

function beginQuest(event, itemId)
{
	doCommand(event, "BeginQuest", {itemId:itemId}, function(data){
		if (data.error) return;
		
		viewQuest(data.questDefKey);
	});
}

function viewNoobWindow()
{
	var html = "" +
			"<h4>Need some help?</h4>" +
			"<h5>Do the 'noob' quests</h5>" +
			"<p>Follow the noob quests, they teach you how to play the game and they don't take very long to complete. Do so by clicking the big white ! button on the button bar to open your quest list. <br><a onclick='clearMakeIntoPopup();viewQuests()'>Your quests list</a></p>" +
			"<h5>Got more questions you need answers to?</h5>" +
			"<p>This is a FAQ made by players, for players. Common questions and their answers. You can find this by typing /faq in chat as well.<br><a onclick='clearMakeIntoPopup();' href='http://initium.wikia.com/wiki/Staub%27s_FAQ_Guide' target='_blank'>Community FAQ</a></p>" +
			"<h5>Community-made fast track cheat sheet</h5>" +
			"<p>This will help you make good decisions early in the game. This is mostly for new players but it's a good place to start if you don't want to make the same mistakes everyone else has made already.<br><a onclick='clearMakeIntoPopup();' href='http://initium.wikia.com/wiki/The_Starter_and_Progression_Guide' target='_blank'>Player-made guide</a></p>" +
			"<h5>Talk to the community in global!</h5>" +
			"<p>Last but not least our small gaming community is (generally) very helpful and happy you're here! If you have any questions just ask and many will be willing to go out of their way to help you out.</p>" +
			"";
	
	makeIntoPopupHtml(html);
}

function viewJoinTeam()
{
	var html = "" +
			"				<p>We are always on the look-out for those who want to help build Initium. You don\'t need to be a programmer to help! There are 3 categories of \r\n" + 
			"				help you can provide with varying degrees of skill required. They are as follows...</p>\r\n" + 
			"\r\n" + 
			"				<h5>Back and Frontend Developers</h5>\r\n" + 
			"				<p>\r\n" + 
			"				(Java, Javascript, Html, and/or CSS experience recommended)<br>" +
			"				Got some programming experience? We\'re able to accommodate a variety of different skill levels for the backend development. If\r\n" + 
			"				you\'re interested just send me an email and then jump right into getting your development environment setup \r\n" + 
			"				<a href=\'https://github.com/Emperorlou/Initium-ODP\'>using these installation instructions</a>. If you have any trouble, definitely \r\n" + 
			"				let me know via email and I\'ll try to help you out.\r\n" + 
			"				<br><br>				\r\n" + 
			"				If you\'re more a frontend guy, there is certainly stuff to be done there as well so don\'t hesitate to share your javascript and jquery experience with us!\r\n" + 
			"				<br><br>\r\n" + 
			"				<a href=\'mailto:nikolasarmstrong@gmail.com?Subject=Applying to be a Code Developer for Initium\' target=\"_top\">Apply to be a Code Developer here</a>\r\n" + 
			"				<br><br>\r\n" + 
			"				<i>Include how much time you have on a daily basis to help with development, what you do for a living, and what kind of experience\r\n" + 
			"				you have with web development. You don\'t necessarily need experience to be accepted!</i> \r\n" + 
			"				</p>			\r\n" + 
			"\r\n" + 
			"				\r\n" + 
			"				<h5>Content Developers</h5>\r\n" + 
			"				<p>\r\n" + 
			"				(no specific skill required but you probably wont get in till you've been active for a month or so)<br>" +
			"				We usually have a handful of content developers active at any time. These are people who create the game world using an online editor that was custom built for\r\n" + 
			"				Initium. It is very easy to use. If you wish to become a content developer, give me a shout! Just so you are aware however, I generally pick people who are\r\n" + 
			"				regular, active members of the community and tend to play quite a lot. The most important part of being a content developer is that you have a lot of time \r\n" + 
			"				on your hands! \r\n" + 
			"				<br><br>\r\n" + 
			"				<a href=\'mailto:nikolasarmstrong@gmail.com?Subject=Applying to be a Content Developer for Initium\' target=\"_top\">Apply to be a Content Developer here</a>\r\n" + 
			"				<br><br>\r\n" + 
			"				<i>Simply include your in-game character name, how much time you have on a daily basis to help with world building, what you do for a living, and any other \r\n" + 
			"				bits of information that would help me decide to choose you as our next content dev!</i> \r\n" + 
			"				</p>\r\n" + 
			"	\r\n" + 
			"	\r\n" + 
			"				<h5>Artists</h5>\r\n" + 
			"				<p>\r\n" + 
			"				(pixel art, photoshop work)<br>" +
			"				We can always use talented artists to help us create new banner images and equipment icons. After you\'ve had a good look at the game and the art style, \r\n" + 
			"				apply here and provide us with some cool sample work that relates!\r\n" + 
			"				<br><br>\r\n" + 
			"				If you\'re not really an artist but you can use a computer AND you can get your hands on Photoshop CS5 (CS5 is important) then you have everything you need\r\n" + 
			"				to help us with creating beautiful banners from photographs. Most of the work is just 1-click once you\'re setup!\r\n" + 
			"				<br><br>\r\n" + 
			"				<a href=\'mailto:nikolasarmstrong@gmail.com?Subject=Applying to be an Artist for Initium\' target=\"_top\">Apply to be an Artist here</a>\r\n" + 
			"				<br><br>\r\n" + 
			"				<i>Include how much time you have on a daily basis to help with art development, what you do for a living, and include any sample art pieces\r\n" + 
			"				that might relate (or not) to help your application!</i> \r\n" + 
			"				</p>			\r\n" + 
			"	\r\n" + 
			"				<h5>Marketing</h5>\r\n" + 
			"				<p>\r\n" + 
			"				(using Reddit, social media, posting on forums, contacting journalists, research)<br>" +
			"				This is a new position that we\'re preparing for. While Initium is still very new and a work-in-progress, many people have had hundreds of hours of fun\r\n" + 
			"				with the game even in it\'s current state. We\'d like to start marketing the game in a variety of different ways that finally branch outside of using\r\n" + 
			"				Reddit (which has been our exclusive marketing platform up to this point). As a marketing agent, you would help us fill out a spreadsheet of possible\r\n" + 
			"				places that we can advertise the game for free (like different subreddits, or gaming magazines), journalists that might be interested in writing an \r\n" + 
			"				article about Initium, or maybe just using other forms of social media to get the word out.  \r\n" + 
			"				<br><br>\r\n" + 
			"				<a href=\'mailto:nikolasarmstrong@gmail.com?Subject=Applying to be Marketing agent for Initium\' target=\"_top\">Apply to be Marketing Agent here</a>\r\n" + 
			"				<br><br>\r\n" + 
			"				<i>Include how much time you have on a daily basis to help with marketing, and what you do for a living.</i> \r\n" + 
			"				</p>\r\n" + 
			"";
	
	
	pagePopupHtml("Join the team!", html);
	
}

function viewThisLocationWindow()
{
	makeIntoPopup(".this-location-box");
}

function viewLocalNavigation(event, showHidden)
{
	miniPagePopup("/odp/sublocations?showHidden="+showHidden, "Local Places");
}

function showLootPopup()
{
	clearMakeIntoPopup();
	
	var html = "";
	html += "<div id='locationQuickList-contents'>";
	html += "<div><h4>Loot</h4></div>";
	html += "<p style='text-align:center'><a onclick='clearMakeIntoPopup();doAutoExplore();'>Explore</a></p>";
	if (window.singleLeavePathId!=null)
		html += "<p><a onclick='clearMakeIntoPopup();doGoto(event, window.singleLeavePathId, false);' style='float:right'>Leave</a></p>";
	html += "<p><a onclick='clearMakeIntoPopup();window.btnLeaveAndForget.click()' style='float:left'>Leave and forget</a></p>";
	html += "<div style='height:34px;'></div>";
	html += "	<div id='inline-characters'>";
	html += "	</div>";
	html += "	<div id='inline-collectables'>";
	html += "	</div>";
	html += "	<div id='inline-items'>";
	html += "	</div>";
	html += "</div>";
	
	clearMakeIntoPopup();
	
	makeIntoPopupHtml(html, true);
	loadInlineItemsAndCharacters();
}

function viewGuardSettings(event)
{
	pagePopup("/odp/guardsettings", null, "Guard Settings");
}

function deleteGuardSetting(event, id)
{
	doCommand(event, "GuardDeleteSetting", {guardSettingId:id});
}

function newGuardSetting(event, entity, type, attack)
{
	doCommand(event, "GuardNewSetting", {entityKey:entity, type:type, attack:attack});
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
			else if (htmlData.type==2)
			{
				$(htmlData.selector).first().before(htmlData.html);
			}
			else if (htmlData.type==3)
			{
				$(htmlData.selector).last().after(htmlData.html);
			}
			else if (htmlData.type==4)
			{
				$("#"+htmlData.id).remove();
				$("body").append("<script id='"+htmlData.id+"' type='text/javascript'>"+htmlData.js+"</script>");
			}
			else if (htmlData.type==5)
			{
				$(htmlData.selector).remove();
			}
			else if (htmlData.type==6)
			{
				$(htmlData.selector).prepend(htmlData.html);
			}
			else if (htmlData.type==7)
			{
				$(htmlData.selector).append(htmlData.html);
			}
			else if (htmlData.type==8)
			{
				eval(htmlData.html);
			}
		}
	}
}

/**
 * This function activates the wait gif animation on the clicked element and returns 
 * a function that, when called, will restore the original html for the clicked element.
 * @param eventObject
 * @returns {Function}
 */
function activateWaitGif(eventObject)
{
	var clickedElement = null;
	var originalText = null;
	if (eventObject!=null)
	{
		clickedElement = $(eventObject.currentTarget);
		if(clickedElement.find("img.wait").length == 0) {
			originalText = clickedElement.html();
			clickedElement.html("<img class='wait' src='/javascript/images/wait.gif' border=0/>");
		}
	}
	
	return function(){
		if (eventObject!=null)
		{
			clickedElement.html(originalText.replace("hasTooltip", ""));
		}
	};
	
}

window.commandInProgress = false;
function doCommand(eventObject, commandName, parameters, callback, userRequestId)
{
	if (window.commandInProgress==true)
		return;
	
	// Changing to a post now, so no need to generate the URL parameter string anymore.
	if (parameters==null)
	{
		parameters = {"v":verifyCode, "selected2DTileX":selectedTileX, "selected2DTileY":selectedTileY, "uiStyle": uiStyle};
	}
	else
	{
		parameters.v = verifyCode;
		parameters.selected2DTileX = selectedTileX;
		parameters.selected2DTileY = selectedTileY;
		parameters.uiStyle = uiStyle;
	}
	
	parameters.mainPageUrl = location.href;
	
	if (window.characterOverride!=null && window.characterOverride.length>10)
		parameters.char = window.characterOverride;
	
	// Now generate the url. We might use this later on to recall the command for some reason... probably not though. To be honest, this part was copypasta from the LongOperation command type
	var url = "/cmd?cmd="+commandName;
	
	
	var clickedElement = null;
	var originalText = null;
	if (eventObject!=null)
	{
		clickedElement = $(eventObject.currentTarget);
		if(clickedElement.find("img.wait").length == 0) {
			originalText = clickedElement.html();
			clickedElement.html("<img class='wait' src='/javascript/images/wait.gif' border=0/>");
		}
	}
	
	ga('send', 'pageview', url);
	
	var selectedItems = null;
	if (userRequestId!=null)
	{
		selectedItems = confirmRequirements_collectChoices(eventObject);
		if (selectedItems==null) selectedItems = {};
		parameters["__"+userRequestId+"UserResponse"] = JSON.stringify(selectedItems);
	}
	
	window.commandInProgress = true;
	// We need to post, as larger batch operations failed due to URL string being too long
	$.post(url, parameters)
	.done(function(data)
	{
		window.commandInProgress = false;
		// Return clicked element back to original state first.
		// Ajax updates get overwritten if they're not simple updates
		// on the original element.
		if (eventObject!=null && originalText)
			clickedElement.html(originalText.replace("hasTooltip", ""));
		
		if (data.antiBotQuestionActive == true)
		{
			antiBotQuestionPopup();
			return;
		}
		
		// Refresh the full page or the pagePopup if applicable
		if (data.javascriptResponse == "FullPageRefresh")
		{
			fullpageRefresh();
			return;		// No need to go any further, we're refreshing the page anyway
		}
		else if (data.javascriptResponse == "ReloadPagePopup")
			reloadPagePopup();
		else if (data.javascriptResponse == "ReloadMiniPagePopup")
			reloadMiniPagePopup();

		// Do the page update first, regarless if there was an error. We do this because even errored responses may contain page updates.
		ajaxUpdatePage(data);

		if (data.hasNewGameMessages==true)
		{
			messager.getMessages(true);
		}

		
		// Here we handle the special: UserRequestBuilder page popup mechanism
		if (data.pagePopupUrl!=null)
		{
			handleUserRequest(data);
		}
		
		// Here we display the system message if there was a system message
		if (data.message!=null && data.message.length>0)
			popupMessage("System Message", data.message);
		
		// Update the map with response data
		if (data._2dViewportUpdates!=null) {
			// For testing purposes, we might not have method in scope
			if (typeof updateGridFromServer === "function") {
				updateGridFromServer(data._2dViewportUpdates);
			}
		}

		// Here we display an error message popup if there was an error
		var error = false;
		if (data.errorMessage!=null && data.errorMessage.length>0)
		{
			error = true;
			popupMessage("System Message", data.errorMessage);
		}

		if (callback!=null && data!=null)
			callback(data.callbackData, error);
		else if (callback!=null && data==null)
			callback(null, error);
	
	})
	.fail(function(data)
	{
		window.commandInProgress = false;
		popupMessage("ERROR", "There was a server error when trying to perform the "+commandName+" command. Feel free to report this on <a href='http://initium.reddit.com'>/r/initium</a>. A log has been generated.");
		if (eventObject!=null)
			clickedElement.html(originalText.replace("hasTooltip", ""));
	});
	
	if (eventObject!=null)
		eventObject.stopPropagation();
	
}

function systemMessage(msg)
{
	popupMessage("System Message", msg);
}

function doSetLeader(eventObject, charId, charName)
{
	closeAllPopups();
	closeAllTooltips();
	confirmPopup("Set new leader", "Are you sure you want set " + charName + " to be the leader of your party?", function(){
		doCommand(eventObject,"PartySetLeader",{"charId":charId});
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


var longOperationCountdownTimer = null;
function startLongOperationCountdown(data)
{
	var startTimeSeconds = data.startTime/1000;
	var endTimeSeconds = data.endTime/1000;
	var totalSeconds = endTimeSeconds-startTimeSeconds;
	var timeLeftSeconds = data.timeLeft;
	lastLongOperationDueTime = new Date().getTime()+((timeLeftSeconds+1)*1000);
	lastLongOperationStartTime = data.startTime;
	updateLongOperationTimeLeft();
	
	stopLongOperationCountdown();
	longOperationCountdownTimer = setInterval(updateLongOperationTimeLeft, 1000);
	
	showLongOperationProgress();
	setLongOperationProgress(totalSeconds-timeLeftSeconds/totalSeconds, data.longOperationName, data.longOperationDescription);
}
function stopLongOperationCountdown()
{
	if (window.longOperationCountdownTimer!=null)
	{
		clearInterval(longOperationCountdownTimer);
		longOperationCountdownTimer = null;
	}
}

function updateLongOperationTimeLeft()
{
	if (lastLongOperationDueTime!=null)
	{
		var currentTime = new Date().getTime();
		var dueTime = lastLongOperationDueTime;
		var totalMs = dueTime-lastLongOperationStartTime;
		var totalSecs = totalMs/1000;
		var timeLeft = (dueTime-currentTime)/1000;
		var timeSpent = totalSecs-timeLeft;
		
		var percent = timeSpent/totalSecs*100;
		if (percent>100) percent = 100;
		if (percent<0) percent = 0;
		// Here we're making the progress bar only go from 2% to 98% because of the progress bar graphic's specific requirements to fit properly
		var percentCorrected = 2;
		percentCorrected += percent*0.96;
		
		if (timeLeft<0)
			$("#long-operation-timeleft").text("0 seconds remaining.");
		else
			$("#long-operation-timeleft").text(secondsToTimeString(timeLeft)+" remaining.");
		
		var loProgressBar = $("#long-operation-status");
		if (loProgressBar.length>0)
		{
			loProgressBar.find(".progress-bar-text").text(secondsToTimeString(timeLeft));
			loProgressBar.find(".progress-bar-fill").css("width", percentCorrected+"%"); 
		}
	}
}

function secondsToTimeString(seconds)
{
	if (seconds<0) return "100%";
	
	var days = Math.floor(seconds/86400);
	seconds -= (days*86400);

	var hours = Math.floor(seconds/3600);
	seconds -= (hours*3600);
	
	var minutes = Math.floor(seconds/60);
	seconds -= (minutes*60);
	
	var result = "";
	
	if (days>0) 
	{
		result+=days+" days ";
		result+=hours+" hours ";
	}
	else if (hours>0) 
	{
		result+=hours+" hours ";
		result+=minutes+" minutes ";
	}
	else if (minutes>0) 
	{
		result+=minutes+" minutes ";
		result+=Math.round(seconds)+" seconds";
	}
	else if (seconds>0)
	{
		result+=Math.round(seconds)+" seconds";
	}
	else
		result+="100%";
	
	
	return result;
	
}


function longOperation_fullPageRefresh(eventObject, operationName, operationDescription, operationBannerUrl, actionUrl, fullPageRefreshSeconds)
{
	var originalText = $(eventObject.currentTarget).html();
	$(eventObject.currentTarget).html("<img src='/javascript/images/wait.gif' border=0/>");
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

var lastLongOperationTimer = null;
var lastLongOperationDueTime = null;
var lastLongOperationEventObject = null;
var lastLongOperationStartTime = null;
/**
 * 
 * @param eventObject
 * @param actionUrl
 * @param responseFunction This is the handler that is called when the operation returns. The data that is passed into the handler includes: data.isComplete (boolean), data.error (boolean), data.timeLeft (seconds remaining to wait)
 * @param recallFunction This function should call the original javascript call that includes the call to longOperation. This handler is invoked when the time on the long operation runs out.
 */
function longOperation(eventObject, commandName, parameters, responseFunction, recallFunction, userRequestId)
{
	if (window.commandInProgress==true)
		return;
	
	lastLongOperationEventObject = eventObject;		// We're persisting the event object because when the ajax call returns, we may need to know what element was clicked when starting the long operation

	// Changing to a post now, so no need to generate the URL parameter string anymore.
	if (parameters==null)
	{
		parameters = {"v":verifyCode, "selected2DTileX":selectedTileX, "selected2DTileY":selectedTileY, "uiStyle": uiStyle};
	}
	else
	{
		parameters.v = verifyCode;
		parameters.selected2DTileX = selectedTileX;
		parameters.selected2DTileY = selectedTileY;
		parameters.uiStyle = uiStyle;
	}
	
	parameters.mainPageUrl = location.href;
	
	if (window.characterOverride!=null && window.characterOverride.length>10)
		parameters.char = window.characterOverride;
	
	// Now generate the url. We might use this later on to recall the command for some reason... probably not though. To be honest, this part was copypasta from the LongOperation command type
	var url = "/longoperation?cmd="+commandName;
	
	
	var clickedElement = null;
	var originalText = null;
	if (eventObject!=null)
	{
		clickedElement = $(eventObject.currentTarget);
		if(clickedElement.find("img.wait").length == 0) {
			originalText = clickedElement.html();
			clickedElement.addClass("marching-ants");
		}
	}

	ga('send', 'pageview', url);	
	
	var selectedItems = null;
	if (userRequestId!=null && eventObject!=null)
	{
		selectedItems = confirmRequirements_collectChoices(eventObject);
		if (selectedItems==null) selectedItems = {};
		parameters["__"+userRequestId+"UserResponse"] = JSON.stringify(selectedItems);
	}
	

	window.commandInProgress=true;
	$.post(url, parameters)
	.done(function(data)
	{
		window.commandInProgress=false;
		if (clickedElement!=null)
		{
			clickedElement.html(originalText);
			clickedElement.removeClass("marching-ants");
		}
		
		if (data.captcha==true)
		{
			antiBotQuestionPopup();
			return;
		}
		
		// Do the page update first, regarless if there was an error. We do this because even errored responses may contain page updates.
		ajaxUpdatePage(data);
	
		if (data.hasNewGameMessages==true)
		{
			messager.getMessages(true);
		}
		
		if (data.cancelled)
		{
			clearPopupPermanentOverlay();
			if (lastLongOperationTimer!=null)
			{
				clearTimeout(lastLongOperationTimer);
				lastLongOperationTimer = null;
			}
		}
		
		if (data.error!=undefined)
		{
			hideBannerLoadingIcon();
			clearPopupPermanentOverlay();
			popupMessage("System Message", data.error, false);
			if (data.refresh==true)
			{
				fullpageRefresh();
				return;
			}
		}
		if (data.silentError==true)
		{
			hideBannerLoadingIcon();
			clearPopupPermanentOverlay();
			return;
		}
		
		if (data.refresh==true)
		{
			fullpageRefresh();
			return;
		}
		if (data.message!=null)
		{
			popupMessage("System Message", data.message, false);
		}
		
		// Here we handle the special: UserRequestBuilder page popup mechanism
		if (data.pagePopupUrl!=null)
		{
			handleUserRequest(data, parameters.autoStart);
			return;
		}

		
		if (responseFunction!=null)
			responseFunction(data);

		if (data.error!=undefined)
			return;
		
		
		if (data.isComplete==false)
		{
			if (data.timeLeft>0)
			{
				lastLongOperationTimer = setTimeout(recallFunction, (data.timeLeft+1)*1000);
				if (data.timeLeft>=5)
					popupPremiumReminder();

				startLongOperationCountdown( data);
				
				window.scrollTo(0,0);
			}
			else
			{
				lastLongOperationTimer = setTimeout(recallFunction, 0);
			}
		}
		else
		{
			hideLongOperationProgress();
			if (data.description!=null)
				$("#long-operation-complete-text").html(data.description);
		}
		
		
		
		lastLongOperationEventObject = null;
	})
	.fail(function(xhr, textStatus, errorThrown){
		window.commandInProgress=false;
		hideBannerLoadingIcon();
		clearPopupPermanentOverlay();
		if (errorThrown=="Internal Server Error")
			popupMessage(errorThrown, "There was an error when trying to perform the action. Feel free to report this on <a href='http://initium.reddit.com'>/r/initium</a>. A log has been generated.");
		else
			popupMessage(errorThrown, "There was an error when trying to perform the action.");

		if (clickedElement!=null)
		{
			clickedElement.html(originalText);
			clickedElement.removeClass("marching-ants");
		}
		
		lastLongOperationEventObject = null;
	});
	
	if (eventObject!=null && eventObject.stopPropagation)
		eventObject.stopPropagation();
}

function cancelLongOperations(eventObject)
{
	if (window.commandInProgress)
		return;
	
	stopLongOperationCountdown();
	longOperation(eventObject, "cancelLongOperations", null, function(){
		clearPopupPermanentOverlay();
		if (lastLongOperationTimer!=null)
		{
			clearTimeout(lastLongOperationTimer);
			lastLongOperationTimer = null;
		}
	});
}


function showBannerLoadingIcon()
{
	$("#banner-base").append("<img id='banner-loading-icon' src='/javascript/images/wait.gif' border=0/>");
}

function setBannerImage(url)
{
	if (bannerUrl!=null && url!=bannerUrl && window.previousBannerUrl!=bannerUrl)
		window.previousBannerUrl = bannerUrl;
	
	bannerUrl = url;
	updateBannerWeatherSystem();
}

function setBannerOverlayText(title, text, noCancel)
{
	if (text==null)
		text = "";
	
	var contents = "<div class='travel-scene-text'><h1>"+title+"</h1>"+text+"<p><a onclick='cancelLongOperations(event)'>Cancel</a></p></div>";
	
	if (noCancel)
		contents = "<div class='travel-scene-text'><h1>"+title+"</h1>"+text+"</div>";
	
	$(".travel-scene-text").remove();
	$("#banner-base").append(contents);
}

function hideBannerLoadingIcon()
{
	$('#banner-loading-icon').remove();
}

function doGoto(event, pathId, attack)
{
	clearMakeIntoPopup();
	
	if (attack == null)
		attack = false;
	showBannerLoadingIcon();
	longOperation(event, "TakePath", {pathId:pathId,attack:attack}, 
			function(action) // responseFunction
			{
				if(action.error !== undefined)
				{
					clearPopupPermanentOverlay(); 
					closeAllPagePopups();
				}
				else if (action.isComplete)
				{
					// If we're in the 2D view when we arrive, lets get out of there
					var bannerstate = $("body").attr("bannerstate");
					if (bannerstate == "location-2d")
						$("body").attr("bannerstate", "");
					
					
					clearPopupPermanentOverlay(); 
					updateBannerWeatherSystem();
					setAudioDescriptor(locationAudioDescriptor, locationAudioDescriptorPreset, isOutside);
					clearLoopedSounds();
					playLoopedSounds();
					//clearPopupPermanentOverlay(); 
					//fullpageRefresh();
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



function doExperiment(event, itemId)
{
	var checkedIds = $(".experiment-item-checkbox:checked").map(function(){return $(this).attr('id');}).get();
	if (checkedIds.length==0) checkedIds = null;
	if (itemId!=null) checkIds = [itemId];
	
	showBannerLoadingIcon();
	longOperation(event, "InventionExperimentNew", {itemIds:checkedIds}, 
			function(action) // responseFunction
			{
				if(action.error !== undefined)
				{
					clearPopupPermanentOverlay(); 
				}
				else if (action.isComplete)
				{
					clearPopupPermanentOverlay(); 
					reloadPagePopup(false);
				}
				else
				{
					popupPermanentOverlay_Experiment("Experimenting", "You are performing experiments on the things around you so you might understand them better...");

				}
			},
			function()	// recallFunction
			{
				doExperiment(null);
			});
}


function repeatConfirmRequirementsButton(repsUniqueId)
{
//	How many times do you want to do this: <input type='number' id='repetitionCount' min='1' max='${maxReps}' uniqueId='${repsUniqueId}'/>
	var input = $("input[uniqueId="+repsUniqueId+"]");
	var repCount = input.val();
	if (isNaN(repCount)==false)
	{
		if (parseInt(repCount)>0)
		{
			repCount--;
			input.val(repCount);
			$("#confirmRequirementsButton-"+repsUniqueId).click();
			return true;
		}
	}
	return false;
}

function doCreatePrototype(event, ideaId, ideaName, userRequestId, repsUniqueId, autoStart)
{
	if (autoStart)
	{
		var btn = $("#gridmap-repeatinvention-button");
		if (btn.length>0)
		{
			btn.off("click");
			btn.on("click", function(event){
				doCreatePrototype(event, ideaId, ideaName, userRequestId, repsUniqueId, autoStart);
			});
			btn.show();
		}
	}
	
	showBannerLoadingIcon();
	//BeginPrototype
	longOperation(event, "InventionPrototypeNew", {ideaName:ideaName,ideaId:ideaId,repsUniqueId:repsUniqueId, autoStart:autoStart}, 
			function(action) // responseFunction
			{
				if(action.error !== undefined)
				{
					clearPopupPermanentOverlay(); 
				}
				else if (action.isComplete)
				{
					if (repeatConfirmRequirementsButton(repsUniqueId))
					{
						// do nothing I guess
					}
					else
					{
						clearPopupPermanentOverlay(); 
						if (isSoundEffectsEnabled()) playAudio("complete1");
						doSimpleDesktopNotification(ideaName+" prototyping complete.", "");
					}
				}
				else
				{
					popupPermanentOverlay_Experiment("Prototyping", "You are attempting to create a new prototype of the "+ideaName+" idea.<br><span id='long-operation-timeleft'></span>");
				}
			},
			function()	// recallFunction
			{
				doCreatePrototype(event, ideaId, ideaName, userRequestId, repsUniqueId);
			}, 
			userRequestId);
	
}

function doConstructItemSkill(event, skillId, skillName, userRequestId, repsUniqueId, autoStart)
{
	if (autoStart)
	{
		var btn = $("#gridmap-repeatinvention-button");
		if (btn.length>0)
		{
			btn.off("click");
			btn.on("click", function(event){
				doConstructItemSkill(event, skillId, skillName, userRequestId, repsUniqueId, autoStart);
			});
			btn.show();
		}
	}
	
	closeAllTooltips();
	
	showBannerLoadingIcon();
	//DoSkillConstructItem
	longOperation(event, "InventionConstructItemSkillNew", {skillName:skillName, constructItemSkillId:skillId,repsUniqueId:repsUniqueId, autoStart:autoStart}, 
			function(action) // responseFunction
			{
				if(action.error !== undefined)
				{
					clearPopupPermanentOverlay(); 
				}
				else if (action.isComplete)
				{
					if (repeatConfirmRequirementsButton(repsUniqueId))
					{
						// do nothing I guess
					}
					else
					{
						clearPopupPermanentOverlay();
						if (isSoundEffectsEnabled()) playAudio("complete1");
						doSimpleDesktopNotification(skillName+" skill complete.", "");
					}
				}
				else
				{
					popupPermanentOverlay_Experiment("Construct Item", "You are performing your "+skillName+" skill.<br><span id='long-operation-timeleft'></span>");
				}
			},
			function()	// recallFunction
			{
				doConstructItemSkill(event, skillId, skillName, userRequestId, repsUniqueId);
			}, 
			userRequestId);
	
}


function doCollectCollectable(event, collectableId, userRequestId)
{
	clearMakeIntoPopup();
	showBannerLoadingIcon();
	longOperation(event, "CollectCollectable", {collectableId:collectableId},  
			function(action) // responseFunction
			{
				if(action.error !== undefined)
				{
					clearPopupPermanentOverlay(); 
				}
				else if (action.isComplete)
				{
					clearPopupPermanentOverlay(); 
					hideBannerLoadingIcon();
				}
				else
				{
					popupPermanentOverlay_Experiment("Collect Resource", "<span id='long-operation-timeleft'></span>");
					hideBannerLoadingIcon();
				}
			},
			function()	// recallFunction
			{
				doCollectCollectable(null, collectableId, userRequestId);
			},
			userRequestId);
}


function doAutoExplore(event)
{
	var lastExploreState = localStorage.getItem("auto-explore");
	if (lastExploreState==null) lastExploreState = "explore";
	
	if (lastExploreState=="explore")
		doExplore(event, false, false);
	else if (lastExploreState=="ignoreCombatSites")
		doExplore(event, true, false);
	else if (lastExploreState == "findNaturalResources")
		doExplore(event, false, true);
}

function updateAutoExploreButton()
{
	var exploreState = localStorage.getItem("auto-explore");
	if (exploreState == "ignoreCombatSites")
		exploreState = "Explore (no old sites)";
	else if (exploreState == "findNaturalResources")
		exploreState = "Find Natural Resources";
	else
		exploreState = "Explore";
	
	$(".auto-explore-link").text(exploreState);
}


function doExplore(event, ignoreCombatSites, findNaturalResources)
{
	var exploreState = null;
	
	if (ignoreCombatSites!=null && findNaturalResources != null)
	{
		if (ignoreCombatSites==true)
			exploreState = "ignoreCombatSites";
		else if (findNaturalResources==true)
			exploreState = "findNaturalResources";
		else
			exploreState = "explore";
		localStorage.setItem("auto-explore", exploreState);
	}
	else
		localStorage.setItem("auto-explore", "explore");

	updateAutoExploreButton();
	
	
	clearMakeIntoPopup();
	
	if (ignoreCombatSites == null)
		ignoreCombatSites = getBannerIgnoreOldSites();
	if (findNaturalResources==null)
		findNaturalResources = false;
	showBannerLoadingIcon();
	longOperation(event, "Explore", {ignoreCombatSites:ignoreCombatSites, findNaturalResources:findNaturalResources}, 
			function(action) // responseFunction
			{
				if(action.error !== undefined)
				{
					clearPopupPermanentOverlay(); 
				}
				else if (action.isComplete)
				{
					clearPopupPermanentOverlay();
					//fullpageRefresh();
				}
				else
				{
					var locationName = action.locationName;
					popupPermanentOverlay_Searching(locationName, window.biome);

				}
			},
			function()	// recallFunction
			{
				doExplore(null, ignoreCombatSites);
			});
}

function doRest()
{
	showBannerLoadingIcon();
	longOperation(null, "Rest", null, 
			function(action) // responseFunction
			{
				if(action.error !== undefined)
				{
					clearPopupPermanentOverlay(); 
				}
				else if (action.isComplete)
				{
					clearPopupPermanentOverlay();
					//fullpageRefresh();
				}
				else
				{
					hideBannerLoadingIcon();
					setBannerImage("https://initium-resources.appspot.com/images/action-campsite1.gif");
					setBannerOverlayText("Resting..", action.description+"<br><span id='long-operation-timeleft'></span>");
				}
			},
			function()	// recallFunction
			{
				doRest();
			});
}

function doCampDefend()
{
	showBannerLoadingIcon();
	longOperation(null, "CampDefend", null, 
			function(action) // responseFunction
			{
				if(action.error !== undefined || action.isComplete)
				{
					clearPopupPermanentOverlay(); 
				}
				else
				{
					hideBannerLoadingIcon();
					setBannerImage("https://initium-resources.appspot.com/images/action-campsite1.gif");
					setBannerOverlayText("Defending", "Protect the camp!");
				}
			},
			function()	// recallFunction
			{
				doCampDefend();
			});
}

function doCampCreate(campName)
{
	showBannerLoadingIcon();
	longOperation(null, "CampCreate", {"name":campName}, 
		function(action) // responseFunction
		{
			if(action.error !== undefined)
			{
				clearPopupPermanentOverlay(); 
			}
			else if (action.isComplete)
			{
				clearPopupPermanentOverlay();
			}
			else
			{
				hideBannerLoadingIcon();
				setBannerOverlayText("Creating a new campsite...", "You are hard at work setting up a new camp. Make sure you defend it or it won't last long!");
			}
		},
		function()	// recallFunction
		{
			doCampCreate(campName);
		});
}

function handleUserRequest(data, autoStart)
{
	if (data.pagePopupUrl.indexOf("?")>=0)
		data.pagePopupUrl+="&userRequestId="+data.userRequestId+"&"+data.urlParameters+"&autoStart="+autoStart;
	else
		data.pagePopupUrl+="?userRequestId="+data.userRequestId+"&"+data.urlParameters+"&autoStart="+autoStart;
	
	// Add the exta request data to the url..
	
	
	closeAllPopups();
	closeAllTooltips();
	pagePopup(data.pagePopupUrl, 
	function ()
	{
		hideBannerLoadingIcon();
	}, 
	data.pagePopupTitle);
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

function updateMinimizeChat()
{
	$(window).load(function(){
		var minimized = localStorage.getItem("minimizeChat");
		if (minimized == "true")
			toggleMinimizeChat(false);
		else if (minimized == "false")
			toggleMinimizeChat(true);
	});
}

function isChatMinimized()
{
	var chatbox = $(".chat_box");
	
	if (chatbox.hasClass("minimized-chat"))
		return true;
	
	return false;
}

function toggleMinimizeChat(forceHide)
{
	$("#chat_tab").toggle();
	
	var chatbox = $("#chatbox-container");
	if (forceHide==false || chatbox.hasClass("chat-hide"))
	{
		// Show it
		localStorage.setItem("minimizeChat", "true");
		$("#chatbox-container").removeClass("chat-hide");
		$(".minimize-chat-button").text("<");
	}
	else if (forceHide==true || chatbox.hasClass("chat-hide")==false)
	{
		// Hide it
		localStorage.setItem("minimizeChat", "false");
		$("#chatbox-container").addClass("chat-hide");
		$(".minimize-chat-button").text(">");
	}
	
	if (window.updateBannerSize) updateBannerSize();
}

function toggleMinimizeSoldItems()
{
	$("#soldItems").toggle();
}

function updateMinimizeBox(buttonElement, selector)
{
	$(window).load(function(){
		var minimized = localStorage.getItem("minimizeBox"+selector);
		if (minimized == "true")
			minimizeBox({target:$(buttonElement)}, selector);
		else
			maximizeBox({target:$(buttonElement)}, selector);
		
		if (window.updateBannerSize) updateBannerSize();
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
	$(selector).addClass("minimized-chat");
	localStorage.setItem("minimizeBox"+selector, "true");
	if (window.updateBannerSize) updateBannerSize();
}

function maximizeBox(event, selector)
{
	$(selector).removeClass("minimized-chat");
	localStorage.setItem("minimizeBox"+selector, "false");
	if (window.updateBannerSize) updateBannerSize();
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
	pagePopup("/odp/ajax_trade.jsp",function(){
		doCommand(null,"TradeCancel");
//		popupMessage("Trade Cancelled","This trade has been cancelled.")
	}, "Trade");	
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
	territoryView.html("<img src='/javascript/images/wait.gif' border=0/>"+territoryView.html());
	territoryView.load("ajax_territoryview.jsp?char="+window.characterOverride);
}


/////////////////////////////////////////
// Shortcut key bindings


$(document).keyup(function(event){
	var buttonToPress = $('[shortcut="'+event.which+'"]');
	if (buttonToPress.length>0)
	{
		buttonToPress[0].click();
	}
	else if (!event.ctrlKey && event.which==67) // C
	{
		viewCharacterSwitcher();
	}
	else if (event.which==71) // G
	{
		viewGuardSettings();
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
	//user can just refresh the page. besides, this breaks on experimental.
	//else if (event.which==76) // Changed to L since B is now for Nearby Characters list
	//{
	//	window.location.href='main.jsp';
	//}
	else if (event.altKey && event.which==85){ // U - go up in character list
		switchCharacter(event, null, 1);
	}
	else if (event.altKey && event.which==74){ // J - go down in character list
		switchCharacter(event, null, 2);
	}
	else if (event.altKey && event.which==75){ // K - iterate through characters in same acc and party
		switchCharacter(event, null, 3);
	}
	else if (event.altKey && event.which==76){ // L - switch to party leader if possible
		switchCharacter(event, null, 4);
	}
	else if (event.which==88){ // X - open the invention window.
		viewInvention(event);
	}
	else if (event.which==78){ // N - opens the navigation window.
		if(uiStyle == "experimental"){
			viewLocalNavigation(event);
		}
		else{
			makeIntoPopup(".navigation-box");
		}
	}
	else if (event.which==81){ //Q - opens the quest log.
		viewQuests();
	}
	else if (event.which==72){ // H - opens the hotkey window.
		openHotkeyWindow();
	}
	else if(event.which==65){ // A - aborts the current long operation.
		cancelLongOperations(); //i tested, and sending this with no parameters DOES work
	}
});

function openHotkeyWindow(){
	pagePopup("/odp/hotkeys.html", null, "Hotkeys");
}

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
function confirmCancelPopup(title, content, showCancel, yesFunction, noFunction)
{
	var unique = "ID"+Math.floor((Math.random() * 990000000) + 1);
	var popupClassOverride = null;
	if (popupClassOverride==null)
		popupClassOverride = "popup backdrop1c v3-window1";
	closeAllPopups();
	$(".popupBlurrable").addClass("blur");
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
    $("#popups").html(currentPopups + '<div tabindex="0" id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground" onclick="closepopupMessage('+popupsNum+')"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper" onclick="event.stopPropagation();"><div id="popup_' + popupsNum + '" class="'+popupClassOverride+'"><div id="popup_header_' + popupsNum + '" class="popup_header">' + title + '</div><div id="popup_body_' + popupsNum + '" class="popup_body"><div id="popup_text_' + popupsNum + '" class="popup_text"><p>' + content + '</p><br></div></div><div id="popup_footer_' + popupsNum + '" class="popup_footer"><a id="'+unique+'-yes" class="popup_confirm_option confirm_yes">Yes</a><a id="'+unique+'-no"  class="popup_confirm_option confirm_no">No</a>' + (showCancel ? '<a id="'+unique+'-cancel"  class="popup_confirm_option confirm_cancel">Cancel</a>' : '') + '</div></div></div></div>');
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
        if (!showCancel && e.keyCode == 27)
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
    if(showCancel)
	{
    	$("#"+unique+"-cancel").click(function(){
        	closepopupMessage(currentPopup());
        });
	}
}

function confirmPopup(title, content, yesFunction, noFunction)
{
	confirmCancelPopup(title, content, false, yesFunction, noFunction);
}

function rangePopup(title, content, minValue, maxValue, valueFunction, yesFunction, noFunction, doNotFocus)
{
	if (content!=null)
		content = content.replace("\n", "<br>");
	
	var unique = "ID"+Math.floor((Math.random() * 990000000) + 1);
	var popupClassOverride = null;
	if (popupClassOverride==null)
		popupClassOverride = "popup backdrop1c v3-window1";
	closeAllPopups();
	$(".popupBlurrable").addClass("blur");
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
	var step = (maxValue-minValue) / 4;
	var rangeId = "popup_prompt_range_"+unique;
	var numberId = "popup_prompt_number_"+unique;
	var displayId = "popup_prompt_displaytext_"+unique;
	var changeText = function() { 
		var newText = this.value;
		if(valueFunction) {
			newText = valueFunction(Math.min(this.value,maxValue));
		} 
		$("#"+displayId).text(newText);
	};
	var newPopup = 
		'<div id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground" onclick="closepopupMessage('+popupsNum+')">' +
		'<div id="popupWrapper_' + popupsNum + '" class="popupWrapper" onclick="event.stopPropagation();">'+
		'<div id="popup_' + popupsNum + '" class="'+popupClassOverride+'">'+
		'<div id="popup_header_' + popupsNum + '" class="popup_header">' + title + '</div>'+
		'<div id="popup_body_' + popupsNum + '" class="popup_body">'+
		'<div id="popup_text_' + popupsNum + '" class="popup_text" style="max-height:200px">'+
		'<p style="margin:0px 2px">' + content + '</p>'+
		'<div class="popup_range_body" style="text-align:center">';
	var nudHtml = '<input id="'+numberId+'" class="popup_range_number" type="number" min="'+minValue+'" max="'+maxValue+'" value="1" onchange="'+rangeId+'.value=this.value;"/>'; 
	var increment = 1, numArrow = 1;
	while(increment < maxValue && numArrow < 5) // Don't allow more than 4 increment arrows
	{
		nudHtml = '<a class="range-increment" data-inc="'+(increment*-1)+'" style="float-left">'+('&lt;'.repeat(numArrow))+'</a>' + nudHtml + 
				'<a class="range-increment" data-inc="'+increment+'" style="float-right">'+('&gt;'.repeat(numArrow))+'</a>';
		numArrow++;
		increment*=10;
	}
	
	newPopup += '<div class="noselect">'+nudHtml+'</div><input id="'+rangeId+'" class="popup_range_slider" style="width:80%;" type="range" min="'+minValue+'" max="'+maxValue+'" step="1" value="'+minValue+'" oninput="'+numberId+'.value = this.value;" onchange="'+numberId+'.value = this.value;"/>';	
	newPopup += '<br/><span id="'+displayId+'"></span>';
	newPopup += '</div></div></div><div id="popup_footer_' + popupsNum + '" class="popup_footer"><a id="'+unique+'-yes" class="popup_confirm_option option_okay">Okay</a><a id="'+unique+'-no" class="popup_confirm_option option_cancel">Cancel</a></div></div></div></div>';
    $("#popups").html(currentPopups + newPopup);
	$("#popup_"+popupsNum).on("change", "#"+rangeId+",#"+numberId, changeText);
    expandpopupMessage();
    
    var inputText = $('#'+numberId);
    
    if (doNotFocus!=true)
    {
	    inputText.focus();
    }

    
    
	// Use range, since that limits the acceptable values.
    var inputRange = $('#'+rangeId);
    inputRange.change();
	
	var promptNo = function()
	{
		closepopupMessage(currentPopup());
    	if (noFunction)
    	{
    		noFunction();
    	}
	};
	var promptYes = function()
	{
		var value = null;
		if (yesFunction)
		{
			value = inputRange.val();
			if(value === "0")
			{
				promptNo();
				return;
			}
		}
		
		closepopupMessage(currentPopup());
		
		if (yesFunction)
			yesFunction(value);
	};
	
    inputRange.keyup(function(e){
    	stopEventPropagation(e);
    });
    inputRange.keydown(function(e){
    	stopEventPropagation(e);
        if (e.keyCode == 13) 
        {
        	promptYes();
        }
        if (e.keyCode == 27)
        {
        	promptNo();
        }
        
        e.stopPropagation();
    });
    
    // Increment arrow click.
    $("#popup_"+popupsNum).on("click", ".range-increment", function(event) { var inc = +$(this).data("inc"); inputRange.val(+inputRange.val()+inc); inputRange.change(); });
    // Yes/no click.
    $("#"+unique+"-yes").click(promptYes);
    $("#"+unique+"-no").click(promptNo);
}

function promptPopup(title, content, defaultText, yesFunction, noFunction, doNotFocus)
{
	if (content!=null)
		content = content.replace("\n", "<br>");
	
	if (defaultText==null)
		defaultText = "";
	
	defaultText = defaultText+"";
	
	defaultText = defaultText.replace("\"", "`").replace("'", "`");
	
	
	var unique = "ID"+Math.floor((Math.random() * 990000000) + 1);
	var popupClassOverride = null;
	if (popupClassOverride==null)
		popupClassOverride = "popup backdrop1c v3-window1";
	closeAllPopups();
	$(".popupBlurrable").addClass("blur");
    window.popupsNum++;
    window.popupsOpen++;
    window.popupsArray[popupsNum-1] = "yes";
    $("#popups").show();
    currentPopups = $("#popups").html();
    $("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="popupWrapperBackground" onclick="closepopupMessage('+popupsNum+')"><div id="popupWrapper_' + popupsNum + '" class="popupWrapper" onclick="event.stopPropagation();"><div id="popup_' + popupsNum + '" class="'+popupClassOverride+'"><div id="popup_header_' + popupsNum + '" class="popup_header">' + title + '</div><div id="popup_body_' + popupsNum + '" class="popup_body"><div id="popup_text_' + popupsNum + '" class="popup_text"><p style="margin:0px">' + content + '</p><br><div style="text-align:center"><input id="popup_prompt_input_'+unique+'" class="popup_prompt_input" type="text" value="'+defaultText+'"/></div></div></div><div id="popup_footer_' + popupsNum + '" class="popup_footer"><a id="'+unique+'-yes" class="popup_confirm_option confirm_okay">Okay</a><a id="'+unique+'-no" class="popup_confirm_option confirm_cancel">Cancel</a></div></div></div></div>');
    //$("#popups").html(currentPopups + '<div id="popupWrapperBackground_' + popupsNum + '" class="page-popup"><div id="popup_header_' + popupsNum + '" class="popup_header">' + title + '</div><p>' + content + '</p><br><input id="popup_prompt_input_'+unique+'" class="popup_prompt_input" type="text" value="'+defaultText+'"/><a id="'+unique+'-yes" class="popup_confirm_yes">Okay</a><a id="'+unique+'-no" class="popup_confirm_no">Cancel</a></div>');
    expandpopupMessage();
    
    var inputText = $('#popup_prompt_input_'+unique);
    
    if (doNotFocus!=true)
    {
	    inputText.focus();
	    inputText.select();
    }

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
function resetChat()
{
	$(".chat_messages").html("");
	messager.reconnect();
}

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

function isAdsEnabled()
{
	var setting = localStorage.getItem("checkboxDisableAds");
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

function getMaxScreenWidth()
{
	var setting = localStorage.getItem("sliderMaxScreenWidth");
	if (setting==null) return 1280;
	return parseInt(setting);
}

function getMapQuality()
{
	var setting = localStorage.getItem("checkboxUseLowResolutionMap") || "false";
	if(setting === "true") return "low";
	return "high";
}

function getBannerIgnoreOldSites()
{
	var setting = localStorage.getItem("checkboxBannerIgnoreOldSites") || "false";
	return setting === "true";
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
	{
		$("#header-mute img").attr("src", "https://initium-resources.appspot.com/images/ui/sound-button1-mute.png");
		$("#sound-button img").attr("src", "https://initium-resources.appspot.com/images/ui3/header-button-sound-off1.png");
	}
	else
	{
		$("#header-mute img").attr("src", "https://initium-resources.appspot.com/images/ui/sound-button1.png");
		$("#sound-button img").attr("src", "https://initium-resources.appspot.com/images/ui3/header-button-sound-on1.png");
	}
	
}

function updateEnvironmentSoundEffectsVolume()
{
	var vol = getSoundEffectsVolume();
	vol = parseFloat(vol)/100;
	createjs.Sound.volume = vol;
}

function playBannerFx(animationUrl, flipX, flipY)
{
	var divParent = $("#banner-fx");
	var fxDiv = divParent.children();
	
	fxDiv.remove();

	divParent.html("<img style='position:absolute; top:0px; width:100%; height:100%;' src='"+animationUrl+"'/>");
		
	fxDiv = divParent.children();

 	fxDiv.show().each(function() {
            this.offsetHeight;
        }).prop("src", animationUrl);	
	
	
	if (flipX==true)
		fxDiv.addClass("flip-background-x");
	if (flipY==true)
		fxDiv.addClass("flip-background-y");
}

////////////////////////////////////////////////////////
//Notifications
function doPopupNotification(iconUrl, title, text, category, options, onclick, onerror)
{
	if(notifyHandler == null || notifyHandler.popupNotify === "undefined") return;
	return notifyHandler.popupNotify(iconUrl, title, text, category, options, onclick, onerror);
}


function doSimpleDesktopNotification(text, category)
{
	doPopupNotification(null, "Initium", text, category, null, null);
}


////////////////////////////////////////////////////////////
// Anti-bot question stuff

function antiBotAnswer(response)
{
	doCommand(null, "AntiBotAnswer", {response:response}, function(data,error){
		if (error)
		{
			closeAllPopups();
			antiBotQuestionPopup();
		}
		else
		{
			closeAllPopups();
		}
	});
}

function antiBotQuestionPopup()
{
	popupMessage("Anti Bot Check", "<div id='myCaptcha' style='float:left;margin-right:10px;'></div><p>We have to check from time to time to make sure you're a human playing. This is to prevent people from " +
			"playing the game automatically using bots. Having trouble? <a onclick='location.reload()'>Click here</a> to refresh.</p>");
	
	grecaptcha.render( 'myCaptcha', {
		  'sitekey' : '6Ldx9wcUAAAAAG78kIIiv-pnhHBaAaTrpcX5ZDwT',  // required
		  'theme' : 'light',  // optional
		  'callback': antiBotAnswer  // optional
		});
}

// ///////////////////////////////////////////////////////////
// Stack maintenance

function mergeItemStacks(eventObject, selector)
{
	var batchItems = $(selector).has("input:checkbox:visible:checked");
	if(batchItems.length == 0) return;
	var itemIds = batchItems.map(function(i, selItem){ return $(selItem).attr("ref"); }).get().join(",");
	doCommand(eventObject, "ItemsStackMerge",{"itemIds":itemIds});
}

function splitItemStack(eventObject, selector)
{

	var batchItems = $(selector).has("input:checkbox:visible:checked");
	if(batchItems.length == 0) return;
	var itemIds = batchItems.map(function(i, selItem){ return $(selItem).attr("ref"); }).get().join(",");

	promptPopup("Stack Split","Enter a stack size to create:","1",function(stackSize){
		if(stackSize != null && stackSize != ""){
			promptPopup("Stack Split","Enter how many stacks you want:","1",function(stacks){
				if(stacks != null && stacks != ""){
					doCommand(eventObject, "ItemsStackSplit",{"itemIds":itemIds, "stackSize":stackSize, "stacks":stacks});
				}
			});
		}
	});
}

function renameUnnamedPlayer(eventObject)
{
	promptPopup("Rename Player", "Enter a new name for your character:", "", function(newName) {
		if (newName != null && newName != "")
		{
			doCommand(eventObject, "RenameUnnamedPlayer", {"newName" : newName});
		}
	});
}

function swapContainers(event, selector)
{
	var batchItems = $(selector).has("input:checkbox:visible:checked");
	if(batchItems.length == 0) return;	
	
	var itemIds = batchItems.map(function(i, selItem){ return $(selItem).attr("ref"); }).get().join(",");

	doCommand(event,"ItemsSwapStorageContainers",{"itemIds":itemIds});
}

function toggle2DGrid() {
	vpc = document.getElementById("viewportcontainer").style.display;
	if (vpc.style.display == "none") {
		vpc.style.display = "block";
	} else {
		vpc.style.display = "none";
	}
}


function changeGenericTab(event, tabId)
{
	var element = $(event.target);
	var id = element.attr("id");
	var code = id.substring(0, id.indexOf("-"));
	
	$("."+tabId+".tab-selected").removeClass("tab-selected");
	element.addClass("tab-selected");
	
	$("."+tabId+".tab-content-selected").removeClass("tab-content-selected").addClass("tab-content");
	$("."+tabId+"."+code+"-content").addClass("tab-content-selected").removeClass("tab-content");
	console.log(code);
}

function addType1Button()
{
	// A placeholder since this is actually in full.js now
}


function changeUITo(value)
{
	localStorage.setItem("selectUIStyle", value);
	location.reload();
}

function changeGuardRunHitpoints(eventObject, current)
{
	promptPopup("Change Guard Run Hitpoints", "Enter the hitpoints you should have to trigger automatic running while guarding:", current, function(newValue) {
		doCommand(eventObject, "GuardChangeRunHitpoints", {"value" : newValue});
	});
}

function viewHCMLeaderboard()
{
	makeIntoPopupFromUrl("/odp/hcmleaderboard", "Hardcore Leaderboard", false);
}



function showLongOperationProgress()
{
	$("body").attr("longoperation", "true");
}

function hideLongOperationProgress()
{
	$("body").attr("longoperation", "");
	$(".progress-bar-fill").removeAttr("style");
	
	stopLongOperationCountdown();
}

function setLongOperationProgress(percent, title, description)
{
	var statusDiv = $("#long-operation-status");
	if (title!=null)
	{
		statusDiv.find(".long-operation-status-title").text(title);
	}
	if (description!=null)
	{
		statusDiv.find(".long-operation-status-description").text(description);
	}
	
	percent *= 0.96;
	percent += 2;
	
	statusDiv.find(".progress-bar-fill").css("width", percent+"%");
}

function removeTileObjects()
{
	// Not implemented here
}

function addGridObjectToMap()
{
	// Not implemented here
}

function refreshPositions()
{
	// Not implemented here
}

function inspectCellContents()
{
	// Not implemented here
}

function petFeedExecute(event, petId, itemId)
{
//	confirmPopup("Feed pet", "Are you sure you want to feed this to your pet? <br>(If you want to feed only a partial amount, you will have to split the stack first in your inventory)", function(){
		doCommand(event, "PetFeed", {petId:petId, itemId:[itemId]}, function(data){
			// Refresh the popup 
//			$("#petfoodcompatibleitemslist-container").html(
//					"	<a class='make-popup-X' onclick='clearMakeIntoPopup()'>X</a>" +
//					"	<h4>Item picker</h4>" +
//					"	<div id='petfoodcompatibleitemslist'><img class='wait' src='/javascript/images/wait.gif' border='0'/></div>");
//			$("#petfoodcompatibleitemslist").load("/odp/petfoodcompatibleitemslist?petId="+petId+"&char="+window.characterOverride);
		});
//	});
}

function petFeedExecuteAll(event, petId)
{
	var itemIds = [];
	var items = $("#petfoodcompatibleitemslist").children(".selectable");
	for(var i = 0; (i<items.length && i<20); i++)
	{
		var id = items.get(i).id;
		itemIds.push(parseInt(id));
	}
	
	confirmPopup("Feed pet", "Are you sure you want to give this pet EVERYTHING you are holding, to eat? Note: They will only eat what they like to eat.<br>(Only the first 20 in this list will be given)", function(){
		doCommand(event, "PetFeed", {petId:petId, itemId:itemIds}, function(data){
//			$("#petfoodcompatibleitemslist-container").html(
//					"	<a class='make-popup-X' onclick='clearMakeIntoPopup()'>X</a>" +
//					"	<h4>Item picker</h4>" +
//					"	<div id='buyordercompatibleitemslist'><img class='wait' src='/javascript/images/wait.gif' border='0'/></div>");
//			$("#petfoodcompatibleitemslist").load("/odp/petfoodcompatibleitemslist?petId="+petId+"&char="+window.characterOverride);
		});
	});
}

function viewPetFoodOptions(event, petId)
{
	$("body").append("" +
			"<div id='petfoodcompatibleitemslist-container' class='main-buttonbox v3-window3 make-popup make-popup-removable'>" +
			"	<a class='make-popup-X' onclick='clearMakeIntoPopup()'>X</a>" +
			"	<h4>Item picker</h4>" +
			"	<div id='petfoodcompatibleitemslist'><img class='wait' src='/javascript/images/wait.gif' border='0'/></div>" +
			"</div>" +
			"<div onclick='clearMakeIntoPopup()' class='make-popup-underlay'></div>");
	$("#petfoodcompatibleitemslist").load("/odp/petfoodcompatibleitemslist?petId="+petId+"&char="+window.characterOverride);
	closeAllTooltips();
}

function updateItemImage(itemId, image)
{
	 var elements = $('.item-icon-img-'+itemId);
	 
	 for(var i = 0; i<elements.length; i++)
	 {
		 if (elements[i].nodeName == "IMG")
		 {
			 $(elements[i]).attr("src", image);
		 }
		 else if (elements[i].nodeName == "DIV")
		 {
			 $(elements[i]).css("background-image", "url('"+image+"')");
		 }
	 }
}

function updatePetImage(petId, image)
{
	updateItemImage(petId, image);
}

function updateNeededPetFood(amount)
{
	$("#petFoodMaxKg").text(amount);
}

window.petFeedAnimTimeoutIds = {};
function doFeedPetAnimation(petId, feedAnimation, animLengthMs, regularImage)
{
	if (window.petFeedAnimTimeoutIds["ID"+petId]!=null)
		clearTimeout(window.petFeedAnimTimeoutIds["ID"+petId]);
	
	var originalSrc = regularImage;
	
	var timeoutId = setTimeout(function(){
		updatePetImage(petId, originalSrc);
		window.petFeedAnimTimeoutIds["ID"+petId] = null;
	}, animLengthMs,
	originalSrc, petId);
	
	updatePetImage(petId, feedAnimation);
	
	window.petFeedAnimTimeoutIds["ID"+petId] = timeoutId;
}


















/*
 * 
 * 
 * Confirm requirements JSP scripts
 * 
 * 
 */

var crSelectedRequirementSlotIndex = null;
function selectRequirement(event, requirementSlotIndex, gerKeyList)
{
	crSelectedRequirementSlotIndex = requirementSlotIndex.split(":").join("\\:");
	$(".confirm-requirements-requirement").removeClass("confirm-requirements-selected");
	$(event.target).closest(".confirm-requirements-requirement").addClass("confirm-requirements-selected");
	doCommand(event, "ConfirmRequirementsUpdate", {slotIndex:requirementSlotIndex, gerKeyList:gerKeyList});	
}

function selectAll(event)
{
	var itemPanelForRequirement = $("#itemHtmlForRequirement"+crSelectedRequirementSlotIndex);

	var itemVisual = $("#item-candidates").find(".confirm-requirements-item-candidate");
	itemVisual.detach();
	itemPanelForRequirement.append(itemVisual);
	itemVisual.hide();
	itemVisual.fadeIn("slow");
	event.stopPropagation();
}

function selectItem(event, itemId)
{
	// Check that we're not trying to select an item that is already chosen as a requirement. It doesn't work that way.
	if ($(event.target).closest(".confirm-requirements-requirement").length>0)
		return;
	
	//unselectItem(null);
	
	
	$("#"+crSelectedRequirementSlotIndex).val(itemId);
	var itemPanelForRequirement = $("#itemHtmlForRequirement"+crSelectedRequirementSlotIndex);
	var itemVisual = $(event.target).closest(".confirm-requirements-item-candidate");
	itemVisual.detach();
	itemPanelForRequirement.append(itemVisual);
	itemVisual.hide();
	itemVisual.fadeIn("slow");
	event.stopPropagation();
}

function unselectItem(event)
{
	var candidatesContainer = $("#item-candidates");
	var container = $("#requirement-container-"+crSelectedRequirementSlotIndex);
	var currentSelectedItem = container.find(".itemToSelect");
	if (currentSelectedItem.length==0)
		return; 	// Nothing is selected

	// Get all .confirm-requirements-item-candidate divs and find one that is empty for reuse
	container.children("input").val("");
	var e = container.find(".itemToSelect").detach();
	candidatesContainer.children(".list").prepend(e);
	e.hide();
	e.fadeIn("slow");
	
	if (event!=null)
		event.stopPropagation();
}

function confirmRequirements_collectChoices(event)
{
	var result = {};
	result.slots = {};
	result.repetitionCount = null;
	var requirementsContainers = $("#requirement-categories").find(".confirm-requirements-entry");
	for(var i = 0; i<requirementsContainers.length; i++)
	{
		var requirementsContainer = $(requirementsContainers[i]);

		var itemsSelected = requirementsContainer.find(".itemToSelect");
		if (itemsSelected.length>0)
		{
			var val = "";
			var firstTime = true;
			for(var ii = 0; ii<itemsSelected.length; ii++)
			{
				if (firstTime)
					firstTime = false;
				else
					val+=",";
				
				val+=$(itemsSelected[ii]).attr("itemKey");
			}
			
			result.slots[requirementsContainer.attr("slotName")] = val;
		}
	}
	
	if ($("#repetitionCount").length>0)
		result.repetitionCount = $("#repetitionCount").val();
	
	return result;
}

function clearQuestCompleteEffect()
{
	clearTimeout(window.questCompleteEffectTimer);
	if ($("#questCompleteEffect img").length>0)
		$("#questCompleteEffect img")[0].src='';
	$("#questCompleteEffect").remove();
}

var overlayEffectIdCounter = 0;
function overlayEffect(imgUrl, timeoutMs)
{
	overlayEffectIdCounter++;
	
	var effectId = overlayEffectIdCounter;
	var effectTimer = null;
	function clearOverlayEffect()
	{
		clearTimeout(effectTimer);
		$("#overlayEffect"+effectId).fadeOut(1000, function(){
			if ($("#overlayEffect"+effectId+" img").length>0)
				$("#overlayEffect"+effectId+" img")[0].src='';
			$("#overlayEffect"+effectId).remove();
		});
	}
	
	var html = "<div class='overlay-effect-container' id='overlayEffect"+effectId+"' >"+
	"<img src='"+imgUrl+"'>"+
	"</div>";
	$("#banner-fx").prepend(html);
	
	effectTimer = setTimeout(clearOverlayEffect, timeoutMs);
	
}


var genericOverlayEffectIdCounter = 0;
function genericOverlayEffect(imgUrl, timeoutMs)
{
	genericOverlayEffectIdCounter++;
	
	var effectId = genericOverlayEffectIdCounter;
	var effectTimer = null;
	function clearGenericOverlayEffect()
	{
		clearTimeout(effectTimer);
		$("#genericOverlayEffect"+effectId).fadeOut(1000, function(){
			if ($("#genericOverlayEffect"+effectId+" img").length>0)
				$("#genericOverlayEffect"+effectId+" img")[0].src='';
			$("#genericOverlayEffect"+effectId).remove();
		});
	}
	
	var html = "<div class='overlay-effect-container' id='genericOverlayEffect"+effectId+"' >"+
	"<img src='"+imgUrl+"'>"+
	"</div>";
	$("body").append(html);
	
	effectTimer = setTimeout(clearGenericOverlayEffect, timeoutMs);
	
}


function doQuestCompleteBannerEffect()
{
	overlayEffect("https://i.imgur.com/s2XdVIl.gif", 5000);
	if (isSoundEffectsEnabled()) playAudio("quest-complete1", 0.6);
	window.scrollTo(0,0);
}

function doObjectiveCompleteBannerEffect()
{
	overlayEffect("https://i.imgur.com/mv9LtvM.gif", 5000);
	if (isSoundEffectsEnabled()) playAudio("objective-complete1", 0.3);
	window.scrollTo(0,0);
}


function minimapZoomIn()
{
	var scale = $(".minimap-container .overheadmap-cell-container-base:first").css("transform");
	if (scale=="none")
	{
		scale = 1.0;
	}
	else
	{
		scale = scale.split(",")[0];
		scale = scale.split("(")[1];
		scale = parseFloat(scale);
	}
	scale *= 1.15;
	$(".minimap-container .overheadmap-cell-container-base").css("transform", "scale("+scale+")");
}

function minimapZoomOut()
{
	var scale = $(".minimap-container .overheadmap-cell-container-base:first").css("transform");
	if (scale=="none")
	{
		scale = 1.0;
	}
	else
	{
		scale = scale.split(",")[0];
		scale = scale.split("(")[1];
		scale = parseFloat(scale);
	}
	scale *= 0.86956521739130434782608695652174;
	$(".minimap-container .overheadmap-cell-container-base").css("transform", "scale("+scale+")");
}

function navMapZoomIn()
{
	var scale = $(".global-navigation-map .overheadmap-cell-container-base:first").css("transform");
	if (scale=="none")
	{
		scale = 1.0;
	}
	else
	{
		scale = scale.split(",")[0];
		scale = scale.split("(")[1];
		scale = parseFloat(scale);
	}
	scale *= 1.15;
	$(".global-navigation-map .overheadmap-cell-container-base").css("transform", "scale("+scale+")");
}

function navMapZoomOut()
{
	var scale = $(".global-navigation-map .overheadmap-cell-container-base:first").css("transform");
	if (scale=="none")
	{
		scale = 1.0;
	}
	else
	{
		scale = scale.split(",")[0];
		scale = scale.split("(")[1];
		scale = parseFloat(scale);
	}
	scale *= 0.86956521739130434782608695652174;
	$(".global-navigation-map .overheadmap-cell-container-base").css("transform", "scale("+scale+")");
}

//function doQuestCompleteEffect()
//{
//	$("#quest-complete-label").remove();
//	clearQuestCompleteEffect();
//	
//	var html = "<div id='questCompleteEffect' style='position: fixed;overflow: visible;mix-blend-mode: color-dodge;width: 0px;height: 0px;left: 50%;top: 30%;z-index: 1000000000;'>"+
//	"<img src='https://initium-resources.appspot.com/images/ui3/complete-effect.gif' style='position:absolute;margin-left: -250px;margin-top: 0px;float: left;pointer-events: none;transform: scale(2);'>"+
//	"</div>";
//	$("body").prepend(html);
//	
//	var completeHtml = "<h3 id='quest-complete-label'>Complete!</h3>";
//	setTimeout(function(){$(".quest-window").append(completeHtml);}, 500);
//	window.questCompleteEffectTimer = setTimeout(clearQuestCompleteEffect, 5000);
//
//	$("#questlist-questkey-${questDefKey}").addClass("quest-complete");
//}



