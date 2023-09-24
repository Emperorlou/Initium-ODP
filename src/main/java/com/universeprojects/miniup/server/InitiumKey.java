package com.universeprojects.miniup.server;

import java.io.Serializable;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.gefcommon.shared.elements.GameObject;
import com.universeprojects.miniup.server.services.GridMapService;


public class InitiumKey implements Serializable
{
	private static final long serialVersionUID = -8182690146141699616L;
	
	Key key;
	String proceduralKey;
	
	private InitiumKey()
	{
	}

	public static InitiumKey fromGameObject(GameObject<Key> selectedItem)
	{
		CachedEntity e = ((InitiumObject)selectedItem).getEntity();
		
		if (e.getKey().isComplete())
			return InitiumKey.fromKey(e.getKey());
		else
			return InitiumKey.fromObject(e.getAttribute("proceduralKey"));
	}	
	
	public static InitiumKey fromKey(Key key)
	{
		InitiumKey iKey = new InitiumKey();
		iKey.key = key;
		return iKey;
	}
	
	public static InitiumKey fromProceduralKey(String proceduralKey)
	{
		InitiumKey iKey = new InitiumKey();
		iKey.proceduralKey = proceduralKey;
		return iKey;
	}
	
	public static InitiumKey fromString(String key)
	{
		return fromObject(key);
	}
	
	public static InitiumKey fromObject(Object key)
	{
		if (key==null) throw new IllegalArgumentException("key cannot be null");
		
		if (key instanceof Key)
		{
			return InitiumKey.fromKey((Key)key);
		}
		else if (key instanceof String)
		{
			String str = (String)key;
			
			if (GridMapService.isProceduralKey(str))
			{
				return InitiumKey.fromProceduralKey(str);
			}
			else
			{
				try
				{
					return InitiumKey.fromKey(KeyFactory.stringToKey(str));
				}
				catch(IllegalArgumentException e)
				{}
				
				throw new IllegalArgumentException("The key given is unhandled: "+key);
			}
		}
		else
			throw new IllegalArgumentException("The key's class is unhandled: "+key.getClass().getSimpleName());
	}

	public boolean isProceduralKey()
	{
		return proceduralKey!=null;
	}
	
	public boolean isDatastoreKey()
	{
		return key!=null;
	}
	
	public Key getDatastoreKey()
	{
		return key;
	}
	
	public String getProceduralKey()
	{
		return proceduralKey;
	}

	
	@Override
	public String toString()
	{
		if (key!=null)
			return KeyFactory.keyToString(key);
		else if (proceduralKey!=null)
			return proceduralKey;
		
		return "(Not initialized)";
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof InitiumKey)
		{
			return obj.toString().equals(this.toString());
		}
		
		return super.equals(obj);
	}

}
