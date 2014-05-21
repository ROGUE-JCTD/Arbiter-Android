Arbiter.ControlPanelHelper = function(){
	
};

Arbiter.ControlPanelHelper.prototype.ACTIVE_CONTROL = "cp_active_control";
Arbiter.ControlPanelHelper.prototype.LAYER_ID = "cp_layer_id";
Arbiter.ControlPanelHelper.prototype.FEATURE_ID = "cp_feature_id";
Arbiter.ControlPanelHelper.prototype.GEOMETRY  = "cp_geometry";

// For CONTROLS.MODIFY;
Arbiter.ControlPanelHelper.prototype.INDEX_CHAIN = "cp_index_chain";

Arbiter.ControlPanelHelper.prototype.CONTROLS = {
	NONE: "0",
	SELECT: "1",
	MODIFY: "2",
	INSERT: "3"
};

Arbiter.ControlPanelHelper.prototype.clear = function(onSuccess, onFailure){
	
	this.set(0, 0, this.CONTROLS.NONE, 0, null, onSuccess, function(e){
		if(Arbiter.Util.funcExists(onFailure)){
			onFailure("Error clearing controlPanel - " + e);
		}
	});
};

Arbiter.ControlPanelHelper.prototype.set = function(featureId, layerId, control, geometry, indexChain, onSuccess, onFailure){
	var context = this;
	
	context.setActiveControl(control, function(){
		
		context.setFeatureId(featureId, function(){
			
			context.setLayerId(layerId, function(){
				
				context.setGeometry(geometry, function(){
					
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
			onFailure("ControlPanelHelper.js "  + e);
		}
	});
};

Arbiter.ControlPanelHelper.prototype.setActiveControl = function(control, onSuccess, onFailure){
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

Arbiter.ControlPanelHelper.prototype.setLayerId = function(layerId, onSuccess, onFailure){
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

Arbiter.ControlPanelHelper.prototype.setFeatureId = function(featureId, onSuccess, onFailure){
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

Arbiter.ControlPanelHelper.prototype.setGeometry = function(geometry, onSuccess, onFailure){
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

Arbiter.ControlPanelHelper.prototype.setIndexChain = function(indexChain, onSuccess, onFailure){
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

Arbiter.ControlPanelHelper.prototype.getActiveControl = function(onSuccess, onFailure){
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

Arbiter.ControlPanelHelper.prototype.getLayerId = function(onSuccess, onFailure){
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

Arbiter.ControlPanelHelper.prototype.getFeatureId = function(onSuccess, onFailure){
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

Arbiter.ControlPanelHelper.prototype.getGeometry = function(onSuccess, onFailure){
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

Arbiter.ControlPanelHelper.prototype.getIndexChain = function(onSuccess, onFailure){
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