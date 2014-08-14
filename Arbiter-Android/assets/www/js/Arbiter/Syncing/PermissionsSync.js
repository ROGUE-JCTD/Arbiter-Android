(function(){
	
	Arbiter.PermissionsSync = function(schemas){
		this.schemas = schemas;
		this.onSuccess = null;
		this.onFailure = null;
		this._keys = [];
		
		for(var key in schemas){
			
			this._keys.push(key);
		}
	};
	
	var prototype = Arbiter.PermissionsSync.prototype;
	
	prototype._onSyncSuccess = function(){
		
		console.log("Arbiter.PermissionsSync _onSyncSuccess");
		
		if(Arbiter.Util.existsAndNotNull(this.onSuccess)){
			this.onSuccess();
		}
	};
	
	prototype._onSyncFailure = function(e){
		
		console.log("Arbiter.PermissionsSync _onSyncFailure", e);
		
		if(Arbiter.Util.existsAndNotNull(this.onFailure)){
			this.onFailure(e);
		}
	};
	
	prototype._pop = function(){
		
		var key = this._keys.shift();
		
		if(Arbiter.Util.existsAndNotNull(key)){
			return this.schemas[key];
		}else{
			return null;
		}
	};
	
	prototype.sync = function(onSuccess, onFailure){
		
		this.onSuccess = onSuccess;
		this.onFailure = onFailure;
		
		var schema = this._pop();
		
		this._queryLayer(schema);
	};
	
	prototype._queryLayer = function(schema){
		
		if(!Arbiter.Util.existsAndNotNull(schema)){
			
			this._onSyncSuccess();
			
			return;
		}
		
		var context = this;
		
		var url = schema.getUrl();
		
		var featureType = "";
		
		var prefix = schema.getPrefix();
		
		if(Arbiter.Util.existsAndNotNull(prefix)){
			
			featureType += prefix + ":";
		}
		
		featureType += schema.getFeatureType();
		
		var server = Arbiter.Util.Servers.getServer(schema.getServerId());
		
		var credentials = Arbiter.Util.getEncodedCredentials(server.getUsername(), server.getPassword());
		
		var next = function(){
			
			var schema = context._pop();
			
			context._queryLayer(schema);
		};
		
		
		if(!schema.isEditable()){
			
			next();
			
			return;
		}
		
		var layerPermissionChecker = new Arbiter.LayerPermissionChecker(url, featureType, credentials);
		
		layerPermissionChecker.checkReadOnly(function(isReadOnly){
			
			schema.setReadOnly(isReadOnly);
			
			next();
		}, function(e){
			
			if(e === Arbiter.Error.Sync.TIMED_OUT){
				
				context._onSyncFailure(e);
			}else{
				next();
			}
		});
	};
})();