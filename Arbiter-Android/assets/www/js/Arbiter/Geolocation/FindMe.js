(function(){
	
	Arbiter.FindMe = function(olMap, olLayer){
		this.olMap = olMap;
		this.olLayer = olLayer;
		this.timeout = 60000;
		this.watchInterval = 3000;
		this.marker = null;
		this.minimumFindMeZoom = 18;
		
		this.styleChangeIntervalId = null;
		this.styleChangeInterval = 1000;
		
		this.removeMarkerTimeout = 10000;
		
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
	
	prototype._startCycle = function(onSuccess, onFailure){
		
		var context = this;
		
		var highAccuracyResponded = false;
		var highAccuracySucceeded = false;
		
		var lowAccuracyResponded = false;
		var lowAccuracySucceeded = false;
		var position = null;
		
		var done = function(pos){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess(pos);
			}
		};
		
		var fail = function(e){
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		this._getHighAccuracy(function(pos){
			
			// set the high accuracy request responded and succeeded
			highAccuracyResponded = true;
			highAccuracySucceeded = true;
			
			// Update the location of the marker
			context._gotLocation(pos, context.highAccuracyStyle);
			
			// The cycle has completed
			done(pos);
		}, function(e){
			
			highAccuracyResponded = true;
			highAccuracySucceeded = false;
			
			// If the low accuracy responded
			if(lowAccuracyResponded){
				
				if(lowAccuracySucceeded){
					done(position);
				}else{
					fail(e);
				}
			}
		});
		
		this._getLowAccuracy(function(pos){
			
			lowAccuracyResponded = true;
			lowAccuracySucceeded = true;
			
			// Only update the location of the marker, 
			// if this cycle's highAccuracy request hasn't
			// responded yet.
			if(!highAccuracyResponded){
			
				position = pos;
				
				context._gotLocation(pos, context.lowAccuracyStyle);
			}
		}, function(e){
			
			lowAccuracyResponded = true;
			lowAccuracySucceeded = false;
			
			// Both the high and the low accuracy attempts to get
			// the users location failed, so call the failure callback
			if(highAccuracyResponded && !highAccuracySucceeded){
				
				fail(e);
			}
		});
	};
	
	prototype.watchLocation = function(onFailure){
		
		var context = this;
		
		var startNextCycle = function(){
			
			// Wait the time of the watchInterval, and then
			// start a new cycle
			window.setTimeout(function(){
				
				context.watchLocation(onFailure);
			}, context.watchInterval);
		};
		
		this._startCycle(function(pos){
			
			// Start the next cycle
			startNextCycle();
		}, function(e){
			
			// This cycle to get the users location failed both on high accuracy
			// and low accuracy, so execute the onFailure callback
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				
				onFailure(e);
			}
			
			// Start the next cycle
			startNextCycle();
		});
	};
	
	prototype.getLocation = function(onSuccess, onFailure){
		
		var context = this;
		
		this._startCycle(function(pos){
			
			context._zoom(pos);
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess(pos);
			}
			
			// remove the marker in ten seconds
			window.setTimeout(function(){
				
				context._removeMarker();
			}, context.removeMarkerTimeout);
		}, onFailure);
	};
	
	prototype._gotLocation = function(pos, style){
		
		this._updateMarker(pos, style);
	};
	
	prototype._getCurrentPosition = function(enableHighAccuracy, onSuccess, onFailure){
		
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
				
				if(Arbiter.Util.existsAndNotNull(onSuccess)){
					onSuccess(pos);
				}
			}, 10000);
		}else{
			
			navigator.geolocation.getCurrentPosition(function(pos){
				
				if(Arbiter.Util.existsAndNotNull(onSuccess)){
					onSuccess(pos);
				}
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
		
		this._getCurrentPosition(true, function(pos){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess(pos);
			}
		}, function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		});
	};
	
	prototype._getLowAccuracy = function(onSuccess, onFailure){
		
		var context = this;
		
		this._getCurrentPosition(false, function(pos){
			
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