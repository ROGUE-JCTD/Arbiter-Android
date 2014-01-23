Arbiter.ReattemptFailedMediaUploads = function(_failedMediaUploads, _onSuccess, _onFailure){
	
	Arbiter.ReattemptFailed.call(this, _failedMediaUploads, _onSuccess, _onFailure);
	
	this.dataType = Arbiter.FailedSyncHelper.DATA_TYPES.MEDIA;
	this.syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.UPLOAD;
	this.mediaDir = null;
};

Arbiter.ReattemptFailedMediaUploads.prototype = new Arbiter.ReattemptFailed();

Arbiter.ReattemptFailedMediaUploads.constructor = Arbiter.ReattemptFailedMediaUploads;

Arbiter.ReattemptFailedMediaUploads.prototype.startAttempts = function(){
	
	var context = this;
	
	Arbiter.FileSystem.ensureMediaDirectoryExists(function(_mediaDir){
		
		context.mediaDir = _mediaDir;
		
		Arbiter.ReattemptFailed.prototype.startAttempts.call(context);
	}, function(e){
		
		context.onFailure("Could not get the media directory - " + JSON.stringify(e));
	});
};

Arbiter.ReattemptFailedMediaUploads.prototype.attempt = function(failedItem){
	
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
	
	this.upload(url, credentials, file, function(){
		
		Arbiter.FailedSyncHelper.remove(file, context.dataType,
				context.syncType, layerId, function(){
			
			context.attemptNext();
		}, function(e){
			
			console.log("Could not remove key = " + file + ", syncType = "
					+ syncType + ", layerId = " + layerId + " - " 
					+ JSON.stringify(e));
			
			context.attemptNext();
		});
	}, function(e){
		
		context.addToFailedAttempts(failedItem);
		
		context.attemptNext();
	});
};

Arbiter.ReattemptFailedMediaUploads.prototype.upload = function(url, credentials, 
		file, onSuccess, onFailure){
	
	this.mediaDir.getFile(file, {create: false, exclusive: false}, function(fileEntry) {
		
        var options = new FileUploadOptions();
        options.fileKey="file";
        options.fileName=fileEntry.name;
        options.mimeType="image/jpeg";
        options.headers= {
        	Authorization: 'Basic ' + credentials	
    	};
                                
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
        
        ft.upload(fileEntry.fullPath, encodeURI(url), function(response) {
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
        console.log("Unable to transfer " + file 
        		+ ": File not found locally.", file, error.code);
        
        onFailure(error);
    });
};