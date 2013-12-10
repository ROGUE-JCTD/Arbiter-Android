var app = (function() {

	var waitFuncs = [];
	var ArbiterInitialized = false;

	/**
	 * On device ready
	 */
	var onDeviceReady = function() {
		Arbiter.Init(function() {
			Arbiter.Cordova.OOM_Workaround
				.registerMapListeners();
			
			Arbiter.Controls.ControlPanel
				.registerMapListeners();
			
			Arbiter.Loaders.LayersLoader.addEventTypes();
			
			Arbiter.Loaders.LayersLoader.load();
		});

		for ( var i = 0; i < waitFuncs.length; i++) {
			waitFuncs[i].call();
		}

		ArbiterInitialized = true;
	};
	
	/**
	 * Bind event listeners
	 */
	var bindEvents = function() {
		document.addEventListener('deviceready', onDeviceReady, false);
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