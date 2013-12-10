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
			
			Arbiter.FeatureTableHelper.removeFeatures([olFeature], schema, function(){
				console.log("removing features");
				olFeature.layer.removeFeatures([olFeature]);
			}, function(e){
				console.log("Error removing feature: ", e);
			});
		}
	};
};