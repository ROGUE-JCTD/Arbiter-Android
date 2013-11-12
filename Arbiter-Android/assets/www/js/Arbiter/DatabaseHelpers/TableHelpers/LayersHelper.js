Arbiter.LayersHelper = (function(){
	var LAYERS_TABLE_NAME = "layers";
	var LAYER_ID = "_id";
	var LAYER_TITLE = "layer_title";
	var WORKSPACE = "workspace";
	// Feature type with prefix ex. geonode:roads
	var FEATURE_TYPE = "feature_type"; 
	var SERVER_ID = "server_id";
	var BOUNDING_BOX = "bbox";
	var LAYER_VISIBILITY = "visibility";
	
	var getLayersArray = function(res){
		var layers = [];
		var layer;
		var item;
		
		for(var i = 0; i < res.rows.length; i++){
			item = res.rows.item(i);
			
			layer = {};
			
			layer[LAYER_ID] = item[LAYER_ID];
			layer[WORKSPACE] = item[WORKSPACE];
			layer[FEATURE_TYPE] = item[FEATURE_TYPE];
			layer[SERVER_ID] = item[SERVER_ID];
			layer[LAYER_VISIBILITY] = item[LAYER_VISIBILITY];
			layer[BOUNDING_BOX] = item[BOUNDING_BOX];
			layer[LAYER_TITLE] = item[LAYER_TITLE];
			
			layers.push(layer);
		}
		
		console.log("getLayersArray: ", layers);
		return layers;
	};
	
	return {
		
		loadLayers: function(_context, callback){
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			var context = this;
			
			db.transaction(function(tx){
				context.getLayers(tx, _context, callback);
			}, function(e){
				console.log("ERROR: Arbiter.LayersHelper", e);
			});
		},
		
		getLayers: function(tx, context, callback){
			var sql = "select * from " + LAYERS_TABLE_NAME + ";";
			
			tx.executeSql(sql, [], function(tx, res){
				callback.call(context, getLayersArray(res));
			}, function(tx, e){
				console.log("Arbiter.LayersHelper.getLayers", e);
			});
		},
		
		updateLayer: function(featureType, content, _context, _callback){
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			var context = this;
			
			db.transaction(function(tx){
				context.update(tx, featureType, 
						content, _context, _callback);
			}, function(e){
				
			});
		},
		
		update: function(tx, featureType, content, context, callback){
			console.log("LayersHelper.update: content", content);
			var sql = "UPDATE " + LAYERS_TABLE_NAME + " SET ";
			
			var first = true;
			var values = [];
			
			for(var key in content){
				if(first){
					sql += key + "=?";
					first = false;
				}else{
					sql += ", " + key + "=?";
				}
				
				values.push(content[key]);
			}
			
			sql += " WHERE " + FEATURE_TYPE + "=?;";
			values.push(featureType);
			
			console.log("LayersHelper.update " + sql, values);
			
			tx.executeSql(sql, values, function(tx, res){
				console.log("SUCCESS: LayersHelpler.update" + sql);
				callback.call(context);
			}, function(tx,e){
				console.log("ERROR: LayersHelpler.update" + sql, e);
			});
		},
		
		layerId: function(){
			return LAYER_ID;
		},
		
		workspace: function(){
			return WORKSPACE;
		},
		
		layersTableName: function(){
			return LAYERS_TABLE_NAME;
		},
		
		layerTitle: function(){
			return LAYER_TITLE;
		},
		
		featureType: function(){
			return FEATURE_TYPE;
		},
		
		serverId: function(){
			return SERVER_ID;
		},
		
		boundingBox: function(){
			return BOUNDING_BOX;
		},
		
		layerVisibility: function(){
			return LAYER_VISIBILITY;
		}
		
	};
})();