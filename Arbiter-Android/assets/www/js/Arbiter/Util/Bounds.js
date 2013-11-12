Arbiter.Util.Bounds = function(_left, _bottom, _right, _top){
	var left = _left;
	var bottom = _bottom;
	var right = _right;
	var top = _top;
	
	var defaultSRID = "EPSG:900913";
	
	return {
		getLeft: function(){
			return left;
		}, 
		
		getBottom: function(){
			return bottom;
		},
		
		getRight: function(){
			return right;
		},
		
		getTop: function(){
			return top;
		},
		
		getDefaultSRID: function(){
			return defaultSRID;
		}
	};
};
