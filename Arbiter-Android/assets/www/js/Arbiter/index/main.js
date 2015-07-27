var app = (function() {
	
	var waitFuncs = [];
	var ArbiterInitialized = false;
	
	/**
	 * On device ready
	 */
	var onDeviceReady = function() {
        var appDb = Arbiter.ApplicationDbHelper.getDatabase();
	    Arbiter.PreferencesHelper.get(appDb, Arbiter.NO_CON_CHECKS, this, function(value) {
            if (value === 'true') {
                Arbiter.isOnline(true);
            } else {
                Arbiter.isOnline('onLine' in navigator && navigator.onLine);
            }
        });
	    
	    var context = this;
	    
		Arbiter.Init(function() {
			
			// Get the file system for use in TileUtil.js
			Arbiter.FileSystem.setFileSystem(function(fileSystem){
				
				var baseLayerLoader = new Arbiter.Loaders.BaseLayer();
				
				// Make sure the directories for storing tiles exists
				baseLayerLoader.load(function(baseLayer){

					// Get the saved bounds and zoom
					Arbiter.Cordova.Project.getSavedBounds(function(savedBounds, savedZoom){

						// Get the locale
						navigator.globalization.getLocaleName(function(locale){
							var localeCode = 'en';
							if(locale.value.indexOf('es') >= 0){
								localeCode = 'es'
							}
							Arbiter.Localization.setLocale(localeCode);

							var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();

							// Get the AOI to check to see if it's been set
							Arbiter.PreferencesHelper.get(projectDb, Arbiter.AOI, context, function(_aoi){

								var bounds = null;

								Arbiter.PreferencesHelper.get(appDb, Arbiter.SWITCHED_PROJECT, context, function(value) {
									if(value !== "true" && savedBounds !== null && savedBounds !== undefined
											&& savedZoom !== null
											&& savedZoom !== undefined){

										bounds = savedBounds.split(',');

										if(_aoi !== null && _aoi !== undefined
												&& _aoi !== ""){

											Arbiter.aoiHasBeenSet(true);
										}

										Arbiter.Map.zoomToExtent(bounds[0],
												bounds[1], bounds[2],
												bounds[3], savedZoom);
									}else{

										if(_aoi !== null && _aoi !== undefined
												&& _aoi !== ""){

											Arbiter.aoiHasBeenSet(true);

											bounds = _aoi.split(',');

											Arbiter.Map.zoomToExtent(bounds[0],
													bounds[1], bounds[2],
													bounds[3]);
										}
									}
									Arbiter.PreferencesHelper.put(appDb, Arbiter.SWITCHED_PROJECT, "false", context);
								}, function(e){
									console.log("Could not read SWITCHED_PROJECT variable from Preferences database: " + JSON.stringify(e));
								});

								Arbiter.Cordova.OOM_Workaround
									.registerMapListeners();

								Arbiter.Controls.ControlPanel
									.registerMapListeners();

								Arbiter.setTileUtil(
									new Arbiter.TileUtil(
										Arbiter.ProjectDbHelper.getProjectDatabase(),
										Arbiter.Map.getMap()
									)
								);

								Arbiter.Loaders.LayersLoader.addEventTypes();

								Arbiter.Loaders.LayersLoader.load(function(){

									Arbiter.PreferencesHelper.get(projectDb, Arbiter.ALWAYS_SHOW_LOCATION, context, function(alwaysShowLocation){

										if(alwaysShowLocation === true || alwaysShowLocation === "true"){

											var map = Arbiter.Map.getMap();

											var aoiLayer = map.getLayersByName(Arbiter.AOI)[0];

											if(Arbiter.Util.existsAndNotNull(aoiLayer)){

												Arbiter.findme = new Arbiter.FindMe(map, aoiLayer);

												Arbiter.findme.watchLocation(function(e){

													Arbiter.Cordova.alertGeolocationError();
												});
											}else{
												console.log("There is no aoi layer!");
											}
										}
									}, function(e){ console.log((Arbiter.Util.existsAndNotNull(e.stack)) ? e.stack : e); });

									//findMeOOM();

									Arbiter.Cordova.appFinishedLoading();
								}, function(e){
									console.log("Could not load layers during initialization: " + JSON.stringify(e));
								});

								for ( var i = 0; i < waitFuncs.length; i++) {
									waitFuncs[i].call();
								}

								ArbiterInitialized = true;
							});
						}, function(e){
							console.log("Error initializing Arbiter while getting the locale", e);
						});
					}, function(e){
						console.log("Error initializing Arbiter while getting saved bounds", e);
					});
				}, function(e){
					console.log("Error initializing Arbiter loading base layer", e);
				});
			}, function(e){
				console.log("Error initializing Arbiter - ", e);
			});
		});
	};
	
	var onOnline = function(){
		app.waitForArbiterInit(function(){
			Arbiter.isOnline(true);
		});
	};
	
	var onOffline = function(){
		app.waitForArbiterInit(function(){
			var appDb = Arbiter.ApplicationDbHelper.getDatabase();
            Arbiter.PreferencesHelper.get(appDb, Arbiter.NO_CON_CHECKS, this, function(value) {
                if (value === 'true') {
                    Arbiter.isOnline(true);
                } else {
                    Arbiter.Layers.toggleWMSLayers(false);
                    Arbiter.isOnline(false);
                }
            });
		});
	};
	
	/**
	 * Bind event listeners
	 */
	var bindEvents = function() {
		document.addEventListener('deviceready', onDeviceReady, false);
		document.addEventListener('online', onOnline, false);
		document.addEventListener('offline', onOffline, false);
	};
	
	/**
	 * Initialize the app
	 */
	var Init = function() {
		bindEvents();
	};
	
	Init();
	
	return {
		waitForArbiterInit : function(func) {
			if (!ArbiterInitialized) {
				waitFuncs.push(func);
			} else {
				func.call();
			}
		},
		
		zoomToFeature: function(layerId, fid){
			
			var map = Arbiter.Map.getMap();
			
			var layer = Arbiter.Layers.getLayerById(layerId, Arbiter.Layers.type.WFS);
			
			var feature = layer.getFeatureByFid(fid);
			
			if(Arbiter.Util.existsAndNotNull(feature)){
				feature.geometry.calculateBounds();
				var bounds = feature.geometry.getBounds();
				
				var zoomForExtent = map.getZoomForExtent(bounds);
				
				if(zoomForExtent > 18){
					
					var centroid = feature.geometry.getCentroid();
					
					map.setCenter(new OpenLayers.LonLat(centroid.x, centroid.y), 18);
				}else{
					
					map.zoomToExtent(bounds);
				}
				
				feature.renderIntent = "select";
				
				layer.drawFeature(feature);
			}
		},
		
		showWMSLayersForServer: function(serverId){
			
			var schemas = Arbiter.getLayerSchemas();
			var schema = null;
			var layer = null;
			
			for(var key in schemas){
				
				schema = schemas[key];
				
				if(schema.getServerId() == serverId){
					
					layer = Arbiter.Layers.getLayerById(schema.getLayerId(), Arbiter.Layers.type.WMS);
					
					if(Arbiter.Util.existsAndNotNull(layer)){
						
						layer.setVisibility(true);
					}
				}
			}
		}
	};
})();