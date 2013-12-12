Arbiter.Cordova.OOM_Workaround = (function(){
	
	var onMoveEnd = function(){
		var map = Arbiter.Map.getMap();
		var context = Arbiter.Cordova.OOM_Workaround;
		
		map.events.register("moveend", Arbiter.Cordova.OOM_Workaround, function(event) {
			if (context.tileCounter > context.RESET_ARBITER_ON) {
				Arbiter.Cordova.resetWebApp();
			}
		});
		
		map.events.register("addlayer", Arbiter.Cordova.OOM_Workaround, function(event){
			if(event && event.layer 
					&& event.layer.getURL){
				context.overrideGetURL(event.layer);
			}
		});
	};
	
	return {
		registered: false,
		
		OOM_Workaround: "oomWorkaround",
		
		tileCounter : 0,

		RESET_ARBITER_ON : 150,
		
		overrideGetURL : function(layer) {
			var context = this;
			
			if(!layer.metadata){
				layer.metadata = {};
			}
			
			layer.metadata[context.OOM_Workaround] = true;
			
			layer.getURL = function(bounds) {
				var url = Object.getPrototypeOf(this).getURL.call(this, bounds);
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