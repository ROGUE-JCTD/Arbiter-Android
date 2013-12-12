Arbiter.SQLiteTransaction = function(_db, _transactionCompleted){
	var context = this;
	
	var db = _db;
	
	var startNextTransaction = db.startNextTransaction;
	this.isDone = function(){
		return !db.txQ[0];
	};
	
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
};