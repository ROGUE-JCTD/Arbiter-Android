Arbiter.Cordova.Project = (function(){
	var describeFeatureTypeReader = new OpenLayers.Format.WFSDescribeFeatureType();
	
	// When the layerFinishedCount reaches
	// the layerCount, execute the callback.
	var layerCount = null;
	var layerFinishedCount = 0;
	var layerSchemas = {};
	
	var incrementLayerFinishedCount = function(){
		layerFinishedCount++;
	};
	
	var doneGettingLayers = function(){
		return (layerFinishedCount == layerCount);
	};
	
	var getLayerSchema = function(layer, bounds, onSuccess, onFailure){
		console.log("getLayerSchema");
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
				console.log("dft response: ", response);
				var results = describeFeatureTypeReader.read(response.responseText);
				console.log("describeFeatureType: " + featureType, JSON.stringify(results));
				
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
				
				console.log("ready to update layer!");
				// Update the layers workspace in the Layers table.
				Arbiter.LayersHelper.updateLayer(featureType, content, this, function(){
					console.log("updateLayer done.")
					// After updating the layer workspace, 
					// add the layer to the GeometryColumns table
					Arbiter.GeometryColumnsHelper.addToGeometryColumns(schema, function(){
						console.log("addToGeometryColumns done.")
						// After adding the layer to the GeometryColumns table
						// create the feature table for the layer
						Arbiter.FeatureTableHelper.createFeatureTable(schema, function(){
							console.log("createFeatureTable done.")
							// After creating the feature table for the layer,
							// download the features from the layer
							context.downloadFeatures(schema, bounds, encodedCredentials, function(){
								
								// All the features have been downloaded and inserted
								// for this layer.  Increment the layerFinishedCount 
								incrementLayerFinishedCount();
								
								// If all the layers have finished downloading,
								// call the callback.
								if(doneGettingLayers() && Arbiter.Util.funcExists(onSuccess)){
									console.log("calling getLayerSchema callback");
									onSuccess.call(context);
								}
							}, onFailure);
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
		}, 15000);
	};
	
	var storeData = function(context, layers, bounds, onSuccess, onFailure){
		console.log("storeData", layers, bounds);
		Arbiter.ServersHelper.loadServers(context, function(){
			console.log("serversLoaded");
			context.storeFeatureData(layers, bounds, onSuccess, onFailure);
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
			
			var onFailure = function(e){
				console.log("Arbiter.Cordova.Project", e);
				Arbiter.Cordova.errorCreatingProject(e);
			};
			
			Arbiter.ProjectDbHelper.getProjectDatabase().close();
			Arbiter.FeatureDbHelper.getFeatureDatabase().close();
			
			Arbiter.PreferencesHelper.get(Arbiter.AOI, this, function(_aoi){
				if(_aoi !== null && _aoi !== undefined 
						&& _aoi !== ""){
					
					var aoi = _aoi.split(',');
					
					var bounds = new Arbiter.Util.Bounds(aoi[0], 
							aoi[1], aoi[2], aoi[3]);
					
					storeData(context, layers, bounds, function(){
						console.log("Ready to load layers!");
						Arbiter.Loaders.LayersLoader.load(function(){
							context.zoomToAOI(context, function(){
								Arbiter.Cordova.doneCreatingProject();
							}, onFailure);
						}, onFailure);
					}, onFailure);
				}
			});
		},
		
		addLayers: function(layers){
			var context = this;
			
			var onFailure = function(e){
				Arbiter.Cordova.errorAddingLayers(e);
			};
			
			Arbiter.PreferencesHelper.get(Arbiter.AOI, context, function(_aoi){
				var aoi = _aoi.split(','); 
				
				var bounds = new Arbiter.Util.Bounds(aoi[0], aoi[1], aoi[2], aoi[3]);
				storeData(context, layers, bounds, function(){
					Arbiter.Loaders.LayersLoader.load(null, onFailure);
				}, onFailure);
			}, onFailure);
		},
		
		storeFeatureData: function(layers, bounds, onSuccess, onFailure){
			layerCount = layers.length;
			
			for(var i = 0; i < layers.length; i++){
				getLayerSchema(layers[i], bounds, function(){
					if(doneGettingLayers()){
						if(Arbiter.Util.funcExists(onSuccess)){
							onSuccess();
						}
					}
				}, onFailure);
			}
		},
		
		/**
		 * callback is the callback to be executed
		 * after all of the features finish downloading
		 */
		downloadFeatures: function(schema, bounds, encodedCredentials, callback){
			var context = this;
			
			// Insert the downloaded features
			// into the layers feature table
			Arbiter.Util.Feature.downloadFeatures(schema, bounds, 
					encodedCredentials, function(schema, features){
				// Features will be in their native srid at this point so pass the srid of the schema
				Arbiter.FeatureTableHelper.insertFeatures(schema, schema.getSRID(), features, function(){
					callback.call();
				});
			});
		},
		
		/**
		 * Get the area of interest in the aoi map and call the native method to
		 * set the projects aoi
		 */
		setProjectsAOI : function(layers) {
			var bbox = Arbiter.Map.getCurrentExtent().toBBOX();
			console.log("setProjectAOI bbox = " + bbox);
			
			cordova.exec(null, null, "ArbiterCordova", "setProjectsAOI", [bbox]);
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
			console.log("zoomToAOI");
			Arbiter.PreferencesHelper.get(Arbiter.AOI, this, function(_aoi){
				console.log("Arbiter.Preferences.get callback");
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
		}
	};
})();