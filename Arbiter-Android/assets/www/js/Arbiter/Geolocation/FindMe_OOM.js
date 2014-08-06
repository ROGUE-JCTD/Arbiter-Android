Arbiter.SavedFindMe = function(gotHighAccuracy, position){
	this.gotHighAccuracy = gotHighAccuracy;
	this.position = position;
};

Arbiter.FindMe_OOM = function(){};

Arbiter.FindMe_OOM.prototype.FINDME = "findme";

Arbiter.FindMe_OOM.prototype.savePoint = function(gotHighAccuracy, position, onSuccess, onFailure){
	
	var obj = new Arbiter.SavedFindMe(gotHighAccuracy, position);
	
	console.log("saving point: " + obj.gotHighAccuracy);
	
	var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
	
	Arbiter.PreferencesHelper.put(projectDb, this.FINDME,
			JSON.stringify(obj), this, function(){
		
		console.log("point saved successfully: " + obj.gotHighAccuracy);
		if(Arbiter.Util.funcExists(onSuccess)){
			onSuccess();
		}
	}, function(e){
		
		console.log("could not save point: " + obj.gotHighAccuracy);
		if(Arbiter.Util.funcExists(onFailure)){
			onFailure(e);
		}
	});
};

Arbiter.FindMe_OOM.prototype.getPoint = function(onSuccess, onFailure){
	var context = this;
	
	var success = function(findme){
		if(Arbiter.Util.funcExists(onSuccess)){
			
			if(Arbiter.Util.existsAndNotNull(findme)){
				onSuccess(JSON.parse(findme));
			}else{
				onSuccess(findme);
			}
		}
	};
	
	var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
	
	Arbiter.PreferencesHelper.get(projectDb, this.FINDME, this, function(findme){
		
		success(findme);
	}, function(e){
		
		if(Arbiter.Util.funcExists(onFailure)){
			onFailure(e);
		}
	});
};

Arbiter.FindMe_OOM.prototype.clearSavedPoint = function(onSuccess, onFailure){
	
	var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
	
	Arbiter.PreferencesHelper.remove(projectDb, this.FINDME, this, function(){
		
		if(Arbiter.Util.funcExists(onSuccess)){
			onSuccess();
		}
	}, function(e){
		
		if(Arbiter.Util.funcExists(onFailure)){
			onFailure(e);
		}
	});
};
