package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Random;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;


/**
 * Does one of the following:
 *  - If has Buff Blessing of the Ebon Raven: Nothing
 *  - If not equipped Raven Guards: Nothing
 *  - If not full HP: Set Buff "BlessRaven - Restored", heal HP
 *  - If dogecoins < 1000: Set Buff "BlessRaven - Enriched", gain 1000 dogecoins
 *  - If has Buff Well Rested+Buff Pumped: Set Buff "BlessRaven - Enchanted"
 *  - Set random Buff "BlessRaven - Empowered|Invigorated|Enlightened"
 * 
 * Usage notes:
 * Checks if caller is at Burial Site of The Ebon Raven and if so adds buff
 * according to the rules above. 
 * 
 * Parameters:
 * 		none 
 * 
 * @author NJ
 *
 */
public class CommandRavenPayRespects extends Command {

	public CommandRavenPayRespects(HttpServletRequest request, HttpServletResponse response) 
	{
		super(request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		CachedEntity character = db.getCurrentCharacter(request);
		
		// Verify the caller is at Burial Site of The Ebon Raven
		Key key = (Key)character.getProperty("locationKey");
		if (key==null)
			throw new UserErrorMessage("You cannot do that here.");
		if (key.getId()!=4808618326097920L)
			throw new UserErrorMessage("You cannot do that here.");
		
		// Get all buffs and check for Blessing of the Raven
		List<CachedEntity> buffs = db.getBuffsFor(character.getKey());
		if (buffs!=null && buffs.isEmpty()==false)
		{
			for(CachedEntity b:buffs)
			{
				if ("Blessing of the Ebon Raven".equals(b.getProperty("name")))
				{
					setPopupMessage("You feel the spirit of the Ebon Raven's gaze upon you, as if trying to figure out if it had been a mistake to bless you in the first place.");
					return;
				}
			}
		}
		
		// Check if Raven Guards are equipped
		CachedEntity item = db.getEntity((Key)character.getProperty("equipmentGloves"));
		if (item==null || "Raven Guards".equals(item.getProperty("name"))==false)
		{
			setPopupMessage("You feel the spirit of the Ebon Raven's gaze upon you, as if trying to figure out if you are being sincere.");
			return;
		}
		
		// At this point, we're always at least setting the buff, so get ds
		// and set Refresh for client so buff shows up
		CachedDatastoreService ds = getDS();
		CachedEntity buff;
		setJavascriptResponse(JavascriptResponse.FullPageRefresh);
		
		// Check for Hp trigger and if met, set buff effect.
		Double curHp = (Double)character.getProperty("hitpoints");
		Double maxHp = (Double)character.getProperty("maxHitpoints");
		if (curHp!=null && maxHp!=null && curHp>0 && curHp<maxHp)
		{
			buff = db.awardBuff(ds, character.getKey(), db.getBuffDefByInternalName(ds, "BlessRaven - Restored"));
			if (buff != null)
			{
				ds.put(buff);
				character.setProperty("hitpoints", maxHp);
				ds.put(character);
				return;
			}
			throw new UserErrorMessage("Error while setting buff. Please contact a Dev.");
		}
		
		// Check for dogecoin trigger and if met, set buff effect.
		Long dogecoins = (Long)character.getProperty("dogecoins");
		if (dogecoins!=null && dogecoins<1000)
		{
			buff = db.awardBuff(ds, character.getKey(), db.getBuffDefByInternalName(ds, "BlessRaven - Enriched"));
			if (buff != null)
			{
				ds.put(buff);
				character.setProperty("dogecoins", dogecoins+1000);
				ds.put(character);
				return;
			}
			throw new UserErrorMessage("Error while setting buff. Please contact a Dev.");
		}
		
		// Check for buff trigger and if met, set buff effect.
		if (buffs!=null && buffs.isEmpty()==false)
		{
			boolean trigger1 = false;
			boolean trigger2 = false;
			for(CachedEntity b:buffs)
			{
				String name = (String)b.getProperty("name");
				if ("Well Rested".equals(name))
					trigger1 = true;
				if ("Pumped!".equals(name))
					trigger2 = true;
			}
			if (trigger1 && trigger2)
			{
				buff = db.awardBuff(ds, character.getKey(), db.getBuffDefByInternalName(ds, "BlessRaven - Enchanted"));
				if (buff != null)
				{
					ds.put(buff);
					return;
				}
				throw new UserErrorMessage("Error while setting buff. Please contact a Dev.");
			}
		}
		
		// all previous triggers failed, set buff effect
		Random rnd = new Random();
		int stat = rnd.nextInt(3);
		switch (stat) {
		case 0:
			buff = db.awardBuff(ds, character.getKey(), db.getBuffDefByInternalName(ds, "BlessRaven - Empowered"));
			if (buff != null)
			{
				ds.put(buff);
				return;
			}
			throw new UserErrorMessage("Error while setting buff. Please contact a Dev.");
		case 1:
			buff = db.awardBuff(ds, character.getKey(), db.getBuffDefByInternalName(ds, "BlessRaven - Invigorated"));
			if (buff != null)
			{
				ds.put(buff);
				return;
			}
			throw new UserErrorMessage("Error while setting buff. Please contact a Dev.");
		case 2:
			buff = db.awardBuff(ds, character.getKey(), db.getBuffDefByInternalName(ds, "BlessRaven - Enlightened"));
			if (buff != null)
			{
				ds.put(buff);
				return;
			}
			throw new UserErrorMessage("Error while setting buff. Please contact a Dev.");
		}
		
	}

}
