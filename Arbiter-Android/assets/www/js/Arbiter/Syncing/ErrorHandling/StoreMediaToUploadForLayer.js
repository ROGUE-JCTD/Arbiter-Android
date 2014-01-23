Arbiter.StoreMediaToUploadForLayer = function(_layerId, _mediaArray, _onFinish){
	
	this.onFinish = _onFinish;
	this.layerId = _layerId;
	this.mediaArray = _mediaArray;
	this.failedMedia = null;
	
	this.index = -1;
	
	if(!Arbiter.Util.existsAndNotNull(this.mediaArray)){
		this.onCompleted();
	}
};

Arbiter.StoreMediaToUploadForLayer.prototype.pop = function(){
	
	if(++this.index < this.mediaArray.length){
		
		return this.mediaArray[this.index];
	}
	
	return undefined;
};

Arbiter.StoreMediaToUploadForLayer.prototype.onCompleted = function(){
	
	if(Arbiter.Util.existsAndNotNull(this.onFinish)){
		this.onFinish(this.failedMedia);
	}
};

Arbiter.StoreMediaToUploadForLayer.prototype.addToFailedMedia = function(failed){
	
	if(Arbiter.Util.existsAndNotNull(failed)){
		
		if(!Arbiter.Util.existsAndNotNull(this.failedMedia)){
			this.failedMedia = [];
		}
		
		this.failedMedia.push(failed);
	}
};

Arbiter.StoreMediaToUploadForLayer.prototype.startStore = function(){
	this.storeNext();
};

Arbiter.StoreMediaToUploadForLayer.prototype.storeNext = function(){
	
	var media = this.pop();
	
	if(media !== undefined){
		
		this.store(media);
	}else{
		this.onCompleted();
	}
};

Arbiter.StoreMediaToUploadForLayer.prototype.store = function(media){
	
	var context = this;
	
	var key = media;
	var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.MEDIA;
	var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.UPLOAD;
	
	Arbiter.FailedSyncHelper.insert(key, dataType, syncType, this.layerId, function(){
		
		context.storeNext();
	}, function(e){
		
		console.log("Couldn't store " + media 
				+ " in failed_sync - " + JSON.stringify(e));
		
		context.addToFailedMedia(failed);
		
		context.storeNext();
	});
};
