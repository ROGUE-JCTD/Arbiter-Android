Arbiter.MediaDownloadCounter = function(schema, _features){
	this.mediaColumn = schema.getMediaColumn();
	
	// Don't modify this array here.
	this.features = _features;
};

Arbiter.MediaDownloadCounter.prototype.getCount = function(){
	
	var count = 0;
	var feature = null;
	
	for(var i = 0; i < this.features.length; i++){
		feature = this.features[i];
		
		count += this.getMediaCount(feature);
	}
	
	return count;
};

Arbiter.MediaDownloadCounter.prototype.getMediaCount = function(feature){
	
	var context = this;
	
	var mediaAttribute = feature[context.mediaColumn];
	
	var featureMedia = null;
	
	if(mediaAttribute !== null && mediaAttribute !== undefined){
		featureMedia = JSON.parse(mediaAttribute);
		
		console.log(mediaAttribute);
		
		return featureMedia.length;
	}else{
		return 0;
	}
};

