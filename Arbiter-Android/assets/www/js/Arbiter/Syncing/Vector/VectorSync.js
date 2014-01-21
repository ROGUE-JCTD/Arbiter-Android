Arbiter.VectorSync = function(_map, _bounds, _onSuccess, _onFailure){

	this.map = _map;
	
	this.bounds = _bounds;
	
	this.layers = this.map.getLayersByClass("OpenLayers.Layer.Vector");
	
	this.usingSpecificSchemas = false;
	
	this.index = -1;
	
	this.failedToUpload = [];
	this.failedToDownload = [];
	
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	
	this.queuedCount = 0;
	
	this.setQueuedCount();
};

Arbiter.VectorSync.prototype.setQueuedCount = function(){
	
	this.queuedCount = this.layers.length;
	
	if(this.usingSpecificSchemas === false 
			|| this.usingSpecificSchemas === "false"){
		
		console.log("before aoi");
		var aoiLayer = this.map.getLayersByName(Arbiter.AOI);
		console.log("before aoi");
		
		if(aoiLayer !== null 
				&& aoiLayer !== undefined
				&& aoiLayer.length > 0){
			
			this.queuedCount--;
		}
	}
};

Arbiter.VectorSync.prototype.setSpecificSchemas = function(_schemas){
	this.layers = _schemas;
	
	this.setQueuedCount();
	
	this.usingSpecificSchemas = true;
};

Arbiter.VectorSync.prototype.onUploadComplete = function(){
	console.log("Vector data has finished uploading");
	
	Arbiter.Cordova.dismissUploadingVectorDataProgress();
	
	this.startDownload();
};

Arbiter.VectorSync.prototype.onDownloadComplete = function(){
	console.log("Vector data has finished downloading");
	
	Arbiter.Cordova.dismissDownloadingVectorDataProgress();
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		this.onSuccess(this.failedToUpload,
				this.failedToDownload);
	}
};

Arbiter.VectorSync.prototype.pop = function(){
	var layer = this.layers[++this.index];
	
	// Skip the aoi layer
	if(layer.name === Arbiter.AOI){
		layer = this.layers[++this.index];
	}
	
	return layer;
};

Arbiter.VectorSync.prototype.startUpload = function(){
	
	if(this.layers.length > 0){
		Arbiter.Cordova.showUploadingVectorDataProgress(this.queuedCount);
	}
	
	this.startNextUpload();
};

Arbiter.VectorSync.prototype.startNextUpload = function(){
	var context = this;
	var layer = this.pop();
	
	if(layer !== null && layer !== undefined){
		
		var callback = function(){
			Arbiter.Cordova.updateUploadingVectorDataProgress(
					(context.index + 1), context.queuedCount);
			
			context.startNextUpload();
		};
		
		var uploader = new Arbiter.VectorUploader(layer, function(){
			
			callback();
		}, function(featureType){
			
			context.failedToUpload.push(featureType);
			
			callback();
		});
		
		uploader.upload();
	}else{
		this.onUploadComplete();
	}
};

Arbiter.VectorSync.prototype.startDownload = function(){
	
	console.log("startDownload vectorSync");
	
	if(this.layers.length > 0){
		Arbiter.Cordova.showDownloadingVectorDataProgress(this.queuedCount);
	}
	
	console.log("hello there chap");
	this.index = -1;
	
	this.startNextDownload();
};

Arbiter.VectorSync.prototype.startNextDownload = function(){
	
	var context = this;
	var layer = this.pop();
	console.log("bounds", JSON.stringify(this.bounds));
	
	if(layer !== null & layer !== undefined){
		
		var schema = null;
		
		if(this.usingSpecificSchemas === true){
			schema = layer;
		}else{
			schema = Arbiter.Util.getSchemaFromOlLayer(layer);
		}
		
		var callback = function(){
			Arbiter.Cordova.updateDownloadingVectorDataProgress(
					(context.index + 1), context.queuedCount);
			
			context.startNextDownload();
		};
		
		var downloader = new Arbiter.VectorDownloader(schema, this.bounds, function(){
			
			callback();
		}, function(featureType){
			context.failedToDownload.push(featureType);
			
			callback();
		});
		
		downloader.download();
	}else{
		this.onDownloadComplete();
	}
};
