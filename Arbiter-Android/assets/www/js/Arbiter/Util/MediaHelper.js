Arbiter.MediaHelper = (function(){
    
    var addMediaToFeature = function(key, fileName, featuresMedia){
    	if(featuresMedia === null || featuresMedia === undefined){
    		featuresMedia = [];
    	}
    	
    	featuresMedia.push(fileName);
    	
    	Arbiter.Cordova.addMediaToFeature(key, featuresMedia, fileName);
    };
    
    var copyFail = function(e){
    	console.log("Error copying file - ", e);
    };
    
    var copySuccess = function(newFile, key, featuresMedia){
    	newFile.file(function(file) {
            var reader = new FileReader();
            
            reader.onloadend = function(evt) {
                var newFileName = SHA1(evt.target.result) + ".jpg";
                newFile.getParent(function(parentDir) {
                    newFile.moveTo(parentDir,newFileName,
                        function(copiedFile) {
                    	
                            addMediaToFeature(key, newFileName, featuresMedia);
                            
                        }, function(error) {
                            if(error.code != FileError.PATH_EXISTS_ERR) {
                                copyFail(null);
                            } else {
                                newFile.remove(function() {
                                	console.log("successfully removed file after error.");
                                }, function() {
                                	console.log("error removing file after error.");
                                });
                            }
                        });
                }, function(e){
                	copyFail("Arbiter.MediaHelper copySuccess" 
                			+ " - error getting parent dir - " + e);
                });
            };
            
            reader.readAsDataURL(file);
        }, function(e){
        	copyFail("Arbiter.MediaHelper copySuccess" 
        			+ " - Error getting file - " + e);
        });
    };
    
    var copyMedia = function(fileEntry, key, featuresMedia, removeFile){
    	var fileSystem = Arbiter.FileSystem.getFileSystem();
    	
    	// Make sure the media directory exists
    	Arbiter.FileSystem.ensureMediaDirectoryExists(function(mediaDir){
    		
    		// Move media to the media directory
    		mediaDir.getFile("temp.jpg", {create: true, exclusive: false},
    			function(tempFile){
    			
    				tempFile.remove(function(){
    					fileEntry.copyTo(mediaDir, "temp.jpg", function(newFile){
    						
    						// Delete the temporary file created by the camera
    						if (removeFile) {
        						fileEntry.remove(function(){
        							
        							// The file was copied successfully,
        							// so name the file properly.
        							
        							//For some reason the newFile from the copyTo function isn't returning the correct toURL() path
        							mediaDir.getFile("temp.jpg", {create: true, exclusive: false}, function(newFile){
        								copySuccess(newFile, key, featuresMedia);
        							}, function(e){
        								copyFail("Arbiter.MediaHelper copyMedia - Error getting created temp file - " + e);
        							});
        						}, function(e){
        							console.log("Arbiter.MediaHelper - Could not remove temporary file - ", e);
        							
        							// The file was copied successfully,
        							// so name the file properly.
        							copySuccess(newFile);
        						});
    						} else {
    							mediaDir.getFile("temp.jpg", {create: true, exclusive: false}, function(newFile){
    								copySuccess(newFile, key, featuresMedia);
    							}, function(e){
    								copyFail("Arbiter.MediaHelper copyMedia - Error getting created temp file - " + e);
    							});
    						}

    					}, function(e){
    						copyFail("Arbiter.MediaHelper copyMedia - Error copying file - " + e);
    					});
    				}, function(e){
    					copyFail("Arbiter.MediaHelper copyMedia - Error removing file - " + e);
    				});
    			}, function(e){
    				copyFail("Arbiter.MediaHelper copyMedia - Error creating temp file - " + e);
    			}
    		);
    	}, function(e){
    		copyFail("Arbiter.MediaHelper copyMedia - Error getting media dir - " + e);
    	});
    };
    
    var onPictureTaken = function(imageUri, key, featuresMedia){
    	window.resolveLocalFileSystemURI(imageUri, function(fileEntry){
    		copyMedia(fileEntry, key, featuresMedia);
    	}, copyFail, true);
    };
    
    var onPictureSelected = function(imageUri, key, featuresMedia){
    	window.resolveLocalFileSystemURI(imageUri, function(fileEntry){
    		copyMedia(fileEntry, key, featuresMedia);
    	}, copyFail, false);
    };
    
	return {
	    
	    takePicture: function(key, media){
	    	
	    	Arbiter.Cordova.setState(Arbiter.Cordova.STATES.TAKING_PICTURE);
	    	
	    	var cameraOptions = { 
    			quality: 20, 
    			allowEdit: false,
    			correctOrientation: true,
    			destinationType: Camera.DestinationType.FILE_URI,
    			encodingType: Camera.EncodingType.JPEG
    	    };
	    	
	    	navigator.camera.getPicture(function(imageUri){
	    		
	    		Arbiter.Cordova.gotPicture();
	    		
	    		onPictureTaken(imageUri, key, media);
	    	}, function(e){
	    		console.log("Failed to take picture - ", e);
	    	}, cameraOptions);
	    },
	
		selectPicture: function(key, media){
	    	
	    	Arbiter.Cordova.setState(Arbiter.Cordova.STATES.TAKING_PICTURE);
	    	
	    	var cameraOptions = { 
				quality: 20, 
				allowEdit: false,
				correctOrientation: true,
				destinationType: Camera.DestinationType.FILE_URI,
				encodingType: Camera.EncodingType.JPEG,
				sourceType: Camera.PictureSourceType.PHOTOLIBRARY
		    };
	    	
	    	navigator.camera.getPicture(function(imageUri){
	    		
	    		Arbiter.Cordova.gotPicture();
	    		
	    		onPictureSelected(imageUri, key, media);
	    	}, function(e){
	    		console.log("Failed to load picture from library - ", e);
	    	}, cameraOptions);
	    }
	};
})();