Arbiter.Util.TileUtil = {

debug: false,
debugProgress: false,
cacheTilesTest1Couter: 0,
counterCacheInProgressMax: 2,
androidClearWebCacheAfter: 15,

formatSuffixMap: {
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
},
		
dumpFiles: function() {
	console.log("---- TileUtil.dumpFiles");
	window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, 
		function(fs){
			try {
				 console.log("---- fs.root: " + fs.root.name);
				 var directoryReader = fs.root.createReader();
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
						Arbiter.error("dumpFiles.exception", err);
					}
				);
			} catch (e) {
				Arbiter.error("dumpFiles.exception", e);
			}
		}, 
		function(err) {
			Arbiter.error("failed to get fs (fileSystem)", err);
		}
	);	
},

dumpDirectory: function(dir) {
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
},


// start caching the cacheTile
startCachingTiles: function(successCallback) {
	console.log("---- startCachingTiles");
	
	if (typeof caching !== 'undefined') {
		Arbiter.warning("Tile Caching already in progress. Aborting new request");
		return;
	}
	
	Arbiter.setMessageOverlay(Arbiter.localizeString("Caching Tiles","label","cachingTiles"), Arbiter.localizeString("Queuing Request","label","queuingRequest"));
	
	var layer = map.baseLayer;

	caching = {
		// extent before we start caching	
		extentOriginal: map.getExtent(),
		zoomOriginal: map.zoom,
		// extent that we should use as starting point of caching
		extent: Arbiter.currentProject.aoi,
		startZoom: map.getZoomForExtent(Arbiter.currentProject.aoi, true),
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
	
	Arbiter.setMessageOverlay(Arbiter.localizeString("Caching Tiles","label","cachingTiles"), Arbiter.localizeString("Will Download {0} Tiles","label","willDownloadTiles").format(caching.counterMax));
	TileUtil.serviceCacheRequests();
},


checkCachingStatus: function(){
    if (caching.requestQueue.length === 0){
    	if (caching.counterCacheInProgress === 0){
    		TileUtil.cachingComplete();
    	}
    } else {
    	TileUtil.serviceCacheRequests();
    }
},


cachingComplete: function(){
    console.log("---- caching complete!");
    console.log(caching);
	
    if (caching.counterDownloadFailed > 0){
    	var msg = Arbiter.localizeString("Tile caching completed but {0} of {1} tiles failed to download. Recache if possible.","label","tilesFailed").format(caching.counterDownloadFailed, caching.counterMax);
		alert(msg);
		Arbiter.warning(msg);
	}
	
    var callback = caching.successCallback;
    
    
    // force the map to zoom to a different zoom level than we are about to go to so that it
    // 'refreshes' the map and pulls the tiles to display
    if (map.zoom === caching.zoomOriginal){
    	map.zoomTo((map.zoom + 1) < map.options.numZoomLevels ? map.zoom + 1: map.zoom - 1);
    }
    
	// we're done - go back to the view user had before they started caching
    //map.zoomTo();
    map.zoomToExtent(caching.extentOriginal, true);
    
    // keep for debugging
    cachingLast = caching;
	caching = undefined;
	
	// Note: caching var is removed before servicing call back as an issue in callback will
	// leave caching hanging around which will have serious implications 
	if (callback){
		callback();
	}
	
	console.log("---- finalized last line of TileUtil.cacheTiles!");
},

serviceCacheRequests: function(){
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
},

cacheTiles: function(successCallback, errorCallback){
	
	if (typeof caching !== 'undefined') {
		Arbiter.warning(Arbiter.localizeString("Tile Caching already in progress. Aborting new request.","label","cachingInProgress"));
		return;
	} 
	
	Arbiter.showMessageOverlay(Arbiter.localizeString("Caching Tiles","label","cachingTiles"));

	//-- make sure user wants to clear cache and then download tiles after 
	//   telling them how many tiles will be downloaded 
	var count = TileUtil.queueCacheRequests(Arbiter.currentProject.aoi, true /* only count the tiles do not queue yet */);
	var okay = confirm(Arbiter.localizeString("Clear cache and download {0} tiles?","label","clearCache").format(count));
	
	if (!okay) {
		console.log("-- cache cancelled by user");
		Arbiter.hideMessageOverlay();
		
		if (successCallback){
			successCallback();
		}
		
		return;
	}
	
	
	//-- continue with clearing cache and then re downloading tiles 
	TileUtil.clearCache("Arbiter/osm", function(){
		
		if (TileUtil.cacheTilesTest1Couter > 0) {
			console.log("~~~~ cacheTiles. done clear cache. starting testTilesTableIsEmpty MAKE sure there is only one project in arbiter!");
			TileUtil.testTilesTableIsEmpty(
				function(){
					console.log("---- cacheTiles.clearCache: success no tiles in Tiles table");
					
					// once all the cache for this project is cleared, start caching again. 
					TileUtil.startCachingTiles(
						function(){
							console.log("~~~~ done caching");
							Arbiter.hideMessageOverlay();
							
							if (successCallback){
								successCallback();
							}
						}
					);
				},
				function(){
					TileUtil.dumpTilesTable();
					Arbiter.error("----[ TEST FAILED: testTilesTableIsEmpty cacheTiles.clearCache: failed! Tiles Table not empty. just dumped tilestable");
					Arbiter.hideMessageOverlay();
	
					if (errorCallback){
						errorCallback();
					}
				}
			);
		} else {
			console.log("~~~~ cacheTiles, done clearing clearing cache. starting caching");
			// once all the cache for this project is cleared, start caching again. 
			TileUtil.startCachingTiles(
				function(){
					console.log("~~~~ done caching");
					Arbiter.hideMessageOverlay();
					
					if (successCallback){
						successCallback();
					}
				}
			);
		}
	});
},

testCacheTilesRepeatStart: function(millisec, maxRepeat){
	
	var onTimeout = function(){
		TileUtil.cacheTilesTest1Couter += 1;
		console.log("---[ cacheTilesTest1Couter: " + TileUtil.cacheTilesTest1Couter );
		
		if (typeof maxRepeat !== 'undefined') { 
			if (TileUtil.cacheTilesTest1Couter > maxRepeat) {
				testCacheTilesRepeatStop();
				alert("testCacheTilesRepeat completed");
			}
		}

		TileUtil.cacheTiles(null, function(){ 
			console.log("stoping test, testCacheTilesRepeatStop");
			TileUtil.testCacheTilesRepeatStop();
		});
	};
	
	cacheTilesTest1Timer = setInterval(onTimeout, millisec);
},

testCacheTilesRepeatStop: function(){
	clearTimeout(cacheTilesTest1Timer);
},

// make sure all ids in tileIds are in tiles table
testTileIdsTableIntegrity: function(){
},

// make sure entries in tiles table exist on disk
testTilesTableIntegrity: function(){
},

// make sure there are no entries in Tiles table
testTilesTableIsEmpty: function(_success, _error){
	Cordova.transaction(
		Arbiter.globalDatabase,
		"SELECT * FROM tiles;", 
		[], 
		function(tx, res){
			if (res.rows.length == 0){
				if (_success){
					_success();
				}
			} else {
				if (_error){
					_error();
				}
			}
		},
		function(e1, e2){
			Arbiter.error("testTilesTableIsEmpty. ", e1, e2);
		}
	);
},

// count how many png files are actually on file system
testPngTileCount: function(){
},

downloadTile: function(_url, _numTimes, _success, _error) {
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
				
			TileUtil.saveTile(_url, "Arbiter/osm", "15", "5199", "12123", "png", saveTileSuccessTest, saveTileErrorTest);
		};
		
		//Update databases
		TileUtil.addTile(_url, "Arbiter/osm", "15", "5199", "12123", "png", addTileCallbackTest, 1, 1000);
},

testTileDownload: function(_url, _numTimes) {
	
	//Get the baseLayer to cache
	var layer = map.baseLayer;

	//Create a caching variable to store information
	caching = {
		// extent before we start caching	
		extentOriginal: map.getExtent(),
		// extent that we should use as starting point of caching
		extent: Arbiter.currentProject.aoi,
		startZoom: map.getZoomForExtent(Arbiter.currentProject.aoi, true),
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
},

onUpdateCachingDownloadProgress: function(){

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
	Arbiter.setMessageOverlay(Arbiter.localizeString("Caching Tiles","label","cachingTiles"), Arbiter.localizeString("Downloaded: ","label","downloaded") + percent + "%");

	if (TileUtil.debugProgress) {
		console.log("onUpdateCachingDownloadProgress: " + percent + ". counterDownloaded: " + caching.counterDownloaded + ", counterMax: " + caching.counterMax);
	}
},

getURL: function(bounds) {
	var xyz = TileUtil.getXYZ(bounds, map.baseLayer);
    var ext = TileUtil.getLayerFormatExtension(this);
    
    // use the info we have to derive were the tile would be stored on the device
    var path = Arbiter.fileSystem.root.fullPath + "/" + "Arbiter/osm" +"/" + xyz.z + "/" + xyz.x + "/" + xyz.y + "." + ext;
 	return path;
},

getLayerFormatExtension: function(layer) {
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
},

queueCacheRequests: function(bounds, onlyCountTile) {

	// store current zoom since the function will change zoom level
	var currentZoom = map.zoom;
	
	var count = 0;
	for (var i=0; i < map.baseLayer.numZoomLevels; i++) {
		count += TileUtil.queueCacheRequestsForZoom(map.baseLayer, bounds, i, onlyCountTile);
	}

	return count;
},

queueCacheRequestsForZoom: function(layer, bounds, zoomLevel, onlyCountTile) {
	
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
},


queueCacheRequestsForZoomOpenLayers212: function(layer, bounds, zoomLevel, onlyCountTile) {
	
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
},

/**
 * Method: getXYZ
 * Calculates x, y and z for the given bounds. We need this to know where to store a tile regardless of it coming from an XYZ layer, WMTS, WMS, etc layer type
 * alternitavely can store all files in a flat directory but guessing that performance might be come an issue with several projects with thousands of tiles each
 */
getXYZ: function(bounds, layer, zoom) {
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
	
	
    if (layer.wrapDateLine) {
        var limit = Math.pow(2, z);
        x = ((x % limit) + limit) % limit;
    }

    return {'x': x, 'y': y, 'z': z};
},


getURLForXYZLayerOnly: function (layer, xyz) {
    var url = layer.url;
    if (OpenLayers.Util.isArray(url)) {
        var s = '' + xyz.x + xyz.y + xyz.z;
        url = layer.selectUrl(s, url);
    }
    
    return OpenLayers.String.format(url, xyz);
},

cacheTile: function(bounds, zoom){
	
    if (TileUtil.debug) {
    	console.log("--- TileUtil.cacheTile, bounds & zoom: ", bounds, zoom);
    }
    	
    var xyz = TileUtil.getXYZ(bounds, caching.layer, zoom);
	console.log("cacheTile 's xyz:", xyz.x, xyz.y, xyz.z);
    //alert("printed tile xyz. " + xyz);
	
	var url = '';

	// If the layer is an OSM (or XYZ) layer, don't call its getURL because it elies on the current 
	// zoom level of the map which we avoid settings since it results in more crashes on android
	if (caching.layer instanceof OpenLayers.Layer.XYZ) {
		url = TileUtil.getURLForXYZLayerOnly(caching.layer, xyz);
	} else {
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
	Arbiter.globalDatabase.transaction(function(tx){

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
				Arbiter.error("TileUtil.getURL: Multiple Entries for tile! see console for details. count: " + res.rows.length);
			}
			
			var ext = TileUtil.getLayerFormatExtension(caching.layer);
			
			var addTileCallback = function(){
	    		//TODO: save tile should rely on add tile!!! and if add tile fails, the whole thing fail?
	    		
				var saveTileSuccess = function(url, path){
					caching.counterCacheInProgress--;
					caching.counterDownloaded++;
					TileUtil.onUpdateCachingDownloadProgress();
					
					TileUtil.checkCachingStatus();
				};
				
				var saveTileError = function(url, path, err){
					caching.counterDownloadFailed++;
					caching.counterCacheInProgress--;
					TileUtil.onUpdateCachingDownloadProgress();
					Arbiter.warning("saveTileError filename: " + url + ", path: " + path, err);
					
					TileUtil.checkCachingStatus();
					//TODO: failed to download file and save to disk so just remove it from global.tiles and project.tileIds tables
					//TileUtil.removeTile(url, path);
				};
				
				// write the tile to device
				TileUtil.saveTile(url, "Arbiter/osm", xyz.z, xyz.x, xyz.y, ext, saveTileSuccess, saveTileError);
			};

			//TODO: get rid of tile ids in general and just store it as a json array in projectKeyValueDatabase?
			
			// add the tile to databases immediately so that if multiple getURL calls come in for a given tile, 
			// we do not download the tile multiple times
    		TileUtil.addTile(url, "Arbiter/osm", xyz.z, xyz.x, xyz.y, ext, addTileCallback, tileNewRefCounter, tileId);
		}, function(e1, e2) {
			Arbiter.error("chk27", e1, e2);
		});	
	}, function(e1, e2) {
		Arbiter.error("chk28", e1, e2);
	});
},


saveTile: function(fileUrl, tileset, z, x, y, ext, successCallback, errorCallback) {
	if (TileUtil.debug) {
		console.log("---- TileUtil.saveTile. tileset: " + tileset + ", z: " + z + ", x: " + x + ", y: " + y + ", url: " + fileUrl);
	}
	
	Arbiter.fileSystem.root.getDirectory(tileset, {create: true}, 
		function(tilesetDirEntry){
			//console.log("---- tilesetDirEntry: " + tilesetDirEntry.fullPath);
			tilesetDirEntry.getDirectory("" + z, {create: true}, 
				function(zDirEntry){
					//console.log("---- zDirEntry: " + zDirEntry.fullPath);
					zDirEntry.getDirectory("" + x, {create: true}, 
						function(xDirEntry){
							//console.log("---- xDirEntry: " + xDirEntry.fullPath);
							var filePath = xDirEntry.fullPath + "/" + y + "." + ext; 
							
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
									
									Arbiter.warning("Failed download or save file to: " + filePath, err);
									
									if (errorCallback){
										errorCallback(fileUrl, filePath, err);
									}
								}
							);
						}, function(e1, e2) {
							if (errorCallback){
								errorCallback(e1, e2);
							}
							Arbiter.error("chk29", e1, e2);
						}
					);
				}, function(e1, e2) {
					if (errorCallback){
						errorCallback(e1, e2);
					}
					Arbiter.error("chk30", e1, e2);
				}
			);
		}, function(e1, e2) {
			if (errorCallback){
				errorCallback(e1, e2);
			}
			Arbiter.error("chk31", e1, e2);
		}
	);

	return;
},

addTile: function(url, tileset, z, x, y, ext, successCallback, tileNewRefCounter, tileId) {
	
    if (TileUtil.debug) {
    	console.log("---- TileUtil.addTile: ", url, tileset, z, x, y, ext, tileNewRefCounter, tileId );
    }

	if (tileNewRefCounter === 1) {
		// alert("inserted tile. id: " + res.insertId);
		Arbiter.globalDatabase.transaction(
			function(tx) {
				var path = Arbiter.fileSystem.root.fullPath + "/" + tileset +"/" + z + "/" + x + "/" + y + "." + ext;

				var statement = "INSERT INTO tiles (tileset, z, x, y, path, url, ref_counter) VALUES (?, ?, ?, ?, ?, ?, ?);";
				tx.executeSql(statement, [ tileset, z, x, y, path, url, 1 ], function(tx, res) {
					
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
				}, function(e1, e2) {
					Arbiter.error("chk1", e1, e2);
				});
			}, function(e1, e2) {
				Arbiter.error("chk2", e1, e2);
			});

	} else if (tileNewRefCounter > 1) {
		//TEST NOTE only for testing single project
		//Arbiter.warning("only a warning if arbiter only has a single project cached. about to increment existing tiles refcounter for id: " + tileId);
		
		Arbiter.globalDatabase.transaction(function(tx) {

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
			}, function(e1, e2) {
				Arbiter.error("chk3", e1, e2);
			});
		}, function(e1, e2) {
			Arbiter.error("chk4", e1, e2);
		});

	} else {
		Arbiter.error("TileUtil.addTile tileNewRefCounter not >= 1: " + tileNewRefCounter);
	}
}, 

//clear entries in db, removed tiles from device
clearCache : function(tileset, successCallback, vDb) {
	if (TileUtil.debug) {
		console.log("---- TileUtil.clearCache");
	}	
	
	Arbiter.setMessageOverlay(Arbiter.localizeString("Caching Tiles","label","cachingTiles"), Arbiter.localizeString("Removing Tiles","label","removingTiles"));

	
	var op = function(tx){
		var sql = "SELECT id FROM tileIds;";
		tx.executeSql(sql, [], function(tx, res){
			
			if (res.rows.length > 0) {
			
				var removeCounter = 0;
				
				var removeCounterCallback = function() {
					removeCounter += 1;
					var percent = Math.round(removeCounter/res.rows.length * 100);
					Arbiter.setMessageOverlay(Arbiter.localizeString("Caching Tiles","label","cachingTiles"), Arbiter.localizeString("Removed: ","label","removed") + percent + "%");

					if (TileUtil.debug) {
						console.log("removeCounter: " + removeCounter + ", percent cleared: " + percent);
					}
					
					if (removeCounter === res.rows.length) {
						TileUtil.deleteTileIdsEntries(vDb, successCallback);
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

		}, function(e1, e2) {
			Arbiter.error("chk7", e1, e2);
		});
	};
	
	if (!vDb){
		vDb = Arbiter.currentProject.variablesDatabase;
	}
		
	//alert("inserted tile. id: " + res.insertId);
	vDb.transaction(op, function(e1, e2) {
		Arbiter.error("chk8", e1, e2);
	});		
}, 

deleteTileIdsEntries: function(vDb, successCallback){
	console.log("---- TileUtil.deleteTileIdsEntries");
	
	if (!vDb){
		vDb = Arbiter.currentProject.variablesDatabase;
	}
		
	// Remove everything from tileIds table
	vDb.transaction(function(tx){
		var statement = "DELETE FROM tileIds;";
		tx.executeSql(statement, [], function(tx, res){
			if (TileUtil.debug) {
				console.log("---- TileUtil.deleteTileIdsEntries done");
			}
			
			if (successCallback){
				successCallback();
			}
		}, function(e1, e2) {
			Arbiter.error("chk9", e1, e2);
		});					
	}, function(e1, e2) {
		Arbiter.error("chk10", e1, e2);
	});	
},

deleteTilesEntries: function(){
	console.log("---- TileUtil.deleteTileIdsEntries");

	// Remove everything from tiles table
	Cordova.transaction(
		Arbiter.globalDatabase,
		"DELETE FROM tiles;",
		[], 
		function(tx, res){
			if (TileUtil.debug) {
				console.log("---- TileUtil.deleteTilesEntries done");
			}
		}, 
		function(e1, e2) {
			Arbiter.error("chk101", e1, e2);
		}					
	);
},

deleteAllTileTableEntriesAndTheirPngFiles: function(){
	console.log("---- TileUtil.deleteAllTileTableEntriesAndTheirPngFiles");

	Cordova.transaction(
		Arbiter.globalDatabase,
		"SELECT * FROM tiles;", 
		[], 
		function(tx, res){
			console.log("deleting pngs mapped to entries. count:" + res.rows.length);
			
			for(var i = 0; i < res.rows.length; i++){
				var row = res.rows.item(i);
				TileUtil.removeTileFromDevice(row.path, row.id, null, function(){
					Arbiter.error("chk100");
				});
				
				TileUtil.deleteTilesEntries();
			}	
		},
		function(e1, e2){
			Arbiter.error("deleteAllTileTableEntriesAndTheirPngFiles. ", e1, e2);
		}
	);
},

insertIntoTileIds: function(id, successCallback) {
    if (TileUtil.debug) {
    	console.log("---- TileUtil.addToTileIds. id: " + id);
    }
    
    //var idsBeforeTx = TileUtil.tableToString(Arbiter.currentProject.variablesDatabase, "tileIds");
    //console.log("dumping before inserting id into tileIds. id: " + id);
    //TileUtil.dumpTileIds();
    
	Arbiter.currentProject.variablesDatabase.transaction(function(tx) {
		
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
		}, function(e1, e2) {
			console.log("dumping after error for id: " + id);
			//TileUtil.dumpTileIds();
			console.log("CHK: " + statement + ": " + id);
			Arbiter.error("chk11", e1, e2);
		});
	}, function(e1, e2) {
		Arbiter.error("chk12", e1, e2);
	});
},

removeTileFromDevice: function(path, id, successCallback, errorCallback){
	// remove tile from disk
	var newPath = path.replace(Arbiter.fileSystem.root.fullPath + '/','');
	Arbiter.fileSystem.root.getFile(newPath, {create: false},
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
				function(err){
					Arbiter.error("failed to delete tile from disk. tiles.id: " + id + ", path: " + path, err);
				}
			);
		}, 
		function(err){
			Arbiter.warning("get file from root failed. will assume success. tiles.id: " + id + ", path: " + path, err);
	
			if (successCallback){
				successCallback();
			}
		}
	);
},


/**
 * given a tileId, remove it from the project's tileIds table
 * then look in the global tiles table to decrement the reference counter
 * if counter is already only one, remove the entry from the global table
 * and delete the actual tile from the device. 
 */
removeTileById: function(id, successCallback, errorCallback, txProject, txGlobal) {
    if (TileUtil.debug) {
    	console.log("---- TileUtil.removeTileById: " + id);
    }
	
	//TODO: use txProject, txGlobal if provided
	
	Arbiter.globalDatabase.transaction(function(tx){
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

					Arbiter.globalDatabase.transaction(function(tx){

						var statement = "DELETE FROM tiles WHERE id=?;";
						tx.executeSql(statement, [id], function(tx, res){
	
							TileUtil.removeTileFromDevice(tileEntry.path, id, successCallback, errorCallback);
							
						}, function(e1, e2) {
							Arbiter.error("chk13", e1, e2);
						});
					}, function(e1, e2) {
						Arbiter.error("chk14", e1, e2);
					});							
					
				} else if (tileEntry.ref_counter > 1){
					Arbiter.globalDatabase.transaction(function(tx){
						// decrement ref_counter
						var statement = "UPDATE tiles SET ref_counter=? WHERE id=?;";
						tx.executeSql(statement, [(tileEntry.ref_counter - 1), id], function(tx, res){
						    
							if (TileUtil.debug) {
						    	console.log("-- decremented ref_counter to: " + (tileEntry.ref_counter - 1));
						    }
						    
							if (successCallback){
								successCallback();
							}
						}, function(e1, e2) {
							Arbiter.error("chk15", e1, e2);
						});
					}, function(e1, e2) {
						Arbiter.error("chk16", e1, e2);
					});	
					
				} else {
					Arbiter.error("Error: tileEntry.ref_counter <= 0 for id: " + id);
				}
				
			} else if (res.rows.length === 0){
				// should not happen
				Arbiter.warning("tile id from tileIds not in tiles table. Will return succes so the tileIds table gets flushed anyway. id: " + id);
				if (successCallback){
					successCallback();
				}
			} else {
				// should not happen
				Arbiter.error("tiles table has multiple entries for id: " + id);
			}
		}, function(e1, e2) {
			Arbiter.error("chk17", e1, e2);
		});
	}, function(e1, e2) {
		Arbiter.error("chk18", e1, e2);
	});	
	

}, 

dumpTableNames: function(database){
	console.log("---- TileUtil.dumpTable");
	database.transaction(function(tx){
		tx.executeSql("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;", [], function(tx, res){
			console.log(TileUtil.rowsToString(res.rows));
		}, function(e1, e2) {
			Arbiter.error("chk19", e1, e2);
		});	
	}, function(e1, e2) {
		Arbiter.error("chk20", e1, e2);
	});
},


dumpTableRows: function(database, tableName){
	console.log("---- TileUtil.dumpTableRows");
	database.transaction(function(tx){
		tx.executeSql("SELECT * FROM " + tableName + ";", [], function(tx, res){
			console.log(TileUtil.rowsToString(res.rows));
		}, function(e1, e2) {
			Arbiter.error("chk21", e1, e2);
		});	
	}, function(e1, e2) {
		Arbiter.error("chk22", e1, e2);
	});
},


dumpTableAttributes: function(database, tableName){
	console.log("---- TileUtil.dumpTableAttributes");

	// get the attributes of the layer
	var sql = "PRAGMA table_info (" + tableName + ");";

	Cordova.transaction(Arbiter.currentProject.dataDatabase, sql, [], function(tx, res) {
		var str = 'attribute count: ' + res.rows.length + '\n';
		for (var i=0; i<res.rows.length; i++) {
			var attr = res.rows.item(i);
			str += 'name: ' + attr.name + ', type: ' + attr.type + ', notnull: ' + attr.notnull + '\n';
		}
		
		console.log(str);
		
	}, Arbiter.error);
},

rowsToString: function(rows) {
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
},

dumpTilesTable: function(){
	TileUtil.dumpTableRows(Arbiter.globalDatabase, "tiles");
},

dumpTilesWithRefCount: function(count){
	console.log("---- TileUtil.dumpTilesWithRefCount");
	Arbiter.globalDatabase.transaction(
		function(tx){
			tx.executeSql(
				"SELECT * FROM tiles WHERE ref_counter=?;", 
				[count], 
				function(tx, res){
					console.log(TileUtil.rowsToString(res.rows));
				}, 
				function(e1, e2) {
					Arbiter.error("chk23", e1, e2);
				}
			);	
		}, 
		function(e1, e2) {
			Arbiter.error("chk24", e1, e2);
		}
	);
},

dumpTileIds: function(){
	console.log("---- TileUtil.dumpTileIds");
	Arbiter.currentProject.variablesDatabase.transaction(
		function(tx){
			tx.executeSql(
				"SELECT * FROM tileIds;", 
				[], 
				function(tx, res){
					console.log(TileUtil.rowsToString(res.rows));
				}, 
				function(e1, e2) {
					Arbiter.error("chk25", e1, e2);
				}
			);	
		}, 
		function(e1, e2) {
			Arbiter.error("chk26", e1, e2);
		}
	);
}

};
