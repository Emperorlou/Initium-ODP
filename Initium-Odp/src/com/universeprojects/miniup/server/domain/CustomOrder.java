package com.universeprojects.miniup.server.domain;

import java.util.Date;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * These orders are for customizations that players would like to see for various things. This can include item customizations, houses...and more as we add them.
 * 
 * @author kyle-miller
 *
 */
public class CustomOrder extends OdpDomain {
	public static final String KIND = "CustomOrder";

	public CustomOrder() {
		super(new CachedEntity(KIND));
	}

	private CustomOrder(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	public static final CustomOrder wrap(CachedEntity cachedEntity) {
		return new CustomOrder(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 *  The date this order was created by the user.
	 *  
	 * @param createdDate
	 */
	public void setCreatedDate(Date createdDate) {
		getCachedEntity().setProperty("createdDate", createdDate);
	}

	public Date getCreatedDate() {
		return (Date) getCachedEntity().getProperty("createdDate");
	}

	/**
	 *  (CustomOrderType)
	 *  
	 * @param customOrderTypeKey
	 */
	public void setCustomOrderTypeKey(Key customOrderTypeKey) {
		getCachedEntity().setProperty("customOrderTypeKey", customOrderTypeKey);
	}

	public Key getCustomOrderTypeKey() {
		return (Key) getCachedEntity().getProperty("customOrderTypeKey");
	}

	/**
	 *  A description of the change that the user would like to make. It might be a name change, or a description of the custom icon they'd like to have, or it may even include links to external images for reference.
	 *  
	 * @param description
	 */
	public void setDescription(Text description) {
		getCachedEntity().setProperty("description", description);
	}

	public Text getDescription() {
		return (Text) getCachedEntity().getProperty("description");
	}

	/**
	 *  The amount of donation credit (in cents) that has been taken from the user for this order.
	 *  
	 * @param donationCredit
	 */
	public void setDonationCredit(Long donationCredit) {
		getCachedEntity().setProperty("donationCredit", donationCredit);
	}

	public Long getDonationCredit() {
		return (Long) getCachedEntity().getProperty("donationCredit");
	}

	/**
	 *  The thing the user wants to change. This could be an Item, a player house, and maybe other things as we keep adding them.
	 *  
	 * @param entityKey
	 */
	public void setEntityKey(Key entityKey) {
		getCachedEntity().setProperty("entityKey", entityKey);
	}

	public Key getEntityKey() {
		return (Key) getCachedEntity().getProperty("entityKey");
	}

	/**
	 * The last time this order was worked on or updated by a content developer.
	 * 
	 * @param lastUpdateDate
	 */
	public void setLastUpdateDate(Date lastUpdateDate) {
		getCachedEntity().setProperty("lastUpdateDate", lastUpdateDate);
	}

	public Date getLastUpdateDate() {
		return (Date) getCachedEntity().getProperty("lastUpdateDate");
	}

	/**
	 *  Content developer notes that can help to understand what's going on with an order.
	 *  
	 * @param progressNotes
	 */
	public void setProgressNotes(Text progressNotes) {
		getCachedEntity().setProperty("progressNotes", progressNotes);
	}

	public Text getProgressNotes() {
		return (Text) getCachedEntity().getProperty("progressNotes");
	}

	/**
	 *  (User)
	 *  
	 * @param userKey
	 */
	public void setUserKey(Key userKey) {
		getCachedEntity().setProperty("userKey", userKey);
	}

	public Key getUserKey() {
		return (Key) getCachedEntity().getProperty("userKey");
	}

	public enum Status {
		New("New"),
		InProgress("In Progress"),
		Problem("Problem"),
		Complete("Complete"),
		Refunded("Refunded"),
		CancelRequest("Cancel Request"),
		;

		private String value;

		private Status(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

	/**
	 * 
	 * @param status
	 */
	public void setStatus(Status status) {
		getCachedEntity().setProperty("status", status);
	}

	public Status getStatus() {
		return (Status) getCachedEntity().getProperty("status");
	}

}
