package com.universeprojects.miniup.server.model;

public class GridObject {

	private String fileName;
	private int xOffset;
	private int yOffset;
	private int xCoord;
	private int yCoord;
	private int xAttach;
	private int yAttach;

	public GridObject(String fileName, int xOffset, int yOffset, int xCoord, int yCoord, int xAttach, int yAttach) {
		this.fileName = fileName;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.xAttach = xAttach;
		this.yAttach = yAttach;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getxOffset() {
		return xOffset;
	}

	public void setxOffset(int xOffset) {
		this.xOffset = xOffset;
	}

	public int getyOffset() {
		return yOffset;
	}

	public void setyOffset(int yOffset) {
		this.yOffset = yOffset;
	}

	public int getxCoord() {
		return xCoord;
	}

	public void setxCoord(int xCoord) {
		this.xCoord = xCoord;
	}

	public int getyCoord() {
		return yCoord;
	}

	public void setyCoord(int yCoord) {
		this.yCoord = yCoord;
	}

	public int getxAttach() {
		return xAttach;
	}

	public void setxAttach(int xAttach) {
		this.xAttach = xAttach;
	}

	public int getyAttach() {
		return yAttach;
	}

	public void setyAttach(int yAttach) {
		this.yAttach = yAttach;
	}

	@Override
	public String toString() {
		return "{\"fileName\":\"" + this.fileName +
				"\",\"xOffset\":" + this.xOffset +
				",\"yOffset\":" + this.yOffset +
				",\"xCoord\":" + this.xCoord +
				",\"yCoord\":" + this.yCoord +
				",\"xAttach\":" + this.xAttach +
				",\"yAttach\":" + this.yAttach +
				"}";
	}
}
