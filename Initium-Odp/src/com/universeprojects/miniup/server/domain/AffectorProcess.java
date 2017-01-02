package com.universeprojects.miniup.server.domain;

import com.universeprojects.cacheddatastore.CachedEntity;

//This type of affector allows us to describe how a we (the tool/material..etc) are to affect the process of building/gathering/doing something.
public class AffectorProcess extends OdpDomain {

	public AffectorProcess() {
		super(new CachedEntity("AffectorProcess"));
	}

	public AffectorProcess(CachedEntity cachedEntity) {
		super(cachedEntity, "AffectorProcess");
	}

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

	public enum ProcessParameter {
		Speed,
	}

	public void setProcessParameter(ProcessParameter processParameter) {
		getCachedEntity().setProperty("processParameter", processParameter);
	}

	public ProcessParameter getProcessParameter() {
		return (ProcessParameter) getCachedEntity().getProperty("processParameter");
	}

}
