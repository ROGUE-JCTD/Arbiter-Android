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
	
	var LayerSchema;
	
	if(arguments.length === 6){ // Downloaded from the interwebs...
		var _url = arguments[0];
		var _workspace = arguments[1];
		var _featureType = arguments[2];
		var _srid = arguments[3];
		var properties = arguments[4];
		var _serverId = arguments[5];
		
		LayerSchema = function(_url, _workspace, _featureType, _srid, properties, _serverId){
			var parsedFeatureType = Arbiter.Util.parseFeatureType(_featureType);
			featureType = parsedFeatureType.featureType;
			prefix = parsedFeatureType.prefix;
			
			editable = true;
			url = _url;
			workspace = _workspace;
			srid = _srid;
			attributes = [];
			enumeration = new Arbiter.Util.Enumeration();
			visibility = true;
			serverId = _serverId;
			
			var attribute = null;
			
			// Iterate through the properties adding 
			// them to the array of attributes for this featureType
			for(var i = 0; i < properties.length; i++){
				property = properties[i];
				
				if(property.type.indexOf("gml:") >= 0){
					geometryName = property.name;
					geometryType = property.type.substring(4, property.type.indexOf("PropertyType"));
				}else if(property.type.indexOf("xsd:") >= 0){
					attribute = new Arbiter.Util.Attribute(property.name,
							property.type.substr(4), property.nillable);
					
					attributes.push(attribute);
					
					if(property.restriction && property.restriction.enumeration){
						enumeration.addEnumeration(property.name, property.restriction);
					}
				}
			}
		};
		
		LayerSchema(_url, _workspace, _featureType, _srid, properties, _serverId);
	}else if(arguments.length === 11){ // Loaded from database
		var _url = arguments[0];
		var _workspace = arguments[1];
		var _prefix = arguments[2];
		var _featureType = arguments[3];
		var _srid = arguments[4];
		var _geometryName = arguments[5];
		var _geometryType = arguments[6];
		var _enumeration = arguments[7];
		var _attributes = arguments[8];
		var _visibility = arguments[9];
		var _serverId = arguments[10];
		
		LayerSchema = function(_url, _workspace, 
				_prefix, _featureType, _srid, 
				_geometryName, _geometryType,
				_enumeration, _attributes, 
				_visibility, _serverId){
			
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
		};
		
		LayerSchema(_url, _workspace, _prefix, _featureType, 
				_srid, _geometryName, _geometryType, 
				_enumeration, _attributes, _visibility, _serverId);
	}else if(arguments.length === 5){ // LayerGroup
		var _url = arguments[0];
		var _workspace = arguments[1];
		var _featureType = arguments[2];
		var _visibility = arguments[3];
		var _serverId = arguments[4];
		
		LayerSchema = function(_url, _workspace, 
				_featureType, _visibility, _serverId){
			var parsedFeatureType = Arbiter.Util.parseFeatureType(_featureType);
			
			workspace = _workspace;
			featureType = parsedFeatureType.featureType;
			prefix = parsedFeatureType.prefix;
			url = _url;
			editable = false;
			visibility = _visibility;
			serverId = _serverId;
		};
		
		LayerSchema(_url, _workspace, _featureType, _visibility, _serverId);
	}else{
		throw "LayerSchema: Invalid number of arguments - " + arguments.length;
	}
	
	
	return {
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
		
		isEditable: function(){
			return editable;
		}
		
	};
};