Arbiter.FeatureTableHelper = (function(){
	
	var wktFormatter = new OpenLayers.Format.WKT();
	var loadedCount = 0;
	var layersCount = 0;
		
	var incrementLoadedCount = function(){
		loadedCount++;
	};
	
	var doneLoadingSchemas = function(){
		return layersCount === loadedCount;
	};
	
	var getFeatures = function(tx, schema, context, onSuccess, onFailure){
		
		var sql = "select * from " + schema.getFeatureType() + ";";
		
		tx.executeSql(sql, [], function(tx, res){
			if(res.rows.length > 0){
				for(var i = 0; i < res.rows.length; i++){
					onSuccess.call(context, res.rows.item(i), i, res.rows.length);
				}
			}else{
				onSuccess.call(context, null, null, 0);
			}
		}, function(tx, e){
			console.log("ERROR: Arbiter.FeatureTableHelper.getFeatures", e);
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure.call(context, e);
			}
		});
	};
	
	return {
		ID : "arbiter_id",
		FID : "fid",
		SYNC_STATE: "sync_state",
		MODIFIED_STATE: "modified_state",
		FOTOS: "fotos",
		PHOTOS: "photos",
		PART_OF_MULTI: "partOfMulti",
		
		MODIFIED_STATES: {
			NONE: 0,
			INSERTED: 1,
			MODIFIED: 2,
			DELETED: 3
		},
		
		SYNC_STATES: {
			NOT_SYNCED: 0,
			SYNCED: 1
		},
		
		// Make sure the key in the feature table isn't there as metadata
		isMetaKey: function(key){
				
			if(key === Arbiter.FeatureTableHelper.ID 
					|| key === Arbiter.FeatureTableHelper.SYNC_STATE 
					|| key === Arbiter.FeatureTableHelper.MODIFIED_STATE){
				
				return true;
			}
		
			return false;
		},
		
		/**
    	 * Create the table
    	 */
    	createFeatureTable: function(schema, onSuccess, onFailure){
    		var db = Arbiter.FeatureDbHelper.getFeatureDatabase();
    		var context = this;
    		
    		db.transaction(function(tx){
    			context.createTable(tx, schema, onSuccess, onFailure);
    		}, function(e){
    			console.log("ERROR: Arbiter.FeatureTableHelper"
    					+ ".createFeatureTable", e);
    			
    			if(Arbiter.Util.funcExists(onFailure)){
    				onFailure(e);
    			}
    		});
    	},
    	
    	createTable: function(tx, schema, onSuccess, onFailure){
    		var sql = "CREATE TABLE IF NOT EXISTS "
    			+ schema.getFeatureType() + " ("
    			+ this.ID + " integer primary key, "
    			+ this.FID + " text, "
    			+ this.SYNC_STATE + " integer not null, "
    			+ this.MODIFIED_STATE + " integer not null, "
    			+ schema.getGeometryName() + " text not null";
    		
    		var attributes = schema.getAttributes();
    		
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
    		
    		tx.executeSql(sql, [], function(tx, res){
    			
    			if(Arbiter.Util.funcExists(onSuccess)){
    				onSuccess();
    			}
    		}, function(e){
    			console.log("ERROR: create table - " + sql);
    			if(Arbiter.Util.funcExists(onFailure)){
    				onFailure(e);
    			}
    		});
    	},
    	
    	/**
    	 * srid is the srid the geometries 
    	 * are in when they are being inserted.
    	 */
    	insertFeatures: function(schema, srid, features,
    			isDownload, onSuccess, onFailure){
    		
    		var insertCount = 0;
    		var featureCount = features.length;
    		
    		// If the featureCount is 0, increment the
    		// insertCount and execute the onSuccess
    		// callback if done.
    		if(featureCount === 0){
    			
    			if(((++insertCount === featureCount) || (featureCount === 0)) && 
						Arbiter.Util.funcExists(onSuccess)){
					
					onSuccess();
				}
    			
    			return;
    		}
    		
    		var db = Arbiter.FeatureDbHelper.getFeatureDatabase();
    		var context = this;
    		
    		db.transaction(function(tx){
    			for(var i = 0; i < featureCount; i++){
    				context.insertFeature(tx, schema, srid, 
    						features[i], isDownload, function(){
    					
    					if(++insertCount === featureCount && 
    							Arbiter.Util.funcExists(onSuccess)){
    						
    						onSuccess();
    					}
    				}, onFailure);
    			}
    		}, function(e){
    			console.log("ERROR: Arbiter.FeatureTableHelper"
    					+ ".insertFeatures", e);
    			
    			if(Arbiter.Util.funcExists(onFailure)){
    				onFailure(e);
    			}
    		});
    	},
    	
    	/**
    	 * Insert feature into the feature table
    	 * srid is the srid the geometry
    	 */
    	insertFeature: function(tx, schema, srid, feature,
    			isDownload, onSuccess, onFailure){
    		
    		var sql = "INSERT INTO " + schema.getFeatureType()
    			+ " (" + schema.getGeometryName() + ", "
    			+ this.MODIFIED_STATE + ", "
    			+ this.SYNC_STATE;
    		
    		var attributes = schema.getAttributes();
    		
    		// Adding as many question marks as there are attributes
    		var questionMarks = "?,?,?";
    		var values = [];
    		var attributeName = null;
    		
    		// Push the geometry
    		var nativeFeature = Arbiter.Util.getFeatureInNativeProjection(srid, 
    				schema.getSRID(), feature);
    		
    		var wkt = wktFormatter.write(nativeFeature);
    		
    		values.push(wkt);
    		
    		// Push the modified state
    		if(isDownload){
    			
    			values.push(this.MODIFIED_STATES.NONE);
        		
        		values.push(this.SYNC_STATES.SYNCED);
        		
        		// TODO: Assuming that the primary key of the table is fid,
        		// Add insert the FID into the db
    			sql += ", " + this.FID;
    			
    			questionMarks += ",?";
    			
    			values.push(feature.fid);
    		}else{
    			values.push(this.MODIFIED_STATES.INSERTED);
        		
        		values.push(this.SYNC_STATES.NOT_SYNCED);
    		}
    		
    		// Push the attributes
    		for(var i = 0; i < attributes.length; i++){
    			attributeName = attributes[i].getName();
    			sql += ", " + attributeName;
    			
    			values.push(feature.attributes[attributeName]);
    			// Add a question mark to represent the value of 
    			// the attribute
    			questionMarks += ", ?";
    		}
    		
    		sql += ") VALUES (" + questionMarks + ");";
    		
    		tx.executeSql(sql, values, function(tx, res){
    			
    			if(Arbiter.Util.funcExists(onSuccess)){
    				onSuccess.call();
    			}
    		}, function(tx, e){
    			console.log("ERROR: Arbiter.FeatureTableHelper" 
    					+ ".insertFeature " + sql, e);
    			
    			if(Arbiter.Util.funcExists(onFailure)){
    				onFailure(e);
    			}
    		});
    	},
    	
    	updateFeaturesSyncStatus: function(featureType, onSuccess, onFailure){
    		var db = Arbiter.FeatureDbHelper.getFeatureDatabase();
    		var context = this;
    		
    		db.transaction(function(tx){
    			
    			var sql = "UPDATE " + featureType + " SET " 
    				+ context.SYNC_STATE + "=? WHERE " 
    				+ context.SYNC_STATE + "=?";
    			
    			var values = [context.SYNC_STATES.SYNCED, context.SYNC_STATES.NOT_SYNCED];
    			
    			tx.executeSql(sql, values, function(tx, res){
    				
    				if(Arbiter.Util.funcExists(onSuccess)){
        				onSuccess();
        			}
    			}, function(tx, e){
    				
    				if(Arbiter.Util.funcExists(onFailure)){
        				onFailure(e);
        			}
    			});
    			
    		}, function(e){
    			
    			if(Arbiter.Util.funcExists(onFailure)){
    				onFailure(e);
    			}
    		});
    	},
    	
    	getUnsyncedFeatureCount: function(featureType, onSuccess, onFailure){
    		
    		var db = Arbiter.FeatureDbHelper.getFeatureDatabase();
    		var context = this;
    		
    		var fail = function(e){
    			
    			if(Arbiter.Util.existsAndNotNull(onFailure)){
    				onFailure(e);
    			}
    		};
    		
    		db.transaction(function(tx){
    			
    			var sql = "select count(*) from " + featureType + " where " + context.SYNC_STATE + "=?";
    			
    			tx.executeSql(sql, [context.SYNC_STATES.NOT_SYNCED], function(_tx, res){
    				
    				var count = 0;
    				
    				if(res.rows.length > 0){
    					count = res.rows.item(0)["count(*)"];
    				}
    				
    				if(Arbiter.Util.existsAndNotNull(onSuccess)){
    					onSuccess(count);
    				}
    			}, function(_tx, e){
    				fail(e);
    			});
    		}, fail);
    	},
    	
    	clearFeatureTable: function(schema, onSuccess, onFailure){
    		var db = Arbiter.FeatureDbHelper.getFeatureDatabase();
    		
    		db.transaction(function(tx){
    			
    			var sql = "DELETE FROM " + schema.getFeatureType() + ";";
    			
    			tx.executeSql(sql, [], function(tx, res){
    				
    				if(Arbiter.Util.funcExists(onSuccess)){
    					onSuccess();
    				}
    			}, function(tx, e){
    				if(Arbiter.Util.funcExists(onFailure)){
    					onFailure(e);
    				}
    			});
    			
    		}, function(e){
    			
    			if(Arbiter.Util.funcExists(onFailure)){
    				onFailure(e);
    			}
    		});
    	},
    	
    	removeFeatures: function(features, schema, onSuccess, onFailure){
    		var deleteCount = 0;
    		var featureCount = features.length;
    		
    		var db = Arbiter.FeatureDbHelper.getFeatureDatabase();
    		var context = this;
    		
    		db.transaction(function(tx){
    			
    			for(var i = 0; i < featureCount; i++){
    				context.removeFeature(tx, schema, features[i], function(){
    					
    					if(++deleteCount === featureCount && 
    							Arbiter.Util.funcExists(onSuccess)){
    						
    						onSuccess();
    					}
    				}, onFailure);
    			}
    		}, function(e){
    			console.log("ERROR: Arbiter.FeatureTableHelper"
    					+ ".deleteFeatures", e);
    			
    			if(Arbiter.Util.funcExists(onFailure)){
    				onFailure(e);
    			}
    		});
    	},
    	
    	removeFeature: function(tx, schema, feature, onSuccess, onFailure){
    		var sql = "DELETE FROM " + schema.getFeatureType()
    			+ " WHERE " + this.ID + "=?";
    		
    		tx.executeSql(sql, [feature.metadata[this.ID]], function(tx, res){
    			if(Arbiter.Util.funcExists(onSuccess)){
    				onSuccess();
    			}
    		}, function(tx, e){
    			if(Arbiter.Util.funcExists(onFailure)){
    				onFailure(e);
    			}
    		});
    	},
    	
    	// layers is an array of objects with key value pairs
    	// corresponding to the Arbiter.LayersHelper constants
    	loadLayerSchemas: function(layers, onSuccess, onFailure){
    		var db = Arbiter.FeatureDbHelper.getFeatureDatabase();
    		var context = this;
    		
    		// Reset the layer schema
    		Arbiter.resetLayerSchemas();
    		
    		// When the loadedCount equals the layerCount
    		// execute the callback
    		loadedCount = 0;
    		layersCount = layers.length;
    		
    		// If there are no layers, call the callback
    		if(doneLoadingSchemas() && 
					Arbiter.Util.funcExists(onSuccess)){
				onSuccess.call();
			}
    		
    		for(var i = 0; i < layers.length; i++){
    			
    			// Get the GeometryColumn info for the featureType
    			Arbiter.GeometryColumnsHelper.
    				getGeometryColumn(layers[i], context, function(row, layer){
    				
					db.transaction(function(tx){
		    			context.getLayerSchema(tx, row, layer, function(){
		    				
		    				incrementLoadedCount();
		    				
		    				if(doneLoadingSchemas() && 
		    						Arbiter.Util.funcExists(onSuccess)){
		    					onSuccess.call();
		    				}
		    			}, onFailure);
		    		}, function(e){
		    			console.log("ERROR: Arbiter.FeatureTableHelper" +
		    					".loadLayerSchemas", e);
		    			
		    			if(Arbiter.Util.funcExists(onFailure)){
		    				onFailure(e);
		    			}
		    		});
    			}, function(layer){ // If there is no geometry column, then the schema is not editable
    				
    				var helper = Arbiter.LayersHelper;
    				var serverId = layer[helper.serverId()]
    				var server = Arbiter.Util.Servers.getServer(serverId);
    				var url = server.getUrl();
    				var serverType = server.getType();
    				var color = layer[helper.color()];
    				
    				var layerId = layer[helper.layerId()];
    				var featureType = layer[helper.featureType()];
    				
    				var schema = new Arbiter.Util.LayerSchema(layerId, url, layer[helper.workspace()],
    						featureType, layer[helper.layerVisibility()], serverId, serverType, color);
    				Arbiter.putLayerSchema(layerId, schema);
    				
    				incrementLoadedCount();
    				
    				if(doneLoadingSchemas() && 
    						Arbiter.Util.funcExists(onSuccess)){
    					onSuccess.call();
    				}
    			}, onFailure);
    		}
		},
		
		// layer is from the results of a query with the sqlite plugin
		getLayerSchema: function(tx, row, layer, onSuccess, onFailure){
			var context = this;
			
			var helper = Arbiter.GeometryColumnsHelper;
			var layersHelper = Arbiter.LayersHelper;
			
			var serverId = layer[layersHelper.serverId()];
			var server = Arbiter.Util.Servers.getServer(serverId);
			
			var serverType = server.getType();
			
			var url = server.getUrl();
			var srid = row[helper.featureGeometrySRID()];
			var geometryName = row[helper.featureGeometryName()];
			var geometryType = row[helper.featureGeometryType()];
			var enumeration = row[helper.featureEnumeration()];
			var color = layer[layersHelper.color()];
			var isReadOnly = layer[layersHelper.readOnly()];
			var workspace = layer[layersHelper.workspace()];
			
			var parsedFeatureType = Arbiter.Util.parseFeatureType(layer[layersHelper.featureType()]);
			var featureType = parsedFeatureType.featureType;
			var prefix = parsedFeatureType.prefix;
			
			var visibility = layer[layersHelper.layerVisibility()];
			
			var sql = "PRAGMA table_info(" + featureType + ");";
			
			tx.executeSql(sql, [], function(tx, res){
				var attributes = [];
				var row = null;
				
				var mediaColumn = null;
				
				for(var i = 0; i < res.rows.length; i++){
					row = res.rows.item(i);
					
					if(row.name !== context.ID && row.name !== geometryName){
						if(row.name === Arbiter.FeatureTableHelper.PHOTOS 
								|| row.name === Arbiter.FeatureTableHelper.FOTOS){
							mediaColumn = row.name;
						}
						
						attributes.push(new Arbiter.Util.Attribute(row.name, 
								row.type, row.notnull));
					}
				}
				
				var layerId = layer[layersHelper.layerId()];
				
				var schema = new Arbiter.Util.LayerSchema(layerId, url,
						workspace, prefix, featureType, srid, geometryName,
						geometryType, enumeration, attributes,
						visibility, serverId, serverType, mediaColumn, color, isReadOnly);
				
				Arbiter.putLayerSchema(layerId, schema);
				
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess.call();
				}
			}, function(tx, e){
				console.log("ERROR: FeatureTableHelper.getLayerSchema", e)
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure(e);
				}
			});
			
		},
		
		/**
		 * onSuccess should expect a feature, the current index on the feature,
		 * and number of features being iterated over.
		 */
		loadFeatures: function(schema, context, onSuccess, onFailure){
			var db = Arbiter.FeatureDbHelper.getFeatureDatabase();
			
			db.transaction(function(tx){
				getFeatures(tx, schema, context, onSuccess, onFailure);
			}, function(e){
				console.log("ERROR: Arbiter.FeatureTableHelper"
						+ ".loadFeatures", e);
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure.call(context, e);
				}
			});
		}
	};
})();
