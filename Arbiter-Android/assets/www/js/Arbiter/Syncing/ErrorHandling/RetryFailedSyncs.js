Arbiter.RetryFailedSyncs = function(){
	
	this.hasFailed = false;
	
	this.onHasFailedItems = null;
	
	this.failedVectorUploads = null;
	this.failedVectorDownloads = null;
	
	this.failedMediaUploads = null;
	this.failedMediaDownloads = null;
};

Arbiter.RetryFailedSyncs.prototype.hasFailedItems = function(_onHasFailedItems){
	
	this.onHasFailedItems = _onHasFailedItems;
	
	this.getFailedVectorUploads();
};

Arbiter.RetryFailedSyncs.prototype.initialized = function(){
	if(Arbiter.Util.funcExists(this.onHasFailedItems)){
		
		this.onHasFailedItems(this.hasFailed);
	}
};

Arbiter.RetryFailedSyncs.prototype.getFailedVectorUploads = function(){
	
	var context = this;
	
	var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.VECTOR;
	var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.UPLOAD;
	
	this.getFailed(dataType, syncType, function(arrayOfFailed){
		
		context.failedVectorUploads = arrayOfFailed;
		
		context.getFailedVectorDownloads();
	}, function(e){
		
		console.log(e);
		
		context.getFailedVectorDownloads();
	});
};

Arbiter.RetryFailedSyncs.prototype.getFailedVectorDownloads = function(){
	var context = this;
	
	var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.VECTOR;
	var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.DOWNLOAD;
	
	this.getFailed(dataType, syncType, function(arrayOfFailed){
		
		context.failedVectorDownloads = arrayOfFailed;
		
		context.getFailedMediaUploads();
	}, function(e){
		
		console.log(e);
		
		context.getFailedMediaUploads();
	});
};

Arbiter.RetryFailedSyncs.prototype.getFailedMediaUploads = function(){
	var context = this;
	
	var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.MEDIA;
	var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.UPLOAD;
	
	this.getFailed(dataType, syncType, function(arrayOfFailed){
		
		context.failedMediaUploads = arrayOfFailed;
		
		context.getFailedMediaDownloads();
	}, function(e){
		
		console.log(e);
		
		context.getFailedMediaDownloads();
	});
};

Arbiter.RetryFailedSyncs.prototype.getFailedMediaDownloads = function(){
	var context = this;
	
	var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.MEDIA;
	var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.DOWNLOAD;
	
	this.getFailed(dataType, syncType, function(arrayOfFailed){
		
		context.failedMediaDownloads = arrayOfFailed;
		
		
	}, function(e){
		
		console.log(e);
	});
};

Arbiter.RetryFailedSyncs.prototype.getFailed = function(dataType, syncType, onHasFailed, onFailure){
	var context = this;
	
	Arbiter.FailedSyncHelper.getFailedToSync(dataType, syncType, function(arrayOfFailed){
		
		if(arrayOfFailed.length > 0){
			context.hasFailed = true;
			
			if(Arbiter.Util.funcExists(onHasFailed)){
				onHasFailed(arrayOfFailed);
			}
		}
		
	}, function(e){
		var msg = "Error reading failed_sync for " + Arbiter.FailedSyncHelper.DATA_TYPE
			+ " = " + dataType + ", " + Arbiter.FailedSyncHelper.SYNC_TYPE
			+ " = " + syncType + " - " + JSON.stringify(e);
		
		if(Arbiter.Util.funcExists(onFailure)){
			onFailure(msg)
		}
	});
};

Arbiter.RetryFailedSyncs.prototype.reattemptFailed = function(onSuccess, onFailure){
	
	if(this.hasFailed){
		
		var reattemptSync = new Arbiter.ReattemptSync(
				this.failedVectorUploads, this.failedVectorDownloads,
				this.failedMediaUploads, this.failedMediaDownloads, function(failedAttempts){
				
			console.log("RetrySync completed successfully!");
			
			if(Arbiter.Util.existsAndNotNull(failedAttempts)){
				console.log("Failed attemps: " + JSON.stringify(failedAttempts));
			}
		}, function(e){
			
			console.log("RetrySync failed! - " + JSON.stringify(e));
		});
	}
};

