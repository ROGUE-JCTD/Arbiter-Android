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
		
		funcExists: function(func){
			if(func !== undefined && func !== null){
				return true;
			}
			
			return false;
		}
	};
})();