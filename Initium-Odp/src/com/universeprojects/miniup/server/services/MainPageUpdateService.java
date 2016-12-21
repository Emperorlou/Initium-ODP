package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.ScriptType;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.WebUtils;

public class MainPageUpdateService extends Service
{
	final private CachedEntity user;
	final private CachedEntity character;
	final private CachedEntity location;
	final private OperationBase operation;

	private CachedEntity group = null;
	
	// Path related caches
	boolean hasHiddenPaths = false;
	private List<CachedEntity> discoveries = null;  // All the discoveries we have for this character and location.
	private List<CachedEntity> paths = null;  // All the paths that we can currently see that are connected to the path we're location in.
	private List<CachedEntity> destLocations = null;  // The location entities at the other end of the paths; on the side we're not on currently.
	private List<Integer> pathEnds = null;  // 1 or 2. Since each path is 2 sided, this number indicates which side we are NOT on currently.
	
	/**
	 * 
	 * @param db
	 * @param operation Pass in the Command or LongOperation that wants to make use of this service.
	 */
	public MainPageUpdateService(ODPDBAccess db, CachedEntity user, CachedEntity character, CachedEntity location, OperationBase operation)
	{
		super(db);
		this.operation = operation;
		this.user = user;
		this.character = character;
		this.location = location;
	}

	private String updateHtmlContents(String selector, String newHtml)
	{
		if (operation!=null)
			operation.updateHtmlContents(selector, newHtml);
		
		return newHtml;
	}

	private String updateJavascript(String id, String js)
	{
		if (operation!=null)
			operation.updateJavascript(id, js);
		
		return js;
	}


	private boolean isGroupLoaded = false;
	private CachedEntity getGroup()
	{
		if (isGroupLoaded==false)
		{
			group = db.getEntity((Key)character.getProperty("groupKey"));
			isGroupLoaded = true;
		}
		
		return group;
	}
	
	public void setPathCache(List<CachedEntity> discoveries, List<CachedEntity> paths, List<CachedEntity> destLocations, List<Integer> pathEnds, boolean hasHiddenPaths)
	{
		this.discoveries = discoveries;
		this.paths = paths;
		this.destLocations = destLocations;
		this.pathEnds = pathEnds;
		this.hasHiddenPaths = hasHiddenPaths;
	}
	
	/**
	 * This will load all the path related caches, but only if we haven't loaded them before for
	 * this particular session.
	 * @return
	 */
	private void loadPathCache() {
		loadPathCache(false);
	}
	
	/**
	 * This will load all the path related caches, but only if we haven't loaded them before for
	 * this particular session.
	 * 
	 * @param showHidden  A boolean value that determines whether we should load the hidden paths.
	 * @return
	 */
	private void loadPathCache(boolean showHidden)
	{
		if (paths==null)
		{
			discoveries = db.getDiscoveriesForCharacterAndLocation(character.getKey(), location.getKey());
			
			paths = new ArrayList<CachedEntity>();
			destLocations = new ArrayList<CachedEntity>();
			pathEnds = new ArrayList<Integer>();
			
			List<Key> destLocationsToLoad = new ArrayList<Key>();
			
			
			//Automatically include any paths that have a discovery of 100
			List<CachedEntity> alwaysVisiblePaths = db.getFilteredList("Path", "location1Key", location.getKey(), "discoveryChance", 100d);
			alwaysVisiblePaths.addAll(db.getFilteredList("Path", "location2Key", location.getKey(), "discoveryChance", 100d));
			for(CachedEntity path:alwaysVisiblePaths)
			{
				if (paths.contains(path))
					continue;
				
				Key destination = null;
				// First get the character's current location
				Key currentLocationKey = location.getKey();
				
				// Then determine which location the character will end up on.
				// If we find that the character isn't on either end of the path, we'll throw.
				Integer pathEnd = null;
				Key pathLocation1Key = (Key)path.getProperty("location1Key");
				Key pathLocation2Key = (Key)path.getProperty("location2Key");
				if (pathLocation1Key==null)
					continue;
				if (pathLocation2Key==null)
					continue;
				if (currentLocationKey.getId()==pathLocation1Key.getId())
				{
					destination = pathLocation2Key;
					pathEnd = 2;
				}
				else if (currentLocationKey.getId()==pathLocation2Key.getId())
				{
					destination = pathLocation1Key;
					pathEnd = 1;
				}
				
				if (pathEnd == 1 && "FromLocation1Only".equals(path.getProperty("forceOneWay")))
					continue;	
				else if (pathEnd == 2 && "FromLocation2Only".equals(path.getProperty("forceOneWay")))
					continue;	
				
				if (destination!=null)
				{
					paths.add(path);
					destLocationsToLoad.add(destination);
					pathEnds.add(pathEnd);
				}
				
			}
			

			
			// Get all discovered paths...
			for(CachedEntity discovery:discoveries)
			{
				if (discovery==null) continue;	// Why would it be null I have no idea. Maybe just a race condition when deleting and this fetch took place.
				
				if ("Path".equals(discovery.getProperty("kind")))
				{
					CachedEntity path = db.getPathById(((Key)discovery.getProperty("entityKey")).getId());
					if (path==null)
					{
						// If we get here, it's because the path was deleted. Lets remove the discovery for future and skip this one for now
						db.getDB().delete(discovery.getKey());
						continue;
					}
					
					if (paths.contains(path))
						continue;
				
					Key destination = null;
					// First get the character's current location
					Key currentLocationKey = location.getKey();
					
					// Then determine which location the character will end up on.
					// If we find that the character isn't on either end of the path, we'll throw.
					Integer pathEnd = null;
					Key pathLocation1Key = (Key)path.getProperty("location1Key");
					Key pathLocation2Key = (Key)path.getProperty("location2Key");
					if (pathLocation1Key==null)
						continue;
					if (pathLocation2Key==null)
						continue;
					if (currentLocationKey.getId()==pathLocation1Key.getId())
					{
						destination = pathLocation2Key;
						pathEnd = 2;
					}
					else if (currentLocationKey.getId()==pathLocation2Key.getId())
					{
						destination = pathLocation1Key;
						pathEnd = 1;
					}
					
					if (destination!=null)
					{
						String forceOneWay = (String)path.getProperty("forceOneWay");
						if ("FromLocation1Only".equals(forceOneWay) && currentLocationKey.getId() == pathLocation2Key.getId())
							continue;
						if ("FromLocation2Only".equals(forceOneWay) && currentLocationKey.getId() == pathLocation1Key.getId())
							continue;
				
						if ("TRUE".equals(discovery.getProperty("hidden")) && (db.getRequest().getParameter("showHiddenPaths")==null || !showHidden))
						{
							// Skip this path, it's hidden
							hasHiddenPaths = true;
							continue;
						}
						
						paths.add(path);
						destLocationsToLoad.add(destination);
						pathEnds.add(pathEnd);
					}
				}
			}
			
			
			// Now that we have a list of destLocations to load, we will load them in now and delete any that ended up being null..
			destLocations = db.getEntities(destLocationsToLoad);
			List<Key> entitiesToDelete = new ArrayList<Key>();
			for(int i = destLocations.size()-1; i>=0; i--)
			{
				if (destLocations.get(i)==null)
				{
					paths.remove(i);
					pathEnds.remove(i);
					destLocations.remove(i);
					// I kinda wanna delete the null destLocation from the DB but.. what if its just not there because it was only created
					// recently? Maybe it'll show up soon? :/
				}
			}
			
		}
	}
	
	
	
	
	
	
	
	
	public String getLocationBanner()
	{
		String banner = (String)location.getProperty("banner");
		if (banner!=null && banner.startsWith("http")==false)
			return "https://initium-resources.appspot.com/"+banner;
		else
			return banner;
	}

	public String getLocationBiome()
	{
		String biome = (String)location.getProperty("biomeType");
		if (biome==null) biome = "Temperate";
		return biome;
	}
	
	public Long getInstanceRespawnTime()
	{
		if ("TRUE".equals(location.getProperty("instanceModeEnabled")) && location.getProperty("instanceRespawnDate")!=null)
		{
			Date respawnDate = (Date)location.getProperty("instanceRespawnDate");
			return respawnDate.getTime();
		}

		return null;
	}
	
	private boolean territoryCacheLoaded = false;
	private CachedEntity territory=null;
	private CachedEntity territoryOwningGroup=null;
	private void loadTerritoryEntities()
	{
		if (territoryCacheLoaded==false)
		{
			
			territory = db.getEntity((Key)location.getProperty("territoryKey"));
			territoryOwningGroup = db.getEntity((Key)territory.getProperty("owningGroupKey"));
			
			territoryCacheLoaded = true;
			
		}
	}
	
	public CachedEntity getTerritory()
	{
		loadTerritoryEntities();
		
		return territory;
	}
	
	public CachedEntity getTerritoryOwningGroup()
	{
		loadTerritoryEntities();
		
		return territoryOwningGroup;
	}
	
	private List<CachedEntity> partyMembers = null;
	public List<CachedEntity> getParty()
	{
		if (isInParty() && partyMembers==null)
		{
			partyMembers = db.getParty(null, character);
		}
		
		return partyMembers;
	}
	
	public boolean isInParty()
	{
		if (character.getProperty("partyCode")!=null)
			return true;
		
		return false;
	}
	
	public boolean isPartyLeader()
	{
		return "TRUE".equals(character.getProperty("partyLeader"));
	}
	
	
	
	
	/**
	 * This updates the gold amount in the header bar.
	 * 
	 * @param character
	 * @return Returns the new html just in case we want to use this method directly
	 */
	public String updateMoney()
	{
		Long gold = (Long)character.getProperty("dogecoins");
		String goldFormatted = GameUtils.formatNumber(gold);
		
		return updateHtmlContents("#mainGoldIndicator", goldFormatted);
	}
	
	public String updateLocationName()
	{
		return updateHtmlContents("#locationName", (String)location.getProperty("name"));
	}
	
	/**
	 * This updates the html in the character widget that is in the top left corner of the banner.
	 * 
	 * @param userOfViewer
	 * @param groupOfCharacter
	 */
	public String updateInBannerCharacterWidget()
	{
		String newHtml = GameUtils.renderCharacterWidget(db.getRequest(), db, character, user, true);
		
		return updateHtmlContents("#inBannerCharacterWidget", newHtml);
	}
	
	
	public String updateInBannerOverlayLinks()
	{
		loadPathCache();
		
		StringBuilder newHtml = new StringBuilder();
		for(int i = 0; i<paths.size(); i++)
		{
			CachedEntity path = paths.get(i);
			CachedEntity destLocation = destLocations.get(i);
			Integer pathEnd = pathEnds.get(i);
				
			String destLocationName = (String)destLocation.getProperty("name");

			String overlayCoordinates = (String)path.getProperty("location"+pathEnd+"OverlayCoordinates");
			if (overlayCoordinates==null || overlayCoordinates.matches("\\d+x\\d+")==false)
				continue;
			String top = "";
			String left = "";
			String[] split = overlayCoordinates.split("x");
			left = split[0];
			top = split[1];
			
			// Conver to percentage coordinates
			double topInt = Double.parseDouble(top);
			double leftInt = Double.parseDouble(left);
			top = new Double(topInt/211d*100).intValue()+"";
			left = new Double(leftInt/728d*100).intValue()+"";
			
			String buttonCaption = "Head towards "+destLocationName;
			String buttonCaptionOverride = (String)path.getProperty("location"+pathEnd+"ButtonNameOverride");
			String overlayCaptionOverride = (String)path.getProperty("location"+pathEnd+"OverlayText");
			if (buttonCaptionOverride!=null && buttonCaptionOverride.trim().equals("")==false)
				buttonCaption = buttonCaptionOverride;
			if (overlayCaptionOverride!=null && overlayCaptionOverride.trim().equals("")==false)
				buttonCaption = overlayCaptionOverride;
			
			
			Long travelTime = (Long)path.getProperty("travelTime");
			String onclick = "";
			if (travelTime==null || travelTime>0)
				onclick = "onclick='popupPermanentOverlay_Walking(\""+destLocationName+"\")'";
			
			String shortcutPart = "";
			String shortcutKeyIndicatorPart = "";
	
			newHtml.append("<a onclick='doGoto(event, "+path.getKey().getId()+", true)' class='path-overlay-link' style='top:"+top+"%;left: "+left+"%;' "+onclick+">"+buttonCaption+"</a>");
	
		}
		
		
		return updateHtmlContents("#banner-text-overlay", newHtml.toString());
	}

	public String updateButtonList(CombatService cs, boolean showHidden){
		loadPathCache(showHidden);
		
		if (cs.isInCombat(character))
			return updateButtonList_CombatMode();
		else
			return updateButtonList_NormalMode();
	}
	
	public String updateButtonList(CombatService cs)
	{
		return updateButtonList(cs, false);
	}
	
	private String updateButtonList_NormalMode()
	{
		StringBuilder newHtml = new StringBuilder();

		newHtml.append("<a class='main-button-icon' href='#' shortcut='87' onclick='doExplore(true)'><img src='https://initium-resources.appspot.com/images/ui/ignore-combat-sites.png' title='This button allows you to explore while ignoring combat sites. The shortcut key for this is W.' border=0/></a>");
		newHtml.append("<a href='#' class='main-button' shortcut='69' onclick='doExplore(false)'><span class='shortcut-key'>(E)</span>Explore "+location.getProperty("name")+"</a>");
					
		newHtml.append("<br>");
		
		
		if ("CampSite".equals(location.getProperty("type")))
		{
			newHtml.append("<a onclick='campsiteDefend()' class='main-button' shortcut='68' onclick='popupPermanentOverlay(\"Defending...\", \"You are looking out for threats to your camp.\")'><span class='shortcut-key'>(D)</span>Defend</a>");
			newHtml.append("<br>");
		}

		if ("RestSite".equals(location.getProperty("type")) || "CampSite".equals(location.getProperty("type")))
		{
			newHtml.append("<a href='#' class='main-button' shortcut='82' onclick='doRest()'><span class='shortcut-key'>(R)</span>Rest</a>");
			newHtml.append("<br>");
		}

		if (location.getProperty("name").toString().equals("Aera Inn"))
		{
			newHtml.append("<a class='main-button' onclick='doDrinkBeer(event)'>Drink Beer</a>");
			newHtml.append("<br>");
		}

		if (getGroup()!=null && location.getProperty("ownerKey")!=null && ((Key)location.getProperty("ownerKey")).getId() == user.getKey().getId())
		{
			newHtml.append("<a href='#' class='main-button' onclick='giveHouseToGroup()'>Give this property to your group</a>");
			newHtml.append("<br>");
		}
		
		if (db.isCharacterAbleToCreateCampsite(db.getDB(), character, location))
		{
			newHtml.append("<a href='#' class='main-button' onclick='createCampsite()'>Create a campsite here</a>");
			newHtml.append("<br>");
		}

		if ("CityHall".equals(location.getProperty("type")))
		{
			newHtml.append("<a href='#' class='main-button' onclick='buyHouse(event)'>Buy a new house</a>");
			newHtml.append("<br>");
		}
		
		if (user!=null && GameUtils.equals(location.getProperty("ownerKey"), user.getKey()))
		{
			newHtml.append("<a onclick='renamePlayerHouse(event)' class='main-button'>Rename this house</a>");
		}
		

		int shortcutStart = 49;
		int shortcutNumber = 1;
		int forgettableCombatSites = 0;
		StringBuilder forgettableCombatSiteList = new StringBuilder();
		forgettableCombatSiteList.append("\"");
		for(int i = 0; i<paths.size(); i++)
		{
			CachedEntity path = paths.get(i);
			CachedEntity destLocation = destLocations.get(i);
			Integer pathEnd = pathEnds.get(i);

			
			
				
			String destLocationName = (String)destLocation.getProperty("name");
			
			String buttonCaption = "Head towards "+destLocationName;
			String buttonCaptionOverride = (String)path.getProperty("location"+pathEnd+"ButtonNameOverride");
			if (buttonCaptionOverride!=null && buttonCaptionOverride.trim().equals("")==false)
				buttonCaption = buttonCaptionOverride;
			
			Long travelTime = (Long)path.getProperty("travelTime");
			String onclick = "";
			if (travelTime==null || travelTime>0)
				onclick = "onclick='popupPermanentOverlay_Walking(\""+destLocationName+"\")'";
			
			String shortcutPart = "";
			String shortcutKeyIndicatorPart = "";
			if (shortcutNumber<10)
			{
				shortcutPart = "shortcut='"+(shortcutStart+shortcutNumber-1)+"'";
				shortcutKeyIndicatorPart = "<span class='shortcut-key' title='This indicates the keyboard shortcut to use to activate this button'>("+shortcutNumber+")</span>";
				shortcutNumber++;
			}

			boolean defensiveStructureAllowed = false;
			if ("TRUE".equals(destLocation.getProperty("defenceStructuresAllowed")))
					defensiveStructureAllowed = true;

			if ("PlayerHouse".equals(path.getProperty("type")))
			{
				if (user!=null && Boolean.TRUE.equals(user.getProperty("premium")))
				{/*Simpler if logic this way*/}
				else
					newHtml.append("<p style='text-align:center;' title='Save this link to your house. This is a temporary workaround'>https://www.playinitium.com/ServletCharacterControl?type=goto&pathId="+path.getKey().getId()+"</p>");
			}


			

			if ("CombatSite".equals(location.getProperty("type")))
			{
				newHtml.append("<a href='#' onclick='doGoto(event, "+path.getKey().getId()+")' class='main-button' "+shortcutPart+" >"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
				newHtml.append("<br>");
				newHtml.append("<a onclick='leaveAndForgetCombatSite("+path.getKey().getId()+")' class='main-button' shortcut='70' "+onclick+"><span class='shortcut-key'>(F)</span>Leave this site and forget about it</a>");
				newHtml.append("<br>");
			}
			else if ("CombatSite".equals(destLocation.getProperty("type"))) {
				newHtml.append("<a class='main-forgetPath' onclick='doForgetCombatSite(event,"+destLocation.getKey().getId()+")'>X</a><a onclick='doGoto(event, "+path.getKey().getId()+")' class='main-button' "+shortcutPart+" "+onclick+">"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
				forgettableCombatSites++;
				String destLocationKeyId = String.valueOf(destLocation.getKey().getId());
				forgettableCombatSiteList.append(destLocationKeyId+",");
			}
			else if ("BlockadeSite".equals(destLocation.getProperty("type")) || defensiveStructureAllowed)
				newHtml.append("<a href='#' class='main-button-icon' onclick='doGoto(event, "+path.getKey().getId()+", true)'><img src='https://initium-resources.appspot.com/images/ui/attack1.png' title='This button allows you to travel to this location with the intent to attack any player-made defences without a confirmation' border=0/></a><a href='#' onclick='doGoto(event, "+path.getKey().getId()+")' class='main-button' "+shortcutPart+" >"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
			else if ("CollectionSite".equals(location.getProperty("type")))
			{
				newHtml.append("<br>");
				newHtml.append("<a onclick='leaveAndForgetCombatSite("+path.getKey().getId()+")' class='main-button' shortcut='70' "+onclick+"><span class='shortcut-key'>(F)</span>Leave this site and forget about it</a>");
			}
			// If we're looking at a player-house path, but we're not actually INSIDE the player house currently
			else if (GameUtils.equals(location.getProperty("ownerKey"), null) && "PlayerHouse".equals(path.getProperty("type")))
			{
				newHtml.append("<a class='main-forgetPath' onclick='deletePlayerHouse(event, "+path.getId()+")'>X</a><a onclick='doGoto(event, "+path.getKey().getId()+")' class='main-button' "+shortcutPart+" "+onclick+">"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
			}
			else
				newHtml.append("<a href='#' onclick='doGoto(event, "+path.getKey().getId()+")' class='main-button' "+shortcutPart+" >"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
	//		newHtml.append("<a href='ServletCharacterControl?type=goto&pathId="+path.getKey().getId()+"' class='main-button' "+shortcutPart+"  "+onclick+">"+shortcutKeyIndicatorPart+buttonCaption+"</a>");

		}
		
		if (location.getProperty("defenceStructure")!=null)
			newHtml.append("<a onclick='attackStructure()' shortcut='65' class='main-button'><span class='shortcut-key'>(A)</span>Attack this structure's defenders</a>");
			

		
		if(forgettableCombatSites > 1) {
			//remove the last comma
			forgettableCombatSiteList.deleteCharAt(forgettableCombatSiteList.length()-1);
			forgettableCombatSiteList.append("\"");
			newHtml.append("<center><a onclick='doForgetAllCombatSites(event, "+forgettableCombatSiteList.toString()+")'>Forget all forgettable sites</a></center><br/>");
		}
			
		
		if (hasHiddenPaths)
		{
			newHtml.append("<center><a href='main.jsp?showHiddenPaths=true'>Show hidden paths</a></center>");
		}
		
		return updateHtmlContents("#main-button-list", newHtml.toString());
	}
	
	private String updateButtonList_CombatMode()
	{
		StringBuilder newHtml = new StringBuilder();
		
		return updateHtmlContents("#main-button-list", newHtml.toString());
		
	}

	
	public String updateLocationJs()
	{
		StringBuilder js = new StringBuilder();
		
		
		
		js.append("var bannerUrl = '"+getLocationBanner()+"';");

		js.append("if (isAnimatedBannersEnabled()==false && bannerUrl.indexOf('.gif')>0)");
		js.append("bannerUrl = 'https://initium-resources.appspot.com/images/banner---placeholder.gif';");
		js.append("else if (isBannersEnabled()==false)");
		js.append("bannerUrl = 'https://initium-resources.appspot.com/images/banner---placeholder.gif';");
		js.append("else if (bannerUrl=='' || bannerUrl == 'null')");
		js.append("bannerUrl = 'https://initium-resources.appspot.com/images/banner---placeholder.gif';");

		js.append("var serverTime = "+System.currentTimeMillis()+";");

		js.append("var isOutside = '"+location.getProperty("isOutside")+"';");
	
		if ("TRADING".equals(character.getProperty("mode")))
			js.append("$(document).ready(function(){_viewTrade();});");

		
		
		js.append("window.biome = '"+getLocationBiome()+"';");

		js.append("window.instanceRespawnMs = "+getInstanceRespawnTime()+";");
		js.append("if (window.instanceRespawnMs!=null)");
		js.append("{");
		js.append("	if (window.instanceRespawnWarningId!=null) clearInterval(window.instanceRespawnWarningId);");
		js.append("	refreshInstanceRespawnWarning();");
		js.append("	window.instanceRespawnWarningId = setInterval(refreshInstanceRespawnWarning, 1000);");
		js.append("}");
		

		// Remove the walking man stuff
		js.append("$('#banner-base').html('');");
		
		js.append("$(document).ready(updateBannerWeatherSystem);");
		
		return updateJavascript("ajaxJs", js.toString());
	}
	
	public String updateActivePlayerCount()
	{
		return updateHtmlContents("#activePlayerCount", db.getActivePlayers()+"");
	}
	
	public String updateButtonBar()
	{
		return updateHtmlContents("#buttonBar", HtmlComponents.generateButtonBar(character));
	}

	public String updateLocationDescription()
	{
		String desc = (String)location.getProperty("description");
		if (desc==null) desc = "";
		return updateHtmlContents("#locationDescription", desc);
	}
	
	
	public String updateLocationDirectScripts()
	{
		StringBuilder newHtml = new StringBuilder();
		List<Key> scriptKeys = (List<Key>)location.getProperty("scripts");
		if (scriptKeys!=null && scriptKeys.isEmpty()==false)
		{
			List<CachedEntity> directLocationScripts = db.getScriptsOfType(scriptKeys, ScriptType.directLocation);
			if (directLocationScripts!=null && directLocationScripts.isEmpty()==false)
			{
				for(CachedEntity script:directLocationScripts)
				{
					String caption = (String)script.getProperty("caption");
					String id = script.getId().toString();
					String description = (String)script.getProperty("description");
					Boolean isEnabled = (Boolean)script.getProperty("isEnabled");
					if (isEnabled==null) isEnabled = true;

					if (isEnabled)
						newHtml.append("<a class='main-button-half' title='"+WebUtils.jsSafe(description)+"' onclick='doTriggerLocation(event, "+id+", "+location.getId()+")'>"+WebUtils.htmlSafe(caption)+"</a>");
					
				}
			}
		}
		
		return updateHtmlContents("#locationScripts", newHtml.toString());
		
	}

	
	public String updateTerritoryView()
	{
		if (location.getProperty("territoryKey")!=null)
		{
			return HtmlComponents.generateTerritoryView(character, getTerritoryOwningGroup(), getTerritory());
		}

		return updateHtmlContents("#locationScripts", "");
	}
	
	public String updatePartyView()
	{
		StringBuilder newHtml = new StringBuilder();

		if (isInParty())
		{
			newHtml.append("<div class='boldbox'>");
			newHtml.append("<a onclick='leaveParty()' style='float:right'>Leave Party</a>");
			newHtml.append("<h4>Your party</h4>");
			List<CachedEntity> party = getParty();
			if (party!=null)
			{
				for(CachedEntity character:party)
				{
					boolean isThisMemberTheLeader = false;
					if ("TRUE".equals(character.getProperty("partyLeader")))
						isThisMemberTheLeader = true;
					boolean dead = false;
					if (((Double)character.getProperty("hitpoints"))<=0)
						dead = true;
					newHtml.append("<div class='main-splitScreen-2columns'>");
					newHtml.append("<a class='main-item clue' rel='viewcharactermini.jsp?characterId="+character.getKey().getId()+"'>"+character.getProperty("name"));
					if (isThisMemberTheLeader)
						newHtml.append("<div class='main-item-controls' style='top:0px;'>(Leader)</div>");
					if (dead)
					{
						newHtml.append("<div class='main-item-controls' style='top:0px'>");
						newHtml.append("<a onclick='collectDogecoinFromCharacter("+character.getKey().getId()+")'>Collect "+character.getProperty("dogecoins")+" gold</a>");
						newHtml.append("</div>");
					}
					else
					{
						newHtml.append("<div class='main-item-controls' style='top:0px'>");
						// If this party character is not currently the leader and we are the current party leader then render the "make leader" button
						if (isThisMemberTheLeader == false && isPartyLeader())
							newHtml.append("<a onclick='doSetLeader(event, "+character.getKey().getId()+")'>Make Leader</a>");
						newHtml.append("</div>");
					}
					newHtml.append("</a>");
					newHtml.append("</div>");
				}
			}
			newHtml.append("</div>");
		}
		
		return updateHtmlContents("#partyPanel", newHtml.toString());
	}
	
}