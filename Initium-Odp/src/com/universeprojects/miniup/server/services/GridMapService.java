package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.appengine.api.datastore.DataTypeTranslator;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.DBUtils;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
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

	public static class ProceduralKeyData
	{
		public Long locationId;
		public Integer tileX;
		public Integer tileY;
		public Integer index;
	}
	
	public static int MAX_ITEMS_PER_TILE = 10;
	public static double GLOBAL_ITEM_SCALE = 0.4;

	
	final private ODPDBAccess db;
	final private CachedDatastoreService ds;
	final private QueryHelper query;
	final private CachedEntity location;
	private Integer locationWidth;
	private Integer locationHeight;
	private boolean initialized = false;
	private boolean initializedLocationData = false;
	private List<CachedEntity> locationPresets = null;
	private Map<CachedEntity, Double> naturalItemsMap = null;
	private CachedEntity locationDataEntity = null;
	private EmbeddedEntity locationData = null;
	
	
	public GridMapService(ODPDBAccess db, CachedEntity location)
	{
		this.db = db;
		this.ds = db.getDB();
		this.query = new QueryHelper(ds);
		this.location = location;
		locationWidth = intVal(location.getProperty("gridMapWidth"));
		locationHeight = intVal(location.getProperty("gridMapHeight"));
		if (locationWidth==null) locationWidth = 1;
		if (locationHeight==null) locationHeight = 1;
	}

	public boolean isForLocation(Key locationKey)
	{
		return GameUtils.equals(location.getKey(), locationKey);
	}
	
	private void initializeLocationData()
	{
		if (initializedLocationData) return;

		initializedLocationData = true;
		
		locationDataEntity = db.getEntity(KeyFactory.createKey("GridMapLocationData", location.getId()));
		if (locationDataEntity!=null)
		{
			locationData = (EmbeddedEntity)locationDataEntity.getProperty("gridMapTiles");
		}
		else
		{
			locationDataEntity = new CachedEntity("GridMapLocationData", location.getId());
			locationData = new EmbeddedEntity();
			
			generateDBItemTileCache(500, 500);
		}
	}
	
	private void initialize()
	{
		if (initialized) return;

		// First get all the preset entities
		@SuppressWarnings("unchecked")
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
	
	
	@SuppressWarnings("unchecked")
	private List<CachedEntity> generateNaturalTileItems(Random rnd, int tileX, int tileY)
	{
		initialize();

		EmbeddedEntity gridMapTile = getGridMapTile(tileX, tileY);
		Boolean isProceduralItemsCleared = false;
		if (gridMapTile!=null) isProceduralItemsCleared = (Boolean)gridMapTile.getProperty("clearedProceduralItems");
		if (isProceduralItemsCleared==null) isProceduralItemsCleared = false;
		
		List<CachedEntity> tileItems = new ArrayList<>();
		List<CachedEntity> result = new ArrayList<>();
		
		if (isProceduralItemsCleared==false)
		{		
			
			//TODO: if one of the items generated takes up the entire tile then we should skip generating the rest of the items
			int index = 0;
			for(CachedEntity itemDef:naturalItemsMap.keySet())
			{
				Double odds = naturalItemsMap.get(itemDef);
				if (odds<=0) continue;
				int spawnCount = (int)Math.floor(odds/100);
				double lessThan100SpawnOdds = odds-(spawnCount*100);
				
				
				if (rnd.nextDouble()*100<lessThan100SpawnOdds)
				{
					//TODO: Consider using a fresh Random and adding an index number to the seed so we can regenerate this item without having to generate the whole tile
					CachedEntity item = generateItem(rnd, itemDef, tileX, tileY);
					item.setAttribute("proceduralKey", generateProceduralKey(location.getId(), tileX, tileY, index));
					index++;
					result.add(item);
				}
				
				if (spawnCount>0)
					spawnCount = rnd.nextInt(spawnCount+1);
				
				for(int i = 0; i<spawnCount; i++)
				{
					//TODO: Consider using a fresh Random and adding an index number to the seed so we can regenerate this item without having to generate the whole tile
					CachedEntity item = generateItem(rnd, itemDef, tileX, tileY);
					item.setAttribute("proceduralKey", generateProceduralKey(location.getId(), tileX, tileY, index));
					index++;
					result.add(item);
				}
			}
			
		}
		
		List<EmbeddedEntity> proceduralEntries = null;
		if (gridMapTile!=null)
			proceduralEntries = (List<EmbeddedEntity>)gridMapTile.getProperty("proceduralItems");

		// Go over all procedural items and remove the ones that were deleted 
		if (proceduralEntries!=null)
		{
			for(int i = 0; i<result.size(); i++)
			{
				EmbeddedEntity entry = proceduralEntries.get(i);
				
				ItemEntryStatus status = null;
				if (entry!=null)
					status = parseIES(entry.getProperty("status"));
				
				if (status != ItemEntryStatus.ProceduralDeleted)
					tileItems.add(result.get(i));
				else
					tileItems.add(null);
			}
		}
		else
		{
			tileItems.addAll(result);
		}
		
		return tileItems;
	}
	
	public int getRowStart()
	{
		return 500-locationWidth/2;
	}
	
	public int getRowEnd()
	{
		return (int)Math.ceil(500d+locationWidth.doubleValue()/2);
	}
	
	public int getColumnStart()
	{
		return 500-locationHeight/2;
	}
	
	public int getColumnEnd()
	{
		return (int)Math.ceil(500d+locationHeight.doubleValue()/2);
	}
	
	
	@SuppressWarnings("unchecked")
	public List<CachedEntity> generateTileItems(int tileX, int tileY)
	{
		initializeLocationData();
		
		Random rnd = getRandomForTile(tileX, tileY);
		
		List<CachedEntity> tileItems = generateNaturalTileItems(rnd, tileX, tileY);
		
		// Now remove any nulls
		for(int i = tileItems.size()-1; i>=0; i--) 
			if (tileItems.get(i)==null) tileItems.remove(i);

		EmbeddedEntity gridMapTile = getGridMapTile(tileX, tileY);
			
		// Go over all DB items 
		List<EmbeddedEntity> itemDBEntries = null;
		if (gridMapTile!=null) itemDBEntries = (List<EmbeddedEntity>)gridMapTile.getProperty("dbItems");
		if (itemDBEntries==null) 
		{
			List<CachedEntity> dbItems = generateDBItemTileCache(tileX, tileY);
			if (dbItems!=null)
			{
				tileItems.addAll(dbItems);
				return tileItems;
			}
		}

		List<Key> keysToFetch = new ArrayList<>();
		if (itemDBEntries!=null)
			for(EmbeddedEntity entry:itemDBEntries)
			{
				Key itemKey = (Key)entry.getProperty("itemKey");
				keysToFetch.add(itemKey);
			}
		List<CachedEntity> dbEntities = db.getEntities(keysToFetch);
		tileItems.addAll(dbEntities);
		

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
		item.setProperty("gridMapPositionX", (long)tileX);
		item.setProperty("gridMapPositionY", (long)tileY);
		item.setProperty("gridMapCellOffsetX", (long)rnd.nextInt(64));
		item.setProperty("gridMapCellOffsetY", (long)rnd.nextInt(64));
		item.setProperty("containerKey", location.getKey());
		
		return item;
	}
	
	public String generateGridObjectJson(int tileX, int tileY)
	{
		StringBuilder js = new StringBuilder();
		
		// First clear the cell of objects
		js.append("removeTileObjects("+tileX+", "+tileY+");\n");
		
		Map<String, GridObject> gridObjects = generateGridObjects(tileX, tileY);
		for(GridObject go:gridObjects.values())
		{
			js.append("addGridObjectToMap(")
				.append(go.getJsonObject().toJSONString())
				.append(");\n");
		}
		js.append("refreshPositions();\n");
		
		return js.toString();
	}
	
	public Map<String, GridObject> generateGridObjects(int tileX, int tileY)
	{
		Random rnd = getRandomForTile(tileX, tileY);
		List<CachedEntity> items = generateNaturalTileItems(rnd, tileX, tileY);

		Map<String, GridObject> result = new HashMap<>();
		if (items!=null)
		for(int i = 0; i<items.size(); i++)
		{
			boolean pileRandom = false;
			
			CachedEntity item = items.get(i);
			if (item==null) continue;
			String imageUrl = null;
			Integer cellOffsetX = null;
			Integer cellOffsetY = null;
			Double rotation = null;
			Integer imageWidth = null;
			Integer imageHeight = null;
			if (item.getProperty("GridMapObject:image")==null || item.getProperty("GridMapObject:imageWidth")==null || item.getProperty("GridMapObject:imageHeight")==null) 
			{
				imageUrl = (String)item.getProperty("icon");
				imageWidth = (int)Math.floor(32*GLOBAL_ITEM_SCALE);
				imageHeight = (int)Math.floor(32*GLOBAL_ITEM_SCALE);
				pileRandom = true;
			}
			else
			{
				imageUrl = (String)item.getProperty("GridMapObject:image");
				imageWidth = (int)Math.floor(intVal(item.getProperty("GridMapObject:imageWidth"))*GLOBAL_ITEM_SCALE);
				imageHeight = (int)Math.floor(intVal(item.getProperty("GridMapObject:imageHeight"))*GLOBAL_ITEM_SCALE);
				
			}
			cellOffsetX = intVal(item.getProperty("gridMapCellOffsetX"));
			cellOffsetY = intVal(item.getProperty("gridMapCellOffsetY"));
			rotation = (Double)item.getProperty("gridMapRotation");

			if (cellOffsetX==null || cellOffsetY==null || pileRandom)
			{
				long seed = location.getId()+(tileY*1000)+tileX+items.size();
				if (seed<0) seed += Long.MAX_VALUE;
				Random offsetRnd = new Random(seed);
				cellOffsetX = (offsetRnd.nextInt(64)+offsetRnd.nextInt(64))/2;
				cellOffsetY = (offsetRnd.nextInt(64)+offsetRnd.nextInt(64))/2;
			}
			
			if (rotation==null)
			{
				rotation = 0d;
			}

			
//			double scale = 1-(rnd.nextDouble()*type.scaleVariance);
//			double width = ((double)type.width)*scale*GLOBAL_SCALE;
//			double height = ((double)type.height)*scale*GLOBAL_SCALE;
			
			
			
			String generatedKey = null;
			if (item.getKey().isComplete())
				generatedKey = item.getKey().toString();
			else
				generatedKey = (String)item.getAttribute("proceduralKey");
			
			result.put(generatedKey, new GridObject(
					generatedKey,
					imageUrl,
					"",
					tileX, tileY,
					rotation,
					cellOffsetX,
					cellOffsetY,
					(int)(imageWidth / 2), (int)(imageHeight*0.95), (int)(imageWidth), (int)(imageHeight), false, false, 
					getRowStart(), getColumnStart()));
		}
		
		
		EmbeddedEntity gridMapTile = getGridMapTile(tileX, tileY);
		if (gridMapTile==null || gridMapTile.getProperty("dbItems")==null) 
		{
			generateDBItemTileCache(tileX, tileY);
			
			gridMapTile = getGridMapTile(tileX, tileY);
			if (gridMapTile==null) return result;
		}

		@SuppressWarnings("unchecked")
		List<EmbeddedEntity> dbItems = (List<EmbeddedEntity>)gridMapTile.getProperty("dbItems");
		
		if (dbItems!=null)
		for(EmbeddedEntity item:dbItems)
		{
			
			String generatedKey = "DK-"+((Key)item.getProperty("itemKey")).toString();
			Long cellOffsetX = (Long)item.getProperty("dbItemCellOffsetX");
			Long cellOffsetY = (Long)item.getProperty("dbItemCellOffsetY");
			String imageUrl = GameUtils.getResourceUrl(item.getProperty("dbItemImage"));
			Long imageWidth = (Long)item.getProperty("dbItemWidth");
			Long imageHeight = (Long)item.getProperty("dbItemHeight");
			Double rotation = (Double)item.getProperty("dbItemRotation");
			
			if (cellOffsetX==null || cellOffsetY==null)
			{
				cellOffsetX = 0L;
				cellOffsetY = 0L;
			}
			
			if (imageWidth==null || imageHeight==null)
			{
				imageWidth = (long)Math.floor(32d*GLOBAL_ITEM_SCALE);
				imageHeight = (long)Math.floor(32d*GLOBAL_ITEM_SCALE);
			}
			
			if (rotation==null)
			{
				rotation = rnd.nextDouble()*360;
			}
			
			result.put(generatedKey, new GridObject(
					generatedKey,
					imageUrl,
					"",
					tileX, tileY,
					rotation,
					cellOffsetX.intValue(),
					cellOffsetY.intValue(),
					(int)(imageWidth / 2), (int)(imageHeight*0.95), imageWidth.intValue(), imageHeight.intValue(), false, false,
					getRowStart(), getColumnStart()));
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

	/**
	 * This sets the fields on subLocation that are used to determine how to procedurally generate the location. They are determined
	 * from the parentLocation's settings. 
	 * @param db
	 * @param parentLocation
	 * @param subLocation
	 */
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
	
	public String generateProceduralKey(Long locationId, int tileX, int tileY, int index)
	{
		return "PKA"+locationId+"TX"+tileX+"TY"+tileY+"-"+index;
	}
	
	public ProceduralKeyData getProceduralKeyData(String proceduralKey)
	{
		if (proceduralKey==null || proceduralKey.matches("PKA\\d+TX\\d+TY\\d+-\\d+")==false)
			throw new IllegalArgumentException("Invalid procedural key: "+proceduralKey);
		
		String[] parts = proceduralKey.split("(PKA|TX|TY|-)");
		
		ProceduralKeyData pkd = new ProceduralKeyData();
		pkd.locationId = Long.parseLong(parts[1]);
		pkd.tileX = Integer.parseInt(parts[2]);
		pkd.tileY = Integer.parseInt(parts[3]);
		pkd.index = Integer.parseInt(parts[4]);
		
		return pkd;
	}

	public static CachedEntity generateSingleItemFromProceduralKey(ODPDBAccess db, CachedEntity location, String proceduralKey)
	{
		GridMapService gms = new GridMapService(db, location);
		
		ProceduralKeyData data = gms.getProceduralKeyData(proceduralKey);
		
		Random rnd = gms.getRandomForTile(data.tileX, data.tileY);
		
		List<CachedEntity> items = gms.generateNaturalTileItems(rnd, data.tileX, data.tileY);
		
		if (items==null || items.size()-1<data.index) return null;
		
		CachedEntity entity = items.get(data.index);
		
		if (proceduralKey.equals(entity.getAttribute("proceduralKey")))
			return entity;
		
		return null;
	}

	
	protected EmbeddedEntity getGridMapTile(int tileX, int tileY)
	{
		initializeLocationData();
		
		if (locationData==null) return null;
		
		return (EmbeddedEntity)locationData.getProperty(tileX+"x"+tileY);
	}
	
	protected EmbeddedEntity getOrCreateGridMapTile(int tileX, int tileY)
	{
		EmbeddedEntity tile = getGridMapTile(tileX, tileY);
		
		if (tile==null)
		{
			tile = new EmbeddedEntity();
			long id = new Random().nextLong();
			if (id<0) id+=Long.MAX_VALUE;
			tile.setKey(KeyFactory.createKey("GridMapTile", id));
			
			setGridMapTile(tileX, tileY, tile);
		}
		
		return tile;
	}
	
	protected EmbeddedEntity getGridMapTileProceduralEntry(int tileX, int tileY, int index)
	{
		
		EmbeddedEntity gridMapTile = getGridMapTile(tileX, tileY);
		if (gridMapTile!=null)
		{
			@SuppressWarnings("unchecked")
			List<EmbeddedEntity> gridMapTileEntries = (List<EmbeddedEntity>)gridMapTile.getProperty("proceduralItems");
			if (gridMapTileEntries!=null)
				return gridMapTileEntries.get(index);
		}
		
		
		
		return null;
	}
	
	protected EmbeddedEntity getOrCreateGridMapTileProceduralEntry(int tileX, int tileY, int index)
	{
		EmbeddedEntity entry = getGridMapTileProceduralEntry(tileX, tileY, index);
		
		if (entry==null)
		{
			entry = generateDefaultProceduralTileEntry(tileX, tileY, index);
			
			setGridMapTileProceduralEntry(tileX, tileY, index, entry);
		}
		
		return entry;
	}
	
	protected void setGridMapTileProceduralEntry(int tileX, int tileY, int index, EmbeddedEntity entity)
	{
		EmbeddedEntity gridMapTile = getOrCreateGridMapTile(tileX, tileY);
		@SuppressWarnings("unchecked")
		List<EmbeddedEntity> gridMapTileEntries = (List<EmbeddedEntity>)gridMapTile.getProperty("proceduralItems");
		if (gridMapTileEntries!=null)
			gridMapTileEntries.set(index, entity);
		else
		{
			List<CachedEntity> naturalTileItems = generateNaturalTileItems(getRandomForTile(tileX, tileY), tileX, tileY);
			gridMapTileEntries = new ArrayList<EmbeddedEntity>(Arrays.asList(new EmbeddedEntity[naturalTileItems.size()]));
			gridMapTileEntries.set(index, entity);
			gridMapTile.setProperty("proceduralItems", gridMapTileEntries);
		}
		
		setGridMapTile(tileX, tileY, gridMapTile);
	}
	
	protected void setGridMapTile(int tileX, int tileY, EmbeddedEntity entity)
	{
		locationData.setProperty(tileX+"x"+tileY, entity);
		locationDataEntity.setProperty("gridMapTiles", locationData);
	}
	
	public boolean isStillProceduralEntity(String proceduralKey)
	{
		ProceduralKeyData data = getProceduralKeyData(proceduralKey);
		
		if (GameUtils.equals(location.getId(), data.locationId)==false)
			throw new RuntimeException("The procedural key's location ("+data.locationId+") is not for the location ("+location.getId()+") this GridMapService is serving.");
		
		EmbeddedEntity gridMapTile = getGridMapTile(data.tileX, data.tileY);
		
		if (gridMapTile==null) return true;
		
		if (GameUtils.equals(gridMapTile.getProperty("clearedProceduralItems"), true)) return false;
		
		@SuppressWarnings("unchecked")
		List<EmbeddedEntity> entries = (List<EmbeddedEntity>)gridMapTile.getProperty("proceduralItems");
		if (entries!=null && entries.size()<=data.index)
			throw new RuntimeException("The procedural key index ("+data.index+") is larger than the entry list ("+entries.size()+").");
			
		if (entries==null) return true;
		
		EmbeddedEntity entry = getGridMapTileProceduralEntry(data.tileX, data.tileY, data.index);
		
		if (entry==null) return true;
		
		String status = (String)entry.getProperty("status");
		Long expectedIndex = (Long)entry.getProperty("proceduralGenerationIndex");
		
		if (GameUtils.equals(expectedIndex, data.index)==false)
			throw new RuntimeException("Something's not right. The expected index ("+expectedIndex+") was not the same as the actual index ("+data.index+").");
		
		if (GameUtils.equals(status, null) || GameUtils.equals(status, "Procedural"))
			return true;
		else
			return false;
	}
	
	public void removeProceduralEntity(String proceduralKey)
	{
		// 1. Check if the entity is already in the database - if it is, just leave
		// 2. Generate the appropriate entry in the GridMapCell, but don't put it

		if (isStillProceduralEntity(proceduralKey)==false) return;
		
		ProceduralKeyData data = getProceduralKeyData(proceduralKey);
		
		EmbeddedEntity entry = getOrCreateGridMapTileProceduralEntry(data.tileX, data.tileY, data.index);
		
		entry.setProperty("status", "ProceduralDeleted");
		
		setGridMapTileProceduralEntry(data.tileX, data.tileY, data.index, entry);
		
		checkIfAllProceduralEntitiesAreRemoved(data.tileX, data.tileY);
			
	}
	
	/**
	 * This checks, and then sets the clearedProceduralItems flag to true if all procedural items have been removed.
	 * 
	 * Note: It doesn't unset the flag if the procedural items haven't been cleared but the flag was true.
	 * 
	 * @param tileX
	 * @param tileY
	 * @return
	 */
	private boolean checkIfAllProceduralEntitiesAreRemoved(int tileX, int tileY)
	{
		EmbeddedEntity gridMapTile = getGridMapTile(tileX, tileY);
		if (gridMapTile==null) return false;
		
		@SuppressWarnings("unchecked")
		List<EmbeddedEntity> gridMapTileEntries = (List<EmbeddedEntity>)gridMapTile.getProperty("proceduralItems");
		if (gridMapTileEntries==null) return false;
		
		
		for(EmbeddedEntity entry:gridMapTileEntries)
		{
			if (entry==null) return false;
			if (GameUtils.equals(entry.getProperty("status"), "ProceduralDeleted")==false)
				return false;
		}
		
		gridMapTile.setProperty("clearedProceduralItems", true);
		
		setGridMapTile(tileX, tileY, gridMapTile);
		
		return true;
	}
	
	
	protected EmbeddedEntity generateDefaultProceduralTileEntry(int tileX, int tileY, int index)
	{
		EmbeddedEntity entity = new EmbeddedEntity();
		long id = new Random().nextLong();
		if (id<0) id+=Long.MAX_VALUE;
		
		entity.setKey(KeyFactory.createKey("Item", id));
		
		entity.setProperty("proceduralGenerationIndex", index);
		entity.setProperty("status", "Procedural");
		
		return entity;
	}

	public void removeEntity(CachedEntity itemEntity)
	{
		if (itemEntity.getAttribute("proceduralKey")!=null)
		{
			// It is procedurally generated and we need to remove it that way
			removeProceduralEntity((String)itemEntity.getAttribute("proceduralKey"));
		}
		else if (itemEntity.getKey()!=null && itemEntity.getKey().isComplete())
		{
			Long tileX = (Long)itemEntity.getProperty("gridMapPositionX");
			Long tileY = (Long)itemEntity.getProperty("gridMapPositionY");
			if (tileX==null) tileX = 500L;
			if (tileY==null) tileY = 500L;
			
			generateDBItemTileCache(tileX.intValue(), tileY.intValue());
		}
	}

	private void removeDBEntity(CachedEntity itemEntity)
	{
		Long tileX = (Long)itemEntity.getProperty("gridMapPositionX");
		Long tileY = (Long)itemEntity.getProperty("gridMapPositionY");
		if (tileX==null) tileX = 500L;
		if (tileY==null) tileY = 500L;
		
		EmbeddedEntity tile = getOrCreateGridMapTile(tileX.intValue(), tileY.intValue());
		
		@SuppressWarnings("unchecked")
		List<EmbeddedEntity> tileEntries = (List<EmbeddedEntity>)tile.getProperty("dbItems");
		
		if (tileEntries!=null)
			for(int i = tileEntries.size()-1;i>=0; i--)
			{
				EmbeddedEntity entry = tileEntries.get(i);
				if (entry==null) continue;
				
				Key itemKey = (Key)entry.getProperty("itemKey");
				if (GameUtils.equals(itemKey, itemEntity.getKey()))
				{
					tileEntries.remove(i);
					tile.setProperty("dbItems", tileEntries);
					return;
				}
			}
	}
	
	public void regenerateDBItemTileCache(int tileX, int tileY)
	{
		generateDBItemTileCache(tileX, tileY);
	}
	
	private List<CachedEntity> generateDBItemTileCache(int tileX, int tileY)
	{
//		if (tileX!=500 || tileY!=500)
//			return null;
		// 1. Clear out the existing DB related items, but leave the procedural ones
		// 2. Query for the items in that location (special consideration for tile 500x500 as it is the default tile and it's where everything  that has no tile position should be considered to be
		// 3. Add up to 10 of the items to the cache and put it back
		
		EmbeddedEntity tile = getOrCreateGridMapTile(tileX, tileY);
		
		List<CachedEntity> items = null;
		Query q = new Query("Item").setFilter(
				CompositeFilterOperator.and(
						new FilterPredicate("containerKey", FilterOperator.EQUAL, location.getKey()),
						new FilterPredicate("gridMapPositionX", FilterOperator.EQUAL, (long)tileX),
						new FilterPredicate("gridMapPositionY", FilterOperator.EQUAL, (long)tileY)))
				.addSort("movedTimestamp", SortDirection.DESCENDING);

		items = ds.fetchAsList(q, 10);

		// if we're looking at the center of the map, then ALSO do another query that deals with legacy items
		if (tileX==500 && tileY==500)
		{
			q = new Query("Item").setFilter(
					new FilterPredicate("containerKey", FilterOperator.EQUAL, location.getKey()))
					.addSort("movedTimestamp", SortDirection.DESCENDING);
			
			
			List<CachedEntity> legacyItems = ds.fetchAsList(q, 50);
			for(CachedEntity legacyItem:legacyItems)
			{
				if (CommonChecks.checkItemIsLegacyGridMapItem(legacyItem)==true)
					items.add(legacyItem);
			}
			
		}
		
		List<EmbeddedEntity> dbItems = new ArrayList<>();
		for(CachedEntity entity:items)
		{
			dbItems.add(generateTileEntryFromItem(entity));
		}

		tile.setProperty("dbItems", dbItems);
		
		setGridMapTile(tileX, tileY, tile);
		
		return items;
	}
	
	
	private EmbeddedEntity generateTileEntryFromItem(CachedEntity item)
	{
		EmbeddedEntity entry = new EmbeddedEntity();

		// Grab the GridMapObject values and overrides
		String mode = (String)item.getProperty("GridMapObject:mode");
		Long cellOffsetX = (Long)item.getProperty("gridMapCellOffsetX");
		Long cellOffsetY = (Long)item.getProperty("gridMapCellOffsetY");
		Long width = (Long)item.getProperty("GridMapObject:imageWidth");
		Long height = (Long)item.getProperty("GridMapObject:imageHeight");
		Long tileX = (Long)item.getProperty("gridMapPositionX");
		Long tileY = (Long)item.getProperty("gridMapPositionY");
		String image = (String)item.getProperty("GridMapObject:image");
		
		
		// Process the values out...
		if (tileX==null) tileX = 500L;
		if (tileY==null) tileY = 500L;
		Random rnd = GameUtils.getSeededRandom(item.getId(), tileX, tileY, location.getId());
		if (image==null || image.trim().equals("")) image = (String)item.getProperty("icon");
		if (cellOffsetX==null || cellOffsetY==null)
		{
			cellOffsetX = (long)(rnd.nextInt(64)+rnd.nextInt(64))/2;
			cellOffsetY = (long)(rnd.nextInt(64)+rnd.nextInt(64))/2;			
		}
		if (width==null || height==null)
		{
			width = (long)Math.floor(32d*GLOBAL_ITEM_SCALE);
			height = (long)Math.floor(32d*GLOBAL_ITEM_SCALE);
		}

		
		
		
		// Set the values on the entry
		Long id = rnd.nextLong();
		if (id<=0) id+=Long.MAX_VALUE;
		entry.setKey(KeyFactory.createKey("GridMapTileItemEntry", id));
		entry.setProperty("status", "Database");
		entry.setProperty("dbItemCellOffsetX", cellOffsetX);
		entry.setProperty("dbItemCellOffsetY", cellOffsetY);
		entry.setProperty("dbItemHeight", width);
		entry.setProperty("dbItemWidth", height);
		entry.setProperty("itemKey", item.getKey());
		entry.setProperty("dbItemImage", image);
		
		return entry;
	}

	
	public boolean isLocationDataChanged()
	{
		if (locationDataEntity==null) return false;
		return locationDataEntity.isUnsaved();
	}
	
	public void putLocationData(CachedDatastoreService ds)
	{
		if (locationDataEntity!=null)
		{
			System.out.println("Size: "+DBUtils.getEntitySize(locationDataEntity)+" bytes");
			ds.put(locationDataEntity);
		}
	}

	public void setItemPosition(CachedEntity item, Long tileX, Long tileY)
	{
		if (tileX==null) tileX = 500L;
		if (tileY==null) tileY = 500L;
		
		item.setProperty("gridMapPositionX", tileX);
		item.setProperty("gridMapPositionY", tileY);
		
		EmbeddedEntity tile = getOrCreateGridMapTile(tileX.intValue(), tileY.intValue());
		
		List<EmbeddedEntity> dbItems = (List<EmbeddedEntity>)tile.getProperty("dbItems");
		if (dbItems==null) dbItems = new ArrayList<>();
		
		EmbeddedEntity tileEntry = generateTileEntryFromItem(item);
		
		dbItems.add(tileEntry);
		
		if (dbItems.size()>10)
			dbItems.remove(0);

		tile.setProperty("dbItems", dbItems);
		
		setGridMapTile(tileX.intValue(), tileY.intValue(), tile);
	}
}
