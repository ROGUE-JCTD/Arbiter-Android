Arbiter.ReattemptFailedMediaUploads = function(_failedMediaUploads, _onSuccess, _onFailure){
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	
	this.dataType = Arbiter.FailedSyncHelper.DATA_TYPES.MEDIA;
	this.syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.UPLOAD;
	
	this.mediaDir = null;
	
	this.mediaToSend = _failedMediaUploads;
	this.layerIds = this.getLayerIds();
	this.index = -1;
	
	this.finishedLayersCount = 0;
	this.totalLayersCount = this.layerIds.length;
	this.failedToUpload = null;
};

Arbiter.ReattemptFailedMediaUploads.prototype.onFinishedAttempts = function(){
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		this.onSuccess(this.failedToUpload);
	}
};

Arbiter.ReattemptFailedMediaUploads.prototype.startAttempts = function(){
	
	var context = this;
	
	Arbiter.FileSystem.ensureMediaDirectoryExists(function(_mediaDir){
		
		context.mediaDir = _mediaDir;
		
		context.attemptNext();
	}, function(e){
		
		context.onFailure("Could not get the media directory - " + JSON.stringify(e));
	});
};

Arbiter.ReattemptFailedMediaUploads.prototype.pop = function(){
	
	if(++this.index < this.layerIds.length){
		
		var context = this;
		var layerId = this.layerIds[++this.index];
		
		var mediaForLayer = this.mediaToSend[layerId];
		
		return {
			layerId: layerId,
			mediaForLayer: mediaForLayer
		};
	}
	
	return undefined;
};

Arbiter.ReattemptFailedMediaUploads.prototype.getLayerIds = function(){
	var layerIds = [];
	
	for(var key in this.mediaToSend){
		layerIds.push(key);
	}
	
	return layerIds;
};

Arbiter.ReattemptFailedMediaUploads.prototype.attemptNext = function(){
	
	var obj = this.pop();
	
	if(obj !== undefined){
		this.upload(obj);
	}else{
		this.onFinishedAttempts();
	}
};

Arbiter.ReattemptFailedMediaUploads.prototype.addToFailedToUpload = function(layerId, failed){
	
	if(Arbiter.Util.existsAndNotNull(layerId) && Arbiter.Util.existsAndNotNull(failed)){
		
		if(!Arbiter.Util.existsAndNotNull(this.failedToUpload)){
			this.failedToUpload = {};
		}
		
		this.failedToUpload[layerId] = failed;
	}
};

Arbiter.ReattemptFailedMediaUploads.prototype.upload = function(obj){
	var context = this;
	
	var mediaForLayer = obj.mediaForLayer;
	
	var schema = Arbiter.getLayerSchemas()[obj.layerId];
	
	if(!Arbiter.Util.existsAndNotNull(schema)){
		throw "Schema must exist and not be null";
	}
	
	if(mediaForLayer === null 
			|| mediaForLayer === undefined 
			|| mediaForLayer.length === 0){
		
		++this.finishedLayersCount;
		
		if(this.finishedLayersCount === this.totalLayersCount){
			
			var featureType = schema.getFeatureType();
			var finishedMediaCount = 0;
			var totalMediaCount = 0;
			
			Arbiter.Cordova.updateMediaUploadingStatus(featureType,
					finishedMediaCount, totalMediaCount,
					this.finishedLayersCount, this.totalLayersCount);
		}
		
		this.attemptNext();
		
		return;
	}
	
	var server = Arbiter.Util.Servers.getServer(schema.getServerId());
	
	if(!Arbiter.Util.existsAndNotNull(server)){
		throw "Server must exist and not be null";
	}
	
	var uploader = new Arbiter.MediaUploader(schema, this.mediaToSend,
			server, this.mediaDir, this.finishedLayersCount, this.totalLayersCount);
	
	uploader.startUpload(function(failed){
		
		context.finishedLayersCount++;
		
		context.addToFailedToUpload(obj.layerId, failed);
		
		context.attemptNext();
	});
	
};