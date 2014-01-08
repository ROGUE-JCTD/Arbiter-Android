Arbiter.Util.Enumeration = function(){
	var enumeration = {};
	
	return {
		get: function(){
			return JSON.stringify(enumeration);
		},
		
		addEnumeration: function(_attributeName, _type, _enumeration){
			
			enumeration[_attributeName] = {
				"type": _type,
				"enumeration": _enumeration
			};
		}
	};
};