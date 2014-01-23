Arbiter.MediaUploadCounter = function(_mediaToSend){
	this.mediaToSend = _mediaToSend;
};

Arbiter.MediaUploadCounter.prototype.getCount = function(){
	var count = 0;
	
	return this.mediaToSend.length;
};