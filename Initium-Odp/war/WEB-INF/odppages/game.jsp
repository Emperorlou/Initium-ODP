
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


<%-- <script type='text/javascript'>
	// THIS SECTION IS NEEDED FOR THE BANNER-WEATHER.JS FILE ACTUALLY
	var bannerUrl = "<%=common.getLocationBanner()%>";
	
	if (bannerUrl.indexOf("http")!=0)
		bannerUrl = "https://initium-resources.appspot.com/"+bannerUrl;
	
	if (isAnimatedBannersEnabled()==false && bannerUrl.indexOf(".gif")>0)
		bannerUrl = "https://initium-resources.appspot.com/images/banner---placeholder2.gif";
	else if (isBannersEnabled()==false)
		bannerUrl = "https://initium-resources.appspot.com/images/banner---placeholder2.gif";
	else if (bannerUrl=="" || bannerUrl == "null")
		bannerUrl = "https://initium-resources.appspot.com/images/banner---placeholder2.gif";
		
	var serverTime = <%=currentTimeMs%>;
	var isOutside = "<%=common.getLocation().getProperty("isOutside")%>";
	
	<c:if test="${isTrading}">
	$(document).ready(function(){
		_viewTrade();
	});
	</c:if>
	
</script> --%>


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
	<div class='page'>
		<div class='page-upperhalf'>
			<div class='header1'>
				<div class='header1-spacer'></div>
				<div class='header1-display'>Aera Countryside</div>
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
							<span>
								<a onclick='viewManageStore()' title='Opens your storefront management page so you can setup items for sale'><img src='https://initium-resources.appspot.com/images/ui/manageStore.png' border=0/></a>
							</span>
							<c:if test="${storeEnabled==true}">
								<a onclick='storeDisabled()' title='Clicking here will disable your storefront so other players cannot buy your goods'><img src='https://initium-resources.appspot.com/images/ui/storefrontEnabled.png' border=0/></a>
							</c:if>
							<c:if test="${storeEnabled==false}">
								<a onclick='storeEnabled()' title='Clicking here will ENABLE your storefront so other players can buy your goods'><img src='https://initium-resources.appspot.com/images/ui/storefrontDisabled.png' border=0 style='margin-right: 5px;'/></a>
							</c:if>
							<c:if test="${isPartyLeader==true}">
								<c:if test="${partyJoinsAllowed==false }">
									<span  >
										<a onclick='partyEnableJoins()' title='Clicking here will allow other players to join your party'><img src='https://initium-resources.appspot.com/images/ui/partyJoinsDisallowed.png' border=0/></a>
									</span>
								</c:if>
								<c:if test="${partyJoinsAllowed==true }">
									<span  >
										<a onclick='partyDisableJoins()' title='Clicking here will disallow other players from joining your party'><img src='https://initium-resources.appspot.com/images/ui/partyJoinsAllowed.png' border=0/></a>
									</span>
								</c:if>
							</c:if>
							<c:if test="${isPartyLeader==false}">
								<c:if test="${partyJoinsAllowed==false }">
									<span >
										<img src='https://initium-resources.appspot.com/images/ui/partyJoinsDisallowed.png' border=0 title='Party joins disabled; other players cannot join your party. You are not the party leader, so you cannot change this.'/>
									</span>
								</c:if>
								<c:if test="${partyJoinsAllowed==true }">
									<span  >
										<img src='https://initium-resources.appspot.com/images/ui/partyJoinsAllowed.png' border=0 title='Party joins are currently enabled; other players can join your party. You are not the party leader, so you cannot change this.'/>
									</span>
								</c:if>
							</c:if>
							<c:if test="${duelRequestsAllowed==false }">
								<span  >
									<a onclick='allowDuelRequests()'  title='CLicking here will enable duel requests. This would allow other players to request a duel with you.'><img src='https://initium-resources.appspot.com/images/ui/duelRequestsDisallowed.png' border=0/></a>
								</span>
							</c:if>
							<c:if test="${duelRequestsAllowed==true }">
								<span  >
									<a href='disallowDuelRequests()' title='Clicking here will DISABLE duel requests. Other players would not be allowed to request a duel with you.'><img src='https://initium-resources.appspot.com/images/ui/duelRequestsAllowed.png' border=0/></a>
								</span>
							</c:if>
							
							<div class='hiddenTooltip' id='buttonbar'>
								<h5>The Button Bar</h5>
								<img src='https://initium-resources.appspot.com/images/ui/manageStore.png' border=0 style='float:left; padding:4px;'/>
								<p>This button will take you to your storefront management page. This page allows you to setup your storefront
								by specifying which items you would like to sell to other players and for how much. More help can be found in
								the storefront page itself.</p>
								<img src='https://initium-resources.appspot.com/images/ui/storefrontEnabled.png' border=0 style='float:left;padding:4px;'/>
								<p>This button will turn on and off your storefront. Since you cannot move while vending, you will need to
								turn off your store before you go off adventuring. This button makes turning your store on and off quick and easy.</p>
								<img src='https://initium-resources.appspot.com/images/ui/partyJoinsAllowed.png' border=0 style='float:left; padding:4px;'/>
								<p>This is the party join button. When enabled (without the red cross), other characters will be able to join you
								in a party. If you are not already in a party then when someone joins you, you will automatically become the party
								leader. <br>
								More information on parties and how they work can be found in the <a href='odp/mechanics.jsp#parties'>game mechanics page</a>.</p>
								<img src='https://initium-resources.appspot.com/images/ui/duelRequestsAllowed.png' border=0 style='float:left; padding:4px;'/>
								<p>This button allows you to control whether or not you are accepting duel requests. When enabled, other players are able to
								request to duel with you. You will be given the option to accept a duel request or deny it. When you accept, you will be whisked
								away into a special arena where you and the other player will engage in battle.<br> 
								More information on the different types of duels and how they work can be found in the <a href='odp/mechanics.jsp#duels'>game mechanics page</a>.</p>
							</div>
							<span class='hint' rel='#buttonbar' style='float:right'><img src='https://initium-resources.appspot.com/images/ui/help.png' border=0/></span>
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
