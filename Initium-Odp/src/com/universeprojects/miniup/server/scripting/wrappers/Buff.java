package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.services.EmbeddedEntity;
import com.universeprojects.miniup.server.services.ScriptService;

public class Buff extends EntityWrapper {
	protected EntityWrapper parentEntity;
	public Buff(CachedEntity entity, ODPDBAccess db) 
	{
		super(entity, db);
	}
	
	public Buff(CachedEntity entity, ODPDBAccess db, EntityWrapper parentEntity)
	{
		this(entity, db);
		this.parentEntity = parentEntity;
	}
	
	public Buff(EmbeddedEntity entity, ODPDBAccess db, EntityWrapper parentEntity)
	{
		this(WrapEmbeddedEntity(entity), db, parentEntity);
	}
	
	private CachedEntity WrapEmbeddedEntity(EmbeddedEntity entity)
	{
		CachedEntity ce = new CachedEntity(entity.getKey());
		for(String fieldName:entity.getProperties().keySet())
			ce.setProperty(fieldName, entity.getProperty(fieldName));
		ce.setAttribute("entity", entity);
	}
	
	public Date expiry()
	{
		return (Date)this.getProperty("expiry");
	}
	
	public String internalName()
	{
		return (String)this.getProperty("internalName");
	}
	
	public String field1Effect()
	{
		return (String)this.getProperty("field1Effect");
	}
	
	public String field1Name()
	{
		return (String)this.getProperty("field1Name");
	}
	
	public String field2Effect()
	{
		return (String)this.getProperty("field2Effect");
	}
	
	public String field2Name()
	{
		return (String)this.getProperty("field2Name");
	}
	
	public String field3Effect()
	{
		return (String)this.getProperty("field3Effect");
	}
	
	public String field3Name()
	{
		return (String)this.getProperty("field3Name");
	}
	
	public Map<String, String> buffEffects()
	{
		Map<String, String> effects = new HashMap<String, String>();
		if(this.field1Name() != null & this.field1Effect() != null)
			effects.put(this.field1Name(), this.field1Effect());
		if(this.field2Name() != null & this.field2Effect() != null)
			effects.put(this.field2Name(), this.field2Effect());
		if(this.field3Name() != null & this.field3Effect() != null)
			effects.put(this.field3Name(), this.field3Effect());
		return effects;
	}
	
	public EntityWrapper parentEntity()
	{
		if(this.parentEntity == null)
		{
			CachedEntity parEnt = db.getEntity(parentEntityKey());
			parentEntity = ScriptService.wrapEntity(parEnt, db);
		}
		return this.parentEntity;
	}
	
	public Key parentEntityKey()
	{
		if(parentEntity != null) return parentEntity.getKey();
		return (Key)this.getProperty("parentKey");
	}
	
	// This does NOT actually delete the buff, and it shouldn't. That should be handled by the ODP.
	public boolean inEffect()
	{
		if(this.expiry().before(new Date()))
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public void setProperty(String property, Object value)
	{
		super.setProperty(property, value);
		if(this.isEmbeddedEntity)
		{
			EmbeddedEntity ent = (EmbeddedEntity)this.wrappedEntity.getAttribute("entity");
			ent.setProperty(property, value);
		}
	}
}
