(function(){
	
	Arbiter.SchemaDir = function(fileSystem, serverId, featureType){
		this.fileSystem = fileSystem;
		this.serverId = serverId;
		this.featureType = featureType;
		this.schemaDirPath = this._getSchemaFileDir(this.serverId, this.featureType);
	};
	
	var prototype = Arbiter.SchemaDir.prototype;
	
	prototype._getRoot = function(){
		return Arbiter.FileSystem.ROOT_LEVEL 
			+ Arbiter.FileSystem.fileSeparator 
			+ Arbiter.FileSystem.SCHEMA_FILES_ROOT;
	};
	
	// Return the path up to the workspace
	prototype._getSchemaFileDir = function(serverId, featureType){
		
		if(!Arbiter.Util.existsAndNotNull(serverId)){
			throw "Expecting servers id";
		}
		
		if(!Arbiter.Util.existsAndNotNull(featureType)){
			throw "Expecting featureType";
		}
		
		var path = this._getRoot() + Arbiter.FileSystem.fileSeparator + serverId;
		
		if(featureType.indexOf(":") > -1){
			path += Arbiter.FileSystem.fileSeparator + featureType.split(":")[0];
		}
		
		return path;
	};
	
	prototype.getDir = function(onSuccess, onFailure){
		
		console.log("schemaDirPath: " + this.schemaDirPath);
		
		var schemaDir = new Arbiter.Directory(this.fileSystem, this.schemaDirPath);
		
		var options = {
			create: true,
			exclusive: false
		};
		
		schemaDir.getDirectory(options, function(){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess(schemaDir);
			}
		}, function(e){
			
			if(Arbiter.Util.existsAndNotNull(e) && Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		});
	};
})();