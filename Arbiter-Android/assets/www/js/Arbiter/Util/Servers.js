Arbiter.Util.Servers = (function(){
	var servers = {};
	
	return {
		getServer: function(key){
			return servers[key];
		},
		
		putServer: function(key, server){
			servers[key] = server;
		},
		
		resetServers: function(){
			servers = {};
		},
		
		deleteServer: function(key){
			delete servers[key];
		}
	};
})();