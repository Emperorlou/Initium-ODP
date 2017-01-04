package com.universeprojects.miniup.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generator to aid with 2D tile generation.
 *
 */
public class RandomTileGenerator {

    private static String picURL = "https://initium-resources.appspot.com/images/newCombat/";
    
    private RandomTileGenerator() {
    }

    public static List<List<BuildingCell>> getBuildingCells(int seed, int hexEdge) {

        List<List<BuildingCell>> grid = new ArrayList<>();

        int rowLength = hexEdge;
        int diagonalLength = hexEdge*2 - 1;
        boolean reachedHalf = false;

        // Loop over left sides of hexagon
        for(int i = 0;i < diagonalLength; i++) {
            List<BuildingCell> hexRow = new ArrayList<>();
            for(int j = 0;j < rowLength; j++) {
                hexRow.add(new BuildingCell("tile-grass" + (new Random(seed * (i*j+ i*10 + j)).nextInt(7)) + ".png",
                        (new Random(seed * (i*10 + j)).nextInt(10)))
                );
            }
            grid.add(hexRow);
            if (rowLength < diagonalLength && !reachedHalf) {
                rowLength++;
                if (rowLength == diagonalLength) {
                    reachedHalf = true;
                }
            } else {
                rowLength--;
            }
        }
        return grid;
    }
}

class BuildingCell {
    private String fileName;
    private int zIndex;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public BuildingCell(String fileName, int zIndex) {
        this.fileName = fileName;
        this.zIndex = zIndex;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        BuildingCell rhs = (BuildingCell) obj;
        return rhs.fileName.equals(this.fileName) && rhs.zIndex == this.zIndex;
    }

    @Override
    public String toString() {
        return "fileName: '" + this.fileName + "', zIndex: '" + this.zIndex + "'";
    }
}