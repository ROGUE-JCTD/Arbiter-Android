Arbiter.ProjectDbHelper = (function(){
	var PROJECT_DATABASE_NAME = "projectdb";
	
	var projectDb = null;
	
	var manager = null;
	
	return {
		getProjectDatabase: function(){
			if(projectDb === null){
				projectDb = sqlitePlugin.openDatabase(PROJECT_DATABASE_NAME);
				
				Arbiter.SQLiteTransactionManager.push(projectDb);
			}
			
			return projectDb;
		}
	};
})();