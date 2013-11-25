Arbiter.Controls.Select = (function(){
	
	var vectorLayers = [];
	
	var selectController = null;
	
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
	
	var update = function(){
		if(vectorLayers.length > 0){
			
			// If the selectController is null, initialize it, attach it to the map and activate it
			if(selectController === null){
				selectController = new OpenLayers.Control.SelectFeature(vectorLayers, {
					clickout: false,
					toggle: true
				});
				
				_attachToMap();
			}else{
				selectController.setLayer(vectorLayers);
			}
		}else{
			_detachFromMap();
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
	
	var onAddLayer = function(){
		var map = Arbiter.Map.getMap();
		
		map.events.register("addlayer", Arbiter.Controls.Select, function(event){
			if(event && event.layer 
					&& (event.layer instanceof OpenLayers.Layer.Vector)
					&& !(event.layer instanceof OpenLayers.Layer.Vector.RootContainer)){
				
				vectorLayers.push(event.layer);
				
				update();
			}
		});
	};
	
	var onRemoveLayer = function(){
		var map = Arbiter.Map.getMap();
		
		map.events.register("removelayer", Arbiter.Controls.Select, function(event){
			
			if(event && event.layer 
					&& (event.layer instanceof OpenLayers.Layer.Vector)
					&& !(event.layer instanceof OpenLayers.Layer.Vector.RootContainer)){
				
				removeFromVectorLayers(event.layer);
				
				update();
			}
		});
	};
	
	var getSavedSelectedFeature = function(onSuccess, onFailure){
		var context = Arbiter.Controls.Select;
		
		// Get the saved selected layer
		Arbiter.PreferencesHelper.get(Arbiter.SELECTED_LAYER, context, 
				function(selectedLayer){
			
			// Get the saved selected fid
			Arbiter.PreferencesHelper.get(Arbiter.SELECTED_FID, context, 
					function(selectedFid){
				
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess(selectedLayer, selectedFid);
				}
			}, function(e){
				console.log("Error getting " + Arbiter.SELECTED_FID, e);
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure(e);
				}
			});
		}, function(e){
			console.log("Error getting " + Arbiter.SELECTED_LAYER, e);
			
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure(e);
			}
		});
	};
	
	var clearOutSaved = function(){
		
		// Get rid of the previously saved selected_fid
		Arbiter.PreferencesHelper.remove(Arbiter.SELECTED_FID, null, function(){
			
			// Get rid of the previously saved selected_layer
			Arbiter.PreferencesHelper.remove(Arbiter.SELECTED_LAYER, null, function(){
				
			}, function(e){
				console.log("ERROR: Arbiter.Controls.Select clearOutSaved inner", e);
			});
		}, function(e){
			console.log("ERROR: Arbiter.Controls.Select clearOutSaved outer", e);
		});
	};
	
	var selectSavedFeature = function(selectedLayer, selectedFid){
		
		// There was no selected feature so do nothing.
		if(selectedLayer === null || selectedLayer === undefined 
				|| selectedFid === null || selectedFid === undefined){
			return;
		}
		
		// Get the selected layer from the map.
		var layers = map.getLayersByName(selectedLayer);
		
		// Get the selected feature from the selected layer
		if(layers && layers.length === 1){
			var feature = layers[0].getFeatureByFid(selectedFid);
			
			if(feature !== null){
				selectController.select(feature);
				
				clearOutSaved();
			}else{
				throw "Couldn't find feature with fid '" + selectedFid + "'";
			}
		}else{
			if(layers && layers.length > 1){
				throw "Too many layers with name '" + selectedLayer + "'";
			}else{
				throw "No layers with name '" + selectedLayer + "'";
			}
		}
	};
	
	var onDoneLoadingLayers = function(){
		var map = Arbiter.Map.getMap();
		
		map.events.register(Arbiter.Loaders.LayersLoader.DONE_LOADING_LAYERS, 
				Arbiter.Controls.Select, function(event){
			
			var onFailure = function(){
				console.log("Error selected saved feature.");
			};
			
			getSavedSelectedFeature(selectSavedFeature, onFailure);
		});
	};
	
	return {
		getSelectController: function(){
			return selectController;
		},
		
		registerMapListeners: function(){
			onAddLayer();
			onRemoveLayer();
			onDoneLoadingLayers();
		}
	};
})();
