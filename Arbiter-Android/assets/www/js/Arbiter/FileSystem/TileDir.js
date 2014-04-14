(function(){
	
	Arbiter.TileDir = function(fileSystem, baseLayer){
		this.baseLayer = baseLayer;
		this.fileSystem = fileSystem;
	};
	
	var prototype = Arbiter.TileDir.prototype;
	
	prototype.getTileSetRoot = function(){
		return Arbiter.FileSystem.ROOT_LEVEL 
			+ Arbiter.FileSystem.fileSeparator 
			+ Arbiter.FileSystem.TILESET_ROOT;
	};
	
	prototype.getTileSetDir = function(serverId, featureType){
		
		if(!Arbiter.Util.existsAndNotNull(serverId)){
			throw "Expecting servers id";
		}
		
		if(!Arbiter.Util.existsAndNotNull(featureType)){
			throw "Expecting featureType";
		}
		
		var path = this.getTileSetRoot() + Arbiter.FileSystem.fileSeparator + serverId;
		
		var parts = featureType.split(":");
		
		for(var i = 0; i < parts.length; i++){
			path += Arbiter.FileSystem.fileSeparator + parts[i];
		}
		
		return path;
	};
	
	prototype.getTileDirPath = function(){
		
		var serverId = null;
		var featureType = null;
		
		if(!Arbiter.Util.existsAndNotNull(this.baseLayer)){
			serverId = "OpenStreetMap";
			featureType = "";
		}else{
			serverId = this.baseLayer[Arbiter.BaseLayer.SERVER_ID];
			featureType = this.baseLayer[Arbiter.BaseLayer.FEATURE_TYPE];
		}
		
		return this.getTileSetDir(serverId, featureType);
	};
	
	prototype.getTileDir = function(onSuccess, onFailure){
		
		console.log("getTileDir: " +  this.getTileDirPath(this.baseLayer), JSON.stringify(this.baseLayer));
		
		var tileDir = new Arbiter.Directory(this.fileSystem, this.getTileDirPath(this.baseLayer));
		
		var options = {
			create: true,
			exclusive: false
		};
		
		tileDir.getDirectory(options, function(){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess(tileDir);
			}
		}, function(e){
			
			if(Arbiter.Util.existsAndNotNull(e) && Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		});
	};
})();