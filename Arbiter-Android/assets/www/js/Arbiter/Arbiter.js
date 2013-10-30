Arbiter = (function(){
	
	return {
		Init: function(){
			Arbiter.Map.createMap();
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
				allOverlayers: true,
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
		 * Get a name for the layer, supplying a com.lmn.Arbiter_Android.BaseClasses.Layer
		 * and a type (wms or wfs)
		 * @param layerId The id of the layer
		 * @param type The type of the layer
		 */
		getLayerName: function(layerId, type){
			if(layerId === null || layerId === undefined){
				throw "Arbiter.Map.Layers.getLayerName: id must not be " + layerId;
			}
			
			if(type === "wms" || type === "wfs"){
				return layerId + "-" + type;
			}
			
			throw "Arbiter.Map.Layers.getLayerName: " + type + " is not a valid type!";
		},
		
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
		
		setNewBaseLayer: function(layer){
			var map = Arbiter.Map.getMap();
			map.setBaseLayer(layer);
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
		removeLayerById: function(layerId){
			console.log("Arbiter.Map.Layer.removeLayer");
			var map = Arbiter.Map.getMap();
			
			var wmsName = this.getLayerName(layerId, "wms");
			var wfsName = this.getLayerName(layerId, "wfs");
			
			console.log("Arbiter.Map.Layer.removeLayer: wmsName = " + wmsName);
			console.log("Arbiter.Map.Layer.removeLayer: wfsName = " + wfsName);
			
			this.removeLayerByName(wmsName);
			this.removeLayerByName(wfsName);
		},
		
		removeLayerByName: function(layerName){
			var map = Arbiter.Map.getMap();
			var isBaseLayer = false;
			
			var layers = map.getLayersByName(layerName);
			
			if(layers && layers.length > 0){
				isBaseLayer = layers[0].isBaseLayer;
				map.removeLayer(layers[0]);
			}
			
			if((map.layers.length > 0) && (isBaseLayer === true)){
				this.setNewBaseLayer(map.layers[0]);
			}
		},
		
		/**
		 * Remove all layers from the map
		 */
		removeAllLayers: function(){
			var map = Arbiter.Map.getMap();
			var layerCount = map.layers.length;
			
			for(var i = 0; i < layerCount; i++){
				map.removeLayer(map.layers[0]);
			}
		}
	};
})();