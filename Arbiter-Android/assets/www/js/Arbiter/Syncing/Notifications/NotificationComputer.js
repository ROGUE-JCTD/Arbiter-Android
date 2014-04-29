(function(){
	
	Arbiter.NotificationComputer = function(featureDb, projectDb, schema, syncId, onSuccess, onFailure){
		this.featureDb = featureDb;
		this.projectDb = projectDb;
		this.schema = schema;
		this.syncId = syncId;
		
		this.onComputeSuccess = function(){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess();
			}
		};
		
		this.onComputeFailure = function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		this.currentFeatures = null;
		this.previousFeatures = null;
		this.featureTx = null;
		this.projectTx = null;
		this.notifications = null;
	};
	
	var prototype = Arbiter.NotificationComputer.prototype;
	
	var construct = Arbiter.NotificationComputer;
	
	construct.TABLE_NAME = "notifications";
	construct.ID = "_id";
	construct.SYNC_ID = "syncId";
	construct.LAYER_ID = "layerId";
	construct.FID = "fid";
	construct.STATE = "state";
	
	prototype.computeNotifications = function(){
		
		var context = this;
		
		this.featureDb.transaction(function(tx){
			
			context.featureTx = tx;
			
			context._checkTempFeatureTableExists();
		}, function(e){
			
			context.onComputeFailure(e);
		});
	};
	
	prototype._checkTempFeatureTableExists = function(){
		
		var context = this;
		
		var sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" 
			+ Arbiter.Util.getTempFeatureTableName(this.schema.getFeatureType()) + "';";
		
		this.featureTx.executeSql(sql, [], function(tx, res){
			
			// If the table exists, proceed
			if(res.rows.length > 0){
				context._getCurrentFeatures();
			}else{
				// If the table doesn't exist, then call the success here.
				context.onComputeSuccess();
			}
		}, function(tx, e){
			context.onComputeFailure(e);
		});
	};
	
	prototype._getCurrentFeatures = function(){
		
		var context = this;
		
		var featureType = Arbiter.Util.getFeatureTypeNoPrefix(this.schema.getFeatureType());
		
		var sql = "SELECT * FROM '" + featureType + "';";
		
		this.currentFeatures = {
			ids: []	
		};
		
		this.featureTx.executeSql(sql, [], function(_tx, res){
			
			var feature = null;
			var featureId = null;
			
			for(var i = 0; i < res.rows.length; i++){
				
				feature = res.rows.item(i);
				featureId = feature[Arbiter.FeatureTableHelper.FID];
				
				context.currentFeatures.ids.push(featureId);
				context.currentFeatures[featureId] = feature;
			}
			
			context._getPreviousFeatures();
		}, function(_tx, e){
			
			context.onComputeFailure(e);
		});
	};
	
	prototype._getPreviousFeatures = function(){
		
		var context = this;
		
		var tempTableName = Arbiter.Util.getTempFeatureTableName(this.schema.getFeatureType());
		
		var sql = "SELECT * FROM '" + tempTableName + "';";
		
		this.previousFeatures = {
			ids: []
		};
		
		this.featureTx.executeSql(sql, [], function(_tx, res){
			
			var feature = null;
			var featureId = null;
			
			for(var i = 0; i < res.rows.length; i++){
				
				feature = res.rows.item(i);
				featureId = feature[Arbiter.FeatureTableHelper.FID];
				
				context.previousFeatures.ids.push(featureId);
				context.previousFeatures[feature[Arbiter.FeatureTableHelper.FID]] = feature;
			}
			
			context._compute();
		}, function(_tx, e){
			
			context.onComputeFailure(e);
		});
	};
	
	prototype._compute = function(){
		
		this.notifications = [];
		
		var notification = null;
		var currentFeature = null;
		var currentFeatureId = null;
		var previousFeature = null;
		var indexOfPreviousId = null;
		
		for(var i = 0; i < this.currentFeatures.ids.length; i++){
			
			notification = null;
			
			// Get the first feature and its id
			currentFeatureId = this.currentFeatures.ids[i];
			currentFeature = this.currentFeatures[currentFeatureId];
			
			// Get the previous feature matching the current features id if there is one
			previousFeature = this.previousFeatures[currentFeatureId];
			
			// Remove the feature from the list of previous features.
			// This way, after getting through all of the features,
			// The only features we have in the previous features list
			// Are the features that got removed.
			indexOfPreviousId = this.previousFeatures.ids.indexOf(currentFeatureId);
			this.previousFeatures.ids.splice(indexOfPreviousId, 1);
			delete this.previousFeatures[currentFeatureId];
			
			// The feature didn't exist previously so it must've been added
			if(!Arbiter.Util.existsAndNotNull(previousFeature)){
				notification = {};
				
				notification[construct.FID] = currentFeatureId;
				notification[construct.STATE] = "ADDED";
				notification[construct.LAYER_ID] = this.schema.getLayerId();
				notification[construct.SYNC_ID] = this.syncId;
				
			}else{ 
			
				// Remove the metadata because it interferes with the hash
				currentFeature = this._removeMetadata(currentFeature);
				previousFeature = this._removeMetadata(previousFeature);
				
				// If the hash of the features isn't the same, then the feature must have been modified
				if(SHA1(JSON.stringify(currentFeature)) !== SHA1(JSON.stringify(previousFeature))){
				
					notification = {};
					
					notification[construct.FID] = currentFeatureId;
					notification[construct.STATE] = this._getDiff(currentFeature, previousFeature);
					notification[construct.LAYER_ID] = this.schema.getLayerId();
					notification[construct.SYNC_ID] = this.syncId;
				}
			}
			
			if(Arbiter.Util.existsAndNotNull(notification)){
				this.notifications.push(notification);
			}
		}
		
		this._getRemovalNotifications();
	};
	
	prototype._removeMetadata = function(feature){
		
		delete feature[Arbiter.FeatureTableHelper.ID];
		delete feature[Arbiter.FeatureTableHelper.SYNC_STATE];
		delete feature[Arbiter.FeatureTableHelper.MODIFIED_STATE];
		
		return feature;
	};
	
	// Only the features that were removed will still be in the previous features list at this point
	prototype._getRemovalNotifications = function(){
		
		var feature = null;
		var featureId = null;
		var notification = null;
		
		for(var i = 0; i < this.previousFeatures.ids.length; i++){
			
			featureId = this.previousFeatures.ids[i];
			feature = this.previousFeatures[featureId];
			
			notification = {};
			notification[construct.STATE] = "REMOVED";
			notification[construct.FID] = featureId;
			notification[construct.LAYER_ID] = this.schema.getLayerId();
			notification[construct.SYNC_ID] = this.syncId;
			
			this.notifications.push(notification);
		}
		
		this._saveNotifications();
	};
	
	prototype._saveNotifications = function(){
		var context = this;
		
		this.projectDb.transaction(function(tx){
			
			context.projectTx = tx;
			
			context._saveNotification();
		}, function(e){
			context.onComputeFailure(e);
		});
	};
	
	prototype._saveNotification = function(){
		
		var notification = this.notifications.shift();
		
		if(!Arbiter.Util.existsAndNotNull(notification)){
			
			this.onComputeSuccess();
			
			return;
		}
		
		var context = this;
		
		var query = this._getInsertNotificationQuery(notification);
		
		this.projectTx.executeSql(query.sql, query.values, function(tx, res){
			
			context._saveNotification();
		}, function(tx, e){
			console.log("Couldn't insert notification: " + e.stack);
			context.onComputeFailure(e);
		});
	};
	
	prototype._getInsertNotificationQuery = function(notification){
		
		var sql = "INSERT INTO '" + construct.TABLE_NAME 
			+ "' (" + construct.SYNC_ID + "," 
			+ construct.LAYER_ID + "," 
			+ construct.FID + "," 
			+ construct.STATE + ") VALUES (?,?,?,?);" ;
		
		var stateValue = notification[construct.STATE];
		
		if(stateValue.constructor !== String){
			stateValue = JSON.stringify(stateValue);
		}
		
		var values = [
		    notification[construct.SYNC_ID],
		    notification[construct.LAYER_ID],
		    notification[construct.FID],
		    stateValue
		];
		
		return {
			sql: sql,
			values: values
		};
	};
	
	prototype._getDiff = function(currentFeature, previousFeature){
		var diff = {};
		
		var currentValue = null;
		var previousValue = null;
		
		// Assuming the attributes are the same for each feature (they should be...)
		for(var key in currentFeature){
		
			if(!Arbiter.FeatureTableHelper.isMetaKey(key)){
				
				currentValue = currentFeature[key];
				previousValue = previousFeature[key];
				
				if(currentValue !== previousValue){
					diff[key] = {
						"before": previousValue,
						"after": currentValue
					};
				}
			}
		}
		
		return diff;
	};
})();