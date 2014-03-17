Arbiter.VectorSync = function(_map, _bounds, _onSuccess, _onFailure){

	this.map = _map;
	this.bounds = _bounds;
	this.layers = this.map.getLayersByClass("OpenLayers.Layer.Vector");
	
	this.usingSpecificSchemas = false;
	
	this.index = -1;
	
	this.failedToUpload = null;
	this.failedToDownload = null;
	
    //this will be set to true if a request times out and will cancel the rest of the sync
    this.syncTimedOut = false;
	
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
    if(this.syncTimedOut) {
        this.index = -1;
        var layer = this.pop();
            
        while(layer !== null & layer !== undefined){
            this.putFailedDownload(Arbiter.Util.getSchemaFromOlLayer(layer).getFeatureType());
                
            layer = this.pop();
        }
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
		this.onSuccess(this.failedToUpload, this.failedToDownload);
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
		    console.log("vector upload callback");
			if(!context.syncTimedOut) {
			    Arbiter.Cordova.updateUploadingVectorDataProgress(
					++context.finishedLayoutUploadCount,
					context.totalLayerCount);
			    context.startNextUpload();
			} else {
			    layer = context.pop();
			    while(layer !== null && layer !== undefined) {
	                context.putFailedUpload(Arbiter.Util.getSchemaFromOlLayer(layer).getFeatureType());
	                layer = context.pop();
			    }
			    context.onUploadComplete();
			}
		};
		
		var key = Arbiter.Util.getLayerId(layer);
		var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.VECTOR;
		var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.UPLOAD;
		
		var uploader = new Arbiter.VectorUploader(layer, function(cancelSync){
            context.syncTimedOut = cancelSync;
            
			Arbiter.FailedSyncHelper.remove(key, dataType, 
					syncType, key, function(){
				
				callback();
			}, function(){
				console.log("Could not remove this layer from failed_sync - " + key);
				
				callback();
			});
		}, function(featureType, cancelSync){
			context.putFailedUpload(featureType);
			
		    context.syncTimedOut = cancelSync;
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
            if(!context.syncTimedOut) {
                Arbiter.Cordova.updateDownloadingVectorDataProgress(
                        ++context.finishedLayerDownloadCount,
                        context.totalLayerCount);
                
                context.startNextDownload();
            } else {
                layer = context.pop();
                while(layer !== null && layer !== undefined) {
                    context.putFailedDownload(Arbiter.Util.getSchemaFromOlLayer(layer).getFeatureType());
                    layer = context.pop();
                }
                context.onDownloadComplete();
            }
		};
		
		var key = schema.getLayerId();
		
		var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.VECTOR;
		var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.DOWNLOAD;
		
		var downloader = new Arbiter.VectorDownloader(schema, this.bounds, function(cancelSync){
		    context.syncTimedOut = cancelSync;
		    
			Arbiter.FailedSyncHelper.remove(key, dataType, syncType, key, function(){
				
				callback();
			}, function(e){
				console.log("Could not store this layer in failed_sync: " + key);
				
				callback();
			});
		}, function(featureType, cancelSync){
		    context.syncTimedOut = cancelSync;
		    
			context.putFailedDownload(featureType)
			callback();
		});
		
		downloader.download();
	}else{
		this.onDownloadComplete();
	}
};
