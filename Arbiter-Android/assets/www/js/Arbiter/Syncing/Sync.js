Arbiter.Sync = function(_map, _cacheTiles, _bounds,
		_downloadOnly, _onSuccess, _onFailure){
	
	this.map = _map;
	this.cacheTiles = _cacheTiles;
	this.bounds = _bounds;
	this.downloadOnly = _downloadOnly;
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	this.specificSchemas = null;
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

Arbiter.Sync.prototype.sync = function(){
	var context = this;
	
	var vectorSync = new Arbiter.VectorSync(this.map, this.bounds,
			function(failedUploads, failedDownloads){
		
		console.log("vector sync completed failedUploads = " 
				+ JSON.stringify(failedUploads) 
				+ ", failedDownloads = " 
				+ JSON.stringify(failedDownloads));
		
		if(failedUploads.length > 0 || failedDownloads.length > 0){
			
			//Arbiter.Cordova.alertVectorSyncFailures(context);
		}

			
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
		vectorSync.startUpload();
	}
};

Arbiter.Sync.prototype.startMediaSync = function(){
	var context = this;
	
	var mediaSync = new Arbiter.MediaSync(Arbiter.getLayerSchemas());
	
	mediaSync.startSync(function(failedUploads, failedDownloads){
		
		console.log("media sync completed failedUploads = " 
				+ JSON.stringify(failedUploads) 
				+ ", failedDownloads = " 
				+ JSON.stringify(failedDownloads));
		
		if(failedUploads.length > 0 || failedDownloads.length > 0){
			
			//Arbiter.Cordova.alertMediaSyncFailures(context);
		}

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

