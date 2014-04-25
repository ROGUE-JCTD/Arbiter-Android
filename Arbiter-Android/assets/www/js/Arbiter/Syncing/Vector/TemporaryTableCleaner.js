(function(){
	
	Arbiter.TemporaryTableCleaner = function(db, layers, schemas, onSuccess, onFailure){
		
		this.db = db;
		this.layers = layers;
		this.schemas = schemas;
		
		this.onCleanupSuccess = function(){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess();
			}
		};
		
		this.onCleanupFailure = function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		this._tx = null;
	};
	
	var prototype = Arbiter.TemporaryTableCleaner.prototype;
	
	prototype.cleanup = function(){
		var context = this;
		
		this.db.transaction(function(tx){
			context._tx = tx;
			
			context.removeTables();
		}, function(e){
			context.onCleanupFailure(e);
		});
	};
	
	prototype.removeTables = function(layerIndex){
		
		if(!Arbiter.Util.existsAndNotNull(layerIndex)){
			layerIndex = 0;
		}else if(layerIndex >= this.layers.length){
			
			this.onCleanupSuccess();
			
			return;
		}
		
		var context = this;
		
		var layer = this.layers[layerIndex];
		
		var schema = this.schemas[layer[Arbiter.LayersHelper.layerId()]];
		
		var sql = "DROP TABLE IF EXISTS '" + Arbiter.Util.getTempFeatureTableName(schema.getFeatureType()) + "';";
		
		this._tx.executeSql(sql, [], function(tx, res){
			
			context.removeTables(++layerIndex);
		}, function(tx, e){
			console.log("Failed to remove temporary table: " + e.stack);
			context.onCleanupFailure(e);
		});
	};
})();