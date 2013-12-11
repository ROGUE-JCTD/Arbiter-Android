Arbiter.SQLiteTransaction = function(_db, _transactionCompleted){
	var context = this;
	
	var db = _db;
	
	startNextTransaction = db.startNextTransaction;
	
	db.startNextTransaction = function(){
		
		startNextTransaction.call(db);
		
		// The transaction queue is empty so
		// there must be no more transactions!
		if(context.isDone()){
			console.log("No more transactions!");
			
			if(Arbiter.Util.funcExists(_transactionCompleted)){
				_transactionCompleted();
			}
		}
	};
	
	return {
		isDone: function(){
			return db.txQ.length === 0;
		}
	};
};