package com.universeprojects.miniup.server.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.universeprojects.miniup.server.SeededSimplexNoise;
import com.universeprojects.miniup.server.model.GridCell;
import com.universeprojects.miniup.server.model.GridMap;
import com.universeprojects.miniup.server.model.GridObject;

public class GridMapService {

	public static class ObjectType
	{
		public String img;
		public int width;
		public int height;
		public double scale;
		public double scaleVariance;
		public boolean flippable;
		
		public ObjectType(String img, int width, int height, double scale, double scaleVariance, boolean flippable)
		{
			this.img = img;
			this.width = width;
			this.height = height;
			this.scale = scale;
			this.scaleVariance = scaleVariance;
			this.flippable = flippable;
		}
	}
	
	public static double GLOBAL_SCALE = 0.4;
	
	public static ObjectType[] trees = {
			new ObjectType("trees/tree1.png", 365, 486, 1, 0.2, true),
			new ObjectType("trees/tree2.png", 307, 581, 1, 0.2, true),
			new ObjectType("trees/tree3.png", 160, 461, 1, 0.2, true),
			new ObjectType("trees/tree4.png", 199, 487, 1, 0.2, true),
			new ObjectType("trees/tree5.png", 165, 445, 1, 0.2, true),
			new ObjectType("trees/tree6.png", 71, 73, 1, 0.2, true),
			new ObjectType("https://i.imgur.com/B8sfVYr.png", 960, 540, 1, 0.2, true),
			new ObjectType("https://i.imgur.com/P6HnmkJ.png", 480, 270, 1, 0.2, true),
			new ObjectType("https://i.imgur.com/RtroHjA.png", 480, 270, 1, 0.2, true),
			new ObjectType("https://i.imgur.com/tqxfxrw.png", 480, 270, 1, 0.2, true),
			
	};
	
	public static ObjectType[] bushes = {
			new ObjectType("bushes/bush1.png", 130, 86, 1, 0.2, true),
			new ObjectType("bushes/bush2.png", 112, 103, 1, 0.2, true),
			new ObjectType("bushes/bush3.png", 113, 100, 1, 0.2, true),
			new ObjectType("bushes/bush4.png", 112, 104, 1, 0.2, true),
			new ObjectType("bushes/bush5.png", 94, 94, 1, 0.2, true),
			new ObjectType("bushes/bush6.png", 156, 100, 1, 0.2, true),
			new ObjectType("bushes/bush7.png", 107, 105, 1, 0.2, true),
			new ObjectType("bushes/bush8.png", 98, 102, 1, 0.2, true),
	};
	
	public static ObjectType[] plants = {
			new ObjectType("plants/mushroom1.png", 33, 37, 1, 0.2, true),
			new ObjectType("plants/mushroom2.png", 58, 43, 1, 0.2, true),
			new ObjectType("plants/mushroom3.png", 55, 61, 1, 0.2, true),
			new ObjectType("plants/mushroom4.png", 34, 38, 1, 0.2, true),
			new ObjectType("plants/plant1.png", 125, 87, 1, 0.2, true),
			new ObjectType("plants/plant2.png", 118, 90, 1, 0.2, true),
			new ObjectType("plants/plant3.png", 120, 98, 1, 0.2, true),
			new ObjectType("plants/plant4.png", 131, 109, 1, 0.2, true),
			new ObjectType("plants/plant5.png", 132, 95, 1, 0.2, true),
			new ObjectType("plants/plant6.png", 132, 116, 1, 0.2, true),
			new ObjectType("plants/plant7.png", 110, 77, 1, 0.2, true),
			new ObjectType("plants/plant8.png", 115, 116, 1, 0.2, true),
			new ObjectType("plants/plant9.png", 128, 86, 1, 0.2, true),
			new ObjectType("plants/plant10.png", 104, 62, 1, 0.2, true),
			new ObjectType("plants/plant11.png", 82, 67, 1, 0.2, true),
			new ObjectType("plants/plant12.png", 47, 60, 1, 0.2, true),
			new ObjectType("plants/plant13.png", 108, 96, 1, 0.2, true),
			new ObjectType("plants/plant14.png", 92, 74, 1, 0.2, true),
			new ObjectType("plants/plant15.png", 103, 82, 1, 0.2, true),
			new ObjectType("plants/plant16.png", 93, 74, 1, 0.2, true),
	};
	
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
	

	public static int generateRandomObject(Map<String, GridObject> objectMap, ObjectType[] typeList, int x, int y, double count, long seed)
	{
		Random rnd = new Random(seed*(x+1)*(y+1));
		if (count<0) return 0;
		if (count<1) 
		{
			if (rnd.nextDouble()>=count) 
				return 0;
			else
				count = 1;
		}
		
		for(int index = 0; index<count; index++)
		{
			ObjectType type = typeList[rnd.nextInt(typeList.length)];
			double scale = 1-(rnd.nextDouble()*type.scaleVariance);
			double width = ((double)type.width)*scale*GLOBAL_SCALE;
			double height = ((double)type.height)*scale*GLOBAL_SCALE;
			objectMap.put(type.img + "tempKey:" + x + "-" + y, new GridObject(
					"tempKey:" + x + "-" + y,
					type.img,
					"",
					x, y,
					rnd.nextInt(64),
					rnd.nextInt(64),
					(int)(width / 2), (int)(height*0.95), (int)(width), (int)(height), false, false));
		}
		
		return (int)count;
	}
	
	public static GridMap buildNewGrid(int seed, int rowLength, int columnLength, int forestry) {

		GridCell[][] grid = new GridCell[rowLength][columnLength];
		SeededSimplexNoise treeSsn = new SeededSimplexNoise(seed);
		SeededSimplexNoise shrubSsn = new SeededSimplexNoise(seed + 1);
		Map<String, GridObject> objectMap = new HashMap<>();

		// Loop over grid size
		for (int i = 0; i < rowLength; i++) {
			for (int j = 0; j < columnLength; j++) {
				Random rnd = new Random(seed*(j+1)*(i+1));
				
				int treeCount = 0;
				int bushCount = 0;
				int plantCount = 0;
				
				treeCount = generateRandomObject(objectMap, trees, j, i, 0.3, seed);
				if (treeCount==0)
					bushCount = generateRandomObject(objectMap, bushes, j, i, 0.3, seed);
				
				if (treeCount>0) 
					plantCount = rnd.nextInt(4);
				else if (bushCount>0)
					plantCount = rnd.nextInt(6);
				else
					plantCount = rnd.nextInt(10);
				plantCount = generateRandomObject(objectMap, plants, j, i, plantCount, seed);
				
//				double treeNoiseResult = treeSsn.eval(j, i);
//				double shrubNoiseResult = shrubSsn.eval(j, i);
//				// Determine if object is generated at coordinate
//				if (treeNoiseResult < ((forestry / 5.0) - 1)) 
//				{
//				}
//				if (shrubNoiseResult < ((forestry / 5.0) - 1)) {
//					objectMap.put("shrub1.png" + "tempKey:" + i + "-" + j, new GridObject(
//							"tempKey:" + i + "-" + j,
//							"shrub1.png",
//							"A shrubbery!",
//							i, j,
//							new Random(seed * (i * j + i * 11 + j)).nextInt(20),
//							new Random(seed * (i * j + i * 10 + j)).nextInt(20),
//							75 / 2, ((65) / 2), 77, 65, false, false));
//				}
				// Build background data for coordinate
				grid[i][j] = new GridCell("floor/grass/tile-grass" + rnd.nextInt(7) + ".png",
						i, j,
						rnd.nextInt(10));
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
