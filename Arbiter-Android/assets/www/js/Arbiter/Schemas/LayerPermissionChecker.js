(function(){
	
	Arbiter.LayerPermissionChecker = function(url, featureType, credentials){
		this.url = url;
		this.featureType = featureType;
		this.isReadOnly = false;
		this.credentials = credentials;
	};
	
	var prototype = Arbiter.LayerPermissionChecker.prototype;
	
	prototype.checkReadOnly = function(onSuccess, onFailure) {
		var context = this;
		
		var success = function(isReadOnly){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess(isReadOnly);
			}
		};
		
		var fail = function(e){
			
			console.log("Arbiter.LayerPermissionChecker Error checkingReadOnly", e);
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		var gotRequestBack = false;
		
		var url = this.url.substring(0, this.url.length - 4);
		
		var options = {
			url: url + "/wfs/WfsDispatcher",
			data: '<?xml version="1.0" encoding="UTF-8"?> ' +
            '<wfs:Transaction xmlns:wfs="http://www.opengis.net/wfs" ' +
            'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
            'service="WFS" version="1.0.0" ' +
            'xsi:schemaLocation="http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.0.0/wfs.xsd"> ' +
            '<wfs:Update xmlns:feature="http://www.geonode.org/" typeName="' +
            context.featureType + '">' +
            '<ogc:Filter xmlns:ogc="http://www.opengis.net/ogc">' +
            '<ogc:FeatureId fid="garbage_id" />' +
            '</ogc:Filter></wfs:Update>' +
            '</wfs:Transaction>',
			callback: function(response){
				
				gotRequestBack = true;
				
				var xml = response.responseXML;
				
				if(xml && xml.childNodes){
					
					var node = null;
					var reportNode = null;
					var exceptionNode = null;
					
					for(var i = 0; i < xml.childNodes.length; i++){
						
						node = xml.childNodes[i];
						
						// The only node we care about
						if(node.nodeName === "ServiceExceptionReport"){
							
							if(node.childNodes){
								
								// Run through the exceptions
								for(var j = 0; j < node.childNodes.length; j++){
									
									reportNode = node.childNodes[j];
									
									if(reportNode.nodeName === "ServiceException"){
										
										if(reportNode.childNodes){
											
											for(var k = 0; k < reportNode.childNodes.length; k++){
												
												exceptionNode = reportNode.childNodes[k];
												
												if(exceptionNode.nodeValue.indexOf('read-only') >= 0){
													
													context.isReadOnly = true;
													
													break;
												}
											}
										}
									}
								}
							}
							
							break;
						}
					}
				}
				
				context._saveReadOnly(success, fail);
			}
		};
		
		if(Arbiter.Util.existsAndNotNull(this.credentials)){
			context.headers = {
				Authorization: 'Basic ' + context.credentials
			};
		}
		
		var request = new OpenLayers.Request.POST(options);
		
		window.setTimeout(function(){
			if(!gotRequestBack){
				request.abort();
				
				Arbiter.Cordova.syncOperationTimedOut(function(){
					// Continue
					fail();
				}, function(){
					// Cancel
					fail(Arbiter.Error.Sync.TIMED_OUT);
				});
			}
		}, 30000);
	};

	prototype._saveReadOnly = function(onSuccess, onFailure){
		
		var context = this;
		
		var content = {};
		
		content[Arbiter.LayersHelper.readOnly()] = this.isReadOnly;
		
		Arbiter.LayersHelper.updateLayer(this.featureType, content, this, function(){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess(context.isReadOnly);
			}
		}, function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		});
	};
})();