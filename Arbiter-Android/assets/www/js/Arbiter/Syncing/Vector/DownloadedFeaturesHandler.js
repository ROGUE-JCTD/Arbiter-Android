(function(){
	
	Arbiter.DownloadedFeaturesHandler = function(db, schema, credentials, features, onSuccess, onFailure){
		this.schema = schema;
		this.encodedCredentials = credentials;
		this.features = features;
		this.onSuccess = onSuccess;
		this.onFailure = onFailure;
		this.db = db;
		this.wktFormatter = new OpenLayers.Format.WKT();
		this.createTableSql = null;
		this.gmtOffset = null;
	};
	
	var prototype = Arbiter.DownloadedFeaturesHandler.prototype;
	
	prototype.handleSuccess = function(){
		
		if(Arbiter.Util.existsAndNotNull(this.onSuccess)){
			this.onSuccess();
		}
	};
	
	prototype.handleFailed = function(e){
		
		if(Arbiter.Util.existsAndNotNull(this.onFailure)){
			this.onFailure(e);
		}
	};
	
	prototype.storeDownloads = function(){
		
		if(Arbiter.Util.existsAndNotNull(this.schema.getTimeProperty())){
			this.checkTimeDifference();
		}else{
			this._storeDownloads();
		}
	};
	
	prototype.checkTimeDifference = function(){
		
		if(Arbiter.Util.existsAndNotNull(this.features) && this.features.length > 0){
			
			var context = this;
			
			var featureId = this.features[0].fid;
			
			var featureType = "";
			
			var prefix = this.schema.getPrefix();
			
			if(Arbiter.Util.existsAndNotNull(prefix) && prefix !== ""){
				
				featureType += prefix + ":";
			}
			
			featureType += this.schema.getFeatureType();
			
			var url =  this.schema.getUrl() + "/wfs?service=wfs&version=1.1.0&outputFormat=json&request=GetFeature&typeNames=" + featureType + "&featureID=" + featureId;
			
			var gotRequestBack = false;
	        
	        var options = {
	            url: url,
	            headers: {
	                    'Content-Type': 'text/xml;charset=utf-8',
	            },
	            success: function(response){
	            	gotRequestBack = true;
	            	
	            	response = JSON.parse(response.responseText);
	            	
	            	console.log("checkTimeDifference response", response);
	            	
	                var features = response.features;
	                
	                console.log("CheckTimeDifference features: ", features);
	                
	                context.calculateTimeDifference(features);
	            },
	            failure: function(response){
	            	gotRequestBack = true;
	            	
	            	var error = Arbiter.Error.Sync.getErrorFromStatusCode(response.status);
	            	
	            	context.handleFailed(error);
	            }
	        };
	        
	        if(Arbiter.Util.existsAndNotNull(this.encodedCredentials)){
	        	options.headers['Authorization'] = 'Basic ' + this.encodedCredentials;
	        }
	        
	        var request = new OpenLayers.Request.GET(options);
	        
			window.setTimeout(function(){
				if(!gotRequestBack){
					request.abort();
					
					context.handleFailed(Arbiter.Error.Sync.TIMED_OUT);
				}
			}, 30000);
		}else{
			
			this._storeDownloads();
		}
	};

	//TODO: this approach is very dependent on which features happens to be the first one on the layer
	//      contributes to problems hard to track down. Update with better solution!
	prototype.calculateTimeDifference = function(features){
		
		var timeProperty = this.schema.getTimeProperty();
		
		if(Arbiter.Util.existsAndNotNull(features) && features.length > 0 
				&& Arbiter.Util.existsAndNotNull(timeProperty) 
				&& this.isTimeType(timeProperty.type)){
			
			var geoJSONFeature = features[0];
			
			if(Arbiter.Util.existsAndNotNull(timeProperty)){
				
				var geoJSONValue = geoJSONFeature.properties[timeProperty.key];
				
				if(Arbiter.Util.existsAndNotNull(geoJSONValue)){
					
					var wfs1_0_0Feature = this.features[0];
					
					var wfs1_0_0Value = wfs1_0_0Feature.attributes[timeProperty.key];
					
					var isoDate = null;
					var localDate = null;
					
					if(timeProperty.type === "xsd:dateTime" || timeProperty.type === "dateTime"){
						
						isoDate = new Date(geoJSONValue);
						localDate = new Date(wfs1_0_0Value);
					}else{ // timeProperty.type === "xsd:time"
						
						var nowString = (new Date()).toISOString();
						
						var parts = nowString.split("T");
						
						parts[1] = geoJSONValue;
						
						isoDate = new Date(parts.join("T"));
						
						parts[1] = wfs1_0_0Value;
						
						localDate = new Date(parts.join("T"));
					}
					
					var diffInMilli = isoDate.getTime() - localDate.getTime();
					
					console.log("diffInMilli = " + diffInMilli);
					
					this.storeTimeDiff(diffInMilli);
				} else {
					// Note: not catching this cases causes app to get stuck if the *first* returned
					//       from map happens to have null datetime value. assume same case a "no features" for now...
					// No features, so can't calculate the time difference.  Proceed as usual.
					this._storeDownloads();
				}
			} else {
				// Note: not catching this cases causes app to get stuck if the *first* returned
				//       from map happens to have null datetime value. assume same case a "no features" for now...
				// No features, so can't calculate the time difference.  Proceed as usual.
				this._storeDownloads();
			}
		}else{
			// No features, so can't calculate the time difference.  Proceed as usual.
			this._storeDownloads();
		}
	};
	
	prototype.storeTimeDiff = function(gmtOffset){
		
		var context = this;
		
		console.log("storeTimeDiff: offset = " + gmtOffset + ", serverId = " + this.schema.getServerId());
		
		Arbiter.ServersHelper.updateServer(this.schema.getServerId(), gmtOffset, function(gmtOffset){
			
			console.log("updated server");
			
			context.gmtOffset = gmtOffset;
			
			context._storeDownloads();
		}, function(e){
			
			context.handleFailed(Arbiter.Error.Sync.ARBITER_ERROR);
		});
	};
	
	prototype._storeDownloads = function(){
		
		console.log("storing downloads");
		
		var context = this;
		
		var fail = function(e){
			
			context.handleFailed(Arbiter.Error.Sync.ARBITER_ERROR);
		};
		
		this.db.transaction(function(tx){
			
			context.getCreateSql(tx, function(sql){
				
				context.createTableSql = sql;
				
				context.renameOldTable(tx, function(){
					
					context.createNewTable(tx, function(){
						
						context.storeFeatures(tx);
						
					}, fail);
				}, fail);
			}, fail);
		}, fail);
	};
	
	prototype.storeFeatures = function(tx){
		
		this.storeNextFeature(tx);
	};
	
	prototype.storeNextFeature = function(tx){
		
		var feature = this.features.shift();
		
		if(!Arbiter.Util.existsAndNotNull(feature)){
			
			this.handleSuccess();
			
			return;
		}
		
		var context = this;
		
		var sql = this.getInsertSqlForFeature(feature);
		
		tx.executeSql(sql.query, sql.values, function(_tx, res){
			
			context.storeNextFeature(tx);
			
		}, function(tx, e){
			
			console.log("Failed to store features", e.stack);
				
			context.handleFailed(Arbiter.Error.Sync.ARBITER_ERROR);
		});
	};
	
	// TODO: Need to refactor the FeatureTableHelper and this so
	// the two insert features the same way...
	prototype.getInsertSqlForFeature = function(feature){
		
		var values = [];
		
		var query = "INSERT INTO '" + Arbiter.Util.getFeatureTypeNoPrefix(this.schema.getFeatureType())
		+ "' (" + this.schema.getGeometryName() + ", "
		+ Arbiter.FeatureTableHelper.MODIFIED_STATE + ", "
		+ Arbiter.FeatureTableHelper.SYNC_STATE;
	
		var attributes = this.schema.getAttributes();
		
		// Adding as many question marks as there are attributes
		var questionMarks = "?,?,?";
		
		var attributeName = null;
		
		var wkt = this.wktFormatter.write(feature);
		
		values.push(wkt);
			
		values.push(Arbiter.FeatureTableHelper.MODIFIED_STATES.NONE);
		
		values.push(Arbiter.FeatureTableHelper.SYNC_STATES.SYNCED);
		
		// TODO: Assuming that the primary key of the table is fid,
		// Add insert the FID into the db
		query += ", " + Arbiter.FeatureTableHelper.FID;
		
		questionMarks += ",?";
		
		values.push(feature.fid);
		
		var attribute = null;
		var isoTimeString = null;
		
		// Push the attributes
		for(var i = 0; i < attributes.length; i++){
			attribute = attributes[i];
			attributeName = attribute.getName();
			query += ", " + attributeName;
			
			if(this.isTimeType(attribute.getType())){
				isoTimeString = this.addGMTOffset(feature.attributes[attributeName], attribute.getType());
				
				values.push(isoTimeString);
			}else{
				values.push(feature.attributes[attributeName]);
			}
			
			// Add a question mark to represent the value of 
			// the attribute
			questionMarks += ", ?";
		}
		
		query += ") VALUES (" + questionMarks + ");";
		
		return {
			query: query,
			values: values
		};
	};
	
	prototype.isTimeType = function(type){
		
		return (type === "xsd:dateTime" || type === "xsd:time" || type === "dateTime" || type === "time");
	};
	
	prototype.addGMTOffset = function(timestring, type){
		
		console.log("addGMTOffset");
		
		if(Arbiter.Util.existsAndNotNull(this.gmtOffset) 
				&& Arbiter.Util.existsAndNotNull(timestring)
				&& this.isTimeType(type)){
			
			if(type === "xsd:dateTime" || type === "dateTime"){
				
				var localDate = new Date(timestring);
				
				var isoDate = new Date(localDate.getTime() + this.gmtOffset);
				
				return isoDate.toISOString();
			}else if(type === "xsd:time" || type === "time"){
				
				var now = new Date().toISOString();
				
				var parts = now.split("T");
				
				parts[1] = timestring;
				
				var localDate = new Date(parts.join("T"));
				
				var isoDate = new Date(localDate.getTime() + this.gmtOffset);
				
				parts = isoDate.toISOString().split("T");
				
				return parts[1];
			}
		}
		
		return timestring;
	};
	
	prototype.getCreateSql = function(tx, onSuccess, onFailure){
		
		var context = this;
		
		var featureType = Arbiter.Util.getFeatureTypeNoPrefix(this.schema.getFeatureType());
		
		var sql = "SELECT sql FROM sqlite_master WHERE type='table' AND name='" + featureType + "';";
		
		tx.executeSql(sql, [], function(tx, res){
			
			if(res.rows.length === 0){
				
				if(Arbiter.Util.existsAndNotNull(onFailure)){
					onFailure();
				}
			}else{
				
				var sql = res.rows.item(0).sql;
				
				if(Arbiter.Util.existsAndNotNull(onSuccess)){
					onSuccess(sql);
				}
			}
		}, function(tx, e){
			
			console.log("Failed to create table for downloads", e.stack);
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(Arbiter.Error.Sync.ARBITER_ERROR);
			}
		});
	};
	
	prototype.renameOldTable = function(tx, onSuccess, onFailure){
		
		var featureType = Arbiter.Util.getFeatureTypeNoPrefix(this.schema.getFeatureType());
		
		var tempTableName = Arbiter.Util.getTempFeatureTableName(this.schema.getFeatureType());
		
		var context = this;
		
		tx.executeSql("ALTER TABLE '" + featureType + "' RENAME TO '" + tempTableName + "';", [], function(tx, res){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess();
			}
		}, function(_tx, e){
			
			console.log("Couldn't rename table '" + featureType + "' to '" + tempTableName + "'", e.stack);
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(Arbiter.Error.Sync.ARBITER_ERROR);
			}
		});
	};
	
	prototype.createNewTable = function(tx, onSuccess, onFailure){
		
		var context = this;
			
		if(!Arbiter.Util.existsAndNotNull(this.createTableSql)){
			throw {
				message: "createTableSql must not be " + this.createTableSql
			};
		}
		
		// Create a temporary table
		tx.executeSql(this.createTableSql, [], function(tx, res){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess();
			}
		}, function(tx, e){
			
			if(Arbiter.Util.existsAndNull(onFailure)){
				onFailure(Arbiter.Error.Sync.ARBITER_ERROR);
			}
		});
	};
})();