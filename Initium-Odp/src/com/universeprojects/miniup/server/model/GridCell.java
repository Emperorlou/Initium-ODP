package com.universeprojects.miniup.server.model;

import org.json.simple.JSONObject;

public class GridCell {
	private String backgroundFile;
	private int xGridCoord;
	private int yGridCoord;
	private int zIndex;
	private JSONObject jsonObject;
	
	public GridCell() {
		jsonObject = new JSONObject();
	}

	public GridCell(String backgroundFile, int xGridCoord, int yGridCoord, int zIndex) {
		this.backgroundFile = backgroundFile;
		this.xGridCoord = xGridCoord;
		this.yGridCoord = yGridCoord;
		this.zIndex = zIndex;
		
		jsonObject = new JSONObject();
		jsonObject.put("backgroundFile", backgroundFile);
		jsonObject.put("xGridCoord", xGridCoord);
		jsonObject.put("yGridCoord", yGridCoord);
		jsonObject.put("zIndex", zIndex);
	}

	public String getBackgroundFile() {
		return backgroundFile;
	}

	public void setBackgroundFile(String backgroundFile) {
		this.backgroundFile = backgroundFile;
		this.jsonObject.put("backgroundFile", backgroundFile);
	}

	public int getxGridCoord() {
		return xGridCoord;
	}

	public void setxGridCoord(int xGridCoord) {
		this.xGridCoord = xGridCoord;
		this.jsonObject.put("xGridCoord", xGridCoord);
	}

	public int getyGridCoord() {
		return yGridCoord;
	}

	public void setyGridCoord(int yGridCoord) {
		this.yGridCoord = yGridCoord;
		this.jsonObject.put("yGridCoord", yGridCoord);
	}

	public int getzIndex() {
		return zIndex;
	}

	public void setzIndex(int zIndex) {
		this.zIndex = zIndex;
		this.jsonObject.put("zIndex", zIndex);
	}

	public JSONObject getJsonObject() {
		return jsonObject;
	}

	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	@Override
	public String toString() {
			return jsonObject.toString();
	}
}
