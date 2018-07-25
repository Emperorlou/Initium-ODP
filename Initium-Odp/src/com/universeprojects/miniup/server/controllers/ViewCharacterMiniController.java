package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.ShardedCounterService;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

/**
 * Controller for the ViewCharacterMini page.
 * Available request params:
 *
**/

@Controller
public class ViewCharacterMiniController extends PageController {

	public ViewCharacterMiniController() {
		super("viewcharactermini");
	}
	
	@Override
	protected String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException 
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}
		
		Long charId = WebUtils.getLongParam(request, "characterId");
		Key charKey = KeyFactory.createKey("Character", charId);
		CachedEntity character = db.getEntity(charKey); 
		if (character==null)
		{
			response.sendError(404);
			return null;
		}
		CachedEntity user = db.getEntity((Key)character.getProperty("userKey"));
	
		/**
		boolean isSelf = false;
		if ((user!=null && auth.getAuthenticatedUserId()!=null && auth.getAuthenticatedUserId()==user.getKey().getId()) || 
				(auth.getAuthenticatedInstantCharacterId()!=null && auth.getAuthenticatedInstantCharacterId().equals(characterKey.getId())))
			isSelf = true;
		request.setAttribute("isSelf", isSelf); 
		**/
		
		boolean isCloaked = false;
		if (GameUtils.equals(character.getProperty("cloaked"), true))
			isCloaked = true;
		request.setAttribute("isCloaked", isCloaked);
		
		
		String type = (String)character.getProperty("type");
		if (type==null || type.equals(""))
			type = "PC";
		request.setAttribute("type", type);		
	
		// If the character we're trying to inspect is an NPC and he is still alive, don't allow the inspection
		if (type.equals("NPC") && (Double)character.getProperty("hitpoints")>0d)
			throw new RuntimeException("Should not have been able to view this character.");
		
		// isSelf is false by default, check if character is not an NPC and after that check if chosen character is self.
		boolean isSelf = false;
		
		if (!type.equals("NPC") && user!=null)
			isSelf = GameUtils.equals(user.getKey(), db.getCurrentUserKey());
		
		request.setAttribute("isSelf", isSelf);	
	
		// If character investigated is self also show weight.
		if (isSelf)
		{
			long carrying = db.getCharacterCarryingWeight(character);
			long maxCarrying = db.getCharacterMaxCarryingWeight(character);
			
			String carrySummary = "Inventory weight: <div class='main-item-subnote'>"+GameUtils.formatNumber(new Double(carrying)/1000d, false)+"/"+GameUtils.formatNumber(new Double(maxCarrying)/1000d, false)+"kg ("+GameUtils.formatPercent(new Double(carrying)/new Double(maxCarrying))+")</div>";
			request.setAttribute("inventoryWeight", carrySummary);
		}
		
		Key groupKey = (Key)character.getProperty("groupKey");
		CachedEntity group = db.getEntity(groupKey);
		if (group!=null)
		{
			request.setAttribute("groupName", group.getProperty("name"));		
			request.setAttribute("groupRank", character.getProperty("groupRank"));		
			request.setAttribute("groupStatus", character.getProperty("groupStatus"));
			
			if ("Admin".equals(request.getAttribute("groupStatus")) || "Member".equals(request.getAttribute("groupStatus")))
				request.setAttribute("inGroup", true);
		}
		
		if (character.getProperty("partyCode")!=null && character.getProperty("partyCode").equals("")==false)
			request.setAttribute("isPartied", true);
		
		List<CachedEntity> buffs = db.getBuffsFor(character.getKey());
		if (buffs!=null && buffs.isEmpty()==false)
			request.setAttribute("buffs", true);
	
		
		List<CachedEntity> achievements = null;
		if (user!=null && (List<Key>)user.getProperty("achievements")!=null)
		{
			achievements = db.getEntities((List<Key>)user.getProperty("achievements"));
			for (CachedEntity achievement:achievements)
			{
				if (achievement!=null && achievement.getProperty("pointValue")==null)
				{
					achievement.setProperty("pointValue", 0);
				}
			}
			Collections.sort(achievements, new Comparator<CachedEntity>() 
			{
				public int compare(CachedEntity o1, CachedEntity o2)
				{
					Long o1PointValue = 0l;
					Long o2PointValue = 0l;
					if (o1!=null && o1.getProperty("pointValue")!=null)
						o1PointValue = ((Number)o1.getProperty("pointValue")).longValue();
					if (o2!=null && o2.getProperty("pointValue")!=null)
						o2PointValue = ((Number)o2.getProperty("pointValue")).longValue();
					
					return o1PointValue.compareTo(o2PointValue);
				}
			});
		}
		if (achievements!=null && achievements.isEmpty()==false)
			request.setAttribute("hasAchievements", true);
		
	
		request.setAttribute("characterName", character.getProperty("name"));
		
		// Referral stuff
		if (user!=null)
		{
			ShardedCounterService sc = ShardedCounterService.getInstance(db.getDB());
			Long referralViews = sc.readCounter(user.getKey(), "referralViews");
			Long referralSignups = sc.readCounter(user.getKey(), "referralSignups");
			Long referralDonations = sc.readCounter(user.getKey(), "referralDonations");
			Long premiumGiftsGiven = (Long)user.getProperty("premiumGiftsGiven");
			if (referralViews==null) referralViews = 0L;
			if (referralSignups==null) referralSignups = 0L;
			if (referralDonations==null) referralDonations = 0L;
			if (premiumGiftsGiven==null) premiumGiftsGiven = 0L;
			
			request.setAttribute("referralViews", GameUtils.formatNumber(referralViews));
			request.setAttribute("referralSignups", GameUtils.formatNumber(referralSignups));
			request.setAttribute("referralDonations", "$"+GameUtils.formatNumber(referralDonations.doubleValue()/100d, true));
			request.setAttribute("premiumGiftsGiven", GameUtils.formatNumber(premiumGiftsGiven));
			
			boolean isPremium = CommonChecks.checkUserIsPremium(user);
			request.setAttribute("isPremium", isPremium);
		}
		
		// Printing buffs
		StringBuilder sb = new StringBuilder();
		
		for(CachedEntity buff:buffs)
		{
			String iconUrl = GameUtils.getResourceUrl(buff.getProperty("icon"));
			sb.append("<img src='" + iconUrl + "' border='0'>");
		}
		request.setAttribute("printBuff", sb.toString());
	
		// Printing achievements
		
		sb = new StringBuilder();
		if (achievements!=null)
			for(CachedEntity achievement:achievements)
			{
				String iconUrl = GameUtils.getResourceUrl(achievement.getProperty("icon"));
				sb.append("<img src='" + iconUrl + "' border='0'>");
			}
		request.setAttribute("printAchievement", sb.toString());
		
		// Printing the equipment list with the EquipmentControl
					
		sb = new StringBuilder();
		
		for(String slot:ODPDBAccess.EQUIPMENT_SLOTS)
			{
				sb.append("<div class='main-item'>"+slot+": ");
				CachedEntity item = db.getEntity((Key)character.getProperty("equipment"+slot));
				if(item == null)
					sb.append("None");
				else
				{
					sb.append(" <div class='main-item-container'>");
					sb.append(GameUtils.renderItem(db, request, null, item, true, false));
					sb.append("</div>");
				}
				sb.append("</div>\n");
			}
		request.setAttribute("equipList", sb.toString());
		
		// Starting a chat	
		String characterNameStr = character.getProperty("name").toString().replaceAll("<.*?>", "");
		long characterIDKey = character.getKey().getId();
		long characterID = character.getId();
		request.setAttribute("characterNameStr", characterNameStr);
		request.setAttribute("characterIDKey", characterIDKey);
		request.setAttribute("characterID", characterID);
					
		// Character widget
		request.setAttribute("characterWidget", GameUtils.renderCharacterWidget(null, request, db, character, null, group, true, false, true, true, true));			
					
		// Buff list
		request.setAttribute("buffList", GameUtils.renderBuffsList(buffs));		
		
		// Achievement list
		request.setAttribute("achievementList", GameUtils.renderAchievementsList(achievements));	
		
		// Check character stats
		request.setAttribute("getStrength", GameUtils.formatNumber(character.getProperty("strength")));  
		request.setAttribute("characterStrength", GameUtils.formatNumber(db.getCharacterStrength(character)));
		request.setAttribute("getDexterity", GameUtils.formatNumber(character.getProperty("dexterity")));
		request.setAttribute("characterDexterity", GameUtils.formatNumber(db.getCharacterDexterity(character)));
		request.setAttribute("getIntelligence", GameUtils.formatNumber(character.getProperty("intelligence"))); 
		request.setAttribute("characterIntelligence", GameUtils.formatNumber(db.getCharacterIntelligence(character)));
	
					
		return "/WEB-INF/odppages/viewcharactermini.jsp";
	}
}