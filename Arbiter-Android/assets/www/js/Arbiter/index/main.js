var app = (function() {
	
	var waitFuncs = [];
	var ArbiterInitialized = false;

	/**
	 * On device ready
	 */
	var onDeviceReady = function() {
		Arbiter.Init(function() {
			
			// Get the file system for use in TileUtil.js
			Arbiter.FileSystem.setFileSystem(function(){
				
				// Make sure the directories for storing tiles exists
				Arbiter.FileSystem.ensureTileDirectoryExists(function(){
					
					// Get the saved bounds and zoom
					Arbiter.Cordova.Project.getSavedBounds(function(savedBounds, savedZoom){
						
						// Get the locale
						navigator.globalization.getLocaleName(function(locale){
							var localeCode = 'en';
							if(locale.value.indexOf('es') >= 0){
								localeCode = 'es'
							}
							Arbiter.Localization.setLocale(localeCode);
						
							// Get the AOI to check to see if it's been set
							Arbiter.PreferencesHelper.get(Arbiter.AOI, this, function(_aoi){
								
								var bounds = null;
								
								if(savedBounds !== null && savedBounds !== undefined 
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
								
								Arbiter.Cordova.OOM_Workaround
									.registerMapListeners();
							
								Arbiter.Controls.ControlPanel
									.registerMapListeners();
								
								Arbiter.setTileUtil(
									new Arbiter.Util.TileUtil(
										Arbiter.ApplicationDbHelper.getDatabase(),
										Arbiter.ProjectDbHelper.getProjectDatabase(),
										Arbiter.Map.getMap(),
										Arbiter.FileSystem.getFileSystem()
									)
								);
								
								Arbiter.Loaders.LayersLoader.addEventTypes();
								
								Arbiter.Loaders.LayersLoader.load();
								
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
					console.log("Error initializing Arbiter during Tile Directory creation", e);
				});
			}, function(e){
				console.log("Error initializing Arbiter - ", e);
			});
		});
	};
	
	var onOnline = function(){
		app.waitForArbiterInit(function(){
			Arbiter.Layers.toggleWMSLayers(true);
			
			Arbiter.isOnline(true);
		});
	};
	
	var onOffline = function(){
		app.waitForArbiterInit(function(){
			Arbiter.Layers.toggleWMSLayers(false);
			
			Arbiter.isOnline(false);
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
		}
	};
})();