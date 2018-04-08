package com.universeprojects.miniup.server.dbentities;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

public class GuardSettingsControllerTest
{

	@Test
	public void testGenerateHumanReadableList()
	{
		String[] list = {
				"item1",
				"item2",
				"item3"
		};
		String result = GuardSetting.generateHumanReadableList(Arrays.<String>asList(list));
		Assert.assertEquals("item1, item2, and item3", result);

		list = new String[]{
				"item1",
		};
		result = GuardSetting.generateHumanReadableList(Arrays.<String>asList(list));
		Assert.assertEquals("item1", result);

		list = new String[]{
				"item1",
				"item2"
		};
		result = GuardSetting.generateHumanReadableList(Arrays.<String>asList(list));
		Assert.assertEquals("item1, and item2", result);
	}

}
