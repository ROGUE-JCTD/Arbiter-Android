(function(){
	
	Arbiter.DownloadedFeaturesHandler = function(db, schema, features, onSuccess, onFailure){
		this.schema = schema;
		this.features = features;
		this.onSuccess = onSuccess;
		this.onFailure = onFailure;
		this.db = db;
		this.wktFormatter = new OpenLayers.Format.WKT();
		this.createTableSql = null;
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
		
		var context = this;
		
		this.db.transaction(function(tx){
			
			context.getCreateSql(tx, function(sql){
				
				context.createTableSql = sql;
				
				context.renameOldTable(tx, function(){
					
					context.createNewTable(tx, function(){
						
						context.storeFeatures(tx);
						
					}, context.handleFailed);
				}, context.handleFailed);
			}, context.handleFailed);
		}, context.handleFailed);
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
				
			context.handleFailed(e);
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
		
		if(Arbiter.Util.existsAndNotNull(feature.fid) && feature.fid.indexOf("@") !== -1){
			
			var parts = feature.fid.split("@");
			
			feature.fid = parts[0];
		}
		
		values.push(feature.fid);
		
		// Push the attributes
		for(var i = 0; i < attributes.length; i++){
			attributeName = attributes[i].getName();
			query += ", " + attributeName;
			
			values.push(feature.attributes[attributeName]);
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
				onFailure(e);
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
				onFailure(e);
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
				onFailure(e);
			}
		});
	};
})();