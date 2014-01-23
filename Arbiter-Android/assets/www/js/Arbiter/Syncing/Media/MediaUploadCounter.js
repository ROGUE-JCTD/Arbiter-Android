Arbiter.MediaUploadCounter = function(_mediaToSend){
	this.mediaToSend = _mediaToSend;
};

Arbiter.MediaUploadCounter.prototype.getCount = function(){
	var count = 0;
	
	var media = null;
	
	for(var key in this.mediaToSend){
		media = this.mediaToSend[key];
		
		if(Arbiter.Util.existsAndNotNull(media)){
			count += media.length;
		}
	}
	
	return count;
};