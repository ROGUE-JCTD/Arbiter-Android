Arbiter.Layers.WFSLayer = (function(){
	
	var layerColors = ['aqua', 'yellow', 'teal',
	                   'purple', 'fuchsia', 'lime',
	                   'maroon', 'black', 'navy',
	                   'olive', 'grey', 'red',
	                   'green', 'silver', 'white' ];
	
	var createWFSProtocol = function(url, featureNamespace, geometryName,
			featureType, srid, encodedCredentials) {

		return new OpenLayers.Protocol.WFS({
			version : "1.0.0",
			url : url,
			featureNS : featureNamespace,
			geometryName : geometryName,
			featureType : featureType,
			srsName : srid,
			headers : {
				Authorization : 'Basic ' + encodedCredentials
			}
		});
	};
	
	// layer is an openlayers layer
	var onSaveSuccess = function(key, layer, encodedCredentials) {

		// update the wms layer
		Arbiter.Layers.WMSLayer.refreshWMSLayer(key);

		// update the feature data
		
		// Download the latest given the project aoi
		
			// On successful download, delete the layers feature table
		
				// On successful delete, insert the downloaded features
		
	};
	
	var getSaveStrategy = function(key, encodedCredentials) {
		var saveStrategy = new OpenLayers.Strategy.Save();

		saveStrategy.events.register("success", this, function(event) {
			onSaveSuccess(key, event.object.layer,
					encodedCredentials);
		});
		
		return saveStrategy;
	};
	
	var getLayerColor = function(){
		return layerColors[Arbiter.Map.getMap()
		    .getNumLayers() % layerColors.length];
	};
	
	var getStyleMap = function(geometryType){
		var color = getLayerColor();
		
        var defaultStyleTable = OpenLayers.Util.applyDefaults({
            fillColor: color,
            strokeColor: color
        }, OpenLayers.Feature.Vector.style["default"]);
        
        var selectStyleTable = OpenLayers.Util.applyDefaults({},
        		OpenLayers.Feature.Vector.style["select"]);
		
		if(geometryType === "Point"){
			defaultStyleTable.pointRadius = 18;
            selectStyleTable.pointRadius = 18;
		}else{
			defaultStyleTable.pointRadius = 1;
            selectStyleTable.pointRadius = 1;
		}
		
		return new OpenLayers.StyleMap({
            'default': new OpenLayers.Style(defaultStyleTable),
            'select': new OpenLayers.Style(selectStyleTable)
		});
	};
	
	return {
		create: function(key, schema) {
			var context = this;
			
			var server = Arbiter.Util.Servers.getServer(schema.getServerId());
			
			var encodedCredentials = 
				Arbiter.Util.getEncodedCredentials(
						server.getUsername(), 
						server.getPassword());
			
			var saveStrategy = getSaveStrategy(key, encodedCredentials);

			var srid = schema.getSRID();
			
			var wfsProtocol = createWFSProtocol(
					schema.getUrl(), 
					schema.getWorkspace(),
					schema.getGeometryName(), 
					schema.getFeatureType(), 
					srid, 
					encodedCredentials);

			var name = Arbiter.Layers.getLayerName(key, Arbiter.Layers.type.WFS);
			
			var options = {
				strategies : [ getSaveStrategy(key, encodedCredentials) ],
				projection : new OpenLayers.Projection(srid),
				protocol : wfsProtocol
			};
			
			var styleMap = getStyleMap(schema.getGeometryType());
			
			if(styleMap !== null && styleMap !== undefined){
				options.styleMap = styleMap;
			}
			
			return new OpenLayers.Layer.Vector(name, options);
		}
	};
})();