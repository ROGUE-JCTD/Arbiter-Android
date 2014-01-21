Arbiter.MediaSync = function(_layerSchemas){
	this.mediaToSend = null;
	this.failedOnUpload = {};
	this.failedOnDownload = {};
	this.layerSchemas = _layerSchemas;
	
	this.layers = [];
	
	// Gets populated as sync completes
	this.layersForDownload = [];
	
	this.mediaDir = null;
	
	this.onSyncComplete = null;
	
	this.initialized = false;
	
	this.finishedUploading = -1;
	this.queuedUploading = 0;
	
	this.finishedDownloading = -1;
	this.queueDownloading = 0;
};

// url
// layerId
// auth headers

Arbiter.MediaSync.prototype.MEDIA_TO_SEND = "mediaToSend";

Arbiter.MediaSync.prototype.initialize = function(onSuccess, onFailure){
	var context = this;
	
	var success = function(){
		if(Arbiter.Util.funcExists(onSuccess)){
			onSuccess();
		}
	};
	
	if(this.initialized === true){
		success();
		
		return;
	}
	
	// Make sure the media directory exists
	Arbiter.FileSystem.ensureMediaDirectoryExists(function(mediaDir){
		
		context.mediaDir = mediaDir;
		
		// Get the media to send object from the db
		Arbiter.PreferencesHelper.get(context.MEDIA_TO_SEND, context, function(mediaToSend){
			
			if(mediaToSend !== null && mediaToSend !== undefined){
				context.mediaToSend = JSON.parse(mediaToSend);
			}
				
			// Load the layers from the database
			Arbiter.LayersHelper.loadLayers(context, function(layers){
				
				context.layers = layers;
				
				context.queueDownloading = layers.length - 1;
				context.queuedUploading = layers.length - 1;
				
				context.initialized = true;
				
				success();
			}, function(e){
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure("MediaSync.js Error loading layers - " + e);
				}
			});
		}, function(e){
			console.log("MediaSync.js Error getting " + context.MEDIA_TO_SEND, e);
			
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("MediaSync.js Error getting " 
						+ context.MEDIA_TO_SEND + " - " + e);
			}
		});
	}, function(e){
		console.log("MediaSync.js Error getting media directory", e);
		
		if(Arbiter.Util.funcExists(onFailure)){
			onFailure("MediaSync.js Error getting media directory - " + e);
		}
	});
};

Arbiter.MediaSync.prototype.startSync = function(onSuccess, onFailure, downloadOnly){
	var context = this;
	
	this.onSyncComplete = onSuccess;
	
	this.initialize(function(){
		
		console.log("mediaSync initialized");
		
		if(downloadOnly === true || downloadOnly === "true"){
			this.layersForDownload = this.layers;
			context.startDownloadForNext();
		}else{
			context.startUploadForNext();
		}
	}, onFailure);
};

Arbiter.MediaSync.prototype.onUploadComplete = function(){
	// TODO: Handle errors
	console.log("MediaSync.js Upload completed.  The following"
			+ " failed to upload:", this.failedOnUpload);
	
	Arbiter.Cordova.finishMediaUploading();
	
	this.startDownloadForNext();
};

Arbiter.MediaSync.prototype.onDownloadComplete = function(){
	// TODO: Handle errors
	console.log("MediaSync.js Download completed.  The following"
			+ " failed to download:", JSON.stringify(this.failedOnDownload));
	
	Arbiter.Cordova.finishMediaDownloading();
	
	if(Arbiter.Util.funcExists(this.onSyncComplete)){
		this.onSyncComplete(this.failedOnUpload,
				this.failedOnDownload);
	}
};

Arbiter.MediaSync.prototype.startUploadForNext = function(){
	var context = this;
	
	console.log("getting next to upload");
	
	var layer = this.layers.shift();
	
	// Push the layer onto layersForDownload
	// so that the layers will still be
	// available for download
	this.layersForDownload.push(layer);
	
	if(layer !== undefined && (this.mediaToSend !== null 
			&& this.mediaToSend !== undefined)){
		
		this.uploadMedia(layer);
	}else{
		this.onUploadComplete();
	}
};

Arbiter.MediaSync.prototype.startDownloadForNext = function(){
	var context = this;
	
	console.log("startDownloadForNext", this.layersForDownload);
	
	var layer = this.layersForDownload.shift();
	
	if(layer !== undefined){
		this.downloadMedia(layer);
	}else{
		this.onDownloadComplete();
	}
};

Arbiter.MediaSync.prototype.uploadMedia = function(layer){
	var context = this;
	
	var layerId = layer[Arbiter.LayersHelper.layerId()];
	var serverId = layer[Arbiter.LayersHelper.serverId()];
	
	var mediaForLayer = this.mediaToSend[layerId];
	
	this.finishedUploading++;
	
	if(mediaForLayer === null 
			|| mediaForLayer === undefined 
			|| mediaForLayer.length === 0){
		
		this.startUploadForNext();
		
		return;
	}
	
	var server = Arbiter.Util.Servers.getServer(serverId);
	
	var mediaUploader = new Arbiter.MediaUploader(
			this.layerSchemas[layerId], mediaForLayer,
			server, context.mediaDir);
	
	mediaUploader.startUpload(function(failedMedia){
		
		context.failedOnUpload[layerId] = failedMedia;
		
		context.startUploadForNext();
	});
};

Arbiter.MediaSync.prototype.downloadMedia = function(layer){
	var context = this;
	
	console.log("downloadMedia called");
	var layerId = layer[Arbiter.LayersHelper.layerId()];
	var serverId = layer[Arbiter.LayersHelper.serverId()];
	
	var schema = this.layerSchemas[layerId];
	
	var server = Arbiter.Util.Servers.getServer(serverId);
	
	var featureDb = Arbiter.FeatureDbHelper.getFeatureDatabase();
	
	this.finishedDownloading++;
	
	var mediaDownloader = new Arbiter.MediaDownloader(featureDb,
			schema, server, context.mediaDir);
	
	mediaDownloader.startDownload(function(failedMedia){
		
		context.failedOnDownload[layerId] = failedMedia;
		
		context.startDownloadForNext();
	});
};