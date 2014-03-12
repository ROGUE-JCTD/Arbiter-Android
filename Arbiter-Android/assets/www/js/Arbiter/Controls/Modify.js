Arbiter.Controls.Modify = function(_map, _olLayer, _featureOfInterest, _schema, onFeatureModified){
	
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
	
	var featureModified =  function(event){
		console.log("onFeatureModified", event);
		if(Arbiter.Util.funcExists(onFeatureModified)){
			onFeatureModified(event.feature);
		}
		
		if(!modified){
			modified = true;
			
			Arbiter.Cordova.enableDoneEditingBtn();
		}
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
	};
	
	var registerEvents = function(){
		modifyLayer.events.register("featuremodified", null, featureModified);
		modifyLayer.events.register("beforefeaturemodified", null, onBeforeFeatureModified);
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
		
		olLayer.removeFeatures([featureOfInterest]);
		
		geometryExpander = new Arbiter.GeometryExpander();
		
		geometryExpander.expand(featureOfInterest.geometry);
		
		for(var i = 0; i < geometryExpander.features; i++){
			geometryExpander.features[i].renderIntent = "select";
		}
		
		selectLayer.addFeatures(geometryExpander.features);
		
		map.addLayers([selectLayer]);
		
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
		
		done: function(onDone, cancel){
			
			var context = this;
			
			if(Arbiter.Util.existsAndNotNull(geometryAdder)){
				geometryAdder.finish();
				
				geometryAdder = null;
			}
			
			var features = modifyLayer.features;
			
			modifyLayer.removeAllFeatures();
			
			selectLayer.addFeatures(features);
			
			if(selectLayer.features.length > 0 || cancel){
				controlPanelHelper.clear(function(){
					
					context.deactivate();
					
					if(Arbiter.Util.existsAndNotNull(onDone)){
						onDone();
					}
				}, function(e){
					console.log("endModifyMode error",e);
				});
			}else{
				
				Arbiter.Cordova.notifyUserToAddGeometry();
			}
		
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
			}, true);
		},
		
		beginAddPart: function(){
			
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
			});
		},
		
		beginAddGeometry: function(_geometryType){
			
			var geometryType = Arbiter.Geometry.getGeometryType(null, _geometryType);
			
			modifyController.deactivate();
			
			geometryAdder = new Arbiter.GeometryAdder(map, modifyLayer, geometryType, function(feature){
				
				geometryPart.addUncle(geometryType, feature);
				
				modifyController.activate();
			});
		},
		
		removePart: function(){
			
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
				
				Arbiter.Cordova.enableDoneEditingBtn();
			}
		},
		
		removeGeometry: function(){
			
			if(Arbiter.Util.existsAndNotNull(geometryPart)){
				
				modifyController.deactivate();
				
				// Remove the geometry part from the geometry
				// expansion record.
				geometryPart.removeFromCollection(function(feature){
					
					modifyLayer.removeFeatures([feature]);
				});
				
				geometryPart = null;
				
				modifyController.activate();
				
				Arbiter.Cordova.enableDoneEditingBtn();
			}
		},
		
		getGeometryExpander: function(){
			return geometryExpander;
		}
	};
};