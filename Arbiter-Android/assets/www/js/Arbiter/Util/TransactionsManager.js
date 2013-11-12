Arbiter.Util.TransactionsManager = function(){
	var transactions = [];
	var transactionCounter = 0;

	return {
		buildTransactions: function(){
			var transactionCounter = arguments.length;
			
			for(var i = 0; i < transactionCounter; i++){
				transactions.push(arguments[i]);
			}
		},
		
		execute: function(){
			for(var i = 0; i < transactions.length; i++){
				transactions[i](transactions)
			}
		}
	};
};

/*Arbiter.Util.TransactionManager.build(function(success){
	
	// Update the layers workspace in the Layers table.
	Arbiter.LayersHelper.updateLayer(featureType, content, context, success);
}, function(success){
	
	// After updating the layer workspace, 
	// add the layer to the GeometryColumns table
	Arbiter.GeometryColumnsHelper.addToGeometryColumns(schema, success);
}, function(success){
	
	// After adding the layer to the GeometryColumns table
	// create the feature table for the layer
	Arbiter.FeatureTableHelper.createFeatureTable(schema, success);
}, function(success){
	
	// After creating the feature table for the layer,
	// download the features from the layer
	context.downloadFeatures(schema, bounds, encodedCredentials, function(){
		
		// All the features have been downloaded and inserted
		// for this layer.  Increment the layerFinishedCount 
		incrementLayerFinishedCount();
		
		// If all the layers have finished downloading,
		// call the callback.
		if(doneGettingLayers()){
			_callback.call(context);
		}
	});
});*/