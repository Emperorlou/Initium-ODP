package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.AffectorItemField;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class AffectorItemFieldDao extends OdpDao<AffectorItemField> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public AffectorItemFieldDao(CachedDatastoreService datastore) {
		super(datastore, AffectorItemField.KIND, AffectorItemField.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
