<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<!doctype html>
<html>
<head>
	<script type='text/javascript'>
	window.paceOptions = {
			ajax: false,
			restartOnRequestAfter: false
	};
	</script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/pace/1.0.2/pace.min.js"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/pace/1.0.2/themes/black/pace-theme-center-simple.css" />
	
<meta charset="UTF-8">    
<meta http-equiv="content-type" content="text/html;charset=utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no"/> 
<!-- <meta name="viewport" content="minimum-scale=0.3, maximum-scale=1"/> -->
<meta name="keywords" content="initium, game, web game, video game, free to play, mmorpg, mmo">
<meta name="referrer" content="no-referrer" />

<script type="text/javascript" src="https://code.createjs.com/preloadjs-0.6.2.min.js"></script>
<script type="text/javascript" src="https://code.createjs.com/soundjs-0.6.2.min.js"></script>

<script type="text/javascript" src="/javascript/modernizr.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js" onload="if (window.module!=null) window.$ = window.jQuery = module.exports;"></script>
<script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jquery-scrollTo/2.1.2/jquery.scrollTo.min.js"></script>
<script type="text/javascript" src="/javascript/jquery.browser.min.js"></script>
<script type="text/javascript" src="/javascript/jquery.preload.min.js"></script>
<script type="text/javascript" src="/odp/javascript/sockjs.min.js"></script>
<script type="text/javascript" src="/odp/javascript/vertx-eventbus.js"></script>
<script type="text/javascript" src="/odp/javascript/seedrandom.js"></script>
<script type="text/javascript" src="/javascript/jquery.cluetip.all.min.js"></script>
<link type="text/css" rel="stylesheet" href="/javascript/jquery.cluetip.css"/>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.finger/0.1.6/jquery.finger.min.js"></script>

<script type="text/javascript" src="/odp/javascript/script.js?v=${version}"></script>
<link type="text/css" rel="stylesheet" href="/odp/MiniUP.css?v=${version}">



<script type="text/javascript" src="/odp/javascript/messager.js?v=70"></script>

<script type="text/javascript" src="/odp/javascript/PopupNotifications.js?v=3"></script>
<script type="text/javascript" src="/odp/javascript/BrowserPopupNotifications-impl.js?v=3"></script>





<link type="text/css" rel="stylesheet" href="/javascript/rangeslider/rangeslider.css"/>
<script src="/javascript/rangeslider/rangeslider.min.js"></script>

<script src='/odp/javascript/openseadragon/openseadragon.min.js'></script>
<script src='/odp/javascript/map.js?t=4'></script>

<script src="https://www.google.com/recaptcha/api.js?onload=onCaptchaLoaded&render=explicit" async defer></script>

<script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-62104245-1', 'auto');
  ga('send', 'pageview');

  var loc = window.parent.location.toString();
  if (loc.indexOf("RemoteRunner.html")!=-1) {
    document.location="about:blank";
  }
  
</script>


<script type="text/javascript">
	<c:if test='${chatIdToken!=null}'>
	window.chatIdToken = "${chatIdToken}";
	</c:if>
	<c:if test='${characterId!=null}'>
	window.characterId = ${characterId};
	</c:if>
	<c:if test='${newChatIdToken!=null}'>
	window.newChatIdToken = "${newChatIdToken}";
	</c:if>
	<c:if test='${userId!=null}'>
	window.userId = ${userId};
	</c:if>
	window.isTestServer = ${isTestServer};
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
			cluetipClass: '',
			showTitle: false, 
			height: 'auto', 
			width: 299,
	        sticky: true, 
	        closePosition: 'title',
	        arrows: true,
	        ajaxCache: false,
	        mouseOutClose: false,
	        cluezIndex: 20000000,
	        onShow: function(e) 
	        {
	        	$("#cluetip-waitimage").css('z-index', 20000000); 
	        	$("#cluetip").css('z-index', 20000000).css("box-shadow", "rgba(0, 0, 0, 1) 3px 3px 14px").addClass("v3-window3");
	        	return true;
	        }
	        
	    }).addClass("hasTooltip");
	    event.preventDefault();
	});
	
	
	$(document).delegate(".clue:not(.hasTooltip)", "mouseenter", function (event) {

		
	    $(this).cluetip(
	    {
			cluetipClass: '',
			showTitle: false, 
			height: 'auto', 
			width: 299,
	        sticky: true, 
	        closePosition: 'title', 
	        arrows: true,
	        ajaxCache: false,
	        mouseOutClose: false,
	        activation:"click",
	        cluezIndex: 20000000,
	        onShow: function() {
	            // close cluetip when users click outside of it
	            $(document).click(function(e) {
	                var isInClueTip = $(e.target).closest('#cluetip');
	                if (isInClueTip.length === 0) {
	                	$(document).trigger('hideCluetip');
	                }
	            })
	            
	            // Make the cluetip on top of everything
	        	$("#cluetip-waitimage").css('z-index', 20000000); 
	        	$("#cluetip").css('z-index', 20000000).css("box-shadow", "rgba(0, 0, 0, 1) 3px 3px 14px").addClass("v3-window3");
	        	return true;
	        }	        
	    }).addClass("hasTooltip");
	    event.preventDefault();
	});

	
	$(document).delegate(".hint:not(.hasTooltip)", "mouseenter", function (event) {

		
	    $(this).cluetip(
	    {
			cluetipClass: '',
			showTitle: false, 
			height: 'auto', 
			width: 299,
	        sticky: true, 
	        closePosition: 'title',
	        closeText: ' ',
	        arrows: true,
	        ajaxCache: false,
	        mouseOutClose: false,
	        activation:"click",
	        local:true,
	        cluezIndex: 20000000,
	        onShow: function() {
	            // close cluetip when users click outside of it
	            $(document).click(function(e) {
	                var isInClueTip = $(e.target).closest('#cluetip');
	                if (isInClueTip.length === 0) {
	                	$(document).trigger('hideCluetip');
	                }
	            })

	            // Make the cluetip on top of everything
	        	$("#cluetip-waitimage").css('z-index', 20000000); 
	        	$("#cluetip").css('z-index', 20000000).css("box-shadow", "rgba(0, 0, 0, 1) 3px 3px 14px").addClass("v3-window3");
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

	
	<title>Main - Initium</title>





<script type='text/javascript' src='/odp/javascript/banner-weather.js?v=12'></script>
<script type='text/javascript' src='/odp/javascript/soundeffects.js?v=13'></script>


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

<script type='text/javascript' src='/odp/javascript/messager-impl.js?v=53'></script>


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
	
	$(document).ready(function(){
		// Request permission to use desktop notifications
		notifyHandler.requestPermission();
		
		// Start the tutorial if tutorial has been specified
		if (location.search!=null && location.search.indexOf("tutorial=true")>-1)
			uiTutorial.run();
		
		if (location.search!=null && location.search.indexOf("welcome=true")>-1)
			createDonationWindow();
		
		updateBannerSize();
		
	});

	
	$(function() {
	    Pace.on("done", function(){
	        $("#contents").fadeIn(1000);
	    });
	});
</script>


<script type='text/javascript' src='/odp/javascript/reveal-tutorial.js?v=15'></script>
<script type='text/javascript' src='/odp/javascript/reveal-tutorial-impl.js?v=6'></script>

<!-- Dynamically add style tags that modify -->
<style id='msw'>
</style>
<script type='text/javascript'>
function modifyMaxScreenWidth()
{
	var msw = getMaxScreenWidth();
	if (msw!=1280)
	{
		$("#msw").empty();
		$("#msw").append(".main-page{max-width:"+parseInt(msw)+"px;}");
	}
	else
	{
		$("#msw").empty();		
	}
}

modifyMaxScreenWidth();

function updateBannerSize()
{
	var win = $(window);
	if (win.width()<=400)
		$("#banner").css("height", (win.height()-309+40)+"px");
	else if (win.width()<=480)
		$("#banner").css("height", (win.height()-309)+"px");
	else
		$("#banner").css("height", (win.height()-333)+"px");
}
$(window).resize(updateBannerSize);


function incrementStackIndex()
{
	currentPopupStackIndex++;
    if (currentPopupStackIndex==1)
    {
		$("#page-popup-root").html("");
	    $(document).bind("keydown", popupKeydownHandler);
	    $(".main-page").addClass("main-page-half");
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
		$(document).unbind("keydown", popupKeydownHandler);
	    $(".main-page").removeClass("main-page-half");
	}
	else
	{
		$("#page-popup"+currentPopupStackIndex).show();
		$("#page-popup"+(currentPopupStackIndex+1)).remove();
	}
	return currentPopupStackIndex;
}

</script>

<style>
#buttonbar-main
{
	padding-top:5px;
}	
.major-banner-links
{
	margin-left:-116px;
}



@media (min-width:1200px)
{

	.page-popup
	{
    position: fixed;
    z-index: 2;
    bottom: 0px;
    top: 0px;
    left: 50%;
    right: -29px;
    margin: 0px;
	}
	
	.page-popup-title
	{
		position:absolute;
	    text-align: center;
	    top: -32px;
	    width: 100%;
	    margin: 0 auto;
	    left: 0px;	
	}
	
	.page-popup-title h4
	{
		top:0px;
	}
	
	.page-popup-content
	{
		overflow-y:auto;
		height:100%;
	}
	
	#page-popup-root
	{
		z-index:10000000;
	}
	
	.main-page-half
	{
	    margin: 0px!important;
	    position: relative!important;
	    width: 50%!important;
	}

}

@media (max-width:720px)
{
	.major-banner-links
	{
		margin-left:-70px;
	}
}

@media (max-width:480px)
{
	#buttonbar-main
	{
		padding-top:3px;
	}
}


</style>

</head>

<!--
		HEY!!
		
Did you know you can help code Initium?
Check out our github and get yourself setup,
then talk to the lead dev so you can get yourself
on our slack channel!

http://github.com/Emperorlou/Initium-ODP 

Version: ${version}
                                           -->
<body>
<!-- Fixes a bug with chrome regarding mix-blending -->
<div id='mix-blending-fix'></div>
<div id='popups'></div>
<div id='contents'>

	<div class='main-page popupBlurrable'>
		<div class='header v3-header1'>
			<div class='header-inner'>
				<div id='pullout-button' class='header-inner-section'><a onclick='toggleMainPullout()'><img src='https://initium-resources.appspot.com/images/ui3/header-button-options1.png' alt='Pullout menu' height='34px'/></a></div>
				<div class='header-inner-section' style='width:100%;vertical-align: middle; text-align: center;'><div style='margin-left:-32px; margin-right:-22px;'><a id='locationName' onclick='location.reload()'>${locationName}</a></div></div>
				<div id='mainMoneyIndicator' class='header-inner-section' onclick='viewProfile()'>
					${mainMoneyIndicator}
				</div>
				<!-- <div id='map-button' class='header-inner-section'><a onclick='viewMap()'><img alt='View player-made world map' src='https://initium-resources.appspot.com/images/ui3/header-button-map1.png' height='34px'/></a></div> -->
				<div id='settings-button' class='header-inner-section'><a onclick='viewSettings()'><img alt='Game settings' src='https://initium-resources.appspot.com/images/ui3/header-button-settings1.png' height='34px'/></a></div>
				<div id='sound-button' class='header-inner-section'><a onclick='toggleEnvironmentSoundEffects()'><img alt='Sound effects' src='https://initium-resources.appspot.com/images/ui3/header-button-sound-on1.png' height='34px'/></a></div>
			</div>
		</div>
	</div>
		
	<div class='main-page popupBlurrable'>
		<div id='banner'>
		
			<div class='main-banner' >
				<c:if test="${isOutside=='TRUE' }">
					<div class='banner-shadowbox'>
				</c:if>
				<c:if test="${isOutside!='TRUE' }">
					<div class='banner-shadowbox' style="background: url('https://initium-resources.appspot.com/images/banner---placeholder.gif') no-repeat center center; background-size:cover;">
				</c:if>
					
				
					<div style="overflow:hidden;position:absolute;width:100%;height:100%;">
						<div id='banner-base' class='banner-daynight'></div>
						<div id='banner-fx'></div>
	<!-- 					<div id='main-viewport-container'></div> -->
						<div id='partyPanel'>
						${partyPanel}
						</div>
						<div id='inBannerCharacterWidget' class='characterWidgetContainer'>
							${inBannerCharacterWidget}
						</div>
						<div id='inBannerCombatantWidget' class='combatantWidgetContainer'>
							${inBannerCombatantWidget }
						</div>
						<div id='immovablesPanel'>${immovablesPanel}</div>				
						<div id='banner-text-overlay'>${bannerTextOverlay}</div>
					</div>
				</div>
			</div>
		</div>
<!-- 	
		<div class='highlightbox-red paragraph' style='margin:0px'>
		The game is semi-down for the moment. Sorry for the inconvenience! I'm waiting to be contacted back by a rep from Google Cloud Platform regarding this issue.
		</div>
 -->		
<!--
		<div id='promo-24hr-2x-2' class="highlightbox-green paragraph" style='margin:0px'>
		<a onclick="closeClosableMessage('promo-24hr-2x-2')" style="float: left;padding-right: 12px;">X</a>
		The 2x promo for donation credit has been extended due to popular demand. For the next 24 hours (until mid-day November 4th) for every dollar you donate you will receive 2 dollars donation credit! <a onclick="viewProfile()">Take advantage!</a></div>
		<script type='text/javascript'>initializeClosableMessage("promo-24hr-2x-1");</script>
		 <div id='newui-progress-link' style="text-align:right; font-size:13px;margin-bottom:2px">Check out the progress on the <a href="odp/game">new UI here!</a></div> 
-->
		<div id='buttonbar' class='v3-header1-flipped'>${buttonBar}</div>
 		<div class='chat_box above-page-popup v3-window1'>
 			<div class='chat_box_inner'>
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
					<div class='chat_messages' id="chat_messages_GameMessages"></div>
					<div class='chat_messages' id="chat_messages_GlobalChat"></div>
					<div class='chat_messages' id="chat_messages_LocationChat"></div>
					<div class='chat_messages' id="chat_messages_GroupChat"></div>
					<div class='chat_messages' id="chat_messages_PartyChat"></div>
					<div class='chat_messages' id="chat_messages_PrivateChat"></div>
				</div>
				<div class='chat_tab_footer'>
					<a id='ignore-players' class='clue' rel='/odp/ajax_ignore.jsp' style='float:left'>Ignore Players</a>
					
					<c:if test="${usedCustomOrders}">
						<a onclick='customizeItemOrderPage()'>Customs</a> 
						&#8226; 
					</c:if>
					<a onclick='viewReferrals()'>View Active Referral Urls</a> 
					&#8226; 
					Players: <span id='activePlayerCount'>${activePlayers}</span>
					<span id='ping' title='This indicates whether or not you&quot;re connected to the chat server'> &#9679;</span>
				</div>
				<script type="text/javascript">updateMinimizeBox("#chat_box_minimize_button", ".chat_box")</script>
			</div>
		</div>
		
		<div>
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
								<!-- %=GameUtils.renderCharacter(null, leader) %> -->
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
			
			<div id='midMessagePanel'>
				${midMessagePanel}
			</div>
	
			<div id='collectablesPanel' class='paragraph'>
				${collectablesPanel}
			</div>
<!-- 			
			<div id='locationDescription' class='paragraph'>
				${locationDescription}
			</div>
-->
			<div id='locationQuicklist'>
				${locationQuicklist}
			</div>
            <!-- <div id='monsterCountPanel'>${monsterCountPanel}</div> -->
			
			<div id='locationScripts'>${locationScripts}</div>
			
			
			<div id='main-button-list'>${mainButtonList}</div>
			
			</div>
		</div>
	</div>

	<div class='hiddenPullout' id='main-pullout'>
		<div class='hiddenPullout-content'>
			<h5 style='margin-top:0px;'>Your Referrals</h5>
			<p><a href='${refUrl}' title='Share this link online and with your friends'>Your referral link (share this!)</a></p>
			<div style='margin-left:10px'>
			<p>
				Referral views: ${referralViews}<br>
				Referral signups: ${referralSignups}<br>
				Referral donations: $${referralDonations}
			</p>
			</div>
			<br/>
			<p><a onclick='toggleMainPullout(); viewCharacterSwitcher()'>Switch characters</a></p>
			<p><a onclick='toggleMainPullout(); viewProfile()'>View your profile</a></p>
			<p><a onclick='toggleMainPullout(); popupCharacterTransferService(${characterId}, "<c:out value="${characterName}"/>", "<c:out value="${characterToTransfer}"/>")' style='cursor:pointer'>Open the Character Transfer Service</a></p>
			<p><a onclick='toggleMainPullout(); uiTutorial.run();'>Watch the UI tutorial</a></p>
			<p><a onclick='toggleMainPullout(); viewAutofix()'>Help! Something's Wrong!</a></p>
			<p><a onclick='toggleMainPullout(); logout()'>Logout</a></p>
		</div>
	</div>

	<script id='ajaxJs' type='text/javascript'>
	${bannerJs}
	</script>
	
	<div id='test-panel'>
	${testPanel}
	</div>
	
	<c:if test="${isThrowawayInSession==true}">
		<p id='throwawayWarning' class='highlightbox-red' style='position:fixed; bottom:0px;z-index:9999999; left:0px;right:0px; background-color:#000000;'>
			WARNING: Your throwaway character ${throwawayName} associated with this browser could be destroyed at any time! <a href='signup.jsp?convertThrowaway=true'>Click here to convert your character to a full account. It's free!</a><br>
			<br>
			Alternatively, you can <a onclick='destroyThrowaway()'>destroy your throwaway character</a>.
		</p>
	</c:if>

	</div>
	
	<div class='map-contents global-navigation-map'>${globalNavigationMap}</div>	
	<div id='loot-popup'></div>
</body>
</html>
