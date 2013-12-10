/**
 * @class Arbiter.Controls.Insert
 * @constructor
 * @param {OpenLayers.Layer.Vector} _olLayer The layer to insert into.
 * @param {Arbiter.Util.Geometry.type} _geometryType The type.
 * @param {Function} insertCallback To be executed after insert.
 */
Arbiter.Controls.Insert = function(_olLayer, _geometryType, insertCallback){
	var context = this;
	
	var controller = null
	
	console.log("_olLayer", _olLayer);
	
	var olLayer = _olLayer;
	
	var geometryType = _geometryType;
	
	var registerListeners = function(){
		
		if(olLayer !== null && olLayer !== undefined){
			
			olLayer.events.register("featureadded", 
					context, onFeatureAdded);
		}
	};
	
	var unregisterListeners = function(){
		if(olLayer !== null && olLayer !== undefined){
			
			olLayer.events.unregister("featureadded", 
					context, onFeatureAdded);
		}
	};
	
	var _attachToMap = function(){
		if(controller !== null){
			var map = Arbiter.Map.getMap();
			
			map.addControl(controller);
			
			controller.activate();
			
			registerListeners();
		}
	};
	
	var _detachFromMap = function(){
		if(controller !== null){
			unregisterListeners();
			
			var map = Arbiter.Map.getMap();
			
			controller.deactivate();
			
			map.removeControl(controller);
			
			// TODO: this causes an exception 
			//controller.destroy();
			
			controller = null;
		}
	};
	
	var onFeatureAdded = function(event){
		if(Arbiter.Util.funcExists(insertCallback)){
			insertCallback(event.feature);
		}
	};
	
	var initController = function(){
		
		var handler = null;
		
		var type = Arbiter.Util.Geometry.type;
		
		switch(geometryType){
			case type.POINT:
				handler = OpenLayers.Handler.Point;
				
				break;
				
			case type.LINE:
				handler = OpenLayers.Handler.Path;
				
				break;
				
			case type.POLYGON:
				handler = OpenLayers.Handler.Point;
				
				break;
			
			default:
				
		}
		
		controller = new OpenLayers.Control.DrawFeature(olLayer, handler);
		
		_attachToMap();
	};
	
	initController();
	
	return {
		deactivate: function(){
			_detachFromMap();
		}
	};
};