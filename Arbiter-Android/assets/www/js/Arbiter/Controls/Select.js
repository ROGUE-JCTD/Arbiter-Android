/**
 * @class Arbiter.Controls.Select
 * @constructor
 * @param {Function} onSelect To be executed on feature select
 * @param {Function} onUnselect To be execute on feature unselect
 */
Arbiter.Controls.Select = function(onSelect, onUnselect){
	var context = this;
	
	var vectorLayers = [];
	
	var selectController = null;
	
	var selectedFeature = null;
	
	var initSelectController = function(){
		selectController = new OpenLayers.Control.SelectFeature(vectorLayers, {
			clickout: false,
			toggle: true,
			onSelect: function(feature){
				
				if(Arbiter.Util.existsAndNotNull(selectedFeature) && (selectedFeature === feature)){
					
					if(Arbiter.Util.funcExists(onSelect)){
						onSelect(feature);
					}
					
					selectedFeature = null;
				}else{
					
					selectedFeature = feature;
				}
			},
			onUnselect: function(feature){
				
				if(Arbiter.Util.existsAndNotNull(selectedFeature) && (selectedFeature === feature)){
					
					selectController.select(feature);
				}else{
					
					if(Arbiter.Util.funcExists(onUnselect)){
						onUnselect(feature);
					}
					
					selectedFeature = null;
				}
			}
		});
		
		selectController.handlers.feature.stopDown = false;
	};
	
	var _attachToMap = function(){
		if(selectController !== null){
			var map = Arbiter.Map.getMap();
			
			map.addControl(selectController);
			
			selectController.activate();
		}
	};
	
	var _detachFromMap = function(){
		if(selectController !== null){
			var map = Arbiter.Map.getMap();
			
			map.removeControl(selectController);
			
			selectController = null;
		}
	};
	
	var removeFromVectorLayers = function(layer){
		for(var i = 0; i < vectorLayers.length; i++){
			if(vectorLayers[i] == layer){
				vectorLayers.splice(i, 1);
				break;
			}
		}
	};
	
	var update = function(){
		if(vectorLayers.length > 0){
			
			// If the selectController is null, initialize it, attach it to the map and activate it
			if(selectController === null){
				initSelectController();
				
				_attachToMap();
			}else{
				selectController.setLayer(vectorLayers);
			}
		}else{
			_detachFromMap();
		}
	};
	
	var onAddLayer = function(){
		var map = Arbiter.Map.getMap();
		
		map.events.register("addlayer", context, function(event){
			if(event && event.layer 
					&& Arbiter.Util.isArbiterWFSLayer(event.layer)){
				
				vectorLayers.push(event.layer);
				
				update();
			}
		});
	};
	
	var onRemoveLayer = function(){
		var map = Arbiter.Map.getMap();
		
		map.events.register("removelayer", context, function(event){
			
			if(event && event.layer 
					&& Arbiter.Util.isArbiterWFSLayer(event.layer)){
				
				removeFromVectorLayers(event.layer);
				
				update();
			}
		});
	};
	
	return {
		registerMapListeners: function(){
			onAddLayer();
			onRemoveLayer();
		},
		
		unselect: function(){
			selectController.unselectAll();
		},
		
		activate: function(){
			selectController.activate();
		},
		
		deactivate: function(){
			selectController.unselectAll();
			
			selectController.deactivate();
		},
		
		select: function(feature){
			selectController.select(feature);
		},
		
		isActive: function(){
			
			var active = false;
			
			if(Arbiter.Util.existsAndNotNull(selectController)){
				active = selectController.active;
			}
			
			return active;
		}
	};
};
