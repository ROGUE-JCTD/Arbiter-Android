Arbiter.VectorDownloader = function(_schema, _bounds, _onSuccess, _onFailure){
	this.schema = _schema;
	this.bounds = _bounds;
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	this.gotResponse = false;
	
    this.timedOut = false;
	this.succeded = false;
	
	var serverId = this.schema.getServerId();
	
	var server = Arbiter.Util.Servers.getServer(serverId);
	
	this.credentials = Arbiter.Util.getEncodedCredentials(
			server.getUsername(), server.getPassword());
};

Arbiter.VectorDownloader.prototype.onDownloadFailure = function(){
    this.gotResponse = true;
    this.succeeded = false;
	//alert("download failed");
	if(Arbiter.Util.funcExists(this.onFailure) && !this.timedOut){
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
	
	if(this.schema.isEditable() === false){
		
		if(Arbiter.Util.funcExists(context.onSuccess)){
			this.onSuccess();
		}
		
		return;
	}
	
	// Download the latest given the project aoi
	Arbiter.Util.Feature.downloadFeatures(this.schema, this.bounds,
			this.credentials, function(schema, features){
		
		// Call the onDownloadSuccess method
		context.onDownloadSuccess(features);
		
	}, function(e){
		
		console.log("Failed to download features", e);
		
		context.onDownloadFailure();
	});
    window.setTimeout(function(){
        if(!context.gotResponse) {
            context.timedOut = true;
            Arbiter.Cordova.showSyncTimeOutDialog(function() {
                if(context.gotResponse) {
                    if(context.succeeded){
                        if(Arbiter.Util.funcExists(context.onSuccess)) {
                            context.onSuccess();
                        }
                    } else {
                        if(Arbiter.Util.funcExists(context.onFailure)) {
                            context.onFailure(context.schema.getFeatureType());
                        }
                    }
                } else {
                    context.timedOut = false;
                }
            });
        }
    }, 20000);
};

Arbiter.VectorDownloader.prototype.onDownloadSuccess = function(features){
	var context = this;
	
    this.gotResponse = true;
    this.succeeded = true;
	//alert("download success");
	// On successful download, delete the layers feature table
	Arbiter.FeatureTableHelper.clearFeatureTable(this.schema, function(){
		
		var isDownload = true;
		
		Arbiter.FeatureTableHelper.insertFeatures(context.schema,
				context.schema.getSRID(), features,
				isDownload, function(){
			
			var storeMediaForSchema = new Arbiter.StoreFeaturesMediaToDownload(
					context.schema, features, function(failedToStore){
					
				if(Arbiter.Util.funcExists(context.onSuccess) && !context.timedOut){
					context.onSuccess();
				}
				
			}, function(e){
				
				//TODO: handle error
				console.log("VectorDownloader download error - " + JSON.stringify(e));
				
				if(Arbiter.Util.funcExists(context.onSuccess) && !context.timedOut){
					context.onSuccess();
				}
			});
			
			storeMediaForSchema.startStoring();
			
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