package com.universeprojects.miniup.server.services;

import com.universeprojects.miniup.server.model.GridMap;
import com.universeprojects.miniup.server.model.UpdateGridCell;
import com.universeprojects.miniup.server.model.UpdateGridObject;

import java.util.HashMap;
import java.util.Map;

public class GridMapService {
	
	public Map<String, Object> updateGridCellBackground(GridMap gridMap, int row, int column, String backgroundFile) {
		gridMap.getMap()[row][column].setBackgroundFile(backgroundFile);
		Map<String, Object> ret = new HashMap<>();
		ret.put("backgroundFile", new UpdateGridCell(column, row, backgroundFile));
		return ret;
	}

	public Map<String, Object> updateGridObjectName(GridMap gridMap, String objectKey, String newName) {
		gridMap.getGridObjects().get(objectKey).setName(newName);
		Map<String, Object> ret = new HashMap<>();
		ret.put("name", new UpdateGridObject(objectKey, newName));
		return ret;
	}
}
