(function(){
	
	Arbiter.Validation.Feature = function(feature){
		this.feature = feature;
		this.geometry = feature.geometry;
	};
	
	var prototype = Arbiter.Validation.Feature.prototype;
	
	/**
	* Validate the feature
	*
	* @method validate
	* @param {Boolean} [removeInvalidGeometries=false] Remove the geometries that aren't valid
	* @return {Object} Returns a list of invalid geometries or null
	*/
	prototype.validate = function(removeInvalidGeometries){
		
		var olGeometryClass = this.geometry.CLASS_NAME;
		
		if(olGeometryClass === "OpenLayers.Geometry.Point"){
			
		}else if(olGeometryClass === "OpenLayers.Geometry.LineString"){
			
		}else if(olGeometryClass === "OpenLayers.Geometry.Polygon"){
			
		}else{ // Collection
			
		}
	};
	
	prototype.checkFeatureAddedInsideAOI = function(){
		
		console.log("onFeatureAddedOutsideAOI", this.feature);
		
		var map = Arbiter.Map.getMap();
		
		var aoiLayer = map.getLayersByName(Arbiter.AOI);
		
		if(Arbiter.Util.existsAndNotNull(aoiLayer) && aoiLayer.length > 0){
			aoiLayer = aoiLayer[0];
		}
		
		if(Arbiter.Util.existsAndNotNull(aoiLayer)){
			
			if(Arbiter.Util.existsAndNotNull(this.feature)){
				
				this.feature.geometry.calculateBounds();
				
				var featureBounds = this.feature.geometry.getBounds();
				
				var aoiFeature = null;
				
				if(aoiLayer.features.length > 0){
					aoiFeature = aoiLayer.features[0];
				}
				
				if(Arbiter.Util.existsAndNotNull(aoiFeature)){
					
					aoiFeature.geometry.calculateBounds();
					
					var aoiBounds = aoiFeature.geometry.getBounds();
					
					if(featureBounds.intersectsBounds(aoiBounds)){
						
						console.log("inside the aoi!");
						
						return true;
					}
						
					return false;
				}
			}
		}
	};
	
})();