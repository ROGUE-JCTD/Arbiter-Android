// Traverse all layers, traverse all features, traverse all media of features
Arbiter.StoreMediaToDownload = function(_db, _dbLayers, _schemas, _onSuccess){
	this.db = _db;
	this.dbLayers = _dbLayers;
	this.schemas = _schemas;
	console.log("storeMediaToDownload layers = " + JSON.stringify(this.dbLayers));
	this.onSuccess = _onSuccess;
	this.index = -1;
	
	this.storeFailed = null;
};

// Gets the next schema
Arbiter.StoreMediaToDownload.prototype.pop = function(){
	
	console.log("store media to download pop");
	if(++this.index < this.dbLayers.length){
		var layer = this.dbLayers[this.index];
		var layerId = layer[Arbiter.LayersHelper.layerId()];
		
		var schema = this.schemas[layerId];
		
		if(Arbiter.Util.existsAndNotNull(schema)){
			return {
				id: layerId,
				schema: schema
			};
		}

		// If the schema doesn't exist, skip this layer
		// because it won't have any photos
		this.pop();
	}
	
	return undefined;
};

Arbiter.StoreMediaToDownload.prototype.onStoreComplete = function(){
	console.log("finished storing things for download");
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		this.onSuccess(this.storeFailed);
	}
};

Arbiter.StoreMediaToDownload.prototype.startStoring = function(){

	this.storeNextLayer();
};

Arbiter.StoreMediaToDownload.prototype.storeNextLayer = function(){
	
	var schemaAndLayerId = this.pop();
	
	if(schemaAndLayerId !== undefined){
		
		console.log("schema isn't undefined");
		this.store(schemaAndLayerId);
	}else{
		console.log("schema is undefined");
		this.onStoreComplete();
	}
};

Arbiter.StoreMediaToDownload.prototype.addFailedStore = function(layerId, failedMedia){
	
	if(Arbiter.Util.existsAndNotNull(failedMedia) 
			&& Arbiter.Util.existsAndNotNull(layerId)){
		
		if(!Arbiter.Util.existsAndNotNull(this.storeFailed)){
			this.storeFailed = {};
		}
		
		this.storeFailed[layerId] = failedMedia;
	}
};

Arbiter.StoreMediaToDownload.prototype.store = function(schemaAndLayerId){

	var context = this;
	
	var storeMediaForSchema = new Arbiter.StoreFeaturesMediaToDownload(
			schemaAndLayerId.schema, this.db, function(failedToStore){
		
		console.log("finished storing for " + schemaAndLayerId.schema.getFeatureType());
		context.addFailedStore(schemaAndLayerId.layerId, failedToStore);
		
		context.storeNextLayer();
		
	}, function(e){
		
		console.log("error", e);
		
		context.addFailedStore(schemaAndLayerId.layerId, [e]);
		
		context.storeNextLayer();
	});
	
	storeMediaForSchema.startStoring();
};