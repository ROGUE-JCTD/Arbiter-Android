Arbiter.Layers = (function() {

	return {
		/**
		 * Get a name for the layer, supplying a
		 * com.lmn.Arbiter_Android.BaseClasses.Layer and a type (wms or wfs)
		 * 
		 * @param layerId
		 *            The id of the layer
		 * @param type
		 *            The type of the layer
		 */
		getLayerName : function(layerId, type) {
			if (layerId === null || layerId === undefined) {
				throw "Arbiter.Layers.getLayerName: id must not be " + layerId;
			}

			if (type === "wms" || type === "wfs") {
				return layerId + "-" + type;
			}

			throw "Arbiter.Layers.getLayerName: " + type
					+ " is not a valid type!";
		},
		
		setNewBaseLayer : function(layer) {
			var map = Arbiter.Map.getMap();
			map.setBaseLayer(layer);
		},

		/**
		 * Add a layer to the map
		 */
		addLayer : function(layer) {
			Arbiter.Map.getMap().addLayer(layer);
		},

		addDefaultLayer : function(visibility){
			var osmLayer = new OpenLayers.Layer.OSM("OpenStreetMap", null,
					{
						transitionEffect : 'resize'
					});

			this.addLayer(osmLayer);

			osmLayer.setVisibility(visibility);
		},
		
		/**
		 * Remove the layer from the map
		 * 
		 * @param layer
		 *            Layer to remove from the map
		 */
		removeLayerById : function(layerId) {
			var wmsName = this.getLayerName(layerId, "wms");
			var wfsName = this.getLayerName(layerId, "wfs");

			this.removeLayerByName(wmsName);
			this.removeLayerByName(wfsName);
		},

		removeDefaultLayer : function() {
			this.removeLayerByName("OpenStreetMap");
		},
		
		removeLayerByName : function(layerName) {
			var map = Arbiter.Map.getMap();
			var isBaseLayer = false;

			var layers = map.getLayersByName(layerName);

			if (layers && layers.length > 0) {
				isBaseLayer = layers[0].isBaseLayer;
				map.removeLayer(layers[0]);
			}

			if ((map.layers.length > 0) && (isBaseLayer === true)) {
				this.setNewBaseLayer(map.layers[0]);
			}
		},

		/**
		 * Remove all layers from the map
		 */
		removeAllLayers : function() {
			console.log("REMOVE ALL LAYERS FROM THE MAP");
			var map = Arbiter.Map.getMap();
			var layerCount = map.layers.length;

			for ( var i = 0; i < layerCount; i++) {
				map.removeLayer(map.layers[0]);
			}
		},

		/**
		 * Set the layers visibility
		 */
		toggleLayerVisibilityById : function(layerId) {
			var wmsName = this.getLayerName(layerId, "wms");
			var wfsName = this.getLayerName(layerId, "wfs");

			this.toggleLayerVisibilityByName(wmsName);
			this.toggleLayerVisibilityByName(wfsName);
		},

		toggleLayerVisibilityByName : function(layerName) {
			var map = Arbiter.Map.getMap();

			var layers = map.getLayersByName(layerName);

			if (layers && layers.length > 0) {
				var layer = layers[0];
				layer.setVisibility(!layer.getVisibility());
			}
		},
		
		toggleDefaultLayerVisibility : function() {
			this.toggleLayerVisibilityByName("OpenStreetMap");
		}
	};
})();