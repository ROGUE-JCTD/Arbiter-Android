Arbiter.Util.Enumeration = function(){
	var enumeration = "";

	return {
		get: function(){
			return enumeration;
		},
		
		addEnumeration: function(attributeName, restrictions){
			enumeration += attributeName + ":" + JSON.stringify(restrictions);
		}
	};
};