package com.universeprojects.miniup.server.model;

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
}
