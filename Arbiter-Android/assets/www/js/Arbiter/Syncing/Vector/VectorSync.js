Arbiter.VectorSync = function(_map, _bounds, _onSuccess, _onFailure){

	this.map = _map;
	
	this.bounds = _bounds;
	
	this.layers = this.map.getLayersByClass("OpenLayers.Layer.Vector");
	
	this.usingSpecificSchemas = false;
	
	this.index = -1;
	
	this.failedToUpload = null;
	this.failedToDownload = null;
	
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	
	
	this.finishedLayerDownloadCount = 0;
	this.finishedLayoutUploadCount = 0;
	this.totalLayerCount = this.getTotalLayerCount();
};

Arbiter.VectorSync.prototype.getTotalLayerCount = function(){
	
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

Arbiter.VectorSync.prototype.setSpecificSchemas = function(_schemas){
	this.layers = _schemas;
	
	this.totalLayerCount = this.layers.length;
	
	this.usingSpecificSchemas = true;
};

Arbiter.VectorSync.prototype.onUploadComplete = function(){
	console.log("Vector data has finished uploading");
	
	this.startDownload();
};

Arbiter.VectorSync.prototype.onDownloadComplete = function(){
	console.log("Vector data has finished downloading");
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		this.onSuccess(this.failedToUpload,
				this.failedToDownload);
	}
};

Arbiter.VectorSync.prototype.pop = function(){
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

Arbiter.VectorSync.prototype.startUpload = function(){
	
	this.startNextUpload();
};

Arbiter.VectorSync.prototype.putFailedUpload = function(failed){
	
	if(failed !== null && failed !== undefined){
		
		if(this.failedToUpload === null || this.failedToUpload === undefined){
			this.failedToUpload = [];
		}
		
		this.failedToUpload.push(failed);
	}
};

Arbiter.VectorSync.prototype.startNextUpload = function(){
	var context = this;
	var layer = this.pop();
	
	if(layer !== null && layer !== undefined){
		
		var callback = function(){
			Arbiter.Cordova.updateUploadingVectorDataProgress(
					++context.finishedLayoutUploadCount,
					context.totalLayerCount);
			
			context.startNextUpload();
		};
		
		var key = Arbiter.Util.getLayerId(layer);
		var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.VECTOR;
		var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.UPLOAD;
		
		var uploader = new Arbiter.VectorUploader(layer, function(){
			
			Arbiter.FailedSyncHelper.remove(key, dataType, 
					syncType, key, function(){
				
				callback();
			}, function(){
				console.log("Could not remove this layer from failed_sync - " + key);
				
				callback();
			});
		}, function(featureType){
			
			context.putFailedUpload(featureType);
			
			callback();
		});
		
		uploader.upload();
	}else{
		this.onUploadComplete();
	}
};

Arbiter.VectorSync.prototype.startDownload = function(){
	
	this.index = -1;
	
	this.startNextDownload();
};

Arbiter.VectorSync.prototype.putFailedDownload = function(failed){
	
	if(failed !== null && failed !== undefined){
		
		if(this.failedToDownload === null || this.failedToDownload === undefined){
			this.failedToDownload = [];
		}
		
		this.failedToDownload.push(failed);
	}
};

Arbiter.VectorSync.prototype.startNextDownload = function(){
	
	var context = this;
	
	var layer = this.pop();
	
	if(layer !== null & layer !== undefined){
		
		var schema = null;
		
		if(this.usingSpecificSchemas === true){
			schema = layer;
		}else{
			schema = Arbiter.Util.getSchemaFromOlLayer(layer);
		}
		
		var callback = function(){
			Arbiter.Cordova.updateDownloadingVectorDataProgress(
					++context.finishedLayerDownloadCount,
					context.totalLayerCount);
			
			context.startNextDownload();
		};
		
		var key = schema.getLayerId();
		
		var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.VECTOR;
		var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.DOWNLOAD;
		
		var downloader = new Arbiter.VectorDownloader(schema, this.bounds, function(){
			
			Arbiter.FailedSyncHelper.remove(key, dataType, syncType, key, function(){
				
				callback();
			}, function(e){
				console.log("Could not store this layer in failed_sync: " + key);
				
				callback();
			});
		}, function(featureType){
			
			console.log("vectorDownloader failure");
			
			context.putFailedDownload(featureType)
			callback();
		});
		
		downloader.download();
	}else{
		this.onDownloadComplete();
	}
};
