Arbiter.PreferencesHelper = (function(){
	var TABLE_NAME = "preferences";
	var KEY = "key";
	var VALUE = "value";
	
	return {
		get: function(key, context, callback){
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			db.transaction(function(tx){
				var sql = "select " + VALUE + " from " 
					+ TABLE_NAME + " where " + KEY + "=?";
				
				tx.executeSql(sql, [key], function(tx, res){
					
					if(res.rows.length > 0 && callback !== null
							&& callback !== undefined){
						
						callback.call(context, res.rows.item(0)[VALUE]);
					}else{
						console.log("There is no preference with key = " + key);
						callback.call(context, null);
					}
				}, function(tx, e){
					console.log("ERROR: Arbiter.PreferencesHelper.get inner", e);
				});
			}, function(e){
				console.log("ERROR: Arbiter.PreferencesHelper.get outer", e);
			});
		},
		
		put: function(key, value, context, callback){
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			db.transaction(function(tx){
				var sql = "INSERT OR REPLACE INTO " + TABLE_NAME + 
					" (" + KEY + "," + VALUE + ") VALUES (?,?);";
				
				tx.executeSql(sql, [key, value], function(tx, res){
					if(callback !== null && callback !== undefined){
						callback.call(context);
					}
				}, function(tx, e){
					console.log("ERROR: Arbiter.PreferencesHelper.put inner", e);
				});
			}, function(e){
				console.log("ERROR: Arbiter.PreferencesHelper.put outer", e);
			});
		}
	};
})();