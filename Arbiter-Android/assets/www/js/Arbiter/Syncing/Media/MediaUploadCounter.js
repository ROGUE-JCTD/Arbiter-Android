Arbiter.MediaUploadCounter = function(_media){
	this.media = _media;
};

Arbiter.MediaUploadCounter.prototype.getCount = function(){
	
	return this.media.length;
};