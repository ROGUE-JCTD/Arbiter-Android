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
						type: "png",
						resolutions: [156543.03390625, 78271.516953125, 39135.7584765625,
				                      19567.87923828125, 9783.939619140625, 4891.9698095703125,
				                      2445.9849047851562, 1222.9924523925781, 611.4962261962891,
				                      305.74811309814453, 152.87405654907226, 76.43702827453613,
				                      38.218514137268066, 19.109257068634033, 9.554628534317017,
				                      4.777314267158508, 2.388657133579254, 1.194328566789627,
				                      0.5971642833948135, 0.25, 0.1, 0.05],
				        serverResolutions: [156543.03390625, 78271.516953125, 39135.7584765625,
				                            19567.87923828125, 9783.939619140625,
				                            4891.9698095703125, 2445.9849047851562,
				                            1222.9924523925781, 611.4962261962891,
				                            305.74811309814453, 152.87405654907226,
				                            76.43702827453613, 38.218514137268066,
				                            19.109257068634033, 9.554628534317017,
				                            4.777314267158508, 2.388657133579254,
				                            1.194328566789627, 0.5971642833948135]
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