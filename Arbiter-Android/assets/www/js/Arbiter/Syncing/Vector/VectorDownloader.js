Arbiter.VectorDownloader = function(_schema, _bounds, _onSuccess, _onFailure){
	this.schema = _schema;
	this.bounds = _bounds;
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	
	var serverId = this.schema.getServerId();
	var server = Arbiter.Util.Servers.getServer(serverId);
	
	this.credentials = Arbiter.Util.getEncodedCredentials(
			server.getUsername(), server.getPassword());
};

Arbiter.VectorDownloader.prototype.onDownloadFailure = function(){
	
	if(Arbiter.Util.funcExists(this.onFailure)){
		this.onFailure(this.schema.getFeatureType());
	}
};

Arbiter.VectorDownloader.prototype.onDownloadComplete = function(){
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		this.onSuccess();
	}
};

Arbiter.VectorDownloader.prototype.download = function(){
	
	var context = this;
	
	// Download the latest given the project aoi
	Arbiter.Util.Feature.downloadFeatures(this.schema, this.bounds,
			this.credentials, function(schema, features){
		
		// Call the onDownloadSuccess method
		context.onDownloadSuccess(features);
		
	}, function(e){
		
		console.log("Failed to download features", e);
		
		context.onDownloadFailure();
	});
};

Arbiter.VectorDownloader.prototype.onDownloadSuccess = function(features){
	var context = this;
	
	// On successful download, delete the layers feature table
	Arbiter.FeatureTableHelper.clearFeatureTable(this.schema, function(){
		
		var isDownload = true;
		
		Arbiter.FeatureTableHelper.insertFeatures(context.schema,
				context.schema.getSRID(), features,
				isDownload, function(){
			
			context.onSuccess();
			
		}, function(e){
			
			console.log("Failed to insert features into " 
					+ context.schema.getFeatureType(), e);
			
			context.onDownloadFailure();
		});
	}, function(e){
		
		console.log("Failed to clear old features from " 
				+ context.schema.getFeatureType(), e);
		
		context.onDownloadFailure();
	});
};