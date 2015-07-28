Arbiter.TileUtil = function(_projectDb, _map){
	var TileUtil = this;
	var projectDb = _projectDb;
	var map = _map;
	var METADATA_KEY = "TILE_UTIL_OVERRIDEN";
	var currentDatabase = null;
	var mbTilesDb = null;

	Arbiter.PreferencesHelper.get(projectDb, Arbiter.BASE_LAYER, this, function(baseLayer){

		// Helper function
		if(!String.prototype.startsWith){
		    String.prototype.startsWith = function (str) {
		        return !this.indexOf(str);
		    }
		}

		if(Arbiter.Util.existsAndNotNull(baseLayer)){
			try{
				// base layer is stored as an array of json objects
				baseLayer = JSON.parse(baseLayer)[0];
			}catch(e){
				console.log(e.stack);
			}

			var urlString = baseLayer.url;
			if (urlString.startsWith("file")){
				// Open Database
				currentDatabase = urlString.substring("file://TileSets/".length);

				mbTilesDb = sqlitePlugin.openDatabase(currentDatabase);
				Arbiter.SQLiteTransactionManager.push(mbTilesDb);
			} else if (urlString.startsWith("http")){
				// Use URL - taken care of in GetURL
			} else {
				// Use OpenStreetMap - also taken care of
			}
		}

	}, function(e){
		if(Arbiter.Util.funcExists(onFailure)){
			onFailure(e);
		}
	});

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

	var registerOnLayerAdded = function(){
		map.events.register("preaddlayer", TileUtil, function(event){
			if(event && event.layer && event.layer.getURL){
				
				if(!Arbiter.Util.existsAndNotNull(event.layer.metadata)){
					event.layer.metadata = {};
				}

				var metadata = event.layer.metadata;
				// If the key isn't there yet, or it's false
				if(!Arbiter.Util.existsAndNotNull(metadata[METADATA_KEY]) || !metadata[METADATA_KEY]){
					event.layer.getURL_Original = event.layer.getURL;
					event.layer.getURL = TileUtil.getURL;
					metadata[METADATA_KEY] = true;
				}
			}
		});
	};
	
	registerOnLayerAdded();

	this.getURL = function(bounds){
		var xyz = TileUtil.getXYZ(bounds, map.baseLayer);
		
	    var ext = TileUtil.getLayerFormatExtension(this);
	    
	    // use the info we have to derive where the tile would be stored on the device

		var path;
	    
	    if(Arbiter.hasAOIBeenSet() && Arbiter.Util.existsAndNotNull(this.metadata) && this.metadata.isBaseLayer
	    	&& Arbiter.Util.existsAndNotNull(mbTilesDb)){

			var i = 0;

	    	// Create fake path to store into HTML img src
	    	// Optimize to not use new Date()?
	    	path = "file:///" + "fake_" + xyz.z + "_" + xyz.x + "_" + xyz.y + "?_t=" + new Date().getTime();

			// Using mbTilesDb Database, extract data and store it into DOM src
			mbTilesDb.transaction(function(trans){
				var sql = "SELECT tile_data FROM tiles WHERE zoom_level=? AND tile_column=? AND tile_row=?;";

				// Access Zoom, Col, and Row
				var sqlData = [xyz.z, xyz.x, xyz.y];

				// Runs SQL Query
				trans.executeSql(sql, sqlData, function(tx, tileData){

					if (tileData.rows.length < 1){
						return;
					}

					// Success Case
					// The file was found inside the Database

					var Elements = document.querySelectorAll(".olTileImage");
					//TODO: This checks everything more than it probably should. Optimize?
					for (; i < Elements.length; i++) {
						var imgSrc = Elements[i].getAttribute("src");

						// Test for Data or Fake
						var FakeTest = imgSrc.charAt(0);

						// d for data, f for fake, if data, don't do anything with it
						if (FakeTest === "d"){
							continue;
						}

						// Test for Fake File before comparisons, if fake, replace with data
						if (FakeTest === "f"){
							var imgSrcParts = imgSrc.split(/[-_\s:/]+/);

							if (parseInt(imgSrcParts[2]) == xyz.z
							&& parseInt(imgSrcParts[3]) == xyz.x
							&& parseInt(imgSrcParts[4]) == xyz.y){

								// Replace the HTML img src with the tile data
                            	var rawTileData = "data:image/" + ext + ";base64," + tileData.rows.item(0).tile_data;

								Elements[i].setAttribute("src", rawTileData);

								// Sometimes would return olTileImage error, thinking it's a bad file.
								//    This lets it update & render out the replaced image data.
								Elements[i].className = "olTileImage";

								break;
							}
						}
					}
				  },
				  function(tx, error){
					 // Error Case
					 console.log("Error - Cannot find data in mbTilesDb with Error: " + error);
				  });

			   },
			function(trans, error){
			   console.log("GetURL - Something went wrong with transaction call. Error: " + error);
			});

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
};