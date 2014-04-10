(function(){
	
	Arbiter.Directory = function(fileSystem, path){
		this.fileSystem = fileSystem;
		this.path = path;
		this.dir = null;
	};
	
	var prototype = Arbiter.Directory.prototype;
	
	prototype.getDirectory = function(options, onExists, onNotExists, parentDir, path){
		
		if(Arbiter.Util.existsAndNotNull(this.dir)){
			return this.dir;
		}
		
		var context = this;
		
		if(!Arbiter.Util.existsAndNotNull(path)){
			path = this.path;
		}
		
		if(path.constructor === String){
			path = path.split('/');
		}
		
		if(!Arbiter.Util.existsAndNotNull(parentDir)){
			parentDir = this.fileSystem.root;
		}
		
		if(!Arbiter.Util.existsAndNotNull(options)){
			options = {
				create: false
			};
		}
		
		var part = path.shift();
		
		if(Arbiter.Util.existsAndNotNull(part)){
			
			parentDir.getDirectory(part, options, function(dir){
				
				if(path.length > 0){
					context.getDirectory(options, onExists, onNotExists, dir, path);
				}else{
					if(Arbiter.Util.existsAndNotNull(onExists)){
						context.dir = dir;
						onExists(dir);
					}
				}
			}, function(e){
				if(e.code === FileError.NOT_FOUND_ERR){
					if(Arbiter.Util.existsAndNotNull(onNotExists)){
						onNotExists();
					}
				}else{
					if(Arbiter.Util.existsAndNotNull(onNotExists)){
						console.log("Couldn't get directory", e);
						
						onNotExists(e);
					}
				}
			});
		}
	};
})();