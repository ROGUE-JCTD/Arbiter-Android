Arbiter.MediaDownloaderHelper = function(feature, schema,
		_header, _url, _mediaDir){
	
	this.mediaDir = _mediaDir;
	this.header = _header;
	this.url = _url;
	
	this.failedMedia = [];
	
	var mediaAttribute = feature[schema.getMediaColumn()];
	
	this.featureMedia = [];
	
    if(mediaAttribute !== null && mediaAttribute !== undefined) {
        this.featureMedia = JSON.parse(mediaAttribute);
    }
    
    this.onDownloadComplete = null;
};

Arbiter.MediaDownloaderHelper.prototype.startDownload = function(onSuccess){
	
	this.onDownloadComplete = onSuccess;
	
	this.startDownloadingNext();
};

Arbiter.MediaDownloaderHelper.prototype.addToFailedMedia = function(_failed, _error){
	if(_failed !== null && _failed !== undefined){
		this.failedMedia.push({
			media: _failed,
			error: _error
		});
	}
};

Arbiter.MediaDownloaderHelper.prototype.startDownloadingNext = function(){
	
	var media = this.featureMedia.shift();
	
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
	
    //only download if we don't have it
    this.mediaDir.getFile(media, {create: false, exclusive: false},
        function(fileEntry) {
    		
    		context.startDownloadingNext();
        }, function(error) {
        	if(error.code === FileError.NOT_FOUND_ERR){
        		
                var fileTransfer = new FileTransfer();
                var uri = encodeURI(context.url + media);
                fileTransfer.download(uri, context.mediaDir.fullPath + "/" + media, function(result) {
                        console.log("download complete: " + result.fullPath);
                        
                        context.startDownloadingNext();
                        
                    }, function(transferError) {
                        console.log("download error source " + transferError.source);
                        console.log("download error target " + transferError.target);
                        console.log("download error code" + transferError.code);
                        
                        onFailure(transferError);
                    }, undefined, {
                            headers: context.header
                    });
        	}else{
        		onFailure(error);
        	}
        }
    );
};