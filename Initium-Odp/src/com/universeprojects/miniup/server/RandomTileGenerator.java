package com.universeprojects.miniup.server;

import java.util.Random;

/**
 * Generator to aid with 2D tile generation.
 *
 */
public class RandomTileGenerator {

    private static String picURL = "https://initium-resources.appspot.com/images/newCombat/";
    
    private RandomTileGenerator() {
    }

    public static BuildingCell[][] getBuildingCells(int seed, int row, int col) {

        BuildingCell[][] buildingCells = new BuildingCell[row][col];
        for(int i = 0;i < row;i++) {
            for(int j = 0;j < col;j++) {
                //TODO: Turn this into a int and mod(%) it down, will need to take in number of possible tiles, and change BuildingCells filename to a index.
                buildingCells[i][j] = new BuildingCell(
                        "tile-grass" + (new Random(seed * (i*j+ i*10 + j)).nextInt(7)) + ".png",
                        (new Random(seed * (i*10 + j)).nextInt(10))
                );
            }
        }
        return buildingCells;
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
        return "fileName: '" + this.fileName + "', zIndex: '" + this.zIndex;
    }
}