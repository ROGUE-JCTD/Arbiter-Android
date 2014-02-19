Arbiter.Util.Feature = (function(){
    var gmlReader = new OpenLayers.Format.GML.v2({
            extractAttributes: true
    });
    
    gmlReader.geometryTypes["OpenLayers.Geometry.Collection"] = "MultiGeometry";
    
    gmlReader.readers.gml["MultiGeometry"] = gmlReader.readers.gml["GeometryCollection"];
    
    return {
    	transformBounds: function(srid, bounds){
    		var olBounds = new OpenLayers.Bounds([bounds.getLeft(),
    		    bounds.getBottom(), bounds.getRight(), bounds.getTop()]);
    		
    		var newBounds = olBounds.transform(
    				new OpenLayers.Projection(bounds.getDefaultSRID()),
    				new OpenLayers.Projection(srid));
    		
    		return new Arbiter.Util.Bounds(
    				newBounds.left, 
    				newBounds.bottom, 
    				newBounds.right, 
    				newBounds.top);
    	},
    	
    	/**
    	 * schema is type Arbiter.Util.LayerSchema
    	 * bounds is type Arbiter.Util.Bounds
    	 */
        downloadFeatures: function(schema, bounds, 
        		encodedCredentials, onSuccess, onFailure){
        	
        	if(schema.getSRID() === null || schema.getSRID() === undefined){
        		if(Arbiter.Util.funcExists(onFailure)){
					onFailure();
				}
        		
        		return;
        	}
        	
            var srsNumberStr = schema.getSRID().substring(
            		schema.getSRID().indexOf(":") + 1);
            
            var transformedBounds = this.transformBounds(schema.getSRID(), bounds);
            
            var getFeatureRequest = 
                '<wfs:GetFeature service="WFS" version="1.0.0" outputFormat="GML2" ' +
                'xmlns:wfs="http://www.opengis.net/wfs" ' +
                'xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml" ' +
   
                    // TODO
                //NOTE: this data param is a hack to force the browser on ios 6.0 vs older versions 
                //                to believe it is a completely new request and so it doesn't respond to this getfeature
                //      with something it had previously cached. 
                'date="' + (new Date().getTime()) + '" ' +
                'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wfs ' +
                'http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd"> ' +

                '<wfs:Query typeName="' + schema.getPrefix() + ":" + schema.getFeatureType() + '">' +
                        '<ogc:Filter>' +
                                '<ogc:BBOX>' +
                                        '<ogc:PropertyName>' + schema.getGeometryName() + '</ogc:PropertyName>' +
                                            '<gml:Box srsName="http://www.opengis.net/gml/srs/epsg.xml#' + srsNumberStr + '">' +
                                                    '<gml:coordinates>' 
                                                    	+ transformedBounds.getLeft() + ',' 
                                                    	+ transformedBounds.getBottom() + ' '
                                                    	+ transformedBounds.getRight() + ','
                                                    	+ transformedBounds.getTop() + 
                                                    '</gml:coordinates>' +
                                            '</gml:Box>' +
                                '</ogc:BBOX>' +
                        '</ogc:Filter>' +
                '</wfs:Query>' +
            '</wfs:GetFeature>';
                
            console.log("getFeatureRequest: " + getFeatureRequest);
            
            var gotRequestBack = false;
            
            var request = new OpenLayers.Request.POST({
                url: schema.getUrl() + "/wfs",
                data: getFeatureRequest,
                headers: {
                        'Content-Type': 'text/xml;charset=utf-8',
                        'Authorization': 'Basic ' + encodedCredentials
                },
                success: function(response){
                	gotRequestBack = true;
                	
                    var features = gmlReader.read(response.responseText);
                    
                    console.log("GetFeature: ", features);
                    
                    if(Arbiter.Util.funcExists(onSuccess)){
                    	onSuccess.call(Arbiter.Util.Feature, 
                    		schema, features);
                    }
                },
                failure: function(response){
                	gotRequestBack = true;
                	
                	if(Arbiter.Util.funcExists(onFailure)){
    					onFailure();
    				}
                }
            });
            
            // Couldn't find a way to set timeout for an openlayers
    		// request, so I did this to abort the request after
    		// 15 seconds of not getting a response
    		window.setTimeout(function(){
    			if(!gotRequestBack){
    				request.abort();
    				
    				if(Arbiter.Util.funcExists(onFailure)){
    					onFailure();
    				}
    			}
    		}, 30000);
        }
    };
})();