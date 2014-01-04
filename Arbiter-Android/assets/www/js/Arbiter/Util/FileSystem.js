Arbiter.FileSystem = (function(){
	var fileSystem = null;
	
	var createTileDirectories = function(onSuccess, onFailure){
		var tileset = Arbiter.FileSystem.ROOT_LEVEL + Arbiter.FileSystem.fileSeparator
			+ Arbiter.FileSystem.TILESET;
		
		fileSystem.root.getDirectory(Arbiter.FileSystem.ROOT_LEVEL, {create: true}, function(parent){
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
				onFailure("Error getting " + Arbiter.FileSystem.ROOT_LEVEL + " directory");
			}
		});
	};
	
	var createMediaDirectories = function(projectName, onSuccess, onFailure){
		var path = Arbiter.FileSystem.ROOT_LEVEL;
		
		fileSystem.root.getDirectory(Arbiter.FileSystem.ROOT_LEVEL, {create: true}, function(dir){
			
			path += Arbiter.FileSystem.fileSeparator + Arbiter.FileSystem.PROJECTS;
			
			fileSystem.root.getDirectory(path, {create: true}, function(dir){
				
				path += Arbiter.FileSystem.fileSeparator + projectName;
				
				fileSystem.root.getDirectory(path,
						{create: true}, function(dir){
					
					path += Arbiter.FileSystem.fileSeparator + Arbiter.FileSystem.MEDIA;
					
					fileSystem.root.getDirectory(path, {create: true}, function(mediaDir){
						
						if(Arbiter.Util.funcExists(onSuccess)){
							onSuccess(mediaDir);
						}
						
					}, function(){
						if(Arbiter.Util.funcExists(onFailure)){
							onFailure("Error getting " + path + " directory");
						}
					});	
					
				}, function(){
					if(Arbiter.Util.funcExists(onFailure)){
						onFailure("Error getting " + path + " directory");
					}
				});		
				
			}, function(){
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure("Error getting " + path + " directory");
				}
			});
		}, function(){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Error getting " + path + " directory");
			}
		});
	};
	
	return{
		ROOT_LEVEL : "Arbiter",
		
		TILESET : "osm",
		
		// Media dirs begin
		PROJECTS : "Projects",
		MEDIA : "Media",
		fileSeparator : "/",
		
		// Media dirs end.
		
		setFileSystem: function(onSuccess, onFailure){
			window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function(_fileSystem){
				fileSystem = _fileSystem;
				
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess();
				}
			}, function(e){
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure(e.code);
				}
			});
		},
		
		getFileSystem: function(){
			return fileSystem;
		},
		
		ensureTileDirectoryExists: function(onSuccess, onFailure){
			createTileDirectories(function(){
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess(fileSystem);
				}
			}, onFailure);
		},
		
		ensureMediaDirectoryExists: function(onSuccess, onFailure){
			Arbiter.PreferencesHelper.get(Arbiter.PROJECT_NAME, Arbiter.FileSystem, function(projectName){
				
				// Make sure the directories being 
				// used with the file api exist.
				createMediaDirectories(projectName, function(mediaDir){
					if(Arbiter.Util.funcExists(onSuccess)){
						onSuccess(mediaDir);
					}
				}, onFailure);
				
			}, function(e){
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure("Arbiter.FileSystem createMediaDirectories ERROR - " + e);
				}
			});
		}
	};
})();