Arbiter.GeometryColumnsHelper = (function(){
	var GEOMETRY_COLUMNS_TABLE_NAME = "geometry_columns";
	var FEATURE_TABLE_NAME = "feature_table_name";
	var FEATURE_GEOMETRY_COLUMN = "feature_geometry_column";
	var FEATURE_GEOMETRY_TYPE = "feature_geometry_type";
	var FEATURE_GEOMETRY_SRID = "feature_geometry_srid";
	var FEATURE_ENUMERATION = "feature_enumeration";
	
	return {
		/**
    	 * Add the feature type to the GeometryColumns table
    	 */
    	addToGeometryColumns: function(layerSchema, onSuccess, onFailure){
    		var db = Arbiter.FeatureDbHelper.getFeatureDatabase();
    		var context = this;
    		db.transaction(function(tx){
    			context.insertIntoGeometryColumns(tx, layerSchema, onSuccess, onFailure);
    		}, function(e){
    			console.log("ERROR: GeometryColumnsHelper" 
    					+ ".addToGeometryColumns", e);
    			if(Arbiter.Util.funcExists(onFailure)){
    				onFailure(e);
    			}
    		});
    	},
    	
    	insertIntoGeometryColumns: function(tx, schema, onSuccess, onFailure){
			var sql = "INSERT INTO " + GEOMETRY_COLUMNS_TABLE_NAME + "("
				+ FEATURE_TABLE_NAME + ", "
				+ FEATURE_GEOMETRY_COLUMN + ", "
				+ FEATURE_GEOMETRY_TYPE + ", "
				+ FEATURE_GEOMETRY_SRID + ", "
				+ FEATURE_ENUMERATION + ") VALUES (?, ?, ?, ?, ?);";
			
			var values = [schema.getFeatureType(), 
		              schema.getGeometryName(), 
		              schema.getGeometryType(), 
		              schema.getSRID(), 
		              schema.getEnumeration().get()];
			
			tx.executeSql(sql, values, function(tx, res){
				console.log("SUCCESS: insert into geometry columns");
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess();
				}
			}, function(e){
				console.log("ERROR: insert into geometry columns");
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure(e);
				}
			});
    	},
    	
    	// layer is a layer returned by the result of the sqlite plugin
    	getGeometryColumn: function(layer, context, isThereCallback, isNotThereCallback, onFailure){
    		var db = Arbiter.FeatureDbHelper.getFeatureDatabase();
    		var context = this;
    		
    		var featureType = layer[Arbiter.LayersHelper.featureType()];
    		var parsedFeatureType = Arbiter.Util.parseFeatureType(featureType);
    		featureType = parsedFeatureType.featureType;
    		
    		db.transaction(function(tx){
    			
    			var sql = "select * from " + GEOMETRY_COLUMNS_TABLE_NAME
				+ " where " + FEATURE_TABLE_NAME + "=?;";
    			
    			tx.executeSql(sql, [featureType], function(tx, res){
    				
    				if(res.rows.length === 1){
    					isThereCallback.call(context, res.rows.item(0), layer);
    				}else{
    					isNotThereCallback.call(context, layer);
    				}
    			}, function(tx, e){
    				console.log("ERROR: Arbiter.GeometryColumnsHelper"
    						+ ".getGeometryColumn inner", e.stack);
    				
    				if(Arbiter.Util.funcExists(onFailure)){
    					onFailure.call(context, e);
    				}
    			});
    		}, function(e){
    			console.log("ERROR: Arbiter.GeometryColumnsHelper"
						+ ".getGeometryColumn outer", e);
    			
    			if(Arbiter.Util.funcExists(onFailure)){
					onFailure.call(context, e);
				}
    		});
    	},
    	
    	geometryColumnsTableName: function(){
    		return GEOMETRY_COLUMNS_TABLE_NAME;
    	},
    	
    	featureTableName: function(){
    		return FEATURE_TABLE_NAME;
    	},
    	
    	featureGeometryName: function(){
    		return FEATURE_GEOMETRY_COLUMN;
    	},
    	
    	featureGeometryType: function(){
    		return FEATURE_GEOMETRY_TYPE;
    	},
    	
    	featureGeometrySRID: function(){
    		return FEATURE_GEOMETRY_SRID;
    	},
    	
    	featureEnumeration: function(){
    		return FEATURE_ENUMERATION;
    	}
    	
	};
})();