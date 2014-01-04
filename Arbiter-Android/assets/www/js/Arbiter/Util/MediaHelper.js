Arbiter.MediaHelper = (function(){
	var mediaToDownload = 0;
	var featureCount = 0;
	var featureDownloaded = 0;
	
	var reset = function(){
		mediaToDownload = 0;
		featureCount = 0;
    	featureDownloaded = 0;
	};
	
	var getMediaUrl = function(schema){
		if(schema === null || schema === undefined){
			throw "Arbiter.MediaHelper getMediaUrl schema should not be " + schema;
		}
		
		var serverUrl = schema.getUrl();
		
		if(serverUrl === ""){
			throw "Arbiter.MediaHelper getMediaUrl schema.getUrl() should not be "
				+ serverUrl;
		}
		
		var mediaURL = serverUrl + "/wfs";
        var index = mediaURL.indexOf("geoserver/wfs");
        mediaURL = mediaURL.substring(0,index) + "file-service/";
        
		return mediaURL;
	};
	
	var getMediaFromFeature = function(schema, feature){
		var mediaAttribute = feature.attributes[schema.getMediaColumn()];
		var featureMedia = null;
		
        if(mediaAttribute !== null && mediaAttribute !== undefined) {
            featureMedia = JSON.parse(mediaAttribute);
            console.log("featureMedia parsed: ", featureMedia);
            
            if(featureMedia !== null && featureMedia !== undefined 
            		&& featureMedia.length !== undefined) {
            	
                mediaToDownload += featureMedia.length;
            }
        }
        
        return featureMedia;
	};
	
	var _downloadMediaEntry = function(projectName, url,encodedCredentials, entry, onSuccess, onFailure) {
		var fileSeparator = Arbiter.FileSystem.fileSeparator;
		
		var path = Arbiter.FileSystem.ROOT_LEVEL + fileSeparator 
			+ Arbiter.FileSystem.PROJECTS + fileSeparator 
			+ projectName + fileSeparator + Arbiter.FileSystem.MEDIA;
		
		console.log("_downloadMediaEntry = " + path + fileSeparator + entry);
		
        //only download if we don't have it
        Arbiter.FileSystem.getFileSystem().root.getFile(path + fileSeparator + entry, {create: false, exclusive: false},
            function(fileEntry) {
        		if(Arbiter.Util.funcExists(onSuccess)){
        			onSuccess();
        		}
            }, function(error) {
            	console.log("_downloadMediaEntry error", error);
                // download
                Arbiter.FileSystem.getFileSystem().root.getDirectory(path, {create: true, exclusive: false},
                    function(dir) {
                        var fileTransfer = new FileTransfer();
                        var uri = encodeURI(url + entry);
                        fileTransfer.download(uri,dir.fullPath + "/" + entry,
                            function(result) {
                                console.log("download complete: " + result.fullPath);
                                if(Arbiter.Util.funcExists(onSuccess)){
                                	onSuccess();
                                }
                            }, function(transferError) {
                                console.log("download error source " + transferError.source);
                                console.log("download error target " + transferError.target);
                                console.log("upload error code" + transferError.code);
                                if(Arbiter.Util.funcExists(onFailure)){
                                	onFailure("Arbiter.MediaHelper - Error downloading media: source, target, code",
                                			transferError.source, transferError.target, transferError.code);
                                }
                            }, undefined, {
                                    headers: {
                                        'Authorization': 'Basic ' + encodedCredentials
                                }
                            });
                    }, onFailure);
            });

    };
    
	var _downloadMedia = function(url,encodedCredentials, media, onSuccess, onFailure) {
		
		// If media doesn't exists execute the success callback
		if(media === null || media === undefined){
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess();
			}
			
			return;
		}
		
		console.log("_downloadMedia media = ", media);
		Arbiter.PreferencesHelper.get(Arbiter.PROJECT_NAME, Arbiter.MediaHelper, function(projectName){
			for(var i = 0; i < media.length;i++) {
	            _downloadMediaEntry(projectName, url, encodedCredentials, media[i], onSuccess, onFailure);
	        }
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Arbiter.MediaHelper _downloadMedia: ", e);
			}
		});
    };
    
    var sendMedia = function(url, header, media,mediaCallback) {
        Arbiter.FileSystem.getFileSystem().root.getFile("Arbiter/Projects/" + Arbiter.currentProject.name + "/Media/" + media,
        		{create: false, exclusive: false}, function(fileEntry) {
        			
            var options = new FileUploadOptions();
            options.fileKey="file";
            options.fileName=fileEntry.name;
            options.mimeType="image/jpeg";
            options.headers= {
                    'Authorization': header
            };
                                    
            var params = {};
            
            options.params = params;
            
            var ft = new FileTransfer();
            ft.upload(fileEntry.fullPath, encodeURI(url), function(response) {
                console.log("Code = " + response.responseCode);
                console.log("Response = " + response.response);
                console.log("Sent = " + response.bytesSent);
                if(mediaCallback) {
                    mediaCallback(true);
                }
            }, function(error) {
                console.log("upload error source " + error.source);
                console.log("upload error target " + error.target);
                if(mediaCallback) {
                    mediaCallback(false,media);
                }
            }, options);
        }, function(error) {
            console.log("Unable to transfer " + media + ": File not found locally.", media);
            if(mediaCallback) {
                mediaCallback(false,media);
            }
        });
    };
    
    var copyFail = function(e){
    	console.log("Error copying file - ", e);
    };
    
    var copySuccess = function(newFile){
    	newFile.file(function(file) {
            var reader = new FileReader();
            
            reader.onloadend = function(evt) {
                var newFileName = SHA1(evt.target.result) + ".jpg";
                newFile.getParent(function(parentDir) {
                    newFile.moveTo(parentDir,newFileName,
                        function(copiedFile) {
                            //mediaEntries[mediaEntries.length] = newFileName;
                            //Arbiter.PopulateMediaPanel();
                    		console.log("newFile: ", newFile, newFile.fullPath);
                    		
                            /*newFile.remove(function(entry) {
                            	console.log("removal succeeded");
                            }, function(e) {
                            	console.log("error removing file", e);
                            });*/
                        }, function(error) {
                            if(error.code != FileError.PATH_EXISTS_ERR) {
                                copyFail(null);
                            } else {
                                //mediaEntries[mediaEntries.length] = newFileName;
                                //Arbiter.PopulateMediaPanel();
                                newFile.remove(function() {
                                	console.log("successfully removed file after error.");
                                }, function() {
                                	console.log("error removing file after error.");
                                });
                            }
                        });
                }, Arbiter.copyFail);
            };
            
            reader.readAsDataURL(file);
        }, Arbiter.copyFail);
    };
    
    var copyMedia = function(fileEntry){
    	var fileSystem = Arbiter.FileSystem.getFileSystem();
    	
    	// Make sure the media directory exists
    	Arbiter.FileSystem.ensureMediaDirectoryExists(function(mediaDir){
    		
    		// Move media to the media directory
    		mediaDir.getFile("temp.jpg", {create: true, exclusive: false},
    			function(tempFile){
    				tempFile.remove(function(){
    					fileEntry.copyTo(mediaDir, "temp.jpg", function(newFile){
    						
    						fileEntry.remove(function(){
    							copySuccess(newFile);
    						}, function(e){
    							console.log("Arbiter.MediaHelper - Could not remove temporary file - ", e);
    							
    							copySuccess(newFile);
    						});
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
    
    var onPictureTaken = function(imageUri){
    	console.log("onPictureTaken: imageUri = " + imageUri);
    	
    	
    	window.resolveLocalFileSystemURI(imageUri, copyMedia, copyFail);
    };
    
	return {
		MEDIA_TO_SEND: "mediaToSend",
		
		syncMedia: function(layer, onSuccess, onFailure) {
			
			Arbiter.FileSystem.ensureMediaDirectoryExists(function(){
				var url = layer.protocol.url;
		        var index = url.indexOf("geoserver/wfs");
		        url = url.substring(0,index) + "file-service/upload";
		        var header = layer.protocol.headers;
		        Arbiter.PreferencesHelper.get(MEDIA_TO_SEND, Arbiter.MediaHelper, function(_value){
		        	if(_value === null || _value === undefined){
		        		console.log("Arbiter.MediaHelper no media to send");
		        		
		        		return;
		        	}
		        	
		        	var value = JSON.parse(_value);
		        	
		            var mediaLayer = value.layer[layer.name];
		            if(mediaLayer !== null && mediaLayer !== undefined
		            		&& mediaLayer.length > 0) {
		            	
		                var mediaCounter = 0;
		                var failedMedia = new Array();
		                var mediaCallback = function(success,media) {
		                    mediaCounter++;
		                    console.log("MEDIA CALLBACK: success:", success," media: ",media);
		                    if(success === false) {
		                        failedMedia.push(media);
		                    }
		                    if(mediaCounter === mediaLayer.length) {
		                        if(Arbiter.Util.funcExists(onSuccess)) {
		                            onSuccess(layer.name,failedMedia);
		                        }
		                    }
		                };
		                for(var i = 0; i < mediaLayer.length;i++) {
		                    sendMedia(url, header['Authorization'], mediaLayer[i],mediaCallback);
		                }
		            }
		        }, function(e){
		        	if(Arbiter.Util.funcExists(onFailure)){
		        		onFailure(e);
		        	}
		        });
			}, function(e){
				if(Arbiter.Util.funcExists(onFailure)){
	        		onFailure(e);
	        	}
			});
	    },
	    
	    downloadMedia: function(schema, encodedCredentials, features, onSuccess, onFailure){
	    	
	    	Arbiter.FileSystem.ensureMediaDirectoryExists(function(){
	    		var _success = function(){
		    		if(Arbiter.Util.funcExists(onSuccess)){
		    			onSuccess();
		    		}
		    	};
		    	
		    	reset();
		    	
		    	if(features === null || features === undefined){
		    		_success();
		    		
		    		return;
		    	}
		    	
		    	featureCount = features.length;
		    	
		    	if(featureCount === 0){
		    		_success();
		    	}
		    	
		    	var media = null;
		    	
		    	for(var i = 0; i < featureCount; i++){
		    		
		    		_downloadMedia(getMediaUrl(schema), encodedCredentials, 
		    				getMediaFromFeature(schema, features[i]), function(){
		    			
		    			if(++featureDownloaded === featureCount){
		    				_success();
		    			}
		    		}, onFailure);
		    	}
	    	}, function(e){
	    		if(Arbiter.Util.funcExists(onFailure)){
	    			onFailure(e);
	    		}
	    	});
	    },
	    
	    takePicture: function(){
	    	console.log("Arbiter.MediaHelper.takePicture()");
	    	
	    	var cameraOptions = { 
    			quality: 20, 
    			allowEdit: false,
    			correctOrientation: true,
    			destinationType: Camera.DestinationType.FILE_URI,
    			encodingType: Camera.EncodingType.JPEG 
    	    };
	    	
	    	navigator.camera.getPicture(function(imageUri){
	    		onPictureTaken(imageUri);
	    	}, function(e){
	    		console.log("Failed to take picture - ", e);
	    	}, cameraOptions);
	    }
	};
})();