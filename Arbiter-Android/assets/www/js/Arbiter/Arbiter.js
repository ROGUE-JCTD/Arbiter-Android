Arbiter = (function(){
	var layerSchemas = {};
	var layerSchemasLength = 0;
	
	var aoiHasBeenSet = false;
	var tileUtil = null;
	
	var isOnline = false;
	
	return {
		// WFS version for DescribeFeatureType request
		WFS_DFT_VERSION: "1.1.0",
		
		// Keys for preferences table in project db
		INCLUDE_DEFAULT_LAYER: "include_default_layer",
		DEFAULT_LAYER_VISIBILITY: "default_layer_visibility",
		AOI: "aoi",
		DISABLE_WMS: "disable_wms",
		DOWNLOAD_PHOTOS: "download_photos",
        NO_CON_CHECKS: "no_con_checks",
		PROJECT_NAME: "project_name",
		SELECTED_FID: "selected_fid",
		SELECTED_LAYER: "selected_layer",
		
		SAVED_ZOOM_LEVEL: "saved_zoom",
		SAVED_BOUNDS: "saved_bounds",
		SHOULD_ZOOM_TO_AOI: "should_zoom_to_aoi",
		MEDIA_TO_SEND: "mediaToSend",
		BASE_LAYER: "base_layer",
		SWITCHED_PROJECT: "switched_project",
		ALWAYS_SHOW_LOCATION: "always_show_location",
		
		DEFAULT_ZOOM_EXTENT: "-20037508.34,-20037508.34,20037508.34,20037508.34",
		
		TEMP_FEATURE_TABLE_UUID: 'f34f2efa-6691-210a-6763-aab91bec3806',
		
		findme: null,
		
		Init: function(createMapCallback){
			Arbiter.Map.createMap(createMapCallback);
		},
		
		getLayerSchemas: function(){
			return layerSchemas;
		},
		
		getLayerSchemasLength: function(){
			return layerSchemasLength;
		},
		
		/**
		 * key is the id of the layer in the 
		 * projects database
		 */
		putLayerSchema: function(key, schema){
			// If the key isn't in layerSchemas yet, increment the length
			if(layerSchemas[key] === null || layerSchemas[key] === undefined){
				layerSchemasLength++;
			}
			
			layerSchemas[key] = schema;
		},
		
		resetLayerSchemas: function(){
			layerSchemas = {};
			layerSchemasLength = 0;
		},
		
		/**
		 * Has the aoi been set?
		 */
		aoiHasBeenSet: function(_hasBeenSet){
			aoiHasBeenSet = _hasBeenSet;
		},
		
		hasAOIBeenSet: function(){
			return aoiHasBeenSet;
		}, 
		
		setTileUtil: function(_tileUtil){
			tileUtil = _tileUtil;
		},
		
		getTileUtil: function(){
			return tileUtil;
		},
		
		isOnline: function(online){
			if(online !== null && online !== undefined){
				isOnline = online;
			}
			
			return isOnline;
		}
	};
})();