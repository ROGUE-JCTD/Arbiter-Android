Arbiter.StoreMediaToUpload = function(_mediaToUpload, _onSuccess){
	
	this.mediaToUpload = _mediaToUpload;
	
	this.onSuccess = _onSuccess;
	
	this.layerIds = this.getLayerIds(this.mediaToUpload);
	
	this.index = -1;
	
	this.failedToStore = null;
};

Arbiter.StoreMediaToUpload.prototype.getLayerIds = function(mediaToUpload){
	
	var layerIds = [];
	
	if(!Arbiter.Util.existsAndNotNull(this.mediaToUpload)){
		return layerIds;
	}
	
	for(var key in mediaToUpload){
		layerIds.push(key);
	}
	
	return layerIds;
};

Arbiter.StoreMediaToUpload.prototype.pop = function(){
	
	if(++this.index < this.layerIds.length){
		var id = this.layerIds[this.index];
		
		var media = this.mediaToUpload[id];
		
		return {
			id: id,
			media: media
		};
	}
	
	return undefined;
};

Arbiter.StoreMediaToUpload.prototype.finished = function(){
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		this.onSuccess(this.failedToStore);
	}
};

Arbiter.StoreMediaToUpload.prototype.addFailedToStore = function(key, failed){
	
	if(Arbiter.Util.existsAndNotNull(failed) 
			&& Arbiter.Util.existsAndNotNull(key)){
		
		if(!Arbiter.Util.existsAndNotNull(this.failedToStore)){
			
			this.failedToStore = {};
		}
		
		this.failedToStore[key] = failed;
	}
};

Arbiter.StoreMediaToUpload.prototype.startStore = function(){
	this.storeNext();
};

Arbiter.StoreMediaToUpload.prototype.storeNext = function(){
	
	var obj = this.pop();
	
	if(obj !== undefined){
		this.store(obj);
	}else{
		this.finished();
	}
};

Arbiter.StoreMediaToUpload.prototype.store = function(obj){
	var context = this;
	
	var storeMedia = new Arbiter.StoreMediaToUploadForLayer(obj.id, obj.media, function(failed){
		
		if(Arbiter.Util.existsAndNotNull(failed)){
			context.addFailedToStore(obj.id, obj.media);
		}
		
		context.storeNext();
	});
	
	storeMedia.startStore();
};
