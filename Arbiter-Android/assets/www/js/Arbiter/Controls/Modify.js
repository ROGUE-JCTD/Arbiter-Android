Arbiter.Controls.Modify = function(_olLayer, _selectedFeature){
	var modifyController = null;
	
	var selectedFeature = _selectedFeature;
	
	var olLayer = _olLayer;
	
	var _attachToMap = function(){
		if(modifyController !== null){
			var map = Arbiter.Map.getMap();
			
			map.addControl(modifyController);
			
			modifyController.activate();
		}
	};
	
	var _detachFromMap = function(){
		if(modifyController !== null){
			var map = Arbiter.Map.getMap();
			
			modifyController.deactivate();
			
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