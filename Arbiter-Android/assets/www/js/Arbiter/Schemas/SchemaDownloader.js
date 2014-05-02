Arbiter.SchemaDownloader = function(_layers, _wfsVersion, _onSuccess, _onFailure){
	this.layers = _layers;
	this.wfsVersion = _wfsVersion;
	
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	this.index = -1;
	this.failedLayers = [];
	this.layersAlreadyInProject = [];
	
	this.queuedCount = this.layers.length;
	this.finishedCount = 0;
};

Arbiter.SchemaDownloader.prototype.pop = function(){
	var index = ++this.index;
	
	var layer = this.layers[index];
	
	var obj = null;
	
	if(Arbiter.Util.existsAndNotNull(layer)){
		obj = {
			layer: layer,
			index: index
		};
	}
	
	return obj;
};

Arbiter.SchemaDownloader.prototype.onDownloadComplete = function(){
	
	if(this.queuedCount > 0){
		Arbiter.Cordova.dismissDownloadingSchemasProgress();
	}
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		
		this.onSuccess(this.layersAlreadyInProject, this.failedLayers);
	}
};

Arbiter.SchemaDownloader.prototype.startDownload = function(){
	
	if(this.queuedCount > 0){
		Arbiter.Cordova.showDownloadingSchemasProgress(this.queuedCount);
	}
	
	this.startNextDownload();
};

Arbiter.SchemaDownloader.prototype.startNextDownload = function(){
	
	var obj = this.pop();
	
	if(Arbiter.Util.existsAndNotNull(obj)){
		
		this.download(obj);
	}else{
		
		this.onDownloadComplete();
	}
};

Arbiter.SchemaDownloader.prototype.download = function(obj){
	var context = this;
	
	var callback = function(){
		
		Arbiter.Cordova.updateDownloadingSchemasProgress(
				++context.finishedCount, context.queuedCount);
		
		context.startNextDownload();
	};
	
	var downloaderHelper = new Arbiter.SchemaDownloaderHelper(obj.layer, this.wfsVersion, function(alreadyInProject){
		
		if(alreadyInProject){
			
			context.layersAlreadyInProject.push(obj.layer[Arbiter.LayersHelper.layerTitle()]);
			context.layers.splice(obj.index, 1);
		}
		
		callback();
	}, function(featureType){
		
		context.failedLayers.push(featureType);
		
		callback();
	});
	
	downloaderHelper.downloadSchema();
};