(function(){
	
	Arbiter.GeometryExpander = function(){
		this.record = new Arbiter.GeometryExpansionPart(null, null, null);
		
		// Array to be used for adding the features to the map
		this.features = [];
	};

	var prototype = Arbiter.GeometryExpander.prototype;
	
	prototype.isCollection = function(geometry){
		var olGeometryClass = geometry.CLASS_NAME;
		
		return olGeometryClass === "OpenLayers.Geometry.Collection" 
			|| olGeometryClass === "OpenLayers.Geometry.MultiPoint"
			|| olGeometryClass === "OpenLayers.Geometry.MultiPolygon"
			|| olGeometryClass === "OpenLayers.Geometry.MultiLineString";
	};

	prototype.expand = function(geometry, parent){
		
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

	prototype.getOlGeometryClass = function(geometryType){
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

	prototype.addToCollection = function(geometryType, feature){
		
		var parent = this.record.children[0];
		
		var type = this.getOlGeometryClass(geometryType);
		
		var child = new Arbiter.GeometryExpansionPart(type, parent, parent.nextChild);
		
		parent.addChild(child);
		
		if(child.type === "OpenLayers.Geometry.MultiPoint" 
			|| child.type === "OpenLayers.Geometry.MultiLineString" 
				|| child.type === "OpenLayers.Geometry.MultiPolygon"){
			
			var leafType = child.type.replace("Multi", "");
			
			var leaf = new Arbiter.GeometryExpansionPart(leafType, child, child.nextChild);
			
			child.addChild(leaf);
			leaf.feature = feature;
			feature.metadata = {
				part: leaf
			};
			
		}else{
			child.feature = feature;
			
			feature.metadata = {
				part: child	
			};
		}
	};

	prototype.compress = function(){
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

	prototype.getGeometry = function(type, components){
		
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

	prototype.getChildComponents = function(next){
		var geometry = null;
		var components = [];
		
		if(next.isLeaf()){
			
			if(Arbiter.Util.existsAndNotNull(next.feature)){
				geometry = next.feature.geometry.clone();
			}else{
				geometry = this.getGeometry(next.type, components);
			}
			
			return geometry;
		}
		
		for(var key in next.children){
		
			components.push(this.getChildComponents(next.children[key]));
		}
		
		console.log("getChildComponents type = " + next.type + ", components = ", components);
		
		geometry = this.getGeometry(next.type, components);
		
		if(!Arbiter.Util.existsAndNotNull(geometry) && components.length === 1){
			geometry = components[0];
		}
		
		return geometry;
	};

	prototype.print = function(next){
		
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
})();


