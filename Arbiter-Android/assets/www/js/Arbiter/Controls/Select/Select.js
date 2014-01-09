/**
 * @class Arbiter.Controls.Select
 * @constructor
 * @param {Function} onSelect To be executed on feature select
 * @param {Function} onUnselect To be execute on feature unselect
 * @param {Boolean} includeOOMWorkaround Should the OOM workaround be included?
 */
Arbiter.Controls.Select = function(onSelect, onUnselect, includeOOMWorkaround){
	var context = this;
	
	var vectorLayers = [];
	
	var selectController = null;
	
	/*var oomWorkaround = null;
	
	if(includeOOMWorkaround){
		oomWorkaround = new Arbiter.Controls
			.Select.OOM_Workaround(this);
	}*/
	
	var initSelectController = function(){
		selectController = new OpenLayers.Control.SelectFeature(vectorLayers, {
			clickout: false,
			toggle: true,
			onSelect: function(feature){
				if(Arbiter.Util.funcExists(onSelect)){
					onSelect(feature);
				}
			},
			onUnselect: function(){
				if(Arbiter.Util.funcExists(onUnselect)){
					onUnselect();
				}
			}
		});
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
					&& Arbiter.Util.layerIsEditable(event.layer)
					&& event.layer.name !== Arbiter.AOI){
				
				vectorLayers.push(event.layer);
				
				update();
			}
		});
	};
	
	var onRemoveLayer = function(){
		var map = Arbiter.Map.getMap();
		
		map.events.register("removelayer", context, function(event){
			
			if(event && event.layer 
					&& Arbiter.Util.layerIsEditable(event.layer)
					&& event.layer !== Arbiter.AOI){
				
				removeFromVectorLayers(event.layer);
				
				update();
			}
		});
	};
	
	return {
		registerMapListeners: function(){
			onAddLayer();
			onRemoveLayer();
			
			/*if(oomWorkaround !== null &&
					oomWorkaround !== undefined){
				
				oomWorkaround.registerMapListeners();
			}*/
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
		}
	};
};
