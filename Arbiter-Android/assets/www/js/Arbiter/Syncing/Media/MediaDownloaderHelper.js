Arbiter.MediaDownloaderHelper = function(feature, schema,
		_header, _url, _mediaDir){
	
	this.mediaDir = _mediaDir;
	this.header = _header;
	this.url = _url;
	
	this.failedMedia = null;
	
	var mediaAttribute = feature[schema.getMediaColumn()];
	
	this.featureMedia = [];
	this.index = -1;
    if(mediaAttribute !== null && mediaAttribute !== undefined) {
        this.featureMedia = JSON.parse(mediaAttribute);
    }
    
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
			this.onDownloadComplete(this.failedMedia);
		}
	}
};

Arbiter.MediaDownloaderHelper.prototype.downloadNext = function(media){
	var context = this;
	
	var onFailure = function(error){
		
		context.addToFailedMedia(media, error);
		
		context.startDownloadingNext();
	};
	
	var onSuccess = function(){
		
		var key = media;
		
		var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.MEDIA;
		
		var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.DOWNLOAD;
		
		Arbiter.FailedSyncHelper.remove(key, dataType, syncType, function(){
			
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
                fileTransfer.download(uri, context.mediaDir.fullPath + "/" + media, function(result) {
                        console.log("download complete: " + result.fullPath);
                        
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
                    }, undefined, {
                            headers: context.header
                    });
        	}else{
        		onFailure(error);
        	}
        }
    );
};