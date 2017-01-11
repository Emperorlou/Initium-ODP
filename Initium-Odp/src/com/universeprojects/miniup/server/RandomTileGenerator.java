package com.universeprojects.miniup.server;

import com.universeprojects.miniup.server.model.GridBackground;
import com.universeprojects.miniup.server.model.GridObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Generator to aid with 2D tile generation.
 *
 */
public class RandomTileGenerator {

    private static String picURL = "https://initium-resources.appspot.com/images/newCombat/";
    
    private RandomTileGenerator() {
    }

    public static Map<String, Object> getBuildingCells(int seed, int rowLength, int forestry) {

        Map<String, Object> data = new HashMap<>();
        List<List<GridBackground>> grid = new ArrayList<>();

        SeededSimplexNoise ssn = new SeededSimplexNoise(seed);
        Map<String, GridObject> objectMap = new HashMap<>();

        // Loop over left sides of hexagon
        for(int i = 0;i < rowLength; i++) {
            List<GridBackground> row = new ArrayList<>();
            for (int j = 0; j < rowLength; j++) {
                double noiseResult = ssn.eval(j,i);
                if (noiseResult < ((forestry/5.0) - 1)) {
                    objectMap.put("tempKey:" + i + "-" + j, new GridObject(
                            "tree1.png",
                            new Random(seed * (i*j+ i*11 + j)).nextInt(60)*2 - 1,
                            new Random(seed * (i*j+ i*10 + j)).nextInt(60)*2 - 1,
                            j, i, 192/2, ((256)-20)));
                }
                row.add(new GridBackground("tile-grass" + (new Random(seed * (i*j+ i*10 + j)).nextInt(7)) + ".png",
                        (new Random(seed * (i*10 + j)).nextInt(10)))
                );
            }
            grid.add(row);
        }
        data.put("backgroundTiles", grid);
        data.put("objectMap", objectMap);
        return data;
    }
}