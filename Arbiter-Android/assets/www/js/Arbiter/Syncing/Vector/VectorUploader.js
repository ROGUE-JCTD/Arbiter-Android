(function(){
	
	Arbiter.VectorUploader = function(_layer, _onSuccess, _onFailure){
		this.layer = _layer;
		this.schema = Arbiter.Util.getSchemaFromOlLayer(_layer);
		this.onSuccess = _onSuccess;
		this.onFailure = _onFailure;
		
		this.requestInProgress = false;
		this.requestSucceeded = false;
		this.requestTimedOut = false;
		this.requestCancelled = false;
	};

	var prototype = Arbiter.VectorUploader.prototype;
	
	prototype.clearSaveCallbacks = function(layer){
		
		delete layer.metadata["onSaveSuccess"];
		delete layer.metadata["onSaveFailure"];
	};

	prototype.onSaveSuccess = function(layer){
		console.log("Arbiter.VectorUploader.onSaveSuccess");
		
		this.requestInProgress = false;
		this.requestSucceeded = true;
		
		if(!this.requestTimedOut || this.requestCancelled){
			this.updateSyncStatus(layer);
		}
	};

	prototype.updateSyncStatus = function(layer){
		var context = this;
		
		Arbiter.FeatureTableHelper.updateFeaturesSyncStatus(this.schema.getFeatureType(), function(){
			
			context.clearSaveCallbacks(layer);
			
			if(Arbiter.Util.funcExists(context.onSuccess)){
				context.onSuccess(context.requestCancelled);
			}
		}, function(e){
			console.log("Could not update sync status for: " + context.schema.getFeatureType());
			
			context.onSaveFailure(layer);
		});
	};

	prototype.onSaveFailure = function(layer){
		console.log("Arbiter.VectorUploader.onSaveFailure");
		
		this.requestInProgress = false;
		this.requestSucceeded = false;
		
		if(!this.requestTimedOut || this.requestCancelled){
			
			this.clearSaveCallbacks(layer);
			
			if(Arbiter.Util.funcExists(this.onFailure)){
				this.onFailure(this.requestCancelled);
			}
		}
	};
	
	prototype.upload = function(){
		
		if(this.schema.isEditable() === false || this.schema.isReadOnly()){
			
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
		
		this.wait(true);
	};
	
	prototype._continueRemainingRequests = function(){
		// continue with the requests
		
		console.log("continue with requests");
		
		// Set requestTimedOut to false so that the layers callbacks will execute fully.
		this.requestTimedOut = false;
		
		// Request came back already
		if(!this.requestInProgress){
			
			if(this.requestSucceeded){
				
				this.onSaveSuccess(this.layer);
			}else{
				this.onSaveFailure(this.layer);
			}
		}else{ // Still waiting for the request
			
			this.wait(false);
		}
	};
	
	prototype._cancelRemainingRequests = function(){
		// cancel the requests
		console.log("cancel remaining requests");
		
		this.requestCancelled = true;
		
		// Got response back so call the corresponding callbacks for whether the request succeeded or not
		if(!this.requestInProgress){
			// Got response back already.
			
			// If it succeeded
			if(this.requestSucceeded){
				
				this.onSaveSuccess(this.layer);
			}else{
				this.onSaveFailure(this.layer);
			}
		}
		
		// Otherwise wait for the request to come back and then decide what to do in the callbacks above.
	};
	
	prototype.wait = function(firstRequest){
		
		this.requestInProgress = true;
		
		if(firstRequest){
			
			this.layer.strategies[0].save();
		}
		
		var context = this;
		
		window.setTimeout(function(){
			
			if(context.requestInProgress){
				
				context.requestTimedOut = true;
				
				Arbiter.Cordova.syncOperationTimedOut(function(){
					context._continueRemainingRequests.call(context);
				}, function(){
					context._cancelRemainingRequests.call(context);
				});
			}
		}, 30000);
	};
	
})();