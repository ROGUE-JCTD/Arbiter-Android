Arbiter.VectorDownloader = function(_schema, _bounds, _onSuccess, _onFailure){
	this.schema = _schema;
	this.bounds = _bounds;
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	
	console.log("VectorDownloader initializing");
	var serverId = this.schema.getServerId();
	
	console.log("vectorDownloader serverId: " + serverId);
	
	var server = Arbiter.Util.Servers.getServer(serverId);
	
	this.credentials = Arbiter.Util.getEncodedCredentials(
			server.getUsername(), server.getPassword());
	
	console.log("vectorDownloader credentials: " + this.credentials);
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
	
	console.log("vector downloader download");
	
	var context = this;
	
	// Download the latest given the project aoi
	Arbiter.Util.Feature.downloadFeatures(this.schema, this.bounds,
			this.credentials, function(schema, features){
		
		console.log("downloadFeatures success", features);
		
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
		
		console.log("clearFeatureTable success");
		
		var isDownload = true;
		
		Arbiter.FeatureTableHelper.insertFeatures(context.schema,
				context.schema.getSRID(), features,
				isDownload, function(){
			
			console.log("insertFeatures success");
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