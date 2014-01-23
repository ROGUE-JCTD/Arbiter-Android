Arbiter.ReattemptSync = function(_failedVectorUploads, _failedVectorDownloads,
		_failedMediaUploads, _failedMediaDownloads, _onSuccess, _onFailure){
	
	this.failedVectorUploads = _failedVectorUploads;
	this.failedVectorDownloads = _failedVectorDownloads;
	this.failedMediaUploads = _failedMediaUploads;
	this.failedMediaDownloads = _failedMediaDownloads;
	
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	
	this.failedAttempts = null;
};

Arbiter.ReattemptSync.prototype.VECTOR_UPLOADS = 0;
Arbiter.ReattemptSync.prototype.VECTOR_DOWNLOADS = 1;
Arbiter.ReattemptSync.prototype.MEDIA_UPLOADS = 2;
Arbiter.ReattemptSync.prototype.MEDIA_DOWNLOADS = 3;

Arbiter.ReattemptSync.prototype.startReattempt = function(){
	
	this.reattemptFailedVectorUploads();
};

Arbiter.ReattemptSync.prototype.reattemptFailedVectorUploads = function(){
	
	var context = this;
	
	console.log("starting reattempts");
	
	var reattempt = new Arbiter.ReattemptFailedVectorUploads(
			this.failedVectorUploads, function(failed){
		
		console.log("Successfully synced failed vector uploads");
		
		context.addToFailedAttempts(context.VECTOR_UPLOADS, failed);
		
		context.reattemptFailedVectorDownloads();
	}, function(e){
		
		console.log(JSON.stringify(e));
		
		context.reattemptFailedVectorDownloads();
	});
};

Arbiter.ReattemptSync.prototype.reattemptFailedVectorDownloads = function(){
	var context = this;
	
	console.log("starting reattempts vector downloads");
	
	var reattempt = new Arbiter.ReattemptFailedVectorDownloads(
			this.failedVectorDownloads, function(failed){
		
		console.log("Successfully synced failed vector downloads");
		
		context.addToFailedAttempts(context.VECTOR_DOWNLOADS, failed);
		
		context.reattemptFailedMediaUploads();
	}, function(e){
		
		console.log(JSON.stringify(e));
		
		context.reattemptFailedMediaUploads();
	});
};

Arbiter.ReattemptSync.prototype.reattemptFailedMediaUploads = function(){
	var context = this;
	
	console.log("starting reattempts media uploads");
	
	var reattempt = new Arbiter.ReattemptFailedMediaUploads(
			this.failedMediaUploads, function(failed){
		
		console.log("Successfully synced failed media uploads");
		
		context.addToFailedAttempts(context.MEDIA_UPLOADS, failed);
		
		context.reattemptFailedMediaDownloads();
	}, function(e){
		
		console.log(JSON.stringify(e));
		
		context.reattemptFailedMediaDownloads();
	});
};

Arbiter.ReattemptSync.prototype.reattemptFailedMediaDownloads = function(){
	var context = this;
	
	console.log("starting reattempts");
	
	var reattempt = new Arbiter.ReattemptFailedMediaDownloads(
			this.failedMediaDownloads, function(failed){
		
		console.log("Successfully synced failed vector uploads");
		
		context.addToFailedAttempts(context.MEDIA_DOWNLOADS, failed);
		
		context.onCompletedAttempts();
	}, function(e){
		
		console.log(JSON.stringify(e));
		
		context.onCompletedAttempts();
	});
};

Arbiter.ReattemptSync.prototype.addToFailedAttempts = function(key, failed){
	
	if(Arbiter.Util.existsAndNotNull(failed)){
		
		if(!Arbiter.Util.existsAndNotNull(this.failedAttempts)){
			this.failedAttempts = {};
		}
		
		this.failedAttempts[key] = failed;
	}
};

Arbiter.ReattemptSync.prototype.onCompletedAttempts = function(){
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		this.onSuccess(this.failedAttempts);
	}
};