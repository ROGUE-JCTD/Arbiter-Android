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
			//var WGS84_Google_Mercator = new OpenLayers.Projection("EPSG:900913");
			//var WGS84 = new OpenLayers.Projection("EPSG:4326");
			
			map = new OpenLayers.Map({
				div: "map",
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
		}
	};
})();

Arbiter.Map.Layers = (function(){
	
	return {
		
		/**
		 * Gets a list of layers from the url
		 */
		getLayers: function(url){
			/*var request = new OpenLayers.Request.GET({
				url: serverInfo.url + "/wms?service=wms&version=1.1.1&request=getCapabilities",
				headers: {
					Authorization : 'Basic ' + encodedCredentials
				},
				user: serverInfo.username,
				password: serverInfo.password,
				callback: function(response){
					var capes = capabilitiesFormatter.read(response.responseText);
					
					if(capes && capes.capability && capes.capability.layers){
						var layer;
						var layersrs;
						var layerList = capes.capability.layers;
						layerList.sort(function(a, b){
							var titleA = a.title.toLowerCase();
							var titleB = b.title.toLowerCase();
							if(titleA < titleB) return -1;
							if(titleA > titleB) return 1;
							return 0;
						});
					}
				}*/
			
			return [{
				layername: "layer1"
			},{
				layername: "layer2"
			}];
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
		
		/**
		 * Add a layer to the map
		 */
		addLayer: function(layer){
			Arbiter.Map.getMap().addLayers([layer]);
		},
		
		/**
		 * 
		 */
		removeLayer: function(){
			
		}
	};
})();


Arbiter.Init();