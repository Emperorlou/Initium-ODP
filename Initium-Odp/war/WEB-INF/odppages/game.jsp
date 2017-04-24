<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>


<!DOCTYPE html>
<html>
<head>
	<title>Main - Initium</title>

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
<script type="text/javascript" src="/odp/javascript/script.js?v=70"></script>

<script type="text/javascript" src="/odp/javascript/messager.js?v=18"></script>

<script type="text/javascript" src="/odp/javascript/PopupNotifications.js?v=3"></script>
<script type="text/javascript" src="/odp/javascript/BrowserPopupNotifications-impl.js?v=3"></script>



<script type="text/javascript" src="/javascript/jquery.cluetip.all.min.js"></script>
<link type="text/css" rel="stylesheet" href="/javascript/jquery.cluetip.css"/>

<link type="text/css" rel="stylesheet" href="/odp/MiniUP.css?v=60">

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

<script type='text/javascript' src='/odp/javascript/messager-impl.js'></script>

<script type='text/javascript' src='/odp/javascript/soundeffects.js?v=1'></script>
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

</head>

<!--
		HEY!!
		
Did you know you can help code Initium?
Check out our github and get yourself setup,
then talk to the lead dev so you can get yourself
on our slack channel!

https://github.com/Emperorlou/Initium-ODP

                                           -->

<body id='newui'>
	<div id="popups"></div>
	<div class='page popupBlurrable'>
		<div class='location-controls-container half-page-variant'></div>
		<div class='page-upperhalf'>
			<div id='main-header' class='header1'>
				<div class='header1-spacer'></div>
				<div class='header1-display'><span id='locationName'><a href='/odp/game'>${locationName}</a></span></div>
				<div class='header1-spacer'></div>
				<div class='header1-rightchunk'>
					<div class='header1-button' onclick='viewMap()'>MAP</div>
					<div class='header1-spacer'></div>
					<div class='header1-button' onclick='inventory()'>EQUIP</div>
					<div class='header1-spacer'></div>
					<div class='header1-display'><img src='https://initium-resources.appspot.com/images/dogecoin-18px.png' border=0/><span id='mainGoldIndicator'>${mainGoldIndicator}</span></div>
					<div class='header1-spacer'></div>
				</div>
			</div>
			<div id='main-banner' class='banner1'>
				<img id='banner-sizer' src='https://initium-resources.appspot.com/images/banner---placeholder2.png' border=0/>
				<div id='banner-base'></div>
				<div id='main-viewport-container'></div>							
				<div id='inBannerCharacterWidget' class='characterWidgetContainer'>
					${inBannerCharacterWidget}
				</div>				
			
			</div>
		</div>
		<div class='page-maincontent'>
			<div class='chat-container'>
				<div class='header1 chat-header'>
					<div class='header1-spacer'></div>
					<div class='header1-button chat-button' id='GameMessages_tab_newui' onclick='changeChatTab("GameMessages")'><div id='GameMessages-chat-indicator' class='chat-button-indicator'></div>!</div>
					<div class='header1-spacer'></div>
					<div class='header1-button chat-button' id='GlobalChat_tab_newui' onclick='changeChatTab("GlobalChat")'><div id='GlobalChat-chat-indicator' class='chat-button-indicator'></div>GLOBAL</div>
					<div class='header1-spacer'></div>
					<div class='header1-button chat-button' id='LocationChat_tab_newui' onclick='changeChatTab("LocationChat")'><div id='LocationChat-chat-indicator' class='chat-button-indicator'></div>LOCATION</div>
					<div class='header1-spacer'></div>
					<div class='header1-button chat-button' id='GroupChat_tab_newui' onclick='changeChatTab("GroupChat")'><div id='GroupChat-chat-indicator' class='chat-button-indicator'></div>GROUP</div>
					<div class='header1-spacer'></div>
					<div class='header1-button chat-button' id='PartyChat_tab_newui' onclick='changeChatTab("PartyChat")'><div id='PartyChat-chat-indicator' class='chat-button-indicator'></div>PARTY</div>
					<div class='header1-spacer'></div>
					<div class='header1-button chat-button' id='PrivateChat_tab_newui' onclick='changeChatTab("PrivateChat")'><div id='PrivateChat-chat-indicator' class='chat-button-indicator' ></div>PRIVATE</div>
					<div class='header1-spacer'></div>
				</div>
				<div class='main1'>
					<div class='backdrop1d chat-text-container'>
						<div id="chat_tab">
				<div id="chat_form_wrapper">
					<form id="chat_form">
						<span class='fullscreenChatButton' onclick='toggleFullscreenChat()'>[&nbsp;]</span><input type='hidden' id='chatroomId' value='L${locationId}' />
						<div class='chat_form_input'>
							<input id="chat_input" type="text" autocorrect="on" spellcheck="true" autocapitalize="on" autocomplete="off" placeholder='Chat with anyone else in this location' maxlength='2000'/>
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


					</div>
				</div>
		<div class='newui-popup' id='page-popup-root'></div> 
				
				<div class='location-controls-container'>
					<div class='location-controls-page'>
						<div class='header1'>
							<div class='header1-buttonbar'>
								<div class='header1-buttonbar-inner'>
									<div class='header1-buttonbar-middle'>
										<div id='buttonbar'>${buttonBar}</div>
									</div>
								</div>
							</div>
						</div>
						<div class='main1 location-controls-page-internal'>
							<div class='location-controls'>
							
							
								<div id='mainButtonList' class='main1-inset1 location-controls-navigation'>
									${mainButtonList}
								</div>					
							</div>
						</div>
					</div>
				</div>
				
			</div>

		</div>
	 </div>
</body>
</html>