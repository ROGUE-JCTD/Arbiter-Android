Arbiter.MediaDownloaderHelper = function(feature,
		_schema, _header, _url, _mediaDir, 
		_finishedMediaCount, _totalMediaCount,
		_finishedFeatures, _totalFeatures,
		_finishedLayers, _totalLayers){
	
	this.mediaDir = _mediaDir;
	this.header = _header;
	this.url = _url;
	
	this.failedMedia = null;
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
};

Arbiter.MediaDownloaderHelper.prototype.pop = function(){
	
	if(++this.index < this.featureMedia.length){
		return this.featureMedia[this.index];
	}
	
	return undefined;
};

Arbiter.MediaDownloaderHelper.prototype.startDownload = function(onSuccess){
	
	this.onDownloadComplete = onSuccess;
	
	if(this.featureMedia.length === 0){
		this.updateProgressDialog(false);
	}
	
	this.startDownloadingNext();
};

Arbiter.MediaDownloaderHelper.prototype.addToFailedMedia = function(_failed, _error){
	
	if(_failed !== null && _failed !== undefined){
		
		if(this.failedMedia === null || this.failedMedia === undefined){
			this.failedMedia = [];
		}
		
		this.failedMedia.push({
			media: _failed,
			error: _error
		});
	}
};

Arbiter.MediaDownloaderHelper.prototype.startDownloadingNext = function(){
	
	var media = this.pop();
	
	if(media !== undefined){
		
		this.downloadNext(media);
	}else{
		
		if(Arbiter.Util.funcExists(this.onDownloadComplete)){
			this.onDownloadComplete(this.finishedMediaCount, this.failedMedia);
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
	
	var onFailure = function(error){
		
		context.updateProgressDialog(true);
		
		context.addToFailedMedia(media, error);
		
		context.startDownloadingNext();
	};
	
	var onSuccess = function(){
		
		var key = media;
		
		var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.MEDIA;
		
		var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.DOWNLOAD;
		
		context.updateProgressDialog(true);
		
		Arbiter.FailedSyncHelper.remove(key, dataType, syncType,
				context.schema.getLayerId(), function(){
			
			context.startDownloadingNext();
			
		}, function(e){
			
			var msg = "Unable to remove " + key 
				+ " from failed_sync - " + JSON.stringify(e);
			
			onFailure(msg);
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
                	
                	onFailure("Download timed out");
                	
                });
                
                progressListener.watchProgress();
                
                var uri = encodeURI(context.url + media);
                
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
                        	onFailure(transferError);
                        }
                    }, undefined, options);
        	}else{
        		onFailure(error);
        	}
        }
    );
};
