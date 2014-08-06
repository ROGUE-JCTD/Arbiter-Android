(function(){
	
	Arbiter.FindMe = function(olMap, olLayer){
		this.olMap = olMap;
		this.olLayer = olLayer;
		this.timeout = 60000;
		this.watchInterval = 3000;
		this.marker = null;
		this.isWatching = false;
		this.minimumFindMeZoom = 18;
		
		this.styleChangeIntervalId = null;
		this.styleChangeInterval = 1000;
		
		this.bigRadius = 20;
		this.smallRadius = 10;
		
		this.bigBallHighAccuracy = "file:///android_asset/www/img/blue_ball_big.png";
		this.smallBallHighAccuracy = "file:///android_asset/www/img/blue_ball_small.png";
		
		this.bigBallLowAccuracy = "file:///android_asset/www/img/yellow_ball_big.png";
		this.smallBallLowAccuracy = "file:///android_asset/www/img/yellow_ball_small.png";
		
		this.lowAccuracyStyle = {
			externalGraphic: this.smallBallLowAccuracy,
			pointRadius: this.smallRadius
		}; 
		
		this.highAccuracyStyle = {
			externalGraphic: this.smallBallHighAccuracy,
			pointRadius: this.smallRadius
		};
	};
	
	var prototype = Arbiter.FindMe.prototype;
	
	var construct = Arbiter.FindMe;
	
	construct.TESTING = false;
	
	prototype.watchLocation = function(onFailure){
		
		this.isWatching = true;
		
		var context = this;
		
		this._getHighAccuracy(function(pos){
				
			// don't remove the marker, but instead, set a timeout to start another request
			window.setTimeout(function(){
				
				context.watchLocation(onFailure);
			}, context.watchInterval);
		}, onFailure);
	};
	
	prototype.getLocation = function(onSuccess, onFailure){
		
		var context = this;
		
		this._getHighAccuracy(function(pos){
			
			context._zoom(pos);
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess();
			}
			
			// remove the marker in ten seconds
			window.setTimeout(function(){
				
				context._removeMarker();
			}, 10000);
		}, onFailure);
	};
	
	prototype._gotLocation = function(pos, style, onSuccess){
		
		this._updateMarker(pos, style);
		
		if(Arbiter.Util.existsAndNotNull(onSuccess)){
			onSuccess(pos);
		}
	};
	
	prototype._getCurrentPosition = function(enableHighAccuracy, style, onSuccess, onFailure){
		
		var context = this;
		
		if(construct.TESTING){
			
			window.setTimeout(function(){
				
				var pos = {};
				
				if(enableHighAccuracy === true){
					pos.coords = {
						longitude: "-73.9797447",
						latitude: "40.7423127"
					};
				}else{
					pos.coords = {
						longitude: "-73.9795034",
						latitude: "40.7426833"
					};
				}
				
				context._gotLocation(pos, style, onSuccess);
			}, 10000);
		}else{
			
			navigator.geolocation.getCurrentPosition(function(pos){
				
				context._gotLocation(pos, style, onSuccess);
			}, function(e){
				
				if(Arbiter.Util.existsAndNotNull(onFailure)){
					onFailure(e);
				}
			}, {
				enableHighAccuracy: enableHighAccuracy,
				maximumAge: 3000,
				timeout: context.timeout
			});
		}
	};
	
	prototype._getHighAccuracy = function(onSuccess, onFailure){
		
		var context = this;
		
		this._getCurrentPosition(true, this.highAccuracyStyle, function(pos){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess(pos);
			}
		}, function(e){
			
			context._getLowAccuracy(onSuccess, onFailure);
		});
	};
	
	prototype._getLowAccuracy = function(onSuccess, onFailure){
		
		var context = this;
		
		this._getCurrentPosition(false, this.lowAccuracyStyle, function(pos){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess(pos);
			}
		}, function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		});
	};
	
	prototype._getLonLat = function(position){
		
		var lonlat = new OpenLayers.LonLat(position.coords.longitude,
				position.coords.latitude);
		
		lonlat.transform(new OpenLayers.Projection("EPSG:4326"),
				new OpenLayers.Projection(this.olMap.getProjection()));
		
		return lonlat;
	};
	
	prototype.clearIntervals = function(){
		
		this._clearStyleChangeInterval();
	};
	
	prototype._clearStyleChangeInterval = function(){
		
		if(Arbiter.Util.existsAndNotNull(this.styleChangeIntervalId)){
			
			window.clearInterval(this.styleChangeIntervalId);
			
			this.styleChangeIntervalId = null;
		}
	};
	
	prototype._setStyleChangeInterval = function(style){
		
		var context = this;
		
		this.styleChangeIntervalId = window.setInterval(function(){
			
			var ball = style.externalGraphic; 
			var radius = style.pointRadius;
			
			if(radius === context.smallRadius){
				
				if(style === context.highAccuracyStyle){
					
					style.externalGraphic = context.bigBallHighAccuracy;
				}else{
					
					style.externalGraphic = context.bigBallLowAccuracy;
				}
				
				style.pointRadius = context.bigRadius;
			}else{
				
				if(style === context.highAccuracyStyle){
					
					style.externalGraphic = context.smallBallHighAccuracy;
				}else{
					
					style.externalGraphic = context.smallBallLowAccuracy;
				}
				
				style.pointRadius = context.smallRadius;
			}
			
			context.olLayer.redraw();
			
		}, this.styleChangeInterval);
	};
	
	prototype._updateMarker = function(pos, style){
		
		this._clearStyleChangeInterval();
		
		var lonlat = this._getLonLat(pos);
		
		var geometry = new OpenLayers.Geometry.Point(lonlat.lon, lonlat.lat);
		
		// if the marker doesn't exist add it, add it at the location
		if(!Arbiter.Util.existsAndNotNull(this.marker)){
			
			this.marker = new OpenLayers.Feature.Vector(geometry, null, style);
		}else{
			
			// if the marker does exist, move it to the current location
			this.olLayer.removeFeatures([this.marker]);
			this.marker.geometry = geometry;
		}
		
		this.olLayer.addFeatures([this.marker]);
		
		this.olLayer.redraw();
		
		this._setStyleChangeInterval(style);
	};
	
	prototype._removeMarker = function(){
		
		this._clearStyleChangeInterval();
		
		if(Arbiter.Util.existsAndNotNull(this.marker)){
			this.olLayer.removeFeatures([this.marker]);
			this.marker = null;
		}
	};
	
	prototype._zoom = function(position){
		
		var lonlat = this._getLonLat(position);
		
		var currentZoom = this.olMap.getZoom();
		
		if(currentZoom <= this.minimumFindMeZoom){
			currentZoom = this.minimumFindMeZoom;
		}
		
		this.olMap.setCenter(lonlat, currentZoom);
	};
})();