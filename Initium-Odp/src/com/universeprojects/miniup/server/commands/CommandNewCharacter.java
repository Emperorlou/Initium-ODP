package com.universeprojects.miniup.server.commands;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPAuthenticator;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * Create group command.
 * 
 * @author Atmostphear
 */

public class CommandNewCharacter extends Command
{
	/**
	 * Max length of group name
	 */
	public static final int MAX_GROUP_NAME_LENGTH = 40;

	/**
	 * Command to create a group.
	 * 
	 * Parameters: groupName - The name of the group being created.
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandNewCharacter(final ODPDBAccess db,
			final HttpServletRequest request, final HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public final void run(final Map<String, String> parameters)
			throws UserErrorMessage
	{
		ODPAuthenticator auth = getAuthenticator(); 
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		String name = WebUtils.getStrParam(request, "name", true);
		CachedEntity user = db.getCurrentUser();
		
		if (user==null)
		{
			throw new UserErrorMessage("Please login first before creating a new character.");
		}
		
		name = GameUtils.cleanCharacterName(name);

		db.isCharacterNameOk(request, name);
			
//			String ip = WebUtils.getClientIpAddr(request);
//			CachedDatastoreService ds = db.getDB();
//			if (ds.flagActionLimiter("signupIPLimiter"+ip, 600, 1))
//				throw new UserErrorMessage("Please report this error to the admin.");
			

		// Now check that we have enough character slots
		Long maximumCharacterCount = db.getUserCharacterSlotCount(user);
		
		List<CachedEntity> characterList = db.getUserCharacters(user);			
		if (characterList.size()>=maximumCharacterCount)
			throw new UserErrorMessage("You cannot create another character because you only have "+maximumCharacterCount+" character slots available and they are all used. " +
					"<a onclick='viewProfile()'>Click here</a> to get more character slots.");
		
		// CHeck if this is their first character ever. If so, add a welcome message. If not, don't.
		boolean isNewUser = false;
		if (characterList.isEmpty())
			isNewUser = true;
		
		CachedEntity character = db.newPlayerCharacter(null, auth, user, name, null);
		
		ds.put(character);

		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, user, character, db.getEntity((Key)character.getProperty("locationKey")), this);
		mpus.updateFullPage_shortcut();
		
//		if (isNewUser)
//		{
//			// if this user signed up because of a referral, give credit where credit is due!
//			String referrerCharacterHtml = null;
//			CachedEntity referrerUser = db.getEntity((Key)user.getProperty("referrerKey"));
//			if (referrerUser!=null)
//			{
//				CachedEntity referrerCharacter = db.getEntity((Key)referrerUser.getProperty("characterKey"));
//				if (referrerCharacter!=null)
//					referrerCharacterHtml = GameUtils.renderCharacter(referrerUser, referrerCharacter);
//			}
//
//			String referrerHeaderEntry = (String)user.getProperty("referrerHeaderEntry");
//			String fromWhere = "";
//			if (referrerHeaderEntry!=null && 
//							(referrerHeaderEntry.contains("facebook.com") || 
//							referrerHeaderEntry.contains("reddit.com") || 
//							referrerHeaderEntry.contains("twitter.com")))
//				fromWhere =" <a href='"+referrerHeaderEntry+"' target='_blank'>from here</a>";
//
//			if (ODPDBAccess.welcomeMessagesLimiter==0 || new Random().nextInt(ODPDBAccess.welcomeMessagesLimiter)==0)
//			{
//				String limitTag = "";
//				if (ODPDBAccess.welcomeMessagesLimiter>0)
//					limitTag = " (+"+(ODPDBAccess.welcomeMessagesLimiter-1)+" others as well)";
//				if (referrerCharacterHtml!=null)
//					db.sendGlobalMessage(db.getDB(), GameUtils.renderCharacter(user, character)+" just joined Initium"+fromWhere+" thanks to "+referrerCharacterHtml+"!"+limitTag);
//				else
//					ServletMessager.addGlobalMessage(db.getDB(), GameUtils.renderCharacter(user, character)+" just joined Initium"+fromWhere+"!"+limitTag);
//			}
//			
//			WebUtils.askForRedirectClientTo("main.jsp?welcome=true", request, response);
//		}
//		else
//			WebUtils.askForRedirectClientTo("main.jsp", request, response);
			
	}
}