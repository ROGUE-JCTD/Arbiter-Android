Arbiter.VectorUploader = function(_layer, _onSuccess, _onFailure){
	this.layer = _layer;
	this.schema = Arbiter.Util.getSchemaFromOlLayer(_layer);
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	
	this.gotResponse = false;
	this.timedOut = false;
	this.succeeded = false;
	this.abortSync = false;
};

Arbiter.VectorUploader.prototype.clearSaveCallbacks = function(layer){
	
	delete layer.metadata["onSaveSuccess"];
	delete layer.metadata["onSaveFailure"];
};

Arbiter.VectorUploader.prototype.onSaveSuccess = function(layer){
	console.log("Arbiter.VectorUploader.onSaveSuccess");
	this.gotResponse = true;
    this.succeeded = true;
	this.updateSyncStatus(layer);
};

Arbiter.VectorUploader.prototype.updateSyncStatus = function(layer){
	var context = this;
	
	Arbiter.FeatureTableHelper.updateFeaturesSyncStatus(this.schema.getFeatureType(), function(){
		
		context.clearSaveCallbacks(layer);
		
		if(Arbiter.Util.funcExists(context.onSuccess) && !context.timedOut){
			context.onSuccess(context.abortSync);
		}
	}, function(e){
		console.log("Could not update sync status for: " + context.schema.getFeatureType());
		
		context.onSaveFailure(layer);
	});
};

Arbiter.VectorUploader.prototype.onSaveFailure = function(layer){
	console.log("Arbiter.VectorUploader.onSaveFailure");

	this.gotResponse = true;
	this.clearSaveCallbacks(layer);
	
	if(Arbiter.Util.funcExists(this.onFailure) && !this.timedOut){
		this.onFailure(this.schema.getFeatureType(), this.abortSync);
	}
};

Arbiter.VectorUploader.prototype.upload = function(){
	
	if(this.schema.isEditable() === false){
		
		if(Arbiter.Util.funcExists(this.onSuccess)){
			this.onSuccess(this.abortSync);
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
	
	metadata["onSaveFailure"] = function(event){
		console.log("my onSaveFailure", event);
		context.onSaveFailure(context.layer);
	};
	
	this.layer.strategies[0].save();
	
	var timeoutDialogCallback = function() {
        if(context.gotResponse) {
            if(context.succeeded){
                if(Arbiter.Util.funcExists(context.onSuccess)) {
                    context.onSuccess(context.abortSync);
                }
            } else {
                if(Arbiter.Util.funcExists(context.onFailure)) {
                    context.onFailure(context.schema.getFeatureType());
                }
            }
        } else {
            context.timedOut = false;
        }
    };
	
	window.setTimeout(function(){
	    //prompt user to abort sync or wait
        if(!context.gotResponse) {
            context.timedOut = true;
            Arbiter.Cordova.showSyncTimeOutDialog(timeoutDialogCallback, function() {
                //add parameter to failure callback
                context.abortSync = true;
                timeoutDialogCallback();
            });
        }
    }, 30000);
};