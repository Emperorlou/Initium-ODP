<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@page import="com.google.appengine.api.datastore.Key"%>
<%@page import="com.google.appengine.api.datastore.Entity"%>
<%@page import="com.universeprojects.miniup.server.GameUtils"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	response.addHeader("Access-Control-Allow-Origin", "*");

	long serverTime = System.currentTimeMillis();
	request.setAttribute("serverTime", serverTime);
	
	request.setAttribute("isTestServer", GameUtils.isTestServer(request));
%>
    
<meta charset="UTF-8">    
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no"/> 
<!-- <meta name="viewport" content="minimum-scale=0.3, maximum-scale=1"/> -->
<meta name="keywords" content="initium, game, web game, video game, free to play, mmorpg, mmo">
<meta name="referrer" content="no-referrer" />

<script type="text/javascript" src="https://code.createjs.com/preloadjs-0.6.2.min.js"></script>
<script type="text/javascript" src="https://code.createjs.com/soundjs-0.6.2.min.js"></script>

<script type="text/javascript" src="javascript/modernizr.js"></script>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script type="text/javascript" src="/javascript/jquery.browser.min.js"></script>
<script type="text/javascript" src="/javascript/jquery.preload.min.js"></script>
<script type="text/javascript" src="/odp/javascript/sockjs.min.js"></script>
<script type="text/javascript" src="/odp/javascript/vertx-eventbus.js"></script>

<script type="text/javascript" src="/odp/javascript/seedrandom.js"></script>
<script type="text/javascript" src="/odp/javascript/script.js?v=97"></script>

<script type="text/javascript" src="/odp/javascript/messager.js?v=23"></script>

<script type="text/javascript" src="/odp/javascript/PopupNotifications.js?v=3"></script>
<script type="text/javascript" src="/odp/javascript/BrowserPopupNotifications-impl.js?v=3"></script>



<script type="text/javascript" src="/javascript/jquery.cluetip.all.min.js"></script>
<link type="text/css" rel="stylesheet" href="/javascript/jquery.cluetip.css"/>

<link type="text/css" rel="stylesheet" href="/odp/MiniUP.css?v=66">

<link type="text/css" rel="stylesheet" href="/javascript/rangeslider/rangeslider.css"/>
<script src="javascript/rangeslider/rangeslider.min.js"></script>

<script src='/odp/javascript/openseadragon/openseadragon.min.js'></script>
<script src='/odp/javascript/map.js?t=4'></script>

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
<c:if test='${chatIdToken!=null}'>
window.chatIdToken = "${chatIdToken}";
</c:if>
<c:if test='${characterId!=null}'>
window.characterId = ${characterId};
</c:if>
<c:if test='${newChatIdToken!=null}'>
window.newChatIdToken = "${newChatIdToken}";
</c:if>
	window.isTestServer = ${isTestServer};
	window.verifyCode = "${verifyCode}";
	window.serverTime=<c:out value="${serverTime}"/>;
	window.clientTime=new Date().getTime();
	


	<c:if test="${userMessage!=null}">
	$(window).ready(function()
	{
		popupMessage("System Message", "${userMessage}");
		<%
		request.setAttribute("userMessage", null);
		%>
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
			cluetipClass: 'rounded',
			showTitle: false, 
			height: 'auto', 
			width: 320,
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
			cluetipClass: 'rounded',
			showTitle: false, 
			height: 'auto', 
			width: 320,
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
			cluetipClass: 'rounded',
			showTitle: false, 
			height: 'auto', 
			width: 320,
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

