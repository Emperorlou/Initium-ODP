package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.model.GridCell;
import com.universeprojects.miniup.server.model.GridMap;
import com.universeprojects.miniup.server.model.GridObject;

public class GridMapService {

	
	public static double GLOBAL_SCALE = 0.4;

	
	final private ODPDBAccess db;
	final private CachedDatastoreService ds;
	final private CachedEntity location;
	final private int locationWidth;
	final private int locationHeight;
	private boolean initialized = false;
	private List<CachedEntity> locationPresets = null;
	private Map<CachedEntity, Double> naturalItemsMap = null;
	
	public GridMapService(ODPDBAccess db, CachedEntity location)
	{
		this.db = db;
		this.ds = db.getDB();
		this.location = location;
		locationWidth = ((Long)location.getProperty("gridMapWidth")).intValue();
		locationHeight = ((Long)location.getProperty("gridMapHeight")).intValue();
	}
	
	private void initialize()
	{
		if (initialized) return;

		// First get all the preset entities
		List<Key> gridMapPresetKeys = (List<Key>)location.getProperty("gridMapPresets");
		if (gridMapPresetKeys!=null)
		{
			locationPresets = ds.get(gridMapPresetKeys);
		}
		
		naturalItemsMap = new HashMap<>();
		
		// Then get all the ItemDef entities and/or the names of the ItemDefs that the presets require
		// After this, the itemDefKeys will contain keys that are Key types if they were a valid Key type, 
		// as well as keys that are String types if they weren't valid keys. We will be treating the string 
		// types as the names of the itemdefs and resolving them later. 
		Map<Object, Double> itemDefKeys = new HashMap<>();
		if (locationPresets!=null)
			for(CachedEntity locationPreset:locationPresets)
			{
				Map<String, Double> elements = db.getFieldTypeMapEntityDouble(locationPreset, "elements");
				if (elements!=null)
					for(String key:elements.keySet())
					{
						if (db.isKey(key))
						{
							Key realKey = db.keyStringToKey(key);
							Double existingValue = itemDefKeys.get(realKey);
							if (existingValue==null) existingValue = 0d;
							existingValue += elements.get(key);
							
							itemDefKeys.put(realKey, existingValue);
						}
						else
						{
							Double existingValue = itemDefKeys.get(key);
							if (existingValue==null) existingValue = 0d;
							existingValue += elements.get(key);
							
							itemDefKeys.put(key, existingValue);
						}
					}
			}
		// Also add all of the location's elements
		Map<String, Double> elements = db.getFieldTypeMapEntityDouble(location, "gridMapElements");
		if (elements!=null)
			for(String key:elements.keySet())
			{
				if (db.isKey(key))
				{
					Key realKey = db.keyStringToKey(key);
					Double existingValue = itemDefKeys.get(realKey);
					if (existingValue==null) existingValue = 0d;
					existingValue += elements.get(key);
					
					itemDefKeys.put(realKey, existingValue);
				}
				else
				{
					Double existingValue = itemDefKeys.get(key);
					if (existingValue==null) existingValue = 0d;
					existingValue += elements.get(key);
					
					itemDefKeys.put(key, existingValue);
				}
			}
		
		
		
		// Now turn the itemDef keys and names we collected into actual ItemDef entities
		// First do a bulk get for all the actual keys
		List<Key> listOfKeysToFetch = new ArrayList<>();
		for(Object keyObj:itemDefKeys.keySet())
		{
			if (keyObj instanceof Key)
			{
				listOfKeysToFetch.add((Key)keyObj);
			}
		}
		Map<Key, CachedEntity> asMap = ds.getAsMap(listOfKeysToFetch);
		for(Key k:asMap.keySet())
		{
			CachedEntity entity = asMap.get(k);
			Double value = itemDefKeys.get(k);
			
			naturalItemsMap.put(entity, value);
		}
		
		// Now fetch the named keys one by one, unfortunately...
		QueryHelper query = new QueryHelper(ds);
		for(Object keyObj:itemDefKeys.keySet())
		{
			if (keyObj instanceof String)
			{
				String name = (String)keyObj;
				List<CachedEntity> list = query.getFilteredList("ItemDef", "name", name);
				
				if (list!=null)
					for(CachedEntity entity:list)
					{
						Double value = itemDefKeys.get(name);
						naturalItemsMap.put(entity, value/(double)list.size());
					}
			}
		}
		
		initialized = true;
	}
	
	private Random getRandomForTile(int tileX, int tileY)
	{
		long seed = location.getKey().getId();
		seed += (tileY*10000)+(tileX*10);

		if (seed<0) seed += Long.MAX_VALUE;
		
		Random rnd = new Random(seed);
		
		return rnd;
	}
	
	
	private List<CachedEntity> generateNaturalTileItems(Random rnd, int tileX, int tileY)
	{
		initialize();

		
		List<CachedEntity> result = new ArrayList<>();;
		//TODO: if one of the items generated takes up the entire tile then we should skip generating the rest of the items
		for(CachedEntity itemDef:naturalItemsMap.keySet())
		{
			Double odds = naturalItemsMap.get(itemDef);
			if (odds<=0) continue;
			int spawnCount = (int)Math.floor(odds/100);
			double lessThan100SpawnOdds = odds-(spawnCount*100);
			
			
			if (rnd.nextDouble()*100<lessThan100SpawnOdds)
			{
				//TODO: Consider using a fresh Random and adding an index number to the seed so we can regenerate this item without having to generate the whole tile
				result.add(generateItem(rnd, itemDef, tileX, tileY));
			}
			
			if (spawnCount>0)
				spawnCount = rnd.nextInt(spawnCount+1);
			
			for(int i = 0; i<spawnCount; i++)
			{
				//TODO: Consider using a fresh Random and adding an index number to the seed so we can regenerate this item without having to generate the whole tile
				result.add(generateItem(rnd, itemDef, tileX, tileY));
			}
		}
		
		return result;
	}
	
	private CachedEntity generateItem(Random rnd, CachedEntity itemDef, int tileX, int tileY)
	{
		CachedEntity item = db.generateNewObject(rnd, itemDef, "Item", false);
		item.setProperty("gridMapPositionX", tileX);
		item.setProperty("gridMapPositionY", tileY);
		item.setProperty("gridMapCellOffsetX", rnd.nextInt(64));
		item.setProperty("gridMapCellOffsetY", rnd.nextInt(64));
		item.setProperty("containerKey", location.getKey());
		
		return item;
	}
	
	public Map<String, GridObject> generateGridObjects(int tileX, int tileY)
	{
		Random rnd = getRandomForTile(tileX, tileY);
		List<CachedEntity> items = generateNaturalTileItems(rnd, tileX, tileY);

		if (items==null) return null;
		
		Map<String, GridObject> result = new HashMap<>();
		for(int i = 0; i<items.size(); i++)
		{
			CachedEntity item = items.get(i);
			String imageUrl = (String)item.getProperty("GridMapObject:image");
			Integer cellOffsetX = intVal(item.getProperty("gridMapCellOffsetX"));
			Integer cellOffsetY = intVal(item.getProperty("gridMapCellOffsetY"));
			Integer imageWidth = (int)Math.floor(intVal(item.getProperty("GridMapObject:imageWidth"))*GLOBAL_SCALE);
			Integer imageHeight = (int)Math.floor(intVal(item.getProperty("GridMapObject:imageHeight"))*GLOBAL_SCALE);

			
//			double scale = 1-(rnd.nextDouble()*type.scaleVariance);
//			double width = ((double)type.width)*scale*GLOBAL_SCALE;
//			double height = ((double)type.height)*scale*GLOBAL_SCALE;
			
			
			if (imageWidth==null || imageHeight==null) continue;
			
			String generatedKey = "Location:"+location.getKey().getId()+"-X:"+tileX+"-Y:"+tileY+"-Index:"+i;
			
			result.put(generatedKey, new GridObject(
					generatedKey,
					imageUrl,
					"",
					tileX-500+locationWidth/2, tileY-500+locationHeight/2,
					cellOffsetX,
					cellOffsetY,
					(int)(imageWidth / 2), (int)(imageHeight*0.95), (int)(imageWidth), (int)(imageHeight), false, false));
		}
		
		return result;
		
	}
	
	
	public GridCell updateGridCellBackground(GridMap gridMap, int row, int column, String backgroundFile) {
		gridMap.getMap()[row][column].setBackgroundFile(backgroundFile);
		GridCell gridCell = new GridCell(column, row);
		gridCell.setBackgroundFile(backgroundFile);
		return gridCell;
	}

	public GridObject updateGridObjectName(GridMap gridMap, String objectKey, String newName) {
		gridMap.getGridObjects().get(objectKey).setName(newName);
		GridObject gridObject = new GridObject(objectKey);
		gridObject.setName(newName);
		return gridObject;
	}
	
	

//	public static int generateRandomObject(Map<String, GridObject> objectMap, ObjectType[] typeList, int x, int y, double count, long seed)
//	{
//		Random rnd = new Random(seed*(x+1)*(y+1));
//		if (count<0) return 0;
//		if (count<1) 
//		{
//			if (rnd.nextDouble()>=count) 
//				return 0;
//			else
//				count = 1;
//		}
//		
//		for(int index = 0; index<count; index++)
//		{
//			ObjectType type = typeList[rnd.nextInt(typeList.length)];
//			double scale = 1-(rnd.nextDouble()*type.scaleVariance);
//			double width = ((double)type.width)*scale*GLOBAL_SCALE;
//			double height = ((double)type.height)*scale*GLOBAL_SCALE;
//			objectMap.put(type.img + "tempKey:" + x + "-" + y, new GridObject(
//					"tempKey:" + x + "-" + y,
//					type.img,
//					"",
//					x, y,
//					rnd.nextInt(64),
//					rnd.nextInt(64),
//					(int)(width / 2), (int)(height*0.95), (int)(width), (int)(height), false, false));
//		}
//		
//		return (int)count;
//	}
	
	public GridMap buildNewGrid() {

		if (location.getProperty("gridMapWidth")==null || location.getProperty("gridMapHeight")==null)
			return null;
		Integer columnLength = intVal(location.getProperty("gridMapWidth"));
		Integer rowLength = intVal(location.getProperty("gridMapHeight"));
		
		GridCell[][] grid = new GridCell[rowLength][columnLength];
		Map<String, GridObject> objectMap = new HashMap<>();

		// Loop over grid size
		for (int tileX = 500-rowLength/2; tileX < Math.ceil(500d+rowLength.doubleValue()/2); tileX++) {
			for (int tileY = 500-columnLength/2; tileY < Math.ceil(500d+columnLength.doubleValue()/2); tileY++) {
				Random rnd = getRandomForTile(tileX, tileY);
				
				Map<String, GridObject> objects = generateGridObjects(tileX, tileY);
				
				if (objects!=null)
					objectMap.putAll(objects);
				
				// Build background data for coordinate
				grid[tileX-500+rowLength/2][tileY-500+columnLength/2] = new GridCell("images/2d/floor/grass/tile-grass" + rnd.nextInt(7) + ".png",
						tileX-500+rowLength/2, tileY-500+columnLength/2,
						rnd.nextInt(10));
			}
		}

		// Dummy object data for testing 
		int dumpX = 8;
		int dumpY = 12;
		int attachX = -15;
		int attachY = -15;
		int width = 30;
		int height = 30;
		int offsetX = 30;
		int offsetY = 30;
		Random gausRx = new Random();
		Random gausRy = new Random();
		objectMap.put("images/small2/Pixel_Art-Armor-Chest-elvenhunter.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Armor-Chest-elvenhunter.png",
						"Norwood Cloak", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));
		objectMap.put("images/small2/Pixel_Art-Armor-Hardenedleatherboots_Old.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Armor-Hardenedleatherboots_Old.png",
						"Leather Shin Protectors", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30),
						(int) Math.round(gausRy.nextGaussian() * 10 + 30), attachY, attachX, width, height, false, false));
		objectMap.put("images/small/Pixel_Art-Tools-Shovel1.png" + "tempKey:o1",
				new GridObject("o1", "images/small/Pixel_Art-Tools-Shovel1.png",
						"Ogre-Sized Shovel", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));
		objectMap.put("images/small2/Pixel_Art-Gems-Topaz_Perfect.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Gems-Topaz_Perfect.png",
						"Perfect Topaz", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));
		objectMap.put("images/small2/Pixel_Art-Weapon-Energy-Blade.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Weapon-Energy-Blade.png",
						"Energy Blade", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));
		objectMap.put("images/small/Pixel_Art-Weapons-Chain-W_Mace005.png" + "tempKey:o1",
				new GridObject("o1", "images/small/Pixel_Art-Weapons-Chain-W_Mace005.png",
						"Flail of the Desert Prince", dumpY, dumpX, offsetX, (int) Math.round(gausRy.nextGaussian() * 10 + 30), attachY, attachX, width,
						height, false, false));
		objectMap.put("images/small2/Pixel-Art-Armor-Gladiator-gauntlets.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel-Art-Armor-Gladiator-gauntlets.png",
						"Gladiator's Gauntlets", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30),
						(int) Math.round(gausRy.nextGaussian() * 10 + 30), attachY, attachX, width, height, false, false));
		objectMap.put("images/small2/Pixel_Art-Gems-Sapphire_Flawed.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Gems-Sapphire_Flawed.png",
						"Flawed Sapphire", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));
		objectMap.put("images/small/Pixel_Art-Tools-Pick1.png" + "tempKey:o1",
				new GridObject("o1", "images/small/Pixel_Art-Tools-Pick1.png",
						"Orcish Pick", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));
		objectMap.put("images/small2/Pixel_Art-Weapon-Chieftains-Axe.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Weapon-Chieftains-Axe.png",
						"Ogre Chieftain's Axe", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30),
						(int) Math.round(gausRy.nextGaussian() * 10 + 30), attachY, attachX, width, height, false, false));
		objectMap.put("images/small2/Pixel_Art-Armor-Head-Santa-Hat.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Armor-Head-Santa-Hat.png",
						"Fake Santa Hat", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));

		return new GridMap(grid, objectMap);
	}
	
	private Integer intVal(Object obj)
	{
		if (obj==null) return null;
		if (obj instanceof Integer) return (Integer)obj;
		return ((Long)obj).intValue();
	}

	public int getGridWidth()
	{
		Long width = (Long)location.getProperty("gridMapWidth");
		if (width==null) width = 0L;
		return width.intValue();
	}
	
	public int getGridHeight()
	{
		Long height = (Long)location.getProperty("gridMapHeight");
		if (height==null) height = 0L;
		return height.intValue();
	}
	
}
