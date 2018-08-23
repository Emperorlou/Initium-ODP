package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.InitiumObject;
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
	final protected CombatService cs;

	protected CachedEntity group = null;
	
	// Path related caches
	boolean hasHiddenPaths = false;
	protected List<CachedEntity> discoveries = null;  // All the discoveries we have for this character and location.
	protected List<CachedEntity> paths = null;  // All the paths that we can currently see that are connected to the path we're location in.
	protected List<CachedEntity> destLocations = null;  // The location entities at the other end of the paths; on the side we're not on currently.
	protected Map<Key, CachedEntity> destLocationsMap = null;
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
		this.cs = new CombatService(db);

		// If the character's location is null, time to send them to their home town or default location
		if (location==null)
			this.location = db.getCharacterLocation(character);
		
	}
	
	public static MainPageUpdateService getInstance(ODPDBAccess db, CachedEntity user, CachedEntity character, CachedEntity location, OperationBase operation)
	{
		String mainPageUrl = db.getRequest().getParameter("mainPageUrl");
		if (mainPageUrl==null) mainPageUrl = "";
		if (mainPageUrl.contains("/odp/full"))
		{
			return new FullPageUpdateService(db, user, character, location, operation);
		}
		else if (mainPageUrl.contains("/odp/experimental"))
		{
			return new ExperimentalPageUpdateService(db, user, character, location, operation);
		}
		else if (mainPageUrl.contains("/odp/game"))
		{
			return new GamePageUpdateService(db, user, character, location, operation);
		}
		else
		{
			return new MainPageUpdateService(db, user, character, location, operation);
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
			if (db.isTestServer()==false) // Don't get immovables if we're on the test server, its too slow...?
				immovables = db.getEntities(db.getImmovablesKeys(location.getKey()));
			if (immovables == null) immovables = new ArrayList<CachedEntity>(0);
		}
	}
	
	protected Map<Key,CachedEntity> getDestLocationsMap()
	{
		if (destLocations==null) return null;
		
		if (destLocationsMap==null)
		{
			destLocationsMap = new HashMap<>();
			for(CachedEntity entity:destLocations)
				destLocationsMap.put(entity.getKey(), entity);
		}
		
		return destLocationsMap;
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
			boolean speedUpTest = false;
			if (speedUpTest==false || db.isTestServer()==false)
				discoveries = db.getDiscoveriesForCharacterAndLocation(character.getKey(), location.getKey(), showHidden);
			else
				discoveries = new ArrayList<>(); // <-- when debugging locally
			
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
			List<CachedEntity> alwaysVisiblePaths = db.getLocationAlwaysVisiblePaths(location.getKey());
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
			
			// Fetch the paths all at once, then combine.
			List<Key> pathsToFetch = new ArrayList<Key>();
			for(CachedEntity discovery:discoveries)
			{
				// We allow null entityKey. Need to maintain ordinality.
				if(discovery != null)
					pathsToFetch.add((Key)discovery.getProperty("entityKey"));
				else
					pathsToFetch.add(null);
			}
					
			List<CachedEntity> discoveryEntities = db.getEntities(pathsToFetch);
			List<Key> keysToDelete = new ArrayList<Key>();
			// Get all discovered paths...
			for(int i = 0; i < discoveries.size(); i++)
			{
				CachedEntity discovery = discoveries.get(i);
				if (discovery==null) continue;	// Why would it be null I have no idea. Maybe just a race condition when deleting and this fetch took place.
				
				if ("Path".equals(discovery.getProperty("kind")))
				{
					CachedEntity path = discoveryEntities.get(i);
					if (path==null)
					{
						// If we get here, it's because the path was deleted. Lets remove the discovery for future and skip this one for now
						keysToDelete.add(discovery.getKey());
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
			
			ds.delete(keysToDelete);
			
			
			
			
			
			
			// Now that we're here, there is a special case for player houses. If we're in a player house and we have no
			// paths available to us, show the path out (if one exists)
			if (paths.isEmpty() && GameUtils.equals("RestSite", location.getProperty("type")))
			{
				List<CachedEntity> list = query.getFilteredList("Path", 2, "location1Key", FilterOperator.EQUAL, location.getKey());
				list.addAll(query.getFilteredList("Path", 2, "location2Key", FilterOperator.EQUAL, location.getKey()));
				
				if (list.size()==1)
				{
					CachedEntity path = list.get(0);
					
					Key destination = null;
					// First get the character's current location
					Key currentLocationKey = location.getKey();
					
					// Then determine which location the character will end up on.
					// If we find that the character isn't on either end of the path, we'll throw.
					Integer pathEnd = null;
					Key pathLocation1Key = (Key)path.getProperty("location1Key");
					Key pathLocation2Key = (Key)path.getProperty("location2Key");
					if (pathLocation1Key==null)
						return;
					if (pathLocation2Key==null)
						return;
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
						return;	
					else if (pathEnd == 2 && "FromLocation2Only".equals(path.getProperty("forceOneWay")))
						return;	
					
					if (destination!=null)
					{
						paths.add(path);
						destLocations.add(db.getEntity(destination));
						pathEnds.add(pathEnd);
					}
					
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	public String getLocationBanner()
	{
		String banner = (String)location.getProperty("banner");
		if (CommonChecks.checkCharacterIsIncapacitated(character))
		{
			banner = "images/unconscious1.jpg";
		}
		else if (CommonChecks.checkCharacterIsInCombat(character) && CommonChecks.checkLocationIsCombatSite(location)==false)
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
	
	public void updateFullPage_shortcut()
	{
		updateMoney();
		updateButtonList();
		updateInBannerOverlayLinks();
		updateLocationJs();	
		updateActivePlayerCount();
		updateButtonBar();
		updateLocationName();
		updateLocationDescription();
		updateCampsPanel();
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
		updateGlobalNavigationMap();
		updateLocation2D();
	}


	public void updateFullPage_shortcut(boolean refreshChat)
	{
		updateFullPage_shortcut();
		
		if (refreshChat==true)
			updateLocationJs(refreshChat);	
	}

	/**
	 * This updates the gold amount in the header bar.
	 *
	 * @return Returns the new html just in case we want to use this method directly
	 */
	public String updateMoney()
	{
		
		Long availableDonations = null;
		if (user!=null)
			availableDonations = (Long)user.getProperty("totalDonations");
		if (availableDonations==null) availableDonations = 0L;
		Double donationsDbl = availableDonations.doubleValue()/100;
		String donationsFormatted = GameUtils.formatNumber(donationsDbl);
		
		Long gold = (Long)character.getProperty("dogecoins");
		String goldFormatted = GameUtils.formatNumber(gold);
		
		String html = "<div class='top-section'>Gold: <span>"+goldFormatted+"</span></div>\r\n" + 
				"<div class='top-section' style='color: #a29852;'>Credit: $"+donationsFormatted+"</span></div>\r\n";
		
		return updateHtmlContents("#mainMoneyIndicator", html);
	}


	public String updateLocation2D()
	{
		String newHtml = "";
		return updateHtmlContents(".location-2d", newHtml);
	}	
	
	public String updateLocationName()
	{
		String html = "??";
		if (CommonChecks.checkCharacterIsIncapacitated(character))
			html = "";
		else
			html = (String)location.getProperty("name");
		
		return updateHtmlContents("#locationName", html);
	}
	
	/**
	 * This updates the html in the character widget that is in the top left corner of the banner.
	 *
	 */
	public String updateInBannerCharacterWidget()
	{
//		if(user != null && Boolean.TRUE.equals(user.getProperty("premium")) && db.getRequest().getAttribute("characterList") == null)
//		{
//			List<CachedEntity> characterList = db.getFilteredList("Character", "userKey", user.getKey());
//			db.getRequest().setAttribute("characterList", characterList);  
//		}
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

//			newHtml.append("<input type='hidden' id='chat_token' value='"+db.getChatToken()+"'/>");
//			newHtml.append("<div id='chat_tab'>");
//			newHtml.append("<input type='hidden' id='chatroomId' value='GLOBAL'>");
//			
//			newHtml.append("<div id='chat_form_wrapper'>");
//			newHtml.append("<form id='chat_form'>");
//			newHtml.append("<div class='chat_form_input'>");
//			newHtml.append("<input id='chat_input' type='text' autocomplete='off'>");
//			newHtml.append("</div>");
//			newHtml.append("<div class='chat_form_submit'>");
//			newHtml.append("<input id='chat_submit' type='button' onclick='submitMessage(event)'>");
//			newHtml.append("</div>");
//			newHtml.append("</form>");
//			newHtml.append("</div>");
//			
//			
//			newHtml.append("<div id='chat_messages' style='height:150px; max-height:150px'></div>");
//			newHtml.append("</div>");
//
//			newHtml.append("<script type='text/javascript' src='/odp/javascript/newchat.js?v=2'></script>");
			
			/// The 2D Map...
//			newHtml.append("<div id=\"viewportcontainer\" class=\"vpcontainer\">");
//			newHtml.append("<div id=\"menu\" class=\"menuContainer\" style=\"visibility: hidden;\"></div>");
//			newHtml.append("<div id=\"viewport\" class=\"vp\">");
//			newHtml.append("<div id=\"grid\" class=\"grid\">");
//			newHtml.append("<div id=\"ui-layer\" class=\"uiLayer\"></div>");
//			newHtml.append("<div id=\"cell-layer\" class=\"cellLayer\"></div>");
//			newHtml.append("<div id=\"ground-layer\" class=\"groundLayer\"></div>");
//			newHtml.append("<div id=\"object-layer\" class=\"objectLayer\"></div>");
//			newHtml.append("</div>");
//			newHtml.append("</div>");
//			newHtml.append("</div>");
//			newHtml.append("<button type=\"button\" onclick=\"openMenu()\">Menu</button>");
//			newHtml.append("<button type=\"button\" onclick=\"mapPlow(event)\">Plow</button>");
//			newHtml.append("<button type=\"button\" onclick=\"mapPlaceHouse(event)\" style=\"position:relative\">Place House</button>");
//			newHtml.append("<button type=\"button\" onclick=\"mapPlaceCity(event)\" style=\"position:relative\">Place City</button>");
//			newHtml.append("<center><p id=\"selectedObjects\" class=\"selectedObjectList\"></p></center>");
//			newHtml.append("<script type=\"text/javascript\" src=\"/odp/javascript/Sandbox.js\"></script>");
//			newHtml.append("<script>");
//			newHtml.append("var mapData = '" + GridMapService.buildNewGrid(123456,20,20,2).toString() + "';");
//			newHtml.append("$(document).on(\"click\", \"#somebutton\", function() { pressedButton(); });");
//			newHtml.append("</script>");
			
			return updateHtmlContents("#test-panel", newHtml.toString());
		}
		return "";
	}
	
	public String updateGlobalNavigationMap()
	{
		StringBuilder html = new StringBuilder();
		
		if (CommonChecks.checkCharacterIsIncapacitated(character))
		{
			html.append("<script type='text/javascript'>");
			html.append("	viewBannerDefault();");
			html.append("</script>");
		}
		else
		{
			List<CachedEntity> paths = null;
			List<CachedEntity> destLocations = null;
			CachedEntity location = db.getRootLocation(this.location);
			if (location==null || location.equals(this.location))
			{
				location = this.location;
				paths = this.paths;
				destLocations = this.destLocations;
			}
			else
			{
				// Load the paths and destLocations for the root location
				paths = db.getPathsByLocation_PermanentOnly(location.getKey());
				
				// Go through the paths and get the destination locations pooled
				for(CachedEntity path:paths)
					db.pool.addToQueue(path.getProperty("location1Key"), path.getProperty("location2Key"));
				
				db.pool.loadEntities();

				destLocations = new ArrayList<>();
				for(CachedEntity path:paths)
				if (GameUtils.equals(location.getKey(), path.getProperty("location1Key")))
					destLocations.add(db.pool.get(path.getProperty("location2Key")));
				else if (GameUtils.equals(location.getKey(), path.getProperty("location2Key")))
					destLocations.add(db.pool.get(path.getProperty("location1Key")));
				else
					destLocations.add(null);
			}
			
			
			Long shiftX = (Long)location.getProperty("mapComponentX");
			Long shiftY = (Long)location.getProperty("mapComponentY");
	
			if (shiftX!=null && shiftY!=null && "Global".equals(location.getProperty("mapComponentType")))
			{
					
				addGlobalNavigationMapEntry(html, location, location, null, shiftX, shiftY);
				
				if (paths!=null)
					for(int i = 0; i<paths.size(); i++)
					{
						CachedEntity path = paths.get(i);
						CachedEntity destLocation = destLocations.get(i);
						
						if (destLocation==null) continue;
						
						String type = (String)destLocation.getProperty("mapComponentType");
						if ("Global".equals(type))
						{
							addGlobalNavigationMapEntry(html, location, destLocation, path, shiftX, shiftY);
						}
						
					}
			}
			else
			{
				if (CommonChecks.checkLocationIsRootLocation(location))
					html.append("<p>'"+location.getProperty("name")+"' is not mapped yet. Complain to a content dev!</p>");
				else
					html.append("<p>You might be too deep to easily navigate away. Try coming out of wherever you are.</p>");
				html.append("<input type='hidden' id='blank-global-navigation' value='true'/>");
			}
		}
		return updateHtmlContents(".map-contents", html.toString());
	}
	
	private void addGlobalNavigationMapEntry(StringBuilder html, CachedEntity startLocation, CachedEntity location, CachedEntity path, Long shiftX, Long shiftY)
	{
		String imageUrl = GameUtils.getResourceUrl((String)location.getProperty("mapComponentImage"));
		if (imageUrl==null) imageUrl = GameUtils.getResourceUrl("images/overheadmap/unavailable1.png");
		Long positionX = (Long)location.getProperty("mapComponentX");
		Long positionY = (Long)location.getProperty("mapComponentY");
		
		if (positionX==null || positionY==null || imageUrl==null)
			return;
		
		positionX-=shiftX;
		positionY-=shiftY;
		
		positionX = positionX*100;
		positionY = positionY*100;
		
		
		html.append("<div class='overheadmap-cell-container-base'>"); 
		if (path!=null)
		{
			Long currentPositionX = (Long)startLocation.getProperty("mapComponentX");
			Long currentPositionY = (Long)startLocation.getProperty("mapComponentY");
			String travelLine = "";
			if (currentPositionX!=null && currentPositionY!=null)
			{
				Long seconds = db.getPathTravelTime(path, character);
				travelLine = "drawTravelLine(event, 0, 0,"+(positionX)+", "+(positionY)+", "+seconds+");";
			}
			html.append("		<a onclick='"+travelLine+" doGoto(event, "+path.getId()+", false);' class='path-overlay-link overheadmap-cell-container' style='left:"+positionX+"px; top:"+positionY+"px;'>");
		}
		else
			html.append("		<a onclick='viewLocalNavigation(event)' class='path-overlay-link overheadmap-cell-container' style='left:"+positionX+"px; top:"+positionY+"px;'>");
		html.append("			<div class='overheadmap-cell-label-container'>");
		html.append("				<div class='label'>"+location.getProperty("name")+"</div>");
		html.append("			</div>");
		html.append("			<img src='"+imageUrl+"'/>");
		html.append("		</a>");
		html.append("</div>");
	}
	
	
	protected String getSideBannerLinks()
	{
		StringBuilder newHtml = new StringBuilder();
		
		newHtml.append("<a id='thisLocation-button' class='path-overlay-link' onclick='makeIntoPopup(\".this-location-box\")' style='right:0px;top:0px;'><img alt='Location actions' src='https://initium-resources.appspot.com/images/ui/magnifying-glass2.png' style='max-width:32px'></a>");			
		newHtml.append("<a id='navigation-button' class='path-overlay-link' onclick='makeIntoPopup(\".navigation-box\")' style='right:0px;top:32px;'><img alt='Navigation' src='https://initium-resources.appspot.com/images/ui/compass1.png' style='max-width:32px'></a>");			
		newHtml.append("<a id='globe-navigation-button' class='path-overlay-link' onclick='viewGlobeNavigation()' style='right:4px;top:74px;'><img alt='Global navigation' src='https://initium-resources.appspot.com/images/ui/navigation-map-icon2.png' style='max-width:32px'></a>");			
		newHtml.append("<a id='local-navigation-button' class='path-overlay-link' onclick='viewLocalNavigation()' style='right:4px;top:108px;'><img alt='Local navigation' src='https://initium-resources.appspot.com/images/ui/navigation-local-icon1.png' style='max-width:32px'></a>");			
//		newHtml.append("<a id='guard-button' class='path-overlay-link' onclick='viewGuardSettings()' style='right:4px;top:142px;'><img alt='Guard settings' src='https://initium-resources.appspot.com/images/ui/guardsettings1.png' style='max-width:32px'></a>");
		
		return newHtml.toString();
	}
	
	public String updateInBannerOverlayLinks()
	{
		StringBuilder newHtml = new StringBuilder();
		newHtml.append("<script type='text/javascript'>window.singleLeavePathId=null;</script>");
		if (CommonChecks.checkCharacterIsInCombat(character)==false)
		{
			// Check if we should add the show loot popup button
			if (CommonChecks.checkCharacterIsIncapacitated(character)==false && 
					CommonChecks.checkLocationIsCombatSite(location) && 
					CommonChecks.checkCharacterIsInCombat(character)==false)
			{
				newHtml.append(getHtmlForInBannerLinkCentered(30, 50, "Show loot", "showLootPopup()"));				
			}
			
			newHtml.append(getSideBannerLinks());
			
			loadPathCache();

			boolean noOverlayLinks = true;
			for(int i = 0; i<paths.size(); i++)
			{
				CachedEntity path = paths.get(i);
				CachedEntity destLocation = destLocations.get(i);
				Integer pathEnd = pathEnds.get(i);
					
				String destLocationName = (String)destLocation.getProperty("name");
	
				String overlayCoordinates = (String)path.getProperty("location"+pathEnd+"OverlayCoordinates");
				if (overlayCoordinates==null || overlayCoordinates.matches("\\d+x\\d+")==false)
					continue;
				
				noOverlayLinks = false;
				
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

			if (CommonChecks.checkCharacterIsIncapacitated(character)){
				String onclick = null;				
				if (CommonChecks.checkCharacterIsDead(character))
					onclick = "newCharacterFromDead()";
				if (CommonChecks.checkCharacterIsUnconscious(character))
					onclick = "newCharacterFromUnconscious()";
				
				newHtml.append(getHtmlForInBannerLinkCentered(70, 50, "Spawn a new character", onclick));
				
			}
			else
			{
				// Look at the paths to see if there is only one permanent path away from this location. If so, we'll use that
				// and put it on the banner to go back easily...
				Integer onlyOnePathIndex = null;
				for(int i = 0; i<paths.size(); i++)
				{
					if ("Permanent".equals(paths.get(i).getProperty("type")))
					{
						if (onlyOnePathIndex==null)
						{
							onlyOnePathIndex = i;
						}
						else
						{
							// If we found more than one, then just get out and nevermind.
							onlyOnePathIndex = null;
							break;
						}
					}
				}
				// Here we'll add a default overlay link if there is only 1 path and there are no other overlay links setup
				if (paths.size()==1 || onlyOnePathIndex!=null)
				{
					if (paths.size()==1) onlyOnePathIndex = 0;
					CachedEntity path = paths.get(onlyOnePathIndex);
					CachedEntity destLocation = destLocations.get(onlyOnePathIndex);
					Integer pathEnd = pathEnds.get(onlyOnePathIndex);
						
					String destLocationName = (String)destLocation.getProperty("name");
					
					
					double topDbl = 150;
					double leftDbl = 728/2;
					int topInt = new Double(topDbl/211d*100).intValue();
					int leftInt = new Double(leftDbl/728d*100).intValue();
			
	
					String buttonCaption = "Head towards "+destLocationName;
					String buttonCaptionOverride = (String)path.getProperty("location"+pathEnd+"ButtonNameOverride");
					String overlayCaptionOverride = (String)path.getProperty("location"+pathEnd+"OverlayText");
					if (buttonCaptionOverride!=null && buttonCaptionOverride.trim().equals("")==false)
						buttonCaption = buttonCaptionOverride;
					if (overlayCaptionOverride!=null && overlayCaptionOverride.trim().equals("")==false)
						buttonCaption = overlayCaptionOverride;

					newHtml.append("<script type='text/javascript'>window.singleLeavePathId=").append(paths.get(onlyOnePathIndex).getId()).append(";</script>");

					String onclick = "doGoto(event, window.singleLeavePathId, true);";				
					
					newHtml.append(getHtmlForInBannerLinkCentered(topInt, leftInt, buttonCaption, onclick));
					
				}
				
				if (CommonChecks.checkLocationIsGoodRestSite(location))
				{
					newHtml.append(getHtmlForInBannerLinkCentered(60, 50, "Rest", "doRest();"));
				}
				
				
	
				if (CommonChecks.checkLocationIsCombatSite(location)==false)
				{
					if (CommonChecks.checkLocationIsCampSite(location))
					{
						newHtml.append(getHtmlForInBannerLink(50, 46, "<span id='defendCampsiteBannerButton' style='padding:5px;z-index:2000002;display:none;' title='This is the same as clicking the Defend button below.'>Defend</span>", "window.btnDefendCamp.click();$(this).hide();"));
						String js = 
								"<script type='text/javascript'>" +
								"setTimeout(function(){" +
								"window.btnDefendCamp = $('.v3-main-button[shortcut=68]');" +
								"if (window.btnDefendCamp.length>0)" +
								"{" +
								"	$('#defendCampsiteBannerButton').show();" +
								"	if (window.btnDefendCamp.length>1) window.btnDefendCamp = $(window.btnDefendCamp[0]);" +
								"}" +
								"}, 500);" +
								"</script>";
						newHtml.append(js);
					}
					//	newHtml.append(getHtmlForInBannerLink(50, 45, "<span style='padding:5px;z-index:2000002;'>Explore</span>", "doExplore(event)"));
				}
				else if (CommonChecks.checkCharacterIsIncapacitated(character))
				{
					// Do nothing, we don't want to show any overlay links
				}
				else
				{
					newHtml.append(getHtmlForInBannerLinkCentered(50, 50, "<span id='leaveAndForgetBannerButton' style='padding:5px;z-index:2000002;display:none;' title='This is the same as clicking the Leave and Forget button below.'>Leave and forget</span>", "window.btnLeaveAndForget.click()"));
					String js = 
							"<script type='text/javascript'>" +
							"setTimeout(function(){" +
							"window.btnLeaveAndForget = $('.v3-main-button[shortcut=70]');" +
							"if (window.btnLeaveAndForget.length>0)" +
							"{" +
							"	$('#leaveAndForgetBannerButton').show();" +
							"	if (window.btnLeaveAndForget.length>1) window.btnLeaveAndForget = $(window.btnLeaveAndForget[0]);" +
							"}" +
							"}, 500);" +
							"</script>";
					newHtml.append(js);
				}
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
			
			newHtml.append(getHtmlForInBannerLinkCentered(45, 40, "<img src='"+leftIcon+"' alt='Left Hand' class='combat-button' />", "doCombatAttackLeftHand(event)", "1", 49));
			newHtml.append(getHtmlForInBannerLinkCentered(45, 60, "<img src='"+rightIcon+"' alt='Right Hand' class='combat-button' />", "doCombatAttackRightHand(event)", "2", 50));
			newHtml.append(getHtmlForInBannerLinkCentered(70, 50, "<span style='padding:5px;z-index:2000002;'>RUN!</span>", "doCombatEscape(event)", "3", 51));
		}
		
		
		if (CommonChecks.checkCharacterIsIncapacitated(character)==false && 
				CommonChecks.checkLocationIsCombatSite(location)==false &&
				CommonChecks.checkLocationIsCampSite(location)==false &&
				CommonChecks.checkLocationIsInstance(location)==false && 
				CommonChecks.checkCharacterIsBusy(character)==false)
		{
			newHtml.append(getHtmlForInBannerLinkCentered(50, 50, "Explore", "doExplore(event);"));
		}
		
		
		
		return updateHtmlContents("#banner-text-overlay", newHtml.toString());
	}
	
	protected String getHtmlForInBannerLink(double top, double left, String buttonCaption, String onclickJs)
	{
		return "<a onclick='"+onclickJs.replace("'", "\\'")+"' class='path-overlay-link' style='top:"+top+"%;left: "+left+"%;'>"+buttonCaption+"</a>";
		
	}

	protected String getHtmlForInBannerLinkCentered(double top, double left, String buttonCaption, String onclickJs)
	{
		return getHtmlForInBannerLinkCentered(top, left, buttonCaption, onclickJs, null, null);
	}
	
	protected String getHtmlForInBannerLinkCentered(double top, double left, String buttonCaption, String onclickJs, String shortcutKeyName, Integer shortcutCode)
	{
		String shortcutKeyHtml = "";
		if (shortcutKeyName!=null)
			shortcutKeyHtml = "<span style='top:-8px; left:-16px;' class='shortcut-key'>("+shortcutKeyName+")</span>";
		
		String shortcutCodeHtml = "";
		if (shortcutCode!=null)
			shortcutCodeHtml = " shortcut='"+shortcutCode+"' ";
		
		return "<div style='position:absolute;top:"+top+"%;left:"+left+"%;'><a "+shortcutCodeHtml+" onclick='"+onclickJs.replace("'", "\\'")+"' class='path-overlay-link' style='position:relative; margin-left:-50%; margin-top:-50%;'>"+shortcutKeyHtml+""+buttonCaption+"</a></div>";
	}

	public String updateButtonList(boolean showHidden){
		
		if (CommonChecks.checkCharacterIsIncapacitated(character))
			return updateButtonList_Incapacitated();
		else if (cs.isInCombat(character))
			return updateButtonList_CombatMode();
		else
		{
			loadPathCache(showHidden);
			return updateButtonList_NormalMode();
		}
	}
	
	public String updateButtonList()
	{
		return updateButtonList(false);
	}
	
	protected String updateButtonList_NormalMode()
	{
		StringBuilder newHtml = new StringBuilder();

//		newHtml.append("<div class='main-buttonbox' style='display:block'>");
//		newHtml.append("</div>");
		
		newHtml.append("<div class='main-buttonbox v3-window3 this-location-box'>");
		newHtml.append("<h4>This Location</h4>");
		
		newHtml.append("<div id='in-button-list-location-description'>");
		String desc = (String)location.getProperty("description");
		if (desc==null) desc = "";
		newHtml.append(desc);
		newHtml.append("</div>");
		
		
		newHtml.append("<a id='main-explore' href='#' class='v3-main-button' shortcut='69' onclick='doExplore(event, false)'><span class='shortcut-key'>(E)</span>Explore "+location.getProperty("name")+"</a>");
		newHtml.append("<br>");
		newHtml.append("<a id='main-explore-ignorecombatsites' class='v3-main-button' href='#' shortcut='87' onclick='doExplore(event, true)'><span class='shortcut-key'>(W)</span>Explore (no old sites)</a>");
		if (CommonChecks.checkLocationIsGoodForNaturalResource(location)) 
		{
			newHtml.append("<br>");
			newHtml.append("<a id='main-explore' href='#' class='v3-main-button' onclick='doExplore(event, false, true)'><span class='shortcut-key'></span>Find Natural Resources</a>");
		}
		
		newHtml.append("<br>");
		newHtml.append("<div id='main-merchantlist' class='v3-main-button-half' onclick='loadLocationMerchants()' shortcut='83'>");
		newHtml.append("<span class='shortcut-key'> (S)</span>Nearby stores");
		newHtml.append("</div>");

		newHtml.append("<div id='main-itemlist' class='v3-main-button-half' onclick='loadLocationItems()' shortcut='86'>");
		newHtml.append("<span class='shortcut-key'> (V)</span>Nearby items");
		newHtml.append("</div>");
		
		newHtml.append("<br>");
		newHtml.append("<div id='main-characterlist' class='v3-main-button-half' onclick='loadLocationCharacters()' shortcut='66'>");
		newHtml.append("<span class='shortcut-key'> (B)</span>Nearby characters");
		newHtml.append("</div>");
		
		newHtml.append("<div class='v3-main-button-half'>");
		newHtml.append("<span class='shortcut-key'> </span>Nearby territory");
		newHtml.append("</div>");
					
		newHtml.append("<br>");
		
		
		if ("CampSite".equals(location.getProperty("type")))
		{
			newHtml.append("<a href='#' onclick='doCampDefend()' class='v3-main-button' shortcut='68'><span class='shortcut-key'>(D)</span>Defend</a>");
			newHtml.append("<br>");
		}

		if ("RestSite".equals(location.getProperty("type")) || "CampSite".equals(location.getProperty("type")))
		{
			newHtml.append("<a href='#' class='v3-main-button' shortcut='82' onclick='doRest()'><span class='shortcut-key'>(R)</span>Rest</a>");
			newHtml.append("<br>");
		}

		if (location.getProperty("name").toString().equals("Aera Inn"))
		{
			newHtml.append("<a class='v3-main-button' onclick='doDrinkBeer(event)'>Drink Beer</a>");
			newHtml.append("<br>");
		}

		if (getGroup()!=null && location.getProperty("ownerKey")!=null && ((Key)location.getProperty("ownerKey")).getId() == user.getKey().getId())
		{
			newHtml.append("<a href='#' class='v3-main-button' onclick='giveHouseToGroup()'>Give this property to your group</a>");
			newHtml.append("<br>");
		}
		
		if (db.isCharacterAbleToCreateCampsite(db.getDB(), character, location))
		{
			if("RestSite".equals(location.getProperty("type")))
				newHtml.append("<a href='#' class='v3-main-button' onclick='createCampsite()'>Create a campsite here</a>");
			else
				newHtml.append("<a href='#' class='v3-main-button' shortcut='82' onclick='createCampsite()'><span class='shortcut-key'>(R)</span>Create a campsite here</a>");
			newHtml.append("<br>");
		}

		if ("CityHall".equals(location.getProperty("type")))
		{
			newHtml.append("<a href='#' class='v3-main-button' onclick='buyHouse(event)'>Buy a new house</a>");
			newHtml.append("<br>");
		}
		
		if (user!=null && GameUtils.equals(location.getProperty("ownerKey"), user.getKey()))
		{
			newHtml.append("<a onclick='createMapToHouse(event)' class='v3-main-button'>Create map to this house</a><br/>");
			newHtml.append("<a onclick='renamePlayerHouse(event)' class='v3-main-button'>Rename this house</a>");
		}

		newHtml.append("</div>");
		newHtml.append("<div class='main-buttonbox v3-window3 navigation-box'>");
		newHtml.append("<h4>Navigation</h4>");

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

			newHtml.append("<div class='location-link-"+destLocation.getId()+"'>");
			
				
			String destLocationName = (String)destLocation.getProperty("name");
			
			String buttonCaption = "Head towards "+destLocationName;
			if("Script".equals(destLocation.getKind()))
			{
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

//			if ("PlayerHouse".equals(path.getProperty("type")))
//			{
//				if (user!=null && Boolean.TRUE.equals(user.getProperty("premium")))
//				{/*Simpler if logic this way*/}
//				else
//					newHtml.append("<p style='text-align:center;' title='Save this link to your house. This is a temporary workaround'>https://www.playinitium.com/ServletCharacterControl?type=goto&pathId="+path.getKey().getId()+"</p>");
//			}


			
			if("Script".equals(destLocation.getKind()))
			{
				// Direct location is only thing we support right now, in terms of paths.
				if(GameUtils.enumEquals(destLocation.getProperty("type"), ScriptType.directLocation))
				{
					String title = (String)destLocation.getProperty("description");
					if(title != null && title.length() > 0) title = "title='" + WebUtils.htmlSafe(title) + "'";
					else title = "";

					newHtml.append("<a href='#' onclick='doTriggerLocation(event, "+destLocation.getId()+", " + location.getId() + ")' class='v3-main-button' " + title + " "+shortcutPart+" >"+shortcutKeyIndicatorPart+buttonCaption+"</a>");					
				}
			}
			else if ("CombatSite".equals(location.getProperty("type")) && "CombatSite".equals(destLocation.getProperty("type"))==false)
			{
				newHtml.append("<a href='#' onclick='doGoto(event, "+path.getKey().getId()+")' class='v3-main-button' "+shortcutPart+" >"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
				newHtml.append("<br>");
				newHtml.append("<a href='#' onclick='doLeaveAndForgetCombatSite(event, "+path.getKey().getId()+")' class='v3-main-button' shortcut='70' "+onclick+"><span class='shortcut-key'>(F)</span>Leave this site and forget about it</a>");
				newHtml.append("<br>");
			}
			else if ("CombatSite".equals(destLocation.getProperty("type"))) {
				newHtml.append("<a class='main-forgetPath' onclick='doForgetCombatSite(event,"+destLocation.getKey().getId()+")'>X</a><a onclick='doGoto(event, "+path.getKey().getId()+")' class='v3-main-button' "+shortcutPart+" "+onclick+">"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
				forgettableCombatSites++;
				String destLocationKeyId = String.valueOf(destLocation.getKey().getId());
				forgettableCombatSiteList.append(destLocationKeyId+",");
			}
			else if ("BlockadeSite".equals(destLocation.getProperty("type")) || defensiveStructureAllowed)
				newHtml.append("<a href='#' class='main-button-icon' onclick='doGoto(event, "+path.getKey().getId()+", true)'><img src='https://initium-resources.appspot.com/images/ui/attack1.png' title='This button allows you to travel to this location with the intent to attack any player-made defences without a confirmation' border=0/></a><a href='#' onclick='doGoto(event, "+path.getKey().getId()+")' class='v3-main-button' "+shortcutPart+" >"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
			else if ("CollectionSite".equals(location.getProperty("type")))
			{
				newHtml.append("<br>");
				newHtml.append("<a href='#' onclick='doLeaveAndForgetCombatSite(event, "+path.getKey().getId()+")' class='v3-main-button' shortcut='70' "+onclick+"><span class='shortcut-key'>(F)</span>Leave this site and forget about it</a>");
			}
			// If we're looking at a player-house path, but we're not actually INSIDE the player house currently
			else if (GameUtils.equals(location.getProperty("ownerKey"), null) && "PlayerHouse".equals(path.getProperty("type")))
			{
				newHtml.append("<a href='#' class='main-forgetPath' onclick='deletePlayerHouse(event, "+path.getId()+")'>X</a><a onclick='doGoto(event, "+path.getKey().getId()+")' class='v3-main-button' "+shortcutPart+" "+onclick+">"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
			}
			else
				newHtml.append("<a href='#' onclick='doGoto(event, "+path.getKey().getId()+")' class='v3-main-button' "+shortcutPart+" >"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
	//		newHtml.append("<a href='ServletCharacterControl?type=goto&pathId="+path.getKey().getId()+"' class='v3-main-button' "+shortcutPart+"  "+onclick+">"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
			newHtml.append("</div>");

		}
		
		if (location.getProperty("defenceStructure")!=null)
			newHtml.append("<a onclick='attackStructure()' shortcut='65' class='v3-main-button'><span class='shortcut-key'>(A)</span>Attack this structure's defenders</a>");
			

		
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
		newHtml.append("<a onclick='doCombatEscape(event)' class='v3-main-button' shortcut='51'><span class='shortcut-key'>(3)</span>Try to run away</a>");
		
		return updateHtmlContents("#main-button-list", newHtml.toString());
	}
	
	protected String updateButtonList_Incapacitated()
	{
		StringBuilder newHtml = new StringBuilder();
		
		if (CommonChecks.checkCharacterIsDead(character))
			newHtml.append("<a onclick='newCharacterFromDead()' class='v3-main-button'>Spawn a new character</a>");
		if (CommonChecks.checkCharacterIsUnconscious(character))
			newHtml.append("<a onclick='newCharacterFromUnconscious()' class='v3-main-button'>Spawn a new character</a>");
		
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
		return updateLocationJs(false);
	}	
	
	public String updateLocationJs(boolean refreshChat)
	{
		StringBuilder js = new StringBuilder();
		
		js.append("clearMakeIntoPopup();");
		
		js.append("window.bannerUrl = '"+getLocationBanner()+"';");
		js.append("window.previousBannerUrl = null;");
		
		if (refreshChat==false)
		{
			js.append("if (window.newChatIdToken!='"+db.getChatToken()+"')");
			js.append("{");
			
		}
		js.append("		window.newChatIdToken= '"+db.getChatToken()+"';");
		js.append("		$('.chat_messages').html('');");
		js.append("		messager.reconnect('https://eventserver.universeprojects.com:8080/', window.newChatIdToken);");
	   	
		
		if (refreshChat==false)
			js.append("}");

		js.append("if (isAnimatedBannersEnabled()==false && bannerUrl.indexOf('.gif')>0)");
		js.append("bannerUrl = 'https://initium-resources.appspot.com/images/banner---placeholder.gif';");
		js.append("else if (isBannersEnabled()==false)");
		js.append("bannerUrl = 'https://initium-resources.appspot.com/images/banner---placeholder.gif';");
		js.append("else if (bannerUrl=='' || bannerUrl == 'null')");
		js.append("bannerUrl = 'https://initium-resources.appspot.com/images/banner---placeholder.gif';");

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

		if (CommonChecks.checkCharacterIsUnconscious(character))
		{
			js.append("setBannerOverlayText('YOU&#39;RE INCAPACITATED!', 'Ok so you&#39;re not dead yet, but all of your inventory has been dropped at the site where you fell. If you&#39;re a premium member, it&#39;s POSSIBLE to be rescued if help comes in time.', true);");
			
		}
		else if (CommonChecks.checkCharacterIsDead(character))
		{
			js.append("setBannerOverlayText('YOU&#39;RE DEAD!', 'Ok so you&#39;re dead. What does this mean? It means you have lost all of your stuff, all of your progress you&#39;re essentially starting a new character from the beginning. Please be careful next time!', true);");
		}
		
		if (CommonChecks.checkCharacterIsIncapacitated(character))
			js.append("$('#campsPanel').hide();");
		else
			js.append("$('#campsPanel').show();");
		
		
		if (CommonChecks.checkCharacterIsInCombat(character))
			js.append("if (window.onCombatBegin) onCombatBegin();");
		else
			js.append("if (window.onCombatComplete && $('body').attr('bannerstate')=='combat') onCombatComplete();");
		
		
		// Here we'll update the button bar
		//addType1Button(position, imageUrl, shortcut, javascript)
		js.append("addType1Button(1, 'https://initium-resources.appspot.com/images/ui/magnifying-glass2.png', 'E', 'viewThisLocationWindow()');");
		js.append("addType1Button(2, 'https://initium-resources.appspot.com/images/ui/magnifying-glass2.png', 'E', 'viewThisLocationWindow()');");
		
		
		return updateJavascript("ajaxJs", js.toString());
	}
	
	public String updateActivePlayerCount()
	{
		return updateHtmlContents("#activePlayerCount", db.getActivePlayers()+"");
	}
	
	public String updateButtonBar()
	{
		if(cs.isInCombat(character) || CommonChecks.checkCharacterIsIncapacitated(character)) return updateHtmlContents("#buttonbar", "");
		return updateHtmlContents("#buttonbar", HtmlComponents.generateButtonBar(character));
	}

	public String updateLocationDescription()
	{
		StringBuilder html = new StringBuilder();
		String desc = (String)location.getProperty("description");
		if (desc==null) desc = "";
		
		if (CommonChecks.checkCharacterIsDead(character))
		{
			desc += "If you were carrying some good stuff when you died, all that stuff is now lying beside your corpse. "; 
			desc += "When you start a new character, you could try to recover it and any gold you might have had on you. "; 
			desc += "If it's in a tricky spot (I mean, you were killed right? Probably pretty dangerous), then you MIGHT be  "; 
			desc += "able to enlist some help from people in chat with your new character. Be careful though! Not everyone ";
			desc += "can be trusted! ";
			
		}
		else if (CommonChecks.checkCharacterIsUnconscious(character))
		{
			desc += "It's not too late for you, you can be saved still! In order to be saved, you must be picked up by someone ";
			desc += "and taken to a rest area and then dropped there. As soon as you are dropped, if you're still alive, you will ";
			desc += "revive with 1 hitpoint. <strong>This can only be done if you AND your rescuer are both premium members. You ";
			desc += "also cannot use an alt, it MUST be another player that rescues you.</strong>";

			if (CommonChecks.checkUserIsPremium(user)==false)
			{
				desc += "<p class='highlightbox-red'>";
				desc += "You are not a premium member, but it is not too late to become one. If you ";
				desc += "become a premium member, someone could pick up your body and bring you to a rest area to save you. "; 
				desc += "<strong style='color:red'>Please note that becoming a premium member does NOT guarantee that you will be saved. It just means ";
				desc += "that someone COULD save you if you don't die before they get you to a rest area.</strong> ";
				desc += "</p> ";
				desc += "<center><a onclick='viewProfile()' class='v3-main-button' style='color:#FFFFFF'>Become a premium member for $5</a></center>";
			}
			else
			{
				desc += "<p class='highlightbox-green'>";
				desc += "You ARE a premium member and so it is possible for somebody to come and save you!";
				desc += "</p>";
			}
			
		}

		

		// Here we add the camp situation
		html.append(desc);
		if (CommonChecks.checkCharacterIsIncapacitated(character)==false && cs.isInCombat(character)==false)
		{
			html.append("<div class='main-description'>");
			if(location != null && db.isCharacterAbleToCreateCampsite(ds, character, location))
				html.append("This location could host up to " + location.getProperty("supportsCamps") + " camps.");
			else
				html.append("This location is not suitable for a camp.");
			html.append("</div>");
		}
		
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
//		if (location.getProperty("territoryKey")!=null)
//		{
//			return HtmlComponents.generateTerritoryView(character, getTerritoryOwningGroup(), getTerritory());
//		}

		return updateHtmlContents("#locationScripts", "");
	}
	
	public String updatePartyView()
	{
		StringBuilder newHtml = new StringBuilder();

		if (isInParty())
		{
			newHtml.append("<div class='boldbox'>");
			newHtml.append("<a onclick='doLeaveParty(event)' style='float:right'>Leave Party</a>");
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
				Collections.sort(party, new Comparator<CachedEntity>(){
					@Override
					public int compare(CachedEntity c1, CachedEntity c2){
						String partyCharacter1 = (String)c1.getProperty("name");
						String partyCharacter2 = (String)c2.getProperty("name");
						return partyCharacter1.compareTo(partyCharacter2);
					}
				});
				pool.loadEntities();

				for(CachedEntity partyCharacter:party)
				{
					CachedEntity partyUser = pool.get((Key)partyCharacter.getProperty("userKey"));
					boolean isThisMemberTheLeader = false;
					if ("TRUE".equals(partyCharacter.getProperty("partyLeader")))
						isThisMemberTheLeader = true;
					boolean dead = CommonChecks.checkCharacterIsUnconscious(partyCharacter);

					newHtml.append("<div style='display:inline-block;vertical-align:top;'>");
					newHtml.append("<a class='main-item clue' style='width:inherit;' rel='/odp/viewcharactermini?characterId="+partyCharacter.getKey().getId()+"'>");
					newHtml.append(GameUtils.renderCharacterWidget(db.getRequest(), db, partyCharacter, partyUser, true));
					newHtml.append("<br>");
					if (!GameUtils.equals(character.getKey(), partyCharacter.getKey()) && user!=null && 
							GameUtils.equals(user.getKey(), partyUser.getKey())) {
						newHtml.append("<div class='main-item-controls' style='top:0px'>");
						newHtml.append("<a onclick='switchCharacter(event, "+partyCharacter.getKey().getId()+")'>Switch</a>");
						newHtml.append("</div>");
					}

					if (isThisMemberTheLeader)
						newHtml.append("<div class='main-item-controls' style='top:0px;'>(Leader)</div>");
					else if (dead)
					{
						if(GameUtils.equals(character.getProperty("locationKey"), partyCharacter.getProperty("locationKey")))
						{
							newHtml.append("<div class='main-item-controls' style='top:0px'>");
							newHtml.append("<a onclick='collectDogecoinFromCharacter("+partyCharacter.getKey().getId()+")'>Collect "+partyCharacter.getProperty("dogecoins")+" gold</a>");
							newHtml.append("</div>");
						}
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
		// Probably getting rid of this		
		return updateHtmlContents("#collectablesPanel", html.toString());
	}

	public String updateImmovablesPanel(CachedEntity updatedItem)
	{
		if (CommonChecks.checkCharacterIsIncapacitated(character))
			return updateHtmlContents("#immovablesPanel", "");

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
		if (CommonChecks.checkCharacterIsIncapacitated(character))
			return updateHtmlContents("#immovablesPanel", "");

		
		StringBuilder html = new StringBuilder();
		
		loadImmovables();
		
		if (immovables!=null && immovables.isEmpty()==false)
		{
			for(CachedEntity item:immovables)
			{
				String iconUrl = (String)item.getProperty("icon2");
				if (iconUrl==null || iconUrl.trim().length()==0) iconUrl = (String)item.getProperty("icon");
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
	
	public String updateCampsPanel()
	{
		StringBuilder html = new StringBuilder();
		
		if (CommonChecks.checkCharacterIsIncapacitated(character)==false && cs.isInCombat(character)==false)
		{
			html.append("<div class='main-description'>");
			if(location != null && db.isCharacterAbleToCreateCampsite(ds, character, location))
				html.append("This location could host up to " + location.getProperty("supportsCamps") + " camps.");
			else
				html.append("This location is not suitable for a camp.");
			html.append("</div>");
		}
		
		return updateHtmlContents("#campsPanel", html.toString());
	}

	public String updateMonsterCountPanel()
	{
		if(cs.isInCombat(character))
			return updateHtmlContents("#monsterCountPanel", "");

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
				if (maxMonsterCount>10 && 
						GameUtils.booleanEquals(location.getProperty("hideMonsterActivity"), true)==false)
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
		
		if (CommonChecks.checkCharacterIsIncapacitated(character)==false)
			if (CommonChecks.checkLocationIsCombatSite(location) && CommonChecks.checkCharacterIsInCombat(character)==false)
			{
				html.append("<script type='text/javascript'>");
				if (paths!=null && paths.size()==1)
					html.append("	showLootPopup();");
				html.append("</script>");
			}
		
		
		
		return updateHtmlContents("#locationQuicklist", html.toString());
	}
}
