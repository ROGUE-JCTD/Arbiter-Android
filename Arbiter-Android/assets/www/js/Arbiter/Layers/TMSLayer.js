Arbiter.Layers.TMSLayer = (function(){
	
	return {
		create: function(key, schema){
			
			var featureType = "";
			
			if(Arbiter.Util.existsAndNotNull(schema.getPrefix()) && schema.getPrefix() !== "null"){
				featureType += schema.getPrefix() + ":";
			}
			
			featureType += schema.getFeatureType();
			
			var layer = null;
			
			var url = schema.getUrl().substring(0, schema.getUrl().length - 6);
			layer = new OpenLayers.Layer.TMS(Arbiter.Layers.getLayerName(
					key, Arbiter.Layers.type.TMS),
					url, {
						layername : featureType,
						isBaseLayer: false,
						type: "png"
					});
			
			return layer;
		},
		
		refreshTMSLayer : function(key) {
			var layerName = Arbiter.Layers.getLayerName(key, Arbiter.Layers.type.TMS);
			
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