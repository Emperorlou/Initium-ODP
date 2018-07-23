package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.model.GridCell;
import com.universeprojects.miniup.server.model.GridMap;
import com.universeprojects.miniup.server.model.GridObject;

public class GridMapService {

	public enum ItemEntryStatus
	{
		Procedural,
		ProceduralDeleted,
		Database
	}
	
	public static double GLOBAL_SCALE = 0.4;

	
	final private ODPDBAccess db;
	final private CachedDatastoreService ds;
	final private CachedEntity location;
	private Integer locationWidth;
	private Integer locationHeight;
	private boolean initialized = false;
	private boolean initializedLocationData = false;
	private List<CachedEntity> locationPresets = null;
	private Map<CachedEntity, Double> naturalItemsMap = null;
	private EmbeddedEntity locationData = null;
	
	public GridMapService(ODPDBAccess db, CachedEntity location)
	{
		this.db = db;
		this.ds = db.getDB();
		this.location = location;
		locationWidth = intVal(location.getProperty("gridMapWidth"));
		locationHeight = intVal(location.getProperty("gridMapHeight"));
		if (locationWidth==null) locationWidth = 1;
		if (locationHeight==null) locationHeight = 1;
	}

	private void initializeLocationData()
	{
		if (initializedLocationData) return;
		
		CachedEntity locationDataEntity = db.getEntity(KeyFactory.createKey("GridMapLocationData", location.getId()));
		if (locationDataEntity!=null)
		{
			locationData = (EmbeddedEntity)locationDataEntity.getProperty("gridMapTiles");
		}
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
	
	private int getRowStart()
	{
		return 500-locationWidth/2;
	}
	
	private int getRowEnd()
	{
		return (int)Math.ceil(500d+locationWidth.doubleValue()/2);
	}
	
	private int getColumnStart()
	{
		return 500-locationHeight/2;
	}
	
	private int getColumnEnd()
	{
		return (int)Math.ceil(500d+locationHeight.doubleValue()/2);
	}
	
	private List<CachedEntity> generateTileItems(Random rnd, int tileX, int tileY)
	{
		initializeLocationData();
		
		List<CachedEntity> tileItems = generateNaturalTileItems(rnd, tileX, tileY);
		
		if (locationData==null) return tileItems;
		
		EmbeddedEntity gridMapTile = (EmbeddedEntity)locationData.getProperty(tileX+"x"+tileY);
		
		if (gridMapTile==null) return tileItems;
		
		Boolean isProceduralItemsCleared = (Boolean)gridMapTile.getProperty("clearedProceduralItems"); 
		Boolean hasDatabaseItems = (Boolean)gridMapTile.getProperty("hasDatabaseItems");
		List<EmbeddedEntity> itemDBEntries = (List<EmbeddedEntity>)gridMapTile.getProperty("items");
		
		if (isProceduralItemsCleared) tileItems.clear();
		
		if (hasDatabaseItems && itemDBEntries!=null && itemDBEntries.isEmpty()==false)
		{
			for(EmbeddedEntity entry:itemDBEntries)
			{
				Key itemKey = (Key)entry.getProperty("itemKey");
				ItemEntryStatus status = parseIES(entry.getProperty("status"));
				Integer proceduralGenerationIndex = null;
				if (entry.getProperty("proceduralGenerationIndex")!=null) proceduralGenerationIndex = ((Long)entry.getProperty("proceduralGenerationIndex")).intValue();
				
				if (status == ItemEntryStatus.ProceduralDeleted && isProceduralItemsCleared==false)
					tileItems.set(proceduralGenerationIndex, null);
				else if (status == ItemEntryStatus.Database)
				{
					// TODO: I guess we... load it from the db here?
				}
				else if (status == ItemEntryStatus.Procedural)
				{
					// Do nothing here I suppose, we already generated everything
				}
				else
					throw new RuntimeException("ItemEntryStatus type not handled: "+status);
			}
		}
		
		
		return tileItems;
	}
	
	private ItemEntryStatus parseIES(Object value)
	{
		if (value==null) return null;
		return ItemEntryStatus.valueOf((String)value);
	}
	
	private CachedEntity getGridMapTileEntity(int tileX, int tileY)
	{
		String keyString = location.getKey().getId()+"-"+tileX+"-"+tileY;
		
		Key key = KeyFactory.createKey("GridMapTile", keyString);
		
		return db.getEntity(key);
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
			boolean pileRandom = false;
			
			CachedEntity item = items.get(i);
			String imageUrl = null;
			Integer cellOffsetX = null;
			Integer cellOffsetY = null;
			Integer imageWidth = null;
			Integer imageHeight = null;
			if (item.getProperty("GridMapObject:image")==null || item.getProperty("GridMapObject:imageWidth")==null || item.getProperty("GridMapObject:imageHeight")==null) 
			{
				imageUrl = (String)item.getProperty("icon");
				imageWidth = 8;
				imageHeight = 8;
				pileRandom = true;
			}
			else
			{
				imageUrl = (String)item.getProperty("GridMapObject:image");
				imageWidth = (int)Math.floor(intVal(item.getProperty("GridMapObject:imageWidth"))*GLOBAL_SCALE);
				imageHeight = (int)Math.floor(intVal(item.getProperty("GridMapObject:imageHeight"))*GLOBAL_SCALE);
				
			}
			cellOffsetX = intVal(item.getProperty("gridMapCellOffsetX"));
			cellOffsetY = intVal(item.getProperty("gridMapCellOffsetY"));

			if (cellOffsetX==null || cellOffsetY==null || pileRandom)
			{
				long seed = location.getId()+(tileY*1000)+tileX+items.size();
				if (seed<0) seed += Long.MAX_VALUE;
				Random offsetRnd = new Random(seed);
				cellOffsetX = (offsetRnd.nextInt(64)+offsetRnd.nextInt(64))/2;
				cellOffsetY = (offsetRnd.nextInt(64)+offsetRnd.nextInt(64))/2;
			}

			
//			double scale = 1-(rnd.nextDouble()*type.scaleVariance);
//			double width = ((double)type.width)*scale*GLOBAL_SCALE;
//			double height = ((double)type.height)*scale*GLOBAL_SCALE;
			
			
			
			String generatedKey = "Location:"+location.getKey().getId()+"-X:"+tileX+"-Y:"+tileY+"-Index:"+i;
			
			result.put(generatedKey, new GridObject(
					generatedKey,
					imageUrl,
					"",
					tileX-getRowStart(), tileY-getColumnStart(),
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
	
	

	
	public GridMap buildNewGrid() {

		Integer rowLength = locationWidth;
		Integer columnLength = locationHeight;
		
		GridCell[][] grid = new GridCell[rowLength][columnLength];
		Map<String, GridObject> objectMap = new HashMap<>();

		// Loop over grid size
		for (int tileX = getRowStart(); tileX < getRowEnd(); tileX++) {
			for (int tileY = getColumnStart(); tileY < getColumnEnd(); tileY++) {
				Random rnd = getRandomForTile(tileX, tileY);
				
				Map<String, GridObject> objects = generateGridObjects(tileX, tileY);
				
				if (objects!=null)
					objectMap.putAll(objects);
				
				// Build background data for coordinate
				grid[tileX-getRowStart()][tileY-getColumnStart()] = new GridCell("images/2d/floor/grass/tile-grass" + rnd.nextInt(7) + ".png",
						tileX-getRowStart(), tileY-getColumnStart(),
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
		if (width==null || width<1) width = 1L;
		return width.intValue();
	}
	
	public int getGridHeight()
	{
		Long height = (Long)location.getProperty("gridMapHeight");
		if (height==null || height<1) height = 1L;
		return height.intValue();
	}

	public void generateGridMapElementsFromParent(ODPDBAccess db, CachedEntity parentLocation, CachedEntity subLocation)
	{
		Map<String, Double> elements = new HashMap<>();
		
		// Add the location's elements from the generator field
		populateElementsFromGenerator(db, parentLocation, "gridMapElementsGenerator", elements);
		
		
		// Add the preset's elements from their generators too
		if (parentLocation.getProperty("gridMapPresets")!=null && ((Collection<Key>)parentLocation.getProperty("gridMapPresets")).isEmpty()==false)
		{
			initialize();
			
			for(CachedEntity preset:locationPresets)
			{
				populateElementsFromGenerator(db, preset, "elementsGenerator", elements);
			}
		}
		
		db.setFieldTypeMapEntityDouble(subLocation, "gridMapElements", elements);
	}
	
	private void populateElementsFromGenerator(ODPDBAccess db, CachedEntity entity, String fieldName, Map<String, Double> elements)
	{
		Map<String, String> elementsGenerator = db.getFieldTypeStringStringMap(entity, fieldName);
		if (elementsGenerator!=null)
			for(String key:elementsGenerator.keySet())
			{
				String value = elementsGenerator.get(key);
				
				Double dblValue = db.solveCurve_Double(value);
				
				elements.put(key, dblValue);
			}
	}
	
}
