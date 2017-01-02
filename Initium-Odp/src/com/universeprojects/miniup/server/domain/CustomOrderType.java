package com.universeprojects.miniup.server.domain;

import com.google.appengine.api.datastore.Text;
import com.universeprojects.cacheddatastore.CachedEntity;

//All the different types of orders a user can make for customizing things for donation credit.
public class CustomOrderType extends OdpDomain {
	public static final String KIND = "CustomOrderType";

	public CustomOrderType() {
		super(new CachedEntity(KIND));
	}

	public CustomOrderType(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	// If there is a special deal for ordering bulk amounts of this order type, this is done by specifying a maximum cost of the total batch here. Use this field in conjunction with the maxCount field.
	public void setBulkDeal(Long bulkDeal) {
		getCachedEntity().setProperty("bulkDeal", bulkDeal);
	}

	public Long getBulkDeal() {
		return (Long) getCachedEntity().getProperty("bulkDeal");
	}

	// The amount of donation credit required for this order type.
	public void setCost(Long cost) {
		getCachedEntity().setProperty("cost", cost);
	}

	public Long getCost() {
		return (Long) getCachedEntity().getProperty("cost");
	}

	// A description that is shown to the user that gives them all the information they need to know regarding this order type.
	public void setDescription(Text description) {
		getCachedEntity().setProperty("description", description);
	}

	public Text getDescription() {
		return (Text) getCachedEntity().getProperty("description");
	}

	// This number determines where this order type falls in the customization menu list.
	public void setIndex(Long index) {
		getCachedEntity().setProperty("index", index);
	}

	public Long getIndex() {
		return (Long) getCachedEntity().getProperty("index");
	}

	// The total number of orders that can be open for this order type for any given time.
	public void setMaxCount(Long maxCount) {
		getCachedEntity().setProperty("maxCount", maxCount);
	}

	public Long getMaxCount() {
		return (Long) getCachedEntity().getProperty("maxCount");
	}

	// A (probably verbose) name given to the type of order this is.
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	// All the notes a content developer needs to know in order to fulfil the order.
	public void setNotesForDev(Text notesForDev) {
		getCachedEntity().setProperty("notesForDev", notesForDev);
	}

	public Text getNotesForDev() {
		return (Text) getCachedEntity().getProperty("notesForDev");
	}

	// This is a short description of the details the player must provide to complete the order. They will be prompted with this when they place their order.
	public void setRequiredDetailsSummary(Text requiredDetailsSummary) {
		getCachedEntity().setProperty("requiredDetailsSummary", requiredDetailsSummary);
	}

	public Text getRequiredDetailsSummary() {
		return (Text) getCachedEntity().getProperty("requiredDetailsSummary");
	}

	// If this order type requires an item or not.
	public void setRequiresItem(Boolean requiresItem) {
		getCachedEntity().setProperty("requiresItem", requiresItem);
	}

	public Boolean getRequiresItem() {
		return (Boolean) getCachedEntity().getProperty("requiresItem");
	}

}
