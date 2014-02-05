Arbiter.Geolocation = function(context, lowAccuracyCallback, highAccuracyCallback, onFailure){
	this.callbackContext = context;
	this.lowAccuracyCallback = lowAccuracyCallback;
	this.highAccuracyCallback = highAccuracyCallback;
	this.onFailure = onFailure;
	
	this.options = {
		timeout: 5000,
		maximumAge: 3000
	};
};

Arbiter.Geolocation.prototype.getLocation = function(){
	
	if(Arbiter.Util.funcExists(this.lowAccuracyCallback)){
		this.getCurrentLocationLowAccuracy();
	}
	
	var context = this;
	
	window.setTimeout(function(){
		if(Arbiter.Util.funcExists(context.highAccuracyCallback)){
			context.getCurrentLocationHighAccuracy();
		}
	}, 2000);
};

Arbiter.Geolocation.prototype.getCurrentLocation = function(onSuccess, onFailure){
	
	/*navigator.geolocation.getCurrentPosition(function(position){
		if(Arbiter.Util.funcExists(onSuccess)){
			onSuccess(position);
		}
	}, function(e){
		if(Arbiter.Util.funcExists(onFailure)){
			onFailure(e);
		}
	}, this.options);*/
	
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
			onSuccess.call(context.callbackContext, pos);
		}
	}, 4000);
};

Arbiter.Geolocation.prototype.getCurrentLocationLowAccuracy = function(){
	
	var context = this;
	
	delete this.options.enableHighAccuracy;
	
	this.getCurrentLocation(this.lowAccuracyCallback, this.onFailure);
};

Arbiter.Geolocation.prototype.getCurrentLocationHighAccuracy = function(){
	
	var context = this;
	
	this.options.enableHighAccuracy = true;
	
	this.getCurrentLocation(this.highAccuracyCallback, this.onFailure);
};