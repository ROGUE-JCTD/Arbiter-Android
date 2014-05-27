Arbiter.Controls.Modify = function(_map, _olLayer, _featureOfInterest, _schema){
	
	var map = _map;
	
	var selectController = null;
	
	var modifyController = null;
	
	var featureOfInterest = _featureOfInterest;
	
	var selectedFeature = null;
	
	var schema = _schema;
	
	var olLayer = _olLayer;
	
	var selectLayer = null;
	
	var modifyLayer = null;
	
	var modified = false;
	
	var controlPanelHelper = new Arbiter.ControlPanelHelper();
	
	var olGeometryClass = null;
	
	var geometryPart = null;
	
	var geometryExpander = null;
	
	var geometryAdder = null;
	
	var addingGeometryPart = false;
	
	var saveControlPanelInfo = function(geometryPart, geometryTypeName){
			
		var featureId = null;
		
		if(Arbiter.Util.existsAndNotNull(featureOfInterest.metadata)){
			featureId = featureOfInterest.metadata[Arbiter.FeatureTableHelper.ID];
		}
		
		var layerId = schema.getLayerId();
		
		var geometry = geometryExpander.compress();
		
		var tempFeature = new OpenLayers.Feature.Vector(geometry);
		
		var wktGeometry = Arbiter.Geometry.getNativeWKT(tempFeature, layerId);
		
		var indexChain = geometryPart.getIndexChain();
		
		console.log("saveControlPanelInfo: \nwktGeometry = " + wktGeometry + "\nindexChain = " + indexChain);
		
		controlPanelHelper.set(featureId, layerId, 
				controlPanelHelper.CONTROLS.MODIFY, 
				wktGeometry, geometryTypeName, indexChain, function(){
			
			console.log("successfully updated geometry");
		}, function(e){
			console.log("error updating modified geometry", e);
		});
	};
	
	var featureModified =  function(event){
		console.log("onFeatureModified", event);
		
		saveControlPanelInfo(event.feature.metadata.part);
	};

	var onFeatureSelected = function(feature){
		console.log("onFeatureSelected", feature);
		
		selectedFeature = feature;
		
		selectController.deactivate();
		
		var siblings = feature.metadata.part.getSiblings();
		
		siblings.push(feature);
		
		selectLayer.removeFeatures(siblings);
		
		for(var i = 0; i < siblings.length; i++){
			siblings[i].renderIntent = "select";
		}
		
		modifyLayer.addFeatures(siblings);
		
		modifyController.activate();
		
		modifyController.selectFeature(selectedFeature);
	};
	
	var onBeforeFeatureModified = function(event){
		console.log("onBeforeFeatureModified", event);
		
		var geomClsName = event.feature.geometry.CLASS_NAME;
		
		var enable = false;
		var enableCollection = false;
		
		var type = Arbiter.Geometry.getGeometryType(schema.getLayerId(), schema.getGeometryType());
		
		var geometryTypeName = Arbiter.Geometry.getGeometryName(type);
		
		var feature = event.feature;
		
		if(Arbiter.Util.existsAndNotNull(feature.metadata)){
			
			var part = feature.metadata.part;
			
			if(Arbiter.Util.existsAndNotNull(part) 
					&& Arbiter.Util.existsAndNotNull(part.parent)
					&& Arbiter.Util.existsAndNotNull(part.parent.type)
					&& (part.parent.type === "OpenLayers.Geometry.MultiPoint" 
						|| part.parent.type === "OpenLayers.Geometry.MultiLineString"
						|| part.parent.type === "OpenLayers.Geometry.MultiPolygon")){
				
				enable = true;
			}
		}
		
		if(type === Arbiter.Geometry.type.MULTIGEOMETRY){
			enableCollection = true;
		}
		
		geometryPart = part;
		
		Arbiter.Cordova.setMultiPartBtnsEnabled(enable, enableCollection);
		
		saveControlPanelInfo(event.feature.metadata.part, geometryTypeName);
	};
	
	var registerEvents = function(){
		modifyLayer.events.register("featuremodified", null, function(event){ 
			try{
			
				featureModified(event);
			}catch(e){
				console.log("featureModified", e.stack);
			}
		});
		modifyLayer.events.register("beforefeaturemodified", null, function(event){
			try{
				onBeforeFeatureModified(event);
			}catch(e){
				console.log("beforeFeatureModified", e.stack);
			}
		});
	};
	
	var _attachToMap = function(){
		if(modifyController !== null){
			
			map.addControl(modifyController);
			
			registerEvents();
			
			modifyController.activate();
		}
	};
	
	var initSelectController = function(){
	
		selectLayer = new OpenLayers.Layer.Vector("selectLayer", {
			styleMap: olLayer.styleMap
		});
		
		map.addLayers([selectLayer]);
		
		// Make sure features get rendered as selected
		selectLayer.events.register("featureadded", null, function(event){
			event.feature.renderIntent = "select";
			selectLayer.drawFeature(event.feature);
		});
		
		olLayer.removeFeatures([featureOfInterest]);
		
		geometryExpander = new Arbiter.GeometryExpander();
		
		geometryExpander.expand(featureOfInterest.geometry);
		
		selectLayer.addFeatures(geometryExpander.features);
		
		selectController = new OpenLayers.Control.SelectFeature(selectLayer, {
			clickout: false,
			toggle: true,
			onSelect: function(feature){
				onFeatureSelected(feature);
			},
			onUnselect: function(feature){
				console.log("Modify.js onUnselect");
			}
		});
		
		map.addControl(selectController);
		
		selectController.activate();
	};
	
	var initModifyController = function(){
		
		modifyLayer = new OpenLayers.Layer.Vector("modifyLayer", {
			styleMap: olLayer.styleMap
		});
		
		map.addLayers([modifyLayer]);
		
		modifyController = new OpenLayers.Control.ModifyFeature(modifyLayer, {
			vertexRenderIntent: "select",
			standalone: false,
			toggle: false,
			clickout: false
		});
		
		map.addControl(modifyController);
	};
	
	return {
		activate: function(){
			initSelectController();
			initModifyController();
			registerEvents();
		},
		
		deactivate: function(){
			console.log("deactivate: ");
			if(modifyController.active){
				console.log("deactivate modifyController");
				modifyController.deactivate();
			}else{
				console.log("deactivate selectController");
				selectController.deactivate();
			}
			
			map.removeControl(modifyController);
			
			map.removeControl(selectController);
			
			modifyLayer.removeAllFeatures();
			
			selectLayer.removeAllFeatures();
			
			map.removeLayer(selectLayer);
			
			map.removeLayer(modifyLayer);
			
			selectController.destroy();
			
			modifyController.destroy();
			
			selectController = null;
			
			modifyController = null;

			var geometry = geometryExpander.compress();
			
			featureOfInterest.geometry = geometry;
			
			olLayer.addFeatures(featureOfInterest);
		},
		
		validEdit: function(){
			
			var featuresLength = 0;
			
			if(Arbiter.Util.existsAndNotNull(selectLayer)){
				featuresLength += selectLayer.features.length;
			}

			if(Arbiter.Util.existsAndNotNull(modifyLayer)){
				featuresLength += modifyLayer.features.length;
			}
			
			var type = Arbiter.Geometry.getGeometryType(schema.getLayerId(), schema.getGeometryType());
			var types = Arbiter.Geometry.type;
			
			return featuresLength > 0 || (type !== types.MULTIGEOMETRY
					&& type !== types.MULTIPOINT 
					&& type !== types.MULTILINE
					&& type !== types.MULTIPOLYGON);
		},
		
		done: function(onDone){
			
			var context = this;
			
			if(Arbiter.Util.existsAndNotNull(geometryAdder)){
				
				try{
					geometryAdder.finish();
					
					geometryAdder = null;
				}catch(e){
					console.log(e.stack);
				}
			}
			
			var features = modifyLayer.features;
			
			modifyLayer.removeAllFeatures();
			
			selectLayer.addFeatures(features);
			
			controlPanelHelper.clear(function(){
				
				context.deactivate();
				
				if(Arbiter.Util.existsAndNotNull(onDone)){
					onDone();
				}
			}, function(e){
				console.log("endModifyMode error",e);
			});
		},
		
		restoreGeometry: function(wktGeometry){
			// Parse the geometry and add it back to the layer
			
			var geomFeature = Arbiter.Geometry.readWKT(wktGeometry);
			
			var srid = map.projection.projCode;
			
			geomFeature.geometry.transform(new OpenLayers.Projection(schema.getSRID()),
					new OpenLayers.Projection(srid));
			
			olLayer.removeFeatures([featureOfInterest]);
			
			featureOfInterest.geometry = geomFeature.geometry;
			
			olLayer.addFeatures([featureOfInterest]);
		},
		
		cancel: function(wktGeometry, onCancelled){
			var context = this;
			
			this.done(function(){
				
				geometryAdder = null;
				
				context.restoreGeometry(wktGeometry);
				
				if(Arbiter.Util.existsAndNotNull(onCancelled)){
					onCancelled();
				}
			});
		},
		
		beginAddPart: function(){
			
			if(addingGeometryPart){
				return;
			}
			
			addingGeometryPart = true;
			
			var geometryType = null;
			
			if(geometryPart.type === "OpenLayers.Geometry.Point"){
				geometryType = Arbiter.Geometry.type.POINT;
			}else if(geometryPart.type === "OpenLayers.Geometry.LineString"){
				geometryType = Arbiter.Geometry.type.LINE;
			}else if(geometryPart.type === "OpenLayers.Geometry.Polygon"){
				geometryType = Arbiter.Geometry.type.POLYGON;
			}else{
				throw "Modify.js beginAddPart invalid type: " + geometryType;
			}
			
			modifyController.deactivate();
			
			geometryAdder = new Arbiter.GeometryAdder(map, modifyLayer, geometryType, function(feature){
				
				geometryPart.addPart(geometryPart.type, feature, geometryPart.parent);
				
				modifyController.activate();
				
				addingGeometryPart = false;
			});
		},
		
		isAddingPart: function(){
			
			return addingGeometryPart;
		},
		
		beginAddGeometry: function(_geometryType){
			
			console.log("beginAddGeometry addingGeometryPart = " + addingGeometryPart);
			
			if(addingGeometryPart){
				return;
			}
			
			addingGeometryPart = true;
			
			var geometryType = Arbiter.Geometry.getGeometryType(null, _geometryType);
			
			modifyController.deactivate();
			
			geometryAdder = new Arbiter.GeometryAdder(map, modifyLayer, geometryType, function(feature){
				
				if(Arbiter.Util.existsAndNotNull(geometryPart)){
					geometryPart.addUncle(geometryType, feature);
				}else{
					geometryExpander.addToCollection(geometryType, feature);
				}
				
				modifyController.activate();
				
				addingGeometryPart = false;
			});
		},
		
		removePart: function(){
			
			if(addingGeometryPart){
				return;
			}
			
			if(Arbiter.Util.existsAndNotNull(geometryPart)){
				
				modifyController.deactivate();
				
				// Remove the geometry part from the geometry
				// expansion record.
				geometryPart.remove(function(feature){
					
					// Remove the geometry part from the layer
					modifyLayer.removeFeatures([feature]);
				});
				
				geometryPart = null;
				
				modifyController.activate();
				
				Arbiter.Cordova.hidePartButtons();
			}
		},
		
		removeGeometry: function(){
			
			if(addingGeometryPart){
				return;
			}
			
			if(Arbiter.Util.existsAndNotNull(geometryPart)){
				
				modifyController.deactivate();
				
				// Remove the geometry part from the geometry
				// expansion record.
				geometryPart.removeFromCollection(function(feature){
					
					modifyLayer.removeFeatures([feature]);
				});
				
				geometryPart = null;
				
				Arbiter.Cordova.hidePartButtons();
				
				modifyController.activate();
			}
		},
		
		getGeometryExpander: function(){
			return geometryExpander;
		},
		
		selectGeometryPartByIndexChain: function(indexChain){
			
			var indices = indexChain.split(',');
			
			var next = geometryExpander.record;
			
			for(var i = 0; i < indices.length; i++){
				
				next = next.children[indices[i]];
			}
			
			onFeatureSelected(next.feature);
		}
	};
};