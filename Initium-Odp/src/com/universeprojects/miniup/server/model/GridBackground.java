package com.universeprojects.miniup.server.model;

public class GridBackground {
	private String backgroundFile;
	private int zIndex;

	public String getBackgroundFile() {
		return backgroundFile;
	}

	public void setBackgroundFile(String backgroundFile) {
		this.backgroundFile = backgroundFile;
	}

	public int getzIndex() {
		return zIndex;
	}

	public void setzIndex(int zIndex) {
		this.zIndex = zIndex;
	}

	public GridBackground(String backgroundFile, int zIndex) {
		this.backgroundFile = backgroundFile;
		this.zIndex = zIndex;
		
	}
//
//	@Override
//	public boolean equals(Object obj) {
//
//		if (obj == null) {
//			return false;
//		}
//		if (obj == this) {
//			return true;
//		}
//		if (obj.getClass() != getClass()) {
//			return false;
//		}
//
//		GridCell rhs = (GridCell) obj;
//		return rhs.backgroundFile.equals(this.backgroundFile) 
//				&& rhs.zIndex == this.zIndex
//				&& rhs.gridObjects == this.gridObjects;
//	}

	@Override
	public String toString() {
			return "{\"backgroundFile\":\"" + this.backgroundFile +
					"\",\"zIndex\":" + this.zIndex + "}";
	}
}
