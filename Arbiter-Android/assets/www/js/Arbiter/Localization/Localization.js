Arbiter.Localization = (function() {
	
	var localizedStrings={
		    added:{
		        'en':'Added',
		        'es':'Añadido'
		    },
		    modified:{
		    	'en':'Modified',
		    	'es':'Modificado'
		    },
		    removed:{
		    	'en':'Removed',
		    	'es':'Eliminado'
		    },
		    feature:{
		    	'en':'feature',
		    	'es':'elemento'
		    },
		    features:{
		    	'en':'features',
		    	'es':'elementos'
		    },
		    viaArbiter:{
		    	'en':'via Arbiter',
		    	'es':'a través de Arbiter'
		    }
		};
	
	var locale = 'en';

	
	return {
		setLocale : function(_locale) {
			locale = _locale;
		},
		
		localize: function(key){
			return localizedStrings[key][locale];
		}
	};
})();