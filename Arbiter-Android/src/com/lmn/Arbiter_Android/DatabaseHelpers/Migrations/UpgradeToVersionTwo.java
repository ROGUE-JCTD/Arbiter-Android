package com.lmn.Arbiter_Android.DatabaseHelpers.Migrations;

import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;

import android.database.sqlite.SQLiteDatabase;

public class UpgradeToVersionTwo {
	private String tempTableName;
	private SQLiteDatabase db;
	
	public UpgradeToVersionTwo(SQLiteDatabase db, int oldVersion, int newVersion) throws DatabaseVersionException{
		
		if(oldVersion != 1){
			throw new DatabaseVersionException("Can't upgrade db from version: " + Integer.toString(oldVersion));
		}
		
		if(newVersion != 2){
			throw new DatabaseVersionException("Can't upgrade db to version: " + Integer.toString(newVersion));
		}
		
		this.tempTableName = "temporaryLayersTable";
		this.db = db;
	}
	
	public void upgrade(){
		
		createBackupTable();
		
		createTableWithNewSchema();
		
		moveDataOver();
		
		dropTemporaryTable();
	}
	
	private void createBackupTable(){
		
		String createBackupTable = "ALTER TABLE " + LayersHelper.LAYERS_TABLE_NAME + " RENAME TO " + tempTableName + ";";
		
		db.execSQL(createBackupTable);
	}
	
	private void createTableWithNewSchema(){
		LayersHelper.getLayersHelper().createTable(db);
	}
	
	private void moveDataOver(){
		String fields = LayersHelper._ID + "," +
				LayersHelper.LAYER_TITLE + "," +
				LayersHelper.FEATURE_TYPE + "," +
				LayersHelper.SERVER_ID + "," +
				LayersHelper.BOUNDING_BOX + "," +
				LayersHelper.COLOR + "," + 
				LayersHelper.LAYER_VISIBILITY + "," +
				LayersHelper.WORKSPACE;
		
		String moveDataOver = "INSERT INTO "+ LayersHelper.LAYERS_TABLE_NAME 
				+ " (" + fields + ") SELECT " + fields + " FROM " + tempTableName;
		
		db.execSQL(moveDataOver);
	}
	
	private void dropTemporaryTable(){
		
		db.execSQL("DROP TABLE " + tempTableName);
	}
}
