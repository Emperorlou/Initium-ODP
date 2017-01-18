package com.universeprojects.miniup.server.domain;

import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * This type of affector allows us to describe how a we (the tool/material..etc) are to affect the process of building/gathering/doing something.
 * 
 * @author kyle-miller
 *
 */
public class AffectorProcess extends OdpDomain {
	public static final String KIND = "AffectorProcess";

	public AffectorProcess() {
		super(new CachedEntity(KIND));
	}

	private AffectorProcess(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	public static final AffectorProcess wrap(CachedEntity cachedEntity) {
		return new AffectorProcess(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 * 
	 * @param maximumMultiplier
	 */
	public void setMaximumMultiplier(Double maximumMultiplier) {
		getCachedEntity().setProperty("maximumMultiplier", maximumMultiplier);
	}

	public Double getMaximumMultiplier() {
		return (Double) getCachedEntity().getProperty("maximumMultiplier");
	}

	/**
	 * 
	 * @param minimumMultiplier
	 */
	public void setMinimumMultiplier(Double minimumMultiplier) {
		getCachedEntity().setProperty("minimumMultiplier", minimumMultiplier);
	}

	public Double getMinimumMultiplier() {
		return (Double) getCachedEntity().getProperty("minimumMultiplier");
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name) {
		getCachedEntity().setProperty("name", name);
	}

	public String getName() {
		return (String) getCachedEntity().getProperty("name");
	}

	/**
	 * 
	 * @param sourceFieldMaximumValue
	 */
	public void setSourceFieldMaximumValue(Double sourceFieldMaximumValue) {
		getCachedEntity().setProperty("sourceFieldMaximumValue", sourceFieldMaximumValue);
	}

	public Double getSourceFieldMaximumValue() {
		return (Double) getCachedEntity().getProperty("sourceFieldMaximumValue");
	}

	/**
	 * 
	 * @param sourceFieldMinimumValue
	 */
	public void setSourceFieldMinimumValue(Double sourceFieldMinimumValue) {
		getCachedEntity().setProperty("sourceFieldMinimumValue", sourceFieldMinimumValue);
	}

	public Double getSourceFieldMinimumValue() {
		return (Double) getCachedEntity().getProperty("sourceFieldMinimumValue");
	}

	/**
	 * (Item)
	 * 
	 * @param sourceFieldName
	 */
	public void setSourceFieldName(String sourceFieldName) {
		getCachedEntity().setProperty("sourceFieldName", sourceFieldName);
	}

	public String getSourceFieldName() {
		return (String) getCachedEntity().getProperty("sourceFieldName");
	}

	public enum ProcessParameter {
		Speed,
	}

	/**
	 * 
	 * @param processParameter
	 */
	public void setProcessParameter(ProcessParameter processParameter) {
		getCachedEntity().setProperty("processParameter", processParameter);
	}

	public ProcessParameter getProcessParameter() {
		return (ProcessParameter) getCachedEntity().getProperty("processParameter");
	}

}
