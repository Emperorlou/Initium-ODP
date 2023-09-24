package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * Toggle cloak command.
 * 
 * @author Atmostphear
 * @author SPFiredrake
 * 
 */
public class CommandToggleCloak extends Command
{

	/**
	 * Command toggles cloaking, enabling or disabling based on current character entity state
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandToggleCloak(final ODPDBAccess db,
			final HttpServletRequest request, final HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public final void run(final Map<String, String> parameters)
			throws UserErrorMessage
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");

		if ("COMBAT".equals(character.getProperty("mode")))
		{
			throw new UserErrorMessage("You cannot cloak in combat.");
		}

		character.setProperty("cloaked", !Boolean.TRUE.equals(character.getProperty("cloaked")));
		ds.put(character);

		updateHtml("#"+parameters.get("buttonId"), HtmlComponents.generateToggleCloak(character));
		// Update character widget.
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, db.getCurrentUser(), character, null, this);
		mpus.updateInBannerCharacterWidget();
	}
}