package com.universeprojects.miniup.server.model;

public class UpdateGridCell {
	private int xGridCoord;
	private int yGridCoord;
	private Object payload;

	public UpdateGridCell(int xGridCoord, int yGridCoord, Object payload) {
		this.xGridCoord = xGridCoord;
		this.yGridCoord = yGridCoord;
		this.payload = payload;
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

	public Object getPayLoad() {
		return payload;
	}

	public void setPayLoad(Object payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "{\"xGridCoord\":" + this.xGridCoord +
				",\"yGridCoord\":" + this.yGridCoord +
				",\"payload\":\"" + this.payload.toString() + "\"}";
	}
}
