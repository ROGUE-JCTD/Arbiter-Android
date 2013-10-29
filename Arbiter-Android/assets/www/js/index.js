var app = {
			
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
    	
    	// Initialize Arbiter
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
    	var protocol;
    	var layer;
    	
    	for(var i = 0; i < layers.length; i++){
    		
    		layer = new OpenLayers.Layer.WMS(i, layers[i].serverUrl + "/wms", 
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
    }
    
    /*getLayerProtocol: function(layer){
    	console.log("getLayerProtocol", layer);
    	
    	var colonIndex = layer.featureType.indexOf(":");
    	var featureType = layer.featureType.substring(0, colonIndex);
    	
    	console.log("featureType: ", featureType);
    	
    	var protocol = new OpenLayers.Protocol.WFS({
    		version : "1.0.0",
    		url : layer.serverUrl,
    		srsName : layer.srs,
    		geometryName: layer.geomName,
    		featureType: featureType,
    		featureNS: layer.namespace
    	});
    	
    	return protocol;
    }*/
    
};

app.Init();