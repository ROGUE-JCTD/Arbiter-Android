var app = (function(){
	
	return {
			
		
		/**
		 * Initialize the app
		 */
	    Init: function() {
	        this.bindEvents();
	    },
	    
	    /**
	     * Bind event listeners
	     */
	    bindEvents: function() {
	        document.addEventListener('deviceready', this.onDeviceReady, false);
	    },
		
	    /**
	     * On device ready
	     */
	    onDeviceReady: function() {
	    	Arbiter.Init();
	    },
	    
	    /**
	     * Get the area of interest in the aoi map and call
	     * the native method to create the project with the aoi
	     */
	    setNewProjectsAOI: function(){
	    	var bbox = Arbiter.Map.getCurrentExtent().toBBOX();
	    	cordova.exec(null, null, "ArbiterCordova", "setNewProjectsAOI", [bbox]);
	    },
	    
	    zoomToAOI: function(left, bottom, right, top){
	    	Arbiter.Map.zoomToExtent(left, bottom, right, top);
	    },
	    
	    clearMap: function(){
	    	Arbiter.Map.Layers.removeAllLayers();
	    },
	    
	    loadMap: function(layers){
	    	this.clearMap();
	    	
	    	var protocol;
	    	var layer;
	    	
	    	for(var i = 0; i < layers.length; i++){
	    		
	    		layer = new OpenLayers.Layer.WMS(Arbiter.Map.Layers.getLayerName(layers[i].layerId, "wms"), layers[i].serverUrl + "/wms", 
	    				{
	    					layers: layers[i].featureType,
	    					transparent: true,
	    					format: "image/png"
	    					
	    				},
	    				{
	    					isBaseLayer: false,
	    					transitionEffect: 'resize',
	    					visibility: true
	    				});
	    		
	    		Arbiter.Map.Layers.addLayer(layer);
	    	}
	    	
	    	var map = Arbiter.Map.getMap();
	    	if(map.layers.length){
	        	Arbiter.Map.Layers.setNewBaseLayer(map.layers[0]);
	    	}
	    },
	    
	    removeLayer: function(layerId){
	    	Arbiter.Map.Layers.removeLayer(layerId);
	    }
	};
})();

app.Init();