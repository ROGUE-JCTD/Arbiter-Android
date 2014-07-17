(function(){
	
	Arbiter.DescribeFeatureType = function(url, credentials, wfsVersion, featureType, describeFeatureTypeReader, timeout){
		this.url = url;
		this.credentials = credentials;
		this.wfsVersion = wfsVersion;
		this.featureType = featureType;
		this.describeFeatureTypeReader = describeFeatureTypeReader;
		this.timeout = timeout;
	};
	
	var prototype = Arbiter.DescribeFeatureType.prototype;
	
	prototype.download = function(onSuccess, onFailure){
		var context = this;
		
		var gotRequestBack = false;
		
		var onDownloadFailure = function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		var url = this.url.substring(0, this.url.length - 4);
		
		var options = {
			url: url + "/wfs?service=wfs&version=" + context.wfsVersion + "&request=DescribeFeatureType&typeName=" + context.featureType,
			success: function(response){
				gotRequestBack = true;
				
				var results = response;
				
				if(Arbiter.Util.existsAndNotNull(context.describeFeatureTypeReader)){
					
					results = context.describeFeatureTypeReader.read(response.responseText);
				}
				
				if(Arbiter.Util.existsAndNotNull(onSuccess)){
					onSuccess(results);
				}
			},
			failure: function(response){
				gotRequestBack = true;
				
				onDownloadFailure(response);
			}
		};
		
		if(Arbiter.Util.existsAndNotNull(context.credentials)){
			options.headers = {
				Authorization: 'Basic ' + context.credentials
			};
		}
		
		var request = new OpenLayers.Request.GET(options);
		
		window.setTimeout(function(){
			if(!gotRequestBack){
				request.abort();
				
				onDownloadFailure("Timeout");
			}
		}, this.timeout);
	};
})();