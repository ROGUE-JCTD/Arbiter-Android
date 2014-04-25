(function(){
	
	Arbiter.NotificationHandler = function(db){
		this.db = db;
		this.syncId = null;
	};
	
	var prototype = Arbiter.NotificationHandler.prototype;
	
	var construct = Arbiter.NotificationHandler;
	
	construct.SYNC = {
		ID: '_id',
		TABLE_NAME: 'syncs',
		TIMESTAMP: 'timestamp',
		NOTIFICATIONS_ARE_SET: 'notifications_are_set'
	};
	
	prototype.getTimestamp = function(){
		
		return (new Date()).toISOString();
	};
	
	prototype.startNewSync = function(onSuccess, onFailure){
		
		var context = this;
		
		var fail = function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		this.db.transaction(function(tx){
			var sql = "INSERT INTO " + construct.SYNC.TABLE_NAME + " (" + construct.SYNC.TIMESTAMP + ") VALUES (?);";
			
			tx.executeSql(sql, [context.getTimestamp()], function(tx, res){
				
				console.log("new sync res.insertId = " + res.insertId);
				
				//HACK WORKAROUND: 	the first time something is inserted into a table
				// 					the inserterId comes back null for some reason. 
				//					catch it and assume it was id of 1
				if (res.insertId == null){
					res.insertId = 1;
					//Arbiter.warning("@@@@@@ caught res.insertId == null inserintg into tiles. using 1 as workaround");
				}
				
				context.syncId = res.insertId;
				
				console.log("inserted new sync id = " + context.syncId);
				
				if(Arbiter.Util.existsAndNotNull(onSuccess)){
					onSuccess(context.syncId);
				}
			}, function(tx, e){
				
				fail(e);
			});
		}, function(e){
			
			fail(e);
		});
	};
	
	prototype.endCurrentSync = function(onSuccess, onFailure){
		
		var context = this;
		
		var fail = function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		this.db.transaction(function(tx){
			var sql = "UPDATE " + construct.SYNC.TABLE_NAME + " SET " + construct.SYNC.NOTIFICATIONS_ARE_SET + "=? WHERE " + construct.SYNC.ID + "=?;";
			
			tx.executeSql(sql, [true, context.syncId], function(tx, res){
				
				console.log("Updated sync notification set to true for syncId = " + context.syncId);
				
				if(Arbiter.Util.existsAndNotNull(onSuccess)){
					onSuccess();
				}
			}, function(tx, e){
				
				fail(e);
			});
		}, function(e){
			
			fail(e);
		});
	};
})();