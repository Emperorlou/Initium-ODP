<html class=" multiplebgs backgroundblendmode backgroundcliptext"><head>
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


<script type="text/javascript" src="/odp/javascript/script.js?v=231"></script>
<!--<link type="text/css" rel="stylesheet" href="/odp/MiniUP.css?v=211">-->



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
	window.isPremium = true;
	
	
	
	$(document).ready(function(){
		// Request permission to use desktop notifications
		notifyHandler.requestPermission();
		
		// Start the tutorial if tutorial has been specified
		if (location.search!=null && location.search.indexOf("tutorial=true")>-1)
			uiTutorial.run();
		
		if (location.search!=null && location.search.indexOf("welcome=true")>-1)
			createDonationWindow();
		
		
	});

	
	$(function() {
	    Pace.on("done", function(){
	        $("#contents").fadeIn(1000);
	    });
	});
</script>


<script type="text/javascript" src="/odp/javascript/reveal-tutorial.js?v=15"></script>
<script type="text/javascript" src="/odp/javascript/reveal-tutorial-impl.js?v=6"></script>

<!-- Dynamically add style tags that modify -->
<style id="msw"></style>
<script type="text/javascript">
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

</script>
	<link rel="stylesheet" href="/odp/MiniUP.css?t=1"/>
	<link rel="stylesheet" href="/odp/full.css"/>

</head>

<!--
		HEY!!
		
Did you know you can help code Initium?
Check out our github and get yourself setup,
then talk to the lead dev so you can get yourself
on our slack channel!

http://github.com/Emperorlou/Initium-ODP 

                                           -->
<body class="banner1" style="
    background-image: url(https://initium-resources.appspot.com/images/fullscreen-banners/aera1.jpg);
    background-size: cover;
    background-position: center center;
">

	<div class='minimap-container'>
		<div class='minimap-contents'>
			${globalNavigationMap}
		</div>
		<div class='minimap-overlay'>
		</div>
	</div>
	
	<div id="chatbox-container">
	    <div class="chatbox-tab">!</div>
	    <div class="chatbox-tab highlighted">Global</div>
	    <div class="chatbox-tab">Location</div>
	    <div class="chatbox-tab">Group</div>
	    <div class="chatbox-tab">Party</div>
	    <div class="chatbox-tab">Private</div>
		<div id="chatbox">
			<div class='chat_messages' id="chat_messages_GameMessages"></div>
			<div class='chat_messages' id="chat_messages_GlobalChat"></div>
			<div class='chat_messages' id="chat_messages_LocationChat"></div>
			<div class='chat_messages' id="chat_messages_GroupChat"></div>
			<div class='chat_messages' id="chat_messages_PartyChat"></div>
			<div class='chat_messages' id="chat_messages_PrivateChat"></div>
		</div>
	</div>    



	
		

	<div id='global-navigation-map'>${globalNavigationMap}</div>	

	<script id='ajaxJs' type='text/javascript'>
	${bannerJs}
	</script>

</body>
</html>