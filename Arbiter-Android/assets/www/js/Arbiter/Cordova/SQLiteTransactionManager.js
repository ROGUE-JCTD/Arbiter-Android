Arbiter.SQLiteTransactionManager = (function(){
	var dbs = [];
	
	var onAllCompleted = [];
	
	var addToOnCompleted = function(func){
		onAllCompleted.push(func);
	};
	
	var isDone = function(){
		for(var i = 0; i < dbs.length; i++){
			if(!dbs[i].isDone()){
				return false;
			}
		}
		
		return true;
	};
	
	var executeOnAllCompleted = function(){
		var func = null;
		
		for(func = onAllCompleted.shift(); Arbiter.Util.funcExists(func);
			func = onAllCompleted.shift()){
			
			func();
		}
	};
	
	// Make sure that all transactions are completed
	var transactionCompleted = function(){
		if(isDone()){
			executeOnAllCompleted();
		}
	};
	
	return {
		push: function(_db){
			dbs.push(new Arbiter.SQLiteTransaction(_db,
					transactionCompleted));
		},
		
		executeAfterDone: function(func){
			if(!Arbiter.Util.funcExists(func)){
				return;
			}
			
			if(isDone()){
				func();
			}else{
				addToOnCompleted(func);
			}
		}
	};
})();