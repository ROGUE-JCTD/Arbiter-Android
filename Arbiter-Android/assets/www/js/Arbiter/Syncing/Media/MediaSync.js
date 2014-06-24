Arbiter.MediaSync = function(projectDb, _dbLayers, _layerSchemas, _mediaDir, _mediaToSend){
	this.projectDb = projectDb;
	this.mediaToSend = _mediaToSend;
	this.mediaDir = _mediaDir;
	
	this.layers = _dbLayers;
	
	this.layerSchemas = _layerSchemas;
	this.totalLayers = this.layers.length;
	
	this.index = -1;
	this.finishedLayersDownloading = 0;
	this.finishedLayersUploading = 0;
	
	this.onSyncComplete = null;
	this.onSyncFailure = null;
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
	this.onSyncFailure = onFailure;

	if(downloadOnly === true || downloadOnly === "true"){
		
		context.startDownloadForNext();
	}else{
		
		context.startUploadForNext();
	}
};

Arbiter.MediaSync.prototype.onUploadComplete = function(){
	
	this.index = -1;
	
	this.startDownloadForNext();
};

Arbiter.MediaSync.prototype.onDownloadComplete = function(){
	
	if(Arbiter.Util.funcExists(this.onSyncComplete)){
		this.onSyncComplete();
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
	
	var downloadPhotos = false;
	
	var doWork = function() {
		if(layer !== undefined && downloadPhotos){
			context.downloadMedia(layer);
		}else{
			context.onDownloadComplete();
		}
	}
	
	Arbiter.PreferencesHelper.get(Arbiter.ProjectDbHelper.getProjectDatabase(), Arbiter.DOWNLOAD_PHOTOS, context, function(_downloadPhotos){
		if (_downloadPhotos !== undefined && _downloadPhotos !== null) {
			downloadPhotos = _downloadPhotos == 'true';
		}
		doWork();
	}, function(e){
		doWork();
	});
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
	
	if(schema.isEditable() === false || schema.isReadOnly()){
	
		++this.finishedLayersUploading;
		
		this.startUploadForNext();
		
		return;
	}
	
	var proceed = function(){
		
		++context.finishedLayersUploading;
		
		context.startUploadForNext();
	};
	
	var mediaUploader = new Arbiter.MediaUploader(this.projectDb,
			schema, this.mediaToSend,
			server, context.mediaDir,
			this.finishedLayersUploading, this.totalLayers);
	
	mediaUploader.startUpload(function(){
		
		proceed();
	}, function(e){
		
		// If the error was a timeout, call the sync failure callback to cancel the rest of the uploads
		if(e === Arbiter.Error.Sync.TIMED_OUT){
			
			if(Arbiter.Util.existsAndNotNull(context.onSyncFailure)){
				
				context.onSyncFailure(e);
			}
		}else{
			
			// Otherwise proceed with uploading the rest.
			proceed();
		}
	});
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
	
	var proceed = function(){
		
		++context.finishedLayersDownloading;
		
		context.startDownloadForNext();
	};
	
	mediaDownloader.startDownload(function(){
		
		proceed();
	}, function(e){
		
		if(e === Arbiter.Error.Sync.TIMED_OUT){
			
			if(Arbiter.Util.existsAndNotNull(context.onSyncFailure)){
				context.onSyncFailure(e);
			}
		}else{
			proceed();
		}
	});
};