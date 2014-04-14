(function(){
	
	Arbiter.Loaders.BaseLayer = function(){
		
	};
	
	var prototype = Arbiter.Loaders.BaseLayer.prototype;
	
	prototype.load = function(onSuccess, onFailure){
		Arbiter.PreferencesHelper.get(Arbiter.BASE_LAYER, this, function(baseLayer){
			
			console.log("loaded base layer = " + baseLayer);
			
			if(Arbiter.Util.existsAndNotNull(baseLayer)){
				try{
					// base layer is stored as an array of json objects
					baseLayer = JSON.parse(baseLayer)[0];
				}catch(e){
					console.log(e.stack);
				}
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