Arbiter.Layers.SyncHelper = (function(){
	var onSyncSuccess = function(){
		// Arbiter.Cordova.syncCompleted();
		Arbiter.Cordova.resetWebApp();
	};
	
	var onSyncFailure = function(){
		// Arbiter.Cordova.syncFailed();
	};
	
	var onClearSuccess = function(schema, features){
		// On successful delete, insert the downloaded features
		var isDownload = true;
		
		Arbiter.FeatureTableHelper.insertFeatures(schema, 
			schema.getSRID(), features, isDownload,
			onSyncSuccess, onSyncFailure);
	};
	
	var onClearFailure = function(e){
		
	};
	
	var onDownloadSuccess = function(schema, features){
		// On successful download, delete the layers feature table
		Arbiter.FeatureTableHelper.clearFeatureTable(schema, function(){
			onClearSuccess(schema, features);
		}, onClearFailure);
	};
	
	var onDownloadFailure = function(){
		
	};
	
	return {
		sync: function(){
			var map = Arbiter.Map.getMap();
			
			// Get the WFS layers on the map
			var wfsLayers = map.getLayersByClass("OpenLayers.Layer.Vector");
			
			if(wfsLayers.length === 0){
				return;
			}
			
			for(var i = 0; i < wfsLayers.length; i++){
				if(wfsLayers[i].strategies[0]){
					
					wfsLayers[i].strategies[0].save();
				}
			}
		},
		
		onSaveSuccess: function(key, olLayer, encodedCredentials){
			try{
				// Refresh the WMS layer
				Arbiter.Layers.WMSLayer.refreshWMSLayer(key);
			}catch(e){
				// Ignore the exception for now.  It just means that
				// there is no corresponding wms layer.
			}
			
			Arbiter.PreferencesHelper.get(Arbiter.AOI, this, function(_aoi){
				if(_aoi !== null && _aoi !== undefined 
						&& _aoi !== ""){
					
					var aoi = _aoi.split(',');
					
					var bounds = new Arbiter.Util.Bounds(aoi[0], 
							aoi[1], aoi[2], aoi[3]);
					
					var schema = Arbiter.getLayerSchemas()[key];
					
					// Download the latest given the project aoi
					Arbiter.Util.Feature.downloadFeatures(schema, bounds,
							encodedCredentials, onDownloadSuccess, onDownloadFailure);
				}else{
					console.log("Arbiter.Layers.SyncHelper ERROR NO AOI");
				}
			}, function(e){
				console.log("Arbiter.Layers.SyncHelper getAOI error", e);
			});
		},
		
		onSaveFailure: function(key, olLayer, encodedCredentials){
			// TODO: Handle save failed.
			console.log("Arbiter.Layers.SyncHelper.onSaveFailure()");
		}
	};
})();