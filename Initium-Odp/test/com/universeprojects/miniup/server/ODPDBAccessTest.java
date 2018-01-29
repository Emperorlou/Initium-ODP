package com.universeprojects.miniup.server;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess.GroupStatus;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

import helper.utilities.HttpServletRequestMock;

public class ODPDBAccessTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	private ODPDBAccess testObj;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void before() {
		helper.setUp();
		CachedDatastoreService.disableRemoteAPI();
		testObj = new ODPDBAccess(new HttpServletRequestMock());
	}

	@After
	public void after() {
		helper.tearDown();
	}

	@Test
	public void doCharacterTakePath_userOwner_noParty_newDiscovery() throws Exception { // Single character happy path
		CachedEntity user = new CachedEntity("User");
		testObj.getDB().put(user);
		CachedEntity character = new CachedEntity("Character");
		Key currentLocationKey = KeyFactory.createKey("locationKey", 1);
		character.setProperty("locationKey", currentLocationKey);
		character.setProperty("userKey", user.getKey());
		testObj.getDB().put(character);
		CachedEntity destinationEntity = new CachedEntity("Location");
		destinationEntity.setProperty("ownerKey", user.getKey());
		testObj.getDB().put(destinationEntity);
		CachedEntity path = new CachedEntity("Path");
		path.setProperty("location1Key", currentLocationKey);
		path.setProperty("location2Key", destinationEntity.getKey());
		path.setProperty("ownerKey", user.getKey());
		testObj.getDB().put(path);

		CachedEntity actualDestination = testObj.doCharacterTakePath(testObj.getDB(), character, path , false);

		assertEquals(destinationEntity, actualDestination);
		assertEquals(destinationEntity.getKey(), testObj.getDB().get(character.getKey()).getProperty("locationKey"));
		CachedEntity discovery = testObj.getDiscoveryByEntity(character.getKey(), path.getKey()); // TODO getDiscoveryByEntity needs tests and needs to go to a DAO
		assertEquals(character.getKey(), discovery.getProperty("characterKey"));
		assertEquals(path.getKey(), discovery.getProperty("entityKey"));
		assertEquals(path.getKind(), discovery.getProperty("kind"));
		assertEquals(currentLocationKey, discovery.getProperty("location1Key"));
		assertEquals(destinationEntity.getKey(), discovery.getProperty("location2Key"));
	}

	@Test
	public void doCharacterTakePath_noParty_userNotOwner() throws Exception { // Single character not the owner
		expectedException.expect(UserErrorMessage.class);
		expectedException.expectMessage("You cannot enter a player owned house unless you already have been given access."); // TODO - move these exceptions to properties

		CachedEntity user = new CachedEntity("User");
		testObj.getDB().put(user);
		CachedEntity character = new CachedEntity("Character");
		Key currentLocationKey = KeyFactory.createKey("locationKey", 1);
		character.setProperty("locationKey", currentLocationKey);
		character.setProperty("userKey", user.getKey());
		testObj.getDB().put(character);
		CachedEntity destinationEntity = new CachedEntity("Location");
		destinationEntity.setProperty("ownerKey", user.getKey());
		testObj.getDB().put(destinationEntity);
		CachedEntity path = new CachedEntity("Path");
		path.setProperty("location1Key", currentLocationKey);
		path.setProperty("location2Key", destinationEntity.getKey());
		CachedEntity nonMatchingUser = new CachedEntity("User");
		testObj.getDB().put(user);
		path.setProperty("ownerKey", nonMatchingUser.getKey());
		testObj.getDB().put(path);

		testObj.doCharacterTakePath(testObj.getDB(), character, path , false);
	}

	@Test
	public void doCharacterTakePath_userOwner_inParty_sameUser_isOwner_isLeader() throws Exception { // Two user characters, happy path
		CachedEntity user = new CachedEntity("User");
		testObj.getDB().put(user);
		CachedEntity character1 = new CachedEntity("Character");
		Key currentLocationKey = KeyFactory.createKey("locationKey", 1);
		character1.setProperty("locationKey", currentLocationKey);
		String partyCode = "partyCode";
		character1.setProperty("partyCode", partyCode);
		character1.setProperty("userKey", user.getKey());
		character1.setProperty("partyLeader", "TRUE");
		character1.setProperty("hitpoints", 100d);
		testObj.getDB().put(character1);
		CachedEntity character2 = new CachedEntity("Character");
		character2.setProperty("locationKey", currentLocationKey);
		character2.setProperty("partyCode", partyCode);
		character2.setProperty("userKey", user.getKey());
		character2.setProperty("hitpoints", 100d);
		testObj.getDB().put(character2);
		CachedEntity destinationEntity = new CachedEntity("Location");
		destinationEntity.setProperty("ownerKey", user.getKey());
		testObj.getDB().put(destinationEntity);
		CachedEntity path = new CachedEntity("Path");
		path.setProperty("location1Key", currentLocationKey);
		path.setProperty("location2Key", destinationEntity.getKey());
		path.setProperty("ownerKey", user.getKey());
		testObj.getDB().put(path);
		
		CachedEntity actualDestination = testObj.doCharacterTakePath(testObj.getDB(), character1, path , false);
		
		assertEquals(destinationEntity, actualDestination);
		assertEquals(destinationEntity.getKey(), testObj.getDB().get(character1.getKey()).getProperty("locationKey"));
		CachedEntity discovery = testObj.getDiscoveryByEntity(character1.getKey(), path.getKey());
		assertEquals(character1.getKey(), discovery.getProperty("characterKey"));
		assertEquals(path.getKey(), discovery.getProperty("entityKey"));
		assertEquals(path.getKind(), discovery.getProperty("kind"));
		assertEquals(currentLocationKey, discovery.getProperty("location1Key"));
		assertEquals(destinationEntity.getKey(), discovery.getProperty("location2Key"));
	}

	@Test
	public void doCharacterTakePath_userOwner_inParty_oneOwner_oneDiscovery() throws Exception { // Two characters of different users, one is owner
		CachedEntity user1 = new CachedEntity("User");
		testObj.getDB().put(user1);
		CachedEntity character1 = new CachedEntity("Character");
		Key currentLocationKey = KeyFactory.createKey("locationKey", 1);
		character1.setProperty("locationKey", currentLocationKey);
		String partyCode = "partyCode";
		character1.setProperty("partyCode", partyCode);
		character1.setProperty("userKey", user1.getKey());
		character1.setProperty("partyLeader", "TRUE");
		testObj.getDB().put(character1);
		CachedEntity user2 = new CachedEntity("User");
		testObj.getDB().put(user2);
		CachedEntity character2 = new CachedEntity("Character");
		character2.setProperty("locationKey", currentLocationKey);
		character2.setProperty("partyCode", partyCode);
		character2.setProperty("userKey", user2.getKey());
		testObj.getDB().put(character2);
		CachedEntity destinationEntity = new CachedEntity("Location");
		destinationEntity.setProperty("ownerKey", user1.getKey());
		testObj.getDB().put(destinationEntity);
		CachedEntity path = new CachedEntity("Path");
		path.setProperty("location1Key", currentLocationKey);
		path.setProperty("location2Key", destinationEntity.getKey());
		path.setProperty("ownerKey", user1.getKey());
		testObj.getDB().put(path);
		CachedEntity discovery = new CachedEntity("Discovery");
		discovery.setProperty("characterKey", character2.getKey());
		discovery.setProperty("entityKey", path.getKey());
		discovery.setProperty("kind", path.getKind());
		discovery.setProperty("location1Key", path.getProperty("location1Key"));
		discovery.setProperty("location2Key", path.getProperty("location2Key"));
		testObj.getDB().put(discovery);
	}

	@Test
	public void doCharacterTakePath_userOwner_inParty_noOwner_isLeader() throws Exception { // Two characters of different users, one is owner
		expectedException.expect(UserErrorMessage.class);
		expectedException.expectMessage("You cannot enter a player owned house unless every character already has been given access."); // TODO - move these exceptions to properties

		CachedEntity user1 = new CachedEntity("User");
		testObj.getDB().put(user1);
		CachedEntity character1 = new CachedEntity("Character");
		Key currentLocationKey = KeyFactory.createKey("locationKey", 1);
		character1.setProperty("locationKey", currentLocationKey);
		String partyCode = "partyCode";
		character1.setProperty("partyCode", partyCode);
		character1.setProperty("userKey", user1.getKey());
		character1.setProperty("partyLeader", "TRUE");
		character1.setProperty("hitpoints", 100d);
		testObj.getDB().put(character1);
		CachedEntity user2 = new CachedEntity("User");
		testObj.getDB().put(user2);
		CachedEntity character2 = new CachedEntity("Character");
		character2.setProperty("locationKey", currentLocationKey);
		character2.setProperty("partyCode", partyCode);
		character2.setProperty("userKey", user2.getKey());
		character2.setProperty("hitpoints", 100d);
		testObj.getDB().put(character2);
		CachedEntity user3 = new CachedEntity("User");
		testObj.getDB().put(user3);
		CachedEntity destinationEntity = new CachedEntity("Location");
		destinationEntity.setProperty("ownerKey", user3.getKey());
		testObj.getDB().put(destinationEntity);
		CachedEntity path = new CachedEntity("Path");
		path.setProperty("location1Key", currentLocationKey);
		path.setProperty("location2Key", destinationEntity.getKey());
		path.setProperty("ownerKey", user3.getKey());
		testObj.getDB().put(path);
		
		testObj.doCharacterTakePath(testObj.getDB(), character1, path , false);
	}

	@Test
	public void doCharacterTakePath_userOwner_inParty_sameUser_isOwner_isNotLeader() throws Exception { // Two user characters and is the owner, but not using the party leader character
		expectedException.expect(UserErrorMessage.class);
		expectedException.expectMessage("You cannot move the party because you're not the party leader."); // TODO - move these exceptions to properties

		CachedEntity user = new CachedEntity("User");
		testObj.getDB().put(user);
		CachedEntity character1 = new CachedEntity("Character");
		Key currentLocationKey = KeyFactory.createKey("locationKey", 1);
		character1.setProperty("locationKey", currentLocationKey);
		String partyCode = "partyCode";
		character1.setProperty("partyCode", partyCode);
		character1.setProperty("userKey", user.getKey());
		character1.setProperty("partyLeader", "TRUE");
		character1.setProperty("hitpoints", 100d);
		testObj.getDB().put(character1);
		CachedEntity character2 = new CachedEntity("Character");
		character2.setProperty("locationKey", currentLocationKey);
		character2.setProperty("partyCode", partyCode);
		character2.setProperty("userKey", user.getKey());
		character2.setProperty("hitpoints", 100d);
		testObj.getDB().put(character2);
		CachedEntity destinationEntity = new CachedEntity("Location");
		destinationEntity.setProperty("ownerKey", user.getKey());
		testObj.getDB().put(destinationEntity);
		CachedEntity path = new CachedEntity("Path");
		path.setProperty("location1Key", currentLocationKey);
		path.setProperty("location2Key", destinationEntity.getKey());
		path.setProperty("ownerKey", user.getKey());
		testObj.getDB().put(path);
		
		testObj.doCharacterTakePath(testObj.getDB(), character2, path , false);
	}

	@Test
	public void doCharacterTakePath_groupOwner_singleChar_inGroupAsAdmin() throws Exception {
		CachedEntity group = new CachedEntity("Group");
		testObj.getDB().put(group);
		CachedEntity user = new CachedEntity("User");
		testObj.getDB().put(user);
		CachedEntity character = new CachedEntity("Character");
		Key currentLocationKey = KeyFactory.createKey("locationKey", 1);
		character.setProperty("locationKey", currentLocationKey);
		character.setProperty("userKey", user.getKey());
		character.setProperty("groupKey", group.getKey());
		character.setProperty("groupStatus", GroupStatus.Admin.name());
		testObj.getDB().put(character);
		CachedEntity destinationEntity = new CachedEntity("Location");
		destinationEntity.setProperty("ownerKey", group.getKey());
		testObj.getDB().put(destinationEntity);
		CachedEntity path = new CachedEntity("Path");
		path.setProperty("location1Key", currentLocationKey);
		path.setProperty("location2Key", destinationEntity.getKey());
		path.setProperty("ownerKey", group.getKey());
		testObj.getDB().put(path);

		CachedEntity actualDestination = testObj.doCharacterTakePath(testObj.getDB(), character, path , false);

		assertEquals(destinationEntity, actualDestination);
		assertEquals(destinationEntity.getKey(), testObj.getDB().get(character.getKey()).getProperty("locationKey"));
		CachedEntity discovery = testObj.getDiscoveryByEntity(character.getKey(), path.getKey()); // TODO getDiscoveryByEntity needs tests and needs to go to a DAO
		assertEquals(character.getKey(), discovery.getProperty("characterKey"));
		assertEquals(path.getKey(), discovery.getProperty("entityKey"));
		assertEquals(path.getKind(), discovery.getProperty("kind"));
		assertEquals(currentLocationKey, discovery.getProperty("location1Key"));
		assertEquals(destinationEntity.getKey(), discovery.getProperty("location2Key"));
	}

	@Test
	public void doCharacterTakePath_groupOwner_singleChar_inGroupAsMember() throws Exception {
		CachedEntity group = new CachedEntity("Group");
		testObj.getDB().put(group);
		CachedEntity user = new CachedEntity("User");
		testObj.getDB().put(user);
		CachedEntity character = new CachedEntity("Character");
		Key currentLocationKey = KeyFactory.createKey("locationKey", 1);
		character.setProperty("locationKey", currentLocationKey);
		character.setProperty("userKey", user.getKey());
		character.setProperty("groupKey", group.getKey());
		character.setProperty("groupStatus", GroupStatus.Member.name());
		testObj.getDB().put(character);
		CachedEntity destinationEntity = new CachedEntity("Location");
		destinationEntity.setProperty("ownerKey", group.getKey());
		testObj.getDB().put(destinationEntity);
		CachedEntity path = new CachedEntity("Path");
		path.setProperty("location1Key", currentLocationKey);
		path.setProperty("location2Key", destinationEntity.getKey());
		path.setProperty("ownerKey", group.getKey());
		testObj.getDB().put(path);

		CachedEntity actualDestination = testObj.doCharacterTakePath(testObj.getDB(), character, path , false);

		assertEquals(destinationEntity, actualDestination);
		assertEquals(destinationEntity.getKey(), testObj.getDB().get(character.getKey()).getProperty("locationKey"));
		CachedEntity discovery = testObj.getDiscoveryByEntity(character.getKey(), path.getKey()); // TODO getDiscoveryByEntity needs tests and needs to go to a DAO
		assertEquals(character.getKey(), discovery.getProperty("characterKey"));
		assertEquals(path.getKey(), discovery.getProperty("entityKey"));
		assertEquals(path.getKind(), discovery.getProperty("kind"));
		assertEquals(currentLocationKey, discovery.getProperty("location1Key"));
		assertEquals(destinationEntity.getKey(), discovery.getProperty("location2Key"));
	}

	@Test
	public void doCharacterTakePath_groupOwner_twoChars_inGroup() throws Exception {
		CachedEntity group = new CachedEntity("Group");
		testObj.getDB().put(group);
		CachedEntity user = new CachedEntity("User");
		testObj.getDB().put(user);
		CachedEntity character1 = new CachedEntity("Character");
		Key currentLocationKey = KeyFactory.createKey("locationKey", 1);
		character1.setProperty("locationKey", currentLocationKey);
		character1.setProperty("userKey", user.getKey());
		character1.setProperty("groupKey", group.getKey());
		character1.setProperty("groupStatus", GroupStatus.Member.name());
		character1.setProperty("hitpoints", 100d);
		String partyCode = "partyCode";
		character1.setProperty("partyCode", partyCode);
		character1.setProperty("partyLeader", "TRUE");
		testObj.getDB().put(character1);
		CachedEntity character2 = new CachedEntity("Character");
		character2.setProperty("locationKey", currentLocationKey);
		character2.setProperty("userKey", user.getKey());
		character2.setProperty("partyCode", partyCode);
		character2.setProperty("groupKey", group.getKey());
		character2.setProperty("groupStatus", GroupStatus.Member.name());
		character2.setProperty("hitpoints", 100d);
		testObj.getDB().put(character2);
		CachedEntity destinationEntity = new CachedEntity("Location");
		destinationEntity.setProperty("ownerKey", group.getKey());
		testObj.getDB().put(destinationEntity);
		CachedEntity path = new CachedEntity("Path");
		path.setProperty("location1Key", currentLocationKey);
		path.setProperty("location2Key", destinationEntity.getKey());
		path.setProperty("ownerKey", group.getKey());
		testObj.getDB().put(path);
		
		CachedEntity actualDestination = testObj.doCharacterTakePath(testObj.getDB(), character1, path , false);
		
		assertEquals(destinationEntity, actualDestination);
		assertEquals(destinationEntity.getKey(), testObj.getDB().get(character1.getKey()).getProperty("locationKey"));
		CachedEntity discovery = testObj.getDiscoveryByEntity(character1.getKey(), path.getKey()); // TODO getDiscoveryByEntity needs tests and needs to go to a DAO
		assertEquals(character1.getKey(), discovery.getProperty("characterKey"));
		assertEquals(path.getKey(), discovery.getProperty("entityKey"));
		assertEquals(path.getKind(), discovery.getProperty("kind"));
		assertEquals(currentLocationKey, discovery.getProperty("location1Key"));
		assertEquals(destinationEntity.getKey(), discovery.getProperty("location2Key"));
	}

	@Test
	public void doCharacterTakePath_groupOwner_twoChar_oneInGroupAsAdmin_otherNoGroup_asAdmin() throws Exception {
		expectedException.expect(UserErrorMessage.class);
		expectedException.expectMessage("You cannot enter a group owned house unless all members of your party are members of the group."); // TODO - move these exceptions to properties

		CachedEntity group = new CachedEntity("Group");
		testObj.getDB().put(group);
		CachedEntity user = new CachedEntity("User");
		testObj.getDB().put(user);
		CachedEntity character1 = new CachedEntity("Character");
		Key currentLocationKey = KeyFactory.createKey("locationKey", 1);
		character1.setProperty("locationKey", currentLocationKey);
		character1.setProperty("userKey", user.getKey());
		character1.setProperty("groupKey", group.getKey());
		character1.setProperty("groupStatus", GroupStatus.Admin.name());
		character1.setProperty("hitpoints", 100d);
		String partyCode = "partyCode";
		character1.setProperty("partyCode", partyCode);
		character1.setProperty("partyLeader", "TRUE");
		testObj.getDB().put(character1);
		CachedEntity character2 = new CachedEntity("Character");
		character2.setProperty("locationKey", currentLocationKey);
		character2.setProperty("userKey", user.getKey());
		character2.setProperty("partyCode", partyCode);
		character2.setProperty("hitpoints", 100d);
		testObj.getDB().put(character2);
		CachedEntity destinationEntity = new CachedEntity("Location");
		destinationEntity.setProperty("ownerKey", group.getKey());
		testObj.getDB().put(destinationEntity);
		CachedEntity path = new CachedEntity("Path");
		path.setProperty("location1Key", currentLocationKey);
		path.setProperty("location2Key", destinationEntity.getKey());
		path.setProperty("ownerKey", group.getKey());
		testObj.getDB().put(path);

		testObj.doCharacterTakePath(testObj.getDB(), character1, path , false);
	}

	@Test
	public void doCharacterTakePath_groupOwner_twoChar_oneInGroupAsAdmin_otherNoGroup_asNoGroupChar() throws Exception {
		expectedException.expect(UserErrorMessage.class);
		expectedException.expectMessage("You cannot enter a group owned house unless all members of your party are members of the group."); // TODO - move these exceptions to properties

		CachedEntity group = new CachedEntity("Group");
		testObj.getDB().put(group);
		CachedEntity user = new CachedEntity("User");
		testObj.getDB().put(user);
		CachedEntity character1 = new CachedEntity("Character");
		Key currentLocationKey = KeyFactory.createKey("locationKey", 1);
		character1.setProperty("locationKey", currentLocationKey);
		character1.setProperty("userKey", user.getKey());
		character1.setProperty("groupKey", group.getKey());
		character1.setProperty("groupStatus", GroupStatus.Admin.name());
		character1.setProperty("hitpoints", 100d);
		String partyCode = "partyCode";
		character1.setProperty("partyCode", partyCode);
		testObj.getDB().put(character1);
		CachedEntity character2 = new CachedEntity("Character");
		character2.setProperty("locationKey", currentLocationKey);
		character2.setProperty("userKey", user.getKey());
		character2.setProperty("partyCode", partyCode);
		character2.setProperty("hitpoints", 100d);
		character2.setProperty("partyLeader", "TRUE");
		testObj.getDB().put(character2);
		CachedEntity destinationEntity = new CachedEntity("Location");
		destinationEntity.setProperty("ownerKey", group.getKey());
		testObj.getDB().put(destinationEntity);
		CachedEntity path = new CachedEntity("Path");
		path.setProperty("location1Key", currentLocationKey);
		path.setProperty("location2Key", destinationEntity.getKey());
		path.setProperty("ownerKey", group.getKey());
		testObj.getDB().put(path);

		testObj.doCharacterTakePath(testObj.getDB(), character2, path , false);
	}

	@Test
	public void doCharacterTakePath_groupOwner_twoChar_oneInGroupAsAdmin_otherDifferentGroup_asAdmin() throws Exception {
		expectedException.expect(UserErrorMessage.class);
		expectedException.expectMessage("You cannot enter a group owned house unless all members of your party are members of the group."); // TODO - move these exceptions to properties

		CachedEntity group1 = new CachedEntity("Group");
		testObj.getDB().put(group1);
		CachedEntity group2 = new CachedEntity("Group");
		testObj.getDB().put(group2);
		CachedEntity user = new CachedEntity("User");
		testObj.getDB().put(user);
		CachedEntity character1 = new CachedEntity("Character");
		Key currentLocationKey = KeyFactory.createKey("locationKey", 1);
		character1.setProperty("locationKey", currentLocationKey);
		character1.setProperty("userKey", user.getKey());
		character1.setProperty("groupKey", group1.getKey());
		character1.setProperty("groupStatus", GroupStatus.Admin.name());
		String partyCode = "partyCode";
		character1.setProperty("partyCode", partyCode);
		character1.setProperty("partyLeader", "TRUE");
		character1.setProperty("hitpoints", 100d);
		testObj.getDB().put(character1);
		CachedEntity character2 = new CachedEntity("Character");
		character2.setProperty("locationKey", currentLocationKey);
		character2.setProperty("userKey", user.getKey());
		character2.setProperty("partyCode", partyCode);
		character2.setProperty("groupKey", group2.getKey());
		character2.setProperty("groupStatus", GroupStatus.Member.name());
		character2.setProperty("hitpoints", 100d);
		testObj.getDB().put(character2);
		CachedEntity destinationEntity = new CachedEntity("Location");
		destinationEntity.setProperty("ownerKey", group1.getKey());
		testObj.getDB().put(destinationEntity);
		CachedEntity path = new CachedEntity("Path");
		path.setProperty("location1Key", currentLocationKey);
		path.setProperty("location2Key", destinationEntity.getKey());
		path.setProperty("ownerKey", group1.getKey());
		testObj.getDB().put(path);

		testObj.doCharacterTakePath(testObj.getDB(), character1, path , false);
	}

	@Test
	public void doCharacterTakePath_groupOwner_twoChar_oneInGroupAsAdmin_otherDifferentGroup_asOther() throws Exception {
		expectedException.expect(UserErrorMessage.class);
		expectedException.expectMessage("You cannot enter a group owned house unless all members of your party are members of the group."); // TODO - move these exceptions to properties
		
		CachedEntity group1 = new CachedEntity("Group");
		testObj.getDB().put(group1);
		CachedEntity group2 = new CachedEntity("Group");
		testObj.getDB().put(group2);
		CachedEntity user = new CachedEntity("User");
		testObj.getDB().put(user);
		CachedEntity character1 = new CachedEntity("Character");
		Key currentLocationKey = KeyFactory.createKey("locationKey", 1);
		character1.setProperty("locationKey", currentLocationKey);
		character1.setProperty("userKey", user.getKey());
		character1.setProperty("groupKey", group1.getKey());
		character1.setProperty("groupStatus", GroupStatus.Admin.name());
		character1.setProperty("hitpoints", 100d);
		String partyCode = "partyCode";
		character1.setProperty("partyCode", partyCode);
		testObj.getDB().put(character1);
		CachedEntity character2 = new CachedEntity("Character");
		character2.setProperty("locationKey", currentLocationKey);
		character2.setProperty("userKey", user.getKey());
		character2.setProperty("partyCode", partyCode);
		character1.setProperty("partyLeader", "TRUE");
		character2.setProperty("groupKey", group2.getKey());
		character2.setProperty("groupStatus", GroupStatus.Member.name());
		character2.setProperty("hitpoints", 100d);
		testObj.getDB().put(character2);
		CachedEntity destinationEntity = new CachedEntity("Location");
		destinationEntity.setProperty("ownerKey", group1.getKey());
		testObj.getDB().put(destinationEntity);
		CachedEntity path = new CachedEntity("Path");
		path.setProperty("location1Key", currentLocationKey);
		path.setProperty("location2Key", destinationEntity.getKey());
		path.setProperty("ownerKey", group1.getKey());
		testObj.getDB().put(path);
		
		testObj.doCharacterTakePath(testObj.getDB(), character2, path , false);
	}
}
