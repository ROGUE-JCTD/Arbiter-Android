Arbiter.Cordova.OOM_Workaround = (function(){
	
	var kitkat = "4.4";
	
	var onMoveEnd = function(){
		var map = Arbiter.Map.getMap();
		var context = Arbiter.Cordova.OOM_Workaround;
		
		map.events.register("moveend", Arbiter.Cordova.OOM_Workaround, function(event) {
			
			if ((device.version < kitkat) && context.tileCounter > context.RESET_ARBITER_ON) {
				Arbiter.Cordova.resetWebApp();
			}
		});
		
		map.events.register("addlayer", Arbiter.Cordova.OOM_Workaround, function(event){
			if((device.version < kitkat) && event && event.layer 
					&& event.layer.getURL){
				
				if(!Arbiter.Util.existsAndNotNull(event.layer.metadata)){
					event.layer.metadata = {};
				}
				
				var metadata = event.layer.metadata;
				
				if(!Arbiter.Util.existsAndNotNull(metadata[context.METADATA_KEY]) || !metadata[context.METADATA_KEY]){
					
					context.overrideGetURL(event.layer);
					
					metadata[context.METADATA_KEY] = true;
				}
			}
		});
	};
	
	return {
		registered: false,
		
		tileCounter : 0,

		RESET_ARBITER_ON : 150,
		
		METADATA_KEY: "OOM_OVERRIDEN",
		
		overrideGetURL : function(layer) {
			var context = this;
			
			var getURL = layer.getURL;
			
			layer.getURL = function(bounds) {
				var url = getURL.call(this, bounds);
				
				context.tileCounter++;
				
				return url;
			};
		},
		
		registerMapListeners : function() {
			onMoveEnd();
			
			this.registered = true;
		}
	};
})();