package com.universeprojects.miniup.server.commands;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Command to allow a player to drink beer.
 *
 * @author Fayyne
 */
public class CommandDrinkBeer extends Command {
    public CommandDrinkBeer(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
        super(db, request, response);
    }

    @Override
    public void run(Map<String, String> parameters) throws UserErrorMessage {
        ODPDBAccess db = getDB();
        CachedDatastoreService ds = getDS();
        CachedEntity character = db.getCurrentCharacter();
        CachedEntity user = db.getCurrentUser();
        MainPageUpdateService service = new MainPageUpdateService(db, user, character, null, this);

        db.doDrinkBeer(ds, character);
        service.updateInBannerCharacterWidget(user, character);
    }

}
