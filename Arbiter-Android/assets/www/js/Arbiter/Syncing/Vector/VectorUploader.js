Arbiter.VectorUploader = function(_layer, _onSuccess, _onFailure){
	this.layer = _layer;
	this.schema = Arbiter.Util.getSchemaFromOlLayer(_layer);
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
};

Arbiter.VectorUploader.prototype.clearSaveCallbacks = function(layer){
	
	delete layer.metadata["onSaveSuccess"];
	delete layer.metadata["onSaveFailure"];
};

Arbiter.VectorUploader.prototype.onSaveSuccess = function(layer){
	console.log("Arbiter.VectorUploader.onSaveSuccess");
	
	this.updateSyncStatus(layer);
};

Arbiter.VectorUploader.prototype.updateSyncStatus = function(layer){
	var context = this;
	
	Arbiter.FeatureTableHelper.updateFeaturesSyncStatus(this.schema.getFeatureType(), function(){
		
		context.clearSaveCallbacks(layer);
		
		if(Arbiter.Util.funcExists(context.onSuccess)){
			context.onSuccess();
		}
	}, function(e){
		console.log("Could not update sync status for: " + context.schema.getFeatureType());
		
		context.onSaveFailure(layer);
	});
};

Arbiter.VectorUploader.prototype.onSaveFailure = function(layer){
	console.log("Arbiter.VectorUploader.onSaveFailure");
	
	this.clearSaveCallbacks(layer);
	
	if(Arbiter.Util.funcExists(this.onFailure)){
		this.onFailure(this.schema.getFeatureType());
	}
};

Arbiter.VectorUploader.prototype.upload = function(){
	
	if(this.schema.isEditable() === false){
		
		if(Arbiter.Util.funcExists(this.onSuccess)){
			this.onSuccess();
		}
		
		return;
	}
	
	if(this.layer.metadata === null || this.layer.metadata === undefined){
		this.layer.metadata = {};
	}
	
	var metadata = this.layer.metadata;
	
	var context = this;
	
	metadata["onSaveSuccess"] = function(){
		console.log("my onSaveSuccess");
		context.onSaveSuccess(context.layer);
	};
	
	metadata["onSaveFailure"] = function(){
		console.log("my onSaveFailure");
		context.onSaveFailure(context.layer);
	};
	
	this.layer.strategies[0].save();
};