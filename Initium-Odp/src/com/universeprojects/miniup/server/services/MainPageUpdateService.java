package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;

public class MainPageUpdateService extends Service
{
	final private CachedEntity character;
	final private CachedEntity location;
	final private OperationBase operation;
	
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
	public MainPageUpdateService(ODPDBAccess db, CachedEntity character, CachedEntity location, OperationBase operation)
	{
		super(db);
		this.operation = operation;
		this.character = character;
		this.location = location;
	}

	private String updateHtmlContents(String selector, String newHtml)
	{
		if (operation!=null)
			operation.updateHtmlContents(selector, newHtml);
		
		return newHtml;
	}
	
	/**
	 * This will load all the path related caches, but only if we haven't loaded them before for
	 * this particular session.
	 * @return
	 */
	private void loadPathCache()
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
				if (discovery.getProperty("kind").equals("Path"))
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
				
						if ("TRUE".equals(discovery.getProperty("hidden")) && db.getRequest().getParameter("showHiddenPaths")==null)
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
			for(int i = destLocations.size()-1; i>=0; i--)
			{
				if (destLocations.get(i)==null)
				{
					paths.remove(i);
					pathEnds.remove(i);
					destLocations.remove(i);
				}
			}
			
		}
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
	
	/**
	 * This updates the html in the character widget that is in the top left corner of the banner.
	 * 
	 * @param userOfViewer
	 * @param character
	 * @param groupOfCharacter
	 */
	public String updateInBannerCharacterWidget(CachedEntity userOfViewer, CachedEntity groupOfCharacter)
	{
		String newHtml = GameUtils.renderCharacterWidget(db.getRequest(), db, character, userOfViewer, groupOfCharacter, true, true, false, false);
		
		return updateHtmlContents("#inBannerCharacterWidget", newHtml);
	}
	
	
	public String updateInBannerOverlayLinks()
	{
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
	
	
}
