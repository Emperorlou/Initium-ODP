package com.universeprojects.miniup.server.model;

public class GridBackground {
	private String backgroundFile;
	private int zIndex;

	public String getBackgroundFile() {
		return backgroundFile;
	}

	public void setBackgroundFile(String backgroundFile) {
		this.backgroundFile = backgroundFile;
	}

	public int getzIndex() {
		return zIndex;
	}

	public void setzIndex(int zIndex) {
		this.zIndex = zIndex;
	}

	public GridBackground(String backgroundFile, int zIndex) {
		this.backgroundFile = backgroundFile;
		this.zIndex = zIndex;
		
	}

	@Override
	public String toString() {
			return "{\"backgroundFile\":\"" + this.backgroundFile +
					"\",\"zIndex\":" + this.zIndex + "}";
	}
}
