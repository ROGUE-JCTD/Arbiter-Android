Arbiter.Layers.WFSLayer = (function(){
	
	var createWFSProtocol = function(url, featureNamespace, geometryName,
			featureType, srid, encodedCredentials) {

		return new OpenLayers.Protocol.WFS({
			version : "1.0.0",
			url : url + "/wfs",
			featureNS : featureNamespace,
			geometryName : geometryName,
			featureType : featureType,
			srsName : srid,
			headers : {
				Authorization : 'Basic ' + encodedCredentials
			}
		});
	};
	
	var getSaveStrategy = function(key, encodedCredentials) {
		var saveStrategy = new OpenLayers.Strategy.Save();
		
		saveStrategy.events.register("start", this, function(event) {
			//Arbiter.Layers.SyncHelper.onSaveSuccess(key, event.object.layer,
			//		encodedCredentials);
			
			var layer = saveStrategy.layer;
			
			if(layer !== null && layer !== undefined){
				var metadata = layer.metadata;
				
				if(metadata !== null && metadata !== undefined 
						&& Arbiter.Util.funcExists(metadata["onSaveStart"])){
					
					var onSaveStart = metadata["onSaveStart"];
					
					onSaveStart(event);
				}
			}
		});

		saveStrategy.events.register("success", this, function(event) {
			
			var layer = saveStrategy.layer;
			
			if(layer !== null && layer !== undefined){
				var metadata = layer.metadata;
				
				if(metadata !== null && metadata !== undefined 
						&& Arbiter.Util.funcExists(metadata["onSaveSuccess"])){
					
					var onSaveSuccess = metadata["onSaveSuccess"];
					
					onSaveSuccess();
				}
			}
		});
		
		saveStrategy.events.register("fail", this, function(event){
			console.log("save failed - ", event);
			
			var layer = saveStrategy.layer;
			
			if(layer !== null && layer !== undefined){
				var metadata = layer.metadata;
				
				if(metadata !== null && metadata !== undefined 
						&& Arbiter.Util.funcExists(metadata["onSaveFailure"])){
					
					var onSaveFailure = metadata["onSaveFailure"];
					
					onSaveFailure(event);
				}
			}
		});
		
		return saveStrategy;
	};
	
	var getStyleMap = function(geometryType, color){
        var defaultStyleTable = OpenLayers.Util.applyDefaults({
            fillColor: color,
            strokeColor: color
        }, OpenLayers.Feature.Vector.style["default"]);
        
        var selectStyleTable = OpenLayers.Util.applyDefaults({},
        		OpenLayers.Feature.Vector.style["select"]);
		
	/*	if(geometryType === "Point" || 
				geometryType === "MultiPoint" ||
				geometryType === "Geometry" ||
				geometryType === "MultiGeometry"){
			*/
			defaultStyleTable.pointRadius = 18;
            selectStyleTable.pointRadius = 18;
		/*}else{
			defaultStyleTable.pointRadius = 1;
            selectStyleTable.pointRadius = 1;
		}*/
		
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
			
			var styleMap = getStyleMap(schema.getGeometryType(), schema.getColor());
			
			if(styleMap !== null && styleMap !== undefined){
				options.styleMap = styleMap;
			}
			
			return new OpenLayers.Layer.Vector(name, options);
		}
	};
})();