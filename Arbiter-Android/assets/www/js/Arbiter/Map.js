Arbiter.Map = (function() {
	var map = null;

	return {
		createMap : function(callback) {
			this.WGS84_Google_Mercator = new OpenLayers.Projection(
					"EPSG:900913");
			this.WGS84 = new OpenLayers.Projection("EPSG:4326");

			map = new OpenLayers.Map({
				div : "map",
				projection : this.WGS84_Google_Mercator,
				displayProjection : this.WGS84,
				theme : null,
				numZoomLevels : 19,
				allOverlayers : true,
				controls : [ new OpenLayers.Control.Attribution(),
						new OpenLayers.Control.TouchNavigation({
							dragPanOptions : {
								enableKinetic : true
							}
						}) ],
				layers : [ new OpenLayers.Layer.OSM("OpenStreetMap", null, {
					transitionEffect : 'resize'
				}) ],
				center : new OpenLayers.LonLat(742000, 5861000),
				zoom : 3
			});

			if (callback !== null && callback !== undefined) {
				callback.call();
			}
		},

		getMap : function() {
			return map;
		},

		getCurrentExtent : function() {
			return map.getExtent();
		},

		getZoom : function() {
			return map.getZoom();
		},
		
		zoomToExtent : function(left, bottom, right, top, zoomLevel) {
			if (zoomLevel === null || zoomLevel === undefined) {
				map.zoomToExtent([ left, bottom, right, top ]);
			}else{
				var bounds = new OpenLayers.Bounds(left, bottom, right, top);

				map.setCenter(bounds.getCenterLonLat(), zoomLevel, true);
			}
		}
	};
})();