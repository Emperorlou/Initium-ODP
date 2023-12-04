package com.universeprojects.miniup.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.server.services.GridMapService;

public class InitiumEntityPool extends EntityPool
{
	final private GridMapService gms;

	private List<String> proceduralKeyQueue = new ArrayList<>();
	private Map<String, CachedEntity> proceduralItemsMap = new HashMap<>();
	
	public InitiumEntityPool(CachedDatastoreService ds, GridMapService gms)
	{
		super(ds);
		this.gms = gms;
	}
 
	@Override
	public void addToQueue(Object... keyList)
	{
		if (keyList==null) return;
		
		List<Object> cleanedList = new ArrayList<Object>();
		cleanedList.addAll(Arrays.asList(keyList));
		for(Object o:keyList)
		{
			if (o==null)
			{
				// Lets just skip this one
				continue;
			}
			else if (o instanceof String)
			{
				if (GridMapService.isProceduralKey((String)o))
				{
					proceduralKeyQueue.add((String)o);
					cleanedList.remove((String)o);
				}
			}
			else if (o instanceof InitiumKey)
			{
				InitiumKey k = (InitiumKey)o;
				
				if (k.isProceduralKey())
				{
					proceduralKeyQueue.add(k.getProceduralKey());
					cleanedList.remove(o);
				}
				else if (k.isDatastoreKey())
				{
					super.addToQueue(k.getDatastoreKey());
					cleanedList.remove(o);
				}
				else
					throw new RuntimeException("Unhandled key type");
			}
			else if (o instanceof Iterable)
			{
				Iterable<?> list = (Iterable<?>)o;
				for(Object obj:list)
				{
					addToQueue(obj);
				}
				cleanedList.remove(o);
			}
			
		}
		
		super.addToQueue(cleanedList);
	}
	
	@Override
	public Map<Key, CachedEntity> loadEntities(Object... keyList)
	{
		if (keyList!=null)
			for(Object o:keyList)
			{
				if (o==null)
				{
					// Lets just skip this one
					continue;
				}
				else if (o instanceof String)
				{
					if (GridMapService.isProceduralKey((String)o))
						proceduralKeyQueue.add((String)o);
				}
				else if (o instanceof InitiumKey)
				{
					InitiumKey k = (InitiumKey)o;
					
					if (k.isProceduralKey())
						proceduralKeyQueue.add(k.getProceduralKey());
					else if (k.isDatastoreKey())
						super.addToQueue(k.getDatastoreKey());
					else
						throw new RuntimeException("Unhandled key type");
				}
			}
		List<CachedEntity> proceduralItems = gms.generateItemsFromProceduralKeys(proceduralKeyQueue, true);
		
		for(int i = 0; i<proceduralKeyQueue.size(); i++)
			proceduralItemsMap.put(proceduralKeyQueue.get(i), proceduralItems.get(i));
		
		List<Object> cleanedList = Arrays.asList(keyList);
		cleanedList.removeAll(proceduralKeyQueue);
		
		proceduralKeyQueue.clear();
		
		return super.loadEntities(cleanedList);
	}
	
	@Override
	public List<CachedEntity> get(Collection entityKeys)
	{
		List dsKeys = new ArrayList();
		List proceduralKeys = new ArrayList();
		
		for(Object key:entityKeys)
		{
			if ((key instanceof String) && GridMapService.isProceduralKey((String)key))
			{
				proceduralKeys.add(key);
				dsKeys.add(null);
			}
			else if ((key instanceof InitiumKey) && ((InitiumKey)key).isProceduralKey())
			{
				proceduralKeys.add(((InitiumKey)key).getProceduralKey());
				dsKeys.add(null);
			}
			else
			{
				proceduralKeys.add(null);
				dsKeys.add(key);
			}
		}
		
		List<CachedEntity> result = gms.generateItemsFromProceduralKeys(proceduralKeys, true);
		
		List<CachedEntity> dsItems = super.get(dsKeys);
		
		for(int i = 0; i<entityKeys.size(); i++) 
		{
			Object dsKey = dsKeys.get(i);
			Object procKey = proceduralKeys.get(i);
			
			if (dsKey!=null && procKey!=null) throw new RuntimeException("This shouldn't be possible. This method has a logic error.");
			
			if (dsKey!=null)
				result.set(i, dsItems.get(i));
		}
		
		return result;
	}
	
	@Override
	public CachedEntity get(Object key)
	{
		if ((key instanceof String) && GridMapService.isProceduralKey((String)key))
		{
			return gms.generateSingleItemFromProceduralKey((String)key);
		}
		else if ((key instanceof InitiumKey) && ((InitiumKey)key).isProceduralKey())
		{
			return gms.generateSingleItemFromProceduralKey(((InitiumKey)key).getProceduralKey());
		}
		else if ((key instanceof InitiumKey) && ((InitiumKey)key).isDatastoreKey())
		{
			return super.get(((InitiumKey)key).getDatastoreKey());
		}
		else
		{
			return super.get(key);
		}
	}
	
}
