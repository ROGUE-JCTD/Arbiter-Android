Arbiter.MediaProgressListener = function(_fileTransfer, _notMakingProgress, _isTransferFinished){
	this.lastChecked = 0;
	this.progress = 0;
	this.fileTransfer = _fileTransfer;
	this.notMakingProgress = _notMakingProgress;
	this.isTransferFinished = _isTransferFinished;
	
	var context = this;
	
	this.fileTransfer.onprogress = function(progressEvent){
		context.progress++;
	};
};

Arbiter.MediaProgressListener.prototype.watchProgress = function(){
	var context = this;
	
	window.setTimeout(function(){
		
		if(context.isMakingProgress()){
			context.watchProgress();
		}else{
			console.log("not making progress");
			context.notMakingProgress();
		}
	}, 30000);
};

Arbiter.MediaProgressListener.prototype.isMakingProgress = function(){
	var makingProgress = this.progress !== this.lastChecked;
	
	console.log("isMakingProgress = " + makingProgress);
	this.lastChecked = this.progress;
	
	return makingProgress;
};

