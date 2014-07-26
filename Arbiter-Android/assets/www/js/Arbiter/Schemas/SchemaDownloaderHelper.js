(function(){
	
	Arbiter.SchemaDownloaderHelper = function(_layer, _wfsVersion, _onSuccess, _onFailure){
		this.wfsVersion = _wfsVersion;
		this.layer = _layer;
		this.onSuccess = _onSuccess;
		this.onFailure = _onFailure;
		
		this.serverId = this.layer[Arbiter.LayersHelper.serverId()];
		
		this.layerId = this.layer[Arbiter.LayersHelper.layerId()];
		
		var server = Arbiter.Util.Servers.getServer(this.serverId);
		
		this.serverType = server.getType();
		
		this.url = server.getUrl();
		this.credentials = Arbiter.Util.getEncodedCredentials(
				server.getUsername(), 
				server.getPassword());
		
		this.featureType = this.layer[Arbiter.LayersHelper.featureType()];
		this.srid = this.layer[Arbiter.GeometryColumnsHelper.featureGeometrySRID()];
		
		this.describeFeatureTypeReader = new OpenLayers.Format.WFSDescribeFeatureType();
		
		this.color = this.layer[Arbiter.LayersHelper.color()];
		
		this.failed = false;
		
		this.workspace = null;
		
		this.schema = null;
	};
	
	var prototype = Arbiter.SchemaDownloaderHelper.prototype;

	prototype.onDownloadSuccess = function(alreadyInProject){
		
		if(Arbiter.Util.funcExists(this.onSuccess)){
			this.onSuccess(alreadyInProject);
		}
	};

	prototype.onDownloadFailure = function(){
		
		if(Arbiter.Util.funcExists(this.onFailure)){
			this.onFailure(this.featureType);
		}
	};
	
	prototype.downloadSchema = function() {
		if(this.serverType === "TMS"){
			
			this.onDownloadSuccess(false);
			
			return;
		}
		
		this._downloadSchema();
	};
	
	prototype._downloadSchema = function(){
		var context = this;
		
		var gotRequestBack = false;
		
		var url = this.url.substring(0, this.url.length - 4);
		
		var options = {
			url: url + "/wfs?service=wfs&version=" + context.wfsVersion + "&request=DescribeFeatureType&typeName=" + context.featureType,
			success: function(response){
				gotRequestBack = true;
				
				var results = context.describeFeatureTypeReader.read(response.responseText);
				
				// If there are no feature types, return.
				if(!results.featureTypes || !results.featureTypes.length){
					
					context.onDownloadSuccess(false);
					
					return;
				}
				
				try{
					context.schema = new Arbiter.Util.LayerSchema(context.layerId, context.url,
							results.targetNamespace, context.featureType, context.srid,
							results.featureTypes[0].properties, context.serverId,
							context.serverType, context.color, false);
				}catch(e){
					var msg = "Could not create schema - " + JSON.stringify(e);
					
					throw msg;
				}
				
				context.workspace = results.targetNamespace;
				
				context.checkNotInProject();
			},
			failure: function(response){
				gotRequestBack = true;
				
				context.onDownloadFailure();
			}
		};
		
		if(Arbiter.Util.existsAndNotNull(context.credentials)){
			options.headers = {
				Authorization: 'Basic ' + context.credentials
			};
		}
		
		var request = new OpenLayers.Request.GET(options);
		
		window.setTimeout(function(){
			if(!gotRequestBack){
				request.abort();
				
				context.onDownloadFailure();
			}
		}, 30000);
	};

	prototype.checkNotInProject = function(){
		
		var context = this;
		
		Arbiter.GeometryColumnsHelper.getGeometryColumn(this.layer, context, function(){
			
			// Is in project so don't add this layer.
			console.log("layer is in project, so delete the layer from the project");
			
			context.deleteLayer();
		}, function(){
			// Isn't in project so continue.
			
			console.log("layer isn't in project so continue");
			
			context.saveWorkspace();
		}, function(e){
			
			console.log("Error checking to see if layer is in project");
			
			context.onDownloadFailure(e);
		});
	};
	
	prototype.deleteLayer = function(){
		
		var context = this;
		
		Arbiter.LayersHelper.deleteLayer(this.layer[Arbiter.LayersHelper.layerId()], function(){
			
			console.log("deleted layer successfully");
			
			context.onDownloadSuccess(true);
		}, function(e){
			
			console.log("Couldn't delete layer", e);
			
			context.onDownloadFailure(e);
		});
	};
	
	prototype.saveWorkspace = function(){
		var context = this;
		
		var content = {};
		
		content[Arbiter.LayersHelper.workspace()] = this.workspace;
		
		console.log("udpating the workspace!");
		
		// Update the layers workspace in the Layers table.
		Arbiter.LayersHelper.updateLayer(context.featureType, content, this, function(){
			console.log("udpated the workspace of the layer");
			
			context.addToGeometryColumns();
		}, function(e){
			
			context.onDownloadFailure();
		});
	};

	prototype.addToGeometryColumns = function(){
		var context = this;
		
		// After updating the layer workspace, 
		// add the layer to the GeometryColumns table
		Arbiter.GeometryColumnsHelper.addToGeometryColumns(this.schema, function(){
			console.log("added the table to the geometrycolumns table!");
			
			context.createFeatureTable();
		}, function(e){
			context.onDownloadFailure();
		});
	};

	prototype.createFeatureTable = function(){
		var context = this;
		
		// After adding the layer to the GeometryColumns table
		// create the feature table for the layer
		Arbiter.FeatureTableHelper.createFeatureTable(this.schema, function(){
			
			context.onDownloadSuccess(false);
		}, function(e){
			
			context.onDownloadFailure();
		});
	};
})();