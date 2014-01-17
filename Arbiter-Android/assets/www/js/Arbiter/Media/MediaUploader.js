Arbiter.MediaUploader = function(_schema, _media, _server, _mediaDir, 
		_finishedLayersUploading, _queuedLayersUploading){
	
	this.schema = _schema;
	this.media = _media;
	this.server = _server;
	this.mediaDir = _mediaDir;
	this.failedMedia = [];
	this.finishedLayersUploading = _finishedLayersUploading;
	this.queuedLayersUploading = _queuedLayersUploading;
	
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
	this.finishedCount = -1;
	
	this.queuedCount = 0;
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
	var next = this.media.shift();
	
	this.finishedCount++;
	
	Arbiter.Cordova.updateMediaUploadingStatus(
			this.schema.getFeatureType(),
			this.finishedCount,
			this.queuedCount,
			this.finishedLayersUploading,
			this.queuedLayersUploading);
	
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
		this.failedMedia.push({
			media: _failed,
			error: _error
		});
	}
};

Arbiter.MediaUploader.prototype.uploadNext = function(next){
	var context = this;
	
	var onFailure = function(error){
		context.addToFailedMedia(next, error);
        
        context.startUploadingNext();
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
        ft.upload(fileEntry.fullPath, encodeURI(context.url), function(response) {
            console.log("Code = " + response.responseCode);
            console.log("Response = " + response.response);
            console.log("Sent = " + response.bytesSent);
            
            context.startUploadingNext();
        }, function(error) {
            console.log("upload error source " + error.source);
            console.log("upload error target " + error.target);
            console.log("upload error code" + error.code);
            
            onFailure(error);
        }, options);
    }, function(error) {
        console.log("Unable to transfer " + next 
        		+ ": File not found locally.", next, error.code);
        
        onFailure(error);
    });
};

