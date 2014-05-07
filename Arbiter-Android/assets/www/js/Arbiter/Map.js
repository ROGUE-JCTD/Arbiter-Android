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
				numZoomLevels : 21,
				allOverlayers : true,
				controls : [ new OpenLayers.Control.Attribution(),
						new OpenLayers.Control.TouchNavigation({
							dragPanOptions : {
								enableKinetic : true
							},
							clickHandlerOptions: {
								double: false
							}
						}) ],
				layers : [ new OpenLayers.Layer.OSM("OpenStreetMap", null, {
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
			                            1.194328566789627, 0.5971642833948135],
			        transitionEffect: 'resize'
			    }) ],
				center : new OpenLayers.LonLat(742000, 5861000),
				zoom : 3
			});

			Arbiter.Map.setOSMLink();
			
			if (callback !== null && callback !== undefined) {
				callback.call();
			}
		},

		setOSMLink: function(){
			
			var olControlAttribution = $('.olControlAttribution');
			
			olControlAttribution.click(function(evt){
				
				evt.preventDefault();
				
				Arbiter.Cordova.osmLinkClicked();
			});
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