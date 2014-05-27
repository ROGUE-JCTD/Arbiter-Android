(function(){
	
	Arbiter.Loaders.BaseLayer = function(){
		
	};
	
	var prototype = Arbiter.Loaders.BaseLayer.prototype;
	
	prototype.load = function(onSuccess, onFailure){
		
		var projectDb = Arbiter.ProjectDbHelper.getProjectDatabase();
		
		Arbiter.PreferencesHelper.get(projectDb, Arbiter.BASE_LAYER, this, function(baseLayer){
			
			if(Arbiter.Util.existsAndNotNull(baseLayer)){
				try{
					// base layer is stored as an array of json objects
					baseLayer = JSON.parse(baseLayer)[0];
				}catch(e){
					console.log(e.stack);
				}
			}else{
				var osm = "OpenStreetMap";
				
				baseLayer = {};
				
				baseLayer[Arbiter.BaseLayer.NAME] = "OpenStreetMap";
				baseLayer[Arbiter.BaseLayer.URL] = null;
				baseLayer[Arbiter.BaseLayer.SERVER_NAME] = "OpenStreetMap";
				baseLayer[Arbiter.BaseLayer.SERVER_ID] = "OpenStreetMap";
				baseLayer[Arbiter.BaseLayer.FEATURE_TYPE] = "";
			}
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess(baseLayer);
			}
		}, function(e){
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		});
	};
})();