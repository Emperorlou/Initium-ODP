package com.universeprojects.miniup.server.domain;

import java.util.List;

import com.universeprojects.cacheddatastore.CachedEntity;

/**
 * This is used as a complete definition of a tool "slot". It includes rules for how to identify a valid tool for this slot, and the effect the tool has on the outcome of thing the tool is being used to create.
 * 
 * @author kyle-miller
 *
 */
public class ConstructionToolRequirement extends OdpDomain {
	public static final String KIND = "ConstructionToolRequirement";

	public ConstructionToolRequirement() {
		super(new CachedEntity(KIND));
	}

	private ConstructionToolRequirement(CachedEntity cachedEntity) {
		super(cachedEntity);
	}

	public static final ConstructionToolRequirement wrap(CachedEntity cachedEntity) {
		return new ConstructionToolRequirement(cachedEntity);
	}

	@Override
	public String getKind() {
		return KIND;
	}

	/**
	 *  (EntityRequirement|type==Tool)
	 *  
	 * @param entityRequirement
	 */
	public void setEntityRequirement(EntityRequirement entityRequirement) {
		getCachedEntity().setProperty("entityRequirement", entityRequirement);
	}

	public EntityRequirement getEntityRequirement() {
		return (EntityRequirement) getCachedEntity().getProperty("entityRequirement");
	}

	/**
	 *  This name is likely going to be player facing and should probably mirror the name on the EntityRequirement.
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
	 *  (AffectorProcess)
	 *  
	 * @param processAffectors
	 */
	public void setProcessAffectors(List<AffectorProcess> processAffectors) {
		getCachedEntity().setProperty("processAffectors", processAffectors);
	}

	@SuppressWarnings("unchecked")
	public List<AffectorProcess> getProcessAffectors() {
		return (List<AffectorProcess>) getCachedEntity().getProperty("processAffectors");
	}

	/**
	 *  (AffectorItemField)
	 *  
	 * @param resultingFieldAffectors
	 */
	public void setResultingFieldAffectors(List<AffectorItemField> resultingFieldAffectors) {
		getCachedEntity().setProperty("resultingFieldAffectors", resultingFieldAffectors);
	}

	@SuppressWarnings("unchecked")
	public List<AffectorItemField> getResultingFieldAffectors() {
		return (List<AffectorItemField>) getCachedEntity().getProperty("resultingFieldAffectors");
	}

}
