package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class SyncTableHelper implements BaseColumns{
	
	public static final String TABLE_NAME = "syncs";
	public static final String TIMESTAMP = "timestamp";
	public static final String NOTIFICATIONS_ARE_SET = "notifications_are_set";
	
	private SQLiteDatabase db;
	
	public SyncTableHelper(SQLiteDatabase db){
		this.db = db;
	}
	
	public void createTable(){
		String sql = "CREATE TABLE " + TABLE_NAME + " (" 
				+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ TIMESTAMP + " TEXT, " 
				+ NOTIFICATIONS_ARE_SET + " BOOLEAN);";
		
		this.db.execSQL(sql);
	}
}
