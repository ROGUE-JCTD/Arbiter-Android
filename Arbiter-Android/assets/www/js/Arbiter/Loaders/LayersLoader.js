Arbiter.Loaders.LayersLoader = (function(){
	var layersToLoad; // Number of layers
	var featuresLoadedFor; // Features loaded for how many layers
	var errorLoadingFeatures; // An array of feature types that couldn't be loaded
	
	var reset = function(){
		layersToLoad = 0;
		featuresLoadedFor = 0;
		errorLoadingFeatures = [];
	};
	
	var triggerDoneLoadingEvent = function(){
		var map = Arbiter.Map.getMap();
		
		map.events.triggerEvent(Arbiter.Loaders.
				LayersLoader.DONE_LOADING_LAYERS);
	};
	
	var isDone = function(onSuccess){
		if(featuresLoadedFor === layersToLoad){
			onSuccess();
			triggerDoneLoadingEvent();
		}
		
		if(errorLoadingFeatures.length > 0){
			console.log("DISPLAY ERROR LOADING FEATURES", errorLoadingFeatures);
			Arbiter.Cordova.errorLoadingFeatures(errorLoadingFeatures);
		}
	};
	
	var clearMap = function() {
		Arbiter.Layers.removeAllLayers();
	};
	
	var setBaseLayer = function(){
		var map = Arbiter.Map.getMap();
		if (map.layers.length) {
			Arbiter.Layers.setNewBaseLayer(map.layers[0]);
		}
	};
	
	var loadWFSLayer = function(key, schema, _onSuccess){
		var olLayer = Arbiter.Layers.WFSLayer.create(key, schema);
		
		Arbiter.Layers.addLayer(olLayer);
		
		olLayer.setVisibility(schema.isVisible());
		
		// TODO: BUG - The success callback isn't getting called
		// for every feature. It should only be called after all
		// of the features are loaded for the layer.
		var onSuccess = function(){
			featuresLoadedFor++;
			isDone(_onSuccess);
		};
		
		var onFailure = function(){
			errorLoadingFeatures.push(schema.getFeatureType());
			onSuccess();
		};
		
		Arbiter.Loaders.FeaturesLoader.loadFeatures(schema, 
				olLayer, onSuccess, onFailure);
	};
	
	var loadWMSLayer = function(key, schema){
		var olLayer = Arbiter.Layers.WMSLayer.create(key, schema);
		
		Arbiter.Layers.addLayer(olLayer);
		
		olLayer.setVisibility(schema.isVisible());
	};
	
	var loadLayers = function(includeDefaultLayer, defaultLayerVisibility, onSuccess){
		reset();
		
		clearMap();
		
		var layerSchemas = Arbiter.getLayerSchemas();
		
		var layer;

		if (includeDefaultLayer === "true") {
			Arbiter.Layers.addDefaultLayer(defaultLayerVisibility);
		}
		
		if(layerSchemas === undefined 
				|| layerSchemas === null){
			
			return;
		}
		
		var schema;
		var key;
		
		for(key in layerSchemas){
			schema = layerSchemas[key];
			
			// Load the wms layer
			loadWMSLayer(key, schema);
			
			layersToLoad++;
		}
		
		for(key in layerSchemas){
			schema = layerSchemas[key];
			
			if(schema.isEditable()){
				// Load the vector layer
				loadWFSLayer(key, schema, onSuccess);
			}
		}
		
		setBaseLayer();
	};
	
	var loadDefaultLayerInfo = function(context, onSuccess, onFailure){
		Arbiter.PreferencesHelper.get(Arbiter.INCLUDE_DEFAULT_LAYER, Arbiter.Loaders.LayersLoader, function(includeDefaultLayer){
			
			Arbiter.PreferencesHelper.get(Arbiter.DEFAULT_LAYER_VISIBILITY, Arbiter.Loaders.LayersLoader, function(defaultLayerVisibility){
				
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess.call(context, includeDefaultLayer, defaultLayerVisibility);
				}
			}, onFailure);
		}, onFailure);
	};
	
	return {
		DONE_LOADING_LAYERS: "arbiter_done_loading_layers",
		
		load: function(onSuccess, onFailure){
			var context = this;
			
			// Load the servers
			Arbiter.ServersHelper.loadServers(this, function(){
				
				// Load the layers from the database
				Arbiter.LayersHelper.loadLayers(this, function(layers){
					
					// Load the layer schemas with layer data loaded from the db
					Arbiter.FeatureTableHelper.loadLayerSchemas(layers, function(){
						
						// Load the default layer info
						loadDefaultLayerInfo(this, function(includeDefaultLayer, defaultLayerVisibility){
							
							// Load the layers onto the map
							loadLayers(includeDefaultLayer, defaultLayerVisibility, function(){
								if(Arbiter.Util.funcExists(onSuccess)){
									onSuccess();
								}
							});
						}, onFailure);
					}, onFailure);
				}, onFailure);
			}, onFailure);
		},
		
		addEventTypes: function(){
			var map = Arbiter.Map.getMap();
			
			map.events.addEventType(this.DONE_LOADING_LAYERS);
		}
	};
})();