Arbiter.MediaHelper = (function(){
	
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
        }
        
        return featureMedia;
	};
	
	var getMediaFromDbFeature = function(schema, dbFeature){
		var mediaAttribute = dbFeature[schema.getMediaColumn()];
		
		var featureMedia = null;
		
        if(mediaAttribute !== null && mediaAttribute !== undefined) {
            featureMedia = JSON.parse(mediaAttribute);
        }
        
        return featureMedia;
	};
	
	var _downloadMediaEntry = function(projectName, url,encodedCredentials, entry, onSuccess, onFailure) {
		var fileSeparator = Arbiter.FileSystem.fileSeparator;
		
		var path = Arbiter.FileSystem.ROOT_LEVEL + fileSeparator 
			+ Arbiter.FileSystem.PROJECTS + fileSeparator 
			+ projectName + fileSeparator + Arbiter.FileSystem.MEDIA;
		
        //only download if we don't have it
        Arbiter.FileSystem.getFileSystem().root.getFile(path + fileSeparator + entry, {create: false, exclusive: false},
            function(fileEntry) {
        		if(Arbiter.Util.funcExists(onSuccess)){
        			onSuccess();
        		}
            }, function(error) {
            	if(error.code === FileError.NOT_FOUND_ERR){
            		
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
                                    console.log("download error code" + transferError.code);
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
            	}
            });

    };
    
	var _downloadMedia = function(url,encodedCredentials, media, onSuccess, onFailure) {
		
		// If media doesn't exists execute the success callback
		if(media === null || media === undefined || media.length === 0){
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess();
			}
			
			return;
		}
		
		Arbiter.PreferencesHelper.get(Arbiter.PROJECT_NAME, Arbiter.MediaHelper, function(projectName){
			var mediaLength = media.length;
			var downloadedMedia = 0;
			
			for(var i = 0; i < mediaLength;i++) {
	            _downloadMediaEntry(projectName, url, encodedCredentials, media[i], function(){
	            	if(++downloadedMedia === mediaLength){
	            		if(Arbiter.Util.funcExists(onSuccess)){
	            			onSuccess();
	            		}
	            	}
	            }, onFailure);
	        }
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Arbiter.MediaHelper _downloadMedia: ", e);
			}
		});
    };
    
    var getEncodedCredentials = function(schema){
    	var serverId = schema.getServerId();
		
		var server = Arbiter.Util.Servers.getServer(serverId);
		
		return Arbiter.Util.getEncodedCredentials(
					server.getUsername(), 
					server.getPassword());
    };
    
    var downloadMediaForDbFeature = function(schema, dbFeature, onSuccess, onFailure){
		var serverId = schema.getServerId();
		
		var server = Arbiter.Util.Servers.getServer(serverId);
		
		var encodedCredentials = getEncodedCredentials(schema);
		
		_downloadMedia(getMediaUrl(schema), encodedCredentials, getMediaFromDbFeature(schema,
				dbFeature), function(){
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess();
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("downloadMediaForDbFeature error - " + e);
			}
		});
	};
    
    var sendMedia = function(url, header, media,mediaCallback) {
    	
    	Arbiter.FileSystem.ensureMediaDirectoryExists(function(mediaDir){
    		console.log("path to media file: " + mediaDir.fullPath + "/" + media);
    		mediaDir.getFile(media, {create: false, exclusive: false}, function(fileEntry) {
            			
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
                    console.log("upload error code" + error.code);
                    
                    if(mediaCallback) {
                        mediaCallback(false,media);
                    }
                }, options);
            }, function(error) {
                console.log("Unable to transfer " + media + ": File not found locally.", media, error.code);
                if(mediaCallback) {
                    mediaCallback(false,media);
                }
            });
    		
    	}, function(e){
    		console.log("Arbiter.MediaHelper sendMedia - "
    				+ "error getting media directory", e);
    		
    		if(Arbiter.Util.funcExists(mediaCallback)){
    			mediaCallback(false, media);
    		}
    	});
    };
    
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
    
    var copyMedia = function(fileEntry, key, featuresMedia){
    	var fileSystem = Arbiter.FileSystem.getFileSystem();
    	
    	// Make sure the media directory exists
    	Arbiter.FileSystem.ensureMediaDirectoryExists(function(mediaDir){
    		
    		// Move media to the media directory
    		mediaDir.getFile("temp.jpg", {create: true, exclusive: false},
    			function(tempFile){
    				tempFile.remove(function(){
    					fileEntry.copyTo(mediaDir, "temp.jpg", function(newFile){
    						
    						// Delete the temporary file created by the camera
    						fileEntry.remove(function(){
    							
    							// The file was copied successfully,
    							// so name the file properly.
    							copySuccess(newFile, key, featuresMedia);
    						}, function(e){
    							console.log("Arbiter.MediaHelper - Could not remove temporary file - ", e);
    							
    							// The file was copied successfully,
    							// so name the file properly.
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
    
    var onPictureTaken = function(imageUri, key, featuresMedia){
    	window.resolveLocalFileSystemURI(imageUri, function(fileEntry){
    		copyMedia(fileEntry, key, featuresMedia);
    	}, copyFail);
    };
    
    var downloadMediaForFeatures = function(schema, layerId, layerName,
    		failedMedia, onSuccess, onFailure){
    	
    	var finishedFeatures = 0;
		
		Arbiter.FeatureTableHelper.loadFeatures(schema, this, 
				function(feature, currentFeatureIndex, featureCount){
			
			if(featureCount === 0){
				if(Arbiter.Util.funcExists(onSuccess)){
					onSuccess(layerId, layerName, failedMedia);
				}
				
				return;
			}
			
			downloadMediaForDbFeature(schema, feature, function(){
				
				if(++finishedFeatures === featureCount){
					if(Arbiter.Util.funcExists(onSuccess)){
						onSuccess(layerId, layerName, failedMedia);
					}
				}
			}, function(e){
				if(Arbiter.Util.funcExists(onFailure)){
					++finishedFeatures;
					
					onFailure("downloadMediaForFeatures media download failed - " + e);
				}
			});
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("downloadMediaForFeatures - Error loading features - " + e);
			}
		});
    };
    
    var downloadMediaForLayer = function(schema, layerId, layerName,
    		failedMedia, onSuccess, onFailure){
    	
		downloadMediaForFeatures(schema, layerId, layerName,
				failedMedia, function(){
			
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess();
			}
		}, function(e){
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Arbiter.MediaHelper downloadMediaForLayers -"
						+ " error downloading media - " + e);
			}
		});
    };
    
	return {
		MEDIA_TO_SEND: "mediaToSend",
		
		/**
		 * @param {OpenLayers.Layer.Vector} layer Layer being synced.
		 */
		syncMedia: function(layer, onSuccess, onFailure) {
			
			var context = this;
			
			var failedMedia = new Array();
			
			var success = function(layerId, layerName){
				var schema = Arbiter.getLayerSchemas()[layerId];
				
				downloadMediaForLayer(schema, layerId, layerName, 
						failedMedia, onSuccess, onFailure);
			};
			
			Arbiter.FileSystem.ensureMediaDirectoryExists(function(){
				var url = layer.protocol.url;
		        var index = url.indexOf("geoserver/wfs");
		        url = url.substring(0,index) + "file-service/upload";
		        var header = layer.protocol.headers;
		        var layerId = Arbiter.Util.getLayerId(layer);
		        
		        Arbiter.PreferencesHelper.get(context.MEDIA_TO_SEND, Arbiter.MediaHelper, function(_mediaToSend){
		        	if(_mediaToSend === null || _mediaToSend === undefined){
		        		console.log("Arbiter.MediaHelper no media to send");
		        		
                        success(layerId, layer.name);
		        		
		        		return;
		        	}
		        	
		        	var mediaToSend = JSON.parse(_mediaToSend);
		        	
		            var layerMedia = mediaToSend[layerId];
		            
		            if(layerMedia !== null && layerMedia !== undefined
		            		&& layerMedia.length > 0) {
		            	
		                var mediaCounter = 0;
		                
		                var mediaCallback = function(_success,media) {
		                    mediaCounter++;
		                    console.log("MEDIA CALLBACK: success:", _success," media: ",media);
		                    if(_success === false) {
		                        failedMedia.push(media);
		                    }
		                    if(mediaCounter === layerMedia.length) {
		                    	success(layerId, layer.name);
		                    }
		                };
		                for(var i = 0; i < layerMedia.length;i++) {
		                    sendMedia(url, header['Authorization'], layerMedia[i], mediaCallback);
		                }
		            }else{
		            	success(layerId, layer.name);
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
		    	
		    	if(features === null || features === undefined){
		    		_success();
		    		
		    		return;
		    	}
		    	
		    	var featureCount = features.length;
		    	var featureDownloaded = 0;
		    	
		    	if(featureCount === 0){
		    		_success();
		    	}
		    	
		    	var media = null;
		    	
		    	for(var i = 0; i < featureCount; i++){
		    		
		    		_downloadMedia(getMediaUrl(schema), encodedCredentials, 
		    				getMediaFromFeature(schema, features[i]), function(){
		    			
		    			if(++featureDownloaded === featureCount){
		    				console.log("executing downloadMedia Success");
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
	    
	    takePicture: function(key, media){
	    	
	    	var cameraOptions = { 
    			quality: 20, 
    			allowEdit: false,
    			correctOrientation: true,
    			destinationType: Camera.DestinationType.FILE_URI,
    			encodingType: Camera.EncodingType.JPEG 
    	    };
	    	
	    	navigator.camera.getPicture(function(imageUri){
	    		onPictureTaken(imageUri, key, media);
	    	}, function(e){
	    		console.log("Failed to take picture - ", e);
	    	}, cameraOptions);
	    }
	};
})();