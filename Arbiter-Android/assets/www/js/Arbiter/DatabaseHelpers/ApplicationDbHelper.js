Arbiter.ApplicationDbHelper = (function(){
	var APPLICATION_DATABASE_NAME = "appdb";
	
	var appDb = null;
	
	return {
		getDatabase: function(){
			if(appDb === null){
				appDb = sqlitePlugin.openDatabase(APPLICATION_DATABASE_NAME);
			}
			
			return appDb;
		}
	};
})();