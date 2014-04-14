Arbiter.Layers.WMSLayer = (function(){
	
	return {
		create: function(key, schema){
			
			console.log("create wmsLayer for: " + schema.getPrefix() + ":" + schema.getFeatureType());
			
			var featureType = "";
			
			if(Arbiter.Util.existsAndNotNull(schema.getPrefix()) && schema.getPrefix() !== "null"){
				featureType += schema.getPrefix() + ":";
			}
			
			featureType += schema.getFeatureType();
			
			var layer = new OpenLayers.Layer.WMS(Arbiter.Layers.getLayerName(
					key, Arbiter.Layers.type.WMS),
					schema.getUrl(), {
						layers : featureType,
						transparent : true,
						format : "image/png"

					}, {
						isBaseLayer : false,
						transitionEffect : 'resize',
						visibility : schema.isVisible()
					});

			return layer;
		},
		
		refreshWMSLayer : function(key) {
			var layerName = Arbiter.Layers.getLayerName(key, Arbiter.Layers.type.WMS);
			
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