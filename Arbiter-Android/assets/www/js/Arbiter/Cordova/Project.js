Arbiter.Cordova.Project = (function(){
	var describeFeatureTypeReader = new OpenLayers.Format.WFSDescribeFeatureType();
	
	// When the layerFinishedCount reaches
	// the layerCount, execute the callback.
	var layerCount = 0;
	var layerFinishedCount = 0;
	var currentPositionZoomLevel = 14;
	
	var reset = function(){
		layerCount = 0;
		layerFinishedCount = 0;
	};
	
	var incrementLayerFinishedCount = function(){
		layerFinishedCount++;
	};
	
	var doneGettingLayers = function(){
		return (layerFinishedCount == layerCount);
	};
	
	var getLayerSchema = function(layer, bounds, onSuccess, onFailure){
		var serverId = layer[Arbiter.LayersHelper.serverId()];
		
		var server = Arbiter.Util.Servers.getServer(serverId);
		var url = server.getUrl();
		
		var encodedCredentials = 
			Arbiter.Util.getEncodedCredentials(
					server.getUsername(), 
					server.getPassword());
		
		var featureType = layer[Arbiter.LayersHelper.featureType()];
		var srid = layer[Arbiter.GeometryColumnsHelper.featureGeometrySRID()];
		
		var gotRequestBack = false;
		
		var request = new OpenLayers.Request.GET({
			url: url + "/wfs?service=wfs&version=1.0.0&request=DescribeFeatureType&typeName=" + featureType,
			headers: {
				Authorization: 'Basic ' + encodedCredentials
			},
			success: function(response){
				gotRequestBack = true;
				
				var context = Arbiter.Cordova.Project;
				var results = describeFeatureTypeReader.read(response.responseText);
				
				// If there are no feature types, return.
				if(!results.featureTypes || !results.featureTypes.length){
					
					incrementLayerFinishedCount();
					
					if(doneGettingLayers() && Arbiter.Util.funcExists(onSuccess)){
						onSuccess.call(context);
					}
					
					return;
				}
				
				var schema = new Arbiter.Util.LayerSchema(url,
						results.targetNamespace, featureType,srid,
						results.featureTypes[0].properties, serverId);
				
				var helper = Arbiter.GeometryColumnsHelper;
				
				var content = {};
				
				content[Arbiter.LayersHelper.workspace()] = results.targetNamespace;
				
				// Update the layers workspace in the Layers table.
				Arbiter.LayersHelper.updateLayer(featureType, content, this, function(){
					
					// After updating the layer workspace, 
					// add the layer to the GeometryColumns table
					Arbiter.GeometryColumnsHelper.addToGeometryColumns(schema, function(){
						
						// After adding the layer to the GeometryColumns table
						// create the feature table for the layer
						Arbiter.FeatureTableHelper.createFeatureTable(schema, function(){
							
							if(bounds !== null && bounds !== undefined && bounds !== ""){
								// After creating the feature table for the layer,
								// download the features from the layer
								context.downloadFeatures(schema, bounds, encodedCredentials, function(){
									
									// All the features have been downloaded and inserted
									// for this layer.  Increment the layerFinishedCount 
									incrementLayerFinishedCount();
									
									// If all the layers have finished downloading,
									// call the callback.
									if(doneGettingLayers() && Arbiter.Util.funcExists(onSuccess)){
										
										onSuccess.call(context);
									}
								}, onFailure);
							}else{
								// All the features have been downloaded and inserted
								// for this layer.  Increment the layerFinishedCount 
								incrementLayerFinishedCount();
								
								// If all the layers have finished downloading,
								// call the callback.
								if(doneGettingLayers() && Arbiter.Util.funcExists(onSuccess)){
									
									onSuccess.call(context);
								}
							}
							
						}, onFailure);
					}, onFailure);
				}, onFailure);
			},
			failure: function(response){
				gotRequestBack = true;
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure();
				}
			}
		});
		
		// Couldn't find a way to set timeout for an openlayers
		// request, so I did this to abort the request after
		// 15 seconds of not getting a response
		window.setTimeout(function(){
			if(!gotRequestBack){
				request.abort();
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure();
				}
			}
		}, 30000);
	};
	
	var storeFeatureData = function(layers, bounds, cacheTiles, onSuccess, onFailure){
		reset();
		
		layerCount = layers.length;
		
		for(var i = 0; i < layers.length; i++){
			getLayerSchema(layers[i], bounds, function(){
				if(doneGettingLayers()){
					if(cacheTiles){
						console.log("caching tiles!");
						var olAOI = new OpenLayers.Bounds(bounds.getLeft(), 
								bounds.getBottom(), bounds.getRight(), bounds.getTop());
						
						Arbiter.getTileUtil().cacheTiles(olAOI, function(){
							console.log("Tiles cached!");
							onSuccess();
						}, onFailure);
					}else{
						console.log("not caching tiles!");
						if(Arbiter.Util.funcExists(onSuccess)){
							onSuccess();
						}
					}
				}
			}, onFailure);
		}
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
				Arbiter.Loaders.LayersLoader.load(function(){
					
					Arbiter.Cordova.doneCreatingProject();
				}, onFailure);
			};
			
			var onFailure = function(e){
				console.log("Arbiter.Cordova.Project", e);
				Arbiter.Cordova.errorCreatingProject(e);
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
				
				console.log("Arbiter.Cordova.Project.createProject bounds = ", bounds);
				
				if(layers.length > 0){
					storeData(context, layers, bounds, true, function(){
						onSuccess();
					}, onFailure);
				}else{
					// If there are no layers, that means that there are
					// either no layers, or it's just the osm default layer
					
					if(bounds === null){
						throw "Arbiter.Project.CreateProject bounds should not be " 
							+ bounds;
					}
					
					var olAOI = new OpenLayers.Bounds(bounds.getLeft(), 
							bounds.getBottom(), bounds.getRight(), bounds.getTop());
					
					Arbiter.getTileUtil().cacheTiles(olAOI, function(){
						console.log("Tiles cached!");
						onSuccess();
					}, onFailure);
				}
			});
		},
		
		addLayers: function(layers){
			var context = this;
			
			var onSuccess = function(){
				Arbiter.Cordova.doneAddingLayers();
			};
			
			var onFailure = function(e){
				Arbiter.Cordova.errorAddingLayers(e);
			};
			
			Arbiter.PreferencesHelper.get(Arbiter.AOI, context, function(_aoi){
				var aoi = _aoi.split(','); 
				var bounds = null;
				
				if(_aoi !== null && _aoi !== undefined && _aoi !== ""){
					bounds = new Arbiter.Util.Bounds(aoi[0], aoi[1], aoi[2], aoi[3]);
				}
				
				if(layers.length > 0){
					storeData(context, layers, bounds, false, function(){
						Arbiter.Loaders.LayersLoader.load(onSuccess, onFailure);
					}, onFailure);
				}else{
					// If there are no layers, that means that the 
					// default osm layer got added.
					Arbiter.Loaders.LayersLoader.load(onSuccess, onFailure);
				}
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
					
					if(Arbiter.Util.funcExists(onSuccess)){
						onSuccess();
					}
				});
			}, onFailure);
		},
		
		/**
		 * Get the area of interest in the aoi map and call the native method to
		 * save it to be set when onResume gets called on the MapActivity
		 */
		setProjectsAOI : function(layers) {
			var bbox = Arbiter.Map.getCurrentExtent().toBBOX();
			
			cordova.exec(null, null, "ArbiterCordova", "setProjectsAOI", [bbox]);
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
				Arbiter.Layers.SyncHelper.sync(true);
			}, onFailure);
		},
		
		zoomToSavedBounds: function(context, onSuccess, onFailure){
			Arbiter.PreferencesHelper.get(Arbiter.SAVED_BOUNDS, this, function(savedBounds){
				Arbiter.PreferencesHelper.get(Arbiter.SAVED_ZOOM_LEVEL, this, function(savedZoom){
					if(savedBounds && savedZoom){
						zoomToExtent(savedBounds, savedZoom);
						
						if(onSuccess !== null && onSuccess !== undefined){
							onSuccess.call(context);
						}
					}else{
						if(onFailure !== null && onFailure !== undefined){
							onFailure.call(context);
						}
					}
				});
			});
		},
		
		zoomToAOI: function(context, onSuccess, onFailure){
			Arbiter.PreferencesHelper.get(Arbiter.AOI, this, function(_aoi){
				console.log("zoomToAOI", _aoi);
				if(_aoi !== null && _aoi !== undefined 
						&& _aoi !== ""){
					
					var aoi = _aoi.split(',');
					
					Arbiter.Map.zoomToExtent(aoi[0], 
							aoi[1], aoi[2], aoi[3]);
				}else{
					Arbiter.Cordova.Project.zoomToDefault();
				}
				
				
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess.call(context);
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
		}
	};
})();