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
    
    private RandomTileGenerator() {
    }

    public static Map<String, Object> getBuildingCells(int seed, int rowLength, int columnLength, int forestry) {

        Map<String, Object> data = new HashMap<>();
        List<List<GridBackground>> grid = new ArrayList<>();

        SeededSimplexNoise ssn = new SeededSimplexNoise(seed);
        Map<String, GridObject> objectMap = new HashMap<>();

        // Loop over left sides of hexagon
        for(int i = 0;i < rowLength; i++) {
            List<GridBackground> row = new ArrayList<>();
            for (int j = 0; j < columnLength; j++) {
                double noiseResult = ssn.eval(j,i);
                if (noiseResult < ((forestry/5.0) - 1)) {
                    objectMap.put("tempKey:" + i + "-" + j, new GridObject(
                            "tempKey:" + i + "-" + j,
                            "tree1.png",
                            i, j,
                            new Random(seed * (i*j+ i*11 + j)).nextInt(20),
                            new Random(seed * (i*j+ i*10 + j)).nextInt(20),
                            192/2, ((256)-20), 192, 256));
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