Arbiter.Util.Attribute = function(_name, _type, _nillable) {
	var name = _name;
	var type = _type;
	var nillable = _nillable;
	
	return {
		getName : function() {
			return name;
		},

		getType : function() {
			return type;
		},

		isNillable : function() {
			return nillable;
		}
	};
};