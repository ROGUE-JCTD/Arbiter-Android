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
				UPDATING: 2
		},
		
		SYNC_MESSAGE_ID : {
			
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
			
			var count = Arbiter.getTileUtil().getTileCount(bbox);
			
			cordova.exec(null, null, "ArbiterCordova",
					"confirmTileCount", [count]);
		},
		
		/**
		 * Get the area of interest in the aoi map and call the native method to
		 * save it to be set when onResume gets called on the MapActivity
		 */
		setProjectsAOI : function(layers) {
			var bbox = Arbiter.Map.getCurrentExtent();
			
			var count = Arbiter.getTileUtil().getTileCount(bbox);
			
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
		
		getUpdatedGeometry: function(){
			
			Arbiter.Controls.ControlPanel.finishGeometry();
			
			Arbiter.Controls.ControlPanel.exitModifyMode(function(){
				
				try{
					var selectedFeature = Arbiter.Controls
						.ControlPanel.getSelectedFeature();
				
					if(selectedFeature === null || selectedFeature === undefined){
						throw "getUpdatedGeometry() - selectedFeature should not be empty";
					}
					
					var featureId = null;
					
					if(selectedFeature.metadata !== null 
							&& selectedFeature.metadata !== undefined){
						
						featureId = selectedFeature.metadata[
						    Arbiter.FeatureTableHelper.ID];
					}
					
					var layerId = Arbiter.Util.getLayerId(selectedFeature.layer);
					
					console.log("layerId = " + layerId);
					
					var schema = Arbiter.getLayerSchemas()[layerId];
					
					var wktGeometry = null;
					
					if(Arbiter.Util.existsAndNotNull(selectedFeature.geometry)){
						if(!Arbiter.Util.existsAndNotNull(featureId)){
							wktGeometry = Arbiter.Geometry.getNativeWKT(selectedFeature, layerId);
						}else{
							wktGeometry = Arbiter.Geometry.checkForGeometryCollection(layerId, featureId, schema.getSRID());
						}
					}
					
					console.log("getUpdatedGeometry = " + wktGeometry);
					
					cordova.exec(null, null, "ArbiterCordova", "showUpdatedGeometry", 
							[schema.getFeatureType(), featureId, layerId, wktGeometry]);
				}catch(e){
					console.log(e.stack);
				}
			});
		},
		
		featureUnselected: function(){
			
			cordova.exec(null, null, "ArbiterCordova", "featureUnselected", []);
		},
		
		featureSelected : function(featureType, featureId, layerId,
				feature, mode, cancel){
			
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
			
			Arbiter.Controls.ControlPanel.exitModifyMode();
			
			console.log("displayFeatureDialog selectedFeature: ", Arbiter.Controls.ControlPanel.getSelectedFeature());
			
			cordova.exec(null, null, "ArbiterCordova", "featureSelected",
					[featureType, featureId, layerId, wktGeometry, mode]);
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
			
			cordova.exec(function(){
				Arbiter.Cordova.setState(Arbiter.Cordova.STATES.NEUTRAL);
			}, null, "ArbiterCordova",
					"syncFailed", [e]);
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
		
		alertGeolocationError: function(msg){
			
			cordova.exec(null, null, "ArbiterCordova",
					"alertGeolocationError", [msg]);
		},
		
		enableDoneEditingBtn: function(){
			
			cordova.exec(null, null, "ArbiterCordova",
					"enableDoneEditingBtn", []);
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
		
		notifyUserToAddGeometry: function(){
			cordova.exec(null, null, "ArbiterCordova", "notifyUserToAddGeometry", []);
		},
		
		hidePartButtons: function(){
			cordova.exec(null, null, "ArbiterCordova", "hidePartButtons", []);
		},
		
		reportLayersWithUnsupportedCRS: function(layers){
			cordova.exec(null, null, "ArbiterCordova", "reportLayersWithUnsupportedCRS", [layers]);
		},
        
        dismissSyncProgressDialog: function(){
            cordova.exec(null, null, "ArbiterCordova",
                    "dismissSyncProgressDialog", []);
        },
        
        showSyncTimeOutDialog: function(keepWaitingCallback, cancelSyncCallback){
            cordova.exec(keepWaitingCallback, cancelSyncCallback, "ArbiterCordova",
                    "showSyncTimeOutDialog", []);
        }
	};
})();