(function(){
	
	Arbiter.VectorDownloader = function(db, _schema, _bounds, _onSuccess, _onFailure){
		this.schema = _schema;
		this.bounds = _bounds;
		this.onSuccess = _onSuccess;
		this.onFailure = _onFailure;
		this.db = db;
		
		var serverId = this.schema.getServerId();
		
		var server = Arbiter.Util.Servers.getServer(serverId);
		
		this.credentials = Arbiter.Util.getEncodedCredentials(
				server.getUsername(), server.getPassword());
	};

	var prototype = Arbiter.VectorDownloader.prototype;
	
	prototype.onDownloadFailure = function(e){
		
		if(Arbiter.Util.funcExists(this.onFailure)){
			this.onFailure(e);
		}
	};

	prototype.onDownloadComplete = function(){
		
		if(Arbiter.Util.funcExists(this.onSuccess)){
			this.onSuccess();
		}
	};

	prototype.download = function(){
		
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
			
		}, function(error){
			
			console.log("Failed to download features: error = " + error);
			
			context.onDownloadFailure(error);
		});
	};

	prototype.onDownloadSuccess = function(features){
		var context = this;
		
		var downloadedFeaturesHandler = new Arbiter.DownloadedFeaturesHandler(this.db, this.schema, this.credentials, features, function(){
			
			var storeMediaForSchema = new Arbiter.StoreFeaturesMediaToDownload(
					context.schema, features, function(failedToStore){
				
				if(Arbiter.Util.funcExists(context.onSuccess)){
					context.onSuccess();
				}
				
			}, function(e){
				
				//TODO: handle error
				console.log("VectorDownloader download error - " + JSON.stringify(e));
				
				if(Arbiter.Util.funcExists(context.onSuccess)){
					context.onSuccess();
				}
			});
			
			storeMediaForSchema.startStoring();
		}, function(e){
			
			console.log("Failed to insert features into " 
					+ context.schema.getFeatureType(), e);
			
			context.onDownloadFailure(e);
		});
		
		downloadedFeaturesHandler.storeDownloads();
	};
})();