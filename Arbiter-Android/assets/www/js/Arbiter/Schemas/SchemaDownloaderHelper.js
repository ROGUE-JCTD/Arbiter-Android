Arbiter.SchemaDownloaderHelper = function(_layer, _onSuccess, _onFailure){
	this.layer = _layer;
	this.onSuccess = _onSuccess;
	this.onFailure = _onFailure;
	
	this.serverId = this.layer[Arbiter.LayersHelper.serverId()];
	
	var server = Arbiter.Util.Servers.getServer(this.serverId);
	
	this.url = server.getUrl();
	this.credentials = Arbiter.Util.getEncodedCredentials(
			server.getUsername(), 
			server.getPassword());
	
	this.featureType = this.layer[Arbiter.LayersHelper.featureType()];
	this.srid = this.layer[Arbiter.GeometryColumnsHelper.featureGeometrySRID()];
	
	this.describeFeatureTypeReader = new OpenLayers.Format.WFSDescribeFeatureType();
	
	this.failed = false;
	
	this.workspace = null;
	
	this.schema = null;
};

Arbiter.SchemaDownloaderHelper.prototype.onDownloadSuccess = function(){
	
	if(Arbiter.Util.funcExists(this.onSuccess)){
		this.onSuccess();
	}
};

Arbiter.SchemaDownloaderHelper.prototype.onDownloadFailure = function(){
	
	if(Arbiter.Util.funcExists(this.onFailure)){
		this.onFailure(this.featureType);
	}
};

Arbiter.SchemaDownloaderHelper.prototype.downloadSchema = function(){
	var context = this;
	
	var gotRequestBack = false;
	
	var request = new OpenLayers.Request.GET({
		url: context.url + "/wfs?service=wfs&version=1.0.0&request=DescribeFeatureType&typeName=" + context.featureType,
		headers: {
			Authorization: 'Basic ' + context.credentials
		},
		success: function(response){
			gotRequestBack = true;
			
			var results = context.describeFeatureTypeReader.read(response.responseText);
			
			// If there are no feature types, return.
			if(!results.featureTypes || !results.featureTypes.length){
				
				context.onDownloadSuccess();
				
				return;
			}
			
			try{
				context.schema = new Arbiter.Util.LayerSchema(context.url,
						results.targetNamespace, context.featureType, context.srid,
						results.featureTypes[0].properties, context.serverId);
			}catch(e){
				var msg = "Could not create schema - " + JSON.stringify(e);
				
				throw msg;
			}
			
			context.workspace = results.targetNamespace;
			
			context.saveWorkspace();
		},
		failure: function(response){
			gotRequestBack = true;
			
			context.onDownloadFailure();
		}
	});
	
	// Couldn't find a way to set timeout for an openlayers
	// request, so I did this to abort the request after
	// 15 seconds of not getting a response
	window.setTimeout(function(){
		if(!gotRequestBack){
			request.abort();
			
			context.onDownloadFailure();
		}
	}, 30000);
};

Arbiter.SchemaDownloaderHelper.prototype.saveWorkspace = function(){
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

Arbiter.SchemaDownloaderHelper.prototype.addToGeometryColumns = function(){
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

Arbiter.SchemaDownloaderHelper.prototype.createFeatureTable = function(){
	var context = this;
	
	// After adding the layer to the GeometryColumns table
	// create the feature table for the layer
	Arbiter.FeatureTableHelper.createFeatureTable(this.schema, function(){
		
		context.onDownloadSuccess();
	}, function(e){
		
		context.onDownloadFailure();
	});
};