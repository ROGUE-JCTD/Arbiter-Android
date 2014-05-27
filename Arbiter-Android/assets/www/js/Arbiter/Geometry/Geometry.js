Arbiter.Geometry = (function(){
	var wktFormatter = new OpenLayers.Format.WKT();
	
	return {
		type: {
			POINT: 0,
			LINE: 1,
			POLYGON: 2,
			MULTIPOINT: 3,
			MULTILINE: 4,
			MULTIPOLYGON: 5,
			GEOMETRY: 6,
			MULTIGEOMETRY: 7
		},
		
		getGeometryName: function(type){
			
			var name = null;
			
			switch(type){
				case this.type.POINT:
					
					name = "Point";
					
					break;
					
				case this.type.LINE:
					
					name = "LineString";
					
					break;
					
				case this.type.POLYGON:
					
					name = "Polygon";
					
					break;
					
				case this.type.MULTIPOINT:
					
					name = "MultiPoint";
					
					break;
					
				case this.type.MULTILINE:
					
					name = "MultiLineString";
					
					break;
					
				case this.type.MULTIPOLYGON:
					
					name = "MultiPolygon";
					
					break;
					
				case this.type.GEOMETRY:
					
					name = "Geometry";
					
					break;
					
				case this.type.MULTIGEOMETRY:
					
					name = "MultiGeometry";
					
					break;
					
				default:
					
					throw "Geometry type '" + type + "' is not supported yet!";
			}
			
			return name;
		},
		
		/**
		 * Get the geometry type of a layer given the layer id
		 * @param {Number} layerId The id of the layer
		 */
		getGeometryType: function(layerId, geometryType){
			var schemas = Arbiter.getLayerSchemas();
			
			var type = geometryType;
				
			if(!Arbiter.Util.existsAndNotNull(type)){
				type = schemas[layerId].getGeometryType();
			}
			
			if(type === "Point"){
				return this.type.POINT;
			}
			
			if(type === "LineString"){
				return this.type.LINE;
			}
			
			if(type === "Curve"){
				return this.type.LINE;
			}
			
			if(type === "Polygon"){
				return this.type.POLYGON;
			}
			
			if(type === "Surface"){
				return this.type.POLYGON;
			}
			
			if(type === "MultiPoint"){
				return this.type.MULTIPOINT;
			}
			
			if(type === "MultiLineString"){
				return this.type.MULTILINE;
			}

			if(type === "MultiCurve"){
				if(Arbiter.WFS_DFT_VERSION === "1.1.0"){
					return this.type.MULTIGEOMETRY;
				}
				
				// wfsVersion 2.0.0
				return this.type.MULTILINE;	
			}
			
			if(type === "MultiPolygon"){
				return this.type.MULTIPOLYGON;
			}
			
			if(type === "MultiSurface"){
				return this.type.MULTIPOLYGON;
			}
			
			if(type === "Geometry"){
				return this.type.GEOMETRY;
			}
			
			if(type === "MultiGeometry"){
				return this.type.MULTIGEOMETRY;
			}
			
			throw "this.getGeometryType - "
				+ "Geometry type '" + type + "' is not "
				+ "supported yet!";
		},
		
		shouldBeMulti: function(partOfMulti, wkt){
			
			if(partOfMulti && (wkt.substring(0, 5) !== "MULTI")){
				return true;
			}
			
			return false;
		},
		
		convertToMulti: function(wkt){
			
			var openParens = wkt.indexOf("(");
			
			console.log("converToMulti old: " + wkt);
			
			var geometryType = wkt.substring(0, openParens);
			
			var geom = wkt.substring(openParens, wkt.length);
			
			// Surround with another set of parens
			geom = '(' + geom + ')';
			
			wkt = "MULTI" + geometryType + geom;
			
			console.log("convertToMulti new: " + wkt);
			
			return wkt;
		},
		
		isGeometryCollection: function(layerId){
			
			var schema = Arbiter.getLayerSchemas()[layerId];
			
			var geometryType = this.getGeometryType(layerId, schema.getGeometryType());
			
			return geometryType === this.type.MULTIGEOMETRY;
		},
		
		getNativeWKT: function(feature, layerId){
			
			var srid = Arbiter.Map.getMap().projection.projCode;
			
			var schema = Arbiter.getLayerSchemas()[layerId];
			
			var wkt = wktFormatter.write(Arbiter.Util.getFeatureInNativeProjection(srid, schema.getSRID(), feature));
			
			return wkt;
		},
		
		/**
		 * WKT returns a single feature for point, line, polygon, multipoint, multiline, multipolygon, but
		 * multiple features for a geometry collection, so if it's multiple features, return a single feature with a
		 * geometry collection
		 */
		readWKT: function(wkt){
			
			console.log("readWKT wkt = '" + wkt + "'");
			
			var features = wktFormatter.read(wkt);
			
			if(features.constructor != Array){
				return features;
			}
			
			var collection = new OpenLayers.Geometry.Collection();
			
			for(var i = 0; i < features.length; i++){
				collection.addComponents([features[i].geometry]);
			}
			
			features[0].geometry = collection;
			
			return features[0]; 
		},
		
		checkForGeometryCollection: function(layerId, arbiterId, nativeSRID){
			
			var wktGeometry = null;
			
			var features = Arbiter.Util.getFeaturesById(layerId, arbiterId);
			
			var schema = Arbiter.getLayerSchemas()[layerId];
			
			var geometryType = this.getGeometryType(layerId, schema.getGeometryType());
			
			// Geometry collection
			if(features.length > 1 && this.isGeometryCollection(layerId)){
				
				var srid = Arbiter.Map.getMap().projection.projCode;
				
				var geometryCollection = [];
				
				var featureInNativeProj = null;
				
				for(var i = 0; i < features.length; i++){
					featureInNativeProj = Arbiter.Util.getFeatureInNativeProjection(srid, nativeSRID, features[i]);
					
					geometryCollection.push(featureInNativeProj);
				}
				
				wktGeometry = wktFormatter.write(geometryCollection);
				
			// Single feature
			}else if(features.length === 1){
				
				wktGeometry = this.getNativeWKT(features[0], layerId);
			}
			
			return wktGeometry;
		}
	};
})();