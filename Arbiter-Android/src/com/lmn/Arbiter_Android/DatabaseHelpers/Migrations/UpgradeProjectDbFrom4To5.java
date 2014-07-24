package com.lmn.Arbiter_Android.DatabaseHelpers.Migrations;

import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FailedSync;

import android.database.sqlite.SQLiteDatabase;

public class UpgradeProjectDbFrom4To5 implements Migration{
	private String tempTableName;
	
	public UpgradeProjectDbFrom4To5(){
		
		this.tempTableName = "temporaryFailedSyncTable";
	}
	
	public void migrate(SQLiteDatabase db){
		
		createBackupTable(db);
		
		createTableWithNewSchema(db);
		
		moveDataOver(db);
		
		dropTemporaryTable(db);
	}
	
	private void createBackupTable(SQLiteDatabase db){
		
		String createBackupTable = "ALTER TABLE " + FailedSync.TABLE_NAME + " RENAME TO " + tempTableName + ";";
		
		db.execSQL(createBackupTable);
	}
	
	private void createTableWithNewSchema(SQLiteDatabase db){
		FailedSync.getHelper().createTable(db);
	}
	
	private void moveDataOver(SQLiteDatabase db){
		String fields = FailedSync._ID + "," +
				FailedSync.KEY + "," +
				FailedSync.DATA_TYPE + "," +
				FailedSync.LAYER_ID + "," +
				FailedSync.SYNC_TYPE;
		
		String moveDataOver = "INSERT INTO "+ FailedSync.TABLE_NAME 
				+ " (" + fields + ") SELECT " + fields + " FROM " + tempTableName;
		
		db.execSQL(moveDataOver);
	}
	
	private void dropTemporaryTable(SQLiteDatabase db){
		
		db.execSQL("DROP TABLE " + tempTableName);
	}
}
