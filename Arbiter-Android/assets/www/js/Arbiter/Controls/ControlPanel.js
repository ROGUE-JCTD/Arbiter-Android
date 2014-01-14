Arbiter.Controls.ControlPanel = (function(){
	var selectedFeature = null;
	
	var selectControl = null;
	
	var modifyControl = null;
	
	var insertControl = null;
	
	var controlPanelHelper = new Arbiter.ControlPanelHelper();
	
	var wktFormatter = new OpenLayers.Format.WKT();
	
	var mode = Arbiter.ControlPanelHelper.prototype.CONTROLS.SELECT;
	
	var cancel = false;
	
	var _endInsertMode = function(){
		
		if(insertControl !== null && insertControl !== undefined){
			insertControl.deactivate();
			
			insertControl = null;
			
			selectControl.activate();
		}
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
		
		controlPanelHelper.set(0, layerId, controlPanelHelper.CONTROLS.INSERT, 0, function(){
			
			insertControl = new Arbiter.Controls.Insert(olLayer,
					geometryType, function(feature){
				
				_endInsertMode();
				
				mode = Arbiter.ControlPanelHelper.prototype.CONTROLS.INSERT;
				
				selectControl.select(feature);
			});
		}, function(e){
			console.log("start insert mode error");
		});
	};
	
	var getNativeGeometry = function(feature, layerId){
		var schemas = Arbiter.getLayerSchemas();
		
		var schema = schemas[layerId];
		
		var srid = Arbiter.Map.getMap().projection.projCode;
		
		var wktGeometry = wktFormatter.write(
				Arbiter.Util.getFeatureInNativeProjection(srid,
						schema.getSRID(), feature));
		
		return wktGeometry;
	};
	
	var startModifyMode = function(feature){
		
		var featureId = null;
		
		if(feature.metadata !== null && feature.metadata !== undefined){
			featureId = feature.metadata[Arbiter.FeatureTableHelper.ID];
		}
		
		var layerId = Arbiter.Util.getLayerId(feature.layer);
		
		var wktGeometry = getNativeGeometry(feature, layerId);
		
		modifyControl = new Arbiter.Controls.Modify(
				feature.layer, feature, function(feature){
		
			var wktGeometry = getNativeGeometry(feature, layerId);
			
			controlPanelHelper.set(featureId, layerId, 
					controlPanelHelper.CONTROLS.MODIFY, 
					wktGeometry, function(){
				
				console.log("successfully updated geometry");
			}, function(e){
				console.log("error updating modified geometry", e);
			});
		});
		
		controlPanelHelper.set(featureId, layerId,
				controlPanelHelper.CONTROLS.MODIFY, wktGeometry, function(){
			
			selectControl.deactivate();
			
			modifyControl.activate();
			
			mode = Arbiter.ControlPanelHelper.prototype.CONTROLS.MODIFY;
			
		}, function(e){
			console.log("start modify mode error", e);
		});
	};
	
	var endModifyMode = function(_cancel){
		
		cancel = _cancel;
		
		if(selectedFeature === null 
				|| selectedFeature === undefined){
			console.log("Arbiter.Controls.ControlPanel.exitModifyMode()"
				+ " selectedFeature should not be ", selectedFeature);
			
			return;
		}
		
		if(modifyControl === null || modifyControl === undefined){
			console.log("Arbiter.Controls.ControlPanel.exitModifyMode()"
					+ " modifyControl is " + modifyControl);
			
			return;
		}
		
		controlPanelHelper.clear(function(){
			
			if(modifyControl){
				// Deactivate the modifyControl
				modifyControl.deactivate();
				
				modifyControl = null;
			}
			
			// Reactivate the selectControl
			selectControl.activate();
			
			// Reselect the selectedFeature
			selectControl.select(selectedFeature);
		}, function(e){
			console.log("endModifyMode error",e);
		});
	};
	
	var _restoreGeometry = function(wktGeometry){
		
		if(wktGeometry === null || wktGeometry === undefined){
			throw "Arbiter.Controls.ControlPanel - couldn't restore original"
			+ " geometry because wktGeometry is " + wktGeometry;
		} 
		
		if(selectedFeature === null || selectedFeature === undefined){
			throw "Arbiter.Controls.Select - couldn't restore original"
			+ " geometry because selectedFeature is " + selectedFeature;
		}
		
		var geomFeature = wktFormatter.read(wktGeometry);
		
		// Get the center of the geometry
		var centroid = geomFeature.geometry.getCentroid();
		
		var layerId = Arbiter.Util.getLayerId(selectedFeature.layer);
		
		var schema = Arbiter.getLayerSchemas()[layerId];
		var srid = Arbiter.Map.getMap().projection.projCode;
		
		centroid.transform(new OpenLayers.Projection(schema.getSRID()),
				new OpenLayers.Projection(srid));
		
		selectedFeature.move(new OpenLayers.LonLat(centroid.x, centroid.y));
	};
	
	var removeFeature = function(feature){
		var layer = feature.layer;
		
		layer.removeFeatures([feature]);
	};
	
	selectControl = new Arbiter.Controls.Select(function(feature){
		console.log("feature selected and selectedFeature = " + selectedFeature);
		// If the selectedFeature hasn't been
		// cleared out yet, then it means this was
		// the reselect of the feature in
		// endModifyMode()
		if(selectedFeature === null 
				|| selectedFeature === undefined){
			
			selectedFeature = feature;
			
			var _mode = mode;
			var _cancel = cancel;
			
			// Make sure the mode related variables are reset
			mode = Arbiter.ControlPanelHelper.prototype.CONTROLS.SELECT;
			cancel = false;
			
			var featureId = null;
			
			if(feature.metadata !== null && feature.metadata !== undefined){
				featureId = feature.metadata[Arbiter.FeatureTableHelper.ID];
			}
			
			var layerId = Arbiter.Util.getLayerId(feature.layer);
			
			controlPanelHelper.set(featureId, layerId, controlPanelHelper.CONTROLS.SELECT, 0, function(){
				Arbiter.Cordova.displayFeatureDialog(
						feature.layer.protocol.featureType,
						featureId,
						layerId,
						feature,
						_mode,
						_cancel
						
				);
			}, function(e){
				console.log("Error saving select mode", e);
			});
		}
	}, function(feature){
		
		// If the modifyControl is null,
		// make sure selectedFeature is
		// cleared out.
		if(modifyControl === null){
			selectedFeature = null;
			
			controlPanelHelper.clear();
		}
	});
	
	return {
		registerMapListeners: function(){
			selectControl.registerMapListeners();
		},
		
		enterModifyMode: function(feature){
			try{
				if(feature === null 
						|| feature === undefined){
					
					feature = selectedFeature;
				}
				
				if(feature === null || feature === undefined){
					throw "ControlPanel.js feature should not be " + feature;
				}
				
				startModifyMode(feature);
			}catch(e){
				console.log("enterModifyMode error", e);
			}
		},
		
		exitModifyMode: function(_cancel){
			
			endModifyMode(_cancel);
		},
		
		unselect: function(){
			console.log("ControlPanel.unselect()");
			selectControl.unselect();
			
			if(modifyControl === null){
				selectedFeature = null;
				
				controlPanelHelper.clear();
			}
		},
		
		/**
		 * Restore the geometry to its original location,
		 * and exit modify mode.
		 */
		cancelEdit: function(wktGeometry){
			
			_restoreGeometry(wktGeometry);
			
			this.exitModifyMode(true);
		},
		
		getSelectedFeature: function(){
			return selectedFeature;
		},
		
		startInsertMode: function(layerId){
			_startInsertMode(layerId);
		},
		
		getInsertControl: function(){
			return insertControl;
		},
		
		restoreGeometry: function(wktGeometry){
			_restoreGeometry(wktGeometry);
		},
		
		/**
		 * @param { OpenLayers.Feature.Vector } feature The feature to select.
		 */
		select: function(feature){
			selectControl.select(feature);
		},
		
		setSelectedFeature: function(feature){
			selectedFeature = feature;
		},
		
		setMode: function(_mode){
			mode = _mode;
		},
		
		getMode: function(){
			return mode;
		},
		
		getCancel: function(){
			return cancel;
		}
	};
})();