package com.universeprojects.miniup.server.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.Map;

public class GridMap {
	
	private GridCell[][] map;
	private Map<String, GridObject> gridObjects;

	public GridMap(GridCell[][] map, Map<String, GridObject> gridObjects) {
		this.map = map;
		this.gridObjects = gridObjects;
	}

	public GridCell[][] getMap() {
		return map;
	}

	public void setMap(GridCell[][] map) {
		this.map = map;
	}

	public Map<String, GridObject> getGridObjects() {
		return gridObjects;
	}

	public void setGridObjects(Map<String, GridObject> gridObjects) {
		this.gridObjects = gridObjects;
	}

	@Override
	public String toString() {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonGrid = new JSONArray();
		for (GridCell[] column : map) {
			JSONArray jsonColumn  = new JSONArray();
			jsonColumn.addAll(Arrays.asList(column));
			jsonGrid.add(jsonColumn);
		}
		jsonObject.put("backgroundTiles", jsonGrid);
		jsonObject.put("objectMap", gridObjects);
		return jsonObject.toJSONString();
	}
}
