Arbiter.GeometryExpander = function(){
	this.record = {
		nextChild: 0
	};
	
	// Array to be used for adding the features to the map
	this.features = [];
	this.id = -1;
};

Arbiter.GeometryExpander.prototype.isLeaf = function(obj){
	return obj.nextChild === 0;
};

Arbiter.GeometryExpander.prototype.isCollection = function(geometry){
	var olGeometryClass = geometry.CLASS_NAME;
	
	return olGeometryClass === "OpenLayers.Geometry.Collection" 
		|| olGeometryClass === "OpenLayers.Geometry.MultiPoint"
		|| olGeometryClass === "OpenLayers.Geometry.MultiPolygon"
		|| olGeometryClass === "OpenLayers.Geometry.MultiLineString";
};

Arbiter.GeometryExpander.prototype.expand = function(geometry, parent){
	
	var olGeometryClass = geometry.CLASS_NAME;
	
	var obj = {
		type: olGeometryClass,
		nextChild: 0
	};
	
	if(!Arbiter.Util.existsAndNotNull(parent)){
		parent = this.record;
	}
		
	var current = parent.nextChild++;
	
	parent[current] = obj;
	
	if(this.isCollection(geometry)){
		for(var i = 0; i < geometry.components.length; i++){
			this.expand(geometry.components[i], parent[current]);
		}
	}else{
		var feature = new OpenLayers.Feature.Vector(geometry);
		
		feature.metadata = {
			parent: parent,
			selfIndex: current
		};
		
		parent[current].feature = feature;
		
		this.features.push(feature);
	}
};

Arbiter.GeometryExpander.prototype.compress = function(){
	var geometry = null;
	
	try{
		console.log("before compressing");
		geometry = this.getChildComponents(this.record);
		console.log("geometry = " + geometry);
	}catch(e){
		console.log(e.stack);
	}
	
	return geometry;
};

Arbiter.GeometryExpander.prototype.getGeometry = function(type, components){
	
	if(!Arbiter.Util.existsAndNotNull(type)){
		return null;
	}
	
	var geometry = null;
	
	console.log("getGeometry begin");
	
	if(type === "OpenLayers.Geometry.Collection"){
		geometry = new OpenLayers.Geometry.Collection(components);
	}else if(type === "OpenLayers.Geometry.MultiPoint"){
		geometry = new OpenLayers.Geometry.MultiPoint(components);
	}else if(type === "OpenLayers.Geometry.MultiLineString"){
		geometry = new OpenLayers.Geometry.MultiLineString(components);
	}else if(type === "OpenLayers.Geometry.MultiPolygon"){
		geometry = new OpenLayers.Geometry.MultiPolygon(components);
	}else{
		throw "Invalid geometry type for Arbiter.GeometryExpander.getGeometry: " + type;
	}
	
	console.log("getGeometry: ", geometry);
	
	return geometry;
};

Arbiter.GeometryExpander.prototype.getChildComponents = function(next){
	
	if(this.isLeaf(next)){
		console.log("is leaf");
		return next.feature.geometry;
	}
	
	console.log("getting child components");
	
	var geometry = null;
	
	var components = [];
	
	console.log("hi friend");
	
	for(var key in next){
	
		if(key !== "type" && key !== "nextChild"
			&& key !== "feature"){
			
			components.push(this.getChildComponents(next[key]));
		}
	}
	
	geometry = this.getGeometry(next.type, components);
	
	if(!Arbiter.Util.existsAndNotNull(geometry) && components.length === 1){
		geometry = components[0];
	}
	
	return geometry;
};


