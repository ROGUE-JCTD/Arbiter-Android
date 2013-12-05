/**
 * @class Arbiter.Controls.Insert
 * @constructor
 * @param {OpenLayers.Layer.Vector} _olLayer The layer to insert into.
 * @param {Arbiter.Util.Geometry.type} _geometryType The type.
 * @param {Function} insertCallback To be executed after insert.
 */
Arbiter.Controls.Insert = function(_olLayer, _geometryType,
		callbackContext, insertCallback){
	
	var controller = null
	
	var olLayer = _olLayer;
	
	var geometryType = _geometryType;
	
	var _attachToMap = function(){
		if(controller !== null){
			var map = Arbiter.Map.getMap();
			
			map.addControl(controller);
			
			controller.activate();
		}
	};
	
	var _detachFromMap = function(){
		if(controller !== null){
			var map = Arbiter.Map.getMap();
			
			controller.deactivate();
			
			map.removeControl(controller);
			
			controller.destroy();
			
			controller = null;
		}
	};
	
	var registerListeners = function(){
		if(olLayer !== null && olLayer !== undefined){
			olLayer.events.register("featureadded", 
					callbackContext, insertCallback);
		}
	};
	
	var initController = function(){
		
		var handler = null;
		
		var type = Arbiter.Util.Geometry.type;
		
		switch(geometryType){
			case type.POINT:
				handler = OpenLayers.Handler.Point();
				
				break;
				
			case type.LINE:
				handler = OpenLayers.Handler.Path();
				
				break;
				
			case type.POLYGON:
				handler = OpenLayers.Handler.Point;
				
				break;
			
			default:
				
		}
		
		controller = new OpenLayers.Control.DrawFeature(olLayer, handler);
		
		_attachToMap();
	};
	
	return {
		getController: function(){
			return controller;
		},
		
		activate: function(){
			initController();
		},
		
		deactivate: function(){
			_detachFromMap();
		}
	};
};