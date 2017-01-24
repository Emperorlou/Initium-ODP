package com.universeprojects.miniup.server.model;

public class UpdateGridObject {
	
	private String key;
	private String payload;

	public UpdateGridObject(String key, String payload) {
		this.key = key;
		this.payload = payload;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "{\"key\":\"" + this.key +
				"\",\"payLoad\":\"" + this.payload + "\"}";
	}
}
