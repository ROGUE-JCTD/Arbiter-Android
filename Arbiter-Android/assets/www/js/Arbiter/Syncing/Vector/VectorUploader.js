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
			
			context.onSaveFailure(layer, Arbiter.Error.Sync.ARBITER_ERROR);
		});
	};

	prototype.onSaveFailure = function(layer, updateError){
		console.log("Arbiter.VectorUploader.onSaveFailure");
		
		this.requestInProgress = false;
		this.requestSucceeded = false;
		
		if(!this.requestTimedOut || this.requestCancelled){
			
			this.clearSaveCallbacks(layer);
			
			if(Arbiter.Util.funcExists(this.onFailure)){
				this.onFailure(this.requestCancelled, updateError);
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
		
		metadata["onSaveFailure"] = function(event){
			console.log("my onSaveFailure event", event);
			
			var error = false;
			
			var statusCode = event.response.priv.status;
			
			if((statusCode == 200) && (event.response.priv.responseText.indexOf("Update error") > -1)){
					
				error = Arbiter.Error.Sync.UPDATE_ERROR;
			}else if((statusCode == 200) && (event.response.priv.responseText.indexOf("read-only") > -1)){
				
				error = Arbiter.Error.Sync.UNAUTHORIZED;
			}else{
				
				error = Arbiter.Error.Sync.getErrorFromStatusCode(statusCode);
			}
			
			context.onSaveFailure(context.layer, error);
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
				this.onSaveFailure(this.layer, Arbiter.Error.Sync.UNKNOWN_ERROR);
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
				this.onSaveFailure(this.layer, Arbiter.Error.Sync.TIMED_OUT);
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