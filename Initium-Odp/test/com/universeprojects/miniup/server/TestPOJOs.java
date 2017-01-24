package com.universeprojects.miniup.server;

import com.universeprojects.miniup.server.model.GridCell;
import org.junit.Assert;
import org.junit.Test;

public class TestPOJOs {
	@Test
	public void testSimplePOJO() {
		GridCell tester = new GridCell();
		String nothing = tester.getBackgroundFile();
		Assert.assertTrue(nothing == null);
	}
}
