Arbiter.Cordova.Project = (function(){
	var describeFeatureTypeReader = new OpenLayers.Format.WFSDescribeFeatureType();
	
	var currentPositionZoomLevel = 14;
	
	var getSchemasFromDbLayers = function(dbLayers){
		var specificSchemas = [];
		
		var layerId = null;
		var arbiterSchemas = Arbiter.getLayerSchemas();
		
		for(var i = 0; i < dbLayers.length; i++){
			layerId = dbLayers[i][Arbiter.LayersHelper.layerId()];
			
			specificSchemas.push(arbiterSchemas[layerId]);
		}
		
		return specificSchemas;
	};
	
	var prepareSync = function(layers, bounds, cacheTiles, onSuccess, onFailure){
		
		Arbiter.Loaders.LayersLoader.load(function(){
			
			if(bounds !== "" && bounds !== null && bounds !== undefined){
				
				var specificSchemas = getSchemasFromDbLayers(layers);
				
				console.log("prepareSync dbLayers: " + JSON.stringify(layers));
				
				console.log("print specific schemas");
				
				for(var i = 0; i < specificSchemas.length; i++){
					console.log("schema featureType = " + specificSchemas[i].getFeatureType());
				}
				
				Arbiter.Cordova.Project.sync(cacheTiles, true,
						specificSchemas, onSuccess, onFailure);
			}else{
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess();
				}
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure(e);
			}
		});
	};
	
	var storeFeatureData = function(layers, bounds, cacheTiles, onSuccess, onFailure){
		
		var schemaDownloader = new Arbiter.SchemaDownloader(layers, function(failedLayers){
			
			prepareSync(layers, bounds, cacheTiles, onSuccess, onFailure);
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure(e);
			}
		});
		
		schemaDownloader.startDownload();
	};
	
	var storeData = function(context, layers, bounds, cacheTiles, onSuccess, onFailure){
		Arbiter.ServersHelper.loadServers(context, function(){
			storeFeatureData(layers, bounds, cacheTiles, onSuccess, onFailure);
		}, onFailure);
	};
	
	var zoomToExtent = function(savedBounds, savedZoom){
		var bounds = savedBounds.split(',');
		
		Arbiter.Map.zoomToExtent(bounds[0], bounds[1],
				bounds[2], bounds[3], savedZoom);
	};
	
	return {
		createProject: function(layers){
			var context = this;
			
			Arbiter.Cordova.setState(Arbiter.Cordova.STATES.CREATING_PROJECT);
			
			var onSuccess = function(){
				
				Arbiter.Cordova.syncCompleted();
			};
			
			var onFailure = function(e){
				console.log("Arbiter.Cordova.Project", e);
			//	Arbiter.Cordova.errorCreatingProject(e);
				Arbiter.Cordova.syncCompleted();
			};
			
			Arbiter.ProjectDbHelper.getProjectDatabase().close();
			Arbiter.FeatureDbHelper.getFeatureDatabase().close();
			
			Arbiter.PreferencesHelper.get(Arbiter.AOI, this, function(_aoi){
				var bounds = null;
				
				if(_aoi !== null && _aoi !== undefined 
						&& _aoi !== ""){
					
					var aoi = _aoi.split(',');
					
					bounds = new Arbiter.Util.Bounds(aoi[0], 
						aoi[1], aoi[2], aoi[3]);
				}
				
				storeData(context, layers, bounds, true, function(){
					onSuccess();
				}, onFailure);
			}, onFailure);
		},
		
		addLayers: function(layers){
			var context = this;
			
			var onSuccess = function(){
				Arbiter.Cordova.syncCompleted();
			};
			
			var onFailure = function(e){
			//	Arbiter.Cordova.errorAddingLayers(e);
				Arbiter.Cordova.syncCompleted();
			};
			
			Arbiter.PreferencesHelper.get(Arbiter.AOI, context, function(_aoi){
				var aoi = _aoi.split(','); 
				var bounds = null;
				
				if(_aoi !== null && _aoi !== undefined && _aoi !== ""){
					bounds = new Arbiter.Util.Bounds(aoi[0], aoi[1], aoi[2], aoi[3]);
				}
				
				storeData(context, layers, bounds, false, function(){
					
					onSuccess();
				}, onFailure);
			}, onFailure);
		},
		
		/**
		 * callback is the callback to be executed
		 * after all of the features finish downloading
		 */
		downloadFeatures: function(schema, bounds, encodedCredentials, onSuccess, onFailure){
			var context = this;
			
			// Insert the downloaded features
			// into the layers feature table
			Arbiter.Util.Feature.downloadFeatures(schema, bounds, 
					encodedCredentials, function(schema, features){
				// Features will be in their native srid at this point so pass the srid of the schema
				var isDownload = true;
				
				Arbiter.FeatureTableHelper.insertFeatures(schema, schema.getSRID(),
						features, isDownload, function(){
					try{
						console.log("inserted features now downloading media for said features");
						
						Arbiter.MediaHelper.downloadMedia(schema, encodedCredentials, features, function(){
							if(Arbiter.Util.funcExists(onSuccess)){
								console.log("executing download features onSuccess");
								onSuccess();
							}
						}, onFailure);
					}catch(e){
						console.log("Media failed to download");
					}
					
				});
			}, onFailure);
		},
		
		updateAOI: function(left, bottom, right, top){
			var aoi = left + ", " + bottom 
				+ ", " + right + ", " + top;
			
			// onSyncFailure execute the native
			// method to report the error to the
			// user.
			var onFailure = function(e){
				Arbiter.Cordova.errorUpdatingAOI(e);
			};
			
			Arbiter.PreferencesHelper.put(Arbiter.AOI, aoi, this, function(){
				
				Arbiter.Cordova.Project.sync(true);
			}, onFailure);
		},
		
		getSavedBounds: function(onSuccess, onFailure){
			Arbiter.PreferencesHelper.get(Arbiter.SAVED_BOUNDS, this, function(savedBounds){
				Arbiter.PreferencesHelper.get(Arbiter.SAVED_ZOOM_LEVEL, this, function(savedZoom){
					
					if(Arbiter.Util.funcExists(onSuccess)){
						onSuccess(savedBounds, savedZoom);
					}
				}, onFailure);
			}, onFailure);
		},
		
		zoomToAOI: function(onSuccess, onFailure){
			Arbiter.PreferencesHelper.get(Arbiter.AOI, this, function(_aoi){
				
				if(_aoi !== null && _aoi !== undefined 
						&& _aoi !== ""){
					
					var aoi = _aoi.split(',');
					
					Arbiter.Map.zoomToExtent(aoi[0], 
							aoi[1], aoi[2], aoi[3]);
				}else{
					Arbiter.Cordova.Project.zoomToDefault();
				}
				
				
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess();
				}
			}, onFailure);
		},
		
		zoomToDefault: function(){
			zoomToExtent(Arbiter.DEFAULT_ZOOM_EXTENT);
		},
		
		getCurrentPosition: function(onSuccess, onFailure){
			navigator.geolocation.getCurrentPosition(function(position){
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess(position);
				}
			}, function(e){
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure(e);
				}
			});
		},
		
		zoomToCurrentPosition: function(onSuccess, onFailure){
			try{
				this.getCurrentPosition(function(position){
					var lonlat = new OpenLayers.LonLat(position.coords
							.longitude, position.coords.latitude);
					
					var map = Arbiter.Map.getMap();
					
					lonlat.transform(new OpenLayers.Projection("EPSG:4326"),
							new OpenLayers.Projection(map.getProjection()));
					
					map.setCenter(lonlat, currentPositionZoomLevel);
				});
			}catch(e){
				console.log(e);
			}
		},
		
		sync: function(_cacheTiles, _downloadOnly, _specificSchemas, onSuccess, onFailure){
			console.log("sync");
			var map = Arbiter.Map.getMap();
			var cacheTiles = _cacheTiles;
			var downloadOnly = _downloadOnly;
			var specificSchemas = _specificSchemas;
			
			if(cacheTiles === null || cacheTiles === undefined){
				cacheTiles = false;
			}
			
			if(downloadOnly === null || downloadOnly === undefined){
				downloadOnly = false;
			}
			
			if(Arbiter.getLayerSchemasLength() > 0 ||
					((downloadOnly === true || downloadOnly === true)
							&& specificSchemas.length > 0) || cacheTiles){
				
				Arbiter.Cordova.setState(Arbiter.Cordova.STATES.UPDATING);
				
				Arbiter.PreferencesHelper.get(Arbiter.AOI, this, function(_aoi){
					
					if(_aoi !== null && _aoi !== undefined 
							&& _aoi !== ""){
						
						var aoi = _aoi.split(',');
						
						var bounds = new Arbiter.Util.Bounds(aoi[0], aoi[1], aoi[2], aoi[3]);
						
						var syncHelper = new Arbiter.Sync(map, cacheTiles,
								bounds, downloadOnly, function(){
							
							if(Arbiter.Util.funcExists(onSuccess)){
								onSuccess();
							}else{
								Arbiter.Cordova.syncCompleted();
							}
						}, function(e){
							
							console.log("sync failed", e);
							
							if(Arbiter.Util.funcExists(onFailure)){
								onFailure(e);
							}else{
								Arbiter.Cordova.syncCompleted();
							}
						});
						
						if(downloadOnly === true || downloadOnly === "true"){
							
							syncHelper.setSpecificSchemas(specificSchemas);
						}
						
						syncHelper.sync();
					}
				}, function(e){
					
					if(Arbiter.Util.funcExists(onFailure)){
						onFailure(e);
					}
				});
			}
		}
	};
})();