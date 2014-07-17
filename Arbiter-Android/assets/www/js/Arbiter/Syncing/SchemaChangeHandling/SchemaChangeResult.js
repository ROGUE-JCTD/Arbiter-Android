(function(){
	
	Arbiter.SchemaChangeResult = function(id, featureType, serverId, checked, didChange, migrationSucceeded){
		this.id = id;
		this.featureType = featureType;
		this.serverId = serverId;
		this.checked = checked;
		this.didChange = didChange;
		this.migrationSucceeded = migrationSucceeded;
	};
	
	var prototype = Arbiter.SchemaChangeResult.prototype;
	
	
})();