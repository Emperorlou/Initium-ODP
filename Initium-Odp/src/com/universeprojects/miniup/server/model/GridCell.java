package com.universeprojects.miniup.server.model;

public class GridCell {
	private String backgroundFile;
	private int xGridCoord;
	private int yGridCoord;
	private int zIndex;

	public GridCell(String backgroundFile, int xGridCoord, int yGridCoord, int zIndex) {
		this.backgroundFile = backgroundFile;
		this.xGridCoord = xGridCoord;
		this.yGridCoord = yGridCoord;
		this.zIndex = zIndex;
	}

	public String getBackgroundFile() {
		return backgroundFile;
	}

	public void setBackgroundFile(String backgroundFile) {
		this.backgroundFile = backgroundFile;
	}

	public int getxGridCoord() {
		return xGridCoord;
	}

	public void setxGridCoord(int xGridCoord) {
		this.xGridCoord = xGridCoord;
	}

	public int getyGridCoord() {
		return yGridCoord;
	}

	public void setyGridCoord(int yGridCoord) {
		this.yGridCoord = yGridCoord;
	}

	public int getzIndex() {
		return zIndex;
	}

	public void setzIndex(int zIndex) {
		this.zIndex = zIndex;
	}

	@Override
	public String toString() {
			return "{\"backgroundFile\":\"" + this.backgroundFile +
					"\",\"xGridCoord\":" + this.xGridCoord +
					",\"yGridCoord\":" + this.yGridCoord +
					"\",\"zIndex\":" + this.zIndex + "}";
	}
}
