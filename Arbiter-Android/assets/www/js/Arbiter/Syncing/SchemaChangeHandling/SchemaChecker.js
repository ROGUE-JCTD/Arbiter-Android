(function(){
	
	/**
	 * @param {Arbiter.Util.LayerSchema} schema The layer schema to check
	 */
	Arbiter.SchemaChecker = function(schema, featureDb, fileSystem, wfsVersion){
		this.schema = schema;
		this.featureDb = featureDb;
		this.fileSystem = fileSystem;
		this.wfsVersion = wfsVersion;
		this._onCheckSuccess = null;
		this._onCheckFailure = null;
		
		this.featureType = this.schema.getFeatureTypeWithPrefix();
		
		var serverId = this.schema.getServerId();
		
		var server = Arbiter.Util.Servers.getServer(serverId);
		
		this.serverType = server.getType();
		
		this.url = server.getUrl();
		
		this.credentials = Arbiter.Util.getEncodedCredentials(
				server.getUsername(), 
				server.getPassword());
		
		this.describeFeatureTypeReader = new OpenLayers.Format.WFSDescribeFeatureType();
	};
	
	var prototype = Arbiter.SchemaChecker.prototype;
	
	prototype.checkForSchemaChange = function(onSuccess, onFailure){
		
		this._onCheckSuccess = function(didChange, migrationSuccess){
			
			if(Arbiter.Util.existsAndNotNull(onSuccess)){
				onSuccess(didChange, migrationSuccess);
			}
		};
		
		this._onCheckFailure = function(e){
			
			if(Arbiter.Util.existsAndNotNull(onFailure)){
				onFailure(e);
			}
		};
		
		this._downloadSchemaFile();
	};
	
	prototype._downloadSchemaFile = function(){
		
		var context = this;
		
		var describeFeatureType = new Arbiter.DescribeFeatureType(this.url, this.credentials,
				this.wfsVersion, this.featureType, null, 30000);
		
		describeFeatureType.download(function(results){
			
			context._checkForSchemaFile(results.responseText);
		}, function(e){
			
			context._onCheckFailure(e);
		});
	};
	
	/*
	 * If the schema file exists locally, then check to see
	 * if the files are different before trying to handle a change.
	 */
	prototype._checkForSchemaFile = function(results){
		
		var context = this;
		
		var schemaDir = new Arbiter.SchemaDir(this.fileSystem,
				this.schema.getServerId(), this.featureType);
		
		schemaDir.getDir(function(dir){
			
			var fileName = context.schema.getFeatureType() + ".xsd";
			
			var onFileExists = function(fileEntry){
				
				context._compareSchemaFiles(fileEntry, results, function(areEqual){
					
					if(!areEqual){
						
						console.log("aren't equal");
						
						context._performMigration(results, fileEntry);
					}else{
						
						console.log("are equal!");
						
						context._onCheckSuccess(false, true);
					}
				});
			};
			
			var onFileNotExists = function(fileError){
				
				if(fileError.code === FileError.NOT_FOUND_ERR){
					
					// It doesn't exist, so perform the migration.
					context._performMigration(results);
				}else{
					
					context._onCheckFailure(fileError);
				}
			};
			
			dir.dir.getFile(fileName, {create: true, exclusive: false}, onFileExists, onFileNotExists);
			
		}, function(e){
			
			context._onCheckFailure(e);
		});
	};
	
	prototype._compareSchemaFiles = function(fileEntry, downloadedResults, onComparison){
		
		var context = this;
		
		var fileReader = new FileReader();
		
		fileReader.onload = function(evt){
			
			onComparison(this.result == downloadedResults);
		};
		
		fileReader.onerror = this._onCheckFailure;
		
		fileEntry.file(function(file){
			
			fileReader.readAsText(file);
		}, this._onCheckFailure);
	};
	
	prototype._performMigration = function(downloadedResults, fileEntry){
		
		var context = this;
		
		var describeFeatureTypeResults = this.describeFeatureTypeReader.read(downloadedResults);
		
		var migration = new Arbiter.SchemaMigration(describeFeatureTypeResults, this.featureDb, this.schema);
		
		migration.migrate(function(){
			
			context._saveSchemaFile(downloadedResults, fileEntry);
		}, function(e){
			
			if(e === Arbiter.SchemaMigration.MIGRATION_FAILED){
				
				context._onCheckSuccess(true, false);
			}else{
				
				context._onCheckFailure(e);
			}
		});
	};
	
	prototype._saveSchemaFile = function(describeFeatureTypeResults, fileEntry){
		
		var context = this;
		
		fileEntry.createWriter(function(writer){
			
			writer.onwrite = function(){
				
				context._onCheckSuccess(true, true);
			};
			
			writer.onerror = context._onCheckFailure;
			
			writer.write(describeFeatureTypeResults);
		}, context._onCheckFailure);
	};
})();