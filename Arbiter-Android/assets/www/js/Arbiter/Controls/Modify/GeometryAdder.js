Arbiter.GeometryAdder = function(map, modifyLayer, geometryType, featureAddedHandler){
	this.map = map;
	this.modifyLayer = modifyLayer;
	this.geometryType = geometryType;
	this.featureAddedHandler = featureAddedHandler;
	
	this.sketchStarted = false;
	this.sketchCompleted = false;
	
	this.vertexCount = 0;
	
	this.insertController = null;
	
	this.registerEvents();
	
	this.addInsertController();
};

Arbiter.GeometryAdder.prototype.registerEvents = function(){
	this.modifyLayer.events.register("sketchstarted", this, this.onSketchStarted);
	this.modifyLayer.events.register("sketchcomplete", this, this.onSketchComplete);
	this.modifyLayer.events.register("featureadded", this, this.onFeatureAdded);
};

Arbiter.GeometryAdder.prototype.onFeatureAdded = function(event){
	
	if(Arbiter.Util.existsAndNotNull(this.insertController) && this.insertController.active){
		this.removeInsertController();
		
		this.featureAddedHandler.call(this, event.feature);
	}
};

Arbiter.GeometryAdder.prototype.onSketchStarted = function(){
	console.log("onSketchStarted");
	
	this.sketchStarted = true;
};

Arbiter.GeometryAdder.prototype.onSketchComplete = function(){
	
	console.log("onSketchComplete");
	this.sketchCompleted = true;
};

Arbiter.GeometryAdder.prototype.addInsertController = function(){
		
	var type = Arbiter.Geometry.type;
	
	var options = {};
	
	var handler = null;
	
	switch(this.geometryType){
		case type.POINT:
			
			handler = OpenLayers.Handler.Point;
			
			break;
			
		case type.LINE:
			
			handler = OpenLayers.Handler.Path;
			
			break;
			
		case type.POLYGON:
			
			handler = OpenLayers.Handler.Polygon;
			
			break;
		
		case type.MULTIPOINT:
			
			handler = OpenLayers.Handler.Point;
			
			break;
			
		case type.MULTILINE:
			
			handler = OpenLayers.Handler.Path;
			
			break;
		case type.MULTIPOLYGON:
			
			handler = OpenLayers.Handler.Polygon;
			
			break;
			
		case type.GEOMETRY:
			
			handler = OpenLayers.Handler.Point;
			
			break;
			
		case type.MULTIGEOMETRY:
			
			handler = OpenLayers.Handler.Point;
			
			break;
			
		default:
			
	}
	
	this.insertController = new OpenLayers.Control.DrawFeature(this.modifyLayer, handler, options);
	
	this.map.addControl(this.insertController);
	
	this.insertController.activate();
};

Arbiter.GeometryAdder.prototype.removeInsertController = function(){
	
	if(Arbiter.Util.existsAndNotNull(this.insertController) && this.insertController.active){
		this.insertController.deactivate();
		
		this.map.removeControl(this.insertController);
	}
};

Arbiter.GeometryAdder.prototype.finish = function(){
	
	if(!this.sketchCompleted && Arbiter.Util.existsAndNotNull(this.insertController)
			&& (this.geometryType !== Arbiter.Geometry.type.POINT
			&& this.geometryType !== Arbiter.Geometry.type.MULTIPOINT)){
	
		this.insertController.finishSketch();
	}
};