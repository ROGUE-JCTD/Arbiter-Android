Arbiter = (function(){
	var layerSchemas = {};
	
	return {
		
		// Keys for preferences table in project db
		INCLUDE_DEFAULT_LAYER: "include_default_layer",
		DEFAULT_LAYER_VISIBILITY: "default_layer_visibility",
		AOI: "aoi",
		SELECTED_FID: "selected_fid",
		SELECTED_LAYER: "selected_layer",
		
		SAVED_ZOOM_LEVEL: "saved_zoom_level",
		SAVED_BOUNDS: "saved_bounds",
		
		DEFAULT_ZOOM_EXTENT: "-20037508.34,-20037508.34,20037508.34,20037508.34",
		
		Init: function(createMapCallback){
			Arbiter.Map.createMap(createMapCallback);
		},
		
		getLayerSchemas: function(){
			return layerSchemas;
		},
		
		/**
		 * key is the id of the layer in the 
		 * projects database
		 */
		putLayerSchema: function(key, schema){
			layerSchemas[key] = schema;
		},
		
		resetLayerSchemas: function(){
			layerSchemas = {};
		}
	};
})();