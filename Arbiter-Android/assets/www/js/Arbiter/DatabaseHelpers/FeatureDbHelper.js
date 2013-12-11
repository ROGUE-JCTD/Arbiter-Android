Arbiter.FeatureDbHelper = (function(){
	var FEATURE_DATABASE_NAME = "featuredb";
	
	var featureDb = null;
	
	return {
		getFeatureDatabase: function(){
			if(featureDb === null){
				featureDb = sqlitePlugin.openDatabase(FEATURE_DATABASE_NAME);
				
				Arbiter.SQLiteTransactionManager.push(featureDb);
			}
			
			return featureDb;
		}
	};
})();