Arbiter.MediaProgressListener = function(_fileTransfer, _notMakingProgress){
	this.lastChecked = 0;
	this.progress = 0;
	this.fileTransfer = _fileTransfer;
	this.notMakingProgress = _notMakingProgress;
	
	var context = this;
	
	this.fileTransfer.onprogress = function(progressEvent){
		context.progress++;
	};
	
	this.watcher = null;
};

Arbiter.MediaProgressListener.prototype.stopWatching = function(){
	if(Arbiter.Util.existsAndNotNull(this.watcher)){
		window.clearTimeout(this.watcher);
		this.watcher = null;
	}
};

Arbiter.MediaProgressListener.prototype.watchProgress = function(){
	var context = this;
	
	this.watcher = window.setTimeout(function(){
		
		if(context.isMakingProgress()){
			context.watchProgress();
		}else{
			context.notMakingProgress();
		}
	}, 30000);
};

Arbiter.MediaProgressListener.prototype.isMakingProgress = function(){
	var makingProgress = this.progress !== this.lastChecked;
	
	this.lastChecked = this.progress;
	
	return makingProgress;
};

