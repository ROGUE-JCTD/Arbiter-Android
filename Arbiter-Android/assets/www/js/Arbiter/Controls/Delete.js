Arbiter.Controls.Delete = function(){
	
	var getLayerSchema = function(olFeature){
		var id = Arbiter.Util.getLayerId(olFeature.layer);
		
		return Arbiter.getLayerSchemas()[id];
	};
	
	return {
		deleteFeature: function(olFeature){
			var schema = getLayerSchema(olFeature);
			
			if(schema === undefined || schema === null){
				throw "Could not get schema for id = " + id;
			}
			
			olFeature.layer.removeFeatures([olFeature]);
		}
	};
};