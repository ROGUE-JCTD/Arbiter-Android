(function(){
	
	Arbiter.CheckNotificationsComputed = function(projectDb, onSuccess, onFailure){
		this.projectDb = projectDb;
		
		this.checkFailure = function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		this.checkSuccess = function(response){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess(response);
			}
		};
		
		this._tx = null;
	};
	
	var prototype = Arbiter.CheckNotificationsComputed.prototype;
	
	prototype.checkNotificationsAreComputed = function(){
		
		var context = this;
		
		this.projectDb.transaction(function(tx){
			
			context._tx = tx;
			
			context._getLastSync();
		}, function(e){
			context.checkFailure(e);
		});
	};
	
	prototype._getLastSync = function(){
		
		var context = this;
		var tableName = Arbiter.NotificationHandler.SYNC.TABLE_NAME;
		var id = Arbiter.NotificationHandler.SYNC.ID;
		
		var sql = "select " + Arbiter.NotificationHandler.SYNC.NOTIFICATIONS_ARE_SET + " from " 
			+ tableName + " where " + id + " = (select max(" + id + ") from " + tableName + ");";
		
		this._tx.executeSql(sql, [], function(tx, res){
			
			var computed = false;
			
			if(res.rows.length > 0){
				computed = res.rows.item(0);
			}
			
			console.log("already computed = " + computed);
			
			context.checkSuccess(computed);
		}, function(tx, e){
			context.checkFailure(e);
		});
	};
})();