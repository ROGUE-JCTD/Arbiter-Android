package com.lmn.Arbiter_Android.DatabaseHelpers.Migrations;

import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ServersHelper;

import android.database.sqlite.SQLiteDatabase;

public class UpgradeAppDbFrom3To4 implements Migration{

	private String tempTableName;
	
	public UpgradeAppDbFrom3To4(){
		
		this.tempTableName = "temporaryServersTable";
	}
	
	public void migrate(SQLiteDatabase db){
		
		createBackupTable(db);
		
		createTableWithNewSchema(db);
		
		moveDataOver(db);
		
		dropTemporaryTable(db);
	}
	
	private void createBackupTable(SQLiteDatabase db){
		
		String createBackupTable = "ALTER TABLE " + ServersHelper.SERVERS_TABLE_NAME + " RENAME TO " + tempTableName + ";";
		
		db.execSQL(createBackupTable);
	}
	
	private void createTableWithNewSchema(SQLiteDatabase db){
		ServersHelper.getServersHelper().createTable(db);
	}
	
	private void moveDataOver(SQLiteDatabase db){
		String fields = ServersHelper._ID + "," +
				ServersHelper.SERVER_NAME + "," +
				ServersHelper.SERVER_URL + "," +
				ServersHelper.SERVER_USERNAME + "," +
				ServersHelper.SERVER_PASSWORD + "," +
				ServersHelper.SERVER_TYPE;
		
		String moveDataOver = "INSERT INTO "+ ServersHelper.SERVERS_TABLE_NAME 
				+ " (" + fields + ") SELECT " + fields + " FROM " + tempTableName;
		
		db.execSQL(moveDataOver);
	}
	
	private void dropTemporaryTable(SQLiteDatabase db){
		
		db.execSQL("DROP TABLE " + tempTableName);
	}
}
