(function(){
	
	Arbiter.MediaUploader = function(projectDb, _schema, _mediaToSend, _server, _mediaDir, _finishedLayerCount, _totalLayerCount){
		
		this.projectDb = projectDb;
		this.schema = _schema;
		this.mediaToSend = _mediaToSend;
		this.media = this.mediaToSend[this.schema.getLayerId()];
		
		this.server = _server;
		this.mediaDir = _mediaDir;
		this.finishedLayerCount = _finishedLayerCount;
		this.totalLayerCount = _totalLayerCount;
		
		var credentials = Arbiter.Util.getEncodedCredentials(
				this.server.getUsername(), 
				this.server.getPassword());
		
	    this.url = Arbiter.Util.getFileServiceUploadURL(this.server.getUrl());
	    
	    this.header = null;
	    
	    if(Arbiter.Util.existsAndNotNull(credentials)){
	    	
	    	this.header = {
	    		Authorization: 'Basic ' + credentials	
	    	};
	    }
		
		this.onUploadSuccess = null;
		this.onUploadFailure = null;
		
		// Start at -1 to account for the first upload
		this.finishedMediaCount = 0;
		this.totalMediaCount = 0;
	};

	var prototype = Arbiter.MediaUploader.prototype;
	
	prototype.pop = function(){
		
		return this.media.shift();
	};

	prototype.startUpload = function(onSuccess, onFailure){
		this.onUploadSuccess = onSuccess;
		this.onUploadFailure = onFailure;
		
		var mediaUploadCounter = new Arbiter.MediaUploadCounter(this.media);
		
		this.totalMediaCount = mediaUploadCounter.getCount();
		
		if(this.totalMediaCount === 0){
			
			if(Arbiter.Util.funcExists(this.onUploadSuccess)){
				this.onUploadSuccess();
			}
			
			return;
		}
		
		this.startUploadingNext();
	};

	prototype.startUploadingNext = function(){
		var next = this.pop();
		
		if(next !== undefined){
			
			this.uploadNext(next);
		}else{
			
			if(Arbiter.Util.funcExists(this.onUploadSuccess)){
				this.onUploadSuccess();
			}
		}
	};

	prototype.updateProgressDialog = function(isMedia){
		
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

	prototype.updateMediaToSend = function(onSuccess, onFailure){
		
		if(this.media.length === 0){
			delete this.mediaToSend[this.schema.getLayerId()];
		}
		
		Arbiter.PreferencesHelper.put(this.projectDb, Arbiter.MEDIA_TO_SEND,
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

	prototype.uploadNext = function(next){
		
		var context = this;
		
		var callback = function(){
			
			context.updateProgressDialog(true);
			
			context.startUploadingNext();
		};
		
		var onFailure = function(e){
	        
			if(e === Arbiter.Error.Sync.TIMED_OUT){
				
				Arbiter.Cordova.syncOperationTimedOut(callback, function(){
					
					if(Arbiter.Util.existsAndNotNull(context.onUploadFailure)){
						context.onUploadFailure(Arbiter.Error.Sync.TIMED_OUT);
					}
				});
			}else{
				callback();
			}
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
	        
	        if(Arbiter.Util.existsAndNotNull(context.header)){
	        	
	        	options.headers = context.header;
	        }
	                                
	        var params = {};
	        
	        options.params = params;
	        
	        var ft = new FileTransfer();
	        
	        var isFinished = false;
	        
	        var progressListener = new Arbiter.MediaProgressListener(ft,
	        		function(){
	        	
	        	if(isFinished === false){
	        		ft.abort();
	        	}
	        	
	        	onFailure(Arbiter.Error.Sync.TIMED_OUT);
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
})();

