Arbiter.MediaUploader = function(_schema, _mediaToSend, _server, _mediaDir, _finishedLayerCount, _totalLayerCount){
	
	this.schema = _schema;
	this.mediaToSend = _mediaToSend;
	this.media = this.mediaToSend[this.schema.getLayerId()];
	
	this.server = _server;
	this.mediaDir = _mediaDir;
	this.failedMedia = null;
	this.finishedLayerCount = _finishedLayerCount;
	this.totalLayerCount = _totalLayerCount;
	
	var credentials = Arbiter.Util.getEncodedCredentials(
			this.server.getUsername(), 
			this.server.getPassword());
	
    this.url = Arbiter.Util.getFileServiceURL(this.server.getUrl()) + "upload";
    
	this.header = {
		Authorization: 'Basic ' + credentials	
	};
	
	this.onUploadSuccess = null;
	
	// Start at -1 to account for the first upload
	this.finishedMediaCount = 0;
	this.totalMediaCount = 0;
};

Arbiter.MediaUploader.prototype.pop = function(){
	
	return this.media.shift();
};

Arbiter.MediaUploader.prototype.startUpload = function(onSuccess){
	this.onUploadSuccess = onSuccess;
	
	var mediaUploadCounter = new Arbiter.MediaUploadCounter(this.media);
	
	this.totalMediaCount = mediaUploadCounter.getCount();
	
	if(this.totalMediaCount === 0){
		
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

Arbiter.MediaUploader.prototype.updateProgressDialog = function(isMedia){
	
	// If there is no more media, then increment the layer count
	if(this.media.length === 0){
		this.finishedLayerCount++;
	}
	
	if(isMedia === true){
		this.finishedMediaCount++;
	}
	
	Arbiter.Cordova.updateMediaUploadingStatus(
			this.schema.getFeatureType(), 
			this.finishedMediaCount,
			this.totalMediaCount, 
			this.finishedLayerCount,
			this.totalLayerCount);
};

Arbiter.MediaUploader.prototype.updateMediaToSend = function(onSuccess, onFailure){
	
	if(this.media.length === 0){
		delete this.mediaToSend[this.schema.getLayerId()];
	}
	
	Arbiter.PreferencesHelper.put(Arbiter.MEDIA_TO_SEND,
			JSON.stringify(this.mediaToSend),
			this, function(){
		
		if(Arbiter.Util.funcExists(onSuccess)){
			onSuccess();
		}
	}, function(e){
		
		if(Arbiter.Util.funcExists(onFailure)){
			onFailure(e);
		}
	});
};

Arbiter.MediaUploader.prototype.uploadNext = function(next){
	
	var context = this;
	
	var callback = function(){
		
		context.updateProgressDialog(true);
		
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
		
		context.updateMediaToSend(function(){
			
			callback();
			
		}, function(e){
			
			var msg = "Could not remove update " 
				+ Arbiter.MEDIA_TO_SEND + " - " 
				+ JSON.stringify(e);
			
			onFailure(msg);
		});
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
        
        ft.upload(fileEntry.toURL(), encodeURI(context.url), function(response) {
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

