Arbiter.GeometryExpansionPart = function(type, parent, selfIndex){
	this.type = type;
	this.parent = parent;
	this.selfIndex = selfIndex;
	this.feature = null;
	this.nextChild = 0;
	this.length = 0;
	this.children = {};
};

Arbiter.GeometryExpansionPart.prototype.isLeaf = function(){
	return this.length === 0;
};

Arbiter.GeometryExpansionPart.prototype.isRoot = function(){
	return Arbiter.Util.existsAndNotNull(this.parent) && this.parent.type === null;
};

Arbiter.GeometryExpansionPart.prototype.remove = function(removeFeatureHandler){
	
	if(this.isLeaf()){
		
		if(Arbiter.Util.existsAndNotNull(removeFeatureHandler)){
			removeFeatureHandler(this.feature);
		}
		
		if(Arbiter.Util.existsAndNotNull(this.parent)){
			this.parent.removeChild(this);
		}
		
		return;
	}
	
	for(var key in this.children){
		this.children[key].remove(removeFeatureHandler);
	}
	
	if(Arbiter.Util.existsAndNotNull(this.parent)){
		this.parent.removeChild(this);
	}
};

Arbiter.GeometryExpansionPart.prototype.removeFromCollection = function(removeFeatureHandler){
	
	if(Arbiter.Util.existsAndNotNull(this.parent) 
			&& Arbiter.Util.existsAndNotNull(this.parent.type)
			&& this.parent.type !== "OpenLayers.Geometry.Collection"){
		
		console.log("removeFromCollection isn't collection: " + this.parent.type);
		this.parent.remove(removeFeatureHandler);
	}else{
		
		console.log("removeFromCollection safety");
		console.log("removeFromCollection collection or parent is null" + this.parent.type);
		this.remove(removeFeatureHandler);
	}
};

Arbiter.GeometryExpansionPart.prototype.addChild = function(newChild){
	
	this.children[this.nextChild++] = newChild;
	
	this.length++;
};

Arbiter.GeometryExpansionPart.prototype.removeChild = function(childToRemove){
	
	delete this.children[childToRemove.selfIndex];
	
	this.length--;
	
	// If it doesn't have any children and it's parent isn't the root
	if(this.length === 0 && !this.isRoot()){
		this.remove();
	}
};

Arbiter.GeometryExpansionPart.prototype.getSiblings = function(){
	
	var siblings = [];
	
	// If the parent is of type multipoint, multiline, or multipolygon
	if(Arbiter.Util.existsAndNotNull(this.parent)
			&& Arbiter.Util.existsAndNotNull(this.parent.type) 
			&& (this.parent.type === "OpenLayers.Geometry.MultiPoint" 
				|| this.parent.type === "OpenLayers.Geometry.MultiLineString"
				|| this.parent.type === "OpenLayers.Geometry.MultiPolygon")){
		
		var next = null;
		
		for(var key in this.parent.children){
			
			if(key !== this.selfIndex){
				siblings.push(this.parent.children[key].feature);
			}
		}
	}
	
	return siblings;
};

Arbiter.GeometryExpansionPart.prototype.getOlGeometryClass = function(geometryType){
	var olGeometryClass = null;
	
	var type = Arbiter.Geometry.type;
	
	switch(geometryType){
		case type.POINT: 
			
			olGeometryClass = "OpenLayers.Geometry.Point";
			
			break;
			
		case type.LINE:
			
			olGeometryClass = "OpenLayers.Geometry.LineString";
			
			break;
			
		case type.POLYGON:
			
			olGeometryClass = "OpenLayers.Geometry.Polygon";
			
			break;
			
		case type.MULTIPOINT:
			
			olGeometryClass = "OpenLayers.Geometry.MultiPoint";
			
			break;
			
		case type.MULTILINE:
			
			olGeometryClass = "OpenLayers.Geometry.MultiLineString";
			
			break;
			
		case type.MULTIPOLYGON:
			
			olGeometryClass = "OpenLayers.Geometry.MultiPolygon";
			
			break;
		
		default:
			
			throw "GeometryExpansionPart.addUncle() invalid geometryType: " + geometryType;
	}
	
	return olGeometryClass;
};

Arbiter.GeometryExpansionPart.prototype.addPart = function(olGeometryClass, feature, parent){
	
	var part = new Arbiter.GeometryExpansionPart(olGeometryClass, parent, parent.nextChild);
	
	parent.addChild(part);
	
	part.feature = feature;
	
	feature.metadata = {
		part: part
	};
};

Arbiter.GeometryExpansionPart.prototype.addUncle = function(geometryType, feature){
	
	var type = null;
	
	try{
		type = this.getOlGeometryClass(geometryType);
	}catch(e){
		console.log(e.stack);
	}
	
	var target = null;
	
	var parentGeometryCls = this.parent.type;
	
	if(Arbiter.Util.existsAndNotNull(parentGeometryCls) && 
			(parentGeometryCls === "OpenLayers.Geometry.MultiPoint" 
				|| parentGeometryCls === "OpenLayers.Geometry.MultiLineString" 
				|| parentGeometryCls === "OpenLayers.Geometry.MultiPolygon")){
		target = this.parent.parent;
	}else{
		target = this.parent;
	}
	
	var uncle = new Arbiter.GeometryExpansionPart(type, target, target.nextChild);
	
	target.addChild(uncle);
	
	if(uncle.type === "OpenLayers.Geometry.MultiPoint" 
		|| uncle.type === "OpenLayers.Geometry.MultiLineString" 
			|| uncle.type === "OpenLayers.Geometry.MultiPolygon"){
		
		var cousinType = uncle.type.replace("Multi", "");
		
		var cousin = new Arbiter.GeometryExpansionPart(cousinType, uncle, uncle.nextChild);
		
		uncle.addChild(cousin);
		cousin.feature = feature;
		feature.metadata = {
			part: cousin
		};
		
	}else{
		uncle.feature = feature;
		
		feature.metadata = {
			part: uncle	
		};
	}
};