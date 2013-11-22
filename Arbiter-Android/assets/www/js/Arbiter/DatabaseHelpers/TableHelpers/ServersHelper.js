Arbiter.ServersHelper = (function(){
	var _ID = "_id"
	var SERVER_NAME = "server_name";
	var SERVER_URL = "url";
	var SERVER_USERNAME = "username";
	var SERVER_PASSWORD = "password";
	var SERVERS_TABLE_NAME = "servers";
	
	return {
		loadServers: function(context, onSuccess, onFailure){
			var db = Arbiter.ApplicationDbHelper.getDatabase();
			var context = this;
			
			Arbiter.Util.Servers.resetServers();
			
			db.transaction(function(tx){
				context.getServers(tx, context, onSuccess, onFailure);
			}, function(e){
				console.log("ERROR: Arbiter.ServersHelper.loadServers", e);
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure.call(context, e);
				}
			});
		},
		
		getServers: function(tx, context, onSuccess, onFailure){
			var sql = "select * from " + SERVERS_TABLE_NAME + ";";
			
			tx.executeSql(sql, [], function(tx, res){
				var row = null;
				
				for(var i = 0; i < res.rows.length; i++){
					row = res.rows.item(i);
					
					Arbiter.Util.Servers.putServer(row[_ID], 
							new Arbiter.Util.Server(
									row[SERVER_NAME], 
									row[SERVER_URL], 
									row[SERVER_USERNAME], 
									row[SERVER_PASSWORD]));
				}
				
				if(Arbiter.Util.funcExists(onSuccess)){
					
					onSuccess.call(context);
				}
				
			}, function(tx, e){
				console.log("ERROR: Arbiter.ServersHelper.getServers", e);
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure.call(context, e);
				}
			});
		}
	};
})();