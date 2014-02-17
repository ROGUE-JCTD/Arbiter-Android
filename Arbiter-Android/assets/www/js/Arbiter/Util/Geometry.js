Arbiter.Util.Geometry = function(){
	return {
		
	};
};

Arbiter.Util.Geometry.type = {
	POINT: 0,
	LINE: 1,
	POLYGON: 2,
	MULTIPOINT: 3,
	MULTILINE: 4,
	MULTIPOLYGON: 5,
	GEOMETRY: 6,
	MULTIGEOMETRY: 7
};

/**
 * Get the geometry type of a layer given the layer id
 * @param {Number} layerId The id of the layer
 */
Arbiter.Util.Geometry.getGeometryType = function(layerId){
	var schemas = Arbiter.getLayerSchemas();
	
	var type = schemas[layerId].getGeometryType();
	
	if(type === "Point"){
		return Arbiter.Util.Geometry.type.POINT;
	}
	
	if(type === "Line"){
		return Arbiter.Util.Geometry.type.LINE;
	}
	
	if(type === "Polygon"){
		return Arbiter.Util.Geometry.type.POLYGON;
	}
	
	if(type === "MultiPoint"){
		return Arbiter.Util.Geometry.type.MULTIPOINT;
	}
	
	if(type === "MultiLineString"){
		return Arbiter.Util.Geometry.type.MULTILINE;
	}

	if(type === "MultiCurve"){
		return Arbiter.Util.Geometry.type.MULTILINE;
	}
	
	if(type === "MultiPolygon"){
		return Arbiter.Util.Geometry.type.MULTIPOLYGON;
	}
	
	if(type === "MultiSurface"){
		return Arbiter.Util.Geometry.type.MULTIPOLYGON;
	}
	
	if(type === "Geometry"){
		return Arbiter.Util.Geometry.type.GEOMETRY;
	}
	
	if(type === "MultiGeometry"){
		return Arbiter.Util.Geometry.type.MULTIGEOMETRY;
	}
	
	throw "Arbiter.Util.Geometry.getGeometryType - "
		+ "Geometry type '" + type + "' is not "
		+ "supported yet!";
};