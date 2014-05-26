Arbiter.PreferencesHelper = (function(){
	var TABLE_NAME = "preferences";
	var KEY = "key";
	var VALUE = "value";
	
	return {
		get: function(db, key, context, onSuccess, onFailure){
			//var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			db.transaction(function(tx){
				var sql = "select " + VALUE + " from " 
					+ TABLE_NAME + " where " + KEY + "=?";
				
				tx.executeSql(sql, [key], function(tx, res){
					
					if(Arbiter.Util.funcExists(onSuccess)){
						if(res.rows.length > 0){
							onSuccess.call(context, res.rows.item(0)[VALUE]);
						}else{
							console.log("There is no preference with key = " + key);
							onSuccess.call(context, null);
						}
					}
				}, function(tx, e){
					console.log("ERROR: Arbiter.PreferencesHelper.get inner", e);
					
					if(Arbiter.Util.funcExists(onFailure)){
						onFailure.call(context, e);
					}
				});
			}, function(e){
				console.log("ERROR: Arbiter.PreferencesHelper.get outer", e.stack);
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure.call(context, e);
				}
			});
		},
		
		put: function(db, key, value, context, onSuccess, onFailure){
			//var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			db.transaction(function(tx){
				var sql = "INSERT OR REPLACE INTO " + TABLE_NAME + 
					" (" + KEY + "," + VALUE + ") VALUES (?,?);";
				
				tx.executeSql(sql, [key, value], function(tx, res){
					if(Arbiter.Util.funcExists(onSuccess)){
						onSuccess.call(context);
					}
				}, function(tx, e){
					console.log("ERROR: Arbiter.PreferencesHelper.put inner", e);
					
					if(Arbiter.Util.funcExists(onFailure)){
						onFailure.call(context, e);
					}
				});
			}, function(e){
				console.log("ERROR: Arbiter.PreferencesHelper.put outer", e.stack);
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure.call(context, e);
				}
			});
		},
		
		remove: function(db, key, context, onSuccess, onFailure){
			//var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			db.transaction(function(tx){
				var sql = "DELETE FROM " + TABLE_NAME 
					+ " WHERE " + KEY + "=?";
				
				tx.executeSql(sql, [key], function(tx, res){
					if(Arbiter.Util.funcExists(onSuccess)){
						onSuccess.call(context);
					}
				}, function(tx, e){
					console.log("ERROR: Arbiter.PreferencesHelper.remove inner", e);
					
					if(Arbiter.Util.funcExists(onFailure)){
						onFailure.call(context, e);
					}
				});
			}, function(e){
				console.log("ERROR: Arbiter.PreferencesHelper.remove outer", e);
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure.call(context, e);
				}
			});
		}
	};
})();