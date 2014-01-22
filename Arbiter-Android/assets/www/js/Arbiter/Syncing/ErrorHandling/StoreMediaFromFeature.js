Arbiter.StoreMediaFromFeature = function(_feature, _schema, _onSuccess){
	this.feature = _feature;
	this.schema = _schema;
	this.index = -1;
	this.onSuccess = _onSuccess;
	this.failedToStore = null;
	
	console.log("feature: " + JSON.stringify(_feature));
	
	console.log("schema mediaColumn = " + this.schema.getMediaColumn());
	
	this.featureMedia = this.getMediaFromOlFeature(this.feature);
	
	console.log("featureMedia ", this.featureMedia);
};

Arbiter.StoreMediaFromFeature.prototype.getMediaFromOlFeature = function(olFeature){
	
	return olFeature.attributes[this.schema.getMediaColumn()];
};

Arbiter.StoreMediaFromFeature.prototype.storeComplete = function(){
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		this.onSuccess(this.failedToStore);
	}
};

Arbiter.StoreMediaFromFeature.prototype.pop = function(){
	
	if(++this.index < this.featureMedia.length){
		return this.featureMedia[this.index];
	}
	
	return undefined;
};

Arbiter.StoreMediaFromFeature.prototype.startStoring = function(){
	
	this.storeNext();
};

Arbiter.StoreMediaFromFeature.prototype.storeNext = function(){
	
	var mediaFile = this.pop();
	
	if(mediaFile !== undefined){
		
		console.log("store media from features storeNext");
		
		this.store(mediaFile);
	}else{
		
		console.log("media is undefined");
		this.storeComplete();
	}
};

Arbiter.StoreMediaFromFeature.prototype.addFailedToStore = function(failed){
	
	if(Arbiter.Util.existsAndNotNull(failed)){
		
		if(Arbiter.Util.existsAndNotNull(this.failedToStore)){
			this.failedToStore = [];
		}
		
		this.failedToStore.push(this.feature[Arbiter.FeatureTableHelper.ID], failed);
	}
};

Arbiter.StoreMediaFromFeature.prototype.store = function(mediaFile){
	
	var dataType = Arbiter.FailedSyncHelper.DATA_TYPES.MEDIA;
	var syncType = Arbiter.FailedSyncHelper.SYNC_TYPES.DOWNLOAD;
	
	var key = mediaFile;
	
	var context = this;
	
	console.log("storeMediaFromFeature store failed_sync");
	
	Arbiter.FailedSyncHelper.insert(key, dataType, syncType, function(){
		
		console.log("inserted " + key + " into failed_sync");
		context.storeNext();
	}, function(e){
		
		console.log("couldn't insert into failed_sync - " + JSON.Stringify(e));
		context.addFailedToStore(mediaFile);
		
		context.storeNext();
	});
};

