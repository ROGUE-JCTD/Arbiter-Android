(function(){
	
	Arbiter.Sync = function(_map,_bounds,
			_downloadOnly, _onSuccess, _onFailure,
			_fileSystem, _baseLayer, _cacheTiles, projectDb, featureDb){
		
		this.map = _map;
		this.fileSystem = _fileSystem;
		this.baseLayer = _baseLayer;
		this.cacheTiles = _cacheTiles;
		this.bounds = _bounds;
		this.downloadOnly = _downloadOnly;
		this.onSuccess = _onSuccess;
		this.onFailure = _onFailure;
		this.specificSchemas = null;
		this.projectDb = projectDb;
		this.featureDb = featureDb;
		
		this.mediaDir = null;
		this.mediaToSend = null;
		
		this.initialized = false;
		
		this.layers = null;
		this.schemas = null;
		
		this.syncInProgress = false;
		
		if(!this.downloadOnly){
			this.notificationHandler = new Arbiter.NotificationHandler(this.projectDb);
			this.syncId = null;
		}
	};

	var prototype = Arbiter.Sync.prototype;
	
	// ol layers
	prototype.setSpecificSchemas = function(schemas){
		
		console.log("sync this.downloadOnly = " + this.downloadOnly);
		
		if(this.downloadOnly !== true && this.downloadOnly !== "true"){
			throw "You cannot specify specific schema unless you're downloading only";
		}
		
		this.specificSchemas = schemas;
	};

	prototype.onSyncCompleted = function(){
		
		console.log("Arbiter.Sync completed");
		
		var context = this;
		
		var run = function(){
			
			context.removeTemporaryTables(function(){
				context.syncInProgress = false;
				
				if(Arbiter.Util.funcExists(context.onSuccess)){
					context.onSuccess();
				}
			}, function(e){
				console.log("Couldn't remove temporary tables...", e);
				
				context.onSyncFailed(e);
			});
		};
		
		run();
	};

	prototype.removeTemporaryTables = function(onSuccess, onFailure){
		
		var success = function(){
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess();
			}
		};
		
		if(Arbiter.Util.existsAndNotNull(this.schemas) && Arbiter.Util.existsAndNotNull(this.layers) && this.layers.length > 0){
			
			var tempTableCleaner = new Arbiter.TemporaryTableCleaner(this.featureDb, this.layers, this.schemas, function(){
				
				success();
			}, function(e){
				
				if(Arbiter.Util.existsAndNotNull(onFailure)){
					onFailure(e);
				}
			});
			
			tempTableCleaner.cleanup();
		}else{
			success();
		}
	};
	
	prototype.onSyncFailed = function(e){
		
		Arbiter.Cordova.syncFailed(e);
		
		if(Arbiter.Util.funcExists(this.onFailure)){
			this.onFailure(e);
		}
	};

	prototype.initialize = function(onSuccess, onFailure){
		var context = this;
		
		var success = function(){
			if(Arbiter.Util.funcExists(onSuccess)){
				onSuccess();
			}
		};
		
		if(this.initialized === true){
			success();
			
			return;
		}
		
		// Make sure the media directory exists
		Arbiter.FileSystem.ensureMediaDirectoryExists(function(mediaDir){
			
			context.mediaDir = mediaDir;
			
			// Get the media to send object from the db
			Arbiter.PreferencesHelper.get(context.projectDb, Arbiter.MEDIA_TO_SEND, context, function(mediaToSend){
				
				var callback = function(){
					
					var storeVectorSync = new Arbiter.StoreVectorToSync(context.map, context.downloadOnly,
							context.specificSchemas, function(){
						
						// Load the layers from the database
						Arbiter.LayersHelper.loadLayers(context, function(layers){
							
							context.layers = layers;
							
							context.initialized = true;
							
							context.schemas = Arbiter.getLayerSchemas();
							
							success();
						}, function(e){
							if(Arbiter.Util.funcExists(onFailure)){
								onFailure("Sync.js Error loading layers - " + e);
							}
						});
					});
					
					storeVectorSync.startStore();
				};
				
				if(Arbiter.Util.existsAndNotNull(mediaToSend) && mediaToSend !== ""){
					context.mediaToSend = JSON.parse(mediaToSend);
					
					callback();
				}else{
					callback();
				}
			}, function(e){
				console.log("Sync.js Error getting " + Arbiter.MEDIA_TO_SEND, e);
				
				if(Arbiter.Util.funcExists(onFailure)){
					onFailure("Sync.js Error getting " 
							+ Arbiter.MEDIA_TO_SEND + " - " + e);
				}
			});
		}, function(e){
			console.log("Sync.js Error getting media directory", e);
			
			if(Arbiter.Util.funcExists(onFailure)){
				onFailure("Sync.js Error getting media directory - " + e);
			}
		});
	};

	prototype.sync = function(){
		
		if(this.syncInProgress === true){
			
			console.log("Sync is already underway!");
			
			return;
		}
		
		this.syncInProgress = true;
		
		var context = this;
		
		var run = function(){
			context.initialize(function(){
				
				context.storeUploadsAndDownloads();
			}, function(e){
				
				context.onSyncFailed(e);
			});
		};
		
		if(!this.downloadOnly && Arbiter.Util.existsAndNotNull(this.notificationHandler)){
			
			this.notificationHandler.startNewSync(function(syncId){
				
				context.syncId = syncId;
				
				run();
			}, function(e){
				context.onSyncFailed(e);
			});
		}else{
			run();
		}
	};

	prototype.storeUploadsAndDownloads = function(){
		
		var context = this;
		
		var schemas = this.schemas;
		
		if(this.downloadOnly === true && Arbiter.Util.existsAndNotNull(this.specificSchemas)){
			schemas = this.specificSchemas;
		}
		
		var db = Arbiter.FeatureDbHelper.getFeatureDatabase();
		
		var storeMediaToDownload = new Arbiter.StoreMediaToDownload(
				db, this.layers, schemas, function(failedToStore){
			
			console.log("failedToStore: " + JSON.stringify(failedToStore));
			
			context.checkLayerPermissions();
		});
		
		storeMediaToDownload.startStoring();
	};

	prototype.checkLayerPermissions = function(){
		
		var context = this;
		
		var permissionsSync = new Arbiter.PermissionsSync(this.schemas);
		
		permissionsSync.sync(function(){
			
			context.startVectorSync();
		}, function(e){
			context.onSyncFailed(e);
		});
	};
	
	prototype.startVectorSync = function(){
		var context = this;
		
		var vectorSync = new Arbiter.VectorSync(this.featureDb, this.map, this.bounds,
				function(){
				
			context.startMediaSync();
			
		}, function(e){
			context.onSyncFailed(e);
		});
		
		if(this.specificSchemas !== null && this.specificSchemas !== undefined){
			console.log("set vector specific schemas");
			vectorSync.setSpecificSchemas(this.specificSchemas);
		}
		
		if(this.downloadOnly === true || this.downloadOnly === "true"){
			console.log("vector sync download only");
			vectorSync.startDownload();
		}else{
			console.log("vector sync upload and download");
			vectorSync.startUpload();
		}
	};

	prototype.startMediaSync = function(){
		var context = this;
		
		var mediaSync = new Arbiter.MediaSync(this.projectDb, this.layers, 
				this.schemas, this.mediaDir, this.mediaToSend);
		
		mediaSync.startSync(function(){

			if(context.cacheTiles === true || context.cacheTiles === "true"){
				context.startTileCache();
			}else{
				context.getNotifications();
			}
		}, function(e){
			context.onSyncFailed(e);
		}, this.downloadOnly);
	};
	
	prototype.startTileCache = function(){
		var context = this;
		
		this.initialize(function(){
			context.getNotifications();
		}, function(e){
			context.onSyncFailed(e);
		});
	};
	
	prototype.getNotifications = function(layerIndex){
		
		var context = this;
		
		this.initialize(function(){
			
			// If the notification handler exists, then get the notifications for this layer
			if(Arbiter.Util.existsAndNotNull(context.notificationHandler)){
				
				// If the layerIndex hasn't been specified, set it to 0 to get the first layer
				if(!Arbiter.Util.existsAndNotNull(layerIndex)){
					layerIndex = 0;
				} 
				
				// If the layerIndex is >= the layer count, then
				// there are no more layers to get so the sync is completed
				if(layerIndex >= context.layers.length){
					
					context.notificationHandler.syncId = context.syncId;
					
					context.notificationHandler.endCurrentSync(function(){
						
						console.log("Ended current sync");
						
						context.onSyncCompleted();
					}, function(e){
						console.log("Couldn't end current sync..", e);
						context.onSyncFailed(e);
					});
					
					return;
				}
				
				// Get the schema corresponding to the current layer
				var layer = context.layers[layerIndex];
				
				var schema = context.schemas[layer[Arbiter.LayersHelper.layerId()]];
				
				// If the schema exists and is editable, then get the notifications for the layer
				if(Arbiter.Util.existsAndNotNull(schema) && schema.isEditable()){
					
					var notificationComputer = new Arbiter.NotificationComputer(context.featureDb, context.projectDb, schema, context.syncId, function(){
						
						context.getNotifications(++layerIndex);
					}, function(e){
						console.log("failed to compute notifications", ((Arbiter.Util.existsAndNotNull(e.stack)) ? e.stack : e));
						context.onSyncFailed("Failed to compute notifications: " + ((Arbiter.Util.existsAndNotNull(e.stack)) ? e.stack : e));
					});
					
					notificationComputer.computeNotifications();
				} // If the schema isn't editable or doesn't exist, 
				// iterate to the next layer
				else{
					context.getNotifications(++layerIndex);
				}
			}else{
				context.onSyncCompleted();
			}
		}, function(e){
			context.onSyncFailed(e);
		});
	};
})();

