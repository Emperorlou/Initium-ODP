package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.AffectorProcess;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class AffectorProcessDao extends OdpDao<AffectorProcess> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public AffectorProcessDao(CachedDatastoreService datastore) {
		super(datastore, AffectorProcess.KIND, AffectorProcess.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
