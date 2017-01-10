package com.universeprojects.miniup.server.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.domain.OdpDomain;

import helper.utilities.ClassFinder;
import helper.utilities.HttpServletRequestImpl;

public class OdpDaoTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	private CachedDatastoreService ds;

	@Before
	public void before() {
		helper.setUp();
		CachedDatastoreService.disableRemoteAPI();
		ds = ODPDBAccess.getInstance(new HttpServletRequestImpl()).getDB();
	}

	@After
	public void after() {
		helper.tearDown();
	}

	@Test
	public void getByKey_noData() throws Exception {
		for (OdpDao<? extends OdpDomain> testObj : getDaos()) {
			OdpDomain odpDomain = testObj.get(KeyFactory.createKey(testObj.getKind(), 1));

			assertNull(odpDomain);
		}
	}

	@Test
	public void saveAndGet() throws Exception {
		for (OdpDao<? extends OdpDomain> testObj : getDaos()) {
			Class<?> daoClass = testObj.getClass();
			Class<?> domainClass = getDomainClass(daoClass);

			if (domainClass != null) {
				OdpDomain newOdpDomain = (OdpDomain) domainClass.newInstance();
				String keyStringBeforeSave = newOdpDomain.getCachedEntity().getKey().toString();
				boolean saved = (Boolean) testObj.getClass().getMethod("save", newOdpDomain.getClass().getSuperclass()).invoke(testObj, newOdpDomain);
				OdpDomain savedOdpDomain = testObj.get(newOdpDomain.getCachedEntity().getKey());
	
				assertTrue(saved);
				assertFalse(keyStringBeforeSave.equals(savedOdpDomain.getKey().toString())); // Ensure this thing was saved
				assertTrue(savedOdpDomain.getKey().toString().contains(savedOdpDomain.getKind()));
			}
		}
	}

	private Class<?> getDomainClass(Class<?> daoClass) {
		try {
			String domainPackage = daoClass.getPackage().getName().replace("dao", "domain");
			String domainClassName = daoClass.getSimpleName().substring(0, daoClass.getSimpleName().length() - 3);
			return Class.forName(domainPackage + "." + domainClassName);
		} catch (ClassNotFoundException e) {
			
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<OdpDao<? extends OdpDomain>> getDaos() throws Exception {
		List<OdpDao<? extends OdpDomain>> daos = new ArrayList<>();

		List<Class> daoClasses = new ClassFinder().getDaoClasses();
		for (Class<? extends OdpDao<? extends OdpDomain>> daoClass : daoClasses) {
			if (!OdpDao.class.isAssignableFrom(daoClass) || "OdpDao".equals(daoClass.getSimpleName())) { continue; }
			daos.add(daoClass.getConstructor(CachedDatastoreService.class).newInstance(ds));
		}

		return daos;
	}
}
