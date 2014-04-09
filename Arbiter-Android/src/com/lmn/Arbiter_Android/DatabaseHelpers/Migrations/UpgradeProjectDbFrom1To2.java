package com.lmn.Arbiter_Android.DatabaseHelpers.Migrations;

import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;

import android.database.sqlite.SQLiteDatabase;

public class UpgradeProjectDbFrom1To2 implements Migration{
	private String tempTableName;
	
	public UpgradeProjectDbFrom1To2(){
		
		this.tempTableName = "temporaryLayersTable";
	}
	
	public void migrate(SQLiteDatabase db){
		
		createBackupTable(db);
		
		createTableWithNewSchema(db);
		
		moveDataOver(db);
		
		dropTemporaryTable(db);
	}
	
	private void createBackupTable(SQLiteDatabase db){
		
		String createBackupTable = "ALTER TABLE " + LayersHelper.LAYERS_TABLE_NAME + " RENAME TO " + tempTableName + ";";
		
		db.execSQL(createBackupTable);
	}
	
	private void createTableWithNewSchema(SQLiteDatabase db){
		LayersHelper.getLayersHelper().createTable(db);
	}
	
	private void moveDataOver(SQLiteDatabase db){
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
	
	private void dropTemporaryTable(SQLiteDatabase db){
		
		db.execSQL("DROP TABLE " + tempTableName);
	}
}
