Arbiter.MediaSync = function(projectDb, _dbLayers, _layerSchemas, _mediaDir, _mediaToSend){
	this.projectDb = projectDb;
	this.mediaToSend = _mediaToSend;
	this.mediaDir = _mediaDir;
	this.failedOnUpload = null;
	this.failedOnDownload = null;
	
	this.layers = _dbLayers;
	
	this.layerSchemas = _layerSchemas;
	this.totalLayers = this.layers.length;
	
	this.index = -1;
	this.finishedLayersDownloading = 0;
	this.finishedLayersUploading = 0;
	
	this.onSyncComplete = null;
};

// url
// layerId
// auth headers

Arbiter.MediaSync.prototype.pop = function(){
	
	if(++this.index < this.layers.length){
		return this.layers[this.index];
	}
	
	return undefined;
};

Arbiter.MediaSync.prototype.startSync = function(onSuccess, onFailure, downloadOnly){
	var context = this;
	
	this.onSyncComplete = onSuccess;
		
	if(downloadOnly === true || downloadOnly === "true"){
		
		context.startDownloadForNext();
	}else{
		
		context.startUploadForNext();
	}
};

Arbiter.MediaSync.prototype.onUploadComplete = function(){
	// TODO: Handle errors
	console.log("MediaSync.js Upload completed.  The following"
			+ " failed to upload:", this.failedOnUpload);
	
	this.index = -1;
	
	this.startDownloadForNext();
};

Arbiter.MediaSync.prototype.onDownloadComplete = function(){
	// TODO: Handle errors
	console.log("MediaSync.js Download completed.  The following"
			+ " failed to download:", JSON.stringify(this.failedOnDownload));
	
	if(Arbiter.Util.funcExists(this.onSyncComplete)){
		this.onSyncComplete(this.failedOnUpload,
				this.failedOnDownload);
	}
};

Arbiter.MediaSync.prototype.startUploadForNext = function(){
	var context = this;
	
	var layer = this.pop();
	
	if(layer !== undefined && (this.mediaToSend !== null 
			&& this.mediaToSend !== undefined)){
		
		this.uploadMedia(layer);
	}else{
		this.onUploadComplete();
	}
};

Arbiter.MediaSync.prototype.startDownloadForNext = function(){
	var context = this;
	
	var layer = this.pop();
	
	if(layer !== undefined){
		
		this.downloadMedia(layer);
	}else{
		this.onDownloadComplete();
	}
};

Arbiter.MediaSync.prototype.putFailedUpload = function(key, failed){
	
	if(failed !== null && failed !== undefined){
		
		if(this.failedOnUpload === null 
				|| this.failedOnUpload === undefined){
			
			this.failedOnUpload = {};
		}
		
		this.failedOnUpload[key] = failed;
	}
};

Arbiter.MediaSync.prototype.uploadMedia = function(layer){
	var context = this;
	
	var layerId = layer[Arbiter.LayersHelper.layerId()];
	var serverId = layer[Arbiter.LayersHelper.serverId()];
	
	var mediaForLayer = this.mediaToSend[layerId];
	
	if(mediaForLayer === null 
			|| mediaForLayer === undefined 
			|| mediaForLayer.length === 0){
		
		++this.finishedLayersUploading;
		
		if(this.finishedLayersUploading === this.totalLayers){
			
			var featureType = this.layerSchemas[layerId].getFeatureType();
			var finishedMediaCount = 0;
			var totalMediaCount = 0;
			
			Arbiter.Cordova.updateMediaUploadingStatus(featureType,
					finishedMediaCount, totalMediaCount,
					this.finishedLayersUploading, this.totalLayers);
		}
		
		this.startUploadForNext();
		
		return;
	}
	
	var server = Arbiter.Util.Servers.getServer(serverId);
	
	var schema = this.layerSchemas[layerId];
	
	if(schema.isEditable() === false){
	
		++this.finishedLayersUploading;
		
		this.startUploadForNext();
		
		return;
	}
	
	var mediaUploader = new Arbiter.MediaUploader(this.projectDb,
			schema, this.mediaToSend,
			server, context.mediaDir,
			this.finishedLayersUploading, this.totalLayers);
	
	mediaUploader.startUpload(function(failedMedia){
		
		++context.finishedLayersUploading;
		
		context.putFailedUpload(layerId, failedMedia);
		
		context.startUploadForNext();
	});
};

Arbiter.MediaSync.prototype.putFailedDownload = function(key, failed){
	
	if(failed !== null && failed !== undefined){
		
		if(this.failedOnDownload === null 
				|| this.failedOnDownload === undefined){
			
			this.failedOnDownload = {};
		}
		
		this.failedOnDownload[key] = failed;
	}
};

Arbiter.MediaSync.prototype.downloadMedia = function(layer){
	var context = this;
	
	var layerId = layer[Arbiter.LayersHelper.layerId()];
	var serverId = layer[Arbiter.LayersHelper.serverId()];

	var schema = this.layerSchemas[layerId];
	
	var server = Arbiter.Util.Servers.getServer(serverId);
	
	var featureDb = Arbiter.FeatureDbHelper.getFeatureDatabase();
	
	if(schema.isEditable() === false){
		
		++this.finishedLayersDownloading;
		
		this.startDownloadForNext();
		
		return;
	}
	
	var mediaDownloader = new Arbiter.MediaDownloader(featureDb,
			schema, server, this.mediaDir,
			this.finishedLayersDownloading, this.totalLayers);
	
	mediaDownloader.startDownload(function(failedMedia){
		
		++context.finishedLayersDownloading;
		
		context.putFailedDownload(layerId, failedMedia);
		
		context.startDownloadForNext();
	});
};