Arbiter.FailedSyncHelper = (function(){
	var TABLE_NAME = "failed_sync";
	
	var getArrayOfFailed = function(res){
		var failedItems = [];
		var failed = null;
		var item = null;
		
		for(var i = 0; i < res.rows.length; i++){
			item = res.rows.item(i);
			
			var key = Arbiter.FailedSyncHelper.KEY;
			var dataType = Arbiter.FailedSyncHelper.DATA_TYPE;
			var syncType = Arbiter.FailedSyncHelper.SYNC_TYPE;
			
			failed = {
				key : item[key],
				dataType: item[dataType],
				syncType: item[syncType]
			};
			
			failedItems.push(failed);
		}
		
		return failedItems;
	};
	
	return {
		
		KEY: "key",
		DATA_TYPE: "data_type",
		SYNC_TYPE: "sync_type",
		LAYER_ID: "layer_id",
		
		DATA_TYPES: {
			VECTOR: 0,
			MEDIA: 1
		},
		
		SYNC_TYPES: {
			UPLOAD: 0,
			DOWNLOAD: 1
		},
		
		/**
		 * returns empty array if none have failed
		 */
		getFailedToSync: function(dataType, syncType, onSuccess, onFailure){
			var context = this;
			
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			db.transaction(function(tx){
				
				tx.executeSql("select * from " + TABLE_NAME + " WHERE " 
						+ context.DATA_TYPE + "=? AND " 
						+ context.SYNC_TYPE + "=?;", [dataType, syncType], function(tx, res){
					
					var arrayOfFailed = getArrayOfFailed(res);
					
					if(Arbiter.Util.funcExists(onSuccess)){
						onSuccess(arrayOfFailed);
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
		
		insert: function(key, dataType, syncType, layerId, onSuccess, onFailure){
			
			console.log("inserting key = " + key + " dataType = " + dataType 
					+ " syncType = " + syncType + " layerId = " + layerId);
			
			var context = this;
			
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			db.transaction(function(tx){
				
				var sql = "INSERT INTO " + TABLE_NAME 
					+ "(" + context.KEY + "," 
					+ context.DATA_TYPE + ","
					+ context.SYNC_TYPE + ") VALUES (?,?,?);"
				
				tx.executeSql(sql, [key, dataType, syncType], function(tx, res){
					
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
		
		remove: function(key, dataType, syncType, layerId, onSuccess, onFailure){
			
			console.log("removing key = " + key + " dataType = " + dataType 
					+ " syncType = " + syncType + " layerId = " + layerId);
			
			var context = this;
			
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			db.transaction(function(tx){
				
				var sql = "DELETE from " + TABLE_NAME + " WHERE "
					+ context.KEY + "=? AND " 
					+ context.DATA_TYPE + "=? AND " 
					+ context.SYNC_TYPE + "=?;";
				
				tx.executeSql(sql, [key, dataType, syncType], function(tx, res){
					
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
		}
	};
})();