package com.universeprojects.miniup.server;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.universeprojects.cacheddatastore.CachedEntity;

public class GameUtilsTest
{
	
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Before
	public void setUp() {
	    helper.setUp();
	}
	
	@After
	public void tearDown() {
	    helper.tearDown();
	}	
	
	@Test
	public void deleteMe()
	{
		int[] test1 = new int[]{1,2,3};
		List<int[]> asList = Arrays.asList(test1);
		test1[0] = 10;
		System.out.println(asList);
		
	}

	@Test
	public void testBooleanEquals()
	{
		Assert.assertEquals(true, GameUtils.booleanEquals("TRUE", true));
		Assert.assertEquals(false, GameUtils.booleanEquals("TRUE", false));
		Assert.assertEquals(true, GameUtils.booleanEquals("FALSE", false));
		Assert.assertEquals(false, GameUtils.booleanEquals("FALSE", true));

		Assert.assertEquals(false, GameUtils.booleanEquals(null, true));
		Assert.assertEquals(true, GameUtils.booleanEquals(null, false));
	}
	
	@Test
	public void testElapsed()
	{
		Date date = new Date(117,01,03, 02,53,03);
		
		if (GameUtils.elapsed(date, Calendar.HOUR)<3)
			Assert.fail();
	}
	  
	@Test
	public void testDetermineQuality_outsideRangeTest() {
		Map<String,Object> item = new HashMap<String,Object>();
		item.put("qualityUnit", "blockChance[0..10]");

		item.put("blockChance", "0");
		Assert.assertEquals("item-junk", GameUtils.determineQuality(item));

		item.put("blockChance", "10");
		Assert.assertEquals("item-unique", GameUtils.determineQuality(item));

		item.put("blockChance", "-1");
		Assert.assertEquals("item-junk", GameUtils.determineQuality(item));

		item.put("blockChance", "11");
		Assert.assertEquals("item-unique", GameUtils.determineQuality(item));


		item.put("qualityUnit", "blockChance[10..0]");

		item.put("blockChance", "0");
		Assert.assertEquals("item-unique", GameUtils.determineQuality(item));

		item.put("blockChance", "10");
		Assert.assertEquals("item-junk", GameUtils.determineQuality(item));

		item.put("blockChance", "-1");
		Assert.assertEquals("item-unique", GameUtils.determineQuality(item));

		item.put("blockChance", "11");
		Assert.assertEquals("item-junk", GameUtils.determineQuality(item));


	}

	@Test
	public void testDetermineQuality() {
		Map<String,Object> item = new HashMap<String,Object>();
		item.put("qualityUnit", "weaponDamage(0..7,8..13,14..17,18..30)");

		item.put("weaponDamage", "DD1D6");
		Assert.assertEquals("item-junk", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD1D9");
		Assert.assertEquals("", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D5");
		Assert.assertEquals("", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD1D14");
		Assert.assertEquals("item-rare", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D7");
		Assert.assertEquals("item-rare", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D8");
		Assert.assertEquals("item-rare", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D10");
		Assert.assertEquals("item-unique", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D11");
		Assert.assertEquals("item-unique", GameUtils.determineQuality(item));

	}

	@Test
	public void testDetermineQuality2() {
		//6..8,9..15,16..19,20..22
		Map<String,Object> item = new HashMap<String,Object>();
		item.put("qualityUnit", "weaponDamage[6..22]");

		item.put("weaponDamage", "DD1D6");
		Assert.assertEquals("item-junk", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD1D8");
		Assert.assertEquals("item-junk", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD1D9");
		Assert.assertEquals("", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D5");
		Assert.assertEquals("", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD1D14");
		Assert.assertEquals("", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D7");
		Assert.assertEquals("", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D9");
		Assert.assertEquals("item-rare", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D10");
		Assert.assertEquals("item-unique", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D11");
		Assert.assertEquals("item-unique", GameUtils.determineQuality(item));

	}
	
	
	@Test
	public void testDetermineQuality_twoQualityUnits() {
		//6..8,9..15,16..19,20..22
		Map<String,Object> item = new HashMap<String,Object>();
		item.put("qualityUnit", "weaponDamage[6..22]&&dexterityPenalty[10..0]");

		item.put("weaponDamage", "DD1D6");
		item.put("dexterityPenalty", 10);
		Assert.assertEquals("item-junk", GameUtils.determineQuality(item));
		
		item.put("weaponDamage", "DD1D6");
		item.put("dexterityPenalty", 0);
		Assert.assertEquals("", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD1D8");
		item.put("dexterityPenalty", 0);
		Assert.assertEquals("", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD1D9");
		item.put("dexterityPenalty", 0);
		Assert.assertEquals("", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D5");
		item.put("dexterityPenalty", 10);
		Assert.assertEquals("", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD1D14");
		item.put("dexterityPenalty", 0);
		Assert.assertEquals("", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D7");
		item.put("dexterityPenalty", 0);
		Assert.assertEquals("", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D9");
		item.put("dexterityPenalty", 0);
		Assert.assertEquals("item-rare", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D10");
		item.put("dexterityPenalty", 0);
		Assert.assertEquals("item-unique", GameUtils.determineQuality(item));

		item.put("weaponDamage", "DD2D11");
		item.put("dexterityPenalty", 0);
		Assert.assertEquals("item-unique", GameUtils.determineQuality(item));

	}
	

	@Test
	public void testGetIconIconToUseFor() {
		CachedEntity item = new CachedEntity("Item");
		item.setProperty("icon", "");
		item.setProperty("icon2", "");
		item.setProperty("equipSlot", "Chest and Legs");
		
		assertEquals("icon2", GameUtils.getItemIconToUseFor("equipmentLegs", item));
			
	}	
}
