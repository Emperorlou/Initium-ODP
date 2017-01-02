package com.universeprojects.miniup.server.domain;

import com.universeprojects.cacheddatastore.CachedEntity;

//This type of affector allows us to describe how a we (the tool/material..etc) are to affect the field on an item.
public class AffectorItemField extends OdpDomain {

	public AffectorItemField() {
		super(new CachedEntity("AffectorItemField"));
	}

	public AffectorItemField(CachedEntity cachedEntity) {
		super(cachedEntity, "AffectorItemField");
	}

	// (Item)
	public void setDestinationFieldName(String destinationFieldName) {
		getCachedEntity().setProperty("destinationFieldName", destinationFieldName);
	}

	public String getDestinationFieldName() {
		return (String) getCachedEntity().getProperty("destinationFieldName");
	}

	// A multiplier that is applied directly to the destination field. This multiplier is scaled based on the sourceFieldRange. If the source field value is at the sourceFieldRange's max, then maximumMultiplier will be used. If the source field value is somewhere in the middle of the sourceFieldRange, then the multiplier used will be somewhere between maximumMultiplier and minimumMultiplier.
	public void setMaximumMultiplier(Double maximumMultiplier) {
		getCachedEntity().setProperty("maximumMultiplier", maximumMultiplier);
	}

	public Double getMaximumMultiplier() {
		return (Double) getCachedEntity().getProperty("maximumMultiplier");
	}

	public void setMinimumMultiplier(Double minimumMultiplier) {
		getCachedEntity().setProperty("minimumMultiplier", minimumMultiplier);
	}

	public Double getMinimumMultiplier() {
		return (Double) getCachedEntity().getProperty("minimumMultiplier");
	}

	// Really just for the editor. A name that we can recognize the purpose of this affector when editing stuff.
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	public void setSourceFieldMaximumValue(Double sourceFieldMaximumValue) {
		getCachedEntity().setProperty("sourceFieldMaximumValue", sourceFieldMaximumValue);
	}

	public Double getSourceFieldMaximumValue() {
		return (Double) getCachedEntity().getProperty("sourceFieldMaximumValue");
	}

	public void setSourceFieldMinimumValue(Double sourceFieldMinimumValue) {
		getCachedEntity().setProperty("sourceFieldMinimumValue", sourceFieldMinimumValue);
	}

	public Double getSourceFieldMinimumValue() {
		return (Double) getCachedEntity().getProperty("sourceFieldMinimumValue");
	}

	// (Item)
	public void setSourceFieldName(String sourceFieldName) {
		getCachedEntity().setProperty("sourceFieldName", sourceFieldName);
	}

	public String getSourceFieldName() {
		return (String) getCachedEntity().getProperty("sourceFieldName");
	}

}
