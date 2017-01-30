package com.universeprojects.miniup.server.services;

import com.universeprojects.miniup.server.SeededSimplexNoise;
import com.universeprojects.miniup.server.model.GridCell;
import com.universeprojects.miniup.server.model.GridMap;
import com.universeprojects.miniup.server.model.GridObject;
import org.json.simple.JSONArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GridMapService {
	
	public GridCell updateGridCellBackground(GridMap gridMap, int row, int column, String backgroundFile) {
		gridMap.getMap()[row][column].setBackgroundFile(backgroundFile);
		GridCell gridCell = new GridCell(column, row);
		gridCell.setBackgroundFile(backgroundFile);
		return gridCell;
	}

	public GridObject updateGridObjectName(GridMap gridMap, String objectKey, String newName) {
		gridMap.getGridObjects().get(objectKey).setName(newName);
		GridObject gridObject = new GridObject(objectKey);
		gridObject.setName(newName);
		return gridObject;
	}

	public static GridMap buildNewGrid(int seed, int rowLength, int columnLength, int forestry) {

		GridCell[][] grid = new GridCell[rowLength][columnLength];
		SeededSimplexNoise treeSsn = new SeededSimplexNoise(seed);
		SeededSimplexNoise shrubSsn = new SeededSimplexNoise(seed + 1);
		Map<String, GridObject> objectMap = new HashMap<>();

		// Loop over grid size
		for (int i = 0; i < rowLength; i++) {
			for (int j = 0; j < columnLength; j++) {
				double treeNoiseResult = treeSsn.eval(j, i);
				double shrubNoiseResult = shrubSsn.eval(j, i);
				// Determine if object is generated at coordinate
				if (treeNoiseResult < ((forestry / 5.0) - 1)) {
					objectMap.put("tree1.png" + "tempKey:" + i + "-" + j, new GridObject(
							"tempKey:" + i + "-" + j,
							"tree1.png",
							"A maple tree, maybe?",
							i, j,
							new Random(seed * (i * j + i * 11 + j)).nextInt(20),
							new Random(seed * (i * j + i * 10 + j)).nextInt(20),
							192 / 2, ((256) - 20), 192, 256, false, false));
				}
				if (shrubNoiseResult < ((forestry / 5.0) - 1)) {
					objectMap.put("shrub1.png" + "tempKey:" + i + "-" + j, new GridObject(
							"tempKey:" + i + "-" + j,
							"shrub1.png",
							"A shrubbery!",
							i, j,
							new Random(seed * (i * j + i * 11 + j)).nextInt(20),
							new Random(seed * (i * j + i * 10 + j)).nextInt(20),
							75 / 2, ((65) / 2), 77, 65, false, false));
				}
				// Build background data for coordinate
				grid[i][j] = new GridCell("tile-grass" + (new Random(seed * (i * j + i * 10 + j)).nextInt(7)) + ".png",
						i, j,
						(new Random(seed * (i * 10 + j)).nextInt(10)));
			}
		}

		// Dummy object data for testing 
		int dumpX = 8;
		int dumpY = 12;
		int attachX = -15;
		int attachY = -15;
		int width = 30;
		int height = 30;
		int offsetX = 30;
		int offsetY = 30;
		Random gausRx = new Random(seed);
		Random gausRy = new Random(seed);
		objectMap.put("images/small2/Pixel_Art-Armor-Chest-elvenhunter.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Armor-Chest-elvenhunter.png",
						"Norwood Cloak", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));
		objectMap.put("images/small2/Pixel_Art-Armor-Hardenedleatherboots_Old.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Armor-Hardenedleatherboots_Old.png",
						"Leather Shin Protectors", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30),
						(int) Math.round(gausRy.nextGaussian() * 10 + 30), attachY, attachX, width, height, false, false));
		objectMap.put("images/small/Pixel_Art-Tools-Shovel1.png" + "tempKey:o1",
				new GridObject("o1", "images/small/Pixel_Art-Tools-Shovel1.png",
						"Ogre-Sized Shovel", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));
		objectMap.put("images/small2/Pixel_Art-Gems-Topaz_Perfect.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Gems-Topaz_Perfect.png",
						"Perfect Topaz", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));
		objectMap.put("images/small2/Pixel_Art-Weapon-Energy-Blade.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Weapon-Energy-Blade.png",
						"Energy Blade", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));
		objectMap.put("images/small/Pixel_Art-Weapons-Chain-W_Mace005.png" + "tempKey:o1",
				new GridObject("o1", "images/small/Pixel_Art-Weapons-Chain-W_Mace005.png",
						"Flail of the Desert Prince", dumpY, dumpX, offsetX, (int) Math.round(gausRy.nextGaussian() * 10 + 30), attachY, attachX, width,
						height, false, false));
		objectMap.put("images/small2/Pixel-Art-Armor-Gladiator-gauntlets.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel-Art-Armor-Gladiator-gauntlets.png",
						"Gladiator's Gauntlets", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30),
						(int) Math.round(gausRy.nextGaussian() * 10 + 30), attachY, attachX, width, height, false, false));
		objectMap.put("images/small2/Pixel_Art-Gems-Sapphire_Flawed.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Gems-Sapphire_Flawed.png",
						"Flawed Sapphire", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));
		objectMap.put("images/small/Pixel_Art-Tools-Pick1.png" + "tempKey:o1",
				new GridObject("o1", "images/small/Pixel_Art-Tools-Pick1.png",
						"Orcish Pick", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));
		objectMap.put("images/small2/Pixel_Art-Weapon-Chieftains-Axe.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Weapon-Chieftains-Axe.png",
						"Ogre Chieftain's Axe", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30),
						(int) Math.round(gausRy.nextGaussian() * 10 + 30), attachY, attachX, width, height, false, false));
		objectMap.put("images/small2/Pixel_Art-Armor-Head-Santa-Hat.png" + "tempKey:o1",
				new GridObject("o1", "images/small2/Pixel_Art-Armor-Head-Santa-Hat.png",
						"Fake Santa Hat", dumpY, dumpX, (int) Math.round(gausRx.nextGaussian() * 10 + 30), (int) Math.round(gausRy.nextGaussian() * 10 + 30),
						attachY, attachX, width, height, false, false));

		return new GridMap(grid, objectMap);
	}
}
