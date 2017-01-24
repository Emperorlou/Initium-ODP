package com.universeprojects.miniup.server.model;

import org.json.simple.JSONObject;

public class UpdateGridCell {
	private int xGridCoord;
	private int yGridCoord;
	private String payload;
	private JSONObject jsonObject;

	public UpdateGridCell(int xGridCoord, int yGridCoord, String payload) {
		this.xGridCoord = xGridCoord;
		this.yGridCoord = yGridCoord;
		this.payload = payload;
		jsonObject = new JSONObject();
		jsonObject.put("xGridCoord", xGridCoord);
		jsonObject.put("yGridCoord", xGridCoord);
		jsonObject.put("xGridCoord", xGridCoord);
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

	public String getPayLoad() {
		return payload;
	}

	public void setPayLoad(String payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "{\"xGridCoord\":" + this.xGridCoord +
				",\"yGridCoord\":" + this.yGridCoord +
				",\"payload\":\"" + this.payload + "\"}";
	}
}
