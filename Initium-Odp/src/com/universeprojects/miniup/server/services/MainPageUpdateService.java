package com.universeprojects.miniup.server.services;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;

public class MainPageUpdateService extends Service
{
	final private OperationBase operation;
	/**
	 * 
	 * @param db
	 * @param operation Pass in the Command or LongOperation that wants to make use of this service.
	 */
	public MainPageUpdateService(ODPDBAccess db, OperationBase operation)
	{
		super(db);
		this.operation = operation;
	}

	/**
	 * This updates the gold amount in the header bar.
	 * 
	 * @param character
	 */
	public void updateMoney(CachedEntity character)
	{
		Long gold = (Long)character.getProperty("dogecoins");
		String goldFormatted = GameUtils.formatNumber(gold);
		operation.updateHtmlContents("#mainGoldIndicator", goldFormatted);
	}
	
	/**
	 * This updates the html in the character widget that is in the top left corner of the banner.
	 * 
	 * @param userOfViewer
	 * @param character
	 * @param groupOfCharacter
	 */
	public void updateInBannerCharacterWidget(CachedEntity userOfViewer, CachedEntity character, CachedEntity groupOfCharacter)
	{
		String newHtml = GameUtils.renderCharacterWidget(db.getRequest(), db, character, userOfViewer, groupOfCharacter, true, true, false, false);
		operation.updateHtmlContents("#inBannerCharacterWidget", newHtml);
	}
}
