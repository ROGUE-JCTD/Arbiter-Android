Arbiter.FindMe = function(olMap, olLayer, includeOOM, onSuccess, onFailure){
	this.olMap = olMap;
	this.gotHighAccuracy = false;
	this.olLayer = olLayer;
	
	if(includeOOM){
		this.oom = new Arbiter.FindMe_OOM();
	}
	
	this.minimumFindMeZoom = 18;
	this.point = null;
	
	this.styleChangeIntervalId = null;
	this.styleChangeInterval = 1000;
	
	this.removePointTimeoutId = null;
	this.removePointTimeout = 10000;
	
	this.bigBallHighAccuracy = "file:///android_asset/www/img/blue_ball_big.png";
	this.smallBallHighAccuracy = "file:///android_asset/www/img/blue_ball_small.png";
	
	this.bigBallLowAccuracy = "file:///android_asset/www/img/yellow_ball_big.png";
	this.smallBallLowAccuracy = "file:///android_asset/www/img/yellow_ball_small.png";
	
	this.bigRadius = 20;
	this.smallRadius = 10;
	
	this.onFinishedGettingLocation = function(){
		
		if(Arbiter.Util.existsAndNotNull(onSuccess)){
			onSuccess();
		}
		
		Arbiter.Cordova.finishedGettingLocation();
	};
	
	this.onFailedGettingLocation = function(e){
		
		if(Arbiter.Util.existsAndNotNull(onFailure)){
			onFailure(e);
		}
		
		Arbiter.Cordova.finishedGettingLocation();
	};
	
	this.lowAccuracyPointStyle = {
		externalGraphic: this.smallBallLowAccuracy,
		pointRadius: this.smallRadius
	}; 
	
	this.highAccuracyPointStyle = {
		externalGraphic: this.smallBallHighAccuracy,
		pointRadius: this.smallRadius
	};
	
	var context = this;
	
	this.geolocation = new Arbiter.Geolocation(this, this.lowAccuracyCallback,
			this.highAccuracyCallback, function(e){
		
		if(Arbiter.Util.funcExists(context.onFailure)){
			context.onFailure(e);
		}
	});
};

Arbiter.FindMe.prototype.zoom = function(position){
	
	var lonlat = this.getLonLat(position);
	
	var currentZoom = this.olMap.getZoom();
	
	if(currentZoom <= this.minimumFindMeZoom){
		currentZoom = this.minimumFindMeZoom;
	}
	
	this.olMap.setCenter(lonlat, currentZoom);
};

Arbiter.FindMe.prototype.getLonLat = function(position){
	
	var lonlat = new OpenLayers.LonLat(position.coords.longitude,
			position.coords.latitude);
	
	lonlat.transform(new OpenLayers.Projection("EPSG:4326"),
			new OpenLayers.Projection(this.olMap.getProjection()));
	
	return lonlat;
};

Arbiter.FindMe.prototype.addPoint = function(position, style){
	
	this.removePoint();
	
	var lonlat = this.getLonLat(position);
	
	var geometry = new OpenLayers.Geometry.Point(lonlat.lon, lonlat.lat);
	
	this.point = new OpenLayers.Feature.Vector(geometry, null, style);
	
	this.olLayer.addFeatures([this.point]);
	
	var context = this;
	
	this.removePointTimeoutId = window.setTimeout(function(){
		
		context.removePoint();
		
		if(Arbiter.Util.existsAndNotNull(context.oom)){
			
			context.oom.clearSavedPoint(function(){
				console.log("FindMe removed saved point");
				
				context.onFinishedGettingLocation();
			}, function(e){
				console.log("FindMe could not removed saved point: " + JSON.stringify(e));
				
				context.onFailedGettingLocation(e);
			});
		}else{
			context.onFinishedGettingLocation();
		}
		
	}, this.removePointTimeout);
	
	this.styleChangeIntervalId = window.setInterval(function(){
		
		var ball = style.externalGraphic; 
		var radius = style.pointRadius;
		
		if(radius === context.smallRadius){
			
			if(style === context.highAccuracyPointStyle){
				
				style.externalGraphic = context.bigBallHighAccuracy;
			}else{
				
				style.externalGraphic = context.bigBallLowAccuracy;
			}
			
			style.pointRadius = context.bigRadius;
		}else{
			
			if(style === context.highAccuracyPointStyle){
				
				style.externalGraphic = context.smallBallHighAccuracy;
			}else{
				
				style.externalGraphic = context.smallBallLowAccuracy;
			}
			
			style.pointRadius = context.smallRadius;
		}
		
		context.olLayer.redraw();
		
	}, this.styleChangeInterval);
};

Arbiter.FindMe.prototype.removePoint = function(){
	
	if(Arbiter.Util.existsAndNotNull(this.styleChangeIntervalId)){
		window.clearTimeout(this.styleChangeIntervalId);
		this.styleChangeIntervalId = null;
	}
	
	if(Arbiter.Util.existsAndNotNull(this.removePointTimeoutId)){
		window.clearTimeout(this.removePointTimeoutId);
		this.removePointTimeoutId = null;
	}
	
	if(!Arbiter.Util.existsAndNotNull(this.point)){
		return;
	}
	
	this.olLayer.removeFeatures([this.point]);
	
	this.point = null;
};

Arbiter.FindMe.prototype.savePosition = function(position){
	var context = this;
	
	this.oom.savePoint(this.gotHighAccuracy,
			position, function(){
		
		context.zoom(position);
	}, function(e){
		
		context.zoom(position);
	});
};

Arbiter.FindMe.prototype.lowAccuracyCallback = function(position){
	
	if(this.gotHighAccuracy){
		return;
	}
	
	this.addPoint(position, this.lowAccuracyPointStyle);
	 
	if(Arbiter.Util.existsAndNotNull(this.oom)){
		
		this.savePosition(position);
	}else{
		
		this.zoom(position);
	}
};

Arbiter.FindMe.prototype.highAccuracyCallback = function(position){
	
	this.gotHighAccuracy = true;
	
	this.addPoint(position, this.highAccuracyPointStyle);
	
	if(Arbiter.Util.existsAndNotNull(this.oom)){
		
		this.savePosition(position);
	}else{
		
		this.zoom(position);
	}
};

Arbiter.FindMe.prototype.onFailure = function(e){
	
	var msg = "FindMe could not get location: " + JSON.stringify(e);
	
	console.log(msg);
	
	this.onFailedGettingLocation(e);
	
	Arbiter.Cordova.alertGeolocationError(msg);
};

Arbiter.FindMe.prototype.findMe = function(){
	this.geolocation.getLocation();
};

Arbiter.FindMe.prototype.resume = function(){
	
	if(!Arbiter.Util.existsAndNotNull(this.oom)){
		return;
	}
	
	var context = this;
	
	this.oom.getPoint(function(findme){
		
		if(Arbiter.Util.existsAndNotNull(findme) 
				&& Arbiter.Util.existsAndNotNull(findme.gotHighAccuracy) 
				&& Arbiter.Util.existsAndNotNull(findme.position)){
			
			
			if(!findme.gotHighAccuracy){
				
				context.addPoint(findme.position, context.lowAccuracyPointStyle);
				
				context.geolocation.getCurrentLocationHighAccuracy();
			}else{
				
				context.addPoint(findme.position, context.highAccuracyPointStyle);
			}
		}
	}, function(e){
		console.log("FindMe error loading saved position: " + JSON.stringify(e));
	});
};
