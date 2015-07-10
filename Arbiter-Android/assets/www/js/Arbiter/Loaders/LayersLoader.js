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
	
	var setBaseLayer = function(olBaseLayer){
		var map = Arbiter.Map.getMap();
		if (map.layers.length) {
			Arbiter.Layers.setNewBaseLayer(olBaseLayer);
			
			map.setLayerIndex(olBaseLayer, 0);
			
			if(!Arbiter.Util.existsAndNotNull(olBaseLayer.metadata)){
				olBaseLayer.metadata = {};
			}
			
			olBaseLayer.metadata.isBaseLayer = true;
		}
	};
	
	var loadWFSLayer = function(key, schema, _onSuccess){
		var olLayer = Arbiter.Layers.WFSLayer.create(key, schema);
		
		olLayer.metadata["onSaveStart"] = function(event) {
			var added = 0;
			var modified = 0;
			var removed = 0;
			for (var i = 0; i < event.features.length; i++){
				var feature = event.features[i];
				if (feature.metadata !== undefined && feature.metadata !== null) {
					if (feature.metadata.modified_state === Arbiter.FeatureTableHelper.MODIFIED_STATES.MODIFIED){
						modified++;
					} else if (feature.metadata.modified_state === Arbiter.FeatureTableHelper.MODIFIED_STATES.DELETED) {
						removed++;
					} else if (feature.metadata.modified_state === Arbiter.FeatureTableHelper.MODIFIED_STATES.INSERTED) {
						added++;
					}
				}
			}
			var getFeatureString = function(count) {
				if(count === 1){
					return Arbiter.Localization.localize('feature');
				}
				return Arbiter.Localization.localize('features');
			}
			var commitMsg = '';
			if (added > 0){
				commitMsg += Arbiter.Localization.localize('added') + ' ' + added + ' ' +
					getFeatureString(added);
			}
			if (modified > 0){
				if (added > 0){
					commitMsg += ', ';
				}
				commitMsg += Arbiter.Localization.localize('modified') + ' ' + modified + ' ' +
					getFeatureString(modified);
			}
			if (removed > 0){
				if (added > 0 || modified > 0){
					commitMsg += ', ';
				}
				commitMsg += Arbiter.Localization.localize('removed') + ' ' + removed + ' ' +
					getFeatureString(removed);
			}
			commitMsg += ' ' + Arbiter.Localization.localize('viaArbiter') + '.';
			console.log(commitMsg);
			event.object.layer.protocol.options.handle = commitMsg;
		};
		
		Arbiter.Layers.addLayer(olLayer);
		
		olLayer.setVisibility(schema.isVisible());
		
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
	
	var loadTMSLayer = function(key, schema){
		var olLayer = Arbiter.Layers.TMSLayer.create(key, schema);
		
		Arbiter.Layers.addLayer(olLayer);
		
		olLayer.setVisibility(schema.isVisible());
		
		return olLayer;
	};
	
	var loadWMSLayer = function(key, schema){
		var olLayer = Arbiter.Layers.WMSLayer.create(key, schema);
		
		Arbiter.Layers.addLayer(olLayer);
		
		var appDb = Arbiter.ApplicationDbHelper.getDatabase();
		
        Arbiter.PreferencesHelper.get(appDb, Arbiter.NO_CON_CHECKS, this, function(value) {
            if (value === 'true') {
                olLayer.setVisibility(schema.isVisible());
            } else {
                olLayer.setVisibility(schema.isVisible() && Arbiter.isOnline());
            }
        });
		
		return olLayer;
	};
	
	var addAOIToMap = function(_aoi){
		
		var context = this;
		
		var map = Arbiter.Map.getMap();
		
		var aoi = _aoi.split(',');
		
		var bounds = new OpenLayers.Bounds(aoi);
		
		var feature = new OpenLayers.Feature.Vector(bounds.toGeometry(), {});
		
		var oldLayers = map.getLayersByName(Arbiter.AOI);
		
		var aoiStyleMap = new OpenLayers.StyleMap({
             'default': new OpenLayers.Style({
                         fill: false,
                         strokeColor: 'red',
                         strokeWidth: 5
                 }) 
		});
		 
		var layer = new OpenLayers.Layer.Vector(Arbiter.AOI, { 
			styleMap: aoiStyleMap 
		});
		
		layer.addFeatures([feature]);
		
		if(oldLayers.length > 0){
			map.removeLayer(oldLayers[0]);
		}
		
		map.addLayer(layer);
		
		// Make sure the aoi is the last layer and the highest
		map.setLayerIndex(layer, (map.layers.length - 1));
		//layer.setZIndex(726);
	};
	
	var loadAOILayer = function(){
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.get(projectDb, Arbiter.AOI, Arbiter.Loaders.LayersLoader, function(aoi){
			
			if(aoi !== null && aoi !== undefined && aoi !== ""){
				addAOIToMap(aoi);
			}
		}, function(e){
			console.log("Arbiter.Loaders.LayersLoader - Error loading aoi layer", e);
		});
	};
	
	var loadLayers = function(baseLayer, dbLayers, onSuccess){
		
		reset();
		
		clearMap();
		
		var layerSchemas = Arbiter.getLayerSchemas();
		
		var layer = null;
		var olBaseLayer = null;
		
		var disableWMS = false;
		
		var doWork = function() {
			if(!Arbiter.Util.existsAndNotNull(baseLayer)
			|| (Arbiter.Util.existsAndNotNull(baseLayer)
			&& baseLayer[Arbiter.BaseLayer.SERVER_NAME] === "OpenStreetMap")){
				
				olBaseLayer = Arbiter.Layers.addDefaultLayer(true);
			}
			
			if(layerSchemas === undefined 
					|| layerSchemas === null 
					|| (Arbiter.getLayerSchemasLength() === 0)){
				
				setBaseLayer(olBaseLayer);
				
				loadAOILayer();
				
				if(Arbiter.Util.funcExists(onSuccess)){
					isDone(onSuccess);
				}
				
				return;
			}
			
			var schema;
			var key;
			var editableLayers = 0;
			var featureType = null;
			var serverType = null;
			var isBaseLayer = false;
			
			for(var i = 0; i < dbLayers.length; i++){
				key = dbLayers[i][Arbiter.LayersHelper.layerId()];
				
				schema = layerSchemas[key];
				
				featureType = "";
				
				if(Arbiter.Util.existsAndNotNull(schema.getPrefix()) && schema.getPrefix() !== "null"){
					featureType += schema.getPrefix() + ":";
				}
				
				featureType += schema.getFeatureType();
				
				if(Arbiter.Util.existsAndNotNull(baseLayer) && (featureType === baseLayer[Arbiter.BaseLayer.FEATURE_TYPE])){
					isBaseLayer = true;
				}
				
				serverType = schema.getServerType();
				
				if(serverType === "WMS"){
					if (!disableWMS || isBaseLayer) {
						layer = loadWMSLayer(key, schema, isBaseLayer);
					}
				}else if(serverType === "TMS"){
					layer = loadTMSLayer(key, schema, isBaseLayer);
				}else{
					console.log("Invalid server type: " + serverType);
				}
				
				if(isBaseLayer === true){
					olBaseLayer = layer;
					isBaseLayer = false;
					dbLayers.splice(i--, 1);
				}else{
					if(serverType === "WMS"){
						layersToLoad++;
					}
				}
			}
			
			for(var i = 0; i < dbLayers.length; i++){
				
				key = dbLayers[i][Arbiter.LayersHelper.layerId()];
				
				schema = layerSchemas[key];
				
				if(schema.isEditable()){
					
					editableLayers++;
					// Load the vector layer
					loadWFSLayer(key, schema, onSuccess);
				}else{
					layersToLoad--;
					
					isDone(onSuccess);
				}
			}
			
			loadAOILayer();
			
			if(Arbiter.Util.existsAndNotNull(olBaseLayer)){
				setBaseLayer(olBaseLayer);
			}
			
			if(editableLayers === 0 
					&& Arbiter.Util.funcExists(onSuccess)){
				
				onSuccess();
			}
		}
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.get(projectDb, Arbiter.DISABLE_WMS, Arbiter.Loaders.LayersLoader, function(_disableWMS){
			if (_disableWMS !== undefined && _disableWMS !== null) {
				disableWMS = _disableWMS == 'true';
			}
			doWork();
		}, function(e){
			doWork();
		});
	};
	
	/**
	 * redraw the wfsLayers
	 */
	var redrawWFSLayers = function(){
		var map = Arbiter.Map.getMap();
		
		var wfsLayers = map.getLayersByClass("OpenLayers.Layer.Vector");
		
		for(var i = 0; i < wfsLayers.length; i++){
			wfsLayers[i].redraw();
		}
	};
	
	var checkSupportedCRS = function(baseLayer, dbLayers){
		
		var layerId = null;
		var schema = null;
		var proj4def = null;
		var crs = null;
		var unsupportedLayer = null;
		
		var schemas = Arbiter.getLayerSchemas();
		
		var layerTitleKey = Arbiter.LayersHelper.layerTitle();
		var workspaceKey = Arbiter.LayersHelper.workspace();
		var srsKey = Arbiter.GeometryColumnsHelper.featureGeometrySRID();
		var serverIdKey = Arbiter.LayersHelper.serverId();
		
		var unsupportedLayers = [];
		
		var obj = null;
		
		for(var i = 0; i < dbLayers.length; i++){
			
			layerId = dbLayers[i][Arbiter.LayersHelper.layerId()];
			
			schema = schemas[layerId];
			
			if(dbLayers[i][Arbiter.LayersHelper.featureType()] !== baseLayer[Arbiter.BaseLayer.FEATURE_TYPE] && schema.isEditable()){
				
				crs = schema.getSRID();
				
				proj4def = Proj4js.defs[crs];
				
				if(!Arbiter.Util.existsAndNotNull(proj4def)){
					
					// Add the layer to the list of unsupported layers
					// and decrement the index to continue iterating
					unsupportedLayer = dbLayers.splice(i--, 1);
					
					if(unsupportedLayer.constructor === Array){
						unsupportedLayer = unsupportedLayer[0];
					}
					
					obj = {};
					
					obj[layerTitleKey] = unsupportedLayer[layerTitleKey];
					obj[workspaceKey] = unsupportedLayer[workspaceKey];
					obj[serverIdKey] = unsupportedLayer[serverIdKey];
					obj[srsKey] = crs;
					
					unsupportedLayers.push(obj);
				}
			}else{
				console.log("its the baselayer or isn't editable!");
			}
		}
		
		console.log("unsupportedLayers", unsupportedLayers);
		
		return unsupportedLayers;
	};
	
	return {
		DONE_LOADING_LAYERS: "arbiter_done_loading_layers",
		
		load: function(onSuccess, onFailure){
			var context = this;
			
			var layersWithUnsupportedCRS = null;
			
			// Load the servers
			Arbiter.ServersHelper.loadServers(context, function(){
				
				// Load the layers from the database
				Arbiter.LayersHelper.loadLayers(context, function(layers){
					
					var baseLayerLoader = new Arbiter.Loaders.BaseLayer();
					
					baseLayerLoader.load(function(baseLayer){
						
						// Load the layer schemas with layer data loaded from the db
						Arbiter.FeatureTableHelper.loadLayerSchemas(layers, function(){
							
							// Will return the unsupported layers and remove them from the layers array
							layersWithUnsupportedCRS = checkSupportedCRS(baseLayer, layers);
							
								// Load the layers onto the map
								loadLayers(baseLayer, layers, function(){
									
									var controlPanelHelper = new Arbiter.ControlPanelHelper();
									controlPanelHelper.getActiveControl(function(activeControl){
										
										controlPanelHelper.getLayerId(function(layerId){
											
											controlPanelHelper.getGeometryType(function(geometryType){
												
												if(activeControl == controlPanelHelper.CONTROLS.INSERT){
													Arbiter.Controls.ControlPanel.startInsertMode(layerId, geometryType);
												}
												
												if(Arbiter.Util.funcExists(onSuccess)){
													onSuccess();
												}
												
												// Sometimes after loading,
												// the wfs layers do not get drawn
												// properly.  This ensures they
												// get drawn correctly.
												redrawWFSLayers();
												
												if(Arbiter.Util.existsAndNotNull(layersWithUnsupportedCRS) 
														&& layersWithUnsupportedCRS.length){
													
													Arbiter.Cordova.reportLayersWithUnsupportedCRS(layersWithUnsupportedCRS);
												}
											}, onFailure);
										}, onFailure)
									}, onFailure);
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