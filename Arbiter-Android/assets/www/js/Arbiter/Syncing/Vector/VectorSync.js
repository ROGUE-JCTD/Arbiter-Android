Arbiter.VectorSync = function(db, _map, _bounds, _onSuccess, _onFailure){

	this.db = db;
	
	this.map = _map;
	this.bounds = _bounds;
	this.layers = this.map.getLayersByClass("OpenLayers.Layer.Vector");
	
	this.usingSpecificSchemas = false;
	
	this.index = -1;
	
    //this will be set to true if a request times out and will cancel the rest of the sync
    this.syncTimedOut = false;
    
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	
	this.finishedLayerDownloadCount = 0;
	this.finishedLayoutUploadCount = 0;
	this.totalLayerCount = this.getTotalLayerCount();
	
	// For keeping track of whether a layer failed to upload
	this.failedOnUpload = {};
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
    if(this.syncTimedOut) {
        this.index = -1;
        if(Arbiter.Util.funcExists(this.onFailure)){
            this.onFailure('Connection timed out');
        }
     } else {
         this.startDownload();
     }
};

Arbiter.VectorSync.prototype.onDownloadComplete = function(){

    if(this.syncTimedOut && Arbiter.Util.funcExists(this.onFailure)){
        this.onFailure('Connection timed out');
    }
	if(Arbiter.Util.funcExists(this.onSuccess)){
		this.onSuccess();
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

Arbiter.VectorSync.prototype.startNextUpload = function(){
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
			
		    console.log("vector upload callback");
			if(!context.syncTimedOut) {
			    Arbiter.Cordova.updateUploadingVectorDataProgress(
					++context.finishedLayoutUploadCount,
					context.totalLayerCount);
			    context.startNextUpload();
			} else {
			    context.onUploadComplete();
			}
		};
		
		var uploader = new Arbiter.VectorUploader(layer, function(cancelSync){
            context.syncTimedOut = cancelSync;
            
			Arbiter.FailedSyncHelper.remove(key, dataType, 
					syncType, key, function(){
				
				callback(true);
			}, function(e){
				console.log("Could not remove this layer from failed_sync - " + key, e);
				
				callback(true);
			});
		}, function(featureType, cancelSync){
			
			context.syncTimedOut = cancelSync;
			
			callback(false);
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
		
		var key = schema.getLayerId();
		
		var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.VECTOR;
		var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.DOWNLOAD;
		
		var callback = function(){
            if(!context.syncTimedOut) {
                Arbiter.Cordova.updateDownloadingVectorDataProgress(
                        ++context.finishedLayerDownloadCount,
                        context.totalLayerCount);
                
                context.startNextDownload();
            } else {
                context.onDownloadComplete();
            }
		};
		
		// If the layer failed to upload, don't download
		if(Arbiter.Util.existsAndNotNull(this.failedOnUpload[key])){
			
			callback();
			
			return;
		}
		
		var downloader = new Arbiter.VectorDownloader(this.db, schema, this.bounds, function(cancelSync){
			
		    context.syncTimedOut = cancelSync;
		    
			Arbiter.FailedSyncHelper.remove(key, dataType, syncType, key, function(){
				
				callback();
			}, function(e){
				console.log("Could not store this layer in failed_sync: " + key);
				
				callback();
			});
			
		}, function(featureType, cancelSync){
		    context.syncTimedOut = cancelSync;
		    
			callback();
		});
		
		downloader.download();
	}else{
		this.onDownloadComplete();
	}
};
