Arbiter.MediaDownloaderHelper = function(feature,
		_schema, _header, _url, _mediaDir, 
		_finishedMediaCount, _totalMediaCount,
		_finishedFeatures, _totalFeatures,
		_finishedLayers, _totalLayers){
	
	this.mediaDir = _mediaDir;
	this.header = _header;
	this.url = _url;
	
	this.schema = _schema;
	
	var mediaAttribute = feature[this.schema.getMediaColumn()];
	
	this.featureMedia = [];
	this.index = -1;
    if(Arbiter.Util.existsAndNotNull(mediaAttribute) && mediaAttribute !== "") {
        this.featureMedia = JSON.parse(mediaAttribute);
    }
    
    this.finishedMediaCount = _finishedMediaCount;
    this.totalMediaCount = _totalMediaCount;
    this.finishedFeatures = _finishedFeatures;
    this.totalFeatures = _totalFeatures;
    this.finishedLayers = _finishedLayers;
    this.totalLayers = _totalLayers;
    this.finishedMedia = 0;
    this.onDownloadComplete = null;
    this.onDownloadFailure = null;
};

Arbiter.MediaDownloaderHelper.prototype.pop = function(){
	
	if(++this.index < this.featureMedia.length){
		return this.featureMedia[this.index];
	}
	
	return undefined;
};

Arbiter.MediaDownloaderHelper.prototype.startDownload = function(onSuccess, onFailure){
	
	this.onDownloadComplete = onSuccess;
	this.onDownloadFailure = onFailure;
	
	if(this.featureMedia.length === 0){
		this.updateProgressDialog(false);
	}
	
	this.startDownloadingNext();
};

Arbiter.MediaDownloaderHelper.prototype.startDownloadingNext = function(){
	
	var media = this.pop();
	
	if(media !== undefined){
		
		this.downloadNext(media);
	}else{
		
		if(Arbiter.Util.funcExists(this.onDownloadComplete)){
			this.onDownloadComplete(this.finishedMediaCount);
		}
	}
};

Arbiter.MediaDownloaderHelper.prototype.updateProgressDialog = function(isMedia){
	
	// check if this is the last media file for the feature and increment the feature count if it is
	if(!Arbiter.Util.existsAndNotNull(this.featureMedia[this.index + 1])){
		this.finishedFeatures++;
	}
	
	// check if this is the last feature and increment the layer count if it is
	
	if(this.finishedFeatures === this.totalFeatures){
		this.finishedLayers++;
	}
	
	if(isMedia === true){
		this.finishedMediaCount++;
	}
	
	Arbiter.Cordova.updateMediaDownloadingStatus(
			this.schema.getFeatureType(), 
			this.finishedMediaCount, this.totalMediaCount,
			this.finishedLayers, this.totalLayers);
};

Arbiter.MediaDownloaderHelper.prototype.downloadNext = function(media){
	var context = this;
	
	var key = media;
	
	var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.MEDIA;
	
	var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.DOWNLOAD;
	
	var onFailure = function(e){
		
		var callback = function(){
			
			context.updateProgressDialog(true);
			
			context.startDownloadingNext();
		};
		
		// If it was a timeout, ask the user if they want to continue or cancel, and act accordingly.
		if(e === Arbiter.Error.Sync.TIMED_OUT){
			
			Arbiter.Cordova.syncOperationTimedOut(callback, function(){
				
				Arbiter.FailedSyncHelper.setErrorFor(key, dataType, syncType, context.schema.getLayerId(), e, function(){
					
					console.log("updateError success");
					
					if(Arbiter.Util.existsAndNotNull(context.onDownloadFailure)){
						context.onDownloadFailure(Arbiter.Error.Sync.TIMED_OUT, context.finishedMediaCount);
					}
				}, function(e){
					console.log("updateError failed", (e.stack) ? e.stack : e);
					
					if(Arbiter.Util.existsAndNotNull(context.onDownloadFailure)){
						context.onDownloadFailure(Arbiter.Error.Sync.TIMED_OUT, context.finishedMediaCount);
					}
				});
			});
		}else{
			
			Arbiter.FailedSyncHelper.setErrorFor(key, dataType, syncType, context.schema.getLayerId(), e, function(){
				
				console.log("updateError success");
				
				callback();
			}, function(e){
				console.log("updateError failed", (e.stack) ? e.stack : e);
				
				callback();
			});
		}
	};
	
	var onSuccess = function(){
		
		context.updateProgressDialog(true);
		
		Arbiter.FailedSyncHelper.remove(key, dataType, syncType,
				context.schema.getLayerId(), function(){
			
			context.startDownloadingNext();
			
		}, function(e){
			
			var msg = "Unable to remove " + key 
				+ " from failed_sync - " + JSON.stringify(e);
			
			onFailure(Arbiter.Error.Sync.ARBITER_ERROR);
		});
	};
	
    //only download if we don't have it
    this.mediaDir.getFile(media, {create: false, exclusive: false},
        function(fileEntry) {
    		
    		onSuccess();
        }, function(error) {
        	if(error.code === FileError.NOT_FOUND_ERR){
        		
                var fileTransfer = new FileTransfer();
                
                var isFinished = false;
                
                var progressListener = new Arbiter.MediaProgressListener(fileTransfer,
                		function(){
                	
                	if(isFinished === false){
                		fileTransfer.abort();
                	}
                	
                	onFailure(Arbiter.Error.Sync.TIMED_OUT);
                	
                });
                
                progressListener.watchProgress();
                
                var uri = encodeURI(context.url + media + '/download');
                
                var options = {};
                
                if(Arbiter.Util.existsAndNotNull(context.header)){
                	options.headers = context.header;
                }
                
                fileTransfer.download(uri, context.mediaDir.toURL() + "/" + media, function(result) {
                        console.log("download complete: " + result.toURL());
                        
                        isFinished = true;
                        
                        progressListener.stopWatching();
                        
                        onSuccess();
                        
                    }, function(transferError) {
                        console.log("download error source " + transferError.source);
                        console.log("download error target " + transferError.target);
                        console.log("download error code" + transferError.code);
                        
                        isFinished = true;
                        
                        progressListener.stopWatching();
                        
                        if(transferError.code !== FileTransferError.ABORT_ERR){
                        	onFailure(Arbiter.Error.Sync.UNKNOWN_ERROR);
                        }
                    }, undefined, options);
        	}else{
        		onFailure(Arbiter.Error.Sync.ARBITER_ERROR);
        	}
        }
    );
};
