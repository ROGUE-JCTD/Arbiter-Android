Arbiter.Loaders.FeaturesLoader = (function(){
	var WGS84_Google_Mercator = "EPSG:900913";
	
	var wktFormatter = new OpenLayers.Format.WKT();
	
	var controlPanelHelper = new Arbiter.ControlPanelHelper();
	
	var addMetadata = function(dbFeature, olFeature, partOfMulti){
		if(olFeature.metadata === null 
				|| olFeature.metadata === undefined){
			
			olFeature.metadata = {};
		}
		
		olFeature.metadata[Arbiter.FeatureTableHelper.ID] = 
			dbFeature[Arbiter.FeatureTableHelper.ID]; 
		
		olFeature.metadata[Arbiter.FeatureTableHelper.MODIFIED_STATE] =
			dbFeature[Arbiter.FeatureTableHelper.MODIFIED_STATE];
		
		olFeature.metadata[Arbiter.FeatureTableHelper.SYNC_STATE] =
			dbFeature[Arbiter.FeatureTableHelper.SYNC_STATE];
		
		olFeature.metadata[Arbiter.FeatureTableHelper.PART_OF_MULTI] = partOfMulti;
	};
	
	var addAttributes = function(schema, dbFeature, olFeature){
		olFeature.attributes = {};
		
		var attributes = schema.getAttributes();
		
		var attributeName = null;
		var attributeType = null;
		var attributeValue = null;
		
		for(var i = 0; i < attributes.length; i++){
			attributeName = attributes[i].getName();
			attributeType = attributes[i].getType();
			attributeValue = dbFeature[attributeName];
			
			if(attributeName === Arbiter.FeatureTableHelper.FID){
				
				olFeature[attributeName] 
					= attributeValue;
				
			}else if(attributeName !== Arbiter.FeatureTableHelper.SYNC_STATE
					&& attributeName !== Arbiter.FeatureTableHelper.MODIFIED_STATE){
				
				if(attributeValue === "" 
						|| attributeValue === null 
						|| attributeValue === undefined){
					
					olFeature.attributes[attributeName] = null;
				}else{
					
					if((attributeValue === "[]") && (attributeName === Arbiter.FeatureTableHelper.FOTOS 
							|| attributeName === Arbiter.FeatureTableHelper.PHOTOS)){
						
						olFeature.attributes[attributeName] = null;
					}else{
						
						olFeature.attributes[attributeName] = 
							attributeValue;
					}
				}
			}
		}
	};
	
	var setState = function(olFeature){
		
		var syncState = olFeature.metadata[Arbiter.FeatureTableHelper.SYNC_STATE];
		
		// If the feature is already synced, don't mark the feature for uploading
		if(syncState === Arbiter.FeatureTableHelper.SYNC_STATES.SYNCED){
			return;
		}
		
		var state = olFeature.metadata[Arbiter.FeatureTableHelper.MODIFIED_STATE];
		
		if(state === Arbiter.FeatureTableHelper.MODIFIED_STATES.DELETED){
			olFeature.state = OpenLayers.State.DELETE;
		}else if(state === Arbiter.FeatureTableHelper.MODIFIED_STATES.INSERTED){
			olFeature.state = OpenLayers.State.INSERT;
		}else if(state === Arbiter.FeatureTableHelper.MODIFIED_STATES.MODIFIED){
			olFeature.state = OpenLayers.State.UPDATE;
		}
	};
	
	var setSelectedState = function(olFeature, activeControl,
			layerId, featureId, geometry, indexChain){
		
		var olFeatureId = olFeature.metadata[Arbiter.FeatureTableHelper.ID];
		
		var olLayerId = Arbiter.Util.getLayerId(olFeature.layer);
		
		if((layerId == olLayerId) && (featureId == olFeatureId)){
			
			if(activeControl === controlPanelHelper.CONTROLS.SELECT){
				
				olFeature.metadata["modified"] = true;
				
				Arbiter.Controls.ControlPanel.select(olFeature);
			}else if(activeControl === controlPanelHelper.CONTROLS.MODIFY){
				
				Arbiter.Controls.ControlPanel.setSelectedFeature(olFeature);
				
				Arbiter.Controls.ControlPanel.moveSelectedFeature(geometry);
				
				Arbiter.Controls.ControlPanel.enterModifyMode(olFeature, function(){
					if(Arbiter.Util.existsAndNotNull(indexChain)){
						Arbiter.Controls.ControlPanel.selectGeometryPartByIndexChain(indexChain);
					}
				});
			}else if(activeControl === controlPanelHelper.CONTROLS.INSERT){
				
			}
		}
	};
	
	var addComponents = function(collection, features, geometryType, srid){
		
		var mapProj = Arbiter.Map.getMap().projection.projCode;
		
		var add = null;
		var olFeature = null;
		
		if(geometryType === Arbiter.Geometry.type.MULTIPOINT){
			add = function(collection, feature){
				collection.addPoint(feature.geometry);
			};
		}else{
			add = function(collection, feature){
				collection.addComponents(feature.geometry);
			};
		}
		
		for(var i = 0; i < features.length; i++){
			olFeature = features[i];
			
			if(srid !== mapProj){
				olFeature.geometry.transform
					(new OpenLayers.Projection(srid), 
							new OpenLayers.Projection(mapProj));
			}
			
			add(collection, olFeature);
		}
		
		return collection;
	};
	
	var readFeature = function(schema, layerId, wkt){
		var feature = wktFormatter.read(wkt);
		
		var geometryType = Arbiter.Geometry.getGeometryType(layerId, schema.getGeometryType());
		
		var srid = schema.getSRID();
		
		if(geometryType === Arbiter.Geometry.type.MULTIGEOMETRY){
			
			var collection = new OpenLayers.Geometry.Collection();
			
			if(feature.constructor != Array){
				feature = [feature];
			}
			
			collection = addComponents(collection, feature, geometryType, srid);
			
			feature = feature[0];
			
			feature.geometry = collection;
		}else{
			
			var mapProj = Arbiter.Map.getMap().projection.projCode;
			
			if(srid !== mapProj){
				feature.geometry.transform
					(new OpenLayers.Projection(srid), 
						new OpenLayers.Projection(mapProj));
			}
		}
		
		return feature;
	};
	
	var processFeature = function(schema, dbFeature, olLayer,
			activeControl, layerId, featureId, geometry, indexChain){
		
		var wkt = dbFeature[schema.getGeometryName()];
		
		var partOfMulti = false;
		
		if(wkt.substring(0, 5).indexOf("Multi") >= 0){
			partOfMulti = true;
		}
		
		var feature = readFeature(schema, layerId, wkt);
		
		addAttributes(schema, dbFeature, feature);
		
		addMetadata(dbFeature, feature, partOfMulti);
		
		setState(feature);
		
		olLayer.addFeatures([feature]);
		
		setSelectedState(feature, activeControl,
				layerId, featureId, geometry, indexChain);
	};
	
	var getControlPanelMode = function(onSuccess, onFailure){
		
		controlPanelHelper.getActiveControl(function(activeControl){
			
			controlPanelHelper.getLayerId(function(layerId){
				
				controlPanelHelper.getFeatureId(function(featureId){
					
					controlPanelHelper.getGeometry(function(geometry){
						
						controlPanelHelper.getIndexChain(function(indexChain){
							if(Arbiter.Util.funcExists(onSuccess)){
								onSuccess(activeControl, layerId, featureId, geometry, indexChain);
							}
						}, function(e){
							console.log("error getting indexChain", e);
							
							if(Arbiter.Util.funcExists(onFailure)){
								onFailure(e);
							}
						});
					}, function(e){
						
						console.log("error getting geometry", e);
						
						if(Arbiter.Util.funcExists(onFailure)){
							onFailure(e);
						}
					});
				}, function(e){
					console.log("error getting featureId", e);
					
					if(Arbiter.Util.funcExists(onFailure)){
						onFailure(e);
					}
				});
			}, function(e){
				
				console.log("error getting layerId", e);
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure(e);
				}
			});
		}, function(e){
			
			console.log("error getting activeControl", e);
			
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure(e);
			}
		});
	};
	
	return {
		loadFeatures: function(schema, olLayer, onSuccess, onFailure){
			
			var success = function(activeControl, layerId, featureId, geometry, indexChain){
				
				console.log("loadFeatures success(): activeControl = " 
						+ activeControl + ", layerId = " 
						+ layerId + ", featureId = " 
						+ featureId + ", geometry = " 
						+ geometry + ", indexChain = " + indexChain);
				
				// If the map refreshed during modify mode, on an uninserted feature
				if(activeControl == controlPanelHelper.CONTROLS.MODIFY 
						&& layerId == schema.getLayerId() 
						&& (!Arbiter.Util.existsAndNotNull(featureId) 
								|| featureId === "null" 
								|| featureId === "undefined")){
					
					try{
						
						var feature = readFeature(schema, layerId, geometry);
						
						olLayer.addFeatures([feature]);
						
						Arbiter.Controls.ControlPanel.setSelectedFeature(feature);
						
						Arbiter.Controls.ControlPanel.enterModifyMode(feature, function(){
							if(Arbiter.Util.existsAndNotNull(indexChain)){
								Arbiter.Controls.ControlPanel.selectGeometryPartByIndexChain(indexChain);
							}
							
							if(Arbiter.Util.existsAndNotNull(onSuccess)){
								onSuccess();
							}
						});
						
					}catch(e){
						console.log("error reading feature: " + e.stack);
					}
				}else{
					
					if(Arbiter.Util.existsAndNotNull(onSuccess)){
						onSuccess();
					}
				}
			};
			
			getControlPanelMode(function(activeControl, layerId, featureId, geometry, indexChain){
				
				Arbiter.FeatureTableHelper.loadFeatures(schema, this, 
						function(feature, currentFeatureIndex, featureCount){
					
					try{
						if(feature !== null){
							processFeature(schema, feature, olLayer,
									activeControl, layerId,
									featureId, geometry, indexChain);
						}
					} catch (e) {
						console.log("error loading feature", e);
						
						if(Arbiter.Util.funcExists(onFailure)){
							onFailure(e);
						}
					}
					
					if(featureCount === 0 || (currentFeatureIndex === (featureCount - 1))){
						success(activeControl, layerId, featureId, geometry, indexChain);
					}
						
				}, onFailure);
			}, function(e){
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure(e);
				}
			});
		}
	};
})();