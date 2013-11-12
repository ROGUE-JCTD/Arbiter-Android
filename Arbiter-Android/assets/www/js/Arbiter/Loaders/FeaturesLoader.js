Arbiter.Loaders.FeaturesLoader = (function(){
	var WGS84_Google_Mercator = "EPSG:900913";
	
	var wktFormatter = new OpenLayers.Format.WKT();
	
	var addAttributes = function(schema, dbFeature, olFeature){
		olFeature.attributes = {};
		
		var attributes = schema.getAttributes();
		
		for(var i = 0; i < attributes.length; i++){
			olFeature.attributes[attributes[i].getName()] = 
				dbFeature[attributes[i].getName()];
		}
	};
	
	var processFeature = function(schema, dbFeature, olLayer){
		var olFeature = wktFormatter.
			read(dbFeature[schema.getGeometryName()]);
		
		// make sure the geometry is in EPSG:900913
		var srid = schema.getSRID();
		
		if(srid !== WGS84_Google_Mercator){
			olFeature.geometry.transform
				(new OpenLayers.Projection(schema.getSRID()), 
					new OpenLayers.Projection(WGS84_Google_Mercator));
		}
		
		addAttributes(schema, dbFeature, olFeature);
		
		olLayer.addFeatures([olFeature]);
	};
	
	return {
		loadFeatures: function(schema, olLayer){
			Arbiter.FeatureTableHelper.loadFeatures(schema, this, function(feature){
				processFeature(schema, feature, olLayer);
			});
		}
	};
})();