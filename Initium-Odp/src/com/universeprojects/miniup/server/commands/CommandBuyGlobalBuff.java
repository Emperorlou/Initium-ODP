package com.universeprojects.miniup.server.commands;

import com.universeprojects.cacheddatastore.AbortTransactionException;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.RevenueService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class CommandBuyGlobalBuff extends Command {
    public CommandBuyGlobalBuff(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
        super(db, request, response);
    }

    @Override
    public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {
        Long id = parseLong("id");

        CachedEntity globalBuff = db.getEntity(db.createKey("GlobalBuff", id));
        if(globalBuff == null)
            throw new UserErrorMessage("Buff doesn't exist.");

        RevenueService rs = new RevenueService(db);

        try{
            rs.buyGlobalBuff(db.getCurrentUser(), globalBuff);
        }
        catch(AbortTransactionException ate){
            throw new UserErrorMessage("Aborted transaction: " + ate.getMessage());
        }

    }
}
