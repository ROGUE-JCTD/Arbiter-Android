Arbiter.MediaUploader = function(_schema, _media, _server, _mediaDir){
	
	this.schema = _schema;
	this.media = _media;
	this.server = _server;
	this.mediaDir = _mediaDir;
	this.failedMedia = null;
	
	var credentials = Arbiter.Util.getEncodedCredentials(
			this.server.getUsername(), 
			this.server.getPassword());
	
	this.url = this.server.getUrl();
	this.url = this.url.substring(0,
			this.url.length - 9);
	
    this.url += "file-service/upload";
    
	this.header = {
		Authorization: 'Basic ' + credentials	
	};
	
	this.onUploadSuccess = null;
	
	// Start at -1 to account for the first upload
	this.finishedCount = 0;
	
	this.queuedCount = 0;
	
	this.index = -1;
};

Arbiter.MediaUploader.prototype.pop = function(){
	
	if(++this.index < this.media.length){
		return this.media[this.index];
	}
	
	return undefined;
};

Arbiter.MediaUploader.prototype.startUpload = function(onSuccess){
	this.onUploadSuccess = onSuccess;
	
	var mediaUploadCounter = new Arbiter.MediaUploadCounter(this.media);
	
	this.queuedCount = mediaUploadCounter.getCount();
	
	if(this.queuedCount === 0){
		
		if(Arbiter.Util.funcExists(this.onUploadSuccess)){
			this.onUploadSuccess(this.failedMedia);
		}
		
		return;
	}
	
	this.startUploadingNext();
};

Arbiter.MediaUploader.prototype.startUploadingNext = function(){
	var next = this.pop();
	
	if(next !== undefined){
		
		this.uploadNext(next);
	}else{
		if(Arbiter.Util.funcExists(this.onUploadSuccess)){
			this.onUploadSuccess(this.failedMedia);
		}
	}
};

Arbiter.MediaUploader.prototype.addToFailedMedia = function(_failed, _error){
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

Arbiter.MediaUploader.prototype.uploadNext = function(next){
	var context = this;
	
	var callback = function(){
		
		Arbiter.Cordova.updateMediaUploadingStatus(
				context.schema.getFeatureType(),
				++context.finishedCount,
				context.queuedCount);
		
		context.startUploadingNext();
	};
	
	var onFailure = function(error){
		context.addToFailedMedia(next, error);
        
        callback();
	};
	
	var onSuccess = function(){
		
		var key = next;
		var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.MEDIA;
		var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.UPLOAD;
		
		Arbiter.FailedSyncHelper.remove(key, dataType, syncType, function(){
			
			callback();
		}, function(e){
			var msg = "Could not remove " + key 
				+ " from failed_sync - " + JSON.stringify(e);
			
			onFailure(msg);
		});
		
		callback();
	};
	
	this.mediaDir.getFile(next, {create: false, exclusive: false}, function(fileEntry) {
    			
        var options = new FileUploadOptions();
        options.fileKey="file";
        options.fileName=fileEntry.name;
        options.mimeType="image/jpeg";
        options.headers= context.header;
                                
        var params = {};
        
        options.params = params;
        
        var ft = new FileTransfer();
        
        var isFinished = false;
        
        var progressListener = new Arbiter.MediaProgressListener(ft,
        		function(){
        	
        	if(isFinished === false){
        		ft.abort();
        	}
        	
        	onFailure("Upload timed out");
        	
        });
        
        progressListener.watchProgress();
        
        ft.upload(fileEntry.fullPath, encodeURI(context.url), function(response) {
            console.log("Code = " + response.responseCode);
            console.log("Response = " + response.response);
            console.log("Sent = " + response.bytesSent);
            
            isFinished = true;
            
            progressListener.stopWatching();
            
            onSuccess();
        }, function(error) {
            console.log("upload error source " + error.source);
            console.log("upload error target " + error.target);
            console.log("upload error code" + error.code);
            
            isFinished = true;
            
            progressListener.stopWatching();
            
            if(error.code !== FileTransferError.ABORT_ERR){
            	onFailure(error);
            }
        }, options);
    }, function(error) {
        console.log("Unable to transfer " + next 
        		+ ": File not found locally.", next, error.code);
        
        onFailure(error);
    });
};

