Arbiter.StoreFeaturesMediaToDownload = function(_schema, _features, _onSuccess, _onFailure){
	this.schema = _schema;
	this.features = _features;
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	this.failedToStore = null;
	this.index = -1;
};

Arbiter.StoreFeaturesMediaToDownload.prototype.allStorageComplete = function(){
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		this.onSuccess(this.failedToStore);
	}
};

Arbiter.StoreFeaturesMediaToDownload.prototype.startStoring = function(){
	
	this.storeNext();
};

Arbiter.StoreFeaturesMediaToDownload.prototype.pop = function(){
	
	if(++this.index < this.features.length){
		return this.features[this.index];
	}
	
	return undefined;
};

Arbiter.StoreFeaturesMediaToDownload.prototype.storeNext = function(){
	
	var feature = this.pop();
	
	if(feature !== undefined){
		
		this.store(feature);
	}else{
		console.log("storeFeaturesMedia storeNext feature is undefined");
		this.allStorageComplete();
	}
};

Arbiter.StoreFeaturesMediaToDownload.prototype.addToFailedToStore = function(id, failed){
	
	if(Arbiter.Util.existsAndNotNull(failed) && Arbiter.Util.existsAndNotNull(id)){
		
		if(!Arbiter.Util.existsAndNotNull(this.failedToStore)){
			this.failedToStore = {};
		}
		
		this.failedToStore[id] = failed;
	}
};

Arbiter.StoreFeaturesMediaToDownload.prototype.store = function(feature){
	var context = this;
	
	var storeMedia = new Arbiter.StoreMediaFromFeature(feature, this.schema, function(id, failed){
		
		console.log("store media from feature success: " + id + ", " + failed);
		context.addToFailedToStore(id, failed);
		
		context.storeNext();
	});
	
	storeMedia.startStoring();
};