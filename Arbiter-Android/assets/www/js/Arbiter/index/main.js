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
				
				// Get the AOI to check to see if it's been set
				Arbiter.PreferencesHelper.get(Arbiter.AOI, this, function(_aoi){
					
					if(_aoi !== null && _aoi !== undefined 
							&& _aoi !== ""){
						Arbiter.aoiHasBeenSet(true);
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
				console.log("Error initializing Arbiter - ", e);
			});
		});
	};
	
	var onOnline = function(){
		app.waitForArbiterInit(function(){
			Arbiter.Layers.toggleWMSLayers(true);
		});
	};
	
	var onOffline = function(){
		app.waitForArbiterInit(function(){
			Arbiter.Layers.toggleWMSLayers(false);
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