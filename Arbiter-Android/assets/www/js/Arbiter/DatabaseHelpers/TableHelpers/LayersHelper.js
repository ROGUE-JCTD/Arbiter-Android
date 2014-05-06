Arbiter.LayersHelper = (function(){
	var LAYERS_TABLE_NAME = "layers";
	var LAYER_ID = "_id";
	var LAYER_TITLE = "layer_title";
	var WORKSPACE = "workspace";
	// Feature type with prefix ex. geonode:roads
	var FEATURE_TYPE = "feature_type"; 
	var SERVER_ID = "server_id";
	var BOUNDING_BOX = "bbox";
	var COLOR = "color";
	var LAYER_VISIBILITY = "visibility";
	var LAYER_ORDER = "layerOrder";
	var READ_ONLY = "readOnly";
	
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
			layer[COLOR] = item[COLOR];
			layer[LAYER_TITLE] = item[LAYER_TITLE];
			layer[READ_ONLY] = item[READ_ONLY];
			
			layers.push(layer);
		}
		
		return layers;
	};
	
	return {
		
		loadLayers: function(_context, onSuccess, onFailure){
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			var context = this;
			
			db.transaction(function(tx){
				context.getLayers(tx, _context, onSuccess, onFailure);
			}, function(e){
				console.log("ERROR: Arbiter.LayersHelper", e.stack);
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure.call(_context, e);
				}
			});
		},
		
		getLayers: function(tx, context, onSuccess, onFailure){
			var sql = "select * from " + LAYERS_TABLE_NAME 
				+ " ORDER BY " + LAYER_ORDER + ";";
			
			tx.executeSql(sql, [], function(tx, res){
				
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess.call(context, getLayersArray(res));
				}
			}, function(tx, e){
				console.log("Arbiter.LayersHelper.getLayers", e);
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure.call(context, e);
				}
			});
		},
		
		updateLayer: function(featureType, content, _context, _onSuccess, _onFailure){
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			var context = this;
			
			db.transaction(function(tx){
				context.update(tx, featureType, 
						content, _context, _onSuccess, _onFailure);
			}, function(e){
				if(Arbiter.Util.funcExists(_onFailure)){
					_onFailure.call(_context, e);
				}
			});
		},
		
		update: function(tx, featureType, content, context, onSuccess, onFailure){
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
				
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess.call(context);
				}
			}, function(tx,e){
				console.log("ERROR: LayersHelpler.update" + sql, e);
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure.call(context, e);
				}
			});
		},
		
		deleteLayer: function(layerId, onSuccess, onFailure){
			
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			var fail = function(e){
				
				if(Arbiter.Util.existsAndNotNull(onFailure)){
					onFailure(e);
				}
			};
			
			db.transaction(function(tx){
				
				var sql = "DELETE FROM " + LAYERS_TABLE_NAME + " WHERE " + LAYER_ID + "=?;";
				
				tx.executeSql(sql, [layerId], function(tx, res){
					
					if(Arbiter.Util.existsAndNotNull(onSuccess)){
						onSuccess();
					}
				}, function(tx, e){
					fail(e);
				});
			}, fail);
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
		
		color: function(){
			return COLOR;
		},
		
		layerVisibility: function(){
			return LAYER_VISIBILITY;
		},
		
		readOnly: function(){
			return READ_ONLY;
		}
		
	};
})();