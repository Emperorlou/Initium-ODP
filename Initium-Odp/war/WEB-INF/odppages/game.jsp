
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>


<!DOCTYPE html>
<html>
<head>
	<jsp:include page="common-head2.jsp"/><jsp:include page="odp/common-head.jsp"/>
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

<script type='text/javascript' src='odp/javascript/banner-weather.js?v=5'></script>
<script id='ajaxJs' type='text/javascript'>
${bannerJs}
</script>



<!-- <script type='text/javascript'>
	$(document).ready(function (){
		<c:if test="${combatSite==true}">
		loadInlineItemsAndCharacters();
		</c:if>
		
		<c:if test="${isCollectionSite==true}">
		loadInlineCollectables();
		</c:if>
		
		
	});
</script> -->

<!-- <script type='text/javascript' src='odp/javascript/banner-weather.js?v=4'></script>
 -->
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

<script type='text/javascript' src='odp/javascript/messager-impl.js'></script>

<script type='text/javascript' src='odp/javascript/soundeffects.js?v=1'></script>
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
<script type="text/javascript" src="./javascript/script.js"></script>
<link rel="stylesheet" href="./MiniUP.css">

</head>

<!--
		HEY!!
		
Did you know you can help code Initium?
Check out our github and get yourself setup,
then talk to the lead dev so you can get yourself
on our slack channel!

                                           -->

<body>
	<div class='page'>
		<div class='page-upperhalf'>
			<div class='header1'>
				<div class='header1-spacer'></div>
				<div class='header1-display'><span id='locationName'>${locationName}</span></div>
				<div class='header1-spacer'></div>
				<div class='header1-rightchunk'>
					<div class='header1-button'>MAP</div>
					<div class='header1-spacer'></div>
					<div class='header1-button'>EQUIP</div>
					<div class='header1-spacer'></div>
					<div class='header1-display'><img src='https://initium-resources.appspot.com/images/dogecoin-18px.png' border=0/><span id='mainGoldIndicator'>${mainGoldIndicator}</span></div>
					<div class='header1-spacer'></div>
				</div>
			</div>
			<div class='banner1'>
				<img id='banner-sizer' src='https://initium-resources.appspot.com/images/banner---placeholder2.png' border=0/>
			</div>
		</div>
		<div class='page-maincontent'>
			<div class='chat-container'>
				<div class='header1'>
					<div class='header1-spacer'></div>
					<div class='header1-button chat-button'><div class='chat-button-indicator'></div>!</div>
					<div class='header1-spacer'></div>
					<div class='header1-button chat-button'><div class='chat-button-indicator'></div>GLOBAL</div>
					<div class='header1-spacer'></div>
					<div class='header1-button chat-button'><div class='chat-button-indicator'></div>LOCATION</div>
					<div class='header1-spacer'></div>
					<div class='header1-button chat-button'><div class='chat-button-indicator'></div>GROUP</div>
					<div class='header1-spacer'></div>
					<div class='header1-button chat-button'><div class='chat-button-indicator'></div>PARTY</div>
					<div class='header1-spacer'></div>
					<div class='header1-button chat-button'><div class='chat-button-indicator' style='visibility:visible'>12</div>PRIVATE</div>
					<div class='header1-spacer'></div>
				</div>
				<div class='main1'>
					<div class='backdrop1d chat-text-container'>
						<div id="chat_tab">
				<div id="chat_form_wrapper">
					<form id="chat_form">
						<span class='fullscreenChatButton' onclick='toggleFullscreenChat()'>[&nbsp;]</span><input type='hidden' id='chatroomId' value='L${locationId}' />
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

					</div>
				</div>
			</div>
			<div class='location-controls-container'>
				<div class='header1'></div>
				<div class='main1'>
					<div class='location-controls'>
					<div class='main1-inset1'>
						<div class='backdrop1b buttonbar'>
						<div id='buttonBar'>${buttonBar}</div>
						</div>
					</div>
					
					
					<div class='main1-inset1 location-controls-navigation'>
						<div class='titlebar'>NAVIGATION</div>
						<div class='backdrop2a navigationbox'>
							<div class='titlebar'>Paths</div>
							<div class='button2'>Aera</div>
							<div class='button2'>Swamplands</div>
							<div class='button2'>Troll Camp</div>
						</div>
						<div class='backdrop2a navigationbox'>
							<div class='titlebar'>Properties</div>
							<div class='button2'>Armory</div>
							<div class='button2'>NIK'S BADASS STUFF</div>
							<div class='button2'>Group House Alpha</div>
						</div>
						<div class='backdrop2a navigationbox'>
							<div class='titlebar'>Combat Sites</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Shell Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Shell Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Shell Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Shell Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Troll</div>
							<div class='button2'>Shell Troll</div>
							<div class='button2'>Troll</div>
						</div>
					</div>					
					</div>
				</div>
			</div>
		</div>
	 </div>
</body>
</html>