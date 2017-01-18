package com.universeprojects.miniup.server;

import org.junit.Test;

public class TestRandomTileGenerator {

	@Test
	public void testDetermineQuality_outsideRangeTest() {
//		long startTime = System.currentTimeMillis();
//		List<List<BuildingCell>> hex1 = RandomTileGenerator.getBuildingCells(123456, 4);
//		long endTime = System.currentTimeMillis();
//		long diff = endTime - startTime;
//		long averageDiff = diff;
//		
//		startTime = System.currentTimeMillis();
//		List<List<BuildingCell>>  hex2 = RandomTileGenerator.getBuildingCells(123456, 4);
//		endTime = System.currentTimeMillis();
//		diff = endTime - startTime;
//		averageDiff = (averageDiff + diff)/2;
		
//		// Verify same seed, same grid size has same results
//		Assert.assertTrue(ArrayList.(hex1, hex2));
//		// Verify performance
//		Assert.assertTrue(averageDiff < 50);
//
//		// Check that subset of grid with same seed is same as larger grid
//		BuildingCell[][] buildingCellsSubset = new BuildingCell[10][10];
//		for (int index=0; index<10; index++) {
//			buildingCellsSubset[index] = Arrays.copyOfRange(buildingCells1[index], 0, 10);
//		}
//		
//		BuildingCell[][] buildingCells3 = RandomTileGenerator.getBuildingCells(123456, 10, 10);
//		Assert.assertTrue(Arrays.deepEquals(buildingCellsSubset, buildingCells3));
//		
//		// Check that different seeds have different results
//		BuildingCell[][] buildingCells4 = RandomTileGenerator.getBuildingCells(654321, 20, 20);
//		Assert.assertFalse(Arrays.deepEquals(buildingCells4, buildingCells2));
	}
}
