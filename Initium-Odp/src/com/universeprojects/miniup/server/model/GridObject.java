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

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public int getxGridCellOffset() {
		return xGridCellOffset;
	}

	public void setxGridCellOffset(int xGridCellOffset) {
		this.xGridCellOffset = xGridCellOffset;
	}

	public int getyGridCellOffset() {
		return yGridCellOffset;
	}

	public void setyGridCellOffset(int yGridCellOffset) {
		this.yGridCellOffset = yGridCellOffset;
	}

	public int getxImageOrigin() {
		return xImageOrigin;
	}

	public void setxImageOrigin(int xImageOrigin) {
		this.xImageOrigin = xImageOrigin;
	}

	public int getyImageOrigin() {
		return yImageOrigin;
	}

	public void setyImageOrigin(int yImageOrigin) {
		this.yImageOrigin = yImageOrigin;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public String toString() {
		return "{\"key\":\"" + this.key +
				"\",\"fileName\":\"" + this.fileName +
				"\",\"name\":\"" + this.fileName +
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
