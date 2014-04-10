Arbiter.TileUtil = function(_appDb, _projectDb, _map, _fileSystem, _tileDir){
	var TileUtil = this;
	var appDb = _appDb;
	var projectDb = _projectDb;
	var map = _map;
	var fileSystem = _fileSystem;
	var tileDir = _tileDir;
	
	var registerOnLayerAdded = function(){
		
		map.events.register("addlayer", TileUtil, function(event){
			if(event && event.layer 
					&& event.layer.getURL){
				
				event.layer.getURL_Original = event.layer.getURL;
				
				event.layer.getURL = TileUtil.getURL;
				
				// Check to make sure OOM_Workaround is present and registered on the map
				if(Arbiter.Cordova && Arbiter.Cordova.OOM_Workaround && Arbiter.Cordova.OOM_Workaround.registered
						
						// If the OOM_Workaround is also present, it's not guaranteed that this override will
						// happen before the OOM_Workaround override.  OOM_Workaround's override sets the key
						// Arbiter.Cordova.OOM_Workaround.OOM_Workaround to true if it has already overridden the
						// layers getURL method.  If this is true, we need to make sure the OOM_Workaround override
						// gets applied again.
						&& event.layer.metadata && event.layer.metadata[Arbiter.Cordova.OOM_Workaround.OOM_Workaround]){
					
					Arbiter.Cordova.OOM_Workaround.overrideGetURL(event.layer);
				}
			}
		});
	};
	
	registerOnLayerAdded();
	
	this.maxCachingZoom = 19,
	this.debug = false,
	this.debugProgress = false,
	this.cacheTilesTest1Couter = 0,
	this.counterCacheInProgressMax = 2,
	this.androidClearWebCacheAfter = 15,
	
	this.setTileDir = function(_tileDir){
		tileDir = _tileDir;
	};
	
	this.getTileDir = function(){
		return tileDir;
	};
	
	this.formatSuffixMap = {
	    "image/png": "png",
	    "image/png8": "png",
	    "image/png24": "png",
	    "image/png32": "png",
	    "png": "png",
	    "image/jpeg": "jpg",
	    "image/jpg": "jpg",
	    "jpeg": "jpg",
	    "jpg": "jpg",
	    "image/gif": "gif"
	};
			
	this.dumpFiles = function() {
		console.log("---- TileUtil.dumpFiles");
		try {
			 console.log("---- fileSystem.root: " + fileSystem.root.name);
			 var directoryReader = fileSystem.root.createReader();
			 directoryReader.readEntries(
				function(entries) {
					 var i;
					 for (i=0; i<entries.length; i++) {
						 if (entries[i].isDirectory) {
							 console.log("--{ Dir: " + entries[i].name, "path: " + entries[i].fullPath);
						 } else {
							 console.log("--{ File: " + entries[i].name, "path: " + entries[i].fullPath);
						 }
					 }
				},
				function(err){
					console.log("TileUtil.dumpFiles ERROR", err);
				}
			);
		} catch (e) {
			console.log("TileUtil.dumpFiles ERROR", e);
		}
	};
	
	this.dumpDirectory = function(dir) {
		console.log("---- TileUtil.dumpDirectory");
		try {
			console.log("---- dir: " + dir.name);
			var directoryReader = dir.createReader();
			directoryReader.readEntries(
					function(entries) {
						var i;
						for (i=0; i<entries.length; i++) {
							if (entries[i].isDirectory) {
								console.log("--{ Dir: " + entries[i].name, "path: " + entries[i].fullPath);
							} else {
								console.log("--{ File: " + entries[i].name, "path: " + entries[i].fullPath);
							}
						}
					},
					function(){
						console.log("====>> failDirectoryReader");
					}
			);
		} catch (e) {
			console.log("====>> dumpDirectory.exception: "+ e);
		}
	};
	
	
	// start caching the cacheTile
	/**
	 * @param {OpenLayers.Bounds} aoi Area of interest to cache.
	 */
	this.startCachingTiles = function(aoi, successCallback) {
		console.log("---- startCachingTiles", aoi, successCallback);
		
		if (typeof caching !== 'undefined') {
			console.log("TileUtil.startCachingTiles: Tile Caching already in progress. Aborting new request");
			return;
		}
		
		if(aoi === null || aoi === undefined){
			throw "TileUtil.startCachingTiles aoi should not be " + aoi;
		}
		
		if(map === null || map === undefined){
			throw "TileUtil.startCachingTiles map should not be " + map;
		}
		
		//Arbiter.setMessageOverlay(Arbiter.localizeString("Caching Tiles","label","cachingTiles"), Arbiter.localizeString("Queuing Request","label","queuingRequest"));
		
		var layer = map.baseLayer;
	
		caching = {
			// extent before we start caching	
			extentOriginal: map.getExtent(),
			zoomOriginal: map.zoom,
			// extent that we should use as starting point of caching
			extent: aoi,
			startZoom: map.getZoomForExtent(aoi, true),
			layer: layer,
			counterDownloaded: 0,
			counterDownloadFailed: 0,
			counterCacheInProgress: 0,
			counterCacheInProgressWaitAttempt: 0,
			counterMax: -1,
			successCallback: successCallback, 
			requestQueue: [] 
		};
	
		caching.counterMax = TileUtil.queueCacheRequests(caching.extent);
		
		map.zoomToExtent(caching.extent, true);
		
		//Arbiter.setMessageOverlay(Arbiter.localizeString("Caching Tiles","label","cachingTiles"), Arbiter.localizeString("Will Download {0} Tiles","label","willDownloadTiles").format(caching.counterMax));
		TileUtil.serviceCacheRequests(tileDir);
	};
	
	
	this.checkCachingStatus = function(){
	    if (caching.requestQueue.length === 0){
	    	if (caching.counterCacheInProgress === 0){
	    		TileUtil.cachingComplete();
	    	}
	    } else {
	    	TileUtil.serviceCacheRequests(tileDir);
	    }
	},
	
	
	this.cachingComplete = function(){
	    console.log("---- caching complete!");
	    
	    if(map === null || map === undefined){
	    	throw "TileUtil.cachingComplete map should not be " + map;
	    }
	    
	    console.log(caching);
	    
	    if (caching.counterDownloadFailed > 0){
	    	//var msg = Arbiter.localizeString("Tile caching completed but {0} of {1} tiles failed to download. Recache if possible.","label","tilesFailed").format(caching.counterDownloadFailed, caching.counterMax);
			//alert(msg);
			//Arbiter.warning(msg);
	    	console.log("Tile caching completed but " + caching.counterDownloadFailed
	    			+ " of " + caching.counterMax + " failed to download. Recache if possible.");
		}
		
	    var callback = caching.successCallback;
	    
	    
	    // force the map to zoom to a different zoom level than we are about to go to so that it
	    // 'refreshes' the map and pulls the tiles to display
	    if (map.zoom === caching.zoomOriginal){
	    	map.zoomTo((map.zoom + 1) < map.options.numZoomLevels ? map.zoom + 1: map.zoom - 1);
	    }
	    
		// we're done - go back to the view user had before they started caching
	    //map.zoomTo();
	    //map.zoomToExtent(caching.extentOriginal, true);
	    
	    // keep for debugging
	    cachingLast = caching;
		caching = undefined;
		
		// Note: caching var is removed before servicing call back as an issue in callback will
		// leave caching hanging around which will have serious implications 
		if (callback){
			callback();
		}
		
		console.log("---- finalized last line of TileUtil.cacheTiles!");
	};
	
	this.serviceCacheRequests = function(){
		// start more downloads if we can
		if (caching.counterCacheInProgress < TileUtil.counterCacheInProgressMax){
			var newStart = 0;
			
			while (caching.requestQueue.length) {
				newStart += 1;
				
				var request = caching.requestQueue.pop();
				//TODO: try to compute?
				
				/**
				 * OSM.getURL because of getXYZ relies on getServerResolution and getServerZoom so unless we want to duplciate code, 
				 * we need to set the map to that zoom  
				 */
	//			if (request.zoom != map.zoom) {
	//				console.log("change zoom to: ", request.zoom, ", from: ", map.zoom);
	//				map.zoomTo(request.zoom);
	//			}
				
				TileUtil.cacheTile(request.bounds, request.zoom);
				
				if (caching.counterCacheInProgress + newStart === TileUtil.counterCacheInProgressMax){
					break;
				}
			}
		}
	};
	
	this.getTileCount = function(aoi){
		if(aoi === null || aoi === undefined){
			throw "TileUtil.getTileCount aoi should not be " + aoi;
		}
		
		//Arbiter.showMessageOverlay(Arbiter.localizeString("Caching Tiles","label","cachingTiles"));
	
		//-- make sure user wants to clear cache and then download tiles after 
		//   telling them how many tiles will be downloaded 
		var count = TileUtil.queueCacheRequests(aoi, true /* only count the tiles do not queue yet */);
		console.log("Downloading " + count + " tiles!");
		
		return count;
	};
	
	/**
	 * @param {OpenLayers.Bounds} aoi The aoi for caching
	 */
	this.cacheTiles = function(aoi, successCallback, errorCallback){
		
		if(aoi === null || aoi === undefined){
			throw "TileUtil.cacheTiles aoi should not be " + aoi;
		}
		
		if (typeof caching !== 'undefined') {
			//Arbiter.warning(Arbiter.localizeString("Tile Caching already in progress. Aborting new request.","label","cachingInProgress"));
			console.log("Tile caching already in progress.  Aborting new request.");
			return;
		} 
		
		//-- continue with clearing cache and then re downloading tiles 
		TileUtil.clearCache(function(){
			
			if (TileUtil.cacheTilesTest1Couter > 0) {
				console.log("~~~~ cacheTiles. done clear cache. starting testTilesTableIsEmpty MAKE sure there is only one project in arbiter!");
				TileUtil.testTilesTableIsEmpty(
					function(){
						console.log("---- cacheTiles.clearCache: success no tiles in Tiles table");
						
						// once all the cache for this project is cleared, start caching again. 
						TileUtil.startCachingTiles(aoi,
							function(){
								console.log("~~~~ done caching");
								//Arbiter.hideMessageOverlay();
								
								if (successCallback){
									successCallback();
								}
							}
						);
					},
					function(){
						TileUtil.dumpTilesTable();
						//Arbiter.error("----[ TEST FAILED: testTilesTableIsEmpty cacheTiles.clearCache: failed! Tiles Table not empty. just dumped tilestable");
						//Arbiter.hideMessageOverlay();
		
						if (errorCallback){
							errorCallback();
						}
					}
				);
			} else {
				console.log("~~~~ cacheTiles, done clearing clearing cache. starting caching");
				// once all the cache for this project is cleared, start caching again. 
				TileUtil.startCachingTiles(aoi,
					function(){
						console.log("~~~~ done caching");
						//Arbiter.hideMessageOverlay();
						
						if (successCallback){
							successCallback();
						}
					}
				);
			}
		});
	};
	
	this.testCacheTilesRepeatStart = function(millisec, maxRepeat){
		
		var onTimeout = function(){
			TileUtil.cacheTilesTest1Couter += 1;
			console.log("---[ cacheTilesTest1Couter: " + TileUtil.cacheTilesTest1Couter );
			
			if (typeof maxRepeat !== 'undefined') { 
				if (TileUtil.cacheTilesTest1Couter > maxRepeat) {
					testCacheTilesRepeatStop();
					console.log("TileUtil.testCacheTilesRepeatStart repeat completed");
				}
			}
	
			TileUtil.cacheTiles(null, function(){ 
				console.log("stoping test, testCacheTilesRepeatStop");
				TileUtil.testCacheTilesRepeatStop();
			});
		};
		
		cacheTilesTest1Timer = setInterval(onTimeout, millisec);
	};
	
	this.testCacheTilesRepeatStop = function(){
		clearTimeout(cacheTilesTest1Timer);
	};
	
	// make sure all ids in tileIds are in tiles table
	this.testTileIdsTableIntegrity = function(){
	};
	
	// make sure entries in tiles table exist on disk
	this.testTilesTableIntegrity = function(){
	};
	
	// make sure there are no entries in Tiles table
	this.testTilesTableIsEmpty = function(_success, _error){
		if(appDb === null || appDb === undefined){
			throw "TileUtil.testTilesTableIsEmpty appDb should not be " + appDb;
		}
		
		appDb.transaction(function(tx){
			var sql = "SELECT * FROM tiles;";
			
			tx.executeSql(sql, [], function(tx, res){
				if (res.rows.length == 0){
					if (_success){
						_success();
					}
				} else {
					if (_error){
						_error();
					}
				}
			}, function(tx, e){
				console.log("TileUtil.testTilesTableIsEmpty ERROR", e);
			});
		}, function(e){
			console.log("TileUtil.testTilesTableIsEmpty ERROR", e);
		});
	};
	
	// count how many png files are actually on file system
	this.testPngTileCount = function(){
	};
	
	this.downloadTile = function(_url, _numTimes, _success, _error) {
		//http://b.tile.openstreetmap.org/15/5199/12123.png
	
			//Save file to the device.	
			var addTileCallbackTest = function(){
				var saveTileSuccessTest = function(paramURL, paramPath){
					console.log("Saved tile " + _url, paramURL, paramPath);
					
					//Delete the file from the device
					var successDelete = function(){
						console.log("Tile deleted!");
						
						if(_success) { 
							_success(_numTimes);
						}
					};
					
					var errorDelete = function() {
						console.log("Tile not deleted!");
						
						if(_error) {
							_error();
						}
					};
					
					TileUtil.removeTileFromDevice(paramPath, 1000, successDelete, errorDelete);
				};
					
				var saveTileErrorTest = function(){
					console.log("Could not save tile " + _url);
					
					if(_error) {
						_error();
					}
				};
					
				TileUtil.saveTile(_url, "15", "5199", "12123", "png", saveTileSuccessTest, saveTileErrorTest);
			};
			
			//Update databases
			TileUtil.addTile(_url, "15", "5199", "12123", "png", addTileCallbackTest, 1, 1000);
	};
	
	/**
	 * @param {OpenLayers.Bounds} aoi Area of interest
	 */
	this.testTileDownload = function(aoi, _url, _numTimes) {
		
		//Get the baseLayer to cache
		var layer = map.baseLayer;
	
		//Create a caching variable to store information
		caching = {
			// extent before we start caching	
			extentOriginal: map.getExtent(),
			// extent that we should use as starting point of caching
			extent: aoi,
			startZoom: map.getZoomForExtent(aoi, true),
			layer: layer,
			counterDownloaded: 0,
			counterDownloadFailed: 0,
			counterCacheInProgress: 0,
			counterCacheInProgressWaitAttempt: 0,
			counterMax: -1,
			successCallback: function() { console.log("test caching completed!"); }, 
			requestQueue: [] 
		};
	
		//queueCacheRequest
		caching.counterMax = TileUtil.queueCacheRequests(caching.extent);
	
		//Zoom the map
		map.zoomToExtent(caching.extent, true);
		
		//
		console.log("Cache requests queued!");
		console.log(caching);
		
		if (caching.counterCacheInProgress < TileUtil.counterCacheInProgressMax){
			var newStart = 0;
			
			while (caching.requestQueue.length) {
				newStart += 1;
				
				var request = caching.requestQueue.pop();
				console.log("New request: ", request);
				//TODO: try to compute?
				
				/**
				 * OSM.getURL because of getXYZ relies on getServerResolution and getServerZoom so unless we want to duplciate code, 
				 * we need to set the map to that zoom  
				 */
				if (request.zoom != map.zoom) {
					console.log("change zoom to: ", request.zoom, ", from: ", map.zoom);
					map.zoomTo(request.zoom);
				}
				
				console.log(" -- cacheTile");
				TileUtil.cacheTile(request.bounds, request.zoom);
				
				if (caching.counterCacheInProgress + newStart === TileUtil.counterCacheInProgressMax){
					console.log(" ---- break");
					break;
				}
			}
		}
		
		var tileDownloadSuccess = function(numTimes) {
			console.log("Tile " + _url + " downloaded and removed!");
			
			var timesToRun = (numTimes - 1);
			if(timesToRun > 0) {
				TileUtil.testTileDownload(_url, timesToRun);
			}
		};
		
		var tileDownloadError = function() {
			console.log("Tile " + _url + " failed to download.");
		};
	
		//TileUtil.downloadTile(_url, _numTimes, tileDownloadSuccess, tileDownloadError);
	};
	
	this.onUpdateCachingDownloadProgress = function(){
	
		/*
		//Android crash fix
		if(navigator.app != undefined) {
			//Every 15 (default) tiles clear the web cache
			if((caching.counterDownloaded % TileUtil.androidClearWebCacheAfter) === 0 || caching.counterDownloaded === caching.counterMax) {
				console.log(" ~~~" + caching.counterDownloaded + " tiles downloaded. Clearing Web Cache!");
				navigator.app.clearCache();
			}
		}
		*/
	
		var percent = Math.round(caching.counterDownloaded/caching.counterMax * 100);
		//Arbiter.setMessageOverlay(Arbiter.localizeString("Caching Tiles","label","cachingTiles"), Arbiter.localizeString("Downloaded: ","label","downloaded") + percent + "%");
		var state = Arbiter.Cordova.getState();
		
		if(state === Arbiter.Cordova.STATES.UPDATING){
			Arbiter.Cordova.updateTileSyncingStatus(percent);
		}else if(state === Arbiter.Cordova.STATES.CREATING_PROJECT){
			Arbiter.Cordova.createProjectTileSyncingStatus(percent);
		}else{
			console.log("TileUtil.onUpdateCachingDownloadProgress WARNING: \n\n"
					+ "Arbiter.Cordova.getState() is " + state + ", but should be either " 
					+ Arbiter.Cordova.STATES.CREATING_PROJECT + " or " 
					+ Arbiter.Cordova.STATES.UPDATING);
		}
		
		if (TileUtil.debugProgress) {
			console.log("onUpdateCachingDownloadProgress: " + percent + ". counterDownloaded: " + caching.counterDownloaded + ", counterMax: " + caching.counterMax);
		}
	};
	
	this.getURL = function(bounds) {
		var xyz = TileUtil.getXYZ(bounds, map.baseLayer);
		
	    var ext = TileUtil.getLayerFormatExtension(this);
	    
	    // use the info we have to derive were the tile would be stored on the device
	    
	    var path;
	    
	    if(Arbiter.hasAOIBeenSet() && Arbiter.Util.existsAndNotNull(this.metadata) && this.metadata.isBaseLayer){
	    	
	    	path = fileSystem.root.fullPath + "/" + tileDir.path +"/" 
	    		+ xyz.z + "/" + xyz.x + "/" + xyz.y + "." + ext;
	    }else{
	    	path = this.getURL_Original(bounds);
	    }
	    
	 	return path;
	};
	
	this.getLayerFormatExtension = function(layer) {
		var ext = "png"; 
	    
	    if (layer instanceof OpenLayers.Layer.OSM) {
	    	ext = "png";
	    } else if (layer instanceof OpenLayers.Layer.WMS) {
			ext = TileUtil.formatSuffixMap[layer.params.FORMAT]; 
	    } else if (layer instanceof OpenLayers.Layer.WMTS) {
			ext = TileUtil.formatSuffixMap[layer.format];
	    } else if (layer instanceof OpenLayers.Layer.TMS) {
			ext = layer.type;
	    } else {
	    	console.log();
	    	Arbiter.warning("unknown layer type asssuming extension of " + ext);
	    }
	    
	    return ext;
	};
	
	this.queueCacheRequests = function(bounds, onlyCountTile) {
		
		// store current zoom since the function will change zoom level
		var currentZoom = map.zoom;
		
		var count = 0;
		
		var zoom = map.baseLayer.numZoomLevels;
		
		if(zoom > this.maxCachingZoom){
			zoom = this.maxCachingZoom;
		}
		
		for (var i=0; i < zoom; i++) {
			count += TileUtil.queueCacheRequestsForZoom(map.baseLayer, bounds, i, onlyCountTile);
		}
	
		return count;
	};
	
	this.queueCacheRequestsForZoom = function(layer, bounds, zoomLevel, onlyCountTile) {
		
		var count = 0;
		
		var origin = layer.getTileOrigin();
	    var resolutionForZoom = map.getResolutionForZoom(zoomLevel);
		var resolutionForZoomServer = layer.getServerResolution(resolutionForZoom);
	
	    var tileLayout = layer.calculateGridLayout(bounds, origin, resolutionForZoomServer);
	    var tilelon = tileLayout.tilelon;
	    var tilelat = tileLayout.tilelat;
	    
	    // computing offset the same way OpenLayers 2.12 was doing it before tileoffsetx, tileoffsetlon, tileoffsety, tileoffsetlat where removed 
	    // from the result of calculateGridLayout
	    var offsetlon = bounds.left - origin.lon;
	    var tilecol = Math.floor(offsetlon/tilelon) - layer.buffer;
	    var tilecolremain = offsetlon/tilelon - tilecol;
	    var tileoffsetx = -tilecolremain * layer.tileSize.w;
	    var tileoffsetlon = origin.lon + tilecol * tilelon;
	    
	    var offsetlat = bounds.top - (origin.lat + tilelat);  
	    var tilerow = Math.ceil(offsetlat/tilelat) + layer.buffer;
	    var tilerowremain = tilerow - offsetlat/tilelat;
	    var tileoffsety = -tilerowremain * layer.tileSize.h;
	    var tileoffsetlat = origin.lat + tilerow * tilelat;
	    
		// Added since will compute rows and columns based on the provided bound instead of layer buffer etc
		var extentWidth = (bounds.right - bounds.left) / resolutionForZoom;
	    var extentHeight = (bounds.top - bounds.bottom) / resolutionForZoom;	
	    
		//NOTE: this is a correction over what openlayers does in Grid.initGriddedTiles to catch a bug and include the tiles in the edge cases. 
		var minCols = (Math.abs(tileoffsetx) +  extentWidth) / layer.tileSize.w;
		var minRows = (Math.abs(tileoffsety) +  extentHeight) / layer.tileSize.h;  
	
		var startX = tileoffsetx;
		var startLon = tileoffsetlon;
	
		for (var row = 0; row < minRows; row++) {
			tileoffsetlon = startLon;
			tileoffsetx = startX;
	
			for (var col = 0; col < minCols; col++) {
				var tileBoundsLeft = tileoffsetlon;
				var tileBoundsTop = tileoffsetlat + tilelat;
				var tileBoundsRight = tileoffsetlon + tilelon;
				var tileBoundsBottom = tileoffsetlat;
				
				tileoffsetlon += tilelon;
				tileoffsetx += layer.tileSize.w;
				
				count++;
				if (!onlyCountTile){
					var tileBounds = new OpenLayers.Bounds(tileBoundsLeft, tileBoundsBottom, tileBoundsRight, tileBoundsTop);
					//TODO: try using best fit zoom for bounds instead of storing...should work 
				 	caching.requestQueue.push({ bounds: tileBounds, zoom: zoomLevel });
				}
			}
	
			tileoffsetlat -= tilelat;
			tileoffsety += layer.tileSize.h;
		}
		
		return count;
	};
	
	
	this.queueCacheRequestsForZoomOpenLayers212 = function(layer, bounds, zoomLevel, onlyCountTile) {
		
		var count = 0;
			
	    var resolutionForZoom = map.getResolutionForZoom(zoomLevel);
	    var extentWidth = (bounds.right - bounds.left) / resolutionForZoom;
	    var extentHeight = (bounds.top - bounds.bottom) / resolutionForZoom;
	
		var origin = layer.getTileOrigin();
		var resolution = layer.getServerResolution(resolutionForZoom);
	
		var tileLayout = layer.calculateGridLayout(bounds, origin, resolution);
	
		var tileoffsetx = Math.round(tileLayout.tileoffsetx); 
		var tileoffsety = Math.round(tileLayout.tileoffsety);
		
		var tilelon = tileLayout.tilelon;
		var tilelat = tileLayout.tilelat;
	
		//NOTE: this is a correction over what openlayers does to catch a bug and include the tiles in the edge cases. 
		var minCols = (Math.abs(tileoffsetx) +  extentWidth) / layer.tileSize.w;
		var minRows = (Math.abs(tileoffsety) + extentHeight) / layer.tileSize.h;
	
		
		var tilelon = tileLayout.tilelon;
		var tilelat = tileLayout.tilelat;
	
		var startX = tileoffsetx;
		var startLon = tileoffsetlon;
	
		for (var row = 0; row < minRows; row++) {
			tileoffsetlon = startLon;
			tileoffsetx = startX;
	
			for (var col = 0; col < minCols; col++) {
				var tileBoundsLeft = tileoffsetlon;
				var tileBoundsTop = tileoffsetlat + tilelat;
				var tileBoundsRight = tileoffsetlon + tilelon;
				var tileBoundsBottom = tileoffsetlat;
				
				tileoffsetlon += tilelon;
				tileoffsetx += layer.tileSize.w;
				
				count++;
				if (!onlyCountTile){
					var tileBounds = new OpenLayers.Bounds(tileBoundsLeft, tileBoundsBottom, tileBoundsRight, tileBoundsTop);
					//TODO: try using best fit zoom for bounds instead of storing...should work 
				 	caching.requestQueue.push({ bounds: tileBounds, zoom: zoomLevel });
				}
			}
	
			tileoffsetlat -= tilelat;
			tileoffsety += layer.tileSize.h;
		}
		
		return count;
	};
	
	/**
	 * Method: getXYZ
	 * Calculates x, y and z for the given bounds. We need this to know where to store a tile regardless of it coming from an XYZ layer, WMTS, WMS, etc layer type
	 * alternitavely can store all files in a flat directory but guessing that performance might be come an issue with several projects with thousands of tiles each
	 */
	this.getXYZ = function(bounds, layer, zoom) {
		// unfortunately have to rely on map's zoom... 
		
		var resolutionForZoom = map.getResolutionForZoom(zoom);
		
		var res = layer.getServerResolution(resolutionForZoom);
		
	    var x = Math.round((bounds.left - layer.maxExtent.left) /
	        (res * layer.tileSize.w));
	    var y = Math.round((layer.maxExtent.top - bounds.top) /
	        (res * layer.tileSize.h));
	    
	    //NOTE: not using layer.getServerZoom because it realies on 
	    //      maps current zoom level which we are trying to avoid setting while caching
	    //      to reduce how often android crashes. 
	    // var z = layer.getServerZoom();
	    
	    var z = -1;
	    
		if (layer.serverResolutions){
			z = OpenLayers.Util.indexOf(layer.serverResolutions, res);
		} else {
			z = map.getZoomForResolution(res) + (layer.zoomOffset || 0);
		}
	    
		if (z===-1){
			Arbiter.error('TileUtil.getXYZ, z === -1');
		}
		
		if(layer instanceof OpenLayers.Layer.TMS){
			
			y = (1 << z) - y - 1;
		}else{
			if (layer.wrapDateLine) {
		        var limit = Math.pow(2, z);
		        x = ((x % limit) + limit) % limit;
		    }
		}
	
	    return {'x': x, 'y': y, 'z': z};
	};
	
	this.getURLForXYZLayerOnly = function (layer, xyz) {
	    var url = layer.url;
	    if (OpenLayers.Util.isArray(url)) {
	        var s = '' + xyz.x + xyz.y + xyz.z;
	        url = layer.selectUrl(s, url);
	    }
	    
	    return OpenLayers.String.format(url, xyz);
	};
	
	this.getURLForTMSLayerOnly = function(layer, xyz){
		var url = layer.url;
		
		var path = layer.serviceVersion + "/" + layer.layername 
			+ "/" + xyz.z + "/" + xyz.x + "/" 
			+ xyz.y + "." + layer.type; 
		
        var url = layer.url;
        if (OpenLayers.Util.isArray(url)) {
            url = layer.selectUrl(path, url);
        }
        return url + path;
	};
	
	this.cacheTile = function(bounds, zoom){
		
	    if (TileUtil.debug) {
	    	console.log("--- TileUtil.cacheTile, bounds & zoom: ", bounds, zoom);
	    }
	    	
	    if(appDb === null || appDb === undefined){
	    	throw "TileUtil.cacheTile appDb should not be " + appDb;
	    }
	    
	    var xyz = TileUtil.getXYZ(bounds, caching.layer, zoom);
	    //alert("printed tile xyz. " + xyz);
		
		var url = '';
	
		// If the layer is an OSM (or XYZ) layer, don't call its getURL because it elies on the current 
		// zoom level of the map which we avoid settings since it results in more crashes on android
		if (caching.layer instanceof OpenLayers.Layer.XYZ) {
			url = TileUtil.getURLForXYZLayerOnly(caching.layer, xyz);
		} else if(caching.layer instanceof OpenLayers.Layer.TMS){
			url = TileUtil.getURLForTMSLayerOnly(caching.layer, xyz);
		}else {
			url = caching.layer.getURL_Original(bounds);
		}
	
	    if (TileUtil.debug) {
	    	console.log("cacheTile.url: ", url);
	    }
	    
	
	    //alert("printed url");
		 		
		// track that we are planning to and have essentially starting to cache this tile.
		// once we save the file or the attempt fails for ANY reason, we'll decrement the counter.
		// when the counter hits 0, we are done caching. 
		caching.counterCacheInProgress += 1;	 		
	
		// have we cached the tile already? 
		appDb.transaction(function(tx){
	
			//---- have we cached the tile already?
			//TODO: bring everythign back for now for debugging we only need url though!
			var selectTileSql = "SELECT * FROM tiles WHERE url=?;";
			
			if (TileUtil.debug) {
				console.log("SQL: " + selectTileSql + ", params: " + url);
			}
			
			tx.executeSql(selectTileSql, [url], function(tx, res){
	
				
				if (TileUtil.debug) {
					console.log("getURL.res.rows.length: " + res.rows.length);
					console.log(TileUtil.rowsToString(res.rows));
				}
				
				var tileNewRefCounter = -1;
				var tileId = -1;
				
				// if we do not have this tile already, it'll have ref_count of 1 and
				// if we already have this tile, we'll increment its ref_counter by one also retrieve the tileId
				if (res.rows.length === 0) {
					tileNewRefCounter = 1;
				} else if (res.rows.length === 1) {
					tileNewRefCounter = res.rows.item(0).ref_counter + 1;
					tileId = res.rows.item(0).id;
				} else {
					// for some reason the tile have been cached under two separate entries!!
					console.log("====>> ERROR: TileUtil.getURL: Multiple Entries for tile " + TileUtil.rowsToString(res.rows));
					throw "TileUtil.cacheTile: TileUtil.getURL: Multiple Entries for tile! see console for details. count: " + res.rows.length;
				}
				
				var ext = TileUtil.getLayerFormatExtension(caching.layer);
				
				var addTileCallback = function(){
		    		//TODO: save tile should rely on add tile!!! and if add tile fails, the whole thing fail?
		    		
					var saveTileSuccess = function(url, path){
						caching.counterCacheInProgress--;
						caching.counterDownloaded++;
						console.log(caching.counterDownloaded + " of " + caching.counterMax + " downloaded!");
						TileUtil.onUpdateCachingDownloadProgress();
						
						TileUtil.checkCachingStatus();
					};
					
					var saveTileError = function(url, path, err){
						caching.counterDownloadFailed++;
						caching.counterCacheInProgress--;
						TileUtil.onUpdateCachingDownloadProgress();
						console.log("TileUtil.cacheTile WARNING saveTileError filename, path, error", url, path, err);
						
						TileUtil.checkCachingStatus();
						//TODO: failed to download file and save to disk so just remove it from global.tiles and project.tileIds tables
						//TileUtil.removeTile(url, path);
					};
					
					// write the tile to device
					TileUtil.saveTile(url, xyz.z, xyz.x, xyz.y, ext, saveTileSuccess, saveTileError);
				};
	
				//TODO: get rid of tile ids in general and just store it as a json array in projectKeyValueDatabase?
				
				// add the tile to databases immediately so that if multiple getURL calls come in for a given tile, 
				// we do not download the tile multiple times
	    		TileUtil.addTile(url, xyz.z, xyz.x, xyz.y, ext, addTileCallback, tileNewRefCounter, tileId);
			}, function(tx, e) {
				console.log("TileUtil.cacheTile ERROR", e);
			});	
		}, function(e) {
			console.log("TileUtil.cacheTile ERROR", e);
		});
	};
	
	
	this.saveTile = function(fileUrl, z, x, y, ext, successCallback, errorCallback) {
		if (TileUtil.debug) {
			console.log("---- TileUtil.saveTile. tileset: " + tileDir.path + ", z: " + z + ", x: " + x + ", y: " + y + ", url: " + fileUrl);
		}
		
		//console.log("---- tilesetDirEntry: " + tilesetDirEntry.fullPath);
		tileDir.dir.getDirectory("" + z, {create: true, exclusive: false}, 
			function(zDirEntry){
				//console.log("---- zDirEntry: " + zDirEntry.fullPath);
				zDirEntry.getDirectory("" + x, {create: true, exclusive: false}, 
					function(xDirEntry){
						//console.log("---- xDirEntry: " + xDirEntry.fullPath);
						var filePath = xDirEntry.toURL() + "/" + y + "." + ext; 
						
						//console.log("==== will store file at: " + filePath);
				
						var fileTransfer = new FileTransfer();
						//var uri = encodeURI(fileUrl);
						var uri = fileUrl;
						
						fileTransfer.download(
							uri,
							filePath,
							function(entry) {
								if (successCallback){
									successCallback(fileUrl, filePath);
								}
							},
							function(err) {
								console.log("fileTransfer.download error: ");
								console.log(err);
								
								console.log("TileUtil.saveTile WARNING Failed download or save file to: " + filePath, err);
								
								if (errorCallback){
									errorCallback(fileUrl, filePath, err);
								}
							}
						);
					}, function(e1, e2) {
						if (errorCallback){
							errorCallback(e1, e2);
						}
						console.log("TileUtil.saveTile ERROR 1", e1, e2);
					}
				);
			}, function(e1, e2) {
				if (errorCallback){
					errorCallback(e1, e2);
				}
				console.log("TileUtil.saveTile ERROR 2", e1, e2);
			}
		);
	
		return;
	};
	
	this.addTile = function(url, z, x, y, ext,
			successCallback, tileNewRefCounter, tileId) {
		
		if(appDb === null || appDb === undefined){
			throw "TileUtil.addTile appDb should not be " + appDb;
		}
		
	    if (TileUtil.debug) {
	    	console.log("---- TileUtil.addTile: ", url, tileDir.path, z, x, y, ext, tileNewRefCounter, tileId );
	    }
	
		if (tileNewRefCounter === 1) {
			// alert("inserted tile. id: " + res.insertId);
			appDb.transaction(function(tx) {
				var path = fileSystem.root.fullPath + "/" + tileDir.path +"/" + z + "/" + x + "/" + y + "." + ext;
	
				var statement = "INSERT INTO tiles (tileset, z, x, y, path, url, ref_counter) VALUES (?, ?, ?, ?, ?, ?, ?);";
				tx.executeSql(statement, [ tileDir.path, z, x, y, path, url, 1 ], function(tx, res) {
						
					//HACK WORKAROUND: 	the first time something is inserted into a table
					// 					the inserterId comes back null for some reason. 
					//					catch it and assume it was id of 1
					if (res.insertId == null){
						res.insertId = 1;
						//Arbiter.warning("@@@@@@ caught res.insertId == null inserintg into tiles. using 1 as workaround");
					}
						
				    TileUtil.insertIntoTileIds(res.insertId, successCallback);
	
					if (TileUtil.debug) {
						console.log("inserted new url in tiles. res.insertId: " + res.insertId);
					}
				}, function(tx, e) {
					console.log("TileUtil.addTile ERROR", e);
				});
			}, function(e) {
				console.log("TileUtil.addTile ERROR", e);
			});
	
		} else if (tileNewRefCounter > 1) {
			//TEST NOTE only for testing single project
			//Arbiter.warning("only a warning if arbiter only has a single project cached. about to increment existing tiles refcounter for id: " + tileId);
			
			appDb.transaction(function(tx) {
	
				var statement = "UPDATE tiles SET ref_counter=? WHERE url=?;";
				tx.executeSql(statement, [ tileNewRefCounter, url ], function(tx, res) {
					
					//TEST NOTE message important when debugging single projects
					//console.log("!!!!!!!!!!!!!!! only a warning if arbiter only has a single project cached. calling insertIntoTileIds for an Existing tile in tileId: " + tileId);
				    
					if ((typeof tileId === 'undefined') || tileId === null) {
						Arbiter.error("addTile must be provided tileId when tileNewRefCounter > 1");
					}
					
					TileUtil.insertIntoTileIds(tileId, successCallback);
	
					if (TileUtil.debug) {
						console.log("updated tiles. for url : " + url);
					}
				}, function(tx, e) {
					console.log("TileUtil.addTile ERROR tileNewRefCount > 1", e);
				});
			}, function(e) {
				console.log("TileUtil.addTile ERROR tileNewRefCount > 1", e);
			});
	
		} else {
			console.log("TileUtil.addTile ERROR tileNewRefCounter not >= 1: " + tileNewRefCounter);
		}
	}; 
	
	//clear entries in db, removed tiles from device
	this.clearCache = function(successCallback) {
		if (TileUtil.debug) {
			console.log("---- TileUtil.clearCache");
		}	
		
		//Arbiter.setMessageOverlay(Arbiter.localizeString("Caching Tiles","label","cachingTiles"), Arbiter.localizeString("Removing Tiles","label","removingTiles"));
	
		
		var op = function(tx){
			var sql = "SELECT id FROM tileIds;";
			tx.executeSql(sql, [], function(tx, res){
				
				if (res.rows.length > 0) {
				
					var removeCounter = 0;
					
					var removeCounterCallback = function() {
						removeCounter += 1;
						var percent = Math.round(removeCounter/res.rows.length * 100);
						//Arbiter.setMessageOverlay(Arbiter.localizeString("Caching Tiles","label","cachingTiles"), Arbiter.localizeString("Removed: ","label","removed") + percent + "%");
	
						if (TileUtil.debug) {
							console.log("removeCounter: " + removeCounter + ", percent cleared: " + percent);
						}
						
						if (removeCounter === res.rows.length) {
							TileUtil.deleteTileIdsEntries(successCallback);
						}
					};
					
					for(var i = 0; i < res.rows.length; i++){
						var tileId = res.rows.item(i).id;
						TileUtil.removeTileById(tileId, removeCounterCallback);
					}
				} else {
					if (successCallback){
						successCallback();
					}				
				}
	
			}, function(tx, e) {
				console.log("TileUtil.clearCache ERROR", e);
			});
		};
		
		if(projectDb !== null && projectDb !== undefined){
			projectDb.transaction(op, function(e) {
				console.log("TileUtil.clearCache ERROR", e);
			});
		}else{
			throw "TileUtil.clearCache projectDb should not be '" 
				+ projectDb + "'";
		}
			
	};
	
	this.deleteTileIdsEntries = function(successCallback){
		console.log("---- TileUtil.deleteTileIdsEntries");
		
		if(projectDb === null || projectDb === undefined){
			throw "TileUtil.clearCache projectDb should not be '" 
				+ projectDb + "'";
		}
		
		// Remove everything from tileIds table
		projectDb.transaction(function(tx){
			var statement = "DELETE FROM tileIds;";
			tx.executeSql(statement, [], function(tx, res){
				if (TileUtil.debug) {
					console.log("---- TileUtil.deleteTileIdsEntries done");
				}
				
				if (successCallback){
					successCallback();
				}
			}, function(tx, e) {
				//Arbiter.error("chk9", e1, e2);
				console.log("TileUtil.deleteTileIdsEntries error", e);
			});					
		}, function(e) {
			console.log("TileUtil.deleteTileIdsEntries error", e);
		});	
	};
	
	this.deleteTilesEntries = function(){
		console.log("---- TileUtil.deleteTileIdsEntries");
	
		if(appDb === null || appDb === undefined){
			throw "TileUtil.deleteTileEntries appDb should not be "
				+ appDb;
		}
		
		appDb.transaction(function(tx){
			var sql = "DELETE FROM tiles";
			
			tx.executeSql(sql, [], function(tx, res){
				if (TileUtil.debug) {
					console.log("---- TileUtil.deleteTilesEntries done");
				}
			}, function(tx, err){
				console.log("TileUtil.deleteTilesEntries ERROR", err);
			});
		}, function(e){
			console.log("TileUtil.deleteTilesEntries ERROR", e);
		});
	};
	
	this.deleteAllTileTableEntriesAndTheirPngFiles = function(){
		console.log("---- TileUtil.deleteAllTileTableEntriesAndTheirPngFiles");
	
		if(appDb === null || appDb === undefined){
			throw "TileUtil.deleteAllTileTableEntriesAndTheirPngFiles"
				+ "appDb should not be " + appDb;
		}
		
		appDb.transaction(function(tx){
			var sql = "SELECT * FROM tiles;";
			
			tx.executeSql(sql, [], function(tx, res){
				console.log("deleting pngs mapped to entries. count:" + res.rows.length);
				
				for(var i = 0; i < res.rows.length; i++){
					var row = res.rows.item(i);
					TileUtil.removeTileFromDevice(row.path, row.id, null, function(){
						Arbiter.error("chk100");
					});
					
					TileUtil.deleteTilesEntries();
				}	
			}, function(tx, err){
				console.log("TileUtil.deleleteAllTileTableEntriesAndTheirPngFiles" +
						" ERROR", err);
			});
		}, function(e){
			console.log("TileUtil.deleleteAllTileTableEntriesAndTheirPngFiles" +
					" ERROR", e);
		});
	};
	
	this.insertIntoTileIds = function(id, successCallback) {
		if(projectDb === null || projectDb === undefined){
			throw "TileUtil.insertIntoTileIds projectDb should not be "
				+ projectDb;
		}
		
	    if (TileUtil.debug) {
	    	console.log("---- TileUtil.addToTileIds. id: " + id);
	    }
	    
	    //var idsBeforeTx = TileUtil.tableToString(Arbiter.currentProject.variablesDatabase, "tileIds");
	    //console.log("dumping before inserting id into tileIds. id: " + id);
	    //TileUtil.dumpTileIds();
	    
		projectDb.transaction(function(tx) {
			
			//var statement = "INSERT tileIds (cName) SELECT DISTINCT Name FROM CompResults cr WHERE NOT EXISTS (SELECT * FROM Compettrr c WHERE cr.Name = c.cName)";
			var statement = "INSERT INTO tileIds (id) SELECT ? WHERE NOT EXISTS (SELECT id FROM tileIds WHERE id = ?);";
			
			//var statement = "INSERT INTO tileIds (id) VALUES (?);";
			tx.executeSql(statement, [id, id], function(tx, res) {
				if (TileUtil.debug) {
					console.log("inserted in tileIds. id: " + id);
				}
				
				if (successCallback){
					successCallback();
				}
			}, function(tx, e) {
				console.log("dumping after error for id: " + id);
				//TileUtil.dumpTileIds();
				console.log("CHK: " + statement + ": " + id);
				console.log("TileUtil.insertIntoTileIds ERROR", e);
			});
		}, function(e) {
			console.log("TileUtil.insertIntoTileIds ERROR", e);
		});
	};
	
	this.removeTileFromDevice = function(path, id, successCallback, errorCallback){
		// remove tile from disk
		var newPath = path.replace(fileSystem.root.fullPath + '/','');
		fileSystem.root.getFile(newPath, {create: false},
			function(fileEntry){
				fileEntry.remove(
					function(fileEntry){
		
					    if (TileUtil.debug) {
					    	console.log("-- TileUtil.removeTileById. removed tile from disk . tiles.id: " + id + ", path: " + path);
					    }
						
						if (successCallback){
							successCallback();
						}
					},
					function(e){
						console.log("TileUtil.removeTileFromDevice ERROR: failed to delete tile from disk. tiles.id: "
								+ id + ", path: " + path, e);
					}
				);
			}, 
			function(err){
				console.log("TileUtil.removeTileFromDevice ERROR: get file from root failed. will assume success. tiles.id: " 
						+ id + ", path: " + path, err);
		
				if (successCallback){
					successCallback();
				}
			}
		);
	};
	
	
	/**
	 * given a tileId, remove it from the project's tileIds table
	 * then look in the global tiles table to decrement the reference counter
	 * if counter is already only one, remove the entry from the global table
	 * and delete the actual tile from the device. 
	 */
	this.removeTileById = function(id, successCallback, errorCallback) {
		if(appDb === null || appDb === undefined){
			throw "TileUtil.removeTileById appDb should not be " + appDb;
		}
		
		
	    if (TileUtil.debug) {
	    	console.log("---- TileUtil.removeTileById: " + id);
	    }
		
		appDb.transaction(function(tx){
			var statement = "SELECT id, url, path, ref_counter FROM tiles WHERE id=?;";
			tx.executeSql(statement, [id], function(tx, res){
				
			    if (TileUtil.debug) {
					console.log("TileUtil.removeTileById. rows to remove:");
					console.log(TileUtil.rowsToString(res.rows));
			    }
	
				// we should only have one tile for this url
				if (res.rows.length === 1){
	
					var tileEntry = res.rows.item(0);
					
					// if the counter is only at 1, we can delete the file from disk
					if (tileEntry.ref_counter === 1){
	
						appDb.transaction(function(tx){
	
							var statement = "DELETE FROM tiles WHERE id=?;";
							tx.executeSql(statement, [id], function(tx, res){
		
								TileUtil.removeTileFromDevice(tileEntry.path, id, successCallback, errorCallback);
								
							}, function(tx, e) {
								console.log("TileUtil.removeTileById ERROR deleting tile", e);
							});
						}, function(e) {
							console.log("TileUtil.removeTileById ERROR deleting tile", e);
						});							
						
					} else if (tileEntry.ref_counter > 1){
						appDb.transaction(function(tx){
							// decrement ref_counter
							var statement = "UPDATE tiles SET ref_counter=? WHERE id=?;";
							tx.executeSql(statement, [(tileEntry.ref_counter - 1), id], function(tx, res){
							    
								if (TileUtil.debug) {
							    	console.log("-- decremented ref_counter to: " + (tileEntry.ref_counter - 1));
							    }
							    
								if (successCallback){
									successCallback();
								}
							}, function(tx, e) {
								console.log("TileUtil.removeTileById ERROR updating tile ref_counter", e);
							});
						}, function(e1, e2) {
							console.log("TileUtil.removeTileById ERROR updating tile ref_counter", e);
						});	
						
					} else {
						throw "TileUtil.removeTileById ERROR tileEntry.ref_counter <= 0 for id: " + id;
					}
					
				} else if (res.rows.length === 0){
					// should not happen
					console.log("TileUtil.removeTileById: tile id from tileIds not in tiles table. Will return succes so the tileIds table gets flushed anyway. id: " + id);
					if (successCallback){
						successCallback();
					}
				} else {
					// should not happen
					throw "TileUtil.removeTileById ERROR tiles table has multiple entries for id: " + id;
				}
			}, function(tx, e) {
				console.log("TileUtil.removeTileById ERROR ", e);
			});
		}, function(e) {
			console.log("TileUtil.removeTileById ERROR ", e);
		});	
		
	
	}; 
	
	this.dumpTableNames = function(database){
		console.log("---- TileUtil.dumpTable");
		if(database === null || database === undefined){
			throw "TileUtil.dumpTableNames database should not be " + database;
		}
		
		database.transaction(function(tx){
			tx.executeSql("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;", [], function(tx, res){
				console.log(TileUtil.rowsToString(res.rows));
			}, function(tx, e) {
				console.log("TileUtil.dumpTableNames ERROR", e);
			});	
		}, function(e) {
			console.log("TileUtil.dumpTableNames ERROR", e);
		});
	};
	
	
	this.dumpTableRows = function(database, tableName){
		if(database === null || database === undefined){
			throw "TileUtil.dumpTableRows database should not be " + database;
		}
		
		if(tableName === null || tableName === undefined){
			throw "TileUtil.dumpTableRows tableName should not be " + tableName;
		}
		
		console.log("---- TileUtil.dumpTableRows");
		database.transaction(function(tx){
			tx.executeSql("SELECT * FROM " + tableName + ";", [], function(tx, res){
				console.log(TileUtil.rowsToString(res.rows));
			}, function(tx, e) {
				console.log("TileUtil.dumpTableRows ERROR", e);
			});	
		}, function(e) {
			console.log("TileUtil.dumpTableRows ERROR", e);
		});
	};
	
	
	this.dumpTableAttributes = function(database, tableName){
		console.log("---- TileUtil.dumpTableAttributes");
	
		if(database === null || database === undefined){
			throw "TileUtil.dumpTableRows database should not be " + database;
		}
		
		if(tableName === null || tableName === undefined){
			throw "TileUtil.dumpTableRows tableName should not be " + tableName;
		}
	
		database.transaction(function(tx){
			// get the attributes of the layer
			var sql = "PRAGMA table_info (" + tableName + ");";
			
			tx.executeSql(sql, [], function(tx, res){
				var str = 'attribute count: ' + res.rows.length + '\n';
				for (var i=0; i<res.rows.length; i++) {
					var attr = res.rows.item(i);
					str += 'name: ' + attr.name + ', type: ' + attr.type + ', notnull: ' + attr.notnull + '\n';
				}
				
				console.log(str);
			}, function(tx, e){
				console.log("TileUtil.dumpTableAttributes ERROR", e);
			});
		}, function(e){
			console.log("TileUtil.dumpTableAttributes ERROR", e);
		});
	};
	
	this.rowsToString = function(rows) {
		var rowsStr = "rows.length: " + rows.length + "\n";
		for(var i = 0; i < rows.length; i++){
			var row = rows.item(i);
			var rowData = "";
			
			for(var x in row){
				if (rowData === "") {
					rowData = x + "=" + row[x];
				} else {
					rowData += ", " + x + "=" + row[x];
				}
			}
				  
			rowsStr = rowsStr + "{ " + rowData + " }" + "\n";
		}	
		
		return rowsStr;
	};
	
	this.dumpTilesTable = function(){
		if(appDb === null || appDb === undefined){
			throw "TileUtil.dumpTilesTable appDb should not be " + appDb;
		}
		
		TileUtil.dumpTableRows(appDb, "tiles");
	},
	
	this.dumpTilesWithRefCount = function(count){
		if(appDb === null || appDb === undefined){
			throw "TileUtil.dumpTilesWithRefCount appDb should not be " + appDb;
		}
		
		if(count === null || count === undefined){
			throw "TileUtil.dumpTilesWithRefCount count should not be " + count;
		}
		
		appDb.transaction(function(tx){
			var sql = "SELECT * FROM tiles WHERE ref_counter=?;";
			
			tx.executeSql(sql, [count], function(tx, res){
				console.log(TileUtil.rowsToString(res.rows));
			}, function(tx, e){
				console.log("TileUtil.dumpTilesWithRefCount ERROR", e);
			})
		}, function(e){
			console.log("TileUtil.dumpTilesWithRefCount ERROR", e);
		});
	},
	
	this.dumpTileIds = function(){
		console.log("---- TileUtil.dumpTileIds");
		
		if(projectDb === null || projectDb === undefined){
			throw "TileUtil.dumpTileIds projectDb should not be " + projectDb;
		}
		
		projectDb.transaction(function(tx){
			tx.executeSql("SELECT * FROM tileIds;", [], function(tx, res){
				console.log(TileUtil.rowsToString(res.rows));
			}, function(tx, e) {
				console.log("TileUtil.dumpTileIds ERROR", e);
			});	
		}, function(e) {
			console.log("TileUtil.dumpTileIds ERROR", e);
		});
	};
};