package com.universeprojects.miniup.server.model;

import com.universeprojects.json.shared.JSONObject;


public class GridCell {
	private static String backgroundFile = "backgroundFile";
	private static String xGridCoord = "xGridCoord";
	private static String yGridCoord = "yGridCoord";
	private static String zIndex = "zIndex";
	private JSONObject jsonObject;
	
	public GridCell() {
		jsonObject = new JSONObject();
	}

	public GridCell(Integer xGridCoord, Integer yGridCoord) {
		jsonObject = new JSONObject();
		setxGridCoord(xGridCoord);
		setyGridCoord(yGridCoord);
	}

	public GridCell(String backgroundFile, Integer xGridCoord, Integer yGridCoord, Integer zIndex) {
		jsonObject = new JSONObject();
		setBackgroundFile(backgroundFile);
		setxGridCoord(xGridCoord);
		setyGridCoord(yGridCoord);
		setzIndex(zIndex);
	}

	public String getBackgroundFile() {
		return (String) this.jsonObject.get(this.backgroundFile);
	}

	public void setBackgroundFile(String backgroundFile) {
		this.jsonObject.put(this.backgroundFile, backgroundFile);
	}

	public Integer getxGridCoord() {
		return (Integer) this.jsonObject.get(this.xGridCoord);
	}

	public void setxGridCoord(Integer xGridCoord) {
		this.jsonObject.put("xGridCoord", xGridCoord);
	}

	public Integer getyGridCoord() {
		return (Integer) this.jsonObject.get(this.yGridCoord);
	}

	public void setyGridCoord(Integer yGridCoord) {
		this.jsonObject.put("yGridCoord", yGridCoord);
	}

	public Integer getzIndex() {
		return (Integer) this.jsonObject.get(this.zIndex);
	}

	public void setzIndex(Integer zIndex) {
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
