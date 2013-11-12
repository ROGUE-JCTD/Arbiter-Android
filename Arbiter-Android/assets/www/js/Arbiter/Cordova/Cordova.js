Arbiter.Cordova = (function() {
	
	return {

		/**
		 * Save the current maps extent
		 */
		resetWebApp : function(tx) {
			var bbox = Arbiter.Map.getCurrentExtent().toBBOX();
            var zoom = Arbiter.Map.getZoom();
			var reset = function(){
				console.log("resetWebApp");
				cordova.exec(null, null, "ArbiterCordova",
						"resetWebApp", [bbox, zoom]);
			};
			
			/*if(tx !== null && tx !== undefined){
				tx.commitCallback(Arbiter.Cordova, function(){
					console.log("SQLitePlugin COMMIT SUCCESS!");
					try{
						reset();
					} catch (_error){
						throw "Arbitet.Cordova.resetWebApp(): There's an error in reset()!!!" + _error;
					}
					
				}, function(){
					console.log("SQLitePlugin COMMIT FAILURE!!");
				});
			}else{*/
				reset();
			//}
		},
		
		// TODO: Right the native method and then call here
		updateFeatureData : function(featureType, features) {
			// make call to plugin passing the featureType and features
		},

		/**
		 * Need the AOI of the project for updating feature data after a
		 * successful push to the server.
		 */
		getProjectAOI : function(context, successCallback) {
			cordova.exec(function(left, bottom, right, top) {
				successCallback.call(context, left, bottom, right, top);
			}, function(error) {

			}, "ArbiterCordova", "getProjectsAOI", []);
		}
	};
})();