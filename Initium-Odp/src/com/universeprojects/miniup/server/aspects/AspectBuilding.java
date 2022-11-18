package com.universeprojects.miniup.server.aspects;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.GridMapService;
import com.universeprojects.miniup.server.services.LocationService;

public class AspectBuilding extends ItemAspect
{

	protected AspectBuilding(InitiumObject object)
	{
		super(object);
		
		// Add additional aspect requirements
//		object.addAspect(AspectGridMapObject.class);
	}

	@Override
	public Integer getVersion() {
		return 1;
	}
	
	
	static
	{
		addCommand("BuildingEnter", AspectBuilding.CommandBuildingEnter.class);
	}
	

	public static class CommandBuildingEnter extends Command
	{

		public CommandBuildingEnter(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
		{
			super(db, request, response);
		}

		@Override
		public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {
			String rawKey = parameters.get("itemKey");
			
			CachedEntity entity = GridMapService.generateSingleItemFromProceduralKeyOrUrlSafeKey(db, rawKey);
			InitiumObject building = new InitiumObject(db, entity);
			
			GridMapService gms = new GridMapService(db, db.getEntity(building.getContainerKey()));
			
			gms.convertFromProceduralToDBItem(building);

			if (building.isAspectPresent(AspectBuilding.class) == false) throw new UserErrorMessage("The building you tried to enter was not, in fact, a building.");

			AspectBuilding buildingAspect = building.getAspect(AspectBuilding.class);
			
			if (CommonChecks.checkItemIsAccessible(building.getEntity(), db.getCurrentCharacter()) == false) throw new UserErrorMessage("You're not near this building.");
			
			CachedEntity entrancePath = buildingAspect.getEntrancePath();
			
			if (CommonChecks.checkLocationOrPathIsTemplateMode(entrancePath) == false) {
				db.doCharacterTakePath(ds, db.getCurrentCharacter(), entrancePath, false);
			} else {
				final CachedEntity generatedPath;
				try {
					ds.beginTransaction();
					building.refetch(ds);
					buildingAspect = building.getAspect(AspectBuilding.class);
					LocationService lService = new LocationService(db);
					
					CachedEntity parentLocation = db.getEntity(building.getContainerKey());
					List<CachedEntity> pathAndLocation = lService.generateTemplatePath(parentLocation, entrancePath, buildingAspect.getMaxInstanceDepth());
					generatedPath = pathAndLocation.get(0);
					CachedEntity generatedLocation = pathAndLocation.get(1);
					
					buildingAspect.setNewEntrancePath(generatedPath);
					
					ds.put(pathAndLocation);
					
					ds.commit();
				} finally {
					ds.rollbackIfActive();
				}
				
				db.doCharacterTakePath(ds, db.getCurrentCharacter(), generatedPath);
				
				ds.put(building.getEntity(), generatedPath);
			}
			
			getMPUS().updateFullPage_shortcut(true);
			
		}
		
	}


	public CachedEntity getEntrancePath() {
		return db.getEntity((Key)getProperty("entrance"));
	}

	public int getMaxInstanceDepth() {
		Long maxInstanceDepth = (Long)getProperty("maxInstanceDepth");
		if (maxInstanceDepth == null) maxInstanceDepth = 1l;
		return maxInstanceDepth.intValue();
	}

	public void setNewEntrancePath(CachedEntity newPath) {
		setProperty("entrance", newPath.getKey());
	}	
	
	@Override
	public String get2DViewOverlayHtml() {
		String keyString = GridMapService.determineProceduralKeyOrWebSafeKey(getInitiumObject().getEntity());
		if (getEntrancePath() != null)
			return "<button class='2d-object-overlay' onclick='doEnterBuilding(event, \""+keyString+"\")'>Enter</button>";
		else 
			return "";
	}
	
	
	@Override
	public List<ItemPopupEntry> getItemPopupEntries(CachedEntity currentCharacter)
	{
		ArrayList<ItemPopupEntry> itemPopupEntries = new ArrayList<>();
		
		String keyString = GridMapService.determineProceduralKeyOrWebSafeKey(getInitiumObject().getEntity());
		if (getEntrancePath() != null)
			itemPopupEntries.add(new ItemPopupEntry("Enter", "Enter this building/structure.", "doEnterBuilding(event, '" + keyString + "');"));

		return itemPopupEntries;
	}

	@Override
	public String getPopupTag() {
		return "Enterable Structure";
	}
	

}
