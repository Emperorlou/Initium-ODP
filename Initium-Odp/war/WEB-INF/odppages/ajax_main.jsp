<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@page import="com.universeprojects.miniup.server.JspSnippets"%>
<!doctype html>
<html>
<head>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<meta charset="utf-8">    
<meta http-equiv="content-type" conftent="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no"/> 
<!-- <meta name="viewport" content="minimum-scale=0.3, maximum-scale=1"/> -->
<meta name="keywords" content="initium, game, web game, video game, free to play, mmorpg, mmo">
<meta name="referrer" content="no-referrer" />

<script type="text/javascript" src="https://code.createjs.com/preloadjs-0.6.2.min.js"></script>
<script type="text/javascript" src="https://code.createjs.com/soundjs-0.6.2.min.js"></script>

<script type="text/javascript" src="/javascript/modernizr.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>

<script type="text/javascript" src="https://cdn.rawgit.com/leafo/sticky-kit/v1.1.2/jquery.sticky-kit.min.js"></script>

<script type="text/javascript" src="/javascript/jquery.browser.min.js"></script>
<script type="text/javascript" src="/javascript/jquery.preload.min.js"></script>
<script type="text/javascript" src="/javascript/sockjs.min.js"></script>
<script type="text/javascript" src="/javascript/vertx-eventbus.js"></script>

<script type="text/javascript" src="/odp/javascript/seedrandom.js"></script>
<script type="text/javascript" src="/odp/javascript/script.js?v=82"></script>

<script type="text/javascript" src="/odp/javascript/messager.js?v=22"></script>

<script type="text/javascript" src="/odp/javascript/PopupNotifications.js?v=3"></script>
<script type="text/javascript" src="/odp/javascript/BrowserPopupNotifications-impl.js?v=3"></script>



<script type="text/javascript" src="/javascript/jquery.cluetip.all.min.js"></script>
<link type="text/css" rel="stylesheet" href="/javascript/jquery.cluetip.css"/>

<link type="text/css" rel="stylesheet" href="/odp/MiniUP.css?v=62">

<link type="text/css" rel="stylesheet" href="/javascript/rangeslider/rangeslider.css"/>
<script src="/javascript/rangeslider/rangeslider.min.js"></script>

<script src='/odp/javascript/openseadragon/openseadragon.min.js'></script>
<script src='/odp/javascript/map.js?t=2'></script>

<script src="https://www.google.com/recaptcha/api.js?onload=onCaptchaLoaded&render=explicit"></script>

<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-62104245-1', 'auto');
  ga('send', 'pageview');

</script>


<script type="text/javascript">
	window.newUI = true;
	window.chatIdToken = "${chatIdToken}";
	window.characterId = ${characterId};
	window.verifyCode = "${verifyCode}";
	window.serverTime=<c:out value="${serverTime}"/>;
	window.clientTime=new Date().getTime();
	


	<c:if test="${userMessage!=null}">
		$(window).ready(function()
		{
			popupMessage("System Message", "${userMessage}");
		});
	</c:if>
	
	
	// This will hide any open cluetips on a touch device when touching anywhere on the screen
	$(document).bind('touchstart', function(event) {
		 event = event.originalEvent;
		 var tgt = event.touches[0] && event.touches[0].target,
		     $tgt = $(tgt);

		 if (tgt.nodeName !== 'A' && !$tgt.closest('div.cluetip').length ) {
		   $(document).trigger('hideCluetip');
		 }
		});
	
	$(document).delegate(".clueHover:not(.hasTooltip)", "mouseenter", function (event) {

		
	    $(this).cluetip(
	    {
			cluetipClass: 'newui2',
			showTitle: false, 
			height: 'auto', 
			width: 303,
	        sticky: true, 
	        closePosition: 'title',
	        arrows: true,
	        ajaxCache: false,
	        mouseOutClose: false,
	        cluezIndex: 2000000,
	        onShow: function(e) 
	        {
	        	$("#cluetip-waitimage").css('z-index', 2000000); 
	        	$("#cluetip").css('z-index', 2000000); 
	        	return true;
	        }
	        
	    }).addClass("hasTooltip");
	    event.preventDefault();
	});
	
	
	$(document).delegate(".clue:not(.hasTooltip)", "mouseenter", function (event) {

		
	    $(this).cluetip(
	    {
			cluetipClass: 'newui2',
			showTitle: false, 
			height: 'auto', 
			width: 303,
	        sticky: true, 
	        closePosition: 'title', 
	        arrows: true,
	        ajaxCache: false,
	        mouseOutClose: false,
	        activation:"click",
	        cluezIndex: 2000000,
	        onShow: function() {
	            // close cluetip when users click outside of it
	            $(document).click(function(e) {
	                var isInClueTip = $(e.target).closest('#cluetip');
	                if (isInClueTip.length === 0) {
	                	$(document).trigger('hideCluetip');
	                }
	            })
	            
	            // Make the cluetip on top of everything
	        	$("#cluetip-waitimage").css('z-index', 2000000); 
	        	$("#cluetip").css('z-index', 2000000); 
	        	return true;
	        }	        
	    }).addClass("hasTooltip");
	    event.preventDefault();
	});

	
	$(document).delegate(".hint:not(.hasTooltip)", "mouseenter", function (event) {

		
	    $(this).cluetip(
	    {
			cluetipClass: 'newui2',
			showTitle: false, 
			height: 'auto', 
			width: 303,
	        sticky: true, 
	        closePosition: 'title',
	        closeText: ' ',
	        arrows: true,
	        ajaxCache: false,
	        mouseOutClose: false,
	        activation:"click",
	        local:true,
	        cluezIndex: 2000000,
	        onShow: function() {
	            // close cluetip when users click outside of it
	            $(document).click(function(e) {
	                var isInClueTip = $(e.target).closest('#cluetip');
	                if (isInClueTip.length === 0) {
	                	$(document).trigger('hideCluetip');
	                }
	            })

	            // Make the cluetip on top of everything
	        	$("#cluetip-waitimage").css('z-index', 2000000); 
	        	$("#cluetip").css('z-index', 2000000); 
	        	return true;
	        }	        
	    }).addClass("hasTooltip");
	    event.preventDefault();
	});

	
	
	$.ajaxSetup ({
	    // Disable caching of AJAX responses
	    cache: false
	});
	
	// This is a fix for the older android browsers (below 4.4)
	var ua = navigator.userAgent;
	if (ua.indexOf("Android")>0 && ua.indexOf("Chrome")==-1)
	{
		$("html").removeClass("backgroundcliptext");
		$("html").addClass("no-backgroundcliptext");
	}
</script>

<script type='text/javascript'>
	$(document).ready(function (){
		
		// Request permission to use desktop notifications
		notifyHandler.requestPermission();		
	});
</script>

<script type='text/javascript' src='/odp/javascript/banner-weather.js?v=8'></script>
<script id='ajaxJs' type='text/javascript'>
${bannerJs}
</script>




<script type='text/javascript'>
	if (isAnimationsEnabled())
	{
		$.preload("https://initium-resources.appspot.com/images/anim/walking.gif", 
				"https://initium-resources.appspot.com/images/anim/props/tree1.gif",
				"https://initium-resources.appspot.com/images/anim/props/tree2.gif",
				"https://initium-resources.appspot.com/images/anim/props/tree3.gif",
				"https://initium-resources.appspot.com/images/anim/props/tree4.gif",
				"https://initium-resources.appspot.com/images/anim/props/tree5.gif",
				"https://initium-resources.appspot.com/images/anim/props/tree6.gif",
				"https://initium-resources.appspot.com/images/anim/props/shrub1.gif",
				"https://initium-resources.appspot.com/images/anim/props/shrub2.gif",
				"https://initium-resources.appspot.com/images/anim/props/shrub3.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree1.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree2.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree3.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree4.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree5.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree6.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree7.gif",
				"https://initium-resources.appspot.com/images/anim/props/grass1.gif",
				"https://initium-resources.appspot.com/images/anim/props/grass2.gif",
				"https://initium-resources.appspot.com/images/anim/props/grass3.gif",
				"https://initium-resources.appspot.com/images/anim/props/grass4.gif",
				"https://initium-resources.appspot.com/images/anim/props/grass5.gif",
				"https://initium-resources.appspot.com/images/anim/props/grass6.gif"
				);
	}
</script>

<script type='text/javascript' src='/odp/javascript/messager-impl.js?v=54'></script>

<script type='text/javascript' src='/odp/javascript/soundeffects.js?v=13'></script>
<script type='text/javascript'>
	// THIS SECTION IS NEEDED FOR THE SOUND EFFECTS
	$(document).ready(function(){
		setAudioDescriptor("${locationAudioDescriptor}", "${locationAudioDescriptorPreset}", <c:out value="${isOutside}"/>);
	});
</script>

<script type='text/javascript'>
${longOperationRecallJs}
</script>

<script type='text/javascript'>
	/*Antibot stuff*/
	<c:if test="${botCheck==true}">
		function onCaptchaLoaded(){
			antiBotQuestionPopup();
		}
	</c:if>
</script>

<script type='text/javascript'>
	/*Other javascript variables*/
	window.isPremium = ${isPremium};
</script>


<!-- Overrides of script.js for the new UI -->
<script type="text/javascript">

$("#page-popup-root").html("");	// Clean out the page popup root, we no longer put the close/refresh buttons there
function pagePopup(url, closeCallback)
{
	if (url.indexOf("?")>0)
		url+="&ajax=true";
	else
		url+="?ajax=true";
	
	exitFullscreenChat();
	
	var stackIndex = incrementStackIndex();
	var pagePopupId = "page-popup"+stackIndex;
	
	$("#page-popup-root").append("<div id='"+pagePopupId+"' class='page-popup'>"+
			"<div style='display:table'><div class='header1' style='display:table-row'>"+
			"<div class='header1-button' style='display:table-cell' onclick='reloadPagePopup()'>&#8635;</div>"+
			"<div  style='display:table-cell; width:100%;'></div>"+
			"<div class='header1-button'  style='display:table-cell' onclick='closePagePopup()'>X</div></div></div>"+
			"<div id='"+pagePopupId+"-content' src='"+url+"'><img id='banner-loading-icon' src='/javascript/images/wait.gif' border=0/></div><div class='mobile-spacer'></div></div>");
	$("#"+pagePopupId+"-content").load(url);
	
	if (closeCallback!=null)
		popupStackCloseCallbackHandlers.push(closeCallback);
	else
		popupStackCloseCallbackHandlers.push(null);
}

</script>

<script type="text/javascript">
	// This ensures the bottom half of the page fills the rest of the page and no more
	function normalizePage()
	{
		var adjust = 0;
		var viewportHeight = $(window).height();
		
		var banner = $("#main-banner");
		var header = $("#main-header");
		var contents = $(".page-maincontent");
		var contentsHeight = viewportHeight - banner.height()+header.height()+adjust;
		contents.height(contentsHeight-40);
		contents.css("top", banner.height()+header.height()-adjust);
	}
	$(document).ready(normalizePage);
	$(window).resize(normalizePage);
	$(window).load(normalizePage);
	normalizePage();
</script>

<script type="text/javascript">
function pagePopup(url, closeCallback, popupTitle)
{
	if (url.indexOf("?")>0)
		url+="&ajax=true";
	else
		url+="?ajax=true";
	
	exitFullscreenChat();
	
	var stackIndex = incrementStackIndex();
	var pagePopupId = "page-popup"+stackIndex;
	
	if (popupTitle==null)
		popupTitle = "";
	
	if ($(window).width()>950)
	{
		$(".location-controls-container.half-page-variant").append("<div id='"+pagePopupId+"' class='page-popup-newui location-controls-page'><div class='header1'><div class='header1-buttonbar'><div class='header1-buttonbar-inner'><div class='header1-spacer'></div><div id='page-popup-reloadbutton' class='header1-button header1-buttonbar-left' onclick='reloadPagePopup()'>↻</div><div class='header1-buttonbar-middle'><div class='header1-display' id='pagepopup-title'>"+popupTitle+"</div></div><div class='header1-button header1-buttonbar-right' onclick='closePagePopup()'>X</div><div class='header1-spacer'></div></div></div></div><div class='main1 location-controls-page-internal'><div id='"+pagePopupId+"-content' class='location-controls' src='"+url+"'><img id='banner-loading-icon' src='/javascript/images/wait.gif' border=0/></div></div></div>");
		$(".half-page-variant").show();
	}
	else
		$(".location-controls-container").append("<div id='"+pagePopupId+"' class='page-popup-newui location-controls-page'><div class='header1'><div class='header1-buttonbar'><div class='header1-buttonbar-inner'><div id='page-popup-reloadbutton' class='header1-button header1-buttonbar-left' onclick='reloadPagePopup()'>↻</div><div class='header1-buttonbar-middle'><div class='header1-display' id='pagepopup-title'>"+popupTitle+"</div></div><div class='header1-button header1-buttonbar-right' onclick='closePagePopup()'>X</div></div></div></div><div class='main1 location-controls-page-internal'><div id='"+pagePopupId+"-content' class='location-controls' src='"+url+"'><img id='banner-loading-icon' src='/javascript/images/wait.gif' border=0/></div></div></div>");
	
	$("#"+pagePopupId+"-content").load(url);
	
	
	if (closeCallback!=null)
		popupStackCloseCallbackHandlers.push(closeCallback);
	else
		popupStackCloseCallbackHandlers.push(null);
}

$(function(){
	$(".chat-container").stick_in_parent({bottoming:false});	
});
</script>
	<title>Main - Initium</title>



<script type='text/javascript'>
	$(document).ready(function (){
		<c:if test="${combatSite==true}">
		loadInlineItemsAndCharacters();
		</c:if>
		
		<c:if test="${isCollectionSite==true}">
		loadInlineCollectables();
		</c:if>
		
		// Request permission to use desktop notifications
		notifyHandler.requestPermission();		
	});
</script>

<script type='text/javascript' src='odp/javascript/banner-weather.js?v=6'></script>
<script type='text/javascript' src='odp/javascript/soundeffects.js?v=13'></script>

<script id='ajaxJs' type='text/javascript'>
${bannerJs}
</script>

<script type='text/javascript'>
	if (isAnimationsEnabled())
	{
		$.preload("https://initium-resources.appspot.com/images/anim/walking.gif", 
				"https://initium-resources.appspot.com/images/anim/props/tree1.gif",
				"https://initium-resources.appspot.com/images/anim/props/tree2.gif",
				"https://initium-resources.appspot.com/images/anim/props/tree3.gif",
				"https://initium-resources.appspot.com/images/anim/props/tree4.gif",
				"https://initium-resources.appspot.com/images/anim/props/tree5.gif",
				"https://initium-resources.appspot.com/images/anim/props/tree6.gif",
				"https://initium-resources.appspot.com/images/anim/props/shrub1.gif",
				"https://initium-resources.appspot.com/images/anim/props/shrub2.gif",
				"https://initium-resources.appspot.com/images/anim/props/shrub3.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree1.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree2.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree3.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree4.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree5.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree6.gif",
				"https://initium-resources.appspot.com/images/anim/props/baretree7.gif",
				"https://initium-resources.appspot.com/images/anim/props/grass1.gif",
				"https://initium-resources.appspot.com/images/anim/props/grass2.gif",
				"https://initium-resources.appspot.com/images/anim/props/grass3.gif",
				"https://initium-resources.appspot.com/images/anim/props/grass4.gif",
				"https://initium-resources.appspot.com/images/anim/props/grass5.gif",
				"https://initium-resources.appspot.com/images/anim/props/grass6.gif"
				);
	}
</script>



<script type='text/javascript'>
${longOperationRecallJs}
</script>

<script type='text/javascript'>
	/*Antibot stuff*/
	<c:if test="${botCheck==true}">
		function onCaptchaLoaded(){
			antiBotQuestionPopup();
		}
	</c:if>
</script>

<script type='text/javascript'>
	/*Other javascript variables*/
	window.isPremium = ${isPremium};
	
	<c:if test="${autoOpen=='profile'}">
		$(document).ready(function(){
			viewProfile();
		});
	</c:if>
</script>

</head>

<!--
		HEY!!
		
Did you know you can help code Initium?
Check out our github and get yourself setup,
then talk to the lead dev so you can get yourself
on our slack channel!

http://github.com/Emperorlou/Initium-ODP 

                                           -->
<body>
	<%JspSnippets.allowPopupMessages(out, request);%>
	<div class='main-page'>
		<div class='header'>
			<div class='header-location above-page-popup'><a id='locationName' href='main.jsp'>${locationName }</a></div>
			<div class='header-stats above-page-popup'>
				<a onclick='viewMap()'><img src='https://initium-resources.appspot.com/images/ui/globe2.png' border=0/></a> 
				<a onclick='inventory()'><img src='https://initium-resources.appspot.com/images/small/Pixel_Art-Storage-Bags-Bags4.png' border=0/><div class='header-stats-caption'>EQUIP</div></a> 
				<span><img src='https://initium-resources.appspot.com/images/dogecoin-18px.png' border=0/><div class='header-stats-caption-alwayson' id='mainGoldIndicator'>${characterDogecoinsFormatted}</div></span> 
				<a onclick='viewSettings()'><img src='https://initium-resources.appspot.com/images/ui/settings.gif' border=0 style='max-height:18px'/></a> 
				<a onclick='toggleEnvironmentSoundEffects()'><img id='header-mute' src='https://initium-resources.appspot.com/images/ui/sound-button1.png' border=0 style='max-height:18px'/></a>
			</div>
		</div>
	</div>

	<div class='main-page'>
		<img class='main-page-banner-image' src="https://initium-resources.appspot.com/images/banner-backing.jpg" border=0/>
		<div style="position:absolute; top:27px;z-index:1000100;">
		<img class='main-page-banner-image' src="https://initium-resources.appspot.com/images/banner-backing.jpg" border=0/>
		
		<div class='main-banner' >
			<img class='main-page-banner-image' src="https://initium-resources.appspot.com/images/banner---placeholder.gif" border=0/>
			<c:if test="${isOutside=='TRUE' }">
				<div class='banner-shadowbox'>
			</c:if>
			<c:if test="${isOutside!='TRUE' }">
				<div class='banner-shadowbox' style="background: url('https://initium-resources.appspot.com/images/banner---placeholder.gif') no-repeat center center; background-size:cover;">
			</c:if>
				
			
				<div style="overflow:hidden;position:absolute;width:100%;height:100%;">
					<div id='banner-base' class='banner-daynight'></div>
					<div id='banner-text-overlay'>${bannerTextOverlay}</div>
<!-- 					<div id='main-viewport-container'></div> -->
					<div id='inBannerCharacterWidget' class='characterWidgetContainer'>
						${inBannerCharacterWidget}
					</div>
					<div id='immovablesPanel'>${immovablesPanel}</div>				
				</div>
				</div>
			</div>
		</div>
		
		
		<div style="text-align:right">Check out the progress on the <a href="odp/game">new UI here!</a></div>
 		<div class='chat_box above-page-popup'>
			<div class='chat_tab_container'>
				<span class='chat_tab_button_container'><a id='chat_box_minimize_button' onclick='toggleMinimizeBox(event, ".chat_box");' class='chat_tab_toggle_minimize'>V</a></span><span class='chat_tab_button_container'><a id='GameMessages_tab' class="chat_tab" onclick='changeChatTab("GameMessages")'><span class='chat-button-indicator' id='GameMessages-chat-indicator'></span>!</a></span><span class='chat_tab_button_container'><a id='GlobalChat_tab' class='chat_tab chat_tab_selected' onclick='changeChatTab("GlobalChat")'><span class='chat-button-indicator' id='GlobalChat-chat-indicator'></span>Global</a></span><span class='chat_tab_button_container'><a id='LocationChat_tab' class='chat_tab' onclick='changeChatTab("LocationChat")'><span class='chat-button-indicator' id='LocationChat-chat-indicator'></span>Location</a></span><span class='chat_tab_button_container'><a id='GroupChat_tab' class="chat_tab" onclick='changeChatTab("GroupChat")'><span class='chat-button-indicator' id='GroupChat-chat-indicator'></span>Group</a></span><span class='chat_tab_button_container'><a id='PartyChat_tab' class="chat_tab" onclick='changeChatTab("PartyChat")'><span class='chat-button-indicator' id='PartyChat-chat-indicator'></span>Party</a></span><span class='chat_tab_button_container'><a id='PrivateChat_tab' class="chat_tab" onclick='changeChatTab("PrivateChat")'><span class='chat-button-indicator' id='PrivateChat-chat-indicator'></span>Private</a></span><a onclick='helpPopup();' class='chat_tab_help_button'>?</a>
			</div>
			<div id="chat_tab">
				<div id="chat_form_wrapper">
					<form id="chat_form">
						<span class='fullscreenChatButton' onclick='toggleFullscreenChat()'>[&nbsp;]</span>
						<div class='chat_form_input'>
							<input id="chat_input" type="text" autocomplete="off" placeholder='Chat with anyone else in this location' maxlength='2000'/>
						</div>
						<div class='chat_form_submit'>
							<input id="chat_submit" type="submit" value='Submit'/>
						</div>
					</form>
				</div>
				<div class='chat_messages' id="chat_messages_GameMessages">This is not yet used. It will be where you can see all of the game messages, in particular - combat messages.</div>
				<div class='chat_messages' id="chat_messages_GlobalChat"></div>
				<div class='chat_messages' id="chat_messages_LocationChat"></div>
				<div class='chat_messages' id="chat_messages_GroupChat"></div>
				<div class='chat_messages' id="chat_messages_PartyChat"></div>
				<div class='chat_messages' id="chat_messages_PrivateChat"></div>
			</div>
			<div class='chat_tab_footer'>
				<a class='clue' rel='/odp/ajax_ignore.jsp' style='float:left'>Ignore Players</a>
				<span id='ping' title='Your connection speed with the server in milliseconds'>??</span>
				&#8226; 
				<c:if test="${usedCustomOrders}">
					<a onclick='customizeItemOrderPage()'>View Custom Orders</a> 
					&#8226; 
				</c:if>
				<a onclick='viewReferrals()'>View Active Referral Urls</a> 
				&#8226; 
				Active players: <span id='activePlayerCount'>${activePlayers}</span>
			</div>
			<script type="text/javascript">updateMinimizeBox("#chat_box_minimize_button", ".chat_box")</script>
		</div>
		<div id='buttonbar'>${buttonBar}</div>
		
		<div class='main-bottomhalf'>
			<div id='page-popup-root'></div> 
	
			<div id='territoryView'>
			${territoryViewHtml}
			</div>
			
			<c:if test="${isDefenceStructure}">
				<div class='hiddenTooltip' id='defenders'>
					<h5>Active Defenders</h5>
					Defenders are player characters (or NPCs in the case of an event) that will defend against attacking players. Players can only defend
					while in a defence structure like the one you're looking at now. There are 4 stances: 1st Line, 2nd Line, 3rd Line, and Not Defending.
					Defenders that are in the 1st line will always be the first to enter combat. If no other defenders are available when an attacker
					approaches, then the 2nd Line stanced players will engage...etc. Players that are 'Not Defending' will never engage in PvP, however
					if the building becomes overrun, they can be kicked out by the attacking players. 
				</div>
				<div class='hiddenTooltip' id='defenderCount'>This is the number of defenders that are set to defend in this particular stance.</div>
				<div class='hiddenTooltip' id='engagedDefenders'>This is the number of defenders that are currently engaged in combat.</div>
				<div class='hiddenTooltip' id='joinedDefenderGroup'>This means that you are part of this group of defenders (1st line, 2nd line, 3rd line, or not defending). To choose a different stance, click on the one of the Join links.</div>
				<div class='boldbox' id='defenceStructureBox'>
					<div class='hiddenTooltip' id='defencestructures'>
						<h5>Defence Structures</h5>
						<p>
							Defence structures are used in PvP and territory control. These structures have the capability
							of blocking other players from entering into the location that the structure stands. The leader
							of the structure has some control over this and is able to set the defence mode to:
							<ul>
							<li>Blockade all players from passing through the location the structure was built in</li>
							<li>Defend only the structure itself from intruders</li>
							<li>Disable all defences and open the structure up to the public</li>
							</ul>
							When the defence structure is in defence mode, there needs to be people in the 1st, 2nd, or 3rd 
							defence lines. Otherwise, the structure is publicly accessible and anyone could take it over without
							opposition. To become a leader of a defence structure, you need to be the first person to enter
							the defensive lines. This can be done after all of the defenders are killed, or if the structure is
							unoccupied or simply not defended.
						</p>
						<p>
							Defenders of a structure are always passively defending, even when the player is offline. After
							each combat with an attacking player, the defender will always heal fully; an attacking player must
							kill the defender without running to heal. Loot dropped by killed defenders are dropped within the 
							structure whereas loot dropped by attackers are dropped in the location the attacker was in before
							he initiated the attack. 
						</p>
						<p>
						For more information on defence structures, <a href='odp/mechanics.jsp#defencestructures'>visit the game mechanics page</a>.
						</p>
					</div>
					<script type="text/javascript">updateMinimizeBox("#defenceStructureMinimizeButton", "#defenceStructureBox")</script>
					<h4><a id='defenceStructureMinimizeButton' onclick='toggleMinimizeBox(event, "#defenceStructureBox");' class=''>&#8711;</a> Defensive Structure
						<span class='hint' rel='#defencestructures' style='float:right'><img src='https://initium-resources.appspot.com/images/ui/help.png' border=0 style='max-height:19px;'/></span>			
					</h4>
					<div>Structural Integrity: ${defenceStructureHitpoints}/${defenceStructureMaxHitpoints}</div>
					<p>${statusDescription}</p>
					
						<div class='smallbox'>
							Current Leader
							<p>
							<c:if test='${leader==null}'>
								None
							</c:if>
							<c:if test='${leader!=null }'>
								GameUtils.renderCharacter(null, leader)
							</c:if>
							</p>
						</div>
					
					<c:if test='${isLeader}'>
					
						<div class='smallbox'>
						<h5>Defence Structure Controls</h5>
							
						Defence Mode
						<div class='main-item-controls'>
							<a onclick='setBlockadeRule("BlockAllParent")' class='${defenceModeBlockAllParent}'>Blockade&nbsp;Everything</a>
							<a onclick='setBlockadeRule("BlockAllSelf")' class='${defenceModeBlockAllSelf}'>Defend&nbsp;Structure&nbsp;Only</a>
							<a onclick='setBlockadeRule("None")' class='${defenceModeNone}'>No&nbsp;Defence</a>
						</div>
						</div>
					
					</c:if>
					<h5 class='hint' rel='#defenders'>Active Defenders </h5>
					<div class='main-item-controls'>
						<a class='clue' rel='viewdefendersmini.jsp'>View Defenders</a>
					</div>
					<br>
					<div style='text-align:center'>
					<div class='smallbox'>
						1st Line
						<p><span class='hint' rel='#engagedDefenders'>${defenderEngaged1Count}</span>/<span class='hint' rel='#defenderCount'>${defender1Count}</span></p>
						<c:if test="${characterStatus!='Defending1'}">
							<p><a onclick='enterDefenceStructureSlot("Defending1")'>Join</a></p>
						</c:if>
						<c:if test="${characterStatus=='Defending1'}">
							<p class='hint' rel='#joinedDefenderGroup'>Joined</p>
						</c:if>
					</div>
					<div class='smallbox'>
						2nd Line
						<p><span class='hint' rel='#engagedDefenders'>${defenderEngaged2Count}</span>/<span class='hint' rel='#defenderCount'>${defender2Count}</span></p>
						<c:if test="${characterStatus!='Defending2'}">
							<p><a onclick='enterDefenceStructureSlot("Defending2")'>Join</a></p>
						</c:if>
						<c:if test="${characterStatus=='Defending2'}">
							<p class='hint' rel='#joinedDefenderGroup'>Joined</p>
						</c:if>
					</div>
					<div class='smallbox'>
						3rd Line
						<p><span class='hint' rel='#engagedDefenders'>${defenderEngaged3Count}</span>/<span class='hint' rel='#defenderCount'>${defender3Count}</span></p>
						<c:if test="${characterStatus!='Defending3'}">
							<p><a onclick='enterDefenceStructureSlot("Defending3")'>Join</a></p>
						</c:if>
						<c:if test="${characterStatus=='Defending3'}">
							<p class='hint' rel='#joinedDefenderGroup'>Joined</p>
						</c:if>
					</div>
					<div class='smallbox'>
						Not Defending
						<p>${notDefendingCount}</p>
						<c:if test="${characterStatus!=null}">
							<p><a onclick='enterDefenceStructureSlot("Normal")'>Join</a></p>
						</c:if>
						<c:if test="${characterStatus==null}">
							<p class='hint' rel='#joinedDefenderGroup'>Joined</p>
						</c:if>
					</div>
					</div>
				</div>
			</c:if>
	
			<div id='instanceRespawnWarning'></div>
			
			<c:if test='${(clientDescription!=null &&clientDescription!="") || (midMessage!=null && midMessage!="") || (prefix!=null && prefix!="")}'>
			<div class='main-dynamic-content-box paragraph'>
				<c:if test='${clientDescription!=null && clientDescription!="" && clientDescription!="null"}'>
				${clientDescription}
				</c:if>
				<c:if test='${midMessage!=null && midMessage!="" && midMessage!="null"}'>
				${midMessage}
				</c:if>
			</div>
			</c:if>
	
			<div id='collectablesPanel' class='paragraph'>
				${collectablesPanel}
			</div>
			
			<div id='locationDescription' class='paragraph'>
				${locationDescription}
			</div>
			<c:if test="${combatSite==true}">
				<div class='boldbox'>
					<div id='inline-items' class='main-splitScreen'>
					</div>
					<div id='inline-characters' class='main-splitScreen'>
					</div>
				</div>
			</c:if>
			<c:if test="${supportsCamps!=null && supportsCamps>0}">
				<div class='main-description'>
					This location could host up to ${supportsCamps} camps.
				</div>
			</c:if>
			<c:if test="${supportsCamps==null || supportsCamps==0}">
				<div class='main-description'>
					This location is not suitable for a camp.
				</div>
			</c:if>	
			<div id='monsterCountPanel'>
			<c:if test="${monsterCountPanel!=null}">
				${monsterCountPanel}
			</c:if>	
			</div>
			<div class='main-splitScreen'>
				<div id='main-merchantlist'>
					<div class='main-button-half' onclick='loadLocationMerchants()' shortcut='83'>
	 					<span class='shortcut-key'> (S)</span><img src='https://initium-resources.appspot.com/images/ui/magnifying-glass.png' border=0/> Nearby stores
					</div>
				</div>
			</div>
			<div id='partyPanel' class='main-splitScreen'>
			${partyPanel}
			</div>
			<div></div>
			<div class='main-splitScreen'>
				<div id='main-itemlist'>
					<div class='main-button-half' onclick='loadLocationItems()' shortcut='86'>
	 					<span class='shortcut-key'> (V)</span><img src='https://initium-resources.appspot.com/images/ui/magnifying-glass.png' border=0/> Nearby items
					</div>
				</div>
			</div>
			<div class='main-splitScreen'>
				<div id='main-characterlist'>
					<div class='main-button-half' onclick='loadLocationCharacters()' shortcut='66'>
	 					<span class='shortcut-key'> (B)</span><img src='https://initium-resources.appspot.com/images/ui/magnifying-glass.png' border=0/> Nearby characters
					</div>
				</div>
			</div>
			<div class='main-buttonbox'>
			
			<div id='locationScripts'>${locationScripts}</div>
			
			
			<div id='main-button-list'>${mainButtonList}</div>
			
			</div>
		</div>
	</div>
	
	<div id='test-panel'>
	${testPanel}
	</div>
	
	<div class='mobile-spacer'></div>

	<c:if test="${isThrowawayInSession==true}">
		<p id='throwawayWarning' class='highlightbox-red' style='position:fixed; bottom:0px;z-index:9999999; left:0px;right:0px; background-color:#000000;'>
			WARNING: Your throwaway character ${throwawayName} associated with this browser could be destroyed at any time! <a href='signup.jsp?convertThrowaway=true'>Click here to convert your character to a full account. It's free!</a><br>
			<br>
			Alternatively, you can <a onclick='destroyThrowaway()'>destroy your throwaway character</a>.
		</p>
	</c:if>
</body>
</html>
