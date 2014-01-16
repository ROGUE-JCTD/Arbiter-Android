Arbiter.MediaDownloader = function(_featureDb, _schema, _server, _mediaDir){
	this.db = _featureDb;
	this.schema = _schema;
	this.server = _server;
	this.mediaDir = _mediaDir;
	
	// key by arbiter_id
	this.failedOnDownload = {};
	
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
	
	this.onDownloadSuccess = null;
	
	this.features = [];
};

Arbiter.MediaDownloader.prototype.startDownload = function(onSuccess){
	var context = this;
	
	this.onDownloadSuccess = onSuccess;
	
	this.getFeatures(function(){
		
		context.startDownloadingNext();
	}, function(e){
		// TODO: Handle errors
		console.log(e);
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

Arbiter.MediaDownloader.prototype.startDownloadingNext = function(){
	var context = this;
	
	var feature = this.features.shift();
	
	if(feature !== undefined){
		
		var mediaDownloaderHelper = new Arbiter.MediaDownloaderHelper(feature, 
				this.schema, this.header, this.url, this.mediaDir);
		
		mediaDownloaderHelper.startDownload(function(failedMedia){
			
			var key = feature[Arbiter.FeatureTableHelper.ID];
			
			context.failedOnDownload[key] = failedMedia; 
			
			context.startDownloadingNext();
		});
		
	}else{
		
		if(Arbiter.Util.funcExists(this.onDownloadSuccess)){
			this.onDownloadSuccess(this.failedOnDownload);
		}
	}
};