Arbiter.ReattemptFailed = function(_arrayOfFailed, _onSuccess, _onFailure){
	this.arrayOfFailed = _arrayOfFailed;
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	this.failedAttempts = null;
	
	this.index = -1;
};

Arbiter.ReattemptFailed.prototype.onFinishedAttempts = function(){
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		this.onSuccess(this.failedAttempts);
	}
};

Arbiter.ReattemptFailed.prototype.pop = function(){
	
	if(++this.index < this.arrayOfFailed.length){
		return this.arrayOfFailed[this.index];
	}
	
	return undefined;
};

Arbiter.ReattemptFailed.prototype.startAttempts = function(){
	
	if(!Arbiter.Util.existsAndNotNull(this.arrayOfFailed)){
		this.onFinishedAttempts();
	}else{
		this.attemptNext();
	}
};

Arbiter.ReattemptFailed.prototype.addToFailedAttempts = function(failedItem){
	
	if(Arbiter.Util.existsAndNotNull(failedItem)){
		
		if(!Arbiter.Util.existsAndNotNull(this.failedAttempts)){
			this.failedAttempts = [];
		}
		
		this.failedAttempts.push(failedItem);
	}
};

Arbiter.ReattemptFailed.prototype.attemptNext = function(){
	
	var failedItem = this.pop();
	
	if(failedItem !== undefined){
		
		if(!Arbiter.Util.funcExists(this.attempt)){
			throw "Must implement an attempt method!";
		}
		
		this.attempt(failedItem);
	}else{
		
		this.onFinishedAttempts();
	}
};



