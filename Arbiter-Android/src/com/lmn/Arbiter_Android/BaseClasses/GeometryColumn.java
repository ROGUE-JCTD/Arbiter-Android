package com.lmn.Arbiter_Android.BaseClasses;

public class GeometryColumn {
	private long geometryColumnId;
	private String featureType;
	private String geometryColumn;
	private String geometryType;
	private String srid;
	private String enumeration;

	public GeometryColumn(long geometryColumnId, String featureType,
			String geometryColumn, String geometryType, String srid,
			String enumeration) {

		this.geometryColumnId = geometryColumnId;
		this.featureType = featureType;
		this.geometryColumn = geometryColumn;
		this.geometryType = geometryType;
		this.srid = srid;
		this.enumeration = enumeration;
	}

	public long getId() {
		return this.geometryColumnId;
	}

	public String getFeatureType() {
		return this.featureType;
	}

	public void setFeatureType(String featureType) {
		this.featureType = featureType;
	}

	public String getGeometryColumn() {
		return this.geometryColumn;
	}

	public void setGeometryColumn(String geometryColumn) {
		this.geometryColumn = geometryColumn;
	}

	public String getGeometryType() {
		return this.geometryType;
	}

	public void setGeometryType(String geometryType) {
		this.geometryType = geometryType;
	}

	public String getSRID() {
		return this.srid;
	}

	public void setSRID(String srid) {
		this.srid = srid;
	}

	public String getEnumeration() {
		return this.enumeration;
	}

	public void setEnumeration(String enumeration) {
		this.enumeration = enumeration;
	}
}
