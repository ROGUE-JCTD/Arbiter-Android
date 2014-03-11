Arbiter.SchemaDownloader = function(_layers, _wfsVersion, _onSuccess, _onFailure){
	this.layers = _layers;
	this.wfsVersion = _wfsVersion;
	
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	this.index = -1;
	this.failedLayers = [];
	
	this.queuedCount = this.layers.length;
	this.finishedCount = 0;
};

Arbiter.SchemaDownloader.prototype.pop = function(){
	return this.layers[++this.index];
};

Arbiter.SchemaDownloader.prototype.onDownloadComplete = function(){
	
	if(this.queuedCount > 0){
		Arbiter.Cordova.dismissDownloadingSchemasProgress();
	}
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		
		this.onSuccess(this.failedLayers);
	}
};

Arbiter.SchemaDownloader.prototype.startDownload = function(){
	
	if(this.queuedCount > 0){
		Arbiter.Cordova.showDownloadingSchemasProgress(this.queuedCount);
	}
	
	this.startNextDownload();
};

Arbiter.SchemaDownloader.prototype.startNextDownload = function(){
	
	var layer = this.pop();
	
	if(layer !== undefined){
		
		this.download(layer);
	}else{
		
		this.onDownloadComplete();
	}
};

Arbiter.SchemaDownloader.prototype.download = function(layer){
	var context = this;
	
	var callback = function(){
		
		Arbiter.Cordova.updateDownloadingSchemasProgress(
				++context.finishedCount, context.queuedCount);
		
		context.startNextDownload();
	};
	
	var downloaderHelper = new Arbiter.SchemaDownloaderHelper(layer, this.wfsVersion, function(){
		
		callback();
	}, function(featureType){
		
		context.failedLayers.push(featureType);
		
		callback();
	});
	
	downloaderHelper.downloadSchema();
};