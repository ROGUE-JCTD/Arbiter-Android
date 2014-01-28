Arbiter.Sync = function(_map, _cacheTiles, _bounds,
		_downloadOnly, _onSuccess, _onFailure){
	
	this.map = _map;
	this.cacheTiles = _cacheTiles;
	this.bounds = _bounds;
	this.downloadOnly = _downloadOnly;
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	this.specificSchemas = null;
	
	// Arrays
	this.failedVectorUploads = null;
	this.failedVectorDownloads = null;
	
	// Object[layerId]
	this.failedMediaUploads = null;
	this.failedMediaDownloads = null;
	
	this.mediaDir = null;
	this.mediaToSend = null;
	
	this.initialized = false;
	
	this.layers = null;
	this.schemas = null;
	
	this.syncInProgress = false;
};

// ol layers
Arbiter.Sync.prototype.setSpecificSchemas = function(schemas){
	
	console.log("sync this.downloadOnly = " + this.downloadOnly);
	
	if(this.downloadOnly !== true && this.downloadOnly !== "true"){
		throw "You cannot specify specific schema unless you're downloading only";
	}
	
	this.specificSchemas = schemas;
};

Arbiter.Sync.prototype.onSyncCompleted = function(){
	
	console.log("Arbiter.Sync completed");
	
	this.syncInProgress = false;
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		this.onSuccess();
	}
};

Arbiter.Sync.prototype.onSyncFailed = function(e){
	
	Arbiter.Cordova.syncFailed(e);
	
	if(Arbiter.Util.funcExists(this.onFailure)){
		this.onFailure(e);
	}
};

Arbiter.Sync.prototype.initialize = function(onSuccess, onFailure){
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
		Arbiter.PreferencesHelper.get(Arbiter.MEDIA_TO_SEND, context, function(mediaToSend){
			
			var callback = function(){
				
				var storeVectorSync = new Arbiter.StoreVectorToSync(context.map, context.downloadOnly,
						context.specificSchemas, function(){
					
					// Load the layers from the database
					Arbiter.LayersHelper.loadLayers(context, function(layers){
						
						context.layers = layers;
						
						context.initialized = true;
						
						context.schemas = Arbiter.getLayerSchemas();
						
						success();
					}, function(e){
						if(Arbiter.Util.funcExists(onFailure)){
							onFailure("Sync.js Error loading layers - " + e);
						}
					});
				});
				
				storeVectorSync.startStore();
			};
			
			if(mediaToSend !== null && mediaToSend !== undefined){
				context.mediaToSend = JSON.parse(mediaToSend);
				
				callback();
			}else{
				callback();
			}
		}, function(e){
			console.log("Sync.js Error getting " + Arbiter.MEDIA_TO_SEND, e);
			
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Sync.js Error getting " 
						+ Arbiter.MEDIA_TO_SEND + " - " + e);
			}
		});
	}, function(e){
		console.log("Sync.js Error getting media directory", e);
		
		if(Arbiter.Util.funcExists(onFailure)){
			onFailure("Sync.js Error getting media directory - " + e);
		}
	});
};

Arbiter.Sync.prototype.sync = function(){
	
	if(this.syncInProgress === true){
		
		console.log("Sync is already underway!");
		
		return;
	}
	
	this.syncInProgress = true;
	
	var context = this;
	
	this.initialize(function(){
		
		context.storeUploadsAndDownloads();
	}, function(e){
		
		context.onSyncFailed(e);
	});
};

Arbiter.Sync.prototype.storeUploadsAndDownloads = function(){
	
	var context = this;
	
	var schemas = this.schemas;
	
	if(this.downloadOnly === true && Arbiter.Util.existsAndNotNull(this.specificSchemas)){
		schemas = this.specificSchemas;
	}
	
	var db = Arbiter.FeatureDbHelper.getFeatureDatabase();
	
	var storeMediaToDownload = new Arbiter.StoreMediaToDownload(
			db, this.layers, schemas, function(failedToStore){
		
		console.log("failedToStore: " + JSON.stringify(failedToStore));
		
		context.startVectorSync();
	});
	
	storeMediaToDownload.startStoring();
};

Arbiter.Sync.prototype.startVectorSync = function(){
	var context = this;
	
	var vectorSync = new Arbiter.VectorSync(this.map, this.bounds,
			function(failedUploads, failedDownloads){
		
		context.failedVectorUploads = failedUploads;
		context.failedVectorDownloads = failedDownloads;
		
		console.log("vector sync completed failedUploads = " 
				+ JSON.stringify(failedUploads) 
				+ ", failedDownloads = " 
				+ JSON.stringify(failedDownloads));
			
		context.startMediaSync();
		
	}, function(e){
		context.onSyncFailed(e);
	});
	
	if(this.specificSchemas !== null && this.specificSchemas !== undefined){
		console.log("set vector specific schemas");
		vectorSync.setSpecificSchemas(this.specificSchemas);
	}
	
	if(this.downloadOnly === true || this.downloadOnly === "true"){
		console.log("vector sync download only");
		vectorSync.startDownload();
	}else{
		console.log("vector sync upload and download");
		vectorSync.startUpload();
	}
};

Arbiter.Sync.prototype.startMediaSync = function(){
	var context = this;
	
	var mediaSync = new Arbiter.MediaSync(this.layers, 
			this.schemas, this.mediaDir, this.mediaToSend);
	
	mediaSync.startSync(function(failedUploads, failedDownloads){
		
		context.failedMediaUploads = failedUploads;
		context.failedMediaDownloads = failedDownloads;
		
		console.log("media sync completed failedUploads = " 
				+ JSON.stringify(failedUploads) 
				+ ", failedDownloads = " 
				+ JSON.stringify(failedDownloads));

		if(context.cacheTiles === true || context.cacheTiles === "true"){
			context.startTileCache();
		}else{
			context.onSyncCompleted();
		}

	}, function(e){
		context.onSyncFailed(e);
	}, this.downloadOnly);
};

Arbiter.Sync.prototype.startTileCache = function(){
	var context = this;
	
	var olBounds = new OpenLayers.Bounds(this.bounds.getLeft(),
			this.bounds.getBottom(),
			this.bounds.getRight(),
			this.bounds.getTop());
	
	console.log("startTileCache: " + JSON.stringify(olBounds));
	
	Arbiter.getTileUtil().cacheTiles(olBounds, function(){
		
		console.log("cached tiles");
		
		context.onSyncCompleted();
	}, function(e){
		onSyncFailure("Sync failed to cache tiles: " + e);
	});
};

