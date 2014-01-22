Arbiter.MediaDownloader = function(_featureDb, _schema, _server, _mediaDir){
	
	this.db = _featureDb;
	this.schema = _schema;
	this.server = _server;
	this.mediaDir = _mediaDir;
	
	// key by arbiter_id
	this.failedOnDownload = null;
	
	var credentials = Arbiter.Util.getEncodedCredentials(
			this.server.getUsername(),
			this.server.getPassword());
	
	this.url = this.server.getUrl();
	this.url = this.url.substring(0,
			this.url.length - 9);
	
    this.url += "file-service/";
    
	this.header = {
		Authorization: 'Basic ' + credentials	
	};
	
	this.onDownloadComplete = null;
	
	this.features = [];
	this.index = -1;
	this.queuedCount = 0;
	
	// Start at -1 to account for the first download
	this.finishedDownloading = 0;
};

Arbiter.MediaDownloader.prototype.pop = function(){
	
	if(++this.index < this.features.length){
		return this.features[this.index];
	}
	
	return undefined;
};

Arbiter.MediaDownloader.prototype.startDownload = function(onSuccess){
	var context = this;
	
	this.onDownloadComplete = onSuccess;
	
	this.getFeatures(function(){
		
		var mediaDownloadCounter = new Arbiter.MediaDownloadCounter(
				context.schema, context.features);
		
		context.queuedCount = mediaDownloadCounter.getCount();
		
		if(context.queuedCount === 0){
			
			if(Arbiter.Util.funcExists(context.onDownloadComplete)){
				context.onDownloadComplete(context.failedOnDownload);
			}
			
			return;
		}
		
		context.startDownloadingNext();
	}, function(e){
		// TODO: Handle errors
		console.log(e);
		
		if(Arbiter.Util.funcExists(context.onDownloadComplete)){
			context.onDownloadComplete(context.failedOnDownload);
		}
	});
};

Arbiter.MediaDownloader.prototype.getFeatures = function(onSuccess, onFailure){
	var context = this;
	
	var featureType = this.schema.getFeatureType();
	
	this.db.transaction(function(tx){
		
		tx.executeSql("select * from " + featureType + ";", [], function(tx, res){
			
			for(var i = 0, count = res.rows.length; i < count; i++){
				context.features.push(res.rows.item(i));
			}
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess();
			}
		}, function(tx, e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("MediaDownloader.js Error getting features - " + e);
			}
		});
	}, function(e){
		
		if(Arbiter.Util.funcExists(onFailure)){
			onFailure("MediaDownloader.js Error getting features - " + e);
		}
	});
};

Arbiter.MediaDownloader.prototype.putDownloadFailure = function(key, failedMedia){
	
	if(failedMedia !== null && failedMedia !== undefined){
		
		if(this.failedOnDownload === null || this.failedOnDownload === undefined){
			this.failedOnDownload = {};
		}
		
		this.failedOnDownload[key] = failedMedia;
	}	
};

Arbiter.MediaDownloader.prototype.startDownloadingNext = function(){
	var context = this;
	
	var feature = this.pop();
	
	if(feature !== undefined){
		
		var mediaDownloaderHelper = new Arbiter.MediaDownloaderHelper(feature, 
				this.schema, this.header, this.url, this.mediaDir);
		
		mediaDownloaderHelper.startDownload(function(failedMedia){
			
			Arbiter.Cordova.updateMediaDownloadingStatus(
					context.schema.getFeatureType(), 
					++context.finishedDownloading,
					context.queuedCount);
			
			var key = feature[Arbiter.FeatureTableHelper.ID];
			
			if(failedMedia !== null && failedMedia !== undefined){
				context.putDownloadFailure(key, failedMedia)
			}
			
			context.startDownloadingNext();
		});
	}else{
		
		if(Arbiter.Util.funcExists(this.onDownloadComplete)){
			this.onDownloadComplete(this.failedOnDownload);
		}
	}
};