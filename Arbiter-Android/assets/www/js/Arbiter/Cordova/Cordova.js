Arbiter.Cordova = (function() {
	
	var state = 0;
	
	var isNeutral = function(){
		return state === Arbiter.Cordova.STATES.NEUTRAL;
	};
	
	var waitForNeutralQueue = [];
	
	var waitingForReset = false;
	
	var waitForNeutral = function(func){
		if(!Arbiter.Util.funcExists(func)){
			return;
		}
		
		if(isNeutral()){
			func();
		}else{
			waitForNeutralQueue.push(func);
		}
	};
	
	var executeWaitingFuncs = function(){
		for(var func = waitForNeutralQueue.shift(); Arbiter.Util.funcExists(func);
			func = waitForNeutralQueue.shift()){
		
			func();
		}
	};
	
	return {

		STATES : {
				NEUTRAL: 0,
				CREATING_PROJECT: 1,
				UPDATING: 2,
				OUTSIDE_AOI_WARNING: 3,
				TAKING_PICTURE: 4
		},
		
		osmLinkClicked: function(){
			
			cordova.exec(null, null, "ArbiterCordova", "osmLinkClicked", []);
		},
		
		isAddingGeometryPart: function(isAddingPart){
			
			cordova.exec(null, null, "ArbiterCordova", "isAddingGeometryPart", [isAddingPart]);
		},
		
		gotPicture: function(){
			
			cordova.exec(null, null, "ArbiterCordova", "gotPicture", []);
		},
		
		syncOperationTimedOut: function(continueSync, cancelSync){
			
			cordova.exec(function(){
				
				if(Arbiter.Util.existsAndNotNull(continueSync)){
					continueSync();
				}
			}, function(){
				
				if(Arbiter.Util.existsAndNotNull(cancelSync)){
					cancelSync();
				}
			}, "ArbiterCordova", "syncOperationTimedOut", []);
		},
		
		featureNotInAOI: function(featureId, insertFeature, cancelInsertFeature){
			
			Arbiter.Cordova.setState(Arbiter.Cordova.STATES.OUTSIDE_AOI_WARNING);
			
			cordova.exec(function(){
				
				if(Arbiter.Util.existsAndNotNull(insertFeature)){
					
					insertFeature();
				}
				
				Arbiter.Cordova.setState(Arbiter.Cordova.STATES.NEUTRAL);
				
			}, function(){
				
				if(Arbiter.Util.existsAndNotNull(cancelInsertFeature)){
					cancelInsertFeature();
				}
				
				Arbiter.Cordova.setState(Arbiter.Cordova.STATES.NEUTRAL);
			}, "ArbiterCordova", "featureNotInAOI", [featureId]);
		},
		
		layersAlreadyInProject: function(layersAlreadyInProject){
			
			console.log("layersAlreadyInProject: " + JSON.stringify(layersAlreadyInProject));
			
			cordova.exec(null, null, "ArbiterCordova", "layersAlreadyInProject", [layersAlreadyInProject]);
		},
		
		appFinishedLoading: function(){
			
			cordova.exec(null, null, "ArbiterCordova", "appFinishedLoading", []);
		},
		
		gotNotifications: function(){
			
			cordova.exec(null, null, "ArbiterCordova", "gotNotifications", []);
		},
		
		finishedGettingLocation: function(){
			
			cordova.exec(null, null, "ArbiterCordova", "finishedGettingLocation", []);
		},
		
		/**
		 * Save the current maps extent
		 */
		resetWebApp : function(tx) {
			var bbox = Arbiter.Map.getCurrentExtent().toBBOX();
			
            var zoom = Arbiter.Map.getZoom();
			var reset = function(){
				console.log("resetWebApp: " + JSON.stringify(bbox));
				
				if(Arbiter.Util.existsAndNotNull(Arbiter.findme)){
					Arbiter.findme.clearIntervals();
					
					Arbiter.Cordova.finishedGettingLocation();
				}
				
				cordova.exec(null, null, "ArbiterCordova",
						"resetWebApp", [bbox, zoom]);
			};
			
			if(waitingForReset !== true){
				// Don't reset while a sync is occurring.
				waitForNeutral(function(){
					Arbiter.SQLiteTransactionManager.executeAfterDone(reset);
				});
				
				waitingForReset = true;
			}
		},
		
		getTileCount: function(){
			var bbox = Arbiter.Map.getCurrentExtent();
			
			var count = 0;
			
			cordova.exec(null, null, "ArbiterCordova",
					"confirmTileCount", [count]);
		},
		
		/**
		 * Get the area of interest in the aoi map and call the native method to
		 * save it to be set when onResume gets called on the MapActivity
		 */
		setProjectsAOI : function(layers) {
			var bbox = Arbiter.Map.getCurrentExtent();
			
			var count = 0;
			
			cordova.exec(null, null, "ArbiterCordova",
					"setProjectsAOI", [bbox.toBBOX(), count]);
		},
		
		setNewProjectsAOI: function(){
			var bbox = Arbiter.Map.getCurrentExtent().toBBOX(); 
			console.log("setNewProjectsAOI: bbox = " + bbox);
			cordova.exec(null, null, "ArbiterCordova",
					"setNewProjectsAOI", [bbox]);
		},
		
		goToProjects: function(){
			var bbox = Arbiter.Map.getCurrentExtent().toBBOX();
			
            var zoom = Arbiter.Map.getZoom();
			cordova.exec(null, null, "ArbiterCordova",
					"goToProjects", [bbox, zoom]);
		},
		
		createNewProject: function(){
			var bbox = Arbiter.Map.getCurrentExtent().toBBOX();
			
            var zoom = Arbiter.Map.getZoom();
			cordova.exec(null, null, "ArbiterCordova",
					"createNewProject", [bbox, zoom]);
		},
		
		errorCreatingProject: function(e){
			
			console.log("errorCreatingProject", e);
			cordova.exec(function(){
				Arbiter.Cordova.setState(Arbiter.Cordova.STATES.NEUTRAL);
			}, null, "ArbiterCordova",
					"errorCreatingProject", [e]);
		},
		
		errorLoadingFeatures: function(troublesomeFeatureTypes){
			var str = "";
			
			for(var i = 0; i < troublesomeFeatureTypes.length; i++){
				if(i > 0){
					str += troublesomeFeatureTypes[i];
				}else{
					str += ", " + troublesomeFeatureTypes[i];
				}
			}
			
			cordova.exec(null, null, "ArbiterCordova", "errorLoadingFeatures", [str]);
		},
		
		doneAddingLayers: function(){
			cordova.exec(null, null, "ArbiterCordova",
					"doneAddingLayers", []);
		},
		
		errorAddingLayers: function(e){
			cordova.exec(null, null, "ArbiterCordova", 
					"errorAddingLayers", [e]);
		},
		
		// validate in here. 
		getUpdatedGeometry: function(){
			
			console.log("getUpdatedGeometry");
			
			Arbiter.Controls.ControlPanel.finishInserting(function(){
			
				console.log("finished inserting");
				
				// Also finishes modifying the geometry
				Arbiter.Controls.ControlPanel.exitModifyMode(function(){
					
					try{
						var selectedFeature = Arbiter.Controls
							.ControlPanel.getSelectedFeature();
					
						console.log("selectedFeature", selectedFeature);
						
						if(!Arbiter.Util.existsAndNotNull(selectedFeature)){
							
							cordova.exec(null, null, "ArbiterCordova", "invalidGeometriesEntered", [null, null]);
						}else{
							console.log("selectedFeature isn't null");
							var featureId = null;
							
							if(selectedFeature.metadata !== null 
									&& selectedFeature.metadata !== undefined){
								
								featureId = selectedFeature.metadata[
								    Arbiter.FeatureTableHelper.ID];
							}
							
							var layerId = Arbiter.Util.getLayerId(selectedFeature.layer);
							
							var schema = Arbiter.getLayerSchemas()[layerId];
							
							// Validate the feature and remove any invalid parts.
							var featureValidation = new Arbiter.Validation.Feature(selectedFeature, true);
							
							var invalidGeometries = featureValidation.validate();
							
							if(Arbiter.Util.existsAndNotNull(selectedFeature.metadata) 
									&& selectedFeature.metadata[Arbiter.Validation.Feature.REMOVED_DURING_VALIDATION]){
								
								Arbiter.Controls.ControlPanel.setSelectedFeature(null);
								
								cordova.exec(null, null, "ArbiterCordova", "invalidGeometriesEntered", [schema.getFeatureType(), featureId]);
							}else{
								
								var insideAOI = featureValidation.checkFeatureAddedInsideAOI();
								
								var exec = function(){
									
									var wktGeometry = null;
									
									if(Arbiter.Util.existsAndNotNull(selectedFeature.geometry)){
										if(!Arbiter.Util.existsAndNotNull(featureId)){
											wktGeometry = Arbiter.Geometry.getNativeWKT(selectedFeature, layerId);
										}else{
											wktGeometry = Arbiter.Geometry.checkForGeometryCollection(layerId, featureId, schema.getSRID());
										}
									}
									
									cordova.exec(null, null, "ArbiterCordova", "showUpdatedGeometry", 
											[schema.getFeatureType(), featureId, layerId, wktGeometry]);
								};
								
								if(insideAOI){
									exec();
								}else{
									
									Arbiter.Cordova.featureNotInAOI(featureId, exec, function(){
										
										// Using this to cancel the edit right now...
										Arbiter.Cordova.resetWebApp();
									});
								}
								
							}
						}
					}catch(e){
						console.log(e.stack);
					}
				});
			});
		},
		
		featureUnselected: function(){
			
			cordova.exec(null, null, "ArbiterCordova", "featureUnselected", []);
		},
		
		featureSelected : function(featureType, featureId, layerId,
				feature, mode, cancel){
			
			var featureValidator = new Arbiter.Validation.Feature(feature, false);
			
			featureValidator.validate();
			
			console.log("featureSelected featureValidator", featureValidator);
			
			if(featureValidator.hasValidGeometries === true){
				
				console.log("featureSelected: featureType = " + featureType 
						+ ", featureId = " + featureId + ", layerId = " 
						+ layerId + ", mode = " + mode + ", cancel = " 
						+ cancel + ", feature = ", feature);
				
				var schemas = Arbiter.getLayerSchemas();
				
				var schema = schemas[layerId];
				
				var wktGeometry = null;
				
				if(cancel === false){
					
					if(!Arbiter.Util.existsAndNotNull(featureId)){
						wktGeometry = Arbiter.Geometry.getNativeWKT(feature, layerId);
					}else{
						wktGeometry = Arbiter.Geometry.checkForGeometryCollection(layerId, featureId, schema.getSRID());
					}
				}
				
				// Put check in place because its causing issues when modifying new features.
				// Don't want it to stop editing...
				if(Arbiter.Util.existsAndNotNull(featureId)){
					Arbiter.Controls.ControlPanel.exitModifyMode();
				}
				
				console.log("displayFeatureDialog selectedFeature: ", Arbiter.Controls.ControlPanel.getSelectedFeature());
				
				cordova.exec(null, null, "ArbiterCordova", "featureSelected",
						[featureType, featureId, layerId, wktGeometry, mode]);
			}
		},
		
		updateTileSyncingStatus: function(percent){
			cordova.exec(null, null, "ArbiterCordova",
					"updateTileSyncingStatus", [percent]);
		},
		
		updateMediaUploadingStatus: function(featureType,
				finishedMediaCount, totalMediaCount,
				finishedLayerCount, totalLayerCount){
			
			console.log("updateMediaUploadingStatus featureType = " + featureType 
					+ ", finishedMediaCount = " + finishedMediaCount 
					+ ", totalMediaCount = " + totalMediaCount
					+ ", finishedLayerCount = " + finishedLayerCount 
					+ ", totalLayerCount = " + totalLayerCount);
			
			cordova.exec(null, null, "ArbiterCordova", "updateMediaUploadingStatus", 
					[featureType, finishedMediaCount, totalMediaCount,
					 finishedLayerCount, totalLayerCount]);
		},
		
		updateMediaDownloadingStatus: function(featureType,
				finishedMediaCount, totalMediaCount,
				finishedLayerCount, totalLayerCount){
			
			console.log("updateMediaDownloadingStatus featureType = " + featureType 
					+ ", finishedMediaCount = " + finishedMediaCount 
					+ ", totalMediaCount = " + totalMediaCount
					+ ", finishedLayerCount = " + finishedLayerCount 
					+ ", totalLayerCount = " + totalLayerCount);
			
			cordova.exec(null, null, "ArbiterCordova","updateMediaDownloadingStatus", 
					[featureType, finishedMediaCount, totalMediaCount,
					 finishedLayerCount, totalLayerCount]);
		},
		
		createProjectTileSyncingStatus: function(percent){
			cordova.exec(null, null, "ArbiterCordova",
					"createProjectTileSyncingStatus", [percent]);
		},
		
		syncCompleted: function(){
			console.log("syncCompleted");
			cordova.exec(function(){
				
				Arbiter.Cordova.resetWebApp();
				
				Arbiter.Cordova.setState(Arbiter.Cordova.STATES.NEUTRAL);
			}, null, "ArbiterCordova", "syncCompleted", []);
		},
		
		syncFailed: function(e){
			
			console.log("syncFailed");
			
			Arbiter.Cordova.setState(Arbiter.Cordova.STATES.NEUTRAL);
		},
		
		errorUpdatingAOI: function(e){
			
			cordova.exec(function(){
				Arbiter.Cordova.setState(Arbiter.Cordova.STATES.NEUTRAL);
			}, null, "ArbiterCordova", "errorUpdatingAOI", [e]);
		},
		
		getState: function(){
			return state;
		},
		
		setState: function(_state){
			state = _state;
			
			if(isNeutral()){
				executeWaitingFuncs();
			}
		},
		
		addMediaToFeature: function(key, media, fileName){
			
			cordova.exec(null , null, "ArbiterCordova",
					"addMediaToFeature", [key, media, fileName]);
			
			Arbiter.Cordova.setState(Arbiter.Cordova.STATES.NEUTRAL);
		},
		
		updateUploadingVectorDataProgress: function(finished, total){
			
			cordova.exec(null, null, "ArbiterCordova",
					"updateUploadingVectorDataProgress", [finished, total]);
		},
		
		updateDownloadingVectorDataProgress: function(finished, total){
			
			cordova.exec(null, null, "ArbiterCordova",
					"updateDownloadingVectorDataProgress", [finished, total]);
		},
		
		showDownloadingSchemasProgress: function(count){
			cordova.exec(null, null, "ArbiterCordova",
					"showDownloadingSchemasProgress", [count]);
		},
		
		updateDownloadingSchemasProgress: function(finished, total){
			
			cordova.exec(null, null, "ArbiterCordova",
					"updateDownloadingSchemasProgress", [finished, total]);
		},
		
		dismissDownloadingSchemasProgress: function(){
			
			cordova.exec(null, null, "ArbiterCordova",
					"dismissDownloadingSchemasProgress", []);
		},
		
		alertGeolocationError: function(){
			
			cordova.exec(null, null, "ArbiterCordova",
					"alertGeolocationError", []);
		},
		
		setMultiPartBtnsEnabled: function(enable, enableCollection){
			
			cordova.exec(null, null, "ArbiterCordova", "setMultiPartBtnsEnabled", [enable, enableCollection]);
		},
		
		confirmPartRemoval: function(onConfirm){
			
			cordova.exec(onConfirm, null, "ArbiterCordova", "confirmPartRemoval", []);
		},
		
		confirmGeometryRemoval: function(onConfirm){
			
			cordova.exec(onConfirm, null, "ArbiterCordova", "confirmGeometryRemoval", []);
		},
		
		hidePartButtons: function(){
			cordova.exec(null, null, "ArbiterCordova", "hidePartButtons", []);
		},
		
		reportLayersWithUnsupportedCRS: function(layers){
			cordova.exec(null, null, "ArbiterCordova", "reportLayersWithUnsupportedCRS", [layers]);
		}
	};
})();