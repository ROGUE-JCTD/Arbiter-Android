Arbiter = (function(){
	
	return {
		Init: function(){
			Arbiter.Map.createMap();
		}
	};
})();

Arbiter.Map = (function(){
	
	return {
		createMap: function(){
			//var WGS84_Google_Mercator = new OpenLayers.Projection("EPSG:900913");
			//var WGS84 = new OpenLayers.Projection("EPSG:4326");
			
			this.map = new OpenLayers.Map({
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
		}
	};
})();

Arbiter.Init();