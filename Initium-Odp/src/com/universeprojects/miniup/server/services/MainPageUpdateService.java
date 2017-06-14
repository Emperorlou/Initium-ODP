package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.ScriptType;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.aspects.AspectFireplace;

public class MainPageUpdateService extends Service
{
	final protected CachedEntity user;
	final protected CachedEntity character;
	protected CachedEntity location;
	final protected OperationBase operation;

	protected CachedEntity group = null;
	
	// Path related caches
	boolean hasHiddenPaths = false;
	protected List<CachedEntity> discoveries = null;  // All the discoveries we have for this character and location.
	protected List<CachedEntity> paths = null;  // All the paths that we can currently see that are connected to the path we're location in.
	protected List<CachedEntity> destLocations = null;  // The location entities at the other end of the paths; on the side we're not on currently.
	protected List<Integer> pathEnds = null;  // 1 or 2. Since each path is 2 sided, this number indicates which side we are NOT on currently.
	
	
	protected List<CachedEntity> immovables = null;	// All items that are immovable will be fetched and stored here for the location
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

		// If the character's location is null, time to send them to their home town or default location
		if (character.getProperty("locationKey")==null)
		{
			this.location = db.getEntity((Key)db.getCurrentCharacter().getProperty("homeTownKey"));
			if (this.location==null)
				this.location = db.getEntity(db.getDefaultLocationKey());
			db.getCurrentCharacter().setProperty("locationKey", this.location.getKey());	// Not sure why this was throwing an NPE sometimes. Added a null check that shouldn't happen.
			this.character.setProperty("locationKey", this.location.getKey());
		}
		
		
	}

	protected String updateHtmlContents(String selector, String newHtml)
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

	private CachedEntity combatantCache = null;
	protected CachedEntity getCombatant()
	{
		if (combatantCache==null)
		{
			Key combatantKey = (Key)character.getProperty("combatant");
			combatantCache = db.getEntity(combatantKey);
			
			// QUICK FIX for a recurring state issue...
			if (CommonChecks.checkCharacterIsInCombat(character) && combatantCache==null)
			{
				character.setProperty("mode", "NORMAL");
				character.setProperty("combatant", null);
				ds.put(character);
			}
		}
		return combatantCache;
	}

	private boolean isGroupLoaded = false;
	protected CachedEntity getGroup()
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

	protected void loadImmovables()
	{
		if (immovables==null)
		{
			immovables = query.getFilteredList("Item", "containerKey", location.getKey(), "immovable", true);
			if (immovables == null) immovables = new ArrayList<CachedEntity>(0);
		}
	}
	
	/**
	 * This will load all the path related caches, but only if we haven't loaded them before for
	 * this particular session.
	 * @return
	 */
	protected void loadPathCache() {
		loadPathCache(false);
	}
	
	/**
	 * This will load all the path related caches, but only if we haven't loaded them before for
	 * this particular session.
	 * 
	 * @param showHidden  A boolean value that determines whether we should load the hidden paths.
	 * @return
	 */
	protected void loadPathCache(boolean showHidden)
	{
		if (paths==null)
		{
			discoveries = db.getDiscoveriesForCharacterAndLocation(character.getKey(), location.getKey(), showHidden);
			
			// Order discoveries by createdDate of discovery entity
			Collections.sort(discoveries, new Comparator<CachedEntity>()
			{

				@Override
				public int compare(CachedEntity o1, CachedEntity o2)
				{
					if (o1==null || o2==null)
						return 0;
					Date o1Date = (Date)o1.getProperty("createdDate");
					Date o2Date = (Date)o2.getProperty("createdDate");
					if (o1Date==null || o2Date==null) return 0;
					return o1Date.compareTo(o2Date);
				}
			});
			
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
				
						if (GameUtils.booleanEquals(discovery.getProperty("hidden"), true) && !showHidden)
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
//			List<Key> entitiesToDelete = new ArrayList<Key>();
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
		
		if (CommonChecks.checkCharacterIsInCombat(character) && CommonChecks.checkLocationIsCombatSite(location)==false)
		{
			CachedEntity combatant = getCombatant();
			if (combatant!=null && CommonChecks.checkCharacterIsDead(combatant)==false)
			{
				banner = (String)combatant.getProperty("bannerUrl");
				if (banner==null)
					banner = "images/npc-generic1.jpg";
			}
			else
			{
				// THIS IS A BAD STATE, we will go ahead and fix it now. The player should not be in combat at this point.
				db.getCurrentCharacter().setProperty("mode", "NORMAL");
				db.getCurrentCharacter().setProperty("combatant", null);
				db.getCurrentCharacter().setProperty("combatType", null);
				db.getDB().put(db.getCurrentCharacter());
			}
		}
			
		
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
	
	
	public void shortcut_fullPageUpdate(CombatService combatService)
	{
		updateMoney();
		updateInBannerOverlayLinks();
		updateButtonList(combatService);
		updateLocationJs();	
		updateActivePlayerCount();
		updateButtonBar();
		updateLocationName();
		updateLocationDescription();
		updateMonsterCountPanel();
		updateTerritoryView();
		updatePartyView();
		updateCollectablesView();
		updateLocationDirectScripts();
		updateInBannerCharacterWidget();
		updateInBannerCombatantWidget();
		updateTestPanel();
		updateImmovablesPanel(); 
		updateMidMessagePanel();
		updateLocationQuicklist();
	}

	/**
	 * This updates the gold amount in the header bar.
	 *
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
	 */
	public String updateInBannerCharacterWidget()
	{
		if(user != null && Boolean.TRUE.equals(user.getProperty("premium")) && db.getRequest().getAttribute("characterList") == null)
		{
			List<CachedEntity> characterList = db.getFilteredList("Character", "userKey", user.getKey());
			db.getRequest().setAttribute("characterList", characterList);  
		}
		String newHtml = GameUtils.renderCharacterWidget(db.getRequest(), db, character, user, true);
		
		return updateHtmlContents("#inBannerCharacterWidget", newHtml);
	}
	
	/**
	 * This fetches the combatant and updates the html in the combatant widget that is in the top right corner of the banner.
	 */
	public String updateInBannerCombatantWidget()
	{
		String newHtml = "";
		
		if (CommonChecks.checkCharacterIsInCombat(character))
		{
			CachedEntity combatant = getCombatant();
			newHtml = GameUtils.renderCharacterWidget(db.getRequest(), db, combatant, null, false);
		}
		
		return updateHtmlContents("#inBannerCombatantWidget", newHtml);
	}

	/**
	 * This updates the html in the combatant widget that is in the top right corner of the banner.
	 */
	public String updateInBannerCombatantWidget(CachedEntity combatant)
	{
		String newHtml = GameUtils.renderCharacterWidget(db.getRequest(), db, combatant, null, false);
		
		return updateHtmlContents("#inBannerCombatantWidget", newHtml);
	}

	/**
	 * This updates the TestPanel if environment is currently in test
	 * 
	 */
	public String updateTestPanel()
	{
		if (db.isTestServer()) {
			StringBuilder newHtml = new StringBuilder();
			
			newHtml.append("<div id=\"viewportcontainer\" class=\"vpcontainer\">");
			newHtml.append("<div id=\"menu\" class=\"menuContainer\" style=\"visibility: hidden;\"></div>");
			newHtml.append("<div id=\"viewport\" class=\"vp\">");
			newHtml.append("<div id=\"grid\" class=\"grid\">");
			newHtml.append("<div id=\"ui-layer\" class=\"uiLayer\"></div>");
			newHtml.append("<div id=\"cell-layer\" class=\"cellLayer\"></div>");
			newHtml.append("<div id=\"ground-layer\" class=\"groundLayer\"></div>");
			newHtml.append("<div id=\"object-layer\" class=\"objectLayer\"></div>");
			newHtml.append("</div>");
			newHtml.append("</div>");
			newHtml.append("</div>");
			newHtml.append("<button type=\"button\" onclick=\"openMenu()\">Menu</button>");
			newHtml.append("<button type=\"button\" onclick=\"mapPlow(event)\">Plow</button>");
			newHtml.append("<button type=\"button\" onclick=\"mapPlaceHouse(event)\" style=\"position:relative\">Place House</button>");
			newHtml.append("<button type=\"button\" onclick=\"mapPlaceCity(event)\" style=\"position:relative\">Place City</button>");
			newHtml.append("<center><p id=\"selectedObjects\" class=\"selectedObjectList\"></p></center>");
			newHtml.append("<script type=\"text/javascript\" src=\"/odp/javascript/Sandbox.js\"></script>");
			newHtml.append("<script>");
			newHtml.append("var mapData = '" + GridMapService.buildNewGrid(123456,20,20,2).toString() + "';");
			newHtml.append("$(document).on(\"click\", \"#somebutton\", function() { pressedButton(); });");
			newHtml.append("</script>");
			
			return updateHtmlContents("#test-panel", newHtml.toString());
		}
		return "";
	}
	
	
	public String updateInBannerOverlayLinks()
	{
		StringBuilder newHtml = new StringBuilder();
		if (CommonChecks.checkCharacterIsInCombat(character)==false)
		{
			loadPathCache();
			
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
				
				// Convert to percentage coordinates
				double topDbl = Double.parseDouble(top);
				double leftDbl = Double.parseDouble(left);
				int topInt = new Double(topDbl/211d*100).intValue();
				int leftInt = new Double(leftDbl/728d*100).intValue();
		

				String buttonCaption = "Head towards "+destLocationName;
				String buttonCaptionOverride = (String)path.getProperty("location"+pathEnd+"ButtonNameOverride");
				String overlayCaptionOverride = (String)path.getProperty("location"+pathEnd+"OverlayText");
				if (buttonCaptionOverride!=null && buttonCaptionOverride.trim().equals("")==false)
					buttonCaption = buttonCaptionOverride;
				if (overlayCaptionOverride!=null && overlayCaptionOverride.trim().equals("")==false)
					buttonCaption = overlayCaptionOverride;
				
				
				String onclick = "doGoto(event, "+path.getKey().getId()+", true);";				
				
				newHtml.append(getHtmlForInBannerLink(topInt, leftInt, buttonCaption, onclick));
			}
		}
		else
		{
			// We're in combat, lets throw up some handy buttons
			List<CachedEntity> weapons = db.getEntities((Key)character.getProperty("equipmentLeftHand"), (Key)character.getProperty("equipmentRightHand"));
			String leftIcon = GameUtils.getResourceUrl("images/small/Pixel_Art-Weapons-Other-Natural-Natural2.png"); 
			if (weapons.get(0)!=null)
				leftIcon = GameUtils.getResourceUrl(weapons.get(0).getProperty(GameUtils.getItemIconToUseFor("equipmentLeftHand", weapons.get(0))));
			
			String rightIcon = GameUtils.getResourceUrl("images/small/Pixel_Art-Weapons-Other-Natural-Natural2.png");
			if (weapons.get(1)!=null)
				rightIcon = GameUtils.getResourceUrl(weapons.get(1).getProperty(GameUtils.getItemIconToUseFor("equipmentRightHand", weapons.get(1))));
			
			newHtml.append(getHtmlForInBannerLink(50, 40, "<img src='"+leftIcon+"' alt='Left Hand' style='max-width:32px; max-height:32px;padding:5px;'/>", "doCombatAttackLeftHand(event)"));
			newHtml.append(getHtmlForInBannerLink(50, 60, "<img src='"+rightIcon+"' alt='Right Hand' style='max-width:32px; max-height:32px;padding:5px;'/>", "doCombatAttackRightHand(event)"));
			newHtml.append(getHtmlForInBannerLink(90, 50, "<span style='padding:5px;'>RUN!</span>", "doCombatEscape(event)"));
		}
		
		
		return updateHtmlContents("#banner-text-overlay", newHtml.toString());
	}
	
	private String getHtmlForInBannerLink(double top, double left, String buttonCaption, String onclickJs)
	{
		return "<a onclick='"+onclickJs.replace("'", "\\'")+"' class='path-overlay-link' style='top:"+top+"%;left: "+left+"%;'>"+buttonCaption+"</a>";
		
	}

	public String updateButtonList(CombatService cs, boolean showHidden){
		
		if (cs.isInCombat(character))
			return updateButtonList_CombatMode();
		else
		{
			loadPathCache(showHidden);
			return updateButtonList_NormalMode();
		}
	}
	
	public String updateButtonList(CombatService cs)
	{
		return updateButtonList(cs, false);
	}
	
	protected String updateButtonList_NormalMode()
	{
		StringBuilder newHtml = new StringBuilder();

		newHtml.append("<div class='main-splitScreen'>");
		newHtml.append("<div id='main-merchantlist'>");
		newHtml.append("<div class='main-button-half' onclick='loadLocationMerchants()' shortcut='83'>");
		newHtml.append("<span class='shortcut-key'> (S)</span><img src='https://initium-resources.appspot.com/images/ui/magnifying-glass.png' border=0/> Nearby stores");
		newHtml.append("</div>");
		newHtml.append("</div>");
		newHtml.append("</div>");
		newHtml.append("<div></div>");
		newHtml.append("<div class='main-splitScreen'>");
		newHtml.append("<div id='main-itemlist'>");
		newHtml.append("<div class='main-button-half' onclick='loadLocationItems()' shortcut='86'>");
		newHtml.append("<span class='shortcut-key'> (V)</span><img src='https://initium-resources.appspot.com/images/ui/magnifying-glass.png' border=0/> Nearby items");
		newHtml.append("</div>");
		newHtml.append("</div>");
		newHtml.append("</div>");
		newHtml.append("<div class='main-splitScreen'>");
		newHtml.append("<div id='main-characterlist'>");
		newHtml.append("<div class='main-button-half' onclick='loadLocationCharacters()' shortcut='66'>");
		newHtml.append("<span class='shortcut-key'> (B)</span><img src='https://initium-resources.appspot.com/images/ui/magnifying-glass.png' border=0/> Nearby characters");
		newHtml.append("</div>");
		newHtml.append("</div>");
		newHtml.append("</div>");
		newHtml.append("<div class='main-buttonbox'>");
		
		
		newHtml.append("<a id='main-explore-ignorecombatsites' class='main-button-icon' href='#' shortcut='87' onclick='doExplore(true)'><img src='https://initium-resources.appspot.com/images/ui/ignore-combat-sites.png' title='This button allows you to explore while ignoring combat sites. The shortcut key for this is W.' border=0/></a>");
		newHtml.append("<a id='main-explore' href='#' class='main-button' shortcut='69' onclick='doExplore(false)'><span class='shortcut-key'>(E)</span>Explore "+location.getProperty("name")+"</a>");
					
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
			if("RestSite".equals(location.getProperty("type")))
				newHtml.append("<a href='#' class='main-button' onclick='createCampsite()'>Create a campsite here</a>");
			else
				newHtml.append("<a href='#' class='main-button' shortcut='82' onclick='createCampsite()'><span class='shortcut-key'>(R)</span>Create a campsite here</a>");
			newHtml.append("<br>");
		}

		if ("CityHall".equals(location.getProperty("type")))
		{
			newHtml.append("<a href='#' class='main-button' onclick='buyHouse(event)'>Buy a new house</a>");
			newHtml.append("<br>");
		}
		
		if (user!=null && GameUtils.equals(location.getProperty("ownerKey"), user.getKey()))
		{
			newHtml.append("<a onclick='createMapToHouse(event)' class='main-button'>Create map to this house</a><br/>");
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
			if("Script".equals(destLocation.getKind()))
			{
				if(GameUtils.booleanEquals(destLocation.getProperty("hidden"), true)) 
					continue;
				destLocationName = (String)destLocation.getProperty("caption");
				buttonCaption = destLocationName;
			}
			
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


			
			if("Script".equals(destLocation.getKind()))
			{
				// Direct location is only thing we support right now, in terms of paths.
				if("directLocation".equals(destLocation.getProperty("type")))
				{
					String title = (String)destLocation.getProperty("description");
					if(title != null && title.length() > 0) title = "title='" + WebUtils.htmlSafe(title) + "'";
					else title = "";

					newHtml.append("<a href='#' onclick='doTriggerLocation(event, "+destLocation.getId()+", " + location.getId() + ")' class='main-button' " + title + " "+shortcutPart+" >"+shortcutKeyIndicatorPart+buttonCaption+"</a>");					
				}
			}
			else if ("CombatSite".equals(location.getProperty("type")) && "CombatSite".equals(destLocation.getProperty("type"))==false)
			{
				newHtml.append("<a href='#' onclick='doGoto(event, "+path.getKey().getId()+")' class='main-button' "+shortcutPart+" >"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
				newHtml.append("<br>");
				newHtml.append("<a href='#' onclick='leaveAndForgetCombatSite("+path.getKey().getId()+")' class='main-button' shortcut='70' "+onclick+"><span class='shortcut-key'>(F)</span>Leave this site and forget about it</a>");
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
				newHtml.append("<a href='#' onclick='leaveAndForgetCombatSite("+path.getKey().getId()+")' class='main-button' shortcut='70' "+onclick+"><span class='shortcut-key'>(F)</span>Leave this site and forget about it</a>");
			}
			// If we're looking at a player-house path, but we're not actually INSIDE the player house currently
			else if (GameUtils.equals(location.getProperty("ownerKey"), null) && "PlayerHouse".equals(path.getProperty("type")))
			{
				newHtml.append("<a href='#' class='main-forgetPath' onclick='deletePlayerHouse(event, "+path.getId()+")'>X</a><a onclick='doGoto(event, "+path.getKey().getId()+")' class='main-button' "+shortcutPart+" "+onclick+">"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
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
			
		
			newHtml.append("<center><a onclick='doShowHiddenSites(event)'>Show hidden paths</a></center>");
		
		return updateHtmlContents("#main-button-list", newHtml.toString());
	}
	
	protected String updateButtonList_CombatMode()
	{
		StringBuilder newHtml = new StringBuilder();
		
		List<CachedEntity> weapons = db.getEntities((Key)character.getProperty("equipmentLeftHand"), (Key)character.getProperty("equipmentRightHand"));
		newHtml.append(GameUtils.renderWeaponCommand(weapons.get(0), true));
		newHtml.append(GameUtils.renderWeaponCommand(weapons.get(1), false));
		newHtml.append("<a onclick='doCombatEscape(event)' class='main-button' shortcut='51'><span class='shortcut-key'>(3)</span>Try to run away</a>");
		
		return updateHtmlContents("#main-button-list", newHtml.toString());
	}
	

//	public String updateCombatView(CombatService cs, CachedEntity combatant, String combatResult)
//	{
//		StringBuilder csb = new StringBuilder();
//		if(cs.isInCombat(character))
//		{
//			// Handle updating all the combat UI: widgets, button lists, and combat results.
//			csb.append(updateInBannerCharacterWidget());
//			csb.append(updateInBannerCombatantWidget(combatant));
//			csb.append(updateButtonList(cs));
//		}
//		
//		if(combatResult != null && combatResult.isEmpty()==false)
//		{
//			db.addGameMessage(db.getDB(), character.getKey(), combatResult);
//		}
//		
//		// Empty string is the non-combat condition, which will be used to update 
//		// game state for first pass, which will be refresh.
//		return csb.toString();
//	}
	
	public String updateLocationJs()
	{
		StringBuilder js = new StringBuilder();
		
		
		js.append("window.bannerUrl = '"+getLocationBanner()+"';");
		js.append("window.previousBannerUrl = null;");

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
		
		js.append("updateBannerWeatherSystem();");
		
		
		// Now do the audio stuff..		
		String locationAudioDescriptor = (String)location.getProperty("audioDescriptor");
		if (locationAudioDescriptor==null) locationAudioDescriptor = "";
		js.append("locationAudioDescriptor = '"+locationAudioDescriptor+"';");

		
		
		String locationAudioDescriptorPreset = (String)location.getProperty("audioDescriptorPreset");
		if (locationAudioDescriptorPreset==null) locationAudioDescriptorPreset = "";
		js.append("locationAudioDescriptorPreset = '"+locationAudioDescriptorPreset+"';");
		
		js.append("setAudioDescriptor(locationAudioDescriptor, locationAudioDescriptorPreset, isOutside);");
		
		
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
		@SuppressWarnings("unchecked")
		List<Key> scriptKeys = (List<Key>)location.getProperty("scripts");
		if (scriptKeys!=null && scriptKeys.isEmpty()==false)
		{
			List<CachedEntity> directLocationScripts = db.getScriptsOfType(scriptKeys, ScriptType.directLocation);
			if (directLocationScripts!=null && directLocationScripts.isEmpty()==false)
			{
				for(CachedEntity script:directLocationScripts)
				{
					if(GameUtils.booleanEquals(script.getProperty("hidden"), true)) 
						continue;
					
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
			newHtml.append("<a onclick='leaveParty(event)' style='float:right'>Leave Party</a>");
			newHtml.append("<h4>Your party</h4>");
			List<CachedEntity> party = getParty();
			if (party!=null)
			{
				String mode = (String)character.getProperty("mode");
				Boolean isCharacterBusy = (mode!=null && mode.equals("NORMAL")==false);

				// Get Characters and Users
				EntityPool pool = new EntityPool(db.getDB());
				for(CachedEntity partyCharacter:party)
				{
					pool.addToQueue((Key)partyCharacter.getProperty("userKey"));
				}
				pool.loadEntities();

				for(CachedEntity partyCharacter:party)
				{
					CachedEntity partyUser = pool.get((Key)partyCharacter.getProperty("userKey"));
					boolean isThisMemberTheLeader = false;
					if ("TRUE".equals(partyCharacter.getProperty("partyLeader")))
						isThisMemberTheLeader = true;
					boolean dead = false;
					if (((Double)partyCharacter.getProperty("hitpoints"))<=0)
						dead = true;

					newHtml.append("<div style='display:inline-block;vertical-align:top;'>");
					newHtml.append("<a class='main-item clue' style='width:inherit;' rel='viewcharactermini.jsp?characterId="+partyCharacter.getKey().getId()+"'>");
					newHtml.append(GameUtils.renderCharacterWidget(db.getRequest(), db, partyCharacter, partyUser, true));
					newHtml.append("<br>");
					if (!GameUtils.equals(character.getKey(), partyCharacter.getKey()) &&
							GameUtils.equals(user.getKey(), partyUser.getKey())) {
						newHtml.append("<div class='main-item-controls' style='top:0px'>");
						newHtml.append("<a onclick='switchCharacter(event, "+partyCharacter.getKey().getId()+")'>Switch</a>");
						newHtml.append("</div>");
					}

					if (isThisMemberTheLeader)
						newHtml.append("<div class='main-item-controls' style='top:0px;'>(Leader)</div>");
					if (dead)
					{
						newHtml.append("<div class='main-item-controls' style='top:0px'>");
						newHtml.append("<a onclick='collectDogecoinFromCharacter("+partyCharacter.getKey().getId()+")'>Collect "+partyCharacter.getProperty("dogecoins")+" gold</a>");
						newHtml.append("</div>");
					}
					else if (!isCharacterBusy)
					{
						newHtml.append("<div class='main-item-controls' style='top:0px'>");
						// If this party character is not currently the leader and we are the current party leader then render the "make leader" button
						if (isThisMemberTheLeader == false && isPartyLeader())
							newHtml.append("<a onclick='doSetLeader(event, " + partyCharacter.getKey().getId() + ", \"" + partyCharacter.getProperty("name") + "\")'>Make Leader</a>");
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

	public String updateCollectablesView()
	{
		StringBuilder html = new StringBuilder();
		
		List<CachedEntity> collectablesHere = db.getCollectablesForLocation(location.getKey());
		
		if (collectablesHere!=null && collectablesHere.isEmpty()==false)
		{
			html.append("<h5>Resources</h5>");
			
			for(CachedEntity collectable:collectablesHere)
			{
				Long secondsTime = (Long)collectable.getProperty("extractionEase");
				if (secondsTime==null)
					secondsTime = 0L;
				
				html.append("<div class='main-item'> ");
				html.append("<div class='main-item-container'>");
				html.append(GameUtils.renderCollectable(collectable)); 
				html.append("<br>");
				html.append("<div class='main-item-controls'>");
				html.append("<a href='#' onclick='doCollectCollectable(event, "+collectable.getKey().getId()+")'>Extract/Collect</a>");
				html.append("</div>"); 
				html.append("</div>");
				html.append("</div>");
				html.append("<br/>");
			}
		}
		
		
		return updateHtmlContents("#collectablesPanel", html.toString());
	}

	public String updateImmovablesPanel(CachedEntity updatedItem)
	{
		loadImmovables();
		
		for(int i = 0; i<immovables.size(); i++)
			if (GameUtils.equals(immovables.get(i).getKey(), updatedItem.getKey()))
			{
				immovables.set(i, updatedItem);
				break;
			}
				
		return updateImmovablesPanel();
	}
	
	public String updateImmovablesPanel()
	{
		StringBuilder html = new StringBuilder();
		
		loadImmovables();
		
		if (immovables!=null && immovables.isEmpty()==false)
		{
			for(CachedEntity item:immovables)
			{
				String iconUrl = (String)item.getProperty("icon");
				if (iconUrl!=null && iconUrl.startsWith("http://"))
					iconUrl = "https://"+iconUrl.substring(7);
				else if (iconUrl!=null && iconUrl.startsWith("http")==false)
					iconUrl = "https://initium-resources.appspot.com/"+iconUrl;
				html.append("<div class='clue' rel='/odp/viewitemmini?itemId=").append(item.getKey().getId()).append("'>");
				html.append("<img src='").append(iconUrl).append("' border=0/>");
				html.append("</div>");
				
				// Special case for an active fire, we want sound effects for that
				InitiumObject obj = new InitiumObject(db, item);
				if (obj.isAspectPresent(AspectFireplace.class))
				{
					AspectFireplace fireplaceAspect = (AspectFireplace)obj.getInitiumAspect("Fireplace");
					long currentTimeMs = System.currentTimeMillis();
					if (fireplaceAspect.isFireActive(currentTimeMs))
					{
						double minutesLeft = new Long(fireplaceAspect.getMinutesUntilExpired(currentTimeMs)).doubleValue();
						
						double maxVolume = 0.1;
						double volume = (minutesLeft/40);
						volume*=maxVolume; // Max volume
						if (volume>maxVolume) volume = maxVolume;
						if (volume<0) volume = 0;
						html.append("<script type='text/javascript'>if (isSoundEffectsEnabled()) playLoopedSound('campfire1', ").append(volume).append(");</script>");
					}
					else
					{
						html.append("<script type='text/javascript'>stopLoopedSound('campfire1');</script>");
					}
				}
			}
		}
		
		
		return updateHtmlContents("#immovablesPanel", html.toString());
	}

	public String updateMonsterCountPanel()
	{
		StringBuilder html = new StringBuilder();

		Double monsterCount = db.getMonsterCountForLocation(ds, location);
		Double maxMonsterCount = (Double)location.getProperty("maxMonsterCount");
		
		
		if (monsterCount!=null && maxMonsterCount!=null)
		{
			if ("CampSite".equals(location.getProperty("type")))
			{
				if (monsterCount<1) monsterCount = 0d;
				{
					double monsterPercent = monsterCount/maxMonsterCount;
					html.append("<p>Camp integrity: <span class='main-item-subnote'>"+GameUtils.formatPercent(1d-monsterPercent)+"</span></p>");
					
				}
				
			}
			else
			{
				if (maxMonsterCount>10)
				{
					if (monsterCount<1) monsterCount = 0d;
					double monsterPercent = monsterCount/maxMonsterCount;
					html.append("<div class='main-description'>");
					html.append("The monster activity in this area seems ");
					if (monsterPercent>0.75)
						html.append("high compared to usual.");
					else if (monsterPercent>0.50)
						html.append("moderate compared to usual.");
					else if (monsterPercent>0.25)
						html.append("low compared to usual.");
					else if (monsterPercent>0)
						html.append("very low compared to usual.");
					else
						html.append("to be none.");
					
					//html.append("Debug: "+monsterCount+"/"+maxMonsterCount);
					html.append("</div>");
				}
			}
			
		}
		return updateHtmlContents("#monsterCountPanel", html.toString());
	}

	public String updateMidMessagePanel()
	{
		StringBuilder html = new StringBuilder();

		
		String clientDescription = db.getClientDescriptionAndClear(null, db.getCurrentCharacter().getKey());	// This should be near the end of the jsp's java head to reduce the chance of being redirected away from the page before the message gets displayed
		if (clientDescription==null || "null".equals(clientDescription)) clientDescription = "";
		if (clientDescription.equals("")==false)
		{
			 html.append("<div class='main-dynamic-content-box paragraph'>");			
			 html.append(clientDescription);
			 html.append("</div>");
		}
		
		return updateHtmlContents("#midMessagePanel", html.toString());
	}
	
	public String updateLocationQuicklist()
	{
		StringBuilder html = new StringBuilder();
		
		if (CommonChecks.checkLocationIsCombatSite(location) && CommonChecks.checkCharacterIsInCombat(character)==false)
		{
			html.append("<div class='boldbox'>");
			html.append("	<div id='inline-items' class='main-splitScreen'>");
			html.append("	</div>");
			html.append("	<div id='inline-characters' class='main-splitScreen'>");
			html.append("	</div>");
			html.append("</div>");
			html.append("<script type='text/javascript'>");
			html.append("	loadInlineItemsAndCharacters();");
			html.append("	loadInlineCollectables();");
			html.append("</script>");
		}
		
		
		
		return updateHtmlContents("#locationQuicklist", html.toString());
	}
}