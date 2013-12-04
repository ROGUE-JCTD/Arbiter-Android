Arbiter.Controls.Modify = function(_olLayer, _selectedFeature){
	var modifyController = null;
	
	var selectedFeature = _selectedFeature;
	
	var olLayer = _olLayer;
	
	var initModifyController = function(){
		modifyController = new OpenLayers.Control.ModifyFeature(olLayer, {
			standalone: true,
			toggle: false,
			clickout: false
		});
		
		_attachToMap();
	};
	
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
	
	return {
		getController: function(){
			return modifyController;
		},
		
		activate: function(){
			initModifyController();
			
			modifyController.selectFeature(selectedFeature);
		},
		
		deactivate: function(){
			_detachFromMap();
		}
	};
};