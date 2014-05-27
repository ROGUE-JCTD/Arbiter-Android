(function(){
	
	Arbiter.ControlPanelHelper = function(){
		
	};

	var prototype = Arbiter.ControlPanelHelper.prototype;
	
	prototype.ACTIVE_CONTROL = "cp_active_control";
	prototype.LAYER_ID = "cp_layer_id";
	prototype.FEATURE_ID = "cp_feature_id";
	prototype.GEOMETRY  = "cp_geometry";
	prototype.GEOMETRY_TYPE = "cp_geometry_type";

	// For CONTROLS.MODIFY;
	prototype.INDEX_CHAIN = "cp_index_chain";

	prototype.CONTROLS = {
		NONE: "0",
		SELECT: "1",
		MODIFY: "2",
		INSERT: "3"
	};

	prototype.clear = function(onSuccess, onFailure){
		
		this.set(0, 0, this.CONTROLS.NONE, 0, null, null, onSuccess, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Error clearing controlPanel - " + e);
			}
		});
	};

	prototype.set = function(featureId, layerId, control, geometry, geometryType, indexChain, onSuccess, onFailure){
		var context = this;
		
		context.setActiveControl(control, function(){
			
			context.setFeatureId(featureId, function(){
				
				context.setLayerId(layerId, function(){
					
					context.setGeometry(geometry, function(){
						
						context.setGeometryType(geometryType, function(){
							
							context.setIndexChain(indexChain, function(){
								
								context.mode = control;
								
								if(Arbiter.Util.funcExists(onSuccess)){
									onSuccess();
								}
							}, function(e){
								if(Arbiter.Util.funcExists(onFailure)){
									onFailure("ControlPanelHelper.js" + e);
								}
							});
						}, function(e){
							if(Arbiter.Util.funcExists(onFailure)){
								onFailure("ControlPanelHelper.js" + e);
							}
						});
					}, function(e){
						if(Arbiter.Util.funcExists(onFailure)){
							onFailure("ControlPanelHelper.js" + e);
						}
					});
				}, function(e){
					if(Arbiter.Util.funcExists(onFailure)){
						onFailure("ControlPanelHelper.js" + e);
					}
				});
			}, function(e){
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure("ControlPanelHelper.js" + e);
				}
			});
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("ControlPanelHelper.js "  + e);
			}
		});
	};

	prototype.setActiveControl = function(control, onSuccess, onFailure){
		var context = this;
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.put(projectDb, context.ACTIVE_CONTROL, control, context, function(){
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess();
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Error setting " + context.ACTIVE_CONTROL + " - " + e);
			}
		});
		
	};

	prototype.setLayerId = function(layerId, onSuccess, onFailure){
		var context = this;
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.put(projectDb, context.LAYER_ID, layerId, context, function(){
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess();
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Error setting " + context.LAYER_ID + " - " + e);
			}
		});
	};

	prototype.setFeatureId = function(featureId, onSuccess, onFailure){
		var context = this;
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.put(projectDb, context.FEATURE_ID, featureId, context, function(){
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess();
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Error setting " + context.FEATURE_ID + " - " + e);
			}
		});
	};

	prototype.setGeometry = function(geometry, onSuccess, onFailure){
		var context = this;
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.put(projectDb, context.GEOMETRY, geometry, context, function(){
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess();
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Error setting " + context.GEOMETRY + " - " + e);
			}
		});
	};

	prototype.setGeometryType = function(geometryType, onSuccess, onFailure){
		var context = this;
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.put(projectDb, context.GEOMETRY_TYPE, geometryType, context, function(){
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess();
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Error setting " + context.GEOMETRY_TYPE + " - " + e);
			}
		});
	};
	
	prototype.setIndexChain = function(indexChain, onSuccess, onFailure){
		var context = this;
		
		console.log("setIndexChain: indexChain = " + indexChain);
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.put(projectDb, context.INDEX_CHAIN, indexChain, context, function(){
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess();
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Error setting " + context.INDEX_CHAIN + " - " + e);
			}
		})
	};

	prototype.getActiveControl = function(onSuccess, onFailure){
		var context = this;
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.get(projectDb, context.ACTIVE_CONTROL, context, function(activeControl){
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess(activeControl);
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure(e);
			}
		});
	};

	prototype.getLayerId = function(onSuccess, onFailure){
		var context = this;
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.get(projectDb, context.LAYER_ID, context, function(layerId){
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess(layerId);
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure(e);
			}
		});
	};

	prototype.getFeatureId = function(onSuccess, onFailure){
		var context = this;
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.get(projectDb, context.FEATURE_ID, context, function(featureId){
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess(featureId);
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure(e);
			}
		});
	};

	prototype.getGeometry = function(onSuccess, onFailure){
		var context = this;
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.get(projectDb, context.GEOMETRY, context, function(geometry){
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess(geometry);
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure(e);
			}
		});
	};

	prototype.getGeometryType = function(onSuccess, onFailure){
		var context = this;
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.get(projectDb, context.GEOMETRY_TYPE, context, function(geometryType){
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess(geometryType);
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure(e);
			}
		});
	};
	
	prototype.getIndexChain = function(onSuccess, onFailure){
		var context = this;
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.get(projectDb, context.INDEX_CHAIN, context, function(indexChain){
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess(indexChain);
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure(e);
			}
		});
	};
})();