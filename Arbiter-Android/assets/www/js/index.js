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
    
    setNewProjectsAOI: function(){
    	var bbox = Arbiter.Map.getCurrentExtent().toBBOX();
    	console.log("app.getAOI: ", bbox);
    	cordova.exec(null, null, "ArbiterCordova", "setNewProjectsAOI", [bbox]);
    },
    
    zoomToAOI: function(left, bottom, right, top){
    	Arbiter.Map.zoomToExtent(left, bottom, right, top);
    }
};

app.Init();