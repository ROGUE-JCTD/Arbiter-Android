Arbiter.Loaders.FeaturesLoader = (function(){
	var WGS84_Google_Mercator = "EPSG:900913";
	
	var wktFormatter = new OpenLayers.Format.WKT();
	
	var addMetadata = function(dbFeature, olFeature){
		if(olFeature.metadata === null 
				|| olFeature.metadata === undefined){
			
			olFeature.metadata = {};
		}
		
		olFeature.metadata[Arbiter.FeatureTableHelper.ID] = 
			dbFeature[Arbiter.FeatureTableHelper.ID]; 
		
		olFeature.metadata[Arbiter.FeatureTableHelper.MODIFIED_STATE] =
			dbFeature[Arbiter.FeatureTableHelper.MODIFIED_STATE];
		
		olFeature.metadata[Arbiter.FeatureTableHelper.SYNC_STATE] =
			dbFeature[Arbiter.FeatureTableHelper.SYNC_STATE];
	};
	
	var addAttributes = function(schema, dbFeature, olFeature){
		olFeature.attributes = {};
		
		var attributes = schema.getAttributes();
		
		var attributeName = null;
		
		for(var i = 0; i < attributes.length; i++){
			attributeName = attributes[i].getName();
			
			if(attributeName === Arbiter.FeatureTableHelper.FID){
				
				olFeature[Arbiter.FeatureTableHelper.FID] 
					= dbFeature[Arbiter.FeatureTableHelper.FID];
			}else if(attributeName !== Arbiter.FeatureTableHelper.SYNC_STATE
					&& attributeName !== Arbiter.FeatureTableHelper.MODIFIED_STATE){
				
				olFeature.attributes[attributeName] = 
					dbFeature[attributeName];
			}
		}
	};
	
	var setState = function(olFeature){
		var state = olFeature.metadata[Arbiter.FeatureTableHelper.MODIFIED_STATE];
		
		if(state === Arbiter.FeatureTableHelper.MODIFIED_STATES.DELETED){
			olFeature.state = OpenLayers.State.DELETE;
		}else if(state === Arbiter.FeatureTableHelper.MODIFIED_STATES.INSERTED){
			olFeature.state = OpenLayers.State.INSERT;
		}else if(state === Arbiter.FeatureTableHelper.MODIFIED_STATES.MODIFIED){
			olFeature.state = OpenLayers.State.UPDATE;
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
		
		addMetadata(dbFeature, olFeature);
		
		setState(olFeature);
		
		olLayer.addFeatures([olFeature]);
	};
	
	return {
		loadFeatures: function(schema, olLayer, onSuccess, onFailure){
			Arbiter.FeatureTableHelper.loadFeatures(schema, this, 
					function(feature, currentFeatureIndex, featureCount){
				try{
					if(feature !== null){
						processFeature(schema, feature, olLayer);
					}
				} catch (e) {
					if(Arbiter.Util.funcExists(onFailure)){
						onFailure(e);
					}
				}
				
				if(Arbiter.Util.funcExists(onSuccess)){
					if(featureCount > 0 && (currentFeatureIndex === (featureCount - 1))){
						onSuccess();
					}else{
						onSuccess();
					}
				}
					
			}, onFailure);
		}
	};
})();