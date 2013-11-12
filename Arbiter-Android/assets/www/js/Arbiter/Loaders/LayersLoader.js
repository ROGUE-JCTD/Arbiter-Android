Arbiter.Loaders.LayersLoader = (function(){
	
	var clearMap = function() {
		console.log("clearMap");
		Arbiter.Layers.removeAllLayers();
		console.log("clearMap after");
	};
	
	var setBaseLayer = function(){
		var map = Arbiter.Map.getMap();
		if (map.layers.length) {
			Arbiter.Layers.setNewBaseLayer(map.layers[0]);
		}
	};
	
	var loadWFSLayer = function(key, schema){
		var olLayer = Arbiter.Layers.WFSLayer.create(key, schema);
		
		Arbiter.Layers.addLayer(olLayer);
		
		Arbiter.Loaders.FeaturesLoader.loadFeatures(schema, olLayer);
	};
	
	var loadWMSLayer = function(key, schema){
		var olLayer = Arbiter.Layers.WMSLayer.create(key, schema);
		
		Arbiter.Layers.addLayer(olLayer);
	};
	
	var loadLayers = function(includeDefaultLayer, defaultLayerVisibility){
		console.log("loadLayers clearMap begin");
		clearMap();
		console.log("loadLayers clearMap executed");
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
		
		for(var key in layerSchemas){
			schema = layerSchemas[key];
			
			if(schema.isEditable()){
				// Load the vector layer
				loadWFSLayer(key, schema);
				console.log("loadLayers wfsLayer loaded");
			}
			
			// Load the wms layer
			loadWMSLayer(key, schema);
		}
		
		setBaseLayer();
	};
	
	var loadDefaultLayerInfo = function(context, callback){
		Arbiter.PreferencesHelper.get(Arbiter.INCLUDE_DEFAULT_LAYER, Arbiter.Loaders.LayersLoader, function(includeDefaultLayer){
			console.log("includeDefaultLayer = " + includeDefaultLayer);
			
			Arbiter.PreferencesHelper.get(Arbiter.DEFAULT_LAYER_VISIBILITY, Arbiter.Loaders.LayersLoader, function(defaultLayerVisibility){
				console.log("defaultLayerVisibility = " + defaultLayerVisibility);
				
				callback.call(context, includeDefaultLayer, defaultLayerVisibility);
			});
		});
	};
	
	return {
		load: function(){
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
							loadLayers(includeDefaultLayer, defaultLayerVisibility);
						});
					});
				});
			});
		}
	};
})();