Arbiter.FileSystem = (function(){
	var fileSystem = null;
	
	return{
		setFileSystem: function(onSuccess, onFailure){
			window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function(_fileSystem){
				fileSystem = _fileSystem;
				
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess();
				}
			});
		},
		
		getFileSystem: function(){
			return fileSystem;
		}
	};
})();