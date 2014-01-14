Arbiter.Controls.Modify = function(_olLayer, _selectedFeature, onFeatureModified){
	var modifyController = null;
	
	var selectedFeature = _selectedFeature;
	
	var olLayer = _olLayer;
	
	var featureModified =  function(event){
		if(Arbiter.Util.funcExists(onFeatureModified)){
			onFeatureModified(event.feature);
		}
	};
	
	var registerEvents = function(){
		olLayer.events.register("featuremodified", null, featureModified);
	};
	
	var unregisterEvents = function(){
		olLayer.events.unregister("featuremodified", null, featureModified);
	};
	
	var _attachToMap = function(){
		if(modifyController !== null){
			var map = Arbiter.Map.getMap();
			
			map.addControl(modifyController);
			
			registerEvents();
			
			modifyController.activate();
		}
	};
	
	var _detachFromMap = function(){
		if(modifyController !== null){
			var map = Arbiter.Map.getMap();
			
			modifyController.deactivate();
			
			unregisterEvents();
			
			map.removeControl(modifyController);
			
			modifyController.destroy();
			
			modifyController = null;
		}
	};
	
	var initModifyController = function(){
		modifyController = new OpenLayers.Control.ModifyFeature(olLayer, {
			standalone: true,
			toggle: false,
			clickout: false
		});
		
		_attachToMap();
	};
	
	return {
		activate: function(){
			initModifyController();
			
			modifyController.selectFeature(selectedFeature);
		},
		
		deactivate: function(){
			_detachFromMap();
		}
	};
};