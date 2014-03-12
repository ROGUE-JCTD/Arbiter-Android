Arbiter.GeometryExpander = function(){
	this.record = new Arbiter.GeometryExpansionPart(null, null, null);
	
	// Array to be used for adding the features to the map
	this.features = [];
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
	
	if(!Arbiter.Util.existsAndNotNull(parent)){
		parent = this.record;
	}
	
	console.log("expand olGeometryClass = " + olGeometryClass);
	
	var obj = new Arbiter.GeometryExpansionPart(olGeometryClass, parent, parent.nextChild);
	
	// Add the part to it's parent's children
	//parent.children[current] = obj;
	parent.addChild(obj);
	
	if(this.isCollection(geometry)){
		for(var i = 0; i < geometry.components.length; i++){
			this.expand(geometry.components[i], obj);
		}
	}else{
		var feature = new OpenLayers.Feature.Vector(geometry);
		
		feature.metadata = {
			part: obj
		};
		
		obj.feature = feature;
		
		this.features.push(feature);
	}
};

Arbiter.GeometryExpander.prototype.compress = function(){
	var geometry = null;
	
	try{
		this.print();
		geometry = this.getChildComponents(this.record);
		console.log("compress: ", geometry);
	}catch(e){
		console.log(e.stack);
	}
	
	return geometry;
};

Arbiter.GeometryExpander.prototype.getGeometry = function(type, components){
	
	console.log("expander getGeometry: type = " + type);
	
	if(!Arbiter.Util.existsAndNotNull(type)){
		return null;
	}
	
	var geometry = null;
	
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
	
	return geometry;
};

Arbiter.GeometryExpander.prototype.getChildComponents = function(next){
	var geometry = null;
	var components = [];
	
	if(next.isLeaf()){
		console.log("leaf and type = " + next.type, next);
		
		if(Arbiter.Util.existsAndNotNull(next.feature)){
			geometry = next.feature.geometry;
		}else{
			geometry = this.getGeometry(next.type, components);
		}
		
		console.log("isLeaf", geometry);
		
		return geometry;
	}
	
	for(var key in next.children){
	
		components.push(this.getChildComponents(next.children[key]));
	}
	
	console.log("before getGeometry", components);
	geometry = this.getGeometry(next.type, components);
	console.log("after getGeometry", geometry);
	
	if(!Arbiter.Util.existsAndNotNull(geometry) && components.length === 1){
		geometry = components[0];
	}
	
	console.log("geometry", geometry);
	
	return geometry;
};

Arbiter.GeometryExpander.prototype.print = function(next){
	
	if(!Arbiter.Util.existsAndNotNull(next)){
		next = this.record;
		console.log("printing GeometryExpander.record");
	}else{
		console.log("type: " + next.type + ", length = " + next.length);
	}
	
	for(var key in next.children){
		this.print(next.children[key]);
	}
};


