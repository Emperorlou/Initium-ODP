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

	public static class ObjectType
	{
		public String img;
		public int width;
		public int height;
		public double scale;
		public double scaleVariance;
		public boolean flippable;
		
		public ObjectType(String img, int width, int height, double scale, double scaleVariance, boolean flippable)
		{
			this.img = img;
			this.width = width;
			this.height = height;
			this.scale = scale;
			this.scaleVariance = scaleVariance;
			this.flippable = flippable;
		}
	}
	
	public static double GLOBAL_SCALE = 0.4;
	
//	public static ObjectType[] trees = {
//			new ObjectType("trees/tree1.png", 365, 486, 1, 0.2, true),
//			new ObjectType("trees/tree2.png", 307, 581, 1, 0.2, true),
//			new ObjectType("trees/tree3.png", 160, 461, 1, 0.2, true),
//			new ObjectType("trees/tree4.png", 199, 487, 1, 0.2, true),
//			new ObjectType("trees/tree5.png", 165, 445, 1, 0.2, true),
//			new ObjectType("trees/tree6.png", 71, 73, 1, 0.2, true),
//			new ObjectType("https://i.imgur.com/B8sfVYr.png", 960, 540, 1, 0.2, true),
//			new ObjectType("https://i.imgur.com/P6HnmkJ.png", 480, 270, 1, 0.2, true),
//			new ObjectType("https://i.imgur.com/RtroHjA.png", 480, 270, 1, 0.2, true),
//			new ObjectType("https://i.imgur.com/tqxfxrw.png", 480, 270, 1, 0.2, true),
//			
//	};
//	
//	public static ObjectType[] bushes = {
//			new ObjectType("bushes/bush1.png", 130, 86, 1, 0.2, true),
//			new ObjectType("bushes/bush2.png", 112, 103, 1, 0.2, true),
//			new ObjectType("bushes/bush3.png", 113, 100, 1, 0.2, true),
//			new ObjectType("bushes/bush4.png", 112, 104, 1, 0.2, true),
//			new ObjectType("bushes/bush5.png", 94, 94, 1, 0.2, true),
//			new ObjectType("bushes/bush6.png", 156, 100, 1, 0.2, true),
//			new ObjectType("bushes/bush7.png", 107, 105, 1, 0.2, true),
//			new ObjectType("bushes/bush8.png", 98, 102, 1, 0.2, true),
//	};
//	
//	public static ObjectType[] plants = {
//			new ObjectType("plants/mushroom1.png", 33, 37, 1, 0.2, true),
//			new ObjectType("plants/mushroom2.png", 58, 43, 1, 0.2, true),
//			new ObjectType("plants/mushroom3.png", 55, 61, 1, 0.2, true),
//			new ObjectType("plants/mushroom4.png", 34, 38, 1, 0.2, true),
//			new ObjectType("plants/plant1.png", 125, 87, 1, 0.2, true),
//			new ObjectType("plants/plant2.png", 118, 90, 1, 0.2, true),
//			new ObjectType("plants/plant3.png", 120, 98, 1, 0.2, true),
//			new ObjectType("plants/plant4.png", 131, 109, 1, 0.2, true),
//			new ObjectType("plants/plant5.png", 132, 95, 1, 0.2, true),
//			new ObjectType("plants/plant6.png", 132, 116, 1, 0.2, true),
//			new ObjectType("plants/plant7.png", 110, 77, 1, 0.2, true),
//			new ObjectType("plants/plant8.png", 115, 116, 1, 0.2, true),
//			new ObjectType("plants/plant9.png", 128, 86, 1, 0.2, true),
//			new ObjectType("plants/plant10.png", 104, 62, 1, 0.2, true),
//			new ObjectType("plants/plant11.png", 82, 67, 1, 0.2, true),
//			new ObjectType("plants/plant12.png", 47, 60, 1, 0.2, true),
//			new ObjectType("plants/plant13.png", 108, 96, 1, 0.2, true),
//			new ObjectType("plants/plant14.png", 92, 74, 1, 0.2, true),
//			new ObjectType("plants/plant15.png", 103, 82, 1, 0.2, true),
//			new ObjectType("plants/plant16.png", 93, 74, 1, 0.2, true),
//	};
	
	final private ODPDBAccess db;
	final private CachedDatastoreService ds;
	final private CachedEntity location;
	final private long locationWidth;
	final private long locationHeight;
	private boolean initialized = false;
	private List<CachedEntity> locationPresets = null;
	private Map<CachedEntity, Double> naturalItemsMap = null;
	
	public GridMapService(ODPDBAccess db, CachedEntity location)
	{
		this.db = db;
		this.ds = db.getDB();
		this.location = location;
		locationWidth = (Long)location.getProperty("gridMapSizeX");
		locationHeight = (Long)location.getProperty("gridMapSizeY");
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
				spawnCount = rnd.nextInt(spawnCount);
			
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

			Integer columnLength = intVal(location.getProperty("gridMapWidth"));
			Integer rowLength = intVal(location.getProperty("gridMapHeight"));
			
//			double scale = 1-(rnd.nextDouble()*type.scaleVariance);
//			double width = ((double)type.width)*scale*GLOBAL_SCALE;
//			double height = ((double)type.height)*scale*GLOBAL_SCALE;
			
			
			if (imageWidth==null || imageHeight==null) continue;
			
			String generatedKey = "Location:"+location.getKey().getId()+"-X:"+tileX+"-Y:"+tileY+"-Index:"+i;
			
			result.put(generatedKey, new GridObject(
					generatedKey,
					imageUrl,
					"",
					tileX-500+rowLength/2, tileY-500+columnLength/2,
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
				
//				
//				int treeCount = 0;
//				int bushCount = 0;
//				int plantCount = 0;
//				
//				treeCount = generateRandomObject(objectMap, trees, j, i, 0.3, seed);
//				if (treeCount==0)
//					bushCount = generateRandomObject(objectMap, bushes, j, i, 0.3, seed);
//				
//				if (treeCount>0) 
//					plantCount = rnd.nextInt(4);
//				else if (bushCount>0)
//					plantCount = rnd.nextInt(6);
//				else
//					plantCount = rnd.nextInt(10);
//				plantCount = generateRandomObject(objectMap, plants, j, i, plantCount, seed);
//				
////				double treeNoiseResult = treeSsn.eval(j, i);
////				double shrubNoiseResult = shrubSsn.eval(j, i);
////				// Determine if object is generated at coordinate
////				if (treeNoiseResult < ((forestry / 5.0) - 1)) 
////				{
////				}
////				if (shrubNoiseResult < ((forestry / 5.0) - 1)) {
////					objectMap.put("shrub1.png" + "tempKey:" + i + "-" + j, new GridObject(
////							"tempKey:" + i + "-" + j,
////							"shrub1.png",
////							"A shrubbery!",
////							i, j,
////							new Random(seed * (i * j + i * 11 + j)).nextInt(20),
////							new Random(seed * (i * j + i * 10 + j)).nextInt(20),
////							75 / 2, ((65) / 2), 77, 65, false, false));
////				}
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
