package com.universeprojects.miniup.server.commands;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.TransactionCommand;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;

public class CommandSplitPremiumToken extends TransactionCommand
{

	public CommandSplitPremiumToken(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
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
		CachedEntity premiumMembershipToken = db.getEntity("Item", itemId);
		if (premiumMembershipToken==null)
			throw new UserErrorMessage("The premium token you selected no longer exists.");
		
		if (CommonChecks.checkItemIsPremiumToken(premiumMembershipToken)==false)
			throw new UserErrorMessage("The item you're trying to split is not a valid premium token.");
		
		if (CommonChecks.checkItemIsInCharacterInventory(premiumMembershipToken, character.getKey())==false)
			throw new UserErrorMessage("The item you're trying to split is not in your inventory. You must have the premium token in-hand to split it.");

		CachedEntity chippedToken = new CachedEntity("Item");
		chippedToken.setProperty("name", "Chipped Token");
		chippedToken.setProperty("icon", "images/small2/Chipped-Token1.gif");
		chippedToken.setProperty("forcedItemQuality", "Average");
		chippedToken.setProperty("itemType", "Other");
		chippedToken.setProperty("description", "A single Chipped Token is worth 1/100th of a Premium Membership Token. " +
				"If you have 100 Chipped Tokens, you can turn them into a Premium Membership Token by clicking the button below. " +
				"Likewise, Premium Membership Tokens have a button that allows you split them up into Chipped Tokens.");
		chippedToken.setProperty("quantity", 100L);
		chippedToken.setProperty("itemClass", "Donation Credit");
		chippedToken.setProperty("containerKey", premiumMembershipToken.getProperty("containerKey"));
		chippedToken.setProperty("movedTimestamp", new Date());
		chippedToken.setPropertyManually("premiumTokenType", "Chipped Token");
		
		db.getDB().delete(premiumMembershipToken);
		db.getDB().put(chippedToken);
		
		setPopupMessage("A Premium Membership Token was split up into a single stack of 100 Chipped Tokens.");
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

	@Override
	public void runAfterTransaction(Map<String, String> parameters) throws UserErrorMessage
	{
	}

}
