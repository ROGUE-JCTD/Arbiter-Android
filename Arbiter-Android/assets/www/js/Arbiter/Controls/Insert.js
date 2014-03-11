/**
 * @class Arbiter.Controls.Insert
 * @constructor
 * @param {OpenLayers.Layer.Vector} _olLayer The layer to insert into.
 * @param {Arbiter.Geometry.type} _geometryType The type.
 * @param {Function} insertCallback To be executed after insert.
 */
Arbiter.Controls.Insert = function(_olLayer, _map, _geometryType, insertCallback){
	var context = this;
	
	var controller = null
	
	var olLayer = _olLayer;
	 
	var map = _map;
	
	var insertLayerName = "insertLayer";
	
	var insertLayer = null;
	
	var geometryType = _geometryType;
	
	var sketchStarted = false;
	
	var vertexCount = 0;
	
	var registerListeners = function(){
		
		if(insertLayer !== null && insertLayer !== undefined){
			
			insertLayer.events.register("featureadded", 
					context, onFeatureAdded);
			
			insertLayer.events.register("sketchstarted",
					context, onSketchStarted);
			
			insertLayer.events.register("sketchmodified",
					context, onSketchModified);
		}
	};
	
	var _attachToMap = function(){
		if(controller !== null){
			map.addControl(controller);
			
			controller.activate();
			
			registerListeners();
		}
	};
	
	var _detachFromMap = function(){
		if(controller !== null){
			
			controller.deactivate();
			
			map.removeControl(controller);
			
			controller = null;
		}
	};
	
	var onSketchStarted = function(feature, vertex){
		sketchStarted = true;
	};
	
	var onSketchModified = function(feature, vertex){
		var type = Arbiter.Geometry.type;
		
		vertexCount++;
		
		if(vertexCount === 3 && (geometryType === type.POLYGON || geometryType === type.MULTIPOLYGON)){
			
			Arbiter.Cordova.enableDoneEditingBtn();
		}else if(vertexCount === 2 && (geometryType === type.LINE || geometryType === type.MULTILINE)){
			
			Arbiter.Cordova.enableDoneEditingBtn();
		}else if(vertexCount === 1 && (geometryType === type.POINT || geometryType === type.MULTIPOINT)){
			
			Arbiter.Cordova.enableDoneEditingBtn();
		}
	};
	
	var onFeatureAdded = function(event){
		
		event.feature.renderIntent = 'select';
		insertLayer.redraw();
	};
	
	var initController = function(){
		
		var type = Arbiter.Geometry.type;
		
		var options = {};
		
		var handler = null;
		
		switch(geometryType){
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
		
		insertLayer = new OpenLayers.Layer.Vector(insertLayerName, {
			styleMap: olLayer.styleMap
		});
		
		map.addLayer(insertLayer);
		
		controller = new OpenLayers.Control.DrawFeature(insertLayer, handler, options);
		
		_attachToMap();
	};
	
	initController();
	
	return {
		deactivate: function(){
			_detachFromMap();
		},
		
		finishGeometry: function(){
			try{
				if(Arbiter.Util.existsAndNotNull(controller) && sketchStarted 
						&& (controller.handler.CLASS_NAME !== "OpenLayers.Handler.Point")){
					controller.finishSketch();
				}
			}catch(e){
				e.stack;
			}
		},
		
		finishInserting: function(){
			
			this.finishGeometry();
			
			if(Arbiter.Util.existsAndNotNull(insertLayer) && insertLayer.features.length > 0){
				
				var features = insertLayer.features;
				var feature = null;
				// Create collection
				if(features.length > 1){
					var types = Arbiter.Geometry.type;
					
					var components = [];
					
					for(var i = 0; i < features.length; i++){
						components.push(features[i].geometry);
					}
					
					var collection = null;
					
					switch(geometryType){
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
							
							console.log("finish inserting uh oh: " + geometryType);
					}
					
					feature = new OpenLayers.Feature.Vector(collection);
					
				}else{
					feature = features[0];
				}
				
				insertLayer.removeAllFeatures();
				olLayer.addFeatures([feature]);
				
				map.removeLayer(insertLayer);
				
				_detachFromMap();
				
				if(Arbiter.Util.funcExists(insertCallback)){
					insertCallback(feature);
				}
			}
		}
	};
};