Arbiter.FeatureTableHelper = (function(){
	var wktFormatter = new OpenLayers.Format.WKT();
	var loadedCount = 0;
	var layersCount = 0;
	
	var getFeatureInNativeProjection = function(geometrySRID, nativeSRID, feature){
		if(geometrySRID === nativeSRID){
			return feature;
		}
		
		var clonedFeature = feature.clone();
		clonedFeature.geometry.transform(
				new OpenLayers.Projection(geometrySRID),
				new OpenLayers.Projection(nativeSRID));
		
		return clonedFeature;
	};
	
	var incrementLoadedCount = function(){
		loadedCount++;
	};
	
	var doneLoadingSchemas = function(){
		return layersCount === loadedCount;
	};
	
	var getFeatures = function(tx, schema, context, onSuccess, onFailure){
		var sql = "select * from " + schema.getFeatureType() + ";";
		
		tx.executeSql(sql, [], function(tx, res){
			for(var i = 0; i < res.rows.length; i++){
				onSuccess.call(context, res.rows.item(i), i, res.rows.length);
			}
		}, function(tx, e){
			console.log("ERROR: Arbiter.FeatureTableHelper.getFeatures", e);
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure.call(context, e);
			}
		});
	};
	
	return {
		
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
    			+ "arbiter_id integer primary key, "
    			+ schema.getGeometryName() + " text not null";
    		
    		var attributes = schema.getAttributes();
    		
    		for(var i = 0; i < attributes.length; i++){
    			sql += ", '" + attributes[i].getName() + "' " + attributes[i].getType();
    			
    			if(!attributes[i].isNillable()){
    				sql += " not null";
    			}
    		}
    		
    		sql += ");";
    		
    		tx.executeSql(sql, [], function(tx, res){
    			console.log("SUCCESS: create table - " + sql);
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
    	insertFeatures: function(schema, srid, features, onSuccess, onFailure){
    		var insertCount = 0;
    		var featureCount = features.length;
    		
    		var db = Arbiter.FeatureDbHelper.getFeatureDatabase();
    		var context = this;
    		
    		db.transaction(function(tx){
    			for(var i = 0; i < featureCount; i++){
    				context.insertFeature(tx, schema, srid, 
    						features[i], function(){
    					
    					insertCount++;
    					
    					if(insertCount === featureCount && 
    							Arbiter.Util.funcExists(onSuccess)){
    						
    						console.log("calling insertFeatures callback!");
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
    	insertFeature: function(tx, schema, srid, feature, onSuccess, onFailure){
    		var sql = "INSERT INTO " + schema.getFeatureType()
    			+ " (" + schema.getGeometryName();
    		
    		var attributes = schema.getAttributes();
    		
    		// Adding as many question marks as there are attributes
    		var questionMarks = "?";
    		var values = [];
    		var attributeName = null;
    		
    		values.push(wktFormatter.write(getFeatureInNativeProjection(srid, 
    				schema.getSRID(), feature)));
    		
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
    			console.log("SUCCESS: feature successfully inserted", feature);
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
    				var url = Arbiter.Util.Servers.getServer(serverId);
    				
    				var schema = new Arbiter.Util.LayerSchema(url, layer[helper.workspace()],
    						layer[helper.featureType()], layer[helper.layerVisibility()], serverId);
    				Arbiter.putLayerSchema(layer[helper.layerId()], schema);
    				
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
			var helper = Arbiter.GeometryColumnsHelper;
			var layersHelper = Arbiter.LayersHelper;
			
			var serverId = layer[layersHelper.serverId()];
			var server = Arbiter.Util.Servers.getServer(serverId);
			
			var url = server.getUrl();
			var srid = row[helper.featureGeometrySRID()];
			var geometryName = row[helper.featureGeometryName()];
			var geometryType = row[helper.featureGeometryType()];
			var enumeration = row[helper.featureEnumeration()];
			
			var workspace = layer[layersHelper.workspace()];
			
			var parsedFeatureType = Arbiter.Util.parseFeatureType(layer[layersHelper.featureType()]);
			var featureType = parsedFeatureType.featureType;
			var prefix = parsedFeatureType.prefix;
			
			var visibility = layer[layersHelper.layerVisibility()];
			
			var sql = "PRAGMA table_info(" + featureType + ");";
			
			tx.executeSql(sql, [], function(tx, res){
				var attributes = [];
				var row = null;
				
				for(var i = 0; i < res.rows.length; i++){
					row = res.rows.item(i);
					attributes.push(new Arbiter.Util.Attribute(row.name, 
							row.type, row.notnull));
				}
				
				var schema = new Arbiter.Util.LayerSchema(url,
						workspace, prefix, featureType, srid, geometryName,
						geometryType, enumeration, attributes, visibility, serverId);
				
				Arbiter.putLayerSchema(layer[layersHelper.layerId()], schema);
				
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