Arbiter.FileSystem = (function(){
	var fileSystem = null;
	var ROOT_LEVEL = "Arbiter";
	var TILESET = "osm";
	var fileSeparator = "/";
	
	var createTileDirectories = function(onSuccess, onFailure){
		var tileset = ROOT_LEVEL + fileSeparator + TILESET;
		
		fileSystem.root.getDirectory(ROOT_LEVEL, {create: true}, function(parent){
			fileSystem.root.getDirectory(tileset, {create: true}, function(parent){
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess();
				}
			}, function(){
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure("Error getting " + tileset + " directory");
				}
			});
		}, function(){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Error getting " + ROOT_LEVEL + " directory");
			}
		});
	};
	
	return{
		setFileSystem: function(onSuccess, onFailure){
			window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function(_fileSystem){
				fileSystem = _fileSystem;
				
				createTileDirectories(onSuccess, onFailure);
			}, function(e){
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure(e.code);
				}
			});
		},
		
		getFileSystem: function(){
			return fileSystem;
		}
	};
})();