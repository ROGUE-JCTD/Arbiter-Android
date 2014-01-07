Arbiter.Controls.ControlPanel = (function(){
	var selectedFeature = null;
	
	var selectControl = null;
	
	var modifyControl = null;
	
	var insertControl = null;
	
	var deleteControl = new Arbiter.Controls.Delete();
	
	var insertedFeature = null;
	
	var originalGeometry = null;
	
	var saveOriginalGeometry = function(){
		if(selectedFeature === null 
				|| selectedFeature === undefined){
			return;
		}
		
		originalGeometry = selectedFeature.geometry.clone();
	};
	
	var _endInsertMode = function(){
		
		insertControl.deactivate();
		
		insertControl = null;
		
		selectControl.activate();
	};
	
	var _startInsertMode = function(layerId){
		
		selectControl.deactivate();
		
		var olLayer = Arbiter.Layers.getLayerById(
				layerId, Arbiter.Layers.type.WFS);
		
		var geometryType = Arbiter.Util.Geometry.getGeometryType(layerId);
			
		var context = Arbiter.Controls.ControlPanel;
		
		var schema = Arbiter.getLayerSchemas()[layerId];
		
		if(schema === null || schema === undefined){
			throw "Arbiter.Controls.ControlPanel _startInsertMode - "
				"could not get schema for layer id '" + layerId + "'";
		}
		
		insertControl = new Arbiter.Controls.Insert(olLayer,
				geometryType, function(feature){
			
			_endInsertMode();
			
			insertedFeature = feature;
			
			selectControl.select(feature);
		});
	};
	
	var startModifyMode = function(){
		if(selectedFeature === null 
				|| selectedFeature === undefined){
			return;
		}
		
		modifyControl = new Arbiter.Controls.Modify(
				selectedFeature.layer, selectedFeature);
		
		selectControl.deactivate();
		
		modifyControl.activate();
	};
	
	var endModifyMode = function(){
		if(selectedFeature === null 
				|| selectedFeature === undefined){
			return;
		}
		
		// Deactivate the modifyControl
		modifyControl.deactivate();
		
		modifyControl = null;
		
		// Reactivate the selectControl
		selectControl.activate();
		
		// Reselect the selectedFeature
		selectControl.select(selectedFeature);
	};
	
	var restoreOriginalGeometry = function(){
		if(originalGeometry === null || originalGeometry === undefined){
			throw "Arbiter.Controls.Select - couldn't restore original"
			+ " geometry because originalGeometry is " + originalGeometry;
		} 
		
		if(selectedFeature === null || selectedFeature === undefined){
			
			throw "Arbiter.Controls.Select - couldn't restore original"
				+ " geometry because selectedFeature is " + selectedFeature;
		}
		
		// Get the center of the geometry
		var centroid = originalGeometry.getCentroid();
		
		selectedFeature.move(new OpenLayers.LonLat(centroid.x, centroid.y));
	};
	
	var removeFeature = function(feature){
		var layer = feature.layer;
		
		layer.removeFeatures([feature]);
	};
	
	selectControl = new Arbiter.Controls.Select(function(feature){
		
		// If the selectedFeature hasn't been
		// cleared out yet, then it means this was
		// the reselect of the feature in
		// endModifyMode()
		if(selectedFeature === null 
				|| selectedFeature === undefined){
			
			selectedFeature = feature;
			
			saveOriginalGeometry();
			
			if(insertedFeature === null || insertedFeature === undefined){
				
				Arbiter.Cordova.displayFeatureDialog(
						feature.layer.protocol.featureType,
						feature.metadata[Arbiter.FeatureTableHelper.ID],
						Arbiter.Util.getLayerId(feature.layer)
				);
			}else{
				var layerId = Arbiter.Util.getLayerId(insertedFeature.layer);
				
				Arbiter.Cordova.doneInsertingFeature(layerId, insertedFeature);
			}
		}
	}, function(){
		
		// If the modifyControl is null,
		// make sure selectedFeature is
		// cleared out.
		if(modifyControl === null){
			selectedFeature = null;
			originalGeometry = null;
		}
	});
	
	var setFeatureId = function(olFeature, featureId){
		olFeature.metadata = {};
		
		olFeature.metadata[Arbiter.FeatureTableHelper.ID] = featureId; 
	};
	
	return {
		registerMapListeners: function(){
			selectControl.registerMapListeners();
		},
		
		enterModifyMode: function(){
			startModifyMode();
		},
		
		exitModifyMode: function(){
			endModifyMode();
		},
		
		unselect: function(){
			selectControl.unselect();
		},
		
		/**
		 * Restore the geometry to its original location,
		 * and exit modify mode.
		 */
		cancelEdit: function(){
			restoreOriginalGeometry();
			
			this.exitModifyMode();
		},
		
		/**
		 * Unselect the feature
		 */
		cancelSelection: function(){
			if(insertedFeature === null || insertedFeature === undefined){
				restoreOriginalGeometry();
			}else{
				removeFeature(insertedFeature);
				
				insertedFeature = null;
			}
			
			this.unselect();
		},
		
		getSelectedFeature: function(){
			return selectedFeature;
		},
		
		startInsertMode: function(layerId){
			_startInsertMode(layerId);
		},
		
		getInsertControl: function(){
			return insertControl;
		}
	};
})();