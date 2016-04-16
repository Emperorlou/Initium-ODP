package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Random;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;


/**
 * Does one of the following:
 *  - If has Buff Blessing of the Ebon Raven: Nothing
 *  - If not equipped Raven Guards: Nothing
 *  - If not full HP: Set Buff 1h with no modifiers, heals HP
 *  - If dogecoins < 1000: Set Buff 1h with no modifiers, dogecoins +1000
 *  - If has Buff Well Rested+Buff Pumped: Set Buff 30m with all stats +1
 *  - Set Buff 30m with random stat +1
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
			for(CachedEntity buff:buffs)
			{
				if ("Blessing of the Ebon Raven".equals(buff.getProperty("name")))
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
		setJavascriptResponse(JavascriptResponse.FullPageRefresh);

		// Check for Hp trigger and if met, set buff effect.
		Double curHp = (Double)character.getProperty("hitpoints");
		Double maxHp = (Double)character.getProperty("maxHitpoints");
		if (curHp!=null && maxHp!=null && curHp>0 && curHp<maxHp)
		{
			ds.put(db.awardBuff(ds, character.getKey(), "/images/small/Pixel_Art-Icons-Holy-S_Holy07.png", "Blessing of the Ebon Raven", "The spirit of the Ebon Raven has blessed you. It left you restored.", 3600, null, null, null, null, null, null, 1));
			character.setProperty("hitpoints", maxHp);
			ds.put(character);
			return;
		}
		
		// Check for dogecoin trigger and if met, set buff effect.
		Long dogecoins = (Long)character.getProperty("dogecoins");
		if (dogecoins!=null && dogecoins<1000)
		{
			ds.put(db.awardBuff(ds, character.getKey(), "/images/small/Pixel_Art-Icons-Holy-S_Holy07.png", "Blessing of the Ebon Raven", "The spirit of the Ebon Raven has blessed you. It left you enriched.", 3600, null, null, null, null, null, null, 1));
			character.setProperty("dogecoins", dogecoins+1000);
			ds.put(character);
			return;
		}
		
		// Check for buff trigger and if met, set buff effect.
		if (buffs!=null && buffs.isEmpty()==false)
		{
			boolean trigger1 = false;
			boolean trigger2 = false;
			for(CachedEntity buff:buffs)
			{
				String name = (String)buff.getProperty("name");
				if ("Well Rested".equals(name))
					trigger1 = true;
				if ("Pumped!".equals(name))
					trigger2 = true;
			}
			if (trigger1 && trigger2)
			{
				ds.put(db.awardBuff(ds, character.getKey(), "/images/small/Pixel_Art-Icons-Holy-S_Holy07.png", "Blessing of the Ebon Raven", "The spirit of the Ebon Raven has blessed you. It left you enchanted.", 1800, "strength", "+1", "dexterity", "+1", "intelligence", "+1", 1));
				return;
			}
		}
		
		// all previous triggers failed, set buff effect
		Random rnd = new Random();
		int stat = rnd.nextInt(3);
		switch (stat) {
		case 0:
			ds.put(db.awardBuff(ds, character.getKey(), "/images/small/Pixel_Art-Icons-Holy-S_Holy07.png", "Blessing of the Ebon Raven", "The spirit of the Ebon Raven has blessed you. It left you empowered.", 1800, "strength", "+1", null, null, null, null, 1));
			return;
		case 1:
			ds.put(db.awardBuff(ds, character.getKey(), "/images/small/Pixel_Art-Icons-Holy-S_Holy07.png", "Blessing of the Ebon Raven", "The spirit of the Ebon Raven has blessed you. It left you invigorated.", 1800, "dexterity", "+1", null, null, null, null, 1));
			return;
		case 2:
			ds.put(db.awardBuff(ds, character.getKey(), "/images/small/Pixel_Art-Icons-Holy-S_Holy07.png", "Blessing of the Ebon Raven", "The spirit of the Ebon Raven has blessed you. It left you enlightened.", 1800, "intelligence", "+1", null, null, null, null, 1));
			return;
		}
		
	}

}
