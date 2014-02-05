Arbiter.FindMe = function(olMap, olLayer){
	this.olMap = olMap;
	this.gotHighAccuracy = false;
	this.olLayer = olLayer;
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
	
	this.lowAccuracyPointStyle = {
		externalGraphic: this.smallBallLowAccuracy,
		pointRadius: this.smallRadius
	}; 
	
	this.highAccuracyPointStyle = {
		externalGraphic: this.smallBallHighAccuracy,
		pointRadius: this.smallRadius
	};
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
		console.log("remove point timeout");
		
		context.removePoint();
	}, this.removePointTimeout);
	
	this.styleChangeIntervalId = window.setInterval(function(){
			
		console.log("change style interval");
		
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

Arbiter.FindMe.prototype.lowAccuracyCallback = function(position){
	
	if(this.gotHighAccuracy){
		return;
	}
	
	this.addPoint(position, this.lowAccuracyPointStyle);
	
	this.zoom(position);
};

Arbiter.FindMe.prototype.highAccuracyCallback = function(position){
	
	this.gotHighAccuracy = true;
	
	this.addPoint(position, this.highAccuracyPointStyle);
	
	this.zoom(position);
};

Arbiter.FindMe.prototype.onFailure = function(e){
	
	console.log("FindMe could not get location");
};

Arbiter.FindMe.prototype.findMe = function(){
	var context = this;
	
	var geolocation = new Arbiter.Geolocation(this, this.lowAccuracyCallback,
			this.highAccuracyCallback, function(e){
		
		if(Arbiter.Util.funcExists(context.onFailure)){
			context.onFailure(e);
		}
	});
	
	geolocation.getLocation();
};
