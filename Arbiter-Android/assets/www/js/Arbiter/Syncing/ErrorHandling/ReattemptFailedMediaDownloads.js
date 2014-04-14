Arbiter.ReattemptFailedMediaDownloads = function(_failedMediaUploads, _onSuccess, _onFailure){
	
	Arbiter.ReattemptFailed.call(this, _failedMediaUploads, _onSuccess, _onFailure);
	
	this.dataType = Arbiter.FailedSyncHelper.DATA_TYPES.MEDIA;
	this.syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.DOWNLOAD;
	this.mediaDir = null;
	
	this.finishedMediaCount = 0;
	this.totalMediaCount = _failedMediaUploads.length;
};

Arbiter.ReattemptFailedMediaDownloads.prototype = new Arbiter.ReattemptFailed();

Arbiter.ReattemptFailedMediaDownloads.constructor = Arbiter.ReattemptFailedMediaDownloads;

Arbiter.ReattemptFailedMediaDownloads.prototype.startAttempts = function(){
	
	var context = this;
	
	Arbiter.FileSystem.ensureMediaDirectoryExists(function(_mediaDir){
		
		context.mediaDir = _mediaDir;
		
		Arbiter.ReattemptFailed.prototype.startAttempts.call(context);
	}, function(e){
		
		context.onFailure("Could not get the media directory - " + JSON.stringify(e));
	});
};

Arbiter.ReattemptFailedMediaDownloads.prototype.attempt = function(failedItem){
	
	var context = this;
	
	var layerId = failedItem[Arbiter.FailedSyncHelper.LAYER_ID];
	
	var schema = Arbiter.getLayerSchemas()[layerId];
	
	if(!Arbiter.Util.existsAndNotNull(schema)){
		throw "Schema should not be " + JSON.stringify(schema);
	}
	
	var server = Arbiter.Util.Servers.getServer(schema.getServerId());
	
	if(!Arbiter.Util.existsAndNotNull(server)){
		throw "Server should not be " + JSON.stringify(server);
	}
	
	var credentials = Arbiter.Util.getEncodedCredentials(
			server.getUsername(), server.getPassword());
	
	var file = failedItem[Arbiter.FailedSyncHelper.KEY];
	
	var url = schema.getUrl();
	
	var finishedLayerCount = 0;
	var totalLayerCount = 0;
	
	var callback = function(){
		
		Arbiter.Cordova.updateMediaUploadingStatus(schema.getFeatureType(),
				++context.finishedMediaCount, context.totalMediaCount,
				finishedLayerCount, totalLayerCount);
		
		context.attemptNext();
	};
	
	this.upload(url, credentials, file, function(){
		
		Arbiter.FailedSyncHelper.remove(file, context.dataType,
				context.syncType, layerId, function(){
			
			callback();
		}, function(e){
			
			console.log("Could not remove key = " + file + ", syncType = "
					+ syncType + ", layerId = " + layerId + " - " 
					+ JSON.stringify(e));
			
			callback();
		});
	}, function(e){
		
		context.addToFailedAttempts(failedItem);
		
		context.attemptNext();
	});
};

Arbiter.ReattemptFailedMediaDownloads.prototype.download = function(url, credentials, 
		file, onSuccess, onFailure){
	
	//only download if we don't have it
    this.mediaDir.getFile(file, {create: false, exclusive: false},
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
                
                var uri = encodeURI(context.url + file);
                fileTransfer.download(uri, context.mediaDir.toURL() + "/" + file, function(result) {
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
                    }, undefined, {
                            headers: {
                            	Authorization: 'Basic ' + credentials	
                        	}
                    });
        	}else{
        		onFailure(error);
        	}
        }
    );
};