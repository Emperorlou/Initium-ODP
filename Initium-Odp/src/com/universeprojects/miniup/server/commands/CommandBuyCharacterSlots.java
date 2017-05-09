package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.TransactionCommand;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandBuyCharacterSlots extends TransactionCommand
{

	public CommandBuyCharacterSlots(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void runBeforeTransaction(Map<String, String> parameters) throws UserErrorMessage
	{
		CachedEntity user = db.getCurrentUser();
		int characterCount = db.getUserCharacters(user).size();
		request.setAttribute("characterCount", characterCount);
	}

	@Override
	public void runInsideTransaction(Map<String, String> parameters) throws UserErrorMessage
	{
		CachedEntity user = db.getCurrentUser();
		refetch(user);
		
		if (user==null) throw new UserErrorMessage("You can only get extra character slots if you're not using a throwaway account.");
		
		if (CommonChecks.checkUserIsPremium(user)==false)
			throw new UserErrorMessage("You can only get additional character slots if your account is already a premium account.");
		
		Long availableCredit = (Long)user.getProperty("totalDonations");
		if (availableCredit == null) availableCredit = 0L;
		
		if (availableCredit<500)
			throw new UserErrorMessage("You need at least 5 USD in donation credit to get additional character slots.");
		
		Long maxCharacterCount = db.getUserCharacterSlotCount(user);
		int characterCount = (Integer)request.getAttribute("characterCount");
		Long rawMaxCount = (Long)user.getProperty("maximumCharacterCount");
		
		user.setProperty("totalDonations", availableCredit-500L);
		if (rawMaxCount==null && characterCount>maxCharacterCount)
			maxCharacterCount = characterCount+8L;
		else
			maxCharacterCount+=8;
		user.setProperty("maximumCharacterCount", maxCharacterCount);
		
		db.getDB().put(user);
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
		setPopupMessage("You now have 8 more character slots for a total of: "+maxCharacterCount);
	}

	@Override
	public void runAfterTransaction(Map<String, String> parameters) throws UserErrorMessage
	{
		
	}


}
