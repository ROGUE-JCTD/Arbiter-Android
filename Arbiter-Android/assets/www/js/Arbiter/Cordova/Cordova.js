Arbiter.Cordova = (function() {
	var wktFormatter = new OpenLayers.Format.WKT();
	
	var state = 0;
	
	var isNeutral = function(){
		return state === Arbiter.Cordova.STATES.NEUTRAL;
	};
	
	var waitForNeutralQueue = [];
	
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
		
		/**
		 * Save the current maps extent
		 */
		resetWebApp : function(tx) {
			var bbox = Arbiter.Map.getCurrentExtent().toBBOX();
            var zoom = Arbiter.Map.getZoom();
			var reset = function(){
				console.log("resetWebApp");
				cordova.exec(null, null, "ArbiterCordova",
						"resetWebApp", [bbox, zoom]);
			};
			
			// Don't reset while a sync is occurring.
			waitForNeutral(function(){
				Arbiter.SQLiteTransactionManager.executeAfterDone(reset);
			});
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
		
		doneCreatingProject: function(){
			cordova.exec(function(){
				Arbiter.Cordova.Project.zoomToAOI();
				
				Arbiter.Cordova.setState(Arbiter.Cordova.STATES.NEUTRAL);
			}, null, "ArbiterCordova",
					"doneCreatingProject", []);
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
			
			var schema = Arbiter.getLayerSchemas()[layerId];
			
			var mode = Arbiter.Controls.ControlPanel.getMode();
			
			Arbiter.Controls.ControlPanel.exitModifyMode();
			
			this.displayFeatureDialog(schema.getFeatureType(), 
					featureId, layerId, selectedFeature, mode, false);
		},
		
		displayFeatureDialog : function(featureType, featureId, layerId,
				feature, mode, cancel){
			
			var schemas = Arbiter.getLayerSchemas();
			
			var schema = schemas[layerId];
			
			var srid = Arbiter.Map.getMap().projection.projCode;
			
			var wktGeometry = null;
			
			if(cancel === false){
				wktGeometry = wktFormatter.write(
						Arbiter.Util.getFeatureInNativeProjection(srid,
								schema.getSRID(), feature));
			}
			
			Arbiter.Controls.ControlPanel.exitModifyMode();
			
			cordova.exec(null, null, "ArbiterCordova", "featureSelected",
					[featureType, featureId, layerId, wktGeometry, mode]);
		},
		
		updateTileSyncingStatus: function(percent){
			cordova.exec(null, null, "ArbiterCordova",
					"updateTileSyncingStatus", [percent]);
		},
		
		updateSyncingStatus: function(messageId){
			
		},
		
		createProjectTileSyncingStatus: function(percent){
			cordova.exec(null, null, "ArbiterCordova",
					"createProjectTileSyncingStatus", [percent]);
		},
		
		syncCompleted: function(){
			cordova.exec(function(){
				Arbiter.Cordova.resetWebApp();
				
				Arbiter.Cordova.setState(Arbiter.Cordova.STATES.NEUTRAL);
			}, null, "ArbiterCordova", "syncCompleted", []);
		},
		
		syncFailed: function(e){
			
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
		}
	};
})();