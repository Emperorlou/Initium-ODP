package com.universeprojects.miniup.server.model;

import com.universeprojects.json.shared.JSONObject;
import com.universeprojects.miniup.server.WebUtils;


public class GridObject {

	private static String key = "key";
	private static String filename = "filename";
	private static String name = "name";
	private static String xGridCoord = "xGridCoord";
	private static String yGridCoord = "yGridCoord";
	private static String xGridCoordLocal = "xGridCoordLocal";
	private static String yGridCoordLocal = "yGridCoordLocal";
	private static String xGridCellOffset = "xGridCellOffset";
	private static String yGridCellOffset = "yGridCellOffset";
	private static String xImageOrigin = "xImageOrigin";
	private static String yImageOrigin = "yImageOrigin";
	private static String width = "width";
	private static String height = "height";
	private static String markForRemoval = "markForRemoval";
	private static String markForDeletion = "markForDeletion";
	private static String rotation = "rotation";
	private JSONObject jsonObject;

	public GridObject() {
		jsonObject = new JSONObject();
	}

	public GridObject(String key) {
		jsonObject = new JSONObject();
		setKey(key);
	}
	
	public GridObject(String key, String filename, String name, Integer xGridCoord, Integer yGridCoord, Double rotation, Integer xGridCellOffset, Integer yGridCellOffset, Integer xImageOrigin,
			Integer yImageOrigin, Integer width, Integer height, boolean markForRemoval, boolean markForDeletion, 
			Integer gridStartX, Integer gridStartY, Double lightLevel) {
		jsonObject = new JSONObject();
		setKey(key);
		setfilename(filename);
		setName(WebUtils.jsSafe(name));
		setxGridCoord(xGridCoord, gridStartX);
		setyGridCoord(yGridCoord, gridStartY);
		setRotation(rotation);
		setxGridCellOffset(xGridCellOffset);
		setyGridCellOffset(yGridCellOffset);
		setxImageOrigin(xImageOrigin);
		setyImageOrigin(yImageOrigin);
		setWidth(width);
		setHeight(height);
		setMarkForRemoval(markForRemoval);
		setMarkForDeletion(markForDeletion);
		setLightLevel(lightLevel);
	}
	
	public void setLightLevel(Double level)
	{
		jsonObject.put("lightLevel", level);
	}

	public boolean getMarkForDeletion() { return (boolean) jsonObject.get(this.markForDeletion);}

	public void setMarkForDeletion(boolean markForDeletion) { jsonObject.put(this.markForDeletion, markForDeletion);}
	
	public boolean getMarkForRemoval() { return (boolean) jsonObject.get(this.markForRemoval);}
	
	public void setMarkForRemoval(boolean markForRemoval) { jsonObject.put(this.markForRemoval, markForRemoval);}

	public String getKey() {
		return (String) jsonObject.get(this.key);
	}

	public void setKey(String key) {
		jsonObject.put(this.key, key);
	}

	public String getfilename() {
		return (String) jsonObject.get(this.filename);
	}

	public void setfilename(String filename) {
		jsonObject.put(this.filename, filename);
	}

	public String getName() {
		return (String) jsonObject.get(this.name);
	}

	public void setName(String name) {
		jsonObject.put(this.name, name);
	}

	public Double getRotation() {
		return (Double) jsonObject.get(this.rotation);
	}

	public void setRotation(Double value) {
		jsonObject.put(this.rotation, value);
	}

	public Integer getxGridCoord() {
		return (Integer) jsonObject.get(this.xGridCoord);
	}

	public void setxGridCoord(Integer xGridCoord, Integer gridStartX) {
		jsonObject.put(this.xGridCoord, xGridCoord);
		jsonObject.put(this.xGridCoordLocal, xGridCoord-gridStartX);
	}

	public Integer getyGridCoord() {
		return (Integer) jsonObject.get(this.yGridCoord);
	}

	public void setyGridCoord(Integer yGridCoord, Integer gridStartY) {
		jsonObject.put(this.yGridCoord, yGridCoord);
		jsonObject.put(this.yGridCoordLocal, yGridCoord-gridStartY);
	}

	public Integer getxGridCellOffset() {
		return (Integer) jsonObject.get(this.xGridCellOffset);
	}

	public void setxGridCellOffset(Integer xGridCellOffset) {
		jsonObject.put(this.xGridCellOffset, xGridCellOffset);
	}

	public Integer getyGridCellOffset() {
		return (Integer) jsonObject.get(this.yGridCellOffset);
	}

	public void setyGridCellOffset(Integer yGridCellOffset) {
		jsonObject.put(this.yGridCellOffset, yGridCellOffset);
	}

	public Integer getxImageOrigin() {
		return (Integer) jsonObject.get(this.xImageOrigin);
	}

	public void setxImageOrigin(Integer xImageOrigin) {
		jsonObject.put(this.xImageOrigin, xImageOrigin);
	}

	public Integer getyImageOrigin() {
		return (Integer) jsonObject.get(this.yImageOrigin);
	}

	public void setyImageOrigin(Integer yImageOrigin) {
		jsonObject.put(this.yImageOrigin, yImageOrigin);
	}

	public Integer getWidth() {
		return (Integer) jsonObject.get(this.width);
	}

	public void setWidth(Integer width) {
		jsonObject.put(this.width, width);
	}

	public Integer getHeight() {
		return (Integer) jsonObject.get(this.height);
	}

	public void setHeight(Integer height) {
		jsonObject.put(this.height, height);
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
