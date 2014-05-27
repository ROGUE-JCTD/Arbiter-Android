/**
 * @class Arbiter.Controls.Insert
 * @constructor
 * @param {OpenLayers.Layer.Vector} _olLayer The layer to insert into.
 * @param {Arbiter.Geometry.type} _geometryType The type.
 * @param {Function} insertCallback To be executed after insert.
 */
(function(){
	
	Arbiter.Controls.Insert = function(_olLayer, _map, _geometryType, insertCallback){
		
		this.controller = null
		
		this.olLayer = _olLayer;
		 
		this.map = _map;
		
		this.insertLayerName = "insertLayer";
		
		this.insertLayer = null;
		
		this.geometryType = _geometryType;
		
		this.sketchStarted = false;
		
		this.vertexCount = 0;
		
		this.insertCallback = insertCallback;
		
		this.alreadyFinished = false;
		
		this.initController();
	};
	
	var prototype = Arbiter.Controls.Insert.prototype;
	
	prototype.attachToMap = function(){
		if(this.controller !== null){
			this.map.addControl(this.controller);
			
			this.controller.activate();
			
			this.registerListeners();
		}
	};
	
	prototype.registerListeners = function(){
		
		if(this.insertLayer !== null && this.insertLayer !== undefined){
			
			this.insertLayer.events.register("featureadded", 
					this, this.onFeatureAdded);
			
			this.insertLayer.events.register("sketchstarted",
					this, this.onSketchStarted);
		}
	};
	
	prototype.unregisterListeners = function(){
		
		if(Arbiter.Util.existsAndNotNull(this.insertLayer)){
			
			this.insertLayer.events.unregister("featureadded", 
					this, this.onFeatureAdded);
			
			this.insertLayer.events.unregister("sketchstarted",
					this, this.onSketchStarted);
		}
	};
	
	prototype.deactivate = function(){
		if(this.controller !== null){
			
			this.controller.deactivate();
			
			this.map.removeControl(this.controller);
			
			//unregisterListeners();
			
			this.controller = null;
		}
	};
	
	prototype.onFeatureAdded = function(event){
		console.log("Insert: onFeatureAdded", event);
		event.feature.renderIntent = 'select';
		
		this.insertLayer.redraw();
		
		if(!this.alreadyFinished){
			this.alreadyFinished = true;
			this.finishInserting();
		}
	};
	
	prototype.onSketchStarted = function(feature, vertex){
		this.sketchStarted = true;
	};
	
	prototype.initController = function(){
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
				
				options = {
					multi: true
				};
				
				break;
				
			case type.MULTILINE:
				
				handler = OpenLayers.Handler.Path;
				
				options = {
					multi: true
				};
				
				break;
			case type.MULTIPOLYGON:
				
				handler = OpenLayers.Handler.Polygon;
				
				options = {
					multi: true
				};
				
				break;
				
			case type.GEOMETRY:
				
				handler = OpenLayers.Handler.Point;
				
				break;
				
			case type.MULTIGEOMETRY:
				
				handler = OpenLayers.Handler.Point;
				
				break;
				
			default:
				
		}
		
		this.insertLayer = new OpenLayers.Layer.Vector(this.insertLayerName, {
			styleMap: this.olLayer.styleMap
		});
		
		this.map.addLayer(this.insertLayer);
		
		this.controller = new OpenLayers.Control.DrawFeature(this.insertLayer, handler, options);
		
		this.attachToMap();
	};
	
	prototype.finishGeometry = function(){
		try{
			if(Arbiter.Util.existsAndNotNull(this.controller) && this.sketchStarted 
					&& (this.controller.handler.CLASS_NAME !== "OpenLayers.Handler.Point")){
				this.controller.finishSketch();
			}
		}catch(e){
			e.stack;
		}
	};
	
	prototype.finishInserting = function(){
		
		if(!this.alreadyFinished){
			this.alreadyFinished = true;
			
			this.finishGeometry();
		}
		
		if(Arbiter.Util.existsAndNotNull(this.insertLayer) && this.insertLayer.features.length > 0){
			
			var features = this.insertLayer.features;
			var feature = null;
			// Create collection
			if(features.length > 1){
				var types = Arbiter.Geometry.type;
				
				var components = [];
				
				for(var i = 0; i < features.length; i++){
					components.push(features[i].geometry);
				}
				
				var collection = null;
				
				switch(this.geometryType){
					case types.MULTIPOINT:
					
						collection = new OpenLayers.Geometry.MultiPoint(components);
						
						break;
					
					case types.MULTILINE:
					
						collection = new OpenLayers.Geometry.MultiLineString(components);
						
						break;
					
					case types.MULTIPOLYGON:
					
						collection = new OpenLayers.Geometry.MultiPolygon(components);
						
						break;
					
					case types.MULTIGEOMETRY:
					
						collection = new OpenLayers.Geometry.Collection(components);
						
						break;
					
					default:
						
						console.log("finish inserting uh oh: " + this.geometryType);
				}
				
				feature = new OpenLayers.Feature.Vector(collection);
				
			}else{
				feature = features[0];
			}
			
			this.insertLayer.removeAllFeatures();
			this.olLayer.addFeatures([feature]);
			
			this.map.removeLayer(this.insertLayer);
			
			this.deactivate();
			
			if(Arbiter.Util.funcExists(this.insertCallback)){
				this.insertCallback(feature);
			}
		}else{
			
			this.map.removeLayer(this.insertLayer);
			
			this.deactivate();
			
			if(Arbiter.Util.funcExists(this.insertCallback)){
				this.insertCallback();
			}
		}
	};
})();