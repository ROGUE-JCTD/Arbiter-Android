Arbiter.FileSystem = (function(){
	var fileSystem = null;
	
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
						
						console.log("createMediaDirectory toURL() = " + mediaDir.toURL());
						
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
		NATIVE_ROOT_URL: null,
		
		ROOT_LEVEL : "Arbiter",
		
		TILESET_ROOT : "TileSets",
		
		// Media dirs begin
		PROJECTS : "Projects",
		MEDIA : "Media",
		fileSeparator : "/",
		
		// Media dirs end.
		
		setFileSystem: function(onSuccess, onFailure){
			var fail = function(e){
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure(e.code);
				}
			};
			
			window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function(_fileSystem){
				fileSystem = _fileSystem;
				
				window.resolveLocalFileSystemURL("cdvfile://localhost/persistent/", function(fileEntry){
					
					Arbiter.FileSystem.NATIVE_ROOT_URL = fileEntry.nativeURL;
					
					if(Arbiter.Util.funcExists(onSuccess)){
						onSuccess(fileSystem);
					}
				}, fail);
			}, fail);
		},
		
		getFileSystem: function(){
			return fileSystem;
		},
		
		ensureMediaDirectoryExists: function(onSuccess, onFailure){
			
			var db = Arbiter.ProjectDbHelper.getProjectDatabase();
			
			Arbiter.PreferencesHelper.get(db, Arbiter.PROJECT_NAME, Arbiter.FileSystem, function(projectName){
				
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