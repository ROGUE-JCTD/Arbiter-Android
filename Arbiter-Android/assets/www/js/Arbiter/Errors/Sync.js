(function(){
	
	Arbiter.Error.Sync = {
		
		UNKNOWN_ERROR: 0,
		UPDATE_ERROR: 1,
		UNAUTHORIZED: 2,
		INTERNAL_SERVER_ERROR: 3,
		RESOURCE_NOT_FOUND: 4,
		TIMED_OUT: 5,
		ARBITER_ERROR: 6,
		MUST_COMPLETE_UPLOAD_FIRST: 7
	};
	
	Arbiter.Error.Sync.getErrorFromStatusCode = function(statusCode){
		var error = null;
		
		console.log("getErrorFromStatusCode: statusCode = " + statusCode);
		
		if(statusCode == 401){
			
			error = Arbiter.Error.Sync.UNAUTHORIZED;
		}else if(statusCode == 500){
			
			error = Arbiter.Error.Sync.INTERNAL_SERVER_ERROR;
		}else if(statusCode == 404){
			
			error = Arbiter.Error.Sync.RESOURCE_NOT_FOUND;
		}else{ // Bad gateway and gateway timeout... 
			
			error = Arbiter.Error.Sync.UNKNOWN_ERROR;
		}
		
		return error;
	};
})();