Arbiter.Util = (function(){
	
	return {
		getEncodedCredentials: function(username, password){
			return $.base64.encode(username + ":" + password);
		},
		
		/**
		 * Parse the feature type for the workspace and feature type
		 * Any db queries won't use the workspace, but the http requests
		 * require it.
		 */
		parseFeatureType: function(_featureType){
			var colonIndex = _featureType.indexOf(":");
			var workspace;
			var featureType;
			
			if(colonIndex >= 0){
				workspace = _featureType.substring(0, colonIndex);
				featureType = _featureType.substring(colonIndex + 1);
			}
			
			return {
				"prefix": workspace,
				"featureType": featureType
			};
		},
		
		/**
		 * layer names are in the following format:
		 * 		<id>-<wfs or wms>
		 * so split on "-" and return the first part.
		 */
		getLayerId: function(olLayer){
			return olLayer.name.split("-")[0];
		},
		
		getFeatureInNativeProjection: function(geometrySRID, nativeSRID, feature){
			if(geometrySRID === nativeSRID){
				return feature;
			}
			
			var clonedFeature = feature.clone();
			clonedFeature.geometry.transform(
					new OpenLayers.Projection(geometrySRID),
					new OpenLayers.Projection(nativeSRID));
			
			return clonedFeature;
		},
		
		funcExists: function(func){
			if(func !== undefined && func !== null){
				return true;
			}
			
			return false;
		},
		
		layerIsEditable: function(olLayer){
			if((olLayer instanceof OpenLayers.Layer.Vector)
					&& !(olLayer instanceof OpenLayers.Layer.Vector.RootContainer)){
				return true;
			}
			
			return false;
		},
		
		getSchemaFromOlLayer: function(olLayer){
			if((olLayer instanceof OpenLayers.Layer.Vector)
					&& !(olLayer instanceof OpenLayers.Layer.Vector.RootContainer)){
				
				var layerId = this.getLayerId(olLayer);
				
				var schemas = Arbiter.getLayerSchemas();
				
				if(schemas !== null && schemas !== undefined){
					return schemas[layerId];
				}
			}
			
			return null;
		},
		
		existsAndNotNull: function(someVar){
			if(someVar !== null && someVar !== undefined){
				return true;
			}
			
			return false;
		},
		
		getFeaturesById: function(layerId, arbiterId){
			var olLayer = Arbiter.Layers.getLayerById(layerId, Arbiter.Layers.type.WFS);
			
			var olFeatures = olLayer.features;
			
			var features = [];
			
			var olFeature = null;
			
			for(var i = 0; i < olFeatures.length; i++){
				
				olFeature = olFeatures[i];
				
				if(Arbiter.Util.existsAndNotNull(olFeature.metadata) 
						&& olFeature.metadata[Arbiter.FeatureTableHelper.ID] === arbiterId){
					features.push(olFeature);
				}
			}
			
			return features;
		}
	};
})();