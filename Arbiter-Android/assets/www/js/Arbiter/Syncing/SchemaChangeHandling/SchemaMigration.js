(function(){
	
	Arbiter.SchemaMigration = function(describeFeatureTypeResults, featureDb, oldSchema){
		this.describeFeatureTypeResults = describeFeatureTypeResults;
		this.featureDb = featureDb;
		this._onMigrationSuccess = null;
		this._onMigrationFailure = null;
		this.oldSchema = oldSchema;
		this.newSchema = null;
		this.tempTableName = null;
		this.backupTableName = null;
	};
	
	var prototype = Arbiter.SchemaMigration.prototype;
	
	var construct = Arbiter.SchemaMigration;
	
	construct.MIGRATION_FAILED = "MigrationFailed";
	
	prototype.migrate = function(onSuccess, onFailure){
		
		this._onMigrationSuccess = function(){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess();
			}
		};
		
		this._onMigrationFailure = function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		// If there are no feature types, return.
		if(!this.describeFeatureTypeResults.featureTypes || !this.describeFeatureTypeResults.featureTypes.length){
			
			this._onMigrationSuccess();
			
			return;
		}
		
		try{
			this.newSchema = new Arbiter.Util.LayerSchema(this.oldSchema.getLayerId(), this.oldSchema.getUrl(),
					this.describeFeatureTypeResults.targetNamespace, this.oldSchema.getFeatureTypeWithPrefix(), this.oldSchema.getSRID(),
					this.describeFeatureTypeResults.featureTypes[0].properties, this.oldSchema.getServerId(),
					this.oldSchema.getServerType(), this.oldSchema.getColor(), this.oldSchema.isReadOnly());
			
			this.tempTableName = this._getTempTableName(this.newSchema.getFeatureType());
			
			this.backupTableName = this._getBackupTableName(this.oldSchema.getFeatureType());
		}catch(e){
			console.log(e.stack);
			
			throw e;
		}
		
		// Create new table
		
		// try to move values from old table to new table
		
		// if it succeeds, delete the old table, rename the new table, and replace the entry in the geometryColumns table
		
		// if it fails, delete the new table
		
		this._createNewTable();
	};
	
	prototype._getBackupTableName = function(featureType){
		
		return featureType + "_ARBITER_BACKUP_TABLE_";
	};
	
	prototype._getTempTableName = function(featureType){
		
		return featureType + "_ARBITER_TEMPORARY_TABLE_";
	};
	
	prototype._createNewTable = function(){
		
		console.log("_createNewTable");
		
		var context = this;
		
		this.featureDb.transaction(function(tx){
			
			var sql = "CREATE TABLE IF NOT EXISTS "
    			+ context.tempTableName + " ("
    			+ Arbiter.FeatureTableHelper.ID + " integer primary key, "
    			+ Arbiter.FeatureTableHelper.FID + " text, "
    			+ Arbiter.FeatureTableHelper.SYNC_STATE + " integer not null, "
    			+ Arbiter.FeatureTableHelper.MODIFIED_STATE + " integer not null, "
    			+ context.newSchema.getGeometryName() + " text not null";
			
    		var attributes = context.newSchema.getAttributes();
    		
    		var nillable = null;
    		
    		for(var i = 0; i < attributes.length; i++){
    			sql += ", '" + attributes[i].getName() + "' " + attributes[i].getType();
    			
    			nillable = attributes[i].isNillable();
    			
    			if(nillable === false 
    					|| nillable === "false"){
    				
    				sql += " not null";
    			}
    		}
    		
    		sql += ");";
    		
    		console.log("_createNewTable sql = " + sql);
    		
    		tx.executeSql(sql, [], function(_tx, res){
    			
    			console.log("_createNewTable success");
    			
    			context._getRowsFromOldTable(_tx);
    		}, function(e){
    			
    			console.log("_createNewTable failure: ", e.stack);
    			
    			context._onMigrationFailure(e);
    		});
		}, function(e){
			
			console.log("_createNewTable failure: ", e.stack);
			
			context._onMigrationFailure(e);
		});
	};
	
	prototype._getRowsFromOldTable = function(tx){
		
		console.log("_getRowsFromOldTable");
		
		var context = this;
		
		var sql = "SELECT * FROM " + this.tempTableName;
		
		tx.executeSql(sql, [], function(_tx, res){
			
			console.log("_getRowsFromOldTable success");
			
			context._copyRowsToNewTable(tx, res);
		}, function(_tx, e){
			
			console.log("_getRowsFromOldTable failure: ", e.stack);
			
			context._onMigrationFailure(e);
		});
	};
	
	prototype._copyRowsToNewTable = function(tx, res){
		
		console.log("_copyRowsToNewTable");
		
		var context = this;
		var finishedCopyingCount = 0;
		var totalToCopy = res.rows.length;
		
		var failed = false;
		
		console.log("_copyRowsToNewTable... 1");
		
		var onCopy = function(success){
			console.log("_copyRowsToNewTable onCopy");
			
			if(!success){
				failed = true;
			}
			
			if(++finishedCopyingCount === totalToCopy){
				
				if(!failed){
					
					console.log("_copyRowsToNewTable success");
					
					context._handleSuccessfulMigration();
				}else{
					
					console.log("_copyRowsToNewTable failure");
					
					context._handleFailedMigration();
				}
			}
		};
		
		console.log("_copyRowsToNewTable... 2");
		
		if(res.rows.length === 0){
			
			context._handleSuccessfulMigration();
		}else{
			for(var i = 0; i < res.rows.length; i++){
				
				(function(row){
					
					console.log("_copyRowsToNewTable... 3 - " + i);
					
					var sql = "INSERT INTO " + context.tempTableName 
						+ "(" + Arbiter.FeatureTableHelper.ID + ", "
						+ Arbiter.FeatureTableHelper.FID + ", "
						+ context.newSchema.getGeometryName() + ", "
						+ Arbiter.FeatureTableHelper.MODIFIED_STATE + ", "
						+ Arbiter.FeatureTableHelper.SYNC_STATE;
	    		
		    		var attributes = context.newSchema.getAttributes();
		    		
		    		// Adding as many question marks as there are attributes
		    		var questionMarks = "?,?,?,?,?";
		    		var values = [];
		    		
		    		values.push(row[Arbiter.FeatureTableHelper.ID]);
		    		values.push(row[Arbiter.FeatureTableHelper.FID]);
		    		values.push(row[context.newSchema.getGeometryName()]);
		    		values.push(row[Arbiter.FeatureTableHelper.MODIFIED_STATE]);
		    		values.push(row[Arbiter.FeatureTableHelper.SYNC_STATE]);
		    		
		    		var attributeName = null;
		    		
		    		// Push the attributes
		    		for(var i = 0; i < attributes.length; i++){
		    			attributeName = attributes[i].getName();
		    			sql += ", " + attributeName;
		    			
		    			values.push(row[attributeName]);
		    			// Add a question mark to represent the value of 
		    			// the attribute
		    			questionMarks += ",?";
		    		}
		    		
		    		sql += ") VALUES (" + questionMarks + ");";
		    		
		    		console.log("_copyRowsToNewTable sql = " + sql);
		    		
		    		tx.executeSql(sql, values, function(_tx, res){
		    			
		    			onCopy(true);
		    		}, function(_tx, e){
		    			
		    			onCopy(false);
		    		});
					
				})(res.rows.item(i));
			}
		}
	};
	
	prototype._handleSuccessfulMigration = function(){
		
		console.log("_handleSuccessfulMigration");
		
		var context = this;
		
		this.featureDb.transaction(function(tx){
			
			// Backup the old table just in case...
			var sql = "ALTER TABLE " + context.oldSchema.getFeatureType() + " RENAME TO " + context.backupTableName;
			
			tx.executeSql(sql, [], function(_tx, res){
				
				// Then rename the temp table
				var sql = "ALTER TABLE " + context.tempTableName + " RENAME TO " + context.newSchema.getFeatureType();
				
				tx.executeSql(sql, [], function(_tx, res){
					
					// Now replace the geometry columns row
					var sql = "UPDATE " + Arbiter.GeometryColumnsHelper.geometryColumnsTableName() + " SET " 
						+ Arbiter.GeometryColumnsHelper.featureGeometryName() + "=?, " 
						+ Arbiter.GeometryColumnsHelper.featureGeometryType() + "=?,"
						+ Arbiter.GeometryColumnsHelper.featureGeometrySRID() + "=?,"
						+ Arbiter.GeometryColumnsHelper.featureEnumeration() + "=? "
						+ " WHERE " + Arbiter.GeometryColumnsHelper.featureTableName() + "=?";
					
					var values = [];
					
					values.push(context.newSchema.getGeometryName());
					values.push(context.newSchema.getGeometryType());
					values.push(context.newSchema.getSRID());
					values.push(context.newSchema.getEnumeration().get());
					values.push(context.newSchema.getFeatureType());
					
					tx.executeSql(sql, values, function(_tx, res){
						
						// Finally, delete the backup table
						var sql = "DROP TABLE " + context.backupTableName;
						
						tx.executeSql(sql, [], function(_tx, res){
							
							context._onMigrationSuccess();
						}, function(_tx, e){
							
							context._onMigrationFailure(e);
						});
					}, function(_tx, e){
						
						context._onMigrationFailure(e);
					});
				}, function(_tx, e){
					
					context._onMigrationFailure(e);
				});
			}, function(_tx, e){
				
				context._onMigrationFailure(e);
			});
			
		}, function(e){
			
			context._onMigrationFailure(e);
		});
	};
	
	prototype._handleFailedMigration = function(){
		
		var context = this;
		
		this.featureDb.transaction(function(tx){
			
			var sql = "DROP TABLE IF EXISTS " + context.tempTableName;
			
			tx.executeSql(sql, [], function(_tx, res){
				
				context._onMigrationFailure(construct.MIGRATION_FAILED);
			}, function(_tx, e){
				
				context._onMigrationFailure(e);
			});
		}, function(e){
			
			context._onMigrationFailure(e);
		});
	};
})();