package com.universeprojects.miniup.server.model;

import org.json.simple.JSONObject;

public class GridCell {
	private static String backgroundFile = "backgroundFile";
	private static String xGridCoord = "xGridCoord";
	private static String yGridCoord = "yGridCoord";
	private static String zIndex = "zIndex";
	private JSONObject jsonObject;
	
	public GridCell() {
		jsonObject = new JSONObject();
	}

	public GridCell(String backgroundFile, int xGridCoord, int yGridCoord, int zIndex) {
		jsonObject = new JSONObject();
		jsonObject.put(this.backgroundFile, backgroundFile);
		jsonObject.put(this.xGridCoord, xGridCoord);
		jsonObject.put(this.yGridCoord, yGridCoord);
		jsonObject.put(this.zIndex, zIndex);
	}

	public String getBackgroundFile() {
		return (String) this.jsonObject.get(this.backgroundFile);
	}

	public void setBackgroundFile(String backgroundFile) {
		this.backgroundFile = backgroundFile;
		this.jsonObject.put(this.backgroundFile, backgroundFile);
	}

	public int getxGridCoord() {
		return (int) this.jsonObject.get(this.xGridCoord);
	}

	public void setxGridCoord(int xGridCoord) {
		this.jsonObject.put("xGridCoord", xGridCoord);
	}

	public int getyGridCoord() {
		return (int) this.jsonObject.get(this.yGridCoord);
	}

	public void setyGridCoord(int yGridCoord) {
		this.jsonObject.put("yGridCoord", yGridCoord);
	}

	public int getzIndex() {
		return (int) this.jsonObject.get(this.zIndex);
	}

	public void setzIndex(int zIndex) {
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
