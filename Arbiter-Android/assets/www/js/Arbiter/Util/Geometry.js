Arbiter.Util.Geometry = function(){
	return {
		
	};
};

Arbiter.Util.Geometry.type = {
	POINT: 0,
	LINE: 1,
	POLYGON: 2
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
	
	throw "Arbiter.Util.Geometry.getGeometryType - "
		+ "Geometry type '" + type + "' is not "
		+ "supported yet!";
};