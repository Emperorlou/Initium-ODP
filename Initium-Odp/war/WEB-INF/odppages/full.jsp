<html class=" multiplebgs backgroundblendmode backgroundcliptext">
<head>
	<meta http-equiv="Content-Security-Policy" content="
		default-src 'self'; 
		connect-src 'self'
			https://initium-resources.appspot.com
			https://eventserver.universeprojects.com:8080
			wss://eventserver.universeprojects.com:8080
			https://www.google-analytics.com;
		img-src 'self'
			https://i.imgur.com
			https://initium-resources.appspot.com
			https://www.google-analytics.com; 
		script-src 'self' 'unsafe-inline' 'unsafe-eval'
			https://code.jquery.com 
			https://cdnjs.cloudflare.com
			https://code.createjs.com
			https://ajax.googleapis.com
			https://www.google.com
			https://www.gstatic.com
			https://www.google-analytics.com; 
		style-src 'self' 'unsafe-inline' 
			https://cdnjs.cloudflare.com;
		">	
	<script async="" src="//www.google-analytics.com/analytics.js"></script><script type="text/javascript" async="" src="https://www.gstatic.com/recaptcha/api2/r20171206132803/recaptcha__en.js"></script><script type="text/javascript">
	window.paceOptions = {
			ajax: false,
			restartOnRequestAfter: false
	};
	</script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/pace/1.0.2/pace.min.js"></script>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/pace/1.0.2/themes/black/pace-theme-center-simple.css">
	
	













    
<meta charset="UTF-8">    
<meta http-equiv="content-type" content="text/html;charset=utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no"> 
<!-- <meta name="viewport" content="minimum-scale=0.3, maximum-scale=1"/> -->
<meta name="keywords" content="initium, game, web game, video game, free to play, mmorpg, mmo">
<meta name="referrer" content="no-referrer">

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
<link type="text/css" rel="stylesheet" href="/javascript/jquery.cluetip.css">
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.finger/0.1.6/jquery.finger.min.js"></script>

<script type="text/javascript" src="/odp/javascript/script.js?v=${version}"></script>
<!--<link type="text/css" rel="stylesheet" href="/odp/MiniUP.css?v=${version}">-->



<script type="text/javascript" src="/odp/javascript/messager.js?v=60"></script>

<script type="text/javascript" src="/odp/javascript/PopupNotifications.js?v=3"></script>
<script type="text/javascript" src="/odp/javascript/BrowserPopupNotifications-impl.js?v=3"></script>





<link type="text/css" rel="stylesheet" href="/javascript/rangeslider/rangeslider.css">
<script src="/javascript/rangeslider/rangeslider.min.js"></script>

<script src="/odp/javascript/openseadragon/openseadragon.min.js"></script>
<script src="/odp/javascript/map.js?t=4"></script>

<script src="https://www.google.com/recaptcha/api.js?onload=onCaptchaLoaded&amp;render=explicit"></script>

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
	
	window.chatIdToken = "/yEgMaCxLoDFn4VTv0fhJUVopqIST47l";
	
	
	window.characterId = 5502471308836864;
	
	
	window.newChatIdToken = "/yEgMaCxLoDFn4VTv0fhJUVopqIST47l";
	
	
	window.userId = 5299602559336448;
	
	window.isTestServer = false;
	window.verifyCode = "$2a$04$OeKHYqaFvelD50mSFJ5QdO5yMUccltiWeTlSoOiJyxT4oR8O5qxh6";
	window.serverTime=1513191220293;
	window.clientTime=new Date().getTime();
	


	
	
	
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
	        cluezIndex: 2000000,
	        onShow: function(e) 
	        {
	        	$("#cluetip-waitimage").css('z-index', 2000000); 
	        	$("#cluetip").css('z-index', 2000000).css("box-shadow", "rgba(0, 0, 0, 1) 3px 3px 14px").addClass("v3-window3");
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
	        	$("#cluetip").css('z-index', 2000000).css("box-shadow", "rgba(0, 0, 0, 1) 3px 3px 14px").addClass("v3-window3");
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
	        	$("#cluetip").css('z-index', 2000000).css("box-shadow", "rgba(0, 0, 0, 1) 3px 3px 14px").addClass("v3-window3");
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


	<title>Initium</title>





<script type="text/javascript" src="/odp/javascript/banner-weather.js?v=11"></script>
<script type="text/javascript" src="/odp/javascript/soundeffects.js?v=13"></script>


<script type="text/javascript">
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

<script type="text/javascript" src="/odp/javascript/messager-impl.js?v=35"></script>


<script type="text/javascript">

</script>

<script type="text/javascript">
	/*Antibot stuff*/
	
</script>

<script type="text/javascript">
	/*Other javascript variables*/
	window.isPremium = ${isPremium};
	
	
	
	$(document).ready(function(){
		// Request permission to use desktop notifications
		notifyHandler.requestPermission();
		
		// Start the tutorial if tutorial has been specified
		if (location.search!=null && location.search.indexOf("tutorial=true")>-1)
			uiTutorial.run();
		
		if (location.search!=null && location.search.indexOf("welcome=true")>-1)
			createDonationWindow();
		
		
	});

	// Html5 storage remember our local client
	updateMinimizeBox("#chat_box_minimize_button", ".chat_box");
	updateMinimizeChat();

	
	$(function() {
	    Pace.on("done", function(){
	        $("#contents").fadeIn(1000);
	    });
	});
</script>


<script type="text/javascript" src="/odp/javascript/reveal-tutorial.js?v=15"></script>
<script type="text/javascript" src="/odp/javascript/reveal-tutorial-impl.js?v=6"></script>

<script type="text/javascript" src="/odp/full.js?v=${version}"></script>

	<link rel="stylesheet" href="/odp/MiniUP.css?t=${version}"/>
	<link rel="stylesheet" href="/odp/full.css?t=${version}"/>

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
<body class='make-popup-root'>



	<div id="popups" style="display: none;"></div>
	<div class='popupBlurrable'>
		<div  class="banner1">
			<!-- <div class='banner1-rain-effect1'></div>-->
		</div>

		<div id='partyPanel'>
		${partyPanel}
		</div>

		<div id='inBannerCharacterWidget' class='characterWidgetContainer'>
			${inBannerCharacterWidget}
		</div>

		<div id='banner-text-overlay'>${bannerTextOverlay}</div>
	
		<div class='minimap-container'>
			<!-- <div class="minimap-locationname"><span class='v3-window1'>Aera</span></div> // I don't think we want this anymore since we now have the top bar-->
			<div class='map-contents minimap-contents'>
				${globalNavigationMap}
			</div>
			<div class='minimap-overlay'></div>
			<div class='minimap-button minimap-button-map' onclick='viewGlobeNavigation()'></div>
			<div class='minimap-button minimap-button-cancel standard-button-highlight' onclick='cancelLongOperations(event)' style='display:none'></div>
		</div>
		
		
		<div class='main-buttonbar-container'>
		    <div class='main-buttonbar main-buttonbar-mobile' >
				<div class="type1-button" id='button1'>
				</div>
				<div class="type1-button" id='button2'>
				</div>
				<div class="type1-button" id='button3'>
				</div>
				<div class="type1-button" id='button4'>
				</div>
				<div class="type1-button" id='button5'>
				</div>
				<div class="type1-button" id='button6'>
				</div>
				<div id='immovablesPanel'>${immovablesPanel}</div>
				
				<!-- Micro Menu -->
				<div class='type2-button' id='mm-button1' onclick='inventory();'><img src='https://initium-resources.appspot.com/images/ui4/micromenu-button-character1.png'/></div>
				<div class='type2-button' id='mm-button2' onclick='viewQuests();'><img src='https://initium-resources.appspot.com/images/ui4/micromenu-button-quests1.png'/></div>
				<div class='type2-button' id='mm-button3' onclick='viewManageStore();'><img src='https://initium-resources.appspot.com/images/ui4/micromenu-button-mystore1.png'/></div>
				
		    </div>
		</div>
	
		
		<div id="chatbox-container">
		    <div class="chatbox-tab minimize-chat-button" onclick='toggleMinimizeChat()'>&lt;</div>
		    <div id='GameMessages_tab_fullui' class="chatbox-tab" onclick='changeChatTab("GameMessages")'>!</div>
		    <div id='GlobalChat_tab_fullui' class="chatbox-tab highlighted"onclick='changeChatTab("GlobalChat")'>Global</div>
		    <div id='LocationChat_tab_fullui' class="chatbox-tab"onclick='changeChatTab("LocationChat")'>Location</div>
		    <div id='GroupChat_tab_fullui' class="chatbox-tab"onclick='changeChatTab("GroupChat")'>Group</div>
		    <div id='PartyChat_tab_fullui' class="chatbox-tab"onclick='changeChatTab("PartyChat")'>Party</div>
		    <div id='PrivateChat_tab_fullui' class="chatbox-tab"onclick='changeChatTab("PrivateChat")'>Private</div>
			<div id="chatbox">
				<div class='chat_messages' id="chat_messages_GameMessages"></div>
				<div class='chat_messages' id="chat_messages_GlobalChat"></div>
				<div class='chat_messages' id="chat_messages_LocationChat"></div>
				<div class='chat_messages' id="chat_messages_GroupChat"></div>
				<div class='chat_messages' id="chat_messages_PartyChat"></div>
				<div class='chat_messages' id="chat_messages_PrivateChat"></div>
			</div>
			<div class='chatbox-input-bar'>
				<form id="chat_form">
					<input class='chatbox-input-bar-input' id="chat_input" type="text" autocomplete="off" maxlength='2000'/>
					<input class='chatbox-input-bar-submit' id="chat_submit" type="submit" value='Submit'/>
				</form>
			</div>
		</div>    
	
		<div class='main-page popupBlurrable'>
			<div class='header v3-header1'>
				<div class='header-inner'>
					<div id='pullout-button' class='header-inner-section'><a onclick='toggleMainPullout()'><img src='https://initium-resources.appspot.com/images/ui3/header-button-options1.png' alt='Pullout menu' height='34px'/></a></div>
					<div class='header-inner-section' style='width:100%;vertical-align: middle; text-align: center;'><div style='margin-left:-32px; margin-right:-22px;'><a id='locationName' onclick='location.reload()'>${locationName }</a></div></div>
					<div id='mainMoneyIndicator' class='header-inner-section' onclick='viewProfile()'>
						${mainGoldIndicator}
					</div>
					<!-- <div id='map-button' class='header-inner-section'><a onclick='viewMap()'><img alt='View player-made world map' src='https://initium-resources.appspot.com/images/ui3/header-button-map1.png' height='34px'/></a></div> -->
					<div id='settings-button' class='header-inner-section'><a onclick='viewSettings()'><img alt='Game settings' src='https://initium-resources.appspot.com/images/ui3/header-button-settings1.png' height='34px'/></a></div>
					<div id='sound-button' class='header-inner-section'><a onclick='toggleEnvironmentSoundEffects()'><img alt='Sound effects' src='https://initium-resources.appspot.com/images/ui3/header-button-sound-on1.png' height='34px'/></a></div>
				</div>
			</div>
		</div>
			
		<div id='page-popup-root'></div> 
	
		<div class='map-contents global-navigation-map'>${globalNavigationMap}</div>	
		<div id='loot-popup'></div>
		<div id='main-button-list'>${mainButtonList}</div>
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


</body>
</html>