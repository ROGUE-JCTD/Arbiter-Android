Arbiter.Controls.Modify = function(_olLayer, _selectedFeature, _schema, onFeatureModified){
	var modifyController = null;
	
	var selectedFeature = _selectedFeature;
	
	var schema = _schema;
	
	var olLayer = _olLayer;
	
	var modifyLayer = null;
	
	var modified = false;
	
	var controlPanelHelper = new Arbiter.ControlPanelHelper();
	
	var olGeometryClass = null;
	
	var geometryPart = null;
	
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

	var onFeatureSelected = function(event){
		console.log("onFeatureSelected", event);
		
		var geomClsName = event.feature.geometry.CLASS_NAME;
		
		var enable = false;
		var enableCollection = false;
		
		var type = Arbiter.Geometry.getGeometryType(schema.getLayerId(), schema.getGeometryType());
		
		if(geomClsName === "OpenLayers.Geometry.Collection" 
			|| geomClsName === "OpenLayers.Geometry.MultiPoint"
			|| geomClsName === "OpenLayers.Geometry.MultiLineString"
			|| geomClsName === "OpenLayers.Geometry.MultiPolygon"){
			
			enable = true;
		}
		
		if(type === Arbiter.Geometry.type.MULTIGEOMETRY){
			enableCollection = true;
		}
		
		Arbiter.Cordova.setMultiPartBtnsEnabled(enable, enableCollection);
	};
	
	var registerEvents = function(){
		modifyLayer.events.register("featuremodified", null, featureModified);
		modifyLayer.events.register("beforefeaturemodified", null, onFeatureSelected);
	};
	
	var _attachToMap = function(){
		if(modifyController !== null){
			var map = Arbiter.Map.getMap();
			
			map.addControl(modifyController);
			
			registerEvents();
			
			modifyController.activate();
		}
	};
	
	var _detachFromMap = function(){
		if(modifyController !== null){
			var map = Arbiter.Map.getMap();
			
			modifyController.deactivate();
			
			map.removeControl(modifyController);
			
			var features = modifyLayer.features;
			
			modifyLayer.removeAllFeatures();
			
			var geometry = null;
			
			var components = [];
			
			for(var i = 0; i < features.length; i++){
				components.push(features[i].geometry);
			}
			
			if(olGeometryClass === "OpenLayers.Geometry.Collection"){
				
				geometry = new OpenLayers.Geometry.Collection(components);
			}else if(olGeometryClass === "OpenLayers.Geometry.MultiPoint"){
				geometry = new OpenLayers.Geometry.MultiPoint(components);
			}else if(olGeometryClass === "OpenLayers.Geometry.MultiLineString"){
				geometry = new OpenLayers.Geometry.MultiLineString(components);
			}else if(olGeometryClass === "OpenLayers.Geometry.MultiPolygon"){
				geometry = new OpenLayers.Geometry.MultiPolygon(components);
			}else{
				geometry = features[0].geometry;
			}
			
			selectedFeature.geometry = geometry;
			
			olLayer.addFeatures(selectedFeature);
			
			map.removeLayer(modifyLayer);
			
			modifyController.destroy();
			
			modifyController = null;
		}
	};
	
	var initModifyController = function(){
		
		var map = Arbiter.Map.getMap();
		
		modifyLayer = new OpenLayers.Layer.Vector("modifyLayer", {
			styleMap: olLayer.styleMap
		});
		
		olLayer.removeFeatures([selectedFeature]);
		
		var features = [];
		
		olGeometryClass = selectedFeature.geometry.CLASS_NAME;
		
		if(olGeometryClass === "OpenLayers.Geometry.Collection" 
			|| olGeometryClass === "OpenLayers.Geometry.MultiPoint"
			|| olGeometryClass === "OpenLayers.Geometry.MultiPolygon"
			|| olGeometryClass === "OpenLayers.Geometry.MultiLineString"){
			
			var components = selectedFeature.geometry.components;
			
			var feature = null;
			
			var component = null;
			
			for(var i = 0; i < components.length; i++){
				
				
				feature = new OpenLayers.Feature.Vector(components[i]);
				
				feature.renderIntent = "select";
				
				features.push(feature);
			}
		}else{
			selectedFeature.renderIntent = "select";
			
			features.push(selectedFeature);
		}
		
		modifyLayer.addFeatures(features);
		
		map.addLayers([modifyLayer]);
		
		modifyController = new OpenLayers.Control.ModifyFeature(modifyLayer, {
			vertexRenderIntent: "select",
			standalone: false,
			toggle: false,
			clickout: false
		});
		
		_attachToMap();
	};
	
	return {
		activate: function(){
			initModifyController();
			
			//modifyController.selectFeature(selectedFeature);
		},
		
		deactivate: function(){
			_detachFromMap();
		},
		
		endModifyMode: function(onEndModify){
			
			var context = this;
			
			controlPanelHelper.clear(function(){
				
				context.deactivate();
				
				if(Arbiter.Util.existsAndNotNull(onEndModify)){
					onEndModify();
				}
			}, function(e){
				console.log("endModifyMode error",e);
			});
		},
		
		restoreGeometry: function(wktGeometry){
			// Parse the geometry and add it back to the layer
			
			var geomFeature = Arbiter.Geometry.readWKT(wktGeometry);
			
			var srid = Arbiter.Map.getMap().projection.projCode;
			
			geomFeature.geometry.transform(new OpenLayers.Projection(schema.getSRID()),
					new OpenLayers.Projection(srid));
			
			olLayer.removeFeatures([selectedFeature]);
			
			selectedFeature.geometry = geomFeature.geometry;
			
			olLayer.addFeatures([selectedFeature]);
		},
		
		cancel: function(wktGeometry, onCancelled){
			var context = this;
			
			this.endModifyMode(function(){
				
				context.restoreGeometry(wktGeometry);
				
				if(Arbiter.Util.existsAndNotNull(onCancelled)){
					onCancelled();
				}
			});
		},
		
		removePart: function(){
			
		},
		
		removeGeometry: function(){
			
		}
	};
};