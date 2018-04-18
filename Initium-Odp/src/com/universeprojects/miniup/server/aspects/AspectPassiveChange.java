package com.universeprojects.miniup.server.aspects;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.universeprojects.cacheddatastore.CachedEntity;
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
		return (Date)getProperty("lastUpdate");
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
			for(int i = passiveChanges.size()-1; i>=0; i--)
			{
				EmbeddedEntity passiveChange = passiveChanges.get(i);
				
				if (update(passiveChange)==true)
				{
					passiveChanges.remove(i);
					changeOccurred = true;
				}
			}
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
		Date changeDate = (Date)passiveChange.getProperty("changeDatetime");
		// If no change date was given lets just do the change now
		if (changeDate==null) return true;
		
		return getCurrentDate().after(changeDate);
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
