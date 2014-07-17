(function(){
	
	Arbiter.SchemaChangeHandler = function(projectDb, featureDb, fileSystem, wfsVersion){
		
		this.schemas = null;
		this.projectDb = projectDb;
		this.featureDb = featureDb;
		this.fileSystem = fileSystem;
		this.wfsVersion = wfsVersion;
		this._onHandleSuccess = null;
		this._onHandleFailure = null;
	};
	
	var prototype = Arbiter.SchemaChangeHandler.prototype;
	
	var construct = Arbiter.SchemaChangeHandler.prototype;
	
	construct.ERROR_HANDLING = {
		TABLE_NAME: "migration_handling",
		ID: "id",
		FEATURE_TYPE: "feature_type",
		SERVER_ID: "server_id",
		CHECKED: "checked",
		DID_CHANGE: "did_change",
		MIGRATION_SUCCEEDED: "migration_succeeded"
	};
	
	prototype.checkForChanges = function(schemas, onSuccess, onFailure){
		
		var context = this;
		
		var fail = function(e){
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		this._onHandleSuccess = function(){
			
			console.log("_onHandleSuccess");
			
			context._readResults(function(results){
				
				console.log("_onHandleSuccess _readResults");
				
				if(Arbiter.Util.existsAndNotNull(onSuccess)){
					onSuccess(results);
				}
			}, fail);
		};
		
		this._onHandleFailure = function(e){
			
			console.log("_onHandleFailure");
			
			fail(e);
		};
		
		this.schemas = [];
		
		for(var key in schemas){
			
			this.schemas.push(schemas[key]);
		}
		
		this._writeSchemasForChecking();
	};
	
	prototype.clearErrorHandlingTable = function(onSuccess, onFailure){
		
		console.log("clearErrorHandlingTable");
		
		var context = this;
		
		var fail = function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		this.projectDb.transaction(function(tx){
			
			var sql = "DROP TABLE IF EXISTS " + construct.ERROR_HANDLING.TABLE_NAME + ";";
			
			tx.executeSql(sql, [], function(_tx, res){
				
				if(Arbiter.Util.existsAndNotNull(onSuccess)){
					onSuccess();
				}
			}, function(_tx, e){
				fail(e);
			});
		}, fail);
	};
	
	prototype._writeSchemasForChecking = function(){
		
		console.log("_writeSchemasForChecking");
		
		var context = this;
		
		this.projectDb.transaction(function(tx){
			
			console.log("_writeSchemasForChecking - gotTransaction");
			
			var sql = "CREATE TABLE IF NOT EXISTS " + construct.ERROR_HANDLING.TABLE_NAME 
				+ " (" + construct.ERROR_HANDLING.ID + " INTEGER PRIMARY KEY, "
				+ construct.ERROR_HANDLING.FEATURE_TYPE + " TEXT NOT NULL, " 
				+ construct.ERROR_HANDLING.SERVER_ID + " INTEGER NOT NULL, "
				+ construct.ERROR_HANDLING.CHECKED + " BOOLEAN NOT NULL DEFAULT 0, "
				+ construct.ERROR_HANDLING.DID_CHANGE + " BOOLEAN NOT NULL DEFAULT 0,"
				+ construct.ERROR_HANDLING.MIGRATION_SUCCEEDED + " BOOLEAN NOT NULL DEFAULT 0)";
			
			console.log("_writeSchemasForChecking - create table sql = " + sql);
			
			tx.executeSql(sql, [], function(_tx, e){
				
				console.log("created table, now inserting.");
				
				var sql = "INSERT INTO " + construct.ERROR_HANDLING.TABLE_NAME + " ("
					+ construct.ERROR_HANDLING.FEATURE_TYPE + ", " 
					+ construct.ERROR_HANDLING.SERVER_ID + ") VALUES (?,?);";
				
				console.log("insert errorhandling sql = " + sql);
				
				var writtenCount = 0;
				var total = context.schemas.length;
				var failed = false;
				var errorMessage = null;
				
				var onWrite = function(success, e){
					
					console.log("onWrite: success = " + success);
					
					if(!success){
						failed = true;
						errorMessage = e;
					}
					
					if(++writtenCount === total){
						
						if(!failed){
							context._checkForSchemaChanges(context.schemas.shift());
						}else{
							context._onHandleFailure(e);
						}
					}
				};
				
				for(var i = 0; i < context.schemas.length; i++){
					
					(function(schema){
						
						tx.executeSql(sql, [schema.getFeatureType(), schema.getServerId()], function(_tx, res){
							
							onWrite(true);
						}, function(_tx, e){
							onWrite(false, e);
						});
					})(context.schemas[i]);
				}
				
			}, function(_tx, e){
				
				context._onHandleFailure(e);
			});
		}, context._onHandleFailure);
	};
	
	prototype._writeResults = function(featureType, serverId, didChange, migrationSucceeded, onSuccess, onFailure){
		
		console.log("_writeResults");
		
		var fail = function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		console.log("_writeResults 1");
		
		this.projectDb.transaction(function(tx){
			
			var sql = "UPDATE " + construct.ERROR_HANDLING.TABLE_NAME + " SET " 
				+ construct.ERROR_HANDLING.DID_CHANGE + "=?, " 
				+ construct.ERROR_HANDLING.MIGRATION_SUCCEEDED + "=? WHERE "
				+ construct.ERROR_HANDLING.FEATURE_TYPE + "=?;";
			
			console.log("_writeResults sql = " + sql);
			
			tx.executeSql(sql, [didChange, migrationSucceeded, featureType], function(_tx, res){
				
				if(Arbiter.Util.existsAndNotNull(onSuccess)){
					onSuccess();
				}
			}, function(_tx, e){
				
				fail(e);
			});
		}, fail);
	};
	
	prototype._readResults = function(onSuccess, onFailure){
		
		console.log("_readResults");
		
		var fail = function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		this.projectDb.transaction(function(tx){
			
			var sql = "SELECT * FROM " + construct.ERROR_HANDLING.TABLE_NAME;
			
			tx.executeSql(sql, [], function(_tx, res){
				
				var results = [];
				
				for(var i = 0; i < res.rows.length; i++){
					
					(function(row){
						var result = new Arbiter.SchemaChangeResult(
								row[construct.ERROR_HANDLING.ID],
								row[construct.ERROR_HANDLING.FEATURE_TYPE], 
								row[construct.ERROR_HANDLING.SERVER_ID],
								row[construct.ERROR_HANDLING.CHECKED],
								row[construct.ERROR_HANDLING.DID_CHANGE],
								row[construct.ERROR_HANDLING.MIGRATION_SUCCEEDED]);
						
						results.push(result);
					})(res.rows.item(i));
				}
				
				if(Arbiter.Util.existsAndNotNull(onSuccess)){
					
					onSuccess(results);
				}
			}, function(_tx, e){
				
				fail(e);
			});
		}, fail);
	};
	
	prototype._checkForSchemaChanges = function(schema){
		
		console.log("_checkForSchemaChanges");
		
		var context = this;
		
		if(!Arbiter.Util.existsAndNotNull(schema)){
			
			this._onHandleSuccess();
			
			return;
		}
		
		var schemaChecker = new Arbiter.SchemaChecker(schema, this.featureDb, this.fileSystem, this.wfsVersion);
		
		schemaChecker.checkForSchemaChange(function(didChange, migrationSucceeded){
			
			var onSuccess = function(){
				
				context._checkForSchemaChanges(context.schemas.shift());
			};
			
			var onFailure = function(e){
				
				context._onHandleFailure(e);
			};
			
			context._writeResults(schema.getFeatureType(), schema.getServerId(),
					didChange, migrationSucceeded, onSuccess, onFailure);
		}, function(e){
			
			context._onHandleFailure(e);
		});
	};
})();