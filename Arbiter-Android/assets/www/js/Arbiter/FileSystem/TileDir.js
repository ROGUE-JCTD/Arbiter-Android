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
})();