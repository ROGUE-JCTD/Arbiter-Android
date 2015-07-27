Arbiter.Cordova.Project = (function(){
	var describeFeatureTypeReader = new OpenLayers.Format.WFSDescribeFeatureType();
	
	var includeOOMWorkaround = true;
	
	var syncInProgress = false;
	
	var gettingUsersLocation = false;
	
	var getSchemaHelper = function(specificSchemas, layerId){
		
		specificSchemas.push(Arbiter.getLayerSchemas()[layerId]);
	};
	
	// This is awful, but for the sake of time...
	var layersAlreadyInProject = null;
	
	var getSchemasFromDbLayers = function(dbLayers){
		var specificSchemas = [];
		
		var layerId = null;
		var arbiterSchemas = Arbiter.getLayerSchemas();
		
		for(var i = 0; i < dbLayers.length; i++){
			layerId = dbLayers[i][Arbiter.LayersHelper.layerId()];
			
			getSchemaHelper(specificSchemas, layerId);
		}
		
		return specificSchemas;
	};
	
	var prepareSync = function(layers, bounds, cacheTiles, onSuccess, onFailure){
		
		Arbiter.Loaders.LayersLoader.load(function(){
			
			if(bounds !== "" && bounds !== null && bounds !== undefined){
				
				var specificSchemas = getSchemasFromDbLayers(layers);
				
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
		
		var schemaDownloader = new Arbiter.SchemaDownloader(layers, Arbiter.WFS_DFT_VERSION, function(_layersAlreadyInProject){
			
			layersAlreadyInProject = _layersAlreadyInProject;
			
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
			
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			Arbiter.Cordova.Project.updateBaseLayer(function(){
				Arbiter.PreferencesHelper.get(db, Arbiter.AOI, this, function(_aoi){
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
			}, onFailure);
		},
		
		cacheBaseLayer: function(){
			
			var context = this;
			
			if(syncInProgress){
				
				console.log("sync already in progress");
				
				return;
			}
			
			var fail = function(e){
				
				console.log("sync failed", e);
				
				if(syncInProgress){
					Arbiter.Cordova.syncCompleted();
				}
				
				syncInProgress = false;
			};
			
			Arbiter.Cordova.setState(Arbiter.Cordova.STATES.UPDATING);
			
			Arbiter.Cordova.Project.updateBaseLayer(function(){

				var baseLayerLoader = new Arbiter.Loaders.BaseLayer();
				
				baseLayerLoader.load(function(baseLayer){
					
					Arbiter.Loaders.LayersLoader.load(function(){
						
						var db = Arbiter.ProjectDbHelper.getProjectDatabase();
						
						Arbiter.PreferencesHelper.get(db, Arbiter.AOI, context, function(_aoi){
							
							if(_aoi !== null && _aoi !== undefined 
									&& _aoi !== ""){
								
								var aoi = _aoi.split(',');
								
								var bounds = new Arbiter.Util.Bounds(aoi[0], aoi[1], aoi[2], aoi[3]);
									
								var map = Arbiter.Map.getMap();
								
								var syncHelper = new Arbiter.Sync(map, bounds, true, function(){
									
									syncInProgress = false;
									
									Arbiter.Cordova.syncCompleted();
								}, fail, Arbiter.FileSystem.getFileSystem(), baseLayer, true, 
								Arbiter.ProjectDbHelper.getProjectDatabase(), Arbiter.FeatureDbHelper.getFeatureDatabase());
								
								syncInProgress = true;
								
								syncHelper.startTileCache();
							}
						}, fail);
					}, fail);
				}, fail);
			}, fail);
		},
		
		updateBaseLayer: function(onSuccess, onFailure){
			var baseLayerLoader = new Arbiter.Loaders.BaseLayer();
			
			var fail = function(e){
				console.log("Error changing base layer: " + e);
				if(Arbiter.Util.existsAndNotNull(onFailure)){
					onFailure(e);
				}
			};
			
			baseLayerLoader.load(onSuccess, fail);
		},
		
		addLayers: function(layers){
			var context = this;
			
			Arbiter.Cordova.setState(Arbiter.Cordova.STATES.UPDATING);
			
			var onSuccess = function(){
				
				if(Arbiter.Util.existsAndNotNull(layersAlreadyInProject) && layersAlreadyInProject.length > 0){
					Arbiter.Cordova.layersAlreadyInProject(layersAlreadyInProject);
				}
				
				layersAlreadyInProject = null;
				
				Arbiter.Cordova.syncCompleted();
			};
			
			var onFailure = function(e){
			//	Arbiter.Cordova.errorAddingLayers(e);
				Arbiter.Cordova.syncCompleted();
				
				layersAlreadyInProject = null;
			};
			
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			Arbiter.PreferencesHelper.get(db, Arbiter.AOI, context, function(_aoi){
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
		
		updateAOI: function(left, bottom, right, top){
			var aoi = left + ", " + bottom 
				+ ", " + right + ", " + top;
			
			// onSyncFailure execute the native
			// method to report the error to the
			// user.
			var onFailure = function(e){
				Arbiter.Cordova.errorUpdatingAOI(e);
			};
			
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			Arbiter.PreferencesHelper.put(db, Arbiter.AOI, aoi, this, function(){
				
				Arbiter.Cordova.Project.sync(true);
			}, onFailure);
		},
		
		getSavedBounds: function(onSuccess, onFailure){
			
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			Arbiter.PreferencesHelper.get(db, Arbiter.SAVED_BOUNDS, this, function(savedBounds){
				Arbiter.PreferencesHelper.get(db, Arbiter.SAVED_ZOOM_LEVEL, this, function(savedZoom){
					
					if(Arbiter.Util.funcExists(onSuccess)){
						onSuccess(savedBounds, savedZoom);
					}
				}, onFailure);
			}, onFailure);
		},
		
		zoomToAOI: function(onSuccess, onFailure){
			
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			Arbiter.PreferencesHelper.get(db, Arbiter.AOI, this, function(_aoi){
				
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
		
		zoomToCurrentPosition: function(onSuccess, onFailure){
			
			if(!gettingUsersLocation){
			
				try{
					var map = Arbiter.Map.getMap();
					
					var aoiLayer = map.getLayersByName(Arbiter.AOI)[0];
					
					if(!Arbiter.Util.funcExists(aoiLayer)){
						throw "AOI layer does not exist";
					}
					
					gettingUsersLocation = true;
					
					var onDone = function(){
						
						gettingUsersLocation = false;
						
						Arbiter.Cordova.finishedGettingLocation();
					};
					
					if(!Arbiter.Util.existsAndNotNull(Arbiter.findme)){
						Arbiter.findme = new Arbiter.FindMe(map, aoiLayer);
					}
					
					Arbiter.findme.getLocation(onDone, function(e){
						
						onDone();
						
						Arbiter.Cordova.alertGeolocationError();
					});
					
				}catch(e){
					console.log(e);
				}
			}
		},
		
		sync: function(_cacheTiles, _downloadOnly, _specificSchemas, onSuccess, onFailure){
			console.log("sync");
			
			var context = this;
			
			if(syncInProgress === true){
				
				console.log("sync is already in progress!");
				
				return;
			}
			
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
					((downloadOnly === true || downloadOnly === "true")
							&& specificSchemas.length > 0) || cacheTiles){
				
				Arbiter.Cordova.setState(Arbiter.Cordova.STATES.UPDATING);
				
				var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
				
				Arbiter.PreferencesHelper.get(projectDb, Arbiter.AOI, context, function(_aoi){
					
					if(_aoi !== null && _aoi !== undefined 
							&& _aoi !== ""){
						
						var aoi = _aoi.split(',');
						
						var bounds = new Arbiter.Util.Bounds(aoi[0], aoi[1], aoi[2], aoi[3]);
						
						Arbiter.PreferencesHelper.get(projectDb, Arbiter.BASE_LAYER, context, function(baseLayer){
							
							if(Arbiter.Util.existsAndNotNull(baseLayer)){
								try{
									// base layer is stored as an array of json objects
									baseLayer = JSON.parse(baseLayer)[0];
								}catch(e){
									console.log(e.stack);
								}
							}
							
							var syncHelper = new Arbiter.Sync(map, bounds, downloadOnly, function(){
								
								syncInProgress = false;
								
								if(Arbiter.Util.funcExists(onSuccess)){
									onSuccess();
								}else{
									Arbiter.Cordova.syncCompleted();
								}
							}, function(e){
								
								console.log("sync failed", e);
								
								syncInProgress = false;
								
								if(Arbiter.Util.funcExists(onFailure)){
									onFailure(e);
								}else{
									Arbiter.Cordova.syncCompleted();
								}
							}, Arbiter.FileSystem.getFileSystem(), baseLayer, cacheTiles, 
							Arbiter.ProjectDbHelper.getProjectDatabase(), Arbiter.FeatureDbHelper.getFeatureDatabase());
							
							if(downloadOnly === true || downloadOnly === "true"){
								
								syncHelper.setSpecificSchemas(specificSchemas);
							}
							
							syncInProgress = true;
							
							syncHelper.sync();
						}, function(e){
							if(Arbiter.Util.funcExists(onFailure)){
								onFailure(e);
							}
						});
					}
				}, function(e){
					
					if(Arbiter.Util.funcExists(onFailure)){
						onFailure(e);
					}
				});
			}else{
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess();
				}else{
					Arbiter.Cordova.syncCompleted();
				}
			}
		},
		
		getNotifications: function(syncId){
			
			console.log("getNoficiations: syncId = " + syncId);
			
			var context = this;
			
			if(syncInProgress){
				
				console.log("sync already in progress");
				
				return;
			}
			
			var fail = function(e){
				
				console.log("sync failed", e);
				
				if(syncInProgress){
					Arbiter.Cordova.syncCompleted();
				}
				
				syncInProgress = false;
			};
			
			var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			Arbiter.PreferencesHelper.get(projectDb, Arbiter.AOI, context, function(_aoi){
				
				if(_aoi !== null && _aoi !== undefined 
						&& _aoi !== ""){
					
					var aoi = _aoi.split(',');
					
					var bounds = new Arbiter.Util.Bounds(aoi[0], aoi[1], aoi[2], aoi[3]);
						
					var map = Arbiter.Map.getMap();
					
					var syncHelper = new Arbiter.Sync(map, bounds, false, function(){
						
						syncInProgress = false;
						
						Arbiter.Cordova.gotNotifications();
					}, fail, Arbiter.FileSystem.getFileSystem(), null, false, 
					Arbiter.ProjectDbHelper.getProjectDatabase(), Arbiter.FeatureDbHelper.getFeatureDatabase());
					
					syncInProgress = true;
					
					Arbiter.Cordova.setState(Arbiter.Cordova.STATES.UPDATING);
					
					syncHelper.syncId = syncId;
					
					syncHelper.getNotifications();
				}
			}, fail);
		}
	};
})();