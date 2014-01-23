Arbiter.ReattemptFailedVectorDownloads = function(_failedVectorDownloads, _onSuccess, _onFailure){
	
	Arbiter.ReattemptFailed.call(this, _failedVectorDownloads, _onSuccess, _onFailure);
	
	this.dataType = Arbiter.FailedSyncHelper.DATA_TYPES.VECTOR;
	this.syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.DOWNLOAD;
	this.bounds = null;
};

Arbiter.ReattemptFailedVectorDownloads.prototype = new Arbiter.ReattemptFailed();

Arbiter.ReattemptFailedVectorDownloads.constructor = Arbiter.ReattemptFailedVectorDownloads;

Arbiter.ReattemptFailedVectorDownloads.prototype.startAttempts = function(){
	
	var context = this;
	
	Arbiter.PreferencesHelper.get(Arbiter.AOI, this, function(_aoi){
		
		if(!Arbiter.Util.existsAndNotNull(_aoi)){
			context.onFailure("Area of interest is not set!");
			
			return;
		}
		
		var aoi = _aoi.split(',');
		
		context.bounds = new Arbiter.Util.Bounds(aoi[0],
				aoi[1], aoi[2], aoi[3]);
		
		Arbiter.ReattemptFailed.prototype.startAttempts.call(context);
	}, function(e){
		
		context.onFailure("Could not get the area of interest - " + JSON.stringify(e));
	});
};

Arbiter.ReattemptFailedVectorDownloads.prototype.attempt = function(failedItem){
	
	var context = this;
	
	var layerId = failedItem[Arbiter.FailedSyncHelper.LAYER_ID];
	
	var schema = Arbiter.getLayerSchemas()[layerId];
	
	if(!Arbiter.Util.existsAndNotNull(schema)){
		throw "Schema should not be " + JSON.stringify(schema);
	}
	
	var key = failedItem;
	
	var vectorDownloader = new Arbiter.VectorDownloader(schema, this.bounds, function(){
		
		Arbiter.FailedSyncHelper.remove(key, context.dataType,
				context.syncType, layerId, function(){
			
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
	
	vectorDownloader.download();
};