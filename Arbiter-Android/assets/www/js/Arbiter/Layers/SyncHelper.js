Arbiter.Layers.SyncHelper = (function(){
	var cacheTiles = false;
	var optionalSuccess = null;
	var optionalFailure = null;
	
	var wfsLayerCount = 0;
	var syncedLayers = 0;
	
	var reset = function(){
		cacheTiles = false;
		optionalSuccess = null;
		optionalFailure = null;
		wfsLayerCount = 0;
		syncedLayers = 0;
	};
	
	var onSyncFailure = function(e){
		Arbiter.Cordova.syncFailed(e);
		
		if(Arbiter.Util.funcExists(optionalFailure)){
			optionalFailure(e);
		}
	};
	
	var syncMedia = function(onSuccess, onFailure){
		var map = Arbiter.Map.getMap();
		
		// Get the WFS layers on the map
		var wfsLayers = map.getLayersByClass("OpenLayers.Layer.Vector");
		
		var layerCount = wfsLayers.length;
		
		var syncedLayers = 0;
		
		if(layerCount === 0){
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess();
			}
			
			return;
		}
		
		for(var i = 0; i < layerCount; i++){
			Arbiter.MediaHelper.syncMedia(wfsLayers[i], function(layerId, layerName, failedMedia){
				console.log("Sync Media done layerId = " + layerId + " layerName = "
						+ layerName + " failedMedia = ", failedMedia);
				
				if(++syncedLayers === layerCount){
					if(Arbiter.Util.funcExists(onSuccess)){
						onSuccess();
					}
				}
			}, function(e){
				console.log("Arbiter.SyncHelper - error syncing media - ", e);
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure("Arbiter.SyncHelper - error syncing media - " + e);
				}
				
				++syncedLayers;
			});
		}
	};
	
	/**
	 * @param {Arbiter.Util.Bounds} aoi
	 */
	var onSyncSuccess = function(_aoi){
		if(++syncedLayers === wfsLayerCount || wfsLayerCount === 0){
			
			var success = function(){
				syncMedia(function(){
					Arbiter.Cordova.syncCompleted();
					
					if(Arbiter.Util.funcExists(optionalSuccess)){
						optionalSuccess();
					}
				}, function(e){
					
				});
			};
			
			if(cacheTiles){
				var aoi = new OpenLayers.Bounds([_aoi.getLeft(),
				    _aoi.getBottom(), _aoi.getRight(), _aoi.getTop()]);
				
				console.log("onSyncSuccess aoi: ", _aoi.getLeft() + ", " 
						+ _aoi.getBottom() + ", " + _aoi.getRight() + ", " 
						+ _aoi.getTop());
				
				console.log("onSyncSuccess cacheTiles", aoi);
				
				Arbiter.getTileUtil().cacheTiles(aoi, function(){
					success();
				}, function(e){
					onSyncFailure("Sync failed to cache tiles: " + e);
				});
			}else{
				success();
			}
		}
	};
	
	/**
	 * @param {Arbiter.Util.Bounds} aoi
	 */
	var onClearSuccess = function(aoi, schema, features){
		// On successful delete, insert the downloaded features
		var isDownload = true;
		
		Arbiter.FeatureTableHelper.insertFeatures(schema, 
			schema.getSRID(), features, isDownload, function(){
			
			onSyncSuccess(aoi);
			
		}, onSyncFailure);
	};
	
	var onClearFailure = function(e){
		onSyncFailure("Sync failed to clear featuretable - " + e);
	};
	
	/**
	 * @param {Arbiter.Util.Bounds} aoi
	 */
	var onDownloadSuccess = function(aoi, schema, features){
		// On successful download, delete the layers feature table
		Arbiter.FeatureTableHelper.clearFeatureTable(schema, function(){
			onClearSuccess(aoi, schema, features);
		}, onClearFailure);
	};
	
	var onDownloadFailure = function(){
		onSyncFailure("Sync failed - Could not download features");
	};
	
	var syncVectorData = function(){
		var map = Arbiter.Map.getMap();
		
		// Get the WFS layers on the map
		var wfsLayers = map.getLayersByClass("OpenLayers.Layer.Vector");
		
		wfsLayerCount = wfsLayers.length;
		
		if(wfsLayers.length === 0){
			
			Arbiter.PreferencesHelper.get(Arbiter.AOI, this, function(_aoi){
				var aoi = _aoi.split(',');
				
				var bounds = new Arbiter.Util.Bounds(aoi[0], 
						aoi[1], aoi[2], aoi[3]);
				
				onSyncSuccess(bounds);
			});
			
			return;
		}
		
		for(var i = 0; i < wfsLayers.length; i++){
			if(wfsLayers[i].strategies[0]){
				
				wfsLayers[i].strategies[0].save();
			}
		}
	};
	
	return {
		sync: function(_cacheTiles, onSuccess, onFailure){
			Arbiter.Cordova.setState(Arbiter.Cordova.STATES.UPDATING);
			
			reset();
			
			cacheTiles = _cacheTiles;
			optionalSuccess = onSuccess;
			optionalFailure = onFailure;
			
			syncVectorData();
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
							encodedCredentials, function(schema, features){
						
						// Call the onDownloadSuccess method,
						// but also need to pass the aoi down
						onDownloadSuccess(bounds, schema, features);
						
					}, onDownloadFailure);
				}else{
					onSyncFailure(e);
				}
			}, function(e){
				onSyncFailure("Sync failed to get AOI - " + e);
			});
		},
		
		onSaveFailure: function(){
			// TODO: Handle save failed.
			onSyncFailure("Sync failed - onSaveFailure");
		}
	};
})();