Arbiter.Util = (function(){
	
	return {
		getEncodedCredentials: function(username, password){
			
			if((username === "" && password === "") 
					|| !Arbiter.Util.existsAndNotNull(username) 
					|| !Arbiter.Util.existsAndNotNull(password)){
				
				return null;
			}
			
			return $.base64.encode(username + ":" + password);
		},
		
		/**
		 * Parse the feature type for the workspace and feature type
		 * Any db queries won't use the workspace, but the http requests
		 * require it.
		 */
		parseFeatureType: function(_featureType){
			var colonIndex = _featureType.indexOf(":");
			var workspace = null;
			var featureType = _featureType;
			
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
		
		isArbiterWMSLayer: function(olLayer){
			
			if(this.existsAndNotNull(olLayer) && this.existsAndNotNull(olLayer.name) 
					&& (olLayer.name.indexOf(Arbiter.Layers.type.WMS) != -1)){
				return true;
			}
			
			return false;
		},
		
		isArbiterWFSLayer: function(olLayer){
			
			if(this.existsAndNotNull(olLayer) && this.existsAndNotNull(olLayer.name) 
					&& this.layerIsEditable(olLayer) 
					&& (olLayer.name.indexOf(Arbiter.Layers.type.WFS) != -1)){
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
		},
		
		getFileServiceURL: function(url, mediaName){
			var fileServiceURL = null;
			var prefix = null;

			if(url.indexOf("http://") === 0){
				prefix = "http://";
				fileServiceURL = url.substr(7);
			}else if(url.indexOf("https://") === 0){
				prefix = "https://";
				fileServiceURL = url.substr(8);
			}

			fileServiceURL = prefix + fileServiceURL.split('/')[0] + "/api/fileservice/";

			return fileServiceURL;
		},

		getFileServiceUploadURL: function(url){
			return Arbiter.Util.getFileServiceURL(url);
		},

		getFeatureTypeNoPrefix: function(featureType){
			
			featureType = featureType.split(':');
			
			return featureType[featureType.length - 1];
		},
		
		getTempFeatureTableName: function(featureType){
				
			return Arbiter.TEMP_FEATURE_TABLE_UUID + '_' + Arbiter.Util.getFeatureTypeNoPrefix(featureType);
		},
		
		printKVPairs: function(msg, obj){
			
			if(!Arbiter.Util.existsAndNotNull(msg)){
				msg = "";
			}
			
			if(!Arbiter.Util.existsAndNotNull(obj)){
				obj = {};
			}
			
			msg += " ";
			
			for(var key in obj){
				
				msg += key + " = " + obj[key];
			}
			
			console.log(msg);
		}
	};
})();