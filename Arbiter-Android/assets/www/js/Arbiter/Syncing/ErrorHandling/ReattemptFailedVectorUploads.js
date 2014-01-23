Arbiter.ReattemptFailedVectorUploads = function(_failedVectorUploads, _onSuccess, _onFailure){
	
	Arbiter.ReattemptFailed.call(this, _failedVectorUploads, _onSuccess, _onFailure);
	
	this.dataType = Arbiter.FailedSyncHelper.DATA_TYPES.VECTOR;
	this.syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.UPLOAD;
};

Arbiter.ReattemptFailedVectorUploads.prototype = new Arbiter.ReattemptFailed();

Arbiter.ReattemptFailedVectorUploads.constructor = Arbiter.ReattemptFailedVectorUploads;

Arbiter.ReattemptFailedVectorUploads.prototype.attempt = function(failedItem){
	
	var context = this;
	
	var olLayer = Arbiter.Layers.getLayerById(failedItem[Arbiter.FailedSyncHelper.LAYER_ID]);
	
	var key = failedItem;
	
	var vectorUploader = new Arbiter.VectorUploader(olLayer, function(){
		
		Arbiter.FailedSyncHelper.remove(key, context.dataType,
				context.syncType, key, function(){
			
			context.attemptNext();
		}, function(e){
			
			console.log("Could not remove key = " + key 
					+ ", dataType = " + dataType 
					+ ", syncType = " + syncType
					+ " - " + JSON.stringify(e));
			
			context.attemptNext();
		});
	}, function(featureType){
		
		context.addToFailedAttempts(failedItem);
		
		context.attemptNext();
	});
	
	vectorUploader.upload();
};