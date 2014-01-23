Arbiter.ReattemptFailedVectorUploads = function(_failedVectorUploads, _onSuccess, _onFailure){
	
	Arbiter.ReattemptFailed.call(this, _failedVectorUploads, _onSuccess, _onFailure);
	
	this.dataType = Arbiter.FailedSyncHelper.DATA_TYPES.VECTOR;
	this.syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.UPLOAD;
	
	this.finishedLayerCount = 0;
	this.totalLayerCount = _failedVectorUploads.length;
};

Arbiter.ReattemptFailedVectorUploads.prototype = new Arbiter.ReattemptFailed();

Arbiter.ReattemptFailedVectorUploads.constructor = Arbiter.ReattemptFailedVectorUploads;

Arbiter.ReattemptFailedVectorUploads.prototype.attempt = function(failedItem){
	
	var context = this;
	
	var olLayer = Arbiter.Layers.getLayerById(failedItem[Arbiter.FailedSyncHelper.LAYER_ID]);
	
	var key = failedItem;
	
	var callback = function(){
		
		Arbiter.Cordova.updateUploadingVectorDataProgress(
				++context.finishedLayerCount, context.totalLayerCount);
		
		context.attemptNext();
	};
	
	var vectorUploader = new Arbiter.VectorUploader(olLayer, function(){
		
		Arbiter.FailedSyncHelper.remove(key, context.dataType,
				context.syncType, key, function(){
			
			callback();
		}, function(e){
			
			console.log("Could not remove key = " + key 
					+ ", dataType = " + dataType 
					+ ", syncType = " + syncType
					+ " - " + JSON.stringify(e));
			
			callback();
		});
	}, function(featureType){
		
		context.addToFailedAttempts(failedItem);
		
		context.callback();
	});
	
	vectorUploader.upload();
};