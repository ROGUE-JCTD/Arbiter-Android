Arbiter.Geolocation = function(context, lowAccuracyCallback, highAccuracyCallback, onFailure){
	this.testing = false;
	this.callbackContext = context;
	this.lowAccuracyCallback = lowAccuracyCallback;
	this.highAccuracyCallback = highAccuracyCallback;
	this.onFailure = onFailure;
	
	this.gotResult = false;
	this.executedFailureCallback = false;
	
	this.options = {
		timeout: 10000,
		maximumAge: 3000
	};
};

Arbiter.Geolocation.prototype.onFailed = function(e){
	
	if(!this.gotResult && !this.executedFailureCallback){
		this.executedFailureCallback = true;
		this.onFailure.call(this.callbackContext, e);
	}
};

Arbiter.Geolocation.prototype.getLocation = function(){
	
	if(Arbiter.Util.funcExists(this.lowAccuracyCallback)){
		this.getCurrentLocationLowAccuracy();
	}
	
	var context = this;
	
	var highAccuracy = function(){
		if(Arbiter.Util.funcExists(context.highAccuracyCallback)){
			context.getCurrentLocationHighAccuracy();
		}
	};
	
	highAccuracy();
};

Arbiter.Geolocation.prototype.getCurrentLocation = function(onSuccess){
	
	if(this.testing){
		var pos = {
				
		};
		
		if(this.options.enableHighAccuracy === true){
			pos.coords = {
				longitude: "-77.35760093",
				latitude: "38.95602994"
			};
		}else{
			pos.coords = {
				longitude: "-77.35702157",
				latitude: "38.95762344"	
			};
		}
		
		var context = this;
		
		window.setTimeout(function(){
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess(pos);
			}
		}, 4000);
	}else{
		var context = this;
		
		navigator.geolocation.getCurrentPosition(function(position){
			
			context.gotResult = true;
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess(position);
			}
		}, function(e){
			
			context.onFailed(e);
		}, this.options);
	}
};

Arbiter.Geolocation.prototype.getCurrentLocationLowAccuracy = function(){
	
	var context = this;
	
	this.options.enableHighAccuracy = false;
	
	this.getCurrentLocation(function(pos){
		
		context.lowAccuracyCallback.call(context.callbackContext, pos);
	});
};

Arbiter.Geolocation.prototype.getCurrentLocationHighAccuracy = function(){
	
	var context = this;
	
	this.options.enableHighAccuracy = true;
	
	this.getCurrentLocation(function(pos){
		
		context.highAccuracyCallback.call(context.callbackContext, pos);
	});
};