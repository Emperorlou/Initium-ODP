package com.universeprojects.miniup.server.aspects;

import java.util.Date;
import java.util.List;
import java.util.Random;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.gef.GEFUtils;
import com.universeprojects.gef.PropertyContainerCachedEntity;
import com.universeprojects.gef.PropertyContainerEmbeddedEntity;
import com.universeprojects.miniup.server.InitiumAspect;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ODPDBAccess;

public class AspectPassiveChange extends InitiumAspect
{
	private String lockKey = null;
	Date currentDate;

	public AspectPassiveChange(InitiumObject object)
	{
		super(object);
	}

	public Date getLastUpdate()
	{
		Date lastUpdate = (Date)getProperty("lastUpdate");
		if (lastUpdate==null) lastUpdate = (Date)entity.getProperty("createdDate");
		if (lastUpdate==null) lastUpdate = new Date();
		setLastUpdate(lastUpdate);
		return lastUpdate;
	}
	
	public void setLastUpdate(Date date)
	{
		setProperty("lastUpdate", date);
	}
	
	@SuppressWarnings("unchecked")
	public List<EmbeddedEntity> getPassiveChanges()
	{
		return (List<EmbeddedEntity>)getProperty("passiveChanges");
	}
	
	public void setPassiveChanges(List<EmbeddedEntity> passiveChanges)
	{
		setProperty("passiveChanges", passiveChanges);
		
		if (passiveChanges!=null && passiveChanges.isEmpty()==false)
		{
			EmbeddedEntity passiveChangeEntry = passiveChanges.get(0);
			Long seconds = getEntryWaitTimeSeconds(passiveChangeEntry);
			
			Date nextUpdate = new Date(getLastUpdate().getTime() + (seconds*1000));
			setNextUpdateDate(nextUpdate);
		}
	}

	private Long getEntryWaitTimeSeconds(EmbeddedEntity passiveChangeEntry)
	{
		String curve = (String)passiveChangeEntry.getProperty("changeDatetime");
		if (curve==null) return 0L;
		Date lastUpdate = getLastUpdate();
		Random rnd = null;
		if (lastUpdate==null)
			rnd = new Random(entity.getId());
		else
			rnd = new Random(lastUpdate.getTime());
		
		
		Long seconds = db.solveCurve_Long(rnd, curve);
		return seconds;
	}
	
	
	
	/**
	 * 
	 * @return True if update occurred
	 */
	public boolean update()
	{
		List<EmbeddedEntity> passiveChanges = getPassiveChanges();
		if (passiveChanges==null) return false;
		
		boolean changeOccurred = false;
		try
		{
			EmbeddedEntity passiveChange = passiveChanges.get(0);
			
			if (update(passiveChange)==true)
			{
				passiveChanges.remove(0);
				changeOccurred = true;
			}

			setPassiveChanges(passiveChanges);
			
			releaseLock();
		}
		catch(PassiveChangeLocked e)
		{
			// Lets just get out of here
		}
		
		return changeOccurred;
	}
	
	private boolean update(EmbeddedEntity passiveChange) throws PassiveChangeLocked
	{
		if (isTimeToUpdate(passiveChange)==false)
			return false;
		
		if (acquireLock()==false)
			throw new PassiveChangeLocked();
		
		EmbeddedEntity changeMap = (EmbeddedEntity)passiveChange.getProperty("fieldChangesSelf");
		boolean selfChanged = db.applyFieldChanges(this.entity, changeMap);
		
		EmbeddedEntity entityModifier = (EmbeddedEntity)passiveChange.getProperty("selfModifier");
		db.performModifierTypeOperation(new PropertyContainerCachedEntity(this.entity), new PropertyContainerEmbeddedEntity(this.entity, entityModifier, entityModifier.getKey().getKind(), "doesn't matter here"));
		
		return selfChanged;
	}

	private String getLockKey()
	{
		if (lockKey==null)
			lockKey = "PassiveChangeLock-"+this.entity.toString();
		
		return lockKey;
	}
	
	private boolean acquireLock()
	{
		return db.getDB().getLock(getLockKey(), 30);
	}
	
	private void releaseLock()
	{
		db.getDB().releaseLock(getLockKey());
	}
	
	private Date getCurrentDate()
	{
		if (currentDate==null)
			currentDate = new Date();
		
		return currentDate;
	}
	
	
	private boolean isTimeToUpdate(EmbeddedEntity passiveChange)
	{
		Long seconds = getEntryWaitTimeSeconds(passiveChange);
		// If no change date was given lets just do the change now
		if (seconds<=0) return true;
		
		return System.currentTimeMillis()>getLastUpdate().getTime()+(seconds*1000);
	}
	
	public Date getNextUpdateDate()
	{
		return (Date)this.getProperty("nextUpdate");
	}
	
	public void setNextUpdateDate(Date date)
	{
		setProperty("nextUpdate", date);
	}
	

	public static boolean update(ODPDBAccess db, CachedEntity item)
	{
		InitiumObject initiumObject = new InitiumObject(db, item);
		if (initiumObject.hasAspect("PassiveChange"))
		{
			AspectPassiveChange passiveChange = (AspectPassiveChange)initiumObject.getAspect("PassiveChange");
			return passiveChange.update();
		}
		return false;
	}
	
	
	
	public class PassiveChangeLocked extends Exception
	{
		private static final long serialVersionUID = 1L;
		
	}
}
