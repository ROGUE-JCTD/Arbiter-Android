(function(){
	
	Arbiter.Validation.Feature = function(feature, removeInvalidGeometries){
		this.feature = feature;
		this.geometry = feature.geometry.clone();
		this.removeInvalidGeometries = removeInvalidGeometries;
		this.hasValidGeometries = null;
	};
	
	var construct = Arbiter.Validation.Feature;
	
	construct.REMOVED_DURING_VALIDATION = "removedDuringValidation";
	
	var prototype = Arbiter.Validation.Feature.prototype;
	
	/**
	* Validate the feature
	*
	* @method validate
	* @param {Boolean} [removeInvalidGeometries=false] Remove the geometries that aren't valid
	* @return {Array} Returns a list of invalid geometries or an empty list
	*/
	prototype.validate = function(_geometry, _parent, childIndex){
		
		var geometry = _geometry;
		var parent = _parent;
		
		if(!Arbiter.Util.existsAndNotNull(geometry)){
			geometry = this.geometry;
		}
		
		if(!Arbiter.Util.existsAndNotNull(parent)){
			parent = this.feature;
		}
		
		var olGeometryClass = geometry.CLASS_NAME;
		
		var invalidGeometries = [];
		
		if(olGeometryClass === "OpenLayers.Geometry.Point"){
			
			if(!this._validatePoint(geometry)){
				invalidGeometries.push(geometry);
				
				this._handleRemoveGeometry(parent, childIndex);
			}
		}else if(olGeometryClass === "OpenLayers.Geometry.LineString"){
			
			if(!this._validateLineString(geometry)){
				invalidGeometries.push(geometry);
				
				this._handleRemoveGeometry(parent, childIndex);
			}
		}else if(olGeometryClass === "OpenLayers.Geometry.Polygon"){
			
			if(!this._validatePolygon(geometry)){
				invalidGeometries.push(geometry);
				
				this._handleRemoveGeometry(parent, childIndex);
			}
		}else{ // Collection
			
			var invalidChildGeometries = null;
			
			for(var i = 0; i < geometry.components.length; i++){
				
				invalidChildGeometries = this.validate(geometry.components[i], geometry, i);
				
				invalidGeometries = invalidGeometries.concat(invalidChildGeometries);
			}
		
			this._removeEmptyCollections();
		}
			
		if(parent === this.feature){ // Only true for the first call
			
			this.hasValidGeometries = Arbiter.Util.existsAndNotNull(this.geometry);
			
			// If removing invalid geometries
			if(this.removeInvalidGeometries){
				
				// If no valid geometries, remove the feature
				if(!this.hasValidGeometries){
					
					this.feature.layer.removeFeatures(this.feature);
					
					if(!Arbiter.Util.existsAndNotNull(this.feature.metadata)){
						this.feature.metadata = {};
					}
					
					this.feature.metadata[construct.REMOVED_DURING_VALIDATION] = true;
				}else{ // If there are valid geometries, just change the geometry.
					
					this.feature.geometry = this.geometry;
				}
			}
		}
		
		return invalidGeometries;
	};
	
	prototype._removeEmptyCollections = function(geometry, parent, childIndex){
		
		if(!Arbiter.Util.existsAndNotNull(geometry)){
			geometry = this.geometry;
		}
		
		if(!Arbiter.Util.existsAndNotNull(parent)){
			parent = this.feature;
		}
		
		var olGeometryClass = geometry.CLASS_NAME;
		
		// If a geometry collection, multipoint, multiline, or multipolygon
		if(olGeometryClass !== "OpenLayers.Geometry.Point" 
			&& olGeometryClass !== "OpenLayers.Geometry.LineString" 
				&& olGeometryClass !== "OpenLayers.Geometry.Polygon"){
			
			if(geometry.components.length === 0){
				
				this._handleRemoveGeometry(parent, childIndex);
			}else{
				
				for(var i = 0; i < geometry.components.length; i++){
					
					this._removeEmptyCollections(geometry.components[i], geometry, i);
				}
			}
		}
	};
	
	prototype._handleRemoveGeometry = function(parent, childIndex){
		
		if(parent === this.feature){
			
			this.geometry = null;
		}else{
			
			parent.components.splice(childIndex, 1);
		}
	};
	
	prototype._validatePoint = function(geometry){
		
		if(Arbiter.Util.existsAndNotNull(geometry) 
				&& geometry.CLASS_NAME === "OpenLayers.Geometry.Point"){
			
			return true;
		}
		
		return false;
	};
	
	prototype._validateLineString = function(geometry){
		
		if(geometry.components.length == 2){
			
			if(geometry.components[0].equals(geometry.components[1])){
				return false;
			}
		}else if(geometry.components.length < 2){
			return false;
		}
		
		return true;
	};
	
	prototype._validatePolygon = function(geometry){
		
		if(geometry.components[0].components.length < 4){
			return false;
		}
		
		return true;
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