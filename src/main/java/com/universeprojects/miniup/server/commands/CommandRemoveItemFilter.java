package com.universeprojects.miniup.server.commands;

import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ItemFilterService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class CommandRemoveItemFilter extends Command {
    public CommandRemoveItemFilter(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
        super(db, request, response);
    }

    @Override
    public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {
        String name = parameters.get("name");

        ItemFilterService ifs = new ItemFilterService(db);

        String css = ifs.removeItemFilter(name);

        db.getDB().put(db.getCurrentCharacter());
        db.sendGameMessage("You've removed the item filter for <c class=" + css + ">" + name + "</c>." +
                " <a onclick='loadItemFilterList()'>[View your active item filters]</a>");
    }
}
