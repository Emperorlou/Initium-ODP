package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.TransactionCommand;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandCombineChippedTokens extends TransactionCommand
{

	public CommandCombineChippedTokens(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void runBeforeTransaction(Map<String, String> parameters) throws UserErrorMessage
	{
		
	}

	@Override
	public void runInsideTransaction(Map<String, String> parameters) throws UserErrorMessage
	{
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		Long itemId = tryParseId(parameters, "itemId");
		if (itemId==null)
			throw new IllegalArgumentException("Missing itemId.");
		CachedEntity chippedTokens = db.getEntity("Item", itemId);
		if (chippedTokens==null)
			throw new UserErrorMessage("The chipped tokens you selected no longer exist.");
		
		if (CommonChecks.checkItemIsChippedToken(chippedTokens)==false)
			throw new UserErrorMessage("The item you selected is not a stack of Chipped Tokens. Make sure to select a stack of at least 100 Chipped Tokens first.");
		
		if (CommonChecks.checkItemIsInCharacterInventory(chippedTokens, character.getKey())==false)
			throw new UserErrorMessage("The item you're trying to split is not in the character's inventory. You must have the premium token in-hand to split it.");

		Long quantity = (Long)chippedTokens.getProperty("quantity");
		
		if (quantity<100)
			throw new UserErrorMessage("In order to create a Premium Membership Token, you need to have at least 100 Chipped Tokens in the stack.");
		
		
		// Now do the combination...
		if (quantity==100L)
		{
			db.getDB().delete(chippedTokens);
		}
		else if (quantity>100L)
		{
			quantity-=100L;
			chippedTokens.setProperty("quantity", quantity);
			db.getDB().put(chippedTokens);
		}
		else
			throw new RuntimeException("WHOA BAD PROBLEM");
		db._newPremiumMembershipToken(character.getKey());
		
		setPopupMessage("A new Premium Membership Token was created in your inventory from 100 Chipped Tokens.");
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

	@Override
	public void runAfterTransaction(Map<String, String> parameters) throws UserErrorMessage
	{
		
	}

}
