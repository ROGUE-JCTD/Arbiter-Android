Arbiter = (function(){
	var inited = false;
	
	// List of functions to call
	var funcToCall = [];
	
	/**
	 * Execute the functions that depend on init
	 */
	var executeFuncs = function(){
		inited = true;
		
		for(var i = 0; i < funcToCall.length; i++){
			funcToCall.call();
		}
	};
	
	return {
		Init: function(){
			Arbiter.Map.createMap();
			
			executeFuncs();
		},
		
		/**
		 * Make sure the app has been initialized, before executing func.
		 * This is necessary for the loading calls on the native side.
		 */
		waitForInit: function(func){
			if(inited === false){
				funcToCall.push(func);
				return;
			}
			
			func.call();
		}
	};
})();

Arbiter.Map = (function(){
	var map = null;
	
	return {
		createMap: function(){
			var WGS84_Google_Mercator = new OpenLayers.Projection("EPSG:900913");
			var WGS84 = new OpenLayers.Projection("EPSG:4326");
			
			map = new OpenLayers.Map({
				div: "map",
				projection: WGS84_Google_Mercator,
				displayProjection: WGS84,
				theme: null,
				numZoomLevels: 19,
				controls: [
				   // new OpenLayers.Control.Attribution(),
				    new OpenLayers.Control.TouchNavigation({
				    	dragPanOptions: {
				    		enableKinetic: true
				    	}
				    })//,
				   // new OpenLayers.Control.Zoom()
				 ],
				 layers: [
				     new OpenLayers.Layer.OSM("OpenStreetMap", null, {
				    	 transitionEffect: 'resize'
				     })
				 ],
				 center: new OpenLayers.LonLat(742000, 5861000),
				 zoom: 3
			});
		},
		
		getMap: function(){
			return map; 
		},
		
		getCurrentExtent: function(){
			return map.getExtent();
		},
		
		zoomToExtent: function(left, bottom, right, top){
			map.zoomToExtent([left, bottom, right, top]);
		}
	};
})();

Arbiter.Map.Layers = (function(){
	
	return {
		
		
		/**
		 * Create a layer
		 */
		createLayer: function(lastId, params){
			var layer = new OpenLayers.Layer.Vector(lastId + "-wfs", {
				strategies : params.strategies,
				projection : new OpenLayers.Projection(params.srsName),
				protocol : params.protocol
			});
			
			return layer;
		},
		
		/**
		 * Add a layer to the map
		 */
		addLayer: function(layer){
			Arbiter.Map.getMap().addLayer(layer);
		},
		
		/**
		 * Remove the layer from the map
		 * @param layer Layer to remove from the map
		 */
		removeLayer: function(layer){
			Arbiter.Map.getMap().removeLayer(layer);
		},
		
		/**
		 * Remove all layers from the map
		 */
		removeAllLayers: function(){
			var map = Arbiter.Map.getMap();
			var layerCount = map.layers.length;
			
			for(var i = 0; i < layerCount; i++){
				this.removeLayer(map.layers[0]);
			}
		}
	};
})();