Arbiter.Util.LayerSchema = function(){
	var url = null;
	var attributes = null;
	var enumeration = null;
	var srid = null;
	var featureType = null;
	var geometryName = null;
	var geometryType = null;
	var workspace = null;
	var prefix = null;
	var editable = null;
	var visibility = null;
	var serverId = null;
	var mediaColumn = null;
	var layerId = null;
	var color = null;
	var serverType = null;
	var timeProperty = null;
	var isReadOnly = false;
	var LayerSchema;
	
	if(arguments.length === 10){ // Downloaded from the interwebs...
		var _layerId = arguments[0];
		var _url = arguments[1];
		var _workspace = arguments[2];
		var _featureType = arguments[3];
		var _srid = arguments[4];
		var properties = arguments[5];
		var _serverId = arguments[6];
		var _serverType = arguments[7]
		var _color = arguments[8];
		var _isReadOnly = arguments[9];
		
		LayerSchema = function(_layerId, _url, _workspace, _featureType, _srid, properties, _serverId, _serverType, _color, _isReadOnly){
			var parsedFeatureType = Arbiter.Util.parseFeatureType(_featureType);
			featureType = parsedFeatureType.featureType;
			prefix = parsedFeatureType.prefix;
			
			layerId = _layerId;
			editable = true;
			url = _url;
			workspace = _workspace;
			srid = _srid;
			attributes = [];
			enumeration = new Arbiter.Util.Enumeration();
			visibility = true;
			serverId = _serverId;
			serverType = _serverType;
			color = _color;
			isReadOnly = _isReadOnly;
			
			var attribute = null;
			
			// Iterate through the properties adding 
			// them to the array of attributes for this featureType
			for(var i = 0; i < properties.length; i++){
				property = properties[i];
				
				if(property.type.indexOf("gml:") >= 0){
					geometryName = property.name;
					geometryType = property.type.substring(4, property.type.indexOf("PropertyType"));
				}else if(property.type.indexOf("xsd:") >= 0){
					console.log("property", property);
					attribute = new Arbiter.Util.Attribute(property.name,
							property.type.substr(4), property.nillable);
					
					if(property.name === Arbiter.FeatureTableHelper.PHOTOS
							|| property.name === Arbiter.FeatureTableHelper.FOTOS){
						mediaColumn = property.name;
					}
					
					if(property.type === "xsd:dateTime"){
						
						timeProperty = {
							key: property.name,
							type: property.type
						};
					}else if(property.type === "xsd:time" && !Arbiter.Util.existsAndNotNull(timeProperty)){
						
						timeProperty = {
							key: property.name,
							type: property.type
						};
					}
					
					attributes.push(attribute);
					
					if(property.type === "xsd:boolean"){
						property.enumeration = ["true", "false"];
					}
					
					enumeration.addEnumeration(property.name,
							property.type, property.enumeration);
				}
			}
		};
		
		LayerSchema(_layerId, _url, _workspace, _featureType, _srid, properties, _serverId, _serverType, _color, _isReadOnly);
	}else if(arguments.length === 16){ // Loaded from database
		var _layerId = arguments[0];
		var _url = arguments[1];
		var _workspace = arguments[2];
		var _prefix = arguments[3];
		var _featureType = arguments[4];
		var _srid = arguments[5];
		var _geometryName = arguments[6];
		var _geometryType = arguments[7];
		var _enumeration = arguments[8];
		var _attributes = arguments[9];
		var _visibility = arguments[10];
		var _serverId = arguments[11];
		var _serverType = arguments[12];
		var _mediaColumn = arguments[13];
		var _color = arguments[14];
		var _isReadOnly = arguments[15];
		
		LayerSchema = function(_layerId, _url, _workspace, 
				_prefix, _featureType, _srid, 
				_geometryName, _geometryType,
				_enumeration, _attributes, 
				_visibility, _serverId, _serverType,
				_mediaColumn, _color, _isReadOnly){
			
			layerId = _layerId;
			editable = true;
			prefix = _prefix;
			workspace = _workspace;
			featureType = _featureType;
			url = _url;
			srid = _srid;
			attributes = _attributes;
			enumeration = _enumeration;
			geometryName = _geometryName;
			geometryType = _geometryType;
			visibility = _visibility;
			serverId = _serverId;
			serverType = _serverType;
			mediaColumn = _mediaColumn;
			color = _color;
			isReadOnly = _isReadOnly;
			
			var attribute = null;
			var type = null;
			
			for(var i = 0; i < attributes.length; i++){
				
				attribute = attributes[i];
				
				type = attribute.getType();
				
				if(type === "dateTime"){
					timeProperty = {
						key: attribute.getName(),
						type: "xsd:dateTime"
					};
					break;
				}else if(type === "time" && !Arbiter.Util.existsAndNotNull(timeProperty)){
					timeProperty = {
						key: attribute.getName(),
						type: "xsd:time"
					};
				}
			}
		};
		
		LayerSchema(_layerId, _url, _workspace, _prefix, _featureType, 
				_srid, _geometryName, _geometryType, 
				_enumeration, _attributes, _visibility,
				_serverId, _serverType, _mediaColumn, _color, _isReadOnly);
		
	}else if(arguments.length === 8){ // LayerGroup
		var _layerId = arguments[0];
		var _url = arguments[1];
		var _workspace = arguments[2];
		var _featureType = arguments[3];
		var _visibility = arguments[4];
		var _serverId = arguments[5];
		var _serverType = arguments[6];
		var _color = arguments[7];
		
		LayerSchema = function(_layerId, _url, _workspace, 
				_featureType, _visibility, _serverId, _serverType, _color){
			var parsedFeatureType = Arbiter.Util.parseFeatureType(_featureType);
			
			layerId = _layerId;
			workspace = _workspace;
			featureType = parsedFeatureType.featureType;
			prefix = parsedFeatureType.prefix;
			url = _url;
			editable = false;
			visibility = _visibility;
			serverId = _serverId;
			serverType = _serverType;
			color = _color;
		};
		
		LayerSchema(_layerId, _url, _workspace, _featureType,
				_visibility, _serverId, _serverType, _color);
		
	}else{
		throw "LayerSchema: Invalid number of arguments - " + arguments.length;
	}
	
	
	return {
		getLayerId: function(){
			return layerId;
		},
		
		getGeometryName: function(){
			return geometryName;
		},
		
		getGeometryType: function(){
			return geometryType;
		},
		
		getPrefix: function(){
			return prefix;
		},
		
		getFeatureType: function(){
			return featureType;
		},
		
		getWorkspace: function(){
			return workspace;
		},
		
		getSRID: function(){
			return srid;
		},
		
		getEnumeration: function(){
			return enumeration;
		},
		
		getAttributes: function(){
			return attributes;
		},
		
		getUrl: function(){
			return url;
		},
		
		isVisible: function(){
			return (visibility === "1") ? true : false;
		},
		
		getServerId: function(){
			return serverId;
		},
		
		getServerType: function(){
			return serverType;
		},
		
		isEditable: function(){
			return editable && (serverType !== "TMS");
		}, 
		
		getMediaColumn: function(){
			return mediaColumn;
		},
		
		getColor: function(){
			return color;
		},
		
		getTimeProperty: function(){
			return timeProperty;
		},
		
		isReadOnly: function(){
			return isReadOnly === "true" || isReadOnly === true;
		},
		
		setReadOnly: function(_isReadOnly){
			isReadOnly = _isReadOnly;
		}
	};
};
