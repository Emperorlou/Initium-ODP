package com.universeprojects.miniup.server.model;

public class GridObject {

	private String key;
	private String fileName;
	private String name;
	private int xGridCoord;
	private int yGridCoord;
	private int xGridCellOffset;
	private int yGridCellOffset;
	private int xImageOrigin;
	private int yImageOrigin;
	private int width;
	private int height;

	public GridObject(String key, String fileName, String name, int xGridCoord, int yGridCoord, int xGridCellOffset, int yGridCellOffset, int xImageOrigin,
			int yImageOrigin, int width, int height) {
		this.key = key;
		this.fileName = fileName;
		this.name = name;
		this.xGridCoord = xGridCoord;
		this.yGridCoord = yGridCoord;
		this.xGridCellOffset = xGridCellOffset;
		this.yGridCellOffset = yGridCellOffset;
		this.xImageOrigin = xImageOrigin;
		this.yImageOrigin = yImageOrigin;
		this.width = width;
		this.height = height;
	}

	@Override
	public String toString() {
		return "{\"key\":\"" + this.key +
				"\",\"fileName\":\"" + this.fileName +
				"\",\"name\":" + this.fileName +
				"\",\"xGridCoord\":" + this.xGridCoord +
				",\"yGridCoord\":" + this.yGridCoord +
				",\"xGridCellOffset\":" + this.xGridCellOffset +
				",\"yGridCellOffset\":" + this.yGridCellOffset +
				",\"xImageOrigin\":" + this.xImageOrigin +
				",\"yImageOrigin\":" + this.yImageOrigin +
				",\"width\":" + this.width +
				",\"height\":" + this.height +
				"}";
	}
}
