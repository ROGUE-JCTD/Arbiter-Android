Arbiter.Layers.WMSLayer = (function(){
	
	return {
		create: function(key, schema){
			var layer = new OpenLayers.Layer.WMS(Arbiter.Layers.getLayerName(
					key, "wms"),
					schema.getUrl() + "/wms", {
						layers : schema.getPrefix() + ":" + schema.getFeatureType(),
						transparent : true,
						format : "image/png"

					}, {
						isBaseLayer : false,
						transitionEffect : 'resize',
						visibility : schema.isVisible()
					});

			Arbiter.Cordova.OOM_Workaround.overrideGetURL(layer);

			return layer;
		},
		
		refreshWMSLayer : function(key) {
			var layerName = Arbiter.Layers.getLayerName(key, "wms");
			
			var map = Arbiter.Map.getMap();
			var layers = map.getLayersByName(layerName);

			// If there are no layers, throw an exception
			if (layers === undefined || layers === null || layers.length === 0) {
				throw "There are no layers on the map corresponding to layerId: "
						+ layerId;
			}

			var layer = layers[0];

			layer.mergeNewParams({
				'ver' : Math.random()
			});

			layer.redraw(true);
		}
	};
})();