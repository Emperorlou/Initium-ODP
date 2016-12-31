<%@page import="com.universeprojects.miniup.server.HtmlComponents"%>
<%@page import="java.util.Date"%>
<%@page import="com.universeprojects.cacheddatastore.CachedEntity"%>
<%@page import="com.universeprojects.miniup.server.longoperations.LongOperation"%>
<%@page import="com.universeprojects.miniup.server.TradeObject"%>
<%@page import="com.universeprojects.cacheddatastore.CachedDatastoreService"%>
<%@page import="com.google.appengine.api.datastore.DatastoreService"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.universeprojects.miniup.server.GameUtils"%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@page import="com.universeprojects.miniup.server.GameFunctions"%>
<%@page import="com.google.appengine.api.datastore.Key"%>
<%@page import="java.util.List"%>
<%@page import="com.universeprojects.miniup.server.PrefixCodes"%>
<%@page import="com.universeprojects.miniup.server.WebUtils"%>
<%@page import="com.universeprojects.miniup.server.SecurityException"%>
<%@page import="com.universeprojects.miniup.server.CommonEntities"%>
<%@page import="com.universeprojects.miniup.server.ErrorMessage"%>
<%@page import="com.universeprojects.miniup.server.JspSnippets"%>
<%@page import="com.google.appengine.api.datastore.Entity"%>
<%@page import="com.universeprojects.miniup.server.ODPDBAccess"%>
<%@page import="com.universeprojects.miniup.server.Authenticator"%>
<%
	response.setHeader("Access-Control-Allow-Origin", "*");		// This is absolutely necessary for phonegap to work

	if (request.getServerName().equals("www.playinitium.appspot.com"))
	{ 
		response.setStatus(301);
		response.setHeader("Location", "http://www.playinitium.com");
		return;
	}


	Authenticator auth = Authenticator.getInstance(request);
	GameFunctions db = auth.getDB(request);
	CachedDatastoreService ds = db.getDB();
	try
	{
		auth.doSecurityChecks(request);
	}
	catch(SecurityException e)
	{
		JspSnippets.handleSecurityException(e, request, response);
		return;
	}

	request.setAttribute("isThrowawayInSession", auth.isThrowawayCharacterInSession());
	if (auth.isThrowawayCharacterInSession())
	{
		request.setAttribute("throwawayName", auth.getThroawayCharacter(ds).getProperty("name"));
	}
	
	CommonEntities common = CommonEntities.getInstance(request);

	
	// Check if the user has a verified account. If not, send them back to the quickstart page with a message...
	/*
	if (common.getUser()!=null && common.getUser().getProperty("verified")==null)
	{
		String pleaseVerify = "Please verify your email address account before playing!<br>" + 
								"You should have received a verification email already. If you haven't, you can <a onclick='resendVerificationEmail()'>click here to resend the verification email</a>.<br>" + 
								"If you wish to change your email address, you can <a onclick='changeEmailAddress(&quot;"+common.getUser().getProperty("email")+"&quot;)'>do that here</a>.";
		WebUtils.forceRedirectClientTo("quickstart.jsp", request, response, pleaseVerify);
		return;
	}
	*/
	
	
	
	
	if (GameUtils.isPlayerIncapacitated(common.getCharacter()))
	{
		WebUtils.forceRedirectClientTo("killed.jsp", request, response);
		return;
	}
	
	String characterMode = (String)common.getCharacter().getProperty("mode");
	if (characterMode!=null && characterMode.equals(GameFunctions.CHARACTER_MODE_COMBAT))
	{
		// This has to be "ask for" because forced redirections will include the isRedirection and thus "Looter Protection" will not work
		WebUtils.forceRedirectClientTo("combat.jsp", request, response);
		return;
	}
	if (characterMode!=null && characterMode.equals(GameFunctions.CHARACTER_MODE_TRADING))
	{
//		WebUtils.forceRedirectClientTo("trade.jsp", request, response);
//		return;

		request.setAttribute("isTrading", true);
	}
	
	// We need some user entity attributes
	Boolean isPremium = false;
	if (common.getUser()!=null)
		isPremium = (Boolean)common.getUser().getProperty("premium");
	if (isPremium==null) isPremium = false;
	request.setAttribute("isPremium", isPremium);
	
	// Get the referral url
	String referralUrl = GameUtils.determineReferralUrl(common.getUser());
	request.setAttribute("referralUrl", referralUrl);
	
	// Determine if a long operation is in progress and get the recall javascript if it is
	String longOperationRecallJs = LongOperation.getLongOperationRecall(db, common.getCharacter().getKey());
	request.setAttribute("longOperationRecallJs", longOperationRecallJs);
	
	// Get all the characters that this user has (if he is a premium member)
	List<CachedEntity> characterList = null;
	if (isPremium && common.getUser()!=null)
	{
		characterList = db.getFilteredList("Character", "userKey", common.getUser().getKey());
		request.setAttribute("characterList", characterList);
	}
	
	request.setAttribute("characterName", common.getCharacter().getProperty("name"));
	request.setAttribute("characterId", common.getCharacter().getKey().getId());
	if (common.getUser()!=null)
		request.setAttribute("characterToTransfer", common.getUser().getProperty("transferCharacterName"));
	
	// Get the prefix code so we can add some special text to the description in special cases
	String prefix = "";
	Integer prefixCode = WebUtils.getIntParam(request, "pre");
	if (prefixCode!=null)
	{
		prefix = PrefixCodes.getTextForPrefixCode(prefixCode);
		if (prefix!=null && "null".equals(prefix)==false)
	request.setAttribute("prefix", prefix);
	}
	
	String biome = (String)common.getLocation().getProperty("biomeType");
	if (biome==null) biome = "Temperate";
	request.setAttribute("biome", biome);
	String locationAudioDescriptor = (String)common.getLocation().getProperty("audioDescriptor");
	if (locationAudioDescriptor==null) locationAudioDescriptor = "";
	request.setAttribute("locationAudioDescriptor", locationAudioDescriptor);

	
	
	String locationAudioDescriptorPreset = (String)common.getLocation().getProperty("audioDescriptorPreset");
	if (locationAudioDescriptorPreset==null) locationAudioDescriptorPreset = "";
	request.setAttribute("locationAudioDescriptorPreset", locationAudioDescriptorPreset);
	
	// Get all the things we've discovered here...
	List<CachedEntity> discoveries = db.getDiscoveriesForCharacterAndLocation(common.getCharacter().getKey(), common.getLocation().getKey());
	List<CachedEntity> paths = new ArrayList<CachedEntity>();
	List<CachedEntity> destLocations = new ArrayList<CachedEntity>();
	List<String> pathEnds = new ArrayList<String>();
	
	//Automatically include any paths that have a discovery of 100
	List<CachedEntity> alwaysVisiblePaths = db.getFilteredList("Path", "location1Key", common.getLocation().getKey(), "discoveryChance", 100d);
	alwaysVisiblePaths.addAll(db.getFilteredList("Path", "location2Key", common.getLocation().getKey(), "discoveryChance", 100d));
	for(CachedEntity path:alwaysVisiblePaths)
	{
		if (paths.contains(path))
			continue;
		
		Key destination = null;
		// First get the character's current location
		Key currentLocationKey = (Key)common.getCharacter().getProperty("locationKey");
		
		// Then determine which location the character will end up on.
		// If we find that the character isn't on either end of the path, we'll throw.
		String pathEnd = null;
		Key pathLocation1Key = (Key)path.getProperty("location1Key");
		Key pathLocation2Key = (Key)path.getProperty("location2Key");
		if (pathLocation1Key==null)
			continue;
		if (pathLocation2Key==null)
			continue;
		if (currentLocationKey.getId()==pathLocation1Key.getId())
		{
			destination = pathLocation2Key;
			pathEnd = "2";
		}
		else if (currentLocationKey.getId()==pathLocation2Key.getId())
		{
			destination = pathLocation1Key;
			pathEnd = "1";
		}
		
		if ("1".equals(pathEnd) && "FromLocation1Only".equals(path.getProperty("forceOneWay")))
			continue;	
		else if ("2".equals(pathEnd) && "FromLocation2Only".equals(path.getProperty("forceOneWay")))
			continue;	
		
		if (destination!=null)
		{

			CachedEntity destLocation = db.getEntity(destination);
	
			if (destLocation==null)
			{
				continue;
			}
	
	
			paths.add(path);
			destLocations.add(destLocation);
			pathEnds.add(pathEnd);
		}
		
	}
	
	
	// Get all discovered paths...
	boolean hasHiddenPaths = false;
	for(CachedEntity discovery:discoveries)
	{
		if (discovery.getProperty("kind").equals("Path"))
		{
			CachedEntity path = db.getPathById(((Key)discovery.getProperty("entityKey")).getId());
			if (path==null)
			{
				// If we get here, it's because the path was deleted. Lets remove the discovery for future and skip this one for now
				ds.delete(discovery.getKey());
				continue;
			}
			
			if (paths.contains(path))
				continue;
		
			Key destination = null;
			// First get the character's current location
			Key currentLocationKey = (Key)common.getCharacter().getProperty("locationKey");
			
			// Then determine which location the character will end up on.
			// If we find that the character isn't on either end of the path, we'll throw.
			String pathEnd = null;
			Key pathLocation1Key = (Key)path.getProperty("location1Key");
			Key pathLocation2Key = (Key)path.getProperty("location2Key");
			if (pathLocation1Key==null)
				continue;
			if (pathLocation2Key==null)
				continue;
			if (currentLocationKey.getId()==pathLocation1Key.getId())
			{
				destination = pathLocation2Key;
				pathEnd = "2";
			}
			else if (currentLocationKey.getId()==pathLocation2Key.getId())
			{
				destination = pathLocation1Key;
				pathEnd = "1";
			}
			
			if (destination!=null)
			{
				String forceOneWay = (String)path.getProperty("forceOneWay");
				if ("FromLocation1Only".equals(forceOneWay) && currentLocationKey.getId() == pathLocation2Key.getId())
					continue;
				if ("FromLocation2Only".equals(forceOneWay) && currentLocationKey.getId() == pathLocation1Key.getId())
					continue;
		
				if ("TRUE".equals(discovery.getProperty("hidden")) && request.getParameter("showHiddenPaths")==null)
				{
					// Skip this path, it's hidden
					hasHiddenPaths = true;
					continue;
				}
				
				CachedEntity destLocation = db.getEntity(destination);
				
				if (destLocation==null)
				{
					ds.delete(discovery.getKey());
					continue;
				}
				
				
				paths.add(path);
				destLocations.add(destLocation);
				pathEnds.add(pathEnd);
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	boolean combatSite = false;
	if (((String)common.getLocation().getProperty("name")).startsWith("Combat site: "))
		combatSite = true;
	request.setAttribute("combatSite", combatSite);

	
	// WORKAROUND: Sometimes paths are not created for some reason. This will manually create a path using the parent location if none existed
	if (paths.isEmpty() && combatSite)
	{
		CachedEntity path = db.workaroundNoPaths(common.getCharacter(), common.getLocation());
		paths.add(path);
		destLocations.add(common.getLocation());
		pathEnds.add("2");
	}
	
	Double monsterCount = db.getMonsterCountForLocation(ds, common.getLocation());
	Double maxMonsterCount = (Double)common.getLocation().getProperty("maxMonsterCount");
	request.setAttribute("maxMonsterCount", maxMonsterCount);
	if (monsterCount!=null && maxMonsterCount!=null)
		request.setAttribute("monsterCountRatio", monsterCount/maxMonsterCount);
	else
		request.setAttribute("monsterCountRatio", 0d);
	
	
	request.setAttribute("supportsCamps", common.getLocation().getProperty("supportsCamps"));
	boolean isOutside = false;
	if ("TRUE".equals(common.getLocation().getProperty("isOutside")))
		isOutside = true;
	
	request.setAttribute("isOutside", isOutside);
	
	// Party related stuff
	List<CachedEntity> party = null;
	if (common.getCharacter().getProperty("partyCode")!=null)
	{
		party = db.getParty(ds, common.getCharacter());
		
		if (party!=null)
		{
			request.setAttribute("isPartied", true);
			request.setAttribute("party", party);
			request.setAttribute("partyCount", party.size());
			request.setAttribute("isPartyLeader", "TRUE".equals(common.getCharacter().getProperty("partyLeader")));
		}
	}
	
	// Determine the party leader (or yourself if not in a party)
	CachedEntity partyLeader = null;
	if (party!=null)
	{
		for(CachedEntity e:party)
	if ("TRUE".equals(e.getProperty("partyLeader")))
		partyLeader = e;
	}
	else
		partyLeader = common.getCharacter();
	
	if (partyLeader!=null && partyLeader.equals("")==false)
	{
		if (partyLeader.getKey().getId() == common.getCharacter().getKey().getId())
	request.setAttribute("isPartyLeader", true);
		else
	request.setAttribute("isPartyLeader", false);
		
		if ("TRUE".equals(partyLeader.getProperty("partyJoinsAllowed")))
	request.setAttribute("partyJoinsAllowed", true);
		else
	request.setAttribute("partyJoinsAllowed", false);
	}
		
	if ("TRUE".equals(common.getCharacter().getProperty("duelRequestsAllowed")))
		request.setAttribute("duelRequestsAllowed", true);
	else
		request.setAttribute("duelRequestsAllowed", false);

	if ("MERCHANT".equals(common.getCharacter().getProperty("mode")))
		request.setAttribute("storeEnabled", true);
	else
		request.setAttribute("storeEnabled", false);
	
	
	/////////////////////////////////////////////////////
	// This part pertains to blockade options...

	List<CachedEntity> charactersHere = null;
	CachedEntity leader = null;	
	CachedEntity defenceStructure = db.getEntity((Key)common.getLocation().getProperty("defenceStructure"));
	if (defenceStructure!=null) 
	{
		// This is a defence structure location and has some additional UI elements as such
		request.setAttribute("isDefenceStructure", true);

		String defenceMode = (String)defenceStructure.getProperty("blockadeRule");
		if (defenceMode==null) defenceMode="BlockAllParent";
		request.setAttribute("defenceMode", defenceMode);

		// The class that should be used for each defence mode option
		if (defenceMode.equals("BlockAllParent"))
	request.setAttribute("defenceModeBlockAllParent", "selected-item");
		if (defenceMode.equals("BlockAllSelf"))
	request.setAttribute("defenceModeBlockAllSelf", "selected-item");
		if (defenceMode.equals("None"))
	request.setAttribute("defenceModeNone", "selected-item");
		
		// Find out who the leader is and generate the html used to render his name on the page
		leader = db.getEntity((Key)defenceStructure.getProperty("leaderKey"));
		request.setAttribute("leader", leader);
		if (leader!=null && common.getCharacter().getKey().getId() == leader.getKey().getId())
			request.setAttribute("isLeader", true);
	
		
		
		String status = (String)common.getCharacter().getProperty("status");
		if (status==null || status.equals("") || status.equals("Normal"))		// Normalize the status for easy testing
	status = null;
		request.setAttribute("characterStatus", status);
		
		if ("Defending1".equals(status))
		{
	request.setAttribute("statusDescription", "You are part of the first line of defence if someone were to attack this location.");
	request.setAttribute("nextStatusDescription", "Click here to change to the second line of defence");
		}
		else if ("Defending2".equals(status))
		{
	request.setAttribute("statusDescription", "You are part of the second line of defence if someone were to attack this location.");
	request.setAttribute("nextStatusDescription", "Click here to change to the third line of defence");
		}
		else if ("Defending3".equals(status))
		{
	request.setAttribute("statusDescription", "You are part of the third line of defence if someone were to attack this location.");
	request.setAttribute("nextStatusDescription", "Click here to stop defending");
		}
		else
		{
	request.setAttribute("statusDescription", "You are not currently defending this location.");
	request.setAttribute("nextStatusDescription", "Click here to defend this structure as the first line of defence");
		}
	
		
		request.setAttribute("defenceStructureHitpoints", GameUtils.formatNumber(defenceStructure.getProperty("hitpoints")));
		request.setAttribute("defenceStructureMaxHitpoints", GameUtils.formatNumber(defenceStructure.getProperty("maxHitpoints")));
		
		
		// Now lets get some info on everyone who is here
		charactersHere = db.getFilteredList("Character", "locationKey", common.getLocation().getKey());
		
		int defending1 = 0;
		int defending2 = 0;
		int defending3 = 0;
		int defendingEngaged1 = 0;
		int defendingEngaged2 = 0;
		int defendingEngaged3 = 0;
		
		int notDefending = 0;
		for(CachedEntity chr:charactersHere)
		{
	String chrStatus = (String)chr.getProperty("status");
	if ("Defending1".equals(chrStatus) && (Double)chr.getProperty("hitpoints")>0)
	{
		defending1++;
		
		if ("COMBAT".equals(chr.getProperty("mode")))
			defendingEngaged1++;
	}
	else if ("Defending2".equals(chrStatus) && (Double)chr.getProperty("hitpoints")>0)
	{
		defending2++;

		if ("COMBAT".equals(chr.getProperty("mode")))
			defendingEngaged2++;
	}
	else if ("Defending3".equals(chrStatus) && (Double)chr.getProperty("hitpoints")>0)
	{
		defending3++;

		if ("COMBAT".equals(chr.getProperty("mode")))
			defendingEngaged3++;
	}
	else if ((Double)chr.getProperty("hitpoints")>0)
		notDefending++;
		}
		
		request.setAttribute("defender1Count", defending1);
		request.setAttribute("defender2Count", defending2);
		request.setAttribute("defender3Count", defending3);
		request.setAttribute("defenderEngaged1Count", defendingEngaged1);
		request.setAttribute("defenderEngaged2Count", defendingEngaged2);
		request.setAttribute("defenderEngaged3Count", defendingEngaged3);
		request.setAttribute("notDefendingCount", notDefending);
	}
	else
	{
		request.setAttribute("isDefenceStructure", false);
	}
	
	
	//////////////////////////////
	// Collection site stuff
	
	if ("CollectionSite".equals(common.getLocation().getProperty("type")))
	{
		request.setAttribute("isCollectionSite", true);
	}
	
	
	// Instance respawn javascript
	if ("TRUE".equals(common.getLocation().getProperty("instanceModeEnabled")) && common.getLocation().getProperty("instanceRespawnDate")!=null)
	{
		Date respawnDate = (Date)common.getLocation().getProperty("instanceRespawnDate");
		request.setAttribute("instanceRespawnMs", respawnDate.getTime());
	}
	else
		request.setAttribute("instanceRespawnMs", "null");
	
	long currentTimeMs = System.currentTimeMillis();
	String clientDescription = db.getClientDescriptionAndClear(null, common.getCharacter().getKey());	// This should be near the end of the jsp's java head to reduce the chance of being redirected away from the page before the message gets displayed
	if (clientDescription==null || "null".equals(clientDescription)) clientDescription = "";
	request.setAttribute("clientDescription", clientDescription);
	if (request.getAttribute("midMessage")==null || "null".equals(request.getAttribute("midMessage")))
		request.setAttribute("midMessage", "");
	
	
	request.setAttribute("activePlayers", db.getActivePlayers());
	
	
	// Orders...
	if (common.getUser()!=null)
		request.setAttribute("usedCustomOrders", common.getUser().getProperty("usedCustomOrders"));
	
	
	// Territory...
	if (common.getLocation().getProperty("territoryKey")!=null)
	{
		CachedEntity territory = db.getEntity((Key)common.getLocation().getProperty("territoryKey"));
		CachedEntity territoryOwnerGroup = db.getEntity((Key)territory.getProperty("owningGroupKey"));
		request.setAttribute("territoryViewHtml", HtmlComponents.generateTerritoryView(common.getCharacter(), territoryOwnerGroup, territory));
	}
%>



<!DOCTYPE html>
<html>
<head>
	<jsp:include page="common-head2.jsp"/><jsp:include page="odp/common-head.jsp"/>
	<title>Main - Initium</title>

<script type='text/javascript'>
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
	
</script>


<script type='text/javascript'>
	$(document).ready(function (){
		<c:if test="${combatSite==true}">
		loadInlineItemsAndCharacters();
		</c:if>
		
		<c:if test="${isCollectionSite==true}">
		loadInlineCollectables();
		</c:if>
		
		
	});
</script>

<script type='text/javascript' src='odp/javascript/banner-weather.js?v=4'></script>

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
	/*Other javascript variables*/
	window.isPremium = ${isPremium};
	window.biome = "${biome}";
	window.instanceRespawnMs = ${instanceRespawnMs};
</script>

<script type='text/javascript'>
	// This ensures the bottom half of the page fills the rest of the page and no more
	function normalizePage()
	{
		var adjust = 5;
		var viewportHeight = $(window).height();
		
		var upperhalf = $(".page-upperhalf");
		var contents = $(".page-maincontent");
		var contentsHeight = viewportHeight - upperhalf.height()+adjust;
		contents.height(contentsHeight);
		contents.css("top", upperhalf.height()-adjust);
	}
	$(document).ready(normalizePage);
	$(window).resize(normalizePage);
</script>

</head>
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
					<div class='header1-display'><img src='https://initium-resources.appspot.com/images/dogecoin-18px.png' border=0/>1,320,122</div>
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
									<span class='fullscreenChatButton' onclick='toggleFullscreenChat()'>[&nbsp;]</span><input type='hidden' id='chatroomId' value='L<%=common.getLocation().getKey().getId()%>' />
									<div class='chat_form_input'>
										<input id="chat_input" type="text" autocorrect="on" autocomplete="off" placeholder='Chat with anyone else in this location' maxlength='2000'/>
									</div>
									<div class='chat_form_submit'>
										<input id="chat_submit" type="submit" value='Submit'/>
									</div>
								</form>
							</div>
							<div class='chat_messages' id="chat_messages_GlobalChat"></div>
							<div class='chat_messages' id="chat_messages_LocationChat"></div>
							<div class='chat_messages' id="chat_messages_GroupChat"></div>
							<div class='chat_messages' id="chat_messages_PartyChat"></div>
							<div class='chat_messages' id="chat_messages_PrivateChat"></div>
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