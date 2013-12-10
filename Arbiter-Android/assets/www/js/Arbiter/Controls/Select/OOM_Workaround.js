/**
 * @class Arbiter.Controls.Select.OOM_Workaround
 * @constructor
 * @param {Arbiter.Controls.Select} selectControl A select control.
 */
Arbiter.Controls.Select.OOM_Workaround = function(_selectControl){
	var context = this;
	
	var selectControl = _selectControl;
	
	/**
	 * Get the feature that was selected prior to reset
	 */
	var getSavedSelectedFeature = function(onSuccess, onFailure){
		
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
	
	/**
	 * Clear out the saved feature
	 */
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
	
	/**
	 * Select the feature that wassaved prior to reset
	 */
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
		registerMapListeners: function(){
			onDoneLoadingLayers();
		}
	};
};