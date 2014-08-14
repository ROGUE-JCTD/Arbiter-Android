(function(){
	
	Arbiter.VectorSync = function(db, _map, _bounds, _onSuccess, _onFailure){

		this.db = db;
		
		this.map = _map;
		
		this.bounds = _bounds;
		
		this.layers = this.map.getLayersByClass("OpenLayers.Layer.Vector");
		
		this.usingSpecificSchemas = false;
		
		this.index = -1;
		
		this.onSuccess = _onSuccess;
		this.onFailure = _onFailure;
		
		this.finishedLayerDownloadCount = 0;
		this.finishedLayoutUploadCount = 0;
		this.totalLayerCount = this.getTotalLayerCount();
		
		// For keeping track of whether a layer failed to upload
		this.failedOnUpload = {};
	};

	var prototype = Arbiter.VectorSync.prototype;
	
	prototype.getTotalLayerCount = function(){
		
		var count = this.layers.length;
		
		if(this.usingSpecificSchemas === false 
				|| this.usingSpecificSchemas === "false"){
			
			var aoiLayer = this.map.getLayersByName(Arbiter.AOI);
			
			if(aoiLayer !== null 
					&& aoiLayer !== undefined
					&& aoiLayer.length > 0){
				
				count--;
			}
		}
		
		return count;
	};

	prototype.setSpecificSchemas = function(_schemas){
		this.layers = _schemas;
		
		this.totalLayerCount = this.layers.length;
		
		this.usingSpecificSchemas = true;
	};

	prototype.onUploadComplete = function(){
		
		this.startDownload();
	};

	prototype.onDownloadComplete = function(){
		
		if(Arbiter.Util.funcExists(this.onSuccess)){
			this.onSuccess();
		}
	};

	prototype.onSyncFailure = function(e){
		
		if(Arbiter.Util.funcExists(this.onFailure)){
			this.onFailure(e);
		}
	};
	
	prototype.pop = function(){
		var layer = this.layers[++this.index];
		
		// Skip the aoi layer
		if((this.usingSpecificSchemas !== true 
				&& this.usingSpecificSchemas !== "true" )
				&& (layer !== null && layer !== undefined)
				&& layer.name === Arbiter.AOI){
			
			layer = this.layers[++this.index];
		}
		
		return layer;
	};

	prototype.startUpload = function(){
		
		this.startNextUpload();
	};

	prototype.startNextUpload = function(){
		var context = this;
		var layer = this.pop();
		
		if(layer !== null && layer !== undefined){
			
			var key = Arbiter.Util.getLayerId(layer);
			var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.VECTOR;
			var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.UPLOAD;
			
			var callback = function(succeeded){
				
				if(!succeeded){
					
					context.failedOnUpload[key] = layer;
				}

				Arbiter.Cordova.updateUploadingVectorDataProgress(
						++context.finishedLayoutUploadCount,
						context.totalLayerCount);
				
				context.startNextUpload();
			};
			
			var schema = Arbiter.Util.getSchemaFromOlLayer(layer);
			
			if(Arbiter.Util.existsAndNotNull(schema) && schema.isReadOnly()){
				
				Arbiter.FeatureTableHelper.getUnsyncedFeatureCount(schema.getFeatureType(), function(count){
					
					console.log("getUnsyncedFeatureCount count = " + count);
					
					if(count > 0){
						
						Arbiter.FailedSyncHelper.setErrorFor(key, dataType, syncType, key, Arbiter.Error.Sync.UNAUTHORIZED, function(){
							
							console.log("vectorDownload updateError success");
							
							callback(false);
						}, function(e){
							console.log("vectorDownload updateError failed", (e.stack) ? e.stack : e);
							callback(false);
						});
					}else{
						
						Arbiter.FailedSyncHelper.remove(key, dataType, syncType, key, function(){
							
							callback(true);
						}, function(e){
							
							console.log("failed to remove failed_sync item: ", e);
							
							callback(true);
						});
					}
				}, function(e){
					
					console.log("Error getting unsynced feature count: " , e);
					
					callback(false);
				});
				 
				return;
			}
			
			var onRequestCancelled = function(){
				
				context.onSyncFailure(Arbiter.Error.Sync.TIMED_OUT);
			};
			
			var uploader = new Arbiter.VectorUploader(layer, function(requestCancelled){
				
				Arbiter.FailedSyncHelper.remove(key, dataType, 
						syncType, key, function(){
					
					if(requestCancelled){
						
						onRequestCancelled();
					}else{
						callback(true);
					}
				}, function(e){
					console.log("Could not remove this layer from failed_sync - " + key, e);
					
					if(requestCancelled){
						
						onRequestCancelled();
					}else{
						callback(true);
					}
				});
			}, function(requestCancelled, error){
				
				// Save the type of error that it was.
				if(requestCancelled){
					
					onRequestCancelled();
				}else if(Arbiter.Util.existsAndNotNull(error)){
					
					Arbiter.FailedSyncHelper.setErrorFor(key, dataType, syncType, key, error, function(){
						
						console.log("vectorDownload updateError success");
						
						callback(false);
					}, function(e){
						console.log("vectorDownload updateError failed", (e.stack) ? e.stack : e);
						callback(false);
					});
				}else{
					callback(false);
				}
			});
			
			uploader.upload();
		}else{
			this.onUploadComplete();
		}
	};

	prototype.startDownload = function(){
		
		this.index = -1;
		
		this.startNextDownload();
	};

	prototype.startNextDownload = function(){
		
		var context = this;
		
		var layer = this.pop();
		
		if(layer !== null & layer !== undefined){
			
			var schema = null;
			
			if(this.usingSpecificSchemas === true){
				schema = layer;
			}else{
				schema = Arbiter.Util.getSchemaFromOlLayer(layer);
			}
			
			var key = schema.getLayerId();
			
			var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.VECTOR;
			var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.DOWNLOAD;
			
			var callback = function(){
				Arbiter.Cordova.updateDownloadingVectorDataProgress(
						++context.finishedLayerDownloadCount,
						context.totalLayerCount);
				
				context.startNextDownload();
			};
			
			// If the layer failed to upload, don't download
			if(Arbiter.Util.existsAndNotNull(this.failedOnUpload[key])){
				
				Arbiter.FailedSyncHelper.setErrorFor(key, dataType, syncType, key, Arbiter.Error.Sync.MUST_COMPLETE_UPLOAD_FIRST, function(){
					callback();
				}, function(e){
					callback();
				});
				
				return;
			}
			
			var downloader = new Arbiter.VectorDownloader(this.db, schema, this.bounds, function(){
				
				Arbiter.FailedSyncHelper.remove(key, dataType, syncType, key, function(){
					
					callback();
				}, function(e){
					console.log("Could not store this layer in failed_sync: " + key);
					
					callback();
				});
			}, function(error){
				
				if(error === Arbiter.Error.Sync.TIMED_OUT){
					
					// pass in a function for continuing and a function for cancelling. 
					Arbiter.Cordova.syncOperationTimedOut(callback, function(){
					
						context.onSyncFailure(error);
					});
				}else if(Arbiter.Util.existsAndNotNull(error)){
					
					Arbiter.FailedSyncHelper.setErrorFor(key, dataType, syncType, key, error, function(){
						
						console.log("updateError success");
						
						callback();
					}, function(e){
						console.log("updateError failed", (e.stack) ? e.stack : e);
						callback();
					});

					callback();
				}else{
					callback();
				}
			});
			
			downloader.download();
		}else{
			this.onDownloadComplete();
		}
	};
})();